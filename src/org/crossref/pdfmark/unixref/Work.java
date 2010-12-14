package org.crossref.pdfmark.unixref;

import javax.xml.xpath.XPathExpressionException;

import org.crossref.pdfmark.DcPrismSet;

import com.lowagie.text.xml.xmp.XmpArray;
import com.lowagie.text.xml.xmp.XmpSchema;

public abstract class Work {
    
    public static void addToSchema(XmpSchema schema, String key, String val) {
        if (val != null && !val.isEmpty()) {
            schema.setProperty(key, val);
        }
    }
    
    /**
     * Adds a list of values as a bag if the list size is greater than 1,
     * or as a single element if the list size is 1.
     */
    public static void addToSchema(XmpSchema schema, String key, String[] vals) {
        if (vals.length == 1) {
            schema.setProperty(key, vals[0]);
        } else if (vals.length > 1) {
            XmpArray bag = new XmpArray(XmpArray.ORDERED);
            for (String val : vals) {
                bag.add(val);
            }
            schema.setProperty(key, bag);
        }
    }
    
    public abstract String getYear() throws XPathExpressionException;
    
    public abstract void writeXmp(DcPrismSet dcPrism) throws XPathExpressionException;
    
    

}
