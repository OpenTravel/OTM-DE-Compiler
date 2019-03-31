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

package org.opentravel.schemacompiler.diff.impl;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLMemberFieldOwner;
import org.opentravel.schemacompiler.model.TLProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade used to compare the contents of two fields even though the two fields may be defined as different OTM member
 * field types (attribute, element, or indicator).
 */
public class FieldComparisonFacade {

    private TLMemberField<TLMemberFieldOwner> field;
    private Class<?> memberType;
    private String owningFacet;
    private NamedEntity assignedType;
    private boolean mandatory;
    private int repeatCount;
    private boolean reference;
    private TLDocumentation documentation;
    private List<String> equivalents = new ArrayList<>();
    private List<String> examples = new ArrayList<>();

    /**
     * Creates a comparison facade for the given attribute.
     * 
     * @param field the attribute from which to create the facade
     */
    @SuppressWarnings("unchecked")
    public FieldComparisonFacade(TLMemberField<?> field) {
        this.field = (TLMemberField<TLMemberFieldOwner>) field;

        if (field instanceof TLAttribute) {
            init( (TLAttribute) field );

        } else if (field instanceof TLProperty) {
            init( (TLProperty) field );

        } else {
            init( (TLIndicator) field );
        }
    }

    /**
     * Initializes this comparison facade using the given attribute.
     * 
     * @param field the attribute from which to create the facade
     */
    private void init(TLAttribute field) {
        this.memberType = field.getClass();
        this.owningFacet = getFacetName( field.getOwner() );
        this.assignedType = field.getType();
        this.documentation = field.getDocumentation();
        this.mandatory = field.isMandatory();
        this.reference = field.isReference();
        this.repeatCount = 0;
        this.reference = field.isReference();
        this.examples = ModelCompareUtils.getExamples( field );
        this.equivalents = ModelCompareUtils.getEquivalents( field );

        if (this.reference) {
            this.repeatCount = field.getReferenceRepeat();
        }
    }

    /**
     * Initializes this comparison facade using the given element.
     * 
     * @param field the element from which to create the facade
     */
    private void init(TLProperty field) {
        this.memberType = field.getClass();
        this.owningFacet = getFacetName( field.getOwner() );
        this.assignedType = field.getType();
        this.documentation = field.getDocumentation();
        this.mandatory = field.isMandatory();
        this.repeatCount = field.getRepeat();
        this.reference = field.isReference();
        this.examples = ModelCompareUtils.getExamples( field );
        this.equivalents = ModelCompareUtils.getEquivalents( field );
    }

    /**
     * Initializes this comparison facade using the given indicator.
     * 
     * @param field the indicator from which to create the facade
     */
    private void init(TLIndicator field) {
        this.memberType = field.getClass();
        this.owningFacet = getFacetName( field.getOwner() );
        this.assignedType = ModelCompareUtils.getXsdBooleanType( field.getOwningModel() );
        this.documentation = field.getDocumentation();
        this.mandatory = false;
        this.repeatCount = 0;
        this.reference = false;
        this.equivalents = ModelCompareUtils.getEquivalents( field );
    }

    /**
     * If the given owner is an instance of <code>TLFacet</code>, this method will return its identity name. Otherwise,
     * null will be returned.
     * 
     * @param owner the field owner
     * @return String
     */
    private String getFacetName(NamedEntity owner) {
        String facetName = null;

        if (owner instanceof TLFacet) {
            TLFacet fOwner = (TLFacet) owner;

            facetName = fOwner.getFacetType().getIdentityName( FacetCodegenUtils.getFacetName( fOwner ) );
        }
        return facetName;
    }

    /**
     * Returns the value of the 'field' field.
     *
     * @return TLMemberField&lt;TLMemberFieldOwner&gt;
     */
    public TLMemberField<TLMemberFieldOwner> getField() {
        return field;
    }

    /**
     * Returns the value of the 'memberType' field.
     *
     * @return Class&lt;?&gt;
     */
    public Class<?> getMemberType() {
        return memberType;
    }

    /**
     * Returns the value of the 'owningFacet' field.
     *
     * @return String
     */
    public String getOwningFacet() {
        return owningFacet;
    }

    /**
     * Returns the value of the 'assignedType' field.
     *
     * @return NamedEntity
     */
    public NamedEntity getAssignedType() {
        return assignedType;
    }

    /**
     * Returns the value of the 'mandatory' field.
     *
     * @return boolean
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Returns the value of the 'repeatCount' field.
     *
     * @return int
     */
    public int getRepeatCount() {
        return repeatCount;
    }

    /**
     * Returns the value of the 'reference' field.
     *
     * @return boolean
     */
    public boolean isReference() {
        return reference;
    }

    /**
     * Returns the value of the 'documentation' field.
     *
     * @return TLDocumentation
     */
    public TLDocumentation getDocumentation() {
        return documentation;
    }

    /**
     * Returns the value of the 'equivalents' field.
     *
     * @return List&lt;String&gt;
     */
    public List<String> getEquivalents() {
        return equivalents;
    }

    /**
     * Returns the value of the 'examples' field.
     *
     * @return List&lt;String&gt;
     */
    public List<String> getExamples() {
        return examples;
    }

}
