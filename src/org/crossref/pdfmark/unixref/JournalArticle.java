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
package org.crossref.pdfmark.unixref;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JournalArticle {
	
	private static XPathExpression TITLES_EXPR;
	private static XPathExpression AUTHORS_EXPR;
	private static XPathExpression GIVEN_NAME_EXPR;
	private static XPathExpression SURNAME_EXPR;
	private static XPathExpression DATE_EXPR;
	private static XPathExpression DAY_EXPR;
	private static XPathExpression MONTH_EXPR;
	private static XPathExpression YEAR_EXPR;
	private static XPathExpression DOI_EXPR;
	
	private Node articleNode;
	
	private String[] titles, contributors;
	
	private String publishedDate, doi;
	
	public JournalArticle(Document doc, Node newArticleNode) 
			throws XPathExpressionException {
		articleNode = newArticleNode;
		
		XPath xpath = Unixref.getXPath(doc);
		
		TITLES_EXPR = xpath.compile("cr:titles/cr:title");
		AUTHORS_EXPR = xpath.compile("cr:contributors/cr:person_name"
				+ "[@contributor_role='author']");
		GIVEN_NAME_EXPR = xpath.compile("cr:given_name");
		SURNAME_EXPR = xpath.compile("cr:surname");
		DATE_EXPR = xpath.compile("cr:publication_date");
		DAY_EXPR = xpath.compile("cr:day");
		MONTH_EXPR = xpath.compile("cr:month");
		YEAR_EXPR = xpath.compile("cr:year");
		DOI_EXPR = xpath.compile("cr:doi_data/cr:doi");
	}
	
	public String[] getTitles() throws XPathExpressionException {
		if (titles != null) {
			return titles;
		}
		
		NodeList ts = (NodeList) TITLES_EXPR.evaluate(articleNode, 
				 									  XPathConstants.NODESET);

		String[] strings = new String[ts.getLength()];

		for (int i=0; i<ts.getLength(); i++) {
			strings[i] = ts.item(i).getTextContent();
		}

		return titles = strings;
	}

	public String[] getContributors() throws XPathExpressionException {
		if (contributors != null) {
			return contributors;
		}
		
		NodeList s = (NodeList) AUTHORS_EXPR.evaluate(articleNode, 
				  									  XPathConstants.NODESET);

		String[] names = new String[s.getLength()];

		for (int i=0; i<s.getLength(); i++) {
			Node a = s.item(i);
			Node given = (Node) GIVEN_NAME_EXPR.evaluate(a, XPathConstants.NODE);
			Node surname = (Node) SURNAME_EXPR.evaluate(a, XPathConstants.NODE);
			names[i] = given.getTextContent() + " " + surname.getTextContent();
		}

		return contributors = names;
	}

	public String getDate() throws XPathExpressionException {
		if (publishedDate != null) {
			return publishedDate;
		}
		
		String date = "";
		Node pubDate = (Node) DATE_EXPR.evaluate(articleNode, 
											     XPathConstants.NODE);

		if (pubDate != null) {
			Node year = (Node) YEAR_EXPR.evaluate(pubDate, XPathConstants.NODE);
			Node month = (Node) MONTH_EXPR.evaluate(pubDate, XPathConstants.NODE);
			Node day = (Node) DAY_EXPR.evaluate(pubDate, XPathConstants.NODE);

			// TODO What if month and day are not two digits strings?
			date = year.getTextContent();
			if (month != null) {
				date += "-" + month.getTextContent();
				if (day != null) {
					date += "-" + day.getTextContent();
				}
			}
		}

		return publishedDate = date;
	}
	
	public String getDoi() throws XPathExpressionException {
		if (doi == null) {
			Node n = (Node) DOI_EXPR.evaluate(articleNode, XPathConstants.NODE);
			
			if (n != null) {
				doi = n.getTextContent();
			} else {
				doi = "";
			}
		}
		return doi;
	}

}
