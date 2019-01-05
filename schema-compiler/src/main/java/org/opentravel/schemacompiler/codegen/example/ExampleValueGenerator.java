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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.AliasCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.EnumCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.springframework.context.ApplicationContext;
import org.w3._2001.xmlschema.SimpleType;

/**
 * Returns example values for simple types based on a preferred context.
 * 
 * @author S. Livezey
 */
public class ExampleValueGenerator {

    private static final String SCHEMA_FOR_SCHEMA_EXAMPLES = "/ota2-context/built-ins/s4s-examples.properties";
    private static final String OTA_BUILT_INS_EXAMPLES     = "/ota2-context/built-ins/ota-examples.properties";
    private static final String UNKNOWN_EXAMPLE_VALUE = "???";

    private enum ExampleSearchMode {
        PREFERRED_EXAMPLE, ANY_EXAMPLE, LEGACY_VALUE
    }

    private Map<String, LegacyTypeExampleProvider> legacyExampleProviders;
    private Map<String, Map<String, List<String>>> enumerationExamples = new HashMap<>();
    private MessageIdFactory idFactory = new MessageIdFactory();
    private String preferredContext;

    /**
     * Default constructor.
     */
    public ExampleValueGenerator() {
        setLegacyExampleProviders(null);
    }

    /**
     * Returns a new instance of the example generator from the compiler's application context.
     * 
     * @param preferredContext
     *            the context ID to assign for the new example generator instance
     * @return ExampleValueGenerator
     */
    public static ExampleValueGenerator getInstance(String preferredContext) {
        ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();
        ExampleValueGenerator generator = (ExampleValueGenerator) appContext
                .getBean(SchemaCompilerApplicationContext.EXAMPLE_GENERATOR);

        generator.setPreferredContext(preferredContext);
        return generator;
    }

    /**
     * Assigns the list of <code>LegacyTypeExampleProviders</code> that will be used to produce
     * examples for legacy simple types.
     * 
     * @param exampleProviders
     *            the list of legacy example providers to assign
     */
    public void setLegacyExampleProviders(Collection<LegacyTypeExampleProvider> exampleProviders) {
        initExampleProviders();

        if (exampleProviders != null) {
            for (LegacyTypeExampleProvider exampleProvider : exampleProviders) {
                legacyExampleProviders.put(exampleProvider.getNamespace(), exampleProvider);
            }
        }
    }

    /**
     * Returns the preferred context ID to assign for example generation.
     * 
     * @return String
     */
    public String getPreferredContext() {
        return preferredContext;
    }

    /**
     * Assigns the preferred context ID to assign for example generation.
     * 
     * @param preferredContext
     *            the context ID to assign
     */
    public void setPreferredContext(String preferredContext) {
        this.preferredContext = preferredContext;
    }

    /**
     * Returns an example value for the given <code>TLSimple</code> entity.
     * 
     * @param simple
     *            the simple type for which to return an example value
     * @return String
     */
    public String getExampleValue(TLSimple simple) {
        String exampleValue = null;

        if (!isEmptyValueType(simple) && !isEmptyValueType(simple.getParentType())) {
            exampleValue = getExampleValue(simple, ExampleSearchMode.PREFERRED_EXAMPLE);

            if (exampleValue == null) {
                exampleValue = getExampleValue(simple, ExampleSearchMode.PREFERRED_EXAMPLE);
            }
            if (exampleValue == null) {
                exampleValue = getExampleValue(simple, ExampleSearchMode.ANY_EXAMPLE);
            }
            if (exampleValue == null) {
                exampleValue = getExampleValue(simple, ExampleSearchMode.LEGACY_VALUE);
            }
            if (exampleValue == null) {
                exampleValue = UNKNOWN_EXAMPLE_VALUE;
            }
        }
        return exampleValue;
    }

