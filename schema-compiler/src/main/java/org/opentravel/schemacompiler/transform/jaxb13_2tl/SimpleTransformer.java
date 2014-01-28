
package org.opentravel.schemacompiler.transform.jaxb13_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_03.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_03.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_03.Simple;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Simple</code> type to the
 * <code>TLSimple</code> type.
 *
 * @author S. Livezey
 */
public class SimpleTransformer extends BaseTransformer<Simple,TLSimple,SymbolResolverTransformerContext> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLSimple transform(Simple source) {
		ObjectTransformer<Equivalent,TLEquivalent,SymbolResolverTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(Equivalent.class, TLEquivalent.class);
		String exampleValue = trimString(source.getEx());
		final TLSimple simpleType = new TLSimple();
		
		simpleType.setName( trimString(source.getName()) );
		simpleType.setPattern( trimString(source.getPattern()) );
		simpleType.setMinLength( (source.getMinLength() == null) ? -1 : source.getMinLength().intValue() );
		simpleType.setMaxLength( (source.getMaxLength() == null) ? -1 : source.getMaxLength().intValue() );
		simpleType.setParentTypeName( trimString(source.getType() ));
		
		if (exampleValue != null) {
			TLExample example = new TLExample();
			
			example.setContext(LibraryTransformer.DEFAULT_CONTEXT_ID);
			example.setValue(exampleValue);
			simpleType.addExample(example);
		}
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			simpleType.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (Equivalent sourceEquiv : source.getEquivalent()) {
			simpleType.addEquivalent( equivTransformer.transform(sourceEquiv) );
		}
		
		return simpleType;
	}
	
}
