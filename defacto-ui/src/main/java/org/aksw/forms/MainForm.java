package org.aksw.forms;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.result.SearchResult;
import org.aksw.defacto.util.PropertiesReader;
import org.aksw.defacto.util.SparqlUtil;
import org.aksw.gui.MyComboBox;
import org.aksw.gui.SearchResultRepeater;
import org.aksw.handlers.ComboBoxTextChangeListener;
import org.aksw.helper.TripleComponent;
import org.aksw.validators.UriValidator;
import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.vaadin.addon.JFreeChartWrapper;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Mohamed Morsey
 * Date: 4/22/12
 * Time: 6:31 PM
 * Main form of deFacto.
 */

@SuppressWarnings("serial")

public class MainForm extends Application {
    private Window mainWindow;
    private VerticalLayout mainLayout;

    //This is the TextFiled that will receive the input triple to search for
    private TextField txtInputTriple;
    private MyComboBox cmbSubject;
    private ComboBox cmbPredicate;
    private MyComboBox cmbObject;
    private Button btnGetResults;
    private GridLayout layoutGrid;
    private Label lblInstructions;
    private Embedded browserView;
    PagedTable lstResultingWebsites;
    JFreeChart pageRankChart;
    private ListSelect lstResults;
    private Panel chartPanel = null;
    Application currentApplication;

    private Label txtExample1Subject;
    private Label txtExample1Predicate;
    private Label txtExample1Object;
    private ComboBox cmbExample;
    private Label txtExample2Subject;
    private Label txtExample2Predicate;
    private Label txtExample2Object;
    private Label txtExample3Subject;
    private Label txtExample3Predicate;
    private Label txtExample3Object;
    private Button btnRunExample;

    private ComboBox cmbExampleSubject;
    private TextField txtExamplePredicate;
    private ComboBox cmbExampleObject;

    private Panel popupPanel;
    private PopupView popup;
    private ProgressIndicator subjectProgressIndicator;

    private ArrayList<WebSite> arrResultingWebsites = new ArrayList<WebSite>();

    ArrayList<SearchResult> arrSearchResults = new ArrayList<SearchResult>();

    ProgressIndicator defactoOverallProgressIndicator;
    Logger logger = Logger.getLogger(MainForm.class);


    @Override
    public void init() {
        currentApplication = this;
        setTheme("reindeer");
        mainWindow = new Window("DeFacto");
        mainWindow.setSizeUndefined();


        mainLayout = (VerticalLayout) mainWindow.getContent();
        mainLayout.setImmediate(true);
        mainLayout.setMargin(false);
        setMainWindow(mainWindow);

        final Window mywindow = new Window("Second Window");

        // Manually set the name of the window.
        mywindow.setName("mywindow");

        // Add some content to the window.
        mywindow.addComponent(new Label("Has content."));

        // Add the window to the application.
        addWindow(mywindow);

        buildMainView();

    }

    void buildMainView() {
//        mainLayout.setSizeFull();

//        mainLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
//        mainLayout.setWidth(1000, Sizeable.);
        mainLayout.setSizeUndefined();
        mainLayout.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        Layout header = getHeader();


//        mainLayout.setComponentAlignment(header, Alignment.TOP_RIGHT);
        CssLayout margin = new CssLayout();
        margin.setMargin(false, true, false, true);
        margin.setSizeFull();
        margin.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        Panel p = new Panel();
        p.setSizeFull();
        //AbsoluteLayout p2 = new AbsoluteLayout();
        margin.addComponent(p);
        mainLayout.addComponent(margin);
        mainLayout.setExpandRatio(margin, 1);

        //tabs.addComponent(buildMainFormUsingGridLayout());
        p.addComponent(buildMainFormUsingGridLayout());
        p.setScrollable(true);
        Layout layoutFooter = getFooter();
        mainLayout.addComponent(layoutFooter);
        mainLayout.setComponentAlignment(layoutFooter, Alignment.MIDDLE_CENTER);

        mainLayout.addComponentAsFirst(header);
//        header.setWidth(100, Sizeable.UNITS_PERCENTAGE);
//        mainLayout.setComponentAlignment(header, Alignment.MIDDLE_RIGHT);

    }

    Layout buildMainFormUsingGridLayout() {

        layoutGrid = new GridLayout(3, 6);
        layoutGrid.setImmediate(true);

        layoutGrid.setMargin(false);
        layoutGrid.setSpacing(false);
        layoutGrid.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        layoutGrid.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        layoutGrid.setColumnExpandRatio(0, 0.1f);
        layoutGrid.setColumnExpandRatio(1, 0.6f);
        layoutGrid.setColumnExpandRatio(2, 0.3f);

        GridLayout layoutInputControls = new GridLayout(4, 7);
        layoutInputControls.setWidth(1200, Sizeable.UNITS_PIXELS);
        layoutInputControls.setColumnExpandRatio(0, 0.33f);
        layoutInputControls.setColumnExpandRatio(1, 0.33f);
        layoutInputControls.setColumnExpandRatio(2, 0.33f);
        layoutInputControls.setColumnExpandRatio(3, 0.01f);

        lblInstructions = new Label("<b>Please, enter a subject, a predicate, and an object to validate with DeFacto, " +
                "or use one of the three examples: </b>");
        lblInstructions.setContentMode(Label.CONTENT_XHTML);

        layoutInputControls.addComponent(lblInstructions, 0, 0, 2, 0);

//        layoutGrid.addComponent(lblInstructions, 0, 0, 2, 0);

        //This was a test to create a downloadable file

        //Add text field for the subject of the input predicate
        Label lblSubject = new Label("<b>Subject</b>", Label.CONTENT_XHTML);


        //lblSubject.setStyleName("h2");
//        layoutGrid.addComponent(lblSubject, 0, 1);
        layoutInputControls.addComponent(lblSubject, 0, 1);
        cmbSubject = new MyComboBox();
        ///////////////////
        cmbSubject.setReadOnly(false);
        cmbSubject.setImmediate(true);
        cmbSubject.setNewItemsAllowed(true);
        ////////////////////
        cmbSubject.setWidth(95, Sizeable.UNITS_PERCENTAGE);
        cmbSubject.setInputPrompt("Enter the subject");
        cmbSubject.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_OFF);
        cmbSubject.addItem("http://dbpedia.org/resource/Jamaica_Inn_%28film%29");
//        cmbSubject.addItem("Jamaica Inn (film)");
//        cmbSubject.setValue("Jamaica Inn (film)");
        cmbSubject.setItemCaption("http://dbpedia.org/resource/Jamaica_Inn_%28film%29", "Jamaica Inn (film)" );
        cmbSubject.setValue("http://dbpedia.org/resource/Jamaica_Inn_%28film%29");
        cmbSubject.setRequired(true);
        cmbSubject.setRequiredError("Subject is required");

