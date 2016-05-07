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
package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;

/**
 * @author Eric.Bronson
 *
 */
public abstract class AbstractDocumentationBuilder<T extends TLDocumentationOwner & ModelElement>
		implements DocumentationBuilder {

	protected final T element;

	protected String name;

	protected String namespace;
	
	protected DocumentationBuilder next;
	
	protected DocumentationBuilder prev;

	public AbstractDocumentationBuilder(T element) {
		this.element = element;
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}
	
	public String getOwningLibrary(){
		return getLibraryName(element.getOwningLibrary());
	}

	public String getQualifiedName() {
		return null == namespace ? name : namespace + ":" + name;
	}

	public String getDescription() {
		TLDocumentation doc = element.getDocumentation();
		return doc == null ? null : doc.getDescription();
	}
	
	/**
	 * @return the element
	 */
	public T getElement() {
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractDocumentationBuilder<?> other = (AbstractDocumentationBuilder<?>) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		return true;
	}

	public void setNext(DocumentationBuilder next) {
		this.next = next;
	}

	public void setPrevious(DocumentationBuilder prev) {
		this.prev = prev;
	}
	
	public static String getLibraryName(AbstractLibrary library){
		String name = library.getName();
		String version = library.getVersion();
		if(version != null){
			version = version.replace(".", "_");
			name = name + "_" + version;
		}
		return name;
	}

}
