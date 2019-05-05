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

package org.opentravel.schemacompiler.validate.impl;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParamLocation;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.transform.util.ChameleonFilter;
import org.opentravel.schemacompiler.transform.util.LibraryPrefixResolver;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.Validatable;
import org.opentravel.schemacompiler.validate.ValidationBuilder;
import org.opentravel.schemacompiler.validate.ValidationContext;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.Validator;
import org.opentravel.schemacompiler.validate.ValidatorFactory;
import org.opentravel.schemacompiler.version.LibraryVersionComparator;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.PatchVersionHelper;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.Versioned;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * Base class for all validators used to inspect <code>TLModel</code> member elements.
 * 
 * @param <T> the type of the object to be validated
 * @author S. Livezey
 */
public abstract class TLValidatorBase<T extends Validatable> implements Validator<T> {

    public static final String TLMODEL_PREFIX = "org.opentravel.schemacompiler.";

    protected static final String NAME_FILE_PATTERN = "[A-Za-z][A-Za-z0-9/\\.\\-_]*";
    protected static final String NAME_XML_PATTERN = "(?:[A-Za-z_][A-Za-z0-9\\.\\-_:#]*)?";
    protected static final String PATCH_LEVEL_PATTERN = "[A-Za-z0-9\\.\\-_:#]*";

    private static final String PATH_TEMPLATE = "pathTemplate";

    public static final String ERROR_DUPLICATE_SCHEMA_TYPE_NAME = "DUPLICATE_SCHEMA_TYPE_NAME";
    public static final String ERROR_DUPLICATE_SCHEMA_ELEMENT_NAME = "DUPLICATE_SCHEMA_ELEMENT_NAME";
    public static final String ERROR_DUPLICATE_MAJOR_VERSION_SYMBOL = "DUPLICATE_MAJOR_VERSION_SYMBOL";
    public static final String ERROR_INVALID_VERSION_EXTENSION = "INVALID_VERSION_EXTENSION";
    public static final String ERROR_ILLEGAL_PATCH = "ILLEGAL_PATCH";
    public static final String ERROR_UNDECLARED_PATH_PARAM = "UNDECLARED_PATH_PARAM";
    public static final String ERROR_UNUSED_PATH_PARAM = "UNUSED_PATH_PARAM";
    public static final String ERROR_INVALID_PATH_TEMPLATE = "INVALID_PATH_TEMPLATE";
    public static final String WARNING_EXAMPLE_FOR_EMPTY_TYPE = "EXAMPLE_FOR_EMPTY_TYPE";
    public static final String WARNING_DEPRECATED_DATETIME = "DEPRECATED_DATETIME";

    private static final boolean ENFORCE_DATE_TIME_VALIDATION = false;

    private TLModelValidationContext context;
    private ValidatorFactory validatorFactory;

