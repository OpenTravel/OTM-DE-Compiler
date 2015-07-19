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
package org.opentravel.schemacompiler.model;

import java.util.Comparator;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.model.TLEquivalent.EquivalentListManager;
import org.opentravel.schemacompiler.model.TLExample.ExampleListManager;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Simple data type for library types.
 * 
 * @author S. Livezey
 */
public class TLSimple extends LibraryMember implements Versioned, TLAttributeType,
        TLDocumentationOwner, TLEquivalentOwner, TLExampleOwner {

    private String name;
    private TLDocumentation documentation;
    private TLAttributeType parentType;
    private String parentTypeName;
    private boolean listTypeInd;
    private String pattern;
    private int minLength = -1;
    private int maxLength = -1;
    private int fractionDigits = -1;
    private int totalDigits = -1;
    private String minInclusive;
    private String maxInclusive;
    private String minExclusive;
    private String maxExclusive;
    private EquivalentListManager equivalentManager = new EquivalentListManager(this);
    private ExampleListManager exampleManager = new ExampleListManager(this);

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        StringBuilder identity = new StringBuilder();

        if (owningLibrary != null) {
            identity.append(owningLibrary.getValidationIdentity()).append(" : ");
        }
        if (name == null) {
            identity.append("[Unnamed Simple Type]");
        } else {
            identity.append(name);
        }
        return identity.toString();
    }

    /**
     * @see org.opentravel.schemacompiler.version.Versioned#getVersion()
     */
    @Override
    public String getVersion() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        String version = null;

        if (owningLibrary instanceof TLLibrary) {
            version = ((TLLibrary) owningLibrary).getVersion();
        }
        return version;
    }

    /**
     * @see org.opentravel.schemacompiler.version.Versioned#getVersionScheme()
     */
    @Override
    public String getVersionScheme() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        String versionScheme = null;

        if (owningLibrary instanceof TLLibrary) {
            versionScheme = ((TLLibrary) owningLibrary).getVersionScheme();
        }
        return versionScheme;
    }

    /**
     * @see org.opentravel.schemacompiler.version.Versioned#getBaseNamespace()
     */
    @Override
    public String getBaseNamespace() {
        AbstractLibrary owningLibrary = getOwningLibrary();
        String baseNamespace;

        if (owningLibrary instanceof TLLibrary) {
            baseNamespace = ((TLLibrary) owningLibrary).getBaseNamespace();
        } else {
            baseNamespace = getNamespace();
        }
        return baseNamespace;
    }

    /**
     * @see org.opentravel.schemacompiler.version.Versioned#isLaterVersion(org.opentravel.schemacompiler.version.Versioned)
     */
    @Override
    public boolean isLaterVersion(Versioned otherVersionedItem) {
        boolean result = false;

        if ((otherVersionedItem != null) && otherVersionedItem.getClass().equals(this.getClass())
                && (this.getOwningLibrary() != null)
                && (otherVersionedItem.getOwningLibrary() != null) && (this.getLocalName() != null)
                && this.getLocalName().equals(otherVersionedItem.getLocalName())) {
            result = this.getOwningLibrary().isLaterVersion(otherVersionedItem.getOwningLibrary());
        }
        return result;
    }

    /**
     * Returns the value of the 'name' field.
     * 
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Assigns the value of the 'name' field.
     * 
     * @param name
     *            the field value to assign
     */
    public void setName(String name) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.NAME_MODIFIED, this)
                .setOldValue(this.name).setNewValue(name).buildEvent();

        this.name = name;
        publishEvent(event);
    }

    /**
     * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
     */
    @Override
    public String getLocalName() {
        return name;
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLDocumentationOwner#getDocumentation()
     */
    public TLDocumentation getDocumentation() {
        return documentation;
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLDocumentationOwner#setDocumentation(org.opentravel.schemacompiler.model.TLDocumentation)
     */
    public void setDocumentation(TLDocumentation documentation) {
        if (documentation != this.documentation) {
            ModelEvent<?> event = new ModelEventBuilder(ModelEventType.DOCUMENTATION_MODIFIED, this)
                    .setOldValue(this.documentation).setNewValue(documentation).buildEvent();

            if (documentation != null) {
                documentation.setOwner(this);
            }
            if (this.documentation != null) {
                this.documentation.setOwner(null);
            }
            this.documentation = documentation;
            publishEvent(event);
        }
    }

    /**
     * Returns the value of the 'parentType' field.
     * 
     * @return TLAttributeType
     */
    public TLAttributeType getParentType() {
        return parentType;
    }

    /**
     * Assigns the value of the 'parentType' field.
     * 
     * @param parentType
     *            the field value to assign
     */
    public void setParentType(TLAttributeType parentType) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.TYPE_ASSIGNMENT_MODIFIED, this)
                .setOldValue(this.parentType).setNewValue(parentType).buildEvent();

        this.parentType = parentType;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'parentTypeName' field.
     * 
     * @return String
     */
    public String getParentTypeName() {
        return parentTypeName;
    }

    /**
     * Assigns the value of the 'parentTypeName' field.
     * 
     * @param parentTypeName
     *            the field value to assign
     */
    public void setParentTypeName(String parentTypeName) {
        this.parentTypeName = parentTypeName;
    }

    /**
     * Returns the value of the 'listTypeInd' field.
     * 
     * @return boolean
     */
    public boolean isListTypeInd() {
        return listTypeInd;
    }

    /**
     * Assigns the value of the 'listTypeInd' field.
     * 
     * @param listTypeInd
     *            the field value to assign
     */
    public void setListTypeInd(boolean listTypeInd) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.LIST_TYPE_INDICATOR_MODIFIED,
                this).setOldValue(this.listTypeInd).setNewValue(listTypeInd).buildEvent();

        this.listTypeInd = listTypeInd;
        publishEvent(event);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLAttributeType#getXSDFacetProfile()
     */
    @Override
    public XSDFacetProfile getXSDFacetProfile() {
        return (parentType == null) ? XSDFacetProfile.FP_unknown : parentType.getXSDFacetProfile();
    }

    /**
     * Returns the value of the 'pattern' field.
     * 
     * @return String
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Assigns the value of the 'pattern' field.
     * 
     * @param pattern
     *            the field value to assign
     */
    public void setPattern(String pattern) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.PATTERN_MODIFIED, this)
                .setOldValue(this.pattern).setNewValue(pattern).buildEvent();

        this.pattern = pattern;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'minLength' field.
     * 
     * @return int
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Assigns the value of the 'minLength' field.
     * 
     * @param minLength
     *            the field value to assign
     */
    public void setMinLength(int minLength) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.MIN_LENGTH_MODIFIED, this)
                .setOldValue(this.minLength).setNewValue(minLength).buildEvent();

        this.minLength = minLength;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'maxLength' field.
     * 
     * @return int
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Assigns the value of the 'maxLength' field.
     * 
     * @param maxLength
     *            the field value to assign
     */
    public void setMaxLength(int maxLength) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.MAX_LENGTH_MODIFIED, this)
                .setOldValue(this.maxLength).setNewValue(maxLength).buildEvent();

        this.maxLength = maxLength;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'fractionDigits' field.
     * 
     * @return int
     */
    public int getFractionDigits() {
        return fractionDigits;
    }

    /**
     * Assigns the value of the 'fractionDigits' field.
     * 
     * @param fractionDigits
     *            the field value to assign
     */
    public void setFractionDigits(int fractionDigits) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.FRACTION_DIGITS_MODIFIED, this)
                .setOldValue(this.maxLength).setNewValue(maxLength).buildEvent();

        this.fractionDigits = fractionDigits;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'totalDigits' field.
     * 
     * @return int
     */
    public int getTotalDigits() {
        return totalDigits;
    }

    /**
     * Assigns the value of the 'totalDigits' field.
     * 
     * @param totalDigits
     *            the field value to assign
     */
    public void setTotalDigits(int totalDigits) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.TOTAL_DIGITS_MODIFIED, this)
                .setOldValue(this.maxLength).setNewValue(maxLength).buildEvent();

        this.totalDigits = totalDigits;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'minInclusive' field.
     * 
     * @return String
     */
    public String getMinInclusive() {
        return minInclusive;
    }

    /**
     * Assigns the value of the 'minInclusive' field.
     * 
     * @param minInclusive
     *            the field value to assign
     */
    public void setMinInclusive(String minInclusive) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.MIN_INCLUSIVE_MODIFIED, this)
                .setOldValue(this.minInclusive).setNewValue(minInclusive).buildEvent();

        this.minInclusive = minInclusive;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'maxInclusive' field.
     * 
     * @return String
     */
    public String getMaxInclusive() {
        return maxInclusive;
    }

    /**
     * Assigns the value of the 'maxInclusive' field.
     * 
     * @param maxInclusive
     *            the field value to assign
     */
    public void setMaxInclusive(String maxInclusive) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.MAX_INCLUSIVE_MODIFIED, this)
                .setOldValue(this.maxInclusive).setNewValue(maxInclusive).buildEvent();

        this.maxInclusive = maxInclusive;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'minExclusive' field.
     * 
     * @return String
     */
    public String getMinExclusive() {
        return minExclusive;
    }

    /**
     * Assigns the value of the 'minExclusive' field.
     * 
     * @param minExclusive
     *            the field value to assign
     */
    public void setMinExclusive(String minExclusive) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.MIN_EXCLUSIVE_MODIFIED, this)
                .setOldValue(this.minExclusive).setNewValue(minExclusive).buildEvent();

        this.minExclusive = minExclusive;
        publishEvent(event);
    }

    /**
     * Returns the value of the 'maxExclusive' field.
     * 
     * @return String
     */
    public String getMaxExclusive() {
        return maxExclusive;
    }

    /**
     * Assigns the value of the 'maxExclusive' field.
     * 
     * @param maxExclusive
     *            the field value to assign
     */
    public void setMaxExclusive(String maxExclusive) {
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.MAX_EXCLUSIVE_MODIFIED, this)
                .setOldValue(this.maxExclusive).setNewValue(maxExclusive).buildEvent();

        this.maxExclusive = maxExclusive;
        publishEvent(event);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#getEquivalents()
     */
    @Override
    public List<TLEquivalent> getEquivalents() {
        return equivalentManager.getChildren();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#getEquivalent(java.lang.String)
     */
    @Override
    public TLEquivalent getEquivalent(String context) {
        return equivalentManager.getChild(context);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#addEquivalent(org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void addEquivalent(TLEquivalent equivalent) {
        equivalentManager.addChild(equivalent);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#addEquivalent(int,
     *      org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void addEquivalent(int index, TLEquivalent equivalent) {
        equivalentManager.addChild(index, equivalent);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#removeEquivalent(org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void removeEquivalent(TLEquivalent equivalent) {
        equivalentManager.removeChild(equivalent);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#moveUp(org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void moveUp(TLEquivalent equivalent) {
        equivalentManager.moveUp(equivalent);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#moveDown(org.opentravel.schemacompiler.model.TLEquivalent)
     */
    @Override
    public void moveDown(TLEquivalent equivalent) {
        equivalentManager.moveDown(equivalent);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLEquivalentOwner#sortEquivalents(java.util.Comparator)
     */
    @Override
    public void sortEquivalents(Comparator<TLEquivalent> comparator) {
        equivalentManager.sortChildren(comparator);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#getExamples()
     */
    public List<TLExample> getExamples() {
        return exampleManager.getChildren();
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#getExample(java.lang.String)
     */
    public TLExample getExample(String contextId) {
        return exampleManager.getChild(contextId);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#addExample(org.opentravel.schemacompiler.model.TLExample)
     */
    public void addExample(TLExample example) {
        exampleManager.addChild(example);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#addExample(int,
     *      org.opentravel.schemacompiler.model.TLExample)
     */
    public void addExample(int index, TLExample example) {
        exampleManager.addChild(index, example);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#removeExample(org.opentravel.schemacompiler.model.TLExample)
     */
    public void removeExample(TLExample example) {
        exampleManager.removeChild(example);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#moveUp(org.opentravel.schemacompiler.model.TLExample)
     */
    public void moveUp(TLExample example) {
        exampleManager.moveUp(example);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#moveDown(org.opentravel.schemacompiler.model.TLExample)
     */
    public void moveDown(TLExample example) {
        exampleManager.moveDown(example);
    }

    /**
     * @see org.opentravel.schemacompiler.model.TLExampleOwner#sortExamples(java.util.Comparator)
     */
    public void sortExamples(Comparator<TLExample> comparator) {
        exampleManager.sortChildren(comparator);
    }

}
