package org.aksw.gui;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.jensjansson.pagedtable.PagedTableContainer;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.util.ProvenanceInformationGenerator;
import org.aksw.defacto.util.RandomIntegerGenerator;
import org.aksw.helper.TripleComponent;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.ui.TextAnchor;
import org.vaadin.addon.JFreeChartWrapper;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 6/2/12
 * Time: 9:40 PM
 * Displaying the search results as a list containing the detail of each result, e.g. PageRank, thumbnail.
 */
public class SearchResultRepeater extends VerticalLayout {

    private static Logger logger = Logger.getLogger(SearchResultRepeater.class);

    //This variable holds the name of the LinkButton that was clicked before the last one in order to reenable it,
    //and disable the current one
//    Button previousClickedButton = null;

    private int currentPage = 0;

    private static final int DEFAULT_NUMBER_OF_RESULTS_PER_PAGE = 10;
    private static final String FIRST_PAGE_LABEL = "<<";
    private static final String LAST_PAGE_LABEL = ">>";
    private static final String PREVIOUS_PAGE_LABEL = "<";
    private static final String NEXT_PAGE_LABEL = ">";
    protected boolean alwaysRecalculateColumnWidths = false;

    private PagedTableContainer container;

    private Evidence resultingEvidence = null;
    private String tripleToValidate = "";
    TripleComponent component;
    private ArrayList<WebsiteItem> lstWebsiteItems;

    GridLayout resultsLayout = null;

    private int numOfResultsPerPage;
    double maximumTopicMajorityInTheWeb = 1.0;

    private final ArrayList<MyButton> arrApproveDisapproveButtons = new ArrayList<MyButton>();

    public SearchResultRepeater(Evidence resultingEvidence, int numOfResultsPerPage) {
        this.resultingEvidence = resultingEvidence;
        this.numOfResultsPerPage = numOfResultsPerPage;

        resultsLayout = createCoreGridLayout(numOfResultsPerPage);
        this.addComponent(resultsLayout);
        this.setComponentAlignment(resultsLayout, Alignment.MIDDLE_CENTER);
        sortResults(resultingEvidence);
    }

    public void setTripleToValidate(String tripleToValidate) {
        this.tripleToValidate = tripleToValidate;
    }

    public String getTripleToValidate() {

        return tripleToValidate;
    }

    public void displayResults(){
        this.removeAllComponents();

        GridLayout renderedResults = renderResults();
        if(renderedResults!=null){

            HorizontalLayout layoutHeader = createHeaderTitle(this.resultingEvidence.getAllWebSites().size(), this.tripleToValidate);
            layoutHeader.setWidth(100, Sizeable.UNITS_PERCENTAGE);



            Layout scoreLayout = createDefactoOverallScore();
            this.addComponent(scoreLayout);
            this.setComponentAlignment(scoreLayout, Alignment.MIDDLE_CENTER);

            Label lblHR = new Label();
            lblHR.setValue("<hr/>");
            lblHR.setContentMode(Label.CONTENT_XHTML);
            lblHR.setWidth(610, Sizeable.UNITS_PIXELS);
            addComponent(lblHR);
            setComponentAlignment(lblHR, Alignment.TOP_CENTER);

            this.addComponent(renderedResults);
            this.setComponentAlignment(renderedResults, Alignment.MIDDLE_CENTER);

            this.addComponentAsFirst(layoutHeader);
            this.setComponentAlignment(layoutHeader, Alignment.MIDDLE_CENTER);
        }

        GridLayout layoutControlsOfRepeater = createControls();

        this.addComponent(layoutControlsOfRepeater);
        this.setComponentAlignment(layoutControlsOfRepeater, Alignment.MIDDLE_CENTER);
    }


    /**
     * Creates a header title to display before the search results
     * @param numberOfResults   Number of websites found for validating that triple
     * @param tripleToValidate  String represntation of the triple that should be checked for correctness
     * @return  HorizontalLayout containing the title, which can be added directly to the mail layout
     */
    private HorizontalLayout createHeaderTitle(int numberOfResults, String tripleToValidate) {
        HorizontalLayout headerTitle = new HorizontalLayout();

        Label lblTitle = new Label();

        //Replace < and > in order to render them correctly in HTML
        tripleToValidate = tripleToValidate.replaceAll("<", "&lt;");
        tripleToValidate = tripleToValidate.replaceAll(">", "&gt;");

        lblTitle.setValue("<hr/><CENTER><h2>" + numberOfResults+ " results found for validating triple:&nbsp;" + tripleToValidate + "&nbsp;</h2></CENTER>" );
        lblTitle.setContentMode(Label.CONTENT_XHTML);
//        lblTitle.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        headerTitle.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        lblTitle.setStyleName("h2");
        headerTitle.addComponent(lblTitle);
        headerTitle.setComponentAlignment(lblTitle, Alignment.MIDDLE_CENTER);
        return headerTitle;
    }

    private HorizontalLayout createDefactoOverallScore() {
        HorizontalLayout scoreLayout = new HorizontalLayout();

        Label lblTitle = new Label();
//
//        //Replace < and > in order to render them correctly in HTML
//        tripleToValidate = tripleToValidate.replaceAll("<", "&lt;");
//        tripleToValidate = tripleToValidate.replaceAll(">", "&gt;");
//
        lblTitle.setValue("<h3> Overall DeFacto Score </h3>" );
        lblTitle.setContentMode(Label.CONTENT_XHTML);
//        lblTitle.setWidth(100, Sizeable.UNITS_PERCENTAGE);
//        scoreLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        scoreLayout.setSizeUndefined();
        lblTitle.setStyleName("h2");
        scoreLayout.addComponent(lblTitle);
        scoreLayout.setComponentAlignment(lblTitle, Alignment.MIDDLE_RIGHT);

        Color overallScoreColor;
        if(resultingEvidence.getDeFactoScore() <= 0.25)
            overallScoreColor = Color.RED;
        else if(resultingEvidence.getDeFactoScore() <= 0.5)
            overallScoreColor = Color.ORANGE;
        else if(resultingEvidence.getDeFactoScore() <= 0.75)
            overallScoreColor = Color.YELLOW;
        else
            overallScoreColor = Color.GREEN;

        JFreeChartWrapper defactoScore = createChartWithPercentage(createDataset(resultingEvidence.getDeFactoScore() * 100, "")
                , overallScoreColor);
        scoreLayout.addComponent(defactoScore);

        scoreLayout.setComponentAlignment(defactoScore, Alignment.MIDDLE_CENTER);
        return scoreLayout;
    }

    private GridLayout createCoreGridLayout(int numOfResultsPerPage) {
        //Prepare a grid for the results, containing the required number of rows
        resultsLayout = new GridLayout(3, numOfResultsPerPage);
        resultsLayout.setSizeFull();
//        resultsLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        resultsLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        resultsLayout.setColumnExpandRatio(0, 0.1f);
        resultsLayout.setColumnExpandRatio(1, 0.5f);
        resultsLayout.setColumnExpandRatio(2, 0.4f);
        return resultsLayout;
    }



    public SearchResultRepeater(int numOfResultsPerPage) {

        this.numOfResultsPerPage = numOfResultsPerPage;

        resultsLayout = createCoreGridLayout(numOfResultsPerPage);
        this.addComponent(resultsLayout);
        this.setComponentAlignment(resultsLayout, Alignment.MIDDLE_CENTER);
    }

