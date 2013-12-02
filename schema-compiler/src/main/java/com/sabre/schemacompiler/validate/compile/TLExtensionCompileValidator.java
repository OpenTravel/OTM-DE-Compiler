/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.compile;

import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLComplexTypeBase;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLExtension;
import com.sabre.schemacompiler.model.TLExtensionOwner;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLFacetOwner;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.validate.FindingType;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.impl.CircularReferenceChecker;
import com.sabre.schemacompiler.validate.impl.TLValidationBuilder;
import com.sabre.schemacompiler.validate.impl.TLValidatorBase;

/**
 * Validator for the <code>TLExtension</code> class.
 *
 * @author S. Livezey
 */
public class TLExtensionCompileValidator extends TLValidatorBase<TLExtension> {
	
	public static final String ERROR_INVALID_CIRCULAR_EXTENSION    = "INVALID_CIRCULAR_EXTENSION";
	public static final String ERROR_INVALID_LOCAL_FACET_EXTENSION = "INVALID_LOCAL_FACET_EXTENSION";
	public static final String ERROR_ILLEGAL_EXTENSION             = "ILLEGAL_EXTENSION";
	public static final String WARNING_MUST_BE_EXTENSIBLE          = "MUST_BE_EXTENSIBLE";

	/**
	 * @see com.sabre.schemacompiler.validate.impl.TLValidatorBase#validateFields(com.sabre.schemacompiler.validate.Validatable)
	 */
	@Override
	protected ValidationFindings validateFields(TLExtension target) {
		TLValidationBuilder builder = newValidationBuilder(target);
		NamedEntity extendsEntity = target.getExtendsEntity();
		TLExtensionOwner extensionOwner = target.getOwner();
		
		builder.setEntityReferenceProperty("extendsEntity", target.getExtendsEntity(), target.getExtendsEntityName())
			.setFindingType(FindingType.ERROR).assertNotNull()
			.setFindingType(FindingType.WARNING).assertNotDeprecated();
		
		if (target.getExtendsEntity() != null) {
			// Assert the correct type of entity reference based on the extension owner's type
			if (extensionOwner instanceof TLBusinessObject) {
				builder.setFindingType(FindingType.ERROR).assertValidEntityReference(TLBusinessObject.class);
				
			} else if (extensionOwner instanceof TLCoreObject) {
				builder.setFindingType(FindingType.ERROR).assertValidEntityReference(TLCoreObject.class);
				
			} else if (extensionOwner instanceof TLOperation) {
				builder.setFindingType(FindingType.ERROR).assertValidEntityReference(TLOperation.class);
				
			} else if (extensionOwner instanceof TLExtensionPointFacet) {
				String extendsEntityNamespace = extendsEntity.getNamespace();
				String localNamespace = ((TLExtensionPointFacet) extensionOwner).getNamespace();
				
				// Extension point facets can only extend facets from another namespace
				builder.assertValidEntityReference(TLFacet.class);
				
				if ((extendsEntity instanceof TLFacet) &&
						(localNamespace != null) && localNamespace.equals(extendsEntityNamespace)) {
					builder.addFinding(FindingType.ERROR, "extendsEntity", ERROR_INVALID_LOCAL_FACET_EXTENSION);
				}
			}
			
			// If the extended entity publishes an extension point, the extension owner must publish
			// one as well.
			if (isExtendableEntity(extendsEntity) && !isExtendableEntity((NamedEntity) extensionOwner)) {
				builder.addFinding(FindingType.WARNING, "owner", WARNING_MUST_BE_EXTENSIBLE, extendsEntity.getLocalName());
			}
		}
		
		// Assert that the entity being extended is, in fact, marked as being extendable (XP-facets only)
		if ((extensionOwner instanceof TLExtensionPointFacet) && !isExtendableEntity(extendsEntity)) {
			builder.addFinding(FindingType.ERROR, "extendsEntity", ERROR_ILLEGAL_EXTENSION, target.getExtendsEntityName());
		}
		
		if ( CircularReferenceChecker.hasCircularExtension(target) ) {
			builder.addFinding(FindingType.ERROR, "extendsEntity", ERROR_INVALID_CIRCULAR_EXTENSION);
		}
		
		return builder.getFindings();
	}
	
	/**
	 * Returns true if the given entity has been marked as 'extendable' (or marked as not-notExtendable, as the
	 * case may be).
	 * 
	 * @param extendedEntity  the named entity to analyze
	 * @return boolean
	 */
	private boolean isExtendableEntity(NamedEntity extendedEntity) {
		boolean isExtendable = true;
		
		if (extendedEntity instanceof TLComplexTypeBase) {
			isExtendable = !((TLComplexTypeBase) extendedEntity).isNotExtendable();
			
		} else if (extendedEntity instanceof TLOperation) {
			isExtendable = !((TLOperation) extendedEntity).isNotExtendable();
			
		} else if (extendedEntity instanceof TLFacet) {
			TLFacet extendedFacet = (TLFacet) extendedEntity;
			
			if (extendedFacet.getFacetType().isContextual()) {
				isExtendable = !extendedFacet.isNotExtendable();
				
			} else {
				TLFacetOwner extendedFacetOwner = extendedFacet.getOwningEntity();
				
				if (extendedFacetOwner instanceof TLComplexTypeBase) {
					isExtendable = !((TLComplexTypeBase) extendedFacetOwner).isNotExtendable();
				}
			}
		}
		return isExtendable;
	}
	
}
