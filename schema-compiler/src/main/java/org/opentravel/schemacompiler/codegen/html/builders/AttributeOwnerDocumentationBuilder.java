/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.example.ExampleDocumentBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleJsonBuilder;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLAttributeOwner;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLIndicatorOwner;
import org.opentravel.schemacompiler.validate.ValidationException;

/**
 * @author Eric.Bronson
 *
 */
public abstract class AttributeOwnerDocumentationBuilder<T extends TLDocumentationOwner & TLAttributeOwner & TLIndicatorOwner>
		extends NamedEntityDocumentationBuilder<T> {

	protected List<AttributeDocumentationBuilder> attributes;

	protected List<IndicatorDocumentationBuilder> indicators;

	private String exampleXML;

	private String exampleJSON;

	/**
	 * @param manager
	 */
	public AttributeOwnerDocumentationBuilder(T t) {
		super(t);
		attributes = new ArrayList<AttributeDocumentationBuilder>();
		indicators = new ArrayList<IndicatorDocumentationBuilder>();
		for(TLAttribute attribute : t.getAttributes()){
			AttributeDocumentationBuilder attBuilder = new AttributeDocumentationBuilder(attribute);
			attributes.add(attBuilder);
			attBuilder.setOwner(this);
		}
		
		for(TLIndicator indicator : t.getIndicators()){
			IndicatorDocumentationBuilder indBuilder = new IndicatorDocumentationBuilder(indicator);
			indicators.add(indBuilder);
			indBuilder.setOwner(this);
		}
		buildExamples(t);
	}
	
	protected void buildExamples(T t){
//		OTMExampleDocumentBuilder exampleBuilder = new OTMExampleDocumentBuilder();
//		exampleBuilder.setModelElement(t);
		try {
			ExampleDocumentBuilder exampleBuilder = new ExampleDocumentBuilder(null);
			exampleBuilder.setModelElement(t);
			exampleXML = exampleBuilder.buildString();	
		} catch (ValidationException | CodeGenerationException e) {
			exampleXML = "";
		}
		
		try {
			ExampleJsonBuilder exampleBuilder = new ExampleJsonBuilder(null);
			exampleBuilder.setModelElement(t);
			
			exampleJSON = exampleBuilder.buildString();
		} catch (ValidationException | CodeGenerationException e) {
			exampleJSON = "";
		}	
	}
	
	public List<AttributeDocumentationBuilder> getAttributes() {
		return Collections.unmodifiableList(attributes);
	}
	

	public List<IndicatorDocumentationBuilder> getIndicators() {
		return Collections.unmodifiableList(indicators);
	}
	
	public String getExampleXML() {
		return exampleXML;
	}

	public String getExampleJSON() {
		return exampleJSON;
	}
	
	

}
