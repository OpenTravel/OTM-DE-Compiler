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
package org.opentravel.schemacompiler.task;

/**
 * Interface that defines the options that are specific to the library schema code generation task.
 * 
 * @author S. Livezey
 */
public interface SchemaCompilerTaskOptions extends CommonCompilerTaskOptions,
        ExampleCompilerTaskOptions {
	
	/**
	 * Returns true if the compiler should suppress all 'x-otm-' extensions in the
	 * generated swagger document(s) and JSON schemas.
	 * 
	 * @return boolean
	 */
	public boolean isSuppressOtmExtensions();
	
}
