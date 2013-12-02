/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.repository.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.version.VersionScheme;
import com.sabre.schemacompiler.version.VersionSchemeException;
import com.sabre.schemacompiler.version.VersionSchemeFactory;
import com.sabre.schemacompiler.visitor.DependencyNavigator;

/**
 * Dependency navigator used to identify dependent project items within a project manager's model.
 * 
 * @author S. Livezey
 */
public class ProjectItemDependencyNavigator extends DependencyNavigator {
	
	protected Set<String> visitedLibraries = new HashSet<String>();
	protected Set<NamedEntity> visitedEntities = new HashSet<NamedEntity>();
	
	/**
	 * @see com.sabre.schemacompiler.visitor.DependencyNavigator#navigateDependency(com.sabre.schemacompiler.model.NamedEntity)
	 */
	@Override
	public void navigateDependency(NamedEntity target) {
		AbstractLibrary library = (target == null) ? null : target.getOwningLibrary();
		String libraryUrl = (library == null) ? null : library.getLibraryUrl().toExternalForm();
		
		if ((libraryUrl != null) && !visitedLibraries.contains( libraryUrl )) {
			List<AbstractLibrary> previousLibraryVersions = getPreviousLibraryVersions( library );
			
			visitedLibraries.add( libraryUrl );
			navigateLibraryMembers( library, target );
			
			// Also navigate all prior versions of this library since it is required that they
			// are part of the model
			for (AbstractLibrary previousLibraryVersion : previousLibraryVersions) {
				if (!visitedLibraries.contains(previousLibraryVersion)) {
					if (previousLibraryVersion instanceof TLLibrary) {
						visitor.visitUserDefinedLibrary( (TLLibrary) previousLibraryVersion );
					}
					visitedLibraries.add( previousLibraryVersion.getLibraryUrl().toExternalForm() );
					navigateLibraryMembers( previousLibraryVersion, target );
				}
			}
		}
		if ((target != null) && !visitedEntities.contains( target )) {
			super.navigateDependency( target );
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.visitor.DependencyNavigator#navigateLibrary(com.sabre.schemacompiler.model.AbstractLibrary)
	 */
	@Override
	public void navigateLibrary(AbstractLibrary library) {
		List<AbstractLibrary> previousLibraryVersions = getPreviousLibraryVersions( library );
		
		// We need to make sure we capture each prior version of the library that
		// exists in the model, but we do not need to recursively navigate them.  That
		// is already being handled by the navigateDependency() method above
		for (AbstractLibrary previousLibraryVersion : previousLibraryVersions) {
			if ((!visitedLibraries.contains(previousLibraryVersion)) && (previousLibraryVersion instanceof TLLibrary)) {
				visitor.visitUserDefinedLibrary( (TLLibrary) previousLibraryVersion );
			}
		}
		super.navigateLibrary( library );
	}

	/**
	 * Navigates all members of the given library.
	 * 
	 * @param library  the library whose members are to be navigated
	 * @param originalTarget  the original named entity that initiated this navigation
	 */
	private void navigateLibraryMembers(AbstractLibrary library, NamedEntity originalTarget) {
		for (NamedEntity libraryMember : library.getNamedMembers()) {
			if ((libraryMember != originalTarget) && !visitedEntities.contains( libraryMember )) {
				visitedEntities.add( libraryMember );
				super.navigateDependency( libraryMember );
			}
		}
	}
	
	/**
	 * Searches the model for all previous versions of the given library.  If no prior version exists
	 * in the model, this method will return an empty list.
	 * 
	 * @param library  the library whose prior versions are to be returned
	 * @return List<AbstractLibrary>
	 */
	private List<AbstractLibrary> getPreviousLibraryVersions(AbstractLibrary library) {
		List<AbstractLibrary> previousVersions = new ArrayList<AbstractLibrary>();
		
		if ((library instanceof TLLibrary) && (library.getOwningModel() != null)) {
			try {
				TLLibrary currentVersion = (TLLibrary) library;
				VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( currentVersion.getVersionScheme() );
				List<String> versionChain = vScheme.getMajorVersionChain( currentVersion.getNamespace() );
				
				for (int i = 1; i < versionChain.size(); i++) { // skip the first element since it represents the current library's version
					String previousVersionId = vScheme.getVersionIdentifier( versionChain.get(i) );
					
					for (TLLibrary lib : currentVersion.getOwningModel().getUserDefinedLibraries()) {
						if (lib == currentVersion) {
							continue;
						}
						if (lib.getName().equals(currentVersion.getName())
								&& lib.getVersion().equals(previousVersionId)
								&& lib.getBaseNamespace().equals(currentVersion.getBaseNamespace())) {
							previousVersions.add( lib );
							break; // stop searching after the first match
						}
					}
				}
			} catch (VersionSchemeException e) {
				// Ignore error and return null
			}
		}
		return previousVersions;
	}
	
}
