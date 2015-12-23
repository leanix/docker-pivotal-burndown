package net.leanix.pivotal.burndown;

import com.google.inject.Inject;
import com.sun.jersey.api.client.ClientResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.leanix.pivotal.burndown.api.ApiClient;
import net.leanix.pivotal.burndown.api.ApiException;
import net.leanix.pivotal.burndown.models.Iteration;
import net.leanix.pivotal.burndown.models.IterationHistory;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author berndschoenbach
 */
public class BusinessLogic {

    private final AppConfiguration configuration;
    private final String projectId;

    @Inject
    public BusinessLogic(AppConfiguration configuration) throws Exception {
        this.configuration = configuration;

        projectId = this.configuration.getPivotalProjectId();

        if (projectId == null || projectId.length() < 1) {
            throw new Exception("You need to provide a project ID");
        }
    }

    public void calculateBurndown() throws ApiException {
        Iteration iteration;

        if (configuration.getIteration().length() > 0) {
            iteration = new Iteration();
            iteration.setNumber(Integer.valueOf(configuration.getIteration()));
        } else {
            iteration = getCurrentIteration();
        }

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(configuration.getPivotalUrl());
        apiClient.addDefaultHeader("X-TrackerToken", configuration.getPivotalApiKey());

        ClientResponse historyResponse = apiClient.invokeApiGetCall("projects/" + projectId + "/history/iterations/" + iteration.getNumber() + "/days");
        IterationHistory history = historyResponse.getEntity(IterationHistory.class);
        ArrayList<HashMap<String, String>> pointMapping = getPointMapping(history);

        if (configuration.getTargetPath().length() > 0) {
            try {
                ChartCreator chartCreator = new ChartCreator(iteration, pointMapping);
                createImageFromChart(chartCreator, iteration);
            } catch (IOException ex) {
                Logger.getLogger(BusinessLogic.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String apiKey = configuration.getGeckoboardApiKey();
        String widgetKey = configuration.getGeckoboardWidgetKey();

        if (apiKey != null && widgetKey != null) {
            pushDataToGeckoBoardAsHighChart(pointMapping, apiKey, widgetKey);
        }
    }

    private Iteration getCurrentIteration() throws ApiException {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("https://www.pivotaltracker.com/services/v5/");
        apiClient.addDefaultHeader("X-TrackerToken", configuration.getPivotalApiKey());

        ClientResponse iterationResponse = apiClient.invokeApiGetCall("projects/" + projectId + "/iterations?scope=current");
        List<Iteration> iterations;
        iterations = (List<Iteration>) ApiClient.deserialize((String) iterationResponse.getEntity(String.class), "Array", Iteration.class);

        Iteration iteration = iterations.get(0);

        return iteration;
    }

    private ArrayList<HashMap<String, String>> getPointMapping(IterationHistory history) {
        ArrayList<String> header = history.getHeader();
        ArrayList<ArrayList<String>> data = history.getData();

        ArrayList<HashMap<String, String>> result = new ArrayList<>();
        for (ArrayList<String> currentDataPoint : data) {
            HashMap<String, String> dataMapping = new HashMap<>();
            Integer unfinishedPoints = 0;
            for (int i = 0; i < header.size(); i++) {
                switch (header.get(i)) {
                    case "points_delivered": // The fall through is intended
                    case "points_finished": // The fall through is intended
                    case "points_started": // The fall through is intended
                    case "points_rejected": // The fall through is intended
                    case "points_planned": // The fall through is intended
                    case "points_unstarted":
                        // The fall through is intended
                        unfinishedPoints += Math.round(Float.parseFloat(currentDataPoint.get(i)));
                    case "points_accepted":
                        // The fall through is intended
                        Integer acceptedPoints = Math.round(Float.parseFloat(currentDataPoint.get(i)));
                        dataMapping.put(header.get(i), acceptedPoints.toString());
                        break;
                    case "date":
                        dataMapping.put("formatted_date", getFormattedDate(currentDataPoint.get(i)));
                        dataMapping.put(header.get(i), currentDataPoint.get(i));
                        break;
                }
            }
            dataMapping.put("total_unfinished_points", unfinishedPoints.toString());
            result.add(dataMapping);
        }

        return result;
    }

    private void createImageFromChart(ChartCreator chartCreator, Iteration iteration) throws IOException {
        JFreeChart chart = chartCreator.getChart();
        BufferedImage objBufferedImage = chart.createBufferedImage(877, 620);
        ByteArrayOutputStream bas = new ByteArrayOutputStream();

        ImageIO.write(objBufferedImage, "png", bas);

        byte[] byteArray = bas.toByteArray();

        InputStream in = new ByteArrayInputStream(byteArray);
        BufferedImage image = ImageIO.read(in);
        File outputfile = new File(configuration.getTargetPath() + "burndown_" + iteration.getNumber() + ".png");
        ImageIO.write(image, "png", outputfile);
    }

    private String getFormattedDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-mm-dd");
            Date date = inputFormat.parse(dateString);

            Format outputFormat = new SimpleDateFormat("dd.mm.");
            return outputFormat.format(date);
        } catch (ParseException ex) {
            return "NAN";
        }
    }

    private void pushDataToGeckoBoardAsHighChart(ArrayList<HashMap<String, String>> pointMapping, String apiKey, String widgetId) throws ApiException {
        StringBuilder sb = new StringBuilder("{"
                + "\"api_key\": \"").append(apiKey).append("\","
                        + "\"data\": {"
                        + "\"highchart\": \"{");
        StringBuilder highChart = new StringBuilder(
                "chart: {renderTo: \\\"container\\\", type: \\\"area\\\", backgroundColor: \\\"transparent\\\"}, "
                + "plotOptions: {area: {marker: {enabled: false}}}, "
                + "credits: { enabled: false}, "
                + "title: {style: {color: \\\"#b9bbbb\\\"}, text: \\\"\\\"},"
                + "yAxis: { title: { enabled: false}, endOnTick: false}, legend: { itemStyle: { color: \\\"b9bbbb\\\"}, layout: \\\"vertical\\\", borderWidth: 0, enabled: false}, "
        );

        StringBuilder burnDownValues = new StringBuilder();
        StringBuilder acceptedPointValues = new StringBuilder();
        StringBuilder axisDescription = new StringBuilder();

        boolean firstRow = true;

        for (HashMap<String, String> point : pointMapping) {
            if (!firstRow) {
                burnDownValues.append(",");
                acceptedPointValues.append(",");
                axisDescription.append(",");
            }

            burnDownValues.append(point.get("total_unfinished_points"));
            acceptedPointValues.append(point.get("points_accepted"));
            axisDescription.append("\\\"").append(point.get("formatted_date")).append("\\\"");
            firstRow = false;
        }
        highChart.append("xAxis: { categories:[").append(axisDescription).append("]");
        highChart.append("},"
                + "series:[{type: \\\"column\\\",name: \\\"Accepted Points\\\",");
        highChart.append("data: [").append(acceptedPointValues).append("]"
                + "},");

        highChart.append("{name: \\\"Burndown\\\", type: \\\"area\\\", color: \\\"#D11111\\\", "
                + "data: [").append(burnDownValues).append("],"
                        + "marker: {lineWidth: 2}"
                        + "}"
                );
        highChart.append("]}");

        sb.append(highChart.toString()).append("\"}}");

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("https://push.geckoboard.com/v1/");
        apiClient.invokeApiPostCall("send/" + configuration.getGeckoboardWidgetKey(), sb.toString());
    }
}
