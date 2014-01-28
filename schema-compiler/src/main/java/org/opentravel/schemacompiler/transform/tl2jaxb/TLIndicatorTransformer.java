
package org.opentravel.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Indicator;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLIndicator</code> type to the
 * <code>Indicator</code> type.
 *
 * @author S. Livezey
 */
public class TLIndicatorTransformer extends BaseTransformer<TLIndicator,Indicator,SymbolResolverTransformerContext> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public Indicator transform(TLIndicator source) {
		ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(TLEquivalent.class, Equivalent.class);
		Indicator indicator = new Indicator();
		
		indicator.setName( trimString(source.getName(), false) );
		indicator.setPublishAsElement( source.isPublishAsElement() );
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
			
			indicator.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		for (TLEquivalent sourceEquiv : source.getEquivalents()) {
			indicator.getEquivalent().add( equivTransformer.transform(sourceEquiv) );
		}
		return indicator;
	}
	
}
