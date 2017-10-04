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

package org.opentravel.release;

import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.RepositoryItemCommit;

import javafx.scene.image.Image;

/**
 * Static utility methods for the Example Helper application.
 */
public class Utils {
	
	public static final Image rootIcon = new Image( Utils.class.getResourceAsStream( "/images/root.gif" ) );
	public static final Image repositoryIcon = new Image( Utils.class.getResourceAsStream( "/images/repository.gif" ) );
	public static final Image rootNSIcon = new Image( Utils.class.getResourceAsStream( "/images/rootNS.gif" ) );
	public static final Image baseNSIcon = new Image( Utils.class.getResourceAsStream( "/images/baseNS.gif" ) );
	public static final Image releaseIcon = new Image( Utils.class.getResourceAsStream( "/images/release.gif" ) );
	public static final Image libraryIcon = new Image( Utils.class.getResourceAsStream( "/images/library.png" ) );
	public static final Image folderIcon = new Image( Utils.class.getResourceAsStream( "/images/folder.gif" ) );
	public static final Image businessObjectIcon = new Image( Utils.class.getResourceAsStream( "/images/business-object.png" ) );
	public static final Image choiceObjectIcon = new Image( Utils.class.getResourceAsStream( "/images/choice-object.gif" ) );
	public static final Image coreObjectIcon = new Image( Utils.class.getResourceAsStream( "/images/core-object.gif" ) );
	public static final Image roleValueIcon = new Image( Utils.class.getResourceAsStream( "/images/role-value.jpg" ) );
	public static final Image vwaIcon = new Image( Utils.class.getResourceAsStream( "/images/vwa.gif" ) );
	public static final Image enumerationIcon = new Image( Utils.class.getResourceAsStream( "/images/enumeration.gif" ) );
	public static final Image simpleTypeIcon = new Image( Utils.class.getResourceAsStream( "/images/simple-type.gif" ) );
	public static final Image facetIcon = new Image( Utils.class.getResourceAsStream( "/images/facet.gif" ) );
	public static final Image contextualFacetIcon = new Image( Utils.class.getResourceAsStream( "/images/facet-contextual.gif" ) );
	public static final Image actionFacetIcon = new Image( Utils.class.getResourceAsStream( "/images/facet-action.gif" ) );
	public static final Image extensionPointFacetIcon = new Image( Utils.class.getResourceAsStream( "/images/facet-extension-point.gif" ) );
	public static final Image aliasIcon = new Image( Utils.class.getResourceAsStream( "/images/alias.gif" ) );
	public static final Image attributeIcon = new Image( Utils.class.getResourceAsStream( "/images/attribute.gif" ) );
	public static final Image elementIcon = new Image( Utils.class.getResourceAsStream( "/images/element.gif" ) );
	public static final Image indicatorIcon = new Image( Utils.class.getResourceAsStream( "/images/indicator.gif" ) );
	public static final Image resourceIcon = new Image( Utils.class.getResourceAsStream( "/images/resource.gif" ) );
	public static final Image parentRefIcon = new Image( Utils.class.getResourceAsStream( "/images/parent-ref.png" ) );
	public static final Image paramGroupIcon = new Image( Utils.class.getResourceAsStream( "/images/param-group.gif" ) );
	public static final Image parameterIcon = new Image( Utils.class.getResourceAsStream( "/images/parameter.gif" ) );
	public static final Image actionIcon = new Image( Utils.class.getResourceAsStream( "/images/action.gif" ) );
	public static final Image requestIcon = new Image( Utils.class.getResourceAsStream( "/images/request.gif" ) );
	public static final Image responseIcon = new Image( Utils.class.getResourceAsStream( "/images/response.gif" ) );
	public static final Image serviceIcon = new Image( Utils.class.getResourceAsStream( "/images/service.gif" ) );
	public static final Image infoIcon = new Image( Utils.class.getResourceAsStream( "/images/info.gif" ) );
	public static final Image warningIcon = new Image( Utils.class.getResourceAsStream( "/images/warning.gif" ) );
	public static final Image errorIcon = new Image( Utils.class.getResourceAsStream( "/images/error.gif" ) );
	
	/**
	 * Returns a display name label for the given OTM entity.
	 * 
	 * @param entity  the entity for which to return a display name
	 * @param showPrefix  flag indicating whether the owning library's prefix should be included in the label
	 * @return String
	 */
	public static String getDisplayName(NamedEntity entity, boolean showPrefix) {
		TLLibrary library = (TLLibrary) entity.getOwningLibrary();
		QName elementName = XsdCodegenUtils.getGlobalElementName(entity);
		String localName = (elementName != null) ? elementName.getLocalPart() : entity.getLocalName();
		StringBuilder displayName = new StringBuilder();
		
		if (showPrefix && (library.getPrefix() != null)) {
			displayName.append( library.getPrefix() ).append( ":" );
		}
		displayName.append( localName );
		
		return displayName.toString();
	}
	
	/**
	 * Returns true if the given test date falls after the latest date in the
	 * commit history provided.
	 * 
	 * @param testDate  the date to test against all other commit dates
	 * @param commitHistory  the commit history to test against
	 * @return boolean
	 */
	public static boolean isAfterLatestCommit(Date testDate, List<RepositoryItemCommit> commitHistory) {
		Date latestCommit = getLatestCommitDate( commitHistory );
		
		return (latestCommit == null) || testDate.after( latestCommit );
	}
	
	/**
	 * Returns the latest commit date from the commit history provided.
	 * 
	 * @param commitHistory  the commit history from which to obtain the latest date
	 * @return Date
	 */
	public static Date getLatestCommitDate(List<RepositoryItemCommit> commitHistory) {
		Date latestDate = null;
		
		for (RepositoryItemCommit commit : commitHistory) {
			if ((latestDate == null) || commit.getEffectiveOn().after( latestDate )) {
				latestDate = commit.getEffectiveOn();
			}
		}
		return latestDate;
	}
	
}
