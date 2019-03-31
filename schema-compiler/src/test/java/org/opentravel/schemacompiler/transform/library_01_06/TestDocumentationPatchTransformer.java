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

package org.opentravel.schemacompiler.transform.library_01_06;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_06.DocumentationPatch;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLDocumentationPatch;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Verifies the operation of the transformers that handle conversions to and from <code>TLExtensionPointFacet</code>
 * objects.
 * 
 * @author S. Livezey
 */
public class TestDocumentationPatchTransformer extends Abstract_1_6_TestTransformers {

    @Test
    public void testDocumentationPatchTransformer() throws Exception {
        TLDocumentationPatch patch =
            getDocumentationPatch( PACKAGE_EXT_NAMESPACE, "library_3_ext", "2.0.0/ExampleBusinessObject" );

        assertNotNull( patch );
        assertNotNull( patch.getDocumentation() );
        assertEquals( "2.0.0", patch.getPatchedVersion() );
        assertEquals( "ExampleBusinessObject", patch.getDocPath() );
    }

    @Test
    public void testTLDocumentationPatchTransformer() throws Exception {
        DocumentationPatch patch =
            transformDocumentationPatch( PACKAGE_EXT_NAMESPACE, "library_3_ext", "2.0.0/ExampleBusinessObject" );

        assertNotNull( patch );
        assertNotNull( patch.getDocumentation() );
        assertEquals( "2.0.0", patch.getPatchedVersion() );
        assertEquals( "ExampleBusinessObject", patch.getDocPath() );
    }

    private TLDocumentationPatch getDocumentationPatch(String namespace, String libraryName, String docPatchName)
        throws Exception {
        TLLibrary library = getLibrary( namespace, libraryName );

        return (library == null) ? null : library.getDocumentationPatchType( docPatchName );
    }

    private DocumentationPatch transformDocumentationPatch(String namespace, String libraryName, String docPatchName)
        throws Exception {
        TLDocumentationPatch origDocPatch = getDocumentationPatch( namespace, libraryName, docPatchName );
        TransformerFactory<SymbolResolverTransformerContext> factory =
            TransformerFactory.getInstance( SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                getContextJAXBTransformation( origDocPatch.getOwningLibrary() ) );
        ObjectTransformer<TLDocumentationPatch,DocumentationPatch,SymbolResolverTransformerContext> transformer =
            factory.getTransformer( origDocPatch, DocumentationPatch.class );

        return transformer.transform( origDocPatch );
    }

}
