/*
 * Copyright 2009 CrossRef.org (email: support@crossref.org)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.crossref.pdfmark;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.crossref.pdfmark.prism.Prism21Schema;
import org.crossref.pdfmark.unixref.Journal;
import org.crossref.pdfmark.unixref.JournalArticle;
import org.crossref.pdfmark.unixref.Unixref;

import com.lowagie.text.xml.xmp.DublinCoreSchema;
import com.lowagie.text.xml.xmp.XmpArray;
import com.lowagie.text.xml.xmp.XmpSchema;
import com.lowagie.text.xml.xmp.XmpWriter;

public abstract class MarkBuilder implements MetadataGrabber.Handler {

	private byte[] xmpData;
	
	@Override
	public void onMetadata(String requestedDoi, Unixref md) {
		try {
			if (md.getType() != Unixref.Type.JOURNAL) {
				onFailure(requestedDoi, MetadataGrabber.CRUMMY_XML_CODE,
						"No journal article metadata for DOI.");
				return;
			}
		} catch (XPathExpressionException e) {
			onFailure(requestedDoi, MetadataGrabber.CRUMMY_XML_CODE,
					"Could not determine if DOI has any journal article metadata.");
			return;
		}
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		try {
			Journal journal = md.getJournal();
			JournalArticle article = journal.getArticle();
			
			XmpWriter writer = new XmpWriter(bout);
			
			XmpSchema dc = new DublinCoreSchema();
			addToSchema(dc, DublinCoreSchema.CREATOR, article.getContributors());
			addToSchema(dc, DublinCoreSchema.TITLE, article.getTitles());
			addToSchema(dc, DublinCoreSchema.DATE, article.getDate());
			addToSchema(dc, DublinCoreSchema.IDENTIFIER, "doi:" + article.getDoi());
			writer.addRdfDescription(dc);
			
			XmpSchema prism = new Prism21Schema();
			addToSchema(prism, Prism21Schema.PUBLICATION_DATE, article.getDate());
			addToSchema(prism, Prism21Schema.DOI, "doi:" + article.getDoi());
			addToSchema(prism, Prism21Schema.ISSN, journal.getPreferredIssn());
			addToSchema(prism, Prism21Schema.E_ISSN, journal.getElectronicIssn());
			addToSchema(prism, Prism21Schema.ISSUE_IDENTIFIER, journal.getDoi());
			addToSchema(prism, Prism21Schema.ISSUE_NAME, journal.getFullTitle());
			addToSchema(prism, Prism21Schema.VOLUME, journal.getVolume());
			addToSchema(prism, Prism21Schema.NUMBER, journal.getIssue());
			writer.addRdfDescription(prism);
			
			writer.close();
			xmpData = bout.toByteArray();
			
		} catch (IOException e) {
			onFailure(requestedDoi, MetadataGrabber.CLIENT_EXCEPTION_CODE,
					  e.toString());
		} catch (XPathExpressionException e) {
			onFailure(requestedDoi, MetadataGrabber.CLIENT_EXCEPTION_CODE,
					  e.toString());
		}
	}
	
	private static void addToSchema(XmpSchema schema, String key, String val) {
		if (val != null && !val.isEmpty()) {
			schema.setProperty(key, val);
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
			XmpArray bag = new XmpArray(XmpArray.ORDERED);
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
