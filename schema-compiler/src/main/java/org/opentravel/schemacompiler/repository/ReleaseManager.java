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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.opentravel.ns.ota2.release_v01_00.ReleaseStatus;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.LoaderValidationMessageKeys;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.impl.ReleaseFileUtils;
import org.opentravel.schemacompiler.repository.impl.ReleaseItemImpl;
import org.opentravel.schemacompiler.repository.impl.ReleaseLibraryModuleLoader;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.transform.util.ModelReferenceResolver;
import org.opentravel.schemacompiler.util.ExceptionUtils;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.compile.TLModelCompileValidator;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the loading and saving of OTM releases on the local file system
 * and the OTM repository.
 */
public class ReleaseManager implements LoaderValidationMessageKeys {
	
    private static final Logger log = LoggerFactory.getLogger( ReleaseManager.class );
    private static final VersionScheme versionScheme;
    
	private Release release;
	private TLModel model;
	private RepositoryManager repositoryManager;
	private ReleaseFileUtils fileUtils;
	
	/**
	 * Default constructor.
	 * 
	 * @throws RepositoryException  thrown if the underlying repository manager cannot be initialized
	 */
	public ReleaseManager() throws RepositoryException {
		this( RepositoryManager.getDefault() );
	}
	
	/**
	 * Constructor that supplies a pre-configured repository manager.
	 * 
	 * @param repositoryManager  the repository manager to use when accessing remote content
	 */
	public ReleaseManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
		this.fileUtils = new ReleaseFileUtils( repositoryManager );
		this.model = new TLModel();
		new ProjectManager( model, false, repositoryManager );
	}
	
	/**
	 * Creates a new release for this manager instance.  The version of the new release will
	 * be automatically set to "1.0.0".
	 * 
	 * @param baseNamespace  the base namespace of the new release
	 * @param name  the name of the new release
	 * @param folderLocation  the folder location where the release file should be saved
	 * @return ReleaseItem
	 * @throws LibrarySaveException  thrown if an error occurs while saving the release file
	 */
	public ReleaseItem createNewRelease(String baseNamespace, String name, File folderLocation)
			throws LibrarySaveException {
		if ((baseNamespace == null) || (name == null)
				|| baseNamespace.trim().equals("") || name.trim().equals("")) {
			throw new IllegalArgumentException("The namespace and name of a new release cannot be null or blank.");
		}
		File releaseFile;
		
		this.model.clearModel();
		this.release = new Release();
		
		release.setBaseNamespace( baseNamespace );
		release.setName( name );
		release.setVersion( "1.0.0" );
		release.setStatus( ReleaseStatus.DRAFT );
		releaseFile = new File( folderLocation, fileUtils.getReleaseFilename( release ) );
		release.setReleaseUrl( URLUtils.toURL( releaseFile ) );
		
		saveRelease();
		
		return ReleaseItemImpl.newUnmanagedItem( this );
	}
	
	/**
	 * Returns the file handle for the location where a new release will be saved
	 * during the create operation.
	 * 
	 * @param name  the name of the new release
	 * @param folderLocation  the folder location where the release file should be saved
	 * @return File
	 */
	public File getNewReleaseFile(String name, File folderLocation) {
		Release dummy = new Release();
		
		dummy.setBaseNamespace( "http://www.opentravel.org" );
		dummy.setName( name );
		dummy.setVersion( "1.0.0" );
		return new File( folderLocation, fileUtils.getReleaseFilename( dummy ) );
	}
	
	/**
	 * Loads the contents of a release from the specified file on the local file system.
	 * 
	 * @param releaseFile  the OTM release file to load
	 * @param findings  the validation findings where errors and warning should be reported
	 * @return ReleaseItem
	 * @throws RepositoryException  thrown if an error occurs that prevents the release from being loaded
	 */
	public ReleaseItem loadRelease(File releaseFile, ValidationFindings findings) throws RepositoryException {
		try {
	    	if (findings == null) findings = new ValidationFindings();
	    	
			if (isRepositoryFile( releaseFile )) { // treat as a managed repository release file
				return loadRelease( repositoryManager.getRepositoryItem( releaseFile ), findings );
				
			} else {
				// Clear any existing release & model info so we will not be left in an inconsistent
				// state if something goes wrong during the load.
				this.model.clearModel();
				this.release = null;
				
				this.release = fileUtils.loadReleaseFile( releaseFile, findings );
				loadReleaseModel( findings );
				return ReleaseItemImpl.newUnmanagedItem( this );
			}
			
		} catch (LibraryLoaderException e) {
			throw new RepositoryException("Unexpected error loading release content.", e);
		}
	}
	
	/**
	 * Loads the contents of a release from a remote OTM repository.
	 * 
	 * @param releaseItem  the OTM release item to load
	 * @param findings  the validation findings where errors and warning should be reported
	 * @return ReleaseItem
	 * @throws RepositoryException  thrown if an error occurs that prevents the release from being loaded
	 */
	public ReleaseItem loadRelease(RepositoryItem releaseItem, ValidationFindings findings) throws RepositoryException {
		try {
	    	if (findings == null) findings = new ValidationFindings();
			repositoryManager.refreshLocalCopy( releaseItem );
			URL releaseUrl = repositoryManager.getContentLocation( releaseItem );
			File releaseFile = URLUtils.toFile( releaseUrl );
			ReleaseItem loadedItem = null;
			
			// Clear any existing release & model info so we will not be left in an inconsistent
			// state if something goes wrong during the load.
			this.model.clearModel();
			this.release = null;
			
			this.release = fileUtils.loadReleaseFile( releaseFile, findings );
			
			if (!findings.hasFinding( FindingType.ERROR )) {
				loadReleaseModel( findings );
				loadedItem = ReleaseItemImpl.newManagedItem( releaseItem, this );
			}
			return loadedItem;
			
		} catch (LibraryLoaderException e) {
			throw new RepositoryException("Unexpected error loading release content.", e);
		}
	}
	
	/**
	 * Saves the content of the current release.
	 * 
	 * @throws LibrarySaveException  thrown if an error occurs while saving the release file
	 * @throws IllegalStateException  thrown if a release has not been created or loaded
	 *								  for this manager
	 * @throws UnsupportedOperationException  thrown if the release is remotely-managed
	 *										  and therefore cannot be modified
	 */
	public void saveRelease() throws LibrarySaveException {
		checkModificationAllowed();
		release.setStatus( ReleaseStatus.DRAFT ); // Force DRAFT state for locally-managed files
		fileUtils.saveReleaseFile( release, true );
	}
	
	/**
	 * Saves a copy of the content of the current release.
	 * 
	 * @param targetFolder  the target folder where the copy of the release is to be saved
	 * @throws LibrarySaveException  thrown if an error occurs while saving the release file
	 * @throws IllegalStateException  thrown if a release has not been created or loaded
	 *								  for this manager
	 * @throws UnsupportedOperationException  thrown if the release is remotely-managed
	 *										  and therefore cannot be modified
	 */
	public void saveReleaseAs(File targetFolder) throws LibrarySaveException {
		checkModificationAllowed();
		
		if (targetFolder == null) {
			throw new LibrarySaveException("The folder location cannot be null.");
			
		} else if (targetFolder.exists() && !targetFolder.isDirectory()) {
			throw new LibrarySaveException(
					"The specified folder location is not a valid directory: " +
							targetFolder.getAbsolutePath());
		}
		File saveAsFile = getSaveAsFile( targetFolder );
		
		release.setReleaseUrl( URLUtils.toURL( saveAsFile ) );
		release.setStatus( ReleaseStatus.DRAFT ); // Force DRAFT state for locally-managed files
		fileUtils.saveReleaseFile( release, true );
	}
	
	/**
	 * Returns the file handle for the location where this release will be saved
	 * during the save-as operation.
	 * 
	 * @param folderLocation  the target folder where the copy of the release is to be saved
	 * @return File
	 */
	public File getSaveAsFile(File folderLocation) {
		return new File( folderLocation, fileUtils.getReleaseFilename( release ) );
	}
	
	/**
	 * Returns the corresponding release member from the principal items of the release
	 * or null if no corresponding item exists.
	 * 
	 * @param item  the repository item for which to return a release item
	 * @return ReleaseMember
	 */
	public ReleaseMember getPrincipalMember(RepositoryItem item) {
		return findReleaseMember( item, true, false );
	}
	
	/**
	 * Adds a principal member to the current release.  If the given repository
	 * item is already a principal member, this method has no effect.  If the
	 * item is currently a referenced member, it will be moved to the set of
	 * principal members.
	 * 
	 * @param item  the repository item to be added
	 * @return ReleaseMember
	 * @throws RepositoryException  thrown if the remote repository cannot be accessed
	 * @throws IllegalStateException  thrown if a release has not been created or loaded
	 *								  for this manager
	 * @throws UnsupportedOperationException  thrown if the release is remotely-managed
	 *										  and therefore cannot be modified
	 */
	public ReleaseMember addPrincipalMember(RepositoryItem item) throws RepositoryException {
		checkModificationAllowed();
		return addPrincipalMember( item, release.getDefaultEffectiveDate() );
	}
	
	/**
	 * Adds a principal item to the current release and assigns the given effective
	 * date.  If the given repository item is already a principal member, this method
	 * has no effect beyond assigning the effective date.  If the item is currently a
	 * referenced item, it will be moved to the set of principal members.
	 * 
	 * @param item  the repository item to be added
	 * @param effectiveDate  the effective date to apply for the item (null for latest commit)
	 * @return ReleaseMember
	 * @throws RepositoryException  thrown if the remote repository cannot be accessed
	 * @throws IllegalStateException  thrown if a release has not been created or loaded
	 *								  for this manager
	 * @throws UnsupportedOperationException  thrown if the release is remotely-managed
	 *										  and therefore cannot be modified
	 */
	public ReleaseMember addPrincipalMember(RepositoryItem item, Date effectiveDate)
			throws RepositoryException {
		ReleaseMember releaseMember = findReleaseMember( item, true, false );
		checkModificationAllowed();
		
		if (releaseMember == null) {
			releaseMember = findReleaseMember( item, false, true );
			
			if (releaseMember != null) { // move from referenced to principal list
				release.getReferencedMembers().remove( releaseMember );
				
			} else { // brand new item for this release
				releaseMember = new ReleaseMember();
				releaseMember.setRepositoryItem( item );
			}
			release.getPrincipalMembers().add( releaseMember );
			setEffectiveDate( releaseMember );
		}
		return releaseMember;
	}
	
	/**
	 * Removes the given repository item from the list of principal items in the current
	 * release.  If no such repository item is in the principal list, this method has
	 * no effect.
	 * 
	 * @param item  the repository item to remove from this release
	 * @throws IllegalStateException  thrown if a release has not been created or loaded
	 *								  for this manager
	 * @throws UnsupportedOperationException  thrown if the release is remotely-managed
	 *										  and therefore cannot be modified
	 */
	public void removePrincipalMember(RepositoryItem item) {
		ReleaseMember releaseMember = findReleaseMember( item, true, true );
		
		if (releaseMember != null) {
			release.getPrincipalMembers().remove( releaseMember );
			release.getReferencedMembers().remove( releaseMember );
		}
	}
	
	/**
	 * Returns the corresponding release member from the referenced members of the
	 * release or null if no corresponding item exists.
	 * 
	 * @param item  the repository item for which to return a release member
	 * @return ReleaseMember
	 */
	public ReleaseMember getReferencedMember(RepositoryItem item) {
		return findReleaseMember( item, false, true );
	}
	
	/**
	 * Returns the repository item associated with the given repository URL or null
	 * if the given URL is not for a managed library.
	 * 
	 * @param libraryUrl  the library URL for which to return a repository item
	 * @return RepositoryItem
	 * @throws RepositoryException  thrown if an error occurs while retrieving the repository item
	 */
	public RepositoryItem getRepositoryItem(URL libraryUrl) throws RepositoryException {
		File libraryFile = URLUtils.isFileURL( libraryUrl ) ? URLUtils.toFile( libraryUrl ) : null;
		RepositoryItem item = null;
		
		if (isRepositoryFile( libraryFile )) {
			item = repositoryManager.getRepositoryItem( libraryFile );
		}
		return item;
	}
	
	/**
	 * Returns the commit history for the given release member.
	 * 
	 * @param member  the release member for which to retrieve the history
	 * @return List<RepositoryItemCommit>
	 * @throws RepositoryException  thrown if an error occurs while retrieving the item's history
	 */
	public List<RepositoryItemCommit> getCommitHistory(ReleaseMember member) throws RepositoryException {
		RepositoryItemHistory history = repositoryManager.getHistory( member.getRepositoryItem() );
		List<RepositoryItemCommit> commitHistory = null;
		
		if (history != null) {
			commitHistory = history.getCommitHistory();
		}
		return commitHistory;
	}
	
	/**
	 * Returns the release that was loaded by this manager.
	 * 
	 * @return Release
	 */
	public Release getRelease() {
		return release;
	}
	
	/**
	 * Returns the model that was created from one of the 'loadRelease()' methods.
	 * 
	 * @return TLModel
	 */
	public TLModel getModel() {
		return model;
	}
	
	/**
	 * Returns the library associated with the given release member or null if no
	 * such library exists in the model.
	 * 
	 * @param member  the release member for which to return a model library
	 * @return AbstractLibrary
	 */
	public AbstractLibrary getLibrary(ReleaseMember member) {
		RepositoryItem item = member.getRepositoryItem();
		List<AbstractLibrary> candidateLibs = model.getLibrariesForNamespace( item.getNamespace() );
		AbstractLibrary library = null;
		
		for (AbstractLibrary candidateLib : candidateLibs) {
			if (candidateLib.getName().equals( item.getLibraryName() )) {
				library = candidateLib;
				break;
			}
		}
		return library;
	}
	
	/**
	 * Loads or reloads the model to incorporate changes to effective dates and principal
	 * release members.  During the re-load, the list of referenced libraries for the
	 * release is updated.  Referenced members that are no longer required are removed
	 * and new dependencies are added using the default effective date for the release.
	 * 
	 * @param findings  the validation findings where any load-time errors or warnings
	 *					should be reported
	 * @throws RepositoryException  thrown if a remote repository cannot be accessed
	 * @throws LibraryLoaderException  thrown if an exception occurs during the load process
	 */
	public void loadReleaseModel(ValidationFindings findings)
			throws RepositoryException, LibraryLoaderException {
    	if (findings == null) findings = new ValidationFindings();
        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>( model );
		ReleaseLibraryModuleLoader moduleLoader = new ReleaseLibraryModuleLoader( this, modelLoader.getModuleLoader() );
		
		model.clearModel();
		modelLoader.getNamespaceResolver().setModel( model );
		modelLoader.setModuleLoader( moduleLoader );
		modelLoader.setResolveModelReferences( false );
		
		// Only force-load the principal items; referenced items will be refreshed
		for (ReleaseMember principalMember : release.getPrincipalMembers()) {
			LibraryInputSource<InputStream> inputSource =
					repositoryManager.getHistoricalContentSource(
							principalMember.getRepositoryItem(), principalMember.getEffectiveDate() );
			
			modelLoader.loadLibraryModel( inputSource );
		}
		ModelReferenceResolver.resolveReferences( model );
		moduleLoader.updateReferencedItems();
		validateModel( findings );
	}
	
	/**
	 * Returns true if the current release is a repository-managed item.
	 * 
	 * @return boolean
	 */
	public boolean isManagedRelease() {
		File releaseFile = getReleaseFile();
		return (releaseFile != null) && isRepositoryFile( releaseFile );
	}
	
    /**
     * Publishes the given release to the OTM repository.
     * 
     * @param repository  the repository to which a release should be published
     * @return ReleaseItem
     * @throws RepositoryException  thrown if the publication fails for any reason
     * @throws IllegalStateException  thrown if the release is already managed by a repository
     */
    public ReleaseItem publishRelease(Repository repository) throws RepositoryException {
    	URL originalUrl = release.getReleaseUrl();
    	File releaseFile = getReleaseFile();
    	boolean success = false;
    	
    	// Perform validation checks
    	if (release == null) {
    		throw new IllegalStateException("A release must be created or loaded before attempting to publish.");
    		
    	} else  if (releaseFile == null) {
    		throw new IllegalStateException("The release is not a locally-managed file and therefore cannot be published.");
    		
    	} else if (isRepositoryFile( releaseFile )) {
    		throw new IllegalStateException("The release is already managed by a remote repository.");
    	}
    	
    	// Backup the file locally before publishing
    	try {
    		ReleaseFileUtils.createBackupFile( releaseFile );
    		
    	} catch (IOException e) {
    		throw new RepositoryException("Error creating release backup file: " + releaseFile.getName(), e);
    	}
    	
    	// Update the release's status to BETA or FULL
    	try {
        	ReleaseStatus newStatus = (release.getDefaultEffectiveDate() == null) ? ReleaseStatus.FULL : ReleaseStatus.BETA;
        	
        	if (newStatus != ReleaseStatus.BETA) {
        		for (ReleaseMember member : release.getAllMembers()) {
        			if (member.getEffectiveDate() != null) {
        				newStatus = ReleaseStatus.BETA;
        				break;
        			}
        		}
        	}
        	release.setStatus( newStatus );
        	fileUtils.saveReleaseFile( release, false );
    		
    	} catch (LibrarySaveException e) {
    		throw new RepositoryException("Unable to read from release data file: " + releaseFile.getName(), e);
    	}
    	
    	// Publish the release and delete the local copy of the file (backup will remain)
    	try (InputStream contentStream = new FileInputStream( releaseFile )) {
        	RepositoryItem repoItem = repository.publish( contentStream,
        			fileUtils.getReleaseFilename( release ), release.getName(), release.getNamespace(),
        			release.getVersion(), VersionSchemeFactory.getInstance().getDefaultVersionScheme(),
        			TLLibraryStatus.FINAL );
        	
        	release.setReleaseUrl( repositoryManager.getContentLocation( repoItem ) );
        	releaseFile.delete();
        	success = true;
    		return ReleaseItemImpl.newManagedItem( repoItem, this );
    		
    	} catch (IOException e) {
    		throw new RepositoryException("Unable to read from release data file: " + releaseFile.getName(), e);
    		
    	} finally {
    		if (!success) {
    			release.setReleaseUrl( originalUrl );
    			release.setStatus( ReleaseStatus.DRAFT );
    		}
    	}
    	
    }
    
    /**
     * Unpublishes the release from the OTM repository (requires administrative access).  The
     * current state of the release is saved to the local file system as an unmanaged file before
     * deleting from the repository.  If a null folder location is passed to this method, the
     * release will be deleted from the repository without saving it to the local file system.
     * 
     * <p>This method returns a new <code>RepositoryManager</code> instance that should be
     * used to manage the locally-managed copy of the release (if one was created).
     * 
     * @param saveFolder  the folder to which the release file should be saved (may be null)
     * @return ReleaseManager
     * @throws RepositoryException  thrown if the repository file cannot be unpublished
     * @throws LibrarySaveException  thrown if the new release copy cannot be saved
     */
    public ReleaseManager unpublishRelease(File saveFolder)
    		throws RepositoryException, LibrarySaveException {
    	File releaseFile = getReleaseFile();
    	ReleaseManager newManager = null;
    	
    	// Perform validation checks
    	if (release == null) {
    		throw new IllegalStateException("A release must be loaded from a repository before attempting to unpublish.");
    		
    	} else if ((releaseFile == null) || !isRepositoryFile( releaseFile )) {
    		throw new IllegalStateException(
    				"The release is not managed by a remote repository and therefore cannot be unpublished.");
    	}
    	
    	// Save a local copy of the release file (if required)
    	if (saveFolder != null) {
    		Release newRelease = cloneRelease();
    		File newVersionFile;
    		
    		newManager = new ReleaseManager( repositoryManager );
    		newRelease.setStatus( ReleaseStatus.DRAFT );
    		newVersionFile = new File( saveFolder, fileUtils.getReleaseFilename( newRelease ) );
    		newRelease.setReleaseUrl( URLUtils.toURL( newVersionFile ) );
    		fileUtils.saveReleaseFile( newRelease, false );
    		newManager.loadRelease( newVersionFile, new ValidationFindings() );
    		
    	}
    	
    	// Now we can delete the release from the remote repository
    	RepositoryItem repoItem = repositoryManager.getRepositoryItem( release.getBaseNamespace(),
    			fileUtils.getReleaseFilename( release ), release.getVersion() );
    	
    	repositoryManager.delete( repoItem );
    	model.clearModel();
    	release = null;
    	
    	return newManager;
    }
    
    /**
     * Creates a new version of the current release and saves it as an unmanaged local
     * file in the specified folder location.  the content of the new release will initially
     * be exactly the same as that of the previous version.
     * 
     * <p>This method returns a new <code>RepositoryManager</code> instance that should be
     * used to manage the new version of the release.
     * 
     * @param saveFolder  the folder location to which the new release version should be saved
     * @param findings  the validation findings found while loading the new version of the release
     * @return ReleaseManager
     * @throws RepositoryException  thrown if a later version of the current release already
     *								exists in the remote repository
     * @throws LibrarySaveException  thrown if the new release version cannot be saved
     */
    public ReleaseManager newVersion(File saveFolder, ValidationFindings findings)
    		throws RepositoryException, LibrarySaveException {
    	if (findings == null) findings = new ValidationFindings();
		File releaseFile = getReleaseFile();
		
		// Verify that a later version of this release does not already exist
		if ((releaseFile != null) && isRepositoryFile( releaseFile )) {
			List<RepositoryItem> itemList = repositoryManager.listItems(
					release.getBaseNamespace(), TLLibraryStatus.FINAL, true, RepositoryItemType.RELEASE );
			String releaseVersion = versionScheme.getMajorVersion( release.getVersion() );
			
			for (RepositoryItem item : itemList) {
				if (item.getLibraryName().equals( release.getName() )) {
					String itemVersion = versionScheme.getMajorVersion( item.getVersion() );
					
					try {
						if (Integer.parseInt( itemVersion ) > Integer.parseInt( releaseVersion )) {
							throw new RepositoryException(
									"Cannot create a new version because a later version of this release already exists.");
						}
						
					} catch (NumberFormatException e) {
						throw new RepositoryException("Unrecognized version identifier", e);
					}
				}
			}
		}
		
		// Create the new release version and its new repository manager
		String newVersion = versionScheme.incrementMajorVersion( release.getVersion() );
		ReleaseManager newManager = new ReleaseManager( repositoryManager );
		Release newRelease = cloneRelease();
		File newVersionFile;
		
		newRelease.setVersion( newVersion );
		newVersionFile = new File( saveFolder, fileUtils.getReleaseFilename( newRelease ) );
		newRelease.setReleaseUrl( URLUtils.toURL( newVersionFile ) );
		fileUtils.saveReleaseFile( newRelease, false );
		newManager.loadRelease( newVersionFile, findings );
		
		return newManager;
    }
    
	/**
	 * Returns the file handle for the location where a new release will be saved
	 * during the create operation.
	 * 
	 * @param folderLocation  the folder location where the release file should be saved
	 * @return File
	 */
	public File getNewVersionFile(File folderLocation) {
		Release dummy = new Release();
		
		dummy.setBaseNamespace( release.getBaseNamespace() );
		dummy.setName( release.getName() );
		dummy.setVersion( versionScheme.incrementMajorVersion( release.getVersion() ) );
		return new File( folderLocation, fileUtils.getReleaseFilename( dummy ) );
	}
	
    /**
     * Creates a new copy of the current release and saves it as an unmanaged local
     * file in the specified folder location.  The members of the new release will be
     * identical to that of the the current release, but its base namespace + name will
     * be reassigned, the status will be DRAFT, and the version will be reset to "1.0.0".
     * 
     * <p>This method returns a new <code>RepositoryManager</code> instance that should be
     * used to manage the new copy of the release.
     * 
     * @param releaseName  the name for the new copy of the release 
     * @param saveFolder  the folder location to which the new release should be saved
     * @param findings  the validation findings found while loading the new copy of the release
     * @return ReleaseManager
     * @throws LibrarySaveException  thrown if the new release copy cannot be saved
     */
    public ReleaseManager copyRelease(String baeNamespace, String releaseName, File saveFolder,
    		ValidationFindings findings) throws RepositoryException, LibrarySaveException {
    	if (findings == null) findings = new ValidationFindings();
		ReleaseManager newManager = new ReleaseManager( repositoryManager );
		Release newRelease = cloneRelease();
		File newVersionFile;
		
		newRelease.setName( releaseName );
		newRelease.setVersion( "1.0.0" );
		newRelease.setStatus( ReleaseStatus.DRAFT );
		newVersionFile = new File( saveFolder, fileUtils.getReleaseFilename( newRelease ) );
		newRelease.setReleaseUrl( URLUtils.toURL( newVersionFile ) );
		fileUtils.saveReleaseFile( newRelease, false );
		newManager.loadRelease( newVersionFile, findings );
		
		return newManager;
    }
    
    /**
     * Returns an input source for the given release member that can be used to retrieve
     * its content from the OTM repository.
     * 
     * @param releaseMember  the release member for which to return an input source
     * @return LibraryInputSource<InputStream>
     * @throws RepositoryException  thrown if the input source cannot be retrieved
     */
    public LibraryInputSource<InputStream> getInputSource(ReleaseMember releaseMember)
    		throws RepositoryException {
    	return repositoryManager.getHistoricalContentSource(
    			releaseMember.getRepositoryItem(), releaseMember.getEffectiveDate() );
    }
    
    /**
     * Validates the contents of the current model.
     * 
     * @return ValidationFindings
     */
    public ValidationFindings validateModel() {
    	ValidationFindings findings = new ValidationFindings();
    	
    	validateModel( findings );
    	return findings;
    }
    
    /**
     * Validates the model and capturing any errors/warnings that exist.
     * 
     * @param findings  the validation findings where any errors or warnings should be reported
     */
    private void validateModel(ValidationFindings findings) {
        for (TLLibrary library : model.getUserDefinedLibraries()) {
            try {
                findings.addAll( TLModelCompileValidator.validateModelElement( library, false ) );

            } catch (Throwable t) {
                findings.addFinding( FindingType.ERROR, library,
                        ERROR_UNKNOWN_EXCEPTION_DURING_VALIDATION, library.getName(),
                        ExceptionUtils.getExceptionClass( t ).getSimpleName(),
                        ExceptionUtils.getExceptionMessage( t ) );
                log.debug("Unexpected exception validating liberary module: " + library.getName(), t);
            }
        }
    }
    
    /**
     * Checks to see whether modification of the current release is allowed and throws
     * an appropriate exception if it is not.
     * 
	 * @throws IllegalStateException  thrown if a release has not been created or loaded
	 *								  for this manager
	 * @throws UnsupportedOperationException  thrown if the release is remotely-managed
	 *										  and therefore cannot be modified
     */
    private void checkModificationAllowed() {
		if (release == null) {
			throw new IllegalStateException("Unable to add - no release has been created or loaded.");
		}
		try {
			File releaseFile = getReleaseFile();
			
			if ((releaseFile != null) && isRepositoryFile( releaseFile )) {
				throw new UnsupportedOperationException("Releases published to an OTM repository cannot be modified.");
			}
			
		} catch (IllegalArgumentException e) {
			throw new UnsupportedOperationException(
					"The release is missing key information (member libraries cannot be modified).");
		}
    }
    
	/**
	 * Assigns an appropriate effective date to the new release member.
	 * 
	 * @param member  the new release member whose effective date is to be configured
	 * @throws RepositoryException  thrown if the remote repository cannot be accessed
	 */
	private void setEffectiveDate(ReleaseMember member) throws RepositoryException {
		List<RepositoryItemCommit> commitHistory = getCommitHistory( member );
		Date defaultEffectiveDate = release.getDefaultEffectiveDate();
		
		if (defaultEffectiveDate != null) {
			Date latestCommitDate = null;
			
			for (RepositoryItemCommit commit : commitHistory) {
				if ((latestCommitDate == null) || commit.getEffectiveOn().after( latestCommitDate )) {
					latestCommitDate = commit.getEffectiveOn();
				}
			}
			if (defaultEffectiveDate.after( latestCommitDate )) {
				member.setEffectiveDate( latestCommitDate );
				
			} else {
				member.setEffectiveDate( defaultEffectiveDate );
			}
			
		} else {
			member.setEffectiveDate( null );
		}
	}

    /**
     * Returns the local file system location of the release file or null if no
     * such file exists.
     * 
     * @return File
     */
    private File getReleaseFile() {
    	File releaseFile = null;
    	
    	if (release != null) {
    		releaseFile = URLUtils.isFileURL( release.getReleaseUrl() ) ?
					URLUtils.toFile( release.getReleaseUrl() ) : null;
    	}
    	return releaseFile;
    }

    /**
     * Returns the release member from the current release that corresponds to the
     * given repository item.
     * 
     * @param repoItem  the repository item for which to return the corresponding release member
     * @param includePrincipalMembers  flag indicating whether principal members should be considered
     * @param includeReferencedMembers  flag indicating whether referenced members should be considered
     * @return ReleaseMember
     */
    private ReleaseMember findReleaseMember(RepositoryItem repoItem, boolean includePrincipalMembers,
    		boolean includeReferencedMembers) {
    	ReleaseMember releaseMember = null;
    	
    	if (release != null) {
    		List<ReleaseMember> allItems = new ArrayList<>();
    		
    		if (includePrincipalMembers) {
        		allItems.addAll( release.getPrincipalMembers() );
    		}
    		if (includeReferencedMembers) {
        		allItems.addAll( release.getReferencedMembers() );
    		}
    		
    		for (ReleaseMember item : allItems) {
    			if (isEquivalent( item, repoItem )) {
    				releaseMember = item;
    				break;
    			}
    		}
    	}
    	return releaseMember;
    }
    
    /**
     * Returns true if the given release member is equivalent to the repository member.
     * 
     * @param releaseMember  the release member to compare
     * @param repoItem  the repository item to compare
     * @return boolean
     */
    private boolean isEquivalent(ReleaseMember releaseMember, RepositoryItem repoItem) {
    	boolean result = false;
    	
    	if ((releaseMember != null) && (releaseMember.getRepositoryItem() != null) && (repoItem != null)) {
        	String releaseNS = releaseMember.getRepositoryItem().getBaseNamespace();
        	String releaseFilename = releaseMember.getRepositoryItem().getFilename();
        	String releaseVersion = releaseMember.getRepositoryItem().getVersion();
    		
        	result = (releaseNS != null) && (releaseFilename != null) && (releaseVersion != null)
        			&& releaseNS.equals( repoItem.getBaseNamespace() )
        			&& releaseFilename.equals( repoItem.getFilename() )
        			&& releaseVersion.equals( repoItem.getVersion() );
    	}
    	return result;
    }
    
    /**
     * Returns true if the given file references a location in the user's local
     * repository -- either as a locally-managed item or a local copy of a
     * remotely-managed library or release.
     * 
     * @param file  the release or library file to analyze
     * @return boolean
     */
    private boolean isRepositoryFile(File file) {
        boolean result = false;
        
        if (file != null) {
            File repositoryLocation = repositoryManager.getRepositoryLocation();
            File libraryFolder = file.getParentFile();
            
            while (!result && (libraryFolder != null)) {
                result = libraryFolder.equals(repositoryLocation);
                libraryFolder = libraryFolder.getParentFile();
            }
        }
        return result;
    }
    
    /**
     * Clones the content of the current release and returns the deep copy.  The only
     * difference between the clone and the original is that the clone's status will always
     * be DRAFT, and the URL location will not be initialized.
     * 
     * @return Release
     */
    private Release cloneRelease() {
    	Release clone = (release == null) ? null : new Release();
    	
    	if (release != null) {
    		clone.setBaseNamespace( release.getBaseNamespace() );
    		clone.setName( release.getName() );
    		clone.setVersion( release.getVersion() );
    		clone.setStatus( ReleaseStatus.DRAFT );
    		clone.setDescription( release.getDescription() );
    		clone.setDefaultEffectiveDate( release.getDefaultEffectiveDate() );
    		
    		for (ReleaseMember member : release.getPrincipalMembers()) {
    			clone.getPrincipalMembers().add( cloneMember( member ) );
    		}
    		
    		for (ReleaseMember member : release.getReferencedMembers()) {
    			clone.getReferencedMembers().add( cloneMember( member ) );
    		}
    		
    		if (release.getCompileOptions() != null) {
    			clone.setCompileOptions( new ReleaseCompileOptions(
    					release.getCompileOptions().toProperties() ) );
    		}
    		if (release.getPreferredFacets() != null) {
    			clone.setPreferredFacets( new HashMap<>( release.getPreferredFacets() ) );
    		}
    	}
    	return clone;
    }
    
    /**
     * Returns a cloned copy of the given release member.
     * 
     * @param member  the release member to be cloned
     * @return ReleaseMember
     */
    private ReleaseMember cloneMember(ReleaseMember member) {
    	ReleaseMember clone = new ReleaseMember();
    	
    	clone.setRepositoryItem( member.getRepositoryItem() );
    	clone.setEffectiveDate( member.getEffectiveDate() );
    	return clone;
    }
    
    /**
     * Initializes the default version scheme.
     */
    static {
    	try {
    		VersionSchemeFactory factory = VersionSchemeFactory.getInstance();
    		versionScheme = factory.getVersionScheme( factory.getDefaultVersionScheme() );
    		
    	} catch (Throwable t) {
    		throw new ExceptionInInitializerError( t );
    	}
    }
    
}
