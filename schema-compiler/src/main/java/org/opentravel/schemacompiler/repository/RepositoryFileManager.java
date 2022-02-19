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

package org.opentravel.schemacompiler.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.repositoryinfo_v01_00.RepositoryInfoType;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.loader.impl.MultiVersionLibraryModuleLoader;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.repository.impl.LibraryContentWrapper;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.saver.impl.Library15FileSaveHandler;
import org.opentravel.schemacompiler.saver.impl.Library16FileSaveHandler;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.FileUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.xml.NamespacePrefixMapper;
import org.opentravel.schemacompiler.xml.XMLGregorianCalendarConverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

/**
 * Base class that handles all low-level file I/O for an OTA2.0 repository. Each time a file is written by the
 * 'saveFile()' method it is added to a change set that is maintained for the current thread. Once a repository
 * operation has been completed, the files in the change set must be committed or rolled back by the repository manager.
 * 
 * @author S. Livezey
 */
public abstract class RepositoryFileManager {

    private static final String METADATA_FILE_SUFFIX = "-info.xml";

    public static final String REPOSITORY_METADATA_FILENAME = "repository-metadata.xml";
    public static final String REPOSITORY_HOME_FOLDER = ".ota2/";
    public static final String REPOSITORY_LOCATION_FILENAME = "repository-location.txt";
    public static final String DEFAULT_REPOSITORY_LOCATION = "repository/";
    public static final String WIP_FOLDER_LOCATION = "wip/";
    public static final String PROJECTS_FOLDER_LOCATION = "projects/";
    public static final String NAMESPACE_ID_FILENAME = "nsid.txt";

    private static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema:org.opentravel.ns.ota2.repositoryinfo_v01_00";
    private static final String REPOSITORY_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/RepositoryInfo_v01_00";

    private static Logger log = LogManager.getLogger( RepositoryFileManager.class );

    private static final ThreadLocal<Set<File>> changeSet = ThreadLocal.withInitial( HashSet::new );

    private static javax.xml.validation.Schema repositoryValidationSchema;
    protected static ObjectFactory objectFactory = new ObjectFactory();
    protected static JAXBContext jaxbContext;

    private static File ota2HomeFolder;
    private static File defaultRepositoryLocation;
    private static Map<String,String> namespaceIdCache = new HashMap<>();

    private ThreadLocal<String> currentUserId = ThreadLocal.withInitial( () -> null );

    private File repositoryLocation;

    /**
     * Constructor that initializes a new instance used for managing and retrieving files in an OTA2.0 repository at the
     * specified file location.
     * 
     * @param repositoryLocation the folder location where the OTA2.0 repository resides on the local file system
     */
    public RepositoryFileManager(File repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
    }

    /**
     * Returns the folder location where the OTA2.0 repository and other configuration files are located. Typically,
     * this folder will be located in the "/.ota2" folder of the current user's home directory. That location can be
     * overridden, however, by assigning an alternate location to the "ota2.home" system property.
     * 
     * <p>
     * NOTE: The folder location returned by this method is not guranteed to exist on the local file system.
     * 
     * @return File
     */
    public static File getOta2HomeFolder() {
        return ota2HomeFolder;
    }

    /**
     * Returns the default location of the OTA2.0 repository on the local file system.
     * 
     * @return File
     */
    public static File getDefaultRepositoryLocation() {
        return defaultRepositoryLocation;
    }

    /**
     * Returns the shared JAXB context to use when parsing and saving repository XML files.
     * 
     * @return JAXBContext
     */
    public static JAXBContext getSharedJaxbContext() {
        return jaxbContext;
    }

    /**
     * Returns the location of the OTA2.0 repository that is managed by this <code>RepositoryManager</code> instance.
     * 
     * @return File
     */
    public File getRepositoryLocation() {
        return repositoryLocation;
    }

    /**
     * Assigns the location of the OTA2.0 repository that is managed by this <code>RepositoryManager</code> instance.
     * 
     * @param repositoryLocation the folder location to assign
     */
    protected void setRepositoryLocation(File repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
    }

    /**
     * Assigns the location of the OTA2.0 repository on the local file system.
     * 
     * @param repositoryLocation the root directory of the OTA2.0 repository
     * @throws RepositoryException thrown if the repository location cannot be persisted
     */
    public static void setDefaultRepositoryLocation(File repositoryLocation) throws RepositoryException {
        File repositoryLocationFile = new File( getOta2HomeFolder(), REPOSITORY_LOCATION_FILENAME );
        boolean writeFile = repositoryLocationFile.exists();

        if (!writeFile) {
            File defaultRepositoryLocation = new File( getOta2HomeFolder(), DEFAULT_REPOSITORY_LOCATION );
            writeFile = !repositoryLocation.getAbsolutePath().equals( defaultRepositoryLocation.getAbsolutePath() );
        }

        // Only write out the location file if one already exists or the location is to a
        // non-default directory
        if (writeFile) {
            if (!repositoryLocationFile.getParentFile().exists()) {
                repositoryLocationFile.getParentFile().mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter( new FileWriter( repositoryLocationFile ) )) {
                writer.write( repositoryLocation.getAbsolutePath() );

            } catch (IOException e) {
                throw new RepositoryException( "Unable to persist the location of the new repository.", e );
            }
        }
        RepositoryFileManager.defaultRepositoryLocation = repositoryLocation;
    }

