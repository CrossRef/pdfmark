package org.crossref.pdfmark;

import com.lowagie.text.xml.xmp.XmpSchema;

public class PrismSchema extends XmpSchema {
	
	public static final String DEFAULT_XPATH_ID = "prism";
	public static final String DEFAULT_XPATH_URI 
				= "http://prismstandard.org/namespaces/1.2/basic/";
	
	public static final String CATEGORY = "prism:category";
	public static final String CONTENT_LENGTH = "prism:contentLength";
	public static final String COPYRIGHT = "prism:copyright";
	public static final String CREATION_TIME = "prism:creationTime";
	public static final String DISTRIBUTOR = "prism:distributor";
	public static final String EDITION = "prism:edition";
	public static final String EVENT = "prism:event";
	public static final String EXPIRATION_TIME = "prism:expirationTime";
	public static final String HAS_ALTERNATIVE = "prism:hasAlternative";
	public static final String HAS_CORRECTION = "prism:hasCorrection";
	public static final String HAS_FORMAT = "prism:hasFormat";
	public static final String HAS_PART = "prism:hasPart";
	public static final String HAS_TRANSLATION = "prism:hasTranslation";
	public static final String HAS_VERSION = "prism:hasVersion";
	public static final String INDUSTRY = "prism:industry";
	public static final String IS_ALTERNATIVE_FOR = "prism:isAlternativeFor";
	public static final String BASED_ON = "prism:basedOn";
	public static final String IS_CORRECTION_OF = "prism:isCorrectionOf";
	public static final String IS_FORMAT_OF = "prism:isFormatOf";
	public static final String IS_PART_OF = "prism:isPartOf";
	public static final String IS_REFERENCED_BY = "prism:isReferencedBy";
	public static final String IS_TRANSLATION_OF = "prism:isTranslationOf";
	public static final String IS_REQUIRED_BY = "prism:isRequiredBy";
	public static final String IS_VERSION_OF = "prism:isVersionOf";
	public static final String MODIFICATION_TIME = "prism:modificationTime";
	public static final String NUMBER = "prism:number";
	public static final String OBJECT = "prism:object";
	public static final String ORGANIZATION = "prism:organization";
	public static final String PAGE = "prism:page";
	public static final String PERSON = "prism:person";
	public static final String PUBLICATION_TIME = "prism:publicationTime";
	public static final String RECEPTION_TIME = "prism:receptionTime";
	public static final String REFERENCES = "prism:references";
	public static final String RELEASE_TIME = "prism:releaseTime";
	public static final String REQUIRES = "prism:requires";
	public static final String RIGHTS_AGENT = "prism:rightsAgent";
	public static final String SECTION = "prism:section";
	public static final String VOLUME = "prism:volume";
	
	public PrismSchema() {
		super("xmlns:" 
	               + DEFAULT_XPATH_ID 
	               + "=\"" + DEFAULT_XPATH_URI + "\"");
	}

}
