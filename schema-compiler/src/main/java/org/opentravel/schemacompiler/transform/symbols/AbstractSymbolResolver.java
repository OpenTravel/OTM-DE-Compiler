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

package org.opentravel.schemacompiler.transform.symbols;

import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.opentravel.schemacompiler.transform.PrefixResolver;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.transform.SymbolTable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract base class for symbol resolvers.
 * 
 * @author S. Livezey
 */
public abstract class AbstractSymbolResolver implements SymbolResolver {

    protected static final Pattern prefixedNamePattern =
        Pattern.compile( "([A-Za-z0-9\\.\\-_]+):([A-Za-z0-9\\.\\-_#]+)" );

    protected PrefixResolver prefixResolver = null;
    private AnonymousEntityFilter anonymousEntityFilter = null;

    /**
     * @see org.opentravel.schemacompiler.transform.SymbolResolver#setPrefixResolver(org.opentravel.schemacompiler.transform.PrefixResolver)
     */
    @Override
    public void setPrefixResolver(PrefixResolver prefixResolver) {
        this.prefixResolver = prefixResolver;
    }

    /**
     * @see org.opentravel.schemacompiler.transform.SymbolResolver#setAnonymousEntityFilter(org.opentravel.schemacompiler.transform.AnonymousEntityFilter)
     */
    @Override
    public void setAnonymousEntityFilter(AnonymousEntityFilter filter) {
        this.anonymousEntityFilter = filter;
    }

    /**
     * @see org.opentravel.schemacompiler.transform.SymbolResolver#resolveEntity(java.lang.String)
     */
    @Override
    public Object resolveEntity(String entityName) {
        Object entity = null;

        if (entityName != null) {
            String[] nameComponents = parseEntityName( entityName );
            String prefix = nameComponents[0];
            String localName = nameComponents[1];

            entity = resolvePrefixedEntity( prefix, localName );
        }
        return entity;
    }

    /**
     * @see org.opentravel.schemacompiler.transform.SymbolResolver#resolveOperationEntity(java.lang.String)
     */
    @Override
    public Object resolveOperationEntity(String entityName) {
        Object entity = null;

        if (entityName != null) {
            String[] nameComponents = parseEntityName( entityName );
            String prefix = nameComponents[0];
            String namespace = (prefix == null) ? prefixResolver.getLocalNamespace()
                : prefixResolver.resolveNamespaceFromPrefix( prefix );
            String localName = nameComponents[1];

            entity = resolveQualifiedOperationEntity( namespace, localName );
        }
        return entity;
    }

    /**
     * Resolves the given namespace and local entity name into an operation object reference.
     * 
     * @param namespace the namespace of the operation entity
     * @param localName the local name of the operation entity (independent of namespace)
     * @return Object
     */
    protected abstract Object resolveQualifiedOperationEntity(String namespace, String localName);

    /**
     * @see org.opentravel.schemacompiler.transform.SymbolResolver#resolvePrefixedEntity(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public Object resolvePrefixedEntity(String prefix, String localName) {
        Object entity = null;

        if (prefixResolver != null) {
            String namespace = (prefix == null) ? prefixResolver.getLocalNamespace()
                : prefixResolver.resolveNamespaceFromPrefix( prefix );

            if (namespace != null) {
                entity = resolveQualifiedEntity( namespace, localName );
            }
        }
        return entity;
    }

    /**
     * Attempts to resolve an anonymous entity with the specified local name using the current
     * <code>AnonymousEntityFilter</code>.
     * 
     * @param localName the local name of the entity to resolve
     * @param symbols the symbol table from which to resolve the anonymous entity
     * @return Object
     */
    protected Object resolveAnonymousEntity(String localName, SymbolTable symbols) {
        Object entity = null;

        if (anonymousEntityFilter != null) {
            entity = anonymousEntityFilter.getAnonymousEntity( localName, symbols );
        }
        return entity;
    }

    /**
     * @see org.opentravel.schemacompiler.transform.SymbolResolver#buildEntityName(java.lang.String, java.lang.String)
     */
    @Override
    public String buildEntityName(String namespace, String localName) {
        String entityName = null;

        if (localName != null) {
            String prefix = (prefixResolver == null) ? null : prefixResolver.getPrefixForNamespace( namespace );

            if ((prefix == null) || (prefix.length() == 0)) {
                entityName = localName;
            } else {
                entityName = prefix + ':' + localName;
            }
        }
        return entityName;
    }

    /**
     * Parses an entity name of the format "[prefix:]localname". The first element of the string array that is returned
     * will be the prefix (or null if a prefix is not specified). The second element will always contain the local name
     * component of the entity's name.
     * 
     * @param entityName the entity name to parse
     * @return String[]
     */
    public static String[] parseEntityName(String entityName) {
        String[] nameComponents = new String[2];

        if (entityName != null) {
            Matcher m = prefixedNamePattern.matcher( entityName.trim() );

            if (m.matches()) {
                nameComponents[0] = m.group( 1 );
                nameComponents[1] = m.group( 2 );
            } else {
                nameComponents[1] = entityName;
            }
        }
        return nameComponents;
    }

}
