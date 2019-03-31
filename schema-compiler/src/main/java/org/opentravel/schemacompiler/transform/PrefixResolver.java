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
 * Handles the resolution of type prefixes into the namespaces to which they refer.
 * 
 * @author S. Livezey
 */
public interface PrefixResolver {

    /**
     * Returns the namespace to use for locally-defined types whose names do not include a prefix.
     * 
     * @return String
     */
    public String getLocalNamespace();

    /**
     * Returns the namespace associated with the specified prefix or null if a mapping for the prefix is not defined.
     * 
     * @param prefix the prefix for which to return an associated namespace
     * @return String
     */
    public String resolveNamespaceFromPrefix(String prefix);

    /**
     * Returns the prefix used to identify the specified namespace within this resolver's context. If the namespace is
     * not defined, this method will return null. If the namespace is the local namespace, an empty string will be
     * returned.
     * 
     * @param namespace the namespace for which to lookup a prefix
     * @return String
     */
    public String getPrefixForNamespace(String namespace);

}
