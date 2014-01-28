package org.opentravel.schemacompiler.codegen.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.wsdl.CodeGenerationWsdlBindings;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.springframework.context.ApplicationContext;
import org.w3._2001.xmlschema.Import;
import org.w3._2001.xmlschema.OpenAttrs;
import org.w3._2001.xmlschema.Schema;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * JAXB namespace prefix mapper that obtains its mappings from the namespace import declarations of
 * a <code>TLLibrary</code> module.
 * 
 * @author S. Livezey
 */
public class CodegenNamespacePrefixMapper extends NamespacePrefixMapper {

    private Map<String, String> namespacePrefixMappings = new HashMap<String, String>();
    private List<String> uriDeclarations = new ArrayList<String>();
    private Set<String> importedNamespaces;
    private AbstractJaxbCodeGenerator<?> codeGenerator;

    /**
     * Constructor that assignes the library from which prefix mapping information will be derived.
     * 
     * @param library
     *            the library used to define namespace prefix mappings
     * @param useWsdlMappings
     *            flag indicating whether the prefix mappings for WSDL documents (versus XML schema
     *            documents) should be declared
     * @param codeGenerator
     *            the code generator to which schema dependencies should be reported
     * @param schema
     *            the XML schema for which xmlns prefix declarations will be created (may be null)
     */
    public CodegenNamespacePrefixMapper(AbstractLibrary library, boolean useWsdlMappings,
            AbstractJaxbCodeGenerator<?> codeGenerator, Schema schema) {

        this.codeGenerator = codeGenerator;
        this.importedNamespaces = getImportedNamespaces(schema);

        if (library != null) {
            addLibraryNamespaceMappings(library, codeGenerator.getFilter());

            if (useWsdlMappings) {
                addWsdlMappings();
            } else {
                addXmlSchemaMappings();
            }
        }
        addCompileTimeDependencyMappings();

        // Add a 'tns' mapping for the schema's target namespace if it has not already been mapped
        if ((schema != null) && !namespacePrefixMappings.containsKey(schema.getTargetNamespace())) {
            namespacePrefixMappings.put(schema.getTargetNamespace(), "tns");
            uriDeclarations.add(schema.getTargetNamespace());
        }
    }

    /**
     * Adds all of the namespace prefix mappings required by the import declarations of the given
     * library.
     * 
     * @param library
     *            the library for which to define prefix mappings
     * @param filter
     *            the code generation filter used to remove superfluous prefix mappings
     */
    private void addLibraryNamespaceMappings(AbstractLibrary library, CodeGenerationFilter filter) {
        Map<String, String> importPrefixMappings = new HashMap<String, String>();
        String targetNamespace = library.getNamespace();

        // First build a map of pre-assigned prefixes for namespaces that were explicitly imported
        for (TLNamespaceImport nsImport : library.getNamespaceImports()) {
            importPrefixMappings.put(nsImport.getNamespace(), nsImport.getPrefix());
        }

        // Build a list of all dependent schemas that require imports
        for (AbstractLibrary lib : library.getOwningModel().getAllLibraries()) {
            String namespace = lib.getNamespace();

            // Skip libraries that are in the target or the XML schema namespaces; also skip
            // chameleon
            // libraries since they cannot be imported
            if (namespace.equals(targetNamespace)
                    || namespace.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    || AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE.equals(namespace)
                    || !importedNamespaces.contains(namespace)) {
                continue;
            }

            // Determine whether an import if the current library was required by the
            // filter (if one was provided)
            boolean importRequired = (filter == null);

            if (!importRequired) {
                if (lib instanceof XSDLibrary) {
                    importRequired |= filter.processExtendedLibrary((XSDLibrary) lib);
                }
                importRequired |= filter.processLibrary(lib);
            }

            // Add a prefix mapping for this library's namespace if one is required and does not
            // already exist
            if (importRequired && !namespacePrefixMappings.containsKey(namespace)) {

                // Identify a unique prefix for the imported namespace
                String prefix = importPrefixMappings.get(namespace);

                if (prefix == null) {
                    prefix = (lib.getPrefix() == null) ? "ns1" : lib.getPrefix();
                }
                if (namespacePrefixMappings.containsValue(prefix)) {
                    String prefixStart = getPrefixAlphaChars(prefix);
                    int counter = 1;

                    prefix = prefixStart + counter;

                    while (namespacePrefixMappings.containsKey(prefix)) {
                        prefix = prefixStart + (counter++);
                    }
                }

                namespacePrefixMappings.put(namespace, prefix);
                uriDeclarations.add(namespace);
            }
        }

        if (!AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE.equals(targetNamespace)) {
            uriDeclarations.add(library.getNamespace());
            namespacePrefixMappings.put(library.getNamespace(), library.getPrefix());
        }
    }

