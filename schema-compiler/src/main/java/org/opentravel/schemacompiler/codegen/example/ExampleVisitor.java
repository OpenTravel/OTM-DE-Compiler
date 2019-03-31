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

package org.opentravel.schemacompiler.codegen.example;

import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLPatchableFacet;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;

import java.util.Collection;

/**
 * Visitor interface that defines the callback methods invoked by the <code>ExampleNavigator</code> component.
 * 
 * @author S. Livezey
 */
public interface ExampleVisitor {

    /**
     * After EXAMPLE navigation is complete, this method will return the list of namespaces that were identified (bound)
     * to the EXAMPLE output during navigation/visitation processing.
     * 
     * @return Collection&lt;String&gt;
     */
    public Collection<String> getBoundNamespaces();

    /**
     * Called when a <code>TLAttributeType</code> (i.e. simple type) is encountered during navigation.
     * 
     * @param simpleType the simple type model element to be visited
     */
    public void visitSimpleType(TLAttributeType simpleType);

    /**
     * Called when a <code>TLFacet</code> instance is first encountered during EXAMPLE navigation.
     * 
     * @param facet the model element to be visited
     */
    public void startFacet(TLFacet facet);

    /**
     * Called when a <code>TLFacet</code> instance has completed processing during EXAMPLE navigation.
     * 
     * @param facet the model element to be visited
     */
    public void endFacet(TLFacet facet);

    /**
     * Called when a <code>TLListFacet</code> instance is first encountered during EXAMPLE navigation.
     * 
     * @param listFacet the model element to be visited
     * @param role the role for this particular list facet instance
     */
    public void startListFacet(TLListFacet listFacet, TLRole role);

    /**
     * Called when a <code>TLListFacet</code> instance has completed processing during EXAMPLE navigation.
     * 
     * @param listFacet the model element to be visited
     * @param role the role for this particular list facet instance
     */
    public void endListFacet(TLListFacet listFacet, TLRole role);

    /**
     * Called when a <code>TLAlias</code> instance is first encountered during EXAMPLE navigation.
     * 
     * @param alias the model element to be visited
     */
    public void startAlias(TLAlias alias);

    /**
     * Called when a <code>TLAlias</code> instance has completed processing during EXAMPLE navigation.
     * 
     * @param alias the model element to be visited
     */
    public void endAlias(TLAlias alias);

    /**
     * Called when a <code>TLActionFacet</code> instance is first encountered during EXAMPLE navigation.
     * 
     * @param actionFacet the model element to be visited
     * @param payloadFacet the facet that will supply the members beyond the business object reference
     */
    public void startActionFacet(TLActionFacet actionFacet, TLFacet payloadFacet);

    /**
     * Called when a <code>TLActionFacet</code> instance has completed processing during EXAMPLE navigation.
     * 
     * @param actionFacet the model element to be visited
     * @param payloadFacet the facet that will supply the members beyond the business object reference
     */
    public void endActionFacet(TLActionFacet actionFacet, TLFacet payloadFacet);

    /**
     * Called when a <code>TLAttribute</code> instance is first encountered during EXAMPLE navigation.
     * 
     * @param attribute the model element to be visited
     */
    public void startAttribute(TLAttribute attribute);

    /**
     * Called when a <code>TLAttribute</code> instance has completed processing during EXAMPLE navigation.
     * 
     * @param attribute the model element to be visited
     */
    public void endAttribute(TLAttribute attribute);

    /**
     * Called when a <code>TLProperty</code> instance is first encountered during EXAMPLE navigation, and the property's
     * assigned type is a simple one.
     * 
     * @param element the model element to be visited
     */
    public void startElement(TLProperty element);

    /**
     * Called when a <code>TLProperty</code> instance has completed processing during EXAMPLE navigation, and the
     * property's assigned type is a simple one.
     * 
     * @param element the model element to be visited
     */
    public void endElement(TLProperty element);

    /**
     * Called when a <code>TLIndicator</code> attribute instance is first encountered during EXAMPLE navigation.
     * 
     * @param indicator the model element to be visited
     */
    public void startIndicatorAttribute(TLIndicator indicator);

