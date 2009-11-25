package org.crossref.pdfmark;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CrossRefMetadata {
	
	private static XPathExpression TITLES_EXPR;
	private static XPathExpression AUTHORS_EXPR;
	private static XPathExpression GIVEN_NAME_EXPR;
	private static XPathExpression SURNAME_EXPR;
	private static XPathExpression DATE_EXPR;
	private static XPathExpression DAY_EXPR;
	private static XPathExpression MONTH_EXPR;
	private static XPathExpression YEAR_EXPR;
	
	private Document doc;
	
	static {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
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
			System.err.println("Error: Malformed XPath expressions.");
			System.err.println(e);
			System.exit(2);
		}
	}
	
	public CrossRefMetadata(Document doc) {
		this.doc = doc;
	}
	
	public String getDoi() {
		return "";
	}

	public String[] getTitles() throws XPathExpressionException {
		NodeList ts = (NodeList) TITLES_EXPR.evaluate(doc, XPathConstants.NODESET);

		String[] strings = new String[ts.getLength()];

		for (int i=0; i<ts.getLength(); i++) {
			strings[i] = ts.item(i).getNodeValue();
		}

		return strings;
	}

	public String[] getContributors() throws XPathExpressionException {
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

	public String getDate() throws XPathExpressionException {
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

}
