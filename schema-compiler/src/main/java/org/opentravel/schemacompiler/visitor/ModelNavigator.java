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
package org.opentravel.schemacompiler.visitor;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.model.XSDSimpleType;

/**
 * Navigates all members of a <code>TLModel</code> instance in a pre-order, depth-first fashion.
 * 
 * @author S. Livezey
 */
public class ModelNavigator extends AbstractNavigator<TLModel> {

    /**
     * Constructor that initializes the visitor to be notified when model elements are encountered
     * during navigation.
     * 
     * @param visitor
     *            the visitor to be notified when model elements are encountered
     */
    public ModelNavigator(ModelElementVisitor visitor) {
        super(visitor);
    }

    /**
     * Navigates the model in a depth-first fashion using the given visitor for notification
     * callbacks.
     * 
     * @param model
     *            the model to navigate
     * @param visitor
     *            the visitor to be notified when model elements are encountered
     */
    public static void navigate(TLModel model, ModelElementVisitor visitor) {
        new ModelNavigator(visitor).navigate(model);
    }

    /**
     * Navigates a single library in a depth-first fashion using the given visitor for notification
     * callbacks.
     * 
     * @param library
     *            the library to navigate
     * @param visitor
     *            the visitor to be notified when model elements are encountered
     */
    public static void navigate(AbstractLibrary library, ModelElementVisitor visitor) {
        ModelNavigator navigator = new ModelNavigator(visitor);

        if (library instanceof BuiltInLibrary) {
            navigator.navigateBuiltInLibrary((BuiltInLibrary) library);

        } else if (library instanceof XSDLibrary) {
            navigator.navigateLegacySchemaLibrary((XSDLibrary) library);

        } else if (library instanceof TLLibrary) {
            navigator.navigateUserDefinedLibrary((TLLibrary) library);
        }
    }

