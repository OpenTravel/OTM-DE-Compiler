package org.opentravel.schemacompiler.loader;

import java.util.List;

/**
 * Encapsulates the resulting information from a <code>LibraryModuleLoader</code> call to the
 * 'loadLibrary()' method.
 * 
 * @param <J>
 *            the type of the JAXB artifact that was loaded
 * @author S. Livezey
 */
public final class LibraryModuleInfo<J> {

    private J jaxbArtifact;
    private String libraryName;
    private String namespace;
    private String versionScheme;
    private List<String> includes;
    private List<LibraryModuleImport> imports;

    /**
     * Constructor that supplies the module information required by the model loader.
     * 
     * @param jaxbArtifact
     *            the JAXB object that was loaded
     * @param libraryName
     *            the name of the JAXB library
     * @param namespace
     *            the target namespace of the JAXB library
     * @param versionScheme
     *            the version scheme of the JAXB library
     * @param includes
     *            the include declarations from the JAXB library
     * @param imports
     *            the import declarations from the JAXB library
     */
    public LibraryModuleInfo(J jaxbArtifact, String libraryName, String namespace,
            String versionScheme, List<String> includes, List<LibraryModuleImport> imports) {
        this.jaxbArtifact = jaxbArtifact;
        this.libraryName = libraryName;
        this.namespace = namespace;
        this.versionScheme = versionScheme;
        this.includes = includes;
        this.imports = imports;
    }

    /**
     * Returns the JAXB object that was loaded.
     * 
     * @return J
     */
    public J getJaxbArtifact() {
        return jaxbArtifact;
    }

    /**
     * Returnsthe name of the JAXB library.
     * 
     * @return String
     */
    public String getLibraryName() {
        return libraryName;
    }

    /**
     * Returns the target namespace of the JAXB library.
     * 
     * @return String
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the version scheme of the JAXB library.
     * 
     * @return String
     */
    public String getVersionScheme() {
        return versionScheme;
    }

    /**
     * Returns the include declarations from the JAXB library.
     * 
     * @return List<String>
     */
    public List<String> getIncludes() {
        return includes;
    }

    /**
     * Returns the import declarations from the JAXB library.
     * 
     * @return List<LibraryModuleImport>
     */
    public List<LibraryModuleImport> getImports() {
        return imports;
    }

}
