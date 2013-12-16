/**
 * 
 */
package org.aksw.defacto.widget;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.aksw.defacto.ScoreChartGenerator;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoTimePeriod;
import org.aksw.defacto.util.EvidenceRDFGenerator;
import org.dussan.vaadin.dcharts.DCharts;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Triple;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

/**
 * @author Lorenz Buehmann
 *
 */
public class ResultsPanel extends VerticalLayout{
	
	private ScoreChartGenerator chartGenerator = new ScoreChartGenerator();
	private final NumberFormat nf = DecimalFormat.getPercentInstance(Locale.ENGLISH);
	private final DateFormat df = new SimpleDateFormat("yyyy");
	private Calendar startTime;
	private Calendar endTime;
	
	public ResultsPanel() {
		addStyleName("result-panel");
		setSizeFull();
	}
	
	public void showResults(final Triple triple, final Evidence evidence, Calendar startTime, Calendar endTime){
		this.startTime = startTime;
		this.endTime = endTime;
		removeAllComponents();
		//show some information about the total number of websites, etc.
//		Label totalScore = new Label("<h2>Overall DeFacto Score: " + nf.format(evidence.getDeFactoScore()) + "</h2>", ContentMode.HTML);
//		totalScore.setWidth(null);
//		addComponent(totalScore);
//		setComponentAlignment(totalScore, Alignment.MIDDLE_CENTER);
//		
//		//show time information if exist
//		if(evidence.defactoTimePeriod != null){
//			try {
//				Component timeLine = createTimelineComponent(evidence.defactoTimePeriod);
//				timeLine.setWidth(null);
//				addComponent(timeLine);
//				setComponentAlignment(timeLine, Alignment.MIDDLE_CENTER);
//			} catch (ParseException e) {
//				e.printStackTrace();
//			}
//		}
		String overallResult = "Overall DeFacto Score: " + nf.format(evidence.getDeFactoScore());
		if(evidence.defactoTimePeriod != null){
			DefactoTimePeriod timePeriod = evidence.defactoTimePeriod;
			if(timePeriod.getFrom().equals(timePeriod.getTo())){
				overallResult += "</br>This fact holds for the year " + timePeriod.getFrom();
			} else {
				overallResult = "</br>This fact holds for the years between " + timePeriod.getFrom() + " and " + timePeriod.getTo();
			}
		}
		HorizontalLayout overallScorePanel = new HorizontalLayout();
		overallScorePanel.setWidth(null);
		addComponent(overallScorePanel);
		setComponentAlignment(overallScorePanel, Alignment.MIDDLE_CENTER);
		Label totalScore = new Label("<h2>" + overallResult + "</h2>", ContentMode.HTML);
		totalScore.setWidth(null);
		totalScore.setHeight(null);
		overallScorePanel.addComponent(totalScore);
		overallScorePanel.setComponentAlignment(totalScore, Alignment.TOP_CENTER);
		// export evidence as RDF option
		Button exportButton = new Button("Export as RDF");
		exportButton.setStyleName(Reindeer.BUTTON_LINK);
		StreamResource res = new StreamResource(new StreamSource() {

			@Override
			public InputStream getStream() {
				return new ByteArrayInputStream(asRDF(triple, evidence).getBytes());
			}
		}, "provenance.ttl");
		FileDownloader fileDownloader = new FileDownloader(res);
		fileDownloader.extend(exportButton);
		overallScorePanel.addComponent(exportButton);
		overallScorePanel.setComponentAlignment(exportButton, Alignment.MIDDLE_RIGHT);
		
		
		VerticalLayout webSitesPanel = new VerticalLayout();
		webSitesPanel.setHeight(null);
		Panel panel = new Panel();
		panel.setCaption(evidence.getAllWebSites().size() + " websites containing the fact:");
        panel.setContent(webSitesPanel);
        panel.setWidth("100%");
        panel.setHeight(null);
        panel.setSizeFull();
        addComponent(panel);
        setExpandRatio(panel, 1f);
        
        //sort websites bei defacto score
        List<WebSite> webSites = evidence.getAllWebSites();
        Collections.sort(webSites, new Comparator<WebSite>() {

			@Override
			public int compare(WebSite o1, WebSite o2) {
				return ComparisonChain.start()
					      .compare(o2.getScore(), o1.getScore())
					      .compare(o1.getTitle(), o2.getTitle()) 
					      .result();
			}
		});
        
		//show entry for each website
		int i = 0;
		for (final WebSite website : webSites) {//if(i++==3)break;
			//add panel for website
			webSitesPanel.addComponent(createWebSitePanel(website, triple, evidence));
			//add separator
			Label separator = new Label("<div class=\"line-separator\"></div>", ContentMode.HTML);
			separator.setWidth("80%");
			webSitesPanel.addComponent(separator);
			webSitesPanel.setComponentAlignment(separator, Alignment.MIDDLE_CENTER);
		}
	}
	
