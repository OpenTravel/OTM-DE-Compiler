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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Navigates all of the direct and indirect model dependencies of a <code>LibraryMember</code>
 * instance in a pre-order, depth-first fashion.  Only those dependencies that are required for
 * generated XML/JSON schemas.
 */
public class SchemaDependencyNavigator extends AbstractNavigator<NamedEntity> {

    /**
     * Default constructor.
     */
    public SchemaDependencyNavigator() {
    }

    /**
     * Constructor that initializes the visitor to be notified when model elements are encountered
     * during navigation.
     * 
     * @param visitor  the visitor to be notified when model elements are encountered
     */
    public SchemaDependencyNavigator(ModelElementVisitor visitor) {
        super(visitor);
    }

    /**
     * Navigates the dependencies of all elements of the given library in a depth-first fashion
     * using the given visitor for notification callbacks.
     * 
     * @param library  the library whose dependencies should be navigated
     * @param visitor  the visitor to be notified when dependencies are encountered
     */
    public static void navigate(AbstractLibrary library, ModelElementVisitor visitor) {
        new SchemaDependencyNavigator(visitor).navigateLibrary(library);

    }

    /**
     * Navigates all dependencies of the given element in a depth-first fashion using the given
     * visitor for notification callbacks.
     * 
     * @param target  the library entity whose dependencies should be navigated
     * @param visitor  the visitor to be notified when dependencies are encountered
     */
    public static void navigate(NamedEntity target, ModelElementVisitor visitor) {
        new SchemaDependencyNavigator(visitor).navigate(target);
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.AbstractNavigator#navigate(java.lang.Object)
     */
    @Override
    public void navigate(NamedEntity target) {
        navigateDependency(target);
    }

    /**
     * Called when a top-level library is encountered during navigation.
     * 
     * @param library  the library whose dependencies should be navigated
     */
    public void navigateLibrary(AbstractLibrary library) {
    	List<LibraryMember> libraryMembers = new ArrayList<>( library.getNamedMembers() );
    	
        for (NamedEntity libraryMember : libraryMembers) {
            if (library instanceof TLLibrary) {
                visitor.visitUserDefinedLibrary((TLLibrary) library);

            } else if (library instanceof XSDLibrary) {
                visitor.visitLegacySchemaLibrary((XSDLibrary) library);

            } else if (library instanceof BuiltInLibrary) {
                visitor.visitBuiltInLibrary((BuiltInLibrary) library);
            }
            navigate(libraryMember);
        }
    }

    /**
     * Called when a <code>TLService</code> instance is encountered during model navigation.
     * 
     * @param service  the service entity to visit and navigate
     */
    protected void navigateService(TLService service) {
        if (canVisit(service) && visitor.visitService(service)) {
            for (TLOperation operation : service.getOperations()) {
                navigateOperation(operation);
            }
        }
    }

    /**
     * Called when a <code>TLOperation</code> instance is encountered during model navigation.
     * 
     * @param operation  the operation entity to visit and navigate
     */
    protected void navigateOperation(TLOperation operation) {
        if (canVisit(operation) && visitor.visitOperation(operation)) {
            navigateFacet(operation.getRequest(), null);
            navigateFacet(operation.getResponse(), null);
            navigateFacet(operation.getNotification(), null);
            navigateExtension(operation.getExtension());
        }
    }

    /**
     * Called when a <code>TLSimple</code> instance is encountered during model navigation.
     * 
     * @param simple  the simple entity to visit and navigate
     */
    protected void navigateSimple(TLSimple simple) {
        if (canVisit(simple) && visitor.visitSimple(simple)) {
            navigateDependency(simple.getParentType());
        }
    }

    /**
     * Called when a <code>TLValueWithAttributes</code> instance is encountered during model
     * navigation.
     * 
     * @param valueWithAttributes  the simple entity to visit and navigate
     */
    protected void navigateValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        if (canVisit(valueWithAttributes) && visitor.visitValueWithAttributes(valueWithAttributes)) {
        	List<TLAttribute> attributes = PropertyCodegenUtils.getInheritedAttributes( valueWithAttributes );
        	TLAttributeType parentType = valueWithAttributes.getParentType();
        	
            for (TLAttribute attribute : attributes) {
                navigateAttribute(attribute);
            }
            while (parentType instanceof TLValueWithAttributes) {
            	parentType = ((TLValueWithAttributes) parentType).getParentType();
            }
            navigateDependency(parentType);
        }
    }

