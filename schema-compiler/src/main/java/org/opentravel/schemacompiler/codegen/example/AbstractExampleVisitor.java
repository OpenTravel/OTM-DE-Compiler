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

import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param preferredContext
     *            the context ID of the preferred context from which to generate examples
     */
    public AbstractExampleVisitor(String preferredContext) {
        this.exampleValueGenerator = ExampleValueGenerator.getInstance(preferredContext);
    }

    /**
     * Generates an example value for the given model entity (if possible).
     * 
     * @param entity
     *            the entity for which to generate an example
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
            exampleValue = exampleValueGenerator.getExampleValue(((TLCoreObject) entity)
                    .getSimpleFacet());

        } else if (entity instanceof TLAttribute) {
            exampleValue = exampleValueGenerator.getExampleValue((TLAttribute) entity);

        } else if (entity instanceof TLProperty) {
            exampleValue = exampleValueGenerator.getExampleValue((TLProperty) entity);
        }
        return exampleValue;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#visitSimpleType(org.opentravel.schemacompiler.model.TLAttributeType)
     */
    @Override
    public void visitSimpleType(TLAttributeType simpleType) {
        if (DEBUG) {
            log.info(debugIndent + "visitSimpleType() : " + simpleType.getLocalName() + " --> "
                    + generateExampleValue(simpleType));
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startFacet(org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public void startFacet(TLFacet facet) {
        if (DEBUG) {
            log.info(debugIndent + "startFacet() : " + facet.getLocalName());
            debugIndent.append("  ");
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endFacet(org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public void endFacet(TLFacet facet) {
        if (DEBUG) {
            if (debugIndent.length() > 0)
                debugIndent.setLength(debugIndent.length() - 2);
            log.info(debugIndent + "endFacet() : " + facet.getLocalName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startListFacet(org.opentravel.schemacompiler.model.TLListFacet,
     *      org.opentravel.schemacompiler.model.TLRole)
     */
    @Override
    public void startListFacet(TLListFacet listFacet, TLRole role) {
        if (DEBUG) {
            log.info(debugIndent + "startListFacet() : " + listFacet.getLocalName() + " / "
                    + role.getName());
            debugIndent.append("  ");
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endListFacet(org.opentravel.schemacompiler.model.TLListFacet,
     *      org.opentravel.schemacompiler.model.TLRole)
     */
    @Override
    public void endListFacet(TLListFacet listFacet, TLRole role) {
        if (DEBUG) {
            if (debugIndent.length() > 0)
                debugIndent.setLength(debugIndent.length() - 2);
            log.info(debugIndent + "endListFacet() : " + listFacet.getLocalName() + " / "
                    + role.getName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startAlias(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public void startAlias(TLAlias alias) {
        if (DEBUG) {
            log.info(debugIndent + "startAlias() : " + alias.getLocalName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endAlias(org.opentravel.schemacompiler.model.TLAlias)
     */
    @Override
    public void endAlias(TLAlias alias) {
        if (DEBUG) {
            log.info(debugIndent + "endAlias() : " + alias.getLocalName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startAttribute(org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public void startAttribute(TLAttribute attribute) {
        if (DEBUG) {
            log.info(debugIndent + "startAttribute() : " + attribute.getName() + " --> "
                    + generateExampleValue(attribute));
            debugIndent.append("  ");
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endAttribute(org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public void endAttribute(TLAttribute attribute) {
        if (DEBUG) {
            if (debugIndent.length() > 0)
                debugIndent.setLength(debugIndent.length() - 2);
            log.info(debugIndent + "endAttribute() : " + attribute.getName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startElement(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public void startElement(TLProperty element) {
        if (DEBUG) {
            log.info(debugIndent + "startElement() : " + element.getName());
            debugIndent.append("  ");
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endElement(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public void endElement(TLProperty element) {
        if (DEBUG) {
            if (debugIndent.length() > 0)
                debugIndent.setLength(debugIndent.length() - 2);
            log.info(debugIndent + "endElement() : " + element.getName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startIndicatorAttribute(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void startIndicatorAttribute(TLIndicator indicator) {
        if (DEBUG) {
            log.info(debugIndent + "startIndicatorAttribute() : " + indicator.getName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endIndicatorAttribute(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void endIndicatorAttribute(TLIndicator indicator) {
        if (DEBUG) {
            log.info(debugIndent + "endIndicatorAttribute() : " + indicator.getName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startIndicatorElement(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void startIndicatorElement(TLIndicator indicator) {
        if (DEBUG) {
            log.info(debugIndent + "startIndicatorElement() : " + indicator.getName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endIndicatorElement(org.opentravel.schemacompiler.model.TLIndicator)
     */
    @Override
    public void endIndicatorElement(TLIndicator indicator) {
        if (DEBUG) {
            log.info(debugIndent + "endIndicatorElement() : " + indicator.getName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startOpenEnumeration(org.opentravel.schemacompiler.model.TLOpenEnumeration)
     */
    @Override
    public void startOpenEnumeration(TLOpenEnumeration openEnum) {
        if (DEBUG) {
            log.info(debugIndent + "startOpenEnumeration() : " + openEnum.getLocalName() + " --> "
                    + generateExampleValue(openEnum));
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endOpenEnumeration(org.opentravel.schemacompiler.model.TLOpenEnumeration)
     */
    @Override
    public void endOpenEnumeration(TLOpenEnumeration openEnum) {
        if (DEBUG) {
            log.info(debugIndent + "endOpenEnumeration() : " + openEnum.getLocalName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startRoleEnumeration(org.opentravel.schemacompiler.model.TLRoleEnumeration)
     */
    @Override
    public void startRoleEnumeration(TLRoleEnumeration roleEnum) {
        if (DEBUG) {
            log.info(debugIndent + "startRoleEnumeration() : " + roleEnum.getLocalName() + " --> "
                    + generateExampleValue(roleEnum));
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endRoleEnumeration(org.opentravel.schemacompiler.model.TLRoleEnumeration)
     */
    @Override
    public void endRoleEnumeration(TLRoleEnumeration roleEnum) {
        if (DEBUG) {
            log.info(debugIndent + "endRoleEnumeration() : " + roleEnum.getLocalName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
     */
    @Override
    public void startValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        if (DEBUG) {
            log.info(debugIndent + "startValueWithAttributes() : "
                    + valueWithAttributes.getLocalName() + " --> "
                    + generateExampleValue(valueWithAttributes));
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
     */
    @Override
    public void endValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        if (DEBUG) {
            log.info(debugIndent + "endValueWithAttributes() : "
                    + valueWithAttributes.getLocalName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startExtensionPoint(org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public void startExtensionPoint(TLFacet facet) {
        if (DEBUG) {
            log.info(debugIndent + "startExtensionPoint() : " + facet.getLocalName());
            debugIndent.append("  ");
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endExtensionPoint(org.opentravel.schemacompiler.model.TLFacet)
     */
    @Override
    public void endExtensionPoint(TLFacet facet) {
        if (DEBUG) {
            if (debugIndent.length() > 0)
                debugIndent.setLength(debugIndent.length() - 2);
            log.info(debugIndent + "endExtensionPoint() : " + facet.getLocalName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
     */
    @Override
    public void startExtensionPointFacet(TLExtensionPointFacet facet) {
        if (DEBUG) {
            log.info(debugIndent + "startExtensionPointFacet() : " + facet.getLocalName());
            debugIndent.append("  ");
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endExtensionPointFacet(org.opentravel.schemacompiler.model.TLExtensionPointFacet)
     */
    @Override
    public void endExtensionPointFacet(TLExtensionPointFacet facet) {
        if (DEBUG) {
            if (debugIndent.length() > 0)
                debugIndent.setLength(debugIndent.length() - 2);
            log.info(debugIndent + "endExtensionPointFacet() : " + facet.getLocalName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startXsdComplexType(org.opentravel.schemacompiler.model.XSDComplexType)
     */
    @Override
    public void startXsdComplexType(XSDComplexType xsdComplexType) {
        if (DEBUG) {
            log.info(debugIndent + "startXsdComplexType() : " + xsdComplexType.getLocalName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endXsdComplexType(org.opentravel.schemacompiler.model.XSDComplexType)
     */
    @Override
    public void endXsdComplexType(XSDComplexType xsdComplexType) {
        if (DEBUG) {
            log.info(debugIndent + "endXsdComplexType() : " + xsdComplexType.getLocalName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#startXsdElement(org.opentravel.schemacompiler.model.XSDElement)
     */
    @Override
    public void startXsdElement(XSDElement xsdElement) {
        if (DEBUG) {
            log.info(debugIndent + "startXsdElement() : " + xsdElement.getLocalName());
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.example.ExampleVisitor#endXsdElement(org.opentravel.schemacompiler.model.XSDElement)
     */
    @Override
    public void endXsdElement(XSDElement xsdElement) {
        if (DEBUG) {
            log.info(debugIndent + "endXsdElement() : " + xsdElement.getLocalName());
        }
    }

}
