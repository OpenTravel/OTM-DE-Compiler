/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.builders;

/**
 * @author Eric.Bronson
 *
 */
public interface DocumentationBuilder {

	public String getName();

	public String getNamespace();

	public String getQualifiedName();
	
	public DocumentationBuilderType getDocType();
	
	public void setNext(DocumentationBuilder next);
	
	public void setPrevious(DocumentationBuilder prev);
	
	public void build() throws Exception;
	
	public String getDescription();
	
	public String getOwningLibrary();
	
	public static enum DocumentationBuilderType {
		BUSINESS_OBJECT("BusinessObject"), 
		CORE_OBJECT("CoreObject"), 
		VWA("ValueWithAttributes"), 
		SERVICE("Service"), 
		SIMPLE("SimpleType"), 
		CLOSED_ENUM("Closed Enumeration"),
		OPEN_ENUM("Open Enumeration"),
		FACET("Facet"),
		OPERATION("Operation"), 
		INDICATOR("Indicator"), 
		ATTRIBUTE("Attribute"), 
		PROPERTY("Property"), 
		LIBRARY("Library");
		
		private String type;
		
		private DocumentationBuilderType(String type){
			this.type = type;
		}

		/**
		 * @return the type
		 */
		public String toString() {
			return type;
		}
		
	}

	
}
