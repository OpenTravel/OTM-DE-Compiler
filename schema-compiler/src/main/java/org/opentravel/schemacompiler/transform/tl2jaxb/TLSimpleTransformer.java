
package org.opentravel.schemacompiler.transform.tl2jaxb;

import java.math.BigInteger;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_04.Example;
import org.opentravel.ns.ota2.librarymodel_v01_04.Simple;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>TLSimple</code> type to the
 * <code>Simple</code> type.
 *
 * @author S. Livezey
 */
public class TLSimpleTransformer extends BaseTransformer<TLSimple,Simple,SymbolResolverTransformerContext> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public Simple transform(TLSimple source) {
		ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
				getTransformerFactory().getTransformer(TLEquivalent.class, Equivalent.class);
		ObjectTransformer<TLExample,Example,SymbolResolverTransformerContext> exTransformer =
				getTransformerFactory().getTransformer(TLExample.class, Example.class);
		NamedEntity parentType = source.getParentType();
		Simple simpleType = new Simple();
		
		simpleType.setName( trimString(source.getName(), false) );
		simpleType.setPattern( trimString(source.getPattern()) );
		simpleType.setMinInclusive( trimString(source.getMinInclusive()) );
		simpleType.setMaxInclusive( trimString(source.getMaxInclusive()) );
		simpleType.setMinExclusive( trimString(source.getMinExclusive()) );
		simpleType.setMaxExclusive( trimString(source.getMaxExclusive()) );
		simpleType.setListTypeInd(source.isListTypeInd() ? Boolean.TRUE : null);
		
		if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
			ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(TLDocumentation.class, Documentation.class);
			
			simpleType.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (TLEquivalent sourceEquiv : source.getEquivalents()) {
			simpleType.getEquivalent().add( equivTransformer.transform(sourceEquiv) );
		}
		
		for (TLExample sourceEx : source.getExamples()) {
			simpleType.getExample().add( exTransformer.transform(sourceEx) );
		}
		
		if (source.getMinLength() > 0) {
			simpleType.setMinLength(BigInteger.valueOf(source.getMinLength()));
		}
		if (source.getMaxLength() > 0) {
			simpleType.setMaxLength(BigInteger.valueOf(source.getMaxLength()));
		}
		if (source.getFractionDigits() >= 0) {
			simpleType.setFractionDigits(BigInteger.valueOf(source.getFractionDigits()));
		}
		if (source.getTotalDigits() > 0) {
			simpleType.setTotalDigits(BigInteger.valueOf(source.getTotalDigits()));
		}
		
		if (parentType != null) {
			simpleType.setType( context.getSymbolResolver().buildEntityName(
					parentType.getNamespace(), parentType.getLocalName()) );
		} else {
			simpleType.setType( trimString(source.getParentTypeName(), false) );
		}
		return simpleType;
	}
	
}
