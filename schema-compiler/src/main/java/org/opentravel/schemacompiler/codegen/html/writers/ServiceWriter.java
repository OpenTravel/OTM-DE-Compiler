/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import org.opentravel.schemacompiler.codegen.html.builders.DocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.builders.ServiceDocumentationBuilder;
import org.opentravel.schemacompiler.codegen.html.Content;
import org.opentravel.schemacompiler.codegen.html.writers.info.OperationInfoWriter;

/**
 * @author Eric.Bronson
 *
 */
public class ServiceWriter extends NamedEntityWriter<ServiceDocumentationBuilder> {

	/**
	 * @param member
	 * @param prev
	 * @param next
	 * @throws Exception
	 */
	public ServiceWriter(ServiceDocumentationBuilder member,
			DocumentationBuilder prev,
			DocumentationBuilder next) throws Exception {
		super(member, prev, next);
	}

	
	public void addOperationInfo(Content classInfoTree) {
		if(member.getOperations().size() > 0){
			OperationInfoWriter facetWriter = new OperationInfoWriter(this, member);
			facetWriter.addInfo(classInfoTree);
		}
	}
	
}
