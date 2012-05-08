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
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.xpath.XPathExpressionException;

import org.crossref.pdfmark.prism.Prism21Schema;
import org.crossref.pdfmark.pub.Publisher;
import org.crossref.pdfmark.unixref.Unixref;
import org.crossref.pdfmark.unixref.Work;

import com.itextpdf.text.xml.xmp.DublinCoreSchema;
import com.itextpdf.text.xml.xmp.XmpWriter;

public abstract class MarkBuilder implements MetadataGrabber.Handler {

	private static URI DOI_RESOLVER;
	static {
		try {
			DOI_RESOLVER = new URI("http://dx.doi.org/");
		} catch (URISyntaxException e) {
			/* Not possible. */
		}
	}
	
	private byte[] xmpData;
	
	private Unixref unixref;
	
	private Publisher publisher;
	
	private boolean generateCopyright;
	
	private String rightsAgent;
	
	public MarkBuilder(boolean generateCopyright, String rightsAgent) {
		this.generateCopyright = generateCopyright;
		this.rightsAgent = rightsAgent;
	}
	
	@Override
	public void onMetadata(String requestedDoi, Unixref unixref) {
		this.unixref = unixref;
	}
	
	@Override
	public void onPublisher(String requestedDoi, Publisher pub) {
		this.publisher = pub;
	}
	
	@Override
	public void onComplete(String requestedDoi) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		SchemaSet schemaSet = new SchemaSet();
		
		try {
		    Work work = null;
		    
		    switch (unixref.getType()) {
	        case JOURNAL:
	            work = unixref.getJournal();
	            break;
	        case BOOK:
	            work = unixref.getBook();
	            break;
	        default:
	            break;
	        }
		    
		    if (work != null) {
		        XmpWriter writer = new XmpWriter(bout);
		        
		        work.writeXmp(schemaSet);
		    
    		    if (publisher != null) {
    	            if (generateCopyright) {
    	                String cp = getCopyright(work);
    	                Work.addToSchema(schemaSet.getDc(), DublinCoreSchema.RIGHTS, cp);
    	                Work.addToSchema(schemaSet.getPrism(), Prism21Schema.COPYRIGHT, cp);
    	            }
    	            Work.addToSchema(schemaSet.getDc(), DublinCoreSchema.PUBLISHER, 
    	                             publisher.getName());
    	        }
    		    
    		    Work.addToSchema(schemaSet.getPrism(), Prism21Schema.RIGHTS_AGENT, 
    		                     rightsAgent);
    		    
    		    writer.addRdfDescription(schemaSet.getDc());
                writer.addRdfDescription(schemaSet.getPrism());
                writer.close();
		    }
		    
		    xmpData = bout.toByteArray();
		} catch (IOException e) {
            onFailure(requestedDoi, MetadataGrabber.CLIENT_EXCEPTION_CODE,
                      e.toString());
        } catch (XPathExpressionException e) {
            onFailure(requestedDoi, MetadataGrabber.CLIENT_EXCEPTION_CODE,
                      e.toString());
        }
	}
	
    private String getCopyright(Work work) throws XPathExpressionException {
        return "(C) " + work.getYear() + " " + publisher.getName();
    }
	
	public static String getUrlForDoi(String doi) {
        return DOI_RESOLVER.resolve(doi).toString();
    }
	
	public byte[] getXmpData() {
        return xmpData;
    }

}
