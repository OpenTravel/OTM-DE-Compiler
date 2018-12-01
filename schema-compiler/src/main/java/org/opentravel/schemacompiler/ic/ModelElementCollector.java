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
package org.opentravel.schemacompiler.ic;

import java.util.Collection;
import java.util.HashSet;

import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
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
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Visitor used to collect all of the named entities contained in the library that was removed from
 * the model.
 * 
 * @author S. Livezey
 */
public class ModelElementCollector extends ModelElementVisitorAdapter {

    private Collection<TLModelElement> libraryEntities = new HashSet<>();

    /**
     * Returns the list of named entities that were collected during library navigation.
     * 
     * @return Collection<TLModelElement>
     */
    public Collection<TLModelElement> getLibraryEntities() {
        return libraryEntities;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
     */
    @Override
    public boolean visitSimple(TLSimple simple) {
        libraryEntities.add(simple);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
     */
    @Override
    public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        libraryEntities.add(valueWithAttributes);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitClosedEnumeration(org.opentravel.schemacompiler.model.TLClosedEnumeration)
     */
    @Override
    public boolean visitClosedEnumeration(TLClosedEnumeration enumeration) {
        libraryEntities.add(enumeration);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitOpenEnumeration(org.opentravel.schemacompiler.model.TLOpenEnumeration)
     */
    @Override
    public boolean visitOpenEnumeration(TLOpenEnumeration enumeration) {
        libraryEntities.add(enumeration);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitCoreObject(org.opentravel.schemacompiler.model.TLCoreObject)
     */
    @Override
    public boolean visitCoreObject(TLCoreObject coreObject) {
        libraryEntities.add(coreObject);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitBusinessObject(org.opentravel.schemacompiler.model.TLBusinessObject)
     */
    @Override
    public boolean visitBusinessObject(TLBusinessObject businessObject) {
        libraryEntities.add(businessObject);
        return true;
    }

    /**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitChoiceObject(org.opentravel.schemacompiler.model.TLChoiceObject)
	 */
	@Override
	public boolean visitChoiceObject(TLChoiceObject choiceObject) {
        libraryEntities.add(choiceObject);
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitor#visitContextualFacet(org.opentravel.schemacompiler.model.TLContextualFacet)
	 */
	@Override
	public boolean visitContextualFacet(TLContextualFacet facet) {
        libraryEntities.add(facet);
		return false;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResource(org.opentravel.schemacompiler.model.TLResource)
	 */
	@Override
	public boolean visitResource(TLResource resource) {
        libraryEntities.add(resource);
        return true;
	}

	/**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitOperation(org.opentravel.schemacompiler.model.TLOperation)
     */
    @Override
    public boolean visitOperation(TLOperation operation) {
        libraryEntities.add(operation);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDSimpleType(org.opentravel.schemacompiler.model.XSDSimpleType)
     */
    @Override
    public boolean visitXSDSimpleType(XSDSimpleType xsdSimple) {
        libraryEntities.add(xsdSimple);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDComplexType(org.opentravel.schemacompiler.model.XSDComplexType)
     */
    @Override
    public boolean visitXSDComplexType(XSDComplexType xsdComplex) {
        libraryEntities.add(xsdComplex);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitXSDElement(org.opentravel.schemacompiler.model.XSDElement)
     */
    @Override
    public boolean visitXSDElement(XSDElement xsdElement) {
        libraryEntities.add(xsdElement);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitFacet(org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public boolean visitFacet(TLFacet facet) {
        libraryEntities.add(facet);
        return true;
    }

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionFacet(org.opentravel.schemacompiler.model.TLActionFacet)
	 */
	@Override
	public boolean visitActionFacet(TLActionFacet facet) {
        libraryEntities.add(facet);
        return true;
	}

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
     */
    @Override
    public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
        libraryEntities.add(simpleFacet);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitListFacet(org.opentravel.schemacompiler.model.TLListFacet)
     */
    @Override
    public boolean visitListFacet(TLListFacet listFacet) {
        libraryEntities.add(listFacet);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAlias(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public boolean visitAlias(TLAlias alias) {
        libraryEntities.add(alias);
        return true;
    }

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitRole(org.opentravel.schemacompiler.model.TLRole)
	 */
	@Override
	public boolean visitRole(TLRole role) {
        libraryEntities.add(role);
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitService(org.opentravel.schemacompiler.model.TLService)
	 */
	@Override
	public boolean visitService(TLService service) {
        libraryEntities.add(service);
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitResourceParentRef(org.opentravel.schemacompiler.model.TLResourceParentRef)
	 */
	@Override
	public boolean visitResourceParentRef(TLResourceParentRef parentRef) {
        libraryEntities.add(parentRef);
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParamGroup(org.opentravel.schemacompiler.model.TLParamGroup)
	 */
	@Override
	public boolean visitParamGroup(TLParamGroup paramGroup) {
        libraryEntities.add(paramGroup);
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitParameter(org.opentravel.schemacompiler.model.TLParameter)
	 */
	@Override
	public boolean visitParameter(TLParameter parameter) {
        libraryEntities.add(parameter);
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAction(org.opentravel.schemacompiler.model.TLAction)
	 */
	@Override
	public boolean visitAction(TLAction action) {
        libraryEntities.add(action);
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitActionResponse(org.opentravel.schemacompiler.model.TLActionResponse)
	 */
	@Override
	public boolean visitActionResponse(TLActionResponse actionResponse) {
        libraryEntities.add(actionResponse);
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
	 */
	@Override
	public boolean visitAttribute(TLAttribute attribute) {
        libraryEntities.add(attribute);
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
	 */
	@Override
	public boolean visitElement(TLProperty element) {
        libraryEntities.add(element);
        return true;
	}

	/**
	 * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitIndicator(org.opentravel.schemacompiler.model.TLIndicator)
	 */
	@Override
	public boolean visitIndicator(TLIndicator indicator) {
        libraryEntities.add(indicator);
        return true;
	}

}
