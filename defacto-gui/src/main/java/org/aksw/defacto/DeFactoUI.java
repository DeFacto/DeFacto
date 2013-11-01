package org.aksw.defacto;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aksw.commons.collections.Pair;
import org.aksw.defacto.Defacto.TIME_DISTRIBUTION_ONLY;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.util.DummyData;
import org.aksw.defacto.widget.ResultsPanel;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
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
@SuppressWarnings("serial")
public class DeFactoUI extends UI
{
	private ResultsPanel resultsPanel;
	
	private ComboBox objectBox;
	private ComboBox predicateBox;
	private ComboBox subjectBox;
	
	private Button validateButton;

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);
        setContent(layout);
        
        //header
        Component header = createHeader();
        layout.addComponent(header);
        
        //main panel
        VerticalLayout main = new VerticalLayout();
        main.setSizeFull();
        main.setSpacing(true);
        layout.addComponent(main);
        //add triple input form to main panel in center top
        Component tripleInput = generateTripleInputForm();
        main.addComponent(tripleInput);
        //add results panel to main panel in center bottom
        resultsPanel = new ResultsPanel();
        resultsPanel.setHeight(null);
        //wrap in panel for scrolling
        Panel panel = new Panel();
        panel.setContent(resultsPanel);
        panel.setWidth("100%");
        panel.setHeight(null);
        panel.setSizeFull();
        main.addComponent(panel);
        main.setExpandRatio(panel, 1f);

        //footer
        Component footer = createFooter();
        layout.addComponent(footer);
        
        layout.setExpandRatio(main, 1f);
        
        
        //set DeFacto config
        //TODO we should do it in servlet initialization maybe
        try {
			Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(this.getClass().getClassLoader().getResourceAsStream("defacto.ini")));
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        //set dummy triple
        Triple triple = DummyData.getDummyTriple();
        subjectBox.addItem(triple.getSubject().getURI());
        subjectBox.setValue(triple.getSubject().getURI());
        predicateBox.addItem(triple.getPredicate().getURI());
        predicateBox.setValue(triple.getPredicate().getURI());
        objectBox.addItem(triple.getObject().getURI());
        objectBox.setValue(triple.getObject().getURI());
    }
    
    /**
     * Create the basic input form for the triple to validate.
     * @return
     */
    private Component generateTripleInputForm(){
    	HorizontalLayout l = new HorizontalLayout();
    	l.setWidth("100%");
    	l.setSpacing(true);
    	
    	//subject
    	subjectBox = new ComboBox("Subject"){
        	/* (non-Javadoc)
        	 * @see com.vaadin.ui.ComboBox#changeVariables(java.lang.Object, java.util.Map)
        	 */
        	@Override
        	public void changeVariables(Object source, Map<String, Object> variables) {
        		String filter = (String) variables.get("filter");
        		if(filter != null && filter.length() > 1){
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
        
        //predicate
        predicateBox = new ComboBox("Predicate");
        predicateBox.setWidth("100%");
        predicateBox.setInputPrompt("Please enter the predicate of the triple");
        predicateBox.setImmediate(true);
        predicateBox.addValueChangeListener(new ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				updateValidationAllowed();
			}
		});
        l.addComponent(predicateBox);
        
        //object
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
        
        //validation button
        validateButton = new Button("Validate");
        validateButton.setDescription("Click to start the validation of the triple.");
        validateButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                onValidate();
            }
        });
        l.addComponent(validateButton);
        l.setComponentAlignment(validateButton, Alignment.BOTTOM_RIGHT);
        
        l.setExpandRatio(subjectBox, 1f);
        l.setExpandRatio(predicateBox, 1f);
        l.setExpandRatio(objectBox, 1f);
        
        return l;
    }
    
    /**
     * Enables validate button only if subject, predicate and object are set.
     */
    private void updateValidationAllowed(){
    	boolean enabled = subjectBox.getValue() != null && predicateBox.getValue() != null && objectBox.getValue() != null;
    	validateButton.setEnabled(enabled);
    }
    
    /**
     * Create the header by loading header.html from resources.
     * @return
     */
    private Component createHeader(){
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
     * @return
     */
    private Component createFooter(){
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
     * @param triple
     */
    private void onValidate(){
    	Triple triple = new Triple(
    			NodeFactory.createURI((String) subjectBox.getValue()), 
    			NodeFactory.createURI((String) predicateBox.getValue()), 
    			NodeFactory.createURI((String) objectBox.getValue()));
    	
    	//build the DeFacto model
    	DefactoModel model = DummyData.getDummyModel();
    	
    	//this is actually a dummy call of DeFacto
    	Pair<DefactoModel, Evidence> evidence = DummyData.createDummyData(5);//TODO call DeFacto properly
//    	Map<DefactoModel, Evidence> evidence = Defacto.checkFacts(Lists.newArrayList(model), TIME_DISTRIBUTION_ONLY.NO);
    	
    	//visualize the results
    	resultsPanel.showResults(triple, evidence.second);
    }
    
    private List<String> autoSuggest(String prefix){
    	List<String> suggestions = new ArrayList<String>();
    	try {
			String url = "http://[2001:638:902:2010:0:168:35:138]:8080/solr/en_dbpedia_classes/suggest?q=label=" + prefix + "&wt=json";
			InputStream is = new URL(url).openStream();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(is);
			if(node.get("spellcheck").elements().next().has(1)){
				node = node.get("spellcheck").elements().next().get(1).get("suggestion");
				Iterator<JsonNode> elements = node.elements();
				while(elements.hasNext()){
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
    	return suggestions;
    }
}
