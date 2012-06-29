package org.aksw.forms;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.jensjansson.pagedtable.PagedTable;
import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;
import org.aksw.InformationFinder.TripleResultFinder;
import org.aksw.gui.MyComboBox;
import org.aksw.gui.SearchResultRepeater;
import org.aksw.handlers.ComboBoxTextChangeListener;
import org.aksw.helper.SPARQL;
import org.aksw.provenance.FactChecker;
import org.aksw.provenance.boa.Pattern;
import org.aksw.provenance.evidence.Evidence;
import org.aksw.provenance.evidence.WebSite;
import org.aksw.results.SearchResult;
import org.aksw.validators.UriValidator;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
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
    private TabSheet tabs;

    //This is the TextFiled that will receive the input triple to search for
    private TextField txtInputTriple;
    private TextField txtBoaThreshold;
    private TextField txtNoOfSearchResultsPerQuery;
    //private TextField cmbSubject;
    private MyComboBox cmbSubject;
    private TextField txtPredicate;
    private MyComboBox cmbObject;
    private TextField txtLowestPageRank;
    private Button btnGetResults;
    private Button btnViewWebsite;
    private GridLayout layoutGrid;
    private Embedded browserView;
    PagedTable lstResultingWebsites;
    JFreeChart pageRankChart;
    //ComboBox for results
    private ListSelect lstResults;
    private Panel chartPanel = null;

    private Panel popupPanel;
    private PopupView popup;
    private ProgressIndicator subjectProgressIndicator;

    private ArrayList<WebSite> arrResultingWebsites = new ArrayList<WebSite>();

    ArrayList<SearchResult> arrSearchResults = new ArrayList<SearchResult>();

