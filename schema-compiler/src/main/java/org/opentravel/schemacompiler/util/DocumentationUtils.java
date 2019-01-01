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

import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Static utility methods that assist with the resolution of <code>TLDocumentation</code>
 * elements and documentation patches within the OTM model.
 */
public class DocumentationUtils {
	
	/**
	 * Private constructor to prevent instantiation.
	 */
	private DocumentationUtils() {}
	
	/**
	 * Returns the path for the given documentation owner within its owning library.
	 * 
	 * @param owner  the documentation owner for which to return a path
	 * @return String
	 * @deprecated  Use {@link DocumentationPathBuilder#buildPath(TLDocumentationOwner)} instead
	 */
	@Deprecated
	public static String getDocumentationPath(TLDocumentationOwner owner) {
		return DocumentationPathBuilder.buildPath( owner );
	}
	
	/**
	 * Returns the documentation owner at the specified path within the given OTM
	 * library.
	 * 
	 * @param docPath  the path of the documentation owner to return
	 * @param library  the OTM library from which to retrieve the documentation owner
	 * @return TLDocumentationOwner
	 * @deprecated  Use {@link DocumentationPathResolver#resolve(String, TLLibrary)} instead
	 */
	@Deprecated
	public static TLDocumentationOwner getDocumentationOwner(String docPath, TLLibrary library) {
		return DocumentationPathResolver.resolve( docPath, library );
	}
	
}