    /**
     * Returns an example value for the given <code>TLSimpleFacet</code> entity.
     * 
     * @param simpleFacet
     *            the simple facet for which to return an example value
     * @return String
     */
    public String getExampleValue(TLSimpleFacet simpleFacet) {
        String exampleValue = null;

        if ((simpleFacet != null) && !isEmptyValueType(simpleFacet.getSimpleType())) {
            exampleValue = getExampleValue(simpleFacet, ExampleSearchMode.PREFERRED_EXAMPLE);

            if (exampleValue == null) {
                exampleValue = getExampleValue(simpleFacet, ExampleSearchMode.PREFERRED_EXAMPLE);
            }
            if (exampleValue == null) {
                exampleValue = getExampleValue(simpleFacet, ExampleSearchMode.ANY_EXAMPLE);
            }
            if (exampleValue == null) {
                exampleValue = getExampleValue(simpleFacet, ExampleSearchMode.LEGACY_VALUE);
            }
            if (exampleValue == null) {
                exampleValue = UNKNOWN_EXAMPLE_VALUE;
            }
        }
        return exampleValue;
    }

    /**
     * Returns an example value for the given <code>TLValueWithAttributes</code> entity.
     * 
     * @param valueWithAttributes
     *            the value-with-attributes for which to return an example value
     * @return String
     */
    public String getExampleValue(TLValueWithAttributes valueWithAttributes) {
        String exampleValue = null;

        if ((valueWithAttributes != null) && !isEmptyValueType(valueWithAttributes.getParentType())) {
            exampleValue = getExampleValue(valueWithAttributes, valueWithAttributes.getParentType());
        }
        return exampleValue;
    }

    /**
     * Returns an example value for the given <code>TLAttribute</code> entity.
     * 
     * @param attribute
     *            the attribute for which to return an example value
     * @param owner  the owner that declared or inherited the attribute
     * @return String
     */
    public String getExampleValue(TLAttribute attribute, NamedEntity owner) {
        String exampleValue = null;

        if ((attribute != null) && !isEmptyValueType(attribute.getType())) {
            if (XsdCodegenUtils.isIdType(attribute.getType())) {
                NamedEntity ownerBase = getBaseEntity( (owner != null) ? owner : attribute.getOwner() );
                String localName = ownerBase.getLocalName();
                
                if (ownerBase instanceof TLActionFacet) {
                	localName = ((TLActionFacet) ownerBase).getName();
                }
                exampleValue = idFactory.getMessageId(ownerBase.getNamespace(), localName);
            } else {
                exampleValue = getExampleValue((TLExampleOwner) attribute, attribute.getType());
            }
        }
        return exampleValue;
    }

    /**
     * Returns an example value for the given <code>TLProperty</code> entity.
     * 
     * <p>
     * NOTE: Values will only be returned for elements that are assigned as simple types.
     * 
     * @param element
     *            the element for which to return an example value
     * @param owner  the owner that declared or inherited the element
     * @return String
     */
    public String getExampleValue(TLProperty element, NamedEntity owner) {
        String exampleValue = null;

        if ((element != null) && !isEmptyValueType(element.getType())) {
            if (XsdCodegenUtils.isIdType(element.getType())) {
                NamedEntity ownerBase = getBaseEntity( (owner != null) ? owner : element.getOwner() );
                String localName = ownerBase.getLocalName();
                
                if (ownerBase instanceof TLActionFacet) {
                	localName = ((TLActionFacet) ownerBase).getName();
                }
                exampleValue = idFactory.getMessageId(ownerBase.getNamespace(), localName);
            } else {
                exampleValue = getExampleValue((TLExampleOwner) element, element.getType());
            }
        }
        return exampleValue;
    }

    /**
     * Returns the base of the given entity. If the entity is a facet (or facet alias), the owning
     * core or business object (or its alias) will be returned.
     * 
     * @param entity
     *            the entity for which to return the base
     * @return NamedEntity
     */
    private NamedEntity getBaseEntity(NamedEntity entity) {
        NamedEntity baseEntity;

        if (entity instanceof TLAlias) {
            TLAlias alias = (TLAlias) entity;
            TLAliasOwner aliasOwner = alias.getOwningEntity();

            if (aliasOwner instanceof TLFacet) {
                baseEntity = AliasCodegenUtils.getOwnerAlias(alias);
            } else {
                baseEntity = alias;
            }
        } else if (entity instanceof TLFacet) {
            baseEntity = ((TLFacet) entity).getOwningEntity();

        } else {
            baseEntity = entity;
        }
        return baseEntity;
    }

