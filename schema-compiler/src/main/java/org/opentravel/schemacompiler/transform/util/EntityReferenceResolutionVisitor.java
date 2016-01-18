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
package org.opentravel.schemacompiler.transform.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.validate.impl.TLModelSymbolResolver;
import org.opentravel.schemacompiler.visitor.ModelElementVisitor;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Visitor that attempts to resolve null entity references discovered in a model after new libraries
 * are loaded.
 * 
 * @author S. Livezey
 */
public class EntityReferenceResolutionVisitor extends ModelElementVisitorAdapter {

    private SymbolResolver symbolResolver;
    private Map<String,List<TLMemberField<?>>> inheritedFieldCache = new HashMap<>();
    
    /**
     * Constructor that assigns the model being navigated.
     * 
     * @param model
     *            the model from which all entity names will be obtained
     */
    public EntityReferenceResolutionVisitor(TLModel model) {
        this(new TLModelSymbolResolver(model));
    }
    
    /**
     * Constructor that assigns the model being navigated.
     * 
     * @param symbolResolver
     *            the symbol resolver to use for all named entity lookups
     */
    public EntityReferenceResolutionVisitor(SymbolResolver symbolResolver) {
        this.symbolResolver = symbolResolver;
    }
    
    /**
     * Visits and resolves the parameters of each library resource.  This covers
     * an edge case that causes some parameters not to be resolved on the initial
     * visit since some of the field references may not yet have been resolved on
     * the first pass.
     * 
     * @param model  the model for which to resolve parameter field references
     */
    public static void resolveParameters(TLModel model) {
    	ModelElementVisitor visitor = new EntityReferenceResolutionVisitor(model);
    	
        for (TLLibrary library : model.getUserDefinedLibraries()) {
        	for (TLResource resource : library.getResourceTypes()) {
        		for (TLParamGroup paramGroup : resource.getParamGroups()) {
        			for (TLParameter param : paramGroup.getParameters()) {
                		visitor.visitParameter(param);
        			}
        		}
        	}
        }
    }
    
