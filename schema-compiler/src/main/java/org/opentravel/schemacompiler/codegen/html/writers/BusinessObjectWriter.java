
package org.opentravel.schemacompiler.codegen.html.writers;

import org.opentravel.schemacompiler.codegen.html.builders.BusinessObjectDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;

/**
 * Generate the BusinessObject Information Page.
 */
public class BusinessObjectWriter extends ComplexObjectWriter<BusinessObjectDocumentationBuilder> {

	/**
	 * @param businessObject
	 *            the class being documented.
	 * @param prev
	 *            the previous class that was documented.
	 * @param next
	 *            the next class being documented.
	 * @param classTree
	 *            the class tree for the given class.
	 */
	public BusinessObjectWriter(BusinessObjectDocumentationBuilder businessObject,
			DocumentationBuilder prev, DocumentationBuilder next)
			throws Exception {
		super(businessObject, prev, next);
	}

}
