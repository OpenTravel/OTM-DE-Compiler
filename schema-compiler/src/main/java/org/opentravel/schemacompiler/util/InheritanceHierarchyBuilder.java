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
package org.opentravel.schemacompiler.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;
import org.opentravel.schemacompiler.visitor.ModelNavigator;

/**
 * Visitor that scans a model in order to construct a map that links each member of an inheritance
 * hierarchy with a list of all hierarchy members.
 * 
 * @author S. Livezey
 */
public class InheritanceHierarchyBuilder {

    private static final TLFacetType[] ISOLATED_HIERARCHIES = new TLFacetType[] {
            TLFacetType.QUERY, TLFacetType.REQUEST, TLFacetType.RESPONSE, TLFacetType.NOTIFICATION };
    private static final List<TLFacetType> isolatedHierarchies = Arrays
            .asList(ISOLATED_HIERARCHIES);

    private TLModel model;

    /**
     * Constructor that specifies the model to be scanned for hierarchy information.
     * 
     * @param model
     *            the model instance to be scanned
     */
    public InheritanceHierarchyBuilder(TLModel model) {
        this.model = model;
    }

    /**
     * Constructs a map that associates each individual member of an inheritance hierarchy with a
     * collection that contains all members of that hierarchy tree. The list of hierarchy members
     * are sorted from the top-most hierarchy base member to the lowest (siblings are sorted in
     * alphabetical order by local name).
     * 
     * @return Map<NamedEntity,List<NamedEntity>>
     */
    public Map<NamedEntity, List<NamedEntity>> buildHierarchyInfo() {
        if (model == null) {
            throw new IllegalArgumentException(
                    "A model instance is required for the construction of inheritance hierarchy information.");
        }

        // First, collect all of the extension relationships from the model into an easily-readable
        // map
        ExtensionVisitor visitor = new ExtensionVisitor();
        ModelNavigator.navigate(model, visitor);
        Map<NamedEntity, NamedEntity> extensionMap = visitor.getExtensionMap();

        // Group each of the elements into their separate hierarchies
        Map<NamedEntity,Set<NamedEntity>> rootHierarchyMap = new HashMap<>();

        for (NamedEntity hierarchyMember : extensionMap.keySet()) {
            NamedEntity rootMember = getHierarchyRoot(hierarchyMember, extensionMap);

            if (rootMember != null) {
                Set<NamedEntity> hierarchyMembers = rootHierarchyMap.get(rootMember);

                if (hierarchyMembers == null) {
                    hierarchyMembers = new HashSet<>();
                    rootHierarchyMap.put(rootMember, hierarchyMembers);
                }
                if (!hierarchyMembers.contains(rootMember)) {
                    hierarchyMembers.add(rootMember);
                }
                hierarchyMembers.add(hierarchyMember);
            }
        }

        // Sort the hierarchies and construct the final map
        Map<NamedEntity,List<NamedEntity>> hierarchyInfo = new HashMap<>();
        Comparator<NamedEntity> hierarchyComparator = new HierarchyComparator(extensionMap);

        for (Set<NamedEntity> hierarchyMembers : rootHierarchyMap.values()) {
            List<NamedEntity> sortedMembers = new ArrayList<>(hierarchyMembers);
            Collections.sort(sortedMembers, hierarchyComparator);

            for (NamedEntity member : sortedMembers) {
                hierarchyInfo.put(member, sortedMembers);
            }
        }
        return hierarchyInfo;
    }

    /**
     * Returns the root of the inheritance hierarchy for the given member.
     * 
     * @param member
     *            the hierarchy member for which to return the root
     * @param extensionMap
     *            the pre-computed inheritance mappings
     * @return NamedEntity
     */
    private NamedEntity getHierarchyRoot(NamedEntity member,
            Map<NamedEntity, NamedEntity> extensionMap) {
        NamedEntity rootMember = member;

        if (isIsloatedHierarchyMember(member)) {
            TLAlias memberAlias = null;
            TLFacet memberFacet;

            if (member instanceof TLAlias) {
                memberAlias = (TLAlias) member;
                memberFacet = (TLFacet) memberAlias.getOwningEntity();
            } else {
                memberFacet = (TLFacet) member;
            }
            TLFacetOwner baseFacetOwner = (TLFacetOwner) extensionMap.get(memberFacet
                    .getOwningEntity());

            while (baseFacetOwner != null) {
    			String facetName = FacetCodegenUtils.getFacetName(memberFacet);
                TLFacet baseFacet = FacetCodegenUtils.getFacetOfType(baseFacetOwner,
                        memberFacet.getFacetType(), facetName);

                if (baseFacet != null) {
                    if (memberAlias != null) {
                        rootMember = baseFacet.getAlias(memberAlias.getName());
                    } else {
                        rootMember = baseFacet;
                    }
                }
                baseFacetOwner = (TLFacetOwner) extensionMap.get(baseFacetOwner);
            }
        } else {
            NamedEntity baseMember = extensionMap.get(member);

            while (baseMember != null) {
                rootMember = baseMember;
                baseMember = extensionMap.get(baseMember);
            }
        }
        return rootMember;
    }

