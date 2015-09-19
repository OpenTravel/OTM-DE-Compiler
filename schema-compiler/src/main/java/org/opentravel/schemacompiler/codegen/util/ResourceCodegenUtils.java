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

import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;

/**
 * Shared static methods used during the code generation for <code>TLResource</code> entities.
 * 
 * @author S. Livezey
 */
public class ResourceCodegenUtils {
	
	/**
	 * Returns the list of all parameter groups declared and inherited by the given
	 * resource.
	 * 
	 * @param resource  the resource for which to return inherited parameter groups
	 * @return List<TLParamGroup>
	 */
	public static List<TLParamGroup> getInheritedParamGroups(TLResource resource) {
		List<TLParamGroup> paramGroups = new ArrayList<>();
		Set<String> paramGroupNames = new HashSet<>();
		TLResource currentResource = resource;
		
		while (currentResource != null) {
			for (TLParamGroup paramGroup : resource.getParamGroups()) {
				if (!paramGroupNames.contains(paramGroup.getName())) {
					paramGroups.add(paramGroup);
					paramGroupNames.add(paramGroup.getName());
				}
			}
			currentResource = getExtendedResource(currentResource);
		}
		return paramGroups;
	}
	
	/**
	 * Returns the resource from which the given one extends.  If the given resource
	 * does not extend another, this method will return null.
	 * 
	 * @param resource  the resource for whict to return an extension
	 * @return TLResource
	 */
	public static TLResource getExtendedResource(TLResource resource) {
		TLExtension extension = (resource == null) ? null : resource.getExtension();
		TLResource extendedResource = null;
		
		if ((extension != null) && (extension.getExtendsEntity() instanceof TLResource)) {
			extendedResource = (TLResource) extension.getExtendsEntity();
		}
		return extendedResource;
	}
	
}
