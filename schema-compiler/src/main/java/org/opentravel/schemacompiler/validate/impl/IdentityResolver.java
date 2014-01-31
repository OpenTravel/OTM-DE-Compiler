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
package org.opentravel.schemacompiler.validate.impl;

/**
 * Resolves the identity name of an object during validation, typically for the purpose of
 * identifying duplicate model entities during validation.
 * 
 * @param <E>
 *            the entity type for which names will be resolved
 * @author S. Livezey
 */
public interface IdentityResolver<E> {

    /**
     * Resolves the identity name of the given entity.
     * 
     * @param entity
     *            the entity for which to return an identity
     * @return String
     */
    public String getIdentity(E entity);

}
