package org.opentravel.schemacompiler.task;

import java.io.File;
import java.util.Collection;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.impl.LibraryFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.util.SchemaCompilerException;

/**
 * Compiler task used to generate full (non-trimmed) schema output for the libraries of a model.
 * 
 * @author S. Livezey
 */
public class SchemaCompilerTask extends AbstractSchemaCompilerTask implements
        SchemaCompilerTaskOptions {

    /**
     * Constructor that specifies the filename of the project for which schemas are being compiled.
     * 
     * @param projectFilename
     *            the name of the project (.otp) file
     */
    public SchemaCompilerTask(String projectFilename) {
        this.projectFilename = projectFilename;
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractCompilerTask#generateOutput(java.util.Collection,
     *      java.util.Collection)
     */
    @Override
    protected void generateOutput(Collection<TLLibrary> userDefinedLibraries,
            Collection<XSDLibrary> legacySchemas) throws SchemaCompilerException {
        CodeGenerationContext context = createContext();

        // Generate schemas for all of the user-defined libraries
        compileXmlSchemas(userDefinedLibraries, legacySchemas, context, null, null);

        // Generate example files if required
        if (isGenerateExamples()) {
            generateExampleArtifacts(userDefinedLibraries, context,
                    new LibraryFilenameBuilder<AbstractLibrary>(), null);
        }
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractSchemaCompilerTask#getExampleOutputFolder(org.opentravel.schemacompiler.model.LibraryMember,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected String getExampleOutputFolder(LibraryMember libraryMember,
            CodeGenerationContext context) {
        String libraryFolderName = "examples/"
                + new LibraryFilenameBuilder<AbstractLibrary>().buildFilename(
                        libraryMember.getOwningLibrary(), "");
        String rootOutputFolder = context.getValue(CodeGenerationContext.CK_OUTPUT_FOLDER);

        if (rootOutputFolder == null) {
            rootOutputFolder = System.getProperty("user.dir");
        }
        return new File(rootOutputFolder, libraryFolderName).getAbsolutePath();
    }

    /**
     * @see org.opentravel.schemacompiler.task.AbstractSchemaCompilerTask#getSchemaRelativeFolderPath(org.opentravel.schemacompiler.model.LibraryMember,
     *      org.opentravel.schemacompiler.codegen.CodeGenerationContext)
     */
    @Override
    protected String getSchemaRelativeFolderPath(LibraryMember libraryMember,
            CodeGenerationContext context) {
        return "../../";
    }

}
