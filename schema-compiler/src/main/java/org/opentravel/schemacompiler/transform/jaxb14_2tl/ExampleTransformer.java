
package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Example;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Example</code> type to the
 * <code>TLExample</code> type.
 *
 * @author S. Livezey
 */
public class ExampleTransformer extends BaseTransformer<Example,TLExample,DefaultTransformerContext> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLExample transform(Example source) {
		TLExample example = new TLExample();
		
		example.setContext( source.getContext() );
		example.setValue( source.getValue() );
		return example;
	}
	
}
