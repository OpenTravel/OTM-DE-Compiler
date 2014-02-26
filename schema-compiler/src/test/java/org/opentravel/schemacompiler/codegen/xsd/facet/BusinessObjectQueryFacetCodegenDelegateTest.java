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

import static org.junit.Assert.assertNotNull;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLProperty;

public class BusinessObjectQueryFacetCodegenDelegateTest {

    @Test
    public void shouldReturnExtensionPointElementQuery() {
        // given
        TLBusinessObject base = new TLBusinessObject();
        TLFacet query = createQueryFacet("BaseQuery");
        base.addQueryFacet(query);
        // has to have content, empty one is ignored
        TLProperty prop = new TLProperty();
        query.addElement(prop);

        // when
        BusinessObjectQueryFacetCodegenDelegate delegate = new BusinessObjectQueryFacetCodegenDelegate(
                query);
        QName epe = delegate.getExtensionPointElement();
        // then
        assertNotNull(epe);
    }

    @Test
    public void shouldReturnExtensionPointElementForQueryFacetWithSubtype() {
        // given
        TLBusinessObject base = new TLBusinessObject();
        TLFacet query = createQueryFacet("BaseQuery");
        base.addQueryFacet(query);
        // has to have content, empty one is ignored
        TLProperty prop = new TLProperty();
        query.addElement(prop);

        TLBusinessObject extBO = new TLBusinessObject();
        TLExtension ext = new TLExtension();
        ext.setExtendsEntity(base);
        extBO.setExtension(ext);

        TLFacet queryExt = createQueryFacet(query.getLabel());
        extBO.addQueryFacet(queryExt);

        // when
        BusinessObjectQueryFacetCodegenDelegate delegate = new BusinessObjectQueryFacetCodegenDelegate(
                queryExt);
        QName epe = delegate.getExtensionPointElement();
        // then
        assertNotNull(epe);
    }

    private TLFacet createQueryFacet(String name) {
        TLFacet ret = new TLFacet();
        ret.setFacetType(TLFacetType.QUERY);
        ret.setLabel(name);
        return ret;
    }
}
