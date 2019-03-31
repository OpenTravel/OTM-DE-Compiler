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

import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.model.TLOperation;

/**
 * Performs the translation from <code>TLOperation</code> objects to the JAXB nodes used to produce the schema output.
 * 
 * @author S. Livezey
 */
public class TLOperationCodegenTransformer extends AbstractXsdTransformer<TLOperation,CodegenArtifacts> {

    @Override
    public CodegenArtifacts transform(TLOperation source) {
        FacetCodegenDelegateFactory delegateFactory = new FacetCodegenDelegateFactory( context );
        CodegenArtifacts artifacts = new CodegenArtifacts();

        artifacts.addAllArtifacts(
            buildCorrelatedArtifacts( source, delegateFactory.getDelegate( source.getRequest() ).generateElements(),
                delegateFactory.getDelegate( source.getRequest() ).generateArtifacts() ) );
        artifacts.addAllArtifacts(
            buildCorrelatedArtifacts( source, delegateFactory.getDelegate( source.getResponse() ).generateElements(),
                delegateFactory.getDelegate( source.getResponse() ).generateArtifacts() ) );
        artifacts.addAllArtifacts( buildCorrelatedArtifacts( source,
            delegateFactory.getDelegate( source.getNotification() ).generateElements(),
            delegateFactory.getDelegate( source.getNotification() ).generateArtifacts() ) );
        return artifacts;
    }

}
