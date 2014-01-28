
package org.opentravel.schemacompiler.security.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Properties;

import org.opentravel.schemacompiler.security.PasswordHelper;
import org.opentravel.schemacompiler.security.ProtectedNamespaceGroup;
import org.opentravel.schemacompiler.security.SchemaCompilerSecurityException;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Default implementation of the <code>ProtectedNamespaceGroup</code> that determines write access
 * to a protected namespace by interrogating a remote password file.
 * 
 * @author S. Livezey
 */
public class DefaultProtectedNamespaceGroup implements ProtectedNamespaceGroup {
	
	private static final String CREDENTIAL_FOLDER = "/.ota2";
	
	private String groupId;
	private String groupTitle;
	private List<String> protectedNamespaceUris;
	private URL credentialUrl;
	
	/**
	 * @see org.opentravel.schemacompiler.security.ProtectedNamespaceGroup#getGroupId()
	 */
	@Override
	public String getGroupId() {
		return groupId;
	}
	
	/**
	 * Assigns the ID of this protected namespace group.
	 * 
	 * @param groupId  the ID of the namespace group to assign
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.security.ProtectedNamespaceGroup#getGroupTitle()
	 */
	@Override
	public String getGroupTitle() {
		return groupTitle;
	}
	
	/**
	 * Assigns the user-readable title of this protected namespace group.
	 * 
	 * @param groupTitle  the title of the namespace group to assign
	 */
	public void setGroupTitle(String groupTitle) {
		this.groupTitle = groupTitle;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.security.ProtectedNamespaceGroup#getProtectedNamespaceUris()
	 */
	@Override
	public List<String> getProtectedNamespaceUris() {
		return protectedNamespaceUris;
	}
	
	/**
	 * Assigns the list of protected namespace URI's for this group.
	 * 
	 * @param protectedNamespaceUris  the list of protected namespace URI's
	 */
	public void setProtectedNamespaceUris(List<String> protectedNamespaceUris) {
		this.protectedNamespaceUris = protectedNamespaceUris;
	}
	
	/**
	 * Returns the URL of the file where userId and password credentials can be retrieved.
	 * 
	 * @return URL
	 */
	public URL getCredentialUrl() {
		return credentialUrl;
	}
	
	/**
	 * Assigns the URL of the file where userId and password credentials can be retrieved.
	 * 
	 * @param credentialUrl  the credential file URL to assign
	 */
	public void setCredentialUrl(URL credentialUrl) {
		this.credentialUrl = credentialUrl;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.security.ProtectedNamespaceGroup#hasWriteAccess(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean hasWriteAccess(String userId, String password) throws SchemaCompilerSecurityException {
		boolean hasAccess = false;
		try {
			Properties credentials = loadCredentialsFile();
			
			if (credentials.containsKey(userId)) {
				hasAccess = PasswordHelper.isMatch(password, credentials.getProperty(userId));
			}
			
		} catch (IOException e) {
			throw new SchemaCompilerSecurityException(e);
		}
		return hasAccess;
	}
	
	/**
	 * Loads the content of the credential file and returns it as name/value pairs.  The keys of
	 * the map that is returned are the user ID's, and the values are the encrypted passwords.
	 * 
	 * @return Properties
	 * @throws IOException  thrown if the credentials file cannot be accessed
	 */
	private Properties loadCredentialsFile() throws IOException {
		BufferedReader reader = null;
		
		try {
			Properties credentials = new Properties();
			reader = new BufferedReader(new FileReader( getCredentialsFile() ));
			String line;
			
			while ((line = reader.readLine()) != null) {
				if (line.trim().startsWith("#") || (line.indexOf(':') < 0)) {
					continue;
				}
				String[] lineParts = line.split(":");
				String userId = lineParts[0];
				String encryptedPassword = lineParts[1];
				
				credentials.put(userId, encryptedPassword);
			}
			return credentials;
			
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (Throwable t) {}
		}
	}
	
	/**
	 * Returns a handle to the credentials file for the protected namespace.  The file handle that
	 * is returned is guranteed to exist on the local file system.
	 * 
	 * @return File
	 * @throws IOException  thrown if the credentials file does not exist or cannot be accessed
	 */
	private File getCredentialsFile() throws IOException {
		File credentialsFile;
		
		if (URLUtils.isFileURL(credentialUrl)) {
			credentialsFile = URLUtils.toFile(credentialUrl);
			
		} else {
			loadCredentialsFileFromRemoteHost();
			credentialsFile = getCachedCredentialsFile();
		}
		if (!credentialsFile.exists()) {
			throw new FileNotFoundException(
					"Unable to access protected namespace credentials at: " + credentialUrl.toExternalForm());
		}
		return credentialsFile;
	}
	
	/**
	 * Loads the credentials file from a remote host.  If the connection is established using
	 * HTTP, the download will only occur if the remote file is newer than the one that is stored
	 * in our local cache.
	 * 
	 * <p>NOTE: If the remote connection cannot be established, this method will return fail silently
	 * (no exception).  This will have the effect of using the cached copy of the remote file
	 */
	private void loadCredentialsFileFromRemoteHost() {
		InputStream is = null;
		try {
			URLConnection cnx = credentialUrl.openConnection();
			File cacheFile = getCachedCredentialsFile();
			boolean cacheUpdateRequired = true;
			
			if (cnx instanceof HttpURLConnection) {
				HttpURLConnection httpCnx = (HttpURLConnection) cnx;
				
				if (cacheFile.exists()) {
					httpCnx.setIfModifiedSince(cacheFile.lastModified());
				}
			}
			
			cnx.connect();
			
			// If an HTTP connection reports response code 304 (not modified), there is
			// no need to update our cached file.
			if (cnx instanceof HttpURLConnection) {
				HttpURLConnection httpCnx = (HttpURLConnection) cnx;
				
				if (httpCnx.getResponseCode() == 304) {
					cacheUpdateRequired = false;
				}
			}
			
			if (cacheUpdateRequired) {
				if (!cacheFile.getParentFile().exists()) {
					// Automatically create the cache folder in the user's home directory if it does not already exist
					cacheFile.getParentFile().mkdirs();
				}
				BufferedReader reader = new BufferedReader(new InputStreamReader(is = cnx.getInputStream()));
				PrintWriter writer = new PrintWriter(new FileWriter(cacheFile));
				String line;
				
				while ((line = reader.readLine()) != null) {
					writer.println(line);
				}
				writer.flush();
				writer.close();
			}
			
		} catch (IOException e) {
			// Ignore exception - use the cached copy of the file if a remote connection cannot be established
			
		} finally {
			try {
				if (is != null) is.close();
			} catch (Throwable t) {}
		}
	}
	
	/**
	 * Returns a handle to the cached copy of the credentials file for this protected
	 * namespace group.
	 * 
	 * @return File
	 */
	private File getCachedCredentialsFile() {
		String filename = groupId + ".txt";
		
		filename = filename.replaceAll(" ", "_");
		return new File(System.getProperty("user.home") + CREDENTIAL_FOLDER, filename);
	}
	
}
