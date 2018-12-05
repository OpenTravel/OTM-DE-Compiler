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

import java.io.IOException;
import java.io.Writer;

import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.validate.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Eric.Bronson
 *
 */
public class ExampleJsonBuilder extends ExampleBuilder<JsonNode> {

	public ExampleJsonBuilder(ExampleGeneratorOptions options) {
		super(options);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.example.ExampleBuilder#buildToStream
	 * (java.io.Writer)
	 */
	@Override
	public void buildToStream(Writer buffer) throws ValidationException,
			CodeGenerationException {
		ObjectMapper mapper = new ObjectMapper();

		JsonNode node = buildTree();
		try {
			mapper.writer(SerializationFeature.INDENT_OUTPUT).writeValue(
					buffer, node);
			buffer.flush();
		} catch (IOException e) {
			throw new CodeGenerationException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.opentravel.schemacompiler.codegen.example.ExampleBuilder#buildTree()
	 */
	@Override
	public JsonNode buildTree() throws ValidationException,
			CodeGenerationException {
		validateModelElement();
		JSONExampleVisitor visitor = new JSONExampleVisitor(
				options.getExampleContext());
		ExampleNavigator.navigate(modelElement, visitor, options);
		return visitor.getNode();
	}
}
