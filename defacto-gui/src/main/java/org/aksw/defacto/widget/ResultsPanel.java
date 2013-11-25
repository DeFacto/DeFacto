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
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.aksw.defacto.ScoreChartGenerator;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoTimePeriod;
import org.aksw.defacto.util.ProvenanceInformationGenerator;
import org.aksw.defacto.widget.charts.DateTimeHighChart;
import org.aksw.defacto.widget.charts.HighChart;
import org.dussan.vaadin.dcharts.DCharts;

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
	
	public ResultsPanel() {
		addStyleName("result-panel");
	}
	
	public void showResults(final Triple triple, final Evidence evidence){
		removeAllComponents();
		//show some information about the total number of websites, etc.
		Label totalScore = new Label("<h2>Overall DeFacto Score: " + nf.format(evidence.getDeFactoScore()) + "</h2>", ContentMode.HTML);
		totalScore.setWidth(null);
		addComponent(totalScore);
		setComponentAlignment(totalScore, Alignment.MIDDLE_CENTER);
		
		//show time information if exist
		if(evidence.defactoTimePeriod != null){
			try {
				Component timeLine = createTimelineComponent(evidence.defactoTimePeriod);
				addComponent(timeLine);
				setComponentAlignment(timeLine, Alignment.MIDDLE_CENTER);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		
		//show entry for each website
		for (final WebSite website : evidence.getAllWebSites()) {
			HorizontalLayout l = new HorizontalLayout();
			l.setWidth("100%");
			l.setSpacing(true);
			
			//the left side containing title, URL, proofs, export button, etc
			VerticalLayout leftSide = new VerticalLayout();
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
			proofsLayout.setMargin(new MarginInfo(false, false, false, true));
			leftSide.addComponent(proofsLayout);
			int cnt = 1;
			for (ComplexProof proof : proofs) {
				Component proofPanel = createProofPanel(website, proof);
				proofsLayout.addComponent(new Label(cnt++ + ". "));
				proofsLayout.addComponent(proofPanel);
			}
			
			
			//export as rdf option
			Button exportButton = new Button("Export as RDF");
			exportButton.setStyleName(Reindeer.BUTTON_LINK);
			StreamResource res = new StreamResource(new StreamSource() {
				
				@Override
				public InputStream getStream() {
					return new ByteArrayInputStream(asRDF(triple, evidence, website).getBytes());
				}
			},"provenance.ttl");
	        FileDownloader fileDownloader = new FileDownloader(res);
	        fileDownloader.extend(exportButton);
			leftSide.addComponent(exportButton);
			
			//create the chart for the scores
			DCharts chart = chartGenerator.generateChart(evidence, website);
			chart.setHeight("150px");
			chart.setWidth("300px");
			l.addComponent(chart);
			
			l.setExpandRatio(leftSide, 1f);
			l.setComponentAlignment(chart, Alignment.MIDDLE_RIGHT);
			
			addComponent(l);
			
			Label separator = new Label("<div class=\"line-separator\"></div>", ContentMode.HTML);
			separator.setWidth("80%");
			addComponent(separator);
			setComponentAlignment(separator, Alignment.MIDDLE_CENTER);
		}
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
		final String html = highlightWords(proof.getProofPhrase(), 
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
		DateTimeHighChart chart = new DateTimeHighChart("The fact holds for the years", timePeriod.getFrom(), timePeriod.getTo());
		chart.setId("datetimechart");
		chart.setHeight("100px");
		return chart;
	}
	
	private String asRDF(Triple triple, Evidence evidence, WebSite webSite){
		String provenanceRDF = ProvenanceInformationGenerator.getProvenanceInformationAsString(webSite,
				triple.getSubject().getURI(), triple.getPredicate().getURI(), triple.getObject().getURI(), "TURTLE");
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
	
	
//public static void main(String[] args) throws Exception {
////	String address = "http://en.wikipedia.org/wiki/Brad_Pitt";
////	BufferedImage buff = Graphics2DRenderer.renderToImage(address, 1024, 1024);
////
////	// render
////	try {
//////	  BufferedImage buff = ImageRenderer.renderToImage(address, "w3c-homepage.png", 1024);
////	  ImageIO.write(buff, "png", new File("test.png"));
////	} catch (IOException e) {
////	  e.printStackTrace();
////	}
//	
//	WebDriver driver = new FirefoxDriver();
//	driver.get("http://www.dbpedia.org");
//	File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
//	Files.copy(scrFile, new FileOutputStream("test.png"));
//	driver.close();
//}
}
