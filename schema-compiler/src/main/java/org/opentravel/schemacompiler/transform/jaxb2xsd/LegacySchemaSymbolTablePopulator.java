
package org.opentravel.schemacompiler.transform.jaxb2xsd;

import org.opentravel.schemacompiler.transform.SymbolTable;
import org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator;
import org.w3._2001.xmlschema.OpenAttrs;
import org.w3._2001.xmlschema.Schema;
import org.w3._2001.xmlschema.TopLevelAttribute;
import org.w3._2001.xmlschema.TopLevelComplexType;
import org.w3._2001.xmlschema.TopLevelElement;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Symbol table populator that creates named entries using the members of the JAXB
 * <code>Schema</code> instance provied.
 *
 * @author S. Livezey
 */
public class LegacySchemaSymbolTablePopulator implements SymbolTablePopulator<Schema> {

	/**
	 * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#populateSymbols(java.lang.Object, org.opentravel.schemacompiler.transform.SymbolTable)
	 */
	@Override
	public void populateSymbols(Schema sourceEntity, SymbolTable symbols) {
		String namespace = sourceEntity.getTargetNamespace();
		
		for (OpenAttrs schemaTerm : sourceEntity.getSimpleTypeOrComplexTypeOrGroup()) {
			String localName = getLocalName(schemaTerm);
			
			if (localName != null) {
				symbols.addEntity(namespace, localName, schemaTerm);
			}
		}
	}
	
	/**
	 * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#getLocalName(java.lang.Object)
	 */
	@Override
	public String getLocalName(Object sourceObject) {
		String localName = null;
		
		if (sourceObject instanceof TopLevelSimpleType) {
			localName = ((TopLevelSimpleType) sourceObject).getName();
			
		} else if (sourceObject instanceof TopLevelComplexType) {
			localName = ((TopLevelComplexType) sourceObject).getName();
			
		} else if (sourceObject instanceof TopLevelElement) {
			localName = ((TopLevelElement) sourceObject).getName();
			
		} else if (sourceObject instanceof TopLevelAttribute) {
			localName = ((TopLevelAttribute) sourceObject).getName();
		}
		if (localName != null) {
			localName = localName.trim();
		}
		return localName;
	}

	/**
	 * @see org.opentravel.schemacompiler.transform.symbols.SymbolTablePopulator#getSourceEntityType()
	 */
	@Override
	public Class<Schema> getSourceEntityType() {
		return Schema.class;
	}
	
}
