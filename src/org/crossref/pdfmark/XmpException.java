package org.crossref.pdfmark;

/**
 * A catch-all exception that is often thrown by XMP utility methods.
 */
public class XmpException extends RuntimeException {
	public XmpException(Throwable t) {
		super(t);
	}
}