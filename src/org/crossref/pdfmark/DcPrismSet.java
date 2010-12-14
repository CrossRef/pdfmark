package org.crossref.pdfmark;

import org.crossref.pdfmark.prism.Prism21Schema;

import com.lowagie.text.xml.xmp.DublinCoreSchema;
import com.lowagie.text.xml.xmp.XmpSchema;

public class DcPrismSet {
    
    private XmpSchema prism = new DublinCoreSchema();
    private XmpSchema dc = new Prism21Schema();
    
    public XmpSchema getPrism() {
        return prism;
    }
    
    public XmpSchema getDc() {
        return dc;
    }

}
