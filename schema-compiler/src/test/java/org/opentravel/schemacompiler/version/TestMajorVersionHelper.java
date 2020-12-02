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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFolder;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

import java.io.File;
import java.util.List;

/**
 * Verifies the operation of the <code>MajorVersionHelper</code> class.
 * 
 * @author S. Livezey
 */
public class TestMajorVersionHelper extends AbstractVersionHelperTests {

    @Test
    public void testGetVersionChain_latestVersion() throws Exception {
        TLModel model = loadTestModel( FILE_VERSION_1_2_2 );
        TLLibrary patchVersionLibrary = (TLLibrary) model.getLibrary( NS_VERSION_1_2_2, TEST_LIBRARY_NAME );
        MajorVersionHelper helper = new MajorVersionHelper();
        List<TLLibrary> versionChain = helper.getVersionChain( patchVersionLibrary );

        assertEquals( 5, versionChain.size() );
        assertEquals( NS_VERSION_1_2_2, versionChain.get( 0 ).getNamespace() );
        assertEquals( NS_VERSION_1_2_1, versionChain.get( 1 ).getNamespace() );
        assertEquals( NS_VERSION_1_2, versionChain.get( 2 ).getNamespace() );
        assertEquals( NS_VERSION_1_1, versionChain.get( 3 ).getNamespace() );
        assertEquals( NS_VERSION_1, versionChain.get( 4 ).getNamespace() );
    }

    @Test
    public void testGetVersionChain_notLatestVersion() throws Exception {
        TLModel model = loadTestModel( FILE_VERSION_1_2_2 );
        TLLibrary patchVersionLibrary = (TLLibrary) model.getLibrary( NS_VERSION_1_2_1, TEST_LIBRARY_NAME );
        MajorVersionHelper helper = new MajorVersionHelper();
        List<TLLibrary> versionChain = helper.getVersionChain( patchVersionLibrary );

        assertEquals( 4, versionChain.size() );
        assertEquals( NS_VERSION_1_2_1, versionChain.get( 0 ).getNamespace() );
        assertEquals( NS_VERSION_1_2, versionChain.get( 1 ).getNamespace() );
        assertEquals( NS_VERSION_1_1, versionChain.get( 2 ).getNamespace() );
        assertEquals( NS_VERSION_1, versionChain.get( 3 ).getNamespace() );
    }

    @Test
    public void testGetMajorVersion() throws Exception {
        TLModel model = loadTestModel( FILE_VERSION_1_2_2 );
        TLLibrary majorVersionLibrary = (TLLibrary) model.getLibrary( NS_VERSION_1, TEST_LIBRARY_NAME );
        TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary( NS_VERSION_1_2, TEST_LIBRARY_NAME );
        TLLibrary patchVersionLibrary = (TLLibrary) model.getLibrary( NS_VERSION_1_2_2, TEST_LIBRARY_NAME );
        MajorVersionHelper helper = new MajorVersionHelper();

        assertNotNull( majorVersionLibrary );
        assertNotNull( minorVersionLibrary );
        assertNotNull( patchVersionLibrary );

        assertEquals( majorVersionLibrary, helper.getMajorVersion( majorVersionLibrary ) );
        assertEquals( majorVersionLibrary, helper.getMajorVersion( minorVersionLibrary ) );
        assertEquals( majorVersionLibrary, helper.getMajorVersion( patchVersionLibrary ) );

        model.removeLibrary( majorVersionLibrary );
        assertEquals( null, helper.getMajorVersion( minorVersionLibrary ) );
        assertEquals( null, helper.getMajorVersion( patchVersionLibrary ) );
    }

