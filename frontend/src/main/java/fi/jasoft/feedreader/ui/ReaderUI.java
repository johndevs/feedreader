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

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.contextmenu.ContextMenu;
import com.vaadin.contextmenu.GridContextMenu;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import fi.jasoft.feedreader.data.Feed;
import fi.jasoft.feedreader.data.FeedEntry;
import fi.jasoft.feedreader.service.FeedService;
import fi.jasoft.feedreader.service.FeedServiceImpl;

import java.util.List;
import java.util.Optional;


/**
 * User interface for the application.
 * 
 * @author John Ahlroos / https://devsoap.com
 */
@Theme("Frontend")
@Title("RSS Feed Reader")
public class ReaderUI extends UI {

	/*
	 * Services
	 */
	private final FeedService feedService = new FeedServiceImpl();

	/*
	 * Data providers
	 */
	private final DataProvider<Feed, Void> feeds = DataProvider.fromCallbacks(
		fetch -> feedService.getFeeds().stream(),
		count -> feedService.getFeeds().size()
	);

	// UI components
	private Panel entryPanel = new Panel();
	private Grid<Feed> feedTable;
	private Grid<FeedEntry> entryTable;

	@Override
	protected void init(VaadinRequest request) {

		// Create data tables
		feedTable = createFeedsTable();
		entryTable = createEntriesTable();
		
		// Create the main horizontal split panel
		HorizontalSplitPanel content = new HorizontalSplitPanel();
		content.setSizeFull();
		setContent(content);
	
		// Create the content of the left part of the main split panel
		VerticalLayout vl = new VerticalLayout();
		vl.setSizeFull();
		vl.addComponent(feedTable);
		
		Button addFeedBtn = new Button("Add RSS/Atom feed", (Button.ClickListener) event -> addFeed());
		addFeedBtn.setWidth("100%");
		vl.addComponent(addFeedBtn);
		vl.setExpandRatio(feedTable, 1);
		
		content.setFirstComponent(vl);
		content.setSplitPosition(30);

		// Create and set the content of the right part of the main split panel
		VerticalSplitPanel rightPane = new VerticalSplitPanel();
		rightPane.setSizeFull();
		
		rightPane.addComponent(entryTable);
		
		entryPanel.setSizeFull();
		rightPane.addComponent(entryPanel);
		
		content.addComponent(rightPane);
		rightPane.setSplitPosition(30);

        feeds.fetch(new Query<>())
                .findFirst()
                .ifPresent(feed -> feedTable.select(feed));
	}


	
	/**
	 * Creates the feed table on the left where added feeds are displayed
	 */
	private Grid<Feed> createFeedsTable(){
		Grid<Feed> table = new Grid<>(Feed.class);
		table.setDataProvider(feeds);
		table.setSizeFull();
		table.setSelectionMode(Grid.SelectionMode.SINGLE);
        table.setColumns("url");
        table.removeHeaderRow(0);

		table.addSelectionListener((event) -> {
            Optional<Feed> feed = event.getFirstSelectedItem();
            if(feed.isPresent()) {
                List<FeedEntry> entries = feed.get().getEntries();
                entryTable.setItems(entries.toArray(new FeedEntry[entries.size()]));
            }
        });

		GridContextMenu<Feed> contextMenu = new GridContextMenu<>(table);
		contextMenu.addGridBodyContextMenuListener((event) -> {
			ContextMenu menu = event.getContextMenu();
			menu.removeItems();
			menu.addItem("Add RSS/Atom feed", (item) -> addFeed());
			menu.addItem("Remove RSS/Atom feed", (item) -> removeFeed((Feed) event.getItem()));
			menu.addItem("Syncronize feed", (feed) -> syncronizeFeed((Feed) event.getItem()));
		});

		return table;
	}

	/**
	 * Creates the table on the top where the selected feeds entries are displayed.
	 */
	private Grid<FeedEntry> createEntriesTable(){
		Grid<FeedEntry> table = new Grid<>(FeedEntry.class);
		table.setSizeFull();
		table.setSelectionMode(Grid.SelectionMode.SINGLE);
		table.addItemClickListener(event -> setContent(event.getItem()));
		table.setColumns("title");
		table.removeHeaderRow(0);
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
			title.setStyleName(ValoTheme.LABEL_H1);
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
		addFeedWindow.addCloseListener((Window.CloseListener) e -> {
            Feed feed = addFeedWindow.getFeed();
            if(feed != null){
                feedService.add(feed);
                feedService.syncronize(feed);
                feeds.refreshAll();
                entryTable.getDataProvider().refreshAll();
            }
        });
		getUI().addWindow(addFeedWindow);
	}

	private void removeFeed(Feed feed) {
		feedService.remove(feed);
		feeds.refreshAll();
        feeds.fetch(new Query<>())
                .findFirst()
                .ifPresent(f -> feedTable.select(f));
	}

	private void syncronizeFeed(Feed feed) {
		feedService.syncronize(feed);
		feeds.refreshItem(feed);
		entryTable.getDataProvider().refreshAll();
	}
}
