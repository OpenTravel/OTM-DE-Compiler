package org.opentravel.schemacompiler.repository.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

import org.opentravel.ns.ota2.project_v01_00.ManagedProjectItemType;
import org.opentravel.ns.ota2.project_v01_00.ObjectFactory;
import org.opentravel.ns.ota2.project_v01_00.ProjectType;
import org.opentravel.ns.ota2.project_v01_00.RepositoryRefType;
import org.opentravel.ns.ota2.project_v01_00.RepositoryReferencesType;
import org.opentravel.ns.ota2.project_v01_00.UnmanagedProjectItemType;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LoaderValidationMessageKeys;
import org.opentravel.schemacompiler.loader.impl.FileValidationSource;
import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.ClasspathResourceResolver;
import org.opentravel.schemacompiler.util.ExceptionUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

/**
 * Static helper methods that handle the loading and saving of projects to and from the local file
 * system using JAXB.
 * 
 * @author S. Livezey
 */
public class ProjectFileUtils {

    private static final String SCHEMA_CONTEXT = ":org.w3._2001.xmlschema:org.opentravel.ns.ota2.project_v01_00";
    private static final String PROJECT_FILE_NAMESPACE = "http://www.OpenTravel.org/ns/OTA2/Project_v01_00";

    private static javax.xml.validation.Schema projectValidationSchema;
    private static ObjectFactory objectFactory = new ObjectFactory();
    private static JAXBContext jaxbContext;

    /**
     * Loads the JAXB representation of the project from the specified file location.
     * 
     * @param projectFile
     *            the project file to load
     * @param findings
     *            the validation findings encountered during the load process
     * @return ProjectType
     * @throws LibraryLoaderException
     *             thrown if the project file cannot be loaded
     */
    @SuppressWarnings("unchecked")
    public static ProjectType loadJaxbProjectFile(File projectFile, ValidationFindings findings)
            throws LibraryLoaderException {
        ProjectType jaxbProject = null;
        InputStream is = null;
        try {
            is = new FileInputStream(projectFile);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(projectValidationSchema);

            JAXBElement<ProjectType> documentElement = (JAXBElement<ProjectType>) unmarshaller
                    .unmarshal(is);
            jaxbProject = documentElement.getValue();

        } catch (JAXBException e) {
            String filename = (projectFile == null) ? "[UNKNOWN FILE]" : projectFile.getName();

            findings.addFinding(FindingType.ERROR, new FileValidationSource(projectFile),
                    LoaderValidationMessageKeys.ERROR_UNREADABLE_PROJECT_CONTENT, filename,
                    ExceptionUtils.getExceptionClass(e).getSimpleName(),
                    ExceptionUtils.getExceptionMessage(e));

        } catch (IOException e) {
            if (findings != null) {
                findings.addFinding(FindingType.WARNING, new FileValidationSource(projectFile),
                        LoaderValidationMessageKeys.WARNING_PROJECT_NOT_FOUND,
                        (projectFile == null) ? "[UNKNOWN FILE]" : projectFile.getName());
            } else {
                throw new LibraryLoaderException(e.getMessage(), e);
            }

        } catch (Throwable t) {
            throw new LibraryLoaderException("Unknown error while loading project.", t);

        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (Throwable t) {
            }
        }
        return jaxbProject;
    }