    @Test
    public void testNewMajorLibraryVersion_fromMajorVersion() throws Exception {
        TLModel model = loadTestModel( FILE_VERSION_1_2_2, FILE_VERSION_1_0_1 );
        TLLibrary majorVersionLibrary = (TLLibrary) model.getLibrary( NS_VERSION_1, TEST_LIBRARY_NAME );
        File newVersionLibraryFile = purgeExistingFile(
            new File( System.getProperty( "user.dir" ), "/target/test-save-location/library_v02_00.otm" ) );
        MajorVersionHelper helper = new MajorVersionHelper();

        TLLibrary newMajorVersionLibrary = helper.createNewMajorVersion( majorVersionLibrary, newVersionLibraryFile );

        assertNotNull( newMajorVersionLibrary );
        assertEquals( "2.0.0", newMajorVersionLibrary.getVersion() );
        assertEquals( majorVersionLibrary.getName(), newMajorVersionLibrary.getName() );
        assertEquals( majorVersionLibrary.getBaseNamespace(), newMajorVersionLibrary.getBaseNamespace() );
        assertEquals( majorVersionLibrary.getComments(), newMajorVersionLibrary.getComments() );
        validateLibraryContents( newMajorVersionLibrary );
    }

    @Test
    public void testNewMajorLibraryVersion_fromMinorVersion() throws Exception {
        TLModel model = loadTestModel( FILE_VERSION_1_2_2, FILE_VERSION_1_0_1 );
        TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary( NS_VERSION_1_1, TEST_LIBRARY_NAME );
        File newVersionLibraryFile = purgeExistingFile(
            new File( System.getProperty( "user.dir" ), "/target/test-save-location/library_v02_00.otm" ) );
        MajorVersionHelper helper = new MajorVersionHelper();

        TLLibrary newMajorVersionLibrary = helper.createNewMajorVersion( minorVersionLibrary, newVersionLibraryFile );

        assertNotNull( newMajorVersionLibrary );
        assertEquals( "2.0.0", newMajorVersionLibrary.getVersion() );
        assertEquals( minorVersionLibrary.getName(), newMajorVersionLibrary.getName() );
        assertEquals( minorVersionLibrary.getBaseNamespace(), newMajorVersionLibrary.getBaseNamespace() );
        assertEquals( minorVersionLibrary.getComments(), newMajorVersionLibrary.getComments() );
        validateLibraryContents( newMajorVersionLibrary );
    }

    @Test
    public void testNewMajorLibraryVersion_fromPatchVersion() throws Exception {
        TLModel model = loadTestModel( FILE_VERSION_1_2_2, FILE_VERSION_1_0_1 );
        TLLibrary patchVersionLibrary = (TLLibrary) model.getLibrary( NS_VERSION_1_2_1, TEST_LIBRARY_NAME );
        File newVersionLibraryFile = purgeExistingFile(
            new File( System.getProperty( "user.dir" ), "/target/test-save-location/library_v02_00.otm" ) );
        MajorVersionHelper helper = new MajorVersionHelper();

        TLLibrary newMajorVersionLibrary = helper.createNewMajorVersion( patchVersionLibrary, newVersionLibraryFile );

        assertNotNull( newMajorVersionLibrary );
        assertEquals( "2.0.0", newMajorVersionLibrary.getVersion() );
        assertEquals( patchVersionLibrary.getName(), newMajorVersionLibrary.getName() );
        assertEquals( patchVersionLibrary.getBaseNamespace(), newMajorVersionLibrary.getBaseNamespace() );
        assertEquals( patchVersionLibrary.getComments(), newMajorVersionLibrary.getComments() );
        validateLibraryContents( newMajorVersionLibrary );
    }

