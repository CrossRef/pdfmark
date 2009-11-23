import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

	public static void main(String[] args) {
		if (args[0].equals("-i")) {
		
			try {
				// Grab the XMP
				DataInputStream din = new DataInputStream(new FileInputStream(new File(args[1])));
			
				byte[] buff = new byte[1024], xmpData = new byte[0];
				int read = 0;
				while ((read = din.read(buff, 0, buff.length)) > 0) {
					byte[] tmp = new byte[xmpData.length + read];
					System.arraycopy(xmpData, 0, tmp, 0, xmpData.length);
					System.arraycopy(buff, 0, tmp, xmpData.length, read);
					xmpData = tmp;
				}
				
				din.close();
				
				// Insert the XMP into the document as XMP data.
				PdfReader reader = new PdfReader(new FileInputStream(new File(args[2])));
				PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(new File(args[3])));
				
				byte[] existingMeta = reader.getMetadata();
				// TODO Check that existingMeta is not empty and is XMP.
				stamper.setXmpMetadata(mergeXmp(xmpData, existingMeta));
				
				stamper.close();
				reader.close();
			} catch (IOException e ) {
				System.err.println("File IO error: " + e);
			} catch (DocumentException e) {
				System.err.println("Document error: " + e);
			}
			
		} else if (args[0].equals("-x")) {
			
			try {
				// Grab XMP from a reader and dump it.
				PdfReader reader = new PdfReader(new FileInputStream(new File(args[1])));
				byte[] metadata = reader.getMetadata();
				reader.close();
				
				DataOutputStream dout = new DataOutputStream(new FileOutputStream(new File(args[2])));
				dout.write(metadata);
				dout.close();
			} catch (IOException e) {
				System.err.println(e);
			}
			
		}
	}

	private static byte[] mergeXmp(byte[] left, byte[] right) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
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
