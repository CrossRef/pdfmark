package org.crossref.pdfmark;

import org.crossref.pdfmark.prism.Prism21Schema;

import com.itextpdf.text.xml.xmp.DublinCoreSchema;
import com.itextpdf.text.xml.xmp.XmpSchema;

public class DcPrismSet {
    
    private XmpSchema dc = new DublinCoreSchema();
    private XmpSchema prism = new Prism21Schema();
    
    public XmpSchema getPrism() {
        return prism;
    }
    
    public XmpSchema getDc() {
        return dc;
    }

}