    @Test
    public void testRollupMinorVersion() throws Exception {
        TLModel model = loadTestModel( FILE_VERSION_1_1 );
        TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary( NS_VERSION_1_1, TEST_LIBRARY_NAME );
        File newVersionLibraryFile = purgeExistingFile(
            new File( System.getProperty( "user.dir" ), "/target/test-save-location/library_v02_00.otm" ) );
        MajorVersionHelper helper = new MajorVersionHelper();

        TLLibrary newMajorVersionLibrary = helper.createNewMajorVersion( minorVersionLibrary, newVersionLibraryFile );
        TLBusinessObject lookupBOv2 = newMajorVersionLibrary.getBusinessObjectType( "LookupBO" );

        loadTestModel( model, true, FILE_VERSION_1_2 );
        TLLibrary minorVersionLibrary12 = (TLLibrary) model.getLibrary( NS_VERSION_1_2, TEST_LIBRARY_NAME );
        TLBusinessObject lookupBOv12 = minorVersionLibrary12.getBusinessObjectType( "LookupBO" );

        helper.rollupMinorVersion( lookupBOv2, lookupBOv12 );
        assertNotNull( lookupBOv2.getSummaryFacet().getElement( "Element12" ) );
    }

    @Test
    public void testRollupPatchVersion() throws Exception {
        TLModel model = loadTestModel( FILE_VERSION_1_1 );
        TLLibrary minorVersionLibrary = (TLLibrary) model.getLibrary( NS_VERSION_1_1, TEST_LIBRARY_NAME );
        File newVersionLibraryFile = purgeExistingFile(
            new File( System.getProperty( "user.dir" ), "/target/test-save-location/library_v02_00.otm" ) );
        MajorVersionHelper helper = new MajorVersionHelper();

        TLLibrary newMajorVersionLibrary = helper.createNewMajorVersion( minorVersionLibrary, newVersionLibraryFile );
        TLBusinessObject lookupBOv2 = newMajorVersionLibrary.getBusinessObjectType( "LookupBO" );

        loadTestModel( model, true, FILE_VERSION_1_0_1 );
        TLLibrary patchVersionLibrary101 = (TLLibrary) model.getLibrary( NS_VERSION_1_0_1, TEST_LIBRARY_NAME );
        TLExtensionPointFacet boPatch =
            patchVersionLibrary101.getExtensionPointFacetType( "ExtensionPoint_LookupBO_Summary" );

        helper.rollupPatchVersion( lookupBOv2, boPatch );
        assertNotNull( lookupBOv2.getSummaryFacet().getAttribute( "extBOAttribute101" ) );
    }

