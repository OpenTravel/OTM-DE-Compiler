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

package org.opentravel.schemacompiler.index.builder;

import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.transform.util.ObsoleteBuiltInVisitor;
import org.opentravel.schemacompiler.util.URLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * Extension of the <code>ObsoleteBuiltInVisitor</code> that loads the <code>OTA_SimpleTypes</code> library from the
 * local classpath rather than a remote repository.
 */
public class ObsoleteBuiltInValidationVisitor extends ObsoleteBuiltInVisitor {

    @SuppressWarnings("squid:S1075") // Invalid Sonar finding at this location
    private static final String OTA_SIMPLE_TYPES_PATH =
        "/http/org/opentravel/www/otm/common/0.0.0/OTA_SimpleTypes_0_0_0.otm";

    private static Logger log = LoggerFactory.getLogger( ObsoleteBuiltInValidationVisitor.class );

    /**
     * @see org.opentravel.schemacompiler.transform.util.ObsoleteBuiltInVisitor#loadObsoleteBuiltIn()
     */
    @Override
    protected AbstractLibrary loadObsoleteBuiltIn() {
        AbstractLibrary ota2Simples = null;
        try {
            LibraryModelLoader<InputStream> loader = new LibraryModelLoader<>();
            URL ota2SimpleTypesUrl = LibraryIndexBuilder.class.getResource( "/otm-models/OTA_SimpleTypes_0_0_0.otm" );
            LibraryInputSource<InputStream> ota2SimpleTypesSource = new LibraryStreamInputSource( ota2SimpleTypesUrl );
            loader.loadLibraryModel( ota2SimpleTypesSource );
            TLModel model = loader.getLibraryModel();

            ota2Simples = model.getLibrary( OBSOLETE_BUILTIN_NS, OBSOLETE_BUILTIN_NAME );
            ota2Simples.setLibraryUrl( URLUtils.toURL( new File(
                RepositoryManager.getDefault().getFileManager().getRepositoryLocation(), OTA_SIMPLE_TYPES_PATH ) ) );

        } catch (Exception e) {
            log.warn( "Error loading obsolete built-in dependency from the local classpath.", e );
        }
        return ota2Simples;
    }

}
