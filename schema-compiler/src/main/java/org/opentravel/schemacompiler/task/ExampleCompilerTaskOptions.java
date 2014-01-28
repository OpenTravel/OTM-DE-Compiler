
package org.opentravel.schemacompiler.task;

/**
 * Interface that defines the task options required for the generation of example output.
 *  
 * @author S. Livezey
 */
public interface ExampleCompilerTaskOptions {
	
	/**
	 * Returns the option flag indicating that example data files should be generated.
	 * 
	 * @return boolean
	 */
	public boolean isGenerateExamples();
	
	/**
	 * Returns true if the maximum amount of detail is to be included in generated example data.  If
	 * false, minimum detail will be generated.
	 * 
	 * @return boolean
	 */
	public boolean isGenerateMaxDetailsForExamples();
	
	/**
	 * Returns the preferred context to use when producing example values for simple data types.
	 * 
	 * @return String
	 */
	public String getExampleContext();
	
	/**
	 * Returns the maximum number of times that repeating elements should be displayed in generated example output.
	 * 
	 * @return Integer
	 */
	public Integer getExampleMaxRepeat();
	
	/**
	 * Returns the maximum depth that should be included for nested elements in generated example output.
	 * 
	 * @return Integer
	 */
	public Integer getExampleMaxDepth();
	
}
