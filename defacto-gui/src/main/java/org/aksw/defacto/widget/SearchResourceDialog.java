/**
 * 
 */
package org.aksw.defacto.widget;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Lorenz Buehmann
 *
 */
public class SearchResourceDialog extends Window{
	
	private Table select;
	private SolrServer solr;

	/**
	 * 
	 */
	public SearchResourceDialog() {
		setWidth("500px");
		setHeight("500px");
		solr = new HttpSolrServer("http://localhost:8080/solr/DBpedia_Resources");
		
		VerticalLayout l = new VerticalLayout();
		setContent(l);
		l.setSpacing(true);
		
		TextField tf = new TextField();
		tf.setWidth("90%");
		tf.setCaption("Search for");
		tf.setImmediate(true);
		tf.addTextChangeListener(new TextChangeListener() {
			
			@Override
			public void textChange(TextChangeEvent event) {
				if(event.getText() != null && event.getText().length() > 2){
					autoSuggest(event.getText());
				}
			}
		});
		l.addComponent(tf);
		l.setComponentAlignment(tf, Alignment.MIDDLE_CENTER);
		
		select = new Table("Matching resources");
		select.setImmediate(true);
		select.setWidth("90%");
		select.addGeneratedColumn("desc", new ColumnGenerator() {
			
			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				SOLRResultItem item = (SOLRResultItem) itemId;
				return new Label("<b>" + item.label + "</b><div>" + item.comment + "</div>", ContentMode.HTML);
			}
		});
		select.setSelectable(true);
		select.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
        l.addComponent(select);
        
        l.setExpandRatio(select, 1f);
        l.setComponentAlignment(select, Alignment.MIDDLE_CENTER);
	}
	
	 private void autoSuggest(String prefix){
		 select.removeAllItems();
		 
		 	long t = System.currentTimeMillis();
	    	SolrQuery parameters = new SolrQuery();
	    	parameters.set("q", prefix);
	    	parameters.setRequestHandler("ac");
	    	parameters.setRows(20);
	    	try {
				QueryResponse response = solr.query(parameters);
				SolrDocumentList list = response.getResults();
				for (SolrDocument doc : list) {
					String uri = (String) doc.getFieldValue("uri");
					String label = (String) doc.getFieldValue("label");
					String comment = (String) doc.getFieldValue("comment");
					String imageURL = (String) doc.getFieldValue("thumbnail_url");
					select.addItem(new SOLRResultItem(uri, label, comment, imageURL));
				}
			} catch (SolrServerException e) {
				e.printStackTrace();
			}
	    }
	 
	 class SOLRResultItem{
		 String uri;
		 String label;
		 String comment;
		 String imageURL;
		 
		public SOLRResultItem(String uri, String label, String comment, String imageURL) {
			this.uri = uri;
			this.label = label;
			this.comment = comment;
			this.imageURL = imageURL;
		}
	 }
	 
}
