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

/**
 * Constant definitions for the schema declarations that are required in the Spring application
 * context.
 * 
 * @author S. Livezey
 */
public class SchemaDeclarations {

    public static final SchemaDeclaration OTA2_LIBRARY_SCHEMA_1_4;
    public static final SchemaDeclaration OTA2_LIBRARY_SCHEMA_1_5;
    public static final SchemaDeclaration OTA2_CATALOG_SCHEMA;
    public static final SchemaDeclaration OTA2_PROJECT_SCHEMA;
    public static final SchemaDeclaration OTA2_REPOSITORY_SCHEMA;
    public static final SchemaDeclaration WSDL_SCHEMA;
    public static final SchemaDeclaration SOAP_SCHEMA;
    public static final SchemaDeclaration SCHEMA_FOR_SCHEMAS;
    public static final SchemaDeclaration OTM_COMMON_SCHEMA;
    public static final SchemaDeclaration OTA2_APPINFO_SCHEMA;

    private static final String OTA2_LIBRARY_SCHEMA_1_4_ID = "ota2LibrarySchema_1_4";
    private static final String OTA2_LIBRARY_SCHEMA_1_5_ID = "ota2LibrarySchema_1_5";
    private static final String OTA2_CATALOG_SCHEMA_ID = "ota2CatalogSchema";
    private static final String OTA2_PROJECT_SCHEMA_ID = "ota2ProjectSchema";
    private static final String OTA2_REPOSITORY_SCHEMA_ID = "ota2RepositorySchema";
    private static final String WSDL_SCHEMA_ID = "wsdlSchema";
    private static final String SOAP_SCHEMA_ID = "soapSchema";
    private static final String SCHEMA_FOR_SCHEMAS_ID = "schemaForSchemas";
    private static final String OTM_COMMON_SCHEMA_ID = "otmCommonSchema";
    private static final String OTA2_APPINFO_SCHEMA_ID = "ota2AppInfoSchema";

    /**
     * Initializes the required schema declarations from the Spring application context.
     */
    static {
        try {
            ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();

            OTA2_LIBRARY_SCHEMA_1_4 = (SchemaDeclaration) appContext.getBean(OTA2_LIBRARY_SCHEMA_1_4_ID);
            OTA2_LIBRARY_SCHEMA_1_5 = (SchemaDeclaration) appContext.getBean(OTA2_LIBRARY_SCHEMA_1_5_ID);
            OTA2_CATALOG_SCHEMA = (SchemaDeclaration) appContext.getBean(OTA2_CATALOG_SCHEMA_ID);
            OTA2_PROJECT_SCHEMA = (SchemaDeclaration) appContext.getBean(OTA2_PROJECT_SCHEMA_ID);
            OTA2_REPOSITORY_SCHEMA = (SchemaDeclaration) appContext.getBean(OTA2_REPOSITORY_SCHEMA_ID);
            WSDL_SCHEMA = (SchemaDeclaration) appContext.getBean(WSDL_SCHEMA_ID);
            SOAP_SCHEMA = (SchemaDeclaration) appContext.getBean(SOAP_SCHEMA_ID);
            SCHEMA_FOR_SCHEMAS = (SchemaDeclaration) appContext.getBean(SCHEMA_FOR_SCHEMAS_ID);
            OTM_COMMON_SCHEMA = (SchemaDeclaration) appContext.getBean(OTM_COMMON_SCHEMA_ID);
            OTA2_APPINFO_SCHEMA = (SchemaDeclaration) appContext.getBean(OTA2_APPINFO_SCHEMA_ID);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
