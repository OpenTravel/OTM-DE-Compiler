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

package org.opentravel.schemacompiler.codegen.openapi;

import org.opentravel.schemacompiler.codegen.impl.QualifiedParameter;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiParamType;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiParameter;
import org.opentravel.schemacompiler.codegen.swagger.AbstractSwaggerCodegenTransformer;
import org.opentravel.schemacompiler.codegen.util.EnumCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.util.SimpleTypeInfo;

import java.util.List;

/**
 * Performs the translation from <code>QualifiedParameter</code> objects to the OpenAPI model objects used to produce
 * the output.
 */
public class TLParameterOpenApiTransformer
    extends AbstractSwaggerCodegenTransformer<QualifiedParameter,OpenApiParameter> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public OpenApiParameter transform(QualifiedParameter source) {
        TLMemberField<?> fieldRef = source.getParameter().getFieldRef();
        OpenApiParameter openapiParam = new OpenApiParameter();

        openapiParam.setName( source.getParameterName() );
        openapiParam.setIn( getParamType( source.getParameter() ) );
        openapiParam.setRequired( isRequired( fieldRef ) );
        openapiParam.setType( getFieldSchema( fieldRef ) );

        if (fieldRef instanceof TLDocumentationOwner) {
            transformDocumentation( (TLDocumentationOwner) fieldRef, openapiParam );
        }
        if (fieldRef instanceof TLEquivalentOwner) {
            openapiParam.getEquivalentItems().addAll( jsonUtils.getEquivalentInfo( (TLEquivalentOwner) fieldRef ) );
        }
        if (fieldRef instanceof TLExampleOwner) {
            openapiParam.getExampleItems().addAll( jsonUtils.getExampleInfo( (TLExampleOwner) fieldRef ) );
        }
        return openapiParam;
    }

    /**
     * Returns the JSON schema for the given source field.
     * 
     * @param sourceField the source field for which to return the JSON schema
     * @return JsonSchema
     */
    private JsonSchema getFieldSchema(TLMemberField<?> sourceField) {
        JsonSchema schema = null;

        if (sourceField instanceof TLIndicator) {
            schema = new JsonSchema();
            schema.setType( JsonType.JSON_BOOLEAN );

        } else {
            NamedEntity fieldType = null;

            if (sourceField instanceof TLAttribute) {
                fieldType = ((TLAttribute) sourceField).getType();
            } else {
                fieldType = ((TLProperty) sourceField).getType();
            }

            if (fieldType != null) {
                schema = buildFieldSchema( getLatestMinorVersion( fieldType ) );
            }
        }

        // Last resort if we could not identify a type - use a string
        if ((schema != null) && (schema.getType() == null)) {
            schema.setType( JsonType.JSON_STRING );
        }
        return schema;
    }

    /**
     * Builds a JSON schema for the given field type.
     * 
     * @param fieldType the OTM entity type for which to create a schema
     * @return JsonSchema
     */
    private JsonSchema buildFieldSchema(NamedEntity fieldType) {
        SimpleTypeInfo simpleInfo = SimpleTypeInfo.newInstance( fieldType );
        JsonType jsonType = (simpleInfo == null) ? null : JsonType.valueOf( simpleInfo.getBaseSimpleType() );
        JsonSchema schema;

        if (jsonType != null) {
            schema = jsonUtils.buildSimpleTypeSchema( simpleInfo, jsonType );

        } else {
            schema = new JsonSchema(); // default to an empty schema

            if (fieldType instanceof TLClosedEnumeration) {
                TLClosedEnumeration closedEnum = (TLClosedEnumeration) fieldType;
                List<String> enumValues = schema.getEnumValues();

                for (TLEnumValue modelEnum : EnumCodegenUtils.getInheritedValues( closedEnum )) {
                    enumValues.add( modelEnum.getLiteral() );
                }
                schema.setType( JsonType.JSON_STRING );
            }
        }
        return schema;
    }

    /**
     * Returns the OpenAPI parameter type for the source parameter.
     * 
     * @param source the source parameter
     * @return OpenApiParamType
     */
    private OpenApiParamType getParamType(TLParameter source) {
        OpenApiParamType paramType = null;

        switch (source.getLocation()) {
            case PATH:
                paramType = OpenApiParamType.PATH;
                break;
            case QUERY:
                paramType = OpenApiParamType.QUERY;
                break;
            case HEADER:
                paramType = OpenApiParamType.HEADER;
                break;
            default:
                break;
        }
        return paramType;
    }

    /**
     * Returns true if the given source field is required.
     * 
     * @param sourceField the source field to check
     * @return boolean
     */
    private boolean isRequired(TLMemberField<?> sourceField) {
        boolean required = false;

        if (sourceField instanceof TLAttribute) {
            required = ((TLAttribute) sourceField).isMandatory();

        } else if (sourceField instanceof TLProperty) {
            required = ((TLProperty) sourceField).isMandatory();
        }
        return required;
    }

}
