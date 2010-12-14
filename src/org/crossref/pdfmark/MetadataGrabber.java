/*
 * Copyright 2009 CrossRef.org (email: support@crossref.org)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.crossref.pdfmark;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.crossref.pdfmark.pub.Publisher;
import org.crossref.pdfmark.unixref.Unixref;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MetadataGrabber {
	
	public static final int CLIENT_EXCEPTION_CODE = -1;
	public static final int CRUMMY_XML_CODE = -2;
	public static final int BAD_XPATH_CODE = -3;
	
	private static final String DOI_QUERY = 
	    "http://www.crossref.org/openurl/" +
		"?id=doi:{0}&noredirect=true" +
		"&pid={1}" +
		"&format=unixref";

    private static final String PUBLISHER_QUERY = 
	    "http://www.crossref.org/" +
		"getPrefixPublisher/" +
		"?prefix={0}";
    
    private static final String QUERY_TOKEN = "{0}";
    
    private static final String KEY_TOKEN = "{1}";
	
	private HttpClient client;
	
	private Queue<RequestInfo> requests;
	
	private DocumentBuilder builder;
	
	private boolean terminated;
	
	private Object monitor = new Object();
	
	private String apiKey;
	
	private enum RequestType {
		DOI,
		PUBLISHER,
	}
	
	private class RequestInfo {
		private String doi;
		private HttpUriRequest request;
		private Handler handler;
		private RequestType requestType;
		
		private RequestInfo(RequestType rt) {
			requestType = rt;
		}
		
		private RequestInfo withRequest(String location, String replacement) {
			String detokRequest = location.replace(QUERY_TOKEN, replacement);
			request = new HttpGet(detokRequest);
			return this;
		}
		
		private RequestInfo withRequest(String location, String replacement,
				String key) {
			String detokRequest = location.replace(QUERY_TOKEN, replacement);
			detokRequest = detokRequest.replace(KEY_TOKEN, key);
			request = new HttpGet(detokRequest);
			return this;
		}
		
		private RequestInfo withDoi(String doi) {
			this.doi = doi;
			return this;
		}
		
		private RequestInfo withHandler(Handler handler) {
			this.handler = handler;
			return this;
		}
		
		private void performOn(HttpClient client) {
			try {
				HttpResponse sponse = client.execute(request);
				HttpEntity entity = sponse.getEntity();
				
				if (entity != null) {
					Document doc = builder.parse(entity.getContent());
					
				    if (requestType == RequestType.DOI) {
					    Unixref unixref = new Unixref(doc);
					    String ownerPrefix = unixref.getOwnerPrefix();
					    handler.onMetadata(doi, unixref);
					    if (!ownerPrefix.isEmpty()) {
					    	queuePubReq(doi, handler, unixref.getOwnerPrefix());
					    } else {
					    	handler.onComplete(doi);
					    }
				    } else if (requestType == RequestType.PUBLISHER) {
				    	Publisher publisher = new Publisher(doc);
				    	handler.onPublisher(doi, publisher);
				    	handler.onComplete(doi);
				    }
				    
				} else {
					StatusLine sl = sponse.getStatusLine();
					handler.onFailure(doi, 
							          sl.getStatusCode(), 
							          sl.getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				handler.onFailure(doi, CLIENT_EXCEPTION_CODE, e.toString());
			} catch (IOException e) {
				handler.onFailure(doi, CLIENT_EXCEPTION_CODE, e.toString());
			} catch (SAXException e) {
				handler.onFailure(doi, CRUMMY_XML_CODE, e.toString());
			} catch (XPathExpressionException e) {
				handler.onFailure(doi, BAD_XPATH_CODE, e.toString());
			}
		}
	}
	
	public MetadataGrabber(String apiKey) {
		this.apiKey = apiKey;
		
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			builder = domFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Error: Can't create an XML parser.");
			System.err.println(e);
			System.exit(2);
		}
		
		client = new DefaultHttpClient();
		requests = new LinkedList<RequestInfo>();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!terminated) {
					while (!requests.isEmpty()) {
						requests.peek().performOn(client);
						
						synchronized (monitor) {
							requests.remove();
							monitor.notifyAll();
						}
					}
					
					synchronized (monitor) {
						try {
							monitor.wait();
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}).start();
	}
	
	public void shutDown() {
		terminated = true;
		synchronized (monitor) {
			monitor.notifyAll();
		}
	}
	
	public void grabOne(String doi, Handler handler) {
		requests.add(new RequestInfo(RequestType.DOI)
					.withDoi(doi)
					.withHandler(handler)
					.withRequest(DOI_QUERY, doi, apiKey));
		
		synchronized (monitor) {
			monitor.notifyAll();
		}
		/* Later, when we receive this response, we will queue
		 * a RequestInfo to get publisher data. */
	}
	
	private void queuePubReq(String doi, Handler handler, String pubPrefix) {
		requests.add(new RequestInfo(RequestType.PUBLISHER)
					.withDoi(doi)
					.withHandler(handler)
					.withRequest(PUBLISHER_QUERY, pubPrefix));
		
		synchronized (monitor) {
			monitor.notifyAll();
		}
	}
	
	public void waitForEmpty() {
		synchronized (monitor) {
			while (true) {
				if (requests.isEmpty()) {
					break;
				}
				try {
					monitor.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}
	
	public interface Handler {
		public void onMetadata(String doi, Unixref metadata);
		public void onPublisher(String doi, Publisher publisher);
		public void onComplete(String doi);
		public void onFailure(String doi, int code, String msg);
	}

}
