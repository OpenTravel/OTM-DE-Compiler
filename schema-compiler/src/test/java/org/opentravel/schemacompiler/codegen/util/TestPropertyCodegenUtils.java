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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;

import java.io.File;

/**
 * Verifies the functions of the <code>PropertyCodegenUtils</code> class.
 */
public class TestPropertyCodegenUtils {

    private TLModel testModel;
    private TLLibrary testLibrary;

    @Before
    public void setupModel() throws Exception {
        File projectTestFolder = new File( SchemaCompilerTestUtils.getBaseProjectLocation() );
        File projectFile = new File( projectTestFolder, "/project_1.xml" );
        ProjectManager projectManager = new ProjectManager( false );

        projectManager.loadProject( projectFile );
        testModel = projectManager.getModel();
        testLibrary = (TLLibrary) testModel
            .getLibrary( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v2", "library_1_p2" );
    }

    @Test
    public void testGetAlternateFacets() throws Exception {
        TLBusinessObject sampleBO = testLibrary.getBusinessObjectType( "SampleBusinessObject" );
        TLChoiceObject sampleChoice = testLibrary.getChoiceObjectType( "SampleChoice" );
        TLCoreObject sampleCore = testLibrary.getCoreObjectType( "SampleCore" );

        assertEquals( 0, PropertyCodegenUtils.getAlternateFacets( sampleBO.getIdFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getSummaryFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getDetailFacet() ).length );
        assertEquals( 0, PropertyCodegenUtils.getAlternateFacets( sampleBO.getQueryFacet( "FindByName" ) ).length );

        assertEquals( 0, PropertyCodegenUtils.getAlternateFacets( sampleBO, sampleBO.getIdFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO, sampleBO.getSummaryFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO, sampleBO.getDetailFacet() ).length );
        assertEquals( 2,
            PropertyCodegenUtils.getAlternateFacets( sampleBO, sampleBO.getCustomFacet( "Test1" ) ).length );

        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getIdFacet(), sampleBO,
            sampleBO.getSummaryFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getIdFacet(), sampleBO,
            sampleBO.getDetailFacet() ).length );
        assertEquals( 0, PropertyCodegenUtils.getAlternateFacets( sampleBO.getSummaryFacet(), sampleBO,
            sampleBO.getIdFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getSummaryFacet(), sampleBO,
            sampleBO.getSummaryFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getSummaryFacet(), sampleBO,
            sampleBO.getDetailFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getSummaryFacet(), sampleBO,
            sampleBO.getCustomFacet( "Test1" ) ).length );
        assertEquals( 0, PropertyCodegenUtils.getAlternateFacets( sampleBO.getDetailFacet(), sampleBO,
            sampleBO.getIdFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getDetailFacet(), sampleBO,
            sampleBO.getSummaryFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getDetailFacet(), sampleBO,
            sampleBO.getDetailFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getDetailFacet(), sampleBO,
            sampleBO.getCustomFacet( "Test1" ) ).length );

        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getIdFacet(), sampleCore,
            sampleCore.getSimpleFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getIdFacet(), sampleCore,
            sampleCore.getSummaryFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getIdFacet(), sampleCore,
            sampleCore.getDetailFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getSummaryFacet(), sampleCore,
            sampleCore.getSimpleFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getSummaryFacet(), sampleCore,
            sampleCore.getSummaryFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getSummaryFacet(), sampleCore,
            sampleCore.getDetailFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getDetailFacet(), sampleCore,
            sampleCore.getSimpleFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getDetailFacet(), sampleCore,
            sampleCore.getSummaryFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getDetailFacet(), sampleCore,
            sampleCore.getDetailFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getCustomFacet( "Test1" ), sampleCore,
            sampleCore.getSimpleFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getCustomFacet( "Test1" ), sampleCore,
            sampleCore.getSummaryFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleBO.getCustomFacet( "Test1" ), sampleCore,
            sampleCore.getDetailFacet() ).length );

        assertEquals( 0, PropertyCodegenUtils.getAlternateFacets( sampleChoice.getSharedFacet() ).length );
        assertEquals( 0, PropertyCodegenUtils.getAlternateFacets( sampleChoice.getChoiceFacet( "ChoiceA" ) ).length );

        assertEquals( 0, PropertyCodegenUtils.getAlternateFacets( sampleCore.getSimpleFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleCore.getSummaryFacet() ).length );
        assertEquals( 2, PropertyCodegenUtils.getAlternateFacets( sampleCore.getDetailFacet() ).length );
        assertEquals( 1, PropertyCodegenUtils.getAlternateFacets( sampleCore.getSummaryListFacet() ).length );
        assertEquals( 1, PropertyCodegenUtils.getAlternateFacets( sampleCore.getDetailListFacet() ).length );
    }

}
