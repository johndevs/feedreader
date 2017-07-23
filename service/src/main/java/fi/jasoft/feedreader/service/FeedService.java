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
package fi.jasoft.feedreader.service;

import java.util.List;

import fi.jasoft.feedreader.data.Feed;
import fi.jasoft.feedreader.data.FeedEntry;

/**
 * A service for managing feeds
 * 
 * @author John Ahlroos / https://devsoap.com
 */
public interface FeedService {
	
	/**
	 * Add a new feed to the service. This should only be called for new instances which are not
	 * currently managed by the feed service
	 * 
	 * @param feed
	 * 		The new feed to add
	 */
	void add(Feed feed);
	
	/**
	 * Remove a feed from the service. 
	 * 
	 * @param feed
	 * 		The feed to remove.
	 */
	void remove(Feed feed);
	
	/**
	 * Return all feeds managed by the feed service.
	 */
	List<Feed> getFeeds();
	
	/**
	 * Save a changed feed with the service. This should be 
	 * only be used to save changes to feeds, use {@link FeedService#add(Feed)}
	 * to add a new feed.
	 * 
	 * @param feed
	 * 		The feed to save
	 */
	void save(Feed feed);
	
	/**
	 * Synchronize the feed entries with the online version of the RSS/ATOM feed.
	 * 
	 * @param feed
	 * 		The feed to syncronize
	 */
	void syncronize(Feed feed);
}
