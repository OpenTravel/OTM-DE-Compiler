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
package org.opentravel.schemacompiler.transform.jaxb16_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_06.CoreObject;
import org.opentravel.ns.ota2.librarymodel_v01_06.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_06.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_06.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_06.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_06.Role;
import org.opentravel.ns.ota2.librarymodel_v01_06.SimpleFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>CoreObject</code> type to the
 * <code>TLCoreObject</code> type.
 * 
 * @author S. Livezey
 */
public class CoreObjectTransformer extends
        BaseTransformer<CoreObject, TLCoreObject, DefaultTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLCoreObject transform(CoreObject source) {
        ObjectTransformer<SimpleFacet, TLSimpleFacet, DefaultTransformerContext> simpleFacetTransformer = getTransformerFactory()
                .getTransformer(SimpleFacet.class, TLSimpleFacet.class);
        ObjectTransformer<Facet, TLFacet, DefaultTransformerContext> facetTransformer = getTransformerFactory()
                .getTransformer(Facet.class, TLFacet.class);
        ObjectTransformer<Equivalent, TLEquivalent, DefaultTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(Equivalent.class, TLEquivalent.class);
        final TLCoreObject coreObject = new TLCoreObject();

        coreObject.setName(trimString(source.getName()));
        coreObject.setNotExtendable((source.isNotExtendable() == null) ? false : source
                .isNotExtendable());

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation, TLDocumentation, DefaultTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(Documentation.class, TLDocumentation.class);

            coreObject.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }

        if (source.getExtension() != null) {
            ObjectTransformer<Extension, TLExtension, DefaultTransformerContext> extensionTransformer = getTransformerFactory()
                    .getTransformer(Extension.class, TLExtension.class);

            coreObject.setExtension(extensionTransformer.transform(source.getExtension()));
        }

        for (Equivalent sourceEquiv : source.getEquivalent()) {
            coreObject.addEquivalent(equivTransformer.transform(sourceEquiv));
        }

        if (source.getRoles() != null) {
            ObjectTransformer<Role, TLRole, DefaultTransformerContext> roleTransformer = getTransformerFactory()
                    .getTransformer(Role.class, TLRole.class);

            for (Role sourceRole : source.getRoles().getRole()) {
                coreObject.getRoleEnumeration().addRole(roleTransformer.transform(sourceRole));
            }
        }

        for (String aliasName : trimStrings(source.getAliases())) {
            TLAlias alias = new TLAlias();

            alias.setName(aliasName);
            coreObject.addAlias(alias);
        }

        if (source.getSimple() != null) {
            coreObject.setSimpleFacet(simpleFacetTransformer.transform(source.getSimple()));
        }
        if (source.getSummary() != null) {
            coreObject.setSummaryFacet(facetTransformer.transform(source.getSummary()));
        }
        if (source.getDetail() != null) {
            coreObject.setDetailFacet(facetTransformer.transform(source.getDetail()));
        }

        return coreObject;
    }

}