    /**
     * Returns true if the given member is to be considered a member of an isolated inheritance
     * hierarchy. For example, query facets of business objects should not be considered
     * substitutable with the business object's ID facet, but it may be considered a substitutable
     * with query facets of the same name from an extended business object.
     * 
     * <p>
     * NOTE: Under the current modeling scheme, isloated hierarchies are guranteed to be wholly
     * composed of <code>TLFacet</code> instances or <code>TLAlias</code> objects whose owners are
     * facets.
     * 
     * @param member
     *            the hierarchy member to analyze
     */
    private boolean isIsloatedHierarchyMember(NamedEntity member) {
        boolean result = false;

        if (member instanceof TLAlias) {
            member = ((TLAlias) member).getOwningEntity();
        }
        if (member instanceof TLFacet) {
            result = isolatedHierarchies.contains(((TLFacet) member).getFacetType());
        }
        return result;
    }

    /**
     * Visitor that collects all of the extenion relationships defined within the model.
     */
    private class ExtensionVisitor extends ModelElementVisitorAdapter {

        private Map<NamedEntity,NamedEntity> extensionMap = new HashMap<>();

        /**
         * Returns the map of extension relationships collected during model navigation.
         * 
         * @return Map<NamedEntity,NamedEntity>
         */
        private Map<NamedEntity, NamedEntity> getExtensionMap() {
            return extensionMap;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
         */
        @Override
        public boolean visitExtension(TLExtension extension) {
            NamedEntity extendedEntity = (NamedEntity) extension.getOwner();
            NamedEntity baseEntity = extension.getExtendsEntity();

            if ((extendedEntity != null) && (baseEntity != null)
                    && baseEntity.getClass().equals(extendedEntity.getClass())) {

                // Add an extension entry for the core/business object
                extensionMap.put(extendedEntity, baseEntity);

                if (extendedEntity instanceof TLAliasOwner) {
                    TLAliasOwner extendedAliasOwner = (TLAliasOwner) extendedEntity;
                    TLAliasOwner baseAliasOwner = (TLAliasOwner) baseEntity;

                    // If overridden aliases exist, add them to the inheritance mappings
                    for (TLAlias extendedAlias : extendedAliasOwner.getAliases()) {
                        TLAliasOwner baseOwner = baseAliasOwner;
                        TLAlias baseAlias = null;

                        while ((baseAlias == null) && (baseOwner != null)) {
                            baseAlias = baseOwner.getAlias(extendedAlias.getName());
                            baseOwner = (TLAliasOwner) FacetCodegenUtils
                                    .getFacetOwnerExtension((TLFacetOwner) baseOwner);
                        }
                        if (baseAlias != null) {
                            extensionMap.put(extendedAlias, baseAlias);
                        }
                    }
                }
            }
            return true;
        }

        /**
         * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(org.opentravel.schemacompiler.model.TLFacet)
         */
        @Override
        public boolean visitFacet(TLFacet facet) {
            NamedEntity facetOwner = facet.getOwningEntity();

            if (facetOwner != null) {
                extensionMap.put(facet, facetOwner);

                for (TLAlias facetAlias : facet.getAliases()) {
                    TLAlias ownerAlias = AliasCodegenUtils.getOwnerAlias(facetAlias);

                    if (ownerAlias != null) {
                        extensionMap.put(facetAlias, ownerAlias);
                    }
                }
            }
            return true;
        }

    }

    /**
     * Comparator used to sort members of an inheritance hierarchy.
     */
    private class HierarchyComparator implements Comparator<NamedEntity> {

        private Map<NamedEntity,NamedEntity> extensionMap = new HashMap<>();

        /**
         * Constructor that supplies the extension relationships to use when comparing members of an
         * inheritance hierarchy.
         * 
         * @param extensionMap
         *            the extension mapping information to use for entity comparisons
         */
        public HierarchyComparator(Map<NamedEntity, NamedEntity> extensionMap) {
            this.extensionMap = extensionMap;
        }

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(NamedEntity member1, NamedEntity member2) {
            Integer member1Level = (member1 == null) ? -1 : getHierarchyLevel(member1);
            Integer member2Level = (member2 == null) ? -1 : getHierarchyLevel(member2);
            int result;

            if (member1Level.equals(member2Level)) {
                String member1Name = (member1 == null) ? "" : member1.getLocalName();
                String member2Name = (member2 == null) ? "" : member2.getLocalName();

                result = member1Name.compareTo(member2Name);
            } else {
                return member1Level.compareTo(member2Level);
            }
            return result;
        }

        /**
         * Returns the depth of the member within its inheritance hierarchy.
         * 
         * @param member
         *            the member for which to return the hierarchy depth
         * @return int
         */
        private int getHierarchyLevel(NamedEntity member) {
            int level = 0;

            while (member != null) {
                member = extensionMap.get(member);
                level++;
            }
            return level;
        }

    }

}
