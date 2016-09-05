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
package org.opentravel.schemacompiler.transform.library_01_06;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opentravel.schemacompiler.transform.AbstractTestTransformers;
import org.opentravel.schemacompiler.util.OTM16Upgrade;

/**
 * Base class for all transformer tests that utilize library schema v1.5 data.
 * 
 * @author S. Livezey
 */
public abstract class Abstract_1_6_TestTransformers extends AbstractTestTransformers {
	
	@BeforeClass
	public static void enableOTM16() throws Exception {
		OTM16Upgrade.otm16Enabled = true;
	}
	
	@AfterClass
	public static void disableOTM16() throws Exception {
		OTM16Upgrade.otm16Enabled = false;
	}
	
    /**
     * @see org.opentravel.schemacompiler.transform.AbstractTestTransformers#getBaseLocation()
     */
    @Override
    protected String getBaseLocation() {
        return System.getProperty("user.dir") + "/src/test/resources/libraries_1_6";
    }
    
}
