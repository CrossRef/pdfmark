package org.crossref.pdfmark;

import javax.xml.XMLConstants;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class XmlUtils {
	
	private XmlUtils() {
	}
	
	/**
	 * @return A String[] of length two, [prefix, URI].
	 */
	public static String[] getNamespaceDeclaration(Element ele) {
		String[] ns = new String[2]; // prefix, URI
		
		NamedNodeMap attribs = ele.getAttributes();
		
		for (int i=0; i<attribs.getLength(); i++) {
			Attr attr = (Attr) attribs.item(i);
			if (attr.getName().startsWith("xmlns")) {
				ns[0] = attr.getLocalName(); // prefix
				ns[1] = attr.getTextContent(); // URI
			}
		}
		
		return ns;
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