    /**
     * Returns an example value for the given <code>XSDSimpleType</code> entity.
     * 
     * @param xsdSimple
     *            the XSD simple type for which to return an example value
     * @return String
     */
    public String getExampleValue(XSDSimpleType xsdSimple) {
        String exampleValue = getExampleValue(xsdSimple.getName(), xsdSimple.getNamespace(),
                xsdSimple.getJaxbType(), xsdSimple.getOwningModel());

        if (exampleValue == null) {
            exampleValue = UNKNOWN_EXAMPLE_VALUE;
        }
        return exampleValue;
    }

    /**
     * Returns an example value for the given <code>TLClosedEnumeration</code> entity.
     * 
     * @param enumeration
     *            the enumeration for which to return an example value
     * @return String
     */
    public String getExampleValue(TLClosedEnumeration enumeration) {
        return getEnumExample(enumeration);
    }

    /**
     * Returns an example value for the given <code>TLClosedEnumeration</code> entity.
     * 
     * @param enumeration
     *            the enumeration for which to return an example value
     * @return String
     */
    public String getExampleValue(TLOpenEnumeration enumeration) {
        return getEnumExample(enumeration);
    }

    /**
     * Returns an example value for the given <code>TLRoleEnumeration</code> entity.
     * 
     * @param enumeration
     *            the enumeration for which to return an example value
     * @return String
     */
    public String getExampleValue(TLRoleEnumeration enumeration) {
        return getRoleEnumExample(enumeration);
    }

    /**
     * Returns an example value to use as an attribute value for the given core object's role.
     * 
     * @param coreObject
     *            the core object for which to return an example role value
     * @return Stirng
     */
    public String getExampleRoleValue(TLCoreObject coreObject) {
        Map<String, List<String>> localEnumExamples = enumerationExamples.get(coreObject
                .getNamespace());
        String exampleValue = null;
        List<String> roleExamples;

        // Create the list of example data values if this is our first time generating examples
        // for this enumeration
        if (localEnumExamples == null) {
            localEnumExamples = new HashMap<>();
            enumerationExamples.put(coreObject.getNamespace(), localEnumExamples);
        }
        roleExamples = localEnumExamples.get(coreObject.getLocalName());

        if (roleExamples == null) {
            roleExamples = new ArrayList<>();
            localEnumExamples.put(coreObject.getLocalName(), roleExamples);

            for (TLRole roleValue : coreObject.getRoleEnumeration().getRoles()) {
                roleExamples.add(roleValue.getName());
            }
        }

        // Get the next example in the list
        if (!roleExamples.isEmpty()) {
            exampleValue = roleExamples.remove(0);
            roleExamples.add(exampleValue);
        }
        return (exampleValue == null) ? UNKNOWN_EXAMPLE_VALUE : exampleValue;
    }

    /**
     * Attempts to locate an example value from the specified example owner or one of the examples
     * from its parent type. If a suitable example value cannot be found, a legacy example value
     * will be returned.
     * 
     * @param exampleOwner
     *            the entity for which an example is to be generated
     * @param parentType
     *            the parent type for the exampleOwner entity
     * @return String
     */
    private String getExampleValue(TLExampleOwner exampleOwner, NamedEntity parentType) {
        String exampleValue = getPreferredExample(exampleOwner);

        if (exampleValue == null) {
            exampleValue = getExampleValue(parentType, ExampleSearchMode.PREFERRED_EXAMPLE);
        }
        if (exampleValue == null) {
            exampleValue = getAnyExample(exampleOwner);
        }
        if (exampleValue == null) {
            exampleValue = getExampleValue(parentType, ExampleSearchMode.ANY_EXAMPLE);
        }
        if (exampleValue == null) {
            exampleValue = getExampleValue(parentType, ExampleSearchMode.LEGACY_VALUE);
        }
        if (exampleValue == null) {
            exampleValue = UNKNOWN_EXAMPLE_VALUE;
        }
        return exampleValue;
    }