    /**
     * Loads the XML content from the repository meta-data file at the specified location and returns it as a JAXB
     * object.
     * 
     * @return RepositoryInfoType
     * @throws RepositoryException thrown if the specified file cannot be loaded
     */
    public RepositoryInfoType loadRepositoryMetadata() throws RepositoryException {
        File repositoryMetadataFile = new File( repositoryLocation, REPOSITORY_METADATA_FILENAME );

        if (!repositoryMetadataFile.exists()) {
            throw new RepositoryException( "The requested folder location does not appear to be an OTA2.0 repository: "
                + repositoryLocation.getAbsolutePath() );
        }
        RepositoryInfoType repositoryMetadata = (RepositoryInfoType) loadFile( repositoryMetadataFile );
        List<String> rootNamespaces = new ArrayList<>();

        // Normalize each of the root namespace URI's before returning
        for (String rootNS : repositoryMetadata.getRootNamespace()) {
            rootNamespaces.add( RepositoryNamespaceUtils.normalizeUri( rootNS ) );
        }
        repositoryMetadata.getRootNamespace().clear();
        repositoryMetadata.getRootNamespace().addAll( rootNamespaces );
        return repositoryMetadata;
    }

    /**
     * Saves the given repository meta-data as a file in the root folder of the specified repository location. The
     * meta-data file that is saved is automatically added to the active change set prior to updating its content.
     * 
     * @param repositoryMetadata the OTA2.0 repoisitory meta-data to save
     * @throws RepositoryException thrown if the file cannot be saved
     */
    public void saveRepositoryMetadata(RepositoryInfoType repositoryMetadata) throws RepositoryException {
        if (repositoryMetadata == null) {
            throw new NullPointerException( "The repository meta-data cannote be null." );
        }
        if (!repositoryLocation.exists()) {
            repositoryLocation.mkdirs();
        }
        File repositoryMetadataFile = new File( repositoryLocation, REPOSITORY_METADATA_FILENAME );

        addToChangeSet( repositoryMetadataFile );
        saveFile( repositoryMetadataFile, objectFactory.createRepositoryInfo( repositoryMetadata ), false );
    }

    /**
     * Returns the last-updated date for the repository meta-data, or null if no such meta-data exists for the
     * repository.
     * 
     * @return Date
     */
    public Date getRepositoryMetadataLastUpdated() {
        File repositoryMetadataFile = new File( repositoryLocation, REPOSITORY_METADATA_FILENAME );

        return repositoryMetadataFile.exists() ? new Date( repositoryMetadataFile.lastModified() ) : null;
    }

    /**
     * Returns a file location for the meta-data of the specified item from the repository.
     * 
     * @param baseNamespace the base namespace to which the item is assigned
     * @param filename the filename of the item's content (no path information)
     * @param versionIdentifier the item's version identifier
     * @return File
     * @throws RepositoryException thrown if the namespace URI provided is not valid
     */
    public File getLibraryMetadataLocation(String baseNamespace, String filename, String versionIdentifier)
        throws RepositoryException {
        return new File( getNamespaceFolder( baseNamespace, versionIdentifier ),
            getLibraryMetadataFilename( filename ) );
    }

    /**
     * Returns a file location for the raw XML content of the specified item from the repository.
     * 
     * @param baseNamespace the base namespace to which the item is assigned
     * @param filename the filename of the item's content (no path information)
     * @param versionIdentifier the item's version identifier
     * @return File
     * @throws RepositoryException thrown if the namespace URI provided is not valid
     */
    public File getLibraryContentLocation(String baseNamespace, String filename, String versionIdentifier)
        throws RepositoryException {
        return new File( getNamespaceFolder( baseNamespace, versionIdentifier ), filename );
    }

    /**
     * Returns a file location for the work-in-process (WIP) content of the specified item from the repository. The file
     * that is returned may or may not exist, depending upon whether the item is currently in the
     * <code>MANAGED_WIP</code> state.
     * 
     * @param baseNamespace the base namespace to which the item is assigned
     * @param filename the filename of the item's content (no path information)
     * @return File
     * @throws RepositoryException thrown if the namespace URI provided is not valid
     */
    public File getLibraryWipContentLocation(String baseNamespace, String filename) throws RepositoryException {
        File wipFolder = new File( getNamespaceFolder( baseNamespace, null ), WIP_FOLDER_LOCATION );
        return new File( wipFolder, filename );
    }

    /**
     * Loads the available meta-data for the specified library.
     * 
     * @param baseNamespace the base namespace of the library whose meta-data is to be loaded
     * @param filename the filename of the library whose meta-data is to be loaded
     * @param versionIdentifier the item's version identifier
     * @return LibraryInfoType
     * @throws RepositoryException thrown if the library meta-data cannot be loaded
     */
    public LibraryInfoType loadLibraryMetadata(String baseNamespace, String filename, String versionIdentifier)
        throws RepositoryException {
        return loadLibraryMetadata( getLibraryMetadataLocation( baseNamespace, filename, versionIdentifier ) );
    }

    /**
     * Loads the available meta-data for the specified library.
     * 
     * @param metadataFile the library meta-data file to be loaded
     * @return LibraryInfoType
     * @throws RepositoryException thrown if the library meta-data cannot be loaded
     */
    public LibraryInfoType loadLibraryMetadata(File metadataFile) throws RepositoryException {
        if (!metadataFile.exists()) {
            throw new RepositoryException( "No meta-data found for item: " + metadataFile.getName() );
        }
        LibraryInfoType libraryMetadata = (LibraryInfoType) loadFile( metadataFile );

        // Normalize the namespace URI's before returning
        libraryMetadata.setBaseNamespace( RepositoryNamespaceUtils.normalizeUri( libraryMetadata.getBaseNamespace() ) );
        libraryMetadata.setNamespace( RepositoryNamespaceUtils.normalizeUri( libraryMetadata.getNamespace() ) );
        return libraryMetadata;
    }

