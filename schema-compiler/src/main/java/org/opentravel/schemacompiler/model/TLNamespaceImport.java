
package org.opentravel.schemacompiler.model;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.event.ModelEvent;
import org.opentravel.schemacompiler.event.ModelEventBuilder;
import org.opentravel.schemacompiler.event.ModelEventType;
import org.opentravel.schemacompiler.util.FileHintUtils;

/**
 * Encapsulates a library namespace import that includes the namespace itself and a
 * referencable prefix.
 * 
 * @author S. Livezey
 */
public class TLNamespaceImport extends TLModelElement {
	
	private AbstractLibrary owningLibrary;
	private String prefix;
	private String namespace;
	private List<String> fileHints = new ArrayList<String>();
	
	/**
	 * Full constructor.
	 * 
	 * @param prefix  the referencable prefix for the imported namespace
	 * @param namespace  the imported namespace
	 */
	public TLNamespaceImport(String prefix, String namespace) {
		this.prefix = prefix;
		this.namespace = namespace;
	}

	/**
	 * Returns the value of the 'owningLibrary' field.
	 *
	 * @return AbstractLibrary
	 */
	public AbstractLibrary getOwningLibrary() {
		return owningLibrary;
	}

	/**
	 * @see org.opentravel.schemacompiler.validate.Validatable#getValidationIdentity()
	 */
	@Override
	public String getValidationIdentity() {
		StringBuilder identity = new StringBuilder();
		
		if (owningLibrary != null) {
			identity.append(owningLibrary.getValidationIdentity()).append(" : ");
		}
		if (prefix == null) {
			identity.append("[Un-prefixed namespace import]");
		} else {
			identity.append(prefix);
		}
		return identity.toString();
	}

	/**
	 * Assigns the value of the 'owningLibrary' field.
	 *
	 * @param owningLibrary  the field value to assign
	 */
	public void setOwningLibrary(AbstractLibrary owningLibrary) {
		this.owningLibrary = owningLibrary;
	}

	/**
	 * @see org.opentravel.schemacompiler.model.ModelElement#getOwningModel()
	 */
	@Override
	public TLModel getOwningModel() {
		return (owningLibrary == null) ? null : owningLibrary.getOwningModel();
	}

	/**
	 * Moves this namespace import up by one position in the list of namespace imports maintained by its
	 * owner.  If the owner is null, or this namespace import is already at the front of the list, this
	 * method has no effect.
	 */
	public void moveUp() {
		if (owningLibrary != null) {
			owningLibrary.moveUp(this);
		}
	}

	/**
	 * Moves this namespace import down by one position in the list of namespace imports maintained by its
	 * owner.  If the owner is null, or this namespace import is already at the end of the list, this
	 * method has no effect.
	 */
	public void moveDown() {
		if (owningLibrary != null) {
			owningLibrary.moveDown(this);
		}
	}

	/**
	 * Returns the value of the 'prefix' field.
	 *
	 * @return String
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Assigns the value of the 'prefix' field.
	 *
	 * @param prefix  the field value to assign
	 */
	public void setPrefix(String prefix) {
		ModelEvent<?> event = new ModelEventBuilder(ModelEventType.PREFIX_MODIFIED, this)
				.setOldValue(this.prefix).setNewValue(prefix).buildEvent();

		this.prefix = prefix;
		publishEvent(event);
	}

	/**
	 * Returns the value of the 'namespace' field.
	 *
	 * @return String
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Assigns the value of the 'namespace' field.
	 *
	 * @param namespace  the field value to assign
	 */
	public void setNamespace(String namespace) {
		if (this.namespace != null) {
			throw new IllegalStateException(
					"Once assigned, the namespace value of an import cannot be modified (deleted and re-create instead).");
		}
		this.namespace = namespace;
	}

	/**
	 * Returns the value of the 'fileHints' field.
	 *
	 * @return List<String>
	 */
	public synchronized List<String> getFileHints() {
		if (owningLibrary != null) {
			List<String> oldHints = new ArrayList<String>( fileHints );
			
			fileHints.clear();
			fileHints.addAll( FileHintUtils.resolveHints(oldHints, owningLibrary.getLibraryUrl()) );
		}
		return fileHints;
	}

	/**
	 * Manages lists of <code>TLNamespaceImport</code> entities.
	 *
	 * @author S. Livezey
	 */
	protected static class NamespaceImportListManager extends ChildEntityListManager<TLNamespaceImport,AbstractLibrary> {

		/**
		 * Constructor that specifies the owner of the unerlying list.
		 * 
		 * @param owner  the owner of the underlying list of children
		 */
		public NamespaceImportListManager(AbstractLibrary owner) {
			super(owner, ModelEventType.IMPORT_ADDED, ModelEventType.IMPORT_REMOVED);
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ChildEntityListManager#getChildName(java.lang.Object)
		 */
		@Override
		protected String getChildName(TLNamespaceImport child) {
			return child.getPrefix();
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ChildEntityListManager#assignOwner(java.lang.Object, java.lang.Object)
		 */
		@Override
		protected void assignOwner(TLNamespaceImport child, AbstractLibrary owner) {
			child.setOwningLibrary(owner);
		}

		/**
		 * @see org.opentravel.schemacompiler.model.ChildEntityListManager#publishEvent(java.lang.Object, org.opentravel.schemacompiler.event.ModelEvent)
		 */
		@Override
		protected void publishEvent(AbstractLibrary owner, ModelEvent<?> event) {
			TLModel owningModel = owner.getOwningModel();
			
			if (owningModel != null) {
				owningModel.publishEvent(event);
			}
		}

	}
	
}
