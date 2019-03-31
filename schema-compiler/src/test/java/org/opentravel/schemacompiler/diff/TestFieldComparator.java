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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.schemacompiler.diff.impl.FieldComparator;
import org.opentravel.schemacompiler.diff.impl.FieldComparisonFacade;
import org.opentravel.schemacompiler.model.TLMemberField;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Verifies the operation of the <code>FieldComparator</code> class and its associated helper classes.
 */
public class TestFieldComparator extends AbstractDiffTest {

    @Test
    public void testFieldTypeChanged() throws Exception {
        TLMemberField<?> oldField =
            getMember( TLIBRARY_DIFF1_V1, "TestBusinessObject|@FACET:SUMMARY|modifiedAttr", TLMemberField.class );
        TLMemberField<?> newField =
            getMember( TLIBRARY_DIFF1_V2, "TestBusinessObject|@FACET:SUMMARY|modifiedAttr", TLMemberField.class );
        Set<FieldChangeType> changeTypes = getChangeTypes( oldField, newField );

        // changeTypes.forEach( c -> System.out.println( c ) );
        assertTrue( changeTypes.contains( FieldChangeType.TYPE_CHANGED ) );
    }

    @Test
    public void testFieldMemberTypeChanged() throws Exception {
        TLMemberField<?> oldField =
            getMember( TLIBRARY_DIFF1_V1, "TestBusinessObject|@FACET:SUMMARY|fieldTypeChange", TLMemberField.class );
        TLMemberField<?> newField =
            getMember( TLIBRARY_DIFF1_V2, "TestBusinessObject|@FACET:SUMMARY|fieldTypeChange", TLMemberField.class );
        Set<FieldChangeType> changeTypes = getChangeTypes( oldField, newField );

        assertTrue( changeTypes.contains( FieldChangeType.MEMBER_TYPE_CHANGED ) );
    }

    @Test
    public void testFieldFacetChanged() throws Exception {
        TLMemberField<?> oldField =
            getMember( TLIBRARY_DIFF1_V1, "TestBusinessObject|@FACET:SUMMARY|facetChange", TLMemberField.class );
        TLMemberField<?> newField =
            getMember( TLIBRARY_DIFF1_V2, "TestBusinessObject|@FACET:DETAIL|facetChange", TLMemberField.class );
        Set<FieldChangeType> changeTypes = getChangeTypes( oldField, newField );

        assertTrue( changeTypes.contains( FieldChangeType.OWNING_FACET_CHANGED ) );
    }

    @Test
    public void testFieldCardinalityChanged() throws Exception {
        TLMemberField<?> oldField =
            getMember( TLIBRARY_DIFF1_V1, "TestBusinessObject|@FACET:SUMMARY|CardinalityChange", TLMemberField.class );
        TLMemberField<?> newField =
            getMember( TLIBRARY_DIFF1_V2, "TestBusinessObject|@FACET:SUMMARY|CardinalityChange", TLMemberField.class );
        Set<FieldChangeType> changeTypes = getChangeTypes( oldField, newField );

        assertTrue( changeTypes.contains( FieldChangeType.CARDINALITY_CHANGE ) );
    }

    @Test
    public void testFieldOptionalityChanged() throws Exception {
        TLMemberField<?> oldField =
            getMember( TLIBRARY_DIFF1_V1, "TestBusinessObject|@FACET:SUMMARY|OptionalityChange", TLMemberField.class );
        TLMemberField<?> newField =
            getMember( TLIBRARY_DIFF1_V2, "TestBusinessObject|@FACET:SUMMARY|OptionalityChange", TLMemberField.class );
        Set<FieldChangeType> changeTypes1 = getChangeTypes( oldField, newField );
        Set<FieldChangeType> changeTypes2 = getChangeTypes( newField, oldField );

        assertTrue( changeTypes1.contains( FieldChangeType.CHANGED_TO_MANDATORY ) );
        assertTrue( changeTypes2.contains( FieldChangeType.CHANGED_TO_OPTIONAL ) );
    }

    @Test
    public void testFieldReferenceIndChanged() throws Exception {
        TLMemberField<?> oldField =
            getMember( TLIBRARY_DIFF1_V1, "TestBusinessObject|@FACET:SUMMARY|ReferenceChange", TLMemberField.class );
        TLMemberField<?> newField =
            getMember( TLIBRARY_DIFF1_V2, "TestBusinessObject|@FACET:SUMMARY|ReferenceChange", TLMemberField.class );
        Set<FieldChangeType> changeTypes1 = getChangeTypes( oldField, newField );
        Set<FieldChangeType> changeTypes2 = getChangeTypes( newField, oldField );

        assertTrue( changeTypes1.contains( FieldChangeType.CHANGED_TO_REFERENCE ) );
        assertTrue( changeTypes2.contains( FieldChangeType.CHANGED_TO_NON_REFERENCE ) );
    }

    @Test
    public void testExamplesAndEquivalentsChanged() throws Exception {
        TLMemberField<?> oldField = getMember( TLIBRARY_DIFF1_V1,
            "TestBusinessObject|@FACET:SUMMARY|ExampleAndEquivalentChange", TLMemberField.class );
        TLMemberField<?> newField = getMember( TLIBRARY_DIFF1_V2,
            "TestBusinessObject|@FACET:SUMMARY|ExampleAndEquivalentChange", TLMemberField.class );
        Set<FieldChangeType> changeTypes = getChangeTypes( oldField, newField );

        assertTrue( changeTypes.contains( FieldChangeType.EXAMPLE_ADDED ) );
        assertTrue( changeTypes.contains( FieldChangeType.EXAMPLE_DELETED ) );
        assertTrue( changeTypes.contains( FieldChangeType.EQUIVALENT_ADDED ) );
        assertTrue( changeTypes.contains( FieldChangeType.EQUIVALENT_DELETED ) );
    }

    @Test
    public void testDocumentationChanged() throws Exception {
        TLMemberField<?> oldField = getMember( TLIBRARY_DIFF1_V1,
            "TestBusinessObject|@FACET:SUMMARY|DocumentationChange", TLMemberField.class );
        TLMemberField<?> newField = getMember( TLIBRARY_DIFF1_V2,
            "TestBusinessObject|@FACET:SUMMARY|DocumentationChange", TLMemberField.class );
        Set<FieldChangeType> changeTypes = getChangeTypes( oldField, newField );

        assertTrue( changeTypes.contains( FieldChangeType.DOCUMENTATION_CHANGED ) );
    }

    private Set<FieldChangeType> getChangeTypes(TLMemberField<?> oldField, TLMemberField<?> newField) {
        ModelCompareOptions options = ModelCompareOptions.getDefaultOptions();
        Map<String,String> nsMap = new HashMap<>();
        Set<FieldChangeType> changeTypes = new HashSet<>();
        FieldChangeSet changeSet;

        if (!oldField.getOwningLibrary().getNamespace().equals( newField.getOwningLibrary().getNamespace() )) {
            options.setSuppressFieldVersionChanges( true );
            nsMap.put( oldField.getOwningLibrary().getNamespace(), newField.getOwningLibrary().getNamespace() );
        }
        changeSet = new FieldComparator( options, nsMap ).compareFields( new FieldComparisonFacade( oldField ),
            new FieldComparisonFacade( newField ) );
        changeSet.getChangeItems().forEach( ci -> changeTypes.add( ci.getChangeType() ) );
        return changeTypes;
    }

}
