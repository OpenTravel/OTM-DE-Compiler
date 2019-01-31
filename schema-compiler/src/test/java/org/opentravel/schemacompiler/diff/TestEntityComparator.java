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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;
import org.opentravel.schemacompiler.diff.impl.EntityComparator;
import org.opentravel.schemacompiler.diff.impl.EntityComparisonFacade;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.util.ModelComparator;

/**
 * Verifies the operation of the <code>EntityComparator</code> class and its associated
 * helper classes.
 */
public class TestEntityComparator extends AbstractDiffTest {
    
    @Test
    public void testFullLibraryComparison() throws Exception {
        TLLibrary oldLibrary = getLibrary( TLIBRARY_DIFF1_V1 );
        TLLibrary newLibrary = getLibrary( TLIBRARY_DIFF1_V2 );
        
        ModelComparator comparator = new ModelComparator( ModelCompareOptions.getDefaultOptions() );
        LibraryChangeSet changeSet;
        
        changeSet = comparator.compareLibraries( oldLibrary, newLibrary );
        comparator.compareLibraries( oldLibrary, newLibrary, new ByteArrayOutputStream() );
        assertNotNull( changeSet );
        assertTrue( changeSet.getChangeItems().size() > 0 );
        
        changeSet = comparator.compareLibraries( newLibrary, oldLibrary );
        comparator.compareLibraries( newLibrary, oldLibrary, new ByteArrayOutputStream() );
        assertNotNull( changeSet );
        assertTrue( changeSet.getChangeItems().size() > 0 );
    }
    
    @Test
    public void testSimpleTypeConstraintsChange() throws Exception {
        NamedEntity oldEntity = getMember( TLIBRARY_DIFF1_V1, "TestSimple", NamedEntity.class );
        NamedEntity newEntity = getMember( TLIBRARY_DIFF1_V2, "TestSimple", NamedEntity.class );
        NamedEntity oldEntityList = getMember( TLIBRARY_DIFF1_V1, "TestSimpleList", NamedEntity.class );
        NamedEntity newEntityList = getMember( TLIBRARY_DIFF1_V2, "TestSimpleList", NamedEntity.class );
        Set<EntityChangeType> listChangeTypes1 = getChangeTypes( oldEntityList, newEntityList );
        Set<EntityChangeType> listChangeTypes2 = getChangeTypes( newEntityList, oldEntityList );
        Set<EntityChangeType> changeTypes = getChangeTypes( oldEntity, newEntity );
        
        assertTrue( changeTypes.contains( EntityChangeType.PATTERN_CONSTRAINT_CHANGED ) );
        assertTrue( changeTypes.contains( EntityChangeType.MIN_LENGTH_CONSTRAINT_CHANGED ) );
        assertTrue( changeTypes.contains( EntityChangeType.MAX_LENGTH_CONSTRAINT_CHANGED ) );
        assertTrue( changeTypes.contains( EntityChangeType.FRACTION_DIGITS_CONSTRAINT_CHANGED ) );
        assertTrue( changeTypes.contains( EntityChangeType.TOTAL_DIGITS_CONSTRAINT_CHANGED ) );
        assertTrue( changeTypes.contains( EntityChangeType.MIN_INCLUSIVE_CONSTRAINT_CHANGED ) );
        assertTrue( changeTypes.contains( EntityChangeType.MAX_INCLUSIVE_CONSTRAINT_CHANGED ) );
        assertTrue( changeTypes.contains( EntityChangeType.MIN_EXCLUSIVE_CONSTRAINT_CHANGED ) );
        assertTrue( changeTypes.contains( EntityChangeType.MAX_EXCLUSIVE_CONSTRAINT_CHANGED ) );
        assertTrue( listChangeTypes1.contains( EntityChangeType.CHANGED_TO_SIMPLE_LIST ) );
        assertTrue( listChangeTypes2.contains( EntityChangeType.CHANGED_TO_SIMPLE_NON_LIST ) );
    }
    
    @Test
    public void testEntityTypeChange() throws Exception {
        NamedEntity oldEntity = getMember( TLIBRARY_DIFF1_V1, "TestSimple", NamedEntity.class );
        NamedEntity newEntity = getMember( TLIBRARY_DIFF1_V2, "TestSimpleList", NamedEntity.class );
        Set<EntityChangeType> changeTypes = getChangeTypes( oldEntity, newEntity );
        
        assertTrue( changeTypes.contains( EntityChangeType.NAME_CHANGED ) );
    }
    
