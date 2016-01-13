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

import java.util.Locale;

import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.validate.ValidationBuilder;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

/**
 * Extension of the <code>ValidationBuilder</code> base class that adds assertions for
 * <code>NamedEntity</code> references within a <code>TLModel</code>.
 * 
 * @author S. Livezey
 */
public final class TLValidationBuilder extends ValidationBuilder<TLValidationBuilder> {

    public static final String MISSING_NAMED_ENTITY_REFERENCE = "MISSING_NAMED_ENTITY_REFERENCE";
    public static final String UNRESOLVED_NAMED_ENTITY_REFERENCE = "UNRESOLVED_NAMED_ENTITY_REFERENCE";
    public static final String MISMATCHED_NAMED_ENTITY_REFERENCE = "MISMATCHED_NAMED_ENTITY_REFERENCE";
    public static final String INVALID_NAMED_ENTITY_REFERENCE = "INVALID_NAMED_ENTITY_REFERENCE";
    public static final String CONTAINS_DUPLICATE_ALIAS = "CONTAINS_DUPLICATE_ALIAS";
    public static final String UNRECOGNIZED_VERSION_SCHEME = "UNRECOGNIZED_VERSION_SCHEME";
    public static final String INVALID_NAMESPACE_FOR_VERSION_SCHEME = "INVALID_NAMESPACE_FOR_VERSION_SCHEME";
    public static final String DEPRECATED_TYPE_REFERENCE = "DEPRECATED_TYPE_REFERENCE";

    private TLModelValidationContext validationContext;
    private boolean isNamedEntityProperty = false;
    private String plainTextEntityName;
    private String versionSchemeIdentifier;
    private VersionScheme versionScheme;

    /**
     * Constructor that assigns the validation context to use for name resolution.
     * 
     * @param prefix
     *            the prefix string to pre-pend for all error/warning messages
     * @param validationContext
     *            the validation context for the owning model
     */
    public TLValidationBuilder(String prefix, TLModelValidationContext validationContext) {
        super(prefix);
        this.validationContext = validationContext;
    }

    /**
     * @see org.opentravel.schemacompiler.validate.ValidationBuilder#getThis()
     */
    @Override
    protected TLValidationBuilder getThis() {
        return this;
    }

    /**
     * @see org.opentravel.schemacompiler.validate.ValidationBuilder#setProperty(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public TLValidationBuilder setProperty(String propertyName, Object propertyValue) {
        this.plainTextEntityName = null;
        this.isNamedEntityProperty = false;
        return super.setProperty(propertyName, propertyValue);
    }

    /**
     * Assigns the property value (presumed to be from the target object) to be validated as a
     * reference to another named model entity.
     * 
     * @param propertyName
     *            the name of the entity reference property
     * @param propertyValue
     *            the value of the entity reference
     * @param expectedEntityType
     *            the expected entity
     * @return TLValidationBuilder
     */
    public TLValidationBuilder setEntityReferenceProperty(String propertyName,
            NamedEntity propertyValue, String plainTextEntityName) {
        super.setProperty(propertyName, propertyValue);
        this.plainTextEntityName = plainTextEntityName;
        this.isNamedEntityProperty = true;
        return getThis();
    }

    /**
     * Assigns the version scheme to be used during validation checks. If the given identifier is
     * not valid, this method will assign a null value for the version scheme implementation.
     * 
     * @param versionSchemeIdentifier
     *            the identifier of the version scheme implementation
     * @return TLValidationBuilder
     */
    public TLValidationBuilder setVersionScheme(String versionSchemeIdentifier) {
        try {
            this.versionScheme = VersionSchemeFactory.getInstance().getVersionScheme(
                    versionSchemeIdentifier);
            this.versionSchemeIdentifier = versionSchemeIdentifier;

        } catch (VersionSchemeException e) {
            this.versionScheme = null;
            this.versionSchemeIdentifier = null;
        }
        return getThis();
    }

    /**
     * @see org.opentravel.schemacompiler.validate.ValidationBuilder#assertNotNull()
     */
    @Override
    public TLValidationBuilder assertNotNull() {
        if (isNamedEntityProperty) {
            NamedEntity value = propertyValueAsNamedEntity();

            if (value == null) {
                if ((plainTextEntityName == null) || plainTextEntityName.equals("")) {
                    addFinding(MISSING_NAMED_ENTITY_REFERENCE);
                } else {
                    addFinding(UNRESOLVED_NAMED_ENTITY_REFERENCE, plainTextEntityName);
                }
            }
        } else {
            return super.assertNotNull();
        }
        return getThis();
    }

    /**
     * Asserts that the current entity reference property being evaluated is a sub-class/interface
     * of at least one of the expected types provided.
     * 
     * <p>
     * Null values checked by this method <i>will not</i> produce an error.
     * 
     * @param expectedEntityTypes
     *            the valid entity types for the property reference
     * @return TLValidationBuilder
     */
    public TLValidationBuilder assertValidEntityReference(Class<?>... expectedEntityTypes) {
        NamedEntity value = propertyValueAsNamedEntity();

        if (value != null) {
            String entityName = (value == null) ? null : validationContext.getSymbolResolver()
                    .buildEntityName(value.getNamespace(), value.getLocalName());
            boolean isValid = false;

            for (Class<?> entityType : expectedEntityTypes) {
                isValid |= entityType.isAssignableFrom(value.getClass());
            }
            if (!isValid) {
                addFinding(INVALID_NAMED_ENTITY_REFERENCE, getDisplayNames(expectedEntityTypes),
                        entityName);
            }

            // NOTE: Temporarily removed until integrity-checker implementation is complete; will
            // re-evaluate
            // if this validation check is necessary at that time.
            //
            // if ((plainTextEntityName != null) && !plainTextEntityName.equals(entityName)) {
            // addFinding(MISMATCHED_NAMED_ENTITY_REFERENCE, entityName, plainTextEntityName);
            // }
        }
        return getThis();
    }

