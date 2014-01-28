
package org.opentravel.schemacompiler.transform.jaxb13_2tl;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.ns.ota2.librarymodel_v01_03.AdditionalDoc;
import org.opentravel.ns.ota2.librarymodel_v01_03.Description;
import org.opentravel.ns.ota2.librarymodel_v01_03.Documentation;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Documentation</code> type to the
 * <code>TLDocumentation</code> type.
 *
 * @author S. Livezey
 */
public class DocumentationTransformer extends BaseTransformer<Documentation,TLDocumentation,DefaultTransformerContext> {

	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public TLDocumentation transform(Documentation source) {
		TLDocumentation target = new TLDocumentation();
		
		if ((source.getDescription() != null) && (source.getDescription().size() > 0)) {
			StringBuilder targetDescription = new StringBuilder();
			
			for (String str : trimDescriptionStrings(source.getDescription())) {
				if (targetDescription.length() > 0) {
					targetDescription.append("\n");
				}
				targetDescription.append(str);
			}
			target.setDescription( targetDescription.toString() );
		}
		
		for (String str : trimDescriptionStrings(source.getDeprecated())) {
			target.addDeprecation( newDocumentationItem(str) );
		}
		for (String str : trimDescriptionStrings(source.getDeveloper())) {
			target.addImplementer( newDocumentationItem(str) );
		}
		for (String str : source.getReference()) {
			if ((str = trimString(str)) != null) {
				target.addReference( newDocumentationItem(str) );
			}
		}
		for (String str : source.getMoreInfo()) {
			if ((str = trimString(str)) != null) {
				target.addMoreInfo( newDocumentationItem(str) );
			}
		}
		for (AdditionalDoc otherDoc : source.getOtherDoc()) {
			if (otherDoc != null) {
				TLAdditionalDocumentationItem targetOtherDoc = new TLAdditionalDocumentationItem();
				
				if ((otherDoc.getUse() != null) && (otherDoc.getUse().length() > 0)) {
					targetOtherDoc.setContext(otherDoc.getUse());
				} else {
					targetOtherDoc.setContext(LibraryTransformer.DEFAULT_CONTEXT_ID);
				}
				targetOtherDoc.setText(otherDoc.getValue());
				target.addOtherDoc(targetOtherDoc);
			}
		}
		return target;
	}
	
	/**
	 * Compiles a list of all description strings from the collection and returns the result after
	 * normal 'trimStrings()' processing.
	 * 
	 * @param descriptions  the list of descriptions to process
	 * @return List<String>
	 */
	private List<String> trimDescriptionStrings(List<Description> descriptions) {
		List<String> result = new ArrayList<String>();
		
		if (descriptions != null) {
			for (Description desc : descriptions) {
				result.add(desc.getValue());
			}
		}
		return trimStrings(result);
	}
	
	/**
	 * Returns a new <code>TLDocumentationItem</code> that wraps the documentation text provided.
	 * 
	 * @param text  the text of the documentation item
	 * @return TLDocumentationItem
	 */
	private TLDocumentationItem newDocumentationItem(String text) {
		TLDocumentationItem docItem = new TLDocumentationItem();
		
		docItem.setText(text);
		return docItem;
	}
	
}
