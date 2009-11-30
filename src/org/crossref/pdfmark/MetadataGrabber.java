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
import org.crossref.pdfmark.unixref.Unixref;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MetadataGrabber {
	
	public static final int CLIENT_EXCEPTION_CODE = -1;
	public static final int CRUMMY_XML_CODE = -2;
	public static final int BAD_XPATH_CODE = -3;
	
	private static final String HOST_NAME = "api.labs.crossref.org";
	
	private HttpClient client;
	
	private Queue<RequestInfo> requests;
	
	private DocumentBuilder builder;
	
	private boolean terminated;
	
	private Object monitor = new Object();
	
	private class RequestInfo {
		private String doi;
		private HttpUriRequest request;
		private Handler handler;
	}
	
	public MetadataGrabber() {
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
						dealWithRequest(requests.peek());
						
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
	
	private void dealWithRequest(RequestInfo req) {
		try {
			HttpResponse sponse = client.execute(req.request);
			HttpEntity entity = sponse.getEntity();
			
			if (entity != null) {
			    Document doc = builder.parse(entity.getContent());
			    req.handler.onMetadata(req.doi, new Unixref(doc));
			} else {
				StatusLine sl = sponse.getStatusLine();
				req.handler.onFailure(req.doi, 
						              sl.getStatusCode(), 
						              sl.getReasonPhrase());
			}
		} catch (ClientProtocolException e) {
			req.handler.onFailure(req.doi, CLIENT_EXCEPTION_CODE, e.toString());
		} catch (IOException e) {
			req.handler.onFailure(req.doi, CLIENT_EXCEPTION_CODE, e.toString());
		} catch (SAXException e) {
			req.handler.onFailure(req.doi, CRUMMY_XML_CODE, e.toString());
		} catch (XPathExpressionException e) {
			req.handler.onFailure(req.doi, BAD_XPATH_CODE, e.toString());
		}
	}
	
	public void grabOne(String doi, Handler handler) {
		String uri = "http://" + HOST_NAME + "/" + doi + ".xml";
		HttpGet getRequest = new HttpGet(uri);
		
		RequestInfo ri = new RequestInfo();
		ri.doi = doi;
		ri.request = getRequest;
		ri.handler = handler;
		requests.add(ri);
		
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
		public void onFailure(String doi, int code, String msg);
	}

}
