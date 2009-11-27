package org.crossref.pdfmark.unixref;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Journal {
	
	private static XPathExpression JOURNAL_ARTICLE_EXPR;
	private static XPathExpression PRINT_ISSN_EXPR;
	private static XPathExpression ELECTRONIC_ISSN_EXPR;
	private static XPathExpression ANY_ISSN_EXPR;
	private static XPathExpression DOI_EXPR;
	private static XPathExpression TITLE_EXPR;
	private static XPathExpression VOLUME_EXPR;
	private static XPathExpression ISSUE_EXPR;
	
	private Document doc;
	
	private Node journalNode;
	
	private JournalArticle journalArticle;
	
	private String doi, preferredIssn, title, issue, volume, electronicIssn;
	
	public Journal(Document newDoc, Node newJournalNode) 
			throws XPathExpressionException {
		journalNode = newJournalNode;
		doc = newDoc;
		
		XPath xpath = Unixref.getXPath(doc);
		
		JOURNAL_ARTICLE_EXPR = xpath.compile("cr:journal_article");
		PRINT_ISSN_EXPR = xpath.compile("cr:journal_metadata"
				+ "/cr:issn[@media_type='print']");
		ELECTRONIC_ISSN_EXPR = xpath.compile("cr:journal_metadata"
				+ "/cr:issn[@media_type='electronic']");
		ANY_ISSN_EXPR = xpath.compile("cr:journal_metadata"
				+ "/cr:issn");
		DOI_EXPR = xpath.compile("cr:journal_metadata/cr:doi_data/cr:doi");
		TITLE_EXPR = xpath.compile("cr:journal_metadata/cr:full_title");
		VOLUME_EXPR = xpath.compile("cr:journal_issue/cr:journal_volume/cr:volume");
		ISSUE_EXPR = xpath.compile("cr:journal_issue/cr:issue");
	}
	
	public JournalArticle getArticle() throws XPathExpressionException {
		if (journalArticle == null) {
			Node n = (Node) JOURNAL_ARTICLE_EXPR.evaluate(journalNode, 
				      							          XPathConstants.NODE);
			journalArticle = new JournalArticle(doc, n);
		}
		return journalArticle;
	}
	
	/**
	 * The PRISM 2.1 spec says that if there are multiple ISSNs for the
	 * publication this article was published in, we should prefer the
	 * print ISSN for the <prism:issn> element.
	 */
	public String getPreferredIssn() throws XPathExpressionException {
		if (preferredIssn == null) {
			// Assumption: All ISSNs for a journal are provided in unixref.
			Node n = (Node) PRINT_ISSN_EXPR.evaluate(journalNode, 
					 							     XPathConstants.NODE);
			
			if (n == null) {
				n = (Node) ANY_ISSN_EXPR.evaluate(journalNode,
												  XPathConstants.NODE);
			}
		
			if (n != null) {
				preferredIssn = n.getTextContent();
			} else {
				preferredIssn = "";
			}
		}
		return preferredIssn;
 	}
	
	public String getElectronicIssn() throws XPathExpressionException {
		if (electronicIssn == null) {
			Node n = (Node) ELECTRONIC_ISSN_EXPR.evaluate(journalNode, 
														  XPathConstants.NODE);
			
			if (n != null) {
				electronicIssn = n.getTextContent();
			} else {
				electronicIssn = "";
			}
		}
		return electronicIssn;
	}
	
	public String getDoi() throws XPathExpressionException {
		if (doi == null) {
			Node n = (Node) DOI_EXPR.evaluate(journalNode, XPathConstants.NODE);
			
			if (n != null) {
				doi = n.getTextContent();
			} else {
				doi = "";
			}
		}
		return doi;
	}
	
	public String getVolume() throws XPathExpressionException {
		if (volume == null) {
			Node n = (Node) VOLUME_EXPR.evaluate(journalNode, XPathConstants.NODE);
			
			if (n != null) {
				volume = n.getTextContent();
			} else {
				volume = "";
			}
		}
		return volume;
	}
	
	public String getIssue() throws XPathExpressionException {
		if (issue == null) {
			Node n = (Node) ISSUE_EXPR.evaluate(journalNode, XPathConstants.NODE);
			
			if (n != null) {
				issue = n.getTextContent();
			} else {
				issue = "";
			}
		}
		return issue;
	}
	
	public String getFullTitle() throws XPathExpressionException {
		if (title == null) {
			Node n = (Node) TITLE_EXPR.evaluate(journalNode, XPathConstants.NODE);
			
			if (n != null) {
				title = n.getTextContent();
			} else {
				title = "";
			}
		}
		return title;
	}

}