    /**
     * Returns a list meta-data records for all items published to the specified namespace.
     * 
     * @param baseNamespace the base namespace (no version identifier) to search for published items
     * @return List&lt;LibraryInfoType&gt;
     * @throws RepositoryException thrown if the namespace URI provided is not valid
     */
    public List<LibraryInfoType> loadLibraryMetadataRecords(String baseNamespace) throws RepositoryException {
        List<LibraryInfoType> metadataList = new ArrayList<>();

        // First, compile the list of possible folders for the specified base namespace
        File baseFolder = getNamespaceFolder( baseNamespace, null );
        List<File> namespaceFolders = new ArrayList<>();

        if (baseFolder.exists()) {
            for (File folderMember : baseFolder.listFiles()) {
                if (folderMember.isDirectory()) {
                    namespaceFolders.add( folderMember );
                }
            }
            namespaceFolders.add( baseFolder );

            for (File nsFolder : namespaceFolders) {
                for (File folderMember : nsFolder.listFiles()) {
                    loadIfMetadata( folderMember, baseNamespace, metadataList );
                }
            }
        }
        return metadataList;
    }

    /**
     * If the given folder member is a meta-data file, load it and add to the list of meta-data records discovered so
     * far.
     * 
     * @param folderMember the file to check and load
     * @param baseNamespace the base namespace represented by the file's folder location
     * @param metadataList the list to which a new meta-data record will be added
     */
    private void loadIfMetadata(File folderMember, String baseNamespace, List<LibraryInfoType> metadataList) {
        if (folderMember.isFile() && folderMember.getName().endsWith( METADATA_FILE_SUFFIX )) {
            try {
                LibraryInfoType libraryMetadata = (LibraryInfoType) loadFile( folderMember );

                if (baseNamespace.equals( libraryMetadata.getBaseNamespace() )) {
                    metadataList.add( libraryMetadata );
                }
            } catch (RepositoryException e) {
                log.warn( "Unreadable library meta-data file: " + folderMember.getAbsolutePath() );
            }
        }
    }

    /**
     * Saves the given library meta-data record to the repository.
     * 
     * @param libraryMetadata the library meta-data to be saved
     * @return File
     * @throws RepositoryException thrown if the library meta-data cannot be saved
     */
    public File saveLibraryMetadata(LibraryInfoType libraryMetadata) throws RepositoryException {
        if (libraryMetadata == null) {
            throw new NullPointerException( "The library meta-data cannote be null." );
        }
        File metadataFile = getLibraryMetadataLocation( libraryMetadata.getBaseNamespace(),
            libraryMetadata.getFilename(), libraryMetadata.getVersion() );
        File metadataFolder = metadataFile.getParentFile();

        if (!metadataFolder.exists()) {
            createDirectory( metadataFolder );
        }

        if (libraryMetadata.getLastUpdated() != null) {
            libraryMetadata.setLastUpdated( libraryMetadata.getLastUpdated() );
        } else {
            libraryMetadata.setLastUpdated( XMLGregorianCalendarConverter.toXMLGregorianCalendar( new Date() ) );
        }

        saveFile( metadataFile, objectFactory.createLibraryInfo( libraryMetadata ), true );
        return metadataFile;
    }

    /**
     * Returns the absolute file location that will be used to store library files assigned to the given base namespace
     * URI.
     * 
     * @param baseNamespace the base namespace for which to return the repository folder location
     * @param versionIdentifier the version identifier of the library (if null, the base namespace folder will be
     *        returned)
     * @return File
     * @throws RepositoryException thrown if the namespace URI provided is not valid
     */
    public File getNamespaceFolder(String baseNamespace, String versionIdentifier) throws RepositoryException {
        try {
            StringBuilder nsFolder = new StringBuilder( repositoryLocation.getAbsolutePath() );
            URL url = new URL( baseNamespace );

            if (!repositoryLocation.getAbsolutePath().endsWith( "/" )) {
                nsFolder.append( "/" );
            }

            // The URI protocol/scheme is the top-level folder of the repository hierarchy
            nsFolder.append( url.getProtocol() ).append( "/" );

            // Reverse the order of the authority components for the next level(s) of the folder
            // structure
            String[] authorityParts = url.getHost().split( "\\." );

            for (int i = (authorityParts.length - 1); i >= 0; i--) {
                String folderName = toFolderName( authorityParts[i] );

                if ((folderName != null) && (folderName.length() > 0)) {
                    nsFolder.append( folderName ).append( "/" );
                }
            }

            // Use the remaining components of the URI path as sub-folders in the repository's
            // directory structure
            String[] pathParts = url.getPath().split( "/" );

            for (String pathPart : pathParts) {
                String folderName = toFolderName( pathPart );

                if (folderName != null) {
                    nsFolder.append( folderName ).append( "/" );
                }
            }

            // Append the patch level to the lowest-level folder in the path
            if ((versionIdentifier != null) && !versionIdentifier.trim().equals( "" )) {
                nsFolder.append( versionIdentifier ).append( "/" );
            }

            return new File( nsFolder.toString() );

        } catch (MalformedURLException e) {
            throw new RepositoryException( "Invalid namespace URI: " + baseNamespace );
        }
    }