    /**
     * Called when a <code>TLIndicator</code> attribute instance has completed processing during EXAMPLE navigation.
     * 
     * @param indicator the model element to be visited
     */
    public void endIndicatorAttribute(TLIndicator indicator);

    /**
     * Called when a <code>TLIndicator</code> element instance is first encountered during EXAMPLE navigation.
     * 
     * @param indicator the model element to be visited
     */
    public void startIndicatorElement(TLIndicator indicator);

    /**
     * Called when a <code>TLIndicator</code> element instance has completed processing during EXAMPLE navigation.
     * 
     * @param indicator the model element to be visited
     */
    public void endIndicatorElement(TLIndicator indicator);

    /**
     * Called when a <code>TLOpenEnumeration</code> instance is first encountered during EXAMPLE navigation.
     * 
     * @param openEnum the model element to be visited
     */
    public void startOpenEnumeration(TLOpenEnumeration openEnum);

    /**
     * Called when a <code>TLOpenEnumeration</code> instance has completed processing during EXAMPLE navigation.
     * 
     * @param openEnum the model element to be visited
     */
    public void endOpenEnumeration(TLOpenEnumeration openEnum);

    /**
     * Called when a <code>TLRoleEnumeration</code> instance is first encountered during EXAMPLE navigation.
     * 
     * @param roleEnum the model element to be visited
     */
    public void startRoleEnumeration(TLRoleEnumeration roleEnum);

    /**
     * Called when a <code>TLRoleEnumeration</code> instance has completed processing during EXAMPLE navigation.
     * 
     * @param roleEnum the model element to be visited
     */
    public void endRoleEnumeration(TLRoleEnumeration roleEnum);

    /**
     * Called when a <code>TLValueWithAttributes</code> instance is first encountered during EXAMPLE navigation.
     * 
     * @param valueWithAttributes the model element to be visited
     */
    public void startValueWithAttributes(TLValueWithAttributes valueWithAttributes);

    /**
     * Called when a <code>TLValueWithAttributes</code> instance has completed processing during EXAMPLE navigation.
     * 
     * @param valueWithAttributes the model element to be visited
     */
    public void endValueWithAttributes(TLValueWithAttributes valueWithAttributes);

    /**
     * Called when a series of facet extension points is about to be inserted into the model.
     * 
     * @param facet the facet to which the extension(s) apply
     */
    public void startExtensionPoint(TLPatchableFacet facet);

    /**
     * Called when the navigation of a series of facet extension points has been completed.
     * 
     * @param facet the facet to which the extension(s) apply
     */
    public void endExtensionPoint(TLPatchableFacet facet);

    /**
     * Called when a <code>TLExtensionPointFacet</code> instance is first encountered during EXAMPLE navigation.
     * 
     * @param facet the model element to be visited
     */
    public void startExtensionPointFacet(TLExtensionPointFacet facet);

    /**
     * Called when a <code>TLExtensionPointFacet</code> instance has completed processing during EXAMPLE navigation.
     * 
     * @param facet the model element to be visited
     */
    public void endExtensionPointFacet(TLExtensionPointFacet facet);

    /**
     * Called when a <code>XSDComplexType</code> instance is first encountered during EXAMPLE navigation.
     * 
     * @param xsdComplexType the model element to be visited
     */
    public void startXsdComplexType(XSDComplexType xsdComplexType);

    /**
     * Called when a <code>XSDComplexType</code> instance has completed processing during EXAMPLE navigation.
     * 
     * @param xsdComplexType the model element to be visited
     */
    public void endXsdComplexType(XSDComplexType xsdComplexType);

    /**
     * Called when a <code>XSDElement</code> instance is first encountered during EXAMPLE navigation.
     * 
     * @param xsdElement the model element to be visited
     */
    public void startXsdElement(XSDElement xsdElement);

    /**
     * Called when a <code>XSDElement</code> instance has completed processing during EXAMPLE navigation.
     * 
     * @param xsdElement the model element to be visited
     */
    public void endXsdElement(XSDElement xsdElement);

}