        HashMap<String, String> initialSubjectList = new HashMap<String, String>();
        initialSubjectList.put("http://dbpedia.org/resource/Jamaica_Inn_%28film%29", "Jamaica Inn (film)");
        cmbSubject.itemsList = initialSubjectList;

//        cmbSubject.addValidator(new UriValidator("Invalid URI."));
        cmbSubject.setNullSelectionAllowed(false);

//        HorizontalLayout hlSubjectAndIndicator = new HorizontalLayout();
//        hlSubjectAndIndicator.setWidth(100, Sizeable.UNITS_PERCENTAGE);
//        subjectProgressIndicator = new ProgressIndicator(new Float(0.0));
//        subjectProgressIndicator.setIndeterminate(true);
//        subjectProgressIndicator.setVisible(false);

//        hlSubjectAndIndicator.addComponent(cmbSubject);
//        hlSubjectAndIndicator.addComponent(subjectProgressIndicator);

//        hlSubjectAndIndicator.setSizeFull();

//        layoutGrid.addComponent(cmbSubject, 1, 1);
        layoutInputControls.addComponent(cmbSubject, 0, 2);
        //Add text field for the predicate of the input predicate
//        layoutGrid.addComponent(new Label("<b>Predicate</b>", Label.CONTENT_XHTML), 0, 2);

        layoutInputControls.addComponent(new Label("<b>Predicate</b>", Label.CONTENT_XHTML), 1, 1);

        layoutGrid.setSpacing(true);
        layoutGrid.setMargin(true, false, false, false);

        cmbPredicate = new ComboBox();
        cmbPredicate.setWidth(95, Sizeable.UNITS_PERCENTAGE);
        cmbPredicate.setInputPrompt("Enter the predicate");
        cmbPredicate.removeAllItems();

        //Fill the predicates ComboBox with the data returned from class PropertiesReader
        HashMap<String, String> hmPredicates = PropertiesReader.getPropertiesList();
        for(Map.Entry<String, String> predicate: hmPredicates.entrySet()){
            cmbPredicate.addItem(predicate.getKey());
            cmbPredicate.setItemCaption(predicate.getKey(), predicate.getValue());
        }

        cmbPredicate.setValue("http://dbpedia.org/ontology/director");
        cmbPredicate.setRequired(true);
        cmbPredicate.setRequiredError("Predicate is required");
        cmbPredicate.addValidator(new UriValidator("Invalid URI."));
        cmbPredicate.setNullSelectionAllowed(false);
//        layoutGrid.addComponent(cmbPredicate, 1, 2);

        //Add text field for the object of the input predicate
//        layoutGrid.addComponent(new Label("<b>Object</b>", Label.CONTENT_XHTML), 0, 3);
        layoutInputControls.addComponent(new Label("<b>Object</b>", Label.CONTENT_XHTML), 2, 1);

        layoutInputControls.addComponent(cmbPredicate, 1, 2);
        layoutGrid.setSpacing(true);
        layoutGrid.setMargin(true, false, false, false);

        cmbObject = new MyComboBox();
        cmbObject.setWidth(95, Sizeable.UNITS_PERCENTAGE);
        cmbObject.setInputPrompt("Enter the object");
        cmbObject.setReadOnly(false);
        cmbObject.setImmediate(true);
        cmbObject.setNewItemsAllowed(true);
        cmbObject.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_OFF);
