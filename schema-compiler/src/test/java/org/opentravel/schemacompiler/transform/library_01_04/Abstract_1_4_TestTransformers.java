/*
 * Copyright (c) 2011, Sabre Inc.
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
