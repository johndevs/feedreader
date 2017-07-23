/*
 * Copyright 2017 John Ahlroos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.jasoft.feedreader.ui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import fi.jasoft.feedreader.data.Feed;

/**
 * Window presented for the user when a new feed should be entered
 * 
 * @author John Ahlroos / https://devsoap.com
 */
public class AddFeedWindow extends Window {
	
	private TextField url;
	
	/**
	 * Default constructor
	 */
	public AddFeedWindow(){
		setModal(true);
		setWidth(300, Unit.PIXELS);
		setHeight(200, Unit.PIXELS);
		setResizable(false);
		setDraggable(false);
		setCaption("Add RSS/Atom Feed");
		
		VerticalLayout windowContent = new VerticalLayout();
		windowContent.setSpacing(true);
		windowContent.setMargin(true);
		windowContent.setSizeFull();
		setContent(windowContent);
		
		url = new TextField();
		url.setPlaceholder("http://<feed url>");
		url.setWidth("100%");
		windowContent.addComponent(url);
		windowContent.setComponentAlignment(url, Alignment.MIDDLE_CENTER);
		
		HorizontalLayout buttons = new HorizontalLayout();
		
		buttons.addComponent(new Button("Add", (Button.ClickListener) event -> {
            if(validateUrl(url.getValue())){
                close();
            } else{
                Notification.show("URL not valid");
            }
        }));
		
		buttons.addComponent(new Button("Cancel", (Button.ClickListener) event -> {
            url.setValue("");
            close();
        }));
		
		windowContent.addComponent(buttons);		
		windowContent.setComponentAlignment(buttons, Alignment.BOTTOM_RIGHT);
		
		windowContent.setExpandRatio(url, 1);
	}
	
	/**
	 * Ensure that a text representation of an URL is valid.
	 * 
	 * @param url
	 * 		The url to validate
	 * @return
	 * 		Return true if the url is valid, false if it is invalid
	 */
	private boolean validateUrl(String url){
		// A really simple validation. Should be replaced with a better one.
		return url != null && url.startsWith("http");
	}
	
	/**
	 * Get the feed created by the window. Does not save the feed, 
	 * it should be done by the callee.
	 * 
	 * @return
	 * 		Returns the new feed create by the window.
	 */
	public Feed getFeed(){
		if(!validateUrl(url.getValue())){
			return null;
		}
		Feed feed = new Feed();
		feed.setUrl(url.getValue());
		return feed;
	}
}