    /**
     * Called when a <code>TLClosedEnumeration</code> instance is encountered during model
     * navigation.
     * 
     * @param enumeration  the enumeration entity to visit and navigate
     */
    protected void navigateClosedEnumeration(TLClosedEnumeration enumeration) {
        if (canVisit(enumeration) && visitor.visitClosedEnumeration(enumeration)) {
        	// No further action required
        }
    }

    /**
     * Called when a <code>TLOpenEnumeration</code> instance is encountered during model navigation.
     * 
     * @param enumeration  the enumeration entity to visit and navigate
     */
    protected void navigateOpenEnumeration(TLOpenEnumeration enumeration) {
        if (canVisit(enumeration) && visitor.visitOpenEnumeration(enumeration)) {
        	// No further action required
        }
    }

    /**
     * Called when a <code>TLChoiceObject</code> instance is encountered during model navigation.
     * 
     * @param choiceObject  the choice object entity to visit and navigate
     * @param alias  the alias of the choice object that is to be navigated
     */
    protected void navigateChoiceObject(TLChoiceObject choiceObject, TLAlias alias) {
    	boolean canVisitChoice = canVisit(choiceObject);
    	boolean canVisitAlias = canVisit(alias);
    	
        if (canVisitChoice || canVisitAlias) {
        	if (canVisitChoice) visitor.visitChoiceObject(choiceObject);
        	if (canVisitAlias) visitor.visitAlias(alias);
        	TLAlias sharedAlias = (alias == null) ? null : AliasCodegenUtils.getFacetAlias( alias, TLFacetType.SHARED );
        	
            navigateFacet(choiceObject.getSharedFacet(), sharedAlias);

            for (TLContextualFacet choiceFacet : choiceObject.getChoiceFacets()) {
            	TLAlias choiceAlias = (alias == null) ? null :
            			AliasCodegenUtils.getFacetAlias( alias, TLFacetType.CHOICE, choiceFacet.getName() );
            	
                navigateContextualFacet(choiceFacet, choiceAlias);
            }
        }
    }

    /**
     * Called when a <code>TLCoreObject</code> instance is encountered during model navigation.
     * 
     * @param coreObject  the core object entity to visit and navigate
     * @param alias  the alias of the core object that is to be navigated
     */
    protected void navigateCoreObject(TLCoreObject coreObject, TLAlias alias) {
    	boolean canVisitCore = canVisit(coreObject);
    	boolean canVisitAlias = canVisit(alias);
    	
        if (canVisitCore || canVisitAlias) {
        	if (canVisitCore) visitor.visitCoreObject(coreObject);
        	if (canVisitAlias) visitor.visitAlias(alias);
        	TLAlias summaryAlias = (alias == null) ? null : AliasCodegenUtils.getFacetAlias( alias, TLFacetType.SUMMARY );
        	TLAlias detailAlias = (alias == null) ? null : AliasCodegenUtils.getFacetAlias( alias, TLFacetType.DETAIL );
        	
            navigateSimpleFacet(coreObject.getSimpleFacet());
            navigateFacet(coreObject.getSummaryFacet(), summaryAlias);
            navigateFacet(coreObject.getDetailFacet(), detailAlias);
            navigateListFacet(coreObject.getSimpleListFacet(), null);
            navigateListFacet(coreObject.getSummaryListFacet(), null);
            navigateListFacet(coreObject.getDetailListFacet(), null);
        }
    }
    
