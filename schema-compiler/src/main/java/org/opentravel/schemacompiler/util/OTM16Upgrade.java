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

/**
 * Temporary class used to indicate whether the OTM1.6 file format is enabled for the compiler.
 */
public class OTM16Upgrade {

    /**
     * Private constructor to prevent instantiation.
     */
    private OTM16Upgrade() {}

    /**
     * @deprecated Deprecated and scheduled for removal. Once removed, OTM 1.6 functions will be enabled permanently.
     */
    @Deprecated
    public static boolean otm16Enabled = true;

}
