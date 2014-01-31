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
package org.opentravel.schemacompiler.validate.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLLibraryBaseValidator;
import org.opentravel.schemacompiler.validate.impl.ChameleonTypeChecker;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

/**
 * Validator for the <code>TLLibrary</code> class.
 * 
 * @author S. Livezey
 */
public class TLLibraryCompileValidator extends TLLibraryBaseValidator {

    public static final String ERROR_DUPLICATE_SERVICE_NAME = "DUPLICATE_SERVICE_NAME";
    public static final String ERROR_DUPLICATE_IMPORT_PREFIX = "DUPLICATE_IMPORT_PREFIX";
    public static final String ERROR_DUPLICATE_CHAMELEON_SYMBOLS = "DUPLICATE_CHAMELEON_SYMBOLS";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLLibrary target) {
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("libraryUrl", target.getLibraryUrl()).setFindingType(FindingType.ERROR)
                .assertNotNull();

        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
                .assertNotNullOrBlank().assertPatternMatch(NAME_FILE_PATTERN);

        builder.setProperty("versionScheme", target.getVersionScheme())
                .setFindingType(FindingType.ERROR).assertNotNullOrBlank()
                .assertContainsNoWhitespace().assertValidVersionScheme();

        builder.setProperty("namespace", target.getNamespace()).setFindingType(FindingType.ERROR)
                .setVersionScheme(target.getVersionScheme()).assertNotNullOrBlank()
                .assertContainsNoWhitespace().assertValidNamespaceForVersionScheme();

        builder.setProperty("prefix", target.getPrefix()).setFindingType(FindingType.ERROR)
                .assertNotNullOrBlank().assertContainsNoWhitespace()
                .assertPatternMatch("[A-Za-z].*");

        builder.setProperty("includes", target.getIncludes()).setFindingType(FindingType.ERROR)
                .assertContainsNoNullElements();

        builder.setProperty("namedMembers", target.getNamedMembers())
                .setFindingType(FindingType.ERROR).assertContainsNoNullElements();

        // Validate each of the namespace imports
        Set<String> declaredPrefixes = new HashSet<String>();
        Set<String> duplicatePrefixes = new HashSet<String>();

        for (TLNamespaceImport nsImport : target.getNamespaceImports()) {
            String prefix = nsImport.getPrefix();
            String namespace = nsImport.getNamespace();

            builder.setProperty("namespaceImports.prefix", prefix)
                    .setFindingType(FindingType.ERROR).assertNotNullOrBlank()
                    .assertContainsNoWhitespace();

            builder.setProperty("namespaceImports.namespace", namespace)
                    .setFindingType(FindingType.ERROR).assertNotNullOrBlank()
                    .assertContainsNoWhitespace();

            // Check for duplicate prefixes
            if (declaredPrefixes.contains(prefix)) {
                if (!duplicatePrefixes.contains(prefix)) {
                    duplicatePrefixes.add(prefix);
                }
            } else {
                declaredPrefixes.add(prefix);
            }
        }
        if (!duplicatePrefixes.isEmpty()) {
            builder.addFinding(FindingType.ERROR, "namespaceImports.prefix",
                    ERROR_DUPLICATE_IMPORT_PREFIX, getCommaDelimitedString(duplicatePrefixes));
        }

        // Check for duplicate XSD type/element names due to chameleon references
        Collection<String> duplicateSymbols = ChameleonTypeChecker
                .findDuplicateChameleonSymbols(target);

        if (!duplicateSymbols.isEmpty()) {
            builder.addFinding(FindingType.ERROR, "namedMembers",
                    ERROR_DUPLICATE_CHAMELEON_SYMBOLS, getCommaDelimitedString(duplicateSymbols));
        }
        return builder.getFindings();
    }

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateChildren(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateChildren(TLLibrary target) {
        TLValidationBuilder builder = newValidationBuilder(target).addFindings(
                super.validateChildren(target));

        // Check for duplicate service names (be sure to check against all services throughout the
        // model)
        TLService service = target.getService();

        if ((target.getOwningModel() != null) && (service != null)
                && (service.getLocalName() != null)) {
            Map<String, List<TLService>> allServicesByNamespace = getAllServicesByNamespace(target
                    .getOwningModel());

            for (TLService nsService : allServicesByNamespace.get(service.getNamespace())) {
                if (service == nsService) {
                    continue;
                }
                if (service.getLocalName().equals(nsService.getLocalName())) {
                    builder.addFinding(FindingType.ERROR, "namedMembers",
                            ERROR_DUPLICATE_SERVICE_NAME, service.getLocalName(),
                            service.getNamespace());
                }
            }
        }

        return builder.getFindings();
    }

    /**
     * Returns a map that associates each service namespace with a list of services that are
     * assigned to that namespace.
     * 
     * @param model
     *            the model that owns all of the library/service definitions
     * @return Map<String,List<TLService>>
     */
    protected Map<String, List<TLService>> getAllServicesByNamespace(TLModel model) {
        Map<String, List<TLService>> servicesByNamespace = new HashMap<String, List<TLService>>();

        if (model != null) {
            for (TLLibrary lib : model.getUserDefinedLibraries()) {
                if (lib.getService() != null) {
                    List<TLService> serviceList = servicesByNamespace.get(lib.getService()
                            .getNamespace());

                    if (serviceList == null) {
                        serviceList = new ArrayList<TLService>();
                        servicesByNamespace.put(lib.getService().getNamespace(), serviceList);
                    }
                    serviceList.add(lib.getService());
                }
            }
        }
        return servicesByNamespace;
    }

    /**
     * Returns a comma-delimited string containing the members of the collection provided.
     * 
     * @param strings
     *            the collection of strings from which to build the comma-separated string value
     * @return String
     */
    private String getCommaDelimitedString(Collection<String> strings) {
        StringBuilder out = new StringBuilder();

        for (String str : strings) {
            if (out.length() > 0)
                out.append(", ");
            out.append(str);
        }
        return out.toString();
    }

}
