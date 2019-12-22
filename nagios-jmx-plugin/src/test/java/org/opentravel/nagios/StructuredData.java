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

package org.opentravel.nagios;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * Provides a complex data structure for testing MBean queries.
 */
public class StructuredData extends CompositeDataSupport {

    private static final long serialVersionUID = -3316565040188078289L;

    /**
     * Default constructor.
     * 
     * @throws OpenDataException thrown if the structured data cannot be initialized
     */
    public StructuredData() throws OpenDataException {
        super(
            new CompositeType( "Structured Data", "Structured data.", new String[] {"Value1", "Value2"},
                new String[] {"Value #1", "Value #2"}, new OpenType[] {SimpleType.INTEGER, SimpleType.INTEGER} ),
            new String[] {"Value1", "Value2"}, new Object[] {1, 2} );
    }

    // private int value1 = 1;
    // private int value2 = 2;
    //
    // /**
    // * Returns the value of the 'value1' field.
    // *
    // * @return int
    // */
    // public int getValue1() {
    // return value1;
    // }
    //
    // /**
    // * Returns the value of the 'value2' field.
    // *
    // * @return int
    // */
    // public int getValue2() {
    // return value2;
    // }
    //
}
