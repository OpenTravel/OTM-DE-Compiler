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
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem.AdditionalDocumentationItemListManager;
import org.opentravel.schemacompiler.model.TLDocumentationItem.DocumentationItemListManager;

/**
 * Model container for the various types of library documentation elements.
 * 
 * @author S. Livezey
 */
public class TLDocumentation extends TLModelElement implements LibraryElement {

    private TLDocumentationOwner owner;
    private String description;
    private DocumentationItemListManager deprecationManager = new DocumentationItemListManager(
            this, ModelEventType.DOC_DEPRECATION_ADDED, ModelEventType.DOC_DEPRECATION_REMOVED);
    private DocumentationItemListManager referenceManager = new DocumentationItemListManager(this,
            ModelEventType.DOC_REFERENCE_ADDED, ModelEventType.DOC_REFERENCE_REMOVED);
    private DocumentationItemListManager implementerManager = new DocumentationItemListManager(
            this, ModelEventType.DOC_IMPLEMENTER_ADDED, ModelEventType.DOC_IMPLEMENTER_REMOVED);
    private DocumentationItemListManager moreInfoManager = new DocumentationItemListManager(this,
            ModelEventType.DOC_MORE_INFO_ADDED, ModelEventType.DOC_MORE_INFO_REMOVED);
    private AdditionalDocumentationItemListManager otherDocManager = new AdditionalDocumentationItemListManager(
            this, ModelEventType.DOC_OTHER_DOCS_ADDED, ModelEventType.DOC_OTHER_DOCS_REMOVED);

