package org.crossref.pdfmark;

import com.lowagie.text.xml.xmp.XmpSchema;

public class AnyXmpSchema extends XmpSchema {
	
	public AnyXmpSchema(String nsPrefix, String nsUri) {
		super("xmlns:" + nsPrefix + "=\"" + nsUri + "\"");
	}

}
