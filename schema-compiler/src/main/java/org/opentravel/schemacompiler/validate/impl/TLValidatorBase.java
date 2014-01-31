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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOperation;
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
import org.opentravel.schemacompiler.version.MajorVersionHelper;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.PatchVersionHelper;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.version.Versioned;

import com.sun.xml.txw2.IllegalSignatureException;

/**
 * Base class for all validators used to inspect <code>TLModel</code> member elements.
 * 
 * @param <T>
 *            the type of the object to be validated
 * @author S. Livezey
 */
public abstract class TLValidatorBase<T extends Validatable> implements Validator<T> {

    public static final String TLMODEL_PREFIX = "org.opentravel.schemacompiler.";

    protected static final String NAME_FILE_PATTERN = "[A-Za-z][A-Za-z0-9/\\.\\-_]*";
    protected static final String NAME_XML_PATTERN = "(?:[A-Za-z_][A-Za-z0-9\\.\\-_:#]*)?";
    protected static final String PATCH_LEVEL_PATTERN = "[A-Za-z0-9\\.\\-_:#]*";

    public static final String ERROR_DUPLICATE_SCHEMA_TYPE_NAME = "DUPLICATE_SCHEMA_TYPE_NAME";
    public static final String ERROR_DUPLICATE_SCHEMA_ELEMENT_NAME = "DUPLICATE_SCHEMA_ELEMENT_NAME";
    public static final String ERROR_DUPLICATE_MAJOR_VERSION_SYMBOL = "DUPLICATE_MAJOR_VERSION_SYMBOL";
    public static final String WARNING_EXAMPLE_FOR_EMPTY_TYPE = "EXAMPLE_FOR_EMPTY_TYPE";

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
            throw new NullPointerException("The model validation context is requied.");

        } else {
            throw new IllegalArgumentException(
                    "The validation context must be an instance of TLModelValidationContext.");
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
        ValidationFindings fieldFindings = validateFields(target);
        ValidationFindings childrenFindings = validateChildren(target);
        ValidationFindings allFindings = null;

        if ((fieldFindings != null) || (childrenFindings != null)) {
            allFindings = new ValidationFindings();
            allFindings.addAll(fieldFindings);
            allFindings.addAll(childrenFindings);
        }
        return allFindings;
    }

    /**
     * Called by the {@link #validate(Validatable)} method to perform the validation of the target
     * object's field values. The default implementation of this method returns null.
     * 
     * @param target
     *            the target object to validate
     * @return ValidationFindings
     */
    protected ValidationFindings validateFields(T target) {
        return null;
    }

    /**
     * Called by the {@link #validate(Validatable)} method to perform the validation of the target
     * object's child components (if any). The default implementation of this method returns null.
     * 
     * @param target
     *            the target object to validate
     * @return ValidationFindings
     */
    protected ValidationFindings validateChildren(T target) {
        return null;
    }

    /**
     * Creates a new validation builder instance for the given target object.
     * 
     * @param targetObject
     *            the target object to be validated
     * @return TLValidationBuilder
     */
    protected TLValidationBuilder newValidationBuilder(T targetObject) {
        return new TLValidationBuilder(TLMODEL_PREFIX, context).setTargetObject(targetObject);
    }

    /**
     * Assigns the context library to use for symbol lookups and name resolution.
     * 
     * @param library
     *            the context library to assign
     */
    protected void setContextLibrary(TLLibrary library) {
        if (context != null) {
            SymbolResolver symbolResolver = context.getSymbolResolver();

            if (symbolResolver != null) {
                symbolResolver.setPrefixResolver((library == null) ? null
                        : new LibraryPrefixResolver(library));
                symbolResolver.setAnonymousEntityFilter((library == null) ? null
                        : new ChameleonFilter(library));
            }
        }
    }

    /**
     * Returns an entry from the validation context cache, or null if an entry with the specified
     * key has not been defined.
     * 
     * @param cacheKey
     *            the key for the validation cache entry to return
     * @return Object
     */
    protected Object getContextCacheEntry(String cacheKey) {
        return (context == null) ? null : context.getContextCacheEntry(cacheKey);
    }

    /**
     * Assigns a key/value entry to the validation context cache.
     * 
     * @param cacheKey
     *            the key for the validation cache entry to assign
     * @param cacheValue
     *            the value to be associated with the specified key
     * @return Object
     */
    protected void setContextCacheEntry(String cacheKey, Object cacheValue) {
        if (context != null) {
            context.setContextCacheEntry(cacheKey, cacheValue);
        }
    }

    /**
     * If the string's value is non-null, this method trims any leading/trailing white space and
     * returns the resulting value. If the resulting string is zero-length, this method will return
     * null.
     * 
     * @param str
     *            the string value to trim
     * @return String
     */
    protected String trimString(String str) {
        return trimString(str, true);
    }

    /**
     * If the string's value is non-null, this method trims any leading/trailing white space and
     * returns the resulting value. If the resulting string is zero-length and the
     * 'convertEmptyStringToNull' parameter is true, this method will return null.
     * 
     * @param str
     *            the string value to trim
     * @param convertEmptyStringToNull
     *            indicates that an empty string should be converted to a null value
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
     * If the given owner provides example values, this method will issue a warning if the assigned
     * type is 'ota:Empty'.
     * 
     * @param exampleOwner
     *            the example owner to analyze
     * @param assignedType
     *            the assigned type of the example owner
     * @param propertyName
     *            the name of the property for which a warning should be issues
     * @param builder
     *            the validation builder that will receive the warning (if one is issued)
     */
    protected void checkEmptyValueType(TLExampleOwner exampleOwner, NamedEntity assignedType,
            String propertyName, ValidationBuilder<?> builder) {
        if ((exampleOwner != null) && (exampleOwner.getExamples().size() > 0)) {
            SchemaDependency emptyElement = SchemaDependency.getEmptyElement();
            boolean isEmptyType = (assignedType == null)
                    || (emptyElement.getSchemaDeclaration().getNamespace()
                            .equals(assignedType.getNamespace()) && emptyElement.getLocalName()
                            .equals(assignedType.getLocalName()));

            if (isEmptyType) {
                builder.addFinding(FindingType.WARNING, propertyName,
                        WARNING_EXAMPLE_FOR_EMPTY_TYPE);
            }
        }
    }

    /**
     * Returns true if the given entity is a version extension of another entity in the same base
     * namespace.
     * 
     * @param versionedEntity
     *            the versioned entity to analyze
     * @return boolean
     */
    protected boolean isVersionExtension(Versioned versionedEntity) {
        return (getExtendedVersion(versionedEntity) != null);
    }

    /**
     * Returns the prior version that is extended by the given entity, or null if the given entity
     * is not a version extension.
     * 
     * <p>
     * NOTE: This method is designed to be used for validation purposes. It is less strict than the
     * method provided by the <code>MinorVersionHelper</code> because it does not require the
     * resulting object to be a member of the same major version chain. It only requires that both
     * objects reside within the same base namespace.
     * 
     * @param versionedEntity
     *            the versioned entity for which to return the prior version
     * @return V
     */
    @SuppressWarnings("unchecked")
    protected <V extends Versioned> V getExtendedVersion(V versionedEntity) {
        V candidateVersion = null;
        V extendedVersion = null;

        // Find the entity that is extended by the one passed to this method
        if (versionedEntity instanceof TLExtensionOwner) {
            TLExtension extension = ((TLExtensionOwner) versionedEntity).getExtension();
            NamedEntity extendedEntity = (extension == null) ? null : extension.getExtendsEntity();

            if ((extendedEntity != null)
                    && versionedEntity.getClass().equals(extendedEntity.getClass())) {
                candidateVersion = (V) extendedEntity;
            }
        } else if (versionedEntity instanceof TLValueWithAttributes) {
            TLValueWithAttributes versionedVWA = (TLValueWithAttributes) versionedEntity;

            if (versionedVWA.getParentType() instanceof TLValueWithAttributes) {
                candidateVersion = (V) versionedVWA.getParentType();
            }
        } else if (versionedEntity instanceof TLSimple) {
            candidateVersion = (V) findSimpleExtension((TLSimple) versionedEntity);
        } else if (versionedEntity instanceof TLClosedEnumeration) {
            candidateVersion = (V) findClosedEnumExtension((TLClosedEnumeration) versionedEntity);
        }

        // Determine whether the candidate is a version or non-version extension
        if (candidateVersion != null) {
            if (versionedEntity.getBaseNamespace().equals(candidateVersion.getBaseNamespace())) {
                String versionedEntityName = (versionedEntity instanceof TLOperation) ? ((TLOperation) versionedEntity)
                        .getName() : versionedEntity.getLocalName();
                String candidateName = (candidateVersion instanceof TLOperation) ? ((TLOperation) candidateVersion)
                        .getName() : candidateVersion.getLocalName();

                if (versionedEntityName.equals(candidateName)) {
                    extendedVersion = candidateVersion;
                }
            }
        }
        return extendedVersion;
    }

    private TLClosedEnumeration findClosedEnumExtension(TLClosedEnumeration closedEnum) {
        try {
            TLLibrary minorPreceder = new MinorVersionHelper()
                    .getPriorMinorVersion((TLLibrary) closedEnum.getOwningLibrary());
            if (minorPreceder != null) {
                List<TLLibrary> patches = new PatchVersionHelper()
                        .getLaterPatchVersions(minorPreceder);
                for (TLLibrary lib : patches) {
                    if (lib == closedEnum.getOwningLibrary()) {
                        continue;
                    }
                    LibraryMember candidate = lib.getNamedMember(closedEnum.getLocalName());
                    if (candidate instanceof TLClosedEnumeration) {
                        return (TLClosedEnumeration) candidate;
                    }
                }
            }
        } catch (VersionSchemeException e) {
            throw new IllegalSignatureException(
                    "Cannot find extensions. Problem with version scheme", e);
        }
        return null;
    }

    /**
     * @param simple
     * @return previous version of simple object created on minor roll-up.
     * @throws IllegalStateException
     *             for missing version schema.
     */
    private TLSimple findSimpleExtension(TLSimple simple) {
        try {
            TLLibrary minorPreceder = new MinorVersionHelper()
                    .getPriorMinorVersion((TLLibrary) simple.getOwningLibrary());
            if (minorPreceder != null) {
                List<TLLibrary> patches = new PatchVersionHelper()
                        .getLaterPatchVersions(minorPreceder);
                for (TLLibrary lib : patches) {
                    if (lib == simple.getOwningLibrary()) {
                        continue;
                    }
                    LibraryMember candidate = lib.getNamedMember(simple.getLocalName());
                    if (candidate instanceof TLSimple) {
                        return (TLSimple) candidate;
                    }
                }
            }
        } catch (VersionSchemeException e) {
            throw new IllegalSignatureException(
                    "Cannot find extensions. Problem with version scheme", e);
        }
        return null;
    }

    /**
     * Returns a collection of all version extensions for the given entity.
     * 
     * @param versionedEntity
     *            the versioned entity for which to return version extensions
     * @return Collection<V>
     */
    private <V extends Versioned> Collection<V> getAllExtendedVersions(V versionedEntity) {
        V extendedVersion = getExtendedVersion(versionedEntity);
        List<V> extendedVersions = new ArrayList<V>();

        while (extendedVersion != null) {
            extendedVersions.add(extendedVersion);
            extendedVersion = getExtendedVersion(extendedVersion);
        }
        return extendedVersions;
    }

    /**
     * If the given entity is a minor version extension of another entity in the same base
     * namespace, this method will return true if the extended entity is in a later version than the
     * one passed to this method.
     * 
     * @param versionedEntity
     *            the versioned entity to analyze
     * @return boolean
     */
    protected boolean isInvalidVersionExtension(Versioned versionedEntity) {
        Versioned extendedVersion = getExtendedVersion(versionedEntity);
        boolean result = false;

        if ((extendedVersion != null) && (versionedEntity.getOwningLibrary() instanceof TLLibrary)
                && (extendedVersion.getOwningLibrary() instanceof TLLibrary)) {
            try {
                TLLibrary localLibrary = (TLLibrary) versionedEntity.getOwningLibrary();
                TLLibrary extendedLibrary = (TLLibrary) extendedVersion.getOwningLibrary();
                VersionScheme vScheme = new MinorVersionHelper().getVersionScheme(localLibrary);

                if (vScheme != null) {
                    Comparator<TLLibrary> comparator = new LibraryVersionComparator(
                            vScheme.getComparator(true));
                    result = (comparator.compare(extendedLibrary, localLibrary) >= 0);
                }
            } catch (VersionSchemeException e) {
                // Ignore - Invalid version scheme error will be reported when the owning library is
                // validated
            }
        }
        return result;
    }

    /**
     * Checks to determine if one or more other entities entities in the model will be assigned the
     * same name and namespace in the resulting schema(s) that are generated by the compiler. If a
     * duplicate XSD type and/or global element name is detected, a validation error will be added
     * to the builder provided.
     * 
     * @param entity
     *            the named entity to analyze
     * @param builder
     *            the validation builder that will receive any validation errors that are detected
     */
    protected void checkSchemaNamingConflicts(NamedEntity entity, ValidationBuilder<?> builder) {
        // Check for trivial negative cases...
        if ((entity == null) || (entity.getOwningModel() == null)) {
            return;
        }
        SchemaNameValidationRegistry registry = getSchemaNameRegistry(entity.getOwningModel());
        QName conflictingElement;

        if (registry.hasTypeNameConflicts(entity)) {
            QName typeName = registry.getSchemaTypeName(entity);

            builder.addFinding(FindingType.ERROR, "name", ERROR_DUPLICATE_SCHEMA_TYPE_NAME,
                    typeName.getLocalPart(), typeName.getNamespaceURI());

        } else if ((conflictingElement = registry.getElementNameConflicts(entity)) != null) {
            builder.addFinding(FindingType.ERROR, "name", ERROR_DUPLICATE_SCHEMA_ELEMENT_NAME,
                    conflictingElement.getLocalPart(), conflictingElement.getNamespaceURI());
        }
    }

    /**
     * Returns the cached <code>SchemaNameValidationRegistry</code> instance. If a cached object
     * does not exist, one is created automatically.
     * 
     * @param model
     *            the model to use when populating the contents of the registry
     * @return SchemaNameValidationRegistry
     */
    protected SchemaNameValidationRegistry getSchemaNameRegistry(TLModel model) {
        SchemaNameValidationRegistry schemaNameRegistry = (SchemaNameValidationRegistry) getContextCacheEntry(SchemaNameValidationRegistry.class
                .getName());

        if (schemaNameRegistry == null) {
            schemaNameRegistry = new SchemaNameValidationRegistry(model);
            setContextCacheEntry(SchemaNameValidationRegistry.class.getName(), schemaNameRegistry);
        }
        return schemaNameRegistry;
    }

    /**
     * Checks to determine if another entity is assigned to the given entity's major-version
     * namespace that is not a previous or later version of the owning library. The only duplicate
     * names that are allowed in a major-version namespace are for minor versions of
     * <code>Versioned</code> objects.
     * 
     * @param entity
     *            the named entity to analyze
     * @param builder
     *            the validation builder that will receive any validation errors that are detected
     */
    protected void checkMajorVersionNamingConflicts(NamedEntity entity, ValidationBuilder<?> builder) {
        // Check for trivial negative cases...
        if ((entity == null) || (entity.getOwningModel() == null)
                || !(entity.getOwningLibrary() instanceof TLLibrary)
                || (((TLLibrary) entity.getOwningLibrary()).getBaseNamespace() == null)) {
            return;
        }

        String localName = (entity instanceof TLOperation) ? ((TLOperation) entity).getName()
                : entity.getLocalName();
        String majorVersionNamespace = getMajorVersionNamespace((TLLibrary) entity
                .getOwningLibrary());
        List<NamedEntity> matchingEntities = new ArrayList<NamedEntity>();

        // Build a list of all entities from the base namespace whose local names match that of the
        // given entity
        for (TLLibrary library : entity.getOwningModel().getUserDefinedLibraries()) {
            if (!majorVersionNamespace.equals(getMajorVersionNamespace(library))) {
                continue;
            }
            NamedEntity matchingEntity = null;

            if (entity instanceof TLOperation) {
                // Operation names are not in the general schema namespace assignments, so they need
                // to be analyzed separately.
                if (library.getService() != null) {
                    matchingEntity = library.getService().getOperation(localName);
                }
            } else {
                matchingEntity = library.getNamedMember(localName);
            }
            if (matchingEntity != null) {
                matchingEntities.add(matchingEntity);
            }
        }

        // Only check for validation errors if we find duplicates of the entity's name
        if (matchingEntities.size() > 1) {
            Collection<Versioned> versionFamily = (entity instanceof Versioned) ? getMinorVersionFamily((Versioned) entity)
                    : new ArrayList<Versioned>();

            for (NamedEntity matchingEntity : matchingEntities) {
                if (matchingEntity == entity) {
                    continue;
                }
                NamedEntity conflictingEntity = null;

                if ((entity instanceof Versioned) && (matchingEntity instanceof Versioned)
                        && entity.getClass().equals(matchingEntity.getClass())) {
                    // If both entities are versioned, we do NOT have a naming conflict if they are
                    // members of the same family
                    if (!versionFamily.contains(matchingEntity)) {
                        conflictingEntity = matchingEntity;
                    }
                } else {
                    // If one or both of the entities is not versioned, we have a naming conflict
                    conflictingEntity = matchingEntity;
                }

                if (conflictingEntity != null) {
                    // NOTE: This validation check only reports errors for duplicate names in
                    // DIFFERENT namespaces
                    // of a major-version chain. Duplicate symbols that occur in the same namespace
                    // are reported
                    // by different validation checks.
                    if (!conflictingEntity.getNamespace().equals(entity.getNamespace())) {
                        builder.addFinding(FindingType.ERROR, "name",
                                ERROR_DUPLICATE_MAJOR_VERSION_SYMBOL, localName);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Returns the collection of all later and earlier minor versions of the given entity. At a
     * minimum, the resulting collection will contain the original entity. To improve performance,
     * the minor version families are cached in the validation context.
     * 
     * @param versionedEntity
     *            the versioned entity for which to return the minor version family
     * @return Collection<V>
     */
    @SuppressWarnings("unchecked")
    private <V extends Versioned> Collection<V> getMinorVersionFamily(V versionedEntity) {
        Map<Versioned, Collection<V>> minorVersionFamilyMappings = (Map<Versioned, Collection<V>>) getContextCacheEntry("minorVersionFamilyMappings");

        if (minorVersionFamilyMappings == null) {
            minorVersionFamilyMappings = new HashMap<Versioned, Collection<V>>();
            setContextCacheEntry("minorVersionFamilyMappings", minorVersionFamilyMappings);
        }
        Collection<V> minorVersionFamily = minorVersionFamilyMappings.get(versionedEntity);
        Collection<V> priorEntityVersions = getAllExtendedVersions(versionedEntity);

        if (minorVersionFamily == null) {
            minorVersionFamily = new ArrayList<V>();

            if ((versionedEntity.getBaseNamespace() != null)
                    && (versionedEntity.getOwningModel() != null)) {
                for (TLLibrary library : versionedEntity.getOwningModel().getUserDefinedLibraries()) {
                    if (!library.getBaseNamespace().equals(versionedEntity.getBaseNamespace())) {
                        continue;
                    }
                    List<V> versionedMembers = new ArrayList<V>();

                    // Find all of the entities from this library of the same type as our original
                    // versioned entity
                    if (versionedEntity instanceof TLOperation) {
                        if (library.getService() != null) {
                            for (TLOperation operation : library.getService().getOperations()) {
                                versionedMembers.add((V) operation);
                            }
                        }
                    } else {
                        for (NamedEntity libraryMember : library.getNamedMembers()) {
                            if (libraryMember.getClass().equals(versionedEntity.getClass())) {
                                versionedMembers.add((V) libraryMember);
                            }
                        }
                    }

                    // Determine whether the given entity is part of the original entity's minor
                    // version family
                    for (V versionedMember : versionedMembers) {
                        if ((versionedMember == versionedEntity)
                                || priorEntityVersions.contains(versionedMember)) {
                            // The member is a previous version of our original versioned entity (or
                            // the original entity itself)
                            minorVersionFamily.add(versionedMember);

                        } else {
                            Collection<V> priorMemberVersions = getAllExtendedVersions(versionedMember);

                            if (priorMemberVersions.contains(versionedEntity)) {
                                // The original entity is a previous version of this versioned
                                // member
                                minorVersionFamily.add(versionedMember);
                            }
                        }
                    }
                }
            }

            for (Versioned familyMember : minorVersionFamily) {
                minorVersionFamilyMappings.put(familyMember, minorVersionFamily);
            }
        }
        return minorVersionFamily;
    }

    /**
     * Returns the major-version namespace of the given library.
     * 
     * @param library
     *            the library for which to return the major-version namespace
     * @return String
     */
    @SuppressWarnings("unchecked")
    private String getMajorVersionNamespace(TLLibrary library) {
        Map<String, String> majorVersionNamespaceMappings = (Map<String, String>) getContextCacheEntry("majorVersionNamespaceMappings");

        if (majorVersionNamespaceMappings == null) {
            majorVersionNamespaceMappings = new HashMap<String, String>();
            setContextCacheEntry("majorVersionNamespaceMappings", majorVersionNamespaceMappings);
        }
        String libraryNamespace = library.getNamespace();
        String majorVersionNamespace = majorVersionNamespaceMappings.get(libraryNamespace);

        if (majorVersionNamespace == null) {
            try {
                VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
                VersionScheme versionScheme = factory.getVersionScheme(factory
                        .getDefaultVersionScheme());

                majorVersionNamespace = versionScheme.getMajorVersionNamespace(libraryNamespace);

            } catch (VersionSchemeException e) {
                // Use default naming in case of a URI that does not match the default version
                // scheme
                majorVersionNamespace = libraryNamespace;

            } finally {
                majorVersionNamespaceMappings.put(libraryNamespace, majorVersionNamespace);
            }
        }
        return majorVersionNamespace;
    }

}
