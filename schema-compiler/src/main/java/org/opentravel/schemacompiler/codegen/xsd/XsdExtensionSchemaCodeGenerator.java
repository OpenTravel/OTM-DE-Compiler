
package org.opentravel.schemacompiler.codegen.xsd;

import java.io.File;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.impl.LegacySchemaExtensionFilenameBuilder;
import org.opentravel.schemacompiler.codegen.impl.LibraryFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.XSDComplexType;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.w3._2001.xmlschema.Element;
import org.w3._2001.xmlschema.FormChoice;
import org.w3._2001.xmlschema.Include;
import org.w3._2001.xmlschema.Schema;
import org.w3._2001.xmlschema.TopLevelElement;

/**
 * Code generator implementation used to generate supplemental XSD companion documents for legacy
 * schemas.
 * 
 * <p>The following context variable(s) are required when invoking this code generation module:
 * <ul>
 *   <li><code>schemacompiler.OutputFolder</code> - the folder where generated XSD files should be stored</li>
 *   <li><code>schemacompiler.SchemaFilename</code> - the name of the XSD schema file to be generated (uses library name/version if not specified)</li>
 * </ul>
 * 
 * @author S. Livezey
 */
public class XsdExtensionSchemaCodeGenerator extends AbstractXsdCodeGenerator<XSDLibrary> {

	private static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema";
	
	protected static JAXBContext jaxbContext;
	
	private CodeGenerationFilenameBuilder<XSDLibrary> legacySchemaFilenameBuilder = new LibraryFilenameBuilder<XSDLibrary>();
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(org.opentravel.schemacompiler.model.TLModelElement)
	 */
	@Override
	protected AbstractLibrary getLibrary(XSDLibrary source) {
		return source;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.xsd.AbstractXsdCodeGenerator#canGenerateOutput(org.opentravel.schemacompiler.model.TLModelElement, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected boolean canGenerateOutput(XSDLibrary source, CodeGenerationContext context) {
		return (getFilter() == null) || getFilter().processExtendedLibrary(source);
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getMarshaller(org.opentravel.schemacompiler.model.TLModelElement, org.w3._2001.xmlschema.Schema)
	 */
	@Override
	protected Marshaller getMarshaller(XSDLibrary source, org.w3._2001.xmlschema.Schema schema) throws JAXBException {
		Marshaller m = jaxbContext.createMarshaller();
		
		m.setSchema(validationSchema);
		return m;
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
	 */
	@Override
	protected CodeGenerationFilenameBuilder<XSDLibrary> getDefaultFilenameBuilder() {
		return new LegacySchemaExtensionFilenameBuilder<XSDLibrary>(legacySchemaFilenameBuilder);
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFolder(org.opentravel.schemacompiler.codegen.CodeGenerationContext, java.net.URL)
	 */
	@Override
	protected File getOutputFolder(CodeGenerationContext context, URL libraryUrl) {
		File outputFolder = super.getOutputFolder(context, libraryUrl);
		String legacySchemaFolder = getLegacySchemaOutputLocation(context);
		
		if (legacySchemaFolder != null) {
			outputFolder = new File(outputFolder, legacySchemaFolder);
			if (!outputFolder.exists()) outputFolder.mkdirs();
		}
		return outputFolder;
	}

	/**
	 * Overrides the base class method to perform the transformation locally instead of delegating to
	 * an <code>ObjectTransformer</code> component.
	 * 
	 * @see org.opentravel.schemacompiler.codegen.xsd.AbstractXsdCodeGenerator#transformSourceObjectToJaxb(org.opentravel.schemacompiler.model.TLModelElement, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected Object transformSourceObjectToJaxb(XSDLibrary source, CodeGenerationContext context) throws CodeGenerationException {
		CodeGenerationFilter filter = getFilter();
		Schema schema = new Schema();
		Include incl = new Include();
		
		// Create the root schema element
		schema.setAttributeFormDefault(FormChoice.UNQUALIFIED);
		schema.setElementFormDefault(FormChoice.QUALIFIED);
		
		if (!AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE.equals(source.getNamespace())) {
			schema.setTargetNamespace(source.getNamespace());
		}
		
		// Declare an import for the original legacy schema that is being extended
		incl.setSchemaLocation( legacySchemaFilenameBuilder.buildFilename(source, "xsd") );
		schema.getIncludeOrImportOrRedefine().add(incl);
		
		// For each complex type that does not declare an identity element, create it for this schema
		for (LibraryMember member : source.getNamedMembers()) {
			if ((member instanceof XSDComplexType) && ((filter == null) || filter.processEntity(member))) {
				XSDComplexType complexType = (XSDComplexType) member;
				
				if (complexType.getIdentityAlias() == null) {
					Element element = new TopLevelElement();
					
					element.setName(complexType.getLocalName());
					element.setType( new QName(schema.getTargetNamespace(), complexType.getLocalName()) );
					schema.getSimpleTypeOrComplexTypeOrGroup().add( element );
				}
			}
		}
		return schema;
	}

	/**
	 * Initializes the validation schema and shared JAXB context.
	 */
	static {
		try {
			jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}
