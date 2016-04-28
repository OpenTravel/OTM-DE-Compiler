/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;

/**
 * @author Eric.Bronson
 *
 */
public abstract class NamedEntityDocumentationBuilder<T extends NamedEntity & TLDocumentationOwner> extends
		AbstractDocumentationBuilder<T> {
		
	protected DocumentationBuilder superType;

	/**
	 * @param element
	 */
	public NamedEntityDocumentationBuilder(T element) {
		super(element);
		name = element.getLocalName();
		namespace = element.getNamespace();
		// prevent cyclic dependencies
		DocumentationBuilderFactory.addDocumentationBuilder(this, namespace, name);
	}
	
	public DocumentationBuilder getSuperType() {
		return superType;
	}

}