    /**
     * Searches for an example value for the given parent type using the indicated search mode. If a
     * suitable example cannot be identified, this method will return null.
     * 
     * @param parentType
     *            the parent type for which to return an example
     * @param searchMode
     *            the mode to use when attempting to lookup example values
     * @return String
     */
    private String getExampleValue(NamedEntity parentType, ExampleSearchMode searchMode) {
        String exampleValue = null;

        if (parentType instanceof TLSimple) {
            exampleValue = getExampleValue((TLSimple) parentType, searchMode);

        } else if (parentType instanceof TLSimpleFacet) {
            exampleValue = getExampleValue((TLSimpleFacet) parentType, searchMode);

        } else if (parentType instanceof TLListFacet) {
            exampleValue = getExampleValue((TLListFacet) parentType, searchMode);

        } else if (parentType instanceof TLCoreObject) {
            exampleValue = getExampleValue(((TLCoreObject) parentType).getSimpleFacet(), searchMode);

        } else if (parentType instanceof TLRole) {
            exampleValue = getExampleValue(((TLRole) parentType).getRoleEnumeration()
                    .getOwningEntity().getSimpleFacet(), searchMode);

        } else if (parentType instanceof TLValueWithAttributes) {
            exampleValue = getExampleValue((TLValueWithAttributes) parentType, searchMode);

        } else if (parentType instanceof TLAbstractEnumeration) {
            exampleValue = getEnumExample((TLAbstractEnumeration) parentType);

        } else if (parentType instanceof TLRoleEnumeration) {
            exampleValue = getRoleEnumExample((TLRoleEnumeration) parentType);

        } else if ((parentType instanceof XSDSimpleType)
                && (searchMode == ExampleSearchMode.LEGACY_VALUE)) {
            exampleValue = getExampleValue((XSDSimpleType) parentType);
        }
        return exampleValue;
    }

    /**
     * Attempts to return an example value for the given <code>TLSimple</code> entity using the
     * specified search mode.
     * 
     * @param simple
     *            the simple type for which to return an example value
     * @param searchMode
     *            the mode to use when attempting to lookup example values
     * @return String
     */
	private String getExampleValue(TLSimple simple, ExampleSearchMode searchMode) {
		String exampleValue = null;
		
		if (simple != null) {
			switch (searchMode) {
				case PREFERRED_EXAMPLE:
					exampleValue = getPreferredExample(simple);
					break;
				case ANY_EXAMPLE:
					exampleValue = getAnyExample(simple);
					break;
				default:
					break;
			}
			if (exampleValue == null) {
				exampleValue = buildExampleValue(simple, searchMode);
			}
		}
		return exampleValue;
	}

	/**
	 * Constructs an example value for the specified simple type.
	 * 
	 * @param simple  the simple type for which to create an example value
	 * @param searchMode  the mode to use when attempting to lookup example values
	 * @return String
	 */
	private String buildExampleValue(TLSimple simple, ExampleSearchMode searchMode) {
		TLAttributeType parentType = simple.getParentType();
		int repeatCount = simple.isListTypeInd() ? 3 : 1;
		StringBuilder exampleStr = new StringBuilder();
		String exampleValue = null;
		
		for (int i = 0; i < repeatCount; i++) {
			String exValue = null;
			
			if (parentType instanceof TLSimple) {
				exValue = getExampleValue((TLSimple) parentType, searchMode);
				
			} else if (parentType instanceof TLClosedEnumeration) {
				exValue = getExampleValue((TLClosedEnumeration) parentType, searchMode);
				
			} else if ((parentType instanceof XSDSimpleType)
					&& (searchMode == ExampleSearchMode.LEGACY_VALUE)) {
				exValue = getExampleValue((XSDSimpleType) parentType);
			}
			
			if (exValue != null) {
				if (exampleStr.length() > 0) {
					exampleStr.append(" ");
				}
				exampleStr.append(exValue);
			}
		}
		
		if (exampleStr.length() > 0) {
			exampleValue = exampleStr.toString();
		}
		return exampleValue;
	}
	