    /**
     * Called when a <code>TLBusinessObject</code> instance is encountered during model navigation.
     * 
     * @param businessObject  the business object entity to visit and navigate
     * @param alias  the alias of the business object that is to be navigated
     */
    protected void navigateBusinessObject(TLBusinessObject businessObject, TLAlias alias) {
    	boolean canVisitBO = canVisit(businessObject);
    	boolean canVisitAlias = canVisit(alias);
    	
        if (canVisitBO || canVisitAlias) {
        	if (canVisitBO) visitor.visitBusinessObject(businessObject);
        	if (canVisitAlias) visitor.visitAlias(alias);
        	TLAlias idAlias = (alias == null) ? null : AliasCodegenUtils.getFacetAlias( alias, TLFacetType.ID );
        	TLAlias summaryAlias = (alias == null) ? null : AliasCodegenUtils.getFacetAlias( alias, TLFacetType.SUMMARY );
        	TLAlias detailAlias = (alias == null) ? null : AliasCodegenUtils.getFacetAlias( alias, TLFacetType.DETAIL );
        	
            navigateFacet(businessObject.getIdFacet(), idAlias);
            navigateFacet(businessObject.getSummaryFacet(), summaryAlias);
            navigateFacet(businessObject.getDetailFacet(), detailAlias);

            for (TLContextualFacet customFacet : businessObject.getCustomFacets()) {
            	TLAlias customAlias = (alias == null) ? null :
            			AliasCodegenUtils.getFacetAlias( alias, TLFacetType.CUSTOM, customFacet.getName() );
            	
            	navigateContextualFacet(customFacet, customAlias);
            }
            for (TLContextualFacet queryFacet : businessObject.getQueryFacets()) {
            	TLAlias queryAlias = (alias == null) ? null :
            			AliasCodegenUtils.getFacetAlias( alias, TLFacetType.QUERY, queryFacet.getName() );
        	
            	navigateContextualFacet(queryFacet, queryAlias);
            }
            for (TLContextualFacet updateFacet : businessObject.getUpdateFacets()) {
            	TLAlias updateAlias = (alias == null) ? null :
        				AliasCodegenUtils.getFacetAlias( alias, TLFacetType.UPDATE, updateFacet.getName() );
        	
            	navigateContextualFacet(updateFacet, updateAlias);
            }
        }
    }

