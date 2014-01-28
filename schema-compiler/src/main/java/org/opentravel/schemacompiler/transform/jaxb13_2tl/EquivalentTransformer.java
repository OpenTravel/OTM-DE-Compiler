
package org.opentravel.schemacompiler.transform.jaxb13_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_03.Equivalent;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Equivalent</code> type to the
 * <code>TLEquivalent</code> type.
 *
 * @author S. Livezey
 */
public class EquivalentTransformer extends BaseTransformer<Equivalent,TLEquivalent,DefaultTransformerContext> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLEquivalent transform(Equivalent source) {
		TLEquivalent equiv = new TLEquivalent();
		
		if ((source.getContext() != null) && (source.getContext().length() > 0)) {
			equiv.setContext(source.getContext());
		} else {
			equiv.setContext(LibraryTransformer.DEFAULT_CONTEXT_ID);
		}
		equiv.setDescription(source.getValue());
		return equiv;
	}
	
}
