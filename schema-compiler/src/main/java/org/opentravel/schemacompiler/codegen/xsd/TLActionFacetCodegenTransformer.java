/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemacompiler.codegen.xsd;

import java.util.List;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.DocumentationFinder;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.ChoiceObjectChoiceFacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.ChoiceObjectSharedFacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.CoreObjectDetailFacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.CoreObjectSummaryFacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenElements;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.w3._2001.xmlschema.Annotation;
import org.w3._2001.xmlschema.ComplexType;
import org.w3._2001.xmlschema.Element;
import org.w3._2001.xmlschema.ExplicitGroup;
import org.w3._2001.xmlschema.TopLevelComplexType;
import org.w3._2001.xmlschema.TopLevelElement;

/**
 * Performs the translation from <code>TLActionFacet</code> objects to the JAXB nodes used to produce
 * the schema output.
 */
public class TLActionFacetCodegenTransformer extends AbstractXsdTransformer<TLActionFacet, CodegenArtifacts> {
	
	/**
	 * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
	 */
	@Override
	public CodegenArtifacts transform(TLActionFacet source) {
		CodegenArtifacts artifacts = new CodegenArtifacts();
		NamedEntity payloadType = ResourceCodegenUtils.getPayloadType( source );
		
		if (payloadType instanceof TLActionFacet) {
			NamedEntity basePayload = source.getBasePayload();
			
			if (basePayload instanceof TLCoreObject) {
				artifacts.addAllArtifacts( generateCoreObjectWrapper(
						source, (TLCoreObject) basePayload ) );
				
			} else if (basePayload instanceof TLChoiceObject) {
				artifacts.addAllArtifacts( generateChoiceObjectWrapper(
						source, (TLChoiceObject) basePayload ) );
				
			} else { // must be a wrapper for repeating business objects
				artifacts.addAllArtifacts( generateEmptyWrapper( source ) );
			}
		}
		return artifacts;
	}
	
	/**
	 * Generates artifacts for an action facet wrapper class that is based on the
	 * specified core object.
	 * 
	 * @param source  the source action facet for which artifacts are being generated
	 * @param wrapper  the core object that will provide the basis for the wrapper artifacts
	 * @return CodegenArtifacts
	 */
	private CodegenArtifacts generateCoreObjectWrapper(TLActionFacet source, TLCoreObject wrapper) {
        FacetCodegenElements elementArtifacts = new FacetCodegenElements();
        CodegenArtifacts otherArtifacts = new CodegenArtifacts();
		
        generateFacetArtifacts(getDelegate(source, wrapper.getSummaryFacet()), elementArtifacts, otherArtifacts);
        generateFacetArtifacts(getDelegate(source, wrapper.getDetailFacet()), elementArtifacts, otherArtifacts);
        return buildCorrelatedArtifacts(wrapper, elementArtifacts, otherArtifacts);
	}
	
	/**
	 * Generates artifacts for an action facet wrapper class that is based on the
	 * specified core object.
	 * 
	 * @param source  the source action facet for which artifacts are being generated
	 * @param wrapper  the core object that will provide the basis for the wrapper artifacts
	 * @return CodegenArtifacts
	 */
	private CodegenArtifacts generateChoiceObjectWrapper(TLActionFacet source, TLChoiceObject wrapper) {
        FacetCodegenElements elementArtifacts = new FacetCodegenElements();
        CodegenArtifacts otherArtifacts = new CodegenArtifacts();
		
        generateFacetArtifacts(getDelegate(source, wrapper.getSharedFacet()), elementArtifacts, otherArtifacts);
        
        for (TLFacet choiceFacet : wrapper.getChoiceFacets()) {
            generateFacetArtifacts(getDelegate(source, choiceFacet), elementArtifacts, otherArtifacts);
        }
        for (TLFacet ghostFacet : FacetCodegenUtils.findGhostFacets(wrapper, TLFacetType.CHOICE)) {
            generateFacetArtifacts(getDelegate(source, ghostFacet), elementArtifacts, otherArtifacts);
        }
        return buildCorrelatedArtifacts(wrapper, elementArtifacts, otherArtifacts);
	}
	