    /**
     * Adds namespace/prefix mappings for compile-time dependencies that have been registered with
     * the code generator.
     */
    private void addCompileTimeDependencyMappings() {
        for (SchemaDeclaration schemaDependency : codeGenerator.getCompileTimeDependencies()) {
            addPrefixMapping(schemaDependency);
        }
    }

    /**
     * Adds the namespace mappings required for WSDL documents.
     */
    private void addWsdlMappings() {
        ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();

        // Add namespace mappings required by all WSDL documents
        if (codeGenerator != null) {
            codeGenerator.addCompileTimeDependency(SchemaDeclarations.OTA2_APPINFO_SCHEMA);
        }
        addPrefixMapping(SchemaDeclarations.WSDL_SCHEMA);
        addPrefixMapping(SchemaDeclarations.SOAP_SCHEMA);
        addPrefixMapping(SchemaDeclarations.SCHEMA_FOR_SCHEMAS);
        addPrefixMapping(SchemaDeclarations.OTA2_APPINFO_SCHEMA);

        // If a WSDL bindings component is defined in the application context, allow it to declare
        // any additional
        // URI declarations and prefix mappings that may be required
        if (appContext.containsBean(SchemaCompilerApplicationContext.CODE_GENERATION_WSDL_BINDINGS)) {
            CodeGenerationWsdlBindings wsdlBindings = (CodeGenerationWsdlBindings) appContext
                    .getBean(SchemaCompilerApplicationContext.CODE_GENERATION_WSDL_BINDINGS);

            for (SchemaDeclaration schemaDeclaration : wsdlBindings.getSchemasImports()) {
                addPrefixMapping(schemaDeclaration);
            }

            if (codeGenerator != null) {
                for (SchemaDeclaration schemaDeclaration : wsdlBindings.getDependentSchemas()) {
                    codeGenerator.addCompileTimeDependency(schemaDeclaration);
                }
            }
        }
    }

    /**
     * Adds the namespace mappings required for XML Schema documents.
     */
    private void addXmlSchemaMappings() {
        addPrefixMapping(SchemaDeclarations.SCHEMA_FOR_SCHEMAS);
        addPrefixMapping(SchemaDeclarations.OTA2_APPINFO_SCHEMA);
    }

    /**
     * Adds a namespace mapping for the given schema if one does not already exist.
     * 
     * @param schemaDeclaration
     *            the schema declaration whose mapping is to be added
     */
    private void addPrefixMapping(SchemaDeclaration schemaDeclaration) {
        if (!uriDeclarations.contains(schemaDeclaration.getNamespace())) {
            namespacePrefixMappings.put(schemaDeclaration.getNamespace(),
                    schemaDeclaration.getDefaultPrefix());
            uriDeclarations.add(schemaDeclaration.getNamespace());
        }
    }

    /**
     * Returns the set of all namespaces that are imported by the given schema.
     * 
     * @param schema
     *            the XML schema to analyze
     * @return Set<String>
     */
    private Set<String> getImportedNamespaces(Schema schema) {
        Set<String> nsImports = new HashSet<String>();

        if (schema != null) {
            for (OpenAttrs importOrInclude : schema.getIncludeOrImportOrRedefine()) {
                if (importOrInclude instanceof Import) {
                    Import schemaImport = (Import) importOrInclude;

                    if (schemaImport.getNamespace() != null) {
                        nsImports.add(schemaImport.getNamespace());
                    }
                }
            }
        }
        return nsImports;
    }

    /**
     * Returns the alpha-only characters from the start of the prefix. If the prefix string begins
     * with a number (or is null), the string "ns" will be returned by this method.
     * 
     * @param prefix
     *            the prefix string to process
     * @return String
     */
    private String getPrefixAlphaChars(String prefix) {
        StringBuilder prefixAlphas = new StringBuilder();

        if (prefix != null) {
            for (char ch : prefix.toCharArray()) {
                if (Character.isLetter(ch)) {
                    prefixAlphas.append(ch);
                } else {
                    break;
                }
            }
        }
        if (prefixAlphas.length() == 0) {
            prefixAlphas.append("ns");
        }
        return prefixAlphas.toString();
    }

    /**
     * @see com.sun.xml.bind.marshaller.NamespacePrefixMapper#getPreferredPrefix(java.lang.String,
     *      java.lang.String, boolean)
     */
    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        String preferredPrefix = namespacePrefixMappings.get(namespaceUri);

        return (preferredPrefix == null) ? suggestion : preferredPrefix;
    }

    /**
     * @see com.sun.xml.bind.marshaller.NamespacePrefixMapper#getPreDeclaredNamespaceUris()
     */
    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return uriDeclarations.toArray(new String[uriDeclarations.size()]);
    }

}
