package org.crossref.pdfmark.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.crossref.pdfmark.MarkBuilder;
import org.crossref.pdfmark.MetadataGrabber;
import org.crossref.pdfmark.XmpUtils;
import org.junit.Before;
import org.junit.Test;

import com.lowagie.text.pdf.PdfStamper;
import static org.junit.Assert.*;

/**
 * Runs through a PDF, adding metadata for a DOI. The DOI is located from
 * a text file called "doi.txt", in the operating directory (concept from
 * StamperTest).
 */
public class StockPdfTest extends StamperTest {
	
	private String doi;
	
	public StockPdfTest(String operatingPath, String outputPath) throws IOException {
		super(operatingPath, outputPath);
	}
	
	@Before
	protected void locateDoi() throws IOException {
		File doiFile = new File(getOperatingPath() + "doi.txt");
		DataInputStream dIn = new DataInputStream(new FileInputStream(doiFile));
		doi = dIn.readLine();
		dIn.close();
	}
	
	@Test
	protected void addMetadataToPdf() throws Exception {
		PdfStamper stamper = getStamper();
		byte[] existingXmp = getExistingXmp();
		
		MetadataGrabber grabber = new MetadataGrabber();
		MarkBuilder builder = new MarkBuilder() {
			@Override
			public void onFailure(String doi, int code, String msg) {
				fail("Could not get DOI " + doi + ": " + code + " " + msg);
			}
		};
		
		grabber.grabOne(doi, builder);
		grabber.waitForEmpty();
		
		byte[] merged = XmpUtils.mergeXmp(existingXmp, builder.getXmpData());
		stamper.setXmpMetadata(merged);
	}

}
