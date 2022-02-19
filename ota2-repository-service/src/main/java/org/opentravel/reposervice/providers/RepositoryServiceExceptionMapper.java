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

package org.opentravel.reposervice.providers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositorySecurityException;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;

/**
 * Handles the mapping of common exception to HTTP responses.
 * 
 * @author S. Livezey
 */
public abstract class RepositoryServiceExceptionMapper {

    private static Logger log = LogManager.getLogger( RepositoryServiceExceptionMapper.class );

    /**
     * Private constructor to prevent instantiation.
     */
    private RepositoryServiceExceptionMapper() {}

    /**
     * Converts the given exception into an HTTP response that can be transmitted back to the web service client.
     * 
     * @param t the exception to convert
     * @return Response
     */
    private static Response mapException(Throwable t) {
        Response r;

        if (t instanceof RepositorySecurityException) {
            r = Response.status( Response.Status.UNAUTHORIZED ).entity( t.getMessage() ).build();

        } else {
            r = Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( t.getMessage() ).build();
        }
        log.error( "Unexpected exception during service request processing.", t );
        return r;
    }

    /**
     * Handles the JAX-RS mapping of <code>RepositoryException</code> occurrances.
     */
    @Provider
    public static class RepositoryExceptionMapper extends RepositoryServiceExceptionMapper
        implements ExceptionMapper<RepositoryException> {

        /**
         * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
         */
        @Override
        public Response toResponse(RepositoryException exception) {
            return mapException( exception );
        }

    }

    /**
     * Handles the JAX-RS mapping of <code>RepositorySecurityException</code> occurrances.
     */
    @Provider
    public static class RepositorySecurityExceptionMapper extends RepositoryServiceExceptionMapper
        implements ExceptionMapper<RepositorySecurityException> {

        /**
         * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
         */
        @Override
        public Response toResponse(RepositorySecurityException exception) {
            return mapException( exception );
        }

    }

    /**
     * Handles the JAX-RS mapping of <code>IOException</code> occurrances.
     */
    @Provider
    public static class IOExceptionMapper extends RepositoryServiceExceptionMapper
        implements ExceptionMapper<IOException> {

        /**
         * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
         */
        @Override
        public Response toResponse(IOException exception) {
            return mapException( exception );
        }

    }

    /**
     * Handles the JAX-RS mapping of <code>JAXBException</code> occurrances.
     */
    @Provider
    public static class JAXBExceptionMapper extends RepositoryServiceExceptionMapper
        implements ExceptionMapper<JAXBException> {

        /**
         * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
         */
        @Override
        public Response toResponse(JAXBException exception) {
            return mapException( exception );
        }

    }

}
