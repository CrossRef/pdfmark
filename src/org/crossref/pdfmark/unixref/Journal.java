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

import org.crossref.pdfmark.PdfxSchema;
import org.crossref.pdfmark.SchemaSet;
import org.crossref.pdfmark.MarkBuilder;
import org.crossref.pdfmark.XPathHelpers;
import org.crossref.pdfmark.prism.Prism21Schema;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.itextpdf.text.xml.xmp.DublinCoreSchema;
import com.itextpdf.text.xml.xmp.XmpSchema;
import com.itextpdf.text.xml.xmp.XmpWriter;

public class Journal extends Work {
	
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
		
		JOURNAL_ARTICLE_EXPR = xpath.compile("journal_article");
		PRINT_ISSN_EXPR = xpath.compile("journal_metadata"
				+ "/issn[@media_type='print']");
		ELECTRONIC_ISSN_EXPR = xpath.compile("journal_metadata"
				+ "/issn[@media_type='electronic']");
		ANY_ISSN_EXPR = xpath.compile("journal_metadata"
				+ "/issn");
		DOI_EXPR = xpath.compile("journal_metadata/doi_data/doi");
		TITLE_EXPR = xpath.compile("journal_metadata/full_title");
		VOLUME_EXPR = xpath.compile("journal_issue/journal_volume/volume");
		ISSUE_EXPR = xpath.compile("journal_issue/issue");
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
		    electronicIssn = XPathHelpers.orEmptyStr(ELECTRONIC_ISSN_EXPR, 
		                                             journalNode);
        }
        return electronicIssn;
	}
	
	public String getDoi() throws XPathExpressionException {
	    if (doi == null) {
            doi = XPathHelpers.orEmptyStr(DOI_EXPR, journalNode);
        }
        return doi;
	}
	
	public String getVolume() throws XPathExpressionException {
		if (volume == null) {
		    volume = XPathHelpers.orEmptyStr(VOLUME_EXPR, journalNode);
		}
		return volume;
	}
	
	public String getIssue() throws XPathExpressionException {
	    if (issue == null) {
	        issue = XPathHelpers.orEmptyStr(ISSUE_EXPR, journalNode);
	    }
	    return issue;
	}
	
	public String getFullTitle() throws XPathExpressionException {
	    if (title == null) {
            title = XPathHelpers.orEmptyStr(TITLE_EXPR, journalNode);
        }
        return title;
	}
	
	public String getYear() throws XPathExpressionException {
	    return getArticle().getYear();
	}
	
	public void writeXmp(SchemaSet schemaSet) throws XPathExpressionException {
	    JournalArticle article = getArticle();
	    XmpSchema dc = schemaSet.getDc();
	    XmpSchema prism = schemaSet.getPrism();
	    XmpSchema pdfx = schemaSet.getPdfx();
	    
        addToSchema(dc, DublinCoreSchema.CREATOR, article.getContributors());
        addToSchema(dc, DublinCoreSchema.TITLE, article.getTitles());
        addToSchema(dc, DublinCoreSchema.DATE, article.getDate());
        addToSchema(dc, DublinCoreSchema.IDENTIFIER, article.getDoi());
        
        addToSchema(prism, Prism21Schema.PUBLICATION_DATE, article.getDate());
        addToSchema(prism, Prism21Schema.DOI, article.getDoi());
        addToSchema(prism, Prism21Schema.ISSN, getPreferredIssn());
        addToSchema(prism, Prism21Schema.E_ISSN, getElectronicIssn());
        addToSchema(prism, Prism21Schema.ISSUE_IDENTIFIER, getDoi());
        addToSchema(prism, Prism21Schema.PUBLICATION_NAME, getFullTitle());
        addToSchema(prism, Prism21Schema.VOLUME, getVolume());
        addToSchema(prism, Prism21Schema.NUMBER, getIssue());
        addToSchema(prism, Prism21Schema.STARTING_PAGE, article.getFirstPage());
        addToSchema(prism, Prism21Schema.ENDING_PAGE, article.getLastPage());
        addToSchema(prism, Prism21Schema.URL, MarkBuilder.getUrlForDoi(article.getDoi()));
        
        addToSchema(pdfx, PdfxSchema.DOI, article.getDoi());
	}

}
