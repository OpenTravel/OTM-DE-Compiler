package org.opentravel.schemacompiler.security.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.schemacompiler.config.FileResource;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.security.AuthenticationProvider;
import org.opentravel.schemacompiler.security.PasswordHelper;

/**
 * Authentication provider that performs its checks against a credentials file on the repository's
 * file system.
 * 
 * @author S. Livezey
 */
public class FileAuthenticationProvider implements AuthenticationProvider {

    public static final String CREDENTIALS_FILENAME = "repository-users.txt";

    private static Log log = LogFactory.getLog(FileAuthenticationProvider.class);

    private CredentialsFileResource credentialsResource;

    /**
     * Constructor that provides the location of the web service's repository on the local file
     * system.
     * 
     * @param repositoryLocation
     *            the file system location of the web service's repository
     */
    public FileAuthenticationProvider(File repositoryLocation) {
        credentialsResource = new CredentialsFileResource(repositoryLocation);
    }

    /**
     * @see org.opentravel.schemacompiler.security.AuthenticationProvider#isValidUser(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public boolean isValidUser(String userId, String password) {
        Properties userCredentials = credentialsResource.getResource();
        boolean isValid = false;

        if ((userId != null) && (password != null) && userCredentials.containsKey(userId)) {
            String encryptedPassword = userCredentials.getProperty(userId);
            isValid = password.equals(PasswordHelper.decrypt(encryptedPassword));
        }
        return isValid;
    }

    /**
     * Loads the list of all locally-managed user ID's for the repository.
     * 
     * @param repositoryLocation
     *            the root folder location of the repository
     * @return String[]
     */
    public static String[] getAllUserIds(File repositoryLocation) {
        Properties userAccounts = loadUsers(repositoryLocation);
        List<String> userIds = new ArrayList<String>();

        for (Object userId : userAccounts.keySet()) {
            if (userId instanceof String) {
                userIds.add((String) userId);
            }
        }
        return userIds.toArray(new String[userIds.size()]);
    }

    /**
     * Saves the user ID and password provided. If the user does not yet exist, the account will be
     * created automatically. If the account already exists, its password credential will be
     * replaced with the one provided. If the 'deleteUser' flag is true, the account will be
     * deleted.
     * 
     * @param userId
     *            the ID of the user account to save
     * @param password
     *            the clear-text password credential for the user (ignored
     * @param deleteUser
     *            indicates that the user's account should be deleted
     * @param fileManager
     *            the file manager for the repository
     * @throws IOException
     *             thrown if the user's credentials cannot be saved
     */
    public synchronized static void saveUserCredentials(String userId, String password,
            boolean deleteUser, RepositoryFileManager fileManager) throws IOException {
        Properties userAccounts = loadUsers(fileManager.getRepositoryLocation());
        File usersFile = new File(fileManager.getRepositoryLocation(), CREDENTIALS_FILENAME);
        PrintStream out = null;
        boolean success = false;
        fileManager.startChangeSet();
        try {
            List<String> userIds = new ArrayList<String>();

            // Build a sorted list of all user ID's
            for (Object _userId : userAccounts.keySet()) {
                userIds.add((String) _userId);
            }
            if (!userIds.contains(userIds)) {
                userIds.add(userId);
            }
            Collections.sort(userIds);

            // Save the updated file content
            fileManager.addToChangeSet(usersFile);
            out = new PrintStream(new FileOutputStream(usersFile));

            for (String _userId : userIds) {
                if (deleteUser && _userId.equals(userId)) {
                    continue;
                }
                String encryptedPassword = _userId.equals(userId) ? PasswordHelper
                        .encrypt(password) : userAccounts.getProperty(_userId);
                out.println(_userId + ":" + encryptedPassword);
            }
            out.close();
            fileManager.commitChangeSet();
            success = true;
            out = null;

        } catch (RepositoryException e) {
            throw new IOException("Error committing change set to file system.", e);

        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Throwable t) {
            }
            try {
                if (!success)
                    fileManager.rollbackChangeSet();
            } catch (Throwable t) {
            }
        }
    }

    /**
     * Returns the contents of the user account file for the local repository.
     * 
     * @param repositoryLocation
     *            the root folder location of the repository
     * @return Properties
     * @throws IOException
     *             thrown if the user account file cannot be loaded
     */
    private static Properties loadUsers(File repositoryLocation) {
        File usersFile = new File(repositoryLocation, CREDENTIALS_FILENAME);
        Properties userProps = new Properties();

        if (usersFile.exists()) {
            InputStream fis = null;
            try {

                fis = new FileInputStream(usersFile);
                userProps.load(fis);

            } catch (IOException e) {
                log.error("Unable to load user accounts.", e);

            } finally {
                try {
                    if (fis != null)
                        fis.close();
                } catch (Throwable t) {
                }
            }
        }
        return userProps;
    }

    /**
     * File resource that obtains its content from the OTM credentials file.
     * 
     * @author S. Livezey
     */
    private static class CredentialsFileResource extends FileResource<Properties> {

        /**
         * Constructor that provides the location of the web service's repository on the local file
         * system.
         * 
         * @param repositoryLocation
         *            the file system location of the web service's repository
         */
        public CredentialsFileResource(File repositoryLocation) {
            super(new File(repositoryLocation, CREDENTIALS_FILENAME));
        }

        /**
         * @see org.opentravel.schemacompiler.config.FileResource#getDefaultResourceValue()
         */
        @Override
        protected Properties getDefaultResourceValue() {
            return new Properties();
        }

        /**
         * @see org.opentravel.schemacompiler.config.FileResource#loadResource(java.io.File)
         */
        @Override
        protected Properties loadResource(File dataFile) throws IOException {
            BufferedReader reader = null;
            try {
                Properties credentials = new Properties();
                reader = new BufferedReader(new FileReader(dataFile));
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
                    if (reader != null)
                        reader.close();
                } catch (Throwable t) {
                }
            }
        }

    }

}
