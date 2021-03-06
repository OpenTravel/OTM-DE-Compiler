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

package org.opentravel.schemacompiler.codegen.json.facet;

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
 * Factory used to determine which <code>FacetJsonSchemaDelegate</code> should be used to generate artifacts for a
 * particular facet instance.
 */
public class FacetJsonSchemaDelegateFactory {

    protected CodeGenerationTransformerContext transformerContext;

    /**
     * Constructor that supplies the transformer context to be used by the code generation delegates produced by this
     * factory instance.
     * 
     * @param transformerContext the transformer context for the current code generation task
     */
    public FacetJsonSchemaDelegateFactory(CodeGenerationTransformerContext transformerContext) {
        this.transformerContext = transformerContext;
    }

    /**
     * Returns a <code>FacetJsonSchemaDelegate</code> to use for the generation of code artifacts from the given facet
     * instance.
     * 
     * @param <F> the type of facet for which the delegate will generate artifacts
     * @param facetInstance the facet instance for which to generate code artifacts
     * @return FacetJsonSchemaDelegate&lt;F&gt;
     */
    @SuppressWarnings("unchecked")
    public <F extends TLAbstractFacet> FacetJsonSchemaDelegate<F> getDelegate(F facetInstance) {
        TLFacetOwner facetOwner = facetInstance.getOwningEntity();
        FacetJsonSchemaDelegate<F> delegate = null;

        if (facetOwner instanceof TLBusinessObject) {
            if (facetInstance.getFacetType() == TLFacetType.UPDATE) {
                delegate = (FacetJsonSchemaDelegate<
                    F>) new BusinessObjectUpdateFacetJsonSchemaDelegate( (TLFacet) facetInstance );

            } else {
                delegate = (FacetJsonSchemaDelegate<F>) new TLFacetJsonSchemaDelegate( (TLFacet) facetInstance );
            }

        } else if (facetOwner instanceof TLCoreObject) {
            delegate = getCoreObjectDelegate( facetInstance );

        } else if (facetOwner instanceof TLChoiceObject) {
            delegate = (FacetJsonSchemaDelegate<F>) new TLFacetJsonSchemaDelegate( (TLFacet) facetInstance );

        } else if (facetOwner instanceof TLContextualFacet) {
            delegate = (FacetJsonSchemaDelegate<F>) new TLFacetJsonSchemaDelegate( (TLFacet) facetInstance );

        } else if ((facetOwner instanceof TLOperation) || (facetInstance instanceof TLFacet)) {
            delegate = (FacetJsonSchemaDelegate<F>) new OperationFacetJsonSchemaDelegate( (TLFacet) facetInstance );
        }

        if (delegate != null) {
            delegate.setTransformerContext( transformerContext );
        }
        return delegate;
    }

    /**
     * Returns a delegate for the core object facet instance provided.
     * 
     * @param coreFacet the core object facet
     * @return FacetJsonSchemaDelegate&lt;F&gt;
     */
    @SuppressWarnings("unchecked")
    private <F extends TLAbstractFacet> FacetJsonSchemaDelegate<F> getCoreObjectDelegate(F coreFacet) {
        FacetJsonSchemaDelegate<F> delegate = null;

        if (coreFacet instanceof TLFacet) {
            TLFacet facet = (TLFacet) coreFacet;

            switch (coreFacet.getFacetType()) {
                case SUMMARY:
                    delegate = (FacetJsonSchemaDelegate<F>) new CoreObjectSummaryFacetJsonSchemaDelegate( facet );
                    break;
                case DETAIL:
                    delegate = (FacetJsonSchemaDelegate<F>) new CoreObjectFacetJsonSchemaDelegate( facet );
                    break;
                default:
                    break;
            }

        } else if (coreFacet instanceof TLListFacet) {
            TLListFacet facet = (TLListFacet) coreFacet;

            switch (coreFacet.getFacetType()) {
                case SIMPLE:
                    delegate = (FacetJsonSchemaDelegate<F>) new CoreObjectListSimpleFacetJsonSchemaDelegate( facet );
                    break;
                case SUMMARY:
                case DETAIL:
                    delegate = (FacetJsonSchemaDelegate<F>) new CoreObjectListFacetJsonSchemaDelegate( facet );
                    break;
                default:
                    break;
            }

        } else if (coreFacet instanceof TLSimpleFacet) {
            TLSimpleFacet facet = (TLSimpleFacet) coreFacet;

            if (coreFacet.getFacetType() == TLFacetType.SIMPLE) {
                delegate = (FacetJsonSchemaDelegate<F>) new TLSimpleFacetJsonSchemaDelegate( facet );
            }
        }
        return delegate;
    }

}
