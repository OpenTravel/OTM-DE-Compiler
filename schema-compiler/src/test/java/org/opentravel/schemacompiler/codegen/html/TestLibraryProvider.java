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
package org.opentravel.schemacompiler.codegen.html;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * @author eric.bronson
 *
 */
public class TestLibraryProvider {

	public static boolean DEBUG = false;

	public static final String NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2";

	public static final String LIBRARY_NAME = "library_4_p2";



	private static TLLibrary library = null;

	public static synchronized TLLibrary getLibrary() throws Exception {

		if (library == null) {
			URL url = TestLibraryProvider.class.getResource("/libraries_1_5/test-package_v2/" + LIBRARY_NAME +".xml");
			LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(
					new File(url.getFile()));
			LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();

			ValidationFindings findings = modelLoader
					.loadLibraryModel(libraryInput);
			if (DEBUG) {
				printFindings(findings);
			}

			library = (TLLibrary) modelLoader.getLibraryModel().getLibrary(
					NAMESPACE, LIBRARY_NAME);
		}
		return library;
	}

	public static TLModel getModel() throws Exception {
		return getLibrary().getOwningModel();
	}

	public static TLBusinessObject getBusinessObject(String name) throws Exception {
		TLBusinessObject bo = null;
		for(TLLibrary lib : getModel().getUserDefinedLibraries()){
			bo = lib.getBusinessObjectType(name);
			if(bo != null){
				break;
			}
		}
		return bo;
	}
	
	public static TLCoreObject getCoreObject(String name) throws Exception {
		TLCoreObject co = null;
		for(TLLibrary lib : getModel().getUserDefinedLibraries()){
			co = lib.getCoreObjectType(name);
			if(co != null){
				break;
			}
		}
		return co;
	}

	public static TLAbstractEnumeration getClosedEnum(String name) throws Exception {
		TLClosedEnumeration e = null;
		for(TLLibrary lib : getModel().getUserDefinedLibraries()){
			e = lib.getClosedEnumerationType(name);
			if(e != null){
				break;
			}
		}
		return e;
	}
	
	public static TLValueWithAttributes getVWA(String name) throws Exception {
		TLValueWithAttributes vwa = null;
		for(TLLibrary lib : getModel().getUserDefinedLibraries()){
			vwa = lib.getValueWithAttributesType(name);
			if(vwa != null){
				break;
			}
		}
		return vwa;
	}
	
	public static TLService getService(String name) throws Exception {
		TLService service = null;
		for(TLLibrary lib : getModel().getUserDefinedLibraries()){
			service = lib.getService();
			if((service != null) && service.getName().equals(name)){
				break;
			}
		}
		return service;
	}
	
	/**
	 * Displays the validation findings if one or more findings of the specified
	 * type are present (and debugging is enabled).
	 * 
	 * @param findings
	 *            the validation findings to display
	 * @param findingType
	 *            the finding type to search for
	 */
	public static void printFindings(ValidationFindings findings) {
		if (DEBUG) {
			if (findings.hasFinding()) {
				System.out.println("Validation Findings:");

				for (String message : findings
						.getAllValidationMessages(FindingMessageFormat.DEFAULT)) {
					System.out.println("  " + message);
				}
			}
		}
	}

}