    /**
     * @see org.opentravel.schemacompiler.validate.Validator#setValidationContext(org.opentravel.schemacompiler.validate.ValidationContext)
     */
    @Override
    public void setValidationContext(ValidationContext context) {
        if (context instanceof TLModelValidationContext) {
            this.context = (TLModelValidationContext) context;

        } else if (context == null) {
            throw new NullPointerException( "The model validation context is requied." );

        } else {
            throw new IllegalArgumentException(
                "The validation context must be an instance of TLModelValidationContext." );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validator#getValidatorFactory()
     */
    @Override
    public ValidatorFactory getValidatorFactory() {
        return validatorFactory;
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validator#setValidatorFactory(org.opentravel.schemacompiler.validate.ValidatorFactory)
     */
    public void setValidatorFactory(ValidatorFactory factory) {
        this.validatorFactory = factory;
    }

    /**
     * @see org.opentravel.schemacompiler.validate.Validator#validate(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    public ValidationFindings validate(T target) {
        ValidationFindings fieldFindings = validateFields( target );
        ValidationFindings childrenFindings = validateChildren( target );
        ValidationFindings allFindings = null;

        if ((fieldFindings != null) || (childrenFindings != null)) {
            allFindings = new ValidationFindings();
            allFindings.addAll( fieldFindings );
            allFindings.addAll( childrenFindings );
        }
        return allFindings;
    }

    /**
     * Called by the {@link #validate(Validatable)} method to perform the validation of the target object's field
     * values. The default implementation of this method returns null.
     * 
     * @param target the target object to validate
     * @return ValidationFindings
     */
    protected ValidationFindings validateFields(T target) {
        return null;
    }

    /**
     * Called by the {@link #validate(Validatable)} method to perform the validation of the target object's child
     * components (if any). The default implementation of this method returns null.
     * 
     * @param target the target object to validate
     * @return ValidationFindings
     */
    protected ValidationFindings validateChildren(T target) {
        return null;
    }

    /**
     * Creates a new validation builder instance for the given target object.
     * 
     * @param targetObject the target object to be validated
     * @return TLValidationBuilder
     */
    protected TLValidationBuilder newValidationBuilder(T targetObject) {
        return new TLValidationBuilder( TLMODEL_PREFIX, context ).setTargetObject( targetObject );
    }

    /**
     * Assigns the context library to use for symbol lookups and name resolution.
     * 
     * @param library the context library to assign
     */
    protected void setContextLibrary(TLLibrary library) {
        if (context != null) {
            SymbolResolver symbolResolver = context.getSymbolResolver();

            if (symbolResolver != null) {
                symbolResolver.setPrefixResolver( (library == null) ? null : new LibraryPrefixResolver( library ) );
                symbolResolver.setAnonymousEntityFilter( (library == null) ? null : new ChameleonFilter( library ) );
            }
        }
    }

    /**
     * Returns an entry from the validation context cache, or null if an entry with the specified key has not been
     * defined.
     * 
     * @param cacheKey the key for the validation cache entry to return
     * @return Object
     */
    protected Object getContextCacheEntry(String cacheKey) {
        return (context == null) ? null : context.getContextCacheEntry( cacheKey );
    }

    /**
     * Returns an entry from the validation context cache, or null if an entry with the specified key has not been
     * defined.
     * 
     * @param cacheKey the key for the validation cache entry to return
     * @param entryType the type of the entry to be created if it does not yet exist
     * @param <E> the expected type of the context cache entry
     * @return E
     */
    protected <E> E getContextCacheEntry(String cacheKey, Class<E> entryType) {
        return (context == null) ? null : context.getContextCacheEntry( cacheKey, entryType );
    }

    /**
     * Assigns a key/value entry to the validation context cache.
     * 
     * @param cacheKey the key for the validation cache entry to assign
     * @param cacheValue the value to be associated with the specified key
     */
    protected void setContextCacheEntry(String cacheKey, Object cacheValue) {
        if (context != null) {
            context.setContextCacheEntry( cacheKey, cacheValue );
        }
    }

    /**
     * If the string's value is non-null, this method trims any leading/trailing white space and returns the resulting
     * value. If the resulting string is zero-length, this method will return null.
     * 
     * @param str the string value to trim
     * @return String
     */
    protected String trimString(String str) {
        return trimString( str, true );
    }

    /**
     * If the string's value is non-null, this method trims any leading/trailing white space and returns the resulting
     * value. If the resulting string is zero-length and the 'convertEmptyStringToNull' parameter is true, this method
     * will return null.
     * 
     * @param str the string value to trim
     * @param convertEmptyStringToNull indicates that an empty string should be converted to a null value
     * @return String
     */
    protected String trimString(String str, boolean convertEmptyStringToNull) {
        String result = (str == null) ? "" : str.trim();

        if (convertEmptyStringToNull && (result.length() == 0)) {
            result = null;
        }
        return result;
    }

    /**
     * If the given owner provides EXAMPLE values, this method will issue a warning if the assigned type is 'ota:Empty'.
     * 
     * @param exampleOwner the EXAMPLE owner to analyze
     * @param assignedType the assigned type of the EXAMPLE owner
     * @param propertyName the name of the property for which a warning should be issues
     * @param builder the validation builder that will receive the warning (if one is issued)
     */
    protected void checkEmptyValueType(TLExampleOwner exampleOwner, NamedEntity assignedType, String propertyName,
        ValidationBuilder<?> builder) {
        if ((exampleOwner != null) && !exampleOwner.getExamples().isEmpty()
            && ValidatorUtils.isEmptyValueType( assignedType )) {
            builder.addFinding( FindingType.WARNING, propertyName, WARNING_EXAMPLE_FOR_EMPTY_TYPE );
        }
    }

    /**
     * Returns true if the given entity is a version extension of another entity in the same base namespace.
     * 
     * @param versionedEntity the versioned entity to analyze
     * @return boolean
     */
    protected boolean isVersionExtension(Versioned versionedEntity) {
        return (getExtendedVersion( versionedEntity ) != null);
    }

    /**
     * Returns the prior version that is extended by the given entity, or null if the given entity is not a version
     * extension.
     * 
     * <p>
     * NOTE: This method is designed to be used for validation purposes. It is less strict than the method provided by
     * the <code>MinorVersionHelper</code> because it does not require the resulting object to be a member of the same
     * major version chain. It only requires that both objects reside within the same base namespace.
     * 
     * @param versionedEntity the versioned entity for which to return the prior version
     * @param <V> the type of the versioned entity
     * @return V
     */
    protected <V extends Versioned> V getExtendedVersion(V versionedEntity) {
        V candidateVersion = lookupExtendedVersion( versionedEntity );
        V extendedVersion = null;

        // Determine whether the candidate is a version or non-version extension
        if ((versionedEntity != null) && (candidateVersion != null)
            && versionedEntity.getBaseNamespace().equals( candidateVersion.getBaseNamespace() )) {
            String versionedEntityName =
                (versionedEntity instanceof TLOperation) ? ((TLOperation) versionedEntity).getName()
                    : versionedEntity.getLocalName();
            String candidateName =
                (candidateVersion instanceof TLOperation) ? ((TLOperation) candidateVersion).getName()
                    : candidateVersion.getLocalName();

            if (versionedEntityName.equals( candidateName )) {
                extendedVersion = candidateVersion;
            }
        }
        return extendedVersion;
    }

    /**
     * Find the entity that is extended by the one passed to this method.
     * 
     * @param versionedEntity the versioned entity for which to return the extension
     * @return V
     */
    @SuppressWarnings("unchecked")
    private <V extends Versioned> V lookupExtendedVersion(V versionedEntity) {
        V candidateVersion = null;

        if (versionedEntity instanceof TLExtensionOwner) {
            TLExtension extension = ((TLExtensionOwner) versionedEntity).getExtension();
            NamedEntity extendedEntity = (extension == null) ? null : extension.getExtendsEntity();

            if ((extendedEntity != null) && versionedEntity.getClass().equals( extendedEntity.getClass() )) {
                candidateVersion = (V) extendedEntity;
            }
        } else if (versionedEntity instanceof TLValueWithAttributes) {
            TLValueWithAttributes versionedVWA = (TLValueWithAttributes) versionedEntity;

            if (versionedVWA.getParentType() instanceof TLValueWithAttributes) {
                candidateVersion = (V) versionedVWA.getParentType();
            }
        } else if (versionedEntity instanceof TLSimple) {
            TLAttributeType parentType = ((TLSimple) versionedEntity).getParentType();

            if (parentType instanceof Versioned) {
                candidateVersion = (V) parentType;
            }
        }
        return candidateVersion;
    }

    /**
     * If the given entity is a minor version extension of another entity in the same base namespace, this method will
     * return true if the extended entity is in a later version than the one passed to this method.
     * 
     * @param versionedEntity the versioned entity to analyze
     * @return boolean
     */
    protected boolean isInvalidVersionExtension(Versioned versionedEntity) {
        Versioned extendedVersion = getExtendedVersion( versionedEntity );
        boolean result = false;

        if ((extendedVersion != null) && (versionedEntity.getOwningLibrary() instanceof TLLibrary)
            && (extendedVersion.getOwningLibrary() instanceof TLLibrary)) {
            try {
                TLLibrary localLibrary = (TLLibrary) versionedEntity.getOwningLibrary();
                TLLibrary extendedLibrary = (TLLibrary) extendedVersion.getOwningLibrary();
                VersionScheme vScheme = new MinorVersionHelper().getVersionScheme( localLibrary );

                if (vScheme != null) {
                    Comparator<TLLibrary> comparator = new LibraryVersionComparator( vScheme.getComparator( true ) );
                    result = (comparator.compare( extendedLibrary, localLibrary ) >= 0);
                }
            } catch (VersionSchemeException e) {
                // Ignore - Invalid version scheme error will be reported when the owning library is
                // validated
            }
        }
        return result;
    }

    /**
     * Checks to determine if one or more other entities entities in the model will be assigned the same name and
     * namespace in the resulting schema(s) that are generated by the compiler. If a duplicate XSD type and/or global
     * element name is detected, a validation error will be added to the builder provided.
     * 
     * @param entity the named entity to analyze
     * @param builder the validation builder that will receive any validation errors that are detected
     */
    protected void checkSchemaNamingConflicts(NamedEntity entity, ValidationBuilder<?> builder) {
        // Check for trivial negative cases...
        if ((entity == null) || (entity.getOwningModel() == null)) {
            return;
        }
        SchemaNameValidationRegistry registry = getSchemaNameRegistry( entity.getOwningModel() );
        QName conflictingElement;

        if (registry.hasTypeNameConflicts( entity )) {
            QName typeName = registry.getSchemaTypeName( entity );

            builder.addFinding( FindingType.ERROR, "name", ERROR_DUPLICATE_SCHEMA_TYPE_NAME, typeName.getLocalPart(),
                typeName.getNamespaceURI() );

        } else if ((conflictingElement = registry.getElementNameConflicts( entity )) != null) {
            builder.addFinding( FindingType.ERROR, "name", ERROR_DUPLICATE_SCHEMA_ELEMENT_NAME,
                conflictingElement.getLocalPart(), conflictingElement.getNamespaceURI() );
        }
    }

    /**
     * Returns the cached <code>SchemaNameValidationRegistry</code> instance. If a cached object does not exist, one is
     * created automatically.
     * 
     * @param model the model to use when populating the contents of the registry
     * @return SchemaNameValidationRegistry
     */
    protected SchemaNameValidationRegistry getSchemaNameRegistry(TLModel model) {
        SchemaNameValidationRegistry schemaNameRegistry =
            (SchemaNameValidationRegistry) getContextCacheEntry( SchemaNameValidationRegistry.class.getName() );

        if (schemaNameRegistry == null) {
            schemaNameRegistry = new SchemaNameValidationRegistry( model );
            setContextCacheEntry( SchemaNameValidationRegistry.class.getName(), schemaNameRegistry );
        }
        return schemaNameRegistry;
    }

    /**
     * Recursively validates all local facets in the given list.
     * 
     * @param facetList the list of contextual facets to validate
     * @param validator the validator instance to use
     * @param findings the list of validation findings being compiled
     */
    protected void validateLocalContextualFacets(List<TLContextualFacet> facetList,
        Validator<TLContextualFacet> validator, ValidationFindings findings) {
        for (TLContextualFacet facet : facetList) {
            if (facet.isLocalFacet()) {
                findings.addAll( validator.validate( facet ) );
                validateLocalContextualFacets( facet.getChildFacets(), validator, findings );
            }
        }
    }

    /**
     * Adds a warning to the builder if the given field type is considered to be a deprecated XSD <code>date</code>,
     * <code>time</code>, or <code>dateTime</code> type.
     * 
     * @param fieldType the attribute/element type to validate
     * @param fieldOwner the library within which the field is declared
     * @param builder the validation builder that will receive any validation warnings that are detected
     */
    protected void validateDeprecatedDateTimeUsage(NamedEntity fieldType, AbstractLibrary fieldOwner,
        ValidationBuilder<?> builder) {
        boolean isDraftLibrary = false;

        if (fieldOwner instanceof TLLibrary) {
            isDraftLibrary = (((TLLibrary) fieldOwner).getStatus() == TLLibraryStatus.DRAFT);
        }

        if (enforceDateTimeValidation() && isDraftLibrary && (fieldType != null)
            && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals( fieldType.getNamespace() )) {
            String otaType = null;

            if (fieldType.getLocalName().equals( "dateTime" )) {
                otaType = "UTCDateTime";

            } else if (fieldType.getLocalName().equals( "date" )) {
                otaType = "UTCDate";

            } else if (fieldType.getLocalName().equals( "time" )) {
                otaType = "UTCTime";
            }

            if (otaType != null) {
                builder.addFinding( FindingType.WARNING, "type", WARNING_DEPRECATED_DATETIME,
                    "xsd:" + fieldType.getLocalName(), "ota:" + otaType );
            }
        }
    }

    /**
     * Returns true if date/time validation should be enforced.
     * 
     * @return boolean
     */
    private boolean enforceDateTimeValidation() {
        return ENFORCE_DATE_TIME_VALIDATION;
    }

    /**
     * Performs validation checks required for all versioned objects.
     * 
     * @param target the versioned entity to check
     * @param builder the validation builder that will receive any validation errors that are detected
     */
    protected void validateVersioningRules(Versioned target, ValidationBuilder<?> builder) {
        // Illegal patch violations for operations will be caught when the service is validated
        if (!(target instanceof TLOperation)) {
            try {
                PatchVersionHelper helper = new PatchVersionHelper();
                VersionScheme vScheme = helper.getVersionScheme( target );

                if ((vScheme != null) && vScheme.isPatchVersion( target.getNamespace() )) {
                    builder.addFinding( FindingType.ERROR, "name", ERROR_ILLEGAL_PATCH );
                }

            } catch (VersionSchemeException e) {
                // Ignore - Invalid version scheme error will be reported when the owning library is
                // validated
            }
        }

        if (isInvalidVersionExtension( target )) {
            builder.addFinding( FindingType.ERROR, "versionExtension", ERROR_INVALID_VERSION_EXTENSION );
        }
        checkMajorVersionNamingConflicts( target, builder );
    }

    /**
     * Checks to determine if another entity is assigned to the given entity's major-version namespace that is not a
     * previous or later version of the owning library. The only duplicate names that are allowed in a major-version
     * namespace are for minor versions of <code>Versioned</code> objects.
     * 
     * @param entity the named entity to analyze
     * @param builder the validation builder that will receive any validation errors that are detected
     */
    private void checkMajorVersionNamingConflicts(NamedEntity entity, ValidationBuilder<?> builder) {
        // Check for trivial negative cases...
        if ((entity == null) || (entity.getOwningModel() == null) || !(entity.getOwningLibrary() instanceof TLLibrary)
            || (((TLLibrary) entity.getOwningLibrary()).getBaseNamespace() == null)) {
            return;
        }

        String localName = (entity instanceof TLOperation) ? ((TLOperation) entity).getName() : entity.getLocalName();
        List<NamedEntity> matchingEntities = findMatchingEntities( entity, localName );

        // Only check for validation errors if we find duplicates of the entity's name
        if (matchingEntities.size() <= 1) {
            return;
        }

        Collection<Versioned> versionFamily =
            (entity instanceof Versioned) ? getMajorVersionFamily( (Versioned) entity ) : new ArrayList<>();

        for (NamedEntity matchingEntity : matchingEntities) {
            if (matchingEntity != entity) {
                boolean hasConflict = isConflictingEntity( entity, matchingEntity, versionFamily );

                if (hasConflict) {
                    // NOTE: This validation check only reports errors for duplicate names in
                    // DIFFERENT namespaces of a major-version chain. Duplicate symbols that occur
                    // in the same namespace are reported by different validation checks.
                    if (!matchingEntity.getNamespace().equals( entity.getNamespace() )) {
                        builder.addFinding( FindingType.ERROR, "name", ERROR_DUPLICATE_MAJOR_VERSION_SYMBOL,
                            localName );
                    }
                    break;
                }
            }
        }
    }

    /**
     * Returns true if the given test entity conflicts with the original onee.
     * 
     * @param entity the entity to be tested for a match
     * @param testEntity the entity to test for a match
     * @param versionFamily the version family for the entity being tested
     * @return boolean
     */
    private boolean isConflictingEntity(NamedEntity entity, NamedEntity testEntity,
        Collection<Versioned> versionFamily) {
        boolean isConflict = false;

        if ((entity instanceof Versioned) && (testEntity instanceof Versioned)
            && entity.getClass().equals( testEntity.getClass() )) {
            // If both entities are versioned, we do NOT have a naming conflict if they are
            // members of the same family
            if (!versionFamily.contains( testEntity )) {
                isConflict = true;
            }
        } else {
            // If one or both of the entities is not versioned, we have a naming conflict
            isConflict = true;
        }
        return isConflict;
    }

    /**
     * Build a list of all entities from the base namespace whose local names match that of the given entity.
     * 
     * @param entity the entity for which to find matches
     * @param localName the local name of the entity to match
     * @return List&lt;NamedEntity&gt;
     */
    private List<NamedEntity> findMatchingEntities(NamedEntity entity, String localName) {
        String majorVersionNamespace = getMajorVersionNamespace( (TLLibrary) entity.getOwningLibrary() );
        List<NamedEntity> matchingEntities = new ArrayList<>();

        for (TLLibrary library : entity.getOwningModel().getUserDefinedLibraries()) {
            if (!majorVersionNamespace.equals( getMajorVersionNamespace( library ) )) {
                continue;
            }
            NamedEntity matchingEntity = null;

            if (entity instanceof TLOperation) {
                // Operation names are not in the general schema namespace assignments, so they need
                // to be analyzed separately.
                if (library.getService() != null) {
                    matchingEntity = library.getService().getOperation( localName );
                }
            } else {
                matchingEntity = library.getNamedMember( localName );
            }
            if (matchingEntity != null) {
                matchingEntities.add( matchingEntity );
            }
        }
        return matchingEntities;
    }

    /**
     * Returns the collection of all later and earlier minor versions of the given entity. At a minimum, the resulting
     * collection will contain the original entity. To improve performance, the minor version families are cached in the
     * validation context.
     * 
     * @param versionedEntity the versioned entity for which to return the minor version family
     * @return Collection&lt;V&gt;
     */
    @SuppressWarnings("unchecked")
    private <V extends Versioned> Collection<V> getMajorVersionFamily(V versionedEntity) {
        Map<Versioned,Collection<V>> minorVersionFamilyMappings =
            (Map<Versioned,Collection<V>>) getContextCacheEntry( "majorVersionFamilyMappings" );

        if (minorVersionFamilyMappings == null) {
            minorVersionFamilyMappings = new HashMap<>();
            setContextCacheEntry( "majorVersionFamilyMappings", minorVersionFamilyMappings );
        }
        Collection<V> minorVersionFamily = minorVersionFamilyMappings.get( versionedEntity );

        if (minorVersionFamily == null) {
            try {
                minorVersionFamily = new MinorVersionHelper().getMajorVersionFamily( versionedEntity );

            } catch (VersionSchemeException e) {
                // Ignore; the invalid version scheme will be reported elsewhere
                minorVersionFamily = new ArrayList<>();
            }

            for (Versioned familyMember : minorVersionFamily) {
                minorVersionFamilyMappings.put( familyMember, minorVersionFamily );
            }
        }
        return minorVersionFamily;
    }

    /**
     * Returns the major-version namespace of the given library.
     * 
     * @param library the library for which to return the major-version namespace
     * @return String
     */
    @SuppressWarnings("unchecked")
    private String getMajorVersionNamespace(TLLibrary library) {
        Map<String,String> majorVersionNamespaceMappings =
            (Map<String,String>) getContextCacheEntry( "majorVersionNamespaceMappings", HashMap.class );
        String libraryNamespace = library.getNamespace();

        return majorVersionNamespaceMappings.computeIfAbsent( libraryNamespace, ns -> getMajorVersionNS( library ) );
    }

    /**
     * Returns the major version namespace string for the given library.
     * 
     * @param library the library for which to compute the namespace string
     * @return String
     */
    private String getMajorVersionNS(TLLibrary library) {
        String majorVersionNS;

        try {
            majorVersionNS = new MinorVersionHelper().getMajorVersionNamespace( library );

        } catch (VersionSchemeException e) {
            // Use default naming in case of a URI that does not match the default version
            // scheme
            majorVersionNS = library.getNamespace();
        }
        return majorVersionNS;
    }

    /**
     * Validates all aspects of the given path template, including path parameters that must be declared in the given
     * parameter group.
     * 
     * @param pathTemplate the path template to be validated
     * @param paramGroup the parameter group that declares the path parameters of the template
     * @param builder the validation builder that will receive any validation errors that are detected
     */
    protected void validatePathTemplate(String pathTemplate, TLParamGroup paramGroup, ValidationBuilder<?> builder) {
        if ((pathTemplate == null) || (pathTemplate.length() == 0)) {
            return; // Do not validate if required information is not provided
        }
        ResourceUrlValidator urlValidator = new ResourceUrlValidator( true );

        if (pathTemplate.length() > 0) {
            if (urlValidator.isValidPath( pathTemplate )) {
                List<String> missingParams = getMissingPathParams( pathTemplate, paramGroup, urlValidator );
                List<String> unusedParams = getUnusedPathParams( paramGroup, pathTemplate, urlValidator );

                if ((paramGroup != null) && !missingParams.isEmpty()) {
                    builder.addFinding( FindingType.ERROR, PATH_TEMPLATE, ERROR_UNDECLARED_PATH_PARAM,
                        toCsvString( missingParams ) );
                }
                if (!unusedParams.isEmpty()) {
                    builder.addFinding( FindingType.ERROR, PATH_TEMPLATE, ERROR_UNUSED_PATH_PARAM,
                        toCsvString( unusedParams ) );
                }

            } else {
                builder.addFinding( FindingType.ERROR, PATH_TEMPLATE, ERROR_INVALID_PATH_TEMPLATE, pathTemplate );
            }
        }
    }

    /**
     * Returns the list of parameters from the given path template that do not have a corresponding declaration in the
     * parameter group provided.
     * 
     * @param pathTemplate the path template to analyze
     * @param paramGroup the parameter group which must contain all referenced path parameters
     * @param urlValidator the URL validator to use when identifying path parameters
     * @return List&lt;String&gt;
     */
    private List<String> getMissingPathParams(String pathTemplate, TLParamGroup paramGroup,
        ResourceUrlValidator urlValidator) {
        List<String> pgPathParams = getPathParameterNames( paramGroup );
        List<String> missingParams = new ArrayList<>();

        // Determine which (if any) parameters from the path template do not exist
        for (String templateParam : urlValidator.getPathParameters( pathTemplate )) {
            if (!pgPathParams.contains( templateParam )) {
                missingParams.add( templateParam );
            }
        }
        return missingParams;
    }

    /**
     * Returns the list of parameters from the given parameter group that are not used in the path template provided.
     * 
     * @param paramGroup the parameter group which declares all required path parameters
     * @param pathTemplate the path template that must reference all delcared path parameters
     * @param urlValidator the URL validator to use when identifying path parameters
     * @return List&lt;String&gt;
     */
    private List<String> getUnusedPathParams(TLParamGroup paramGroup, String pathTemplate,
        ResourceUrlValidator urlValidator) {
        List<String> templatePathParams = urlValidator.getPathParameters( pathTemplate );
        List<String> unusedParams = new ArrayList<>();

        // Determine which (if any) parameters from the parameter group are not used
        for (String pgParam : getPathParameterNames( paramGroup )) {
            if (!templatePathParams.contains( pgParam )) {
                unusedParams.add( pgParam );
            }
        }
        return unusedParams;
    }

    /**
     * Returns the list of all path parameter names that are declared or inherited by the given parameter group.
     * 
     * @param paramGroup the parameter group for which to return path parameter names
     * @return List&lt;String&gt;
     */
    @SuppressWarnings("unchecked")
    private List<String> getPathParameterNames(TLParamGroup paramGroup) {
        if ((paramGroup == null) || (paramGroup.getOwner() == null)) {
            return new ArrayList<>(); // return empty list for null param group
        }
        String cacheKey = paramGroup.getOwner().getNamespace() + ":" + paramGroup.getOwner().getLocalName() + ":"
            + paramGroup.getName() + ":pathParams";
        List<String> pathParams = (List<String>) getContextCacheEntry( cacheKey );

        if (pathParams == null) {
            pathParams = new ArrayList<>();

            validatePathParams( pathParams, paramGroup );
            setContextCacheEntry( cacheKey, pathParams );
        }
        return pathParams;
    }

    /**
     * Validates the path parameters of the given <code>TLParamGroup</code>.
     * 
     * @param pathParams the list of path parameters to validate
     * @param paramGroup the parameter group that owns the path parameters
     */
    private void validatePathParams(List<String> pathParams, TLParamGroup paramGroup) {
        for (TLParameter param : ResourceCodegenUtils.getInheritedParameters( paramGroup )) {
            if (param.getLocation() == TLParamLocation.PATH) {
                String paramName;

                if (param.getFieldRef() != null) {
                    paramName = param.getFieldRef().getName();
                } else {
                    paramName = param.getFieldRefName();
                }
                if (paramName != null) {
                    pathParams.add( paramName );
                }
            }
        }
    }

    /**
     * Returns a test path that substitutes a '0' for every parameter in the template.
     * 
     * @param pathTemplate the path template from which to derive the test path string
     * @return String
     */
    protected String buildTestPath(String pathTemplate) {
        ResourceUrlValidator urlValidator = new ResourceUrlValidator( true );
        String testPath = pathTemplate;

        if (testPath != null) {
            for (String pathParam : urlValidator.getPathParameters( pathTemplate )) {
                testPath = testPath.replaceAll( "\\{" + pathParam + "\\}", "0" );
            }
        }
        return testPath;
    }

    /**
     * Returns a string containing comma-separated values from the given list.
     * 
     * @param values the list of values to concatenate
     * @return String
     */
    protected String toCsvString(List<?> values) {
        StringBuilder csv = new StringBuilder();
        boolean firstValue = true;

        for (Object obj : values) {
            if (obj != null) {
                if (!firstValue) {
                    csv.append( ", " );
                }
                csv.append( obj.toString() );
                firstValue = false;
            }
        }
        return csv.toString();
    }

    /**
     * Returns true if the given action facet is either declared or inherited by the given resource.
     * 
     * @param resource the resource in which the facet should be declared or inherited
     * @param actionFacet the action facet to check for inheritance
     * @return boolean
     */
    protected boolean isDeclaredOrInheritedFacet(TLResource resource, TLActionFacet actionFacet) {
        List<TLActionFacet> inheritedFacets = ResourceCodegenUtils.getInheritedActionFacets( resource );
        boolean result = false;

        if (inheritedFacets.contains( actionFacet )) {
            result = true;

        } else if (actionFacet.getName() != null) {
            String facetName = actionFacet.getName();

            for (TLActionFacet ghostFacet : FacetCodegenUtils.findGhostFacets( resource )) {
                if (facetName.equals( ghostFacet.getName() )) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Validates that all of the facets in the given list are assigned to an owning library. Normally, this check would
     * be done in the <code>TLContextualFacet</code> validator, but that is not possible since contextual facets are
     * normally reached from the owning library (i.e. if the owning-library of a facet is not initialized, the facet
     * will likely not be validated at all).
     * 
     * @param facetList the list of contextual facets to validate
     * @return ValidationFindings
     */
    protected ValidationFindings validateContextualFacetLibraryOwnership(List<TLContextualFacet> facetList) {
        ValidationFindings findings = new ValidationFindings();

        if (facetList != null) {
            for (TLContextualFacet facet : facetList) {
                validateContextualFacetLibraryOwnership( facet, findings, new HashSet<TLContextualFacet>() );
            }
        }
        return findings;
    }

    /**
     * Recursive method that validates library ownership for the given contextual facet and all of its children.
     * 
     * @param facet the contextual facet to validate
     * @param findings the validation findings where any errors should be reported
     * @param visitedFacets collection of visited facets (used to avoid circular reference loops)
     */
    private void validateContextualFacetLibraryOwnership(TLContextualFacet facet, ValidationFindings findings,
        Set<TLContextualFacet> visitedFacets) {
        if (!visitedFacets.contains( facet )) {
            visitedFacets.add( facet );

            if (facet.getOwningLibrary() == null) {
                findings.addFinding( FindingType.ERROR, facet,
                    "org.opentravel.schemacompiler.TLContextualFacet.owningLibrary."
                        + ValidationBuilder.ERROR_NULL_VALUE );
            }
            for (TLContextualFacet childFacet : facet.getChildFacets()) {
                validateContextualFacetLibraryOwnership( childFacet, findings, visitedFacets );
            }
        }
    }

}
