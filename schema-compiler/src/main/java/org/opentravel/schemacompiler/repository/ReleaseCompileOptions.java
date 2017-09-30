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
package org.opentravel.schemacompiler.repository;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.task.CommonCompilerTaskOptions;
import org.opentravel.schemacompiler.task.CompileAllTaskOptions;
import org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions;
import org.opentravel.schemacompiler.task.ResourceCompilerTaskOptions;
import org.opentravel.schemacompiler.task.SchemaCompilerTaskOptions;
import org.opentravel.schemacompiler.task.ServiceCompilerTaskOptions;

/**
 * Specifies the compiler options for an OTM release.
 */
public class ReleaseCompileOptions implements CompileAllTaskOptions {
	
	private static final String BINDING_STYLE_KEY       = "bindingStyle";
	private static final String COMPILE_SCHEMAS_KEY     = "compileSchemas";
	private static final String COMPILE_JSON_KEY        = "compileJson";
	private static final String COMPILE_SERVICES_KEY    = "compileServices";
	private static final String COMPILE_SWAGGER_KEY     = "compileSwagger";
	private static final String COMPILE_HTML_KEY        = "compileHtml";
	private static final String SERVICE_ENDPOINT_KEY    = "serviceEndpointUrl";
	private static final String RESOURCE_BASE_URL_KEY   = "resourceBaseUrl";
	private static final String SUPPRESS_EXTENSIONS_KEY = "suppressOtmExtensions";
	private static final String GENERATE_EXAMPLES_KEY   = "generateExamples";
	private static final String EXAMPLE_MAX_DETAILS_KEY = "generateMaxDetailsForExamples";
	private static final String EXAMPLE_CONTEXT_KEY     = "exampleContext";
	private static final String EXAMPLE_MAX_REPEAT_KEY  = "exampleMaxRepeat";
	private static final String EXAMPLE_MAX_DEPTH_KEY   = "exampleMaxDepth";
	
	private String bindingStyle = CompilerExtensionRegistry.getActiveExtension();
    private boolean compileSchemas = true;
    private boolean compileJson = true;
    private boolean compileServices = true;
    private boolean compileSwagger = true;
    private boolean compileHtml = true;
    private String serviceEndpointUrl;
    private String resourceBaseUrl;
    private boolean suppressOtmExtensions = false;
    private boolean generateExamples = true;
    private boolean generateMaxDetailsForExamples = true;
    private String exampleContext;
    private Integer exampleMaxRepeat = 3;
    private Integer exampleMaxDepth = 3;
    
	/**
	 * Default constructor.
	 */
	public ReleaseCompileOptions() {}
	
	/**
	 * Constructor that populates all compiler option values using the given properties.
	 * 
	 * @param p  the key/value pairs from which to populate all compiler options
	 */
	public ReleaseCompileOptions(Map<String,String> p) {
		this.bindingStyle = p.get( BINDING_STYLE_KEY );
		this.compileSchemas = parseBoolean( p.get( COMPILE_SCHEMAS_KEY ) );
		this.compileJson = parseBoolean( p.get( COMPILE_JSON_KEY ) );
		this.compileServices = parseBoolean( p.get( COMPILE_SERVICES_KEY ) );
		this.compileSwagger = parseBoolean( p.get( COMPILE_SWAGGER_KEY ) );
		this.compileHtml = parseBoolean( p.get( COMPILE_HTML_KEY ) );
		this.suppressOtmExtensions = parseBoolean( p.get( SUPPRESS_EXTENSIONS_KEY ) );
		this.generateExamples = parseBoolean( p.get( GENERATE_EXAMPLES_KEY ) );
		this.generateMaxDetailsForExamples = parseBoolean( p.get( EXAMPLE_MAX_DETAILS_KEY ) );
		this.serviceEndpointUrl = p.get( SERVICE_ENDPOINT_KEY );
		this.resourceBaseUrl = p.get( RESOURCE_BASE_URL_KEY );
		this.exampleContext = p.get( EXAMPLE_CONTEXT_KEY );
		this.exampleMaxRepeat = parseInt( p.get( EXAMPLE_MAX_REPEAT_KEY ) );
		this.exampleMaxDepth = parseInt( p.get( EXAMPLE_MAX_DEPTH_KEY ) );
		
		if ((bindingStyle == null) ||
				!CompilerExtensionRegistry.getAvailableExtensionIds().contains( bindingStyle )) {
			bindingStyle = CompilerExtensionRegistry.getActiveExtension();
		}
	}
	
	/**
	 * Returns the binding style that should be used when compiling the OTM model.
	 * 
	 * @return String
	 */
	public String getBindingStyle() {
		return bindingStyle;
	}
	
