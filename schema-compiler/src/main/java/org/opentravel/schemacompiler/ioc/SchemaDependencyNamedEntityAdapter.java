
package org.opentravel.schemacompiler.ioc;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModel;

/**
 * Adapter that allows a <code>SchemaDependency</code> object to behave as an
 * OTA2.0 <code>NamedEntity</code>.
 *
 * @author S. Livezey
 */
public class SchemaDependencyNamedEntityAdapter implements NamedEntity {
	
	private SchemaDependency schemaDependency;
	
	public SchemaDependencyNamedEntityAdapter(SchemaDependency schemaDependency) {
		this.schemaDependency = schemaDependency;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.NamedEntity#getNamespace()
	 */
	public String getNamespace() {
		return schemaDependency.getSchemaDeclaration().getNamespace();
	}

	/**
	 * @see org.opentravel.schemacompiler.model.NamedEntity#getLocalName()
	 */
	public String getLocalName() {
		return schemaDependency.getLocalName();
	}
	
	/**
	 * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
	 */
	public TLModel getOwningModel() {
		return null;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.LibraryElement#getOwningLibrary()
	 */
	public AbstractLibrary getOwningLibrary() {
		return null;
	}

	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	public String getValidationIdentity() {
		return null;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.LibraryElement#cloneElement()
	 */
	public LibraryElement cloneElement() {
		return null;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.LibraryElement#cloneElement(org.opentravel.schemacompiler.model.AbstractLibrary)
	 */
	public LibraryElement cloneElement(AbstractLibrary namingContext) {
		return null;
	}

}
