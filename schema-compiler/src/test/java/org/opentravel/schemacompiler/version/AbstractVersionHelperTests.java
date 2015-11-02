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
package org.opentravel.schemacompiler.version;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.CatalogLibraryNamespaceResolver;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Base class for all tests for the version helper classes.
 * 
 * @author S. Livezey
 */
public abstract class AbstractVersionHelperTests {

    public static final String TEST_LIBRARY_NAME = "test_library";

    public static final String LIBNAME_VERSION_1 = "library_v01_00";
    public static final String LIBNAME_VERSION_1_0_1 = "library_v01_00_01";
    public static final String LIBNAME_VERSION_1_1 = "library_v01_01";
    public static final String LIBNAME_VERSION_1_2 = "library_v01_02";
    public static final String LIBNAME_VERSION_1_2_1 = "library_v01_02_01";
    public static final String LIBNAME_VERSION_1_2_2 = "library_v01_02_02";

    public static final String FILE_VERSION_1 = LIBNAME_VERSION_1 + ".xml";
    public static final String FILE_VERSION_1_0_1 = LIBNAME_VERSION_1_0_1 + ".xml";
    public static final String FILE_VERSION_1_1 = LIBNAME_VERSION_1_1 + ".xml";
    public static final String FILE_VERSION_1_2 = LIBNAME_VERSION_1_2 + ".xml";
    public static final String FILE_VERSION_1_2_1 = LIBNAME_VERSION_1_2_1 + ".xml";
    public static final String FILE_VERSION_1_2_2 = LIBNAME_VERSION_1_2_2 + ".xml";

    public static final String NS_VERSION_1 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/v01";
    public static final String NS_VERSION_1_0_1 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/v01_00_01";
    public static final String NS_VERSION_1_1 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/v01_01";
    public static final String NS_VERSION_1_2 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/v01_02";
    public static final String NS_VERSION_1_2_1 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/v01_02_01";
    public static final String NS_VERSION_1_2_2 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/version-test/v01_02_02";

    protected File testFolder = new File(System.getProperty("user.dir"),
            "/src/test/resources/versions");
    protected File catalogFile = new File(testFolder, "/version-catalog.xml");

    /**
     * Loads the specified library and all of its dependencies. Prior to returning the model, it
     * will be checked to ensure that no validation errors exist.
     * 
     * @param libraryFilenames
     *            the list of filename for the library version to load
     * @return TLModel
     * @throws Exception
     */
    protected TLModel loadTestModel(String... libraryFilenames) throws Exception {
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();

        modelLoader.setNamespaceResolver(new CatalogLibraryNamespaceResolver(catalogFile));

        for (String libraryFilename : libraryFilenames) {
            LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource(new File(
                    testFolder, "/" + libraryFilename));
            ValidationFindings findings = modelLoader.loadLibraryModel(libraryInput);

            SchemaCompilerTestUtils.printFindings(findings, FindingType.ERROR);
            assertFalse(findings.hasFinding(FindingType.ERROR));
        }
        TLModel model = modelLoader.getLibraryModel();
        assertNotNull(model);
        return model;
    }

    protected List<String> getLibraryNames(List<TLLibrary> libraryList) {
        List<String> libraryNames = new ArrayList<String>();

        for (TLLibrary library : libraryList) {
            libraryNames.add(getLibraryName(library));
        }
        return libraryNames;
    }

    protected String getLibraryName(TLLibrary library) {
        String libraryName = null;

        if (library != null) {
            libraryName = URLUtils.getShortRepresentation(library.getLibraryUrl());
            int dotIdx = libraryName.indexOf('.');

            if (dotIdx >= 0) {
                libraryName = libraryName.substring(0, dotIdx);
            }
            if (libraryName.startsWith("/")) {
                libraryName = libraryName.substring(1);
            }
        }
        return libraryName;
    }

    protected File purgeExistingFile(File file) {
        if (file.exists()) {
            file.delete();
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    protected void assertContainsAttributes(TLAttributeOwner attrOwner, String... attrNames) {
        Set<String> ownerAttrs = new HashSet<String>();

        for (TLAttribute attr : attrOwner.getAttributes()) {
            ownerAttrs.add(attr.getName());
        }
        for (String attrName : attrNames) {
            if (!ownerAttrs.contains(attrName)) {
                fail("Expected attribute name not found: " + attrName);
            }
        }
    }

    protected void assertContainsElements(TLPropertyOwner elementOwner, String... elementNames) {
        Set<String> ownerElements = new HashSet<String>();

        for (TLProperty element : elementOwner.getElements()) {
            ownerElements.add(element.getName());
        }
        for (String elementName : elementNames) {
            if (!ownerElements.contains(elementName)) {
            	fail("Expected element name not found: " + elementName);
            }
        }
    }

    protected void assertContainsValues(TLAbstractEnumeration valueOwner, String... valueLiterals) {
        Set<String> ownerLiterals = new HashSet<String>();

        for (TLEnumValue value : valueOwner.getValues()) {
        	ownerLiterals.add(value.getLiteral());
        }
        for (String literal : valueLiterals) {
            if (!ownerLiterals.contains(literal)) {
            	fail("Expected enumeration value not found: " + literal);
            }
        }
    }

    protected void assertContainsParentRefs(TLResource resource, String... pathTemplates) {
        Set<String> resourceParentPaths = new HashSet<String>();

        for (TLResourceParentRef parentRef : resource.getParentRefs()) {
        	resourceParentPaths.add(parentRef.getPathTemplate());
        }
        for (String pathTemplate : pathTemplates) {
            if (!resourceParentPaths.contains(pathTemplate)) {
            	fail("Expected parent reference not found: " + pathTemplate);
            }
        }
    }

    protected void assertContainsParamGroups(TLResource resource, String... paramGroupNames) {
        Set<String> resourceParamGroups = new HashSet<String>();

        for (TLParamGroup paramGroup : resource.getParamGroups()) {
        	resourceParamGroups.add(paramGroup.getName());
        }
        for (String groupName : paramGroupNames) {
            if (!resourceParamGroups.contains(groupName)) {
            	fail("Expected parameter group not found: " + groupName);
            }
        }
    }

    protected void assertContainsParameters(TLParamGroup paramGroup, String... paramNames) {
        Set<String> pgParameterNames = new HashSet<String>();

        for (TLParameter param : paramGroup.getParameters()) {
        	if (param.getFieldRef() != null) {
            	pgParameterNames.add(param.getFieldRef().getName());
        	}
        }
        for (String paramName : paramNames) {
            if (!pgParameterNames.contains(paramName)) {
            	fail("Expected parameter not found: " + paramName);
            }
        }
    }

    protected void assertContainsActionFacets(TLResource resource, String... facetNames) {
        Set<String> resourceActionFacets = new HashSet<String>();

        for (TLActionFacet facet : resource.getActionFacets()) {
        	resourceActionFacets.add(facet.getName());
        }
        for (String facetName : facetNames) {
            if (!resourceActionFacets.contains(facetName)) {
            	fail("Expected action facet not found: " + facetName);
            }
        }
    }

    protected void assertContainsActions(TLResource resource, String... actionIds) {
        Set<String> resourceActionIds = new HashSet<String>();

        for (TLAction action : resource.getActions()) {
        	resourceActionIds.add(action.getActionId());
        }
        for (String actionId : actionIds) {
            if (!resourceActionIds.contains(actionId)) {
            	fail("Expected resource action not found: " + actionId);
            }
        }
    }

}
