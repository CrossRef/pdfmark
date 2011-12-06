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
package org.crossref.pdfmark.prism;

import com.itextpdf.text.xml.xmp.XmpSchema;

public class Prism21Schema extends XmpSchema {
	
	public static final String DEFAULT_XPATH_ID = "prism";
	public static final String DEFAULT_XPATH_URI 
				= "http://prismstandard.org/namespaces/basic/2.1/";
	
	public static final String AGGREGATION_TYPE = "prism:aggregationType";
	public static final String ALTERNATE_TITLE = "prism:alternateTitle";
	public static final String BYTE_COUNT = "prism:byteCount";
	public static final String CHANNEL = "prism:channel";
	public static final String COMPLIANCE_PROFILE = "prism:complianceProfile";
	public static final String COPYRIGHT = "prism:copyright";
	public static final String CORPORATE_ENTITY = "prism:corporateEntity";
	public static final String COVER_DATE = "prism:coverDate";
	public static final String COVER_DISPLAY_DATE = "prism:coverDisplayDate";
	public static final String CREATION_DATE = "prism:creationDate";
	public static final String DATE_RECEIVED = "prism:dateReceived";
	public static final String DISTRIBUTOR = "prism:distributor";
	public static final String DOI = "prism:doi";
	public static final String EDITION = "prism:edition";
	public static final String E_ISSN = "prism:eIssn";
	public static final String ENDING_PAGE = "prism:endingPage";
	public static final String EVENT = "prism:event";
	public static final String EXPIRATION_DATE = "prism:expirationDate";
	public static final String GENRE = "prism:genre";
	public static final String HAS_ALTERNATIVE = "prism:hasAlternative";
	public static final String HAS_CORRECTION = "prism:hasCorrection";
	public static final String HAS_PREVIOUS_VERSION = "prism:hasPreviousVersion";
	public static final String HAS_TRANSLATION = "prism:hasTranslation";
	public static final String INDUSTRY = "prism:industry";
	public static final String ISBN = "prism:isbn";
	public static final String IS_CORRECTION_OF = "prism:isCorrectionOf";
	public static final String ISSN = "prism:issn";
	public static final String ISSUE_IDENTIFIER = "prism:issueIdentifier";
	public static final String ISSUE_NAME = "prism:issueName";
	public static final String IS_TRANSLATION_OF = "prism:isTranslationOf";
	public static final String KEYWORD = "prism:keyword";
	public static final String KILL_DATE = "prism:killDate";
	public static final String LOCATION = "prism:location";
	public static final String METADATA_CONTAINER = "prism:metadataContainer";
	public static final String MODIFICATION_DATE = "prism:modificationDate";
	public static final String NUMBER = "prism:number";
	public static final String OBJECT = "prism:object";
	public static final String ORGANIZATION = "prism:organization";
	public static final String ORIGIN_PLATFORM = "prism:originPlatform";
	public static final String PAGE_RANGE = "prism:pageRange";
	public static final String PERSON = "prism:person";
	public static final String PUBLICATION_DATE = "prism:publicationDate";
	public static final String PUBLICATION_NAME = "prism:publicationName";
	public static final String RIGHTS_AGENT = "prism:rightsAgent";
	public static final String SECTION = "prism:section";
	public static final String STARTING_PAGE = "prism:startingPage";
	public static final String SUBCHANNEL1 = "prism:subchannel1";
	public static final String SUBCHANNEL2 = "prism:subchanell2";
	public static final String SUBCHANNEL3 = "prism:subchanell3";
	public static final String SUBCHANNEL4 = "prism:subchanell4";
	public static final String SUBSECTION1 = "prism:subsection1";
	public static final String SUBSECTION2 = "prism:subsection2";
	public static final String SUBSECTION3 = "prism:subsection3";
	public static final String SUBSECTION4 = "prism:subsection4";
	public static final String TEASER = "prism:teaser";
	public static final String TICKER = "prism:ticker";
	public static final String TIME_PERIOD = "prism:timePeriod";
	public static final String URL = "prism:url";
	public static final String VERSION_IDENTIFIER = "prism:versionIdentifier";
	public static final String VOLUME = "prism:volume";
	public static final String WORD_COUNT = "prism:wordCount";
	
	public Prism21Schema() {
		super("xmlns:" 
	               + DEFAULT_XPATH_ID 
	               + "=\"" + DEFAULT_XPATH_URI + "\"");
	}

}
