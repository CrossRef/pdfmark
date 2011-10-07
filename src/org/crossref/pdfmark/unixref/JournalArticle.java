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

import org.crossref.pdfmark.XPathHelpers;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JournalArticle {
	
	private static XPathExpression DOI_EXPR;
	private static XPathExpression FIRST_PAGE_EXPR;
	private static XPathExpression LAST_PAGE_EXPR;
	
	private Node articleNode;
	
	private String[] titles, contributors;
	
	private String publishedDate, doi, firstPage, lastPage, year;
	
	public JournalArticle(Document doc, Node newArticleNode) 
			throws XPathExpressionException {
		articleNode = newArticleNode;
		
		XPath xpath = Unixref.getXPath(doc);
		
		DOI_EXPR = xpath.compile("doi_data/doi");
		FIRST_PAGE_EXPR = xpath.compile("pages/first_page");
		LAST_PAGE_EXPR = xpath.compile("pages/last_page");
	}
	
	public String[] getTitles() throws XPathExpressionException {
		if (titles == null) {
		    titles = Unixref.getTitles(articleNode);
		}
		return titles;
	}

	public String[] getContributors() throws XPathExpressionException {
        if (contributors == null) {
            contributors = Unixref.getContributors(articleNode);
        }
        return contributors;
    }
	
	public String getDate() throws XPathExpressionException {
	    if (publishedDate == null) {
	        publishedDate = Unixref.getPublicationDate(articleNode);
	    }
	    return publishedDate;
	}
	
	public String getFirstPage() throws XPathExpressionException {
	    if (firstPage == null) {
            firstPage = XPathHelpers.orEmptyStr(FIRST_PAGE_EXPR, articleNode);
        }
        return firstPage;
	}
	
	public String getLastPage() throws XPathExpressionException {
	    if (lastPage == null) {
            lastPage = XPathHelpers.orEmptyStr(LAST_PAGE_EXPR, articleNode);
	    }
        return lastPage;
	}
	
	public String getDoi() throws XPathExpressionException {
		if (doi == null) {
		    doi = XPathHelpers.orEmptyStr(DOI_EXPR, articleNode);
        }
        return doi;
	}
	
	public String getYear() throws XPathExpressionException {
	    if (year == null) {
	        year = Unixref.getPublicationYear(articleNode);
	    }
	    return year;
	}

}