//        cmbObject.addItem("http://dbpedia.org/resource/Alfred_Hitchcock");
//        cmbObject.setValue("http://dbpedia.org/resource/Alfred_Hitchcock");
//        cmbObject.addItem("Alfred Hitchcock");
//        cmbObject.setValue("Alfred Hitchcock");
//        cmbObject.setItemCaption("Alfred Hitchcock", "http://dbpedia.org/resource/Alfred_Hitchcock");

        cmbObject.addItem("http://dbpedia.org/resource/Alfred_Hitchcock");
        cmbObject.setItemCaption("http://dbpedia.org/resource/Alfred_Hitchcock", "Alfred Hitchcock");
        cmbObject.setValue("http://dbpedia.org/resource/Alfred_Hitchcock");

        HashMap<String, String> initialObjectList = new HashMap<String, String>();
        initialObjectList.put("http://dbpedia.org/resource/Alfred_Hitchcock", "Alfred Hitchcock");
        cmbObject.itemsList = initialObjectList;

        cmbObject.setRequired(true);
        cmbObject.setRequiredError("Object is required");
        cmbObject.setNullSelectionAllowed(false);
        cmbObject.setNewItemsAllowed(true);

        layoutInputControls.addComponent(cmbObject, 2, 2);

        //Add controls fro the 3 examples
        /*txtExample1Subject = new Label();
        txtExample1Subject.setWidth(95, Sizeable.UNITS_PERCENTAGE);
//        txtExample1Subject.setEnabled(false);
        txtExample1Subject.setValue("http://dbpedia.org/resource/Hector_A._Cafferata,_Jr.");
        txtExample1Subject.setStyleName("v-textfield");

        txtExample1Predicate = new Label();
        txtExample1Predicate.setWidth(95, Sizeable.UNITS_PERCENTAGE);
//        txtExample1Predicate.setEnabled(false);
        txtExample1Predicate.setValue("http://dbpedia.org/ontology/award");
        txtExample1Predicate.setStyleName("v-textfield");

        txtExample1Object = new Label();
        txtExample1Object.setWidth(95, Sizeable.UNITS_PERCENTAGE);
        txtExample1Object.setValue("http://dbpedia.org/resource/Medal_of_Honor");
//        txtExample1Object.setReadOnly(true);
        txtExample1Object.setStyleName("v-textfield");

        btnRunExample1 = new Button("Run Example");
        btnRunExample1.setImmediate(true);
        btnRunExample1.setWidth(100, Sizeable.UNITS_PIXELS);
        btnRunExample1.addListener(new ResultFetcher());
        */

        cmbExample = new ComboBox();
        cmbExample.setWidth(96, Sizeable.UNITS_PERCENTAGE);
        cmbExample.setNewItemsAllowed(false);
        cmbExample.setTextInputAllowed(false);
        cmbExample.setNullSelectionAllowed(false);

        SparqlUtil sparqlEndpointDBpediaLive = new SparqlUtil("http://live.dbpedia.org/sparql", "http://dbpedia.org");

        String subjectLabel = sparqlEndpointDBpediaLive.getEnLabel("http://dbpedia.org/resource/Hector_A._Cafferata,_Jr.");
        String predicateLabel = sparqlEndpointDBpediaLive.getEnLabel("http://dbpedia.org/ontology/award");
        String objectLabel = sparqlEndpointDBpediaLive.getEnLabel("http://dbpedia.org/resource/Medal_of_Honor");

        String tripleInLabels = subjectLabel + " - " + predicateLabel + " - " + objectLabel;//will be displayed to the user
        String tripleInURIs = "http://dbpedia.org/resource/Hector_A._Cafferata,_Jr." + " - " + "http://dbpedia.org/ontology/award"
                + " - " + "http://dbpedia.org/resource/Medal_of_Honor";//will be execute the query itself

        cmbExample.addItem(tripleInURIs);
        cmbExample.setItemCaption(tripleInURIs, tripleInLabels);
        cmbExample.setValue(tripleInURIs);

        subjectLabel = sparqlEndpointDBpediaLive.getEnLabel("http://dbpedia.org/resource/Coliseum_Rock");
        predicateLabel = sparqlEndpointDBpediaLive.getEnLabel("http://dbpedia.org/ontology/artist");
        objectLabel = sparqlEndpointDBpediaLive.getEnLabel("http://dbpedia.org/resource/Starz_%28band%29");

        tripleInLabels = subjectLabel + " - " + predicateLabel + " - " + objectLabel;//will be displayed to the user
        tripleInURIs = "http://dbpedia.org/resource/Coliseum_Rock" + " - " + "http://dbpedia.org/ontology/artist"
                + " - " + "http://dbpedia.org/resource/Starz_%28band%29";//will be execute the query itself

        cmbExample.addItem(tripleInURIs);
        cmbExample.setItemCaption(tripleInURIs, tripleInLabels);
        cmbExample.setValue(tripleInURIs);

        subjectLabel = sparqlEndpointDBpediaLive.getEnLabel("http://dbpedia.org/resource/Paul_Meany");
        predicateLabel = sparqlEndpointDBpediaLive.getEnLabel("http://dbpedia.org/ontology/associatedBand");
        objectLabel = sparqlEndpointDBpediaLive.getEnLabel("http://dbpedia.org/resource/Macrosick");

        tripleInLabels = subjectLabel + " - " + predicateLabel + " - " + objectLabel;//will be displayed to the user
        tripleInURIs = "http://dbpedia.org/resource/Paul_Meany" + " - " + "http://dbpedia.org/ontology/associatedBand"
                + " - " + "http://dbpedia.org/resource/Macrosick";//will be execute the query itself

        cmbExample.addItem(tripleInURIs);
        cmbExample.setItemCaption(tripleInURIs, tripleInLabels);
        cmbExample.setValue(tripleInURIs);



        /*txtExample2Subject = new Label();
        txtExample2Subject.setWidth(95, Sizeable.UNITS_PERCENTAGE);
//        txtExample2Subject.setEnabled(false);
        txtExample2Subject.setValue("http://dbpedia.org/resource/Coliseum_Rock");
        txtExample2Subject.setStyleName("v-textfield");

        txtExample2Predicate = new Label();
        txtExample2Predicate.setWidth(95, Sizeable.UNITS_PERCENTAGE);
//        txtExample2Predicate.setEnabled(false);
        txtExample2Predicate.setValue("http://dbpedia.org/ontology/artist");
        txtExample2Predicate.setStyleName("v-textfield");

        txtExample2Object = new Label();
        txtExample2Object.setWidth(95, Sizeable.UNITS_PERCENTAGE);
//        txtExample2Object.setEnabled(false);
        txtExample2Object.setValue("http://dbpedia.org/resource/Starz_%28band%29");
        txtExample2Object.setStyleName("v-textfield");

        btnRunExample2 = new Button("Run Example");
        btnRunExample2.setImmediate(true);
        btnRunExample2.setWidth(100, Sizeable.UNITS_PIXELS);
        btnRunExample2.addListener(new ResultFetcher());
        */
        /*
        txtExample3Subject = new Label();
        txtExample3Subject.setWidth(95, Sizeable.UNITS_PERCENTAGE);
//        txtExample3Subject.setEnabled(false);
        txtExample3Subject.setValue("http://dbpedia.org/resource/Paul_Meany");
        txtExample3Subject.setStyleName("v-textfield");

        txtExample3Predicate = new Label();
        txtExample3Predicate.setWidth(95, Sizeable.UNITS_PERCENTAGE);
//        txtExample3Predicate.setEnabled(false);
        txtExample3Predicate.setValue("http://dbpedia.org/ontology/associatedBand");
        txtExample3Predicate.setStyleName("v-textfield");

        txtExample3Object = new Label();
        txtExample3Object.setWidth(95, Sizeable.UNITS_PERCENTAGE);
//        txtExample3Object.setEnabled(false);
        txtExample3Object.setValue("http://dbpedia.org/resource/Macrosick");
        txtExample3Object.setStyleName("v-textfield");

        btnRunExample3 = new Button("Run Example");
        btnRunExample3.setImmediate(true);
        btnRunExample3.setWidth(100, Sizeable.UNITS_PIXELS);
        btnRunExample3.addListener(new ResultFetcher());
        */

        /*layoutInputControls.addComponent(txtExample1Subject, 0, 3);
        layoutInputControls.addComponent(txtExample1Predicate, 1, 3);
        layoutInputControls.addComponent(txtExample1Object, 2, 3);
        layoutInputControls.addComponent(btnRunExample1, 3, 3);
        layoutInputControls.setComponentAlignment(txtExample1Subject, Alignment.MIDDLE_LEFT);
        layoutInputControls.setComponentAlignment(txtExample1Predicate, Alignment.MIDDLE_LEFT);
        layoutInputControls.setComponentAlignment(txtExample1Object, Alignment.MIDDLE_LEFT);



        layoutInputControls.addComponent(txtExample2Subject, 0, 4);
        layoutInputControls.addComponent(txtExample2Predicate, 1, 4);
        layoutInputControls.addComponent(txtExample2Object, 2, 4);
        layoutInputControls.addComponent(btnRunExample2, 3, 4);
        layoutInputControls.setComponentAlignment(txtExample2Subject, Alignment.MIDDLE_LEFT);
        layoutInputControls.setComponentAlignment(txtExample2Predicate, Alignment.MIDDLE_LEFT);
        layoutInputControls.setComponentAlignment(txtExample2Object, Alignment.MIDDLE_LEFT);

        layoutInputControls.addComponent(txtExample3Subject, 0, 5);
        layoutInputControls.addComponent(txtExample3Predicate, 1, 5);
        layoutInputControls.addComponent(txtExample3Object, 2, 5);
        layoutInputControls.addComponent(btnRunExample3, 3, 5);
        layoutInputControls.setComponentAlignment(txtExample3Subject, Alignment.MIDDLE_LEFT);
        layoutInputControls.setComponentAlignment(txtExample3Predicate, Alignment.MIDDLE_LEFT);
        layoutInputControls.setComponentAlignment(txtExample3Object, Alignment.MIDDLE_LEFT);*/



        btnRunExample = new Button("Run Example");
        btnRunExample.setImmediate(true);
        btnRunExample.setWidth(100, Sizeable.UNITS_PIXELS);
        btnRunExample.addListener(new ResultFetcher());

        Label lblSelectExample = new Label("<b>Select an example: </b>");
        lblSelectExample.setContentMode(Label.CONTENT_XHTML);

        layoutInputControls.addComponent(lblSelectExample, 0, 3);
        layoutInputControls.addComponent(cmbExample, 1, 3, 2, 3);
        layoutInputControls.addComponent(btnRunExample, 3, 3);


        /*cmbExampleSubject = new ComboBox();
        cmbExampleSubject.addItem("http://dbpedia.org/resource/Thomas_C._Neibaur");
        cmbExampleSubject.addItem("http://dbpedia.org/resource/Rama_Raghoba_Rane");
        cmbExampleSubject.addItem("http://dbpedia.org/resource/G%C3%BCnther_Josten");
        cmbExampleSubject.setValue("http://dbpedia.org/resource/Thomas_C._Neibaur");
        cmbExampleSubject.setNullSelectionAllowed(false);
        cmbExampleSubject.setNewItemsAllowed(true);
        cmbExampleSubject.setWidth(95, Sizeable.UNITS_PERCENTAGE);

        txtExamplePredicate = new TextField();
        txtExamplePredicate.setValue("http://dbpedia.org/ontology/award");
        txtExamplePredicate.setWidth(95, Sizeable.UNITS_PERCENTAGE);
        txtExamplePredicate.setEnabled(false);

        cmbExampleObject = new ComboBox();
        cmbExampleObject.addItem("http://dbpedia.org/resource/WWI_Victory_Medal");
        cmbExampleObject.addItem("http://dbpedia.org/resource/Param_Vir_Chakra");
        cmbExampleObject.addItem("http://dbpedia.org/resource/Knight%27s_Cross_of_the_Iron_Cross>");
        cmbExampleObject.setValue("http://dbpedia.org/resource/WWI_Victory_Medal");
        cmbExampleObject.setNullSelectionAllowed(false);
        cmbExampleObject.setNewItemsAllowed(true);
        cmbExampleObject.setWidth(95, Sizeable.UNITS_PERCENTAGE);

        btnRunExample = new Button("Run Example");
        btnRunExample.setImmediate(true);
        btnRunExample.setWidth(100, Sizeable.UNITS_PIXELS);
        btnRunExample.addListener(new ResultFetcher());

        layoutInputControls.addComponent(cmbExampleSubject, 0, 3);
        layoutInputControls.addComponent(txtExamplePredicate, 1, 3);
        layoutInputControls.addComponent(cmbExampleObject, 2, 3);
        layoutInputControls.addComponent(btnRunExample, 3, 3);   */

        btnGetResults = new Button("Get Results");
        btnGetResults.setImmediate(true);
        btnGetResults.setWidth(100, Sizeable.UNITS_PIXELS);
        btnGetResults.addStyleName("button-container");
        btnGetResults.addListener(new ResultFetcher());
