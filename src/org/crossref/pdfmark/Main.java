package org.crossref.pdfmark;
import jargs.gnu.CmdLineParser;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class Main {
	
	private MetadataGrabber grabber = new MetadataGrabber();
	
	public static void printUsage() {
		System.err.println("Usage: MarkMunge" +
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
		CmdLineParser.Option provideXmpOp = parser.addStringOption('p', "xmp-file");
		CmdLineParser.Option overwriteOp = parser.addBooleanOption('f', "force-overwrite");
		CmdLineParser.Option outputOp = parser.addStringOption('o', "output-dir");
		CmdLineParser.Option doiOp = parser.addStringOption('d', "doi");
		CmdLineParser.Option searchOp = parser.addBooleanOption('s', "search-for-doi");
		
		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			printUsage();
			System.exit(2);
		}
		
		String optionalXmpFile = (String) parser.getOptionValue(provideXmpOp);
		String outputDir = (String) parser.getOptionValue(outputOp);
		String explicitDoi = (String) parser.getOptionValue(doiOp);
		boolean forceOverwrite = parser.getOptionValue(overwriteOp) == null ? false 
				                   : (Boolean) parser.getOptionValue(overwriteOp);
		boolean searchForDoi = parser.getOptionValue(searchOp) == null ? false
								   : (Boolean) parser.getOptionValue(searchOp);
		
		
		byte[] optionalXmpData = null;
		
		if (!optionalXmpFile.equals("")) {
			/* We will take XMP data from a file. */
			FileInfo xmpFile = FileInfo.readFileFully(optionalXmpFile);
			if (xmpFile.missing) {
				System.err.println("Error: File '" + xmpFile.path + "' does not exist.");
				System.exit(2);
			} else if (xmpFile.error != null) {
				System.err.println("Error: Could not read '" + xmpFile.path + "' because of:");
				System.err.println(xmpFile.error);
				System.exit(2);
			}
			
			optionalXmpData = xmpFile.data;
		}
		
		/* Now we're ready to merge our imported or generated XMP data with what
		 * is already in each PDF. */
		
		for (String pdfFilePath : parser.getRemainingArgs()) {
			String outputPath = pdfFilePath + ".out";
			
			File pdfFile = new File(pdfFilePath);
			File outputFile = new File(pdfFilePath + ".out");
			
			if (!pdfFile.exists()) {
				System.err.println("Error: File '" + pdfFilePath + "' does not exist.");
				continue;
			}
			
			if (outputFile.exists() && !forceOverwrite) {
				System.err.println("Error: File '" + outputPath + "' already exists.");
				System.err.println("Try using -f (force overwrite).");
			}
				
			try {
				FileInputStream fileIn = new FileInputStream(pdfFile);
				FileOutputStream fileOut = new FileOutputStream(outputFile);
				PdfReader reader = new PdfReader(fileIn);
				PdfStamper stamper = new PdfStamper(reader, fileOut);
				
				if (optionalXmpData != null) {
					// TODO Is meta data XMP? Is it empty? What to do if it is not XMP?
					stamper.setXmpMetadata(mergeXmp(reader.getMetadata(), optionalXmpData));
				}
				
				if (explicitDoi != null) {
					/* Let's make a request for the explicit doi. */
					grabber.grabOne(explicitDoi, new MetadataGrabber.Handler() {
						@Override
						public void onMetadata(String doi, String[] titles, String[] creators,
								String publishedDate) {
							System.out.println("Got title = " + titles[0]);
						}
						
						@Override
						public void onFailure(String doi, int code, String msg) {
							
						}
					});
				}
				
				stamper.close();
				reader.close();
			} catch (IOException e) {
				System.err.println("Error: Couldn't handle '" + pdfFilePath + "' because of:");
				System.err.println(e);
			} catch (DocumentException e) {
				System.err.println("Error: Couldn't handle '" + pdfFilePath + "' because of:");
				System.err.println(e);
			}
		}
	}

	private static byte[] mergeXmp(byte[] left, byte[] right) {
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			
			Document leftDoc = builder.parse(new ByteArrayInputStream(left));
			Document rightDoc = builder.parse(new ByteArrayInputStream(right));
			
			Node leftMetaParent = leftDoc.getElementsByTagNameNS("rdf", "RDF").item(0);
			
			NodeList rightMetaNodes = rightDoc.getElementsByTagNameNS("rdf", "RDF").item(0).getChildNodes();
			
			for (int i=0; i<rightMetaNodes.getLength(); i++) {
				Node copy = leftDoc.importNode(rightMetaNodes.item(i), true);
				leftMetaParent.appendChild(copy);
			}
			
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			
			trans.transform(new DOMSource(leftDoc), new StreamResult(bout));
			
			return bout.toByteArray();
		} catch (TransformerException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		} catch (SAXException e) {
			System.err.println(e);
		} catch (ParserConfigurationException e)  {
			System.err.println(e);
		}
		
		return null;
	}
}
