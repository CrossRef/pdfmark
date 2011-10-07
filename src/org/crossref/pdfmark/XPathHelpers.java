package org.crossref.pdfmark;

import java.util.ArrayList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

public final class XPathHelpers {
    
    private XPathHelpers() {
    }
    
    public static String orEmptyStr(XPathExpression xpe, Node n) 
            throws XPathExpressionException {
        Node inner = (Node) xpe.evaluate(n, XPathConstants.NODE);
        return inner == null ? "" : inner.getTextContent();
    }
    
    public static String evalConcat(Node n, String delimiter, XPathExpression... exprs) 
            throws XPathExpressionException {
        ArrayList<String> results = new ArrayList<String>();
        for (XPathExpression expr : exprs) {
            results.add(orEmptyStr(expr, n));
        }
        
        while (results.remove(""));
        
        String retn = "";
        
        for (String s : results) {
            retn += s;
            if (results.indexOf(s) != results.size() -1 ) {
                retn += delimiter;
            }
        }
        
        return retn;
    }
    
    public static Node oneOf(Node parent, XPathExpression... exprs) 
            throws XPathExpressionException {
        for (XPathExpression expr : exprs) {
            Node child = (Node) expr.evaluate(parent, XPathConstants.NODE);
            if (child != null) {
                return child;
            }
        }
        return null;
    }
    
}