    /**
     * Saves the JAXB representation of the project to the specified file location.
     * 
     * @param projectFile
     *            the file to which the project contents should be saved
     * @param jaxbProject
     *            the JAXB representation of the project's contents
     * @throws LibrarySaveException
     *             thrown if the project file cannot be saved
     */
    public static void saveProjectFile(Project project) throws LibrarySaveException {
        boolean success = false;
        File backupFile = null;
        try {
            backupFile = createBackupFile(project.getProjectFile());
        } catch (IOException e) {
            // If we could not create the backup file, proceed without one
        }

        try {
            Marshaller marshaller = jaxbContext.createMarshaller();

            if (!project.getProjectFile().exists()) {
                project.getProjectFile().getParentFile().mkdirs();
            }
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                    new NamespacePrefixMapper() {

                        @Override
                        public String getPreferredPrefix(String namespaceUri, String suggestion,
                                boolean requirePrefix) {
                            return PROJECT_FILE_NAMESPACE.equals(namespaceUri) ? SchemaDeclarations.OTA2_PROJECT_SCHEMA
                                    .getDefaultPrefix() : suggestion;
                        }

                        @Override
                        public String[] getPreDeclaredNamespaceUris() {
                            return new String[] { PROJECT_FILE_NAMESPACE };
                        }

                    });
            marshaller.setSchema(projectValidationSchema);
            marshaller.marshal(objectFactory.createProject(convertToJaxbProject(project)),
                    project.getProjectFile());
            success = true;

        } catch (JAXBException e) {
            throw new LibrarySaveException("Unknown error while saving project.", e);

        } finally {
            if (!success && (backupFile != null)) {
                try {
                    restoreBackupFile(backupFile, project.getProjectFile().getName());
                } catch (Throwable t) {
                }
            }
        }
    }

    /**
     * Copies the specified source file's content to the destination file's location. If the
     * destination file already exists, it will be overwritten.
     * 
     * @param sourceFile
     *            the source file's location
     * @param destinationFile
     *            the destination file's location
     * @throws IOException
     *             thrown if the file cannot be copied
     */
    public static void copyFile(File sourceFile, File destinationFile) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            if (!destinationFile.getParentFile().exists()) {
                destinationFile.getParentFile().mkdirs();
            }
            in = new FileInputStream(sourceFile);
            out = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) >= 0) {
                out.write(buffer, 0, bytesRead);
            }

        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Throwable t) {
            }
            try {
                if (out != null)
                    out.close();
            } catch (Throwable t) {
            }
        }
    }

    /**
     * Transforms the given model project into its JAXB equivalent.
     * 
     * @param project
     *            the model project to transform
     * @return ProjectType
     */
    private static ProjectType convertToJaxbProject(Project project) {
        URL projectFileUrl = URLUtils.toURL(project.getProjectFile());
        ProjectItem defaultItem = project.getDefaultItem();
        Set<String> repositoryIds = new HashSet<String>();
        ProjectType jaxbProject = new ProjectType();

        jaxbProject.setProjectId(project.getProjectId());
        jaxbProject.setName(project.getName());
        jaxbProject.setDescription(project.getDescription());
        jaxbProject.setDefaultContextId(project.getDefaultContextId());

        for (ProjectItem item : purgeImpliedManagedVersions(project.getProjectItems())) {
            if (item.getState() == RepositoryItemState.UNMANAGED) {
                UnmanagedProjectItemType jaxbItem = new UnmanagedProjectItemType();

                jaxbItem.setDefaultItem((defaultItem == item) ? Boolean.TRUE : null);
                jaxbItem.setFileLocation(URLUtils.getRelativeURL(projectFileUrl, item.getContent()
                        .getLibraryUrl(), true));
                jaxbProject.getProjectItemBase().add(
                        objectFactory.createUnmanagedProjectItem(jaxbItem));

            } else {
                String repositoryId = (item.getRepository() == null) ? null : item.getRepository()
                        .getId();
                ManagedProjectItemType jaxbItem = new ManagedProjectItemType();

                if (repositoryId != null) {
                    repositoryIds.add(repositoryId);
                }
                jaxbItem.setDefaultItem((defaultItem == item) ? Boolean.TRUE : null);
                jaxbItem.setRepository(repositoryId);
                jaxbItem.setBaseNamespace(item.getBaseNamespace());
                jaxbItem.setFilename(item.getFilename());
                jaxbItem.setVersion(item.getVersion());
                jaxbProject.getProjectItemBase().add(
                        objectFactory.createManagedProjectItem(jaxbItem));
            }
        }

        // If necessary, compile a list of repositories that are referenced and their endpoint URL's
        ProjectManager projectManager = project.getProjectManager();
        RepositoryManager repositoryManager = (projectManager == null) ? null : projectManager
                .getRepositoryManager();

        if ((repositoryManager != null) && !repositoryIds.isEmpty()) {
            RepositoryReferencesType repositoryRefs = new RepositoryReferencesType();

            for (String repositoryId : repositoryIds) {
                Repository repository = repositoryManager.getRepository(repositoryId);

                if (repository instanceof RemoteRepository) {
                    RepositoryRefType repositoryRef = new RepositoryRefType();

                    repositoryRef.setRepositoryId(repositoryId);
                    repositoryRef.setValue(((RemoteRepository) repository).getEndpointUrl());
                    repositoryRefs.getRepositoryRef().add(repositoryRef);
                }
            }
            jaxbProject.setRepositoryReferences(repositoryRefs);
        }
        return jaxbProject;
    }

    /**
     * Purges any project items from the given list that are implied by the existence of later
     * versions. Only managed project items are processed by this method.
     * 
     * @param projectItems
     *            the list of project items to process
     * @return List<ProjectItem>
     */
    private static List<ProjectItem> purgeImpliedManagedVersions(List<ProjectItem> projectItems) {
        Map<ProjectItem, List<String>> itemVersionChains = new HashMap<ProjectItem, List<String>>();
        List<ProjectItem> keepItems = new ArrayList<ProjectItem>();

        for (ProjectItem item : projectItems) {
            if (item.getState() == RepositoryItemState.UNMANAGED) {
                continue; // only purge managed versions
            }
            try {
                VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme(
                        item.getVersionScheme());
                List<String> itemVersionChain = vScheme.getMajorVersionChain(item.getNamespace());
                boolean purgeCurrentItem = false;

                // Determine if the current item is eclipsed by one of the existing keep item
                for (ProjectItem keepItem : keepItems) {
                    if (!item.getLibraryName().equals(keepItem.getLibraryName())
                            || !item.getBaseNamespace().equals(keepItem.getBaseNamespace())) {
                        continue; // purge candidates must have the same library name and base
                                  // namespace
                    }
                    List<String> keepItemChain = itemVersionChains.get(keepItem);

                    if (isInVersionChain(item.getNamespace(), keepItemChain, vScheme)) {
                        purgeCurrentItem = true;
                        break;
                    }
                }

                if (!purgeCurrentItem) {
                    Iterator<ProjectItem> keepIterator = keepItems.iterator();

                    // Discard any existing keep items that are eclipsed by the current item
                    while (keepIterator.hasNext()) {
                        ProjectItem keepItem = keepIterator.next();

                        if (!item.getLibraryName().equals(keepItem.getLibraryName())
                                || !item.getBaseNamespace().equals(keepItem.getBaseNamespace())) {
                            continue; // purge candidates must have the same library name and base
                                      // namespace
                        }

                        if (isInVersionChain(keepItem.getNamespace(), itemVersionChain, vScheme)) {
                            keepIterator.remove();
                        }
                    }
                    keepItems.add(item);
                }
                itemVersionChains.put(item, itemVersionChain);

            } catch (VersionSchemeException e) {
                // On error, we DO NOT want to purge the item from the list
                keepItems.add(item);
            }
        }

        // Scan the original list, and assemble the final list of items that should be included in
        // project's save file
        List<ProjectItem> result = new ArrayList<ProjectItem>();

        for (ProjectItem item : projectItems) {
            if ((item.getState() == RepositoryItemState.UNMANAGED) || keepItems.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Returns true if the specified namespace is a member of the version chain provided.
     * 
     * <p>
     * NOTE: This method assumed that the base namespaces of the given namespace and the chain have
     * already been matched.
     * 
     * @param namespace
     *            the namespace to analyze
     * @param versionChain
     *            the version chain to check for membership
     * @param scheme
     *            the version scheme for the namespace and version chain
     * @return boolean
     */
    private static boolean isInVersionChain(String namespace, List<String> versionChain,
            VersionScheme scheme) {
        String nsVersion = scheme.getVersionIdentifier(namespace);
        boolean result = false;

        if (nsVersion != null) {
            for (String chainNS : versionChain) {
                if (nsVersion.equals(scheme.getVersionIdentifier(chainNS))) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Creates a backup of the specified original file on the local file system. The location of the
     * newly-created backup file is returned by this method.
     * 
     * @param originalFile
     *            the original file to backup
     * @return File
     * @throws IOException
     *             thrown if the backup file cannot be created
     */
    public static File createBackupFile(File originalFile) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            File backupFile;

            if (!originalFile.exists()) {
                backupFile = null;

            } else {
                byte[] buffer = new byte[1024];
                int bytesRead;

                backupFile = new File(originalFile.getParentFile(), getBackupFilename(originalFile));
                in = new FileInputStream(originalFile);
                out = new FileOutputStream(backupFile);

                while ((bytesRead = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return backupFile;

        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Throwable t) {
            }
            try {
                if (out != null)
                    out.close();
            } catch (Throwable t) {
            }
        }
    }

    /**
     * Creates a backup of the specified original file on the local file system.
     * 
     * @param backupFile
     *            the backup file to restore
     * @param originalFilename
     *            the original name of the file to be restored (without the filepath)
     * @throws IOException
     *             thrown if the backup file cannot be restored
     */
    public static void restoreBackupFile(File backupFile, String originalFilename)
            throws IOException {
        if ((backupFile != null) && backupFile.exists()) {
            String filename = backupFile.getName();

            if (!filename.endsWith(".bak")) {
                throw new IllegalArgumentException("The specified file is not a valid backup.");
            }
            if (!backupFile.exists()) {
                throw new IllegalStateException("The specified backup file no longer exists: "
                        + backupFile.getAbsolutePath());
            }
            File originalFile = new File(backupFile.getParentFile(), originalFilename);

            if (originalFile.exists()) {
                if (!originalFile.delete()) {
                    throw new IOException(
                            "Unable to delete original file during restoration of backup: "
                                    + backupFile.getAbsolutePath());
                }
            }
            backupFile.renameTo(originalFile);
        }
    }

    /**
     * Deletes the inidicated backup file from the local file system.
     * 
     * @param backupFile
     *            the backup file to remove
     */
    public static void removeBackupFile(File backupFile) {
        if ((backupFile != null) && backupFile.exists()) {
            backupFile.delete();
        }
    }

    /**
     * Returns the name of the backup file for the specified original.
     * 
     * @param originalFile
     *            the original file for which a backup is being created
     * @return String
     */
    public static String getBackupFilename(File originalFile) {
        String filename = null;

        if (originalFile != null) {
            String originalFilename = originalFile.getName();
            int dotIdx = originalFilename.lastIndexOf('.');

            filename = ((dotIdx < 0) ? originalFilename : originalFilename.substring(0, dotIdx))
                    + ".bak";
        }
        return filename;
    }

    /**
     * Initializes the validation schema and shared JAXB context.
     */
    static {
        try {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream schemaStream = SchemaDeclarations.OTA2_PROJECT_SCHEMA.getContent();

            schemaFactory.setResourceResolver(new ClasspathResourceResolver());
            projectValidationSchema = schemaFactory.newSchema(new StreamSource(schemaStream));
            jaxbContext = JAXBContext.newInstance(SCHEMA_CONTEXT);

        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

}