    public SearchResultRepeater(Evidence resultingEvidence) {
        this.resultingEvidence = resultingEvidence;

        numOfResultsPerPage = DEFAULT_NUMBER_OF_RESULTS_PER_PAGE;

        resultsLayout = createCoreGridLayout(numOfResultsPerPage * 2 -1); //Use double the number in order to add place for separator
        this.addComponent(resultsLayout);
        this.setComponentAlignment(resultsLayout, Alignment.MIDDLE_CENTER);
        sortResults(resultingEvidence);
    }

    public SearchResultRepeater(Evidence resultingEvidence, String triple) {
        this.resultingEvidence = resultingEvidence;
        this.tripleToValidate = triple;

        numOfResultsPerPage = DEFAULT_NUMBER_OF_RESULTS_PER_PAGE;

        resultsLayout = createCoreGridLayout(numOfResultsPerPage * 2 - 1); //Use double the number in order to add place for separator
        HorizontalLayout layoutHeader = createHeaderTitle(resultingEvidence.getAllWebSites().size(), triple);
        layoutHeader.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        this.addComponent(layoutHeader);
        this.setComponentAlignment(layoutHeader, Alignment.MIDDLE_CENTER);

        Layout scoreLayout = createDefactoOverallScore();
        this.addComponent(scoreLayout);
        this.setComponentAlignment(scoreLayout, Alignment.MIDDLE_CENTER);

        Label lblHR = new Label();
        lblHR.setValue("<hr/>");
        lblHR.setContentMode(Label.CONTENT_XHTML);
//        lblHR.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        addComponent(lblHR);

        this.addComponent(resultsLayout);
        this.setComponentAlignment(resultsLayout, Alignment.MIDDLE_CENTER);

        sortResults(resultingEvidence);
    }




    public SearchResultRepeater(Evidence resultingEvidence, TripleComponent component) {

        this.resultingEvidence = resultingEvidence;
        this.component = component;


        //This model is only used in displaying the results, so it can format the output as NTriples
        Model inputTripleModelForOutput = ModelFactory.createDefaultModel();
        inputTripleModelForOutput.add(ResourceFactory.createStatement(ResourceFactory.createResource(component.getSubject()),
                ResourceFactory.createProperty(component.getPredicate()), inputTripleModelForOutput.createResource(component.getObject())));


//        String triple = createOutputTripleFromModel(inputTripleModelForOutput, "TURTLE");
        tripleToValidate = component.getSubjectLabel() +" " + component.getPredicateLabel() + " " + component.getObjectLabel();

        numOfResultsPerPage = DEFAULT_NUMBER_OF_RESULTS_PER_PAGE;

        resultsLayout = createCoreGridLayout(numOfResultsPerPage * 2 - 1); //Use double the number in order to add place for separator

        HorizontalLayout layoutHeader = createHeaderTitle(resultingEvidence.getAllWebSites().size(), tripleToValidate);
        layoutHeader.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        this.addComponent(layoutHeader);
        this.setComponentAlignment(layoutHeader, Alignment.MIDDLE_CENTER);

        Layout scoreLayout = createDefactoOverallScore();
        this.addComponent(scoreLayout);
        this.setComponentAlignment(scoreLayout, Alignment.MIDDLE_CENTER);


        Label lblHR = new Label();
        lblHR.setValue("<hr/>");
        lblHR.setContentMode(Label.CONTENT_XHTML);
//        lblHR.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        this.addComponent(lblHR);
        this.setComponentAlignment(lblHR, Alignment.MIDDLE_CENTER);

        this.addComponent(resultsLayout);
        this.setComponentAlignment(resultsLayout, Alignment.MIDDLE_CENTER);

        sortResults(resultingEvidence);

    }

    private void sortResults(Evidence resultingEvidence) {

        ArrayList<WebSite> lstResultingWebSites = (ArrayList<WebSite>) resultingEvidence.getAllWebSites();
        // Collections.sort(lstResultingWebSites, new
        // WebsiteSorterByDefactoScore());

        maximumTopicMajorityInTheWeb = calculateMaximumTopicMajorityInTheWeb(lstResultingWebSites);

        lstWebsiteItems = new ArrayList<WebsiteItem>();
        // Iterate through the list, in order to construct ArrayList of
        // WebsiteItem, as it contains the defacto score as well
        // Then sort accordingly
        for (WebSite website : lstResultingWebSites) {

            int score = 0;
            ArrayList<ComplexProof> proofs = (ArrayList<ComplexProof>) resultingEvidence.getComplexProofs(website);

            for (ComplexProof proof : proofs) {

                for (org.aksw.defacto.boa.Pattern pattern : resultingEvidence.getBoaPatterns()) {
                    if (StringUtils.containsIgnoreCase(proof.getProofPhrase(), pattern.naturalLanguageRepresentationNormalized.trim())) {

                        score++;
                    }
                }
            }
            // double defactoScoreForWebsite = calculateDefactoScore(website,
            // maximumTopicMajorityInTheWeb);
            // lstWebsiteItems.add(new WebsiteItem(website,
            // defactoScoreForWebsite));
            lstWebsiteItems.add(new WebsiteItem(website, score));
        }

        Collections.sort(lstWebsiteItems);
    }


    public Evidence getDataSource() {
        return resultingEvidence;
    }

    public void setDataSource(Evidence resultingEvidence) {
        this.resultingEvidence = resultingEvidence;

    }

    public void setPageLength(int pageLength) {

        if (pageLength >= 0 && getPageLength() != pageLength) {
            container.setPageLength(pageLength);

            numOfResultsPerPage = pageLength;
        }

    }

    public int getPageLength() {

        return numOfResultsPerPage;
    }

