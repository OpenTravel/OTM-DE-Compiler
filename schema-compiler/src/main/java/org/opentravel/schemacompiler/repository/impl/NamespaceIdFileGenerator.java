/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.repository.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opentravel.ns.ota2.repositoryinfo_v01_00.LibraryInfoType;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Performs the auto-creation of 'nsid.txt' files in the repository folders during the initialization process
 * of a <code>RepositoryManager</code>.
 * 
 * NOTE: This class is intended as a temporary startup utility since some existing repositories may not yet have
 * created the required 'nsid.txt' files.  Once all existing repositories (local and remote) have been migrated,
 * this class can be safely removed.
 * 
 * @author S. Livezey
 */
public class NamespaceIdFileGenerator {
	
	private RepositoryManager repositoryManager;
	
	/**
	 * Constructor for an instance that will process the folders of the given repository.
	 * 
	 * @param repositoryManager  the repository manager that owns the folder structure to process
	 */
	public NamespaceIdFileGenerator(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}
	
	/**
	 * Automatically creates any required 'nsid.txt' files that do not yet exist.
	 * 
	 * @throws RepositoryException  thrown if one or more 'nsid.txt' files cannot be created
	 */
	public void execute() throws RepositoryException {
		RepositoryFileManager fileManager = repositoryManager.getFileManager();
		List<String> baseNamespaces = new ArrayList<String>();
		boolean success = false;
		
		for (String rootNS : repositoryManager.listRootNamespaces()) {
			findBaseNamespaces( fileManager.getNamespaceFolder(rootNS, null), baseNamespaces );
		}
		
		try {
			fileManager.startChangeSet();
			
			for (String baseNS : baseNamespaces) {
				fileManager.createNamespaceIdFiles( baseNS );
			}
			fileManager.commitChangeSet();
			success = true;
			
		} finally {
			if (!success) {
				fileManager.rollbackChangeSet();
			}
		}
	}
	
	/**
	 * Constructs a list of all base namespaces that are declared in the meta-data files of <code>RepositoryItem</code>
	 * records that are stored in the repository.
	 * 
	 * @param nsFolder  the namespace folder to search for base namespace declarations
	 * @param results  the resulting list of base namespaces being compiled
	 */
	private void findBaseNamespaces(File nsFolder, List<String> results) throws RepositoryException {
		if ((nsFolder == null) || !nsFolder.exists()) return;
		
		for (File folderMember : nsFolder.listFiles()) {
			if (folderMember.getName().startsWith(".")) {
				continue; // skip hidden files and folders
			}
			if (folderMember.isDirectory()) {
				findBaseNamespaces( folderMember, results );
				
			} else if (folderMember.getName().endsWith("-info.xml")){
				try {
					LibraryInfoType libraryMetadata = (LibraryInfoType) repositoryManager.getFileManager().loadLibraryMetadata(folderMember);
					String baseNamespace = libraryMetadata.getBaseNamespace();
					
					if (!results.contains( baseNamespace )) {
						results.add( baseNamespace );
					}
					
				} catch (RepositoryException e) {
					// No error - skip and move on to the next file or folder
				}
			}
		}
	}
	
}
