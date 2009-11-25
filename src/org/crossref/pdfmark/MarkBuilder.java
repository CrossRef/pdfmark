package org.crossref.pdfmark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.lowagie.text.xml.xmp.DublinCoreSchema;
import com.lowagie.text.xml.xmp.XmpArray;
import com.lowagie.text.xml.xmp.XmpSchema;
import com.lowagie.text.xml.xmp.XmpWriter;

public abstract class MarkBuilder implements MetadataGrabber.Handler {

	private byte[] xmpData;
	
	@Override
	public void onMetadata(String doi, String[] titles, String[] authors,
			String publishedDate) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		try {
			XmpWriter writer = new XmpWriter(bout);
			
			XmpSchema dc = new DublinCoreSchema();
			addToSchema(dc, DublinCoreSchema.CREATOR, authors);
//			addToSchema(dc, DublinCoreSchema.TITLE, titles);
			dc.setProperty(DublinCoreSchema.DATE, publishedDate);
			writer.addRdfDescription(dc);
			
			XmpSchema prism = new PrismSchema();
			prism.setProperty(PrismSchema.PUBLICATION_TIME, publishedDate);
			writer.addRdfDescription(prism);
			
			writer.close();
			xmpData = bout.toByteArray();
			System.out.println(new String(xmpData));
		} catch (IOException e) {
			
		}
	}
	
	/**
	 * Adds a list of values as a bag if the list size is greater than 1,
	 * or as a single element if the list size is 1.
	 */
	private static void addToSchema(XmpSchema schema, String key, String[] vals) {
		if (vals.length == 1) {
			schema.setProperty(key, vals[0]);
		} else if (vals.length > 1) {
			XmpArray bag = new XmpArray(XmpArray.UNORDERED);
			for (String val : vals) {
				bag.add(val);
			}
			schema.setProperty(key, bag);
		}
	}
	
	public byte[] getXmpData() {
		return xmpData;
	}

}
