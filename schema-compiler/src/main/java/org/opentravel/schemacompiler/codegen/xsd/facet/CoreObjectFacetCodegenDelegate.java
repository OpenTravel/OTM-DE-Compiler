
package org.opentravel.schemacompiler.codegen.xsd.facet;

import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Attribute;

/**
 * Base class for facet code generation delegates used to generate code artifacts for
 * <code>TLFacet</code model elements that are owned by <code>TLCoreObject</code>
 * instances.
 *
 * @author S. Livezey
 */
public abstract class CoreObjectFacetCodegenDelegate extends TLFacetCodegenDelegate {
	
	/**
	 * Constructor that specifies the source facet for which code artifacts are being
	 * generated.
	 * 
	 * @param sourceFacet  the source facet
	 */
	public CoreObjectFacetCodegenDelegate(TLFacet sourceFacet) {
		super(sourceFacet);
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
	 */
	@Override
	public TLFacet getLocalBaseFacet() {
		return null;
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getExtensionPointElement()
	 */
	@Override
	public QName getExtensionPointElement() {
		return null;
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createJaxbAttributes()
	 */
	@Override
	protected List<Annotated> createJaxbAttributes() {
		List<Annotated> jaxbAttributes = super.createJaxbAttributes();
		
		if (getLocalBaseFacet() == null) {
			TLCoreObject owner = (TLCoreObject) getSourceFacet().getOwningEntity();
			
			while (owner != null) {
				TLCoreObject ownerExtension = (TLCoreObject) FacetCodegenUtils.getFacetOwnerExtension(owner);
				
				if (owner.getRoleEnumeration().getRoles().size() > 0) {
					Attribute roleAttr = new Attribute();
					
					if (ownerExtension != null) {
						roleAttr.setName(owner.getLocalName() + "Role");
					} else {
						roleAttr.setName("role");
					}
					roleAttr.setType( new QName(owner.getNamespace(), owner.getRoleEnumeration().getLocalName() + "_Base") );
					jaxbAttributes.add(roleAttr);
				}
				owner = ownerExtension;
			}
		}
		return jaxbAttributes;
	}

}
