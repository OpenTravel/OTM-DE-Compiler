
package org.opentravel.schemacompiler.repository;

import org.opentravel.ns.ota2.repositoryinfo_v01_00.RefreshPolicy;

/**
 * Interface that defines the interaction model between a local OTA2.0 project and a single
 * remote repository.
 *
 * @author S. Livezey
 */
public interface RemoteRepository extends Repository {
	
	/**
	 * Contacts the remote web service to update all of the server-provided meta-data.
	 * 
	 * @throws RepositoryException  thrown if the remote repository cannot be accessed
	 */
	public void refreshRepositoryMetadata() throws RepositoryException;
	
	/**
	 * Returns the web service endpoint URL of the remote repository.
	 *
	 * @return String
	 */
	public String getEndpointUrl();
	
	/**
	 * Returns the policy value that indicates how often items that are cached in the local repository
	 * should be checked for updates.
	 * 
	 * @return RefreshPolicy
	 */
	public RefreshPolicy getRefreshPolicy();
	
	/**
	 * Downloads the specified content (and its associated meta-data) from the remote repository into the
	 * local instance.  If the refresh policy for this repository does not require an update (or the remote
	 * repository is not accessible), the locally cached copy of the content will be used.
	 * 
	 * @param item  the reposited item whose content is to be downloaded into the local cache
	 * @param forceUpdate  disregards the repository's update policy and forces the remote content to be downloaded
	 * @throws RepositoryException  thrown if the remote repository cannot be accessed
	 */
	public void downloadContent(RepositoryItem item, boolean forceUpdate) throws RepositoryException;
	
}
