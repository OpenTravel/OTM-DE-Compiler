
package org.opentravel.schemacompiler.providers;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.security.RepositorySecurityException;

/**
 * Handles the mapping of common exception to HTTP responses.
 * 
 * @author S. Livezey
 */
public abstract class RepositoryServiceExceptionMapper {
	
	private static Log log = LogFactory.getLog(RepositorySecurityExceptionMapper.class);
	
	/**
	 * Converts the given exception into an HTTP response that can be transmitted back to the web
	 * service client.
	 * 
	 * @param t  the exception to convert
	 * @return Response
	 */
	private static Response mapException(Throwable t) {
		Response r;
		
		if (t instanceof RepositorySecurityException) {
			r = Response.status(Response.Status.UNAUTHORIZED).entity(t.getMessage()).build();
			
		} else {
			r = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(t.getMessage()).build();
		}
		log.error("Unexpected exception during service request processing.", t);
		return r;
	}
	
	/**
	 * Handles the JAX-RS mapping of <code>RepositoryException</code> occurrances.
	 */
	@Provider
	public static class RepositoryExceptionMapper extends RepositoryServiceExceptionMapper implements ExceptionMapper<RepositoryException> {

		/**
		 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
		 */
		@Override
		public Response toResponse(RepositoryException exception) {
			return mapException(exception);
		}
		
	}
	
	/**
	 * Handles the JAX-RS mapping of <code>RepositorySecurityException</code> occurrances.
	 */
	@Provider
	public static class RepositorySecurityExceptionMapper extends RepositoryServiceExceptionMapper implements ExceptionMapper<RepositorySecurityException> {

		/**
		 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
		 */
		@Override
		public Response toResponse(RepositorySecurityException exception) {
			return mapException(exception);
		}
		
	}
	
	/**
	 * Handles the JAX-RS mapping of <code>IOException</code> occurrances.
	 */
	@Provider
	public static class IOExceptionMapper extends RepositoryServiceExceptionMapper implements ExceptionMapper<IOException> {

		/**
		 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
		 */
		@Override
		public Response toResponse(IOException exception) {
			return mapException(exception);
		}
		
	}
	
	/**
	 * Handles the JAX-RS mapping of <code>JAXBException</code> occurrances.
	 */
	@Provider
	public static class JAXBExceptionMapper extends RepositoryServiceExceptionMapper implements ExceptionMapper<JAXBException> {

		/**
		 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
		 */
		@Override
		public Response toResponse(JAXBException exception) {
			return mapException(exception);
		}
		
	}
	
}
