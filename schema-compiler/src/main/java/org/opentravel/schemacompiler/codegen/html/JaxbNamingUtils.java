/*
 * Copyright (c) 2014, Travelport.  All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.codegen.html;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;

import com.sun.xml.bind.api.impl.NameConverter;

/**
 * Static utility methods used to obtain class and property names for JAXB binding
 * classes.
 *
 * @author S. Livezey
 */
public class JaxbNamingUtils {
	
    // see http://java.sun.com/docs/books/tutorial/java/nutsandbolts/_keywords.html
	private static final String[] javaKeywords = new String[] {
		"abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
		"continue", "default", "do", "double", "else", "extends", "final", "finally",
		"float", "for", "goto", "if", "implements", "import", "instanceof", "int",
		"interface", "long", "native", "new", "package", "private", "protected", "public",
		"return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
		"throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false",
		"null", "assert", "enum"
	};
    
    private static final Set<String> reservedKeywords;
    
	/**
	 * Private constructor to prevent instantiation.
	 */
	private JaxbNamingUtils() {}
	
	/**
	 * Returns the name of the JAXB binding class for the given model entity.  If the model entity does not
	 * have a JAXB equivalent, this method will return null.  If the package mappings are omitted (null),
	 * only the simple name of the JAXB class will be returned.
	 * 
	 * @param entity  the JAXB model entity for which to return a class name
	 * @param packageMappings  the package mapping information for the model
	 * @return String
	 */
	public static String getJaxbClassname(NamedEntity entity) {
		boolean isIDREFType = XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(entity.getNamespace())
				&& ("IDREF".equals(entity.getLocalName()) || "IDREFS".equals(entity.getLocalName()));
		String jaxbClassname = null;
		
		if (isIDREFType) {
			jaxbClassname = Object.class.getName();
			
		} else {
			NamedEntity typedEntity = entity;
			
			if (entity instanceof TLAlias) {
				typedEntity = ((TLAlias) entity).getOwningEntity();
			}
			String xmlTypeName = XsdCodegenUtils.getGlobalTypeName(typedEntity);
			String className = toJavaIdentifier( NameConverter.smart.toClassName(xmlTypeName) );
			StringBuilder nameBuilder = new StringBuilder();
			
				nameBuilder.append( NameConverter.smart.toPackageName(entity.getNamespace() ) ).append( '.' );
		
			jaxbClassname = nameBuilder.append( className ).toString();
		}
		return jaxbClassname;
	}
	
	
	
	/**
	 * Returns the JAXB binding class for the entity with the given local name and namespace assignment.
	 * 
	 * @param namespace  the namespace of the JAXB type
	 * @param localName  the local name of the JAXB type
	 * @param packageMappings  the package mapping information for the model
	 * @return String
	 */
	public static String getJaxbClassname(String namespace, String localName) {
		String className = toJavaIdentifier( NameConverter.smart.toClassName(localName) );
		StringBuilder nameBuilder = new StringBuilder();
		
		if (namespace != null) {
			nameBuilder.append( NameConverter.smart.toPackageName( namespace ) ).append( '.' );
		}
		nameBuilder.append(className);
		return nameBuilder.toString();
	}
	
	/**
	 * Returns the JAXB field name for the given attribute.
	 * 
	 * @param attribute  the attribute for which to construct a field name
	 * @return String
	 */
	public static String getJaxbFieldName(TLAttribute attribute) {
		return toJavaIdentifier( NameConverter.smart.toVariableName( attribute.getName() ) );
	}
	
	/**
	 * Returns the JAXB field name for the given element.
	 * 
	 * @param element  the element for which to construct a field name
	 * @return String
	 */
	public static String getJaxbFieldName(TLProperty element) {
		String elementName;
		
		if (PropertyCodegenUtils.hasGlobalElement(element.getType())) {
			elementName = PropertyCodegenUtils.getDefaultXmlElementName(element.getType(), element.isReference()).getLocalPart();
			
			if (isSubstitutionGroupReference(element)) {
				boolean isIDREF = element.isReference() || XsdCodegenUtils.isIdRefType(element.getType())
						|| XsdCodegenUtils.isIdRefsType(element.getType());
				
				// If the element is an IDREF(S) to a substitution group element, JAXB will use the
				// non-substitution group name for the field
				if (!isIDREF) {
					elementName += "SubGrp";
				}
			}
		} else {
			elementName = element.getName();
		}
		return NameConverter.smart.toVariableName( elementName );
	}
	
