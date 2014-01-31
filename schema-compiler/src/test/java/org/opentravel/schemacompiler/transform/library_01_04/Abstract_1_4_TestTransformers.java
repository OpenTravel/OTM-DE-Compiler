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
package org.opentravel.schemacompiler.transform.library_01_04;

import org.opentravel.schemacompiler.transform.AbstractTestTransformers;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;

/**
 * Base class for all transformer tests that utilize library schema v1.4 data.
 * 
 * @author S. Livezey
 */
public abstract class Abstract_1_4_TestTransformers extends AbstractTestTransformers {

    /**
     * @see org.opentravel.schemacompiler.transform.AbstractTestTransformers#getBaseLocation()
     */
    @Override
    protected String getBaseLocation() {
        return SchemaCompilerTestUtils.getBaseLibraryLocation();
    }

}
