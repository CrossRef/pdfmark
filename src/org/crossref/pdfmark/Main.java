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
				" [{-f, --force-overwrite}]" +
				" [{-p, --xmp-file} xmp_file]" +
				" [{-o, --output-dir} output_dir] " +
				" [{-d, --doi} doi]" + 
				" [{-s, --search-for-doi]" + 
				" pdf_files");
	}

	public static void main(String[] args) {
		new Main(args);
	}
	
	public Main(String[] args) {
		CmdLineParser parser = new CmdLineParser();
		Option provideXmpOp = parser.addStringOption('p', "xmp-file");
		Option overwriteOp = parser.addBooleanOption('f', "force-overwrite");
		Option outputOp = parser.addStringOption('o', "output-dir");
		Option doiOp = parser.addStringOption('d', "doi");
		Option searchOp = parser.addBooleanOption('s', "search-for-doi");
		
		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			printUsage();
			System.exit(2);
		}
		
		String optionalXmpFile = (String) 
				                 parser.getOptionValue(provideXmpOp, "");
		String outputDir       = (String) 
		 			             parser.getOptionValue(outputOp);
		String explicitDoi     = (String) 
		                         parser.getOptionValue(doiOp);
		boolean forceOverwrite = (Boolean) 
		                         parser.getOptionValue(overwriteOp, Boolean.FALSE);
		boolean searchForDoi   = (Boolean) 
		                         parser.getOptionValue(searchOp, Boolean.FALSE);
		
		if (!explicitDoi.equals("") && searchForDoi) {
			exitWithError(2, "-d and -s are mutually exclusive options.");
		}
		
		byte[] optionalXmpData = null;
		
		if (!optionalXmpFile.equals("")) {
			/* We will take XMP data from a file. */
			FileInfo xmpFile = FileInfo.readFileFully(optionalXmpFile);
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
						+ "' already exists.\nTry using -f (force overwrite).");
			}
			
			if (explicitDoi != null) {
				/* Let's make a request for the explicit DOI. */
				grabber.grabOne(explicitDoi, new MetadataGrabber.Handler() {
					@Override
					public void onMetadata(String doi, String[] titles, String[] creators,
							String publishedDate) {
						System.out.println("Got metadata, titles " + titles.length 
								+ " creators " + creators.length);
						// TODO
//						resolvedXmpData = new byte[0];
					}
					
					@Override
					public void onFailure(String doi, int code, String msg) {
						if (code == MetadataGrabber.CRUMMY_XML_CODE) {
							exitWithError(2, "Failed to parse XML metadata because of:\n" 
									+ code + ": " + msg);
						} else {
							System.err.println();
							exitWithError(2, "Failed to retreive metadata because of:\n" 
									+ code + ": " + msg);
						}
					}
				});
				
				System.out.print("Grabbing metadata for '" + explicitDoi + "'");
				waitForGrabber();
			}
			
			try {
				FileInputStream fileIn = new FileInputStream(pdfFile);
				FileOutputStream fileOut = new FileOutputStream(outputFile);
				PdfReader reader = new PdfReader(fileIn);
				PdfStamper stamper = new PdfStamper(reader, fileOut);
				
				byte[] merged = reader.getMetadata();
				
				if (optionalXmpData != null) {
					// TODO Is meta data XMP? Is it empty? What to do if it is 
					// not XMP?
					merged = XmpUtils.mergeXmp(merged, optionalXmpData);
				}
				
				if (resolvedXmpData != null) {
					merged = XmpUtils.mergeXmp(merged, resolvedXmpData);
				}
				
				stamper.setXmpMetadata(merged);
				
				stamper.close();
				reader.close();
			} catch (IOException e) {
				exitWithError(2, "Error: Couldn't handle '" + pdfFilePath 
						+ "' because of:\n" + e);
			} catch (DocumentException e) {
				exitWithError(2, "Error: Couldn't handle '" + pdfFilePath 
						+ "' because of:\n" + e);
			}
		}
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
		System.err.println();
		System.err.println(error);
		System.exit(code);
	}

	
}
