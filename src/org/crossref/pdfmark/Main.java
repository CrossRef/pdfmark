package org.crossref.pdfmark;
import jargs.gnu.CmdLineParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import static jargs.gnu.CmdLineParser.Option;

public class Main {
	
	private MetadataGrabber grabber = new MetadataGrabber();
	
	public static void printUsage() {
		System.err.println("Usage: pdfmark" +
				" [{-f, --force}]" +
				" [{-p, --xmp-file} xmp_file]" +
				" [{-o, --output-dir} output_dir] " +
				" [{-d, --doi} doi]" + 
				" [{-s, --search-for-doi]" + 
				" pdf_files");
	}

	public static void main(String[] args) {
		new Main(args);
	}
	
	private void shutDown() {
		grabber.shutDown();
	}
	
	public Main(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		Option provideXmpOp = parser.addStringOption('p', "xmp-file");
		Option overwriteOp = parser.addBooleanOption('f', "force");
		Option outputOp = parser.addStringOption('o', "output-dir");
		Option doiOp = parser.addStringOption('d', "doi");
		Option searchOp = parser.addBooleanOption('s', "search-for-doi");
		
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
		boolean forceOverwrite = (Boolean) 
		                         parser.getOptionValue(overwriteOp, Boolean.FALSE);
		boolean searchForDoi   = (Boolean) 
		                         parser.getOptionValue(searchOp, Boolean.FALSE);
		
		if (!explicitDoi.equals("") && searchForDoi) {
			exitWithError(2, "-d and -s are mutually exclusive options.");
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
		
		/* Now we're ready to merge our imported or generated XMP data with what
		 * is already in each PDF. */
		
		for (String pdfFilePath : parser.getRemainingArgs()) {
			String outputPath = pdfFilePath + ".out";
			
			File pdfFile = new File(pdfFilePath);
			File outputFile = new File(pdfFilePath + ".out");
			
			byte[] resolvedXmpData = null;
			
			if (!pdfFile.exists()) {
				exitWithError(2, "Error: File '" + pdfFilePath 
						+ "' does not exist.");
			}
			
			if (outputFile.exists() && !forceOverwrite) {
				exitWithError(2, "Error: File '" + outputPath 
						+ "' already exists.\nTry using -f (force).");
			}
			
			if (!explicitDoi.equals("")) {
				resolvedXmpData = getXmpForDoi(explicitDoi);
			}
			
			try {
				FileInputStream fileIn = new FileInputStream(pdfFile);
				FileOutputStream fileOut = new FileOutputStream(outputFile);
				PdfReader reader = new PdfReader(fileIn);
				PdfStamper stamper = new PdfStamper(reader, fileOut);
				
				byte[] merged = reader.getMetadata();
				
				if (optionalXmpData != null) {
					merged = XmpUtils.mergeXmp(merged, optionalXmpData);
				}
				
				if (resolvedXmpData != null) {
					merged = XmpUtils.mergeXmp(merged, resolvedXmpData);
				}
				
				merged = resolvedXmpData;
				
				stamper.setXmpMetadata(merged);
				
				stamper.close();
				reader.close();
			} catch (IOException e) {
				exitWithError(2, "Error: Couldn't handle '" + pdfFilePath 
						+ "' because of:\n" + e);
			} catch (DocumentException e) {
				exitWithError(2, "Error: Couldn't handle '" + pdfFilePath 
						+ "' because of:\n" + e);
			} catch (XmpException e) {
				exitWithError(2, "Error: Couldn't handle '" + pdfFilePath
						+ "' because of:\n" + e);
			}
		}
		
		shutDown();
	}
	
	private byte[] getXmpForDoi(String doi) {
		MarkBuilder builder = new MarkBuilder() {
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
		
		System.out.print("Grabbing metadata for '" + doi + "'");
		waitForGrabber();
		
		return builder.getXmpData();
	}
	
	private void waitForGrabber() {
		// TODO Move print into separate thread and do networking 
		// in main thread, and remove this junk - it will slow us
		// down for batches.
		while (grabber.isProcessing()) {
			System.out.print(".");
			System.out.flush();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		System.out.println();
	}
	
	private void exitWithError(int code, String error) {
		shutDown();
		System.err.println();
		System.err.println(error);
		System.exit(code);
	}
}
