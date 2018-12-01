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

/**
 * Handles the creation of unique message ID's for XML documents.
 * 
 * @author S. Livezey
 */
public class MessageIdFactory {

    private Map<QName, String> prefixMap = new HashMap<>();
    private Map<QName, IdCounter> counterMap = new HashMap<>();

    /**
     * Returns a unique XML message ID for the given namespace/name combination.
     * 
     * @param namespace
     *            the namespace of the XML element for which to return a unique message ID
     * @param localName
     *            the local name of the XML element for which to return a unique message ID
     * @return String
     */
    public String getMessageId(String namespace, String localName) {
        return getMessageId(new QName(namespace, localName));
    }

    /**
     * Returns a unique XML message ID for the given namespace/name combination.
     * 
     * @param elementName
     *            the qualified name of the XML element for which to return a unique message ID
     * @return String
     */
    public String getMessageId(QName elementName) {
        return getIdPrefix(elementName) + "_" + getIdOrdinal(elementName);
    }

    /**
     * Returns a unique prefix for the given element name. A given <code>QName</code> value will
     * always have the same ID prefix for the life of this message factory instance.
     * 
     * @param elementName
     *            the qualified name of the element for which to return an ID prefix
     * @return int
     */
    private synchronized String getIdPrefix(QName elementName) {
        synchronized (prefixMap) {
            String prefix = prefixMap.get(elementName);

            if (prefix == null) {
                String localName = elementName.getLocalPart();
                String basePrefix = (localName.length() == 1) ? localName.toLowerCase()
                        : (localName.substring(0, 1).toLowerCase() + localName.substring(1))
                                .replaceAll("_", "");
                int counter = 0;

                if (!Character.isLetter(basePrefix.charAt(0))) {
                    basePrefix = "a" + basePrefix;
                }
                prefix = basePrefix;

                while (prefixMap.containsValue(prefix)) {
                    prefix = basePrefix + (++counter);
                }
                prefixMap.put(elementName, prefix);
            }
            return prefix;
        }
    }

    /**
     * Returns the ordinal component of the message ID value for the given element name.
     * 
     * @param elementName
     *            the qualified name of the element for which to return an ordinal ID value
     * @return int
     */
    private synchronized int getIdOrdinal(QName elementName) {
        synchronized (counterMap) {
            IdCounter counter = counterMap.get(elementName);

            if (counter == null) {
                counter = new IdCounter();
                counterMap.put(elementName, counter);
            }
            return counter.nextId();
        }
    }

    /**
     * Maintains a running count of the ID's created for a specific element <code>QName</code>.
     */
    private class IdCounter {

        private int nextId = 0;

        /**
         * Returns the next available message ID.
         * 
         * @return int
         */
        public int nextId() {
            return ++nextId;
        }

    }

}
