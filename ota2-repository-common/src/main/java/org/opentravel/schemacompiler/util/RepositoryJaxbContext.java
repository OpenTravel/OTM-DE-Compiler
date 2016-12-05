/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.schemacompiler.util;

import javax.xml.bind.JAXBContext;

/**
 * Provides static access to the JAXB context for the XML schemas of the OTM repository.
 * 
 * @author S. Livezey
 */
public class RepositoryJaxbContext {
	
    private static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema:org.opentravel.ns.ota2.repositoryinfo_v01_00";
    private static final String EXT_SCHEMA_CONTEXT = ":org.opentravel.ns.ota2.repositoryinfoext_v01_00";

	private static final JAXBContext jaxbContext;
	private static final JAXBContext extJaxbContext;
	
	/**
	 * Returns the static JAXB context for standard OTM repository messages.
	 * 
	 * @return JAXBContext
	 */
	public static JAXBContext getContext() {
		return jaxbContext;
	}
	
	/**
	 * Returns the static JAXB context for extended OTM repository messages.
	 * 
	 * @return JAXBContext
	 */
	public static JAXBContext getExtContext() {
		return extJaxbContext;
	}
	
    /**
     * Initializes the shared JAXB context.
     */
    static {
        try {
            jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);
            extJaxbContext = JAXBContext.newInstance(EXT_SCHEMA_CONTEXT);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