    /**
     * Navigates the library element in a depth-first fashion using the given visitor for
     * notification callbacks.
     * 
     * @param libraryElement
     *            the library element to navigate
     * @param visitor
     *            the visitor to be notified when model elements are encountered
     */
    public static void navigate(LibraryElement libraryElement, ModelElementVisitor visitor) {
        new ModelNavigator(visitor).navigate(libraryElement);
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.AbstractNavigator#navigate(java.lang.Object)
     */
    public void navigate(TLModel model) {
        if (model != null) {
            for (BuiltInLibrary library : model.getBuiltInLibraries()) {
                navigateBuiltInLibrary(library);
            }
            for (XSDLibrary library : model.getLegacySchemaLibraries()) {
                navigateLegacySchemaLibrary(library);
            }
            for (TLLibrary library : model.getUserDefinedLibraries()) {
                navigateUserDefinedLibrary(library);
            }
        }
    }

    /**
     * Called when a <code>LibraryElement</code> instance is encountered during model navigation.
     * 
     * @param libraryElement
     *            the library element to navigate
     */
    public void navigate(LibraryElement libraryElement) {
        if (libraryElement instanceof TLSimple) {
            navigateSimple((TLSimple) libraryElement);

        } else if (libraryElement instanceof TLValueWithAttributes) {
            navigateValueWithAttributes((TLValueWithAttributes) libraryElement);

        } else if (libraryElement instanceof TLClosedEnumeration) {
            navigateClosedEnumeration((TLClosedEnumeration) libraryElement);

        } else if (libraryElement instanceof TLOpenEnumeration) {
            navigateOpenEnumeration((TLOpenEnumeration) libraryElement);

        } else if (libraryElement instanceof TLChoiceObject) {
            navigateChoiceObject((TLChoiceObject) libraryElement);

        } else if (libraryElement instanceof TLCoreObject) {
            navigateCoreObject((TLCoreObject) libraryElement);

        } else if (libraryElement instanceof TLBusinessObject) {
            navigateBusinessObject((TLBusinessObject) libraryElement);

        } else if (libraryElement instanceof TLResource) {
            navigateResource((TLResource) libraryElement);

        } else if (libraryElement instanceof TLFacet) {
            navigateFacet((TLFacet) libraryElement);

        } else if (libraryElement instanceof TLActionFacet) {
            navigateActionFacet((TLActionFacet) libraryElement);

        } else if (libraryElement instanceof TLSimpleFacet) {
            navigateSimpleFacet((TLSimpleFacet) libraryElement);

        } else if (libraryElement instanceof TLListFacet) {
            navigateListFacet((TLListFacet) libraryElement);

        } else if (libraryElement instanceof TLAlias) {
            navigateAlias((TLAlias) libraryElement);

        } else if (libraryElement instanceof TLService) {
            navigateService((TLService) libraryElement);

        } else if (libraryElement instanceof TLOperation) {
            navigateOperation((TLOperation) libraryElement);

        } else if (libraryElement instanceof TLExtensionPointFacet) {
            navigateExtensionPointFacet((TLExtensionPointFacet) libraryElement);

        } else if (libraryElement instanceof XSDSimpleType) {
            navigateXSDSimpleType((XSDSimpleType) libraryElement);

        } else if (libraryElement instanceof XSDComplexType) {
            navigateXSDComplexType((XSDComplexType) libraryElement);

        } else if (libraryElement instanceof XSDElement) {
            navigateXSDElement((XSDElement) libraryElement);

        } else if (libraryElement instanceof TLContext) {
            navigateContext((TLContext) libraryElement);

        } else if (libraryElement instanceof TLDocumentation) {
            navigateDocumentation((TLDocumentation) libraryElement);

        } else if (libraryElement instanceof TLEquivalent) {
            navigateEquivalent((TLEquivalent) libraryElement);

        } else if (libraryElement instanceof TLExample) {
            navigateExample((TLExample) libraryElement);

        } else if (libraryElement instanceof TLAttribute) {
            navigateAttribute((TLAttribute) libraryElement);

        } else if (libraryElement instanceof TLProperty) {
            navigateElement((TLProperty) libraryElement);

        } else if (libraryElement instanceof TLIndicator) {
            navigateIndicator((TLIndicator) libraryElement);

        } else if (libraryElement instanceof TLRole) {
            navigateRole((TLRole) libraryElement);
        }
    }

    /**
     * Called when a <code>BuiltInLibrary</code> instance is encountered during model navigation.
     * 
     * @param library
     *            the library to visit and navigate
     */
    public void navigateBuiltInLibrary(BuiltInLibrary library) {
        if (canVisit(library) && visitor.visitBuiltInLibrary(library)) {
            for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
                navigateNamespaceImport(nsImport);
            }
            for (LibraryMember builtInType : library.getNamedMembers()) {
                if (builtInType instanceof TLSimple) {
                    navigateSimple((TLSimple) builtInType);

                } else if (builtInType instanceof TLValueWithAttributes) {
                    navigateValueWithAttributes((TLValueWithAttributes) builtInType);

                } else if (builtInType instanceof TLClosedEnumeration) {
                    navigateClosedEnumeration((TLClosedEnumeration) builtInType);

                } else if (builtInType instanceof TLOpenEnumeration) {
                    navigateOpenEnumeration((TLOpenEnumeration) builtInType);

                } else if (builtInType instanceof TLChoiceObject) {
                    navigateChoiceObject((TLChoiceObject) builtInType);

                } else if (builtInType instanceof TLCoreObject) {
                    navigateCoreObject((TLCoreObject) builtInType);

                } else if (builtInType instanceof TLBusinessObject) {
                    navigateBusinessObject((TLBusinessObject) builtInType);

                } else if (builtInType instanceof TLResource) {
                    navigateResource((TLResource) builtInType);

                } else if (builtInType instanceof TLExtensionPointFacet) {
                    navigateExtensionPointFacet((TLExtensionPointFacet) builtInType);

                } else if (builtInType instanceof XSDSimpleType) {
                    navigateXSDSimpleType((XSDSimpleType) builtInType);

                } else if (builtInType instanceof XSDComplexType) {
                    navigateXSDComplexType((XSDComplexType) builtInType);

                } else if (builtInType instanceof XSDElement) {
                    navigateXSDElement((XSDElement) builtInType);
                }
            }
        }
        addVisitedNode(library);
    }

