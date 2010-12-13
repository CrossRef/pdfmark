package org.crossref.pdfmark;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

public final class XPathHelpers {
    
    public static String orEmptyStr(XPathExpression xpe, Node n) 
            throws XPathExpressionException {
        Node inner = (Node) xpe.evaluate(n, XPathConstants.NODE);
        return inner == null ? "" : inner.getTextContent();
    }
    
}