    /**
     * Adds a validation finding if the string value does not represent a valid (recognizable)
     * version scheme identifier.
     * 
     * @return TLValidationBuilder
     */
    public TLValidationBuilder assertValidVersionScheme() {
        String versionSchemeIdentifier = propertyValueAsString();
        try {
            if (versionSchemeIdentifier != null) {
                VersionSchemeFactory.getInstance().getVersionScheme(versionSchemeIdentifier);
            }
        } catch (VersionSchemeException e) {
            addFinding(UNRECOGNIZED_VERSION_SCHEME, versionSchemeIdentifier);
        }
        return getThis();
    }

    /**
     * Adds a validation finding if the string value does not represent a valid (recognizable)
     * version scheme identifier. If a valid version scheme has not been specified prior to this
     * call, the validation WILL NOT be performed.
     * 
     * @return TLValidationBuilder
     */
    public TLValidationBuilder assertValidNamespaceForVersionScheme() {
        if (versionScheme != null) {
            String namespace = propertyValueAsString();

            if (!versionScheme.isValidNamespace(namespace)) {
                addFinding(INVALID_NAMESPACE_FOR_VERSION_SCHEME, namespace, versionSchemeIdentifier);
            }
        }
        return getThis();
    }

    /**
     * Adds a validation finding if the <code>NamedEntity</code> value is deprecated. Entities are
     * deprecated if they contain one or more <code>Deprecation</code> documentation elements.
     * 
     * @return TLValidationBuilder
     */
    public TLValidationBuilder assertNotDeprecated() {
        if (isNamedEntityProperty) {
            NamedEntity value = propertyValueAsNamedEntity();

            if (value != null) {
                boolean isDeprecated = false;

                if (ValidatorUtils.isEmptyValueType(value)) {
                	// Special case - do not warn deprecations on ota2:Empty
                	
                } else if (value instanceof TLDocumentationOwner) {
                    isDeprecated = DocumentationFinder.isDeprecated( (TLDocumentationOwner) value );
                    
                } else {
                    AbstractLibrary valueLibrary = value.getOwningLibrary();

                    // Entities defined in built-in libraries that contain the keyword "Deprecated"
                    // will always
                    // be considered to be deprecated. YES - this is a kludge, but we have no other
                    // (easy) way
                    // to deprecate legacy (XSD) built-in schema entities. :)
                    if (valueLibrary instanceof BuiltInLibrary) {
                        isDeprecated = (valueLibrary.getName() != null)
                                && (valueLibrary.getName().indexOf("Deprecated") >= 0);
                    }
                }
                if (isDeprecated) {
                    addFinding(DEPRECATED_TYPE_REFERENCE, value.getLocalName());
                }
            }
        } else {
            return super.assertNotNull();
        }
        return getThis();
    }

    /**
     * Returns the property value as a <code>NamedEntity</code>.
     * 
     * @return NamedEntity
     * @throws IllegalArgumentException
     *             thrown if the current property value is not a named entity reference
     */
    private NamedEntity propertyValueAsNamedEntity() {
        if (isNamedEntityProperty
                && ((propertyValue == null) || (propertyValue instanceof NamedEntity))) {
            return (NamedEntity) propertyValue;
        } else {
            throw new IllegalArgumentException(
                    "The requested assertion only applies to NamedEntity values");
        }
    }

    /**
     * Returns the display name(s) of the given model element type(s).
     * 
     * @param modelElementTypes
     *            the type(s) for which to return a display name
     * @return String
     */
    private String getDisplayNames(Class<?>... modelElementTypes) {
        String displayName;

        if ((modelElementTypes == null) || (modelElementTypes.length == 0)) {
            displayName = "[UNKNOWN TYPE(S)]";
        } else if (modelElementTypes.length == 1) {
            try {
                displayName = SchemaCompilerApplicationContext.getContext().getMessage(
                        modelElementTypes[0].getSimpleName() + ".displayName", null,
                        Locale.getDefault());

            } catch (Throwable t) {
                displayName = (modelElementTypes[0] == null) ? "[UNKNOWN TYPE]"
                        : modelElementTypes[0].getSimpleName();
            }
        } else {
            StringBuilder dnBuilder = new StringBuilder("[");
            boolean firstType = true;

            for (Class<?> elementType : modelElementTypes) {
                if (!firstType) {
                    dnBuilder.append(", ");
                }
                try {
                    String elementName = SchemaCompilerApplicationContext.getContext()
                            .getMessage(elementType.getSimpleName() + ".displayName", null,
                                    Locale.getDefault());

                    dnBuilder.append(elementName);

                } catch (Throwable t) {
                    dnBuilder.append((elementType == null) ? "[UNKNOWN TYPE]" : elementType
                            .getSimpleName());
                }
                firstType = false;
            }
            displayName = dnBuilder.append(']').toString();
        }
        return displayName;
    }

}
