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
import javax.xml.xpath.XPathFactory;

import org.crossref.pdfmark.XPathHelpers;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Unixref {
	
	public enum Type {
		JOURNAL,
		BOOK,
		DISSERTATION,
		CONFERENCE,
		REPORT_PAPER,
		STANDARD,
		OTHER,
	}
	
	/* Work type */
	private static XPathExpression JOURNAL_EXPR;
	private static XPathExpression BOOK_EXPR;
	private static XPathExpression DISSERTATION_EXPR;
	private static XPathExpression CONFERENCE_EXPR;
	private static XPathExpression REPORT_PAPER_EXPR;
    private static XPathExpression STANDARD_EXPR;
	private static XPathExpression OWNER_PREFIX_EXPR;
	
	/* Publication date */
    private static XPathExpression DATE_EXPR;
    private static XPathExpression YEAR_EXPR;
    private static XPathExpression MONTH_EXPR;
    private static XPathExpression DAY_EXPR;
    
    /* Titles and authors */
    private static XPathExpression TITLES_EXPR;
    private static XPathExpression AUTHORS_EXPR;
    private static XPathExpression GIVEN_NAME_EXPR;
    private static XPathExpression SURNAME_EXPR;
	
	private XPath xpath;
	
	private Document doc;
	
	private String ownerPrefix;
	
	public static XPath getXPath(Document doc) {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		return xpath;
	}
	
	public Unixref(Document doc) throws XPathExpressionException {
		this.doc = doc;
		this.xpath = getXPath(doc);
			
		JOURNAL_EXPR = xpath.compile("//journal");
		BOOK_EXPR = xpath.compile("//book");
		DISSERTATION_EXPR = xpath.compile("//dissertation");
		CONFERENCE_EXPR = xpath.compile("//conference");
		REPORT_PAPER_EXPR = xpath.compile("//report-paper");
		STANDARD_EXPR = xpath.compile("//standard");
		OWNER_PREFIX_EXPR = xpath.compile("//doi_record/@owner");
		
		DATE_EXPR = xpath.compile("publication_date");
        DAY_EXPR = xpath.compile("day");
        MONTH_EXPR = xpath.compile("month");
        YEAR_EXPR = xpath.compile("year");
        
        TITLES_EXPR = xpath.compile("titles/title");
        AUTHORS_EXPR = xpath.compile("contributors/person_name"
                + "[@contributor_role='author']");
        GIVEN_NAME_EXPR = xpath.compile("given_name");
        SURNAME_EXPR = xpath.compile("surname");
	}
	
	public String getOwnerPrefix() throws XPathExpressionException {
	    if (ownerPrefix == null) {
	        ownerPrefix = XPathHelpers.orEmptyStr(OWNER_PREFIX_EXPR, doc);
	    }
	    return ownerPrefix;
	}
	
	public Type getType() throws XPathExpressionException {
		try {
			if (JOURNAL_EXPR.evaluate(doc, XPathConstants.BOOLEAN)
					.equals(Boolean.TRUE)) {
				return Type.JOURNAL;
			} else if ((Boolean) BOOK_EXPR.evaluate(doc, XPathConstants.BOOLEAN)
					.equals(Boolean.TRUE)) {
				return Type.BOOK;
			} else if ((Boolean) DISSERTATION_EXPR.evaluate(doc, XPathConstants.BOOLEAN)
					.equals(Boolean.TRUE)) {
				return Type.DISSERTATION;
			} else if ((Boolean) CONFERENCE_EXPR.evaluate(doc, XPathConstants.BOOLEAN)
					.equals(Boolean.TRUE)) {
				return Type.CONFERENCE;
			} else if ((Boolean) REPORT_PAPER_EXPR.evaluate(doc, XPathConstants.BOOLEAN)
					.equals(Boolean.TRUE)) {
				return Type.REPORT_PAPER;
			} else if ((Boolean) STANDARD_EXPR.evaluate(doc, XPathConstants.BOOLEAN)
			        .equals(Boolean.TRUE)) {
			    return Type.STANDARD;
			}
		} catch (XPathExpressionException e) {
			/* Do nothing. */
		}
		return Type.OTHER;
	}
	
	public Journal getJournal() throws XPathExpressionException {
		return new Journal(doc, (Node) JOURNAL_EXPR.evaluate(doc, XPathConstants.NODE));
	}
	
	public Book getBook() throws XPathExpressionException {
	    return new Book(doc, (Node) BOOK_EXPR.evaluate(doc, XPathConstants.NODE));
	}
	
	static String getPublicationDate(Node work) throws XPathExpressionException {
        String date = "";
        Node pubDate = (Node) DATE_EXPR.evaluate(work, XPathConstants.NODE);

        if (pubDate != null) {
            date = XPathHelpers.evalConcat(pubDate, "-", YEAR_EXPR, MONTH_EXPR, 
                                                        DAY_EXPR);
        }

        return date;
    }
	
	static String getPublicationYear(Node work) throws XPathExpressionException {
	    String year = "";
	    Node pubDate = (Node) DATE_EXPR.evaluate(work, XPathConstants.NODE);
	    
	    if (pubDate != null) {
	        year = XPathHelpers.orEmptyStr(YEAR_EXPR, pubDate);
	    }
	    
	    return year;
	}
	
	static String[] getContributors(Node work) throws XPathExpressionException {
        NodeList s = (NodeList) AUTHORS_EXPR.evaluate(work, XPathConstants.NODESET);

        String[] names = new String[s.getLength()];

        for (int i=0; i<s.getLength(); i++) {
            Node a = s.item(i);
            names[i] = XPathHelpers.evalConcat(a, " ", GIVEN_NAME_EXPR, SURNAME_EXPR);
        }

        return names;
    }
	
	static String[] getTitles(Node work) throws XPathExpressionException {
        NodeList ts = (NodeList) TITLES_EXPR.evaluate(work, XPathConstants.NODESET);

        String[] strings = new String[ts.getLength()];

        for (int i=0; i<ts.getLength(); i++) {
            strings[i] = ts.item(i).getTextContent();
        }

        return strings;
    }
}