//    ArrayList<UriWithLabel> arrSubjects = new ArrayList<UriWithLabel>();
//    ArrayList<UriWithLabel> arrObjects = new ArrayList<UriWithLabel>();



    @Override
    public void init() {
        setTheme("reindeer");
        mainWindow = new Window("DeFacto");
//        mainWindow.setWidth(100, Sizeable.UNITS_PERCENTAGE);
//        mainWindow.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        mainLayout = (VerticalLayout) mainWindow.getContent();
        mainLayout.setMargin(false);
        setMainWindow(mainWindow);

        final Window mywindow = new Window("Second Window");

        // Manually set the name of the window.
        mywindow.setName("mywindow");

        // Add some content to the window.
        mywindow.addComponent(new Label("Has content."));

        // Add the window to the application.
        addWindow(mywindow);

        //mywindow.open(new ExternalResource("www.yahoo.com"), "_new");
        buildMainView();

    }

    void buildMainView() {
        mainLayout.setSizeFull();
        /*mainLayout.addComponent(getTopMenu());*/
        mainLayout.addComponent(getHeader());
        CssLayout margin = new CssLayout();
        margin.setMargin(false, true, true, true);
        margin.setSizeFull();
        /*tabs = new TabSheet();
        tabs.setSizeFull();
        margin.addComponent(tabs);*/
        Panel p = new Panel();
        p.setSizeFull();
        //AbsoluteLayout p2 = new AbsoluteLayout();
        margin.addComponent(p);
        mainLayout.addComponent(margin);
        mainLayout.setExpandRatio(margin, 1);

        //tabs.addComponent(buildMainFormUsingGridLayout());
        p.addComponent(buildMainFormUsingGridLayout());
        p.setScrollable(true);
        //p2.addComponent(showBarBasic());
        //p.setFirstComponent(showBarBasic());
    }

    Layout buildMainFormUsingGridLayout() {

        layoutGrid = new GridLayout(3,11);
//        layoutGrid.setCaption("AKSW Information Finder");
        layoutGrid.setMargin(false);
        layoutGrid.setSpacing(false);
        layoutGrid.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        layoutGrid.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        layoutGrid.setColumnExpandRatio(0, 0.2f);
        layoutGrid.setColumnExpandRatio(1, 0.55f);
        layoutGrid.setColumnExpandRatio(2, 0.25f);
        layoutGrid.setRowExpandRatio(8, 0.3f);

        //Add text field for the BOA threshold, and use a Horizontal layout for that
//        layoutGrid.addComponent(new Label("BOA Confidence Threshold", Label.CONTENT_XHTML), 0, 0);
//        txtBoaThreshold = new TextField();
//        txtBoaThreshold.setWidth(10, Sizeable.UNITS_PERCENTAGE);
//        txtBoaThreshold.setInputPrompt("0.9");
//        txtBoaThreshold.setValue("0.9");
//        txtBoaThreshold.setRequired(true);
//        txtBoaThreshold.setRequiredError("BOA confidence threshold value is required");
//        txtBoaThreshold.setValidationVisible(true);
//        txtBoaThreshold.addValidator(new BoaConfidenceThresholdValidator("Invalid double {0}"));
//        layoutGrid.addComponent(txtBoaThreshold, 1, 0);



        //Add text field for the lowest PageRank allowed, and use a Horizontal layout for that
//        layoutGrid.addComponent(new Label("Lowest Page Rank", Label.CONTENT_XHTML), 0, 1);
//        txtLowestPageRank = new TextField();
//        txtLowestPageRank.setWidth(10, Sizeable.UNITS_PERCENTAGE);
//        txtLowestPageRank.setInputPrompt("4");
//        txtLowestPageRank.setValue("4");
//        txtLowestPageRank.setRequired(true);
//        txtLowestPageRank.setRequiredError("Lowest PageRank is required");
//        txtLowestPageRank.addValidator(new PageRankValidator("Invalid PageRank {0})"));
//        layoutGrid.addComponent(txtLowestPageRank, 1, 1);



        //Add text field for the number of search results per query, and use a Horizontal layout for that
//        layoutGrid.addComponent(new Label("No. of Search Results Per Query", Label.CONTENT_XHTML), 0, 2);
//
//        txtNoOfSearchResultsPerQuery = new TextField();
//        txtNoOfSearchResultsPerQuery.setWidth(10, Sizeable.UNITS_PERCENTAGE);
//        txtNoOfSearchResultsPerQuery.setInputPrompt("5");
//        txtNoOfSearchResultsPerQuery.setValue("25");
//        txtNoOfSearchResultsPerQuery.setRequired(true);
//        txtNoOfSearchResultsPerQuery.setRequiredError("Number of search results per query is required");
//        txtNoOfSearchResultsPerQuery.addValidator(new NumberOfSearchResultsValidator("Invalid Number of search results {0})"));
//        layoutGrid.addComponent(txtNoOfSearchResultsPerQuery, 1, 2);


        //Add text field for the subject of the input predicate
        layoutGrid.addComponent(new Label("Subject", Label.CONTENT_XHTML), 0, 0);

//        cmbSubject = new TextField();
        cmbSubject = new MyComboBox();
        ///////////////////
        cmbSubject.setReadOnly(false);
        cmbSubject.setImmediate(true);
        cmbSubject.setNewItemsAllowed(true);
        ////////////////////
        cmbSubject.setWidth(60, Sizeable.UNITS_PERCENTAGE);
        cmbSubject.setInputPrompt("Enter the subject");
        cmbSubject.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
        cmbSubject.setValue("http://dbpedia.org/resource/Bloom_06");
        cmbSubject.addItem("http://dbpedia.org/resource/Bloom_06");
        cmbSubject.setRequired(true);
        cmbSubject.setRequiredError("Subject is required");
        cmbSubject.addValidator(new UriValidator("Invalid URI."));


        /*HorizontalLayout hlSubjectAndIndicator = new HorizontalLayout();
        hlSubjectAndIndicator.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        subjectProgressIndicator = new ProgressIndicator(new Float(0.0));
        subjectProgressIndicator.setIndeterminate(true);
        subjectProgressIndicator.setVisible(false);

        hlSubjectAndIndicator.addComponent(cmbSubject);
        hlSubjectAndIndicator.addComponent(subjectProgressIndicator);

        hlSubjectAndIndicator.setSizeFull();*/

        layoutGrid.addComponent(cmbSubject, 1, 0);

        /*cmbSubject.addListener(new FieldEvents.BlurListener()
        {
            @Override
            public void blur(FieldEvents.BlurEvent event)
            {
                Object objData = cmbSubject.getValue();
                if(objData != null){
                    String str = objData.toString();
                }
            }
        });*/
        //Add text field for the predicate of the input predicate
        layoutGrid.addComponent(new Label("Predicate", Label.CONTENT_XHTML), 0, 1);

        layoutGrid.setSpacing(true);
        layoutGrid.setMargin(true, false, false, false);

        txtPredicate = new TextField();
        txtPredicate.setWidth(60, Sizeable.UNITS_PERCENTAGE);
        txtPredicate.setInputPrompt("Enter the predicate");
        txtPredicate.setValue("http://dbpedia.org/ontology/associatedBand");
        txtPredicate.setRequired(true);
        txtPredicate.setRequiredError("Predicate is required");
        txtPredicate.addValidator(new UriValidator("Invalid URI."));
        layoutGrid.addComponent(txtPredicate, 1, 1);

        //Add text field for the object of the input predicate
        layoutGrid.addComponent(new Label("Object", Label.CONTENT_XHTML), 0, 2);

        layoutGrid.setSpacing(true);
        layoutGrid.setMargin(true, false, false, false);

        cmbObject = new MyComboBox();
        cmbObject.setWidth(60, Sizeable.UNITS_PERCENTAGE);
        cmbObject.setInputPrompt("Enter the object");
        cmbObject.setReadOnly(false);
        cmbObject.setImmediate(true);
        cmbObject.setNewItemsAllowed(true);
        cmbObject.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
        cmbObject.setValue("http://dbpedia.org/resource/Eiffel_65");
        cmbObject.addItem("http://dbpedia.org/resource/Eiffel_65");
        cmbObject.setRequired(true);
        cmbObject.setRequiredError("Object is required");

        cmbObject.setNewItemsAllowed(true);


        layoutGrid.addComponent(cmbObject, 1, 2);
        //Add button "GetResult"
        btnGetResults = new Button("Get Results");
        //btnGetResults.addListener(this);
        btnGetResults.addListener(new GetResultHandler());
        layoutGrid.addComponent(btnGetResults, 2, 2);


        //Add text field for the input triple, and use a Horizontal layout for that
//        layoutGrid.addComponent(new Label("Triple", Label.CONTENT_XHTML), 0, 3);

        txtInputTriple = new TextField();
        txtInputTriple.setEnabled(false);
        txtInputTriple.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        txtInputTriple.setValue("");
//        layoutGrid.addComponent(txtInputTriple, 1, 3);
        lstResults = new ListSelect();

        /*//Add a ComboBox for results
        layoutGrid.addComponent(new Label("Results", Label.CONTENT_XHTML), 0, 7);
        lstResults = new ListSelect();
        lstResults.addListener(new ViewWebsiteHandler());
        lstResults.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        lstResults.setRows(6);
        lstResults.setImmediate(true);
        layoutGrid.addComponent(lstResults, 1, 7);
        */

        //Add a table for results
        /*lstResultingWebsites = new Table();
        lstResultingWebsites.setImmediate(true); //TODO: is it better (faster) with this outcommented? NOPE!
        lstResultingWebsites.setSelectable(true);
        lstResultingWebsites.setMultiSelect(true);
        lstResultingWebsites.setColumnCollapsingAllowed(true);

        lstResultingWebsites.setRowHeaderMode(Table.ROW_HEADER_MODE_ICON_ONLY);
        lstResultingWebsites.setPageLength(0);
        lstResultingWebsites.setHeight("100%");
        lstResultingWebsites.setWidth("160px");
        lstResultingWebsites.setSortAscending(true);
        lstResultingWebsites.setSortContainerPropertyId("Website");
        lstResultingWebsites.setContainerDataSource(new IndexedContainer());
        lstResultingWebsites.setVisibleColumns(new String[]{"Website", "Feature"});
        lstResultingWebsites.setColumnHeaders(new String[]{"Website", "Feature"});
        lstResultingWebsites.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        layoutGrid.addComponent(lstResultingWebsites, 1, 7);
        */

        // Have a table that allows dragging from
//        layoutGrid.addComponent(new Label("Results", Label.CONTENT_XHTML), 0, 4);
//        lstResultingWebsites = new PagedTable("");
//        lstResultingWebsites.setWidth(100, Sizeable.UNITS_PERCENTAGE);
//        lstResultingWebsites.setDragMode(Table.TableDragMode.ROW);
//        lstResultingWebsites.setSelectable(true);
//        lstResultingWebsites.setImmediate(true);
//        // Initialize the table container
//        ArrayList<WebSite> collection =
//                new ArrayList<WebSite>();
//        collection.add(new WebSite(null, null));
//        final BeanItemContainer<WebSite> tableContainer =
//                new BeanItemContainer<WebSite> (WebSite.class, collection);
//        lstResultingWebsites.setContainerDataSource(tableContainer);
//        lstResultingWebsites.setVisibleColumns(new String[]{"title", "url", "pageRank"});
//        lstResultingWebsites.removeAllItems();
//        lstResultingWebsites.addListener(new ViewStats());
//        lstResultingWebsites.setPageLength(10);
//        lstResultingWebsites.setVisibleColumns(new String[]{});
//
//        // Allow the table to receive drops and handle them
//
//        layoutGrid.addComponent(lstResultingWebsites, 1, 4);
//        layoutGrid.addComponent(lstResultingWebsites.createControls(), 1, 5);
//
//        lstResultingWebsites.setPageLength(10);


        //btnViewWebsite = new Button("View Website");
        //btnViewWebsite.addListener(new ViewWebsiteHandler());
        //layoutGrid.addComponent(btnViewWebsite, 2, 7);

        //Add text field for the BOA threshold, and use a Horizontal layout for that
//        Panel panel = new Panel("Website content");
//        try{
//            browserView = new Embedded("", null);
//            browserView.setWidth(100, Sizeable.UNITS_PERCENTAGE);
//            browserView.setHeight(100, Sizeable.UNITS_PERCENTAGE);
//            browserView.setType(Embedded.TYPE_BROWSER);
//            panel.addComponent(browserView);
//        }
//        catch(Exception exp) {
//
//        }
//        panel.setWidth(100, Sizeable.UNITS_PERCENTAGE); // Defined width.
//        panel.setHeight(200, Sizeable.UNITS_PIXELS); // Defined Height.
//
//        layoutGrid.addComponent(panel, 0, 6, 2, 6);

        //cmbSubject.addListener(new AutoCompleteSPARQLTextBox());

        /*cmbSubject.addListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(FieldEvents.TextChangeEvent event) {

                String subjectLabelPart = event.getText();

                //Only if the user entered more than 2 letters
                if (subjectLabelPart.length() > 2) {
                    cmbSubject.removeAllItems();
                    subjectProgressIndicator.setVisible(true);
                    subjectProgressIndicator.requestRepaint();
                    String queryString = "select ?s ?o where {?s rdfs:label ?o. FILTER REGEX(?o, \"^" + subjectLabelPart + "\",\"i\") } limit 10";

                    SPARQL sparqlEndpointDBpediaLive = new SPARQL("http://live.dbpedia.org/sparql", "http://dbpedia.org");

                    ResultSet potentialLabels = sparqlEndpointDBpediaLive.executeSelectQuery(queryString);
                    while (potentialLabels.hasNext()) {

                        QuerySolution slnSubjectWithLabel = potentialLabels.next();

                        //Add the subject with its label to the arraylist of subjects
//                        arrSubjects.add(new UriWithLabel(slnSubjectWithLabel.get("s").toString(), slnSubjectWithLabel.get("o").toString()));

                        cmbSubject.addItem(slnSubjectWithLabel.get("o").toString());
                    }

                    cmbSubject.requestRepaint();
                    subjectProgressIndicator.setVisible(false);
                    subjectProgressIndicator.requestRepaint();
                }


            }
        });*/
        cmbSubject.addListener(new ComboBoxTextChangeListener());
        cmbObject.addListener(new ComboBoxTextChangeListener());

//        cmbObject.addListener(new AutoCompleteSPARQLTextBox());


        //layoutGrid.addComponent(createPageRankChart(), 0, 9);
        //layoutGrid.addComponent(showBarBasic(), 1, 9);
        //showAreaWithNegValues();

        /*hl.addComponent(nat);
        hl.addComponent(list);
        hl.addComponent(twincol);*/

        if(chartPanel == null)
            chartPanel = new Panel();

        layoutGrid.addComponent(chartPanel, 2, 4);
        chartPanel.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        chartPanel.setHeight(100, Sizeable.UNITS_PERCENTAGE);
        chartPanel.setVisible(false);
//        createPageRankChart(createDataset(4));
        //createPageRankChart(createDataset(1));

        return layoutGrid;
    }

    private void showPopup() {
        if(popup == null){
            popupPanel = new Panel();
            //popupPanel.setWidth(cmbSubject.getWidth());
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
        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setMargin(true);
        header.setSpacing(true);
        // header.setStyleName(Reindeer.LAYOUT_BLACK);
        CssLayout titleLayout = new CssLayout();
        H1 title = new H1("DeFacto");
        titleLayout.addComponent(title);
        H2 description = new H2("Information finder for a given triple");
        description.setSizeUndefined();
        titleLayout.addComponent(description);
        header.addComponent(titleLayout);
        /*HorizontalLayout toggles = new HorizontalLayout();
        toggles.setSpacing(true);
        Label bgColor = new Label("Background color");
        bgColor.setDescription("Set the style name for the mainWindow layout of this window:<ul><li>Default - no style</li><li>White - Reindeer.LAYOUT_WHITE</li><li>Blue - Reindeer.LAYOUT_BLUE</li><li>Black - Reindeer.LAYOUT_BLACK</li></ul>");
        toggles.addComponent(bgColor);
        NativeSelect colors = new NativeSelect();
        colors.setNullSelectionAllowed(false);
        colors.setDescription("Set the style name for the mainWindow layout of this window:<ul><li>Default - no style</li><li>White - Reindeer.LAYOUT_WHITE</li><li>Blue - Reindeer.LAYOUT_BLUE</li><li>Black - Reindeer.LAYOUT_BLACK</li></ul>");
        colors.addItem("Default");
        colors.addItem("White");
        colors.addItem("Blue");
        colors.addItem("Black");
        colors.setImmediate(true);
        colors.addListener(new ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                mainLayout.setStyleName(event.getProperty().getValue()
                        .toString().toLowerCase());
            }
        });
        colors.setValue("Blue");
        toggles.addComponent(colors);
        CheckBox transparent = new CheckBox("Transparent tabs",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        if (event.getButton().booleanValue()) {
                            tabs.setStyleName(Reindeer.TABSHEET_MINIMAL);
                        } else {
                            tabs.removeStyleName(Reindeer.TABSHEET_MINIMAL);
                        }
                        for (Iterator it = tabs.getComponentIterator(); it
                                .hasNext();) {
                            Component c = (Component) it.next();
                            if (event.getButton().booleanValue()) {
                                c.removeStyleName(Reindeer.LAYOUT_WHITE);
                            } else {
                                c.addStyleName(Reindeer.LAYOUT_WHITE);
                            }
                        }
                        // Force refresh
                        getMainWindow().open(new ExternalResource(getURL()));
                    }
                });
        transparent.setImmediate(true);
        transparent
                .setDescription("Set style Reindeer.TABSHEET_MINIMAL to the mainWindow tab sheet (preview components on different background colors).");
        toggles.addComponent(transparent);
        header.addComponent(toggles);
        header.setComponentAlignment(toggles, "middle");
        titleLayout = new CssLayout();
        Label user = new Label("Welcome, Guest");
        user.setSizeUndefined();
        titleLayout.addComponent(user);
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        Button help = new Button("Help", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                openHelpWindow();
            }
        });
        help.setStyleName(Reindeer.BUTTON_SMALL);
        buttons.addComponent(help);
        buttons.setComponentAlignment(help, "middle");
        Button logout = new Button("Logout", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                openLogoutWindow();
            }
        });
        logout.setStyleName(Reindeer.BUTTON_SMALL);
        buttons.addComponent(logout);
        titleLayout.addComponent(buttons);
        header.addComponent(titleLayout);
        header.setComponentAlignment(titleLayout, "right");*/
        return header;
    }
    Window help = new Window("Help");
    void openHelpWindow() {
        if (!"initialized".equals(help.getData())) {
            help.setData("initialized");
            help.setCloseShortcut(KeyCode.ESCAPE, null);
            help.center();
            // help.setStyleName(Reindeer.WINDOW_LIGHT);
            help.setWidth("400px");
            help.setResizable(false);
            Label helpText = new Label(
                    "<strong>How To Use This Application</strong><p>Click around, explore. The purpose of this app is to show you what is possible to achieve with the Reindeer theme and its different styles.</p><p>Most of the UI controls that are visible in this application don't actually do anything. They are purely for show, like the menu items and the components that demostrate the different style names assosiated with the components.</p><strong>So, What Then?</strong><p>Go and use the styles you see here in your own application and make them beautiful!",
                    Label.CONTENT_XHTML);
            help.addComponent(helpText);
        }
        if (!getMainWindow().getChildWindows().contains(help)) {
            getMainWindow().addWindow(help);
        }
    }

    String formulateNTriplesFromInput(){

        /*getData(txtInputTriple.getValue().toString(),boaConfidenceThreshold,maxSearchResults,
        minPageRank);*/
        String subject = cmbSubject.getValue().toString();
        String predicate = txtPredicate.getValue().toString();
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


//        wrapper.setGraphHeight(3000);

        /*chartPanel.setWidth(60, Sizeable.UNITS_PERCENTAGE);
        chartPanel.setHeight(30, Sizeable.UNITS_PERCENTAGE);*/

        chartPanel.addComponent(wrapper);
        /*chartPanel.setWidth(300, Sizeable.UNITS_PIXELS);
        chartPanel.setHeight(100, Sizeable.UNITS_PIXELS);*/
        chartPanel.setScrollable(true);
        ((BarRenderer)pageRankChart.getCategoryPlot().getRenderer()).setMaximumBarWidth(0.2);;
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
    class SmallText extends Label {
        public SmallText(String caption) {
            super(caption);
            setStyleName(Reindeer.LABEL_SMALL);
        }
    }
    class Ruler extends Label {
        public Ruler() {
            super("<hr />", Label.CONTENT_XHTML);
        }
    }

    class GetResultHandler implements Button.ClickListener{
        public void buttonClick(Button.ClickEvent event) {

            // Get the button that was clicked
            /*Button button = event.getButton();
            //btnGetResults.setCaption("Clicked");
            Button btn = event.getButton();

            boolean allInputDataIsValid = txtBoaThreshold.isValid() & txtLowestPageRank.isValid() &
                    txtNoOfSearchResultsPerQuery.isValid() & cmbSubject.isValid() & txtPredicate.isValid();

            if(!allInputDataIsValid)
                return;

            double boaConfidenceThreshold = Double.parseDouble(txtBoaThreshold.getValue().toString());
            int maxSearchResults =Integer.parseInt(txtNoOfSearchResultsPerQuery.getValue().toString());
            int minPageRank = Integer.parseInt(txtLowestPageRank.getValue().toString());
            */
//            getData(formulateNTriplesFromInput(), boaConfidenceThreshold, maxSearchResults, minPageRank);
            getData();
        }


        /**
         *
         * @param strRequiredTriple The triple to search for
         * @param boaConfidenceThreshold    The confidence threshold used for searching BOA
         * @param maxSearchResults  The maximum number of search results to get per search query
         * @param minPageRank   The minimum PageRank of any page to b included in the search results
         */
        private void getData(String strRequiredTriple, double boaConfidenceThreshold, int maxSearchResults,
                             int minPageRank){

            if((strRequiredTriple == null) || (strRequiredTriple == ""))
                return;
//    TripleResultFinder.getCompleteSearchResults("<http://dbpedia.org/resource/C_Sharp_%28programming_language%29> <http://dbpedia.org/ontology/designer> <http://dbpedia.org/resource/DotGNU> .").size();


            arrSearchResults = TripleResultFinder.getCompleteSearchResults(strRequiredTriple, boaConfidenceThreshold, maxSearchResults, minPageRank);

            lstResults.removeAllItems();
            for (SearchResult result: arrSearchResults){

                ResultWebpage resultWebpage = new ResultWebpage(result.getTitle(), result.getUrl());
                lstResults.addItem(resultWebpage.getTitle());

            }



        }

        private void getData(){
            SPARQL sparqlEndpointDBpediaLive = new SPARQL("http://live.dbpedia.org/sparql", "http://dbpedia.org");

            Model inputTripleModel = ModelFactory.createDefaultModel();
            inputTripleModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(cmbSubject.getValue().toString().trim()),
                    ResourceFactory.createProperty(txtPredicate.getValue().toString().trim()),
                    inputTripleModel.createResource(cmbObject.getValue().toString().trim())));



            inputTripleModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(cmbSubject.getValue().toString().trim()),
                    ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),
                    inputTripleModel.createLiteral(sparqlEndpointDBpediaLive.getEnLabel(cmbSubject.getValue().toString().trim()), "en") ));
            //model.createLiteral(sparqlEndpointDBpediaLive.getEnLabel(subject), "en"))

            inputTripleModel.add(ResourceFactory.createStatement(ResourceFactory.createResource(cmbObject.getValue().toString().trim()),
                    ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),
                    inputTripleModel.createLiteral(sparqlEndpointDBpediaLive.getEnLabel(cmbObject.getValue().toString().trim()), "en")));

            Evidence resultingEvidence = FactChecker.checkFact(inputTripleModel);

            //Clear the ArrayList and Combobox
            arrResultingWebsites.clear();
            lstResults.removeAllItems();

            for (Map.Entry<Pattern,List<WebSite>> resultingWebsite: resultingEvidence.getWebSites().entrySet()){
                List<WebSite> websites = resultingWebsite.getValue();

                for(WebSite website: websites){
                    //if(website.confirmsFact())
                    {
                        arrResultingWebsites.add(website);
//                        lstResultingWebsites.addItem(website);
                        //lstResults.addItem(website.getTitle());
                    }
                }

            }

            layoutGrid.addComponent(new SearchResultRepeater(arrResultingWebsites), 1, 4);

            //lstResultingWebsites.setContainerDataSource(new BeanItemContainer<WebSite>(WebSite.class, arrResultingWebsites));
            //lstResultingWebsites.setVisibleColumns(new String[] {"title", "url", "pageRank"});
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
            /*if(strSelectedItem.compareTo("") != 0){
                for(SearchResult result: arrSearchResults){
                    if(result.getTitle().compareTo(strSelectedItem) == 0){

                        browserView.setSource(new ExternalResource(result.getUrl()));

                        break;
                    }

                }
            }*/

            if(strSelectedItem.compareTo("") != 0){
                for(WebSite resultWebsite: arrResultingWebsites){
                    if(resultWebsite.getTitle().compareTo(strSelectedItem) == 0){

                        browserView.setSource(new ExternalResource(resultWebsite.getUrl()));

                        break;
                    }

                }
            }

        }

        public void buttonClick(Button.ClickEvent event) {
            Window window = new Window("Test Window");
            String strSelectedItem = lstResults.getValue().toString();

            String websiteOfSelectedItem = "";

            //Determine the website of the selected item
            if(strSelectedItem.compareTo("") != 0){
                for(SearchResult result: arrSearchResults){
                    if(result.getTitle().compareTo(strSelectedItem) == 0){
                        browserView.setSource(new ExternalResource(result.getUrl()));

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
            
            /*layoutGrid.removeComponent(2, 7);
            layoutGrid.addComponent(createPageRankChart(createDataset(4)), 2, 7);*/
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
            /*WebSite selectedWebsite = (WebSite)lstResultingWebsites.getValue();

            for(WebSite website: arrResultingWebsites){

                //Compare URLs in order to determine the selected website, and then display its PageRank in the chart
                if(selectedWebsite.getUrl().compareTo(website.getUrl()) == 0){

                }
            }
            createPageRankChart(createDataset(4));*/

            /*String strSelectedItem = lstResults.getValue().toString();
            String websiteOfSelectedItem = "";



            if(strSelectedItem.compareTo("") != 0){
                for(WebSite resultWebsite: arrResultingWebsites){
                    if(resultWebsite.getTitle().compareTo(strSelectedItem) == 0){

                        browserView.setSource(new ExternalResource(resultWebsite.getUrl()));

                        break;
                    }

                }
            }
            */
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

    /**
     * This class holds each URI along with its label in order to simplify the selection in autocomplete
     * comboboxes of subject and object
     */
    /*private class UriWithLabel{
        String URI;
        String label;

        public UriWithLabel(String URI, String label) {
            this.URI = URI;
            this.label = label;
        }

        public String getURI() {
            return URI;
        }

        public String getLabel() {
            return label;
        }
    }*/

}
