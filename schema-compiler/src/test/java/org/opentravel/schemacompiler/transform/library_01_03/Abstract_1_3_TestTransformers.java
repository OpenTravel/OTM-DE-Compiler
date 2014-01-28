
package org.opentravel.schemacompiler.transform.library_01_03;

import org.opentravel.schemacompiler.transform.AbstractTestTransformers;

/**
 * Base class for all transformer tests that utilize library schema v1.3 data.
 * 
 * @author S. Livezey
 */
public abstract class Abstract_1_3_TestTransformers extends AbstractTestTransformers {

	/**
	 * @see org.opentravel.schemacompiler.transform.AbstractTestTransformers#getBaseLocation()
	 */
	@Override
	protected String getBaseLocation() {
		return System.getProperty("user.dir") + "/src/test/resources/libraries_1_3";
	}
	
}
