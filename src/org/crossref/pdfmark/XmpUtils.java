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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.lowagie.text.xml.xmp.XmpArray;
import com.lowagie.text.xml.xmp.XmpReader;
import com.lowagie.text.xml.xmp.XmpSchema;
import com.lowagie.text.xml.xmp.XmpWriter;

public class XmpUtils {
	
	private XmpUtils() {
	}
	
	/**
	 * Parse out all the XmpSchema from a blob of XMP data.
	 */
	public static XmpSchema[] parseSchemata(byte[] xmpData) throws XmpException {
		Document doc = null;
		
		try {
			XmpReader reader = new XmpReader(xmpData);
			byte[] xmlData = reader.serializeDoc();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(new ByteArrayInputStream(xmpData));
		} catch (IOException e) {
			throw new XmpException(e);
		} catch (SAXException e) {
			throw new XmpException(e);
		} catch (ParserConfigurationException e) {
			throw new XmpException(e);
		}
		
		NodeList descriptionNodes = doc.getElementsByTagName("rdf:Description");
		XmpSchema[] schemata = new XmpSchema[descriptionNodes.getLength()];
		
		for (int i=0; i<descriptionNodes.getLength(); i++) {
			Element description = (Element) descriptionNodes.item(i);
			NodeList children = description.getChildNodes();
			
			String[] ns = XmlUtils.getNamespaceDeclaration(description);
			schemata[i] = new AnyXmpSchema(ns[0], ns[1]);
			
			for (int j=0; j<children.getLength(); j++) {
				Node n = children.item(j);
				if (n instanceof Element) {
					parseRdfElement(schemata[i], (Element) n);
				}
			}
		}
		
		return schemata;
	}
	
	private static void parseRdfElement(XmpSchema schema, Element ele) {
		String propertyName = ele.getNodeName();
		
		/* Should have either Text or a single <rdf:Bag/Alt/Seq>. */
		Node content = ele.getChildNodes().item(0);
		
		boolean hasElementChildren = false;
		for (int i=0; i<ele.getChildNodes().getLength(); i++) {
		    Node n = ele.getChildNodes().item(i);
		    
		    if (n instanceof Element) {
		        XmpArray ary = parseRdfList((Element) n);
	            schema.setProperty(propertyName, ary);
	            hasElementChildren = true;
		    }
		}
		
		if (!hasElementChildren) {
		    String value = ele.getTextContent();
            schema.setProperty(propertyName, value);
		}
		
		/* And attributes... */
		NamedNodeMap attribs = ele.getAttributes();
		
		for (int i=0; i<attribs.getLength(); i++) {
			Attr attr = (Attr) attribs.item(i);
			if (!attr.getName().startsWith("xmlns")) {
				schema.setProperty(attr.getName(), attr.getTextContent());
			}
		}
	}
	
	private static XmpArray parseRdfList(Element list) {
		XmpArray ary = new XmpArray("rdf:" + list.getLocalName());
		NodeList items = list.getChildNodes();
		for (int i=0; i<items.getLength(); i++) {
			Node n = items.item(i);
			if (n instanceof Element) {
				ary.add(n.getTextContent());
			}
		}
		return ary;
	}
	
	/** 
	 * Combines the RDF description blocks from left and right. Those from
	 * right will overwrite those from left in the case that left and right
	 * contain description blocks with the same namespace.
	 */
	public static byte[] mergeXmp(byte[] left, byte[] right) throws XmpException {
		if (left == null || left.length == 0) return right;
		if (right == null || right.length == 0) return left;
		
		XmpSchema[] leftSchemata = parseSchemata(left);
		XmpSchema[] rightSchemata = parseSchemata(right);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		String[] noWriteList = new String[rightSchemata.length];
		for (int i=0; i<noWriteList.length; i++) {
			noWriteList[i] = rightSchemata[i].getXmlns();
		}
		
		try {
			XmpWriter writer = new XmpWriter(bout);
			for (XmpSchema schema : leftSchemata) {
				boolean found = false;
				for (String checkAgainst : noWriteList) {
					if (schema.getXmlns().equals(checkAgainst)) {
						found = true;
						break;
					}
				}
				if (!found) {
					writer.addRdfDescription(schema);
				}
			}
			for (XmpSchema schema : rightSchemata) {
				writer.addRdfDescription(schema);
			}
			writer.close();
			return bout.toByteArray();
		} catch (IOException e) {
			throw new XmpException(e);
		}
	}
}
