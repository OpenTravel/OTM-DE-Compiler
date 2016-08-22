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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opentravel.ns.ota2.librarymodel_v01_06.Folder;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLFolder;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;

/**
 * Verifies the operation of the transformers that handle conversions to and from
 * <code>TLLibrary</code> objects.
 */
public class TestFolderTransformers extends Abstract_1_6_TestTransformers {
	
    @Test
    public void testFolderTransformer() throws Exception {
    	List<TLFolder> folders = getFolders(PACKAGE_2_NAMESPACE, "library_1_p2");
    	
    	assertEquals(2, folders.size());
    	assertEquals(2, folders.get(0).getFolders().size());
    	assertEquals(0, folders.get(1).getFolders().size());
    	
        TLFolder folder1 = folders.get(0);
        TLFolder folder11 = folders.get(0).getFolders().get(0);
        TLFolder folder12 = folders.get(0).getFolders().get(1);
        TLFolder folder2 = folders.get(1);
        
        assertEquals("Folder1", folder1.getName());
        assertEquals(2, folder1.getEntities().size());
        assertEquals("EmptyBusinessObject", folder1.getEntities().get(0).getLocalName());
        assertEquals("SampleBusinessObject", folder1.getEntities().get(1).getLocalName());
        
        assertEquals("Folder1-1", folder11.getName());
        assertEquals(0, folder11.getEntities().size());
        
        assertEquals("Folder1-2", folder12.getName());
        assertEquals(1, folder12.getEntities().size());
        assertEquals("CompoundBusinessObject", folder12.getEntities().get(0).getLocalName());
        
        assertEquals("Folder2", folder2.getName());
        assertEquals(1, folder2.getEntities().size());
        assertEquals("SampleCore", folder2.getEntities().get(0).getLocalName());
    }
    
    @Test
    public void testTLFolderTransformer() throws Exception {
    	List<Folder> folders = transformFolders(PACKAGE_2_NAMESPACE, "library_1_p2");
    	
    	assertEquals(2, folders.size());
    	assertEquals(2, folders.get(0).getFolder().size());
    	assertEquals(0, folders.get(1).getFolder().size());
    	
        Folder folder1 = folders.get(0);
        Folder folder11 = folders.get(0).getFolder().get(0);
        Folder folder12 = folders.get(0).getFolder().get(1);
        Folder folder2 = folders.get(1);
        
        assertEquals("Folder1", folder1.getName());
        assertEquals(2, folder1.getFolderItem().size());
        assertEquals("EmptyBusinessObject", folder1.getFolderItem().get(0).getValue());
        assertEquals("SampleBusinessObject", folder1.getFolderItem().get(1).getValue());
        
        assertEquals("Folder1-1", folder11.getName());
        assertEquals(0, folder11.getFolderItem().size());
        
        assertEquals("Folder1-2", folder12.getName());
        assertEquals(1, folder12.getFolderItem().size());
        assertEquals("CompoundBusinessObject", folder12.getFolderItem().get(0).getValue());
        
        assertEquals("Folder2", folder2.getName());
        assertEquals(1, folder2.getFolderItem().size());
        assertEquals("SampleCore", folder2.getFolderItem().get(0).getValue());
    }
    
    private List<TLFolder> getFolders(String namespace, String libraryName) throws Exception {
        TLLibrary library = getLibrary(namespace, libraryName);
        
        return (library == null) ? null : library.getFolders();
    }

    private List<Folder> transformFolders(String namespace, String libraryName) throws Exception {
    	List<TLFolder> folders = getFolders(namespace, libraryName);
        TransformerFactory<SymbolResolverTransformerContext> factory = TransformerFactory
                .getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY,
                        getContextJAXBTransformation(folders.get(0).getOwningLibrary()));
        ObjectTransformer<TLFolder, Folder, SymbolResolverTransformerContext> transformer =
        		factory.getTransformer(TLFolder.class, Folder.class);
        List<Folder> jaxbFolders = new ArrayList<>();
        
        for (TLFolder folder : folders) {
            jaxbFolders.add(transformer.transform(folder));
        }
        return jaxbFolders;
    }

}
