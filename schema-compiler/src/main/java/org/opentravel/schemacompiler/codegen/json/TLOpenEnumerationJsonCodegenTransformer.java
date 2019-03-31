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
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.util.EnumCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.TLBaseEnumerationCodegenTransformer;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.util.SimpleTypeInfo;

import java.util.List;

/**
 * Performs the translation from <code>TLOpenEnumeration</code> objects to the JSON schema elements used to produce the
 * output.
 */
public class TLOpenEnumerationJsonCodegenTransformer
    extends AbstractJsonSchemaTransformer<TLOpenEnumeration,CodegenArtifacts> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public CodegenArtifacts transform(TLOpenEnumeration source) {
        CodegenArtifacts artifacts = new CodegenArtifacts();

        artifacts.addArtifact( createComplexTypeSchema( source ) );
        artifacts.addArtifact( createSimpleTypeSchema( source ) );
        return artifacts;
    }

    /**
     * Constructs the complex type schema for the open enumeration.
     * 
     * @param source the source meta-model enumeration
     * @return JsonSchema
     */
    protected JsonSchemaNamedReference createComplexTypeSchema(TLOpenEnumeration source) {
        JsonSchemaNamedReference complexEnum = new JsonSchemaNamedReference();
        JsonSchema schema = new JsonSchema();

        complexEnum.setName( getDefinitionName( source ) );
        complexEnum.setSchema( new JsonSchemaReference( schema ) );

        transformDocumentation( source, schema );
        schema.setEntityInfo( jsonUtils.getEntityInfo( source ) );

        schema.getProperties().add( new JsonSchemaNamedReference( "value",
            new JsonSchemaReference( jsonUtils.getSchemaReferencePath( source, source ) + "_Base" ) ) );
        schema.getProperties().add( new JsonSchemaNamedReference( "extension",
            new JsonSchemaReference( SimpleTypeInfo.ENUM_EXTENSION_SCHEMA ) ) );
        return complexEnum;
    }

    /**
     * Constructs the simple type schema of the open enumeration.
     * 
     * @param source the source meta-model enumeration
     * @return JsonSchema
     */
    protected JsonSchemaNamedReference createSimpleTypeSchema(TLOpenEnumeration source) {
        JsonSchemaNamedReference simpleEnum = new JsonSchemaNamedReference();
        JsonSchema schema = new JsonSchema();
        List<String> enumValues = schema.getEnumValues();

        simpleEnum.setName( getDefinitionName( source ) + "_Base" );
        simpleEnum.setSchema( new JsonSchemaReference( schema ) );

        transformDocumentation( source, schema );
        schema.setEntityInfo( jsonUtils.getEntityInfo( source ) );
        schema.setType( JsonType.JSON_STRING );

        for (TLEnumValue modelEnum : EnumCodegenUtils.getInheritedValues( source )) {
            enumValues.add( modelEnum.getLiteral() );
        }
        enumValues.add( TLBaseEnumerationCodegenTransformer.OPEN_ENUM_VALUE );
        return simpleEnum;
    }

}