    /**
     * Returns the absolute file location for the projects folder for the local repository.
     * 
     * @return File
     */
    public File getProjectsFolder() {
        return new File( repositoryLocation, PROJECTS_FOLDER_LOCATION );
    }

    /**
     * Returns true if the given file is located in this repository's directory structure.
     * 
     * @param file the file to check
     * @return boolean
     */
    public boolean isRepositoryFile(File file) {
        boolean result = false;

        if (file != null) {
            File folder = file.getParentFile();

            while (!result && (folder != null)) {
                result = folder.equals( repositoryLocation );
                folder = folder.getParentFile();
            }
        }
        return result;
    }

    /**
     * Searches the repository folders for the given list of root namespaces, returning a list of all namespaces that
     * have at least one repository item published.
     * 
     * @param rootNamespaces the list of root namespaces under which to search for all published child URI's
     * @return List&lt;String&gt;
     * @throws RepositoryException thrown if the namespace URI provided is not valid
     */
    public List<String> findAllNamespaces(List<String> rootNamespaces) throws RepositoryException {
        List<String> namespaces = new ArrayList<>();

        for (String rootNamespace : rootNamespaces) {
            findNamespaces( rootNamespace, true, namespaces );
        }
        Collections.sort( namespaces );
        return namespaces;
    }

    /**
     * Searches the repository folders for the given list of root namespaces, returning a list of all namespaces that
     * have at least one repository item published.
     * 
     * @param rootNamespaces the root namespaces under which to search for all published child URI's
     * @return List&lt;String&gt;
     * @throws RepositoryException thrown if the namespace URI provided is not valid
     */
    public List<String> findAllBaseNamespaces(List<String> rootNamespaces) throws RepositoryException {
        List<String> namespaces = new ArrayList<>();

        for (String rootNamespace : rootNamespaces) {
            findNamespaces( rootNamespace, false, namespaces );
        }
        Collections.sort( namespaces );
        return namespaces;
    }

    /**
     * Recursively locates all of the child namespaces that exist within the specified base namespace.
     * 
     * @param baseNamespace the base namespace under which to search for all child namespaces
     * @param includeVersionNamespaces indicates whether version-specific namespaces should be included in the results
     * @param results the list of namespaces where the results of the search will be stored
     * @throws RepositoryException thrown if the namespace URI provided is not valid
     */
    protected void findNamespaces(String baseNamespace, boolean includeVersionNamespaces, List<String> results)
        throws RepositoryException {
        List<String> childPaths = findChildBaseNamespacePaths( baseNamespace );

        results.add( baseNamespace );

        for (String childPath : childPaths) {
            findNamespaces( RepositoryNamespaceUtils.appendChildPath( baseNamespace, childPath ),
                includeVersionNamespaces, results );
        }

        if (includeVersionNamespaces) {
            List<String> versionPaths = findChildVersionNamespacePaths( baseNamespace );

            for (String versionPath : versionPaths) {
                findNamespaces( baseNamespace + versionPath, includeVersionNamespaces, results );
            }
        }
    }

    /**
     * Searches the immediate child folders and returns the list of namespace paths immediately below the one provided.
     * 
     * @param baseNamespace the base namespace for which to retrieve child URI paths
     * @return List&lt;String&gt;
     * @throws RepositoryException thrown if the namespace URI provided is not valid
     */
    public List<String> findChildBaseNamespacePaths(String baseNamespace) throws RepositoryException {
        File nsFolder = getNamespaceFolder( baseNamespace, null );
        List<String> childPaths = new ArrayList<>();

        if (nsFolder.exists()) {
            for (File folderMember : nsFolder.listFiles()) {
                if (folderMember.isDirectory() && !folderMember.getName().startsWith( "." )) {
                    try {
                        childPaths.add( getNamespaceUriPathSegment( folderMember ) );

                    } catch (IOException e) {
                        // No error - skip this folder and move on
                    }
                }
            }
        }
        return childPaths;
    }

    /**
     * Searches the immediate child folders and returns the list of namespace paths immediately below the one provided.
     * 
     * <p>
     * NOTE: Since version identifiers in namespace URI's can be separated by different delimeters, the paths returned
     * by this method contain the leading delimeter for the version namespace.
     * 
     * @param baseNamespace the base namespace for which to retrieve child URI paths
     * @return List&lt;String&gt;
     * @throws RepositoryException thrown if an error occurs while accessing the repository
     */
    public List<String> findChildVersionNamespacePaths(String baseNamespace) throws RepositoryException {
        File nsFolder = getNamespaceFolder( baseNamespace, null );
        List<String> childPaths = new ArrayList<>();

        if (nsFolder.exists()) {
            for (File folderMember : nsFolder.listFiles()) {
                boolean skip = false;

                // Skip folders that are not specific to namespace versions
                if (!folderMember.isDirectory()) {
                    skip = true;

                } else if (!folderMember.getName().startsWith( "." )) {
                    File nsidFile = new File( folderMember, NAMESPACE_ID_FILENAME );

                    if (nsidFile.exists()) {
                        skip = true;
                    }
                }
                if (skip) {
                    continue;
                }
                getChildVersionPaths( folderMember, childPaths );
            }
        }
        return childPaths;
    }

