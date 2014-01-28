
package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Example;
import org.opentravel.ns.ota2.librarymodel_v01_04.ValueWithAttributes;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>ValueWithAttributes</code> type to the
 * <code>TLValueWithAttributes</code> type.
 *
 * @author S. Livezey
 */
public class ValueWithAttributesTransformer extends ComplexTypeTransformer<ValueWithAttributes,TLValueWithAttributes> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLValueWithAttributes transform(ValueWithAttributes source) {
		ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
		ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(Equivalent.class, TLEquivalent.class);
		ObjectTransformer<Example,TLExample,DefaultTransformerContext> exampleTransformer =
				getTransformerFactory().getTransformer(Example.class, TLExample.class);
		final TLValueWithAttributes simpleType = new TLValueWithAttributes();
		
		simpleType.setName( trimString(source.getName()) );
		simpleType.setParentTypeName( trimString(source.getType()) );
		
		if (source.getDocumentation() != null) {
			simpleType.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		if (source.getValueDocumentation() != null) {
			simpleType.setValueDocumentation( docTransformer.transform(source.getValueDocumentation()) );
		}
		
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			simpleType.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		
		for (Example sourceExample : source.getExample()) {
			simpleType.addExample( exampleTransformer.transform(sourceExample) );
		}
		
		for (TLAttribute attribute : transformAttributes(source.getAttribute())) {
			simpleType.addAttribute(attribute);
		}
		
		for (TLIndicator indicator : transformIndicators(source.getIndicator())) {
			simpleType.addIndicator(indicator);
		}
		
		return simpleType;
	}
	
}
