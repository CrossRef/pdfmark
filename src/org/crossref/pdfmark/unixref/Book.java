package org.crossref.pdfmark.unixref;

import javax.xml.xpath.XPath;
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

public class Book extends Work {
    
    private static XPathExpression EDITION_NUMBER_EXPR;
    private static XPathExpression ISBN_EXPR;
    private static XPathExpression DOI_EXPR;
    private static XPathExpression ISSN_EXPR;
    
    private static XPathExpression BOOK_META_EXPR;
    private static XPathExpression BOOK_SET_META_EXPR;
    private static XPathExpression BOOK_SERIES_META_EXPR;
    
    private String[] titles, contributors;
    
    private String editionNumber, publicationDate, isbn, doi, issn, year;
    
    private Node bookNode;
    
    /** One of book_metadata, book_set_metadata or book_series_metadata. */
    private Node mdNode;
    
    public Book(Document doc, Node newBookNode) 
            throws XPathExpressionException {
        bookNode = newBookNode;

        XPath xpath = Unixref.getXPath(doc);
        
        BOOK_META_EXPR = xpath.compile("book_metadata");
        BOOK_SET_META_EXPR = xpath.compile("book_set_metadata");
        BOOK_SERIES_META_EXPR = xpath.compile("book_series_metadata");
        
        EDITION_NUMBER_EXPR = xpath.compile("edition_number");
        DOI_EXPR = xpath.compile("doi_data/doi");
        ISBN_EXPR = xpath.compile("isbn");
        ISSN_EXPR = xpath.compile("issn");
        
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
    
    public String getYear() throws XPathExpressionException {
        if (year == null) {
            year = Unixref.getPublicationYear(mdNode);
        }
        return year;
    }
    
    public void writeXmp(SchemaSet schemaSet) throws XPathExpressionException {
        XmpSchema dc = schemaSet.getDc();
        XmpSchema prism = schemaSet.getPrism();
        XmpSchema pdfx = schemaSet.getPdfx();
        
        addToSchema(dc, DublinCoreSchema.CREATOR, getContributors());
        addToSchema(dc, DublinCoreSchema.TITLE, getTitles());
        addToSchema(dc, DublinCoreSchema.DATE, getPublicationDate());
        addToSchema(dc, DublinCoreSchema.IDENTIFIER, getDoi());
        
        addToSchema(prism, Prism21Schema.PUBLICATION_DATE, getPublicationDate());
        addToSchema(prism, Prism21Schema.DOI, getDoi());
        addToSchema(prism, Prism21Schema.URL, MarkBuilder.getUrlForDoi(getDoi()));
        addToSchema(prism, Prism21Schema.ISSUE_IDENTIFIER, getDoi());
        addToSchema(prism, Prism21Schema.EDITION, getEditionNumber());
        addToSchema(prism, Prism21Schema.ISBN, getIsbn());
        addToSchema(prism, Prism21Schema.ISSN, getIssn());
        
        addToSchema(pdfx, PdfxSchema.DOI, getDoi());
        
        // TODO:
        //addToSchema(prism, Prism21Schema.PUBLICATION_NAME, getFullTitle());
    }

}
