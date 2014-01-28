
package org.opentravel.schemacompiler.loader.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.opentravel.ns.ota2.librarycatalog_v01_00.Catalog;
import org.opentravel.ns.ota2.librarycatalog_v01_00.CatalogEntry;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.util.URLUtils;
import org.xml.sax.SAXException;

/**
 * <code>LibraryNamespaceResolver</code> implementation that obtains its namespace mappings
 * from a configuration file.
 * 
 * @author S. Livezey
 */
public class CatalogLibraryNamespaceResolver extends MapLibraryNamespaceResolver {
	
	public static final String DEFAULT_CATALOG_FILE = "library-catalog.xml";
	
	private static final String SCHEMA_CONTEXT = ":org.opentravel.ns.ota2.librarycatalog_v01_00";
	
	private URL catalogFolder = null;
	
	/**
	 * Default constructor that expects the catalog file to be readable from the current directory
	 * location (<code>{user.dir}/library-catalog.xml</code>).
	 * 
	 * @throws LibraryLoaderException  thrown if the default catalog file cannot be loaded
	 */
	public CatalogLibraryNamespaceResolver() throws LibraryLoaderException {
		this(new File(System.getProperty("user.dir"), DEFAULT_CATALOG_FILE));
	}
	
	/**
	 * Constructor that obtains catalog mappings from the file at the specified URL.
	 * 
	 * @param catalogUrl  the location of the catalog mapping file
	 * @throws LibraryLoaderException  thrown if the catalog file cannot be loaded
	 */
	public CatalogLibraryNamespaceResolver(URL catalogUrl) throws LibraryLoaderException {
		try {
			String folderUrl = catalogUrl.toExternalForm();
			int folderIdx = folderUrl.lastIndexOf('/');
			InputStream is = null;
			
			if (folderIdx >= 0) {
				folderUrl = folderUrl.substring(0, folderIdx);
			} else {
				folderUrl = folderUrl.substring(0, folderUrl.length() - catalogUrl.getFile().length());
			}
			catalogFolder = new URL(folderUrl);
			
			if (URLUtils.isFileURL(catalogUrl)) {
				try {
					is = new FileInputStream(URLUtils.toFile(catalogUrl));
					
				} catch (IllegalArgumentException e) {
					// No error - use the openStream() method to establish a connection
				}
			}
			if (is == null) {
				is = catalogUrl.openStream();
			}
			initCatalog(is);
			
		} catch (IOException e) {
			throw new LibraryLoaderException("Error loading catalog file.", e);
		}
	}
	
	/**
	 * Constructor that obtains catalog mappings from the specified file.
	 * 
	 * @param catalogFile  the catalog mapping file
	 * @throws LibraryLoaderException  thrown if the catalog file cannot be loaded
	 */
	public CatalogLibraryNamespaceResolver(File catalogFile) throws LibraryLoaderException {
		try {
			File parentFolder = catalogFile.getAbsoluteFile().getParentFile();
			
			catalogFolder = URLUtils.toURL(parentFolder);
			initCatalog(new FileInputStream(catalogFile));
			
		} catch (IOException e) {
			throw new LibraryLoaderException("Error loading catalog file.", e);
		}
	}
	
	/**
	 * Initializes the content of the catalog using mappings from the input stream provided.
	 * 
	 * @param is  the input stream from which to load catalog mappings
	 * @throws LibraryLoaderException  thrown if the catalog file cannot be loaded
	 */
	private void initCatalog(InputStream is) throws LibraryLoaderException {
		try {
			// Load and parse the catalog content from the stream
    		SchemaFactory schemaFactory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
    		InputStream schemaStream = SchemaDeclarations.OTA2_CATALOG_SCHEMA.getContent();
    		Schema validationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
			Unmarshaller unmarshaller = JAXBContext.newInstance(SCHEMA_CONTEXT).createUnmarshaller();
			unmarshaller.setSchema(validationSchema);
			JAXBElement<?> documentElement = (JAXBElement<?>) unmarshaller.unmarshal(is);
			
			// Populate the namespace mappings with elements from the catalog
			Catalog catalog = (Catalog) documentElement.getValue();
			
			if (catalog != null) {
				for (CatalogEntry catalogEntry : catalog.getCatalogEntry()) {
					for (String location : catalogEntry.getLocation()) {
						addNamespaceMapping(catalogEntry.getNamespace(), location);
					}
				}
			}
		} catch (IOException e) {
			throw new LibraryLoaderException(e);
		} catch (URISyntaxException e) {
			throw new LibraryLoaderException(e);
		} catch (JAXBException e) {
			throw new LibraryLoaderException(e);
		} catch (SAXException e) {
			throw new LibraryLoaderException(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Throwable t) {}
			}
		}
	}

	/**
	 * @see org.opentravel.schemacompiler.loader.impl.MapLibraryNamespaceResolver#getRelativeUrlBase()
	 */
	@Override
	protected URL getRelativeUrlBase() throws MalformedURLException {
		return (catalogFolder != null) ? catalogFolder : super.getRelativeUrlBase();
	}
	
}
