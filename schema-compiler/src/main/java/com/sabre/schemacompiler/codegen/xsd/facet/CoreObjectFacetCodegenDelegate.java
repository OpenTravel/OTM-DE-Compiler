/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd.facet;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3._2001.xmlschema.Annotated;
import org.w3._2001.xmlschema.Attribute;

import com.sabre.schemacompiler.codegen.util.FacetCodegenUtils;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLFacet;

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
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacet()
	 */
	@Override
	public TLFacet getLocalBaseFacet() {
		return null;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getExtensionPointElement()
	 */
	@Override
	public QName getExtensionPointElement() {
		return null;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createJaxbAttributes()
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