    private GridLayout renderResults(){

        //Get the list of all NL representations to highlight them in the output
        ArrayList<String> nlPatterns = new ArrayList<String>();
        for(Pattern boaPattern:resultingEvidence.getBoaPatterns())
            if(boaPattern.naturalLanguageRepresentationNormalized.trim().compareTo("") != 0)
                nlPatterns.add(boaPattern.naturalLanguageRepresentationNormalized.trim());

//        this.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        //If there is no data in the array, then just do nothing
        if((resultingEvidence == null) || (resultingEvidence.getAllWebSites().size() == 0))
            return null;

        //Create the layout, or clear it if it was already created before
        if(resultsLayout == null)
            resultsLayout = createCoreGridLayout(numOfResultsPerPage * 2 - 1); //Use double the number in order to add place for separator
        else
            resultsLayout.removeAllComponents();

        int renderStartIndex = currentPage * getPageLength() ;
        int remainingItemsInList = resultingEvidence.getAllWebSites().size() - renderStartIndex;



        //This index indicates the row at which the next component should be inserted
        //This is useful, as we cannot use the index used for accessing the results array, because we will insert a separator as well
        int currentRowInGrid = 0;



        //Determines the last index at which the render stops, as the could be the remaining number of results
        //is less than number of results per page
        int renderStopIndex = numOfResultsPerPage < remainingItemsInList ? numOfResultsPerPage : remainingItemsInList;

        ExecutorService chartCreatorService, thumbnailCreatorService;
        Future<JFreeChartWrapper> chartCreatorTask;
        Future<String> thumbnailCreatorTask;


        for(int i = 0; i < renderStopIndex; i++ ){

            WebsiteItem currentWebsiteItem = lstWebsiteItems.get(renderStartIndex + i);

            Embedded websiteThumbnail =new Embedded();

            //Call ChartPlotter in parallel to speed up the process of plotting charts
            try{
                thumbnailCreatorService = Executors.newFixedThreadPool(1);
                thumbnailCreatorTask    = thumbnailCreatorService.submit(new ThumbnailCreatorThread(currentWebsiteItem.getWebsite()));


                websiteThumbnail = new Embedded("", new FileResource(new File(thumbnailCreatorTask.get()), this.getApplication()));
                websiteThumbnail.setType(Embedded.TYPE_IMAGE);

            }
            catch (Exception exp){
                logger.warn("Graph of website titled \"" + currentWebsiteItem.getWebsite().getTitle() +"\" cannot be created");
            }

            resultsLayout.addComponent(websiteThumbnail, 0, currentRowInGrid);
            resultsLayout.setComponentAlignment(websiteThumbnail, Alignment.MIDDLE_CENTER);

            //Prepare the middle part, containing the title of the webpage an excerpt from it, and its URL
            VerticalLayout layoutWebsiteContent = new VerticalLayout();
            Link lnklTitle = new Link(currentWebsiteItem.getWebsite().getTitle(), new ExternalResource(currentWebsiteItem.getWebsite().getUrl()));
            lnklTitle.setTargetName("_blank");
            layoutWebsiteContent.addComponent(lnklTitle);
            layoutWebsiteContent.setComponentAlignment(lnklTitle, Alignment.TOP_LEFT);

            //Get the structured proofs of the website
            ArrayList<ComplexProof> complexProofs = (ArrayList<ComplexProof>)resultingEvidence.getComplexProofs(currentWebsiteItem.getWebsite());

            //Remove duplicate context strings returned from the backend module
            ArrayList<String> arrDistinctContexts = new ArrayList<String>();

            if(complexProofs.size() > 0){
                for(ComplexProof proof: complexProofs){
                    if(!arrDistinctContexts.contains(proof.getContext())){
//                        String contextString = proof.getContext();

                        arrDistinctContexts.add(proof.getContext());
                    }

                }

            }

            //Create a grid layout with the appropriate number of rows to display the contents of structured proofs
            GridLayout layoutComplexProof = new GridLayout(4, arrDistinctContexts.size()  + 2);

//            layoutComplexProof.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            layoutComplexProof.setHeight(100, Sizeable.UNITS_PERCENTAGE);
            layoutComplexProof.setWidth(610, Sizeable.UNITS_PIXELS);
            layoutComplexProof.setColumnExpandRatio(0, 0.1f);
            layoutComplexProof.setColumnExpandRatio(1, 0.7f);
            layoutComplexProof.setColumnExpandRatio(2, 0.1f);
            layoutComplexProof.setColumnExpandRatio(3, 0.1f);
            Label lblStructuredProof = new Label("<b>Proof(s):  </b>");
            layoutComplexProof.addComponent(lblStructuredProof, 0, 0);
            lblStructuredProof.setWidth(60, Sizeable.UNITS_PIXELS);
            lblStructuredProof.setContentMode(Label.CONTENT_XHTML);

            Label lblStructuredPhrase;
            int proofIndex = 1;//Start from 1 as 0 will not be used for displaying any proof
            if(arrDistinctContexts.size() > 0){
                for(String proof: arrDistinctContexts){

                    HorizontalLayout hlProofWithApprovalButtons = new HorizontalLayout();
                    hlProofWithApprovalButtons.addStyleName("tweet");

                    HorizontalLayout hlProof = new HorizontalLayout();
                    hlProof.setWidth(430, Sizeable.UNITS_PIXELS);

                    lblStructuredPhrase = new Label();
                    //In order to highlight the subject and object we should write them in bold
                    String contextString = proof.replaceAll("(?i)" + resultingEvidence.getSubjectLabel(), "<b>" + resultingEvidence.getSubjectLabel() + "</b>");
                    contextString = contextString.replaceAll("(?i)" + resultingEvidence.getObjectLabel(), "<b>" + resultingEvidence.getObjectLabel() + "</b>");

                    //Boldface all patterns as well
                    for(String pattern:nlPatterns){
                        if(contextString.toLowerCase().contains(pattern.toLowerCase())){
                            contextString = contextString.replaceAll("(?i)" + pattern, "<b>" + pattern + "</b>");
                            break;
                        }
                    }
                    lblStructuredPhrase.setValue(contextString +"&nbsp;&nbsp;</br>");
                    lblStructuredPhrase.setContentMode(Label.CONTENT_XHTML);
                    lblStructuredPhrase.setSizeFull();
//                    lblStructuredPhrase.setWidth(100, Sizeable.UNITS_PERCENTAGE);
//                    lblStructuredPhrase.setSizeUndefined();

                    hlProof.addComponent(lblStructuredPhrase);
                    hlProof.setComponentAlignment(lblStructuredPhrase, Alignment.BOTTOM_LEFT);

//                    HorizontalLayout hlApproveDisapproveButtons = new HorizontalLayout();
//                    hlApproveDisapproveButtons.setMargin(false, false, false, true);
                    HorizontalLayout layoutButtonsApproval = new HorizontalLayout();
                    //Button for proof approval
                    final MyButton btnApprove = new MyButton(renderStartIndex + i, proofIndex, true);
//                    btnApprove.addStyleName("tweet");
                    btnApprove.setCaption("");
                    btnApprove.setDescription("Accept");
                    btnApprove.addListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            logger.info("Pressed button is for website # " + btnApprove.getWebsiteIndex() + ", proof # "
                                    + btnApprove.getProofIndex());

                            int requiredButtonPosition = arrApproveDisapproveButtons.indexOf(new MyButton(btnApprove.getWebsiteIndex(),
                                    btnApprove.getProofIndex(), false));

                            //Change the status of the current button to disabled and enable the other button
                            btnApprove.setEnabled(false);
                            arrApproveDisapproveButtons.get(requiredButtonPosition).setEnabled(true);
                            getWindow().showNotification("Thank you, for your feedback");
                        }
                    });
                    btnApprove.setIcon(new FileResource(new File("defacto-ui/web/images/correct_30.png"), this.getApplication()));
                    btnApprove.setWidth(40, Sizeable.UNITS_PIXELS);
                    btnApprove.setHeight(40, Sizeable.UNITS_PIXELS);
                    layoutButtonsApproval.addComponent(btnApprove);

//                    hlApproveDisapproveButtons.addComponent(btnApprove);
//                    layoutComplexProof.addComponent(btnApprove, 2, proofIndex);

                    //Button for proof disapproval
                    final MyButton btnDisapprove = new MyButton(renderStartIndex + i, proofIndex, false);
                    btnDisapprove.setCaption("");
                    btnDisapprove.setDescription("Reject");
                    btnDisapprove.addListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            logger.info("Pressed button is for website # " + btnDisapprove.getWebsiteIndex() + ", proof # "
                                    + btnDisapprove.getProofIndex());

                            int requiredButtonPosition= arrApproveDisapproveButtons.indexOf(new MyButton(btnDisapprove.getWebsiteIndex(),
                                    btnDisapprove.getProofIndex(), true));

                            //Change the status of the current button to disabled and enable the other button
                            btnDisapprove.setEnabled(false);
                            arrApproveDisapproveButtons.get(requiredButtonPosition).setEnabled(true);
                            getWindow().showNotification("Thank you, for your feedback");
                        }
                    });
                    btnDisapprove.setIcon(new FileResource(new File("defacto-ui/web/images/incorrect_30.png"), this.getApplication()));
                    btnDisapprove.setWidth(40, Sizeable.UNITS_PIXELS);
                    btnDisapprove.setHeight(40, Sizeable.UNITS_PIXELS);

