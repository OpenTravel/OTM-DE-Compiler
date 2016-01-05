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

import java.io.StringWriter;
import java.io.Writer;

import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;

/**
 * Builder component that is capable of producing example output for model entities in a variety
 * of formats (e.g. Object Tree, text, and streaming output).
 * 
 * @author S. Livezey, E. Bronson
 */
public abstract class ExampleBuilder<Tree> {
	
	protected ExampleGeneratorOptions options = new ExampleGeneratorOptions();
    protected NamedEntity modelElement;
    
    /**
     * Default constructor.
     */
    public ExampleBuilder() {
    }
    
    /**
     * Constructor that assigns the example generation options to use when constructing the example
     * content and formatting the text/stream output.
     * 
     * @param options
     *            the example generation options
     */
    public ExampleBuilder(ExampleGeneratorOptions options) {
        setOptions(options);
    }

    /**
     * Assigns the example generation options for this builder instance. Assigning a null value to
     * this method will result in the default option values being used.
     * 
     * @param options
     *            the example generation options to assign
     * @return ExampleDocumentBuilder
     */
    public ExampleBuilder<Tree> setOptions(ExampleGeneratorOptions options) {
        this.options = (options == null) ? new ExampleGeneratorOptions() : options;
        return this;
    }

    /**
     * Assigns the model element for which example output is to be generated.
     * 
     * @param modelElement
     *            the model element for which to create example output
     * @return ExampleDocumentBuilder
     */
    public ExampleBuilder<Tree> setModelElement(NamedEntity modelElement) {
        this.modelElement = modelElement;
        return this;
    }
    
    /**
     * Validates the current model element and all of its dependencies and throws a
     * <code>ValidationException</code> if one or more errors are detected.
     * 
     * @throws ValidationException
     *             thrown if one or more of the entities for which content is to be generated
     *             contains errors (warnings are acceptable and will not produce an exception)
     */
    protected void validateModelElement() throws ValidationException {
        if (modelElement == null) {
            throw new NullPointerException("The model element for example output cannot be null.");
        }
        ValidationFindings findings = TLModelCompileValidator
                .validateModelElement((TLModelElement) modelElement);

        if (findings.hasFinding(FindingType.ERROR)) {
            throw new ValidationException(
                    "Unable to generate example content due to validation errors.", findings);
        }
    }
    
    /**
     * Generates the example output and returns a string containing the content.
     * 
     * @return String
     * @throws ValidationException
     *             thrown if one or more of the entities for which content is to be generated
     *             contains errors (warnings are acceptable and will not produce an exception)
     * @throws CodeGenerationException
     *             thrown if an error occurs during example content generation
     */
    public String buildString() throws ValidationException, CodeGenerationException {
        StringWriter writer = new StringWriter();

        buildToStream(writer);
        return writer.toString();
    }

    /**
     * Generates the example output and directs the resuting content to the specified writer.
     * 
     * @param buffer
     *            the output writer to which the example content should be directed
     * @return String
     * @throws ValidationException
     *             thrown if one or more of the entities for which content is to be generated
     *             contains errors (warnings are acceptable and will not produce an exception)
     * @throws CodeGenerationException
     *             thrown if an error occurs during example content generation
     */
    public abstract void buildToStream(Writer buffer) throws ValidationException, CodeGenerationException;
    
    /**
     * Generates the example output as a structure and returns the raw tree content.
     * 
     * @return Tree
     * @throws ValidationException
     *             thrown if one or more of the entities for which content is to be generated
     *             contains errors (warnings are acceptable and will not produce an exception)
     * @throws CodeGenerationException
     *             thrown if an error occurs during example content generation
     */
    public abstract Tree buildTree() throws ValidationException, CodeGenerationException;

}