    /**
     * Attempts to return an example value for the given <code>TLSimpleFacet</code> entity using the
     * specified search mode.
     * 
     * @param simpleFacet
     *            the simple facet for which to return an example value
     * @param searchMode
     *            the mode to use when attempting to lookup example values
     * @return String
     */
    private String getExampleValue(TLSimpleFacet simpleFacet, ExampleSearchMode searchMode) {
        String exampleValue = null;

        if (simpleFacet != null) {
            switch (searchMode) {
                case PREFERRED_EXAMPLE:
                    exampleValue = getPreferredExample(simpleFacet);
                    break;
                case ANY_EXAMPLE:
                    exampleValue = getAnyExample(simpleFacet);
                    break;
				default:
					break;
            }
            if (exampleValue == null) {
                NamedEntity parentType = simpleFacet.getSimpleType();

                if (parentType instanceof TLSimpleFacet) {
                    exampleValue = getExampleValue((TLSimpleFacet) parentType, searchMode);

                } else if (parentType instanceof TLSimple) {
                    exampleValue = getExampleValue((TLSimple) parentType, searchMode);

                } else if (parentType instanceof TLCoreObject) {
                    exampleValue = getExampleValue(((TLCoreObject) parentType).getSimpleFacet(),
                            searchMode);

                } else if (parentType instanceof TLAbstractEnumeration) {
                    exampleValue = getEnumExample((TLAbstractEnumeration) parentType);

                } else if ((parentType instanceof XSDSimpleType)
                        && (searchMode == ExampleSearchMode.LEGACY_VALUE)) {
                    exampleValue = getExampleValue((XSDSimpleType) parentType);
                }
            }
        }
        return exampleValue;
    }

    /**
     * Attempts to return an example value for the given <code>TLSimple</code> entity using the
     * specified search mode.
     * 
     * @param valueWithAttributes
     *            the VWA type for which to return an example value
     * @param searchMode
     *            the mode to use when attempting to lookup example values
     * @return String
     */
    private String getExampleValue(TLValueWithAttributes valueWithAttributes,
            ExampleSearchMode searchMode) {
        String exampleValue = null;

        if (valueWithAttributes != null) {
            switch (searchMode) {
                case PREFERRED_EXAMPLE:
                    exampleValue = getPreferredExample(valueWithAttributes);
                    break;
                case ANY_EXAMPLE:
                    exampleValue = getAnyExample(valueWithAttributes);
                    break;
				default:
					break;
            }
            if (exampleValue == null) {
                TLAttributeType parentType = valueWithAttributes.getParentType();

                if (parentType instanceof TLValueWithAttributes) {
                    exampleValue = getExampleValue((TLValueWithAttributes) parentType, searchMode);

                } else if (parentType instanceof TLSimple) {
                    exampleValue = getExampleValue((TLSimple) parentType, searchMode);

                } else if (parentType instanceof TLCoreObject) {
                    exampleValue = getExampleValue(((TLCoreObject) parentType).getSimpleFacet(),
                            searchMode);

                } else if (parentType instanceof TLAbstractEnumeration) {
                    exampleValue = getEnumExample((TLAbstractEnumeration) parentType);

                } else if ((parentType instanceof XSDSimpleType)
                        && (searchMode == ExampleSearchMode.LEGACY_VALUE)) {
                    exampleValue = getExampleValue((XSDSimpleType) parentType);
                }
            }
        }
        return exampleValue;
    }

    /**
     * Attempts to return an example value for the given <code>TLListFacet</code> entity using the
     * specified search mode.
     * 
     * <p>
     * NOTE: This method will only return an example value if the underlying item facet of the list
     * facet is a <code>TLSimpleFacet</code> instance.
     * 
     * @param listFacet
     *            the lsit simple facet for which to return an example value
     * @param searchMode
     *            the mode to use when attempting to lookup example values
     * @return String
     */
    private String getExampleValue(TLListFacet listFacet, ExampleSearchMode searchMode) {
        String exampleValue = null;

        if (listFacet.getItemFacet() instanceof TLSimpleFacet) {
            TLSimpleFacet itemFacet = (TLSimpleFacet) listFacet.getItemFacet();
            StringBuilder exampleList = new StringBuilder();

            for (int i = 0; i < 3; i++) {
                String ex = getExampleValue(itemFacet, searchMode);

                if (ex != null) {
                    if (exampleList.length() > 0)
                        exampleList.append(" ");
                    exampleList.append(ex);
                }
            }
            exampleValue = (exampleList.length() == 0) ? null : exampleList.toString();
        }
        return exampleValue;
    }

