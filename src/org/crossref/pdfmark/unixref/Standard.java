package org.crossref.pdfmark.unixref;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.crossref.pdfmark.SchemaSet;
import org.crossref.pdfmark.XPathHelpers;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Standard extends Work {
    
    private static XPathExpression REPORT_META_EXPR;
    private static XPathExpression CONFPROC_META_EXPR;
    private static XPathExpression DISSERTATION_META_EXPR;
    
    private static XPathExpression ISSUE_NUMBER_EXPR;
    private static XPathExpression EDITION_NUMBER_EXPR;
    private static XPathExpression ISSN_EXPR;
    private static XPathExpression ISBN_EXPR;
    private static XPathExpression SINGLE_CONTRIBUTOR_EXPR;
    private static XPathExpression DOI_EXPR;
    private static XPathExpression INSTITUTION_NAME_EXPR;
    private static XPathExpression INSTITUTION_LOC_EXPR;
    private static XPathExpression NUMBER_EXPR;
    
    private Node workNode;
    private Node mdNode;
    
    private String[] titles, contributors;
    
    private String publicationDate, issueNumber, editionNumber, issn, isbn,
                   doi, singleContributor, institutionName, institutionLocation,
                   number;
    
    public Standard(Document doc, Node workNode) throws XPathExpressionException {
        this.workNode = workNode;
        
        XPath xpath = Unixref.getXPath(doc);
        
        REPORT_META_EXPR = xpath.compile("report");
        
        mdNode = XPathHelpers.oneOf(workNode, REPORT_META_EXPR, 
                                    CONFPROC_META_EXPR, DISSERTATION_META_EXPR);
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
    
    @Override
    public String getYear() throws XPathExpressionException {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void writeXmp(SchemaSet dcPrism) throws XPathExpressionException {
        // TODO Auto-generated method stub
        
    }

}
