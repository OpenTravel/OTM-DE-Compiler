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

package org.opentravel.schemacompiler.codegen.xsd.facet;

import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.model.TLAbstractFacet;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLSimpleFacet;

/**
 * Factory used to determine which <code>FacetCodegenDelegate</code> should be used to generate artifacts for a
 * particular facet instance.
 * 
 * @author S. Livezey
 */
public class FacetCodegenDelegateFactory {

    protected CodeGenerationTransformerContext transformerContext;

    /**
     * Constructor that supplies the transformer context to be used by the code generation delegates produced by this
     * factory instance.
     * 
     * @param transformerContext the transformer context for the current code generation task
     */
    public FacetCodegenDelegateFactory(CodeGenerationTransformerContext transformerContext) {
        this.transformerContext = transformerContext;
    }

    /**
     * Returns a <code>FacetCodegenDelegate</code> to use for the generation of code artifacts from the given facet
     * instance.
     * 
     * @param <F> the type of facet for which the delegate will generate artifacts
     * @param facetInstance the facet instance for which to generate code artifacts
     * @return FacetCodegenDelegate&lt;F&gt;
     */
    @SuppressWarnings("unchecked")
    public <F extends TLAbstractFacet> FacetCodegenDelegate<F> getDelegate(F facetInstance) {
        TLFacetOwner facetOwner = facetInstance.getOwningEntity();
        FacetCodegenDelegate<F> delegate = null;

        if (facetOwner instanceof TLBusinessObject) {
            delegate = getBusinessObjectFacet( facetInstance );

        } else if (facetOwner instanceof TLCoreObject) {
            delegate = getCoreObjectDelegate( facetInstance );

        } else if (facetOwner instanceof TLChoiceObject) {
            delegate = getChoiceObjectFacet( facetInstance );

        } else if (facetOwner instanceof TLContextualFacet) {
            TLContextualFacet facet = (TLContextualFacet) facetInstance;

            switch (facetInstance.getFacetType()) {
                case CUSTOM:
                    delegate = (FacetCodegenDelegate<F>) new BusinessObjectCustomFacetCodegenDelegate( facet );
                    break;
                case QUERY:
                    delegate = (FacetCodegenDelegate<F>) new BusinessObjectQueryFacetCodegenDelegate( facet );
                    break;
                case UPDATE:
                    delegate = (FacetCodegenDelegate<F>) new BusinessObjectUpdateFacetCodegenDelegate( facet );
                    break;
                case CHOICE:
                    delegate = (FacetCodegenDelegate<F>) new ChoiceObjectChoiceFacetCodegenDelegate( facet );
                    break;
                default:
                    break;
            }

        } else if ((facetOwner instanceof TLOperation) && (facetInstance instanceof TLFacet)) {
            delegate = (FacetCodegenDelegate<F>) new OperationFacetCodegenDelegate( (TLFacet) facetInstance );
        }

        if (delegate != null) {
            delegate.setTransformerContext( transformerContext );
        }
        return delegate;
    }

    /**
     * Returns a delegate for the given core object facet.
     * 
     * @param boFacet the business object facet instance
     * @return FacetCodegenDelegate&lt;F&gt;
     */
    @SuppressWarnings("unchecked")
    private <F extends TLAbstractFacet> FacetCodegenDelegate<F> getBusinessObjectFacet(F boFacet) {
        FacetCodegenDelegate<F> delegate = null;

        if (boFacet instanceof TLFacet) {
            TLFacet facet = (TLFacet) boFacet;

            switch (boFacet.getFacetType()) {
                case ID:
                    delegate = (FacetCodegenDelegate<F>) new BusinessObjectIDFacetCodegenDelegate( facet );
                    break;
                case SUMMARY:
                    delegate = (FacetCodegenDelegate<F>) new BusinessObjectSummaryFacetCodegenDelegate( facet );
                    break;
                case DETAIL:
                    delegate = (FacetCodegenDelegate<F>) new BusinessObjectDetailFacetCodegenDelegate( facet );
                    break;
                case CUSTOM:
                    delegate = (FacetCodegenDelegate<F>) new BusinessObjectCustomFacetCodegenDelegate( facet );
                    break;
                case QUERY:
                    delegate = (FacetCodegenDelegate<F>) new BusinessObjectQueryFacetCodegenDelegate( facet );
                    break;
                case UPDATE:
                    delegate = (FacetCodegenDelegate<F>) new BusinessObjectUpdateFacetCodegenDelegate( facet );
                    break;
                default:
                    break;
            }
        }
        return delegate;
    }

    /**
     * Returns a delegate for the given core object facet.
     * 
     * @param coreFacet the core object facet for which to return a delegate
     * @return FacetCodegenDelegate&lt;F&gt;
     */
    @SuppressWarnings("unchecked")
    private <F extends TLAbstractFacet> FacetCodegenDelegate<F> getCoreObjectDelegate(F coreFacet) {
        FacetCodegenDelegate<F> delegate = null;

        if (coreFacet instanceof TLFacet) {
            TLFacet facet = (TLFacet) coreFacet;

            switch (coreFacet.getFacetType()) {
                case SUMMARY:
                    delegate = (FacetCodegenDelegate<F>) new CoreObjectSummaryFacetCodegenDelegate( facet );
                    break;
                case DETAIL:
                    delegate = (FacetCodegenDelegate<F>) new CoreObjectDetailFacetCodegenDelegate( facet );
                    break;
                default:
                    break;
            }
        } else if (coreFacet instanceof TLListFacet) {
            TLListFacet facet = (TLListFacet) coreFacet;

            switch (coreFacet.getFacetType()) {
                case SIMPLE:
                    delegate = (FacetCodegenDelegate<F>) new CoreObjectListSimpleFacetCodegenDelegate( facet );
                    break;
                case SUMMARY:
                case DETAIL:
                    delegate = (FacetCodegenDelegate<F>) new CoreObjectListFacetCodegenDelegate( facet );
                    break;
                default:
                    break;
            }
        } else if (coreFacet instanceof TLSimpleFacet) {
            TLSimpleFacet facet = (TLSimpleFacet) coreFacet;

            if (coreFacet.getFacetType() == TLFacetType.SIMPLE) {
                delegate = (FacetCodegenDelegate<F>) new TLSimpleFacetCodegenDelegate( facet );
            }
        }
        return delegate;
    }

    /**
     * Returns a delegate for the given choice object facet.
     * 
     * @param choiceFacet the choice object facet instance
     * @return FacetCodegenDelegate&lt;F&gt;
     */
    @SuppressWarnings("unchecked")
    private <F extends TLAbstractFacet> FacetCodegenDelegate<F> getChoiceObjectFacet(F choiceFacet) {
        FacetCodegenDelegate<F> delegate = null;

        if (choiceFacet instanceof TLFacet) {
            TLFacet facet = (TLFacet) choiceFacet;

            switch (choiceFacet.getFacetType()) {
                case SHARED:
                    delegate = (FacetCodegenDelegate<F>) new ChoiceObjectSharedFacetCodegenDelegate( facet );
                    break;
                case CHOICE:
                    delegate = (FacetCodegenDelegate<F>) new ChoiceObjectChoiceFacetCodegenDelegate( facet );
                    break;
                default:
                    break;
            }
        }
        return delegate;
    }

}