    /**
     * Returns true if this documentation entity contains no documentation items.
     * 
     * @return boolean
     */
    public boolean isEmpty() {
        return ((description == null) || (description.length() == 0))
                && deprecationManager.getChildren().isEmpty()
                && referenceManager.getChildren().isEmpty()
                && implementerManager.getChildren().isEmpty()
                && moreInfoManager.getChildren().isEmpty()
                && otherDocManager.getChildren().isEmpty();
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
     */
    @Override
    public String getValidationIdentity() {
        StringBuilder identity = new StringBuilder();

        if (owner != null) {
            identity.append(owner.getValidationIdentity()).append("/");
        }
        return identity.append("Documentation").toString();
    }

    /**
     * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
     */
    @Override
    public TLModel getOwningModel() {
        return (owner == null) ? null : owner.getOwningModel();
    }

    /**
     * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
     */
    @Override
    public AbstractLibrary getOwningLibrary() {
        return (owner == null) ? null : owner.getOwningLibrary();
    }

    /**
     * Returns the value of the 'owner' field.
     * 
     * @return TLDocumentationOwner
     */
    public TLDocumentationOwner getOwner() {
        return owner;
    }

    /**
     * Assigns the value of the 'owner' field.
     * 
     * @param owner
     *            the field value to assign
     */
    public void setOwner(TLDocumentationOwner owner) {
        this.owner = owner;
    }

    /**
     * Returns the value of the 'DESCRIPTION' field.
     * 
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /**
     * Assigns the value of the 'description' field.
     * 
     * @param description
     *            the field value to assign
     */
    public void setDescription(String description) {
        String encodedDesc = adjustStringEncoding(description);
        ModelEvent<?> event = new ModelEventBuilder(ModelEventType.DESCRIPTION_MODIFIED, this)
                .setOldValue(this.description).setNewValue(encodedDesc).buildEvent();

        this.description = encodedDesc;
        publishEvent(event);
    }

    /**
     * Adjusts the encoding of the given string to remove any platform-dependent line feed
     * characters from its content.
     * 
     * @param str
     *            the string whose encoding is to be adjusted
     * @return String
     */
    protected static String adjustStringEncoding(String str) {
        String result;

        if (str != null) {
            StringBuilder adjustedStr = new StringBuilder();

            for (char ch : str.toCharArray()) {
                if (ch != 0xD) {
                    adjustedStr.append(ch);
                }
            }
            result = adjustedStr.toString();

        } else {
            result = null;
        }
        return result;
    }

    /**
     * Returns the value of the 'deprecations' field.
     * 
     * @return List<TLDocumentationItem>
     */
    public List<TLDocumentationItem> getDeprecations() {
        return deprecationManager.getChildren();
    }

    /**
     * Adds a deprecation to the current list.
     * 
     * @param deprecation
     *            the deprecation string value to add
     */
    public void addDeprecation(TLDocumentationItem deprecation) {
        deprecationManager.addChild(deprecation);
    }

    /**
     * Adds a deprecation to the current list.
     * 
     * @param index
     *            the index at which the given documentation item should be added
     * @param deprecation
     *            the deprecation string value to add
     * @throws IndexOutOfBoundsException
     *             thrown if the index is out of range (index < 0 || index > size())
     */
    public void addDeprecation(int index, TLDocumentationItem deprecation) {
        deprecationManager.addChild(index, deprecation);
    }

    /**
     * Removes a deprecation from the current list.
     * 
     * @param deprecation
     *            the deprecation string value to remove
     */
    public void removeDeprecation(TLDocumentationItem deprecation) {
        deprecationManager.removeChild(deprecation);
    }

    /**
     * Moves this deprecation up by one position in the list. If the deprecation is not owned by
     * this object or it is already at the front of the list, this method has no effect.
     * 
     * @param deprecation
     *            the deprecation to move
     */
    public void moveDeprecationUp(TLDocumentationItem deprecation) {
        deprecationManager.moveUp(deprecation);
    }

    /**
     * Moves this deprecation down by one position in the list. If the deprecation is not owned by
     * this object or it is already at the end of the list, this method has no effect.
     * 
     * @param deprecation
     *            the deprecation to move
     */
    public void moveDeprecationDown(TLDocumentationItem deprecation) {
        deprecationManager.moveDown(deprecation);
    }

    /**
     * Sorts the list of deprecations using the comparator provided.
     * 
     * @param comparator
     *            the comparator to use when sorting the list
     */
    public void sortDeprecations(Comparator<TLDocumentationItem> comparator) {
        deprecationManager.sortChildren(comparator);
    }

    /**
     * Returns the value of the 'references' field.
     * 
     * @return List<String>
     */
    public List<TLDocumentationItem> getReferences() {
        return referenceManager.getChildren();
    }

    /**
     * Adds a reference to the current list.
     * 
     * @param reference
     *            the string value to add
     */
    public void addReference(TLDocumentationItem reference) {
        referenceManager.addChild(reference);
    }

    /**
     * Adds a reference to the current list.
     * 
     * @param index
     *            the index at which the given documentation item should be added
     * @param reference
     *            the string value to add
     * @throws IndexOutOfBoundsException
     *             thrown if the index is out of range (index < 0 || index > size())
     */
    public void addReference(int index, TLDocumentationItem reference) {
        referenceManager.addChild(index, reference);
    }

    /**
     * Removes a reference from the current list.
     * 
     * @param reference
     *            the string value to remove
     */
    public void removeReference(TLDocumentationItem reference) {
        referenceManager.removeChild(reference);
    }

    /**
     * Moves this reference up by one position in the list. If the reference is not owned by this
     * object or it is already at the front of the list, this method has no effect.
     * 
     * @param reference
     *            the reference to move
     */
    public void moveReferenceUp(TLDocumentationItem reference) {
        referenceManager.moveUp(reference);
    }

    /**
     * Moves this reference down by one position in the list. If the reference is not owned by this
     * object or it is already at the end of the list, this method has no effect.
     * 
     * @param reference
     *            the reference to move
     */
    public void moveReferenceDown(TLDocumentationItem reference) {
        referenceManager.moveDown(reference);
    }

    /**
     * Sorts the list of references using the comparator provided.
     * 
     * @param comparator
     *            the comparator to use when sorting the list
     */
    public void sortReferences(Comparator<TLDocumentationItem> comparator) {
        referenceManager.sortChildren(comparator);
    }

    /**
     * Returns the value of the 'implementers' field.
     * 
     * @return List<String>
     */
    public List<TLDocumentationItem> getImplementers() {
        return implementerManager.getChildren();
    }

    /**
     * Adds a implementer to the current list.
     * 
     * @param implementer
     *            the implementer to add
     */
    public void addImplementer(TLDocumentationItem implementer) {
        implementerManager.addChild(implementer);
    }

    /**
     * Adds a implementer to the current list.
     * 
     * @param index
     *            the index at which the given documentation item should be added
     * @param implementer
     *            the implementer to add
     * @throws IndexOutOfBoundsException
     *             thrown if the index is out of range (index < 0 || index > size())
     */
    public void addImplementer(int index, TLDocumentationItem implementer) {
        implementerManager.addChild(index, implementer);
    }

    /**
     * Removes a implementer from the current list.
     * 
     * @param implementer
     *            the implementer to remove
     */
    public void removeImplementer(TLDocumentationItem implementer) {
        implementerManager.removeChild(implementer);
    }

    /**
     * Moves this implementer up by one position in the list. If the implementer is not owned by
     * this object or it is already at the front of the list, this method has no effect.
     * 
     * @param implementer
     *            the implementer to move
     */
    public void moveImplementerUp(TLDocumentationItem implementer) {
        implementerManager.moveUp(implementer);
    }

    /**
     * Moves this implementer down by one position in the list. If the implementer is not owned by
     * this object or it is already at the end of the list, this method has no effect.
     * 
     * @param implementer
     *            the implementer to move
     */
    public void moveImplementerDown(TLDocumentationItem implementer) {
        implementerManager.moveDown(implementer);
    }

    /**
     * Sorts the list of implementers using the comparator provided.
     * 
     * @param comparator
     *            the comparator to use when sorting the list
     */
    public void sortImplementers(Comparator<TLDocumentationItem> comparator) {
        implementerManager.sortChildren(comparator);
    }

    /**
     * Returns the value of the 'moreInfos' field.
     * 
     * @return List<String>
     */
    public List<TLDocumentationItem> getMoreInfos() {
        return moreInfoManager.getChildren();
    }

    /**
     * Adds a 'moreInfo' value to the current list.
     * 
     * @param moreInfo
     *            the string value to add
     */
    public void addMoreInfo(TLDocumentationItem moreInfo) {
        moreInfoManager.addChild(moreInfo);
    }

    /**
     * Adds a 'moreInfo' value to the current list.
     * 
     * @param index
     *            the index at which the given documentation item should be added
     * @param moreInfo
     *            the string value to add
     * @throws IndexOutOfBoundsException
     *             thrown if the index is out of range (index < 0 || index > size())
     */
    public void addMoreInfo(int index, TLDocumentationItem moreInfo) {
        moreInfoManager.addChild(index, moreInfo);
    }

    /**
     * Removes a 'moreInfo' value from the current list.
     * 
     * @param moreInfo
     *            the string value to remove
     */
    public void removeMoreInfo(TLDocumentationItem moreInfo) {
        moreInfoManager.removeChild(moreInfo);
    }

    /**
     * Moves this more-info up by one position in the list. If the more-info is not owned by this
     * object or it is already at the front of the list, this method has no effect.
     * 
     * @param moreInfo
     *            the more-info to move
     */
    public void moveMoreInfoUp(TLDocumentationItem moreInfo) {
        moreInfoManager.moveUp(moreInfo);
    }

    /**
     * Moves this more-info down by one position in the list. If the more-info is not owned by this
     * object or it is already at the end of the list, this method has no effect.
     * 
     * @param moreInfo
     *            the more-info to move
     */
    public void moveMoreInfoDown(TLDocumentationItem moreInfo) {
        moreInfoManager.moveDown(moreInfo);
    }

    /**
     * Sorts the list of more-infos using the comparator provided.
     * 
     * @param comparator
     *            the comparator to use when sorting the list
     */
    public void sortMoreInfo(Comparator<TLDocumentationItem> comparator) {
        moreInfoManager.sortChildren(comparator);
    }

    /**
     * Returns the value of the 'otherDocs' field.
     * 
     * @return List<TLAdditionalDocumentationItem>
     */
    public List<TLAdditionalDocumentationItem> getOtherDocs() {
        return otherDocManager.getChildren();
    }

    /**
     * Returns the other-doc element with the specified context ID.
     * 
     * @param contextId
     *            the context ID of the other-doc item to return
     * @return TLAdditionalDocumentationItem
     */
    public TLAdditionalDocumentationItem getOtherDoc(String contextId) {
        return otherDocManager.getChild(contextId);
    }

    /**
     * Adds an 'otherDoc' to the current list.
     * 
     * @param otherDoc
     *            the string value to add
     */
    public void addOtherDoc(TLAdditionalDocumentationItem otherDoc) {
        otherDocManager.addChild(otherDoc);
    }

    /**
     * Adds an 'otherDoc' to the current list.
     * 
     * @param index
     *            the index at which the given documentation item should be added
     * @param otherDoc
     *            the string value to add
     * @throws IndexOutOfBoundsException
     *             thrown if the index is out of range (index < 0 || index > size())
     */
    public void addOtherDoc(int index, TLAdditionalDocumentationItem otherDoc) {
        otherDocManager.addChild(index, otherDoc);
    }

    /**
     * Removes an 'otherDoc' value from the current list.
     * 
     * @param otherDoc
     *            the string value to remove
     */
    public void removeOtherDoc(TLAdditionalDocumentationItem otherDoc) {
        otherDocManager.removeChild(otherDoc);
    }

    /**
     * Moves this other-doc up by one position in the list. If the other-doc is not owned by this
     * object or it is already at the front of the list, this method has no effect.
     * 
     * @param otherDoc
     *            the other-doc to move
     */
    public void moveOtherDocUp(TLAdditionalDocumentationItem otherDoc) {
        otherDocManager.moveUp(otherDoc);
    }

    /**
     * Moves this other-doc down by one position in the list. If the other-doc is not owned by this
     * object or it is already at the end of the list, this method has no effect.
     * 
     * @param otherDoc
     *            the other-doc to move
     */
    public void moveOtherDocDown(TLAdditionalDocumentationItem otherDoc) {
        otherDocManager.moveDown(otherDoc);
    }

    /**
     * Sorts the list of other-docs using the comparator provided.
     * 
     * @param comparator
     *            the comparator to use when sorting the list
     */
    public void sortOtherDoc(Comparator<TLAdditionalDocumentationItem> comparator) {
        otherDocManager.sortChildren(comparator);
    }

}
