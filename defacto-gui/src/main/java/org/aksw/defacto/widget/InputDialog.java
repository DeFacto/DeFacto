package org.aksw.defacto.widget;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class InputDialog extends Window {
	Recipient r;
	TextField tf = new TextField();

	public InputDialog(String question, Recipient recipient) {
		r = recipient;
		
		setCaption(question);
		setModal(true);
		
		VerticalLayout content = new VerticalLayout();
		setContent(content);
		
		content.setSizeUndefined();
		content.addComponent(tf);
		
		final Window dialog = this;
		
		content.addComponent(new Button("Ok", new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().removeWindow(dialog);
			}
		}));
	}
	
	public String show(){
		UI.getCurrent().addWindow(this);
		return tf.getValue();
	}

	public interface Recipient {
		public void gotInput(String input);
	}
}