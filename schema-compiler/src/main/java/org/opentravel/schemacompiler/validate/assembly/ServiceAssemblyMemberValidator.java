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

package org.opentravel.schemacompiler.validate.assembly;

import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.ServiceAssemblyMember;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * Validator for the <code>ServiceAssembly</code> class.
 */
public class ServiceAssemblyMemberValidator extends AssemblyValidatorBase<ServiceAssemblyMember> {

    public static final String ERROR_CONFLICTING_COMMIT_LEVEL = "CONFLICTING_COMMIT_LEVEL";
    public static final String ERROR_INVALID_RESOURCE_NAMESPACE = "INVALID_RESOURCE_NAMESPACE";

    /**
     * @see org.opentravel.schemacompiler.validate.Validator#validate(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    public ValidationFindings validate(ServiceAssemblyMember target) {
        AssemblyValidationBuilder builder = newValidationBuilder( target );
        AssemblyValidationContext context = getValidationContext();
        List<RepositoryItem> libraryItems = context.getLibraryItems( target );
        List<RepositoryItem> conflictingItems = new ArrayList<>();

        // Look for conflicting library commit levels across the various
        // releases in the assembly
        for (RepositoryItem libraryItem : libraryItems) {
            if (context.hasMultipleCommitLevels( libraryItem )) {
                conflictingItems.add( libraryItem );
            }
        }
        if (!conflictingItems.isEmpty()) {
            StringBuilder libraryStr = new StringBuilder();
            boolean firstItem = true;

            for (RepositoryItem item : conflictingItems) {
                if (!firstItem) {
                    libraryStr.append( ", " );
                }
                libraryStr.append( item.getFilename() );
                firstItem = false;
            }
            builder.addFinding( FindingType.ERROR, "assemblyItems", ERROR_CONFLICTING_COMMIT_LEVEL,
                libraryStr.toString() );
        }

        // If the resource name is non-null, validate the namespace and local name
        if (target.getResourceName() != null) {
            QName resourceName = target.getResourceName();
            String ns = resourceName.getNamespaceURI();

            builder.setProperty( "resourceName.namespace", ns ).setFindingType( FindingType.ERROR )
                .assertNotNullOrBlank().assertContainsNoWhitespace();

            if ((ns != null) && !versionScheme.isValidNamespace( ns )) {
                builder.addFinding( FindingType.ERROR, "resourceName.namespace", ERROR_INVALID_RESOURCE_NAMESPACE, ns );
            }

            builder.setProperty( "resourceName.localPart", resourceName.getLocalPart() )
                .setFindingType( FindingType.ERROR ).assertNotNullOrBlank().assertContainsNoWhitespace();
        }
        return builder.getFindings();
    }

}
