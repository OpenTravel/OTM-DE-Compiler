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
package org.opentravel.schemacompiler.transform.library_01_05;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_05.ChoiceObject;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Verifies the operation of the transformers that handle conversions to and from
 * <code>TLChoiceObject</code> objects.
 *
 * @author S. Livezey
 */
public class TestChoiceObjectTransformers extends Abstract_1_5_TestTransformers {
	
    @Test
    public void testCoreObjectTransformer() throws Exception {
        TLChoiceObject type = getChoiceObject(PACKAGE_2_NAMESPACE, "library_1_p2", "SampleChoice");

        assertNotNull(type);
        assertEquals("SampleChoice", type.getName());
        assertEquals(2, type.getAliases().size());
        assertTrue(type.getSharedFacet().declaresContent());
        assertEquals(3, type.getSharedFacet().getMemberFields().size());
        assertNotNull(type.getSharedFacet().getAttribute("sharedAttribute"));
        assertNotNull(type.getSharedFacet().getElement("sharedElement"));
        assertNotNull(type.getSharedFacet().getIndicator("sharedIndicator"));
    }

    private TLChoiceObject getChoiceObject(String namespace, String libraryName, String typeName)
            throws Exception {
        TLLibrary library = getLibrary(namespace, libraryName);

        return (library == null) ? null : library.getChoiceObjectType(typeName);
    }

    private ChoiceObject transformChoiceObject(String namespace, String libraryName, String typeName)
            throws Exception {
    	TLChoiceObject origType = getChoiceObject(namespace, libraryName, typeName);
        TransformerFactory<SymbolResolverTransformerContext> factory = TransformerFactory
                .getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                        getContextJAXBTransformation(origType.getOwningLibrary()));
        ObjectTransformer<TLChoiceObject, ChoiceObject, SymbolResolverTransformerContext> transformer =
        		factory.getTransformer(origType, ChoiceObject.class);

        return transformer.transform(origType);
    }

}
