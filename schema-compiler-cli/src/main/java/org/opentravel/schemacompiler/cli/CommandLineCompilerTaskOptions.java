/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.cli;

import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.opentravel.schemacompiler.task.CommonCompilerTaskOptions;
import org.opentravel.schemacompiler.task.CompileAllTaskOptions;
import org.opentravel.schemacompiler.task.TaskUtils;
import org.opentravel.schemacompiler.util.URLUtils;

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
	 * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getCatalogLocation()
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
	 * @see org.opentravel.schemacompiler.task.ServiceCompilerTaskOptions#getServiceLibraryUrl()
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
	 * @see org.opentravel.schemacompiler.task.ServiceCompilerTaskOptions#getServiceEndpointUrl()
	 */
	@Override
	public String getServiceEndpointUrl() {
		return commandLineArgs.getOptionValue("s");
	}

	/**
	 * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getOutputFolder()
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
	 * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileSchemas()
	 */
	@Override
	public boolean isCompileSchemas() {
		return commandLineArgs.hasOption("X") || (!commandLineArgs.hasOption("X") && !commandLineArgs.hasOption("W"));
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileServices()
	 */
	@Override
	public boolean isCompileServices() {
		return commandLineArgs.hasOption("W") || (!commandLineArgs.hasOption("X") && !commandLineArgs.hasOption("W"));
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#isGenerateExamples()
	 */
	@Override
	public boolean isGenerateExamples() {
		return commandLineArgs.hasOption("E");
	}

	/**
	 * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#isGenerateMaxDetailsForExamples()
	 */
	@Override
	public boolean isGenerateMaxDetailsForExamples() {
		return commandLineArgs.hasOption("M");
	}

	/**
	 * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#getExampleContext()
	 */
	@Override
	public String getExampleContext() {
		return commandLineArgs.getOptionValue("C");
	}

	/**
	 * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#getExampleMaxRepeat()
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
	 * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#getExampleMaxDepth()
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
	 * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#applyTaskOptions(org.opentravel.schemacompiler.task.CommonCompilerTaskOptions)
	 */
	@Override
	public void applyTaskOptions(CommonCompilerTaskOptions taskOptions) {
		throw new UnsupportedOperationException();
	}

}
