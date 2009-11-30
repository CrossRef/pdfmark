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

import org.crossref.pdfmark.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Unixref {
	
	public enum Type {
		JOURNAL,
		BOOK,
		DISSERTATION,
		CONFERENCE,
		REPORT_PAPER,
		OTHER,
	}
	
	private static final String NAMESPACE_PREFIX = "cr";
	private static final String NAMESPACE_URI_10= "http://www.crossref.org/xschema/1.0";
	private static final String NAMESPACE_URI_11 = "http://www.crossref.org/xschema/1.1";
	
	private static XPathExpression JOURNAL_EXPR;
	private static XPathExpression BOOK_EXPR;
	private static XPathExpression DISSERTATION_EXPR;
	private static XPathExpression CONFERENCE_EXPR;
	private static XPathExpression REPORT_PAPER_EXPR;
	
	private static XPath xpath;
	
	private Document doc;
	
	private Journal journal;
	
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
			}
		} catch (XPathExpressionException e) {
			/* Do nothing. */
		}
		return Type.OTHER;
	}
	
	public Journal getJournal() throws XPathExpressionException {
		if (journal == null) {
			Node n = (Node) JOURNAL_EXPR.evaluate(doc, XPathConstants.NODE);
			journal = new Journal(doc, n);
		}
		return journal;
	}
}
