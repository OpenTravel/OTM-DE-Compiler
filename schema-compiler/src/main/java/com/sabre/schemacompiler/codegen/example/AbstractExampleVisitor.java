/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLAttributeType;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLListFacet;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLRole;
import com.sabre.schemacompiler.model.TLRoleEnumeration;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;
import com.sabre.schemacompiler.model.XSDComplexType;
import com.sabre.schemacompiler.model.XSDElement;
import com.sabre.schemacompiler.model.XSDSimpleType;

/**
 * Adapter base class for the <code>ExampleVisitor</code> interface that can optionally print
 * debugging information as logging output.
 * 
 * @author S. Livezey
 */
public abstract class AbstractExampleVisitor implements ExampleVisitor {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractExampleVisitor.class);
	private static final boolean DEBUG = false;
	
	private StringBuilder debugIndent = new StringBuilder();
	protected ExampleValueGenerator exampleValueGenerator;
	
	/**
	 * Contstructor that provides the navigation options to use during example generation.
	 * 
	 * @param preferredContext  the context ID of the preferred context from which to generate examples
	 */
	public AbstractExampleVisitor(String preferredContext) {
		this.exampleValueGenerator = ExampleValueGenerator.getInstance( preferredContext );
	}
	
	/**
	 * Generates an example value for the given model entity (if possible).
	 * 
	 * @param entity  the entity for which to generate an example
	 * @return String
	 */
	protected String generateExampleValue(Object entity) {
		String exampleValue = null;
		
		if (entity instanceof TLSimple) {
			exampleValue = exampleValueGenerator.getExampleValue((TLSimple) entity);
			
		} else if (entity instanceof TLSimpleFacet) {
			exampleValue = exampleValueGenerator.getExampleValue((TLSimpleFacet) entity);
			
		} else if (entity instanceof XSDSimpleType) {
			exampleValue = exampleValueGenerator.getExampleValue((XSDSimpleType) entity);
			
		} else if (entity instanceof TLOpenEnumeration) {
			exampleValue = exampleValueGenerator.getExampleValue((TLOpenEnumeration) entity);
			
		} else if (entity instanceof TLRoleEnumeration) {
			exampleValue = exampleValueGenerator.getExampleValue((TLRoleEnumeration) entity);
			
		} else if (entity instanceof TLClosedEnumeration) {
			exampleValue = exampleValueGenerator.getExampleValue((TLClosedEnumeration) entity);
			
		} else if (entity instanceof TLValueWithAttributes) {
			exampleValue = exampleValueGenerator.getExampleValue((TLValueWithAttributes) entity);
			
		} else if (entity instanceof TLCoreObject) {
			exampleValue = exampleValueGenerator.getExampleValue( ((TLCoreObject) entity).getSimpleFacet() );
			
		} else if (entity instanceof TLAttribute) {
			exampleValue = exampleValueGenerator.getExampleValue((TLAttribute) entity);
			
		} else if (entity instanceof TLProperty) {
			exampleValue = exampleValueGenerator.getExampleValue((TLProperty) entity);
		}
		return exampleValue;
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#visitSimpleType(com.sabre.schemacompiler.model.TLAttributeType)
	 */
	@Override
	public void visitSimpleType(TLAttributeType simpleType) {
		if (DEBUG) {
			log.info(debugIndent + "visitSimpleType() : " + simpleType.getLocalName() + " --> " + generateExampleValue(simpleType));
		}
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startFacet(com.sabre.schemacompiler.model.TLFacet)
	 */
	@Override
	public void startFacet(TLFacet facet) {
		if (DEBUG) {
			log.info(debugIndent + "startFacet() : " + facet.getLocalName());
			debugIndent.append("  ");
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endFacet(com.sabre.schemacompiler.model.TLFacet)
	 */
	@Override
	public void endFacet(TLFacet facet) {
		if (DEBUG) {
			if (debugIndent.length() > 0) debugIndent.setLength( debugIndent.length() - 2 );
			log.info(debugIndent + "endFacet() : " + facet.getLocalName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startListFacet(com.sabre.schemacompiler.model.TLListFacet, com.sabre.schemacompiler.model.TLRole)
	 */
	@Override
	public void startListFacet(TLListFacet listFacet, TLRole role) {
		if (DEBUG) {
			log.info(debugIndent + "startListFacet() : " + listFacet.getLocalName() + " / " + role.getName());
			debugIndent.append("  ");
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endListFacet(com.sabre.schemacompiler.model.TLListFacet, com.sabre.schemacompiler.model.TLRole)
	 */
	@Override
	public void endListFacet(TLListFacet listFacet, TLRole role) {
		if (DEBUG) {
			if (debugIndent.length() > 0) debugIndent.setLength( debugIndent.length() - 2 );
			log.info(debugIndent + "endListFacet() : " + listFacet.getLocalName() + " / " + role.getName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startAlias(com.sabre.schemacompiler.model.TLAlias)
	 */
	@Override
	public void startAlias(TLAlias alias) {
		if (DEBUG) {
			log.info(debugIndent + "startAlias() : " + alias.getLocalName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endAlias(com.sabre.schemacompiler.model.TLAlias)
	 */
	@Override
	public void endAlias(TLAlias alias) {
		if (DEBUG) {
			log.info(debugIndent + "endAlias() : " + alias.getLocalName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startAttribute(com.sabre.schemacompiler.model.TLAttribute)
	 */
	@Override
	public void startAttribute(TLAttribute attribute) {
		if (DEBUG) {
			log.info(debugIndent + "startAttribute() : " + attribute.getName() + " --> " + generateExampleValue(attribute));
			debugIndent.append("  ");
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endAttribute(com.sabre.schemacompiler.model.TLAttribute)
	 */
	@Override
	public void endAttribute(TLAttribute attribute) {
		if (DEBUG) {
			if (debugIndent.length() > 0) debugIndent.setLength( debugIndent.length() - 2 );
			log.info(debugIndent + "endAttribute() : " + attribute.getName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startElement(com.sabre.schemacompiler.model.TLProperty)
	 */
	@Override
	public void startElement(TLProperty element) {
		if (DEBUG) {
			log.info(debugIndent + "startElement() : " + element.getName());
			debugIndent.append("  ");
		}
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endElement(com.sabre.schemacompiler.model.TLProperty)
	 */
	@Override
	public void endElement(TLProperty element) {
		if (DEBUG) {
			if (debugIndent.length() > 0) debugIndent.setLength( debugIndent.length() - 2 );
			log.info(debugIndent + "endElement() : " + element.getName());
		}
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startIndicatorAttribute(com.sabre.schemacompiler.model.TLIndicator)
	 */
	@Override
	public void startIndicatorAttribute(TLIndicator indicator) {
		if (DEBUG) {
			log.info(debugIndent + "startIndicatorAttribute() : " + indicator.getName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endIndicatorAttribute(com.sabre.schemacompiler.model.TLIndicator)
	 */
	@Override
	public void endIndicatorAttribute(TLIndicator indicator) {
		if (DEBUG) {
			log.info(debugIndent + "endIndicatorAttribute() : " + indicator.getName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startIndicatorElement(com.sabre.schemacompiler.model.TLIndicator)
	 */
	@Override
	public void startIndicatorElement(TLIndicator indicator) {
		if (DEBUG) {
			log.info(debugIndent + "startIndicatorElement() : " + indicator.getName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endIndicatorElement(com.sabre.schemacompiler.model.TLIndicator)
	 */
	@Override
	public void endIndicatorElement(TLIndicator indicator) {
		if (DEBUG) {
			log.info(debugIndent + "endIndicatorElement() : " + indicator.getName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startOpenEnumeration(com.sabre.schemacompiler.model.TLOpenEnumeration)
	 */
	@Override
	public void startOpenEnumeration(TLOpenEnumeration openEnum) {
		if (DEBUG) {
			log.info(debugIndent + "startOpenEnumeration() : " + openEnum.getLocalName() + " --> " + generateExampleValue(openEnum));
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endOpenEnumeration(com.sabre.schemacompiler.model.TLOpenEnumeration)
	 */
	@Override
	public void endOpenEnumeration(TLOpenEnumeration openEnum) {
		if (DEBUG) {
			log.info(debugIndent + "endOpenEnumeration() : " + openEnum.getLocalName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startRoleEnumeration(com.sabre.schemacompiler.model.TLRoleEnumeration)
	 */
	@Override
	public void startRoleEnumeration(TLRoleEnumeration roleEnum) {
		if (DEBUG) {
			log.info(debugIndent + "startRoleEnumeration() : " + roleEnum.getLocalName() + " --> " + generateExampleValue(roleEnum));
		}
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endRoleEnumeration(com.sabre.schemacompiler.model.TLRoleEnumeration)
	 */
	@Override
	public void endRoleEnumeration(TLRoleEnumeration roleEnum) {
		if (DEBUG) {
			log.info(debugIndent + "endRoleEnumeration() : " + roleEnum.getLocalName());
		}
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
	 */
	@Override
	public void startValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
		if (DEBUG) {
			log.info(debugIndent + "startValueWithAttributes() : " + valueWithAttributes.getLocalName() + " --> " + generateExampleValue(valueWithAttributes));
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endValueWithAttributes(com.sabre.schemacompiler.model.TLValueWithAttributes)
	 */
	@Override
	public void endValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
		if (DEBUG) {
			log.info(debugIndent + "endValueWithAttributes() : " + valueWithAttributes.getLocalName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startExtensionPoint(com.sabre.schemacompiler.model.TLFacet)
	 */
	@Override
	public void startExtensionPoint(TLFacet facet) {
		if (DEBUG) {
			log.info(debugIndent + "startExtensionPoint() : " + facet.getLocalName());
			debugIndent.append("  ");
		}
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endExtensionPoint(com.sabre.schemacompiler.model.TLFacet)
	 */
	@Override
	public void endExtensionPoint(TLFacet facet) {
		if (DEBUG) {
			if (debugIndent.length() > 0) debugIndent.setLength( debugIndent.length() - 2 );
			log.info(debugIndent + "endExtensionPoint() : " + facet.getLocalName());
		}
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startExtensionPointFacet(com.sabre.schemacompiler.model.TLExtensionPointFacet)
	 */
	@Override
	public void startExtensionPointFacet(TLExtensionPointFacet facet) {
		if (DEBUG) {
			log.info(debugIndent + "startExtensionPointFacet() : " + facet.getLocalName());
			debugIndent.append("  ");
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endExtensionPointFacet(com.sabre.schemacompiler.model.TLExtensionPointFacet)
	 */
	@Override
	public void endExtensionPointFacet(TLExtensionPointFacet facet) {
		if (DEBUG) {
			if (debugIndent.length() > 0) debugIndent.setLength( debugIndent.length() - 2 );
			log.info(debugIndent + "endExtensionPointFacet() : " + facet.getLocalName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startXsdComplexType(com.sabre.schemacompiler.model.XSDComplexType)
	 */
	@Override
	public void startXsdComplexType(XSDComplexType xsdComplexType) {
		if (DEBUG) {
			log.info(debugIndent + "startXsdComplexType() : " + xsdComplexType.getLocalName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endXsdComplexType(com.sabre.schemacompiler.model.XSDComplexType)
	 */
	@Override
	public void endXsdComplexType(XSDComplexType xsdComplexType) {
		if (DEBUG) {
			log.info(debugIndent + "endXsdComplexType() : " + xsdComplexType.getLocalName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#startXsdElement(com.sabre.schemacompiler.model.XSDElement)
	 */
	@Override
	public void startXsdElement(XSDElement xsdElement) {
		if (DEBUG) {
			log.info(debugIndent + "startXsdElement() : " + xsdElement.getLocalName());
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.example.ExampleVisitor#endXsdElement(com.sabre.schemacompiler.model.XSDElement)
	 */
	@Override
	public void endXsdElement(XSDElement xsdElement) {
		if (DEBUG) {
			log.info(debugIndent + "endXsdElement() : " + xsdElement.getLocalName());
		}
	}
	
}
