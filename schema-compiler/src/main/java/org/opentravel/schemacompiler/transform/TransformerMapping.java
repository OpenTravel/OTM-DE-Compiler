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
package org.opentravel.schemacompiler.transform;

/**
 * Specifies the transformer to be used for conversion from one type of Java class to another.
 * 
 * @author S. Livezey
 */
public class TransformerMapping {

    private Class<?> source;
    private Class<?> target;
    private Class<?> transformer;

    /**
     * Returns the source class of the transformation.
     * 
     * @return Class<?>
     */
    public Class<?> getSource() {
        return source;
    }

    /**
     * Assigns the source class of the transformation.
     * 
     * @param source
     *            the source object type
     */
    public void setSource(Class<?> sourceClass) {
        this.source = sourceClass;
    }

    /**
     * Returns the target class of the transformation.
     * 
     * @return Class<?>
     */
    public Class<?> getTarget() {
        return target;
    }

    /**
     * Assigns the target class of the transformation.
     * 
     * @param target
     *            the target object
     */
    public void setTarget(Class<?> targetClass) {
        this.target = targetClass;
    }

    /**
     * Returns class that will handle the transformation from the source type to the target type.
     * 
     * @return Class<?>
     */
    public Class<?> getTransformer() {
        return transformer;
    }

    /**
     * Assigns class that will handle the transformation from the source type to the target type.
     * 
     * @param transformer
     *            the name of the class that will perform the transformation
     */
    public void setTransformer(Class<?> transformerClass) {
        this.transformer = transformerClass;
    }

}
