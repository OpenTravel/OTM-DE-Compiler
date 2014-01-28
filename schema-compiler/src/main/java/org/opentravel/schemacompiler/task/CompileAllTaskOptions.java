package org.opentravel.schemacompiler.task;

/**
 * Interface that consolidates the options that are required for the simultaneous execution of all
 * code generation tasks.
 * 
 * @author S. Livezey
 */
public interface CompileAllTaskOptions extends SchemaCompilerTaskOptions,
        ServiceCompilerTaskOptions {

    /**
     * Returns the option flag indicating that XML schema (XSD) files should be generated for all
     * libraries.
     * 
     * @return boolean
     */
    public boolean isCompileSchemas();

    /**
     * Returns the option flag indicating that service (WSDL) files should be generated for service
     * definitions.
     * 
     * @return boolean
     */
    public boolean isCompileServices();

}
