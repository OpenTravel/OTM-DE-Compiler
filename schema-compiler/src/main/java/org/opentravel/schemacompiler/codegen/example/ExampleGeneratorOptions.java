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
package org.opentravel.schemacompiler.codegen.example;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLOperation;

/**
 * Specifies the options to use when generating examples using the <code>ExampleNavigator</code>
 * component.
 * 
 * @author S. Livezey
 */
public class ExampleGeneratorOptions {

    public static enum DetailLevel {
        MINIMUM, MAXIMUM
    };

    private DetailLevel detailLevel = DetailLevel.MAXIMUM;
    private Map<QName,TLFacet> preferredFacetMap = new HashMap<>();
    private String exampleContext;
    private int maxRepeat = 3;
    private int maxRecursionDepth = 2;

    /**
     * Returns the amount of detail to include in the generated example.
     * 
     * @return DetailLevel
     */
    public DetailLevel getDetailLevel() {
        return detailLevel;
    }

    /**
     * Assigns the amount of detail to include in the generated example.
     * 
     * @param detailLevel
     *            the detail level to assign
     */
    public void setDetailLevel(DetailLevel detailLevel) {
        this.detailLevel = detailLevel;
    }
    
    /**
     * Returns the preferred facet to generate for substitution groups with the
     * given entity.  If no preferred facet has been assigned, this method will
     * return null.
     * 
     * @param entity  the entity for which to return a preferred facet
     * @return TLFacet
     */
    public TLFacet getPreferredFacet(TLFacetOwner entity) {
    	return preferredFacetMap.get( new QName( entity.getNamespace(), entity.getLocalName() ) );
    }
    
    /**
     * Returns the preferred facet to generate for substitution groups with the
     * given entity.
     * 
     * @param entity  the entity for which to assign the preferred facet
     * @param preferredFacet  the preferred facet to use when generating substitution
     *						  groups for the entity
     */
    public void setPreferredFacet(TLFacetOwner entity, TLFacet preferredFacet) {
    	QName entityName = new QName( entity.getNamespace(), entity.getLocalName() );
    	
    	if (entity instanceof TLOperation) {
    		throw new IllegalArgumentException("Operation facets are not part of substitution groups.");
    	}
    	if (preferredFacet.getFacetType() == TLFacetType.QUERY) {
    		throw new IllegalArgumentException("Query facets are not part of the substitution group for a business object.");
    	}
    	preferredFacetMap.put( entityName, preferredFacet );
    }

    /**
     * Returns the exampleContext to use identify examples for simple data types.
     * 
     * @return String
     */
    public String getExampleContext() {
        return exampleContext;
    }

    /**
     * Assigns the exampleContext to use identify examples for simple data types.
     * 
     * @param exampleContext
     *            the exampleContext value to assign
     */
    public void setExampleContext(String context) {
        this.exampleContext = context;
    }

    /**
     * Returns the maximum number of time a repeating element should repeat.
     * 
     * @return int
     */
    public int getMaxRepeat() {
        return maxRepeat;
    }

    /**
     * Assigns the maximum number of time a repeating element should repeat.
     * 
     * @param maxRepeat
     *            the maximum repeat value to assign
     */
    public void setMaxRepeat(int maxRepeat) {
        this.maxRepeat = maxRepeat;
    }

    /**
     * Returns the maximum number of times that an element should be recursively visited within a
     * nested object structure.
     * 
     * @return int
     */
    public int getMaxRecursionDepth() {
        return maxRecursionDepth;
    }

    /**
     * Assigns the maximum number of times that an element should be recursively visited within a
     * nested object structure.
     * 
     * @param maxRecursionDepth
     *            the maximum recursion depth to allow during example navigation
     */
    public void setMaxRecursionDepth(int maxRecursionDepth) {
        this.maxRecursionDepth = maxRecursionDepth;
    }

}
