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

import org.opentravel.schemacompiler.codegen.json.model.JsonSchema;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaReference;
import org.opentravel.schemacompiler.codegen.json.model.JsonType;
import org.opentravel.schemacompiler.model.TLIndicator;

/**
 * Performs the translation from <code>TLIndicator</code> objects to the JSON schema elements used to produce the
 * output.
 */
public class TLIndicatorJsonCodegenTransformer
    extends AbstractJsonSchemaTransformer<TLIndicator,JsonSchemaNamedReference> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public JsonSchemaNamedReference transform(TLIndicator source) {
        JsonSchemaNamedReference jsonIndicator = new JsonSchemaNamedReference();
        JsonSchema indicatorSchema = new JsonSchema();
        String indicatorName = source.getName();

        if (!indicatorName.endsWith( "Ind" )) {
            indicatorName += "Ind";
        }

        indicatorSchema.setType( JsonType.JSON_BOOLEAN );
        jsonIndicator.setName( indicatorName );
        jsonIndicator.setSchema( new JsonSchemaReference( indicatorSchema ) );

        transformDocumentation( source, indicatorSchema );
        indicatorSchema.getEquivalentItems().addAll( jsonUtils.getEquivalentInfo( source ) );

        return jsonIndicator;
    }

}
