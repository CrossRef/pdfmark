package org.crossref.pdfmark.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class StamperTest {
	
	private String operatingPath;
	
	private String outputPath;
	
	private PdfStamper stamper;
	
	private byte[] existingXmp;
	
	public StamperTest(String newOperatingPath, String newOutputPath) throws IOException {
		operatingPath = newOperatingPath;
		outputPath = newOutputPath;
		
		if (!new File(operatingPath).exists()) {
			throw new IOException("Path " + newOperatingPath + " does not exist.");
		}
	}
	
	protected String getOperatingPath() {
		return operatingPath;
	}
	
	protected PdfStamper getStamper() {
		return stamper;
	}
	
	protected byte[] getExistingXmp() {
		return existingXmp;
	}
	
	@Before
	protected void createStamper() throws IOException, DocumentException {
		/* Find a PDF file in the operating directory. */
		String pdfFileName = null;
		File operatingDir = new File(operatingPath);
		for (String filename : operatingDir.list()) {
			if (filename.endsWith(".pdf")) {
				pdfFileName = filename;
				break;
			}
		}
		
		String pdfPath = operatingPath + File.separator + pdfFileName;
		String pdfOutPath = outputPath + File.separator + pdfFileName;
		
		/* Read the PDF into a PDF document object. */
		PdfReader reader = new PdfReader(new FileInputStream(pdfPath));
		
		/* Grab a copy of the existing XMP. */
		existingXmp = reader.getMetadata();
		
		/* Open a stamper based on the document above. */
		stamper = new PdfStamper(reader, new FileOutputStream(pdfOutPath));
	}
	
	@After
	protected void disposeStamper() throws IOException, DocumentException {
		/* Close the stamper. */
		stamper.close();
	}

}
