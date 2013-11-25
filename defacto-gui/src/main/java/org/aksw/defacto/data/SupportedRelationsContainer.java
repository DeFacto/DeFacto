/**
 * 
 */
package org.aksw.defacto.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.gwt.thirdparty.guava.common.io.CharStreams;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

/**
 * @author Lorenz Buehmann
 *
 */
public class SupportedRelationsContainer extends IndexedContainer{
	
	private static final String supportedRelationsFile = "supported_relations.txt";
	
	public SupportedRelationsContainer() {
		addContainerProperty("label", String.class, null);
		addContainerProperty("uri", String.class, null);
		
		//load supported relations from file
		try {
			String s = CharStreams.toString(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(supportedRelationsFile), Charsets.UTF_8));
			List<String> relations = Arrays.asList(s.split("\\n"));
			for (String rel : relations) {
				String[] split = rel.split(",");
				
				String label = split[0];
				String uri = split[1];
				
				Item item = addItem(rel);
				item.getItemProperty("label").setValue(label);
				item.getItemProperty("uri").setValue(uri);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
