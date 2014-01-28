
package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Attribute;
import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Example;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Attribute</code> type to the
 * <code>TLAttribute</code> type.
 *
 * @author S. Livezey
 */
public class AttributeTransformer extends BaseTransformer<Attribute,TLAttribute,DefaultTransformerContext> {
	
	@Override
	public TLAttribute transform(Attribute source) {
		ObjectTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(Equivalent.class, TLEquivalent.class);
		ObjectTransformer<Example,TLExample,DefaultTransformerContext> exampleTransformer =
				getTransformerFactory().getTransformer(Example.class, TLExample.class);
		String attributeTypeName = source.getType();
		final TLAttribute attribute = new TLAttribute();
		
		attribute.setName( trimString(source.getName()) );
		attribute.setMandatory( (source.isMandatory() == null) ? false : source.isMandatory().booleanValue() );
		attribute.setTypeName( trimString(attributeTypeName) );
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			attribute.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			attribute.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		
		for (Example sourceExample : source.getExample()) {
			attribute.addExample( exampleTransformer.transform(sourceExample) );
		}
		
		return attribute;
	}
	
}
