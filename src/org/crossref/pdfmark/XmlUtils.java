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

import java.util.UUID;

import javax.xml.XMLConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public final class XmlUtils {
	
	private XmlUtils() {
	}
	
	/**
	 * @return A String[] of length two, [prefix, URI].
	 */
	public static String[] getNamespaceDeclaration(Element ele) {
		String prefixHint = null;
		String[] parts = ele.getNodeName().split(":");
		if (parts.length == 2) {
			prefixHint = parts[0];
		}
		
		return getNamespaceDeclaration(ele, prefixHint);
	}
	
	/**
	 * @return A String[] of length two, [prefix, URI].
	 */
	public static String[] getNamespaceDeclaration(Element ele, String prefixHint) {
		String[] ns = new String[2]; // prefix, URI
		NamedNodeMap attribs = ele.getAttributes();
		
		for (int i=0; i<attribs.getLength(); i++) {
			Attr attr = (Attr) attribs.item(i);
			if (attr.getName().startsWith("xmlns")) {
				if ((prefixHint != null && attr.getName().endsWith(prefixHint))
						|| attr.getName().equals("xmlns")) {
					ns[0] = attr.getLocalName(); // prefix
					ns[1] = attr.getTextContent(); // URI
				
					// catch default namespace change
					if (ns[0] == "xmlns") {
						ns[0] = UUID.randomUUID().toString();
					}
				}
			}
		}
		
		if (ns[1] == null) {
			return getNamespaceDeclaration((Element) ele.getParentNode(), prefixHint);
		} else {
			return ns;
		}
	}
	
	public static String getNamespaceUriDeclaration(Element ele) {
		NamedNodeMap attribs = ele.getAttributes();
		
		for (int i=0; i<attribs.getLength(); i++) {
			Attr attr = (Attr) attribs.item(i);
			if ("xmlns".equals(attr.getLocalName())
					|| XMLConstants.XML_NS_URI.equals(attr.getNamespaceURI())) {
				return attr.getTextContent();
			}
		}
		
		return "";
	}

}