    /**
     * Called when a <code>XSDLibrary</code> instance is encountered during model navigation.
     * 
     * @param library
     *            the library to visit and navigate
     */
    public void navigateLegacySchemaLibrary(XSDLibrary library) {
        if (canVisit(library) && visitor.visitLegacySchemaLibrary(library)) {
            for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
                navigateNamespaceImport(nsImport);
            }
            for (TLInclude include : library.getIncludes()) {
                navigateInclude(include);
            }
            for (LibraryMember builtInType : library.getNamedMembers()) {
                for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
                    navigateNamespaceImport(nsImport);
                }
                for (TLInclude include : library.getIncludes()) {
                    navigateInclude(include);
                }
                if (builtInType instanceof XSDSimpleType) {
                    navigateXSDSimpleType((XSDSimpleType) builtInType);

                } else if (builtInType instanceof XSDComplexType) {
                    navigateXSDComplexType((XSDComplexType) builtInType);

                } else if (builtInType instanceof XSDElement) {
                    navigateXSDElement((XSDElement) builtInType);
                }
            }
        }
        addVisitedNode(library);
    }

    /**
     * Called when a <code>TLLibrary</code> instance is encountered during model navigation.
     * 
     * @param library
     *            the library to visit and navigate
     */
    public void navigateUserDefinedLibrary(TLLibrary library) {
        if (canVisit(library) && visitor.visitUserDefinedLibrary(library)) {
            for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
                navigateNamespaceImport(nsImport);
            }
            for (TLInclude include : library.getIncludes()) {
                navigateInclude(include);
            }
            for (TLSimple entity : library.getSimpleTypes()) {
                navigateSimple(entity);
            }
            for (TLValueWithAttributes entity : library.getValueWithAttributesTypes()) {
                navigateValueWithAttributes(entity);
            }
            for (TLClosedEnumeration entity : library.getClosedEnumerationTypes()) {
                navigateClosedEnumeration(entity);
            }
            for (TLOpenEnumeration entity : library.getOpenEnumerationTypes()) {
                navigateOpenEnumeration(entity);
            }
            for (TLChoiceObject entity : library.getChoiceObjectTypes()) {
                navigateChoiceObject(entity);
            }
            for (TLCoreObject entity : library.getCoreObjectTypes()) {
                navigateCoreObject(entity);
            }
            for (TLBusinessObject entity : library.getBusinessObjectTypes()) {
                navigateBusinessObject(entity);
            }
            for (TLResource entity : library.getResourceTypes()) {
                navigateResource(entity);
            }
            if (library.getService() != null) {
                navigateService(library.getService());
            }
            for (TLExtensionPointFacet entity : library.getExtensionPointFacetTypes()) {
                navigateExtensionPointFacet(entity);
            }
        }
        addVisitedNode(library);
    }

    /**
     * Called when a <code>TLContext</code> instance is encountered during model navigation.
     * 
     * @param context
     *            the context entity to visit and navigate
     */
    public void navigateContext(TLContext context) {
        if (canVisit(context)) {
            visitor.visitContext(context);
        }
    }

    /**
     * Called when a <code>TLSimple</code> instance is encountered during model navigation.
     * 
     * @param simple
     *            the simple entity to visit and navigate
     */
    public void navigateSimple(TLSimple simple) {
        if (canVisit(simple) && visitor.visitSimple(simple)) {
            for (TLEquivalent equivalent : simple.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            for (TLExample example : simple.getExamples()) {
                navigateExample(example);
            }
            navigateDocumentation(simple.getDocumentation());
        }
        addVisitedNode(simple);
    }

    /**
     * Called when a <code>TLValueWithAttributes</code> instance is encountered during model
     * navigation.
     * 
     * @param valueWithAttributes
     *            the simple entity to visit and navigate
     */
    public void navigateValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        if (canVisit(valueWithAttributes) && visitor.visitValueWithAttributes(valueWithAttributes)) {
            for (TLAttribute attribute : valueWithAttributes.getAttributes()) {
                navigateAttribute(attribute);
            }
            for (TLEquivalent equivalent : valueWithAttributes.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            for (TLExample example : valueWithAttributes.getExamples()) {
                navigateExample(example);
            }
            navigateDocumentation(valueWithAttributes.getDocumentation());
            navigateDocumentation(valueWithAttributes.getValueDocumentation());
        }
        addVisitedNode(valueWithAttributes);
    }

    /**
     * Called when a <code>TLClosedEnumeration</code> instance is encountered during model
     * navigation.
     * 
     * @param enumeration
     *            the enumeration entity to visit and navigate
     */
    public void navigateClosedEnumeration(TLClosedEnumeration enumeration) {
        if (canVisit(enumeration) && visitor.visitClosedEnumeration(enumeration)) {
            for (TLEnumValue enumValue : enumeration.getValues()) {
                navigateEnumValue(enumValue);
            }
            navigateExtension(enumeration.getExtension());
            navigateDocumentation(enumeration.getDocumentation());
        }
        addVisitedNode(enumeration);
    }

    /**
     * Called when a <code>TLOpenEnumeration</code> instance is encountered during model navigation.
     * 
     * @param enumeration
     *            the enumeration entity to visit and navigate
     */
    public void navigateOpenEnumeration(TLOpenEnumeration enumeration) {
        if (canVisit(enumeration) && visitor.visitOpenEnumeration(enumeration)) {
            for (TLEnumValue enumValue : enumeration.getValues()) {
                navigateEnumValue(enumValue);
            }
            navigateExtension(enumeration.getExtension());
            navigateDocumentation(enumeration.getDocumentation());
        }
        addVisitedNode(enumeration);
    }

    /**
     * Called when a <code>TLEnumValue</code> instance is encountered during model navigation.
     * 
     * @param enumValue
     *            the enumeration value to visit
     */
    public void navigateEnumValue(TLEnumValue enumValue) {
        if (canVisit(enumValue) && visitor.visitEnumValue(enumValue)) {
            for (TLEquivalent equivalent : enumValue.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            navigateDocumentation(enumValue.getDocumentation());
        }
        addVisitedNode(enumValue);
    }

    /**
     * Called when a <code>TLChoiceObject</code> instance is encountered during model navigation.
     * 
     * @param choiceObject
     *            the choice object entity to visit and navigate
     */
    public void navigateChoiceObject(TLChoiceObject choiceObject) {
        if (canVisit(choiceObject) && visitor.visitChoiceObject(choiceObject)) {
            navigateFacet(choiceObject.getSharedFacet());
            navigateExtension(choiceObject.getExtension());

            for (TLAlias alias : choiceObject.getAliases()) {
                navigateAlias(alias);
            }
            for (TLFacet choiceFacet : choiceObject.getChoiceFacets()) {
                navigateFacet(choiceFacet);
            }
            for (TLEquivalent equivalent : choiceObject.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            navigateDocumentation(choiceObject.getDocumentation());
        }
        addVisitedNode(choiceObject);
    }

    /**
     * Called when a <code>TLCoreObject</code> instance is encountered during model navigation.
     * 
     * @param coreObject
     *            the core object entity to visit and navigate
     */
    public void navigateCoreObject(TLCoreObject coreObject) {
        if (canVisit(coreObject) && visitor.visitCoreObject(coreObject)) {
            navigateSimpleFacet(coreObject.getSimpleFacet());
            navigateFacet(coreObject.getSummaryFacet());
            navigateFacet(coreObject.getDetailFacet());
            navigateExtension(coreObject.getExtension());

            for (TLAlias alias : coreObject.getAliases()) {
                navigateAlias(alias);
            }
            for (TLRole role : coreObject.getRoleEnumeration().getRoles()) {
                navigateRole(role);
            }
            for (TLEquivalent equivalent : coreObject.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            navigateDocumentation(coreObject.getDocumentation());
        }
        addVisitedNode(coreObject);
    }

    /**
     * Called when a <code>TLRole</code> instance is encountered during model navigation.
     * 
     * @param role
     *            the core object role to visit and navigate
     */
    public void navigateRole(TLRole role) {
        if (canVisit(role)) {
            visitor.visitRole(role);
        }
    }

    /**
     * Called when a <code>TLBusinessObject</code> instance is encountered during model navigation.
     * 
     * @param businessObject
     *            the business object entity to visit and navigate
     */
    public void navigateBusinessObject(TLBusinessObject businessObject) {
        if (canVisit(businessObject) && visitor.visitBusinessObject(businessObject)) {
            navigateFacet(businessObject.getIdFacet());
            navigateFacet(businessObject.getSummaryFacet());
            navigateFacet(businessObject.getDetailFacet());
            navigateExtension(businessObject.getExtension());

            for (TLAlias alias : businessObject.getAliases()) {
                navigateAlias(alias);
            }
            for (TLFacet customFacet : businessObject.getCustomFacets()) {
                navigateFacet(customFacet);
            }
            for (TLFacet queryFacet : businessObject.getQueryFacets()) {
                navigateFacet(queryFacet);
            }
            for (TLEquivalent equivalent : businessObject.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            navigateDocumentation(businessObject.getDocumentation());
        }
        addVisitedNode(businessObject);
    }

    /**
     * Called when a <code>TLService</code> instance is encountered during model navigation.
     * 
     * @param service
     *            the service entity to visit and navigate
     */
    public void navigateService(TLService service) {
        if (canVisit(service) && visitor.visitService(service)) {
            for (TLOperation operation : service.getOperations()) {
                navigateOperation(operation);
            }
            for (TLEquivalent equivalent : service.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            navigateDocumentation(service.getDocumentation());
        }
        addVisitedNode(service);
    }

    /**
     * Called when a <code>TLOperation</code> instance is encountered during model navigation.
     * 
     * @param operation
     *            the operation entity to visit and navigate
     */
    public void navigateOperation(TLOperation operation) {
        if (canVisit(operation) && visitor.visitOperation(operation)) {
            navigateFacet(operation.getRequest());
            navigateFacet(operation.getResponse());
            navigateFacet(operation.getNotification());
            navigateExtension(operation.getExtension());

            for (TLEquivalent equivalent : operation.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            navigateDocumentation(operation.getDocumentation());
        }
        addVisitedNode(operation);
    }

    /**
     * Called when a <code>TLResource</code> instance is encountered during model navigation.
     * 
     * @param resource
     *            the resource entity to visit and navigate
     */
    public void navigateResource(TLResource resource) {
        if (canVisit(resource) && visitor.visitResource(resource)) {
            navigateDocumentation(resource.getDocumentation());
            navigateBusinessObject(resource.getBusinessObjectRef());
            navigateExtension(resource.getExtension());
            
            for (TLResourceParentRef parentRef : resource.getParentRefs()) {
            	navigateResourceParentRef(parentRef);
            }
            for (TLParamGroup paramGroup : resource.getParamGroups()) {
            	navigateParamGroup(paramGroup);
            }
            for (TLActionFacet actionFacet : resource.getActionFacets()) {
            	navigateActionFacet(actionFacet);
            }
            for (TLAction action : resource.getActions()) {
            	navigateAction(action);	
            }
        }
        addVisitedNode(resource);
    }

    /**
     * Called when a <code>TLResourceParentRef</code> instance is encountered during model navigation.
     * 
     * @param parentRef
     *            the resource parent reference entity to visit and navigate
     */
    public void navigateResourceParentRef(TLResourceParentRef parentRef) {
        if (canVisit(parentRef) && visitor.visitResourceParentRef(parentRef)) {
            navigateDocumentation(parentRef.getDocumentation());
        }
        addVisitedNode(parentRef);
    }
    
    /**
     * Called when a <code>TLParamGroup</code> instance is encountered during model navigation.
     * 
     * @param paramGroup
     *            the parameter group entity to visit and navigate
     */
    public void navigateParamGroup(TLParamGroup paramGroup) {
        if (canVisit(paramGroup) && visitor.visitParamGroup(paramGroup)) {
        	navigateFacet(paramGroup.getFacetRef());
            navigateDocumentation(paramGroup.getDocumentation());
            
            for (TLParameter parameter : paramGroup.getParameters()) {
            	navigateParameter(parameter);
            }
        }
        addVisitedNode(paramGroup);
    }
    
    /**
     * Called when a <code>TLParameter</code> instance is encountered during model navigation.
     * 
     * @param parameter
     *            the parameter entity to visit and navigate
     */
    public void navigateParameter(TLParameter parameter) {
        if (canVisit(parameter) && visitor.visitParameter(parameter)) {
        	TLMemberField<?> fieldRef = parameter.getFieldRef();
        	
        	if (fieldRef instanceof TLAttribute) {
        		navigateAttribute((TLAttribute) fieldRef);
        		
        	} else if (fieldRef instanceof TLProperty) {
        		navigateElement((TLProperty) fieldRef);
        		
        	} else if (fieldRef instanceof TLIndicator) {
        		navigateIndicator((TLIndicator) fieldRef);
        	}
        	
            for (TLEquivalent equivalent : parameter.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            for (TLExample example : parameter.getExamples()) {
                navigateExample(example);
            }
            navigateDocumentation(parameter.getDocumentation());
        }
        addVisitedNode(parameter);
    }
    
    /**
     * Called when a <code>TLAction</code> instance is encountered during model navigation.
     * 
     * @param action
     *            the action entity to visit and navigate
     */
    public void navigateAction(TLAction action) {
        if (canVisit(action) && visitor.visitAction(action)) {
            navigateDocumentation(action.getDocumentation());
            navigateActionRequest(action.getRequest());
            
            for (TLActionResponse response : action.getResponses()) {
            	navigateActionResponse(response);
            }
        }
        addVisitedNode(action);
    }
    
    /**
     * Called when a <code>TLActionRequest</code> instance is encountered during model navigation.
     * 
     * @param actionRequest
     *            the action request entity to visit and navigate
     */
    public void navigateActionRequest(TLActionRequest actionRequest) {
        if (canVisit(actionRequest) && visitor.visitActionRequest(actionRequest)) {
        	navigateParamGroup(actionRequest.getParamGroup());
        	navigateActionFacet(actionRequest.getActionFacet());
            navigateDocumentation(actionRequest.getDocumentation());
        }
        addVisitedNode(actionRequest);
    }
    
    /**
     * Called when a <code>TLActionResponse</code> instance is encountered during model navigation.
     * 
     * @param actionResponse
     *            the action response entity to visit and navigate
     */
    public void navigateActionResponse(TLActionResponse actionResponse) {
        if (canVisit(actionResponse) && visitor.visitActionResponse(actionResponse)) {
        	navigateActionFacet(actionResponse.getActionFacet());
            navigateDocumentation(actionResponse.getDocumentation());
        }
        addVisitedNode(actionResponse);
    }
    
    /**
     * Called when a <code>TLExtensionPointFacet</code> instance is encountered during model
     * navigation.
     * 
     * @param extensionPointFacet
     *            the extension point facet entity to visit and navigate
     */
    public void navigateExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
        if (canVisit(extensionPointFacet) && visitor.visitExtensionPointFacet(extensionPointFacet)) {
            navigateExtension(extensionPointFacet.getExtension());

            for (TLAttribute attribute : extensionPointFacet.getAttributes()) {
                navigateAttribute(attribute);
            }
            for (TLProperty element : extensionPointFacet.getElements()) {
                navigateElement(element);
            }
            for (TLIndicator indicator : extensionPointFacet.getIndicators()) {
                navigateIndicator(indicator);
            }
            navigateDocumentation(extensionPointFacet.getDocumentation());
        }
    }

    /**
     * Called when a <code>XSDSimpleType</code> instance is encountered during model navigation.
     * 
     * @param xsdSimple
     *            the XSD simple-type entity to visit and navigate
     */
    public void navigateXSDSimpleType(XSDSimpleType xsdSimple) {
        if (canVisit(xsdSimple)) {
            visitor.visitXSDSimpleType(xsdSimple);
        }
        addVisitedNode(xsdSimple);
    }

    /**
     * Called when a <code>XSDComplexType</code> instance is encountered during model navigation.
     * 
     * @param xsdComplex
     *            the XSD complex-type entity to visit and navigate
     */
    public void navigateXSDComplexType(XSDComplexType xsdComplex) {
        if (canVisit(xsdComplex)) {
            visitor.visitXSDComplexType(xsdComplex);
        }
        addVisitedNode(xsdComplex);
    }

    /**
     * Called when a <code>XSDElement</code> instance is encountered during model navigation.
     * 
     * @param xsdElement
     *            the XSD element entity to visit and navigate
     */
    public void navigateXSDElement(XSDElement xsdElement) {
        if (canVisit(xsdElement)) {
            visitor.visitXSDElement(xsdElement);
        }
        addVisitedNode(xsdElement);
    }

    /**
     * Called when a <code>TLExtension</code> instance is encountered during model navigation.
     * 
     * @param extension
     *            the extension entity to visit and navigate
     */
    public void navigateExtension(TLExtension extension) {
        if (canVisit(extension)) {
            visitor.visitExtension(extension);
        }
    }

    /**
     * Called when a <code>TLFacet</code> instance is encountered during model navigation.
     * 
     * @param facet
     *            the facet entity to visit and navigate
     */
    public void navigateFacet(TLFacet facet) {
        if (canVisit(facet) && visitor.visitFacet(facet)) {
            for (TLAlias alias : facet.getAliases()) {
                navigateAlias(alias);
            }
            for (TLAttribute attribute : facet.getAttributes()) {
                navigateAttribute(attribute);
            }
            for (TLProperty element : facet.getElements()) {
                navigateElement(element);
            }
            for (TLIndicator indicator : facet.getIndicators()) {
                navigateIndicator(indicator);
            }
            navigateDocumentation(facet.getDocumentation());
        }
        addVisitedNode(facet);
    }

    /**
     * Called when a <code>TLActionFacet</code> instance is encountered during model navigation.
     * 
     * @param actionFacet
     *            the action facet entity to visit and navigate
     */
    public void navigateActionFacet(TLActionFacet actionFacet) {
        if (canVisit(actionFacet) && visitor.visitActionFacet(actionFacet)) {
            for (TLAttribute attribute : actionFacet.getAttributes()) {
                navigateAttribute(attribute);
            }
            for (TLProperty element : actionFacet.getElements()) {
                navigateElement(element);
            }
            for (TLIndicator indicator : actionFacet.getIndicators()) {
                navigateIndicator(indicator);
            }
            navigateDocumentation(actionFacet.getDocumentation());
        }
        addVisitedNode(actionFacet);
    }
    
    /**
     * Called when a <code>TLSimpleFacet</code> instance is encountered during model navigation.
     * 
     * @param simpleFacet
     *            the simple facet entity to visit and navigate
     */
    public void navigateSimpleFacet(TLSimpleFacet simpleFacet) {
        if (canVisit(simpleFacet) && visitor.visitSimpleFacet(simpleFacet)) {
            for (TLEquivalent equivalent : simpleFacet.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            for (TLExample example : simpleFacet.getExamples()) {
                navigateExample(example);
            }
            navigateDocumentation(simpleFacet.getDocumentation());
        }
        addVisitedNode(simpleFacet);
    }

    /**
     * Called when a <code>TLListFacet</code> instance is encountered during model navigation.
     * 
     * @param listFacet
     *            the list facet entity to visit and navigate
     */
    public void navigateListFacet(TLListFacet listFacet) {
        if (canVisit(listFacet) && visitor.visitListFacet(listFacet)) {
            for (TLAlias alias : listFacet.getAliases()) {
                navigateAlias(alias);
            }
        }
        addVisitedNode(listFacet);
    }

    /**
     * Called when a <code>TLAlias</code> instance is encountered during model navigation.
     * 
     * @param alias
     *            the alias entity to visit and navigate
     */
    public void navigateAlias(TLAlias alias) {
        if (canVisit(alias)) {
            visitor.visitAlias(alias);
        }
        addVisitedNode(alias);
    }

    /**
     * Called when a <code>TLAttribute</code> instance is encountered during model navigation.
     * 
     * @param attribute
     *            the attribute entity to visit and navigate
     */
    public void navigateAttribute(TLAttribute attribute) {
        if (canVisit(attribute) && visitor.visitAttribute(attribute)) {
            for (TLEquivalent equivalent : attribute.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            for (TLExample example : attribute.getExamples()) {
                navigateExample(example);
            }
            navigateDocumentation(attribute.getDocumentation());
        }
        addVisitedNode(attribute);
    }

    /**
     * Called when a <code>TLProperty</code> instance is encountered during model navigation.
     * 
     * @param element
     *            the element entity to visit and navigate
     */
    public void navigateElement(TLProperty element) {
        if (canVisit(element) && visitor.visitElement(element)) {
            for (TLEquivalent equivalent : element.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            for (TLExample example : element.getExamples()) {
                navigateExample(example);
            }
            navigateDocumentation(element.getDocumentation());
        }
        addVisitedNode(element);
    }

    /**
     * Called when a <code>TLIndicator</code> instance is encountered during model navigation.
     * 
     * @param indicator
     *            the indicator entity to visit and navigate
     */
    public void navigateIndicator(TLIndicator indicator) {
        if (canVisit(indicator) && visitor.visitIndicator(indicator)) {
            for (TLEquivalent equivalent : indicator.getEquivalents()) {
                navigateEquivalent(equivalent);
            }
            navigateDocumentation(indicator.getDocumentation());
        }
        addVisitedNode(indicator);
    }

    /**
     * Called when a <code>TLNamespaceImport</code> instance is encountered during model navigation.
     * 
     * @param nsImport
     *            the namespace import entity to visit and navigate
     */
    public void navigateNamespaceImport(TLNamespaceImport nsImport) {
        if (canVisit(nsImport)) {
            visitor.visitNamespaceImport(nsImport);
        }
        addVisitedNode(nsImport);
    }

    /**
     * Called when a <code>TLInclude</code> instance is encountered during model navigation.
     * 
     * @param nsImport
     *            the namespace import entity to visit and navigate
     */
    public void navigateInclude(TLInclude include) {
        if (canVisit(include)) {
            visitor.visitInclude(include);
        }
        addVisitedNode(include);
    }

    /**
     * Called when a <code>TLEquivalent</code> instance is encountered during model navigation.
     * 
     * @param equivalent
     *            the equivalent entity to visit and navigate
     */
    public void navigateEquivalent(TLEquivalent equivalent) {
        if (canVisit(equivalent)) {
            visitor.visitEquivalent(equivalent);
        }
    }

    /**
     * Called when a <code>TLExample</code> instance is encountered during model navigation.
     * 
     * @param example
     *            the example entity to visit and navigate
     */
    public void navigateExample(TLExample example) {
        if (canVisit(example)) {
            visitor.visitExample(example);
        }
    }

    /**
     * Called when a <code>TLDocumentation</code> instance is encountered during model navigation.
     * 
     * @param documentation
     *            the documentation entity to visit and navigate
     */
    public void navigateDocumentation(TLDocumentation documentation) {
        if (canVisit(documentation)) {
            visitor.visitDocumentation(documentation);
        }
        addVisitedNode(documentation);
    }

}