    /**
     * Performs a recursive search of the JAXB simple type instance, attempting to find a model
     * element with an example data set.
     * 
     * @param xsdSimple
     *            the JAXB simple type for which to return an example value
     * @return String
     */
    private String getExampleValue(String simpleTypeName, String namespace, SimpleType xsdSimple,
            TLModel model) {
        LegacyTypeExampleProvider legacyProvider = legacyExampleProviders.get(namespace);
        String exampleValue = null;

        if (legacyProvider != null) {
            exampleValue = legacyProvider.getExampleValue(simpleTypeName);
        }
        if ((exampleValue == null) && (xsdSimple != null)) {
            exampleValue = getXsdExampleValue(xsdSimple, model);
        }
        return exampleValue;
    }

	/**
	 * Returns the example value for the specified XSD simple type.
	 * 
	 * @param xsdSimple  the XSD simple type
	 * @param model  the model for which examples are being generated
	 * @return String
	 */
	private String getXsdExampleValue(SimpleType xsdSimple, TLModel model) {
		String exampleValue = null;
		
		if (xsdSimple.getRestriction() != null) {
		    QName parentTypeName = xsdSimple.getRestriction().getBase();
		    SimpleType parentType = (parentTypeName == null) ? null : findSimpleType(
		            parentTypeName, model);

		    if (parentType != null) {
		        exampleValue = getExampleValue(parentType.getName(),
		                parentTypeName.getNamespaceURI(), parentType, model);
		    }

		} else if ((xsdSimple.getUnion() != null)
		        && !xsdSimple.getUnion().getMemberTypes().isEmpty()) {
		    for (QName parentTypeName : xsdSimple.getUnion().getMemberTypes()) {
		        SimpleType parentType = findSimpleType(parentTypeName, model);

		        if (parentType != null) {
		            exampleValue = getExampleValue(parentType.getName(),
		                    parentTypeName.getNamespaceURI(), parentType, model);
		        }
		        if (exampleValue != null) {
		            break;
		        }
		    }
		}
		return exampleValue;
	}

    /**
     * Attempts to locate a simple type from the model with the specified namespace and name
     * combination.
     * 
     * @param simpleTypeName
     *            the qualified name of the simple type to search for
     * @param model
     *            the model to be searched
     * @return SimpleType
     */
    private SimpleType findSimpleType(QName simpleTypeName, TLModel model) {
        SimpleType simpleType = null;

        for (AbstractLibrary library : model.getLibrariesForNamespace(simpleTypeName
                .getNamespaceURI())) {
            for (LibraryMember member : library.getNamedMembers()) {
                if ((member instanceof XSDSimpleType)
                        && member.getLocalName().equals(simpleTypeName.getLocalPart())) {
                    simpleType = ((XSDSimpleType) member).getJaxbType();
                    break;
                }
            }
            if (simpleType != null) {
                break;
            }
        }
        return simpleType;
    }

    /**
     * Returns an example value from one of the examples that is explicitly defined for the given
     * example owner. If an example is not defined for the preferred context, this method will
     * return null.
     * 
     * @param exampleOwner
     *            the example owner for which to retrieve an example value
     * @return String
     */
    private String getPreferredExample(TLExampleOwner exampleOwner) {
        String exampleValue = null;

        if (preferredContext != null) {
            for (TLExample example : exampleOwner.getExamples()) {
                if (preferredContext.equals(example.getContext())) {
                    exampleValue = example.getValue();
                }
            }
        }
        return exampleValue;
    }

    /**
     * Returns an example value from one of the examples that is explicitly defined for the given
     * example owner. If an example is not defined for the preferred context, this method will an
     * example from the first available context that is defined. If no examples have been defined,
     * this method will return null.
     * 
     * @param exampleOwner
     *            the example owner for which to retrieve an example value
     * @return String
     */
    private String getAnyExample(TLExampleOwner exampleOwner) {
        String exampleValue = null;
        String firstExample = null;

        for (TLExample example : exampleOwner.getExamples()) {
            if (firstExample == null) {
                firstExample = example.getValue();
            }
            if ((preferredContext != null) && preferredContext.equals(example.getContext())) {
                exampleValue = example.getValue();
            }
        }
        if (exampleValue == null) {
            exampleValue = firstExample;
        }
        return exampleValue;
    }