	private Component createWebSitePanel(final WebSite website, final Triple triple, final Evidence evidence){
		HorizontalLayout l = new HorizontalLayout();
		l.setWidth("100%");
		l.setHeight(null);
		l.setSpacing(true);
		
		//the left side containing title, URL, proofs, export button, etc
		VerticalLayout leftSide = new VerticalLayout();
		leftSide.setHeight(null);
		l.addComponent(leftSide);
		
		String html = "";
		//the page title as link to URL
		String url = website.getUrl();
		String title = website.getTitle();
		html += "<a href=\"" + url + "\">" + title + "</a><p><b>Proof(s):</b></p>";
		leftSide.addComponent(new Label(html, ContentMode.HTML));
		//show the proofs
		List<ComplexProof> proofs = evidence.getComplexProofs(website);
		GridLayout proofsLayout = new GridLayout(2, proofs.size());
		proofsLayout.setWidth("100%");
//		proofsLayout.setHeight("100%");
		proofsLayout.setColumnExpandRatio(1, 1f);
		proofsLayout.setMargin(new MarginInfo(false, false, false, true));
		leftSide.addComponent(proofsLayout);
		leftSide.setExpandRatio(proofsLayout, 1f);
		int cnt = 1;
		List<Pattern> boaPatterns = evidence.getBoaPatterns();
		for (ComplexProof proof : proofs) {
			boolean add = false;
			for (Pattern pattern : boaPatterns) {
				if(!pattern.getNormalized().trim().isEmpty() && proof.getProofPhrase().toLowerCase().contains(pattern.getNormalized().toLowerCase())){
					add = true;
					break;
				}
			}
			if(!proof.getTinyContext().contains("http:") && !proof.getTinyContext().contains("ftp:")){
				Component proofPanel = createProofPanel(website, proof);
				Label label = new Label(cnt++ + ". ");
				label.setWidth(null);
				proofsLayout.addComponent(label);
				proofsLayout.addComponent(proofPanel);
			} else {
				System.out.println("TINY:" + proof.getTinyContext());
			}
			
		}
		
		
//		//export as rdf option
//		Button exportButton = new Button("Export as RDF");
//		exportButton.setStyleName(Reindeer.BUTTON_LINK);
//		StreamResource res = new StreamResource(new StreamSource() {
//			
//			@Override
//			public InputStream getStream() {
//				return new ByteArrayInputStream(asRDF(triple, evidence, website).getBytes());
//			}
//		},"provenance.ttl");
//        FileDownloader fileDownloader = new FileDownloader(res);
//        fileDownloader.extend(exportButton);
//		leftSide.addComponent(exportButton);
//		leftSide.setComponentAlignment(exportButton, Alignment.BOTTOM_LEFT);
		
		//create the chart for the scores
		DCharts chart = chartGenerator.generateChart(evidence, website);
		chart.setHeight("150px");
		chart.setWidth("300px");
		l.addComponent(chart);
		
		l.setExpandRatio(leftSide, 1f);
		l.setComponentAlignment(chart, Alignment.TOP_RIGHT);
		
		return l;
	}
	
	/**
	 * Create a panel for the proof which allows for striking through the proof if it is supposed to be wrong.
	 * TODO Feedback to DeFacto
	 * @param website
	 * @param proof
	 * @return
	 */
	private Component createProofPanel(WebSite website, final ComplexProof proof){
		VerticalLayout vl = new VerticalLayout();
		vl.addStyleName("proof-panel");
		final String html = highlightWords(proof.getTinyContext(), 
						Sets.newHashSet(website.getQuery().getSubjectLabel(), website.getQuery().getObjectLabel()));
		final Label label = new Label(html, ContentMode.HTML);
		label.addStyleName("crosshair");
		vl.addComponent(label);
		vl.addLayoutClickListener(new LayoutClickListener() {
			boolean striked = false;
			@Override
			public void layoutClick(LayoutClickEvent event) {
				striked = !striked;
				label.setValue(striked ? ("<strike>" + html + "</strike>") : html);
			}
		});
		return vl;
	}
	
	private Component createTimelineComponent(DefactoTimePeriod timePeriod) throws ParseException {
//		DateTimeHighChart chart = new DateTimeHighChart("The fact holds for the years", timePeriod.getFrom(), timePeriod.getTo());
//		chart.setId("datetimechart");
//		chart.setHeight("100px");
//		return chart;
		
		String message;
		if(timePeriod.getFrom().equals(timePeriod.getTo())){
			message = "This fact holds for the year " + timePeriod.getFrom();
		} else {
			message = "This fact holds for the years between " + timePeriod.getFrom() + " and " + timePeriod.getTo();
		}
		return new Label("<h2>" + message + "</h2>", ContentMode.HTML);
	}
	
	private String asRDF(Triple triple, Evidence evidence){
		String provenanceRDF = EvidenceRDFGenerator.getProvenanceInformationAsString(triple, evidence, startTime, endTime, "TURTLE");
		return provenanceRDF;
	}
	
	/**
	 * Render each word occurrence in the given text bold
	 * @param text
	 * @param words
	 * @return
	 */
	private String highlightWords(String text, Set<String> words){
		String s = text;
		for (String word : words) {
			s = s.replace(word, "<b>" + word + "</b>");
		}
		return s;
	}
	
}
