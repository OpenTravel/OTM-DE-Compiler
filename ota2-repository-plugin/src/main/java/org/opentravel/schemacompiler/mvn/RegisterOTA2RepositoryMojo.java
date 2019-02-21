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

package org.opentravel.schemacompiler.mvn;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.opentravel.schemacompiler.repository.RemoteRepository;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * Registers or updates the registration for an OTA2.0 repository during an
 * automated build process.  If the system property 'otm.repository.forceUpdate'
 * is set to true, the existing registration will be deleted and recreated
 * using the information passed to this plugin.
 */
@Mojo( name = "register-repository", defaultPhase = LifecyclePhase.VALIDATE, threadSafe=true  )
@Execute( goal="register-repository", phase = LifecyclePhase.VALIDATE )
public class RegisterOTA2RepositoryMojo extends AbstractMojo {
	
	public static final String FORCE_UPDATE_SYSPROP = "otm.repository.forceUpdate";
	
	@Parameter( required = true )
	private String repositoryId;
	
	@Parameter( required = true )
	private String repositoryUrl;
	
	@Parameter
	private String userId;
	
	@Parameter
	private String userPassword;

    private RepositoryManager repositoryManager;

	/**
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			Repository repository = repositoryManager.getRepository( repositoryId );
			Log log = getLog();
			
			if ((repository instanceof RemoteRepository) && forceUpdate()) {
				repositoryManager.removeRemoteRepository( (RemoteRepository) repository );
				repository = null;
			}
			
			if (repository == null) {
				RemoteRepository remoteRepo = repositoryManager.addRemoteRepository( repositoryUrl );
				
				if (!remoteRepo.getId().equals( repositoryId )) {
					throw new MojoExecutionException( String.format(
							"The remote repository was added but its ID does not match the required value "
							+ "(expected '%s' but found '%s' instead).", repositoryId, remoteRepo.getId() ) );
				}
				log.info( String.format( "The remote repository '%s' was added successfully.", repositoryId ) );
				
				if (!StringUtils.isBlank( userId ) && !StringUtils.isBlank( userPassword )) {
					repositoryManager.setCredentials( remoteRepo, userId, userPassword );
					log.info( String.format( "Credentials assigned for user '%s'.", userId ) );
					
				} else {
					log.info( "Repository configured for anonymous access." );
				}
				
			} else {
				log.info( String.format( "OTM repository '%s' already registered.", repositoryId ) );
			}
			
		} catch (RepositoryException e) {
			throw new MojoExecutionException(
					"Error registering OTA2.0 repository: " + repositoryId, e);
		}
	}
	
	/**
	 * Returns true if the environment variable has been set to force the update of
	 * the OTM repository registration.  This may be required when updating user
	 * credentials or the base URL of the repository has changed.
	 * 
	 * @return boolean
	 */
	private boolean forceUpdate() {
		String syspropValue = System.getProperty( FORCE_UPDATE_SYSPROP, Boolean.FALSE.toString() );
		
		return Boolean.valueOf( syspropValue );
	}
	
    /**
     * Initializes the repository manager to be used by this mojo.  If null, the default
     * manager instance will be used.
     * 
     * @param repositoryManager  the repository manager instance (null to use default)
     * @throws RepositoryException  thrown if the default instance cannot be initialized
     */
    protected void initRepositoryManager(RepositoryManager repositoryManager) throws RepositoryException {
        if (this.repositoryManager == null) {
            if (repositoryManager == null) {
                this.repositoryManager = RepositoryManager.getDefault();
                
            } else {
                this.repositoryManager = repositoryManager;
            }
        }
    }

}
