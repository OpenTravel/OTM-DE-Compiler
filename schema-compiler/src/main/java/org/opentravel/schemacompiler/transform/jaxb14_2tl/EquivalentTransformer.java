/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
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
		
		equiv.setContext(source.getContext());
		equiv.setDescription(source.getValue());
		return equiv;
	}
	
}
