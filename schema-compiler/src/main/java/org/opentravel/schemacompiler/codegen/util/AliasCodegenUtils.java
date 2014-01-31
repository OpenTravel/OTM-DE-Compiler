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
package org.opentravel.schemacompiler.codegen.util;

import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLListFacet;

/**
 * Static utility methods used during the generation of code output for aliases.
 * 
 * @author S. Livezey
 */
public class AliasCodegenUtils {

    /**
     * Returns the corresponding alias from the source factet's owner.
     * 
     * @param facetAlias
     *            the source facet alias
     * @return TLAlias
     */
    public static TLAlias getOwnerAlias(TLAlias facetAlias) {
        TLAlias ownerAlias = null;

        // If the alias is for a list facet, find the corresponding alias on its item facet
        if (facetAlias.getOwningEntity() instanceof TLListFacet) {
            TLListFacet listFacet = (TLListFacet) facetAlias.getOwningEntity();
            String itemFacetAliasName = facetAlias.getName();

            if (itemFacetAliasName.endsWith("_List")) {
                // Truncate the "_List" from the name to obtain the item facet alias name
                itemFacetAliasName = itemFacetAliasName.substring(0,
                        itemFacetAliasName.length() - 5);
            }
            facetAlias = ((TLFacet) listFacet.getItemFacet()).getAlias(itemFacetAliasName);
        }

        // Locate the corresponding alias on the facet owner
        if (facetAlias.getOwningEntity() instanceof TLFacet) {
            TLFacet sourceFacet = (TLFacet) facetAlias.getOwningEntity();
            TLAliasOwner owner = (TLAliasOwner) sourceFacet.getOwningEntity();
            String aliasSuffix = "_"
                    + sourceFacet.getFacetType().getIdentityName(sourceFacet.getContext(),
                            sourceFacet.getLabel());

            if (facetAlias.getName().endsWith(aliasSuffix)) {
                if (owner instanceof TLAliasOwner) {
                    for (TLAlias candidateAlias : owner.getAliases()) {
                        String derivedAlias = candidateAlias.getName() + aliasSuffix;

                        if (facetAlias.getName().equals(derivedAlias)) {
                            ownerAlias = candidateAlias;
                            break;
                        }
                    }

                    // If a corresponding alias could not be located on the owner, we must assume
                    // this
                    // this to be an inherited alias; therefore we must return a ghost
                    if (ownerAlias == null) {
                        ownerAlias = new TLAlias();
                        ownerAlias.setName(facetAlias.getName().replace(aliasSuffix, ""));
                        ownerAlias.setOwningEntity(owner);
                    }
                }
            }
        }
        return ownerAlias;
    }

    /**
     * Returns the corresponding alias from the sibling facet of the specified type. A "sibling"
     * facet is a facet with the same facet owner as the source facet.
     * 
     * @param facetAlias
     *            the source facet alias
     * @param siblingFacetType
     *            the type of facet from which the sibling alias should be retrieved
     * @return TLAlias
     */
    public static TLAlias getSiblingAlias(TLAlias facetAlias, TLFacetType siblingFacetType) {
        TLAlias siblingAlias = null;

        if (facetAlias.getOwningEntity() instanceof TLFacet) {
            TLFacet sourceFacet = (TLFacet) facetAlias.getOwningEntity();
            String aliasSuffix = "_"
                    + sourceFacet.getFacetType().getIdentityName(sourceFacet.getContext(),
                            sourceFacet.getLabel());

            if (facetAlias.getName().endsWith(aliasSuffix)) {
                String aliasPrefix = facetAlias.getName().replace(aliasSuffix, "");
                TLFacet siblingFacet = FacetCodegenUtils.getFacetOfType(
                        sourceFacet.getOwningEntity(), siblingFacetType, sourceFacet.getContext(),
                        sourceFacet.getLabel());

                // First, find the sibling facet; if one cannot be located, create a ghost facet
                if (siblingFacet == null) {
                    siblingFacet = new TLFacet();
                    siblingFacet.setOwningEntity(sourceFacet.getOwningEntity());
                    siblingFacet.setFacetType(siblingFacetType);
                    siblingFacet.setContext(sourceFacet.getContext());
                    siblingFacet.setLabel(sourceFacet.getLabel());
                }

                // Next, find the corresponding alias on the sibling facet; if one cannot be
                // located,
                // create a ghost alias (yes, this means we can have ghost aliases for ghost facets)
                String siblingSuffix = "_"
                        + siblingFacet.getFacetType().getIdentityName(sourceFacet.getContext(),
                                sourceFacet.getLabel());
                String derivedAlias = aliasPrefix + siblingSuffix;

                for (TLAlias candidateAlias : siblingFacet.getAliases()) {
                    if (candidateAlias.getName().equals(derivedAlias)) {
                        siblingAlias = candidateAlias;
                        break;
                    }
                }

                // If a corresponding alias could not be located on the sibling facet, we must
                // assume this
                // this to be an inherited alias; therefore we must return a ghost
                if (siblingAlias == null) {
                    siblingAlias = new TLAlias();
                    siblingAlias.setName(derivedAlias);
                    siblingAlias.setOwningEntity(siblingFacet);
                }
            }
        }
        return siblingAlias;
    }

    /**
     * Returns the corresponding alias from the facet of the specified type.
     * 
     * @param facetAlias
     *            the source facet alias
     * @param facetType
     *            the type of facet from which the alias should be retrieved
     * @return TLAlias
     */
    public static TLAlias getFacetAlias(TLAlias ownerAlias, TLFacetType facetType) {
        return getFacetAlias(ownerAlias, facetType, null, null);
    }

    /**
     * Returns the corresponding alias from the facet of the specified type.
     * 
     * @param ownerAlias
     *            the alias of the facet owner
     * @param facetType
     *            the type of facet from which the alias should be retrieved
     * @param facetContext
     *            the context of the facet from which the alias should be retrieved
     * @param facetLabel
     *            the label of the facet from which the alias should be retrieved
     * @return TLAlias
     */
    public static TLAlias getFacetAlias(TLAlias ownerAlias, TLFacetType facetType,
            String facetContext, String facetLabel) {
        TLAlias facetAlias = null;

        if (ownerAlias.getOwningEntity() instanceof TLFacetOwner) {
            TLFacet facet = FacetCodegenUtils.getFacetOfType(
                    (TLFacetOwner) ownerAlias.getOwningEntity(), facetType, facetContext,
                    facetLabel);
            String derivedAlias = ownerAlias.getName() + "_"
                    + facetType.getIdentityName(facetContext, facetLabel);

            for (TLAlias alias : facet.getAliases()) {
                if (alias.getName().equals(derivedAlias)) {
                    facetAlias = alias;
                    break;
                }
            }
        }
        return facetAlias;
    }

}
