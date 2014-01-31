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

/**
 * Handles the resolution of qualified entity names into reference to the objects described by those
 * names.
 * 
 * @author S. Livezey
 */
public interface SymbolResolver {

    /**
     * Resolves the given entity name into an object reference. The entity name may either be a
     * simple name (that assumes the local namespace), or a prefixed name.
     * 
     * @param typename
     *            the entity name to resolve
     * @return Object
     */
    public Object resolveEntity(String entityName);

    /**
     * Resolves the given entity name into an object reference. The entity name may either be a
     * simple name (that assumes the local namespace), or a prefixed name.
     * 
     * @param typename
     *            the entity name to resolve
     * @return Object
     */
    public Object resolveOperationEntity(String entityName);

    /**
     * Resolves the given prefix and local entity name into an object reference.
     * 
     * @param prefix
     *            the prefix that denotes the namespace of the entity
     * @param localName
     *            the local name of the entity (independent of namespace)
     * @return Object
     */
    public Object resolvePrefixedEntity(String prefix, String localName);

    /**
     * Resolves the given namespace and local entity name into an object reference.
     * 
     * @param namespace
     *            the namespace of the entity
     * @param localName
     *            the local name of the entity (independent of namespace)
     * @return Object
     */
    public Object resolveQualifiedEntity(String namespace, String localName);

    /**
     * If the given entity is resolvable, this method returns its assigned namespace. If the entity
     * is not resolvable, null will be returned.
     * 
     * @param entity
     *            the entity whose namespace to retrieve
     * @return String
     */
    public String getEntityNamespace(Object entity);

    /**
     * Constructs an entity name using the qualified naming elements provided. The resulting name
     * will conform to the pattern "&lt;prefix&gt;:&lt;local-name&gt;". If the namespace is not
     * available within the current context (or it matches the local context's assigned namespace),
     * the prefix will be omitted from the name that is returned.
     * 
     * <p>
     * NOTE: An entity with the specified qualified name is not required to exist for this method to
     * succeed.
     * 
     * @param namespace
     *            the namespace that will be used to identify the entity's prefix
     * @param localName
     *            the local name of the entity
     * @return String
     */
    public String buildEntityName(String namespace, String localName);

    /**
     * Assigns a new prefix resolver for this symbol resolver component.
     * 
     * @param prefixResolver
     *            the prefix resolver instance
     */
    public void setPrefixResolver(PrefixResolver prefixResolver);

    /**
     * Assigns the filter to employ when resolving entities from an anonymous namespace.
     * 
     * @param filter
     *            the filter instance to assign
     */
    public void setAnonymousEntityFilter(AnonymousEntityFilter filter);

}
