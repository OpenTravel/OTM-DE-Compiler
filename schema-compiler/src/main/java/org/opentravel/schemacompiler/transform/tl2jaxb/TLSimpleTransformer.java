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

package org.opentravel.schemacompiler.transform.tl2jaxb;

import org.opentravel.ns.ota2.librarymodel_v01_05.Documentation;
import org.opentravel.ns.ota2.librarymodel_v01_05.Equivalent;
import org.opentravel.ns.ota2.librarymodel_v01_05.Example;
import org.opentravel.ns.ota2.librarymodel_v01_05.Simple;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLEquivalent;
import org.opentravel.schemacompiler.model.TLExample;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

import java.math.BigInteger;

/**
 * Handles the transformation of objects from the <code>TLSimple</code> type to the <code>Simple</code> type.
 * 
 * @author S. Livezey
 */
public class TLSimpleTransformer extends BaseTransformer<TLSimple,Simple,SymbolResolverTransformerContext> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public Simple transform(TLSimple source) {
        ObjectTransformer<TLEquivalent,Equivalent,SymbolResolverTransformerContext> equivTransformer =
            getTransformerFactory().getTransformer( TLEquivalent.class, Equivalent.class );
        ObjectTransformer<TLExample,Example,SymbolResolverTransformerContext> exTransformer =
            getTransformerFactory().getTransformer( TLExample.class, Example.class );
        NamedEntity parentType = source.getParentType();
        Simple simpleType = new Simple();

        simpleType.setName( trimString( source.getName(), false ) );
        simpleType.setPattern( trimString( source.getPattern() ) );
        simpleType.setMinInclusive( trimString( source.getMinInclusive() ) );
        simpleType.setMaxInclusive( trimString( source.getMaxInclusive() ) );
        simpleType.setMinExclusive( trimString( source.getMinExclusive() ) );
        simpleType.setMaxExclusive( trimString( source.getMaxExclusive() ) );
        simpleType.setListTypeInd( source.isListTypeInd() ? Boolean.TRUE : null );

        if ((source.getDocumentation() != null) && !source.getDocumentation().isEmpty()) {
            ObjectTransformer<TLDocumentation,Documentation,SymbolResolverTransformerContext> docTransformer =
                getTransformerFactory().getTransformer( TLDocumentation.class, Documentation.class );

            simpleType.setDocumentation( docTransformer.transform( source.getDocumentation() ) );
        }

        for (TLEquivalent sourceEquiv : source.getEquivalents()) {
            simpleType.getEquivalent().add( equivTransformer.transform( sourceEquiv ) );
        }

        for (TLExample sourceEx : source.getExamples()) {
            simpleType.getExample().add( exTransformer.transform( sourceEx ) );
        }

        if (source.getMinLength() > 0) {
            simpleType.setMinLength( BigInteger.valueOf( source.getMinLength() ) );
        }
        if (source.getMaxLength() > 0) {
            simpleType.setMaxLength( BigInteger.valueOf( source.getMaxLength() ) );
        }
        if (source.getFractionDigits() >= 0) {
            simpleType.setFractionDigits( BigInteger.valueOf( source.getFractionDigits() ) );
        }
        if (source.getTotalDigits() > 0) {
            simpleType.setTotalDigits( BigInteger.valueOf( source.getTotalDigits() ) );
        }

        if (parentType != null) {
            simpleType.setType(
                context.getSymbolResolver().buildEntityName( parentType.getNamespace(), parentType.getLocalName() ) );
        }
        if (simpleType.getType() == null) {
            simpleType.setType( trimString( source.getParentTypeName(), false ) );
        }
        return simpleType;
    }

}