//                    hlProof.addComponent(btnDisapprove);
//                    hlApproveDisapproveButtons.addComponent(btnDisapprove);
//                    layoutComplexProof.addComponent(btnDisapprove, 3, proofIndex);
//                    layoutComplexProof.setComponentAlignment(btnDisapprove, Alignment.MIDDLE_LEFT);
//                    layoutComplexProof.setComponentAlignment(btnApprove, Alignment.MIDDLE_LEFT);
                    layoutButtonsApproval.addComponent(btnDisapprove);
                    layoutButtonsApproval.addStyleName("buttons");
//                    layoutComplexProof.addComponent(layoutButtonsApproval, 3, proofIndex);
                    //Add buttons to the array
                    arrApproveDisapproveButtons.add(btnApprove);
                    arrApproveDisapproveButtons.add(btnDisapprove);

                    hlProofWithApprovalButtons.addComponent(hlProof);
                    hlProofWithApprovalButtons.addComponent(layoutButtonsApproval);

                    layoutComplexProof.addComponent(hlProofWithApprovalButtons, 1, proofIndex, 3, proofIndex);
                    proofIndex++;

//                    layoutComplexProof.addComponent(lblStructuredPhrase, 1, proofIndex++);
//                    layoutComplexProof.addComponent(hlProof, 1, proofIndex++);
//                    layoutComplexProof.addComponent(hlApproveDisapproveButtons, 1, proofIndex++);
                }

            } else{
                lblStructuredPhrase = new Label("N/A");
                layoutComplexProof.addComponent(lblStructuredPhrase, 1, 0);
            }
            layoutWebsiteContent.addComponent(layoutComplexProof);
            layoutWebsiteContent.setComponentAlignment(layoutComplexProof, Alignment.TOP_LEFT);


            //Get the provenance data as N-Triples
            String provenanceRDF = ProvenanceInformationGenerator.getProvenanceInformationAsString(currentWebsiteItem.getWebsite(),
                    component.getSubject(), component.getPredicate(), component.getObject(), "TURTLE");

            //Create layout for the downloadable provenance data
            Label lblProvenanceData = new Label("<b>Provenance&nbsp;</br>Output:&nbsp;&nbsp;</b>");
            lblProvenanceData.setContentMode(Label.CONTENT_XHTML);
            lblProvenanceData.setWidth(60, Sizeable.UNITS_PIXELS);


            layoutComplexProof.addComponent(lblProvenanceData, 0, arrDistinctContexts.size() + 1);

            //Write the provenance data to a downloadable file
            Link lnkDownloadFile =new Link();
            try{
                // Create file
                FileWriter fstream = new FileWriter("/tmp/provenance"  + i + ".ttl");
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(provenanceRDF);
                //Close the output stream
                out.close();

                lnkDownloadFile =new Link("RDF", new FileResource(new File("/tmp/provenance"  + i + ".ttl"), this.getApplication()));
            } catch (Exception e){//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

            layoutComplexProof.addComponent(lnkDownloadFile, 1, arrDistinctContexts.size()  + 1);


            Label lblWebsiteURL = new Label();
            VerticalLayout websiteLayout = new VerticalLayout();
            lblWebsiteURL.setValue("<font color=\"green\">" + currentWebsiteItem.getWebsite().getUrl() + "</font>");
            lblWebsiteURL.setContentMode(Label.CONTENT_XHTML);
            websiteLayout.addComponent(lblWebsiteURL);
            websiteLayout.setComponentAlignment(lblWebsiteURL, Alignment.BOTTOM_LEFT);
            layoutWebsiteContent.addComponent(websiteLayout);
            layoutWebsiteContent.setComponentAlignment(websiteLayout, Alignment.BOTTOM_LEFT);

            layoutComplexProof.setSizeFull();
            resultsLayout.addComponent(layoutWebsiteContent, 1, currentRowInGrid);
            resultsLayout.setComponentAlignment(layoutWebsiteContent, Alignment.MIDDLE_LEFT);

            //Create the required charts of the results
            HorizontalLayout layoutAllCharts = new HorizontalLayout();

//            chartGrid.setColumnExpandRatio(0, 0.3f);
//            chartGrid.setColumnExpandRatio(1, 0.7f);
            /*
            Label lblPageRank = new Label("<b>Page Rank</b>");
//            lblPageRank.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            lblPageRank.setSizeUndefined();
            lblPageRank.setStyleName("h3");
            lblPageRank.setContentMode(Label.CONTENT_XHTML);

            Label lblDeFactoScore = new Label("<b>DeFacto Score</b>");
            lblPageRank.setSizeUndefined();
            lblDeFactoScore.setStyleName("h3");
            lblDeFactoScore.setContentMode(Label.CONTENT_XHTML);

            Label lblTopicMajorityInWeb = new Label("<b>Topic Majority</br>in the Web</b>");
            lblTopicMajorityInWeb.setSizeUndefined();
            lblTopicMajorityInWeb.setStyleName("h3");
            lblTopicMajorityInWeb.setContentMode(Label.CONTENT_XHTML);

            Label lblTopicMajorityInSearchResults = new Label("<b>Topic Majority</br>in the Search results</b>");
            lblTopicMajorityInSearchResults.setSizeUndefined();
            lblTopicMajorityInSearchResults.setStyleName("h3");
            lblTopicMajorityInSearchResults.setContentMode(Label.CONTENT_XHTML);

            Label lblTopicCoverage = new Label("<b>Topic Coverage</b>");
            lblTopicCoverage.setSizeUndefined();
            lblTopicCoverage.setStyleName("h3");
            lblTopicCoverage.setContentMode(Label.CONTENT_XHTML);
             */
            //Normalize PageRank, as it may return 11, or -1
            double pageRank = currentWebsiteItem.getWebsite().getPageRank();
            if(pageRank <= 0)
                pageRank = 11; //if the PageRank is -ve, then set it to 11 as it is used in the core module to represent undefined

            pageRank = pageRank/10;

            CategoryDataset allValues = createDataset(new double[]{currentWebsiteItem.getWebsite().getScore(), pageRank,
                    currentWebsiteItem.getWebsite().getTopicMajorityWebFeature() / maximumTopicMajorityInTheWeb,//Normalize topic majority in the web
                    currentWebsiteItem.getWebsite().getTopicMajoritySearchFeature(), currentWebsiteItem.getWebsite().getTopicCoverageScore()});



            JFreeChartWrapper websiteDefactoScore = createChart(createDataset(currentWebsiteItem.getDefactoScoreOfWebsite(), "  Defacto Score"),
                    Color.GREEN);

            //Call ChartPlotter in parallel to speed up the process of plotting charts
            try{
                chartCreatorService = Executors.newFixedThreadPool(1);
                chartCreatorTask    = chartCreatorService.submit(new ChartPlotter(allValues));

                //            chartGrid.addComponent(createAllCharts(allValues), 1, 0);
                VerticalLayout layoutCharts = new VerticalLayout();
                layoutCharts.addComponent(websiteDefactoScore);
                layoutCharts.addComponent(chartCreatorTask.get());
                layoutAllCharts.addComponent(layoutCharts);
//                layoutChart.addComponent(chartCreatorTask.get());
//                layoutChart.addComponent(websiteDefactoScore);
            }
            catch (Exception exp){
                logger.warn("Graph of website titled " + currentWebsiteItem.getWebsite().getTitle() +" cannot be created");
            }
            ///////////////////////////////////////////

            resultsLayout.addComponent(layoutAllCharts, 2, currentRowInGrid++);
//            currentRowInGrid++;
            resultsLayout.setComponentAlignment(layoutAllCharts, Alignment.MIDDLE_RIGHT);

            Label lblSeparator = new Label("<hr/>", Label.CONTENT_XHTML);
            lblSeparator.setWidth(610, Sizeable.UNITS_PIXELS);

            if(i< renderStopIndex-1){//We don't need a separator after the last row
                resultsLayout.addComponent(lblSeparator, 1, currentRowInGrid++);
                resultsLayout.setComponentAlignment(lblSeparator, Alignment.MIDDLE_CENTER);
            }
        }

        return resultsLayout;
    }

    /**
     * Calculates the overall defacto score per website, upon which we have settled, in order to be used for sorting
     * the results accordingly
     * @param website   Website object with all details required for the calculation
     * @param maximumTopicMajorityInTheWeb  The value of the maximum topic majority in the web, in order to be used in normalization
     * @return  The overall score calculated according to the formula
     *              (4* fact confirmation + 3 * page rank + TopicMajorityWeb + TopicMajoritySearch + TopicCoverage) / 10
     */
    private double calculateDefactoScore(WebSite website, double maximumTopicMajorityInTheWeb) {

        double normalizedTopicMajorityInTheWeb = website.getTopicMajorityWebFeature() / maximumTopicMajorityInTheWeb;

        double pageRank = website.getPageRank();
        if(pageRank < 0 || pageRank > 10)
            pageRank = 0;

        pageRank = pageRank/10;

        double defactoScore = (4 * website.getScore() + 3 * pageRank + normalizedTopicMajorityInTheWeb+
                website.getTopicMajoritySearchFeature() + website.getTopicCoverageScore()) / 10;


        return defactoScore;
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

    /**
     * Creates a dataset suitable for the chart
     * @param dataItem  The page rank that should be used in the chart
     * @return  Dataset containing the PageRank, but to be used in the chart
     */
    private CategoryDataset createDataset(double dataItem, String datasetLabel) {
        /*double[][] data = new double[][] {
                {dataItem},
        };
        return DatasetUtilities.createCategoryDataset("",
                "", data);*/

        DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();

        //for(int i = 0; i < arrDefactoOutputValues.length; i++)
        //data[i][0] = arrDefactoOutputValues[i];
        defaultcategorydataset.addValue(dataItem, "S1", datasetLabel);
        return defaultcategorydataset;
    }

    /**
     * Creates a sample dataset.
     * @param   arrDefactoOutputValues  An array with all defacto scores, in a particular order of
     *                                  Defacto Score, Page Rank, Topic Majority in the Web,
     *                                  Topic Majority in the Search results,  and Topic Coverage

     * @return A dataset.
     */
    private CategoryDataset createDataset(double []arrDefactoOutputValues) {

        if(arrDefactoOutputValues.length != 5)
            throw new ArrayIndexOutOfBoundsException("Defacto scores array must be exactly of length 5");

        DefaultCategoryDataset defaultcategorydataset = new DefaultCategoryDataset();

        double[][] data = new double[arrDefactoOutputValues.length][1];

        //for(int i = 0; i < arrDefactoOutputValues.length; i++)
        //data[i][0] = arrDefactoOutputValues[i];
        defaultcategorydataset.addValue(arrDefactoOutputValues[0], "S1", "Content Score");
        defaultcategorydataset.addValue(arrDefactoOutputValues[1], "S2", "Page Rank");
        defaultcategorydataset.addValue(arrDefactoOutputValues[2], "S3", "TM in the Web");
        defaultcategorydataset.addValue(arrDefactoOutputValues[3], "S4", "TM in Search");
        defaultcategorydataset.addValue(arrDefactoOutputValues[4], "S5", "Topic Coverage");




//        return DatasetUtilities.createCategoryDataset("", "", data);\
        return defaultcategorydataset;
    }


    /**
     * Creates a dataset suitable for the chart
     * @param dataItem  The page rank that should be used in the chart
     * @return  Dataset containing the PageRank, but to be used in the chart
     */
    private CategoryDataset createDataset(int dataItem) {
        double[][] data = new double[][] {
                {dataItem},
        };
        return DatasetUtilities.createCategoryDataset("",
                "", data);
    }

    /**
     * Creates a chart with the passed data
     * @param dataset   The dataset containing the data that should be plotted
     * @param chartColor    The color of the bar
     * @return  A panel containing the chart
     */
    private JFreeChartWrapper createChart(CategoryDataset dataset, Color chartColor ){

        /*JFreeChart barChart = ChartFactory.createBarChart("", // Title
                "", // x-axis Label
                "", // y-axis Label
                dataset, // Dataset
                PlotOrientation.HORIZONTAL, // Plot Orientation
                false, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
        );


        GradientPaint gpBarColor;

        // set up gradient paints for series...
        if(dataset.getValue(0, 0).doubleValue() > 1.0)//If the value is greater than 1, then use a hollow brush
            gpBarColor = new GradientPaint(0.0f, 0.0f, Color.WHITE,
                    0.0f, 0.0f, Color.WHITE);
        else
            gpBarColor = new GradientPaint(0.0f, 0.0f, chartColor,
                0.0f, 0.0f, Color.DARK_GRAY);

        JFreeChartWrapper wrapper = new JFreeChartWrapper(barChart);


        BarRenderer renderOfChart = ((BarRenderer)barChart.getCategoryPlot().getRenderer());

        renderOfChart.setMaximumBarWidth(0.6);
        renderOfChart.setSeriesPaint(0, gpBarColor);
        renderOfChart.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.CENTER_HORIZONTAL));
        renderOfChart.setShadowVisible(false);
//        renderOfChart.setItemMargin(-2);

        /////////////////////////////////////

        StandardCategoryItemLabelGenerator standardcategoryitemlabelgenerator = new PageRankCategoryItemLabelGenerator("{2}", new DecimalFormat("0.0"));
        renderOfChart.setSeriesItemLabelGenerator(0, standardcategoryitemlabelgenerator);
        renderOfChart.setSeriesItemLabelsVisible(0, true);
        renderOfChart.setSeriesItemLabelFont(0, new Font("SansSerif", 0, 12));
        ItemLabelPosition itemlabelposition = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER);
        renderOfChart.setSeriesPositiveItemLabelPosition(0, itemlabelposition);
        renderOfChart.setPositiveItemLabelPositionFallback(new ItemLabelPosition());

        ////////////////////////////////////

        barChart.getCategoryPlot().setBackgroundPaint(Color.WHITE);


        //((BarRenderer)pageRankChart.getCategoryPlot().getRenderer()).
        barChart.getCategoryPlot().getRangeAxis(0).setRange(0d, 1d);
        ((NumberAxis)barChart.getCategoryPlot().getRangeAxis(0)).setTickUnit(new NumberTickUnit(0.2));
        barChart.getCategoryPlot().getDomainAxis().setVisible(false);

        barChart.getCategoryPlot().getRangeAxis().setVisible(false);

        wrapper.setHeight(50, Sizeable.UNITS_PIXELS);
        wrapper.setWidth(200, Sizeable.UNITS_PIXELS);

        return wrapper; */

        JFreeChart barChart = ChartFactory.createBarChart("", // Title
                "", // x-axis Label
                "", // y-axis Label
                dataset, // Dataset
                PlotOrientation.HORIZONTAL, // Plot Orientation
                false, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
        );

        barChart.setNotify(false);

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        JFreeChartWrapper wrapper = new JFreeChartWrapper(barChart);

        //This is a hollow brush to be used if the value of any factor is unavailable, so it will not be rendered, and
        //it will be labeled with N/A
        GradientPaint gbHollowColor = new GradientPaint(0.0f, 0.0f, Color.WHITE, 0.0f, 0.0f, Color.WHITE);


        BarRenderer rendererOfChart = ((BarRenderer)barChart.getCategoryPlot().getRenderer());

        rendererOfChart.setMaximumBarWidth(0.7);
        rendererOfChart.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.CENTER_HORIZONTAL));
        rendererOfChart.setShadowVisible(false);

        StandardCategoryItemLabelGenerator standardcategoryitemlabelgenerator = new PageRankCategoryItemLabelGenerator("{2}", new DecimalFormat("0.00"));

        Paint brush = new GradientPaint(0.0F, 0.0F, chartColor, 0.0F, 0.0F, Color.DARK_GRAY);

