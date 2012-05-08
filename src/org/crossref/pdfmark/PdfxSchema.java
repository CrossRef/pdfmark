package org.crossref.pdfmark;

import com.itextpdf.text.xml.xmp.XmpSchema;

public class PdfxSchema extends XmpSchema {
	
	public static final String DEFAULT_XPATH_ID = "pdfx";
	public static final String DEFAULT_XPATH_URI 
				= "http://ns.adobe.com/pdfx/1.3/";
	
	public static final String DOI = "doi";
	
	public PdfxSchema() {
		super("xmlns:" 
	               + DEFAULT_XPATH_ID 
	               + "=\"" + DEFAULT_XPATH_URI + "\"");
	}

}