	/**
	 * Generates artifacts for an action facet wrapper class that that only includes a
	 * repeating business object element.
	 * 
	 * @param source  the source action facet for which artifacts are being generated
	 * @return CodegenArtifacts
	 */
	private CodegenArtifacts generateEmptyWrapper(TLActionFacet source) {
        CodegenArtifacts artifacts = new CodegenArtifacts();
        
        // Construct the wrapper element
        Element element = new TopLevelElement();

        element.setName( XsdCodegenUtils.getGlobalElementName( source ).getLocalPart() );
        element.setType(new QName(source.getNamespace(), XsdCodegenUtils.getGlobalTypeName( source )));
        artifacts.addArtifact( element );
        
        // Construct the wrapper type
        ObjectTransformer<TLProperty, TopLevelElement, CodeGenerationTransformerContext> elementTransformer =
        		getTransformerFactory().getTransformer(TLProperty.class, TopLevelElement.class);
        TLDocumentation sourceDoc = DocumentationFinder.getDocumentation( source );
        TLProperty boElement = ResourceCodegenUtils.createBusinessObjectElement( source, null );
        ExplicitGroup sequence = new ExplicitGroup();
        ComplexType type = new TopLevelComplexType();
		
        type.setName( XsdCodegenUtils.getGlobalTypeName( source ) );
        XsdCodegenUtils.addAppInfo( source, type );
        type.setSequence( sequence );
        
        if (sourceDoc != null) {
            ObjectTransformer<TLDocumentation, Annotation, CodeGenerationTransformerContext> docTransformer =
            		getTransformerFactory().getTransformer(sourceDoc, Annotation.class);
            type.setAnnotation(docTransformer.transform(sourceDoc));
        }
        sequence.getParticle().add(
                jaxbObjectFactory.createElement( elementTransformer.transform( boElement ) ) );
        artifacts.addArtifact( type );
        
        return artifacts;
	}
	
	/**
	 * Returns a <code>FacetCodegenDelegate</code> to generate schema artifacts for
	 * the given facet.
	 * 
	 * @param source  the source action facet for which artifacts are being generated
	 * @param facet  the facet for which schema artifacts are to be generated
	 * @return FacetCodegenDelegate<TLFacet>
	 */
	private FacetCodegenDelegate<TLFacet> getDelegate(TLActionFacet source, TLFacet facet) {
        TLFacetOwner facetOwner = facet.getOwningEntity();
        FacetCodegenDelegate<TLFacet> delegate = null;
        
        if (facetOwner instanceof TLCoreObject) {
        	switch (facet.getFacetType()) {
        		case SUMMARY:
        			delegate = new CoreObjectSummaryFacetWrapperDelegate( source, facet );
        			break;
        		case DETAIL:
        			delegate = new CoreObjectDetailFacetWrapperDelegate( source, facet );
        			break;
        		default:
        			break;
        	}
        } else if (facetOwner instanceof TLChoiceObject) {
        	switch (facet.getFacetType()) {
        		case SHARED:
        			delegate = new ChoiceObjectSharedFacetWrapperDelegate( source, facet );
        			break;
        		case CHOICE:
        			delegate = new ChoiceObjectChoiceFacetWrapperDelegate( source, facet );
        			break;
        		default:
        			break;
        	}
        }
        
        if (delegate != null) {
        	delegate.setTransformerContext( context );
        }
        return delegate;
	}
	
    /**
     * Utility method that generates both element and non-element schema content for the source
     * facet of the given delegate.
     * 
     * @param facetDelegate  the facet code generation delegate
     * @param elementArtifacts  the container for all generated schema elements
     * @param otherArtifacts  the container for all generated non-element schema artifacts
     */
    private void generateFacetArtifacts(FacetCodegenDelegate<TLFacet> facetDelegate,
            FacetCodegenElements elementArtifacts, CodegenArtifacts otherArtifacts) {
        elementArtifacts.addAll(facetDelegate.generateElements());
        otherArtifacts.addAllArtifacts(facetDelegate.generateArtifacts());
    }

	/**
	 * Delegate used to generate wrapper class elements for core object summary facets.
	 */
	private class CoreObjectSummaryFacetWrapperDelegate extends CoreObjectSummaryFacetCodegenDelegate {
		
		private TLActionFacet actionFacet;
		
