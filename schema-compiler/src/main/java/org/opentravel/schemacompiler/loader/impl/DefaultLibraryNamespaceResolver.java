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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryNamespaceResolver;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.repository.impl.RepositoryItemImpl;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.version.OTA2VersionScheme;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.w3._2001.xmlschema.Schema;

/**
 * Default implementation of the <code>LibraryNamespaceResolver</code> that utilizes file hints from
 * the library imports to identify the mappings between namespaces and library files.
 * 
 * @author S. Livezey
 */
public class DefaultLibraryNamespaceResolver implements LibraryNamespaceResolver {
	
	private Map<String,String> repositoryUrlCache = new HashMap<>();
    private ProjectManager projectManager;
    private Object contextLibraryOrSchema;
    private String contextVersionScheme;
    private URL libraryUrl;

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryNamespaceResolver#resovleLibraryImport(java.net.URI,
     *      java.lang.String, java.lang.String[])
     */
    @Override
    public Collection<URL> resovleLibraryImport(URI libraryNamespace, String versionScheme,
            String[] fileHints) {
        Collection<URL> libraryUrls = new HashSet<URL>();
        String namespace = libraryNamespace.toString();

        for (String fileHint : ((fileHints == null) ? new String[0] : fileHints)) {
            if (fileHint.endsWith("/"))
                continue; // skip any non-filename hints
            URL referencedUrl;

            // Attempt to resolve the hint from the managed libraries that have already been loaded
            // into the model
            referencedUrl = resolveFromExistingProjectItems(namespace, fileHint);

            // If the hint could not be resolved to an existing item, attempt to resolve it using
            // default processing
            if (referencedUrl == null) {
            	URL fileUrl = resolveAsFilePath(fileHint);
            	
                // Only use this URL if it is a file URL and that file actually exists
                if ((fileUrl != null) && URLUtils.isFileURL(fileUrl) && URLUtils.toFile(fileUrl).exists()) {
                	referencedUrl = fileUrl;
                }
            }

            // If the hint could not be resolved to a local file, attempt to resolve the
            // hint as a repository URI
            if (referencedUrl == null) {
            	referencedUrl = resolveFromRepositoryURI(namespace, fileHint);
            }

            // If none of the previous lookup heuristics have succeeded, attempt a best-effort
            // search of the repository.
            /*
			*/
            if (referencedUrl == null) {
                URL bestEffortUrl = resolveWithBestEffortRepsositorySearch(namespace,
                        versionScheme, fileHint);

                // Only overwrite the current referencedUrl value if our best-effort search was
                // successful
                if (bestEffortUrl != null) {
                    referencedUrl = bestEffortUrl;
                }
            }
            
            // Add the resolved library URL if one of the above strategies was successful
            if (referencedUrl != null) {
                libraryUrls.add(referencedUrl);
            }
        }
        return libraryUrls;
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryNamespaceResolver#resovleLibraryInclude(java.net.URI,
     *      java.lang.String)
     */
    @Override
    public URL resovleLibraryInclude(URI libraryNamespace, String includePath) {
        Collection<URL> resolvedUrls = resovleLibraryImport(libraryNamespace, contextVersionScheme,
                new String[] { includePath });

        return resolvedUrls.isEmpty() ? null : resolvedUrls.iterator().next(); // only one possible
                                                                               // since we passed
                                                                               // only one file hint
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryNamespaceResolver#setModel(org.opentravel.schemacompiler.model.TLModel)
     */
    @Override
    public void setModel(TLModel model) {
        if (model != null) {
            projectManager = ProjectManager.getProjectManager(model);
            repositoryUrlCache.clear();
        }
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryNamespaceResolver#setContextLibrary(org.opentravel.schemacompiler.loader.LibraryModuleInfo,
     *      java.net.URL)
     */
    @Override
    public void setContextLibrary(LibraryModuleInfo<Object> contextLibrary, URL libraryUrl) {
        if (contextLibraryOrSchema != contextLibrary) {
            this.contextVersionScheme = contextLibrary.getVersionScheme();
            this.libraryUrl = libraryUrl;
            this.contextLibraryOrSchema = contextLibrary;
        }
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryNamespaceResolver#setContextSchema(org.opentravel.schemacompiler.loader.SchemaModuleInfo,
     *      java.net.URL)
     */
    @Override
    public void setContextSchema(LibraryModuleInfo<Schema> contextSchema, URL schemaUrl) {
        if (contextLibraryOrSchema != contextSchema) {
            this.contextVersionScheme = contextSchema.getVersionScheme();
            this.libraryUrl = schemaUrl;
            this.contextLibraryOrSchema = contextSchema;
        }
    }

    /**
	 * @see org.opentravel.schemacompiler.loader.LibraryNamespaceResolver#setRepositoryLocation(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setRepositoryLocation(String repositoryUri, String namespace, String libraryUrl) {
        if (repositoryUri.startsWith("otm://")) {
        	String cacheKey = namespace + "~" + repositoryUri;
        	repositoryUrlCache.put( cacheKey, libraryUrl );
        }
	}

	/**
     * Attempts to resolve a URL for the given namespace and file path using the libraries that have
     * already been loaded by the project manager. If the current model does not have an associated
     * project manager, this method will always return null.
     * 
     * @param namespace
     *            the expected namespace of the library to be resolved
     * @param filePath
     *            the path and name of the file to be resolved
     * @return URL
     */
    protected URL resolveFromExistingProjectItems(String namespace, String filePath) {
        URL referencedUrl = null;

        if (projectManager != null) {
            int separatorIdx = filePath.lastIndexOf('/');
            String libraryFilename = (separatorIdx < 0) ? filePath : filePath
                    .substring(separatorIdx + 1);

            // Search the list of known repository items for a match to this file hint
            for (ProjectItem item : projectManager.getAllProjectItems()) {
                // Skip project items that have errors or cannot be resolved
                if ((item == null) || (item.getNamespace() == null) || (item.getFilename() == null)) {
                    continue;
                }
                // Skip project items that are unmanaged
                if (item.getState() == RepositoryItemState.UNMANAGED) {
                    continue;
                }
                // Skip project items that do not match this namespace/file-hint combination
                if (!item.getNamespace().equals(namespace)
                        || !item.getFilename().equals(libraryFilename)) {
                    continue;
                }
                referencedUrl = item.getContent().getLibraryUrl();
            }
        }
        return referencedUrl;
    }

    /**
     * Attempts to resolve a URL for the given namespace and file path as a repository URI. If the
     * file path is not a repository URI, this method will return null.
     * 
     * @param namespace
     *            the expected namespace of the library to be resolved
     * @param filePath
     *            the path and name of the file to be resolved
     * @return URL
     */
    protected URL resolveFromRepositoryURI(String namespace, String filePath) {
        URL referencedUrl = null;

        if (filePath.startsWith("otm://")) {
            try {
            	String cacheKey = namespace + "~" + filePath;
            	String cachedUrl = repositoryUrlCache.get(cacheKey);
            	
            	if (cachedUrl != null) {
            		referencedUrl = new URL( cachedUrl );
            		
            	} else {
                    RepositoryManager repositoryManager = (projectManager != null) ? projectManager
                            .getRepositoryManager() : RepositoryManager.getDefault();
                    RepositoryItem repositoryItem = repositoryManager.getRepositoryItem(filePath,
                            namespace);

                    if (repositoryItem != null) {
                        referencedUrl = repositoryManager.getContentLocation(repositoryItem);
                        repositoryUrlCache.put( cacheKey, referencedUrl.toExternalForm() );
                    }
            	}
            } catch (MalformedURLException e) {
                // Should never happen - return null
            } catch (RepositoryException e) {
                // No error - return null
            } catch (URISyntaxException e) {
                // No error - return null
            }

            // If the lookup attempt did not resolve to a valid repository item, attempt to build a
            // URL
            // using the URI string provided. This should provide a validation error when the lookup
            // is
            // attempted by the loader (after this namespace resolver returns its value).
            if (referencedUrl == null) {
                try {
                    referencedUrl = new URL(filePath);

                } catch (MalformedURLException e) {
                    // Return null instead of throwing an exception
                }
            }
        }
        return referencedUrl;
    }

    /**
     * Attempts to resolve a URL for the given file path as an absolute URL or a relative URL to the
     * current context library.
     * 
     * @param filePath
     *            the file path to be resolved
     * @return URL
     */
    protected URL resolveAsFilePath(String filePath) {
        URL referencedUrl = null;

        try {
            if (filePath != null) {
                referencedUrl = new URL(filePath);
            }

        } catch (MalformedURLException e) {
            // If the path is not a fully-qualified URL, attempt to use it as a relative URL
            try {
                String contextFolderPath = getContextFolderUrl();

                if ((contextFolderPath != null) && (filePath != null)) {
                    referencedUrl = new URL(new StringBuilder(getContextFolderUrl()).append(
                            filePath).toString());
                }

            } catch (MalformedURLException e2) {
                // No action - ignore malformed URL's from bad file paths
            }
        }
        return referencedUrl;
    }

    /**
     * Attempts to resolve a URL for the given namespace and file path using a best-effort search of
     * the known repositories. If the version scheme provided is null, the search will not be
     * attempted and this method will return null.
     * 
     * @param namespace
     *            the expected namespace of the library to be resolved
     * @param versionScheme
     *            the version scheme to apply when interpreting the namespace provided
     * @param filePath
     *            the path and name of the file to be resolved
     * @return URL
     */
    protected URL resolveWithBestEffortRepsositorySearch(String namespace, String versionScheme,
            String filePath) {
        RepositoryManager repositoryManager = null;
        URL referencedUrl = null;

        // First, we need to locate the correct repository manager instance
        if (projectManager != null) {
            repositoryManager = projectManager.getRepositoryManager();

        } else {
            try {
                repositoryManager = RepositoryManager.getDefault();

            } catch (RepositoryException e) {
                // No error - method will return null
            }
        }

        if ((repositoryManager != null) && (versionScheme != null)) {
            RepositoryItemImpl repositoryItem = new RepositoryItemImpl();
            List<Repository> repositories = new ArrayList<Repository>();

            repositories.addAll(repositoryManager.listRemoteRepositories());
            repositories.add(repositoryManager); // the repository manager owns the local repository

            // Configure the repository item using information we know from the requestor
            try {
                VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme(
                        versionScheme);

                repositoryItem.setBaseNamespace(vScheme.getBaseNamespace(namespace));
                repositoryItem.setVersion(vScheme.getVersionIdentifier(namespace));

            } catch (VersionSchemeException e) {
                repositoryItem.setBaseNamespace(namespace);
                repositoryItem.setVersion(new OTA2VersionScheme().getDefaultVersionIdentifer());

            } finally {
                repositoryItem.setNamespace(namespace);
                repositoryItem.setFilename(getFilename(filePath));
                repositoryItem.setState(RepositoryItemState.MANAGED_UNLOCKED);
            }

            // Attempt to resolve our mocked-up repository item in each of the known repositories
            if (repositoryItem.getFilename() != null) {
                for (Repository repository : repositories) {
                    repositoryItem.setRepository(repository);

                    try {
                        referencedUrl = repositoryManager.getContentLocation(repositoryItem);
                        break; // Stop when we find the first match

                    } catch (RepositoryException e) {
                        // Ignore and move on to the next repository
                    }
                }
            }
        }
        return referencedUrl;
    }

    /**
     * Returns the URL of the folder that contains the context library (or null if a context library
     * has not been assigned.
     * 
     * @return String
     */
    protected String getContextFolderUrl() {
        String contextFolderUrl = null;

        if (libraryUrl != null) {
            String folderUrl = libraryUrl.toExternalForm();
            int folderIdx = folderUrl.lastIndexOf('/');

            if (folderIdx >= 0) {
                contextFolderUrl = folderUrl.substring(0, folderIdx);
            } else {
                contextFolderUrl = folderUrl.substring(0, folderUrl.length()
                        - libraryUrl.getFile().length());
            }
            if (!contextFolderUrl.endsWith("/")) {
                contextFolderUrl += "/";
            }
        }
        return contextFolderUrl;
    }

    /**
     * Returns the filename as the last component of the path provided. If the given file path is
     * null, or a filename is not specified (i.e. the path ends with '/'), this method will return
     * null.
     * 
     * @param filePath
     *            the file path from which to extract the filename
     * @return String
     */
    private String getFilename(String filePath) {
        String filename = null;

        if ((filePath != null) && !filePath.endsWith("/")) {
            int separatorIdx = filePath.lastIndexOf('/');

            if (separatorIdx < 0) {
                filename = filePath;

            } else {
                filename = filePath.substring(separatorIdx + 1);
            }
        }
        return filename;
    }

}
