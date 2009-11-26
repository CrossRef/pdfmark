package org.crossref.pdfmark.unixref;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

public class Journal {
	
	private static XPathExpression JOURNAL_ARTICLE_EXPR;
	
	private Node journalNode;
	
	private JournalArticle journalArticle;
	
	static {
		XPath xpath = Unixref.getXPath();
		try {
			JOURNAL_ARTICLE_EXPR = xpath.compile("//cr:journal_article");
		} catch (XPathExpressionException e) {
			System.err.println("Error: Malformed XPath expressions.");
			System.err.println(e);
			System.exit(2);
		}
	}
	
	public Journal(Node newJournalNode) {
		journalNode = newJournalNode;
	}
	
	public JournalArticle getArticle() throws XPathExpressionException {
		if (journalArticle == null) {
			Node n = (Node) JOURNAL_ARTICLE_EXPR.evaluate(journalNode, 
				      							          XPathConstants.NODE);
			journalArticle = new JournalArticle(n);
		}
		return journalArticle;
	}

}
