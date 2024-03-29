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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

/**
 * Resolves XML resources using the local classpath resources.
 * 
 * @author S. Livezey
 */
@SuppressWarnings("unchecked")
public class ClasspathResourceResolver implements LSResourceResolver {

    private static final String SYSTEM_ID_MAPPINGS_CONTEXT_ID = "systemIdMappings";
    private static final Logger log = LogManager.getLogger( ClasspathResourceResolver.class );

    private Map<String,SchemaDeclaration> systemIdMappings;

    /**
     * Default constructor.
     */
    public ClasspathResourceResolver() {
        ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();

        if (appContext.containsBean( SYSTEM_ID_MAPPINGS_CONTEXT_ID )) {
            systemIdMappings = (Map<String,SchemaDeclaration>) appContext.getBean( SYSTEM_ID_MAPPINGS_CONTEXT_ID );
        }

        if (systemIdMappings == null) {
            throw new NullPointerException(
                "System-ID mappings not found in application context for the ClasspathResourceResolver." );
        }
    }

    /**
     * @see org.w3c.dom.ls.LSResourceResolver#resolveResource(java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public LSInput resolveResource(final String type, final String namespaceURI, final String publicID,
        final String systemID, final String baseURI) {
        LSInput input = null;

        if (systemID != null) {
            final InputStream resourceStream = getResourceStream( systemID );
            final Reader resourceReader = new InputStreamReader( resourceStream );

            if (resourceStream != null) {
                input = new LSInput() {

                    @Override
                    public void setSystemId(String systemID) {
                        // No action required
                    }

                    @Override
                    public void setStringData(String stringData) {
                        // No action required
                    }

                    @Override
                    public void setPublicId(String publicID) {
                        // No action required
                    }

                    @Override
                    public void setEncoding(String encoding) {
                        // No action required
                    }

                    @Override
                    public void setCharacterStream(Reader characterStream) {
                        // No action required
                    }

                    @Override
                    public void setCertifiedText(boolean certifiedText) {
                        // No action required
                    }

                    @Override
                    public void setByteStream(InputStream byteStream) {
                        // No action required
                    }

                    @Override
                    public void setBaseURI(String baseURI) {
                        // No action required
                    }

                    @Override
                    public String getSystemId() {
                        return systemID;
                    }

                    @Override
                    public String getStringData() {
                        return null;
                    }

                    @Override
                    public String getPublicId() {
                        return publicID;
                    }

                    @Override
                    public String getEncoding() {
                        return "UTF-8";
                    }

                    @Override
                    public Reader getCharacterStream() {
                        return resourceReader;
                    }

                    @Override
                    public boolean getCertifiedText() {
                        return false;
                    }

                    @Override
                    public InputStream getByteStream() {
                        return resourceStream;
                    }

                    @Override
                    public String getBaseURI() {
                        return baseURI;
                    }
                };
            }
        }
        return input;
    }

    /**
     * Returns an input stream to the resource associated with the given systemID, or null if no such resource was
     * defined in the application context.
     * 
     * @param systemID the systemID for which to return an input stream
     * @return InputStream
     */
    private InputStream getResourceStream(String systemID) {
        InputStream resourceStream = null;
        try {
            SchemaDeclaration schemaDecl = systemIdMappings.get( systemID );
            resourceStream = schemaDecl.getContent( CodeGeneratorFactory.XSD_TARGET_FORMAT );

        } catch (IOException e) {
            // no error - return a null input stream
        }
        if ((resourceStream == null) && log.isWarnEnabled()) {
            log.warn( String.format( "WARNING: No associated schema resource defined for System-ID: %s", systemID ) );
        }
        return resourceStream;
    }

}
