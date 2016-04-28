/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import java.io.File;
import java.util.Collection;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.slf4j.Logger;

import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.HtmlDoclet;

/**
 * @author Eric.Bronson
 *
 */
public class DocumentationGenerator implements CodeGenerator<TLModel> {

	private CodeGenerationFilter filter = null;

	private CodeGenerationFilenameBuilder<TLModel> filenameBuilder;

	private Logger log;

	/**
	 * 
	 */
	public DocumentationGenerator() {
	}

	@Override
	public Collection<File> generateOutput(TLModel model,
			CodeGenerationContext context) throws ValidationException,
			CodeGenerationException {
		Configuration config = Configuration.getInstance();
		File destination = new File(context
				.getValue(CodeGenerationContext.CK_OUTPUT_FOLDER));
		if(!destination.exists()){
			destination.mkdirs();
		}
		config.setDestDirName(destination.getPath() + File.separator);
		String title = context
				.getValue(CodeGenerationContext.CK_PROJECT_FILENAME);
		if (title != null) {
			config.setWindowtitle(title);
			config.doctitle = title;
		}
		config.setModel(model);
		HtmlDoclet.start(model);
		return null;
	}

	@Override
	public CodeGenerationFilter getFilter() {
		return filter;
	}

	@Override
	public void setFilter(CodeGenerationFilter filter) {
		this.filter = filter;
	}

	@Override
	public CodeGenerationFilenameBuilder<TLModel> getFilenameBuilder() {
		return filenameBuilder;
	}

	@Override
	public void setFilenameBuilder(
			CodeGenerationFilenameBuilder<TLModel> filenameBuilder) {
		this.filenameBuilder = filenameBuilder;
	}

	@Override
	public void setLogger(Logger log) {
		this.log = log;
	}

}