    @Test
    public void testEntityNameChange() throws Exception {
        NamedEntity oldEntity = getMember( TLIBRARY_DIFF1_V1, "TestSimple", NamedEntity.class );
        NamedEntity newEntity = getMember( TLIBRARY_DIFF1_V2, "TestTypeChange", NamedEntity.class );
        Set<EntityChangeType> changeTypes = getChangeTypes( oldEntity, newEntity );
        
        assertTrue( changeTypes.contains( EntityChangeType.ENTITY_TYPE_CHANGED ) );
    }
    
    @Test
    public void testEntityDocumentationChange() throws Exception {
        NamedEntity oldEntity = getMember( TLIBRARY_DIFF1_V1, "TestTypeChange", NamedEntity.class );
        NamedEntity newEntity = getMember( TLIBRARY_DIFF1_V2, "TestTypeChange", NamedEntity.class );
        Set<EntityChangeType> changeTypes = getChangeTypes( oldEntity, newEntity );
        
        assertTrue( changeTypes.contains( EntityChangeType.DOCUMENTATION_CHANGED ) );
    }
    
    @Test
    public void testEntityExtensionChange() throws Exception {
        NamedEntity oldEntity = getMember( TLIBRARY_DIFF1_V1, "BaseCore", NamedEntity.class );
        NamedEntity newEntity = getMember( TLIBRARY_DIFF1_V1_1, "BaseCore", NamedEntity.class );
        Set<EntityChangeType> changeTypes = getChangeTypes( oldEntity, newEntity );
        
        assertTrue( changeTypes.contains( EntityChangeType.EXTENSION_CHANGED ) );
    }
    
    @Test
    public void testSimpleFacetChange() throws Exception {
        NamedEntity oldEntity = getMember( TLIBRARY_DIFF1_V1, "BaseCore", NamedEntity.class );
        NamedEntity newEntity = getMember( TLIBRARY_DIFF1_V1_1, "BaseCore", NamedEntity.class );
        Set<EntityChangeType> changeTypes = getChangeTypes( oldEntity, newEntity );
        
        assertTrue( changeTypes.contains( EntityChangeType.SIMPLE_CORE_TYPE_CHANGED ) );
    }
    
    @Test
    public void testActionFacetChanges() throws Exception {
        TLResource oldResource = getMember( TLIBRARY_DIFF1_V1, "TestResource1", TLResource.class );
        TLResource newResource = getMember( TLIBRARY_DIFF1_V2, "TestResource2", TLResource.class );
        NamedEntity oldEntity = oldResource.getActionFacet( "ObjectWrapper" );
        NamedEntity newEntity = newResource.getActionFacet( "ObjectWrapper" );
        Set<EntityChangeType> changeTypes = getChangeTypes( oldEntity, newEntity );
        
        assertTrue( changeTypes.contains( EntityChangeType.BASE_PAYLOAD_CHANGED ) );
        assertTrue( changeTypes.contains( EntityChangeType.REFERENCE_TYPE_CHANGED ) );
        assertTrue( changeTypes.contains( EntityChangeType.REFERENCE_FACET_CHANGED ) );
        assertTrue( changeTypes.contains( EntityChangeType.REFERENCE_REPEAT_CHANGED ) );
    }
    
    @Test
    public void testFieldChanges() throws Exception {
        NamedEntity oldEntity = getMember( TLIBRARY_DIFF1_V1, "TestBusinessObject", NamedEntity.class );
        NamedEntity newEntity = getMember( TLIBRARY_DIFF1_V2, "TestBusinessObject", NamedEntity.class );
        Set<EntityChangeType> changeTypes = getChangeTypes( oldEntity, newEntity );
        
        assertTrue( changeTypes.contains( EntityChangeType.MEMBER_FIELD_ADDED ) );
        assertTrue( changeTypes.contains( EntityChangeType.MEMBER_FIELD_DELETED ) );
        assertTrue( changeTypes.contains( EntityChangeType.MEMBER_FIELD_ADDED ) );
    }
    
    private Set<EntityChangeType> getChangeTypes(NamedEntity oldEntity, NamedEntity newEntity) {
        Set<EntityChangeType> changeTypes = new HashSet<>();
        EntityChangeSet changeSet;
        
        if (oldEntity.getNamespace().equals( newEntity.getNamespace() )) {
            changeSet = new ModelComparator().compareEntities( oldEntity, newEntity );
            
        } else {
            ModelCompareOptions options = ModelCompareOptions.getDefaultOptions();
            Map<String,String> nsMap = new HashMap<>();
            
            options.setSuppressFieldVersionChanges( true );
            nsMap.put( oldEntity.getNamespace(), newEntity.getNamespace() );
            changeSet = new EntityComparator( options, nsMap )
                    .compareEntities( new EntityComparisonFacade( oldEntity ), new EntityComparisonFacade( newEntity ) );
        }
        
        changeSet.getChangeItems().forEach( ci -> changeTypes.add( ci.getChangeType() ) );
        return changeTypes;
    }
    
}
