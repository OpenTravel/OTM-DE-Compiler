package org.opentravel.schemacompiler.ic;

import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.event.ValueChangeEvent;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLSimpleFacet;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.transform.util.ChameleonFilter;
import org.opentravel.schemacompiler.transform.util.LibraryPrefixResolver;
import org.opentravel.schemacompiler.validate.impl.TLModelSymbolResolver;

/**
 * Model integrity check listener, that ensures all 'typeName' fields are synchronized with the type
 * instances that are assigned to the referencing model object.
 * 
 * @author S. Livezey
 */
public class TypeNameIntegrityChecker extends
        AbstractIntegrityChecker<ValueChangeEvent<TLModelElement, NamedEntity>, TLModelElement> {

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#processModelEvent(org.opentravel.schemacompiler.event.ModelEvent)
     */
    @Override
    public void processModelEvent(ValueChangeEvent<TLModelElement, NamedEntity> event) {
        if ((event.getType() == ModelEventType.TYPE_ASSIGNMENT_MODIFIED)
                || (event.getType() == ModelEventType.EXTENDS_ENTITY_MODIFIED)) {
            TLModelElement sourceObject = event.getSource();
            String entityName = buildEntityName(event.getNewValue(), sourceObject);

            if (sourceObject instanceof TLSimple) {
                ((TLSimple) sourceObject).setParentTypeName(entityName);

            } else if (sourceObject instanceof TLValueWithAttributes) {
                ((TLValueWithAttributes) sourceObject).setParentTypeName(entityName);

            } else if (sourceObject instanceof TLSimpleFacet) {
                ((TLSimpleFacet) sourceObject).setSimpleTypeName(entityName);

            } else if (sourceObject instanceof TLProperty) {
                ((TLProperty) sourceObject).setTypeName(entityName);

            } else if (sourceObject instanceof TLAttribute) {
                ((TLAttribute) sourceObject).setTypeName(entityName);

            } else if (sourceObject instanceof TLExtension) {
                ((TLExtension) sourceObject).setExtendsEntityName(entityName);
            }
        }
    }

    /**
     * Returns the name of the given entity as either 'prefix:localName' or simple 'localName' (if
     * the entity is assigned to the local namespace provided).
     * 
     * @param assignedEntity
     *            the entity whose name is to be returned
     * @param sourceObject
     *            the object to which the named entity was assigned
     * @return String
     */
    private String buildEntityName(NamedEntity assignedEntity, TLModelElement sourceObject) {
        String entityName = null;

        if (assignedEntity != null) {
            AbstractLibrary owningLibrary = getOwningLibrary(sourceObject);
            SymbolResolver symbolResolver = new TLModelSymbolResolver(sourceObject.getOwningModel());

            symbolResolver.setPrefixResolver(new LibraryPrefixResolver(owningLibrary));
            symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(owningLibrary));
            entityName = symbolResolver.buildEntityName(assignedEntity.getNamespace(),
                    assignedEntity.getLocalName());
        }
        return entityName;
    }

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#getEventClass()
     */
    @Override
    public Class<?> getEventClass() {
        return ValueChangeEvent.class;
    }

    /**
     * @see org.opentravel.schemacompiler.event.ModelEventListener#getSourceObjectClass()
     */
    @Override
    public Class<TLModelElement> getSourceObjectClass() {
        return TLModelElement.class;
    }

}
