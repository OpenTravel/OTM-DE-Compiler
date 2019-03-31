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

package org.opentravel.schemacompiler.util;

import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBException;

/**
 * Static utility methods to interrogate exceptions.
 * 
 * @author S. Livezey
 */
public class ExceptionUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private ExceptionUtils() {}

    /**
     * Returns the class of the exception for the given throwable. <code>InvocationTargetException</code> instances are
     * recursed to their caused-by exceptions.
     * 
     * @param t the exception to process
     * @return Class&lt;? extends Throwable&gt;
     */
    public static final Class<? extends Throwable> getExceptionClass(Throwable t) {
        Class<? extends Throwable> exceptionClass = (t == null) ? null : t.getClass();
        Throwable actualCause = t;

        while ((actualCause != null)
            && ((actualCause instanceof InvocationTargetException) || (actualCause instanceof JAXBException))) {

            if (actualCause instanceof JAXBException) {
                JAXBException e = (JAXBException) actualCause;
                actualCause = (e.getLinkedException() != null) ? e.getLinkedException() : e.getCause();
            } else {
                actualCause = actualCause.getCause();
            }
        }

        if (actualCause != null) {
            exceptionClass = actualCause.getClass();
        }

        return exceptionClass;
    }

    /**
     * Returns the message exception for the given throwable. In the case of <code>InvocationTargetExceptions</code> or
     * null/empty messages, the caused-by exception is recursed in order to find a meaningful message.
     * 
     * @param t the exception to process
     * @return String
     */
    public static final String getExceptionMessage(Throwable t) {
        String message = (t == null) ? null : t.getMessage();
        Throwable actualCause = t;

        while (((message == null) || message.trim().equals( "" )) && (actualCause != null)
            && ((actualCause instanceof InvocationTargetException) || (actualCause instanceof JAXBException))) {

            if (actualCause instanceof JAXBException) {
                JAXBException e = (JAXBException) actualCause;
                actualCause = (e.getLinkedException() != null) ? e.getLinkedException() : e.getCause();
            } else {
                actualCause = actualCause.getCause();
            }
            message = (actualCause == null) ? null : actualCause.getMessage();
        }
        return message;
    }

}
