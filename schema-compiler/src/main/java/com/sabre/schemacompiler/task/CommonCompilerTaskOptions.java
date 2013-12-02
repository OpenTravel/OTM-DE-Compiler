/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.task;


/**
 * Base interface that defines the options that are common to all code generation tasks.
 * 
 * @author S. Livezey
 */
public interface CommonCompilerTaskOptions {
	
	/**
	 * Copies all of the known task options from the given set of options into this instance.
	 * 
	 * @param taskOptions  the task options from which to copy the configuration settings
	 */
	public void applyTaskOptions(CommonCompilerTaskOptions taskOptions);
	
	/**
	 * Returns the location of the library catalog file as either an absolute or relative URL
	 * string.
	 * 
	 * @return String
	 */
	public String getCatalogLocation();
	
	/**
	 * Returns the output folder location as either an absolute or relative URL
	 * string.
	 * 
	 * @return String
	 */
	public String getOutputFolder();
	
}
