package org.crossref.pdfmark.test;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class PdfInfoDirectory {
	
private String operatingPath;
	
	private String outputPath;
	
	private PdfStamper stamper;
	
	private byte[] existingXmp;
	
	public PdfInfoDirectory(String newOperatingPath, String newOutputPath) 
			throws IOException {
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
	
	protected String getDoi() throws IOException {
		File doiFile = new File(getOperatingPath() + File.separator + "doi.txt");
		String doi = "";
		if (doiFile.exists()) {
			DataInputStream dIn = new DataInputStream(new FileInputStream(doiFile));
			doi = dIn.readLine();
			dIn.close();
		}
		return doi;
	}
	
	protected void create() throws IOException, DocumentException {
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
	
	protected void dispose() throws IOException, DocumentException {
		/* Close the stamper. */
		stamper.close();
	}

}
