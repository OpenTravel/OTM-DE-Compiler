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
package org.opentravel.schemacompiler.providers;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

/**
 * Resolves the JAXB context to the one that includes the required packages for all JAXB classes.
 * 
 * @author S. Livezey
 */
@Provider
public final class JAXBContextResolver implements ContextResolver<JAXBContext> {

    private static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema:org.opentravel.ns.ota2.repositoryinfo_v01_00";

    private static JAXBContext jaxbContext;

    /**
     * @see javax.ws.rs.ext.ContextResolver#getContext(java.lang.Class)
     */
    public JAXBContext getContext(Class<?> type) {
        return jaxbContext;
    }

    /**
     * Initializes the shared JAXB context.
     */
    static {
        try {
            jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}