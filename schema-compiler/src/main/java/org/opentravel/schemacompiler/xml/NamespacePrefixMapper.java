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
package org.opentravel.schemacompiler.xml;

/**
 * Empty extension of the <code>com.sun.xml.bind.marshaller.NamespacePrefixMapper</code>
 * class.  Sonar scans recommend not using the internal <code>com.sun</code> classes, but
 * it seems to be impossible with the current implementation of JAXB.  This class isolates
 * the use into a single location in the schema compiler code allowing the scans for all
 * extending classes to be clean.
 */
@SuppressWarnings( { "squid:S1191", "squid:S2176" } )
public abstract class NamespacePrefixMapper extends com.sun.xml.bind.marshaller.NamespacePrefixMapper {
	
}
