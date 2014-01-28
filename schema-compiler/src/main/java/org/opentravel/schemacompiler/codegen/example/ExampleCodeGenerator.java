package org.opentravel.schemacompiler.codegen.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions.DetailLevel;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.xml.XMLPrettyPrinter;
import org.w3c.dom.Document;

/**
 * Code generator that produces sample XML output for <code>NamedEntity</code> members of the
 * generated libraries.
 * 
 * @author S. Livezey
 */
public class ExampleCodeGenerator extends AbstractCodeGenerator<TLModelElement> {

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#doGenerateOutput(org.opentravel.schemacompiler.model.TLModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    public void doGenerateOutput(TLModelElement source, CodeGenerationContext context)
            throws CodeGenerationException {
        OutputStream out = null;
        try {
            ExampleDocumentBuilder exampleBuilder = new ExampleDocumentBuilder(getOptions(context))
                    .setModelElement((NamedEntity) source);

            // Register the schema location for each library in the model
            registerSchemaLocations(exampleBuilder, source.getOwningModel(), context);

            // Generate the XML document and send formatted content to the output file
            Document domDocument = exampleBuilder.buildDomTree();
            File outputFile = getOutputFile(source, context);

            out = new FileOutputStream(outputFile);
            new XMLPrettyPrinter().formatDocument(domDocument, out);
            out.close();
            out = null;

            addGeneratedFile(outputFile);

        } catch (Throwable t) {
            throw new CodeGenerationException(t);

        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Throwable t) {
            }
        }
    }

    /**
     * Constructs the <code>ExampleGeneratorOptions</code> using the code generation context
     * provided.
     * 
     * @param context
     *            the code generation context
     * @return ExampleGeneratorOptions
     */
    private ExampleGeneratorOptions getOptions(CodeGenerationContext context) {
        ExampleGeneratorOptions options = new ExampleGeneratorOptions();
        String detailLevel = context.getValue(CodeGenerationContext.CK_EXAMPLE_DETAIL_LEVEL);
        String exampleContext = context.getValue(CodeGenerationContext.CK_EXAMPLE_CONTEXT);
        Integer maxRepeat = context.getIntValue(CodeGenerationContext.CK_EXAMPLE_MAX_REPEAT);
        Integer maxDepth = context.getIntValue(CodeGenerationContext.CK_EXAMPLE_MAX_DEPTH);

        if (detailLevel != null) {
            if (detailLevel.equalsIgnoreCase("MINIMUM")) {
                options.setDetailLevel(DetailLevel.MINIMUM);
            }
        }
        if (exampleContext != null) {
            options.setExampleContext(exampleContext);
        }
        if (maxRepeat != null) {
            options.setMaxRepeat(maxRepeat.intValue());
        }
        if (maxDepth != null) {
            options.setMaxRecursionDepth(maxDepth.intValue());
        }
        return options;
    }

    /**
     * Registers schema output locations for all libraries in the specified model.
     * 
     * @param builder
     *            the builder for which schema locations should be registered
     * @param model
     *            the model containing all possible libraries to be resolved
     * @param context
     *            the code generation context
     */
    private void registerSchemaLocations(ExampleDocumentBuilder builder, TLModel model,
            CodeGenerationContext context) {
        if (model != null) {
            // Register the schema locations of all libraries
            for (AbstractLibrary library : model.getAllLibraries()) {
                String schemaPath = context
                        .getValue(CodeGenerationContext.CK_EXAMPLE_SCHEMA_RELATIVE_PATH);
                String schemaFilename = getFilenameBuilder().buildFilename(library, "xsd");
                String schemaLocation;

                if (library instanceof TLLibrary) {
                    schemaLocation = schemaPath + schemaFilename;

                } else if (library instanceof XSDLibrary) {
                    schemaLocation = schemaPath + getLegacySchemaOutputLocation(context) + "/"
                            + schemaFilename;

                } else { // Built-in library
                    schemaLocation = schemaPath + getBuiltInSchemaOutputLocation(context) + "/"
                            + schemaFilename;
                }
                builder.addSchemaLocation(library.getNamespace(), schemaLocation);
            }
        }
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(org.opentravel.schemacompiler.model.TLModelElement,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected File getOutputFile(TLModelElement source, CodeGenerationContext context) {
        if (source == null) {
            throw new NullPointerException("Source model element cannot be null.");
        }
        AbstractLibrary library = getLibrary(source);
        URL libraryUrl = (library == null) ? null : library.getLibraryUrl();
        File outputFolder = getOutputFolder(context, libraryUrl);
        String filename = getFilenameBuilder().buildFilename(source, "xml");

        return new File(outputFolder, filename);
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
     */
    @Override
    protected CodeGenerationFilenameBuilder<TLModelElement> getDefaultFilenameBuilder() {
        return new CodeGenerationFilenameBuilder<TLModelElement>() {

            public String buildFilename(TLModelElement item, String fileExtension) {
                String fileExt = ((fileExtension == null) || (fileExtension.length() == 0)) ? ""
                        : ("." + fileExtension);
                String itemName;

                if ((item instanceof TLFacet)
                        && (((TLFacet) item).getOwningEntity() instanceof TLOperation)) {
                    TLFacet facetItem = (TLFacet) item;
                    itemName = ((TLOperation) facetItem.getOwningEntity()).getName()
                            + facetItem.getFacetType().getIdentityName();

                } else if (item instanceof NamedEntity) {
                    itemName = ((NamedEntity) item).getLocalName();
                } else {
                    itemName = "";
                }
                return itemName.replaceAll("_", "") + fileExt;
            }

        };
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(org.opentravel.schemacompiler.model.TLModelElement)
     */
    @Override
    protected AbstractLibrary getLibrary(TLModelElement source) {
        return (source instanceof LibraryElement) ? ((LibraryElement) source).getOwningLibrary()
                : null;
    }

    /**
     * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#isSupportedSourceObject(org.opentravel.schemacompiler.model.TLModelElement)
     */
    @Override
    protected boolean isSupportedSourceObject(TLModelElement source) {
        return (source instanceof NamedEntity) && !(source instanceof TLService);
    }

}