//        layoutGrid.addComponent(btnGetResults, 2, 3);
        layoutInputControls.addComponent(btnGetResults, 3, 2);

        layoutGrid.addComponent(layoutInputControls, 0, 0, 2, 0);
        layoutGrid.setComponentAlignment(layoutInputControls, Alignment.TOP_CENTER);
//        btnTest = new Button("Test Me");
//        btnTest.addListener(new Button.ClickListener() {
//            @Override
//            public void buttonClick(Button.ClickEvent event) {
//                int x = 3;
//                int y = x * 7;
//            }
//        });
//        layoutGrid.addComponent(btnTest, 2, 2);

        //Add text field for the input triple, and use a Horizontal layout for that

//        txtInputTriple = new TextField();
//        txtInputTriple.setEnabled(false);
//        txtInputTriple.setWidth(100, Sizeable.UNITS_PERCENTAGE);
//        txtInputTriple.setValue("");

        lstResults = new ListSelect();

        defactoOverallProgressIndicator = new ProgressIndicator();
        defactoOverallProgressIndicator.setSizeFull();
        defactoOverallProgressIndicator.setWidth(95, Sizeable.UNITS_PERCENTAGE);
        defactoOverallProgressIndicator.setHeight(30, Sizeable.UNITS_PIXELS);
        defactoOverallProgressIndicator.setIndeterminate(false);
        defactoOverallProgressIndicator.setEnabled(true);
        defactoOverallProgressIndicator.setValue(0.0f);
        defactoOverallProgressIndicator.setCaption("Defacto");
        defactoOverallProgressIndicator.setVisible(false);
        defactoOverallProgressIndicator.setPollingInterval(500);
        defactoOverallProgressIndicator.setImmediate(true);

