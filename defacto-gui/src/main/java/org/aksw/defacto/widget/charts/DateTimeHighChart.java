/**
 * 
 */
package org.aksw.defacto.widget.charts;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * @author Lorenz Buehmann
 *
 */
@JavaScript({ "http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js", "http://code.highcharts.com/highcharts.js", "js_datetime_highchart.js"})
public class DateTimeHighChart extends AbstractJavaScriptComponent{
	
	public DateTimeHighChart(String title, int startYear, int endYear) {
		getState().title = title;
		getState().startYear = startYear;
		getState().endYear = endYear;
	}
	
	/* (non-Javadoc)
	 * @see com.vaadin.ui.AbstractJavaScriptComponent#getState()
	 */
	@Override
	protected DateTimeHighChartState getState() {
		return (DateTimeHighChartState) super.getState();
	}

}
