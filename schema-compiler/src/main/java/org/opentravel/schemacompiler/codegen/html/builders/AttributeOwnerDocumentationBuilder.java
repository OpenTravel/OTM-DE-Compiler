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

package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.example.ExampleDocumentBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.codegen.example.ExampleJsonBuilder;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.validate.ValidationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Eric.Bronson
 *
 */
public abstract class AttributeOwnerDocumentationBuilder<
    T extends TLDocumentationOwner & TLAttributeOwner & TLIndicatorOwner> extends NamedEntityDocumentationBuilder<T> {

    protected List<AttributeDocumentationBuilder> attributes;

    protected List<IndicatorDocumentationBuilder> indicators;

    private String exampleXML;

    private String exampleJSON;

    /**
     * @param t the attribute owner for which to create a builder
     */
    public AttributeOwnerDocumentationBuilder(T t) {
        super( t );
        attributes = new ArrayList<>();
        indicators = new ArrayList<>();
        for (TLAttribute attribute : t.getAttributes()) {
            AttributeDocumentationBuilder attBuilder = new AttributeDocumentationBuilder( attribute );
            attributes.add( attBuilder );
            attBuilder.setOwner( this );
        }

        for (TLIndicator indicator : t.getIndicators()) {
            IndicatorDocumentationBuilder indBuilder = new IndicatorDocumentationBuilder( indicator );
            indicators.add( indBuilder );
            indBuilder.setOwner( this );
        }
        buildExamples( t );
    }

    protected void buildExamples(T t) {
        ExampleGeneratorOptions options = Configuration.getInstance().getExampleOptions();
        try {
            ExampleDocumentBuilder exampleBuilder = new ExampleDocumentBuilder( options );
            exampleBuilder.setModelElement( t );
            exampleXML = exampleBuilder.buildString();
        } catch (ValidationException | CodeGenerationException e) {
            exampleXML = "";
        }

        try {
            ExampleJsonBuilder exampleBuilder = new ExampleJsonBuilder( options );
            exampleBuilder.setModelElement( t );

            exampleJSON = exampleBuilder.buildString();
        } catch (ValidationException | CodeGenerationException e) {
            exampleJSON = "";
        }
    }

    public List<AttributeDocumentationBuilder> getAttributes() {
        return Collections.unmodifiableList( attributes );
    }


    public List<IndicatorDocumentationBuilder> getIndicators() {
        return Collections.unmodifiableList( indicators );
    }

    public String getExampleXML() {
        return exampleXML;
    }

    public String getExampleJSON() {
        return exampleJSON;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.html.builders.AbstractDocumentationBuilder#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals( obj );
    }

}
