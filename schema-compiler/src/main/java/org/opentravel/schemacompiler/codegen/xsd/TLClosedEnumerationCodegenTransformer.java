
package org.opentravel.schemacompiler.codegen.xsd;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.Restriction;
import org.w3._2001.xmlschema.SimpleType;
import org.w3._2001.xmlschema.TopLevelSimpleType;

/**
 * Performs the translation from <code>TLClosedEnumeration</code> objects to the JAXB nodes used
 * to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLClosedEnumerationCodegenTransformer extends TLBaseEnumerationCodegenTransformer<TLClosedEnumeration,CodegenArtifacts> {

	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLClosedEnumeration source) {
		CodegenArtifacts artifacts = new CodegenArtifacts();
		SimpleType xsdEnum = new TopLevelSimpleType();
		Restriction restriction = new Restriction();
		
		xsdEnum.setName(source.getName());
		restriction.setBase( new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string") );
		
		if (source.getDocumentation() != null) {
			ObjectTransformer<TLDocumentation,Annotation,CodeGenerationTransformerContext> docTransformer =
				getTransformerFactory().getTransformer(source.getDocumentation(), Annotation.class);
			
			xsdEnum.setAnnotation( docTransformer.transform(source.getDocumentation()) );
		}
		XsdCodegenUtils.addAppInfo(source, xsdEnum);
		
		for (TLEnumValue modelEnum : source.getValues()) {
			restriction.getFacets().add( createEnumValue(modelEnum) );
		}
		xsdEnum.setRestriction(restriction);
		artifacts.addArtifact(xsdEnum);
		
		return artifacts;
	}
	
}
