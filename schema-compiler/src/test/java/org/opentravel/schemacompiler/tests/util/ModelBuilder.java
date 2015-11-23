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
package org.opentravel.schemacompiler.tests.util;

import org.opentravel.schemacompiler.ic.ModelIntegrityChecker;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

public class ModelBuilder {

    private String name;
    private String namespace;
    private TLModel model;

    public static ModelBuilder create() {
        ModelBuilder mb = new ModelBuilder();
        // to attach include and import dependency listener
        ProjectManager pm = new ProjectManager();
        mb.model = pm.getModel();
        mb.model.addListener(new ModelIntegrityChecker());
        return mb;
    }

    public ModelBuilder newLibrary(String name, String namespace, String version) {
        this.name = name;
        try {
            this.namespace = VersionSchemeFactory.getInstance()
                    .getVersionScheme(VersionSchemeFactory.getInstance().getDefaultVersionScheme())
                    .setVersionIdentifier(namespace, version);
        } catch (VersionSchemeException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public ModelBuilder newLibrary(String name, String namespace) {
        return newLibrary(name, namespace, "0.0.0");
    }

    public TLLibrary build() {
        TLLibrary library = new TLLibrary();
        library.setName(name);
        library.setNamespace(namespace);
        model.addLibrary(library);
        return library;
    }

    public TLModel getModel() {
        return model;
    }

}
