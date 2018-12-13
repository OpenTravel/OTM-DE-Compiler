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
package org.opentravel.schemacompiler.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple simple table implementation that associates named entities within a namespace and the
 * objects that are referenced by those names.
 * 
 * <p>
 * NOTE: For OTM models, symbol names for service operations are considered a special case since
 * they are not eligible for type assignments as model attributes or properties. For this reason,
 * operation objects are maintained in a separate "symbol space" from the other symbols maintained
 * in the table.
 * 
 * @author S. Livezey
 */
public final class SymbolTable {

    private static final String OPERATION_SYMBOL_PREFIX = "OP:";

    private Map<String,Map<String, Object>> namespaceSymbols = new HashMap<>();
    private List<DerivedEntityFactory<Object>> derivedEntityFactories = new ArrayList<>();
    private Map<String,List<Object>> anonymousEntities = new HashMap<>();

    /**
     * Returns the entity with the specified name or null if such an entity has not been defined.
     * 
     * @param namespace
     *            the namespace of the entity to retrieve
     * @param localName
     *            the local name of the entity to retrieve (independent of namespace)
     * @return Object
     */
    public Object getEntity(String namespace, String localName) {
        Object entity = null;

        // Trim whitespace before checking the symbol table
        namespace = (namespace == null) ? null : namespace.trim();
        localName = (localName == null) ? null : localName.trim();

        if ((namespace == null)
                || AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE.equals(namespace)) {
            List<Object> entityList = anonymousEntities.get(localName);

            if ((entityList != null) && !entityList.isEmpty()) {
                // If there are duplicate entities assigned to the same name, we will just
                // pick the first one
                entity = entityList.get(0);
            }
        } else {
            Map<String, Object> localSymbols = namespaceSymbols.get(namespace);

            if (localSymbols != null) {
                entity = localSymbols.get(localName);
            }
        }
        return entity;
    }

    /**
     * Returns the operation entity with the specified name or null if such an entity has not been
     * defined.
     * 
     * @param namespace
     *            the namespace of the operation entity to retrieve
     * @param localName
     *            the local name of the operation entity to retrieve (independent of namespace)
     * @return Object
     */
    public Object getOperationEntity(String namespace, String localName) {
        return getEntity(namespace, ((localName == null) ? OPERATION_SYMBOL_PREFIX
                : OPERATION_SYMBOL_PREFIX + localName.trim()));
    }

    /**
     * Returns the namespace assigned to the given entity or null if a namespace has not been
     * assigned.
     * 
     * @param entity
     *            the entity for which to retrieve the namespace assignment
     * @return String
     */
    public String getNamespaceForEntity(Object entity) {
        String namespace = null;

        for (String ns : namespaceSymbols.keySet()) {
            Map<String, Object> localSymbols = namespaceSymbols.get(ns);

            if (localSymbols.containsValue(entity)) {
                namespace = ns;
                break;
            }
        }

        // If we couldn't find the entity in the list of named entities, check the anonymous
        // entities for a match
        if (namespace == null) {
            for (List<Object> entityList : anonymousEntities.values()) {
                if (entityList.contains(entity)) {
                    namespace = AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE;
                    break;
                }
            }
        }
        return namespace;
    }

    /**
     * Returns all namespaces that have associated entities in this symbol table.
     * 
     * @return Collection<String>
     */
    public Collection<String> getNamespaces() {
        return Collections.unmodifiableSet(new HashSet<String>(namespaceSymbols.keySet()));
    }

    /**
     * Returns the local names associated with the specified namespace in this symbol table. If the
     * namespace is not recognized, an empty collection will be returned.
     * 
     * @param namespace
     *            the namespace for which to return local names
     * @return Collection<String>
     */
    public Collection<String> getLocalNames(String namespace) {
        Set<String> localNames = new HashSet<>();

        if ((namespace == null)
                || AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE.equals(namespace)) {
            localNames.addAll(anonymousEntities.keySet());

        } else {
            Map<String, Object> localSymbols = namespaceSymbols.get(namespace);

            if (localSymbols != null) {
                localNames.addAll(localSymbols.keySet());
            }
        }
        return Collections.unmodifiableSet(localNames);
    }

    /**
     * Adds an entry for the given entity to this symbol table. If an entity with the specified name
     * has already been defined, the name association will be replaced with the given entity.
     * 
     * @param namespace
     *            the namespace of the entity to add
     * @param localName
     *            the local name of the entity to add (independent of namespace)
     * @param entity
     *            the entity to add
     */
    public void addEntity(String namespace, String localName, Object entity) {
        // Trim whitespace before adding to the symbol table
        namespace = (namespace == null) ? null : namespace.trim();
        localName = (localName == null) ? null : localName.trim();

        if ((namespace == null)
                || AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE.equals(namespace)) {
            // Add the entity to the collection of anonymous (no-namespace) names
            anonymousEntities.computeIfAbsent( localName, n -> anonymousEntities.put( n, new ArrayList<>() ) );
            anonymousEntities.get(localName).add( entity );

        } else {
            // Add the entity to the symbol table maps
            namespaceSymbols.computeIfAbsent( namespace, ns -> namespaceSymbols.put( ns, new HashMap<>() ) );
            namespaceSymbols.get(namespace).put( localName, entity );
        }

        // Search for any entities that are derived from the concrete entity we just registered
        for (DerivedEntityFactory<Object> factory : derivedEntityFactories) {
            if (factory.isOriginatingEntity(entity)) {
                factory.registerDerivedEntity(entity, namespace, this);
            }
        }
    }

    /**
     * Adds an operation entry for the given entity to this symbol table. If an entity with the
     * specified name has already been defined, the name association will be replaced with the given
     * entity.
     * 
     * @param namespace
     *            the namespace of the operation entity to add
     * @param localName
     *            the local name of the operation entity to add (independent of namespace)
     * @param entity
     *            the operation entity to add
     */
    public void addOperationEntity(String namespace, String localName, Object entity) {
        String opLocalName = ((localName == null) ? OPERATION_SYMBOL_PREFIX
                : OPERATION_SYMBOL_PREFIX + localName.trim());

        addEntity(namespace, opLocalName, entity);
    }

    /**
     * Registers the given derived entity factory to the list maintained by this symbol table.
     * 
     * @param factory
     *            the derived entity factory to register
     */
    @SuppressWarnings("unchecked")
    public void addDerivedEntityFactory(DerivedEntityFactory<?> factory) {
        if (factory != null) {
            derivedEntityFactories.add((DerivedEntityFactory<Object>) factory);
        }
    }

    /**
     * Displays the contents of this symbol table to standard output (debugging purposes only).
     */
    @SuppressWarnings("squid:S106") // Suppress Sonar finding since this method is used for console debugging purposes
    public void displayTable() {
        System.out.println("Symbol Table:");
        
        for (String ns : namespaceSymbols.keySet()) {
            Map<String, Object> localNameMap = namespaceSymbols.get(ns);
            System.out.println("  " + ns);

            for (String localName : localNameMap.keySet()) {
                Object obj = localNameMap.get(localName);

                System.out.println("    " + localName + " -> " + obj.getClass().getSimpleName());
            }
        }
    }
}