	/**
	 * Assigns the binding style that should be used when compiling the OTM model.
	 * 
	 * @param bindingStyle  the binding style value to assign
	 */
	public void setBindingStyle(String bindingStyle) {
		this.bindingStyle = bindingStyle;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileSchemas()
	 */
	public boolean isCompileSchemas() {
		return compileSchemas;
	}
	
	/**
	 * Assigns the value of the 'compileSchemas' field.
	 *
	 * @param compileSchemas  the field value to assign
	 */
	public void setCompileSchemas(boolean compileSchemas) {
		this.compileSchemas = compileSchemas;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileJsonSchemas()
	 */
	public boolean isCompileJsonSchemas() {
		return compileJson;
	}
	
	/**
	 * Assigns the value of the 'compileJson' field.
	 *
	 * @param compileJson  the field value to assign
	 */
	public void setCompileJsonSchemas(boolean compileJson) {
		this.compileJson = compileJson;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileServices()
	 */
	public boolean isCompileServices() {
		return compileServices;
	}
	
	/**
	 * Assigns the value of the 'compileServices' field.
	 *
	 * @param compileServices  the field value to assign
	 */
	public void setCompileServices(boolean compileServices) {
		this.compileServices = compileServices;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileSwagger()
	 */
	public boolean isCompileSwagger() {
		return compileSwagger;
	}
	
	/**
	 * Assigns the value of the 'compileSwagger' field.
	 *
	 * @param compileSwagger  the field value to assign
	 */
	public void setCompileSwagger(boolean compileSwagger) {
		this.compileSwagger = compileSwagger;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.CompileAllTaskOptions#isCompileHtml()
	 */
	public boolean isCompileHtml() {
		return compileHtml;
	}
	
	/**
	 * Assigns the value of the 'compileHtml' field.
	 *
	 * @param compileHtml  the field value to assign
	 */
	public void setCompileHtml(boolean compileHtml) {
		this.compileHtml = compileHtml;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.ServiceCompilerTaskOptions#getServiceEndpointUrl()
	 */
	public String getServiceEndpointUrl() {
		return serviceEndpointUrl;
	}
	
	/**
	 * Assigns the value of the 'serviceEndpointUrl' field.
	 *
	 * @param serviceEndpointUrl  the field value to assign
	 */
	public void setServiceEndpointUrl(String serviceEndpointUrl) {
		this.serviceEndpointUrl = serviceEndpointUrl;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.ResourceCompilerTaskOptions#getResourceBaseUrl()
	 */
	public String getResourceBaseUrl() {
		return resourceBaseUrl;
	}
	
	/**
	 * Assigns the value of the 'resourceBaseUrl' field.
	 *
	 * @param resourceBaseUrl  the field value to assign
	 */
	public void setResourceBaseUrl(String resourceBaseUrl) {
		this.resourceBaseUrl = resourceBaseUrl;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.SchemaCompilerTaskOptions#isSuppressOtmExtensions()
	 */
	public boolean isSuppressOtmExtensions() {
		return suppressOtmExtensions;
	}
	
	/**
	 * Assigns the value of the 'suppressOtmExtensions' field.
	 *
	 * @param suppressOtmExtensions  the field value to assign
	 */
	public void setSuppressOtmExtensions(boolean suppressOtmExtensions) {
		this.suppressOtmExtensions = suppressOtmExtensions;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#isGenerateExamples()
	 */
	public boolean isGenerateExamples() {
		return generateExamples;
	}
	
	/**
	 * Assigns the value of the 'generateExamples' field.
	 *
	 * @param generateExamples  the field value to assign
	 */
	public void setGenerateExamples(boolean generateExamples) {
		this.generateExamples = generateExamples;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#isGenerateMaxDetailsForExamples()
	 */
	public boolean isGenerateMaxDetailsForExamples() {
		return generateMaxDetailsForExamples;
	}
	
	/**
	 * Assigns the value of the 'generateMaxDetailsForExamples' field.
	 *
	 * @param generateMaxDetailsForExamples  the field value to assign
	 */
	public void setGenerateMaxDetailsForExamples(boolean generateMaxDetailsForExamples) {
		this.generateMaxDetailsForExamples = generateMaxDetailsForExamples;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#getExampleContext()
	 */
	public String getExampleContext() {
		return exampleContext;
	}
	
	/**
	 * Assigns the value of the 'exampleContext' field.
	 *
	 * @param exampleContext  the field value to assign
	 */
	public void setExampleContext(String exampleContext) {
		this.exampleContext = exampleContext;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#getExampleMaxRepeat()
	 */
	public Integer getExampleMaxRepeat() {
		return exampleMaxRepeat;
	}
	
	/**
	 * Assigns the value of the 'exampleMaxRepeat' field.
	 *
	 * @param exampleMaxRepeat  the field value to assign
	 */
	public void setExampleMaxRepeat(Integer exampleMaxRepeat) {
		this.exampleMaxRepeat = exampleMaxRepeat;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.task.ExampleCompilerTaskOptions#getExampleMaxDepth()
	 */
	public Integer getExampleMaxDepth() {
		return exampleMaxDepth;
	}
	
	/**
	 * Assigns the value of the 'exampleMaxDepth' field.
	 *
	 * @param exampleMaxDepth  the field value to assign
	 */
	public void setExampleMaxDepth(Integer exampleMaxDepth) {
		this.exampleMaxDepth = exampleMaxDepth;
	}

	/**
	 * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#applyTaskOptions(org.opentravel.schemacompiler.task.CommonCompilerTaskOptions)
	 */
	@Override
	public void applyTaskOptions(CommonCompilerTaskOptions taskOptions) {
        if (taskOptions instanceof CompileAllTaskOptions) {
            CompileAllTaskOptions compileAllOptions = (CompileAllTaskOptions) taskOptions;

            setCompileSchemas(compileAllOptions.isCompileSchemas());
            setCompileServices(compileAllOptions.isCompileServices());
            setCompileJsonSchemas(compileAllOptions.isCompileJsonSchemas());
            setCompileSwagger(compileAllOptions.isCompileSwagger());
            setCompileHtml(compileAllOptions.isCompileHtml());
        }
        if (taskOptions instanceof SchemaCompilerTaskOptions) {
        	setSuppressOtmExtensions( ((SchemaCompilerTaskOptions) taskOptions).isSuppressOtmExtensions() );
        }
        if (taskOptions instanceof ExampleCompilerTaskOptions) {
            ExampleCompilerTaskOptions exampleOptions = (ExampleCompilerTaskOptions) taskOptions;

            setGenerateExamples(exampleOptions.isGenerateExamples());
            setGenerateMaxDetailsForExamples(exampleOptions.isGenerateMaxDetailsForExamples());
            setExampleContext(exampleOptions.getExampleContext());
            setExampleMaxRepeat(exampleOptions.getExampleMaxRepeat());
            setExampleMaxDepth(exampleOptions.getExampleMaxDepth());
        }
        if (taskOptions instanceof ServiceCompilerTaskOptions) {
            setServiceEndpointUrl(((ServiceCompilerTaskOptions) taskOptions).getServiceEndpointUrl());
        }
        if (taskOptions instanceof ResourceCompilerTaskOptions) {
            setResourceBaseUrl(((ResourceCompilerTaskOptions) taskOptions).getResourceBaseUrl());
        }
	}

	/**
	 * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getCatalogLocation()
	 */
	@Override
	public String getCatalogLocation() {
		return null;
	}

	/**
	 * @see org.opentravel.schemacompiler.task.CommonCompilerTaskOptions#getOutputFolder()
	 */
	@Override
	public String getOutputFolder() {
		return null;
	}
    
	/**
	 * @see org.opentravel.schemacompiler.task.ServiceCompilerTaskOptions#getServiceLibraryUrl()
	 */
	public URL getServiceLibraryUrl() {
		return null;
	}
	
    /**
     * Returns a map of key/value pairs that contains the current state of the compiler options.
     * 
     * @return Map<String,String>
     */
    public Map<String,String> toProperties() {
    	Map<String,String> p = new HashMap<>();
    	
    	p.put( COMPILE_SCHEMAS_KEY, compileSchemas + "" );
    	p.put( COMPILE_JSON_KEY, compileJson + "" );
    	p.put( COMPILE_SERVICES_KEY, compileServices + "" );
    	p.put( COMPILE_SWAGGER_KEY, compileSwagger + "" );
    	p.put( COMPILE_HTML_KEY, compileHtml + "" );
    	p.put( SUPPRESS_EXTENSIONS_KEY, suppressOtmExtensions + "" );
    	p.put( GENERATE_EXAMPLES_KEY, generateExamples + "" );
    	p.put( EXAMPLE_MAX_DETAILS_KEY, generateMaxDetailsForExamples + "" );
    	
    	if (bindingStyle != null) {
        	p.put( BINDING_STYLE_KEY, bindingStyle );
    	}
    	if (serviceEndpointUrl != null) {
        	p.put( SERVICE_ENDPOINT_KEY, serviceEndpointUrl );
    	}
    	if (resourceBaseUrl != null) {
        	p.put( RESOURCE_BASE_URL_KEY, resourceBaseUrl );
    	}
    	if (exampleContext != null) {
        	p.put( EXAMPLE_CONTEXT_KEY, exampleContext );
    	}
    	if (exampleMaxRepeat != null) {
        	p.put( EXAMPLE_MAX_REPEAT_KEY, exampleMaxRepeat.toString() );
    	}
    	if (exampleMaxDepth != null) {
        	p.put( EXAMPLE_MAX_DEPTH_KEY, exampleMaxDepth.toString() );
    	}
    	return p;
    }
    
    /**
     * Parses the given boolean string or returns false if the given string
     * is null or not a boolean value.
     * 
     * @param boolString  the boolean string to parse
     * @return boolean
     */
    private boolean parseBoolean(String boolString) {
    	boolean value = false;
    	
    	if (boolString != null) {
    		value = Boolean.parseBoolean( boolString );
    	}
    	return value;
    }
    
	/**
	 * Parses the given integer value or returns null if the given string
	 * does not represent a valid integer.
	 * 
	 * @param intString  the integer string to parse
	 * @return Integer
	 */
	private Integer parseInt(String intString) {
		Integer value = null;
		try {
			if (intString != null) {
				value = Integer.parseInt( intString );
			}
		} catch (NumberFormatException e) {
			// Ignore error and return null
		}
		return value;
	}
	
}
