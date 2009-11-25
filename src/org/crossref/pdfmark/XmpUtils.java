package org.crossref.pdfmark;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

public class XmpUtils {
	
	private static XmpUtils utils = new XmpUtils();
	
	private XmpUtils() {
		
	}
	
	/**
	 * Merge the <rdf:RDF> section of two XML documents. All <rdf:description>
	 * elements from left and right are maintained in a new XML document.
	 */
	public static byte[] mergeXmp(byte[] left, byte[] right) 
			throws XmpException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			Document leftDoc = builder.parse(new ByteArrayInputStream(left));
			Document rightDoc = builder.parse(new ByteArrayInputStream(right));
			Node rdfNode = leftDoc.getElementsByTagNameNS("rdf", "RDF")
			                      .item(0);
			NodeList descNodes = rightDoc.getElementsByTagNameNS("rdf", "RDF")
			 				             .item(0)
									     .getChildNodes();
			
			for (int i=0; i<descNodes.getLength(); i++) {
				Node copy = leftDoc.importNode(descNodes.item(i), true);
				rdfNode.appendChild(copy);
			}
			
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			
			trans.transform(new DOMSource(leftDoc), new StreamResult(bout));
			return bout.toByteArray();
			
		} catch (TransformerException e) {
			throw utils.new XmpException(e);
		} catch (IOException e) {
			throw utils.new XmpException(e);
		} catch (SAXException e) {
			throw utils.new XmpException(e);
		} catch (ParserConfigurationException e) {
			throw utils.new XmpException(e);
		}
	}
	
	/**
	 * A catch-all exception that is often thrown by XMP utility methods.
	 */
	public class XmpException extends RuntimeException {
		public XmpException(Throwable t) {
			super(t);
		}
	}
}
