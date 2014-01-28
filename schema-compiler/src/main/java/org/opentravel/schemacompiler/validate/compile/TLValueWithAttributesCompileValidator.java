
package org.opentravel.schemacompiler.validate.compile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLValueWithAttributesBaseValidator;
import org.opentravel.schemacompiler.validate.impl.CircularReferenceChecker;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.ValidatorUtils;
import org.opentravel.schemacompiler.version.PatchVersionHelper;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;

/**
 * Validator for the <code>TLValueWithAttributes</code> class.
 * 
 * @author S. Livezey
 */
public class TLValueWithAttributesCompileValidator extends TLValueWithAttributesBaseValidator {

	public static final String ERROR_ILLEGAL_EXTENSION_ATTRIBUTE = "ILLEGAL_EXTENSION_ATTRIBUTE";
	public static final String ERROR_EXTENSION_NAME_CONFLICT     = "EXTENSION_NAME_CONFLICT";
	public static final String ERROR_INHERITANCE_TYPE_CONFLICT   = "INHERITANCE_TYPE_CONFLICT";
	public static final String ERROR_INVALID_CIRCULAR_REFERENCE  = "INVALID_CIRCULAR_REFERENCE";
	public static final String ERROR_MULTIPLE_ID_MEMBERS         = "MULTIPLE_ID_MEMBERS";
	public static final String ERROR_ILLEGAL_PATCH               = "ILLEGAL_PATCH";
	public static final String ERROR_INVALID_VERSION_EXTENSION   = "INVALID_VERSION_EXTENSION";
	
	/**
	 * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLValueWithAttributes target) {
		List<TLModelElement> inheritedMembers = ValidatorUtils.getInheritedMembers(target);
		TLValidationBuilder builder = newValidationBuilder(target);
		
		builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
			.assertNotNullOrBlank()
			.assertPatternMatch(NAME_XML_PATTERN);
		
		if ((target.getParentType() == null) && (target.getParentTypeName() != null)) {
			builder.addFinding(FindingType.ERROR, "parentType", TLValidationBuilder.UNRESOLVED_NAMED_ENTITY_REFERENCE,
					target.getParentTypeName());
		}
		
		builder.setEntityReferenceProperty("parentType", target.getParentType(), target.getParentTypeName())
			.setFindingType(FindingType.ERROR)
				.assertValidEntityReference(TLSimple.class, TLClosedEnumeration.class, XSDSimpleType.class,
						TLOpenEnumeration.class, TLRoleEnumeration.class, TLValueWithAttributes.class)
			.setFindingType(FindingType.WARNING).assertNotDeprecated();
		
		builder.setProperty("attributes", target.getAttributes()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();
		
		builder.setProperty("attributes", inheritedMembers).setFindingType(FindingType.WARNING)
			.assertMinimumSize(1);
		
		builder.setProperty("indicators", target.getIndicators()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();

		checkEmptyValueType(target, target.getParentType(), "parentType", builder);
		
		builder.setProperty("equivalents", target.getEquivalents()).setFindingType(FindingType.ERROR)
			.assertNotNull()
			.assertContainsNoNullElements();
		
		if (ValidatorUtils.hasMultipleIdMembers(target)) {
			builder.addFinding(FindingType.ERROR, "members", ERROR_MULTIPLE_ID_MEMBERS);
		}
		
		if (parentTypeIsOpenEnumeration(target) && hasExtensionAttribute(inheritedMembers)) {
			builder.addFinding(FindingType.ERROR, "parentType", ERROR_ILLEGAL_EXTENSION_ATTRIBUTE);
		}
		
		// Check to see if any of the declared attribute/indicator names conflict with the 'Extension'
		// attributes created for the open enumeration attributes.
		Set<String> openEnumerationAttributes = new HashSet<String>();
		Set<String> attributeNames = new HashSet<String>();
		
		for (TLModelElement member : inheritedMembers) {
			if (member instanceof TLAttribute) {
				TLAttribute attribute = (TLAttribute) member;
				
				if (isOpenEnumerationAttribute( attribute )) {
					openEnumerationAttributes.add( attribute.getName() );
				}
				attributeNames.add( attribute.getName() );
			} else { // must be an indicator
				attributeNames.add( ((TLIndicator) member).getName() );
			}
		}
		for (String openEnumAttrName : openEnumerationAttributes) {
			if (attributeNames.contains(openEnumAttrName + "Extension")) {
				builder.addFinding(FindingType.ERROR, "attributes", ERROR_ILLEGAL_EXTENSION_ATTRIBUTE, openEnumAttrName);
				break;
			}
		}
		
		// Check to see if any duplicate attributes (assumed to be inherited from VWA attributes) have the same
		// name but different type assignments.
		List<TLAttribute> attributesWithDuplicates = PropertyCodegenUtils.getInheritedAttributes(target);
		Map<String,TLAttributeType> attributeTypes = new HashMap<String, TLAttributeType>();
		
		for (TLAttribute attribute : attributesWithDuplicates) {
			TLAttributeType existingType = attributeTypes.get( attribute.getName() );
			
			if (existingType == null) { // First time we have seen an attribute with this name
				attributeTypes.put(attribute.getName(), attribute.getType());
				
			} else if (existingType != attribute.getType()) { // Make sure the attribute types are identical
				builder.addFinding(FindingType.ERROR, "attributes", ERROR_INHERITANCE_TYPE_CONFLICT, attribute.getName());
				break; // stop after the first duplicate
			}
		}
		
		// Validate versioning rules
		try {
			PatchVersionHelper helper = new PatchVersionHelper();
			VersionScheme vScheme = helper.getVersionScheme( target );
			
			if ((vScheme != null) && vScheme.isPatchVersion(target.getNamespace())) {
				builder.addFinding(FindingType.ERROR, "name", ERROR_ILLEGAL_PATCH);
			}
			
			if (isInvalidVersionExtension(target)) {
				builder.addFinding(FindingType.ERROR, "versionExtension", ERROR_INVALID_VERSION_EXTENSION);
			}
			checkMajorVersionNamingConflicts(target, builder);
			
		} catch (VersionSchemeException e) {
			// Ignore - Invalid version scheme error will be reported when the owning library is validated
		}
		
		// Check for circular references
		if ( CircularReferenceChecker.hasCircularReference(target) ) {
			builder.addFinding(FindingType.ERROR, "parentType", ERROR_INVALID_CIRCULAR_REFERENCE);
		}
		
		checkSchemaNamingConflicts( target, builder );
		
		return builder.getFindings();
	}
	
	/**
	 * Returns true if the given attribute's type is an open enumeration or a VWA whose base type
	 * is an open enumeration.
	 * 
	 * @param attribute  the attribute to analyze
	 * @return boolean
	 */
	private boolean isOpenEnumerationAttribute(TLAttribute attribute) {
		TLAttributeType attributeType = attribute.getType();
		
		return (attributeType instanceof TLOpenEnumeration) || ( (attributeType instanceof TLValueWithAttributes)
				&& parentTypeIsOpenEnumeration((TLValueWithAttributes) attributeType) );
	}
	
