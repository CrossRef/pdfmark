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

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.crossref.pdfmark.XPathHelpers;
import org.crossref.pdfmark.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
	
	private static final String NAMESPACE_PREFIX = "cr";
	private static final String NAMESPACE_URI_10= "http://www.crossref.org/xschema/1.0";
	private static final String NAMESPACE_URI_11 = "http://www.crossref.org/xschema/1.1";
	
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
	
	private static XPath xpath;
	
	private Document doc;
	
	private String ownerPrefix;
	
	// TODO Hard code doi_record namespaces so xpath expressions can be
	// compiled per run.
	public static XPath getXPath(Document doc) {
		if (xpath == null) {
			/* Attempt to determine the ns for the record element. */
			Node rn = doc.getElementsByTagNameNS("*", "doi_record").item(0);
			Element record = (Element) rn;
			final String nsUri = XmlUtils.getNamespaceUriDeclaration(record);
			
			XPathFactory factory = XPathFactory.newInstance();
			xpath = factory.newXPath();
			xpath.setNamespaceContext(new NamespaceContext() {
				@Override
				public String getNamespaceURI(String prefix) {
					if (prefix.equals(NAMESPACE_PREFIX)) {
						return nsUri;
					}
					return XMLConstants.NULL_NS_URI;
				}
	
				@Override
				public String getPrefix(String namespaceURI) {
					if (namespaceURI.equals(NAMESPACE_URI_10)
							|| namespaceURI.equals(NAMESPACE_URI_11)) {
						return NAMESPACE_PREFIX;
					}
					return null;
				}
	
				@Override
				public Iterator getPrefixes(String namespaceURI) {
					return null;
				}
			});
		}
		return xpath;
	}
	
	public Unixref(Document doc) throws XPathExpressionException {
		this.doc = doc;
		
		getXPath(doc);
			
		JOURNAL_EXPR = xpath.compile("//cr:journal");
		BOOK_EXPR = xpath.compile("//cr:book");
		DISSERTATION_EXPR = xpath.compile("//cr:dissertation");
		CONFERENCE_EXPR = xpath.compile("//cr:conference");
		REPORT_PAPER_EXPR = xpath.compile("//cr:report-paper");
		STANDARD_EXPR = xpath.compile("//cr:standard");
		OWNER_PREFIX_EXPR = xpath.compile("//cr:doi_record/@owner");
		
		DATE_EXPR = xpath.compile("cr:publication_date");
        DAY_EXPR = xpath.compile("cr:day");
        MONTH_EXPR = xpath.compile("cr:month");
        YEAR_EXPR = xpath.compile("cr:year");
        
        TITLES_EXPR = xpath.compile("cr:titles/cr:title");
        AUTHORS_EXPR = xpath.compile("cr:contributors/cr:person_name"
                + "[@contributor_role='author']");
        GIVEN_NAME_EXPR = xpath.compile("cr:given_name");
        SURNAME_EXPR = xpath.compile("cr:surname");
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
