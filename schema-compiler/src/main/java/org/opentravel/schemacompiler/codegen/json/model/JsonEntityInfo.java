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

package org.opentravel.schemacompiler.codegen.json.model;

import com.google.gson.JsonObject;

/**
 * Provides JSON schema documentation about the OTM named entity from which the schema was generated.
 */
public class JsonEntityInfo implements JsonModelObject {

    private String entityType;
    private String entityName;

    /**
     * Returns the type of the OTM entity.
     *
     * @return String
     */
    public String getEntityType() {
        return entityType;
    }

    /**
     * Assigns the type of the OTM entity.
     *
     * @param entityType the OTM entity type to assign
     */
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    /**
     * Returns the local name of the OTM entity.
     *
     * @return String
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Assigns the local name of the OTM entity.
     *
     * @param entityName the OTM entity name to assign
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    public JsonObject toJson() {
        JsonObject entityInfo = new JsonObject();

        addProperty( entityInfo, "EntityType", entityType );
        addProperty( entityInfo, "EntityName", entityName );
        return entityInfo;
    }

}
