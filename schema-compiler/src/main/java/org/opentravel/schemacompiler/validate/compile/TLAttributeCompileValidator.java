package org.opentravel.schemacompiler.validate.compile;

import java.util.List;

import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLAttributeBaseValidator;
import org.opentravel.schemacompiler.validate.impl.FacetMemberIdentityResolver;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.ValidatorUtils;
import org.opentravel.schemacompiler.version.Versioned;

/**
 * Validator for the <code>TLAttribute</code> class.
 * 
 * @author S. Livezey
 */
public class TLAttributeCompileValidator extends TLAttributeBaseValidator {

    public static final String ERROR_NON_SIMPLE_CORE_AS_ATTRIBUTE = "NON_SIMPLE_CORE_AS_ATTRIBUTE";
    public static final String ERROR_ILLEGAL_LIST_FACET_REFERENCE = "ILLEGAL_LIST_FACET_REFERENCE";
    public static final String ERROR_EMPTY_FACET_REFERENCED = "EMPTY_FACET_REFERENCED";
    public static final String ERROR_ILLEGAL_VWA_ATTRIBUTE = "ILLEGAL_VWA_ATTRIBUTE";
    public static final String ERROR_ILLEGAL_OPEN_ENUM_ATTRIBUTE = "ILLEGAL_OPEN_ENUM_ATTRIBUTE";
    public static final String ERROR_ILLEGAL_REQUIRED_ATTRIBUTE = "ILLEGAL_REQUIRED_ATTRIBUTE";
    public static final String WARNING_BOOLEAN_TYPE_REFERENCE = "BOOLEAN_TYPE_REFERENCE";
    public static final String WARNING_LEGACY_IDREF = "LEGACY_IDREF";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLAttribute target) {
        TLValidationBuilder dupBuilder = newValidationBuilder(target);
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
                .assertPatternMatch(NAME_XML_PATTERN);

        builder.setEntityReferenceProperty("type", target.getType(), target.getTypeName())
                .setFindingType(FindingType.ERROR)
                .assertNotNull()
                .assertValidEntityReference(TLClosedEnumeration.class, TLOpenEnumeration.class,
                        TLValueWithAttributes.class, TLSimple.class, TLCoreObject.class,
                        TLSimpleFacet.class, TLListFacet.class, XSDSimpleType.class)
                .setFindingType(FindingType.WARNING).assertNotDeprecated();

        // VWA and open enumeration attributes are only allowed when the attribute's owner is a VWA
        if (!(target.getAttributeOwner() instanceof TLValueWithAttributes)) {
            if (target.getType() instanceof TLValueWithAttributes) {
                builder.addFinding(FindingType.ERROR, "type", ERROR_ILLEGAL_VWA_ATTRIBUTE);
            } else if (target.getType() instanceof TLOpenEnumeration) {
                builder.addFinding(FindingType.ERROR, "type", ERROR_ILLEGAL_OPEN_ENUM_ATTRIBUTE);
            }
        }

        checkEmptyValueType(target, target.getType(), "type", builder);

        builder.setProperty("equivalents", target.getEquivalents())
                .setFindingType(FindingType.ERROR).assertNotNull().assertContainsNoNullElements();

        // Check for duplicate names of this attribute
        dupBuilder.setProperty("name", getMembersOfOwner(target)).setFindingType(FindingType.ERROR)
                .assertNoDuplicates(new FacetMemberIdentityResolver());

        if (dupBuilder.isEmpty() && !(target.getAttributeOwner() instanceof TLValueWithAttributes)) { // Does
                                                                                                      // not
                                                                                                      // apply
                                                                                                      // to
                                                                                                      // VWA's
            List<TLModelElement> inheritedMembersOfOwner = getInheritedMembersOfOwner(target);

            if (inheritedMembersOfOwner != null) {
                dupBuilder.setProperty("name-upa", inheritedMembersOfOwner)
                        .setFindingType(FindingType.ERROR)
                        .assertNoDuplicates(new FacetMemberIdentityResolver());
            }
        }

        // Verify that properties of minor version extension are optional
        if (target.isMandatory() && isVersionExtension(getVersionedOwner(target))) {
            builder.addFinding(FindingType.ERROR, "mandatory", ERROR_ILLEGAL_REQUIRED_ATTRIBUTE);
        }

        // A warning will be issued for boolean attributes (should be indicators)
        if (ValidatorUtils.isBooleanType(target.getType())) {
            builder.addFinding(FindingType.WARNING, "type", WARNING_BOOLEAN_TYPE_REFERENCE);
        }

        // Cores are only allowed as attribute types if they publish a simple facet
        if (target.getType() instanceof TLCoreObject) {
            TLCoreObject coreObject = (TLCoreObject) target.getType();

            if (!coreObject.getSimpleFacet().declaresContent()) {
                builder.addFinding(FindingType.ERROR, "type", ERROR_NON_SIMPLE_CORE_AS_ATTRIBUTE,
                        coreObject.getLocalName());
            }
        }

        if (ValidatorUtils.isLegacyIDREF(target.getType())) {
            builder.addFinding(FindingType.WARNING, "type", WARNING_LEGACY_IDREF);
        }

        // Issue a warning if an empty facet is referenced
        if (target.getType() instanceof TLAbstractFacet) {
            TLAbstractFacet referencedFacet = (TLAbstractFacet) target.getType();

            if (!referencedFacet.declaresContent()) {
                builder.addFinding(FindingType.WARNING, "type", ERROR_EMPTY_FACET_REFERENCED,
                        referencedFacet.getFacetType().getIdentityName(), referencedFacet
                                .getOwningEntity().getLocalName());
            }
        }

        // List facets can only be referenced if the core object defines one or more roles
        if (target.getType() instanceof TLListFacet) {
            TLListFacet listFacet = (target.getType() instanceof TLListFacet) ? (TLListFacet) target
                    .getType() : (TLListFacet) ((TLAlias) target.getType()).getOwningEntity();
            TLCoreObject referencedCore = (TLCoreObject) ((TLListFacet) target.getType())
                    .getOwningEntity();
            int roleCount = referencedCore.getRoleEnumeration().getRoles().size();

            if ((roleCount == 0) && !(listFacet.getItemFacet() instanceof TLSimpleFacet)) {
                builder.addFinding(FindingType.ERROR, "type", ERROR_ILLEGAL_LIST_FACET_REFERENCE,
                        referencedCore.getName());
            }
        }

        builder.addFindings(dupBuilder.getFindings());
        return builder.getFindings();
    }

    /**
     * Returns the list of attributes, properties, and indicators defined by the given attribute's
     * owner.
     * 
     * @param target
     *            the target attribute being validated
     * @return List<TLModelElement>
     */
    @SuppressWarnings("unchecked")
    private List<TLModelElement> getMembersOfOwner(TLAttribute target) {
        TLAttributeOwner attrOwner = target.getAttributeOwner();
        String cacheKey = attrOwner.getNamespace() + ":" + attrOwner.getLocalName() + ":members";
        List<TLModelElement> members = (List<TLModelElement>) getContextCacheEntry(cacheKey);

        if (members == null) {
            if (attrOwner instanceof TLValueWithAttributes) {
                members = ValidatorUtils.getMembers((TLValueWithAttributes) attrOwner);

            } else if (attrOwner instanceof TLExtensionPointFacet) {
                members = ValidatorUtils.getMembers((TLExtensionPointFacet) attrOwner);

            } else { // TLFacet
                members = ValidatorUtils.getMembers((TLFacet) attrOwner);
            }
            setContextCacheEntry(cacheKey, members);
        }
        return members;
    }

    /**
     * Returns the list of inherited attributes, properties, and indicators defined by the given
     * attribute's owner. If the given attribute is not owned by a facet or VWA, this method will
     * return null (indicating that the concept of inheritance does not apply to the owner).
     * 
     * @param target
     *            the target attribute being validated
     * @return List<TLModelElement>
     */
    @SuppressWarnings("unchecked")
    private List<TLModelElement> getInheritedMembersOfOwner(TLAttribute target) {
        TLAttributeOwner attrOwner = target.getAttributeOwner();
        String cacheKey = attrOwner.getNamespace() + ":" + attrOwner.getLocalName()
                + ":inheritedMembers";
        List<TLModelElement> members = (List<TLModelElement>) getContextCacheEntry(cacheKey);

        if (members == null) {
            if (attrOwner instanceof TLFacet) {
                members = ValidatorUtils.getInheritedMembers((TLFacet) attrOwner);

            } else if (attrOwner instanceof TLValueWithAttributes) {
                members = ValidatorUtils.getInheritedMembers((TLValueWithAttributes) attrOwner);
            }

            if (members != null) {
                setContextCacheEntry(cacheKey, members);
            }
        }
        return members;
    }

    /**
     * Returns the <code>Versioned</code> owner of the target attribute.
     * 
     * @param target
     *            the target attribute being validated
     * @return Versioned
     */
    private Versioned getVersionedOwner(TLAttribute target) {
        Versioned owner = null;

        if (target.getAttributeOwner() instanceof TLFacet) {
            TLFacetOwner facetOwner = ((TLFacet) target.getAttributeOwner()).getOwningEntity();

            if (facetOwner instanceof Versioned) {
                owner = (Versioned) facetOwner;
            }
        } else if (target.getAttributeOwner() instanceof TLValueWithAttributes) {
            owner = (Versioned) target.getAttributeOwner();
        }
        return owner;
    }

}
