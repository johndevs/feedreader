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

package fi.jasoft.feedreader.data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;
import javax.persistence.OrderColumn;

/**
 * A entry in a RSS/ATOM feed
 * 
 * @author John Ahlroos / www.jasoft.fi
 */
@Embeddable
public class FeedEntry {

	@Column(length=256)
	String title;
	
	@Lob
	String content;
	
	@Column(length=256)
	String url;

	/**
	 * Get the title of the feed entry
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the title of the feed entry
	 * 
	 * @param title
	 * 		The title of the entry
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Get the body text of the feed entry. Can be HTML or text
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Set the body text of the feed entry. HTML and text supported
	 * 
	 * @param content
	 * 		The body text
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * The unique url which identified this feed entry. Points to the online version
	 * of the feed entry. 
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set the unique url which identifies this feed entry. Each feed entry in a feed should
	 * have a unique url pointing to the online source of the entry.
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}
}
