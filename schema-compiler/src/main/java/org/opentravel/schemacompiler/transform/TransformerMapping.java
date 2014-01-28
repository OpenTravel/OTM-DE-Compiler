
package org.opentravel.schemacompiler.transform;

/**
 * Specifies the transformer to be used for conversion from one type of Java class to another.
 * 
 * @author S. Livezey
 */
public class TransformerMapping {
	
	private Class<?> source;
	private Class<?> target;
	private Class<?> transformer;
	
	/**
	 * Returns the source class of the transformation.
	 *
	 * @return Class<?>
	 */
	public Class<?> getSource() {
		return source;
	}
	
	/**
	 * Assigns the source class of the transformation.
	 *
	 * @param source  the source object type
	 */
	public void setSource(Class<?> sourceClass) {
		this.source = sourceClass;
	}
	
	/**
	 * Returns the target class of the transformation.
	 *
	 * @return Class<?>
	 */
	public Class<?> getTarget() {
		return target;
	}
	
	/**
	 * Assigns the target class of the transformation.
	 *
	 * @param target  the target object
	 */
	public void setTarget(Class<?> targetClass) {
		this.target = targetClass;
	}
	
	/**
	 * Returns class that will handle the transformation from the source type to the target type.
	 *
	 * @return Class<?>
	 */
	public Class<?> getTransformer() {
		return transformer;
	}
	
	/**
	 * Assigns class that will handle the transformation from the source type to the target type.
	 *
	 * @param transformer  the name of the class that will perform the transformation
	 */
	public void setTransformer(Class<?> transformerClass) {
		this.transformer = transformerClass;
	}
	
}
