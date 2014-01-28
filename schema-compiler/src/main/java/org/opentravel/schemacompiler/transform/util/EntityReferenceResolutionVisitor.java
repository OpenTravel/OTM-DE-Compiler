package org.opentravel.schemacompiler.transform.util;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.validate.impl.TLModelSymbolResolver;
import org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter;

/**
 * Visitor that attempts to resolve null entity references discovered in a model after new libraries
 * are loaded.
 * 
 * @author S. Livezey
 */
public class EntityReferenceResolutionVisitor extends ModelElementVisitorAdapter {

    private SymbolResolver symbolResolver;

    /**
     * Constructor that assigns the model being navigated.
     * 
     * @param model
     *            the model from which all entity names will be obtained
     */
    public EntityReferenceResolutionVisitor(TLModel model) {
        this(new TLModelSymbolResolver(model));
    }

    /**
     * Constructor that assigns the model being navigated.
     * 
     * @param symbolResolver
     *            the symbol resolver to use for all named entity lookups
     */
    public EntityReferenceResolutionVisitor(SymbolResolver symbolResolver) {
        this.symbolResolver = symbolResolver;
    }

    /**
     * Assigns the context library for the symbol resolver.
     * 
     * @param library
     *            the library to assign as the current lookup context
     */
    public void assignContextLibrary(AbstractLibrary library) {
        symbolResolver.setPrefixResolver(new LibraryPrefixResolver(library));
        symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(library));
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitBuiltInLibrary(org.opentravel.schemacompiler.model.BuiltInLibrary)
     */
    @Override
    public boolean visitBuiltInLibrary(BuiltInLibrary library) {
        assignContextLibrary(library);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitLegacySchemaLibrary(org.opentravel.schemacompiler.model.XSDLibrary)
     */
    @Override
    public boolean visitLegacySchemaLibrary(XSDLibrary library) {
        assignContextLibrary(library);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitUserDefinedLibrary(org.opentravel.schemacompiler.model.TLLibrary)
     */
    @Override
    public boolean visitUserDefinedLibrary(TLLibrary library) {
        assignContextLibrary(library);
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimple(org.opentravel.schemacompiler.model.TLSimple)
     */
    @Override
    public boolean visitSimple(TLSimple simple) {
        if ((simple.getParentType() == null) && (simple.getParentTypeName() != null)) {
            Object ref = symbolResolver.resolveEntity(simple.getParentTypeName());

            if (ref instanceof TLAttributeType) {
                simple.setParentType((TLAttributeType) ref);
            }
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitValueWithAttributes(org.opentravel.schemacompiler.model.TLValueWithAttributes)
     */
    @Override
    public boolean visitValueWithAttributes(TLValueWithAttributes valueWithAttributes) {
        if ((valueWithAttributes.getParentType() == null)
                && (valueWithAttributes.getParentTypeName() != null)) {
            Object ref = symbolResolver.resolveEntity(valueWithAttributes.getParentTypeName());

            if (ref instanceof TLAttributeType) {
                valueWithAttributes.setParentType((TLAttributeType) ref);
            }
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitExtension(org.opentravel.schemacompiler.model.TLExtension)
     */
    @Override
    public boolean visitExtension(TLExtension extension) {
        if ((extension.getExtendsEntity() == null) && (extension.getExtendsEntityName() != null)) {
            Object ref;

            if (extension.getOwner() instanceof TLOperation) {
                ref = symbolResolver.resolveOperationEntity(extension.getExtendsEntityName());

            } else {
                ref = symbolResolver.resolveEntity(extension.getExtendsEntityName());
            }

            if (ref instanceof NamedEntity) {
                extension.setExtendsEntity((NamedEntity) ref);
            }
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitSimpleFacet(org.opentravel.schemacompiler.model.TLSimpleFacet)
     */
    @Override
    public boolean visitSimpleFacet(TLSimpleFacet simpleFacet) {
        if ((simpleFacet.getSimpleType() == null) && (simpleFacet.getSimpleTypeName() != null)) {
            Object ref = symbolResolver.resolveEntity(simpleFacet.getSimpleTypeName());

            if (ref instanceof NamedEntity) {
                simpleFacet.setSimpleType((NamedEntity) ref);
            }
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitAttribute(org.opentravel.schemacompiler.model.TLAttribute)
     */
    @Override
    public boolean visitAttribute(TLAttribute attribute) {
        if ((attribute.getType() == null) && (attribute.getTypeName() != null)) {
            Object ref = symbolResolver.resolveEntity(attribute.getTypeName());

            if (ref instanceof TLAttributeType) {
                attribute.setType((TLAttributeType) ref);
            }
        }
        return true;
    }

    /**
     * @see org.opentravel.schemacompiler.visitor.ModelElementVisitorAdapter#visitElement(org.opentravel.schemacompiler.model.TLProperty)
     */
    @Override
    public boolean visitElement(TLProperty element) {
        if ((element.getType() == null) && (element.getTypeName() != null)) {
            Object ref = symbolResolver.resolveEntity(element.getTypeName());

            if (ref instanceof TLPropertyType) {
                element.setType((TLPropertyType) ref);
            }
        }
        return true;
    }

}
