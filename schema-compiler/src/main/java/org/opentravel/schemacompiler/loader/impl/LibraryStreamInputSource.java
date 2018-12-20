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
package org.opentravel.schemacompiler.loader.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.auth.Credentials;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Input source used by loaders that obtain their content from an input stream.
 * 
 * @author S. Livezey
 */
public class LibraryStreamInputSource implements LibraryInputSource<InputStream> {

    private URL libraryUrl;
    private SchemaDeclaration schemaDeclaration;
    private Credentials credentials;
    private Map<String,File> urlCache = new HashMap<>();

    /**
     * Constructor that assigns the URL from which the library's content will be loaded.
     * 
     * @param libraryUrl
     *            the URL location of the library's content
     */
    public LibraryStreamInputSource(URL libraryUrl) {
        this.libraryUrl = URLUtils.normalizeUrl(libraryUrl);
    }

    /**
     * Constructor that assigns the file from which the library's content will be loaded.
     * 
     * @param libraryFile
     *            the file location of the library's content
     */
    public LibraryStreamInputSource(File libraryFile) {
        this.libraryUrl = URLUtils.toURL(libraryFile);
    }

    /**
     * Constructor that explicitly assigns a content stream for the library to that of the given
     * schema declaration instead of allowing the URL location to resolve the location of the file's
     * input stream.
     * 
     * @param libraryUrl
     *            the URL to assign for the library after loading is complete (not used to obtain
     *            content location)
     * @param schemaDeclaration
     *            the schema declaration that provides direct acces to the library file's content
     */
    public LibraryStreamInputSource(URL libraryUrl, SchemaDeclaration schemaDeclaration) {
        this.libraryUrl = libraryUrl;
        this.schemaDeclaration = schemaDeclaration;
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryInputSource#getLibraryURL()
     */
    public URL getLibraryURL() {
        return libraryUrl;
    }

    /**
	 * @see org.opentravel.schemacompiler.loader.LibraryInputSource#setCredentials(org.apache.http.auth.Credentials)
	 */
	@Override
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	/**
     * @see org.opentravel.schemacompiler.loader.LibraryInputSource#getLibraryContent()
     */
	public InputStream getLibraryContent() {
		InputStream contentStream = null;
		
		try {
			if (schemaDeclaration != null) {
				contentStream = schemaDeclaration.getContent(CodeGeneratorFactory.XSD_TARGET_FORMAT);
			}
			if ((contentStream == null) && (libraryUrl != null)) {
				contentStream = getContentStream();
			}
			
		} catch (IOException e) {
			// No action - the contract requires that we eat this exception and return null
		}
		return contentStream;
	}

	/**
	 * Returns a readable input stream for the library URL.
	 * 
	 * @return InputStream
	 * @throws IOException  thrown if the input stream cannot be opened
	 */
	private InputStream getContentStream() throws IOException {
		InputStream contentStream = null;
		
		if (URLUtils.isFileURL(libraryUrl)) {
			try {
				contentStream = new FileInputStream(URLUtils.toFile(libraryUrl));
				
			} catch (IllegalArgumentException e) {
				// No action - use the URL.openStream() method to obtain a connection
			}
		}
		if (contentStream == null) {
			contentStream = new FileInputStream(cacheRemoteFile(libraryUrl));
		}
		return contentStream;
	}
    
    /**
     * Downloads the contents of the given URL to a local file (if not done already)
     * and returns a file handle to the locally-cached content.
     * 
     * @param libraryUrl  the library URL to be cached
     * @return File
     * @throws IOException  thrown if the URL cannot be read from or the cache file written to
     */
    @SuppressWarnings("squid:S1075") // Invalid Sonar finding
	private synchronized File cacheRemoteFile(URL libraryUrl) throws IOException {
		File cacheFile = urlCache.get(libraryUrl.toExternalForm());
		
		if (cacheFile == null) {
			URLConnection urlConnection = libraryUrl.openConnection();
			File tempFolder = new File(System.getProperty("java.io.tmpdir"), "/.ota2");
			tempFolder.mkdirs();
			File tempFile = File.createTempFile("lib", ".otm", tempFolder);
			tempFile.deleteOnExit();
			
			if (credentials != null) {
				String authHeaderStr = credentials.getUserPrincipal().getName() + ":" + credentials.getPassword();
				byte[] authHeaderBytes = Base64.encodeBase64(authHeaderStr.getBytes());
				String authHeader = "Basic " + new String(authHeaderBytes);
				
				urlConnection.setRequestProperty("Authorization", authHeader);
			}
			
			downloadToTempFile(urlConnection, tempFile);
			urlCache.put(libraryUrl.toExternalForm(), tempFile);
			cacheFile = tempFile;
		}
		return cacheFile;
	}

	/**
	 * Downloads content from the URL connection to the specified temp file.
	 * 
	 * @param cnx  the URL connection from which to download content
	 * @param tempFile  the temp file where downloaded content will be stored
	 * @throws IOException  throw if content cannot be downloaded for any reason
	 */
	private void downloadToTempFile(URLConnection cnx, File tempFile) throws IOException {
		try (OutputStream out = new FileOutputStream(tempFile)) {
			try (InputStream contentStream = cnx.getInputStream()) {
				byte[] buffer = new byte[1024];
				int bytesRead;
				
				while ((bytesRead = contentStream.read(buffer, 0, buffer.length)) >= 0) {
					out.write(buffer, 0, bytesRead);
				}
			}
		}
	}
    
}
