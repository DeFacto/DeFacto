package org.aksw.defacto;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.defacto.Defacto.TIME_DISTRIBUTION_ONLY;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.data.SupportedRelationsContainer;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.util.AGDISTIS;
import org.aksw.defacto.util.AGDISTISResult;
import org.aksw.defacto.util.DeFactoModelGenerator;
import org.aksw.defacto.util.DummyData;
import org.aksw.defacto.util.EvidenceRDFGenerator;
import org.aksw.defacto.util.FactBenchExample;
import org.aksw.defacto.util.FactBenchExamplesLoader;
import org.aksw.defacto.widget.ProgressDialog;
import org.aksw.defacto.widget.ResultsPanel;
import org.aksw.defacto.widget.SearchResourceDialog;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Title("DeFacto")
@Push
@Theme("defacto")
@SuppressWarnings("serial")
public class DeFactoUI extends UI
{
    private ResultsPanel          resultsPanel;

    private ComboBox              objectBox;
    private ComboBox              predicateBox;
    private ComboBox              subjectBox;

    private Button                validateButton;
    private DeFactoModelGenerator modelGenerator = new DeFactoModelGenerator(SparqlEndpoint.getEndpointDBpedia());

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);
        setContent(layout);
        layout.addStyleName("main-view");
        // header
        Component header = createHeader();
        layout.addComponent(header);

        // main panel
        VerticalLayout main = new VerticalLayout();
        main.setSizeFull();
        main.setSpacing(true);
        // main.addStyleName("defacto");
        main.addStyleName("main-view");
        // main.addStyleName("result-panel");
        layout.addComponent(main);
        // add triple input form to main panel in center top
        // Component tripleInput = generateTripleInputForm();
        Component tripleInput = generateExampleInputForm();
        main.addComponent(tripleInput);
        // add results panel to main panel in center bottom
        resultsPanel = new ResultsPanel();
        main.addComponent(resultsPanel);
        main.setExpandRatio(resultsPanel, 1f);
        // resultsPanel.setHeight(null);
        // wrap in panel for scrolling
        // Panel panel = new Panel();
        // panel.setContent(resultsPanel);
        // panel.setWidth("100%");
        // panel.setHeight(null);
        // panel.setSizeFull();
        // main.addComponent(panel);
        // main.setExpandRatio(panel, 1f);

        // footer
        Component footer = createFooter();
        footer.setWidth(null);
        layout.addComponent(footer);
        layout.setComponentAlignment(footer, Alignment.MIDDLE_RIGHT);

        layout.setExpandRatio(main, 1f);

        // set DeFacto config
        // TODO we should do it in servlet initialization maybe
        try {
            Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(Defacto.class.getClass().getClassLoader().getResourceAsStream("defacto.ini")));
        } catch (InvalidFileFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadExampleData();
    }

    /**
     * Load examples from FactBench dataset.
     */
    private void loadExampleData() {
        Set<FactBenchExample> examples = FactBenchExamplesLoader.loadExamples();
        for (FactBenchExample example : examples) {
            subjectBox.addItem(example).getItemProperty("fact").setValue(example.getFact());
        }

        // //set dummy triple
        // Triple triple = DummyData.getDummyTriple();
        // // subjectBox.addItem(triple.getSubject().getURI());
        // // subjectBox.setValue(triple.getSubject().getURI());
        // subjectBox.addItem("Albert Einstein");
        // subjectBox.setValue("Albert Einstein");
        // predicateBox.addItem(triple.getPredicate().getURI());
        // predicateBox.setValue(triple.getPredicate().getURI());
        // // objectBox.addItem(triple.getObject().getURI());
        // // objectBox.setValue(triple.getObject().getURI());
        // objectBox.addItem("Nobel Prize in Physics");
        // objectBox.setValue("Nobel Prize in Physics");
    }

    /**
     * Create the basic input form for the triple to validate.
     * 
     * @return
     */
    private Component generateExampleInputForm() {
        HorizontalLayout l = new HorizontalLayout();
        l.setWidth("100%");
        l.addStyleName("triple-input");
        l.setMargin(true);
        l.setSpacing(true);

        // subject
        subjectBox = new ComboBox("Example facts:") {
            /* (non-Javadoc)
             * @see com.vaadin.ui.ComboBox#changeVariables(java.lang.Object, java.util.Map)
             */
            @Override
            public void changeVariables(Object source, Map<String, Object> variables) {
                String filter = (String) variables.get("filter");
                if (filter != null && filter.length() > 2) {
                    List<String> suggestions = autoSuggest(filter);
                    removeAllItems();
                    for (String s : suggestions) {
                        addItem(s);
                    }
                }
                super.changeVariables(source, variables);
            }
        };
        subjectBox.setWidth("100%");
        subjectBox.setInputPrompt("Please choose one of the facts for validation");
        subjectBox.setImmediate(true);
        subjectBox.addContainerProperty("fact", String.class, null);
        subjectBox.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                updateValidationAllowed();
            }
        });
        l.addComponent(subjectBox);

        // validation button
        validateButton = new Button("Validate");
        validateButton.setDescription("Click to start the validation of the fact.");
        validateButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                SearchResourceDialog d = new SearchResourceDialog();
                d.setModal(true);
                onValidateFactBenchExample();
            }
        });
        l.addComponent(validateButton);
        l.setComponentAlignment(validateButton, Alignment.BOTTOM_RIGHT);

        l.setExpandRatio(subjectBox, 1f);

        return l;
    }

    /**
     * Create the basic input form for the triple to validate.
     * 
     * @return
     */
    private Component generateTripleInputForm() {
        HorizontalLayout l = new HorizontalLayout();
        l.setWidth("100%");
        l.addStyleName("triple-input");
        l.setMargin(true);
        l.setSpacing(true);

        // subject
        subjectBox = new ComboBox("Subject") {
            /* (non-Javadoc)
             * @see com.vaadin.ui.ComboBox#changeVariables(java.lang.Object, java.util.Map)
             */
            @Override
            public void changeVariables(Object source, Map<String, Object> variables) {
                String filter = (String) variables.get("filter");
                if (filter != null && filter.length() > 2) {
                    List<String> suggestions = autoSuggest(filter);
                    removeAllItems();
                    for (String s : suggestions) {
                        addItem(s);
                    }
                }
                super.changeVariables(source, variables);
            }
        };
        subjectBox.setWidth("100%");
        subjectBox.setInputPrompt("Please enter the subject of the triple");
        subjectBox.setImmediate(true);
        subjectBox.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                updateValidationAllowed();
            }
        });
        l.addComponent(subjectBox);

        // predicate
        predicateBox = new ComboBox("Predicate");
        predicateBox.setWidth("100%");
        predicateBox.setInputPrompt("Please choose the predicate of the triple");
        predicateBox.setImmediate(true);
        predicateBox.setPageLength(12);
        // add supported predefined relations
        predicateBox.setContainerDataSource(new SupportedRelationsContainer());
        predicateBox.setItemCaptionPropertyId("label");
        predicateBox.setNewItemsAllowed(false);
        predicateBox.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                updateValidationAllowed();
            }
        });
        l.addComponent(predicateBox);

        // object
        objectBox = new ComboBox("Object");
        objectBox.setWidth("100%");
        objectBox.setInputPrompt("Please enter the object of the triple");
        objectBox.setImmediate(true);
        objectBox.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                updateValidationAllowed();
            }
        });
        l.addComponent(objectBox);

        // validation button
        validateButton = new Button("Validate");
        validateButton.setDescription("Click to start the validation of the triple.");
        validateButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                SearchResourceDialog d = new SearchResourceDialog();
                d.setModal(true);
                // UI.getCurrent().addWindow(d);
                onValidate();
            }
        });
        l.addComponent(validateButton);
        l.setComponentAlignment(validateButton, Alignment.BOTTOM_RIGHT);

        l.setExpandRatio(subjectBox, 1f);
        l.setExpandRatio(predicateBox, 1f);
        l.setExpandRatio(objectBox, 1f);

        Panel p = new Panel("Enter the fact:");
        p.setContent(l);
        return p;
    }

    /**
     * Enables validate button only if subject, predicate and object are set.
     */
    private void updateValidationAllowed() {
        boolean enabled = subjectBox.getValue() != null && predicateBox.getValue() != null && objectBox.getValue() != null;
        validateButton.setEnabled(enabled);
    }

    /**
     * Create the header by loading header.html from resources.
     * 
     * @return
     */
    private Component createHeader() {
        Label l = new Label();
        l.setContentMode(ContentMode.HTML);
        l.setWidth("100%");

        try {
            InputSupplier<InputStream> inputSupplier = new InputSupplier<InputStream>() {
                public InputStream getInput() throws IOException {
                    return DeFactoUI.this.getClass().getClassLoader().getResourceAsStream("header.html");
                }
            };
            String s = CharStreams.toString(CharStreams.newReaderSupplier(inputSupplier, Charsets.UTF_8));
            l.setValue(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return l;
    }

    /**
     * Create the footer by loading the footer.html from resource.
     * 
     * @return
     */
    private Component createFooter() {
        Label l = new Label();
        l.setContentMode(ContentMode.HTML);
        l.setWidth("100%");

        try {
            InputSupplier<InputStream> inputSupplier = new InputSupplier<InputStream>() {
                public InputStream getInput() throws IOException {
                    return DeFactoUI.this.getClass().getClassLoader().getResourceAsStream("footer.html");
                }
            };
            String s = CharStreams.toString(CharStreams.newReaderSupplier(inputSupplier, Charsets.UTF_8));
            l.setValue(s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return l;
    }

    /**
     * Run validation of the given triple.
     * 
     * @param triple
     */
    private void onValidate() {
        final ProgressDialog dialog = new ProgressDialog("Validating...");
        addWindow(dialog);

        final String subject = (String) subjectBox.getValue();
        final String predicateURI = (String) predicateBox.getItem(predicateBox.getValue()).getItemProperty("uri").getValue();
        final String object = (String) objectBox.getValue();

        new Thread(new Runnable() {

            @Override
            public void run() {

                // determine the URIs
                AGDISTISResult result = AGDISTIS.disambiguate(subject, object);
                String subjectURI = result.getSubjectURI();
                if (subjectURI == null) {

                }
                String objectURI = result.getObjectURI();
                if (objectURI == null) {

                }

                subjectURI = "http://dbpedia.org/resource/Albert_Einstein";
                objectURI = "http://dbpedia.org/resource/Nobel_Prize_in_Physics";

                // build the triple
                final Triple triple = new Triple(
                        NodeFactory.createURI(subjectURI),
                        NodeFactory.createURI(predicateURI),
                        NodeFactory.createURI(objectURI));

                // build the DeFacto model
                // final DefactoModel model = DummyData.getEinsteinModel();
                final DefactoModel model = modelGenerator.generateModel(Sets.newHashSet(subjectURI, objectURI));

                // call of DeFacto
                final Calendar startTime = Calendar.getInstance();
                final Evidence evidence = Defacto.checkFact(model, TIME_DISTRIBUTION_ONLY.NO);
                final Calendar endTime = Calendar.getInstance();

                UI.getCurrent().access(new Runnable() {
                    @Override
                    public void run() {
                        // remove progress dialog
                        removeWindow(dialog);
                        // visualize the results
                        resultsPanel.showResults(triple, evidence, startTime, endTime);
                    }
                });
            }
        }).start();
    }

    /**
     * Run validation of the given triple.
     * 
     * @param triple
     */
    private void onValidateFactBenchExample() {
        final ProgressDialog dialog = new ProgressDialog("Validating...");
        addWindow(dialog);

        new Thread(new Runnable() {

            @Override
            public void run() {

                final FactBenchExample example = (FactBenchExample) subjectBox.getValue();

                // call of DeFacto
                final Calendar startTime = Calendar.getInstance();
                final Evidence evidence = Defacto.checkFact(example.getModel(), TIME_DISTRIBUTION_ONLY.NO);
                final Calendar endTime = Calendar.getInstance();

                UI.getCurrent().access(new Runnable() {
                    @Override
                    public void run() {
                        // remove progress dialog
                        removeWindow(dialog);
                        // visualize the results
                        resultsPanel.showResults(example.getTriple(), evidence, startTime, endTime);
                    }
                });
            }
        }).start();
    }

    private List<String> autoSuggest(String prefix) {
        List<String> suggestions = new ArrayList<String>();
        try {
            String url = "http://[2001:638:902:2010:0:168:35:138]:8080/solr/en_dbpedia_classes/suggest?q=label=" + prefix + "&wt=json";
            InputStream is = new URL(url).openStream();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(is);
            if (node.get("spellcheck").elements().next().has(1)) {
                node = node.get("spellcheck").elements().next().get(1).get("suggestion");
                Iterator<JsonNode> elements = node.elements();
                while (elements.hasNext()) {
                    suggestions.add(elements.next().asText());
                }
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // SolrQuery parameters = new SolrQuery();
        // parameters.set("q", prefix);
        // parameters.setRequestHandler("ac");
        // parameters.setRows(20);
        // try {
        // QueryResponse response = solr.query(parameters);
        // SolrDocumentList list = response.getResults();
        // for (SolrDocument doc : list) {
        // suggestions.add((String) doc.getFieldValue("uri"));
        // }
        // } catch (SolrServerException e) {
        // e.printStackTrace();
        // }
        return suggestions;
    }

    public static void main(String[] args) throws Exception {
        Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(Defacto.class.getClassLoader().getResourceAsStream("defacto.ini")));
        AGDISTISResult result = AGDISTIS.disambiguate("Albert Einstein", "Nobel Prize");
        System.out.println(result);

        final DefactoModel model = DummyData.getEinsteinModel();

        // this is actually a dummy call of DeFacto
        // final Pair<DefactoModel, Evidence> evidence =
        // DummyData.createDummyData(5);//TODO call DeFacto properly
        Calendar startTime = Calendar.getInstance();
        final Evidence evidence = Defacto.checkFact(model, TIME_DISTRIBUTION_ONLY.NO);
        Calendar endTime = Calendar.getInstance();

        System.out.println(evidence.getDeFactoScore());
        System.out.println(evidence.defactoTimePeriod.getFrom());

        System.out.println(EvidenceRDFGenerator.getProvenanceInformationAsString(DummyData.getDummyTriple(), evidence, startTime, endTime, "TURTLE"));
    }
}
