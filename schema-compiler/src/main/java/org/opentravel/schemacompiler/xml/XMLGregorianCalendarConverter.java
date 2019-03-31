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

package org.opentravel.schemacompiler.xml;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Utility class for converting between <code>XMLGregorianCalendar</code> and <code>java.util.Date</code>.
 * 
 * @author S. Livezey
 */
public class XMLGregorianCalendarConverter {

    private static DatatypeFactory df = null;

    /**
     * Private constructor to prevent instantiation.
     */
    private XMLGregorianCalendarConverter() {}

    /**
     * Converts a <code>java.util.Date</code> into an instance of <code>XMLGregorianCalendar</code>.
     * 
     * @param date Instance of java.util.Date or a null reference
     * @return XMLGregorianCalendar
     */
    public static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
        XMLGregorianCalendar result = null;

        if (date != null) {
            GregorianCalendar gc = new GregorianCalendar();

            gc.setTimeInMillis( date.getTime() );
            result = df.newXMLGregorianCalendar( gc );
        }
        return result;
    }

    /**
     * Converts an <code>XMLGregorianCalendar</code> to an instance of <code>java.util.Date</code>.
     * 
     * @param xgc instance of XMLGregorianCalendar or a null reference
     * @return java.util.Date
     */
    public static Date toJavaDate(XMLGregorianCalendar xgc) {
        Date result = null;

        if (xgc != null) {
            return xgc.toGregorianCalendar().getTime();
        }
        return result;
    }

    /**
     * Initializes the data-type factory used for the date conversions.
     */
    static {
        try {
            df = DatatypeFactory.newInstance();

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