    /**
     * Called when a <code>TLResource</code> instance is encountered during model navigation.
     * 
     * @param resource  the resource entity to visit and navigate
     */
    protected void navigateResource(TLResource resource) {
        if (canVisit(resource) && visitor.visitResource(resource)) {
            navigateBusinessObject(resource.getBusinessObjectRef(), null);
            
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
     * Called when a <code>TLParamGroup</code> instance is encountered during model navigation.
     * 
     * @param paramGroup  the parameter group entity to visit and navigate
     */
    protected void navigateParamGroup(TLParamGroup paramGroup) {
        if (canVisit(paramGroup) && visitor.visitParamGroup(paramGroup)) {
        	navigateFacet(paramGroup.getFacetRef(), null);
            
            for (TLParameter parameter : paramGroup.getParameters()) {
            	navigateParameter(parameter);
            }
        }
        addVisitedNode(paramGroup);
    }
    
    /**
     * Called when a <code>TLParameter</code> instance is encountered during model navigation.
     * 
     * @param parameter  the parameter entity to visit and navigate
     */
    protected void navigateParameter(TLParameter parameter) {
        if (canVisit(parameter) && visitor.visitParameter(parameter)) {
        	TLMemberField<?> fieldRef = parameter.getFieldRef();
        	
        	if (fieldRef instanceof TLAttribute) {
        		navigateAttribute((TLAttribute) fieldRef);
        		
        	} else if (fieldRef instanceof TLProperty) {
        		navigateElement((TLProperty) fieldRef);
        		
        	} else if (fieldRef instanceof TLIndicator) {
        		navigateIndicator((TLIndicator) fieldRef);
        	}
        }
        addVisitedNode(parameter);
    }
    
    /**
     * Called when a <code>TLAction</code> instance is encountered during model navigation.
     * 
     * @param action  the action entity to visit and navigate
     */
    protected void navigateAction(TLAction action) {
        if (canVisit(action) && visitor.visitAction(action)) {
        	if (action.getRequest() != null) {
            	navigate( action.getRequest().getPayloadType() );
        	}
            
            for (TLActionResponse response : action.getResponses()) {
            	navigate( response.getPayloadType() );
            }
        }
        addVisitedNode(action);
    }
    
    /**
     * Called when a <code>TLExtensionPointFacet</code> instance is encountered during model
     * navigation.
     * 
     * @param extensionPointFacet  the extension point facet entity to visit and navigate
     */
    protected void navigateExtensionPointFacet(TLExtensionPointFacet extensionPointFacet) {
        if (canVisit(extensionPointFacet) && visitor.visitExtensionPointFacet(extensionPointFacet)) {
            if (extensionPointFacet.getExtension() != null) {
                navigateExtension(extensionPointFacet.getExtension());
            }
            for (TLAttribute attribute : extensionPointFacet.getAttributes()) {
                navigateAttribute(attribute);
            }
            for (TLProperty element : extensionPointFacet.getElements()) {
                navigateElement(element);
            }
            for (TLIndicator indicator : extensionPointFacet.getIndicators()) {
                navigateIndicator(indicator);
            }
        }
    }

    /**
     * Called when a n<code>XSDLibrary</code> instance is encountered during model navigation.
     * 
     * @param xsdLibrary  the XSD library to visit and navigate
     */
    protected void navigateXSDLibrary(XSDLibrary xsdLibrary) {
        if (canVisit(xsdLibrary) && (xsdLibrary.getOwningModel() != null)
                && visitor.visitLegacySchemaLibrary(xsdLibrary)) {

            for (TLInclude include : xsdLibrary.getIncludes()) {
                if (include.getPath() != null) {
                    URL includedUrl = getReferencedLibraryURL(include.getPath(), xsdLibrary);
                    AbstractLibrary includedLibrary = xsdLibrary.getOwningModel().getLibrary(includedUrl);

                    if ((includedLibrary != null) && (includedLibrary instanceof XSDLibrary)) {
                        navigateXSDLibrary((XSDLibrary) includedLibrary);
                    }
                }
            }

            for (TLNamespaceImport nsImport : xsdLibrary.getNamespaceImports()) {
                if (nsImport.getFileHints() != null) {
                    for (String fileHint : nsImport.getFileHints()) {
                        URL importedUrl = getReferencedLibraryURL(fileHint, xsdLibrary);
                        AbstractLibrary importedLibrary = xsdLibrary.getOwningModel().getLibrary(importedUrl);

                        if ((importedLibrary != null) && (importedLibrary instanceof XSDLibrary)) {
                            navigateXSDLibrary((XSDLibrary) importedLibrary);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the full URL that is referenced by the specified relative URL path.
     * 
     * @param relativeUrl  the relative URL path to resolve
     * @param referringLibrary  the library that is the
     * @return
     */
    private URL getReferencedLibraryURL(String relativeUrl, AbstractLibrary referringLibrary) {
        URL resolvedUrl = null;
        try {
            URL libraryFolderUrl = URLUtils.getParentURL(referringLibrary.getLibraryUrl());
            resolvedUrl = URLUtils.getResolvedURL(relativeUrl, libraryFolderUrl);

        } catch (MalformedURLException e) {
            // no error - return a null URL
        }
        return resolvedUrl;
    }

    /**
     * Called when an <code>XSDSimpleType</code> instance is encountered during model navigation.
     * 
     * @param xsdSimple  the XSD simple-type entity to visit and navigate
     */
    protected void navigateXSDSimpleType(XSDSimpleType xsdSimple) {
        if (canVisit(xsdSimple) && visitor.visitXSDSimpleType(xsdSimple)) {
            AbstractLibrary owningLibrary = xsdSimple.getOwningLibrary();

            if (owningLibrary instanceof XSDLibrary) {
                navigateXSDLibrary((XSDLibrary) owningLibrary);
            }
        }
    }

    /**
     * Called when a <code>XSDComplexType</code> instance is encountered during model navigation.
     * 
     * @param xsdComplex  the XSD complex-type entity to visit and navigate
     */
    protected void navigateXSDComplexType(XSDComplexType xsdComplex) {
        if (canVisit(xsdComplex) && visitor.visitXSDComplexType(xsdComplex)) {
            AbstractLibrary owningLibrary = xsdComplex.getOwningLibrary();

            if (owningLibrary instanceof XSDLibrary) {
                navigateXSDLibrary((XSDLibrary) owningLibrary);
            }
            navigateDependency(xsdComplex.getIdentityAlias());

            for (XSDElement aliasElement : xsdComplex.getAliases()) {
                navigateDependency(aliasElement);
            }
        }
    }

    /**
     * Called when an <code>XSDElement</code> instance is encountered during model navigation.
     * 
     * @param xsdElement  the XSD element entity to visit and navigate
     */
    protected void navigateXSDElement(XSDElement xsdElement) {
        if (canVisit(xsdElement) && visitor.visitXSDElement(xsdElement)) {
            AbstractLibrary owningLibrary = xsdElement.getOwningLibrary();

            if (owningLibrary instanceof XSDLibrary) {
                navigateXSDLibrary((XSDLibrary) owningLibrary);
            }
            navigateDependency(xsdElement.getAliasedType());
        }
    }

    /**
     * Called when a <code>TLExtension</code> instance is encountered during model navigation.
     * 
     * @param extension  the extension entity to visit and navigate
     */
    protected void navigateExtension(TLExtension extension) {
        if (canVisit(extension) && visitor.visitExtension(extension)) {
            navigateDependency(extension.getExtendsEntity());
        }
    }

    /**
     * Called when a <code>TLFacet</code> instance is encountered during model navigation.
     * 
     * @param facet  the facet entity to visit and navigate
     * @param alias  the alias of the facet that is to be navigated
     */
    protected void navigateFacet(TLFacet facet, TLAlias alias) {
    	boolean canVisitFacet = canVisit(facet);
    	boolean canVisitAlias = canVisit(alias);
    	
        if (canVisitFacet || canVisitAlias) {
        	if (canVisitFacet) visitor.visitFacet(facet);
        	if (canVisitAlias) visitor.visitAlias(alias);
        	TLFacetOwner facetOwner = facet.getOwningEntity();
        	TLFacetType facetType = facet.getFacetType();
        	
        	navigateFacetMembers(facet);
        	
        	if (facetOwner instanceof TLCoreObject) {
        		switch (facetType) {
        			case DETAIL:
        				if (alias != null) {
        					navigateAlias( AliasCodegenUtils.getSiblingAlias( alias, TLFacetType.SUMMARY ) );
        				} else {
            				navigateFacet( ((TLCoreObject) facetOwner).getSummaryFacet(), null );
        				}
                		break;
        			default:
        				break;
        		}
        		
        	} else if (facetOwner instanceof TLChoiceObject) {
        		switch (facetType) {
        			case CHOICE:
        				if (alias != null) {
        					navigateAlias( AliasCodegenUtils.getSiblingAlias( alias, TLFacetType.SHARED ) );
        				} else {
            				navigateFacet( ((TLChoiceObject) facetOwner).getSharedFacet(), null );
        				}
                		break;
        			default:
        				break;
        		}
        		
        	} else if (facetOwner instanceof TLBusinessObject) {
        		switch (facetType) {
        			case DETAIL:
        			case CUSTOM:
        				if (alias != null) {
        					navigateAlias( AliasCodegenUtils.getSiblingAlias( alias, TLFacetType.SUMMARY ) );
        				} else {
            				navigateFacet( ((TLBusinessObject) facetOwner).getSummaryFacet(), null );
        				}
                		break;
        			case SUMMARY:
        				if (alias != null) {
        					navigateAlias( AliasCodegenUtils.getSiblingAlias( alias, TLFacetType.ID ) );
        				} else {
            				navigateFacet( ((TLBusinessObject) facetOwner).getIdFacet(), null );
        				}
                		break;
        			default:
        				break;
        		}
        	}
        }
    }

    /**
     * Called when a <code>TLContextualFacet</code> instance is encountered during model navigation.
     * 
     * @param facet  the facet entity to visit and navigate
     * @param alias  the alias of the facet that is to be navigated
     */
    protected void navigateContextualFacet(TLContextualFacet facet, TLAlias alias) {
    	boolean canVisitFacet = canVisit(facet);
    	boolean canVisitAlias = canVisit(alias);
    	
        if (canVisitFacet || canVisitAlias) {
        	if (canVisitFacet) visitor.visitContextualFacet(facet);
        	if (canVisitAlias) visitor.visitAlias(alias);
        	TLFacetOwner facetOwner = facet.getOwningEntity();
        	
        	navigateFacetMembers(facet);
        	
        	if (facetOwner instanceof TLContextualFacet) {
        		if (alias != null) {
        			navigateAlias( AliasCodegenUtils.getOwnerAlias( alias ) );
        		} else {
            		navigateContextualFacet( (TLContextualFacet) facetOwner, null );
        		}
        		
        	} else if (facetOwner instanceof TLChoiceObject) {
        		if (alias != null) {
        			navigateAlias( AliasCodegenUtils.getSiblingAlias( alias, TLFacetType.SHARED ) );
        		} else {
            		navigateFacet( ((TLChoiceObject) facetOwner).getSharedFacet(), null );
        		}
        		
        	} else if (facetOwner instanceof TLBusinessObject) {
        		if (alias != null) {
        			navigateAlias( AliasCodegenUtils.getSiblingAlias( alias, TLFacetType.SUMMARY ) );
        		} else {
            		navigateFacet( ((TLBusinessObject) facetOwner).getSummaryFacet(), null );
        		}
        	}
        }
    }
    
    /**
     * Navigates all field members and aliases as well as the owner of the given facet.
     * 
     * @param facet  the facet whose members are to be navigated
     */
    private void navigateFacetMembers(TLFacet facet) {
        for (TLAttribute attribute : PropertyCodegenUtils.getInheritedAttributes(facet)) {
            navigateAttribute(attribute);
        }
        for (TLProperty element : PropertyCodegenUtils.getInheritedProperties(facet)) {
            navigateElement(element);
        }
        for (TLIndicator indicator : PropertyCodegenUtils.getInheritedIndicators(facet)) {
            navigateIndicator(indicator);
        }
    }

    /**
     * Called when a <code>TLActionFacet</code> instance is encountered during model navigation.
     * 
     * @param actionFacet  the action facet entity to visit and navigate
     */
    protected void navigateActionFacet(TLActionFacet actionFacet) {
        if (canVisit(actionFacet) && visitor.visitActionFacet(actionFacet)) {
        	NamedEntity basePayload = actionFacet.getBasePayload();
        	
        	if (basePayload != null) {
        		if (basePayload instanceof TLCoreObject) {
        			TLCoreObject corePayload = (TLCoreObject) basePayload;
        			
        			if (actionFacet.getReferenceType() == TLReferenceType.NONE) {
        				navigateCoreObject( corePayload, null );
        				
        			} else {
            			navigateFacetMembers( corePayload.getSummaryFacet() );
            			navigateFacetMembers( corePayload.getDetailFacet() );
        			}
        			
        		} else if (basePayload instanceof TLChoiceObject) {
        			TLChoiceObject choicePayload = (TLChoiceObject) basePayload;
        			
        			if (actionFacet.getReferenceType() == TLReferenceType.NONE) {
        				navigateChoiceObject( choicePayload, null );
        				
        			} else {
            			navigateFacetMembers( choicePayload.getSharedFacet() );
            			
            			for (TLContextualFacet choiceFacet : choicePayload.getChoiceFacets()) {
            				do {
            					TLFacetOwner facetOwner = choiceFacet.getOwningEntity();
            					
            					navigateFacetMembers( choiceFacet );
            					choiceFacet = (facetOwner instanceof TLContextualFacet) ? (TLContextualFacet) facetOwner : null;
            					
            				} while (choiceFacet != null);
            			}
        			}
        		}
        	}
            
            // Navigate the resource's business object (if required)
            if (actionFacet.getReferenceType() != TLReferenceType.NONE) {
            	TLResource resource = actionFacet.getOwningResource();
            	TLBusinessObject boRef = (resource == null) ? null : resource.getBusinessObjectRef();
            	
            	if (boRef != null) {
        			TLFacet boFacet = ResourceCodegenUtils.getReferencedFacet( boRef, actionFacet.getReferenceFacetName() );
        			
        			if (boFacet != null) {
        				if (boFacet instanceof TLContextualFacet) {
        					navigateContextualFacet( (TLContextualFacet) boFacet, null );
        					
        				} else {
        					navigateFacet( boFacet, null );
        				}
        				
        			} else {
            			navigateBusinessObject( boRef, null );
        			}
            	}
            }
        }
        addVisitedNode(actionFacet);
    }
    
    /**
     * Called when a <code>TLSimpleFacet</code> instance is encountered during model navigation.
     * 
     * @param simpleFacet  the simple facet entity to visit and navigate
     */
    protected void navigateSimpleFacet(TLSimpleFacet simpleFacet) {
        if (canVisit(simpleFacet) && visitor.visitSimpleFacet(simpleFacet)) {
            navigateDependency(simpleFacet.getSimpleType());
        }
    }

    /**
     * Called when a <code>TLListFacet</code> instance is encountered during model navigation.
     * 
     * @param listFacet  the list facet entity to visit and navigate
     * @param alias  the list facet alias to be visited
     */
    protected void navigateListFacet(TLListFacet listFacet, TLAlias alias) {
    	boolean canVisitFacet = canVisit(listFacet);
    	boolean canVisitAlias = canVisit(alias);
    	
        if (canVisitFacet || canVisitAlias) {
        	if (canVisitFacet) visitor.visitListFacet(listFacet);
        	if (canVisitAlias) visitor.visitAlias(alias);
        	TLAbstractFacet itemFacet = listFacet.getItemFacet();
        	
        	if (itemFacet instanceof TLFacet) {
        		TLAlias itemFacetAlias = (alias == null) ?
        				null : AliasCodegenUtils.getItemFacetAlias( alias );
        		
        		navigateFacet( (TLFacet) itemFacet, itemFacetAlias );
        		
        	} else {
                navigateDependency(itemFacet);
        	}
        }
    }

    /**
     * Called when a <code>TLAlias</code> instance is encountered during model navigation.
     * 
     * @param alias  the alias entity to visit and navigate
     */
    protected void navigateAlias(TLAlias alias) {
    	TLAliasOwner owner = alias.getOwningEntity();
    	
    	if (owner instanceof TLCoreObject) {
    		navigateCoreObject( (TLCoreObject) owner, alias );
    		
    	} else if (owner instanceof TLChoiceObject) {
    		navigateChoiceObject( (TLChoiceObject) owner, alias );
    		
    	} else if (owner instanceof TLBusinessObject) {
    		navigateBusinessObject( (TLBusinessObject) owner, alias );
    		
    	} else if (owner instanceof TLContextualFacet) {
    		navigateContextualFacet( (TLContextualFacet) owner, alias );
    		
    	} else if (owner instanceof TLFacet) {
    		navigateFacet( (TLFacet) owner, alias );
    		
    	} else if (owner instanceof TLListFacet) {
    		navigateListFacet( (TLListFacet) owner, alias );
    	}
    }

    /**
     * Called when a <code>TLRole</code> instance is encountered during model navigation.
     * 
     * @param role  the role entity to visit and navigate
     */
    protected void navigateRole(TLRole role) {
        if (canVisit(role) && visitor.visitRole(role)) {
        	// No further action required
        }
    }

    /**
     * Called when a <code>TLAttribute</code> instance is encountered during model navigation.
     * 
     * @param attribute  the attribute entity to visit and navigate
     */
    protected void navigateAttribute(TLAttribute attribute) {
        if (canVisit(attribute) && visitor.visitAttribute(attribute)) {
            navigateDependency(attribute.getType());
        }
    }

    /**
     * Called when a <code>TLProperty</code> instance is encountered during model navigation.
     * 
     * @param element  the element entity to visit and navigate
     */
    protected void navigateElement(TLProperty element) {
        if (canVisit(element) && visitor.visitElement(element)) {
            navigateDependency(element.getType());
        }
    }

    /**
     * Called when a <code>TLIndicator</code> instance is encountered during model navigation.
     * 
     * @param indicator  the indicator entity to visit and navigate
     */
    protected void navigateIndicator(TLIndicator indicator) {
        if (canVisit(indicator) && visitor.visitIndicator(indicator)) {
        	// No further action required
        }
    }

    /**
     * Navigates the given named entity and (if necessary) any of the entities it references as
     * dependencies.
     * 
     * @param entity  the entity whose dependencies to navigate
     */
    public void navigateDependency(NamedEntity entity) {
        if (entity instanceof TLSimple) {
            navigateSimple((TLSimple) entity);

        } else if (entity instanceof TLValueWithAttributes) {
            navigateValueWithAttributes((TLValueWithAttributes) entity);

        } else if (entity instanceof TLClosedEnumeration) {
            navigateClosedEnumeration((TLClosedEnumeration) entity);

        } else if (entity instanceof TLOpenEnumeration) {
            navigateOpenEnumeration((TLOpenEnumeration) entity);

        } else if (entity instanceof TLChoiceObject) {
            navigateChoiceObject((TLChoiceObject) entity, null);

        } else if (entity instanceof TLCoreObject) {
            navigateCoreObject((TLCoreObject) entity, null);

        } else if (entity instanceof TLBusinessObject) {
            navigateBusinessObject((TLBusinessObject) entity, null);

        } else if (entity instanceof TLResource) {
            navigateResource((TLResource) entity);

        } else if (entity instanceof TLActionFacet) {
            navigateActionFacet((TLActionFacet) entity);

        } else if (entity instanceof XSDSimpleType) {
            navigateXSDSimpleType((XSDSimpleType) entity);

        } else if (entity instanceof XSDComplexType) {
            navigateXSDComplexType((XSDComplexType) entity);

        } else if (entity instanceof XSDElement) {
            navigateXSDElement((XSDElement) entity);

        } else if (entity instanceof TLContextualFacet) {
            navigateContextualFacet((TLContextualFacet) entity, null);

        } else if (entity instanceof TLFacet) {
            navigateFacet((TLFacet) entity, null);

        } else if (entity instanceof TLActionFacet) {
            navigateActionFacet((TLActionFacet) entity);

        } else if (entity instanceof TLSimpleFacet) {
            navigateSimpleFacet((TLSimpleFacet) entity);

        } else if (entity instanceof TLListFacet) {
            navigateListFacet((TLListFacet) entity, null);

        } else if (entity instanceof TLAlias) {
            navigateAlias((TLAlias) entity);

        } else if (entity instanceof TLService) {
            navigateService((TLService) entity);

        } else if (entity instanceof TLOperation) {
            navigateOperation((TLOperation) entity);

        } else if (entity instanceof TLExtensionPointFacet) {
            navigateExtensionPointFacet((TLExtensionPointFacet) entity);
        }
    }

}
