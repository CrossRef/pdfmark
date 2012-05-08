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
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.rowset.spi.XmlWriter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.xml.xmp.XmpArray;
import com.itextpdf.text.xml.xmp.XmpReader;
import com.itextpdf.text.xml.xmp.XmpSchema;
import com.itextpdf.text.xml.xmp.XmpWriter;

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
			doc = builder.parse(new ByteArrayInputStream(xmlData));
		} catch (IOException e) {
			throw new XmpException(e);
		} catch (SAXException e) {
			throw new XmpException(e);
		} catch (ParserConfigurationException e) {
			throw new XmpException(e);
		}
		
		NodeList descriptionNodes = doc.getElementsByTagName("rdf:Description");
		Map<String, XmpSchema> schemata = new HashMap<String, XmpSchema>();
		
		for (int i=0; i<descriptionNodes.getLength(); i++) {
			Element description = (Element) descriptionNodes.item(i);
			NodeList children = description.getChildNodes();
			
			for (int j=0; j<children.getLength(); j++) {
				Node n = children.item(j);
				if (n instanceof Element) {
					parseRdfElement(schemata, (Element) n);
				}
			}
		}
		
		return schemata.values().toArray(new XmpSchema[0]);
	}
	
	private static void parseRdfElement(Map<String, XmpSchema> schemata, Element ele) {
		String propertyName = ele.getNodeName();
		String[] ns = XmlUtils.getNamespaceDeclaration(ele);
		XmpSchema schema = null;
		
		if (schemata.containsKey(ns[1])) {
			schema = schemata.get(ns[1]);
		} else {
			schema = new AnyXmpSchema(ns[0], ns[1]);
			schemata.put(ns[1], schema);
		}
		
		/* Should have either Text or a single <rdf:Bag/Alt/Seq>. */
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
		if (left == null || left.length == 0) {
		    ByteArrayOutputStream bout = new ByteArrayOutputStream();
		    try {
    		    XmpWriter writer = new XmpWriter(bout);
    		    for (XmpSchema schema : parseSchemata(right)) {
    		        writer.addRdfDescription(schema);
    		    }
    		    writer.close();
		    } catch (IOException e) {
		        throw new XmpException(e);
		    }
		    return bout.toByteArray();
		}
		
		if (right == null || right.length == 0) {
		    ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try {
                XmpWriter writer = new XmpWriter(bout);
                for (XmpSchema schema : parseSchemata(left)) {
                    writer.addRdfDescription(schema);
                }
                writer.close();
            } catch (IOException e) {
                throw new XmpException(e);
            }
            return bout.toByteArray();
		}
		
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
	
	/**
	 * Copy key value pairs from PDFX namespace into a PDF's document information
	 * dictionary.
	 */
	public static Map<String, String> toInfo(byte[] xmp) throws XmpException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			
			Map<String, String> info = new HashMap<String, String>();
			
			XmpSchema[] schemata = XmpUtils.parseSchemata(xmp);
			for (XmpSchema schema : schemata) {
				if (schema.getXmlns().contains("pdfx")) {
					for (Entry<Object, Object> entry : schema.entrySet()) {
						Object value = entry.getValue();
						
						String key = (String) entry.getKey();
						String[] parts = key.split(":");
						String infoKey = parts.length == 2 ? parts[1] : parts[0];
						
						String val = (String) entry.getValue();
						
						if (val.toLowerCase().contains("<rdf:seq>") 
								|| val.toLowerCase().contains("<rdf:bag>")) {
							val = "<xml xmlns:rdf=\"rdf\">" + val + "</xml>";
							DocumentBuilder builder = factory.newDocumentBuilder();
							Document doc = builder.parse(new ByteArrayInputStream(val.getBytes()));
							
							NodeList nodes = doc.getElementsByTagName("rdf:li");
							for (int i=0; i<nodes.getLength(); i++) {
								Element item = (Element) nodes.item(i);
								info.put(infoKey + "[" + (i + 1) + "]", item.getTextContent());
							}
						} else {
							info.put(infoKey, (String) value);
						}
					}
				}
			}
			
			return info;
		} catch (DOMException e) {
			throw new XmpException(e);
		} catch (IOException e) {
			throw new XmpException(e);
		} catch (SAXException e) {
			throw new XmpException(e);
		} catch (ParserConfigurationException e) {
			throw new XmpException(e);
		}
	}
	
}
