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

package org.opentravel.schemacompiler.codegen.json;

import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.CorrelatedCodegenArtifacts;
import org.opentravel.schemacompiler.codegen.json.facet.FacetJsonSchemaDelegateFactory;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.TLBaseEnumerationCodegenTransformer;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.util.SimpleTypeInfo;

/**
 * Performs the translation from <code>TLCoreObject</code> objects to the JSON schema elements used to produce the
 * output.
 */
public class TLCoreObjectJsonCodegenTransformer extends AbstractJsonSchemaTransformer<TLCoreObject,CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLCoreObject source) {
        TLRoleEnumeration roleEnum = source.getRoleEnumeration();
        FacetJsonSchemaDelegateFactory delegateFactory = new FacetJsonSchemaDelegateFactory( context );
        CorrelatedCodegenArtifacts artifacts = new CorrelatedCodegenArtifacts();

        generateFacetArtifacts( delegateFactory.getDelegate( source.getSummaryFacet() ), artifacts );
        generateFacetArtifacts( delegateFactory.getDelegate( source.getDetailFacet() ), artifacts );

        if (!source.getRoleEnumeration().getRoles().isEmpty()) {
            artifacts.addArtifact( roleEnum, createRoleEnumerationComplexType( source ) );
            artifacts.addArtifact( roleEnum, createRoleEnumerationSimpleType( source, false ) );
            artifacts.addArtifact( roleEnum, createRoleEnumerationSimpleType( source, true ) );
        }
        return artifacts.getConsolidatedArtifacts();
    }

    /**
     * Creates the open enumeration type for the roles of the core object.
     * 
     * @param source the core object being transformed
     * @return JsonSchemaNamedReference
     */
    private JsonSchemaNamedReference createRoleEnumerationComplexType(TLCoreObject source) {
        JsonSchemaNamedReference roleEnum = new JsonSchemaNamedReference();
        String definitionName = getDefinitionName( source.getRoleEnumeration() );
        JsonSchema schema = new JsonSchema();

        roleEnum.setName( definitionName );
        roleEnum.setSchema( new JsonSchemaReference( schema ) );
        schema.getProperties().add( new JsonSchemaNamedReference( "value", new JsonSchemaReference(
            JsonSchemaCodegenUtils.getBaseDefinitionsPath( context ) + definitionName + "_Open" ) ) );
        schema.getProperties().add( new JsonSchemaNamedReference( "extension",
            new JsonSchemaReference( SimpleTypeInfo.ENUM_EXTENSION_SCHEMA ) ) );
        return roleEnum;
    }

    /**
     * Creates the JSON schema simple enumeration for the roles of the core object.
     * 
     * @param source the core object being transformed
     * @param openEnumeration indicates whether to generate the open or closed enumeration variant of the simple type
     * @return JsonSchemaNamedReference
     */
    private JsonSchemaNamedReference createRoleEnumerationSimpleType(TLCoreObject source, boolean openEnumeration) {
        String enumName = getDefinitionName( source.getRoleEnumeration() ) + (openEnumeration ? "_Open" : "_Base");
        JsonSchemaNamedReference roleEnum = new JsonSchemaNamedReference();
        JsonSchema schema = new JsonSchema();

        roleEnum.setName( enumName );
        roleEnum.setSchema( new JsonSchemaReference( schema ) );
        schema.setType( JsonType.JSON_STRING );

        for (TLRole role : PropertyCodegenUtils.getInheritedRoles( source )) {
            schema.getEnumValues().add( role.getName() );
        }
        if (openEnumeration) {
            schema.getEnumValues().add( TLBaseEnumerationCodegenTransformer.OPEN_ENUM_VALUE );
        }
        return roleEnum;
    }

}
