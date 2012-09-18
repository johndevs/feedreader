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
package fi.jasoft.feedreader.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import fi.jasoft.feedreader.data.Feed;
import fi.jasoft.feedreader.data.FeedEntry;

/**
 * Implementation of {@link FeedService} which stores the feeds using 
 * EclipseLink into a HSQLDB database.
 * 
 * @author John Ahlroos / http://www.jasoft.fi
 */
public class FeedServiceImpl implements FeedService{
	
	private static final String PERSISTANCE_UNIT = "feedReader";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void add(Feed feed) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTANCE_UNIT);
        EntityManager em = emf.createEntityManager();
		try{
	        em.getTransaction().begin();
	        em.persist(feed);
	        em.getTransaction().commit();
		} finally {
	        em.close();
	        emf.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(Feed feed) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTANCE_UNIT);
        EntityManager em = emf.createEntityManager();
		try{
			em.getTransaction().begin();
			em.remove(em.find(Feed.class, feed.getId()));
			em.getTransaction().commit();
		} finally{
			em.close();
			emf.close();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(Feed feed) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTANCE_UNIT);
        EntityManager em = emf.createEntityManager();    
        
        try{
        	save(feed, em);
        } finally{
        	em.flush();
        	em.close();
        	emf.close();
        }
	}
	
	/**
	 * Persists a feed using a specific entity manager. Does not close the entitymanager. 
	 * @param feed
	 * 		The feed to persist
	 * @param em
	 * 		The entitymanager to use
	 */
	private void save(Feed feed, EntityManager em){ 
    	em.getTransaction().begin();
        em.merge(feed);
        em.flush();
        em.getTransaction().commit();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Feed> getFeeds() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTANCE_UNIT);
        EntityManager em = emf.createEntityManager();
       
        TypedQuery<Feed> resultQuery = em.createQuery("SELECT f FROM Feed f", Feed.class);
        List<Feed> feeds = resultQuery.getResultList();
        if (feeds == null) {
            feeds = Collections.emptyList();
        }
        em.close();
        emf.close();
        return feeds;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void syncronize(Feed feed) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTANCE_UNIT);
        EntityManager em = emf.createEntityManager();
		
		try {
			URL feedSource = new URL(feed.getUrl());
			SyndFeedInput input = new SyndFeedInput();
		    SyndFeed f = input.build(new XmlReader(feedSource));
	        for (SyndEntry e : (List<SyndEntry>) f.getEntries()) {
	        	
	        	FeedEntry feedEntry = null;
	        	
	        	 // Check if entry exists, if it does then we reuse it
	        	 for(FeedEntry fe : feed.getEntries()){
	        		 if(fe.getUrl() == e.getUri()){
	        			 feedEntry = fe;
	        			 break;
	        		 }
	        	 }
	        	 
	        	 // No feed found, create a new one
	        	 if(feedEntry == null){
	        		 feedEntry = new FeedEntry();
	        		 feed.getEntries().add(feedEntry);
	        	 }
	        	
	        	 feedEntry.setTitle(e.getTitle());
	        	 
	        	 if (e.getDescription() != null) {
	        		 feedEntry.setContent(e.getDescription().getValue());
	             } else {
	                 String content = "";
	                 for (SyndContent c : (List<SyndContent>) e.getContents()) {
	                     content += c.getValue();
	                 }
	                 feedEntry.setContent(content);
	             }
	        }
	        save(feed, em);
	        
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (FeedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			em.close();
			emf.close();
		}
	}
}
