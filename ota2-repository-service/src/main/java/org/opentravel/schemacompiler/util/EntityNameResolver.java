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

import org.opentravel.schemacompiler.index.LibrarySearchResult;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * Helper class for rendering JSP pages that handles the deresolution of plain-text entity references.
 * 
 * @author S. Livezey
 */
public class EntityNameResolver {

    private static final Set<QName> builtInTypeNames;

    private LibrarySearchResult indexLibrary;

    /**
     * Constructor that specifies the library from which references should be calculated.
     * 
     * @param indexLibrary the source library from which all references are relative
     */
    public EntityNameResolver(LibrarySearchResult indexLibrary) {
        this.indexLibrary = indexLibrary;
    }

    /**
     * Returns the qualified name for the given entity reference. If the entity is not a valid reference (including if
     * it is a reference to an OTM built-in type) this method will return null.
     * 
     * @param entityRef the entity name reference to process
     * @return QName
     */
    public QName getQualifiedName(String entityRef) {
        int delimIdx = entityRef.indexOf( ':' );
        QName qualifiedName = null;
        String localName;
        String ns;

        if (delimIdx < 0) {
            ns = indexLibrary.getItemNamespace();
            localName = PageUtils.trimString( entityRef );

        } else {
            String prefix = entityRef.substring( 0, delimIdx );

            if (prefix.length() == 0) {
                ns = indexLibrary.getItemNamespace();
            } else {
                ns = indexLibrary.getNamespace( prefix );
            }

            if ((entityRef.length() - 1) > delimIdx) {
                localName = PageUtils.trimString( entityRef.substring( delimIdx + 1 ) );
            } else {
                localName = null;
            }
        }

        if ((ns != null) && (localName != null) && !isBuiltInType( ns, localName )) {
            qualifiedName = new QName( ns, localName );
        }

        return qualifiedName;
    }

    /**
     * Returns true if the given qualified name represents an OTM built-in type.
     * 
     * @param namespace the namespace of the qualified entity name
     * @param localName the local name of the qualified entity name
     * @return boolean
     */
    private boolean isBuiltInType(String namespace, String localName) {
        return namespace.equals( XMLConstants.W3C_XML_SCHEMA_NS_URI )
            || builtInTypeNames.contains( new QName( namespace, localName ) );
    }

    /**
     * Initializes the set of built-in type names.
     */
    static {
        try {
            List<BuiltInLibrary> libraries = new TLModel().getBuiltInLibraries();
            Set<QName> typeNames = new HashSet<>();

            for (BuiltInLibrary library : libraries) {
                for (NamedEntity builtInType : library.getNamedMembers()) {
                    typeNames.add( new QName( builtInType.getNamespace(), builtInType.getLocalName() ) );
                }
            }
            builtInTypeNames = Collections.unmodifiableSet( typeNames );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