    /**
     * Assigns the context library for the symbol resolver.
     * 
     * @param library
     *            the library to assign as the current lookup context
     */
    public void assignContextLibrary(AbstractLibrary library) {
        symbolResolver.setPrefixResolver(new LibraryPrefixResolver(library));
        symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(library));
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitBuiltInLibrary(org.opentravel.schemacompiler.model.BuiltInLibrary)
     */
    @Override
    public boolean visitBuiltInLibrary(BuiltInLibrary library) {
        assignContextLibrary(library);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitLegacySchemaLibrary(org.opentravel.schemacompiler.model.XSDLibrary)
     */
    @Override
    public boolean visitLegacySchemaLibrary(XSDLibrary library) {
        assignContextLibrary(library);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitUserDefinedLibrary(org.opentravel.schemacompiler.model.TLLibrary)
     */
    @Override
    public boolean visitUserDefinedLibrary(TLLibrary library) {
        assignContextLibrary(library);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
     */
    @Override
    public boolean visitSimple(TLSimple simple) {
        if ((simple.getParentType() == null) && (simple.getParentTypeName() != null)) {
            Object ref = symbolResolver.resolveEntity(simple.getParentTypeName());

            if (ref instanceof TLAttributeType) {
                simple.setParentType((TLAttributeType) ref);
            }
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
     */
    @Override
    public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        if ((valueWithAttributes.getParentType() == null)
                && (valueWithAttributes.getParentTypeName() != null)) {
            Object ref = symbolResolver.resolveEntity(valueWithAttributes.getParentTypeName());

            if (ref instanceof TLAttributeType) {
                valueWithAttributes.setParentType((TLAttributeType) ref);
            }
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
     */
    @Override
    public boolean visitExtension(TLExtension extension) {
        if ((extension.getExtendsEntity() == null) && (extension.getExtendsEntityName() != null)) {
            Object ref;

            if (extension.getOwner() instanceof TLOperation) {
                ref = symbolResolver.resolveOperationEntity(extension.getExtendsEntityName());

            } else {
                ref = symbolResolver.resolveEntity(extension.getExtendsEntityName());
            }

            if (ref instanceof NamedEntity) {
                extension.setExtendsEntity((NamedEntity) ref);
            }
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
     */
    @Override
    public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
        if ((simpleFacet.getSimpleType() == null) && (simpleFacet.getSimpleTypeName() != null)) {
            Object ref = symbolResolver.resolveEntity(simpleFacet.getSimpleTypeName());

            if (ref instanceof NamedEntity) {
                simpleFacet.setSimpleType((NamedEntity) ref);
            }
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public boolean visitAttribute(TLAttribute attribute) {
        if ((attribute.getType() == null) && (attribute.getTypeName() != null)) {
            Object ref = symbolResolver.resolveEntity(attribute.getTypeName());

            if (ref instanceof TLAttributeType) {
                attribute.setType((TLAttributeType) ref);
            }
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public boolean visitElement(TLProperty element) {
        if ((element.getType() == null) && (element.getTypeName() != null)) {
            Object ref = symbolResolver.resolveEntity(element.getTypeName());

            if (ref instanceof TLPropertyType) {
                element.setType((TLPropertyType) ref);
            }
        }
        return true;
    }

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
	 */
	@Override
	public boolean visitResource(TLResource resource) {
        if ((resource.getBusinessObjectRef() == null) && (resource.getBusinessObjectRefName() != null)) {
            Object ref = symbolResolver.resolveEntity(resource.getBusinessObjectRefName());

            if (ref instanceof TLBusinessObject) {
            	resource.setBusinessObjectRef((TLBusinessObject) ref);
            }
        }
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResourceParentRef(org.opentravel.schemacompiler.model.TLResourceParentRef)
	 */
	@Override
	public boolean visitResourceParentRef(TLResourceParentRef parentRef) {
        if ((parentRef.getParentResource() == null) && (parentRef.getParentResourceName() != null)) {
            Object ref = symbolResolver.resolveEntity(parentRef.getParentResourceName());

            if (ref instanceof TLResource) {
            	parentRef.setParentResource((TLResource) ref);
            }
        }
        
        if ((parentRef.getParentResource() != null) &&
        		(parentRef.getParentParamGroup() == null) && (parentRef.getParentParamGroupName() != null)) {
        	List<TLParamGroup> paramGroups = ResourceCodegenUtils.getInheritedParamGroups(parentRef.getParentResource());
        	String pgName = parentRef.getParentParamGroupName();
        	
        	for (TLParamGroup paramGroup : paramGroups) {
        		if (pgName.equals(paramGroup.getName())) {
        			parentRef.setParentParamGroup(paramGroup);
        			break;
        		}
        	}
        }
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParamGroup(org.opentravel.schemacompiler.model.TLParamGroup)
	 */
	@Override
	public boolean visitParamGroup(TLParamGroup paramGroup) {
        if ((paramGroup.getFacetRef() == null) && (paramGroup.getFacetRefName() != null)) {
            Object ref = symbolResolver.resolveEntity(paramGroup.getFacetRefName());

            if (ref instanceof TLFacet) {
            	paramGroup.setFacetRef((TLFacet) ref);
            }
        }
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParameter(org.opentravel.schemacompiler.model.TLParameter)
	 */
	@Override
	public boolean visitParameter(TLParameter parameter) {
        if ((parameter.getOwner() != null) && (parameter.getOwner().getFacetRef() != null) &&
        		(parameter.getFieldRef() == null) && (parameter.getFieldRefName() != null)) {
        	TLFacet facetRef = parameter.getOwner().getFacetRef();
        	String facetRefKey = facetRef.getNamespace() + ":" + facetRef.getLocalName();
        	List<TLMemberField<?>> memberFields = inheritedFieldCache.get(facetRefKey);
        	String fieldName = parameter.getFieldRefName();
        	
            if (memberFields == null) {
            	memberFields = ResourceCodegenUtils.getAllParameterFields( facetRef );
            	inheritedFieldCache.put(facetRefKey, memberFields);
            }
            
            for (TLMemberField<?> memberField : memberFields) {
            	if (fieldName.equals(memberField.getName())) {
            		parameter.setFieldRef(memberField);
            		break;
            	}
            }
        }
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionFacet(org.opentravel.schemacompiler.model.TLActionFacet)
	 */
	@Override
	public boolean visitActionFacet(TLActionFacet facet) {
        if ((facet.getOwningResource() != null) && (facet.getBasePayload() == null)
        		&& (facet.getBasePayloadName() != null)) {
            Object ref = symbolResolver.resolveEntity(facet.getBasePayloadName());

            if (ref instanceof NamedEntity) {
            	facet.setBasePayload((NamedEntity) ref);
            }
        }
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionRequest(org.opentravel.schemacompiler.model.TLActionRequest)
	 */
	@Override
	public boolean visitActionRequest(TLActionRequest actionRequest) {
        if ((actionRequest.getOwner() != null) && (actionRequest.getOwner().getOwner() != null) &&
        		(actionRequest.getParamGroup() == null) && (actionRequest.getParamGroupName() != null)) {
        	TLResource owningResource = actionRequest.getOwner().getOwner();
        	List<TLParamGroup> paramGroups = ResourceCodegenUtils.getInheritedParamGroups(owningResource);
        	String pgName = actionRequest.getParamGroupName();
        	
        	for (TLParamGroup paramGroup : paramGroups) {
        		if (pgName.equals(paramGroup.getName())) {
        			actionRequest.setParamGroup(paramGroup);
        			break;
        		}
        	}
        }
        if ((actionRequest.getPayloadType() == null) && (actionRequest.getPayloadTypeName() != null)) {
            Object ref = symbolResolver.resolveEntity(actionRequest.getPayloadTypeName());

            if (ref instanceof TLActionFacet) {
            	actionRequest.setPayloadType((TLActionFacet) ref);
            }
        }
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionResponse(org.opentravel.schemacompiler.model.TLActionResponse)
	 */
	@Override
	public boolean visitActionResponse(TLActionResponse actionResponse) {
        if ((actionResponse.getPayloadType() == null) && (actionResponse.getPayloadTypeName() != null)) {
            Object ref = symbolResolver.resolveEntity(actionResponse.getPayloadTypeName());

            if (ref instanceof TLActionFacet) {
            	actionResponse.setPayloadType((TLActionFacet) ref);
            }
        }
        return true;
	}

}
