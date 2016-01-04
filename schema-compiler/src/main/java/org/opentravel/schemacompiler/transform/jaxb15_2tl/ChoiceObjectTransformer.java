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
package org.opentravel.schemacompiler.transform.jaxb15_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_05.ChoiceObject;
import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_05.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_05.Facet;
import org.opentravel.ns.ota2.librarymodel_v01_05.FacetContextual;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>ChoiceObject</code> type to the
 * <code>TLChoiceObject</code> type.
 *
 * @author S. Livezey
 */
public class ChoiceObjectTransformer extends
		ComplexTypeTransformer<ChoiceObject, TLChoiceObject> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLChoiceObject transform(ChoiceObject source) {
        ObjectTransformer<Facet, TLFacet, DefaultTransformerContext> facetTransformer = getTransformerFactory()
                .getTransformer(Facet.class, TLFacet.class);
        ObjectTransformer<FacetContextual, TLFacet, DefaultTransformerContext> facetContextualTransformer = getTransformerFactory()
                .getTransformer(FacetContextual.class, TLFacet.class);
        ObjectTransformer<Equivalent, TLEquivalent, DefaultTransformerContext> equivTransformer = getTransformerFactory()
                .getTransformer(Equivalent.class, TLEquivalent.class);
        TLChoiceObject choiceObject = new TLChoiceObject();

        choiceObject.setName(trimString(source.getName()));
        choiceObject.setNotExtendable((source.isNotExtendable() == null) ? false : source.isNotExtendable());

        if (source.getDocumentation() != null) {
            ObjectTransformer<Documentation, TLDocumentation, DefaultTransformerContext> docTransformer = getTransformerFactory()
                    .getTransformer(Documentation.class, TLDocumentation.class);

            choiceObject.setDocumentation(docTransformer.transform(source.getDocumentation()));
        }

        if (source.getExtension() != null) {
            ObjectTransformer<Extension, TLExtension, DefaultTransformerContext> extensionTransformer = getTransformerFactory()
                    .getTransformer(Extension.class, TLExtension.class);

            choiceObject.setExtension(extensionTransformer.transform(source.getExtension()));
        }

        for (Equivalent sourceEquiv : source.getEquivalent()) {
        	choiceObject.addEquivalent(equivTransformer.transform(sourceEquiv));
        }

        for (String aliasName : trimStrings(source.getAliases())) {
            TLAlias alias = new TLAlias();

            alias.setName(aliasName);
            choiceObject.addAlias(alias);
        }

        if (source.getShared() != null) {
        	choiceObject.setSharedFacet(facetTransformer.transform(source.getShared()));
        }

        if (source.getChoice() != null) {
            for (FacetContextual sourceFacet : source.getChoice()) {
            	choiceObject.addChoiceFacet(facetContextualTransformer.transform(sourceFacet));
            }
        }

        return choiceObject;
	}
	
}