    /**
     * Find all of the version URI path segments from each file found in this folder (multiples are allowed since the
     * version schemes can represent URI version identifiers in multiple ways (e.g. "v1", "v01", "v_1", etc.).
     * 
     * @param nsFolder the namespace folder to search for child version paths
     * @param childPaths the list of child version paths being collected
     */
    private void getChildVersionPaths(File nsFolder, List<String> childPaths) {
        for (File versionMember : nsFolder.listFiles()) {
            if (versionMember.getName().endsWith( METADATA_FILE_SUFFIX )) {
                try {
                    LibraryInfoType libraryMetadata = (LibraryInfoType) loadFile( versionMember );
                    String baseNS = libraryMetadata.getBaseNamespace();
                    String versionNS = libraryMetadata.getNamespace();

                    if (versionNS.length() > baseNS.length()) {
                        childPaths.add( versionNS.substring( baseNS.length() ) );
                    }

                } catch (RepositoryException e) {
                    log.warn( "Unreadable library meta-data file: " + versionMember.getAbsolutePath() );
                }
            }
        }
    }

    /**
     * Returns the case-sensitive URI path segment for the given namespace folder.
     * 
     * @param namespaceFolder the folder for which the URI path segment should be returned
     * @return String
     * @throws IOException thrown if the namespace ID file cannot be accessed
     */
    protected String getNamespaceUriPathSegment(File namespaceFolder) throws IOException {
        synchronized (namespaceIdCache) {
            String nsPath = null;

            if (namespaceIdCache.containsKey( namespaceFolder.getAbsolutePath() )) {
                nsPath = namespaceIdCache.get( namespaceFolder.getAbsolutePath() );

            } else {
                File nsidFile = new File( namespaceFolder, NAMESPACE_ID_FILENAME );

                if (nsidFile.exists()) {
                    try (BufferedReader reader = new BufferedReader( new FileReader( nsidFile ) )) {
                        String line = reader.readLine();

                        if (line != null) {
                            nsPath = line.trim();
                            namespaceIdCache.put( namespaceFolder.getAbsolutePath(), nsPath );

                        } else {
                            throw new IOException( "Empty nsid.txt file." );
                        }

                    } catch (IOException e) {
                        throw new IOException(
                            "Unable to identify the namespace path for folder: " + namespaceFolder.getAbsolutePath(),
                            e );
                    }
                }
            }

            if (nsPath == null) {
                throw new IOException(
                    "The namespace folder is not valid for this repository: " + namespaceFolder.getAbsolutePath() );
            }
            return nsPath;
        }
    }

    /**
     * Creates the namespace folders and any missing 'nsid.txt' files for the specified base namespace and all of its
     * parent namespaces up to and including the folder for the root namespace that contains it.
     * 
     * <p>
     * NOTE: All of the 'nsid.txt' files that are created are added to the current change set, but the change set itself
     * is not committed (or rolled back) by this method.
     * 
     * @param baseNamespace the base namespace for which to create namespace ID files
     * @throws RepositoryException thrown if one or more 'nsid.txt' files cannot be created
     */
    public void createNamespaceIdFiles(String baseNamespace) throws RepositoryException {
        List<String> rootNamespaces = loadRepositoryMetadata().getRootNamespace();
        String ns = baseNamespace;

        if (ns.endsWith( "/" ) && (ns.length() > 1)) {
            ns = ns.substring( 0, ns.length() - 1 );
        }

        while (ns != null) {
            ns = createNamespaceIdFile( ns, baseNamespace, rootNamespaces );
        }
    }

    /**
     * Creates a namespace ID file based upon the next path in the partial namespace URI provided.
     * 
     * @param ns the current partial namespace path being processed
     * @param baseNamespace the base namespace for which ID files are being created
     * @param rootNamespaces the list of all root namespaces in the repository
     * @return String
     * @throws RepositoryException thrown if the expected namespace ID does not match that of the file
     */
    private String createNamespaceIdFile(String ns, String baseNamespace, List<String> rootNamespaces)
        throws RepositoryException {
        File nsFolder = getNamespaceFolder( ns, null );
        File nsidFile = new File( nsFolder, NAMESPACE_ID_FILENAME );
        String nsid = null;

        // Identify the 'nsid' as the last segment of the URI path (or the root namespace
        // itself)
        if (rootNamespaces.contains( ns )) {
            nsid = ns;
            ns = null;

        } else {
            int slashIdx = ns.lastIndexOf( '/' );

            if (slashIdx >= 0) {
                if (ns.length() > (slashIdx + 1)) {
                    nsid = ns.substring( slashIdx + 1 );
                } else {
                    nsid = null;
                }
                ns = ns.substring( 0, slashIdx );

            } else {
                ns = null;
            }
        }

        if (nsid != null) {
            // Create any namespace folders that do not already exist
            if (!nsFolder.exists()) {
                nsFolder.mkdirs();
            }

            if (nsidFile.exists()) {
                // If the namespace file already exists, check it to make sure
                // we are matching on a case-sensitive basis
                validateNamespacdID( nsid, nsidFile, baseNamespace, ns );

            } else {
                // Save the root namespace file if one does not already exist
                createNamespaceIdFile( nsid, nsidFile, ns );
            }
        }
        return ns;
    }

    /**
     * Creates a new namespace ID file using the information provided.
     * 
     * @param nsid the namespace ID
     * @param nsidFile the file to which the namespace ID should be saved
     * @param ns the full namespace of which the ID is a sub-component
     * @throws RepositoryException thrown if the file cannot be saved
     */
    private void createNamespaceIdFile(String nsid, File nsidFile, String ns) throws RepositoryException {
        try (Writer writer = new BufferedWriter( new FileWriter( nsidFile ) )) {
            addToChangeSet( nsidFile );
            writer.write( nsid );

        } catch (IOException e) {
            throw new RepositoryException( "Unable to create namespace identification file for URI: " + ns, e );
        }
    }

