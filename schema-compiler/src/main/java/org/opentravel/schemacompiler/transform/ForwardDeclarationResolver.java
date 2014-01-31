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
 * Base class to be extended by components that can resolve and assign forward-declared objects.
 * 
 * @author S. Livezey
 */
public abstract class ForwardDeclarationResolver {

    private ForwardDeclaration forwardDeclaration;

    /**
     * Constructor that assigns the forward declaration to be resolved.
     * 
     * @param forwardDeclaration
     *            the forward declaration to be resolved
     */
    public ForwardDeclarationResolver(ForwardDeclaration forwardDeclaration) {
        this.forwardDeclaration = forwardDeclaration;
    }

    /**
     * Resolves the entity represented by the forward declaration associated with this resolver
     * instance. This method will return true if the forward declaration was resolved successfully,
     * false otherwise.
     * 
     * @param symbolTable
     *            the symbol table from which the forward-declared entity can be resolved
     * @return boolean
     */
    public boolean resolveForwardDeclaration(SymbolTable symbolTable) {
        return resolveForwardDeclaration(forwardDeclaration, symbolTable);
    }

    /**
     * Resolves the entity represented by the specified forward declaration. This method will return
     * true if the forward declaration was resolved successfully, false otherwise.
     * 
     * @param forwardDeclaration
     *            the forward declaration to resolve
     * @param symbolTable
     *            the symbol table from which the forward-declared entity can be resolved
     * @return boolean
     */
    protected abstract boolean resolveForwardDeclaration(ForwardDeclaration forwardDeclaration,
            SymbolTable symbolTable);

}
