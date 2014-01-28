
package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_04.Extension;
import org.opentravel.ns.ota2.librarymodel_v01_04.ExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;

/**
 * Handles the transformation of objects from the <code>ExtensionPointFacet</code> type to the
 * <code>TLExtensionPointFacet</code> type.
 *
 * @author S. Livezey
 */
public class ExtensionPointFacetTransformer extends ComplexTypeTransformer<ExtensionPointFacet,TLExtensionPointFacet> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLExtensionPointFacet transform(ExtensionPointFacet source) {
		TLExtensionPointFacet facet = new TLExtensionPointFacet();
		
		if (source.getExtension() != null) {
			ObjectTransformer<Extension,TLExtension,DefaultTransformerContext> extensionTransformer =
					getTransformerFactory().getTransformer(Extension.class, TLExtension.class);
			
			facet.setExtension( extensionTransformer.transform(source.getExtension()) );
		}
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
					getTransformerFactory().getTransformer(Documentation.class, TLDocumentation.class);
			
			facet.setDocumentation( docTransformer.transform(source.getDocumentation()) );
		}
		
		for (TLAttribute attribute : transformAttributes(source.getAttribute())) {
			facet.addAttribute(attribute);
		}
		for (TLProperty element : transformElements(source.getElement())) {
			facet.addElement(element);
		}
		for (TLIndicator indicator : transformIndicators(source.getIndicator())) {
			facet.addIndicator(indicator);
		}
		
		return facet;
	}
	
}
