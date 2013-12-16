/**
 * 
 */
package org.aksw.defacto;

import java.util.ArrayList;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.dussan.vaadin.dcharts.DCharts;
import org.dussan.vaadin.dcharts.base.elements.PointLabels;
import org.dussan.vaadin.dcharts.base.elements.XYaxis;
import org.dussan.vaadin.dcharts.data.DataSeries;
import org.dussan.vaadin.dcharts.metadata.XYaxes;
import org.dussan.vaadin.dcharts.metadata.directions.BarDirections;
import org.dussan.vaadin.dcharts.metadata.locations.PointLabelLocations;
import org.dussan.vaadin.dcharts.metadata.renderers.AxisRenderers;
import org.dussan.vaadin.dcharts.metadata.renderers.SeriesRenderers;
import org.dussan.vaadin.dcharts.metadata.ticks.TickFormatters;
import org.dussan.vaadin.dcharts.options.Axes;
import org.dussan.vaadin.dcharts.options.Options;
import org.dussan.vaadin.dcharts.options.SeriesDefaults;
import org.dussan.vaadin.dcharts.renderers.series.BarRenderer;

/**
 * @author Lorenz Buehmann
 *
 */
public class ScoreChartGenerator {
	
	/**
	 * Create a horizontal bar chart for the scores of the given website.
	 * @param evidence
	 * @param webSite
	 * @return
	 */
	public DCharts generateChart(Evidence evidence, WebSite webSite) {
		//the score for the website
		double score = webSite.getScore();
		
		//the page rank score
		double pageRankScore = webSite.getPageRankScore();
		
		//TM web score
		double tmWebScore = webSite.getTopicMajorityWebFeature();
		
		//TM search score
		double tmSearchScore = webSite.getTopicMajoritySearchFeature();
		
		//topic coverage
		double topicCoverage = webSite.getTopicCoverageScore();
		
		
		DataSeries dataSeries = new DataSeries();
		dataSeries.newSeries()
					.add(topicCoverage, "Topic Coverage")
					.add(tmSearchScore, "TM in Search")
					.add(tmWebScore, "TM in Web")
//					.add(pageRankScore, "PageRank Score")
					.add(score, "DeFacto Score");					;
					

		//set horizontal bars
		SeriesDefaults seriesDefaults = new SeriesDefaults()
				.setRenderer(SeriesRenderers.BAR)
				.setPointLabels(
						new PointLabels()
						.setShow(true)
						.setLocation(PointLabelLocations.EAST)
						.setEdgeTolerance(-15)
						.setFormatter(TickFormatters.PERCENT)
//						.setLabels("50%", "50%", "50%", "50%", "50%")
				)
				.setShadowAngle(135)
				.setRendererOptions(
						new BarRenderer()
						.setBarDirection(BarDirections.HOTIZONTAL)
						.setBarWidth(10)
						.setVaryBarColor(true)
						);

		//define axes
		Axes axes = new Axes()
		.addAxis(
				new XYaxis(XYaxes.Y)
				.setRenderer(AxisRenderers.CATEGORY)
				)
		.addAxis(
				new XYaxis(XYaxes.X)
				.setMax(1)
				.setDrawMajorGridlines(false)
				//.setTicks(new Ticks().add("0", "25", "50", "75", "100"))
				);

		Options options = new Options().setSeriesDefaults(seriesDefaults).setAxes(axes);

		DCharts chart = new DCharts().setDataSeries(dataSeries).setOptions(options).show();
		return chart;
	}
	
	/**
     * Calculates the maximum value of topic majority in the web, as this value has no upper bound, so we will
     * use that value to normalize the values of all that feature for all websites, so all resulting values, e.g. topic
     * coverage and others can fit in the same chart
     * @param lstWebSites   A list of websites
     * @return  The maximum topic majority in the web in that list
     */
    private double calculateMaximumTopicMajorityInTheWeb(ArrayList<WebSite> lstWebSites) {
        double maxTopicMajorityInThWeb = 0;

        if(lstWebSites.size() <= 0)
            return 0;

        for(WebSite website:lstWebSites){
            if(website.getTopicMajorityWebFeature() > maxTopicMajorityInThWeb)
                maxTopicMajorityInThWeb = website.getTopicMajorityWebFeature();
        }
        return maxTopicMajorityInThWeb;
    }

}