//        layoutGrid.addComponent(defactoOverallProgressIndicator, 1, 4);
        layoutInputControls.addComponent(defactoOverallProgressIndicator, 1, 6);

        cmbSubject.addListener(new ComboBoxTextChangeListener());
        cmbObject.addListener(new ComboBoxTextChangeListener());

        if(chartPanel == null)
            chartPanel = new Panel();

        chartPanel.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        chartPanel.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        chartPanel.setVisible(false);


        return layoutGrid;
    }

    private void showPopup() {
        if(popup == null){
            popupPanel = new Panel();
            Label label = new Label("move your mouse cursor over >HERE<");
            label.setDescription("now move it >HERE<, and then click one the shadow just above description but within the popup");
            popupPanel.addComponent(label);
            popupPanel.setWidth(cmbSubject.getWidth(), cmbSubject.getWidthUnits());

            popup = new PopupView(null, popupPanel);

            popup.setWidth(cmbSubject.getWidth(), cmbSubject.getWidthUnits());

            popupPanel.addComponent(new ComboBox());
            popupPanel.addComponent(new TextField());

            AbsoluteLayout layout = new AbsoluteLayout();
            layout.addComponent(popup);
            layout.setWidth(cmbSubject.getWidth(), cmbSubject.getWidthUnits());
            //layoutGrid.addComponent(layout, 1, 1, 1, 1);
            Window w = new Window();
            layoutGrid.addComponent(w, 1,1,1,1);
        }
        else
            popup.setPopupVisible(true);
    }

    Layout getHeader() {
        GridLayout header = new GridLayout(3, 1);
        header.setColumnExpandRatio(0, 0.2f);
        header.setColumnExpandRatio(1, 0.7f);
        header.setColumnExpandRatio(2, 0.1f);
//        header.setWidth(1400, Sizeable.UNITS_PIXELS);
        header.setMargin(new Layout.MarginInfo(false, false, false, false));

        VerticalLayout titleLayout = new VerticalLayout();
        titleLayout.setWidth(1200, Sizeable.UNITS_PIXELS);
        titleLayout.setMargin(new Layout.MarginInfo(false, true, false, true));
        H1 title = new H1("DeFacto");
        titleLayout.addComponent(title);

        H2 description = new H2("Deep Fact Validation");
        description.setSizeUndefined();
        titleLayout.addComponent(description);
        titleLayout.setExpandRatio(description, 1f);
//        titleLayout.setWidth(80, Sizeable.UNITS_PERCENTAGE);
        header.addComponent(titleLayout, 0, 0);

        HorizontalLayout layoutCreditsAndLogo = new HorizontalLayout();

        VerticalLayout creditsLayout = new VerticalLayout();
        creditsLayout.setMargin(new Layout.MarginInfo(false, true, true, true));
        creditsLayout.setSizeUndefined();

//        Link dummyLink = new Link(" ", new ExternalResource(""));
//        dummyLink.setTargetName("_blank");
//        creditsLayout.addComponent(dummyLink);
//        creditsLayout.setComponentAlignment(dummyLink, Alignment.TOP_LEFT);

        Link credit1Link = new Link("Jens Lehmann ", new ExternalResource("http://jens-lehmann.org/"));
        credit1Link.setTargetName("_blank");
        creditsLayout.addComponent(credit1Link);
        creditsLayout.setComponentAlignment(credit1Link, Alignment.TOP_LEFT);

        Link credit2Link = new Link("Daniel Gerber ", new ExternalResource("http://bis.informatik.uni-leipzig.de/DanielGerber"));
        credit2Link.setTargetName("_blank");
        creditsLayout.addComponent(credit2Link);
        creditsLayout.setComponentAlignment(credit2Link, Alignment.TOP_LEFT);

        Link credit3Link = new Link("Mohamed Morsey ", new ExternalResource("http://bis.informatik.uni-leipzig.de/MohamedMorsey"));
        credit3Link.setTargetName("_blank");
        creditsLayout.addComponent(credit3Link);
        creditsLayout.setComponentAlignment(credit3Link, Alignment.TOP_LEFT);

        Link credit4Link = new Link("Axel-C. Ngonga Ngomo", new ExternalResource("http://bis.uni-leipzig.de/AxelNgonga"));
        credit4Link.setTargetName("_blank");
        creditsLayout.addComponent(credit4Link);
        creditsLayout.setComponentAlignment(credit4Link, Alignment.TOP_LEFT);

        layoutCreditsAndLogo.addComponent(creditsLayout);


        Link akswGroupLogo = new Link("", new ExternalResource("http://aksw.org"));
        akswGroupLogo.setTargetName("_blank");

        akswGroupLogo.setIcon(new FileResource(new File("web/images/aksw_logo.svg"), this));
        akswGroupLogo.setSizeUndefined();
        akswGroupLogo.setStyleName(Reindeer.LABEL_H1);
        VerticalLayout akswGroupLayout = new VerticalLayout();
        akswGroupLayout.addComponent(akswGroupLogo);
        akswGroupLayout.setComponentAlignment(akswGroupLogo, Alignment.TOP_RIGHT);
        akswGroupLayout.setMargin(new Layout.MarginInfo(false, false, false, false));
        akswGroupLayout.setSizeUndefined();

        layoutCreditsAndLogo.addComponent(akswGroupLayout);

        Label lblEmptySpace = new Label("&nbsp;", Label.CONTENT_XHTML);
        lblEmptySpace.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        HorizontalLayout hl = new HorizontalLayout();
//        hl.setWidth(920, Sizeable.UNITS_PIXELS);
        hl.addComponent(lblEmptySpace);
        hl.setExpandRatio(lblEmptySpace, 1f);
        header.addComponent(hl, 1, 0);


        header.addComponent(layoutCreditsAndLogo, 2, 0);
        header.setComponentAlignment(layoutCreditsAndLogo, Alignment.TOP_RIGHT);

        return header;

        /*VerticalLayout footer = new VerticalLayout();
        mainLayout.addComponentAsFirst(footer);
        footer.setWidth(1400, Sizeable.UNITS_PIXELS );
        footer.setMargin(new Layout.MarginInfo(false, false, false, false));

        HorizontalLayout projectWebPageLayout = new HorizontalLayout();
        projectWebPageLayout.setMargin(new Layout.MarginInfo(false, true, true, true));
        projectWebPageLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        //projectWebPageLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        projectWebPageLayout.setSizeUndefined();
        Label lblProjectHomepage = new Label("<b>More information about the project can be found &nbsp;</b>");
        lblProjectHomepage.setContentMode(Label.CONTENT_XHTML);
//        projectWebPageLayout.addComponent(lblProjectHomepage);
//        projectWebPageLayout.setComponentAlignment(lblProjectHomepage, Alignment.MIDDLE_CENTER);

        Link projectLink = new Link("here", new ExternalResource("http://aksw.org/projects/DeFacto"));
        projectLink.setTargetName("_blank");
//        projectLink.setStyleName();
        projectWebPageLayout.addComponent(lblProjectHomepage);
        projectWebPageLayout.setComponentAlignment(lblProjectHomepage, Alignment.MIDDLE_LEFT);

        Label lblDummy = new Label("TEST LABEL");
        projectWebPageLayout.addComponent(lblDummy);
        projectWebPageLayout.setComponentAlignment(lblDummy, Alignment.MIDDLE_RIGHT);


        footer.addComponent(projectWebPageLayout);
        footer.setComponentAlignment(projectWebPageLayout, Alignment.MIDDLE_CENTER);

        return footer;*/
    }

    Layout getFooter() {
        VerticalLayout footer = new VerticalLayout();
        footer.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        footer.setMargin(new Layout.MarginInfo(false, false, false, false));

        HorizontalLayout projectWebPageLayout = new HorizontalLayout();
        projectWebPageLayout.setMargin(new Layout.MarginInfo(false, true, true, true));
        //projectWebPageLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        projectWebPageLayout.setSizeUndefined();
        Label lblProjectHomepage = new Label("<b>More information about the project can be found &nbsp;</b>");
        lblProjectHomepage.setContentMode(Label.CONTENT_XHTML);
        projectWebPageLayout.addComponent(lblProjectHomepage);
        projectWebPageLayout.setComponentAlignment(lblProjectHomepage, Alignment.MIDDLE_CENTER);

        Link projectLink = new Link("here", new ExternalResource("http://aksw.org/projects/DeFacto"));
        projectLink.setTargetName("_blank");
//        projectLink.setStyleName();
        projectWebPageLayout.addComponent(projectLink);
        projectWebPageLayout.setComponentAlignment(projectLink, Alignment.MIDDLE_CENTER);

        footer.addComponent(projectWebPageLayout);
        footer.setComponentAlignment(projectWebPageLayout, Alignment.MIDDLE_CENTER);

        return footer;
    }


    String formulateNTriplesFromInput(){

        String subject = cmbSubject.getValue().toString();
        String predicate = cmbPredicate.getValue().toString();
        String object = cmbObject.getValue().toString();

        //Remove all angle brackets, as may the use enters one of them and forgets another one,
        //we will remove them all, and reinsert them upon processing
        subject = subject.replaceAll("<","").replaceAll(">","");
        predicate = predicate.replaceAll("<","").replaceAll(">","");

        //This variable indicates that it's a URI, and so we can prepend and append the angle brackets to it.
        if(object.startsWith("<") || object.endsWith(">")){
            object = object.replaceAll("<","").replaceAll(">","");
        }


        boolean isURI = false;


        try{
            //If a URI is successfully created then we can conclude, that the object is URI
            URI objectURI = new URI(object);
            isURI = true;
        }
        catch(Exception exp){
            //If a URI is not a valid URI, then it is a literal
            isURI = false;
        }

        subject = "<" + subject + ">";
        predicate = "<" + predicate + ">";

        if(isURI)
            object = "<" + object + ">";

        return  subject + " " + predicate + " " + object + ".";
    }



    private Panel createPageRankChart(CategoryDataset pageRankDataset){

        pageRankChart = ChartFactory.createBarChart("", // Title
                "", // x-axis Label
                "PageRank", // y-axis Label
                pageRankDataset, // Dataset
                PlotOrientation.HORIZONTAL, // Plot Orientation
                false, // Show Legend
                false, // Use tooltips
                false // Configure chart to generate URLs?
        );

        JFreeChartWrapper wrapper = new JFreeChartWrapper(pageRankChart);


        chartPanel.addComponent(wrapper);

        chartPanel.setScrollable(true);
        ((BarRenderer)pageRankChart.getCategoryPlot().getRenderer()).setMaximumBarWidth(0.2);
        ((BarRenderer)pageRankChart.getCategoryPlot().getRenderer()).setShadowVisible(false);
        ((BarRenderer)pageRankChart.getCategoryPlot().getRenderer()).setItemMargin(-2);
        pageRankChart.getCategoryPlot().setBackgroundPaint(Color.WHITE);
        pageRankChart.getCategoryPlot().getRangeAxis(0).setRange(0d, 10d);
        ((NumberAxis)pageRankChart.getCategoryPlot().getRangeAxis(0)).setTickUnit(new NumberTickUnit(1));

        wrapper.setHeight(100, Sizeable.UNITS_PIXELS);
        wrapper.setWidth(300, Sizeable.UNITS_PIXELS);

        return chartPanel;
    }

    /**
     * Creates a dataset suitable for the chart
     * @param pageRank  The page rank that should be used in the chart
     * @return  Dataset containing the PageRank, but to be used in the chart
     */
    private CategoryDataset createDataset(int pageRank) {
        double[][] data = new double[][] {
                {pageRank},
        };
        return DatasetUtilities.createCategoryDataset("",
                "", data);
    }



    class H1 extends Label {
        public H1(String caption) {
            super(caption);
            setSizeUndefined();
            setStyleName(Reindeer.LABEL_H1);
        }
    }
    class H2 extends Label {
        public H2(String caption) {
            super(caption);
            setSizeUndefined();
            setStyleName(Reindeer.LABEL_H2);
        }
    }


    class ResultFetcher implements Button.ClickListener{
        public void buttonClick(Button.ClickEvent event) {
            //Show the progress bar
            defactoOverallProgressIndicator.setVisible(true);
            defactoOverallProgressIndicator.setValue(0.0f);
            defactoOverallProgressIndicator.setCaption("Calculating DeFacto Scores");

            String subject, predicate, object;

            if(event.getButton() == btnGetResults){
//                subject = cmbSubject.getValue().toString().trim();
                subject = cmbSubject.getSelectedItemCaption();
                predicate = cmbPredicate.getValue().toString().trim();
//                object = cmbObject.getValue().toString().trim();
                object = cmbObject.getSelectedItemCaption();
            }
            else if(event.getButton() == btnRunExample){
                /*subject = txtExample1Subject.getValue().toString().trim();
                predicate = txtExample1Predicate.getValue().toString().trim();
                object = txtExample1Object.getValue().toString().trim();*/
                String fullTriple = cmbExample.getValue().toString().trim();

                String []tripleParts = fullTriple.split("-");

                subject = tripleParts[0].trim();
                predicate = tripleParts[1].trim();
                object = tripleParts[2].trim();

            }
            /*else if(event.getButton() == btnRunExample2){
                subject = txtExample2Subject.getValue().toString().trim();
                predicate = txtExample2Predicate.getValue().toString().trim();
                object = txtExample2Object.getValue().toString().trim();
            }
            else if(event.getButton() == btnRunExample3){
                subject = txtExample3Subject.getValue().toString().trim();
                predicate = txtExample3Predicate.getValue().toString().trim();
                object = txtExample3Object.getValue().toString().trim();
            }*/
            else //undefined button (should not happen)
                return;

            getData(subject, predicate, object);
        }




        private void getData(String subject, String predicate, String object){

            //////////////////////////////////////////////////////
//            layoutGrid.removeComponent(0, 5);

//            layoutGrid.removeComponent(0, 6);
            layoutGrid.removeComponent(0, 1);
            FactCheckerCaller caller = new FactCheckerCaller(subject, predicate, object);
            caller.start();

        }

    }






    private class ViewWebsiteHandler implements  Field.ValueChangeListener {

        public void valueChange(Property.ValueChangeEvent valueChangeEvent) {

            //If non or empty item is selected, then we should clear the browserView
            if(lstResults.getValue() == null){
                browserView.setSource(new ExternalResource(""));
                return;
            }

            String strSelectedItem = lstResults.getValue().toString();
            String websiteOfSelectedItem = "";

            //Determine the website of the selected item

            if(strSelectedItem.compareTo("") != 0){
                for(WebSite resultWebsite: arrResultingWebsites){
                    if(resultWebsite.getTitle().compareTo(strSelectedItem) == 0){

                        browserView.setSource(new ExternalResource(resultWebsite.getUrl()));

                        break;
                    }

                }
            }

        }

    }

    private class InputTriplePartsChange implements FieldEvents.TextChangeListener{

        public void textChange(FieldEvents.TextChangeEvent textChangeEvent) {
            //To change body of implemented methods use File | Settings | File Templates.
            txtInputTriple.setValue(formulateNTriplesFromInput());
        }
    }

    private class ViewStats implements Field.ValueChangeListener{

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            //If non or empty item is selected, then we should clear the browserView
            chartPanel.removeAllComponents();
            chartPanel.setVisible(false);

            if(lstResultingWebsites.getValue() == null){
                browserView.setSource(new ExternalResource(""));
                return;
            }

            int pageRankOfSelectedWebsite = ((WebSite)lstResultingWebsites.getValue()).getPageRank();

            pageRankOfSelectedWebsite = pageRankOfSelectedWebsite >= 0 ? pageRankOfSelectedWebsite : 0;
            pageRankOfSelectedWebsite = pageRankOfSelectedWebsite <= 10 ? pageRankOfSelectedWebsite : 10;

            createPageRankChart(createDataset(pageRankOfSelectedWebsite));
            browserView.setSource(new ExternalResource(((WebSite)lstResultingWebsites.getValue()).getUrl()));
            chartPanel.setVisible(true);

        }
    }

    private class ResultWebpage
    {
        private final String title;
        private final String url;

        @Override public String toString() {return title;}

        public ResultWebpage(String title, String url)
        {
            this.title=title;
            this.url=url;
        }

        public String getTitle() {
            return title;
        }

        public String getURL() {
            return url;
        }
    }

    class FactCheckerCaller extends Thread
    {
        private Evidence resultingEvidence;

        String subject, predicate, object;

        public String getSubject() {
            return subject;
        }

        public String getPredicate() {
            return predicate;
        }

        public String getObject() {
            return object;
        }

        public FactCheckerCaller(String subject, String predicate, String object){
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
        }

        @Override
        public void run() {

            btnGetResults.setEnabled(false);//Disable the button, in order to avoid thread conflict
            cmbSubject.setEnabled(false);
            cmbObject.setEnabled(false);
            cmbPredicate.setEnabled(false);
            btnRunExample.setEnabled(false);
//            btnRunExample2.setEnabled(false);
//            btnRunExample3.setEnabled(false);

            SparqlUtil sparqlEndpointDBpediaLive = new SparqlUtil("http://live.dbpedia.org/sparql", "http://dbpedia.org");



            //This is the actual model used for querying defacto
            Model inputTripleModel = ModelFactory.createDefaultModel();
            inputTripleModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(subject),
                    ResourceFactory.createProperty(predicate),
                    inputTripleModel.createResource(object)));


            String subjectLabel, predicateLabel, objectLabel;

            subjectLabel = sparqlEndpointDBpediaLive.getEnLabel(subject);
            objectLabel = sparqlEndpointDBpediaLive.getEnLabel(object);
            predicateLabel = sparqlEndpointDBpediaLive.getEnLabel(predicate);

            inputTripleModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(subject),
                    ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),
                    inputTripleModel.createLiteral(subjectLabel, "en") ));
            //model.createLiteral(sparqlEndpointDBpediaLive.getEnLabel(subject), "en"))

            inputTripleModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(object),
                    ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),
                    inputTripleModel.createLiteral(objectLabel, "en")));

            inputTripleModel.setNsPrefix("name", "defacto");//Required for the backend to work properly !!!


            //Start a timer to take control fo the progress indicator
            //Timer progressTimer = new Timer("DeFacto Overall Progress");
            DefactoProgressIndicatorThread startingProgressIndicatorThread = new DefactoProgressIndicatorThread(0.7f);
            startingProgressIndicatorThread.start();
            try{
                Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(new File("/home/mohamed/LeipzigUniversity/JavaProjects/test/DeFacto/defacto-core/defacto.ini")));
            }
            catch (Exception exp){

            }
            resultingEvidence = Defacto.checkFact(inputTripleModel);

            defactoOverallProgressIndicator.setValue(0.7f);
            startingProgressIndicatorThread.interrupt();


            defactoOverallProgressIndicator.setCaption("Rendering Resulting Pages");
            DefactoProgressIndicatorThread endingProgressIndicatorThread = new DefactoProgressIndicatorThread(0.95f);
            endingProgressIndicatorThread.start();
            //reschedule the timer to start again but with limit 1
