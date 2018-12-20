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
package org.opentravel.schemacompiler.transform.jaxb2xsd;

import java.util.Set;

import org.opentravel.schemacompiler.loader.LibraryModuleImport;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;
import org.opentravel.schemacompiler.transform.util.SchemaUtils;
import org.opentravel.schemacompiler.version.XSDVersionScheme;
import org.w3._2001.xmlschema.OpenAttrs;
import org.w3._2001.xmlschema.Schema;

/**
 * Handles the transformation of objects from the <code>Schema</code> type to the
 * <code>XSDLibrary</code> type.
 * 
 * @author S. Livezey
 */
public class SchemaTransformer extends
        BaseTransformer<Schema, XSDLibrary, DefaultTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public XSDLibrary transform(Schema source) {
        XSDLibrary target = new XSDLibrary();

        target.setVersionScheme(XSDVersionScheme.ID);
        target.setNamespace(source.getTargetNamespace());
        target.setPrefix(source.getId()); // prefix is stored in the ID field by the
                                          // LibrarySchema1_3_ModuleLoader

        for (String _include : SchemaUtils.getSchemaIncludes(source)) {
            TLInclude include = new TLInclude();

            include.setPath(_include);
            target.addInclude(include);
        }
        for (LibraryModuleImport nsImport : SchemaUtils.getSchemaImports(source)) {
            String[] fileHints = null;

            if (nsImport.getFileHints() != null) {
                fileHints = nsImport.getFileHints().toArray(
                        new String[nsImport.getFileHints().size()]);
            }
            target.addNamespaceImport(trimString(nsImport.getPrefix()),
                    trimString(nsImport.getNamespace()), fileHints);
        }

        for (OpenAttrs sourceMember : source.getSimpleTypeOrComplexTypeOrGroup()) {
            Set<Class<?>> targetTypes = getTransformerFactory().findTargetTypes(sourceMember);
            Class<LibraryMember> targetType = (Class<LibraryMember>)
            		(targetTypes.isEmpty() ? null : targetTypes.iterator().next());

            if (targetType != null) {
                ObjectTransformer<OpenAttrs, LibraryMember, DefaultTransformerContext> memberTransformer = getTransformerFactory()
                        .getTransformer(sourceMember, targetType);

                if (memberTransformer != null) {
                    target.addNamedMember(memberTransformer.transform(sourceMember));
                }
            }
        }
        return target;
    }

}
