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
@JavaScript({ "http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js", "http://code.highcharts.com/highcharts.js", "js_highchart.js"})
public class HighChart extends AbstractJavaScriptComponent{
	
	String data = "Categories,Apples,Pears,Oranges,Bananas\n" +
			          "John,8,4,6,5\n" +
			           "Jane,3,4,2,3\n" +
			            "Joe,86,76,79,77\n" +
			            "Janet,3,16,13,15\n";
	String title = "MyChart";
	String units = "MyUnits";
	
	
	public HighChart() {
		getState().data = data;
		getState().title = title;
		getState().units = units;
	}
	
	/* (non-Javadoc)
	 * @see com.vaadin.ui.AbstractJavaScriptComponent#getState()
	 */
	@Override
	protected HighChartState getState() {
		return (HighChartState) super.getState();
	}

}
