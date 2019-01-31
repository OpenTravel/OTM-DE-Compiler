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

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.validate.AbstractValidatorTest;

/**
 * Verifies the functions of the <code>TLContextualFacetCompileValidator</code> class.
 */
public class TestTLContextualFacetCompileValidator extends AbstractValidatorTest {
    
    @Test
    public void testMultipleIdDeclarations() throws Exception {
        TLContextualFacet facet = getMember( TLIBRARY_ERROR1_V1, "@CONTEXTUAL:TestChoice_ChoiceB", TLContextualFacet.class );
        Set<String> messageKeys = getFindingMessageKeys( facet );
        
        assertTrue( messageKeys.contains( TLFacetCompileValidator.ERROR_MULTIPLE_ID_MEMBERS ) );
    }
    
    @Test
    public void testInvalidFacetType() throws Exception {
        TLContextualFacet choiceFacet = getMember( TLIBRARY_ERROR1_V1, "@CONTEXTUAL:TestChoice_ChoiceB", TLContextualFacet.class );
        TLContextualFacet childFacet = getMember( TLIBRARY_ERROR1_V1, "@CONTEXTUAL:TestChoice_ChoiceA_Child", TLContextualFacet.class );
        TLContextualFacet boFacet = getMember( TLIBRARY_ERROR1_V1, "@CONTEXTUAL:TestBusinessObject_Query_TestQuery", TLContextualFacet.class );
        Set<String> messageKeys;
        
        try {
            choiceFacet.setFacetType( TLFacetType.QUERY );
            childFacet.setFacetType( TLFacetType.UPDATE );
            boFacet.setFacetType( TLFacetType.CHOICE );
            
            messageKeys = getFindingMessageKeys( validateModelElement( choiceFacet ), choiceFacet );
            assertTrue( messageKeys.contains( TLContextualFacetCompileValidator.ERROR_INVALID_FACET_TYPE ) );
            
            messageKeys = getFindingMessageKeys( validateModelElement( childFacet ), childFacet );
            assertTrue( messageKeys.contains( TLContextualFacetCompileValidator.ERROR_INVALID_FACET_TYPE ) );
            
            messageKeys = getFindingMessageKeys( validateModelElement( boFacet ), boFacet );
            assertTrue( messageKeys.contains( TLContextualFacetCompileValidator.ERROR_INVALID_FACET_TYPE ) );
            
        } finally {
            choiceFacet.setFacetType( TLFacetType.CHOICE );
            childFacet.setFacetType( TLFacetType.CHOICE );
            boFacet.setFacetType( TLFacetType.QUERY );
        }
    }
    
    @Test
    public void testCircularFacetReference() throws Exception {
        TLContextualFacet choiceFacet = getMember( TLIBRARY_ERROR1_V1, "@CONTEXTUAL:TestChoice_ChoiceA", TLContextualFacet.class );
        TLContextualFacet childFacet = getMember( TLIBRARY_ERROR1_V1, "@CONTEXTUAL:TestChoice_ChoiceA_Child", TLContextualFacet.class );
        TLFacetOwner origOwner = choiceFacet.getOwningEntity();
        Set<String> messageKeys;
        
        try {
            choiceFacet.setOwningEntity( childFacet ); // Create a circular reference between parent and child
            
            messageKeys = getFindingMessageKeys( validateModelElement( choiceFacet ), choiceFacet );
            assertTrue( messageKeys.contains( TLContextualFacetCompileValidator.ERROR_INVALID_CIRCULAR_REFERENCE ) );
            
            messageKeys = getFindingMessageKeys( validateModelElement( childFacet ), childFacet );
            assertTrue( messageKeys.contains( TLContextualFacetCompileValidator.ERROR_INVALID_CIRCULAR_REFERENCE ) );
            
        } finally {
            choiceFacet.setOwningEntity( origOwner );
        }
    }
    
}
