package net.leanix.pivotal.burndown;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import net.leanix.pivotal.burndown.models.Iteration;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author berndschoenbach
 */
public class ChartCreator {

    private JFreeChart chart;

    public ChartCreator(Iteration iteration, ArrayList<HashMap<String, String>> data) {
        CategoryDataset dataSet = createDataSet(data);
        chart = ChartFactory.createLineChart(
                "Burndown Diagram Sprint: " + iteration.getNumber(),
                "Date",
                "Points left",
                dataSet,
                PlotOrientation.VERTICAL,
                true,
                true,
                true
        );

        CategoryPlot plot = chart.getCategoryPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.lightGray);
        // sets paint color for each series
        renderer.setSeriesPaint(0, new Color(25, 138, 50));
        // sets thickness for series (using strokes)
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));

        plot.setRenderer(renderer);
    }

    private CategoryDataset createDataSet(ArrayList<HashMap<String, String>> data) {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (HashMap<String, String> currentPoint : data) {
            dataset.addValue(Integer.parseInt(currentPoint.get("total_unfinished_points")), "Unfinished Points", currentPoint.get("formatted_date"));
            dataset.addValue(Integer.parseInt(currentPoint.get("points_accepted")), "Accepted Points", currentPoint.get("formatted_date"));

        }

        return dataset;
    }

    public JFreeChart getChart() {
        return chart;
    }
}
