
package org.opentravel.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.EnumValue;
import org.opentravel.ns.ota2.librarymodel_v01_04.EnumerationClosed;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLClosedEnumeration</code> type to the
 * <code>EnumerationClosed</code> type.
 *
 * @author S. Livezey
 */
public class TLClosedEnumerationTransformer extends BaseTransformer<TLClosedEnumeration,EnumerationClosed,SymbolResolverTransformerContext> {
	
	@Override
	public EnumerationClosed transform(TLClosedEnumeration source) {
		ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
		ObjectTransformer<TLEnumValue,EnumValue,SymbolResolverTransformerContext> valueTransformer =
				getTransformerFactory().getTransformer(TLEnumValue.class, EnumValue.class);
		EnumerationClosed enumType = new EnumerationClosed();
		
		enumType.setName( trimString(source.getName(), false) );
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			enumType.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (TLEnumValue modelValue : source.getValues()) {
			enumType.getValue().add( valueTransformer.transform(modelValue) );
		}
		return enumType;
	}
	
}
