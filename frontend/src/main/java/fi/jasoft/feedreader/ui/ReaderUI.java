/*
 * Copyright 2012 John Ahlroos
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

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.WrappedRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table.ColumnHeaderMode;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.data.Property;
import com.vaadin.event.Action;

import fi.jasoft.feedreader.data.Feed;
import fi.jasoft.feedreader.data.FeedEntry;
import fi.jasoft.feedreader.service.FeedService;
import fi.jasoft.feedreader.service.FeedServiceImpl;

/**
 * User interface for the application.
 * 
 * @author John Ahlroos / http://www.jasoft.fi
 */
public class ReaderUI extends UI implements Action.Handler{

	/*
	 * Services
	 */
	private final FeedService feedService = new FeedServiceImpl();
	
	/*
	 * Data providers
	 */
	private final BeanItemContainer<FeedEntry> entries = new BeanItemContainer<FeedEntry>(FeedEntry.class);
	private final BeanItemContainer<Feed> feeds = new BeanItemContainer<Feed>(Feed.class);
	{
		feeds.addAll(feedService.getFeeds());
	}
	
	// UI components
	protected Panel entryPanel = new Panel();
	protected Table feedTable;
	protected Table entryTable;
	
	// Feed actions
	private Action ADD_FEED_ACTION = new Action("Add RSS/Atom feed");
	private Action REMOVE_FEED_ACTION = new Action("Remove RSS/Atom feed");
	private Action SYNCRONIZE_ACTION = new Action("Syncronize feed");
	
	/*
	 * (non-Javadoc)
	 * @see com.vaadin.ui.UI#init(com.vaadin.server.WrappedRequest)
	 */
	@Override
	protected void init(WrappedRequest request) {
		
		// Create data tables
		feedTable = createFeedsTable();
		entryTable = createEntriesTable();
		
		// Create the main horizontal split panel
		HorizontalSplitPanel content = new HorizontalSplitPanel();
		content.setStyleName(Reindeer.SPLITPANEL_SMALL);
		content.setSizeFull();
		setContent(content);
	
		// Create the content of the left part of the main split panel
		VerticalLayout vl = new VerticalLayout();
		vl.setSizeFull();
		vl.addComponent(feedTable);
		
		Button addFeedBtn = new Button("Add RSS/Atom feed", new Button.ClickListener() {		
			@Override
			public void buttonClick(ClickEvent event) {
				addFeed();
			}
		});
		addFeedBtn.setWidth("100%");
		vl.addComponent(addFeedBtn);
		vl.setExpandRatio(feedTable, 1);
		
		content.setFirstComponent(vl);
		content.setSplitPosition(30);

		// Create and set the content of the right part of the main split panel
		VerticalSplitPanel rightPane = new VerticalSplitPanel();
		rightPane.setStyleName(Reindeer.SPLITPANEL_SMALL);
		rightPane.setSizeFull();
		
		rightPane.addComponent(entryTable);
		
		entryPanel.setSizeFull();
		rightPane.addComponent(entryPanel);
		
		content.addComponent(rightPane);
		rightPane.setSplitPosition(30);
		
		if(feeds.size() > 0){
			feedTable.setValue(feeds.getItemIds().iterator().next());
		}
	}
	
	/**
	 * Creates the feed table on the left where added feeds are displayed
	 */
	private Table createFeedsTable(){
		Table table = new Table();
		table.setSizeFull();
		table.setNullSelectionAllowed(false);
		table.setContainerDataSource(feeds);
		table.setSelectable(true);
		table.setImmediate(true);
		table.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				entries.removeAllItems();
				Feed feed = (Feed) event.getProperty().getValue();
				if(feed != null){
					entries.addAll(feed.getEntries());
					if(entries.size() > 0){
						entryTable.setValue(entries.getItemIds().iterator().next());
					}
				}
			}
		});
		table.setVisibleColumns(new Object[]{"url"});
		table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		table.addActionHandler(this);
		
		return table;
	}
	
	/**
	 * Creates the table on the top where the selected feeds entries are displayed.
	 */
	private Table createEntriesTable(){
		Table table = new Table();
		table.setSizeFull();
		table.setNullSelectionAllowed(false);
		table.setSelectable(true);
		table.setContainerDataSource(entries);
		table.setImmediate(true);
		table.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				FeedEntry entry = (FeedEntry) event.getProperty().getValue();
				setContent(entry);
			}
		});
		table.setVisibleColumns(new Object[]{"title"});
		table.setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		
		return table;
	}
	
	/**
	 * Set the content of the feed entry window.
	 * 
	 * @param entry
	 * 		The feed entry to show
	 */
	private void setContent(FeedEntry entry){
		VerticalLayout content = new VerticalLayout();
		content.setMargin(true);
		if(entry != null){
			Label title = new Label(entry.getTitle());
			title.setStyleName(Reindeer.LABEL_H1);
			content.addComponent(title);
			Label entryContent = new Label(entry.getContent(), ContentMode.HTML);
			content.addComponent(entryContent);
		}
		entryPanel.setContent(content);
	}
	
	/**
	 * Opens the add feed dialog window and persists and syncronizes the feed
	 * after the dialog window has bee closed.
	 */
	private void addFeed(){
		final AddFeedWindow addFeedWindow = new AddFeedWindow();
		addFeedWindow.addCloseListener(new Window.CloseListener() {
			@Override
			public void windowClose(com.vaadin.ui.Window.CloseEvent e) {
				Feed feed = addFeedWindow.getFeed();
				if(feed != null){
					// Save new feed and syncronize with remote feed
					feedService.add(feed);
					feeds.addBean(feed);
					syncronize(feed);
				}
			}
		});
		getUI().addWindow(addFeedWindow);
	}

	/*
	 * (non-Javadoc)
	 * @see com.vaadin.event.Action.Handler#getActions(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Action[] getActions(Object target, Object sender) {
		if(target == null){
			return new Action[]{ ADD_FEED_ACTION };
		} else {
			return new Action[]{ REMOVE_FEED_ACTION, SYNCRONIZE_ACTION };
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.vaadin.event.Action.Handler#handleAction(com.vaadin.event.Action, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if(action == ADD_FEED_ACTION){
			addFeed();
			
		} else if(action == REMOVE_FEED_ACTION){
			feedService.remove((Feed)target);
			feedTable.removeItem((Feed)target);
			if(feedTable.getValue() == null && feeds.size() > 0){
				feedTable.setValue(feeds.getItemIds().iterator().next());
			}
			
		} else if(action == SYNCRONIZE_ACTION){
			syncronize((Feed)target);
		}
	}
	
	/**
	 * Syncronizes a feed with the online RSS/ATOM feed. Updated the tables if necessery.
	 * 
	 * @param feed
	 * 		The feed to syncronize
	 */
	private void syncronize(Feed feed){
		// Syncronize feed with remote ATOM/RSS feed
		feedService.syncronize(feed);
		
		if(feedTable.getValue() == feed){

			// Remove old entries
			entries.removeAllItems();
			
			// Add new entries
			entries.addAll(feed.getEntries());
		}
	}
}