//        for(int i = 0; i < dataset.getRowCount(); i++){
        Color colorSilver = new Color(230, 232, 250);

        rendererOfChart.setSeriesItemLabelPaint(0, colorSilver);

        rendererOfChart.setSeriesItemLabelGenerator(0, standardcategoryitemlabelgenerator);
        rendererOfChart.setSeriesItemLabelsVisible(0, true);
        rendererOfChart.setSeriesItemLabelFont(0, new Font("SansSerif", Font.PLAIN, 12));
        ItemLabelPosition itemlabelposition = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER);
        rendererOfChart.setSeriesPositiveItemLabelPosition(0, itemlabelposition);
        rendererOfChart.setPositiveItemLabelPositionFallback(new ItemLabelPosition());
        rendererOfChart.setItemMargin(-2);
        rendererOfChart.setSeriesPaint(0, brush);

//        }


        barChart.getCategoryPlot().setBackgroundPaint(Color.WHITE);

        barChart.getCategoryPlot().getRangeAxis(0).setRange(0d, 1d);
        ((NumberAxis)barChart.getCategoryPlot().getRangeAxis(0)).setTickUnit(new NumberTickUnit(0.2));

        barChart.getCategoryPlot().getRangeAxis().setVisible(false);

        wrapper.setHeight(40, Sizeable.UNITS_PIXELS);
        wrapper.setWidth(340, Sizeable.UNITS_PIXELS);

        return wrapper;

    }



    private Paint[] createPaint()
    {

        Paint apaint[] = new Paint[5];
//        apaint[0] = new GradientPaint(0.0F, 0.0F, Color.RED, 200F, 0F, Color.DARK_GRAY);
        //The colors used are available at http://web.njit.edu/~kevin/rgb.txt.html
        apaint[0] = new GradientPaint(0.0F, 0.0F, Color.RED, 0.0F, 0.0F, Color.DARK_GRAY);
        apaint[1] = new GradientPaint(0.0F, 0.0F, new Color(204, 153, 0), 0.0F, 0.0F, Color.DARK_GRAY); //Color CSS Gold
        apaint[2] = new GradientPaint(0.0F, 0.0F, Color.BLUE, 0.0F, 0.0F, Color.DARK_GRAY);
        apaint[3] = new GradientPaint(0.0F, 0.0F, new Color(184, 115, 51), 0.0F, 0.0F, Color.DARK_GRAY); //Color Copper
        apaint[4] = new GradientPaint(0.0F, 0.0F, Color.MAGENTA, 0.0F, 0.0F, Color.DARK_GRAY);
        return apaint;
    }


    /**
     * Creates a chart with the passed data
     * @param dataset   The dataset containing the data that should be plotted
     * @param chartColor    The color of the bar
     * @return  A panel containing the chart
     */
    private JFreeChartWrapper createChartWithPercentage(CategoryDataset dataset, Color chartColor){

        JFreeChart barChart = ChartFactory.createBarChart("", // Title
                "", // x-axis Label
                "", // y-axis Label
                dataset, // Dataset
                PlotOrientation.HORIZONTAL, // Plot Orientation
                false, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
        );


        GradientPaint gpBarColor;

        // set up gradient paints for series...
        gpBarColor = new GradientPaint(0.0f, 0.0f, chartColor,
                0.0f, 0.0f, Color.DARK_GRAY);

        JFreeChartWrapper wrapper = new JFreeChartWrapper(barChart);

        BarRenderer rendererOfChart = ((BarRenderer)barChart.getCategoryPlot().getRenderer());

        rendererOfChart.setMaximumBarWidth(0.6);
        rendererOfChart.setSeriesPaint(0, gpBarColor);
        rendererOfChart.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.CENTER_HORIZONTAL));
        rendererOfChart.setShadowVisible(false);



        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(0);


        PercentageLabelGenerator standardcategoryitemlabelgenerator = new PercentageLabelGenerator(
                "{3}", new DecimalFormat("0.0%"),
                new DecimalFormat("0%"));//new DecimalFormat("%")

        Color colorSilver = new Color(230, 232, 250);

        rendererOfChart.setSeriesItemLabelPaint(0, colorSilver);
        rendererOfChart.setSeriesItemLabelGenerator(0, standardcategoryitemlabelgenerator);
        rendererOfChart.setSeriesItemLabelsVisible(0, true);
        rendererOfChart.setSeriesItemLabelFont(0, new Font("SansSerif", 0, 12));
        ItemLabelPosition itemlabelposition = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER);
        rendererOfChart.setSeriesPositiveItemLabelPosition(0, itemlabelposition);
        rendererOfChart.setPositiveItemLabelPositionFallback(new ItemLabelPosition());

        barChart.getCategoryPlot().setBackgroundPaint(Color.WHITE);

        barChart.getCategoryPlot().getRangeAxis(0).setRange(0, 100);
