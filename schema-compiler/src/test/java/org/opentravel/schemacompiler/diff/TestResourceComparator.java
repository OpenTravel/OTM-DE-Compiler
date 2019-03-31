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
import org.opentravel.schemacompiler.diff.impl.ResourceComparator;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.util.ModelComparator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Verifies the operation of the <code>ResourceComparator</code> class and its associated helper classes.
 */
public class TestResourceComparator extends AbstractDiffTest {

    @Test
    public void testResourcePropertyChanges() throws Exception {
        TLResource oldResource = getMember( TLIBRARY_DIFF1_V1, "TestResource1", TLResource.class );
        TLResource newResource = getMember( TLIBRARY_DIFF1_V2, "TestResource2", TLResource.class );
        Set<ResourceChangeType> changeTypes = getChangeTypes( oldResource, newResource );

        assertTrue( changeTypes.contains( ResourceChangeType.NAME_CHANGED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.BASE_PATH_CHANGED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.ABSTRACT_IND_CHANGED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.FIRST_CLASS_IND_CHANGED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.EXTENSION_CHANGED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.DOCUMENTATION_CHANGED ) );
    }

    @Test
    public void testBusinessObjectChange() throws Exception {
        TLResource oldResource = getMember( TLIBRARY_DIFF1_V1, "TestResource1", TLResource.class );
        TLResource newResource = getMember( TLIBRARY_DIFF1_V2, "TestResource2", TLResource.class );
        Set<ResourceChangeType> changeTypes = getChangeTypes( oldResource, newResource );

        assertTrue( changeTypes.contains( ResourceChangeType.BUSINESS_OBJECT_REF_CHANGED ) );
    }

    @Test
    public void testParentRefChanges() throws Exception {
        TLResource oldResource = getMember( TLIBRARY_DIFF1_V1, "TestResource1", TLResource.class );
        TLResource newResource = getMember( TLIBRARY_DIFF1_V2, "TestResource2", TLResource.class );
        Set<ResourceChangeType> changeTypes = getChangeTypes( oldResource, newResource );

        assertTrue( changeTypes.contains( ResourceChangeType.PARENT_REF_ADDED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.PARENT_REF_DELETED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.PARENT_REF_CHANGED ) );
    }

    @Test
    public void testParamGroup() throws Exception {
        TLResource oldResource = getMember( TLIBRARY_DIFF1_V1, "TestResource1", TLResource.class );
        TLResource newResource = getMember( TLIBRARY_DIFF1_V2, "TestResource2", TLResource.class );
        Set<ResourceChangeType> changeTypes = getChangeTypes( oldResource, newResource );

        assertTrue( changeTypes.contains( ResourceChangeType.PARAM_GROUP_ADDED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.PARAM_GROUP_DELETED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.PARAM_GROUP_CHANGED ) );
    }

    @Test
    public void testActionFacetChanges() throws Exception {
        TLResource oldResource = getMember( TLIBRARY_DIFF1_V1, "TestResource1", TLResource.class );
        TLResource newResource = getMember( TLIBRARY_DIFF1_V2, "TestResource2", TLResource.class );
        Set<ResourceChangeType> changeTypes = getChangeTypes( oldResource, newResource );

        assertTrue( changeTypes.contains( ResourceChangeType.ACTION_FACET_ADDED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.ACTION_FACET_DELETED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.ACTION_FACET_CHANGED ) );
    }

    @Test
    public void testActionChanges() throws Exception {
        TLResource oldResource = getMember( TLIBRARY_DIFF1_V1, "TestResource1", TLResource.class );
        TLResource newResource = getMember( TLIBRARY_DIFF1_V2, "TestResource2", TLResource.class );
        Set<ResourceChangeType> changeTypes = getChangeTypes( oldResource, newResource );

        assertTrue( changeTypes.contains( ResourceChangeType.ACTION_ADDED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.ACTION_DELETED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.ACTION_CHANGED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.COMMON_ACTION_IND_CHANGED ) );
    }

    @Test
    public void testActionRequestChanges() throws Exception {
        TLResource oldResource = getMember( TLIBRARY_DIFF1_V1, "TestResource1", TLResource.class );
        TLResource newResource = getMember( TLIBRARY_DIFF1_V2, "TestResource2", TLResource.class );
        Set<ResourceChangeType> changeTypes = getChangeTypes( oldResource, newResource );

        assertTrue( changeTypes.contains( ResourceChangeType.REQUEST_METHOD_CHANGED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.REQUEST_PARAM_GROUP_CHANGED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.REQUEST_PAYLOAD_TYPE_CHANGED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.REQUEST_MIME_TYPE_ADDED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.REQUEST_MIME_TYPE_DELETED ) );
    }

    @Test
    public void testActionResponseChanges() throws Exception {
        TLResource oldResource = getMember( TLIBRARY_DIFF1_V1, "TestResource1", TLResource.class );
        TLResource newResource = getMember( TLIBRARY_DIFF1_V2, "TestResource2", TLResource.class );
        Set<ResourceChangeType> changeTypes = getChangeTypes( oldResource, newResource );

        assertTrue( changeTypes.contains( ResourceChangeType.RESPONSE_ADDED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.RESPONSE_DELETED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.RESPONSE_CHANGED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.RESPONSE_PAYLOAD_TYPE_CHANGED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.RESPONSE_MIME_TYPE_ADDED ) );
        assertTrue( changeTypes.contains( ResourceChangeType.RESPONSE_MIME_TYPE_DELETED ) );
    }

    private Set<ResourceChangeType> getChangeTypes(TLResource oldResource, TLResource newResource) {
        Set<ResourceChangeType> changeTypes = new HashSet<>();
        ResourceChangeSet changeSet;

        if (oldResource.getNamespace().equals( newResource.getNamespace() )) {
            changeSet = new ModelComparator().compareResources( oldResource, newResource );

        } else {
            ModelCompareOptions options = ModelCompareOptions.getDefaultOptions();
            Map<String,String> nsMap = new HashMap<>();

            options.setSuppressFieldVersionChanges( true );
            nsMap.put( oldResource.getNamespace(), newResource.getNamespace() );
            changeSet = new ResourceComparator( options, nsMap ).compareResources( oldResource, newResource );
        }

        addChangeItems( changeSet.getChangeItems(), changeTypes );
        return changeTypes;
    }

    private void addChangeItems(List<ResourceChangeItem> changeItems, Set<ResourceChangeType> changeTypes) {
        for (ResourceChangeItem changeItem : changeItems) {
            changeTypes.add( changeItem.getChangeType() );

            if (changeItem.getModifiedParentRef() != null) {
                addChangeItems( changeItem.getModifiedParentRef().getChangeItems(), changeTypes );
            }
            if (changeItem.getModifiedParamGroup() != null) {
                addChangeItems( changeItem.getModifiedParamGroup().getChangeItems(), changeTypes );
            }
            if (changeItem.getModifiedParam() != null) {
                addChangeItems( changeItem.getModifiedParam().getChangeItems(), changeTypes );
            }
            if (changeItem.getModifiedAction() != null) {
                addChangeItems( changeItem.getModifiedAction().getChangeItems(), changeTypes );
            }
            if (changeItem.getModifiedActionResponse() != null) {
                addChangeItems( changeItem.getModifiedActionResponse().getChangeItems(), changeTypes );
            }
        }
    }

}
