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
package org.opentravel.schemacompiler.security;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.springframework.context.ApplicationContext;

/**
 * Handler that determines whether a library can be modified by the user of an editor application.
 * The implementation of the static methods in this class is delegated to the
 * <code>LibraryAccessController</code> instance that is configured in the Spring application
 * context file. If no such access controller is specified, modify permission will be granted to the
 * user by default.
 * 
 * @author S. Livezey
 */
public final class LibrarySecurityHandler {

    private static final LibraryAccessController accessController;

    /**
     * Private contstructor to prevent instantiation of this class.
     */
    private LibrarySecurityHandler() {
    }

    /**
     * Returns true if the current user is allowed to modify the given library.
     * 
     * @param library
     *            the user-defined library
     * @return boolean
     */
    public static boolean hasModifyPermission(TLLibrary library) {
        return (accessController == null) ? true : accessController.hasModifyPermission(library);
    }

    /**
     * Assigns the set of security credentials that determine which (if any) protected namespaces
     * the user has the authority to modify.
     * 
     * @param userCredentials
     *            the user credentials that provide access to update protected namespaces
     */
    public static void setUserCredentials(ProtectedNamespaceCredentials userCredentials) {
        if (accessController != null) {
            accessController.setUserCredentials(userCredentials);
        }
    }

    /**
     * Initializes the <code>LibraryAccessController</code> from the Spring application context.
     */
    static {
        try {
            ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();

            if (appContext.containsBean(SchemaCompilerApplicationContext.LIBRARY_ACCESS_CONTROLLER)) {
                accessController = (LibraryAccessController) appContext
                        .getBean(SchemaCompilerApplicationContext.LIBRARY_ACCESS_CONTROLLER);

            } else {
                accessController = null;
            }
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
