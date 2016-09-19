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
package org.opentravel.schemacompiler.codegen.util;

import java.io.File;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.opentravel.ns.ota2.appinfo_v01_00.ContextualValue;
import org.opentravel.ns.ota2.appinfo_v01_00.Library;
import org.opentravel.ns.ota2.appinfo_v01_00.OTA2Entity;
import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.util.SchemaCompilerInfo;
import org.opentravel.schemacompiler.util.URLUtils;
import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Appinfo;
import org.w3._2001.xmlschema.Attribute;

/**
 * Static utility methods shared among XSD code generation components.
 * 
 * @author S. Livezey
 */
public class XsdCodegenUtils {

    public static final QName XSD_BOOLEAN_TYPE = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "boolean");
    public static final QName XSD_STRING_TYPE = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string");
    public static final QName XML_SCHEMA_ID_TYPE = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "ID");
    private static final Map<Class<?>, String> libraryTypeNames;

    public static final String BUILT_INS_FOLDER = "built-ins";
    public static final String LEGACY_FOLDER = "legacy";

    private static org.opentravel.ns.ota2.appinfo_v01_00.ObjectFactory appInfoObjectFactory = new org.opentravel.ns.ota2.appinfo_v01_00.ObjectFactory();
    private static DatatypeFactory jaxbDatatypeFactory;

    /**
     * Returns the name of the global XML schema element associated with the given property type, or
     * null if the type cannot be referenced by element (e.g. simple types).
     * 
     * @param modelEntity
     *            the model entity for which to return the element name
     * @return QName
     */
    public static QName getGlobalElementName(NamedEntity modelEntity) {
        QName elementName = null;

        if (isSimpleCoreObject(modelEntity)) {
            NamedEntity simpleCoreEntity = modelEntity;

            // For simple cores, a direct reference to the summary facet is the same as a reference
            // to the core itself.
            if (modelEntity instanceof TLAlias) {
                TLAlias alias = (TLAlias) modelEntity;

                if (alias.getOwningEntity() instanceof TLFacet) {
                    simpleCoreEntity = AliasCodegenUtils.getOwnerAlias(alias);
                }
            } else if (modelEntity instanceof TLFacet) {
                simpleCoreEntity = ((TLFacet) modelEntity).getOwningEntity();
            }
            elementName = new QName(simpleCoreEntity.getNamespace(),
                    simpleCoreEntity.getLocalName());

        } else if ((modelEntity instanceof TLAbstractFacet)
                && !(modelEntity instanceof TLSimpleFacet)) {
            TLAbstractFacet facet = (TLAbstractFacet) modelEntity;
            TLFacetOwner facetOwner = facet.getOwningEntity();
            String elementLocalName = facetOwner.getLocalName() + getElementFacetSuffix(facet);

            if (facet.getFacetType() == TLFacetType.ID) {
                elementLocalName = facetOwner.getLocalName() + "Identifier";

            } else if (facetOwner instanceof TLBusinessObject) {
                elementLocalName = facet.getOwningEntity().getLocalName()
                        + getElementFacetSuffix(facet);

            } else if (facetOwner instanceof TLOperation) {
                elementLocalName = ((TLOperation) facet.getOwningEntity()).getName()
                        + getElementFacetSuffix(facet);
            }
            elementName = new QName(modelEntity.getNamespace(), elementLocalName);

        } else if (modelEntity instanceof TLAlias) {
            TLAlias alias = (TLAlias) modelEntity;
            String elementLocalName;

            if (alias.getOwningEntity() instanceof TLFacet) {
                TLFacet facet = (TLFacet) alias.getOwningEntity();
                TLAlias ownerAlias = AliasCodegenUtils.getOwnerAlias(alias);

                if (ownerAlias != null) {
                    if (facet.getFacetType() == TLFacetType.ID) {
                        elementLocalName = ownerAlias.getLocalName() + "Identifier";
                    } else {
                        elementLocalName = ownerAlias.getLocalName() + getElementFacetSuffix(facet);
                    }
                } else {
                    elementLocalName = "ERROR" + getElementFacetSuffix(facet);
                }
            } else if (alias.getOwningEntity() instanceof TLListFacet) {
                TLListFacet listFacet = (TLListFacet) alias.getOwningEntity();

                elementLocalName = AliasCodegenUtils.getOwnerAlias(alias).getLocalName()
                        + getElementFacetSuffix(listFacet.getItemFacet());

            } else {
                elementLocalName = alias.getLocalName();
            }
            elementName = new QName(alias.getNamespace(), elementLocalName);
            
        } else if (modelEntity instanceof TLActionFacet) {
        	NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( (TLActionFacet) modelEntity );
        	
        	if (payloadType == null) {
        		// No action - return null for no global element
        		
        	} else if (payloadType instanceof TLActionFacet) {
        		elementName = new QName(payloadType.getNamespace(), ((TLActionFacet) payloadType).getName());
        		
        	} else {
        		elementName = getGlobalElementName( payloadType );
        	}
        	
        } else if ((modelEntity instanceof TLCoreObject)
        		|| (modelEntity instanceof TLChoiceObject)
                || !(modelEntity instanceof TLAttributeType)) {
            elementName = new QName(modelEntity.getNamespace(), modelEntity.getLocalName());
        }
        return elementName;
    }

    /**
     * Returns the name of the substitutable element for the given facet. Substitutable elements are
     * defined in the schema, but are not referenced by properties.
     * 
     * @param facet
     *            the facet for which to return the element name
     * @return QName
     */
    public static QName getSubstitutableElementName(TLFacet facet) {
        TLFacetOwner facetOwner = facet.getOwningEntity();
        String elementName = null;

        if (facetOwner instanceof TLBusinessObject) {
            if (facet.getFacetType() == TLFacetType.ID) {
                elementName = facetOwner.getLocalName() + "ID";

            } else if (facet.getFacetType() == TLFacetType.SUMMARY) {
                elementName = facetOwner.getLocalName();
            }
        } else if (facetOwner instanceof TLCoreObject) {
            if (facet.getFacetType() == TLFacetType.SUMMARY) {
                elementName = facetOwner.getLocalName();
            }
        } else if (facetOwner instanceof TLChoiceObject) {
            if (facet.getFacetType() == TLFacetType.SHARED) {
                elementName = facetOwner.getLocalName();
            }
        }

        // Default value is the normal global element name
        if (elementName == null) {
            elementName = getGlobalElementName(facet).getLocalPart();
        }
        return new QName(facet.getNamespace(), elementName);
    }

    /**
     * Returns the name of the substitutable element for the given facet alias. Substitutable
     * elements are defined in the schema, but are not referenced by properties.
     * 
     * @param facetAlias
     *            the facet alias for which to return the element name
     * @return QName
     */
    public static QName getSubstitutableElementName(TLAlias facetAlias) {
        TLFacet facet = (TLFacet) facetAlias.getOwningEntity();
        TLFacetOwner facetOwner = facet.getOwningEntity();
        String elementName = null;

        if (facetOwner instanceof TLBusinessObject) {
            TLAlias ownerAlias = AliasCodegenUtils.getOwnerAlias(facetAlias);

            if (facet.getFacetType() == TLFacetType.ID) {
                elementName = ownerAlias.getName() + "ID";

            } else if (facet.getFacetType() == TLFacetType.SUMMARY) {
                elementName = ownerAlias.getName();
            }
        } else if (facetOwner instanceof TLCoreObject) {
            TLAlias ownerAlias = AliasCodegenUtils.getOwnerAlias(facetAlias);

            if (facet.getFacetType() == TLFacetType.SUMMARY) {
                elementName = ownerAlias.getName();
            }
        } else if (facetOwner instanceof TLChoiceObject) {
            TLAlias ownerAlias = AliasCodegenUtils.getOwnerAlias(facetAlias);

            if (facet.getFacetType() == TLFacetType.SHARED) {
                elementName = ownerAlias.getName();
            }
        }

        // Default value is the normal global element name
        if (elementName == null) {
            elementName = getGlobalElementName(facetAlias).getLocalPart();
        }
        return new QName(facet.getNamespace(), elementName);
    }

    /**
     * Returns the name of the global XML schema element associated with head of the entity's
     * substitution, group or null if the type does not represent the head of a substitution group.
     * 
     * @param modelEntity
     *            the model entity for which to return the element name
     * @return QName
     */
    public static QName getSubstitutionGroupElementName(NamedEntity modelEntity) {
        NamedEntity nonAliasEntity = modelEntity;
        QName referenceElementName = null;
        
        if (modelEntity instanceof TLAlias) {
        	nonAliasEntity = ((TLAlias) modelEntity).getOwningEntity();
        }
        boolean isTopLevelFacetOwner =
        		(nonAliasEntity instanceof TLBusinessObject)
                || (nonAliasEntity instanceof TLCoreObject)
                || (nonAliasEntity instanceof TLChoiceObject)
        		|| (nonAliasEntity instanceof TLActionFacet);
        
        if (isTopLevelFacetOwner) {
            QName globalElementName = getGlobalElementName(modelEntity);

            if ((nonAliasEntity instanceof TLChoiceObject) ||
            		(nonAliasEntity instanceof TLActionFacet)) {
            	// Slightly different naming for choice objects and action facets
            	// than cores and business objects
            	referenceElementName = globalElementName;
            	
            } else {
                referenceElementName = new QName(globalElementName.getNamespaceURI(),
                        globalElementName.getLocalPart() + "SubGrp");
            }
        }
        return referenceElementName;
    }
    
    /**
     * In cases where the payload of an action facet maps to a core or choice object, this
     * method will return a facet-specific element name.  If the owner of the payload facet
     * does not match the action facet's base payload type, this method will return null.
     * 
     * @param actionFacet  the action facet to which the payload applies
     * @param payloadFacet  the payload facet for which an element name should be returned
     * @return QName
     */
    public static QName getPayloadElementName(TLActionFacet actionFacet, TLFacet payloadFacet) {
    	QName elementName = null;
    	
    	if (payloadFacet == null) {
    		elementName = getGlobalElementName( actionFacet );
    		
    	} else if (payloadFacet.getOwningEntity() == actionFacet.getBasePayload()) {
    		String facetSuffix = null;
    		
    		elementName = getGlobalElementName( actionFacet );
    		
    		switch (payloadFacet.getFacetType()) {
    			case DETAIL:
    			case CHOICE:
    				facetSuffix = payloadFacet.getFacetType().getIdentityName(
    						FacetCodegenUtils.getFacetName( payloadFacet ) );
    				break;
    			default:
    				break;
    		}
    		if (facetSuffix != null) {
    			elementName = new QName( elementName.getNamespaceURI(),
    					elementName.getLocalPart() + facetSuffix );
    		}
    	}
    	return elementName;
    }
    
    /**
     * Returns true if the given core object is considered "simple" (or an alias of a simple core).
     * Simple cores define summary facet members, but no published or inherited detail facet
     * members.
     * 
     * @param entity
     *            the named entity to analyze
     * @return boolean
     */
    public static boolean isSimpleCoreObject(NamedEntity entity) {
        TLCoreObject core = null;
        boolean isSimple = false;

        if (entity instanceof TLAlias) {
            entity = ((TLAlias) entity).getOwningEntity();
        }
        if (entity instanceof TLFacet) {
            TLFacet facetEntity = (TLFacet) entity;

            if (facetEntity.getFacetType() == TLFacetType.SUMMARY) {
                entity = facetEntity.getOwningEntity();
            }
        }
        if (entity instanceof TLCoreObject) {
            core = (TLCoreObject) entity;
        }

        if (core != null) {
            isSimple = !new FacetCodegenDelegateFactory(null).getDelegate(core.getDetailFacet())
                    .hasContent();
        }
        return isSimple;
    }

    /**
     * Returns the globally-accessible type name for the given entity in the XML schema output.
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may
     * be null.  This indicates that there is no global schema type name associated with the
     * given model entity.
     * 
     * @param modelEntity
     *            the model entity for which to return the element name
     * @return String
     */
    public static String getGlobalTypeName(NamedEntity modelEntity) {
        return getGlobalTypeName(modelEntity, null);
    }

    /**
     * Returns the globally-accessible type name for the given entity in the XML schema output.
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may
     * be null.  This indicates that there is no global schema type name associated with the
     * given model entity.
     * 
     * @param modelEntity
     *            the model entity for which to return the element name
     * @param referencingProperty
     * @return String
     */
    public static String getGlobalTypeName(NamedEntity modelEntity, TLProperty referencingProperty) {
        String typeName;

        if (modelEntity instanceof TLAbstractFacet) {
            typeName = getFacetTypeName((TLAbstractFacet) modelEntity);

        } else if (modelEntity instanceof TLBusinessObject) {
            TLAbstractFacet facet = ((TLBusinessObject) modelEntity).getIdFacet();

            if (referencingProperty != null) {
                facet = PropertyCodegenUtils.findNonEmptyFacet(
                        referencingProperty.getOwner(), facet);
            }
            typeName = getFacetTypeName(facet);

        } else if (modelEntity instanceof TLCoreObject) {
            TLAbstractFacet facet = ((TLCoreObject) modelEntity).getSummaryFacet();

            if (referencingProperty != null) {
                facet = PropertyCodegenUtils.findNonEmptyFacet(
                        referencingProperty.getOwner(), facet);
            }
            typeName = getFacetTypeName(facet);

        } else if (modelEntity instanceof TLChoiceObject) {
        	typeName = getFacetTypeName(((TLChoiceObject) modelEntity).getSharedFacet());
        	
        } else if (modelEntity instanceof TLActionFacet) {
        	NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( (TLActionFacet) modelEntity );
        	
        	if (payloadType == null) {
        		typeName = null;
        		
        	} else if (payloadType instanceof TLActionFacet) {
                typeName = ((TLActionFacet) payloadType).getName();
        		
        	} else {
        		typeName = getGlobalTypeName( payloadType, referencingProperty );
        	}
        	
        } else {
            typeName = modelEntity.getLocalName();
        }
        return typeName;
    }

    /**
     * Returns the type name for the facet in the XML schema output.
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may
     * be null.  This indicates that there is no global schema type name associated with the
     * given model entity.
     * 
     * @param facet
     *            the facet being transformed
     * @return String
     */
    private static String getFacetTypeName(TLAbstractFacet facet) {
        String typeName = facet.getOwningEntity().getLocalName() + getTypeFacetSuffix(facet);

        if (facet.getOwningEntity() instanceof TLCoreObject) {
            if (facet.getFacetType() == TLFacetType.SUMMARY) {
                typeName = facet.getOwningEntity().getLocalName();
            }
            if (facet instanceof TLListFacet) {
                typeName += "_List";
            }
            
        } else if (facet.getOwningEntity() instanceof TLChoiceObject) {
            if (facet.getFacetType() == TLFacetType.SHARED) {
                typeName = facet.getOwningEntity().getLocalName();

            } else if (facet.getFacetType().isContextual()) {
                typeName = facet.getOwningEntity().getLocalName() + getTypeFacetSuffix(facet);
            }
            
        } else if (facet.getOwningEntity() instanceof TLBusinessObject) {
            if (facet instanceof TLFacet) {
                if (facet.getFacetType() == TLFacetType.SUMMARY) {
                    typeName = facet.getOwningEntity().getLocalName();

                } else if (facet.getFacetType().isContextual()) {
                    typeName = facet.getOwningEntity().getLocalName() + getTypeFacetSuffix(facet);
                }
            }
            
        } else if (facet.getOwningEntity() instanceof TLOperation) {
            typeName = ((TLOperation) facet.getOwningEntity()).getName() + getTypeFacetSuffix(facet);
        }
        return typeName;
    }

    /**
     * Returns the type-name suffix to append for the given facet.
     * 
     * @param facet
     *            the facet for which to compute the suffix
     * @return String
     */
    private static String getElementFacetSuffix(TLAbstractFacet facet) {
        return getTypeFacetSuffix(facet).replaceAll("_", "");
    }

    /**
     * Returns the type-name suffix to append for the given facet.
     * 
     * @param facet
     *            the facet for which to compute the suffix
     * @return String
     */
    private static String getTypeFacetSuffix(TLAbstractFacet facet) {
        TLFacetType facetType = facet.getFacetType();
        StringBuilder suffix = new StringBuilder();

        if (facetType.isContextual() && (facet instanceof TLFacet)) {
            TLFacet tlFacet = (TLFacet) facet;
            String facetName = FacetCodegenUtils.getFacetName( tlFacet );
            
            if ((facetType != TLFacetType.CUSTOM) && (facetType != TLFacetType.CHOICE)) {
                suffix.append("_").append(facetType.getIdentityName());
            }
            if ((facetName != null) && !facetName.equals("")) {
                suffix.append("_").append(facetName);
            }
        } else {
            suffix.append("_").append(facetType.getIdentityName());
        }
        return suffix.toString();
    }
    
    /**
     * Returns the base output folder specified by the code generation context provided.
     * 
     * @param context
     *            the code generation context
     * @return File
     */
    public static File getBaseOutputFolder(CodeGenerationContext context) {
        String folderPath = context.getValue(CodeGenerationContext.CK_OUTPUT_FOLDER);
        File outputFolder = null;

        if (folderPath != null) {
            File folder = new File(folderPath);

            if (!folder.exists()) {
                folder.mkdirs();
            }
            if (folder.exists() && folder.isDirectory()) {
                outputFolder = folder;
            }
        }
        if (outputFolder == null) {
            throw new IllegalArgumentException(
                    "Output folder location not specified in the code generation context.");
        }
        return outputFolder;
    }

    /**
     * Returns the sub-folder location (relative to the target output folder) where built-in schemas
     * should be stored during the code generation process. If no sub-folder location is specified
     * by the code generation context, this method will return an empty string, indicating that
     * built-ins schemas should be saved in the same target output folder as the user-defined
     * library/service output.
     * 
     * @param context
     *            the code generation context
     * @return String
     */
    public static String getBuiltInSchemaOutputLocation(CodeGenerationContext context) {
        String subFolder = context.getValue(CodeGenerationContext.CK_BUILTIN_SCHEMA_FOLDER);

        if ((subFolder != null) && !subFolder.endsWith("/")) {
            subFolder += "/";
        }
        return (subFolder == null) ? "" : subFolder;
    }

    /**
     * Returns the sub-folder location (relative to the target output folder) where legacy schemas
     * should be stored during the code generation process. If no sub-folder location is specified
     * by the code generation context, this method will return an empty string, indicating that
     * legacy schemas should be saved in the same target output folder as the user-defined
     * library/service output.
     * 
     * @param context
     *            the code generation context
     * @return String
     */
    public static String getLegacySchemaOutputLocation(CodeGenerationContext context) {
        String subFolder = context.getValue(CodeGenerationContext.CK_LEGACY_SCHEMA_FOLDER);

        if ((subFolder != null) && !subFolder.endsWith("/")) {
            subFolder += "/";
        }
        return (subFolder == null) ? "" : subFolder;
    }

    /**
     * Returns a new ID attribute instance to be included in a simple or complex schema type.
     * 
     * @return Attribute
     */
    public static Attribute createIdAttribute() {
        Attribute idAttribute = new Attribute();

        idAttribute.setName("id");
        idAttribute.setType(XML_SCHEMA_ID_TYPE);
        return idAttribute;
    }

    /**
     * Returns true if the given property type is 'xsd:ID'.
     * 
     * @param type
     *            the property type to analyze
     * @return booean
     */
    public static boolean isIdType(TLPropertyType type) {
        return (type != null) && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type.getNamespace())
                && "ID".equals(type.getLocalName());
    }

    /**
     * Returns true if the given property type is 'xsd:IDREF'.
     * 
     * @param type
     *            the property type to analyze
     * @return booean
     */
    public static boolean isIdRefType(TLPropertyType type) {
        return (type != null) && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type.getNamespace())
                && "IDREF".equals(type.getLocalName());
    }

    /**
     * Returns true if the given property type is 'xsd:IDREFS'.
     * 
     * @param type
     *            the property type to analyze
     * @return booean
     */
    public static boolean isIdRefsType(TLPropertyType type) {
        return (type != null) && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type.getNamespace())
                && "IDREFS".equals(type.getLocalName());
    }

    /**
     * Returns the schema/wsdl-level AppInfo for the given user-defined library.
     * 
     * @param library
     *            the user-defined library instance for which to return application-info
     * @param context
     *            the context for the current code generation task
     * @return Appinfo
     */
    public static Appinfo getAppInfo(AbstractLibrary library, CodeGenerationContext context) {

        Appinfo appInfo = new Appinfo();
        Library libraryInfo = new Library();

        libraryInfo.setProjectName(context.getValue(CodeGenerationContext.CK_PROJECT_FILENAME));
        libraryInfo.setLibraryName(library.getName());
        libraryInfo.setLibraryVersion(library.getVersion());
        libraryInfo.setSourceFile(URLUtils.getShortRepresentation(library.getLibraryUrl()));
        libraryInfo.setCompilerVersion(SchemaCompilerInfo.getInstance().getCompilerVersion());
        libraryInfo.setCompileDate(getCurrentXmlDate());
        appInfo.getContent().add(appInfoObjectFactory.createLibrary(libraryInfo));

        if (library instanceof TLLibrary) {
        	TLLibrary tlLibrary = (TLLibrary) library;
        	
        	if (tlLibrary.getStatus() != null) {
        		libraryInfo.setLibraryStatus(tlLibrary.getStatus().toString());
        	}
        }
        return appInfo;
    }

    /**
     * Returns the schema/wsdl-level service AppInfo for the given library member.
     * 
     * @param libraryMember
     *            the library member for which to return service application-info
     * @param context
     *            the context for the current code generation task
     * @return Appinfo
     */
    @SuppressWarnings("unchecked")
    public static Appinfo getServiceAppInfo(LibraryMember libraryMember,
            CodeGenerationContext context) {
        Appinfo appInfo = getAppInfo((TLLibrary) libraryMember.getOwningLibrary(), context);
        Library libraryInfo = ((JAXBElement<Library>) appInfo.getContent().get(0)).getValue();

        libraryInfo.setServiceName(libraryMember.getLocalName());
        return appInfo;
    }

    /**
     * If required, adds an 'AppInfo' element to the JAXB entity provided with all of the required
     * sub-elements to document its use within the compiler meta-model.
     * 
     * @param modelEntity
     *            the model entity that contains all relevant information to be included in the
     *            'AppInfo' element
     * @param jaxbEntity
     *            the JAXB entity to be decorated with the appropriate 'AppInfo' element(s)
     */
    public static void addAppInfo(NamedEntity modelEntity, Annotated jaxbEntity) {
        if ((modelEntity != null) && (jaxbEntity != null)) {
            Appinfo appInfo = getAppInfo(jaxbEntity);

            // Complex (non-simple) facets do not contain app-info properties, so we need to use the
            // owner's
            // settings in those cases.
            if ((modelEntity instanceof TLAbstractFacet) && !(modelEntity instanceof TLSimpleFacet)) {
                modelEntity = (NamedEntity) ((TLAbstractFacet) modelEntity).getOwningEntity();
            }

            // Add the library type annotation to the appInfo element
            OTA2Entity entityInfo = buildEntityAppInfo(modelEntity);

            if (entityInfo != null) {
                appInfo.getContent().add(appInfoObjectFactory.createOTA2Entity(entityInfo));
            }

            // Add equivalent elements if required
            if (modelEntity instanceof TLEquivalentOwner) {
                addEquivalentInfo((TLEquivalentOwner) modelEntity, appInfo);
            }

            // Add an example element if required
            if (modelEntity instanceof TLExampleOwner) {
                addExampleInfo((TLExampleOwner) modelEntity, appInfo);
            }

            // Remove the entity's annotation if it is still empty at this point
            purgeEmptyAnnotation(jaxbEntity);
        }
    }

    /**
     * Adds equivalent elements to the given app-info schema element from the example owner
     * provided. If the 'appInfo' parameter passed to this method is null, a new one will be created
     * automatically.
     * 
     * @param equivalentOwner
     *            the equivalent owner for which to generate app-info elements
     * @param jaxbEntity
     *            the JAXB entity to be decorated with the appropriate 'AppInfo' element(s)
     */
    public static void addEquivalentInfo(TLEquivalentOwner equivalentOwner, Annotated jaxbEntity) {
        addEquivalentInfo(equivalentOwner, getAppInfo(jaxbEntity));
        purgeEmptyAnnotation(jaxbEntity);
    }

    /**
     * Adds equivalent elements to the given app-info schema element from the example owner
     * provided.
     * 
     * @param equivalentOwner
     *            the equivalent owner for which to generate app-info elements
     * @param appInfo
     *            the 'AppInfo' element to be populated
     */
    private static void addEquivalentInfo(TLEquivalentOwner equivalentOwner, Appinfo appInfo) {
        for (TLEquivalent equivalent : equivalentOwner.getEquivalents()) {
            ContextualValue jaxbEquiv = new ContextualValue();

            jaxbEquiv.setContext(equivalent.getContext());
            jaxbEquiv.setValue(equivalent.getDescription());
            appInfo.getContent().add(appInfoObjectFactory.createEquivalent(jaxbEquiv));
        }
    }

    /**
     * Adds example elements to the given app-info schema element from the example owner provided.
     * If the 'appInfo' parameter passed to this method is null, a new one will be created
     * automatically.
     * 
     * @param exampleOwner
     *            the example owner for which to generate app-info elements
     * @param jaxbEntity
     *            the JAXB entity to be decorated with the appropriate 'AppInfo' element(s)
     */
    public static void addExampleInfo(TLExampleOwner exampleOwner, Annotated jaxbEntity) {
        addExampleInfo(exampleOwner, getAppInfo(jaxbEntity));
        purgeEmptyAnnotation(jaxbEntity);
    }

    /**
     * Adds example elements to the given app-info schema element from the example owner provided.
     * 
     * @param exampleOwner
     *            the example owner for which to generate app-info elements
     * @param appInfo
     *            the 'AppInfo' element to be populated
     */
    private static void addExampleInfo(TLExampleOwner exampleOwner, Appinfo appInfo) {
        for (TLExample example : exampleOwner.getExamples()) {
            ContextualValue jaxbExample = new ContextualValue();

            jaxbExample.setContext(example.getContext());
            jaxbExample.setValue(example.getValue());
            appInfo.getContent().add(appInfoObjectFactory.createExample(jaxbExample));
        }
    }

    /**
     * Returns the 'AppInfo' child element of the given JAXB object. If an 'AppInfo' element is not
     * yet defined, one will be created automatically.
     * 
     * @param jaxbEntity
     *            the JAXB entity for which to return the 'AppInfo' element
     * @return
     */
    private static Appinfo getAppInfo(Annotated jaxbEntity) {
        Annotation annotation = jaxbEntity.getAnnotation();
        Appinfo appInfo = null;

        if (annotation == null) {
            annotation = new Annotation();
            jaxbEntity.setAnnotation(annotation);
        }
        for (Object obj : annotation.getAppinfoOrDocumentation()) {
            if (obj instanceof Appinfo) {
                appInfo = (Appinfo) obj;
                break;
            }
        }
        if (appInfo == null) {
            appInfo = new Appinfo();
            annotation.getAppinfoOrDocumentation().add(appInfo);
        }
        return appInfo;
    }

    /**
     * Searches the contents of the given object's annotation element and removes it if empty.
     * 
     * @param jaxbEntity
     *            the JAXB entity to process
     */
    private static void purgeEmptyAnnotation(Annotated jaxbEntity) {
        Annotation annotation = jaxbEntity.getAnnotation();

        if (annotation != null) {
            // First, look for empty 'AppInfo' elements that need to be purged
            Iterator<Object> iterator = annotation.getAppinfoOrDocumentation().iterator();

            while (iterator.hasNext()) {
                Object obj = iterator.next();

                if (obj instanceof Appinfo) {
                    Appinfo appInfo = (Appinfo) obj;

                    if (appInfo.getContent().isEmpty() && appInfo.getOtherAttributes().isEmpty()) {
                        iterator.remove();
                    }
                }
            }
            if (annotation.getAppinfoOrDocumentation().isEmpty()
                    && annotation.getOtherAttributes().isEmpty()) {
                jaxbEntity.setAnnotation(null);
            }
        }
    }

    /**
     * Returns the current date as an XML object that is compatible with JAXB data structures.
     * 
     * @return XMLGregorianCalendar
     */
    public static XMLGregorianCalendar getCurrentXmlDate() {
        GregorianCalendar cal = new GregorianCalendar();

        cal.setTimeInMillis(System.currentTimeMillis());
        return jaxbDatatypeFactory.newXMLGregorianCalendar(cal);
    }

    /**
     * Returns the <code>OTA2Entity</code> app-info entry that describes the given named entity. If
     * the entity does not have a corresponding app-info description, this method will return null.
     * 
     * @param entityType
     *            the named entity to process
     * @return OTA2Entity
     */
    public static OTA2Entity buildEntityAppInfo(NamedEntity entity) {
        OTA2Entity entityInfo = null;

        if (entity != null) {
            if (entity instanceof TLSimpleFacet) { // special case for simple facets
                entityInfo = buildEntityAppInfo(((TLSimpleFacet) entity).getOwningEntity());

            } else {
                String typeName = libraryTypeNames.get(entity.getClass());

                if (typeName != null) {
                    entityInfo = new OTA2Entity();
                    entityInfo.setType(typeName);
                    entityInfo.setValue(entity.getLocalName());
                }
            }
        }
        return entityInfo;
    }

    /**
     * Returns a camel-case name for a role attribute using the given owner name as a template.
     * 
     * @param roleOwnerName  the name of the role attribute owner
     * @return String
     */
    public static String getRoleAttributeName(String roleOwnerName) {
    	StringBuilder roleName = new StringBuilder().append( roleOwnerName.charAt( 0 ) );
    	
    	if (roleOwnerName.length() > 1 ) {
    		roleName.append( roleOwnerName.substring( 1 ) );
    	}
    	roleName.append( "Role" );
    	return roleName.toString();
    }
    
    /**
     * Initializes the mappings from meta-model classes to library type names.
     */
    static {
        try {
            Map<Class<?>, String> typeNames = new HashMap<Class<?>, String>();

            typeNames.put(TLSimple.class, "Simple");
            typeNames.put(TLValueWithAttributes.class, "ValueWithAttributes");
            typeNames.put(TLCoreObject.class, "CoreObject");
            typeNames.put(TLChoiceObject.class, "ChoiceObject");
            typeNames.put(TLBusinessObject.class, "BusinessObject");
            typeNames.put(TLOpenEnumeration.class, "EnumerationOpen");
            typeNames.put(TLClosedEnumeration.class, "EnumerationClosed");
            typeNames.put(TLOperation.class, "Operation");
            typeNames.put(TLActionFacet.class, "ActionFacet");
            typeNames.put(TLActionRequest.class, "ActionRequest");
            typeNames.put(TLActionResponse.class, "ActionResponse");

            libraryTypeNames = Collections.unmodifiableMap(typeNames);
            jaxbDatatypeFactory = DatatypeFactory.newInstance();

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