    /**
     * Verifies that the namespace ID contained within the specified file matches the expected ID that is provided.
     * 
     * @param nsid the namespace ID to be validated
     * @param nsidFile the namespace ID file whose content should match the expected value
     * @param baseNamespace the base namespace being validated
     * @param ns the full namespace being validated
     * @throws RepositoryException thrown if the expected namespace ID does not match that of the file
     */
    private void validateNamespacdID(String nsid, File nsidFile, String baseNamespace, String ns)
        throws RepositoryException {
        try (BufferedReader reader = new BufferedReader( new FileReader( nsidFile ) )) {
            String existingNsid = reader.readLine();

            if (!nsid.equals( existingNsid )) {
                if (nsid.equalsIgnoreCase( existingNsid )) {
                    throw new RepositoryException(
                        "The given URI conflicts with the case-sensitivity of an existing namespace: "
                            + baseNamespace );

                } else { // failed for some other reason than case-sensitivity
                    throw new RepositoryException(
                        "The given URI conflicts with an existing namespace: " + baseNamespace );
                }
            }

        } catch (IOException e) {
            throw new RepositoryException( "Unable to verify namespace identification file for URI: " + ns, e );
        }
    }

    /**
     * Deletes the 'nsid.txt' file from the namespace folder of the repository if the following conditions are true:
     * <ul>
     * <li>An 'nsid.txt' file currently exists for the given namespace</li>
     * <li>The namespace does not have any child namespaces defined</li>
     * <li>The namespace does not have any OTM library or schema items defined</li>
     * </ul>
     * 
     * <p>
     * NOTE: The 'nsid.txt' file that is deleted will be appended to the current change set, but the change set itself
     * is not committed (or rolled back) by this method.
     * 
     * @param baseNamespace the base namespace for which to delete the namespace ID file
     * @param deleteParentFiles set to true if 'nsid.txt' in higher-level folders should also be deleted
     * @throws RepositoryException thrown if the 'nsid.txt' file cannot be deleted
     */
    public void deleteNamespaceIdFile(String baseNamespace, boolean deleteParentFiles) throws RepositoryException {
        synchronized (namespaceIdCache) {
            File nsidFile = new File( getNamespaceFolder( baseNamespace, null ), NAMESPACE_ID_FILENAME );

            // Perform validation checks before deleting
            if (!nsidFile.exists()) {
                throw new RepositoryException( "Unable to delete namespace '" + baseNamespace
                    + "' because it does not exist in this repository." );
            }

            List<String> childPaths = findChildBaseNamespacePaths( baseNamespace );
            List<String> versionPaths = findChildVersionNamespacePaths( baseNamespace );

            if (!childPaths.isEmpty() || !versionPaths.isEmpty()) {
                throw new RepositoryException(
                    "Unable to delete namespace '" + baseNamespace + "' because it is not empty." );
            }

            // Remove the namespace ID file(s) and add them to the change set
            while (nsidFile != null) {
                File nsidFolder = nsidFile.getParentFile();

                addToChangeSet( nsidFile );

                if (!FileUtils.confirmDelete( nsidFile )) {
                    throw new RepositoryException( "Unable to remove namespace file: " + nsidFile.getAbsolutePath() );
                }
                namespaceIdCache.remove( nsidFolder.getAbsolutePath() );

                if (!deleteParentFiles || nsidFolder.getAbsolutePath().equals( repositoryLocation.getAbsolutePath() )) {
                    nsidFile = null;

                } else {
                    nsidFile = new File( nsidFolder.getParentFile(), NAMESPACE_ID_FILENAME );
                }
            }
        }
    }

    /**
     * Attempts to load the content of the specified file as an OTM library. If any non-validation exceptions occur
     * during the load, the file will be assumed to be a non-OTM file. In such, cases this method will return null
     * instead of throwing an exception.
     * 
     * @param libraryFile the library file load
     * @return LibraryContentWrapper
     */
    public LibraryContentWrapper loadLibraryContent(File libraryFile) {
        boolean is16Library = false;
        TLLibrary library = null;

        try {
            if ((libraryFile != null) && libraryFile.exists()
                && !libraryFile.getName().toLowerCase().endsWith( ".xsd" )) {
                LibraryModuleLoader<InputStream> loader = new MultiVersionLibraryModuleLoader();
                LibraryModuleInfo<Object> moduleInfo =
                    loader.loadLibrary( new LibraryStreamInputSource( libraryFile ), new ValidationFindings() );
                Object jaxbLibrary = moduleInfo.getJaxbArtifact();

                if (jaxbLibrary != null) {
                    TransformerFactory<DefaultTransformerContext> transformerFactory = TransformerFactory.getInstance(
                        SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY, new DefaultTransformerContext() );
                    ObjectTransformer<Object,TLLibrary,DefaultTransformerContext> transformer =
                        transformerFactory.getTransformer( jaxbLibrary, TLLibrary.class );

                    library = transformer.transform( jaxbLibrary );
                    library.setLibraryUrl( URLUtils.toURL( libraryFile ) );
                    is16Library = (jaxbLibrary instanceof org.opentravel.ns.ota2.librarymodel_v01_06.Library);
                }
            }
        } catch (Exception e) {
            // No action - method will return a null library
        }
        return new LibraryContentWrapper( library, libraryFile, is16Library );
    }