	/**
	 * Returns true if the parent type of the VWA is a open (or role) enumeration.  In the case where the
	 * given VWA's parent type is another VWA, this method will recursively determine if the final parent
	 * type is an open enumeration.
	 * 
	 * @param target  the target VWA being validated
	 * @return boolean
	 */
	private boolean parentTypeIsOpenEnumeration(TLValueWithAttributes vwa) {
		return parentTypeIsOpenEnumeration( vwa, new HashSet<TLValueWithAttributes>() );
	}
	
	/**
	 * Recursive method that checks whether the parent type of the VWA is an open enumeration, while
	 * protecting from infinite loops due to circular references.
	 * 
	 * @param target  the target VWA being validated
	 * @return boolean
	 */
	private boolean parentTypeIsOpenEnumeration(TLValueWithAttributes vwa, Set<TLValueWithAttributes> visitedVwas) {
		NamedEntity parentType = vwa.getParentType();
		boolean isOpenEnum = false;
		
		if ((parentType instanceof TLOpenEnumeration) || (parentType instanceof TLRoleEnumeration)) {
			isOpenEnum = true;
			
		} else if (parentType instanceof TLValueWithAttributes) {
			TLValueWithAttributes parentVWA = (TLValueWithAttributes) parentType;
			
			if (!visitedVwas.contains(parentVWA)) {
				visitedVwas.add( parentVWA );
				isOpenEnum = parentTypeIsOpenEnumeration( parentVWA, visitedVwas );
			}
		}
		return isOpenEnum;
	}
	
	/**
	 * Returns true if the given list of VWA members contains an attribute with the name "extension".  If the
	 * parent type is an open/role enumeration, the name will interfere with the implied attribute used for
	 * un-declared enumeration values.
	 * 
	 * <p>NOTE: Indicators with the name "extension" are not considered, since the compiler will automatically
	 * append the "...Ind" suffix during schema generation.
	 * 
	 * @param inheritedMembers  the list of attributes and indicators declared or inherited by the VWA
	 * @return boolean
	 */
	private boolean hasExtensionAttribute(List<TLModelElement> inheritedMembers) {
		boolean result = false;
		
		for (TLModelElement member : inheritedMembers) {
			if (member instanceof TLAttribute) {
				TLAttribute attr = (TLAttribute) member;
				
				if ((attr.getName() != null) && attr.getName().equals("extension")) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
}
