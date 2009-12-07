package org.crossref.pdfmark.pub;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Publisher {
	
	private static XPathExpression NAME_EXPR;
	private static XPathExpression ADDRESS_EXPR;
	
	static {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		try {
			NAME_EXPR = xpath.compile("//publisher_name");
			ADDRESS_EXPR = xpath.compile("//publisher_location");
		} catch (XPathExpressionException e) {
			System.err.println("Error: Invalid XPath expressions in Publisher.");
			System.err.println(e);
		}
	}
	
	private Document doc;
	
	private String name, location;
	
	public Publisher(Document newDoc) {
		doc = newDoc;
	}
	
	public String getName() throws XPathExpressionException {
		if (name == null) {
			Node n = (Node) NAME_EXPR.evaluate(doc, XPathConstants.NODE);
			name = n.getTextContent();
		}
		return name;
	}
	
	public String getLocation() throws XPathExpressionException {
		if (location == null) {
			Node n = (Node) ADDRESS_EXPR.evaluate(doc, XPathConstants.NODE);
			location = n.getTextContent();
		}
		return location;
	}

}
