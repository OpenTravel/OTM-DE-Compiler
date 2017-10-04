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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Proactively pings known remote repositories to verify thier availability.
 */
public class RepositoryAvailabilityChecker {
	
	private static final long DEFAULT_RECHECK_INTERVAL = 300000L;
	
	private static Map<RepositoryManager,RepositoryAvailabilityChecker> instanceCache = new HashMap<>();
	
	private Map<String,Long> successfulPingCache = new HashMap<>();
	private RepositoryManager repositoryManager;
	private long recheckInterval;
	
	/**
	 * Constructor that provides the repository manager that will be used to establish
	 * remote connections.
	 * 
	 * @param repositoryManager  the repository manager instance
	 */
	private RepositoryAvailabilityChecker(RepositoryManager repositoryManager) {
		this( repositoryManager, DEFAULT_RECHECK_INTERVAL );
	}
	
	/**
	 * Returns a new <code>RepositoryAvailabilityChecker</code> instance for the given
	 * repository manager.
	 * 
	 * @param repositoryManager  the repository manager for which to return an availability checker
	 */
	public synchronized static RepositoryAvailabilityChecker getInstance(RepositoryManager repositoryManager) {
		RepositoryAvailabilityChecker availabilityChecker = instanceCache.get( repositoryManager );
		
		if (availabilityChecker == null) {
			availabilityChecker = new RepositoryAvailabilityChecker( repositoryManager );
		}
		return availabilityChecker;
	}
	
	/**
	 * Constructor that provides the repository manager that will be used to establish
	 * remote connections.
	 * 
	 * @param repositoryManager  the repository manager instance
	 * @param recheckInterval  the amount of time between checks if a repository is found to be available
	 */
	public RepositoryAvailabilityChecker(RepositoryManager repositoryManager, long recheckInterval) {
		this.repositoryManager = repositoryManager;
		this.recheckInterval = recheckInterval;
	}
	
	/**
	 * Checks availability of the remote repository for the given item and return false if that
	 * repository is unavailalbe.
	 * 
	 * @param item  the repository item for which to check remote availability
	 * @return boolean
	 */
	public boolean pingRepository(RepositoryItem item) {
		return pingRepository( item.getRepository().getId() );
	}
	
	/**
	 * Checks availability of all repositories upon which the given release has dependencies and
	 * returns false if any of those repositories are unavailalbe.
	 * 
	 * @param release  the release for which to check repository dependencies
	 * @return boolean
	 */
	public boolean pingRepositories(Release release) {
		Set<String> repositoryIds = new HashSet<>();
		
		for (ReleaseMember member : release.getAllMembers()) {
			repositoryIds.add( member.getRepositoryItem().getRepository().getId() );
		}
		return pingAllRepositories( repositoryIds, true );
	}
	
	/**
	 * Checks availability of all repositories upon which the given model has dependencies and
	 * returns false if any of those repositories are unavailalbe.
	 * 
	 * @param model  the model for which to check repository dependencies
	 * @return boolean
	 */
	public boolean pingRepositories(TLModel model) {
		Set<String> repositoryIds = new HashSet<>();
		
		for (TLLibrary library : model.getUserDefinedLibraries()) {
			if (URLUtils.isFileURL( library.getLibraryUrl() )) {
				File libraryFile = URLUtils.toFile( library.getLibraryUrl() );
				try {
					RepositoryItem item = repositoryManager.getRepositoryItem( libraryFile );
					
					if (item != null) {
						repositoryIds.add( item.getRepository().getId() );
					}
					
				} catch (RepositoryException e) {
					// No error - skip this item
				}
				
			} else {
				for (RemoteRepository remoteRepository : repositoryManager.listRemoteRepositories()) {
					if (library.getLibraryUrl().toExternalForm().startsWith( remoteRepository.getEndpointUrl() )) {
						repositoryIds.add( remoteRepository.getId() );
						break;
					}
				}
			}
		}
		return pingAllRepositories( repositoryIds, true );
	}
	
	/**
	 * Performs a ping of all known remote repositories.  If the 'failOnSingleFailure' flag is
	 * true, this method will return false if even a single remote repository is unavailable. 
	 * If false, all repositories must be unavailable for this method to return false.
	 * 
	 * @param failOnSingleFailure  flag indicating whether a single repository unavailable should cause this test to fail
	 * @return boolean
	 */
	public boolean pingAllRepositories(boolean failOnSingleFailure) {
		Set<String> repositoryIds = new HashSet<>();
		
		for (RemoteRepository repository : repositoryManager.listRemoteRepositories()) {
			repositoryIds.add( repository.getId() );
		}
		return pingAllRepositories( repositoryIds, failOnSingleFailure );
	}
	
	/**
	 * Spawns parallel threads to check the availability of all specified repositories.
	 * 
	 * @param repositoryIds  the ID's of the remote repositories to check for availability
	 * @param failOnSingleFailure  flag indicating whether a single repository unavailable should cause this test to fail
	 * @return boolean
	 */
	private boolean pingAllRepositories(Collection<String> repositoryIds, boolean failOnSingleFailure) {
		ExecutorService executor = Executors.newFixedThreadPool( Math.max( repositoryManager.listRemoteRepositories().size(), 1 ) );
		try {
			List<Future<Boolean>> resultList = new ArrayList<>();
			boolean someAvailable = false;
			boolean allAvailable = true;
			
			for (final String repositoryId : repositoryIds) {
				resultList.add(
					executor.submit( new Callable<Boolean>() {
						public Boolean call() throws Exception {
							return pingRepository( repositoryId );
						}
					} )
				);
			}
			
			for (Future<Boolean> result : resultList) {
				boolean repoAvailable;
				try {
					repoAvailable = result.get();
					
				} catch (InterruptedException | ExecutionException e) {
					repoAvailable = false;
				}
				
				someAvailable |= repoAvailable;
				allAvailable &= repoAvailable;
			}
			return repositoryIds.isEmpty() || (failOnSingleFailure ? allAvailable : someAvailable);
			
		} finally {
			executor.shutdown();
		}
	}
	
	/**
	 * Pings the repository with the specified ID.  If a successful ping has already been done
	 * within the recheck interval window, this method will return true without actually pinging
	 * the remote service.
	 * 
	 * <p>If the repository is considered to be available, this method will return true.
	 * 
	 * @param repositoryId  the ID of the repository to ping
	 * @return boolean
	 */
	public boolean pingRepository(String repositoryId) {
		synchronized (repositoryId.intern()) {
			Long lastPing = successfulPingCache.get( repositoryId );
			boolean isAvailable;
			
			if ((lastPing == null) || ((System.currentTimeMillis() - lastPing) > recheckInterval)) {
				Repository repository = repositoryManager.getRepository( repositoryId );
				
				if (repository != null) {
					if (repository instanceof RemoteRepository) {
						try {
							String endpointUrl = ((RemoteRepository) repository).getEndpointUrl();
							
							RemoteRepositoryClient.getRepositoryMetadata( endpointUrl );
							successfulPingCache.put( repositoryId, System.currentTimeMillis() );
							isAvailable = true;
							
						} catch (RepositoryException e) {
							successfulPingCache.remove( repositoryId );
							isAvailable = false;
						}
						
					} else { // Local repository is always available
						isAvailable = true;
					}
					
				} else { // Invalid repository ID, so it is not available
					isAvailable = false;
				}
				
			} else { // Last successful ping was inside the recheck interval (assume available)
				isAvailable = true;
			}
			return isAvailable;
		}
	}
	
}
