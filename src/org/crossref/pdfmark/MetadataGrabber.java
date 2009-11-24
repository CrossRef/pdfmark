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
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MetadataGrabber {
	
	public static final int CLIENT_EXCEPTION_CODE = -1;
	public static final int CRUMMY_XML_CODE = -2;
	
	private static final String HOST_NAME = "api.labs.crossref.org";
	
	private HttpClient client;
	
	private Queue<RequestInfo> requests;
	
	private class RequestInfo {
		private String doi;
		private HttpUriRequest request;
		private Handler handler;
	}
	
	public MetadataGrabber() {
		client = new DefaultHttpClient();
		requests = new LinkedList<RequestInfo>();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (!requests.isEmpty()) {
					RequestInfo req = requests.remove();
					
					try {
						HttpResponse sponse = client.execute(req.request);
						HttpEntity entity = sponse.getEntity();
						if (entity != null) {
							DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
						    domFactory.setNamespaceAware(true);
						    DocumentBuilder builder = domFactory.newDocumentBuilder();
						    Document doc = builder.parse("books.xml");
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
					} catch (ParserConfigurationException e) {
						System.err.println("Warning: Can't create an XML parser.");
						System.err.println(e);
						req.handler.onFailure(req.doi, CLIENT_EXCEPTION_CODE, e.toString());
					} catch (SAXException e) {
						req.handler.onFailure(req.doi, CRUMMY_XML_CODE, e.toString());
					}
				}
			}
		}).start();
	}
	
	private static String[] getTitles(Document doc) {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression authorsExpr = xpath.compile("//contributor[contributor_role='author']");
		XPathExpression givenNameExpr = xpath.compile("//given_name/text()");
		XPathExpression surnameExpr = xpath.compile("//surname/text()");
		
		NodeList authors = (NodeList) authorsExpr.evaluate(doc, XPathConstants.NODESET);
		
		String[] authorNames = new String[authors.getLength()];
		
		for (int i=0; i<authors.getLength(); i++) {
			String givenNames = (String) givenNameExpr.evaluate(authors.item(i), XPathConstants.STRING);
			String surname = (String) surnameExpr.evaluate(authors.item(i), XPathConstants.STRING);
			authorNames[i] = givenNames + " " + surname;
		}
	}
	
	private static String[] getContributors(Document doc) {
		
	}
	
	private static String getDoi(Document doc) {
		
	}
	
	private static String getDate(Document doc) {
		
	}
	
	public void grabOne(String doi, Handler handler) {
		HttpGet getRequest = new HttpGet("http://" + HOST_NAME + "/" + doi + ".xml");
		RequestInfo ri = new RequestInfo();
		ri.doi = doi;
		ri.request = getRequest;
		ri.handler = handler;
		requests.add(ri);
		synchronized (this) {
			notifyAll();
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