		/**
		 * Constructor that specifies the source facet for the delegate.
		 * 
		 * @param source  the source action facet for which artifacts are being generated
		 * @param sourceFacet  the source facet for which schema artifacts will be generated
		 */
		public CoreObjectSummaryFacetWrapperDelegate(TLActionFacet actionFacet, TLFacet sourceFacet) {
			super(sourceFacet);
			this.actionFacet = actionFacet;
		}
		
		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getElementName(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		public String getElementName(TLAlias facetAlias) {
			return XsdCodegenUtils.getGlobalElementName( actionFacet ).getLocalPart();
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getTypeName()
		 */
		@Override
		protected String getTypeName() {
			return XsdCodegenUtils.getGlobalTypeName( actionFacet );
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.CoreObjectFacetCodegenDelegate#createRoleAttributes()
		 */
		@Override
		protected boolean createRoleAttributes() {
			return false;
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getElements()
		 */
		@Override
		public List<TLProperty> getElements() {
			List<TLProperty> elementList = super.getElements();
			
			elementList.add( 0, ResourceCodegenUtils.createBusinessObjectElement( actionFacet, getSourceFacet() ) );
			return elementList;
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createSubstitutionGroupElement(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		protected Element createSubstitutionGroupElement(TLAlias ownerAlias) {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createNonSubstitutableElement(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		protected Element createNonSubstitutableElement(TLAlias facetAlias) {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#createElement(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		protected Element createElement(TLAlias facetAlias) {
			Element element = null;
			
			// Suppress generation of alias elements
			if (facetAlias == null) {
		    	element = super.createNonSubstitutableElement( null );
		    	element.setName( getElementName( null ) );
		    	element.setSubstitutionGroup( null );
		    	element.setType( new QName( actionFacet.getNamespace(), getTypeName() ) );
			}
			return element;
		}
		
	}
	
	/**
	 * Delegate used to generate wrapper class elements for core object detail facets.
	 */
	private class CoreObjectDetailFacetWrapperDelegate extends CoreObjectDetailFacetCodegenDelegate {
		
		private TLActionFacet actionFacet;
		
		/**
		 * Constructor that specifies the source facet for the delegate.
		 * 
		 * @param source  the source action facet for which artifacts are being generated
		 * @param sourceFacet  the source facet for which schema artifacts will be generated
		 */
		public CoreObjectDetailFacetWrapperDelegate(TLActionFacet actionFacet, TLFacet sourceFacet) {
			super(sourceFacet);
			this.actionFacet = actionFacet;
		}
		
		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getElementName(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		public String getElementName(TLAlias facetAlias) {
			return XsdCodegenUtils.getPayloadElementName( actionFacet, getSourceFacet() ).getLocalPart();
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getTypeName()
		 */
		@Override
		protected String getTypeName() {
			return XsdCodegenUtils.getGlobalTypeName( actionFacet ) + "_Detail";
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacetTypeName()
		 */
		@Override
		protected QName getLocalBaseFacetTypeName() {
			return new QName( actionFacet.getNamespace(), XsdCodegenUtils.getGlobalTypeName( actionFacet ) );
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.CoreObjectFacetCodegenDelegate#createRoleAttributes()
		 */
		@Override
		protected boolean createRoleAttributes() {
			return false;
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createSubstitutionGroupElement(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		protected Element createSubstitutionGroupElement(TLAlias ownerAlias) {
			return null;
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createNonSubstitutableElement(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		protected Element createNonSubstitutableElement(TLAlias facetAlias) {
			// Suppress generation of alias elements
			return (facetAlias != null) ? null : super.createNonSubstitutableElement(facetAlias);
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#createElement(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		protected Element createElement(TLAlias facetAlias) {
			Element element = null;
			
			// Suppress generation of alias elements
			if (facetAlias == null) {
		    	element = super.createNonSubstitutableElement( null );
		    	element.setName( getElementName( null ) );
		    	element.setSubstitutionGroup( XsdCodegenUtils.getSubstitutionGroupElementName( actionFacet ) );
		    	element.setType( new QName( actionFacet.getNamespace(), getTypeName() ) );
			}
	    	return element;
		}
		
	}
	
	/**
	 * Delegate used to generate wrapper class elements for choice object shared facets.
	 */
	private class ChoiceObjectSharedFacetWrapperDelegate extends ChoiceObjectSharedFacetCodegenDelegate {
		
		private TLActionFacet actionFacet;
		
		/**
		 * Constructor that specifies the source facet for the delegate.
		 * 
		 * @param source  the source action facet for which artifacts are being generated
		 * @param sourceFacet  the source facet for which schema artifacts will be generated
		 */
		public ChoiceObjectSharedFacetWrapperDelegate(TLActionFacet actionFacet, TLFacet sourceFacet) {
			super(sourceFacet);
			this.actionFacet = actionFacet;
		}
		
		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getElementName(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		public String getElementName(TLAlias facetAlias) {
			return XsdCodegenUtils.getGlobalElementName( actionFacet ).getLocalPart();
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getTypeName()
		 */
		@Override
		protected String getTypeName() {
			return XsdCodegenUtils.getGlobalTypeName( actionFacet );
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getElements()
		 */
		@Override
		public List<TLProperty> getElements() {
			List<TLProperty> elementList = super.getElements();
			
			elementList.add( 0, ResourceCodegenUtils.createBusinessObjectElement( actionFacet, getSourceFacet() ) );
			return elementList;
		}
		
		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createSubstitutionGroupElement(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		protected Element createSubstitutionGroupElement(TLAlias ownerAlias) {
			Element element = null;
			
			// Suppress generation of alias elements
			if (ownerAlias == null) {
		    	element = super.createSubstitutionGroupElement( ownerAlias );
		    	element.setName( XsdCodegenUtils.getSubstitutionGroupElementName( actionFacet ).getLocalPart() );
		    	element.setType( new QName( actionFacet.getNamespace(), getTypeName() ) );
			}
	    	return element;
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createNonSubstitutableElement(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		protected Element createNonSubstitutableElement(TLAlias facetAlias) {
			Element element = null;
			
			// Suppress generation of alias elements
			if (facetAlias == null) {
		    	element = super.createNonSubstitutableElement( null );
		    	element.setName( getElementName( null ) );
		    	element.setSubstitutionGroup( XsdCodegenUtils.getSubstitutionGroupElementName( actionFacet ) );
		    	element.setType( new QName( actionFacet.getNamespace(), getTypeName() ) );
			}
	    	return element;
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#createElement(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		protected Element createElement(TLAlias facetAlias) {
			// Suppress generation of alias elements
			return (facetAlias != null) ? null : super.createElement(facetAlias);
		}
		
	}
	
	/**
	 * Delegate used to generate wrapper class elements for choice object shared facets.
	 */
	private class ChoiceObjectChoiceFacetWrapperDelegate extends ChoiceObjectChoiceFacetCodegenDelegate {
		
		private TLActionFacet actionFacet;
		
		/**
		 * Constructor that specifies the source facet for the delegate.
		 * 
		 * @param source  the source action facet for which artifacts are being generated
		 * @param sourceFacet  the source facet for which schema artifacts will be generated
		 */
		public ChoiceObjectChoiceFacetWrapperDelegate(TLActionFacet actionFacet, TLFacet sourceFacet) {
			super(sourceFacet);
			this.actionFacet = actionFacet;
		}
		
		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getElementName(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		public String getElementName(TLAlias facetAlias) {
			return XsdCodegenUtils.getPayloadElementName( actionFacet, getSourceFacet() ).getLocalPart();
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getTypeName()
		 */
		@Override
		protected String getTypeName() {
			return XsdCodegenUtils.getGlobalTypeName( actionFacet ) + "_" + getSourceFacet().getLabel();
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#getLocalBaseFacetTypeName()
		 */
		@Override
		protected QName getLocalBaseFacetTypeName() {
			return new QName( actionFacet.getNamespace(), XsdCodegenUtils.getGlobalTypeName( actionFacet ) );
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createSubstitutionGroupElement(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		protected Element createSubstitutionGroupElement(TLAlias ownerAlias) {
			// Suppress generation of alias elements
			return (ownerAlias != null) ? null : super.createSubstitutionGroupElement(ownerAlias);
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.TLFacetCodegenDelegate#createNonSubstitutableElement(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		protected Element createNonSubstitutableElement(TLAlias facetAlias) {
			// Suppress generation of alias elements
			return (facetAlias != null) ? null : super.createNonSubstitutableElement(facetAlias);
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate#createElement(org.opentravel.schemacompiler.model.TLAlias)
		 */
		@Override
		protected Element createElement(TLAlias facetAlias) {
			Element element = null;
			
			// Suppress generation of alias elements
			if (facetAlias == null) {
		    	element = super.createNonSubstitutableElement( null );
		    	element.setName( getElementName( null ) );
		    	element.setSubstitutionGroup( XsdCodegenUtils.getSubstitutionGroupElementName( actionFacet ) );
		    	element.setType( new QName( actionFacet.getNamespace(), getTypeName() ) );
			}
	    	return element;
		}
		
	}
	
}
