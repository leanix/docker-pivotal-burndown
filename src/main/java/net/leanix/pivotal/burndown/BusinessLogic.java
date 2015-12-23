package net.leanix.pivotal.burndown;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.leanix.pivotal.burndown.api.ApiClient;
import net.leanix.pivotal.burndown.api.ApiException;
import net.leanix.pivotal.burndown.dao.PivotalTrackerDao;
import net.leanix.pivotal.burndown.models.Iteration;
import net.leanix.pivotal.burndown.models.IterationHistory;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author berndschoenbach
 */
public class BusinessLogic {

    private final String projectId;

    public BusinessLogic() throws Exception {

        projectId = AppConfiguration.getPivotalProjectId();

        if (projectId == null || projectId.length() < 1) {
            throw new Exception("You need to provide a project ID");
        }
    }

    /**
     * Calculates the burndown diagramm calls the functions to create the chart
     * and/or the image.
     *
     * @throws ApiException
     */
    public void calculateBurndown() throws ApiException, Exception {
        Iteration iteration;

        if (AppConfiguration.getIteration().length() > 0) {
            iteration = PivotalTrackerDao.getIterationByNumber(AppConfiguration.getIteration(), projectId);
        } else {
            iteration = PivotalTrackerDao.getCurrentIteration(projectId);
        }

        ArrayList<HashMap<String, String>> pointMapping = getPointMapping(iteration);

        if (AppConfiguration.getTargetPath().length() > 0) {
            try {
                ChartCreator chartCreator = new ChartCreator(iteration, pointMapping);
                createImageFromChart(chartCreator, iteration);
            } catch (IOException ex) {
                Logger.getLogger(BusinessLogic.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        pushDataToGeckoBoardAsHighChart(pointMapping, iteration);
    }

    /**
     * Creates a mapping needed for the burndown chart since Pivotal Tracker
     * does not return this natively.
     *
     * @param the history for the current iteration by days
     *
     * @return the mapped data points having three elements per data point,
     * points_accepted, formatted_date and date
     */
    private ArrayList<HashMap<String, String>> getPointMapping(Iteration iteration) throws ApiException {
        IterationHistory history = PivotalTrackerDao.getIterationHistory(projectId, iteration);

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
                        unfinishedPoints += Math.round(Float.parseFloat(currentDataPoint.get(i)));
                    case "points_accepted":
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

    /**
     * Creates a png image from the given chart.
     *
     * @param the factory with the created chart.
     * @param the iteration for the given chart
     *
     * @throws IOException If file writing or reading fails
     */
    private void createImageFromChart(ChartCreator chartCreator, Iteration iteration) throws IOException {
        JFreeChart chart = chartCreator.getChart();
        BufferedImage objBufferedImage = chart.createBufferedImage(877, 620);
        ByteArrayOutputStream bas = new ByteArrayOutputStream();

        ImageIO.write(objBufferedImage, "png", bas);

        byte[] byteArray = bas.toByteArray();

        InputStream in = new ByteArrayInputStream(byteArray);
        BufferedImage image = ImageIO.read(in);
        File outputfile = new File(AppConfiguration.getTargetPath() + "burndown_" + iteration.getNumber() + ".png");
        ImageIO.write(image, "png", outputfile);
    }

    /**
     * Formats the date string in format yyyy-mm-dd to a date string in format
     * dd.mm.
     *
     * @param The date string in format yyyy-mm-dd
     *
     * @return the formatted date. will be an emnpty string if a parse exception
     * occurred during date parsing.
     */
    private String getFormattedDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-mm-dd");
            Date date = inputFormat.parse(dateString);

            Format outputFormat = new SimpleDateFormat("dd.mm.");
            return outputFormat.format(date);
        } catch (ParseException ex) {
            Logger.getLogger(BusinessLogic.class.getName()).log(Level.WARNING, "Date could not be paresed", ex);
            return "";
        }
    }

    /**
     * Pushes the given point mapping data to a highcharts widget on Geckoboard.
     *
     * @param The data to push
     * @param The API-Key to access the geckoboard
     * @param The ID of the highcharts widget
     * @throws ApiException
     */
    private void pushDataToGeckoBoardAsHighChart(ArrayList<HashMap<String, String>> pointMapping, Iteration iteration) throws ApiException {
        String apiKey = AppConfiguration.getGeckoboardApiKey();
        String widgetKey = AppConfiguration.getGeckoboardWidgetKey();

        if (apiKey == null || widgetKey == null) {
            return;
        }

        StringBuilder sb = new StringBuilder("{"
                + "\"api_key\": \"").append(apiKey).append("\","
                        + "\"data\": {"
                        + "\"highchart\": \"{");
        StringBuilder highChart = new StringBuilder(
                "chart: {renderTo: \\\"container\\\", type: \\\"area\\\", backgroundColor: \\\"transparent\\\"}, "
                + "plotOptions: {area: {marker: {enabled: false}}}, "
                + "credits: { enabled: false}, "
                + "title: {style: {color: \\\"#b9bbbb\\\"}, text: \\\"\\\"},"
                + "yAxis: { title: { enabled: false}, endOnTick: false}, legend: { itemStyle: { color: \\\"#c0c0c0\\\"}, borderWidth: 0, enabled: true}, "
        );

        Integer datesToShow = addDateAxisDescription(iteration, highChart);

        StringBuilder burnDownValues = new StringBuilder();
        StringBuilder acceptedPointValues = new StringBuilder();

        addSeriesValues(pointMapping, burnDownValues, acceptedPointValues, datesToShow);

        highChart.append("},");
        appendSeriesData(highChart, acceptedPointValues, burnDownValues);

        sb.append(highChart.toString()).append("\"}}");

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("https://push.geckoboard.com/v1/");
        apiClient.invokeApiPostCall("send/" + widgetKey, sb.toString());
    }

    /**
     * Appends the series data to the POST body.
     *
     * @param the POST body
     * @param the values for the accepted points
     * @param the values for the burndown chart
     */
    private void appendSeriesData(StringBuilder highChart, StringBuilder acceptedPointValues, StringBuilder burnDownValues) {
        String displayType = AppConfiguration.getDisplayType();
        highChart.append("series:[");
        if (displayType.equals("accepted_points") || displayType.equals("both")) {
            highChart.append("{type: \\\"column\\\",name: \\\"Accepted Points\\\",");
            highChart.append("data: [").append(acceptedPointValues).append("]}");
        }

        if (displayType.equals("both")) {
            highChart.append(",");
        }

        if (displayType.equals("burndown") || displayType.equals("both")) {
            highChart.append("{name: \\\"Burndown\\\", type: \\\"area\\\", color: \\\"#D11111\\\", "
                    + "data: [").append(burnDownValues).append("],"
                            + "marker: {lineWidth: 0}"
                            + "}"
                    );
        }
        highChart.append("]}");
    }

    /**
     * Adds the valuse for each series.
     *
     * If not enough data for the serieses exist the values will be filled with
     * nulls.
     *
     * @param the point data to add
     * @param the string builder instance which represents the burndown values
     * @param the string builder instance which represents the accepted point
     * values
     * @param the number of dates the data points should represent
     */
    private void addSeriesValues(ArrayList<HashMap<String, String>> pointMapping, StringBuilder burnDownValues, StringBuilder acceptedPointValues, Integer datesToShow) {
        boolean firstRow = true;
        Integer datesShown = 0;

        for (HashMap<String, String> point : pointMapping) {
            if (!firstRow) {
                burnDownValues.append(",");
                acceptedPointValues.append(",");
            }

            burnDownValues.append(point.get("total_unfinished_points"));
            acceptedPointValues.append(point.get("points_accepted"));
            firstRow = false;
            datesShown++;
        }

        if (datesToShow > datesShown) {
            for (int i = 0; i < (datesToShow - datesShown); i++) {
                burnDownValues.append(",").append("null");
                acceptedPointValues.append(",").append("null");
            }
        }
    }

    /**
     * Adds the description labels for the X-Axis.
     *
     * @param the iteration to add the axis data for
     * @param the POST data the axis description should be added to
     *
     * @return the numnber of dates set for the description
     */
    private Integer addDateAxisDescription(Iteration iteration, StringBuilder highChart) {
        StringBuilder axisDescription = new StringBuilder();

        ZonedDateTime start = iteration.getStart().withZoneSameInstant(ZoneId.of("CET"));
        ZonedDateTime end = iteration.getFinish().withZoneSameInstant(ZoneId.of("CET"));
        List<String> dateList = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM");

        while (!start.plusDays(1).isAfter(end)) {
            dateList.add(start.format(dateFormatter));
            start = start.plusDays(1);
        }

        dateList.stream().forEach((currentDate) -> {
            if (axisDescription.length() > 0) {
                axisDescription.append(",");
            }

            axisDescription.append("\\\"").append(currentDate).append("\\\"");
        });

        highChart.append("xAxis: { categories:[").append(axisDescription).append("], minTickInterval: 2");

        return dateList.size();
    }
}
