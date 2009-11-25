package org.crossref.pdfmark;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MetadataGrabber {
	
	public static final int CLIENT_EXCEPTION_CODE = -1;
	public static final int CRUMMY_XML_CODE = -2;
	
	private static final String HOST_NAME = "api.labs.crossref.org";
	
	private static XPathExpression TITLES_EXPR;
	private static XPathExpression AUTHORS_EXPR;
	private static XPathExpression GIVEN_NAME_EXPR;
	private static XPathExpression SURNAME_EXPR;
	private static XPathExpression DATE_EXPR;
	private static XPathExpression DAY_EXPR;
	private static XPathExpression MONTH_EXPR;
	private static XPathExpression YEAR_EXPR;
	
	private HttpClient client;
	
	private Queue<RequestInfo> requests;
	
	private DocumentBuilder builder;
	
	private boolean processing;
	
	private Object monitor = new Object();
	
	private class RequestInfo {
		private String doi;
		private HttpUriRequest request;
		private Handler handler;
	}
	
	public MetadataGrabber() {
		/* Set up some compiled XPath queries. */
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			builder = domFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.err.println("Error: Can't create an XML parser.");
			System.err.println(e);
			System.exit(2);
		}
		
		try {
			TITLES_EXPR = xpath.compile("//titles/title");
			AUTHORS_EXPR = xpath.compile("//contributors/person_name"
					+ "[@contributor_role='author']");
			GIVEN_NAME_EXPR = xpath.compile("//given_name");
			SURNAME_EXPR = xpath.compile("//surname");
			DATE_EXPR = xpath.compile("//publication_date");
			DAY_EXPR = xpath.compile("//day");
			MONTH_EXPR = xpath.compile("//month");
			YEAR_EXPR = xpath.compile("//year");
		} catch (XPathExpressionException e) {
			System.err.println("Application has malformed XPath expressions.");
			System.err.println(e);
			System.exit(2);
		}
		
		client = new DefaultHttpClient();
		requests = new LinkedList<RequestInfo>();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean terminate = false;
				
				while (!terminate) {
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
	
	private void dealWithRequest(RequestInfo req) {
		try {
			HttpResponse sponse = client.execute(req.request);
			HttpEntity entity = sponse.getEntity();
			if (entity != null) {
			    Document doc = builder.parse(entity.getContent());
			    req.handler.onMetadata(req.doi, 
			    		               getTitles(doc), 
			    		               getContributors(doc), 
			    		               getDate(doc));
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
			req.handler.onFailure(req.doi, CRUMMY_XML_CODE, e.toString());
		}
	}
	
	public boolean isProcessing() {
		return processing;
	}
	
	private static String[] getTitles(Document doc) 
			throws XPathExpressionException {
		NodeList ts = (NodeList) TITLES_EXPR.evaluate(doc, XPathConstants.NODESET);
		
		String[] strings = new String[ts.getLength()];
		
		for (int i=0; i<ts.getLength(); i++) {
			strings[i] = ts.item(i).getNodeValue();
		}
		
		return strings;
	}
	
	private static String[] getContributors(Document doc) 
			throws XPathExpressionException {
		NodeList s = (NodeList) AUTHORS_EXPR.evaluate(doc, XPathConstants.NODESET);
		
		String[] names = new String[s.getLength()];
		
		for (int i=0; i<s.getLength(); i++) {
			Node a = s.item(i);
			Node given = (Node) GIVEN_NAME_EXPR.evaluate(a, XPathConstants.NODE);
			Node surname = (Node) SURNAME_EXPR.evaluate(a, XPathConstants.NODE);
			names[i] = given + " " + surname;
		}
		
		return names;
	}
	
	private static String getDate(Document doc) 
			throws XPathExpressionException {
		String date = "";
		Node pubDate = (Node) DATE_EXPR.evaluate(doc, XPathConstants.NODE);
		
		if (pubDate != null) {
			Node year = (Node) YEAR_EXPR.evaluate(pubDate, XPathConstants.NODE);
			Node month = (Node) MONTH_EXPR.evaluate(pubDate, XPathConstants.NODE);
			Node day = (Node) DAY_EXPR.evaluate(pubDate, XPathConstants.NODE);
			
			// TODO What if month and day are not two digits strings?
			date = year.getNodeValue();
			if (!month.equals("")) {
				date += "-" + month.getNodeValue();
				if (!day.equals("")) {
					date += "-" + day.getNodeValue();
				}
			}
		}
		
		return date;
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
		public void onMetadata(String doi, 
				               String[] titles, 
				               String[] creators, 
				               String publishedDate);
		
		public void onFailure(String doi, int code, String msg);
	}

}
