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
package org.opentravel.schemacompiler.transform.jaxb14_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_04.Equivalent;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Equivalent</code> type to the
 * <code>TLEquivalent</code> type.
 * 
 * @author S. Livezey
 */
public class EquivalentTransformer extends
        BaseTransformer<Equivalent, TLEquivalent, DefaultTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLEquivalent transform(Equivalent source) {
        TLEquivalent equiv = new TLEquivalent();

        equiv.setContext(source.getContext());
        equiv.setDescription(source.getValue());
        return equiv;
    }

}
