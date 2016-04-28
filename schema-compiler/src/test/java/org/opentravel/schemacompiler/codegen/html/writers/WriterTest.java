/**
 * 
 */
package org.opentravel.schemacompiler.codegen.html.writers;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.opentravel.schemacompiler.model.TLAdditionalDocumentationItem;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationItem;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.TestLibraryProvider;

/**
 * @author Eric.Bronson
 *
 */
public class WriterTest {

	protected static Configuration config;
	
	public static final File DEST_DIR = new File("test");

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Configuration.reset();
		config = Configuration.getInstance();
		config.setDestDirName(DEST_DIR + File.separator);
		config.stylesheetfile = "stylesheet-test.css";
		config.setModel(TestLibraryProvider.getModel());
		config.windowtitle="TestProject";
		config.doctitle="TestProject.otp";		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		cleanDirectory(DEST_DIR);
	}
	
	/**
	 * Delete all files starting from the given directory. Then delete the
	 * directory.
	 * 
	 * @param dir
	 * @return
	 */
	public static void cleanDirectory(File dir) {
		if (dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				cleanDirectory(f);
			}
		}
		dir.delete();
	}

	protected TLDocumentation getTestDocumentation() {
		TLDocumentation doc = new TLDocumentation();
		doc.setDescription("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. ");
		TLDocumentationItem item = new TLDocumentationItem();
		item.setText("This is a deprecation.");
		doc.addDeprecation(item);
		item = new TLDocumentationItem();
		item.setText("This is an implementer.");
		doc.addImplementer(item);
		item = new TLDocumentationItem();
		item.setText("This is a reference.");
		doc.addReference(item);
		item = new TLDocumentationItem();
		item.setText("This is a more info.");
		doc.addMoreInfo(item);
		item = new TLAdditionalDocumentationItem();
		item.setText("This is an other doc.");
		doc.addOtherDoc((TLAdditionalDocumentationItem) item);
		return doc;
	}
}
