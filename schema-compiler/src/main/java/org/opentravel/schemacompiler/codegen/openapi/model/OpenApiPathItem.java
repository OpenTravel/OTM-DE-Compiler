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

package org.opentravel.schemacompiler.codegen.openapi.model;

import org.opentravel.schemacompiler.codegen.json.model.JsonModelObject;

import com.google.gson.JsonObject;

/**
 * Class that defines the meta-model for a Swagger Path Item object.
 */
public class OpenApiPathItem implements JsonModelObject {

    private String pathTemplate;
    private OpenApiOperation getOperation;
    private OpenApiOperation putOperation;
    private OpenApiOperation postOperation;
    private OpenApiOperation deleteOperation;
    private OpenApiOperation optionsOperation;
    private OpenApiOperation headOperation;
    private OpenApiOperation patchOperation;

    /**
     * Returns the value of the 'pathTemplate' field.
     *
     * @return String
     */
    public String getPathTemplate() {
        return pathTemplate;
    }

    /**
     * Assigns the value of the 'pathTemplate' field.
     *
     * @param pathTemplate the field value to assign
     */
    public void setPathTemplate(String pathTemplate) {
        this.pathTemplate = pathTemplate;
    }

    /**
     * Returns the value of the 'getOperation' field.
     *
     * @return OpenApiOperation
     */
    public OpenApiOperation getGetOperation() {
        return getOperation;
    }

    /**
     * Assigns the value of the 'getOperation' field.
     *
     * @param getOperation the field value to assign
     */
    public void setGetOperation(OpenApiOperation getOperation) {
        this.getOperation = getOperation;
    }

    /**
     * Returns the value of the 'putOperation' field.
     *
     * @return OpenApiOperation
     */
    public OpenApiOperation getPutOperation() {
        return putOperation;
    }

    /**
     * Assigns the value of the 'putOperation' field.
     *
     * @param putOperation the field value to assign
     */
    public void setPutOperation(OpenApiOperation putOperation) {
        this.putOperation = putOperation;
    }

    /**
     * Returns the value of the 'postOperation' field.
     *
     * @return OpenApiOperation
     */
    public OpenApiOperation getPostOperation() {
        return postOperation;
    }

    /**
     * Assigns the value of the 'postOperation' field.
     *
     * @param postOperation the field value to assign
     */
    public void setPostOperation(OpenApiOperation postOperation) {
        this.postOperation = postOperation;
    }

    /**
     * Returns the value of the 'deleteOperation' field.
     *
     * @return OpenApiOperation
     */
    public OpenApiOperation getDeleteOperation() {
        return deleteOperation;
    }

    /**
     * Assigns the value of the 'deleteOperation' field.
     *
     * @param deleteOperation the field value to assign
     */
    public void setDeleteOperation(OpenApiOperation deleteOperation) {
        this.deleteOperation = deleteOperation;
    }

    /**
     * Returns the value of the 'optionsOperation' field.
     *
     * @return OpenApiOperation
     */
    public OpenApiOperation getOptionsOperation() {
        return optionsOperation;
    }

    /**
     * Assigns the value of the 'optionsOperation' field.
     *
     * @param optionsOperation the field value to assign
     */
    public void setOptionsOperation(OpenApiOperation optionsOperation) {
        this.optionsOperation = optionsOperation;
    }

    /**
     * Returns the value of the 'headOperation' field.
     *
     * @return OpenApiOperation
     */
    public OpenApiOperation getHeadOperation() {
        return headOperation;
    }

    /**
     * Assigns the value of the 'headOperation' field.
     *
     * @param headOperation the field value to assign
     */
    public void setHeadOperation(OpenApiOperation headOperation) {
        this.headOperation = headOperation;
    }

    /**
     * Returns the value of the 'patchOperation' field.
     *
     * @return OpenApiOperation
     */
    public OpenApiOperation getPatchOperation() {
        return patchOperation;
    }

    /**
     * Assigns the value of the 'patchOperation' field.
     *
     * @param patchOperation the field value to assign
     */
    public void setPatchOperation(OpenApiOperation patchOperation) {
        this.patchOperation = patchOperation;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.json.model.JsonModelObject#toJson()
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        addJsonProperty( json, "get", getOperation );
        addJsonProperty( json, "put", putOperation );
        addJsonProperty( json, "post", postOperation );
        addJsonProperty( json, "delete", deleteOperation );
        addJsonProperty( json, "options", optionsOperation );
        addJsonProperty( json, "head", headOperation );
        addJsonProperty( json, "patch", patchOperation );
        return json;
    }

}
