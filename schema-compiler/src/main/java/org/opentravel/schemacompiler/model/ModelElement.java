package org.opentravel.schemacompiler.model;

import org.opentravel.schemacompiler.validate.Validatable;

/**
 * Basic interface implemented by all entities that are candidate members of a <code>TLModel</code>
 * instance.
 * 
 * @author S. Livezey
 */
public interface ModelElement extends Validatable {

    /**
     * Returns the model that owns this member element.
     * 
     * @return TLModel
     */
    public TLModel getOwningModel();

}
