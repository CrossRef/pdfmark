package org.crossref.pdfmark;

import org.crossref.pdfmark.prism.Prism21Schema;

import com.itextpdf.text.xml.xmp.DublinCoreSchema;
import com.itextpdf.text.xml.xmp.XmpSchema;

public class SchemaSet {
    
    private XmpSchema dc = new DublinCoreSchema();
    private XmpSchema prism = new Prism21Schema();
    private XmpSchema pdfx = new PdfxSchema();
    
    public XmpSchema getPrism() {
        return prism;
    }
    
    public XmpSchema getDc() {
        return dc;
    }
    
    public XmpSchema getPdfx() {
    	return pdfx;
    }

}
