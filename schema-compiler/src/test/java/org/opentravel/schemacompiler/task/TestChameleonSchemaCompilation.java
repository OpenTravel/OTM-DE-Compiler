
package org.opentravel.schemacompiler.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.opentravel.schemacompiler.codegen.CodeGeneratorTestAssertions;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.task.TaskFactory;
import org.opentravel.schemacompiler.util.SchemaCompilerTestUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Tests the validation rules and code generation functionality related to legacy chameleon schemas.
 * 
 * @author S. Livezey
 */
public class TestChameleonSchemaCompilation {
	
	@Test
	public void testChameleonSingleDirectInclude_NoConflicts() throws Exception {
		File sourceFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-chameleon/test_library-1.xml");
		File targetFolder = new File(System.getProperty("user.dir") + "/target/codegen-output/chameleon_test");
		CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);
		
		compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
		compilerTask.setCompileSchemas(true);
		
		ValidationFindings findings = compilerTask.compileOutput(sourceFile);
		
		SchemaCompilerTestUtils.printFindings(findings);
		
		// Assert that no errors exist, and that the XML schema document is valid
		assertFalse(findings.hasFinding(FindingType.ERROR));
		CodeGeneratorTestAssertions.assertValidXsd( getTestOutput("schemas/chameleon_test_1_3_0_0.xsd") );
		CodeGeneratorTestAssertions.assertValidXsd( getTestOutput("schemas/legacy/chameleon_schema-1_Ext.xsd") );
	}
	
	@Test
	public void testChameleonMultipleDirectIncludes_WithConflicts() throws Exception {
		File sourceFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-chameleon/test_library-2.xml");
		File targetFolder = new File(System.getProperty("user.dir") + "/target/codegen-output/chameleon_test");
		CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);
		
		compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
		compilerTask.setCompileSchemas(true);
		
		ValidationFindings findings = compilerTask.compileOutput(sourceFile);
		
		SchemaCompilerTestUtils.printFindings(findings);
		
		// Assert that a validation error exists for conflicting chameleon types
		assertTrue(findings.hasFinding(FindingType.ERROR));
		assertEquals(1, findings.getFindingsAsList(FindingType.ERROR).size());
		assertEquals("org.opentravel.schemacompiler.TLLibrary.namedMembers.DUPLICATE_CHAMELEON_SYMBOLS",
				findings.getAllFindingsAsList().get(0).getMessageKey());
	}
	
	@Test
	public void testChameleonDirectAndIndirectIncludes1_NoConflicts() throws Exception {
		File sourceFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-chameleon/test_library-3.xml");
		File targetFolder = new File(System.getProperty("user.dir") + "/target/codegen-output/chameleon_test");
		CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);
		
		compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
		compilerTask.setCompileSchemas(true);
		
		ValidationFindings findings = compilerTask.compileOutput(sourceFile);
		
		SchemaCompilerTestUtils.printFindings(findings);
		
		// Assert that no errors exist, and that the XML schema document is valid
		assertFalse(findings.hasFinding(FindingType.ERROR));
		CodeGeneratorTestAssertions.assertValidXsd( getTestOutput("schemas/chameleon_test_3_3_0_0.xsd") );
		CodeGeneratorTestAssertions.assertValidXsd( getTestOutput("schemas/legacy/chameleon_schema-1_Ext.xsd") );
	}
	
	@Test
	public void testChameleonMultipleIndirectIncludes_NoConflicts() throws Exception {
		File sourceFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-chameleon/test_library-4.xml");
		File targetFolder = new File(System.getProperty("user.dir") + "/target/codegen-output/chameleon_test");
		CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);
		
		compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
		compilerTask.setCompileSchemas(true);
		
		ValidationFindings findings = compilerTask.compileOutput(sourceFile);
		
		SchemaCompilerTestUtils.printFindings(findings);
		
		// Assert that no errors exist, and that the XML schema document is valid
		assertFalse(findings.hasFinding(FindingType.ERROR));
		CodeGeneratorTestAssertions.assertValidXsd( getTestOutput("schemas/chameleon_test_4_3_0_0.xsd") );
	}
	
	@Test
	public void testChameleonMultipleIndirectIncludes_WithConflicts() throws Exception {
		File sourceFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-chameleon/test_library-5.xml");
		File targetFolder = new File(System.getProperty("user.dir") + "/target/codegen-output/chameleon_test");
		CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);
		
		compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
		compilerTask.setCompileSchemas(true);
		
		ValidationFindings findings = compilerTask.compileOutput(sourceFile);
		
		SchemaCompilerTestUtils.printFindings(findings);
		
		// Assert that a validation error exists for conflicting chameleon types
		assertTrue(findings.hasFinding(FindingType.ERROR));
		assertEquals(1, findings.getFindingsAsList(FindingType.ERROR).size());
		assertEquals("org.opentravel.schemacompiler.TLLibrary.namedMembers.DUPLICATE_CHAMELEON_SYMBOLS",
				findings.getAllFindingsAsList().get(0).getMessageKey());
	}
	
	@Test
	public void testChameleonDirectAndIndirectIncludes2_NoConflicts() throws Exception {
		File sourceFile = new File(SchemaCompilerTestUtils.getBaseLibraryLocation() + "/test-package-chameleon/test_library-6.xml");
		File targetFolder = new File(System.getProperty("user.dir") + "/target/codegen-output/chameleon_test");
		CompileAllCompilerTask compilerTask = TaskFactory.getTask(CompileAllCompilerTask.class);
		
		compilerTask.setOutputFolder(targetFolder.getAbsolutePath());
		compilerTask.setCompileSchemas(true);
		
		ValidationFindings findings = compilerTask.compileOutput(sourceFile);
		
		SchemaCompilerTestUtils.printFindings(findings);
		
		// Assert that no errors exist, and that the XML schema document is valid
		assertFalse(findings.hasFinding(FindingType.ERROR));
		CodeGeneratorTestAssertions.assertValidXsd( getTestOutput("schemas/chameleon_test_6_3_0_0.xsd") );
	}
	
	private File getTestOutput(String filename) {
		return new File(System.getProperty("user.dir") + "/target/codegen-output/chameleon_test/" + filename);
	}
	
}
