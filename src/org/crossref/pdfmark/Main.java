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
import jargs.gnu.CmdLineParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.xml.xmp.XmpSchema;

import static jargs.gnu.CmdLineParser.Option;

public class Main {
	
	private MetadataGrabber grabber;
	
	public static void printUsage() {
		System.err.println("Usage: pdfmark" +
				" [{-f, --force}]" +
				" [{-p, --xmp-file} xmp_file]" +
				" [{-o, --output-dir} output_dir] " +
				" [{-d, --doi} doi]" + 
				" [--no-copyright]" + 
				" [--rights-agent rights_agent_str]" +
				" [--api-key search_key]" + 
				" pdf_files");
	}
	
	public static void printFutureUsage() {
		/* This will be correct once all features are implemented. */
		System.err.println("Usage: pdfmark" +
				" [{-f, --force}]" +
				" [{-p, --xmp-file} xmp_file]" +
				" [{-o, --output-dir} output_dir] " +
				" [{-d, --doi} doi]" + 
				" [{-s, --search-for-doi]" + 
				" [--no-copyright]" + 
				" [--rights-agent rights_agent_str]" +
				" [--api-key search_key]" + 
				" pdf_files");
	}

	public static void main(String[] args) {
		new Main(args);
	}
	
	private void shutDown() {
		grabber.shutDown();
	}
	
	public Main(String[] args) {
		if (args.length == 0) {
			printUsage();
			System.exit(2);
		}
		
		CmdLineParser parser = new CmdLineParser();
		Option provideXmpOp = parser.addStringOption('p', "xmp-file");
		Option overwriteOp = parser.addBooleanOption('f', "force");
		Option outputOp = parser.addStringOption('o', "output-dir");
		Option doiOp = parser.addStringOption('d', "doi");
		Option searchOp = parser.addBooleanOption('s', "search-for-doi");
		Option copyrightOp = parser.addBooleanOption("no-copyright");
		Option rightsOp = parser.addStringOption("rights-agent");
		Option apiKeyOp = parser.addStringOption("api-key");
		
		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			printUsage();
			System.exit(2);
		}
		
		String optionalXmpPath = (String) 
				                 parser.getOptionValue(provideXmpOp, "");
		String outputDir       = (String) 
		 			             parser.getOptionValue(outputOp, "");
		String explicitDoi     = (String) 
		                         parser.getOptionValue(doiOp, "");
		boolean useTheForce    = (Boolean) 
		                         parser.getOptionValue(overwriteOp, Boolean.FALSE);
		boolean searchForDoi   = (Boolean) 
		                         parser.getOptionValue(searchOp, Boolean.FALSE);
		boolean noCopyright    = (Boolean)
								 parser.getOptionValue(copyrightOp, Boolean.FALSE);
		String rightsAgent     = (String)
		 						 parser.getOptionValue(rightsOp, "");
		String apiKey          = (String)
		 						 parser.getOptionValue(apiKeyOp, ApiKey.DEFAULT);
		
		if (!explicitDoi.equals("") && searchForDoi) {
			exitWithError(2, "-d and -s are mutually exclusive options.");
		}
		
		if (!outputDir.isEmpty() && !new File(outputDir).exists()) {
			exitWithError(2, "The output directory, '" + outputDir 
					+ "' does not exist.");
		}
		
		byte[] optionalXmpData = null;
		
		if (!optionalXmpPath.equals("")) {
			/* We will take XMP data from a file. */
			FileInfo xmpFile = FileInfo.readFileFully(optionalXmpPath);
			if (xmpFile.missing) {
				exitWithError(2, "Error: File '" + xmpFile.path 
						+ "' does not exist.");
			} else if (xmpFile.error != null) {
				exitWithError(2, "Error: Could not read '" + xmpFile.path 
						+ "' because of:\n" + xmpFile.error);
			}
			
			optionalXmpData = xmpFile.data;
		}
		
		grabber = new MetadataGrabber(apiKey);
		
		/* Now we're ready to merge our imported or generated XMP data with what
		 * is already in each PDF. */
		
