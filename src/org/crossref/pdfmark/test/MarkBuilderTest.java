package org.crossref.pdfmark.test;

import java.io.File;

import junit.framework.TestCase;

import org.crossref.pdfmark.ApiKey;
import org.crossref.pdfmark.MarkBuilder;
import org.crossref.pdfmark.MetadataGrabber;
import org.crossref.pdfmark.XmpUtils;
import org.junit.Before;
import org.junit.Test;

import com.itextpdf.text.pdf.PdfStamper;
import static org.junit.Assert.*;

/**
 * Runs through PDFs, adding metadata for their DOIs. DOIs are located from
 * a text file called "doi.txt", in the PDF info directory.
 */
public class MarkBuilderTest {
	
	private static final String EXTENDED_TEST_DIR = "test-data/extended";
	
	private static final String OUTPUT_DIR = "output/extended";
	
	private MetadataGrabber grabber = new MetadataGrabber(ApiKey.DEFAULT);
	
	@Before
	public void makeOutputDir() {
		File outputDir = new File(OUTPUT_DIR);
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}
	}
	
	@Before
	public void checkForExtendedTestData() {
		File exTestDir = new File(EXTENDED_TEST_DIR);
		assertTrue("Extended test data not available. Grab the git submodule.", 
				   exTestDir.exists());
	}
	
	@Test
	public void addMetadataToPdfs() throws Exception {
		File exTestDir = new File(EXTENDED_TEST_DIR);
		
		for (String pdfDir : exTestDir.list()) {
			if (pdfDir.startsWith(".")) {
				continue; // skip .git and anything else
			}
			
			PdfInfoDirectory pdfInfo = new PdfInfoDirectory(EXTENDED_TEST_DIR 
					+ File.separator + pdfDir, OUTPUT_DIR);
			pdfInfo.create();
			
			String doi = pdfInfo.getDoi();
			
			if (!doi.isEmpty()) {
				PdfStamper stamper = pdfInfo.getStamper();
				byte[] existingXmp = pdfInfo.getExistingXmp();
				
				MarkBuilder builder = new MarkBuilder(true, "") {
					@Override
					public void onFailure(String doi, int code, String msg) {
						System.err.println(doi + " could not be retrieved.");
					}
				};
				
				grabber.grabOne(doi, builder);
				grabber.waitForEmpty();
				
				byte[] merged = XmpUtils.mergeXmp(existingXmp, builder.getXmpData());
				stamper.setXmpMetadata(merged);
			}
			
			pdfInfo.dispose();
		}
		
		grabber.shutDown();
	}

}
