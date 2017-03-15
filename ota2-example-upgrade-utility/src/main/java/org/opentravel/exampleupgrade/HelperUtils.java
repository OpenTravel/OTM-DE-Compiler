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

package org.opentravel.exampleupgrade;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Static utility methods for the Example Helper application.
 */
public class HelperUtils {
	
	private static VersionScheme otaVersionScheme;
	
	/**
	 * Returns a QName for the given DOM element.
	 * 
	 * @param domElement  the DOM element for which to return a qualified name
	 * @return QName
	 */
	public static QName getElementName(Element domElement) {
		String prefix = domElement.getPrefix();
		
		return new QName( domElement.getNamespaceURI(),
				domElement.getLocalName(), (prefix == null) ? "" : prefix );
	}
	
	/**
	 * Returns the base namespace of the given namespace.
	 * 
	 * @param ns  the namespace for which to return the base
	 * @return String
	 */
	public static String getBaseNamespace(String ns) {
		return otaVersionScheme.getBaseNamespace( ns );
	}
	
	/**
	 * Returns the simple text value of the given DOM element.
	 * 
	 * @param domElement  the DOM element for which to return the text value
	 * @return String
	 */
	public static String getElementTextValue(Element domElement) {
		Node textNode = domElement.getFirstChild();
		String nodeValue = null;
		
		while ((textNode != null) && !(textNode instanceof Text)) {
			textNode = textNode.getNextSibling();
		}
		nodeValue = (textNode == null) ? null : ((Text) textNode).getData();
		
		if ((nodeValue != null) && (nodeValue.trim().length() == 0)) {
			nodeValue = null;
		}
		return nodeValue;
	}
	
	/**
	 * Initializes the OTA2 version scheme.
	 */
	static {
		try {
			VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
			otaVersionScheme = factory.getVersionScheme( factory.getDefaultVersionScheme() );
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}