		for (String pdfFilePath : parser.getRemainingArgs()) {
			String outputPath = getOutFileName(pdfFilePath);
			
			/* Grab the leaf. */
			if (outputPath.contains(File.separator)) {
				String[] split = outputPath.split(File.separator);
				outputPath = split[split.length - 1];
			}
			
			if (!outputDir.isEmpty()) {
				outputPath = outputDir + File.separator + outputPath;
			} else {
				/* Output to the working directory. */
			}
			
			File pdfFile = new File(pdfFilePath);
			File outputFile = new File(outputPath);
			
			byte[] resolvedXmpData = null;
			
			if (!pdfFile.exists()) {
				exitWithError(2, "Error: File '" + pdfFilePath 
						+ "' does not exist.");
			}
			
			if (outputFile.exists() && !useTheForce) {
				exitWithError(2, "Error: File '" + outputPath 
						+ "' already exists.\nTry using -f (force).");
			}
			
			try {
				if (!useTheForce && isLinearizedPdf(new FileInputStream(pdfFile))) {
					exitWithError(2, "Error: '" + pdfFilePath + "' is a"
							+ " linearized PDF and force is not specified."
							+ " This tool will output non-linearized PDF."
							+ "\nIf you don't mind that, use -f (force).");
				}
			} catch (IOException e) {
				exitWithError(2, "Error: Could not determine linearization"
						+ " because of:\n" + e);
			}
			
			if (!explicitDoi.equals("")) {
				resolvedXmpData = getXmpForDoi(explicitDoi, 
						                       !noCopyright, 
						                       rightsAgent);
			}
			
			try {
				new File(outputFile.getPath() + ".tmp").deleteOnExit();
				
				FileInputStream fileIn = new FileInputStream(pdfFile);
				FileOutputStream fileOut = new FileOutputStream(outputFile.getPath() + ".tmp");
				PdfReader reader = new PdfReader(fileIn);
				PdfStamper stamper = new PdfStamper(reader, fileOut);
				
				byte[] merged = reader.getMetadata();
				
				if (optionalXmpData != null) {
					merged = XmpUtils.mergeXmp(merged, optionalXmpData);
				}
				
				if (resolvedXmpData != null) {
					merged = XmpUtils.mergeXmp(merged, resolvedXmpData);
				}

				stamper.setXmpMetadata(merged);
				
				stamper.close();
				reader.close();
				
				fileIn = new FileInputStream(outputFile.getPath() + ".tmp");
				writeInfoDictionary(fileIn, outputFile.getPath(), merged);
			} catch (IOException e) {
				exitWithError(2, "Error: Couldn't handle '" + pdfFilePath 
						+ "' because of:\n" + e);
			} catch (DocumentException e) {
				exitWithError(2, "Error: Couldn't handle '" + pdfFilePath 
						+ "' because of:\n" + e);
			} catch (XmpException e) {
				exitWithError(2, "Error: Couldn't handle '" + pdfFilePath
						+ "' because of:\n" + e);
			} catch (COSVisitorException e) {
				exitWithError(2, "Error: Couldn't write document info dictionary"
						+ " because of:\n" + e);
			}
		}
		
		shutDown();
	}
	
	public static void writeInfoDictionary(FileInputStream in, 
			String outputFile, byte[] xmp) throws IOException, COSVisitorException {
	
		PDFParser parser = new PDFParser(in);
		parser.parse();
	
		PDDocument document = parser.getPDDocument();
		PDDocumentInformation info = document.getDocumentInformation();
		
		for (Entry<String, String> entry : XmpUtils.toInfo(xmp).entrySet()) {
			info.setCustomMetadataValue(entry.getKey(), entry.getValue());
		}
		
		document.setDocumentInformation(info);
		document.save(outputFile);
		document.close();
	}
	
	/**
	 * According to the PDF Reference Manual (appendix F) a linearized PDF
	 * must have as its first object after the PDF header an indirect
	 * dictionary containing only direct objects. Among these objects one
	 * must be assigned the key "Linearized", representing the linearized PDF
	 * version number.
	 * 
	 * @return true if the PDF read by reader is a linearized PDF.
	 */
	public static boolean isLinearizedPdf(FileInputStream in) throws IOException {
		boolean isLinear = false;
		
		PDFParser parser = new PDFParser(in);
		parser.parse();
		COSDocument doc = parser.getDocument();
		
		for (Object o : doc.getObjects()) {
			COSObject obj = (COSObject) o;
			if (obj.getObject() instanceof COSDictionary) {
				COSDictionary dict = (COSDictionary) obj.getObject();
				for (Object key : dict.keyList()) {
					COSName name = (COSName) key;
					if ("Linearized".equals(name.getName())) {
						isLinear = true;
						break;
					}
				}
				
				if (isLinear) break;
			}
		}
		
		doc.close();
		
		return isLinear;
	}
	
	private byte[] getXmpForDoi(String doi, boolean genCr, String agent) {
		MarkBuilder builder = new MarkBuilder(genCr, agent) {
			@Override
			public void onFailure(String doi, int code, String msg) {
				if (code == MetadataGrabber.CRUMMY_XML_CODE) {
					exitWithError(2, "Failed to parse metadata XML because of:\n" 
							+ code + ": " + msg);
				} else {
					System.err.println();
					exitWithError(2, "Failed to retreive metadata because of:\n" 
							+ code + ": " + msg);
				}
			}
		};
		grabber.grabOne(doi, builder);
		System.out.println("Grabbing metadata for '" + doi + "'...");
		grabber.waitForEmpty();
		
		return builder.getXmpData();
	}
	
	private static String getOutFileName(String pdfFileName) {
		if (pdfFileName.endsWith(".pdf")) {
			return pdfFileName.substring(0, pdfFileName.length() - 4)
					+ "_xmp.pdf";
		} else {
			return pdfFileName + "_xmp.pdf";
		}
	}
	
	private void exitWithError(int code, String error) {
		shutDown();
		System.err.println();
		System.err.println(error);
		System.exit(code);
	}
}
