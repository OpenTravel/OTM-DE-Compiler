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

package org.opentravel.schemacompiler.codegen.swagger.model;

import org.opentravel.schemacompiler.codegen.json.model.JsonModelObject;

import com.google.gson.JsonObject;

/**
 * Class that defines the meta-model for a Swagger Path Item object.
 */
public class SwaggerPathItem implements JsonModelObject {

    private String pathTemplate;
    private SwaggerOperation getOperation;
    private SwaggerOperation putOperation;
    private SwaggerOperation postOperation;
    private SwaggerOperation deleteOperation;
    private SwaggerOperation optionsOperation;
    private SwaggerOperation headOperation;
    private SwaggerOperation patchOperation;

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
     * @return SwaggerOperation
     */
    public SwaggerOperation getGetOperation() {
        return getOperation;
    }

    /**
     * Assigns the value of the 'getOperation' field.
     *
     * @param getOperation the field value to assign
     */
    public void setGetOperation(SwaggerOperation getOperation) {
        this.getOperation = getOperation;
    }

    /**
     * Returns the value of the 'putOperation' field.
     *
     * @return SwaggerOperation
     */
    public SwaggerOperation getPutOperation() {
        return putOperation;
    }

    /**
     * Assigns the value of the 'putOperation' field.
     *
     * @param putOperation the field value to assign
     */
    public void setPutOperation(SwaggerOperation putOperation) {
        this.putOperation = putOperation;
    }

    /**
     * Returns the value of the 'postOperation' field.
     *
     * @return SwaggerOperation
     */
    public SwaggerOperation getPostOperation() {
        return postOperation;
    }

    /**
     * Assigns the value of the 'postOperation' field.
     *
     * @param postOperation the field value to assign
     */
    public void setPostOperation(SwaggerOperation postOperation) {
        this.postOperation = postOperation;
    }

    /**
     * Returns the value of the 'deleteOperation' field.
     *
     * @return SwaggerOperation
     */
    public SwaggerOperation getDeleteOperation() {
        return deleteOperation;
    }

    /**
     * Assigns the value of the 'deleteOperation' field.
     *
     * @param deleteOperation the field value to assign
     */
    public void setDeleteOperation(SwaggerOperation deleteOperation) {
        this.deleteOperation = deleteOperation;
    }

    /**
     * Returns the value of the 'optionsOperation' field.
     *
     * @return SwaggerOperation
     */
    public SwaggerOperation getOptionsOperation() {
        return optionsOperation;
    }

    /**
     * Assigns the value of the 'optionsOperation' field.
     *
     * @param optionsOperation the field value to assign
     */
    public void setOptionsOperation(SwaggerOperation optionsOperation) {
        this.optionsOperation = optionsOperation;
    }

    /**
     * Returns the value of the 'headOperation' field.
     *
     * @return SwaggerOperation
     */
    public SwaggerOperation getHeadOperation() {
        return headOperation;
    }

    /**
     * Assigns the value of the 'headOperation' field.
     *
     * @param headOperation the field value to assign
     */
    public void setHeadOperation(SwaggerOperation headOperation) {
        this.headOperation = headOperation;
    }

    /**
     * Returns the value of the 'patchOperation' field.
     *
     * @return SwaggerOperation
     */
    public SwaggerOperation getPatchOperation() {
        return patchOperation;
    }

    /**
     * Assigns the value of the 'patchOperation' field.
     *
     * @param patchOperation the field value to assign
     */
    public void setPatchOperation(SwaggerOperation patchOperation) {
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
