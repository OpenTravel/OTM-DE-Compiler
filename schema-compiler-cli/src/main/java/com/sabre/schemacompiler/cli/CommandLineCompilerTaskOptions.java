/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.cli;

import java.net.URL;

import org.apache.commons.cli.CommandLine;

import com.sabre.schemacompiler.task.CommonCompilerTaskOptions;
import com.sabre.schemacompiler.task.CompileAllTaskOptions;
import com.sabre.schemacompiler.task.TaskUtils;
import com.sabre.schemacompiler.util.URLUtils;

/**
 * Compile-All task options implementation that obtains its settings from the command-line
 * arguments provided by the user.
 *
 * @author S. Livezey
 */
public class CommandLineCompilerTaskOptions implements CompileAllTaskOptions {
	
	private CommandLine commandLineArgs;
	
	/**
	 * Constructor that supplies the command-line arguments provided by the user.
	 * 
	 * @param commandLineArgs  the command-line arguments
	 */
	public CommandLineCompilerTaskOptions(CommandLine commandLineArgs) {
		this.commandLineArgs = commandLineArgs;
	}
	
	/**
	 * @see com.sabre.schemacompiler.task.CommonCompilerTaskOptions#getCatalogLocation()
	 */
	@Override
	public String getCatalogLocation() {
		String catalogLocation = null;
		
		if (commandLineArgs.hasOption("c")) {
			catalogLocation = commandLineArgs.getOptionValue("c");
		}
		return catalogLocation;
	}

	/**
	 * @see com.sabre.schemacompiler.task.ServiceCompilerTaskOptions#getServiceLibraryUrl()
	 */
	@Override
	public URL getServiceLibraryUrl() {
		URL serviceLibraryUrl = null;
		
		if (isCompileServices() && !isCompileSchemas()) {
			String filename = commandLineArgs.getArgs()[0];
			
			serviceLibraryUrl = URLUtils.toURL( TaskUtils.getPathFromOptionValue(filename) );
		}
		return serviceLibraryUrl;
	}

	/**
	 * @see com.sabre.schemacompiler.task.ServiceCompilerTaskOptions#getServiceEndpointUrl()
	 */
	@Override
	public String getServiceEndpointUrl() {
		return commandLineArgs.getOptionValue("s");
	}

	/**
	 * @see com.sabre.schemacompiler.task.CommonCompilerTaskOptions#getOutputFolder()
	 */
	@Override
	public String getOutputFolder() {
		return commandLineArgs.getOptionValue("d");
	}
	
	/**
	 * Returns the user-specified binding style for the compiled output, or null if the default binding
	 * style is to be used.
	 * 
	 * @return String
	 */
	public String getBindingStyle() {
		return commandLineArgs.getOptionValue("b");
	}
	
	/**
	 * @see com.sabre.schemacompiler.task.CompileAllTaskOptions#isCompileSchemas()
	 */
	@Override
	public boolean isCompileSchemas() {
		return commandLineArgs.hasOption("X") || (!commandLineArgs.hasOption("X") && !commandLineArgs.hasOption("W"));
	}
	
	/**
	 * @see com.sabre.schemacompiler.task.CompileAllTaskOptions#isCompileServices()
	 */
	@Override
	public boolean isCompileServices() {
		return commandLineArgs.hasOption("W") || (!commandLineArgs.hasOption("X") && !commandLineArgs.hasOption("W"));
	}
	
	/**
	 * @see com.sabre.schemacompiler.task.ExampleCompilerTaskOptions#isGenerateExamples()
	 */
	@Override
	public boolean isGenerateExamples() {
		return commandLineArgs.hasOption("E");
	}

	/**
	 * @see com.sabre.schemacompiler.task.ExampleCompilerTaskOptions#isGenerateMaxDetailsForExamples()
	 */
	@Override
	public boolean isGenerateMaxDetailsForExamples() {
		return commandLineArgs.hasOption("M");
	}

	/**
	 * @see com.sabre.schemacompiler.task.ExampleCompilerTaskOptions#getExampleContext()
	 */
	@Override
	public String getExampleContext() {
		return commandLineArgs.getOptionValue("C");
	}

	/**
	 * @see com.sabre.schemacompiler.task.ExampleCompilerTaskOptions#getExampleMaxRepeat()
	 */
	@Override
	public Integer getExampleMaxRepeat() {
		String optionValue = commandLineArgs.getOptionValue("r");
		Integer maxRepeat = null;
		
		if (optionValue != null) {
			try {
				maxRepeat = new Integer(optionValue);
				
			} catch (NumberFormatException e) {
				// No error - ignore and return null
			}
		}
		return maxRepeat;
	}

	/**
	 * @see com.sabre.schemacompiler.task.ExampleCompilerTaskOptions#getExampleMaxDepth()
	 */
	@Override
	public Integer getExampleMaxDepth() {
		String optionValue = commandLineArgs.getOptionValue("D");
		Integer maxDepth = null;
		
		if (optionValue != null) {
			try {
				maxDepth = new Integer(optionValue);
				
			} catch (NumberFormatException e) {
				// No error - ignore and return null
			}
		}
		return maxDepth;
	}

	/**
	 * @see com.sabre.schemacompiler.task.CommonCompilerTaskOptions#applyTaskOptions(com.sabre.schemacompiler.task.CommonCompilerTaskOptions)
	 */
	@Override
	public void applyTaskOptions(CommonCompilerTaskOptions taskOptions) {
		throw new UnsupportedOperationException();
	}

}