    /**
     * Returns an example value for the given enumeration.
     * 
     * @param enumeration
     *            the enumeration for which to return an example value
     * @return String
     */
    private String getEnumExample(TLAbstractEnumeration enumeration) {
        String exampleValue = null;

        if (enumeration != null) {
            synchronized (enumerationExamples) {
                Map<String, List<String>> localEnumExamples = enumerationExamples.get(enumeration.getNamespace());
                List<String> enumExamples;

                // Create the list of example data values if this is our first time generating
                // examples
                // for this enumeration
                if (localEnumExamples == null) {
                    localEnumExamples = new HashMap<>();
                    enumerationExamples.put(enumeration.getNamespace(), localEnumExamples);
                }
                enumExamples = localEnumExamples.get(enumeration.getLocalName());

                if (enumExamples == null) {
                    enumExamples = new ArrayList<>();
                    localEnumExamples.put(enumeration.getLocalName(), enumExamples);

                    for (TLEnumValue enumValue : EnumCodegenUtils.getInheritedValues( enumeration )) {
                        enumExamples.add(enumValue.getLiteral());
                    }
                }

                // Get the next example in the list
                if (!enumExamples.isEmpty()) {
                    exampleValue = enumExamples.remove(0);
                    enumExamples.add(exampleValue);
                }
            }
        }
        return exampleValue;
    }

    /**
     * Returns an example value for the given role-enumeration.
     * 
     * @param roleEnum
     *            the enumeration for which to return an example value
     * @return String
     */
    private String getRoleEnumExample(TLRoleEnumeration roleEnum) {
        String exampleValue = null;

        if (roleEnum != null) {
            synchronized (enumerationExamples) {
                Map<String, List<String>> localEnumExamples = enumerationExamples.get(roleEnum
                        .getNamespace());
                List<String> enumExamples;

                // Create the list of example data values if this is our first time generating
                // examples
                // for this enumeration
                if (localEnumExamples == null) {
                    localEnumExamples = new HashMap<>();
                    enumerationExamples.put(roleEnum.getNamespace(), localEnumExamples);
                }
                enumExamples = localEnumExamples.get(roleEnum.getLocalName());

                if (enumExamples == null) {
                    enumExamples = new ArrayList<>();
                    localEnumExamples.put(roleEnum.getLocalName(), enumExamples);

                    for (TLRole roleValue : PropertyCodegenUtils.getInheritedRoles(roleEnum
                            .getOwningEntity())) {
                        enumExamples.add(roleValue.getName());
                    }
                }

                // Get the next example in the list
                if (!enumExamples.isEmpty()) {
                    exampleValue = enumExamples.remove(0);
                    enumExamples.add(exampleValue);
                }
            }
        }
        return exampleValue;
    }

    /**
     * Returns true if the given entity matches the empty element type of the built-in library. If
     * the given entity is null, this method will assume that a match to the empty element and
     * return true.
     * 
     * @param entity
     *            the named entity to analyze
     * @return boolean
     */
    private boolean isEmptyValueType(NamedEntity entity) {
        SchemaDependency emptyElement = SchemaDependency.getEmptyElement();

        return (entity == null)
                || (emptyElement.getSchemaDeclaration().getNamespace()
                        .equals(entity.getNamespace()) && emptyElement.getLocalName().equals(
                        entity.getLocalName()));
    }

    /**
     * Initializes the map used for <code>LegacyTypeExampleProvider</code> lookups. The initial list
     * is seeded with the examples required by the schema-for-schemas simple types.
     */
    private void initExampleProviders() {
        LegacyTypeExampleProvider s4sExampleProvider = new LegacyTypeExampleProvider(SCHEMA_FOR_SCHEMA_EXAMPLES);
        LegacyTypeExampleProvider otaExampleProvider = new LegacyTypeExampleProvider(OTA_BUILT_INS_EXAMPLES);

        legacyExampleProviders = new HashMap<>();
        legacyExampleProviders.put(s4sExampleProvider.getNamespace(), s4sExampleProvider);
        legacyExampleProviders.put(otaExampleProvider.getNamespace(), otaExampleProvider);
    }

}
