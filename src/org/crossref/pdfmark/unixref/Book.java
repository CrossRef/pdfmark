package org.crossref.pdfmark.unixref;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.crossref.pdfmark.XPathHelpers;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Book {
    
    private static XPathExpression EDITION_NUMBER_EXPR;
    private static XPathExpression ISBN_EXPR;
    private static XPathExpression DOI_EXPR;
    private static XPathExpression ISSN_EXPR;
    
    private static XPathExpression BOOK_META_EXPR;
    private static XPathExpression BOOK_SET_META_EXPR;
    private static XPathExpression BOOK_SERIES_META_EXPR;
    
    private String[] titles, contributors;
    
    private String editionNumber, publicationDate, isbn, doi, issn;
    
    private Node bookNode;
    
    /** One of book_metadata, book_set_metadata or book_series_metadata. */
    private Node mdNode;
    
    public Book(Document doc, Node newBookNode) 
            throws XPathExpressionException {
        bookNode = newBookNode;

        XPath xpath = Unixref.getXPath(doc);
        
        BOOK_META_EXPR = xpath.compile("cr:book_metadata");
        BOOK_SET_META_EXPR = xpath.compile("cr:book_set_metadata");
        BOOK_SERIES_META_EXPR = xpath.compile("cr:book_series_metadata");
        
        EDITION_NUMBER_EXPR = xpath.compile("cr:edition_number");
        DOI_EXPR = xpath.compile("cr:doi_data/cr:doi");
        ISBN_EXPR = xpath.compile("cr:isbn");
        ISSN_EXPR = xpath.compile("cr:issn");
        
        mdNode = XPathHelpers.oneOf(bookNode, BOOK_META_EXPR, BOOK_SET_META_EXPR, 
                                    BOOK_SERIES_META_EXPR);
    }
    
    public String[] getTitles() throws XPathExpressionException {
        if (titles == null) {
            titles = Unixref.getTitles(mdNode);
        }
        return titles;
    }
    
    public String[] getContributors() throws XPathExpressionException {
        if (contributors == null) {
            contributors = Unixref.getContributors(mdNode);
        }
        return contributors;
    }
    
    public String getPublicationDate() throws XPathExpressionException {
        if (publicationDate == null) {
            publicationDate = Unixref.getPublicationDate(mdNode);
        }
        return publicationDate;
    }
    
    public String getEditionNumber() throws XPathExpressionException {
        if (editionNumber == null) {
            editionNumber = XPathHelpers.orEmptyStr(EDITION_NUMBER_EXPR, mdNode);
        }
        return editionNumber;
    }
    
    public String getIsbn() throws XPathExpressionException {
        if (isbn == null) {
            isbn = XPathHelpers.orEmptyStr(ISBN_EXPR, mdNode);
        }
        return isbn;
    }
    
    public String getDoi() throws XPathExpressionException {
        if (doi == null) {
            doi = XPathHelpers.orEmptyStr(DOI_EXPR, mdNode);
        }
        return doi;
    }
    
    public String getIssn() throws XPathExpressionException {
        if (issn == null) {
            issn = XPathHelpers.orEmptyStr(ISSN_EXPR, mdNode);
        }
        return issn;
    }

}