//            progressTimer.schedule(new DefactoProgressIndicatorHandler(), 500);
            //Clear the ArrayList and Combobox
            arrResultingWebsites.clear();
            lstResults.removeAllItems();
//            layoutGrid.removeComponent(0, 5);

//            String tripleToValidate = createOutputTripleFromModel(inputTripleModelForOutput, "TURTLE");

            /*SearchResultRepeater resultsView = new SearchResultRepeater(resultingEvidence,
                    cmbSubject.getValue().toString().trim(), cmbPredicate.getValue().toString(),
                                                            cmbObject.getValue().toString().trim());*/

            //Display the labels of subject, predicate, and object instead of the URIs
            TripleComponent component = new TripleComponent(subject, predicate, object, subjectLabel, predicateLabel,
                    objectLabel);

            SearchResultRepeater resultsView = new SearchResultRepeater(resultingEvidence, component);


//            layoutGrid.removeComponent(0, 6);
            layoutGrid.addComponent(resultsView, 0, 1, 2, 1);
            resultsView.setWidth(1200, Sizeable.UNITS_PIXELS);
            layoutGrid.setComponentAlignment(resultsView, Alignment.TOP_CENTER);
//            layoutGrid.addComponent(createHeaderTitle(resultingEvidence.getAllWebSites().size(), tripleToValidate), 0, 4, 2, 4);
            resultsView.displayResults();

            defactoOverallProgressIndicator.setValue(1.0f);
