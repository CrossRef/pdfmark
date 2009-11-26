package org.crossref.pdfmark.unixref;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
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
	private static final String NAMESPACE_URI 
					= "http://www.crossref.org/xschema/1.0";
	
	private static XPathExpression JOURNAL_EXPR;
	private static XPathExpression BOOK_EXPR;
	private static XPathExpression DISSERTATION_EXPR;
	private static XPathExpression CONFERENCE_EXPR;
	private static XPathExpression REPORT_PAPER_EXPR;
	
	private static XPath xpath;
	
	private Document doc;
	
	private Journal journal;
	
	static {
		getXPath();
		
		try {
			JOURNAL_EXPR = xpath.compile("//cr:journal");
			BOOK_EXPR = xpath.compile("//cr:book");
			DISSERTATION_EXPR = xpath.compile("//cr:dissertation");
			CONFERENCE_EXPR = xpath.compile("//cr:conference");
			REPORT_PAPER_EXPR = xpath.compile("//cr:report-paper");
		} catch (XPathExpressionException e) {
			System.err.println("Error: Malformed XPath expressions.");
			System.err.println(e);
			System.exit(2);
		}
	}
	
	public static XPath getXPath() {
		if (xpath == null) {
			XPathFactory factory = XPathFactory.newInstance();
			xpath = factory.newXPath();
			xpath.setNamespaceContext(new NamespaceContext() {
				@Override
				public String getNamespaceURI(String prefix) {
					if (prefix.equals(NAMESPACE_PREFIX)) {
						return NAMESPACE_URI;
					}
					return XMLConstants.NULL_NS_URI;
				}
	
				@Override
				public String getPrefix(String namespaceURI) {
					if (namespaceURI.equals(NAMESPACE_URI)) {
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
	
	public Unixref(Document doc) {
		this.doc = doc;
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
			journal = new Journal(n);
		}
		return journal;
	}
	
	public String getDoi() {
		return "";
	}
}
