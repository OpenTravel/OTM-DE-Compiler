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

package org.opentravel.schemacompiler.ioc;

import org.springframework.context.ApplicationContext;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * Defines a Spring-injected qualified XML name for schema constructs (types, elements, etc.) that generated code will
 * be dependent upon.
 * 
 * @author S. Livezey
 */
public final class SchemaDependency {

    private static final String EMPTY_ELEMENT = "EMPTY_ELEMENT";
    private static final String ENUM_EXTENSION = "ENUM_EXTENSION";
    private static final String MESSAGE_HEADER = "MESSAGE_HEADER";
    private static final String REQUEST_PAYLOAD = "REQUEST_PAYLOAD";
    private static final String RESPONSE_PAYLOAD = "RESPONSE_PAYLOAD";
    private static final String NOTIF_PAYLOAD = "NOTIF_PAYLOAD";
    private static final String MESSAGE_FAULT = "MESSAGE_FAULT";
    private static final String EXTENSION_POINT = "EXTENSION_POINT";
    private static final String EXTENSION_POINT_DETAIL = "EXTENSION_POINT_DETAIL";
    private static final String EXTENSION_POINT_CUSTOM = "EXTENSION_POINT_CUSTOM";
    private static final String EXTENSION_POINT_QUERY = "EXTENSION_POINT_QUERY";
    private static final String EXTENSION_POINT_UPDATE = "EXTENSION_POINT_UPDATE";
    private static final String EXTENSION_POINT_CHOICE = "EXTENSION_POINT_CHOICE";

    private String id;
    private SchemaDeclaration schemaDeclaration;
    private String localName;

    /**
     * Returns the schema dependency for the "Empty" data type.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getEmptyElement() {
        return getDependency( EMPTY_ELEMENT );
    }

    /**
     * Returns the schema dependency for the "String_EnumExtension" data type.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getEnumExtension() {
        return getDependency( ENUM_EXTENSION );
    }

    /**
     * Returns the schema dependency for the "MessageHeader" element.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getMessageHeader() {
        return getDependency( MESSAGE_HEADER );
    }

    /**
     * Returns the schema dependency for the "Fault" element.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getMessageFault() {
        return getDependency( MESSAGE_FAULT );
    }

    /**
     * Returns the schema dependency for the "RequestPayload" element.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getRequestPayload() {
        return getDependency( REQUEST_PAYLOAD );
    }

    /**
     * Returns the schema dependency for the "ResponsePayload" element.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getResponsePayload() {
        return getDependency( RESPONSE_PAYLOAD );
    }

    /**
     * Returns the schema dependency for the "NotifPayload" element.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getNotifPayload() {
        return getDependency( NOTIF_PAYLOAD );
    }

    /**
     * Returns the schema dependency for the "ExtensionPoint" element.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getExtensionPointElement() {
        return getDependency( EXTENSION_POINT );
    }

    /**
     * Returns the schema dependency for the "ExtensionPoint_Detail" element.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getExtensionPointDetailElement() {
        return getDependency( EXTENSION_POINT_DETAIL );
    }

    /**
     * Returns the schema dependency for the "ExtensionPoint_Custom" element.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getExtensionPointCustomElement() {
        return getDependency( EXTENSION_POINT_CUSTOM );
    }

    /**
     * Returns the schema dependency for the "ExtensionPoint_Query" element.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getExtensionPointQueryElement() {
        return getDependency( EXTENSION_POINT_QUERY );
    }

    /**
     * Returns the schema dependency for the "ExtensionPoint_Update" element.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getExtensionPointUpdateElement() {
        return getDependency( EXTENSION_POINT_UPDATE );
    }

    /**
     * Returns the schema dependency for the "ExtensionPoint_Choice" element.
     * 
     * @return SchemaDependency
     */
    public static SchemaDependency getExtensionPointChoiceElement() {
        return getDependency( EXTENSION_POINT_CHOICE );
    }

    /**
     * Returns the <code>SchemaDependency</code> defined in the application context with the specified ID.
     * 
     * @param dependencyId the ID of the dependency construct to return
     * @return SchemaDependency
     */
    @SuppressWarnings("unchecked")
    public static SchemaDependency getDependency(String dependencyId) {
        SchemaDependency dependency = null;

        if (dependencyId != null) {
            ApplicationContext context = SchemaCompilerApplicationContext.getContext();

            if (context.containsBean( SchemaCompilerApplicationContext.SCHEMA_DEPENDENCIES )) {
                List<SchemaDependency> schemaDependencies =
                    (List<SchemaDependency>) context.getBean( SchemaCompilerApplicationContext.SCHEMA_DEPENDENCIES );

                for (SchemaDependency sd : schemaDependencies) {
                    if (dependencyId.equals( sd.getId() )) {
                        dependency = sd;
                        break;
                    }
                }
            }
        }
        if (dependency == null) {
            throw new IllegalArgumentException( "Schema dependency ID not defined: " + dependencyId );
        }
        return dependency;
    }

    /**
     * Returns the list of all schema dependencies defined in the application context.
     * 
     * @return List&lt;SchemaDependency&gt;
     */
    @SuppressWarnings("unchecked")
    public static List<SchemaDependency> getAllDependencies() {
        ApplicationContext context = SchemaCompilerApplicationContext.getContext();
        List<SchemaDependency> schemaDependencies = null;

        if (context.containsBean( SchemaCompilerApplicationContext.SCHEMA_DEPENDENCIES )) {
            schemaDependencies =
                (List<SchemaDependency>) context.getBean( SchemaCompilerApplicationContext.SCHEMA_DEPENDENCIES );
        }
        return schemaDependencies;
    }

    /**
     * Returns a fully-qualified XML name for the schema construct.
     * 
     * @return QName
     */
    public QName toQName() {
        return new QName( schemaDeclaration.getNamespace(), localName );
    }

    /**
     * Returns the ID of this schema dependency construct.
     * 
     * @return String
     */
    public String getId() {
        return id;
    }

    /**
     * Assigns the ID of this schema dependency construct.
     * 
     * @param id the schema construct ID to assign
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the declaration for the file that defines the schema construct.
     * 
     * @return SchemaDeclaration
     */
    public SchemaDeclaration getSchemaDeclaration() {
        return schemaDeclaration;
    }

    /**
     * Assigns the declaration for the file that defines the schema construct.
     * 
     * @param schemaDeclaration the schema declaration to assign
     */
    public void setSchemaDeclaration(SchemaDeclaration schemaDeclaration) {
        this.schemaDeclaration = schemaDeclaration;
    }

    /**
     * Returns the local XML name of the construct, as defined in the schema declaration file.
     * 
     * @return String
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Assigns the local XML name of the construct, as defined in the schema declaration file.
     * 
     * @param localName the local XML name to assign
     */
    public void setLocalName(String localName) {
        this.localName = localName;
    }

}
