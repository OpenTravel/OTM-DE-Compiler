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
package org.opentravel.schemacompiler.diff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.opentravel.schemacompiler.diff.impl.DisplayFormatter;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Validates the functions of the <code>DisplayFormatter</code> class.
 */
@RunWith( MockitoJUnitRunner.class )
public class TestDisplayFormatter extends AbstractDiffTest {
    
    private static URL LIBRARY_URL = URLUtils.toURL( new File(
            SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-diff/test-library.xml" ) );
    private static URL HISTORY_URL = URLUtils.toURL( "http://www.mock-repository.org/services/test-library.xml/historical-content?commit=0" );
    
    @Mock private RepositoryManager mockRepositoryManager;
    @Mock private RemoteRepository mockRepository;
    
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks( this );
    }
    
    @Test
    public void testLibraryViewDetailsUrl() throws Exception {
        TLLibrary library = getLibrary( TLIBRARY_DIFF1_V1 );
        String normalUrl, historicalUrl;
        
        // Unmanaged library should have no URL
        assertEquals( "", new DisplayFormatter().getLibraryViewDetailsUrl( library ) );
        
        // Should have the same URL for the library, regardless of whether a historical URL is provided
        mockRepositoryItem( library, false );
        normalUrl = new DisplayFormatter( mockRepositoryManager ).getLibraryViewDetailsUrl( library );
        
        mockRepositoryItem( library, true );
        historicalUrl = new DisplayFormatter( mockRepositoryManager ).getLibraryViewDetailsUrl( library );
        assertNotEquals( "", normalUrl );
        assertEquals( normalUrl, historicalUrl );
    }
    
    @Test
    public void testEntityViewDetailsUrl() throws Exception {
        NamedEntity entity = getMember( TLIBRARY_DIFF1_V1, "TestSimple", NamedEntity.class );
        TLLibrary library = (TLLibrary) entity.getOwningLibrary();
        String normalUrl, historicalUrl;
        
        // Unmanaged library should have no URL
        assertEquals( "", new DisplayFormatter().getEntityViewDetailsUrl( entity ) );
        
        // Should have the same URL for the library, regardless of whether a historical URL is provided
        mockRepositoryItem( library, false );
        normalUrl = new DisplayFormatter( mockRepositoryManager ).getEntityViewDetailsUrl( entity );
        
        mockRepositoryItem( library, true );
        historicalUrl = new DisplayFormatter( mockRepositoryManager ).getEntityViewDetailsUrl( entity );
        assertNotEquals( "", normalUrl );
        assertEquals( normalUrl, historicalUrl );
    }
    
    @Test
    public void testLibraryDisplayInfo() throws Exception {
        TLLibrary library = getLibrary( TLIBRARY_DIFF1_V1 );
        
        library.setLibraryUrl( LIBRARY_URL );
        
        assertNull( new DisplayFormatter().getLibraryDisplayName( null ) );
        assertEquals( "d1v1:library_diff1", new DisplayFormatter().getLibraryDisplayName( library ) );
        assertEquals( "test-library.xml", new DisplayFormatter().getLibraryFilename( library ) );
        assertEquals( "Under Review", new DisplayFormatter().getLibraryStatusDisplayName( TLLibraryStatus.UNDER_REVIEW ) );
    }
    
    @Test
    public void testEntityDisplayInfo() throws Exception {
        NamedEntity entity = getMember( TLIBRARY_DIFF1_V1, "TestSimple", NamedEntity.class );
        TLOperation op = new TLOperation();
        
        op.setName( "TestOperation" );
        
        assertNull( new DisplayFormatter().getEntityDisplayName( null ) );
        assertEquals( "d1v1:TestSimple", new DisplayFormatter().getEntityDisplayName( entity ) );
        assertEquals( "TestOperation", new DisplayFormatter().getEntityDisplayName( op ) );
        assertEquals( "Simple", new DisplayFormatter().getEntityTypeDisplayName( entity.getClass() ) );
    }
    
    @Test
    public void testFieldName() throws Exception {
        TLMemberField<?> simpleField = getMember( TLIBRARY_DIFF1_V1, "TestBusinessObject|@FACET:SUMMARY|modifiedAttr", TLMemberField.class );
        TLMemberField<?> complexField = getMember( TLIBRARY_DIFF1_V2, "TestBusinessObject|@FACET:SUMMARY|BaseCore", TLMemberField.class );
        
        assertEquals( "modifiedAttr", new DisplayFormatter().getFieldName( simpleField ) );
        assertEquals( "d1v1:BaseCore", new DisplayFormatter().getFieldName( complexField ) );
    }
    
    @Test
    public void testResourceDisplayInfo() throws Exception {
        TLResource resource = getMember( TLIBRARY_DIFF1_V1, "TestResource1", TLResource.class );
        TLResourceParentRef parentRef = resource.getParentRef( "ParentResource/IDParameters" );
        TLParamGroup paramGroup = resource.getParamGroup( "IDParameters" );
        TLParameter parameter = paramGroup.getParameter( "sample_oid" );
        TLAction action = resource.getAction( "Create" );
        TLActionResponse response = action.getResponses().get( 0 );
        
        assertEquals( "ParentResource / IDParameters", new DisplayFormatter().getParentRefDisplayName( parentRef ) );
        assertEquals( "IDParameters", new DisplayFormatter().getParamGroupDisplayName( paramGroup ) );
        assertEquals( "IDParameters - sample_oid", new DisplayFormatter().getParameterDisplayName( parameter ) );
        assertEquals( "Create", new DisplayFormatter().getActionDisplayName( action ) );
        assertEquals( "Create [200, 204]", new DisplayFormatter().getActionResponseDisplayName( response ) );
    }
    
    private RepositoryItem mockRepositoryItem(TLLibrary library, boolean useHistoryUrl) throws Exception {
        RepositoryItemImpl item = new RepositoryItemImpl();
        
        item.setRepository( mockRepository );
        item.setBaseNamespace( library.getBaseNamespace() );
        item.setNamespace( library.getNamespace() );
        item.setLibraryName( library.getName() );
        item.setFilename( library.getName() + "_" + library.getVersion().replace( '.', '_' ) + ".otm" );
        item.setVersion( library.getVersion() );
        item.setVersionScheme( library.getVersionScheme() );
        item.setStatus( library.getStatus() );
        item.setState( RepositoryItemState.MANAGED_UNLOCKED );
        
        if (useHistoryUrl) {
            library.setLibraryUrl( HISTORY_URL );
            when( mockRepositoryManager.getRepositoryItem(
                    item.getBaseNamespace(), ProjectManager.getPublicationFilename( library ),
                    item.getVersion() ) ).thenReturn( item );
            
        } else {
            library.setLibraryUrl( LIBRARY_URL );
            when( mockRepositoryManager.getRepositoryItem(
                    URLUtils.toFile( library.getLibraryUrl() ) ) ).thenReturn( item );
        }
        when( mockRepository.getEndpointUrl() ).thenReturn( "http://www.mock-repository.org" );
        return item;
    }
    
}
