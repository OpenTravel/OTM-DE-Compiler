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

package org.opentravel.schemacompiler.diff.impl;

import java.util.Comparator;

import javax.xml.namespace.QName;

/**
 * Comparator used to sort lists of <code>QName</code> objects.
 */
public class QNameComparator implements Comparator<QName> {

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(QName qn1, QName qn2) {
        int result;

        if (qn1 == null) {
            result = (qn2 == null) ? 0 : 1;

        } else if (qn2 == null) {
            result = -1;

        } else {
            result = compareQName( qn1, qn2 );
        }
        return result;
    }

    /**
     * Compares the two qualified names provided. This method presumes that the two parameters have already been
     * determined to be non-null.
     * 
     * @param qn1 the first qualified name to compare
     * @param qn2 the second qualified name to compare
     * @return int
     */
    private int compareQName(QName qn1, QName qn2) {
        int result;
        String ns1 = qn1.getNamespaceURI();
        String ns2 = qn2.getNamespaceURI();

        if (ns1 == null) {
            result = (ns2 == null) ? 0 : 1;

        } else if (ns2 == null) {
            result = -1;

        } else {
            String local1 = qn1.getLocalPart();
            String local2 = qn2.getLocalPart();

            if (local1 == null) {
                result = (local2 == null) ? 0 : 1;

            } else {
                result = local1.compareTo( local2 );
            }
        }
        return result;
    }

}
