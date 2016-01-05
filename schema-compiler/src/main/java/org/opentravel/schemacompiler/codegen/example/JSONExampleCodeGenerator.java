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
package org.opentravel.schemacompiler.codegen.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModelElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Code generator that produces sample JSON output for <code>NamedEntity</code>
 * members of the generated libraries.
 * 
 * @author E. Bronson
 * 
 */
public class JSONExampleCodeGenerator extends
		AbstractExampleCodeGenerator {
	
	/**
	 * The file extension for json, i.e. filename.json.
	 */
	public static final String JSON_FILE_EXTENSION = "json";
	
	/**
	 * Default constructor. Initializes the proper file extension.
	 */
	public JSONExampleCodeGenerator() {
		super(JSON_FILE_EXTENSION);
	}

	/**
	 * Responsible for creating the json output.
	 */
	private ObjectMapper mapper;

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#doGenerateOutput(org.opentravel.schemacompiler.model.TLModelElement,
	 *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	public void doGenerateOutput(TLModelElement source,
			CodeGenerationContext context) throws CodeGenerationException {
		File outputFile = getOutputFile(source, context);
		try (OutputStream out = new FileOutputStream(outputFile);) {
			ExampleJsonBuilder exampleBuilder = new ExampleJsonBuilder(
					getOptions(context));
			exampleBuilder.setModelElement((NamedEntity) source);
			ObjectMapper mapper = getObjectMapper();
			JsonNode node = exampleBuilder.buildTree();
			mapper.writeValue(out, node);
			addGeneratedFile(outputFile);
		} catch (Throwable t) {
			throw new CodeGenerationException(t);
		}
	}

	/**
	 * Lazy initialization of the ObjectMapper.
	 * @return the configured ObjectMapper
	 */
	private ObjectMapper getObjectMapper() {
		if(mapper == null){
			mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
		}
		return mapper;
	}

}
