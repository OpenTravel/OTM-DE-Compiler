
package org.opentravel.schemacompiler.task;

import java.net.URL;

/**
 * Interface that defines the options that are specific to the service (WSDL) code generation task.
 * 
 * @author S. Livezey
 */
public interface ServiceCompilerTaskOptions extends CommonCompilerTaskOptions, ExampleCompilerTaskOptions {
	
	/**
	 * Returns the URL of the OTM library that contains the single service to be generated.  If present, only
	 * that service's WSDL will be generated.  If not present, WSDL's will be generated for all services that
	 * exist in the OTM model being processed.
	 * 
	 * @return URL
	 */
	public URL getServiceLibraryUrl();
	
	/**
	 * Returns the base URL for all service endpoints generated in WSDL documents.
	 * 
	 * @return String
	 */
	public String getServiceEndpointUrl();
	
}