    private void validateLibraryContents(TLLibrary newMajorVersionLibrary) throws Exception {

        // Verify that all required simple types were carried forward
        assertNotNull( newMajorVersionLibrary.getSimpleType( "TestMajorVersionSimple" ) );
        assertNotNull( newMajorVersionLibrary.getClosedEnumerationType( "TestMajorVersionClosedEnum" ) );
        assertNotNull( newMajorVersionLibrary.getOpenEnumerationType( "TestMajorVersionOpenEnum" ) );
        assertNotNull( newMajorVersionLibrary.getSimpleType( "TestMinorVersionSimple" ) );
        assertNotNull( newMajorVersionLibrary.getClosedEnumerationType( "TestMinorVersionClosedEnum" ) );
        assertNotNull( newMajorVersionLibrary.getOpenEnumerationType( "TestMinorVersionOpenEnum" ) );

        // Validate the 'Lookup...' grouping
        TLBusinessObject lookupBO = newMajorVersionLibrary.getBusinessObjectType( "LookupBO" );
        TLCoreObject lookupCore = newMajorVersionLibrary.getCoreObjectType( "LookupCore" );
        TLChoiceObject lookupChoice = newMajorVersionLibrary.getChoiceObjectType( "LookupChoice" );
        TLValueWithAttributes lookupVWA = newMajorVersionLibrary.getValueWithAttributesType( "LookupVWA" );
        TLOperation lookupOp = newMajorVersionLibrary.getService().getOperation( "LookupOperation" );
        TLOpenEnumeration lookupOpenEnum = newMajorVersionLibrary.getOpenEnumerationType( "LookupOpenEnum" );
        TLClosedEnumeration lookupClosedEnum = newMajorVersionLibrary.getClosedEnumerationType( "LookupClosedEnum" );
        TLSimple lookupSimple = newMajorVersionLibrary.getSimpleType( "LookupSimple" );
        TLResource lookupResource = newMajorVersionLibrary.getResourceType( "LookupResource" );

        assertNotNull( lookupBO );
        assertNull( lookupBO.getExtension() );
        assertEquals( 2, lookupBO.getSummaryFacet().getAttributes().size() );
        assertEquals( 4, lookupBO.getSummaryFacet().getElements().size() );
        assertContainsAttributes( lookupBO.getSummaryFacet(), "extBOAttribute121", "extBOAttribute122" );
        assertContainsElements( lookupBO.getSummaryFacet(), "Element1", "Element11", "Element11Param", "Element12" );

        assertNotNull( lookupCore );
        assertNull( lookupCore.getExtension() );
        assertEquals( 2, lookupCore.getSummaryFacet().getAttributes().size() );
        assertEquals( 3, lookupCore.getSummaryFacet().getElements().size() );
        assertContainsAttributes( lookupCore.getSummaryFacet(), "extCoreAttribute121", "extCoreAttribute122" );
        assertContainsElements( lookupCore.getSummaryFacet(), "Element1", "Element11", "Element12" );

        assertNotNull( lookupChoice );
        assertNull( lookupChoice.getExtension() );
        assertEquals( 2, lookupChoice.getSharedFacet().getAttributes().size() );
        assertEquals( 3, lookupChoice.getSharedFacet().getElements().size() );
        assertContainsAttributes( lookupChoice.getSharedFacet(), "extChoiceAttribute121", "extChoiceAttribute122" );
        assertContainsElements( lookupChoice.getSharedFacet(), "sharedElement1", "sharedElement11", "sharedElement12" );
        assertEquals( 2, lookupChoice.getChoiceFacets().size() );
        assertEquals( "ChoiceA", lookupChoice.getChoiceFacets().get( 0 ).getName() );
        assertEquals( "ChoiceB", lookupChoice.getChoiceFacets().get( 1 ).getName() );
        assertEquals( 3, lookupChoice.getChoiceFacet( "ChoiceB" ).getElements().size() );
        assertContainsElements( lookupChoice.getChoiceFacet( "ChoiceB" ), "choiceBElement1", "choiceBElement11",
            "choiceBElement12" );

        assertEquals( 1, lookupChoice.getChoiceFacet( "ChoiceB" ).getChildFacets().size() );
        assertEquals( "SubChoice1", lookupChoice.getChoiceFacet( "ChoiceB" ).getChildFacets().get( 0 ).getName() );
        assertEquals( 1, lookupChoice.getChoiceFacet( "ChoiceB" ).getChildFacet( "SubChoice1" ).getElements().size() );
        assertContainsElements( lookupChoice.getChoiceFacet( "ChoiceB" ).getChildFacet( "SubChoice1" ),
            "subChoiceB1Element1" );

        assertNotNull( lookupVWA );
        assertNotNull( lookupVWA.getParentType() );
        assertEquals( "string", lookupVWA.getParentType().getLocalName() );
        assertEquals( 3, lookupVWA.getAttributes().size() );
        assertContainsAttributes( lookupVWA, "vwaAttribute1", "vwaAttribute11", "vwaAttribute12" );

        assertNotNull( lookupOp );
        assertNull( lookupOp.getExtension() );
        assertEquals( 2, lookupOp.getRequest().getAttributes().size() );
        assertEquals( 3, lookupOp.getRequest().getElements().size() );
        assertContainsAttributes( lookupOp.getRequest(), "extOperationAttribute121", "extOperationAttribute122" );
        assertContainsElements( lookupOp.getRequest(), "RequestValue1", "RequestValue11", "RequestValue12" );

        assertNotNull( lookupOpenEnum );
        assertNull( lookupOpenEnum.getExtension() );
        assertEquals( 3, lookupOpenEnum.getValues().size() );
        assertContainsValues( lookupOpenEnum, "a1", "b1", "c1" );

        assertNotNull( lookupClosedEnum );
        assertNull( lookupClosedEnum.getExtension() );
        assertEquals( 3, lookupClosedEnum.getValues().size() );
        assertContainsValues( lookupClosedEnum, "a1", "b1", "c1" );

        assertNotNull( lookupSimple );
        assertNotNull( lookupSimple.getParentType() );
        assertEquals( "string", lookupSimple.getParentType().getLocalName() );
        assertEquals( "[A-Za-z]*", lookupSimple.getPattern() );
        assertEquals( 2, lookupSimple.getMinLength() );
        assertEquals( 5, lookupSimple.getMaxLength() );

        assertNotNull( lookupResource );
        assertNull( lookupResource.getExtension() );
        assertEquals( lookupBO, lookupResource.getBusinessObjectRef() );
        assertEquals( 0, lookupResource.getParentRefs().size() );
        assertEquals( 4, lookupResource.getParamGroups().size() );
        assertContainsParamGroups( lookupResource, "LookupParametersShared", "LookupParameters10", "LookupParameters11",
            "LookupParameters12" );
        assertEquals( 3, lookupResource.getParamGroup( "LookupParametersShared" ).getParameters().size() );
        assertContainsParameters( lookupResource.getParamGroup( "LookupParametersShared" ), "Element1",
            "Element11Param", "Element12" );
        assertEquals( 4, lookupResource.getActionFacets().size() );
        assertContainsActionFacets( lookupResource, "LookupFacetShared", "LookupFacet10", "LookupFacet11",
            "LookupFacet12" );
        assertEquals( 3, lookupResource.getActions().size() );
        assertContainsActions( lookupResource, "LookupAction10", "LookupAction11", "LookupAction12" );
        assertNotNull( lookupResource.getAction( "LookupAction10" ).getRequest() );
        assertEquals( 1, lookupResource.getAction( "LookupAction10" ).getResponses().size() );

        // Validate the 'LaterMinorVersion...' grouping
        TLBusinessObject laterMinorVersionBO = newMajorVersionLibrary.getBusinessObjectType( "LaterMinorVersionBO" );
        TLCoreObject laterMinorVersionCore = newMajorVersionLibrary.getCoreObjectType( "LaterMinorVersionCore" );
        TLChoiceObject laterMinorVersionChoice =
            newMajorVersionLibrary.getChoiceObjectType( "LaterMinorVersionChoice" );
        TLValueWithAttributes laterMinorVersionVWA =
            newMajorVersionLibrary.getValueWithAttributesType( "LaterMinorVersionVWA" );
        TLOperation laterMinorVersionOp =
            newMajorVersionLibrary.getService().getOperation( "LaterMinorVersionOperation" );
        TLOpenEnumeration laterMinorVersionOpenEnum =
            newMajorVersionLibrary.getOpenEnumerationType( "LaterMinorVersionOpenEnum" );
        TLClosedEnumeration laterMinorVersionClosedEnum =
            newMajorVersionLibrary.getClosedEnumerationType( "LaterMinorVersionClosedEnum" );
        TLSimple laterMinorVersionSimple = newMajorVersionLibrary.getSimpleType( "LaterMinorVersionSimple" );
        TLResource laterMinorVersionResource = newMajorVersionLibrary.getResourceType( "LaterMinorVersionResource" );

        assertNotNull( laterMinorVersionBO );
        assertNull( laterMinorVersionBO.getExtension() );
        assertEquals( 0, laterMinorVersionBO.getSummaryFacet().getAttributes().size() );
        assertEquals( 2, laterMinorVersionBO.getSummaryFacet().getElements().size() );
        assertContainsElements( laterMinorVersionBO.getSummaryFacet(), "Element1", "Element12" );

        assertNotNull( laterMinorVersionCore );
        assertNull( laterMinorVersionCore.getExtension() );
        assertEquals( 0, laterMinorVersionCore.getSummaryFacet().getAttributes().size() );
        assertEquals( 2, laterMinorVersionCore.getSummaryFacet().getElements().size() );
        assertContainsElements( laterMinorVersionCore.getSummaryFacet(), "Element1", "Element12" );

        assertNotNull( laterMinorVersionChoice );
        assertNull( laterMinorVersionChoice.getExtension() );
        assertEquals( 0, laterMinorVersionChoice.getSharedFacet().getAttributes().size() );
        assertEquals( 2, laterMinorVersionChoice.getSharedFacet().getElements().size() );
        assertContainsElements( laterMinorVersionChoice.getSharedFacet(), "sharedElement1", "sharedElement12" );

        assertNotNull( laterMinorVersionVWA );
        assertNotNull( laterMinorVersionVWA.getParentType() );
        assertEquals( "string", laterMinorVersionVWA.getParentType().getLocalName() );
        assertEquals( 2, laterMinorVersionVWA.getAttributes().size() );
        assertContainsAttributes( laterMinorVersionVWA, "vwaAttribute1", "vwaAttribute12" );

        assertNotNull( laterMinorVersionOp );
        assertNull( laterMinorVersionOp.getExtension() );
        assertEquals( 0, laterMinorVersionOp.getRequest().getAttributes().size() );
        assertEquals( 2, laterMinorVersionOp.getRequest().getElements().size() );
        assertContainsElements( laterMinorVersionOp.getRequest(), "RequestValue1", "RequestValue12" );

        assertNotNull( laterMinorVersionOpenEnum );
        assertNull( laterMinorVersionOpenEnum.getExtension() );
        assertEquals( 3, laterMinorVersionOpenEnum.getValues().size() );
        assertContainsValues( laterMinorVersionOpenEnum, "a1", "b1", "c1" );

        assertNotNull( laterMinorVersionClosedEnum );
        assertNull( laterMinorVersionClosedEnum.getExtension() );
        assertEquals( 3, laterMinorVersionClosedEnum.getValues().size() );
        assertContainsValues( laterMinorVersionClosedEnum, "a1", "b1", "c1" );

        assertNotNull( laterMinorVersionSimple );
        assertNotNull( laterMinorVersionSimple.getParentType() );
        assertEquals( "string", laterMinorVersionSimple.getParentType().getLocalName() );
        assertEquals( "[A-Za-z]*", laterMinorVersionSimple.getPattern() );
        assertEquals( 2, laterMinorVersionSimple.getMinLength() );
        assertEquals( 5, laterMinorVersionSimple.getMaxLength() );

        assertNotNull( laterMinorVersionResource );
        assertNull( laterMinorVersionResource.getExtension() );
        assertEquals( laterMinorVersionBO, laterMinorVersionResource.getBusinessObjectRef() );
        assertEquals( 2, laterMinorVersionResource.getParentRefs().size() );
        assertContainsParentRefs( laterMinorVersionResource, "/parent/{uid}", "/parent2/{uid}" );
        assertEquals( 3, laterMinorVersionResource.getParamGroups().size() );
        assertContainsParamGroups( laterMinorVersionResource, "LaterMinorVersionParametersShared",
            "LaterMinorVersionParameters10", "LaterMinorVersionParameters12" );
        assertEquals( 2,
            laterMinorVersionResource.getParamGroup( "LaterMinorVersionParametersShared" ).getParameters().size() );
        assertContainsParameters( laterMinorVersionResource.getParamGroup( "LaterMinorVersionParametersShared" ),
            "Element1", "Element12" );
        assertEquals( 3, laterMinorVersionResource.getActionFacets().size() );
        assertContainsActionFacets( laterMinorVersionResource, "LaterMinorVersionFacetShared",
            "LaterMinorVersionFacet10", "LaterMinorVersionFacet12" );
        assertEquals( 2, laterMinorVersionResource.getActions().size() );
        assertContainsActions( laterMinorVersionResource, "LaterMinorVersionAction10", "LaterMinorVersionAction12" );
        assertNotNull( laterMinorVersionResource.getAction( "LaterMinorVersionAction10" ).getRequest() );
        assertEquals( 1, laterMinorVersionResource.getAction( "LaterMinorVersionAction10" ).getResponses().size() );

        // Validate the 'MinorVersionTest...' grouping
        TLBusinessObject minorVersionTestBO = newMajorVersionLibrary.getBusinessObjectType( "MinorVersionTestBO" );
        TLCoreObject minorVersionTestCore = newMajorVersionLibrary.getCoreObjectType( "MinorVersionTestCore" );
        TLChoiceObject minorVersionTestChoice = newMajorVersionLibrary.getChoiceObjectType( "MinorVersionTestChoice" );
        TLValueWithAttributes minorVersionTestVWA =
            newMajorVersionLibrary.getValueWithAttributesType( "MinorVersionTestVWA" );
        TLOperation minorVersionTestOp =
            newMajorVersionLibrary.getService().getOperation( "MinorVersionTestOperation" );
        TLOpenEnumeration minorVersionTestOpenEnum =
            newMajorVersionLibrary.getOpenEnumerationType( "MinorVersionTestOpenEnum" );
        TLClosedEnumeration minorVersionTestClosedEnum =
            newMajorVersionLibrary.getClosedEnumerationType( "MinorVersionTestClosedEnum" );
        TLSimple minorVersionTestSimple = newMajorVersionLibrary.getSimpleType( "MinorVersionTestSimple" );
        TLResource minorVersionTestResource = newMajorVersionLibrary.getResourceType( "MinorVersionTestResource" );

        assertNotNull( minorVersionTestBO );
        assertNull( minorVersionTestBO.getExtension() );
        assertEquals( 0, minorVersionTestBO.getSummaryFacet().getAttributes().size() );
        assertEquals( 1, minorVersionTestBO.getSummaryFacet().getElements().size() );
        assertContainsElements( minorVersionTestBO.getSummaryFacet(), "Element11" );

        assertNotNull( minorVersionTestCore );
        assertNull( minorVersionTestCore.getExtension() );
        assertEquals( 0, minorVersionTestCore.getSummaryFacet().getAttributes().size() );
        assertEquals( 1, minorVersionTestCore.getSummaryFacet().getElements().size() );
        assertContainsElements( minorVersionTestCore.getSummaryFacet(), "Element11" );

        assertNotNull( minorVersionTestChoice );
        assertNull( minorVersionTestChoice.getExtension() );
        assertEquals( 0, minorVersionTestChoice.getSharedFacet().getAttributes().size() );
        assertEquals( 1, minorVersionTestChoice.getSharedFacet().getElements().size() );
        assertContainsElements( minorVersionTestChoice.getSharedFacet(), "sharedElement11" );

        assertNotNull( minorVersionTestVWA );
        assertNotNull( minorVersionTestVWA.getParentType() );
        assertEquals( "string", minorVersionTestVWA.getParentType().getLocalName() );
        assertEquals( 1, minorVersionTestVWA.getAttributes().size() );
        assertContainsAttributes( minorVersionTestVWA, "vwaAttribute11" );

        assertNotNull( minorVersionTestOp );
        assertNull( minorVersionTestOp.getExtension() );
        assertEquals( 0, minorVersionTestOp.getRequest().getAttributes().size() );
        assertEquals( 1, minorVersionTestOp.getRequest().getElements().size() );
        assertContainsElements( minorVersionTestOp.getRequest(), "RequestValue11" );

        assertNotNull( minorVersionTestOpenEnum );
        assertNull( minorVersionTestOpenEnum.getExtension() );
        assertEquals( 3, minorVersionTestOpenEnum.getValues().size() );
        assertContainsValues( minorVersionTestOpenEnum, "a1", "b1", "c1" );

        assertNotNull( minorVersionTestClosedEnum );
        assertNull( minorVersionTestClosedEnum.getExtension() );
        assertEquals( 3, minorVersionTestClosedEnum.getValues().size() );
        assertContainsValues( minorVersionTestClosedEnum, "a1", "b1", "c1" );

        assertNotNull( minorVersionTestSimple );
        assertNotNull( minorVersionTestSimple.getParentType() );
        assertEquals( "string", minorVersionTestSimple.getParentType().getLocalName() );
        assertEquals( "[A-Za-z]*", minorVersionTestSimple.getPattern() );
        assertEquals( 2, minorVersionTestSimple.getMinLength() );
        assertEquals( 5, minorVersionTestSimple.getMaxLength() );

        assertNotNull( minorVersionTestResource );
        assertNull( minorVersionTestResource.getExtension() );
        assertEquals( minorVersionTestBO, minorVersionTestResource.getBusinessObjectRef() );
        assertEquals( 0, minorVersionTestResource.getParentRefs().size() );
        assertEquals( 1, minorVersionTestResource.getParamGroups().size() );
        assertContainsParamGroups( minorVersionTestResource, "MinorVersionTestParameters" );
        assertEquals( 1,
            minorVersionTestResource.getParamGroup( "MinorVersionTestParameters" ).getParameters().size() );
        assertContainsParameters( minorVersionTestResource.getParamGroup( "MinorVersionTestParameters" ), "uid" );
        assertEquals( 1, minorVersionTestResource.getActionFacets().size() );
        assertContainsActionFacets( minorVersionTestResource, "MinorVersionTestFacet" );
        assertEquals( 1, minorVersionTestResource.getActions().size() );
        assertContainsActions( minorVersionTestResource, "MinorVersionTestAction" );
        assertNotNull( minorVersionTestResource.getAction( "MinorVersionTestAction" ).getRequest() );
        assertEquals( 1, minorVersionTestResource.getAction( "MinorVersionTestAction" ).getResponses().size() );

        // Validate that the folder structure was rolled-up correctly
        List<TLFolder> folders = newMajorVersionLibrary.getFolders();

        assertEquals( 2, folders.size() );
        assertEquals( 3, folders.get( 0 ).getFolders().size() );
        assertEquals( 0, folders.get( 1 ).getFolders().size() );
        assertEquals( "Folder1", folders.get( 0 ).getName() );
        assertEquals( "Folder1-1", folders.get( 0 ).getFolders().get( 0 ).getName() );
        assertEquals( "Folder1-2", folders.get( 0 ).getFolders().get( 1 ).getName() );
        assertEquals( "Folder1-3", folders.get( 0 ).getFolders().get( 2 ).getName() );
        assertEquals( "Folder2", folders.get( 1 ).getName() );
        assertEquals( 1, folders.get( 0 ).getEntities().size() );
        assertEquals( "LookupCore", folders.get( 0 ).getEntities().get( 0 ).getLocalName() );
        assertEquals( 1, folders.get( 0 ).getFolders().get( 0 ).getEntities().size() );
        assertEquals( "LookupChoice", folders.get( 0 ).getFolders().get( 0 ).getEntities().get( 0 ).getLocalName() );
        assertEquals( 1, folders.get( 1 ).getEntities().size() );
        assertEquals( "LookupBO", folders.get( 1 ).getEntities().get( 0 ).getLocalName() );

        // Verify the total number of elements to make sure nothing
        // exists, except for the items we just tested.
        assertEquals( 36,
            newMajorVersionLibrary.getNamedMembers().size() - newMajorVersionLibrary.getContextualFacetTypes().size() );
        assertEquals( 4, newMajorVersionLibrary.getService().getOperations().size() );
    }

}