    /**
     * Saves the OTM library content using the original file format from which it was loaded.
     * 
     * @param libraryContent the library content to be saved
     * @throws RepositoryException thrown if the library file cannot be added to the current change set
     * @throws LibrarySaveException thrown if an error occurs during the save operation
     */
    public void saveLibraryContent(LibraryContentWrapper libraryContent)
        throws RepositoryException, LibrarySaveException {
        LibraryModelSaver modelSaver = new LibraryModelSaver();

        if (libraryContent.isIs16Library()) {
            modelSaver.setSaveHandler( new Library16FileSaveHandler() );
        } else {
            modelSaver.setSaveHandler( new Library15FileSaveHandler() );
        }
        addToChangeSet( libraryContent.getContentFile() );
        modelSaver.getSaveHandler().setCreateBackupFile( false );
        modelSaver.saveLibrary( libraryContent.getContent() );
    }

    /**
     * Assigns the ID of the current repository user. The user ID is typically associated with a user who is performing
     * modifications to one or more repository files.
     * 
     * @param userId the ID of the current user (may be null for anonymous access)
     */
    public void setCurrentUserId(String userId) {
        currentUserId.set( userId );
    }

    /**
     * Returns the ID of the current repository user. The user ID is typically associated with a user who is performing
     * modifications to one or more repository files.
     * 
     * @return String
     */
    public String getCurrentUserId() {
        return currentUserId.get();
    }

    /**
     * Begins a new change set for the current thread. If the existing change set has not been committed or rolled back,
     * its contents will be rolled back before initializing the new change set.
     */
    public void startChangeSet() {
        Set<File> chgSet = RepositoryFileManager.changeSet.get();
        try {
            if (!chgSet.isEmpty()) {
                log.warn( "Uncommitted change set from previous task - rolling back." );
                rollbackChangeSet();
            }

        } catch (RepositoryException e) {
            // Since these changes are left over from a previous repository job, we do not want this rollback
            // error to cause a failure on the current (and unrelated) job. For that reason, we will simply
            // log the error and clear the change set.
            log.error( "Unable to roll back uncommitted change set from previous task.", e );
            chgSet.clear();
        }
        if (log.isDebugEnabled()) {
            log.debug( "Change set started for thread: " + Thread.currentThread().getName() );
        }
    }

    /**
     * Adds the specified file to the current change set - typically before the change is attempted. The file does not
     * yet have to exist for this method call to be successful.
     * 
     * <p>
     * NOTE: Sub-classes that implement special processing, MUST call this super-class method to perform the default
     * processing.
     * 
     * @param file the file to be added to the current change set
     * @throws RepositoryException thrown if the file cannot be added to the change set for any reason
     */
    public void addToChangeSet(File file) throws RepositoryException {
        if (file != null) {
            if (log.isDebugEnabled()) {
                log.debug( "Adding file to repository change set: " + file.getName() + " [Change Set - "
                    + Thread.currentThread().getName() + "]" );
            }
            RepositoryFileManager.changeSet.get().add( file );
        }
    }

    /**
     * Commits the contents (if any) of the current change set.
     * 
     * @throws RepositoryException thrown if the change set cannot be committed for any reason
     */
    public void commitChangeSet() throws RepositoryException {
        Set<File> chgSet = RepositoryFileManager.changeSet.get();

        if (!chgSet.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug( "Committing repository change set: " + Thread.currentThread().getName() );
            }
            commitChangeSet( chgSet );
        }
        chgSet.clear();

