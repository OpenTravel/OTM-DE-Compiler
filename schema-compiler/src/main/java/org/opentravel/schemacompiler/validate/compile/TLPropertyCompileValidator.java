
package org.opentravel.schemacompiler.validate.compile;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyOwner;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDElement;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLPropertyBaseValidator;
import org.opentravel.schemacompiler.validate.impl.CircularReferenceChecker;
import org.opentravel.schemacompiler.validate.impl.FacetMemberIdentityResolver;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.ValidatorUtils;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Validator for the <code>TLProperty</code> class.
 * 
 * @author S. Livezey
 */
public class TLPropertyCompileValidator extends TLPropertyBaseValidator {

	public static final String ERROR_ILLEGAL_LIST_FACET_REFERENCE = "ILLEGAL_LIST_FACET_REFERENCE";
	public static final String WARNING_LIST_FACET_REPEAT_IGNORED  = "LIST_FACET_REPEAT_IGNORED";
	public static final String ERROR_EMPTY_FACET_REFERENCED       = "EMPTY_FACET_REFERENCED";
	public static final String ERROR_ELEMENT_REF_NAME_MISMATCH    = "ELEMENT_REF_NAME_MISMATCH";
	public static final String ERROR_SUBSTITUTION_GROUP_CONFLICT  = "SUBSTITUTION_GROUP_CONFLICT";
	public static final String ERROR_ILLEGAL_REFERENCE            = "ILLEGAL_REFERENCE";
	public static final String ERROR_ILLEGAL_CIRCULAR_REFERENCE   = "ILLEGAL_CIRCULAR_REFERENCE";
	public static final String ERROR_ILLEGAL_REQUIRED_ELEMENT     = "ILLEGAL_REQUIRED_ELEMENT";
	public static final String WARNING_BOOLEAN_TYPE_REFERENCE     = "BOOLEAN_TYPE_REFERENCE";
	public static final String WARNING_UNNECESSARY_EXAMPLE        = "UNNECESSARY_EXAMPLE";
	public static final String WARNING_LEGACY_IDREF               = "LEGACY_IDREF";
	public static final String WARNING_INVALID_REFERENCE_NAME     = "INVALID_REFERENCE_NAME";
	
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLProperty target) {
		TLValidationBuilder dupBuilder = newValidationBuilder(target);
		TLValidationBuilder builder = newValidationBuilder(target);
		TLPropertyType propertyType = target.getType();
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
			.assertPatternMatch(NAME_XML_PATTERN);
		
		builder.setEntityReferenceProperty("type", propertyType, target.getTypeName())
			.setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertValidEntityReference(TLClosedEnumeration.class, TLSimple.class, TLSimpleFacet.class, TLOpenEnumeration.class,
					TLValueWithAttributes.class, TLFacet.class, TLListFacet.class, TLCoreObject.class, TLBusinessObject.class,
					TLAlias.class, TLRoleEnumeration.class, XSDSimpleType.class, XSDComplexType.class, XSDElement.class)
			.setFindingType(FindingType.WARNING).assertNotDeprecated();
		
		if (ValidatorUtils.isLegacyIDREF( propertyType )) {
			builder.addFinding(FindingType.WARNING, "type", WARNING_LEGACY_IDREF);
		}
		
		if ((propertyType != null) && target.isReference() && !hasID(propertyType)) {
			builder.addFinding(FindingType.ERROR, "type", ERROR_ILLEGAL_REFERENCE, target.getTypeName());
		}
		
		checkEmptyValueType(target, target.getType(), "type", builder);
		
		if (CircularReferenceChecker.hasCircularReference(target)) {
			builder.addFinding(FindingType.ERROR, "type", ERROR_ILLEGAL_CIRCULAR_REFERENCE, target.getTypeName());
		}
		
		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();
		
		dupBuilder.setProperty("name", getMembersOfOwner(target)).setFindingType(FindingType.ERROR)
			.assertNoDuplicates(new FacetMemberIdentityResolver());

		// Check for duplicate names of this property
		if (dupBuilder.isEmpty() && (target.getPropertyOwner() instanceof TLFacet)) {
			List<TLModelElement> inheritedMembers = getInheritedMembersOfOwner(target);
			
			dupBuilder.setProperty("name-upa", inheritedMembers).setFindingType(FindingType.ERROR)
				.assertNoDuplicates(new FacetMemberIdentityResolver());
			
			if (dupBuilder.isEmpty()) {
				TLPropertyType resolvedPropertyType = PropertyCodegenUtils.resolvePropertyType(target.getPropertyOwner(), propertyType);
				NamedEntity targtInheritanceRoot = PropertyCodegenUtils.getInheritanceRoot(resolvedPropertyType);
				
				if (targtInheritanceRoot != null) {
					List<String> baseSubstitutionGroups = getBaseSubstitutionGroups(inheritedMembers, target);
					
					if (baseSubstitutionGroups.contains(targtInheritanceRoot.getLocalName())) {
						builder.addFinding(FindingType.ERROR, "name-upa", ERROR_SUBSTITUTION_GROUP_CONFLICT);
					}
				}
			}
		}
		
		// Verify that properties of minor version extension are optional
		if (target.isMandatory() && isVersionExtension( getVersionedOwner(target) )) {
			builder.addFinding(FindingType.ERROR, "mandatory", ERROR_ILLEGAL_REQUIRED_ELEMENT);
		}
		
		// A warning will be issued for boolean properties (should be indicators)
		if (ValidatorUtils.isBooleanType(propertyType)) {
			builder.addFinding(FindingType.WARNING, "type", WARNING_BOOLEAN_TYPE_REFERENCE);
		}
		
		// Issue a warning for properties that reference complex types where the property name and the
		// referenced type's name do not match (the only exception occurs when the referenced property
		// belongs to a built-in library since it cannot be edited to add aliases).
		if (propertyType != null) {
			TLPropertyType resolvedPropertyType = (target.getPropertyOwner() == null) ? propertyType :
				PropertyCodegenUtils.resolvePropertyType(target.getPropertyOwner(), propertyType);
			
			if (PropertyCodegenUtils.hasGlobalElement(resolvedPropertyType)) {
				String referencedLocalName = PropertyCodegenUtils.getDefaultXmlElementName(resolvedPropertyType, target.isReference()).getLocalPart();
				String propertyLocalName = target.getName();
				
				if (!target.isReference()) {
					if ((propertyLocalName != null) && !propertyLocalName.equals(referencedLocalName)) {
						builder.addFinding(FindingType.WARNING, "name", ERROR_ELEMENT_REF_NAME_MISMATCH, referencedLocalName);
					}
				} else {
					if ((propertyLocalName != null) && !propertyLocalName.equals(referencedLocalName)) {
						builder.addFinding(FindingType.WARNING, "name", WARNING_INVALID_REFERENCE_NAME, referencedLocalName);
					}
				}
			} else {
				if (target.isReference() && (target.getName() != null) && !target.getName().endsWith("Ref")) {
					builder.addFinding(FindingType.WARNING, "name", WARNING_INVALID_REFERENCE_NAME);
				}
			}
		}
		
		// Issue a warning if an empty facet is referenced
		if (propertyType instanceof TLAbstractFacet) {
			TLAbstractFacet referencedFacet = (TLAbstractFacet) propertyType;
			FacetCodegenDelegate<TLAbstractFacet> facetDelegate = new FacetCodegenDelegateFactory(null).getDelegate(referencedFacet);
			boolean hasContent = (facetDelegate == null) ? referencedFacet.declaresContent() : facetDelegate.hasContent();
			
			if (!hasContent) {
				builder.addFinding(FindingType.WARNING, "type", ERROR_EMPTY_FACET_REFERENCED,
						referencedFacet.getFacetType().getIdentityName(), referencedFacet.getOwningEntity().getLocalName());
			}
		}
		
		// List facets can only be referenced if the core object defines one or more roles
		boolean isListFacet = (propertyType instanceof TLListFacet) ||
				((propertyType instanceof TLAlias) && (((TLAlias) propertyType).getOwningEntity() instanceof TLListFacet));
		
		if (isListFacet) {
			TLListFacet listFacet = (propertyType instanceof TLListFacet) ? (TLListFacet) propertyType
					: (TLListFacet) ((TLAlias) propertyType).getOwningEntity();
			TLCoreObject referencedCore = (TLCoreObject) listFacet.getOwningEntity();
			int roleCount = referencedCore.getRoleEnumeration().getRoles().size();
			
			if ((roleCount == 0) && !(listFacet.getItemFacet() instanceof TLSimpleFacet)) {
				builder.addFinding(FindingType.ERROR, "type", ERROR_ILLEGAL_LIST_FACET_REFERENCE, referencedCore.getName());
			}
			if ((target.getRepeat() != 0) && (target.getRepeat() != roleCount)) {
				String repeatValue = (target.getRepeat() < 0) ? "*" : (target.getRepeat() + "");
				builder.addFinding(FindingType.WARNING, "repeat", WARNING_LIST_FACET_REPEAT_IGNORED, repeatValue);
			}
		}
		
		// Issue a warning if one or more examples are provided for a complex property type
		if (!(propertyType instanceof TLAttributeType) && (target.getExamples().size() > 0)) {
			builder.addFinding(FindingType.WARNING, "examples", WARNING_UNNECESSARY_EXAMPLE, target.getTypeName());
		}
		
		builder.addFindings( dupBuilder.getFindings() );
		return builder.getFindings();
	}
	
	/**
	 * Returns the list of attributes, properties, and indicators defined by the given property's owner.
	 * 
	 * @param target  the target property being validated
	 * @return List<TLModelElement>
	 */
	@SuppressWarnings("unchecked")
	private List<TLModelElement> getMembersOfOwner(TLProperty target) {
		TLPropertyOwner propertyOwner = target.getPropertyOwner();
		String cacheKey = propertyOwner.getNamespace() + ":" + propertyOwner.getLocalName() + ":members";
		List<TLModelElement> members = (List<TLModelElement>) getContextCacheEntry(cacheKey);
		
		if (members == null) {
			if (propertyOwner instanceof TLExtensionPointFacet) {
				members = ValidatorUtils.getMembers( (TLExtensionPointFacet) propertyOwner );			
				
			} else { // TLFacet
				members = ValidatorUtils.getMembers( (TLFacet) propertyOwner );			
			}
			setContextCacheEntry(cacheKey, members);
		}
		return members;
	}
	
	/**
	 * Returns the list of inherited attributes, properties, and indicators defined by the given property's owner.
	 * 
	 * @param target  the target property being validated
	 * @return List<TLModelElement>
	 */
	@SuppressWarnings("unchecked")
	private List<TLModelElement> getInheritedMembersOfOwner(TLProperty target) {
		TLPropertyOwner propertyOwner = target.getPropertyOwner();
		String cacheKey = propertyOwner.getNamespace() + ":" + propertyOwner.getLocalName() + ":inheritedMembers";
		List<TLModelElement> members = (List<TLModelElement>) getContextCacheEntry(cacheKey);
		
		if (members == null) {
			members = ValidatorUtils.getInheritedMembers( (TLFacet) propertyOwner );
			setContextCacheEntry(cacheKey, members);
		}
		return members;
	}
	
	/**
	 * Returns all of the base substitution groups for the complex types defined in the given list
	 * of model elements.
	 * 
	 * @param ownerElements  the list of model elements identified for a property owner
	 * @param target  the target property being validated
	 * @return List<String>
	 */
	private List<String> getBaseSubstitutionGroups(List<TLModelElement> ownerElements, TLProperty target) {
		List<String> baseSubstitutionGroups = new ArrayList<String>();
		
		for (TLModelElement ownerElement : ownerElements) {
			if ((ownerElement != target) && (ownerElement instanceof TLProperty)) {
				TLProperty property = (TLProperty) ownerElement;
				TLPropertyType propertyType = PropertyCodegenUtils.resolvePropertyType(property.getPropertyOwner(), property.getType());
				NamedEntity inheritanceRoot = PropertyCodegenUtils.getInheritanceRoot(propertyType);
				
				if (inheritanceRoot != null) {
					baseSubstitutionGroups.add( inheritanceRoot.getLocalName() );
				}
			}
		}
		return baseSubstitutionGroups;
	}
	
	/**
	 * Returns true if the given named entity publishes at least one ID value as an attribute or
	 * element.
	 * 
	 * @param entity  the named entity to analyze
	 * @return boolean
	 */
	private boolean hasID(NamedEntity entity) {
		boolean result = false;
		
		if (entity instanceof TLValueWithAttributes) {
			TLValueWithAttributes vwa = (TLValueWithAttributes) entity;
			
			for (TLAttribute attribute : PropertyCodegenUtils.getInheritedAttributes(vwa)) {
				if (isIDType(attribute.getType())) {
					result = true;
					break;
				}
			}
			
		} else { // non-VWA entity
			TLFacet entityFacet = null;
			
			if (entity instanceof TLAlias) {
				entity = ((TLAlias) entity).getOwningEntity();
			}
			
			if (entity instanceof TLFacet) {
				entityFacet = (TLFacet) entity;
				
			} else if (entity instanceof TLCoreObject) {
				entityFacet = ((TLCoreObject) entity).getSummaryFacet();
				
			} else if (entity instanceof TLBusinessObject) {
				entityFacet = ((TLBusinessObject) entity).getSummaryFacet();
			}
			
			if (entityFacet != null) {
				for (TLAttribute attribute : PropertyCodegenUtils.getInheritedAttributes(entityFacet)) {
					if (isIDType(attribute.getType())) {
						result = true;
						break;
					}
				}
				if (!result) {
					for (TLProperty property : PropertyCodegenUtils.getInheritedProperties(entityFacet)) {
						if (isIDType(property.getType())) {
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns true if the given type reference is 'xsd:ID'.
	 * 
	 * @param type  the type reference to analyze
	 * @return boolean
	 */
	private boolean isIDType(NamedEntity type) {
		return (type != null) && type.getNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)
				&& type.getLocalName().equals("ID");
	}
	
	/**
	 * Returns the <code>Versioned</code> owner of the target property.
	 * 
	 * @param target  the target property being validated
	 * @return Versioned
	 */
	private Versioned getVersionedOwner(TLProperty target) {
		Versioned owner = null;
		
		if (target.getPropertyOwner() instanceof TLFacet) {
			TLFacetOwner facetOwner = ((TLFacet) target.getPropertyOwner()).getOwningEntity();
			
			if (facetOwner instanceof Versioned) {
				owner = (Versioned) facetOwner;
			}
		}
		return owner;
	}
	
}