	/**
	 * Returns the JAXB field name for the given indicator.
	 * 
	 * @param indicator  the indicator for which to construct a field name
	 * @return String
	 */
	public static String getJaxbFieldName(TLIndicator indicator) {
		String indicatorName = indicator.getName();
		
		if (!indicatorName.endsWith("Ind")) {
			indicatorName += "Ind";
		}
		return NameConverter.smart.toVariableName( indicatorName );
	}
	
	/**
	 * Returns the JAXB field name for the extension point element of the given facet.
	 * 
	 * @param facet  the facet for which to calculate the extension point field name
	 * @return String
	 */
	public static String getJaxbExtensionPointFieldName(TLFacet facet) {
		TLFacetCodegenDelegate facetDelegate = (TLFacetCodegenDelegate) new FacetCodegenDelegateFactory(null).getDelegate(facet);
		QName extensionPointQName = facetDelegate.getExtensionPointElement();
		String fieldName = null;
		
		if (extensionPointQName != null) {
			fieldName = NameConverter.smart.toVariableName( extensionPointQName.getLocalPart() );
		}
		return fieldName;
	}
	
	/**
	 * Returns true if the given property references an entity that is the root of a substitution group.
	 * 
	 * @param element  the element to analyze
	 * @return boolean
	 */
	public static boolean isSubstitutionGroupReference(TLProperty element) {
		TLPropertyType elementType = element.getType();
		boolean isSubGrpReference = false;
		
		if (elementType instanceof TLAlias) {
			elementType = (TLPropertyType) ((TLAlias) elementType).getOwningEntity();
		}
		if (elementType instanceof TLBusinessObject) {
			isSubGrpReference = true;
			
		} else if (elementType instanceof TLCoreObject) {
			isSubGrpReference = !XsdCodegenUtils.isSimpleCoreObject(elementType);
		}
		return isSubGrpReference;
	}
	
	/**
	 * Returns true if the given model entity may contain extension point facet elements.
	 * 
	 * @param entity  the OTM entity to analyze
	 * @return boolean
	 */
	public static boolean supportsExtensionPoints(NamedEntity entity) {
		boolean result = false;
		TLFacet facet = null;
		
		if (entity instanceof TLAlias) {
			entity = ((TLAlias) entity).getOwningEntity();
		}
		if (entity instanceof TLFacet) {
			facet = (TLFacet) entity;
		}
		if (facet != null) {
			result = new FacetCodegenDelegateFactory(null).getDelegate(facet).hasExtensionPoint();
		}
		return result;
	}
	
	/**
	 * Returns true if the given entity is a simple type that represent multiple values.
	 * 
	 * @param entity  the entity type to analyze
	 * @return boolean
	 */
	public static boolean isSimpleListType(NamedEntity entity) {
		return ((entity instanceof TLPropertyType) && XsdCodegenUtils.isIdRefsType( (TLPropertyType) entity ))
				|| ((entity instanceof TLSimple) && ((TLSimple) entity).isListTypeInd());
	}
	
	/**
	 * Returns true if the given identifier is one of the reserved Java keywords.
	 * 
	 * @param identifier  the identifier to analyze
	 * @return boolean
	 */
	public static boolean isJavaKeyword(String identifier) {
		return reservedKeywords.contains(identifier);
	}
	
	/**
	 * Returns a valid Java identifier using the value provided as a base.  If the identifier is
	 * already a valid keyword, it will be returned as-is.  If not, the identifier will be returned
	 * with a '_' prefix.
	 * 
	 * @param identifier  the identifier string to process
	 * @return String
	 */
	public static String toJavaIdentifier(String identifier) {
		return isJavaKeyword(identifier) ? ("_" + identifier) : identifier;
	}
	
    /**
     * Initializes the set of Java keywords.
     */
    static {
        try {
        	Set<String> _reservedKeywords = new HashSet<String>();
        	
        	_reservedKeywords.addAll( Arrays.asList(javaKeywords) );
        	reservedKeywords = Collections.unmodifiableSet(_reservedKeywords);
        	
        } catch (Exception t) {
        	throw new ExceptionInInitializerError(t);
        }
    }
    
}