        if (log.isDebugEnabled()) {
            log.debug( "Change set committed for thread: " + Thread.currentThread().getName() );
        }
    }

    /**
     * Commits the contents of the given change set.
     * 
     * @param changeSet the list of files in the change set to be committed
     * @throws RepositoryException thrown if the change set cannot be committed for any reason
     */
    protected abstract void commitChangeSet(Set<File> changeSet) throws RepositoryException;

    /**
     * Rolls back the contents (if any) of the current change set.
     * 
     * @throws RepositoryException thrown if the change set cannot be rolled back for any reason
     */
    public void rollbackChangeSet() throws RepositoryException {
        Set<File> chgSet = RepositoryFileManager.changeSet.get();

        if (!chgSet.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug( "Rolling back repository change set: " + Thread.currentThread().getName() );
            }
            rollbackChangeSet( chgSet );
        }
        chgSet.clear();

        if (log.isDebugEnabled()) {
            log.debug( "Change set rolled back for thread: " + Thread.currentThread().getName() );
        }
    }

    /**
     * Rolls back the contents of the given change set.
     * 
     * @param changeSet the list of files in the change set to be rolled back
     * @throws RepositoryException thrown if the change set cannot be rolled back for any reason
     */
    protected abstract void rollbackChangeSet(Set<File> changeSet) throws RepositoryException;

    /**
     * Returns the given URI path component as a legal folder name for the OTA2.0 repository.
     * 
     * @param uriComponent the URI component string to convert
     * @return String
     */
    protected String toFolderName(String uriComponent) {
        if ((uriComponent == null) || (uriComponent.length() == 0)) {
            return null;

        }
        String folderName = uriComponent.toLowerCase();

        if (!Character.isJavaIdentifierStart( folderName.charAt( 0 ) )) {
            folderName = "_" + folderName;
        }
        return folderName;
    }

    /**
     * Returns the filename (without path information) of the repository meta-data file for the specified library.
     * 
     * @param libraryFilename the filename of the OTM library
     * @return String
     */
    protected String getLibraryMetadataFilename(String libraryFilename) {
        int dotIdx = libraryFilename.lastIndexOf( '.' );
        String baseFilename = (dotIdx < 0) ? libraryFilename : libraryFilename.subSequence( 0, dotIdx ).toString();

        return baseFilename + METADATA_FILE_SUFFIX;
    }

    /**
     * Loads the JAXB representation of the XML content from the specified file location.
     * 
     * @param file the repository file to load
     * @return Object
     * @throws RepositoryException thrown if the file cannot be loaded
     */
    protected Object loadFile(File file) throws RepositoryException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema( repositoryValidationSchema );

            JAXBElement<?> documentElement = (JAXBElement<?>) FileUtils.unmarshalFileContent( file, unmarshaller );
            return documentElement.getValue();

        } catch (JAXBException e) {
            throw new RepositoryException( "Unrecognized file format.", e );

        } catch (Exception e) {
            throw new RepositoryException( "Unknown error while reading repository file.", e );
        }
    }

    /**
     * Saves the content of the given JAXB element the specified file location.
     * 
     * @param file the file to which the JAXB contents should be saved
     * @param jaxbElement the JAXB element whose content is to be saved
     * @param addToChangeSet flag indicating whether the saved file should be added to the current change set
     * @throws RepositoryException thrown if the file cannot be saved
     */
    protected void saveFile(File file, JAXBElement<?> jaxbElement, boolean addToChangeSet) throws RepositoryException {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();

            if (!file.exists()) {
                createDirectory( file.getParentFile() );
            }
            marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            marshaller.setProperty( "com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {

                @Override
                public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                    return REPOSITORY_NAMESPACE.equals( namespaceUri )
                        ? SchemaDeclarations.OTA2_PROJECT_SCHEMA.getDefaultPrefix()
                        : suggestion;
                }

                @Override
                public String[] getPreDeclaredNamespaceUris() {
                    return new String[] {REPOSITORY_NAMESPACE};
                }

            } );
            marshaller.setSchema( repositoryValidationSchema );

            if (addToChangeSet) {
                addToChangeSet( file );
            }
            marshaller.marshal( jaxbElement, file );

        } catch (JAXBException e) {
            throw new RepositoryException( "Unknown error while repository file: " + file.getName(), e );
        }
    }

    /**
     * Saves the content from the given <code>InputStream</code> to the specified file. If a file already exists at the
     * specified location, it will be overwritten by the content that is passed to this method. The file that is saved
     * is automatically added to the current change set.
     * 
     * @param file the file location where the content is to be saved
     * @param fileContent the content of the file to create (or replace)
     * @throws RepositoryException thrown if the content cannot be saved
     */
    public void saveFile(File file, InputStream fileContent) throws RepositoryException {
        try (OutputStream out = new FileOutputStream( file )) {
            addToChangeSet( file );
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileContent.read( buffer )) >= 0) {
                out.write( buffer, 0, bytesRead );
            }
            fileContent.close();

        } catch (IOException e) {
            throw new RepositoryException( "Error saving file: " + file.getName(), e );
        }
    }

    /**
     * Recursively creates the given directory and all parents. Any directories that are created are added to the
     * current change set.
     * 
     * @param directory the directory to be created
     * @throws RepositoryException thrown if a new directory cannot be added to the current change set
     */
    protected void createDirectory(File directory) throws RepositoryException {
        if ((directory != null) && !directory.exists()) {
            createDirectory( directory.getParentFile() );
            directory.mkdir();
            addToChangeSet( directory );
        }
    }

    /**
     * Initializes the location of the repository home folder.
     */
    private static void initOta2HomeFolder() {
        String ota2Home = System.getProperty( "ota2.home" );
        StringBuilder homeFolder = new StringBuilder();

        if (ota2Home == null) {
            String userHome = System.getProperty( "user.home" ).replace( '\\', '/' );

            if ((userHome != null) && !userHome.equals( "?" )) {
                homeFolder.append( userHome );

                if (!userHome.endsWith( "/" )) {
                    homeFolder.append( '/' );
                }
            }
            homeFolder.append( REPOSITORY_HOME_FOLDER );

        } else {
            homeFolder.append( ota2Home.replace( '\\', '/' ) );

            if (!ota2Home.endsWith( "/" )) {
                homeFolder.append( '/' );
            }
        }
        ota2HomeFolder = new File( homeFolder.toString() );
    }

    /**
     * Initializes the location of the OTA2.0 repository.
     */
    private static void initDefaultRepositoryLocation() {
        File repositoryLocationFile = new File( getOta2HomeFolder(), REPOSITORY_LOCATION_FILENAME );
        defaultRepositoryLocation = null;

        if (repositoryLocationFile.exists()) {
            try (BufferedReader reader = new BufferedReader( new FileReader( repositoryLocationFile ) )) {
                defaultRepositoryLocation = new File( reader.readLine().trim() );

            } catch (IOException e) {
                // No error - just return the default repository location
            }
        }
        if (defaultRepositoryLocation == null) {
            defaultRepositoryLocation = new File( getOta2HomeFolder(), DEFAULT_REPOSITORY_LOCATION );
        }
    }

    /**
     * Initializes the validation schema and shared JAXB context.
     */
    static {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            InputStream schemaStream =
                SchemaDeclarations.OTA2_REPOSITORY_SCHEMA.getContent( CodeGeneratorFactory.XSD_TARGET_FORMAT );

            schemaFactory.setResourceResolver( new ClasspathResourceResolver() );
            repositoryValidationSchema = schemaFactory.newSchema( new StreamSource( schemaStream ) );
            jaxbContext = JAXBContext.newInstance( SCHEMA_CONTEXT );

            initOta2HomeFolder();
            initDefaultRepositoryLocation();

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