//            defactoOverallProgressIndicator.setVisible(false);

            endingProgressIndicatorThread.interrupt();
            defactoOverallProgressIndicator.setVisible(false);
            defactoOverallProgressIndicator.setValue(0.0f);

            btnGetResults.setEnabled(true);//Reenable the button, as the process is completed
            cmbSubject.setEnabled(true);
            cmbObject.setEnabled(true);
            cmbPredicate.setEnabled(true);

            btnRunExample.setEnabled(true);
//            btnRunExample2.setEnabled(true);
//            btnRunExample3.setEnabled(true);

            defactoOverallProgressIndicator.setVisible(false);
        }

        public Evidence getResultingEvidence() {
            return resultingEvidence;
        }

    }


    /**
     * This class is in charge of incrementing the value of the progress bar indicating the progress of the overall
     * process
     */
    private class DefactoProgressIndicatorThread extends Thread {

        //This value indicates the value, which the progress indicator is not allowed to exceed even if the time passes
        private float progressUpperLimit;

        private DefactoProgressIndicatorThread(float progressUpperLimit) {
            this.progressUpperLimit = progressUpperLimit;
        }

        private DefactoProgressIndicatorThread() {
            this(1.0f);
        }

        public float getProgressUpperLimit() {
            return progressUpperLimit;
        }

        @Override
        public void run() {
            try{


                while(true){
                    float currentProgressValue = (Float) defactoOverallProgressIndicator.getValue();
                    currentProgressValue+= 0.01;

                    //Check if it exceeds the maximum value allowed
                    currentProgressValue = currentProgressValue > progressUpperLimit ? progressUpperLimit : currentProgressValue;

                    defactoOverallProgressIndicator.setValue(currentProgressValue);
                    defactoOverallProgressIndicator.requestRepaint();


                    Thread.sleep(500);
                }

            }
            catch (InterruptedException exp){
                Thread.currentThread().interrupt();
            }


        }
    }


}