//        ((NumberAxis)barChart.getCategoryPlot().getRangeAxis(0)).setStandardTickUnits(NumberAxis.createStandardTickUnits());
        barChart.getCategoryPlot().getDomainAxis().setVisible(false);

        barChart.getCategoryPlot().getRangeAxis().setVisible(false);

        wrapper.setHeight(60, Sizeable.UNITS_PIXELS);
        wrapper.setWidth(340, Sizeable.UNITS_PIXELS);

        return wrapper;
    }


    public void nextPage() {
        setPageFirstIndex(container.getStartIndex() + getPageLength());
    }

    public void previousPage() {
        setPageFirstIndex(container.getStartIndex() - getPageLength());
    }

    public int getCurrentPage() {
        double pageLength = getPageLength();
        int page = (int) Math.floor((double) container.getStartIndex()
                / pageLength) + 1;
        if (page < 1) {
            page = 1;
        }
        return page;
    }

    public void setCurrentPage(int page) {
        int newIndex = (page - 1) * getPageLength();
        if (newIndex < 0) {
            newIndex = 0;
        }
        if (newIndex >= 0 && newIndex != container.getStartIndex()) {
            setPageFirstIndex(newIndex);
        }
    }

    public int getTotalAmountOfPages() {
        int size = container.getContainer().size();
        double pageLength = getPageLength();
        int pageCount = (int) Math.ceil(size / pageLength);
        if (pageCount < 1) {
            pageCount = 1;
        }
        return pageCount;
    }

    private void setPageFirstIndex(int firstIndex) {
        if (container != null) {
            if (firstIndex <= 0) {
                firstIndex = 0;
            }
            if (firstIndex > container.getRealSize() - 1) {
                int size = container.getRealSize() - 1;
                int pages = 0;
                if (getPageLength() != 0) {
                    pages = (int) Math.floor(0.0 + size / getPageLength());
                }
                firstIndex = pages * getPageLength();
            }
            container.setStartIndex(firstIndex);

            if (alwaysRecalculateColumnWidths) {
                for (Object columnId : container.getContainerPropertyIds()) {
                    //setColumnWidth(columnId, -1);
                }
            }
            //firePagedChangedEvent();
        }
    }

    public GridLayout createControls() {

        GridLayout pageNumbersLayout = new GridLayout(3, 1);
//        pageNumbersLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        pageNumbersLayout.setHeight(20, Sizeable.UNITS_PIXELS);

        HorizontalLayout controlBar = new HorizontalLayout();
        controlBar.setWidth(400, Sizeable.UNITS_PIXELS);

        float approximateNumberOfPages = (float) resultingEvidence.getAllWebSites().size() / numOfResultsPerPage;

        int totalNumberOfPages = (int)Math.ceil(approximateNumberOfPages);

        //Create controls for first page and previous page
        Button firstPage = new Button(FIRST_PAGE_LABEL);
        firstPage.setStyleName(BaseTheme.BUTTON_LINK);
        firstPage.addListener(new PageChangeHandler());
        controlBar.addComponent(firstPage);

        Button previousPage = new Button(PREVIOUS_PAGE_LABEL);
        previousPage.setStyleName(BaseTheme.BUTTON_LINK);
        previousPage.addListener(new PageChangeHandler());
        controlBar.addComponent(previousPage);


        for(int i = 1; i <= totalNumberOfPages; i++){
            Button pageNumberLink = new Button(String.valueOf(i));
            pageNumberLink.setStyleName(BaseTheme.BUTTON_LINK);

//            pageNumberLink.setCaption(String.valueOf(i));
            pageNumberLink.addListener(new PageChangeHandler());

            controlBar.addComponent(pageNumberLink);
            //Disable the button corresponding to the current page
            if((currentPage + 1) == i)
                pageNumberLink.setEnabled(false);
        }

        //Create controls for next page and last page
        Button nextPage = new Button(NEXT_PAGE_LABEL);
        nextPage.setStyleName(BaseTheme.BUTTON_LINK);
        nextPage.addListener(new PageChangeHandler());
        controlBar.addComponent(nextPage);

        Button lastPage = new Button(LAST_PAGE_LABEL);
        lastPage.setStyleName(BaseTheme.BUTTON_LINK);
        lastPage.addListener(new PageChangeHandler());
        controlBar.addComponent(lastPage);

        pageNumbersLayout.addComponent(controlBar, 1, 0);
        pageNumbersLayout.setComponentAlignment(controlBar, Alignment.MIDDLE_CENTER);

        return pageNumbersLayout;

    }

    /**
     * Generates the output triple according to the passed format
     * @param resultsModel  The model containing the triple
     * @param syntax    The syntax of the output, e.g. N-TRIPlE
     * @return  The triple formatted with the required format
     */
    private String createOutputTripleFromModel(Model resultsModel, String syntax) {
//        String syntax = "TURTLE"; // also try "N-TRIPLE" and "TURTLE"
        StringWriter out = new StringWriter();
        resultsModel.setNsPrefix("dbpedia", "http://dbpedia.org/resource/");
        resultsModel.setNsPrefix("dbpediaowl", "http://dbpedia.org/ontology/");
        resultsModel.write(out, syntax);
        String triple = out.toString();

        if(syntax.compareTo("TURTLE") == 0){//If the required format is TURTLE, then we should remove all prefixes
            String []lines = triple.split("\\r?\\n");
            triple = "";
            for(String line: lines){
                if((line.compareTo("") !=0 ) && (!line.contains("@prefix")))
                    triple += "   " + line + "   ";
            }
        }

        //remove all <, >, and replace them with namespaces
        triple = triple.replaceAll("<", "");
        triple = triple.replaceAll(">", "");
        triple = triple.replaceAll("http://dbpedia.org/resource/", "dbpedia:");
        triple = triple.replaceAll("http://dbpedia.org/ontology/", "dbpediaowl:");

        return  triple;
    }


    private class PageChangeHandler implements Button.ClickListener{

        @Override
        public void buttonClick(Button.ClickEvent event) {
            logger.info("BUTTON = " + event.getButton().getCaption());

            String pressedButton = event.getButton().getCaption();
            if(pressedButton.compareTo(FIRST_PAGE_LABEL) == 0)//First page is requested
                currentPage = 0;
            else if(pressedButton.compareTo(PREVIOUS_PAGE_LABEL) == 0)//Previous page is requested
                currentPage = currentPage == 0 ? 0 : currentPage-1;
            else if(pressedButton.compareTo(NEXT_PAGE_LABEL) == 0){//Next page is requested

                float approximateNumberOfPages = (float) resultingEvidence.getAllWebSites().size() / numOfResultsPerPage;

                int totalNumberOfPages = (int)Math.ceil(approximateNumberOfPages);

                currentPage = currentPage == (totalNumberOfPages-1) ? (totalNumberOfPages-1) : currentPage-1;
            }
            else if(pressedButton.compareTo(LAST_PAGE_LABEL) == 0){//Last page is requested

                float approximateNumberOfPages = (float) resultingEvidence.getAllWebSites().size() / numOfResultsPerPage;

                int totalNumberOfPages = (int)Math.ceil(approximateNumberOfPages);

                currentPage = totalNumberOfPages - 1;
            }
            else
                currentPage = Integer.parseInt(event.getButton().getCaption()) - 1;

            removeAllComponents();

            GridLayout renderedResults = renderResults();
            if(renderedResults!=null){
                Layout layoutHeader = createHeaderTitle(resultingEvidence.getAllWebSites().size(), tripleToValidate);
                layoutHeader.setWidth(100, Sizeable.UNITS_PERCENTAGE);

                Layout scoreLayout = createDefactoOverallScore();
                addComponent(scoreLayout);

                Label lblHR = new Label();
                lblHR.setValue("<hr/>");
                lblHR.setContentMode(Label.CONTENT_XHTML);
//                lblHR.setWidth(100, Sizeable.UNITS_PERCENTAGE);
                addComponent(lblHR);

                setComponentAlignment(scoreLayout, Alignment.MIDDLE_CENTER);
                addComponent(renderedResults);
                setComponentAlignment(renderedResults, Alignment.MIDDLE_CENTER);

                addComponentAsFirst(layoutHeader);
                setComponentAlignment(layoutHeader, Alignment.MIDDLE_CENTER);
            }
            GridLayout layoutControls =createControls();
            addComponent(layoutControls);
            setComponentAlignment(layoutControls, Alignment.MIDDLE_CENTER);
        }
    }

    private class WebsiteSorterByDefactoScore implements Comparator<WebSite> {

        @Override
        public int compare(WebSite website1, WebSite website2) {
            return -Double.compare(website1.getScore(), website2.getScore());
        }
    }

    private class ThumbnailCreatorThread implements Callable<String>{

        WebSite website;

        public ThumbnailCreatorThread(WebSite website){
            this.website = website;
        }

        @Override
        public String call() throws Exception {
            return WebsiteThumbnailCreator.fetchWebsiteThumbnail(website.getUrl(),
                    String.valueOf("Thumbnail" + RandomIntegerGenerator.generateRandomInteger(10000000, 99999999)));
//            return thumbnailFullPath;
        }
    }

    /**
     * Used to plot charts but as thread, as using the chart created via JFreeChart takes sometime to render
     */
    private class ChartPlotter implements Callable<JFreeChartWrapper> {

        CategoryDataset dataset;

        public ChartPlotter(CategoryDataset dataset){
            this.dataset = dataset;
        }

        /**
         * Creates a chart containing all Defacto scores, e.g. PageRang, TopicCoveragewith the passed data
         * @param dataset   The dataset containing the data that should be plotted
         * @return  A panel containing the chart
         */
        private JFreeChartWrapper createAllCharts(CategoryDataset dataset){
            JFreeChart barChart = ChartFactory.createBarChart("", // Title
                    "", // x-axis Label
                    "", // y-axis Label
                    dataset, // Dataset
                    PlotOrientation.HORIZONTAL, // Plot Orientation
                    false, // Show Legend
                    true, // Use tooltips
                    false // Configure chart to generate URLs?
            );

            barChart.setNotify(false);

            // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
            JFreeChartWrapper wrapper = new JFreeChartWrapper(barChart);
            ChartPanel panel;

            //This is a hollow brush to be used if the value of any factor is unavailable, so it will not be rendered, and
            //it will be labeled with N/A
            GradientPaint gbHollowColor = new GradientPaint(0.0f, 0.0f, Color.WHITE, 0.0f, 0.0f, Color.WHITE);

//            GradientBarPainter gp = new GradientBarPainter();
//            gp.
            
//            ((BarRenderer)barChart.getCategoryPlot().getRenderer()).setBarPainter(new GradientBarPainter(0.1, 0.20, 0.3));

            BarRenderer rendererOfChart = ((BarRenderer)barChart.getCategoryPlot().getRenderer());
//            rendererOfChart.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(
//                    StandardCategoryToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT_STRING, NumberFormat.getInstance()));



//            rendererOfChart.setBaseToolTipGenerator(new CategoryToolTipGenerator() {
//                public String generateToolTip(CategoryDataset dataset, int row, int column) {
//                    return "My customized tooltip";
//                }
//            });


            rendererOfChart.setMaximumBarWidth(0.7);
            rendererOfChart.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.CENTER_HORIZONTAL));
            rendererOfChart.setShadowVisible(false);

            StandardCategoryItemLabelGenerator standardcategoryitemlabelgenerator = new PageRankCategoryItemLabelGenerator("{2}", new DecimalFormat("0.00"));

            Paint[] brushes = createPaint();
            Color colorSilver = new Color(230, 232, 250);

            for(int i = 0; i < dataset.getRowCount(); i++) {
                rendererOfChart.setSeriesItemLabelGenerator(i, standardcategoryitemlabelgenerator);
                rendererOfChart.setSeriesItemLabelsVisible(i, true);
                rendererOfChart.setSeriesItemLabelPaint(i, colorSilver);
                rendererOfChart.setSeriesItemLabelFont(i, new Font("SansSerif", Font.PLAIN, 12));
                ItemLabelPosition itemlabelposition = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER);
                rendererOfChart.setSeriesPositiveItemLabelPosition(i, itemlabelposition);
                rendererOfChart.setPositiveItemLabelPositionFallback(new ItemLabelPosition());
                rendererOfChart.setItemMargin(-2);

                rendererOfChart.setSeriesToolTipGenerator(i, new CategoryToolTipGenerator() {
                public String generateToolTip(CategoryDataset dataset, int row, int column) {
                    return "My customized tooltip";
                }
            });
                if(dataset.getValue(i, i).doubleValue() < 0 || dataset.getValue(i ,i).doubleValue() > 1) {
                    rendererOfChart.setSeriesPaint(i, gbHollowColor);
                    rendererOfChart.setSeriesItemLabelPaint(i, Color.BLACK);
                }

                else{
                    rendererOfChart.setSeriesPaint(i, brushes[i]);
                }
            }


            barChart.getCategoryPlot().setBackgroundPaint(Color.WHITE);

            barChart.getCategoryPlot().getRangeAxis(0).setRange(0d, 1d);
            ((NumberAxis)barChart.getCategoryPlot().getRangeAxis(0)).setTickUnit(new NumberTickUnit(0.2));

            barChart.getCategoryPlot().getRangeAxis().setVisible(false);


            wrapper.setHeight(150, Sizeable.UNITS_PIXELS);
            wrapper.setWidth(340, Sizeable.UNITS_PIXELS);


            return wrapper;
        }

        @Override
        public JFreeChartWrapper call() throws Exception {
            return createAllCharts(dataset);
        }
    }

    /**
     * This class is used to sort the websites according to the overall defacto score calculated with our formula 
     */
    private class WebsiteItem implements Comparable<WebsiteItem>{

        private WebSite website;
        private double defactoScoreOfWebsite;

        public WebSite getWebsite() {
            return website;
        }

        public double getDefactoScoreOfWebsite() {
            return defactoScoreOfWebsite;
        }

        public WebsiteItem(WebSite webSite, double defactoScoreOfWebsite){
            this.website = webSite;
            this.defactoScoreOfWebsite = defactoScoreOfWebsite;
        }

        @Override
        public int compareTo(WebsiteItem websiteItem) {
            return -Double.compare(this.defactoScoreOfWebsite, websiteItem.getDefactoScoreOfWebsite());
        }
    }

    private class MyButton extends NativeButton{
        final int websiteIndex;
        final int proofIndex;
        final boolean isApprove;

        public MyButton(int websiteNumber, int proofNumber){
//            this.websiteIndex = websiteNumber;
//            this.proofIndex = proofNumber;
            this(websiteNumber, proofNumber, true);
            /*this.addListener(new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    logger.info("Hello from website # " + websiteIndex + ", proof # " + proofIndex);
                    NativeButton x;
                }
            });*/
        }

        public MyButton(int websiteNumber, int proofNumber, boolean isApprove){
            this.websiteIndex = websiteNumber;
            this.proofIndex = proofNumber;
            this.isApprove = isApprove;
            /*this.addListener(new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    logger.info("Hello from website # " + websiteIndex + ", proof # " + proofIndex);
                    NativeButton x;
                }
            });*/
        }

        public int getWebsiteIndex() {
            return websiteIndex;
        }

        public int getProofIndex() {
            return proofIndex;
        }

        public boolean getIsApprove() {
            return isApprove;
        }

        @Override
        public boolean equals(Object obj) {
            if (getClass() != obj.getClass())
                return false;
            MyButton searchButton = (MyButton) obj;
            return (this.websiteIndex == searchButton.getWebsiteIndex() && this.proofIndex == searchButton.getProofIndex()
                    && this.isApprove == searchButton.getIsApprove());
        }

    }


}