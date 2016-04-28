/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

import org.opentravel.schemacompiler.codegen.html.JaxbNamingUtils;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;

import com.sun.xml.bind.api.impl.NameConverter;

/**
 * @author Eric.Bronson
 *
 */
public abstract class FieldDocumentationBuilder<T extends TLDocumentationOwner>
		extends AbstractDocumentationBuilder<T> {

	protected DocumentationBuilder type;
	
	protected NamedEntityDocumentationBuilder<?> owner;

	protected String typeName;

	protected String javaFieldName;	
	
	protected String sinceVersion;
	
	protected boolean isRequired;
	
	protected int maxOcurrences;

	protected String pattern;

	protected String exampleValue;
	
	
//	private int minLength = -1;
//  private int maxLength = -1;
//  private int fractionDigits = -1;
//  private int totalDigits = -1;
//  private String minInclusive;
//  private String maxInclusive;
//  private String minExclusive;
//  private String maxExclusive;
	

	/**
	 * @param 
	 */
	public FieldDocumentationBuilder(T t) {
		super(t);
	}

	/**
	 * @return the isRequired
	 */
	public boolean isRequired() {
		return isRequired;
	}

	/**
	 * @return the maxOcurrences
	 */
	public int getMaxOcurrences() {
		return maxOcurrences;
	}

	/**
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @return the exampleValue
	 */
	public String getExampleValue() {
		return exampleValue;
	}
	
	
	public DocumentationBuilder getType() {
		return type;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getJavaFieldName() {
		return javaFieldName;
	}

	public String getSinceVersion() {
		return sinceVersion;
	}

	/**
	 * @return the owner
	 */
	public NamedEntityDocumentationBuilder<?> getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(NamedEntityDocumentationBuilder<?> owner) {
		this.owner = owner;
	}
	
	/**
	 * Get a Java variable name from an element or attribute name
	 * 
	 * @param localName
	 *            the element or attribute name
	 * @return the variable name
	 */
	public static String getVariableName(String name) {
		int subgrpIndex = name.indexOf("SubGrp");
		if (subgrpIndex > 0) {
			name = name.substring(0, subgrpIndex);
		}
		return JaxbNamingUtils.toJavaIdentifier(NameConverter.smart
				.toVariableName(name));
	}

}
