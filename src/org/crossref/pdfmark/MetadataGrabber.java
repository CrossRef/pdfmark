package org.crossref.pdfmark;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MetadataGrabber {
	
	public static final int CLIENT_EXCEPTION_CODE = -1;
	public static final int CRUMMY_XML_CODE = -2;
	
	private static final String HOST_NAME = "api.labs.crossref.org";
	
	private HttpClient client;
	
	private Queue<RequestInfo> requests;
	
	private DocumentBuilder builder;
	
	private boolean processing;
	
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
			domFactory.setNamespaceAware(false);
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
						dealWithRequest(requests.remove());
					}
					
					processing = false;
					
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
			    req.handler.onMetadata(new CrossRefMetadata(doc));
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
		}
	}
	
	public boolean isProcessing() {
		return processing;
	}
	
	public void grabOne(String doi, Handler handler) {
		String uri = "http://" + HOST_NAME + "/" + doi + ".xml";
		HttpGet getRequest = new HttpGet(uri);
		
		RequestInfo ri = new RequestInfo();
		ri.doi = doi;
		ri.request = getRequest;
		ri.handler = handler;
		requests.add(ri);
		
		processing = true;
		synchronized (monitor) {
			monitor.notifyAll();
		}
	}
	
	public interface Handler {
		public void onMetadata(CrossRefMetadata metadata);
		public void onFailure(String doi, int code, String msg);
	}

}
