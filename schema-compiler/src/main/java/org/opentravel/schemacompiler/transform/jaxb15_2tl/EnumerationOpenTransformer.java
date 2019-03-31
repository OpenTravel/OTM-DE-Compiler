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

package org.opentravel.schemacompiler.transform.jaxb15_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.EnumValue;
import org.opentravel.ns.ota2.librarymodel_v01_05.EnumerationOpen;
import org.opentravel.ns.ota2.librarymodel_v01_05.Extension;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>EnumerationOpen</code> type to the
 * <code>TLOpenEnumeration</code> type.
 * 
 * @author S. Livezey
 */
public class EnumerationOpenTransformer
    extends BaseTransformer<EnumerationOpen,TLOpenEnumeration,DefaultTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public TLOpenEnumeration transform(EnumerationOpen source) {
        ObjectTransformer<Extension,TLExtension,DefaultTransformerContext> extTransformer =
            getTransformerFactory().getTransformer( Extension.class, TLExtension.class );
        ObjectTransformer<Documentation,TLDocumentation,DefaultTransformerContext> docTransformer =
            getTransformerFactory().getTransformer( Documentation.class, TLDocumentation.class );
        TLOpenEnumeration enumType = new TLOpenEnumeration();

        enumType.setName( source.getName() );

        if (source.getExtension() != null) {
            enumType.setExtension( extTransformer.transform( source.getExtension() ) );
        }

        if (source.getDocumentation() != null) {
            enumType.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }

        if (source.getValue() != null) {
            ObjectTransformer<EnumValue,TLEnumValue,DefaultTransformerContext> valueTransformer =
                getTransformerFactory().getTransformer( EnumValue.class, TLEnumValue.class );

            for (EnumValue jaxbValue : source.getValue()) {
                enumType.addValue( valueTransformer.transform( jaxbValue ) );
            }
        }
        return enumType;
    }

}
