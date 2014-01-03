/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.transform.jaxb2xsd;

import java.util.Set;

import org.w3._2001.xmlschema.OpenAttrs;
import org.w3._2001.xmlschema.Schema;

import com.sabre.schemacompiler.loader.LibraryModuleImport;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLInclude;
import com.sabre.schemacompiler.model.XSDLibrary;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.symbols.DefaultTransformerContext;
import com.sabre.schemacompiler.transform.util.BaseTransformer;
import com.sabre.schemacompiler.transform.util.SchemaUtils;
import com.sabre.schemacompiler.version.XSDVersionScheme;

/**
 * Handles the transformation of objects from the <code>Schema</code> type to the
 * <code>XSDLibrary</code> type.
 *
 * @author S. Livezey
 */
public class SchemaTransformer extends BaseTransformer<Schema,XSDLibrary,DefaultTransformerContext> {

	/**
	 * @see com.sabre.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public XSDLibrary transform(Schema source) {
		XSDLibrary target = new XSDLibrary();
		
		target.setVersionScheme(XSDVersionScheme.ID);
		target.setNamespace(source.getTargetNamespace());
		target.setPrefix(source.getId()); // prefix is stored in the ID field by the LibrarySchema1_3_ModuleLoader
		
		for (String _include : SchemaUtils.getSchemaIncludes(source)) {
			TLInclude include = new TLInclude();
			
			include.setPath(_include);
			target.addInclude(include);
		}
		for (LibraryModuleImport nsImport : SchemaUtils.getSchemaImports(source)) {
			String[] fileHints = null;
			
			if (nsImport.getFileHints() != null) {
				fileHints = nsImport.getFileHints().toArray( new String[nsImport.getFileHints().size()] );
			}
			target.addNamespaceImport( trimString(nsImport.getPrefix()),
					trimString(nsImport.getNamespace()), fileHints);
		}
		
		for (OpenAttrs sourceMember : source.getSimpleTypeOrComplexTypeOrGroup()) {
			Set<Class<?>> targetTypes = getTransformerFactory().findTargetTypes(sourceMember);
			Class<LibraryMember> targetType = (Class<LibraryMember>)
					((targetTypes.size() == 0) ? null : targetTypes.iterator().next());
			
			if (targetType != null) {
				ObjectTransformer<OpenAttrs,LibraryMember,DefaultTransformerContext> memberTransformer =
						getTransformerFactory().getTransformer(sourceMember, targetType);
				
				if (memberTransformer != null) {
					target.addNamedMember( memberTransformer.transform(sourceMember) );
				}
			}
		}
		return target;
	}
	
}
