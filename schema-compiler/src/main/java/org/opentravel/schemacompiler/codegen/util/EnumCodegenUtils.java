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
package org.opentravel.schemacompiler.codegen.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtension;

/**
 * Static utility methods used during the generation of code output for open
 * and closed enumerations.
 * 
 * @author S. Livezey
 */
public class EnumCodegenUtils {
	
	/**
	 * Returns the list of declared and inherited enumeration values for the given
	 * entity.
	 * 
	 * @param enumEntity  the enumeration entity for which to return values
	 * @return List<TLEnumValue>
	 */
	public static List<TLEnumValue> getInheritedValues(TLAbstractEnumeration enumEntity) {
		return getInheritedValues( enumEntity, true );
	}
	
	/**
	 * Returns the list of inherited enumeration values for the given entity.  If the
	 * 'includeDeclaredValues' is true, all valid enumeration values will be returned
	 * (declared and inherited).  If false, only those values inherited from parent enum(s)
	 * will be returned.
	 * 
	 * @param enumEntity  the enumeration entity for which to return values
	 * @param includeDeclaredValues  flag indicating whether declared values should be included
	 * @return List<TLEnumValue>
	 */
	public static List<TLEnumValue> getInheritedValues(TLAbstractEnumeration enumEntity, boolean includeDeclaredValues) {
		List<TLEnumValue> valueList = new ArrayList<>();
		
		if (enumEntity != null) {
			List<TLAbstractEnumeration> enumHierarchy = getInheritanceHierarchy( enumEntity );
			
			for (TLAbstractEnumeration hierarchyEnum : enumHierarchy) {
				if (!includeDeclaredValues && (hierarchyEnum == enumEntity)) {
					continue;
				}
				valueList.addAll( hierarchyEnum.getValues() );
			}
		}
		return valueList;
	}
	
	/**
	 * Returns the hierarchy of enumeration extensions starting from the root entity
	 * and ending with the one passed to this method.
	 * 
	 * @param enumEntity  the enumeration entity for which to return the inheritance hierarchy
	 * @return List<TLAbstractEnumeration>
	 */
	private static List<TLAbstractEnumeration> getInheritanceHierarchy(TLAbstractEnumeration enumEntity) {
		List<TLAbstractEnumeration> hierarchy = new ArrayList<>();
		Set<QName> visitedEntityNames = new HashSet<>();
		TLAbstractEnumeration entity = enumEntity;
		
		while (entity != null) {
			QName entityName = new QName( entity.getOwningLibrary().getNamespace(), entity.getLocalName() );
			
			if (visitedEntityNames.contains( entityName )) {
				break; // Break out of the loop in case of circular inheritance
			}
			
			hierarchy.add( 0, entity );
			visitedEntityNames.add( entityName );
			entity = getExtendedEnum( entity );
		}
		return hierarchy;
	}
	
	/**
	 * Returns the enumeration entity that is extended by the given one, or null if the given
	 * entity does not extend another.
	 * 
	 * @param enumEntity  the enumeration entity for which to return the base (extended) enum
	 * @return TLAbstractEnumeration
	 */
	private static TLAbstractEnumeration getExtendedEnum(TLAbstractEnumeration enumEntity) {
		TLExtension extension = enumEntity.getExtension();
		NamedEntity extendedEntity = (extension == null) ? null : extension.getExtendsEntity();
		
		return (extendedEntity instanceof TLAbstractEnumeration) ? (TLAbstractEnumeration) extendedEntity : null;
	}
}
