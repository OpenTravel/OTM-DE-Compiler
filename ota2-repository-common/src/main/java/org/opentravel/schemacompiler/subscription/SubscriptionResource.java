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

package org.opentravel.schemacompiler.subscription;

import java.io.File;
import java.io.IOException;

import org.opentravel.ns.ota2.repositoryinfoext_v01_00.Subscription;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionEventType;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionList;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;
import org.opentravel.schemacompiler.config.FileResource;


/**
 * File-based resource that provides access to a single subscription list.
 *
 * @author S. Livezey
 */
public class SubscriptionResource extends FileResource<SubscriptionList> {
	
    private SubscriptionFileUtils fileUtils;
    private String baseNS;
    private String libraryName;
    private String version;
    
	/**
	 * Constructor that specifies the characteristics of the subscription target.
	 * 
	 * @param fileUtils  the subscription file utilities instance
     * @param baseNS  the base namespace of the subscription target
     * @param libraryName  the library name of the subscription target
     * @param version  the library version of the subscription target
     * @throws IOException  thrown if the subscription list file cannot be identified
	 */
	public SubscriptionResource(SubscriptionFileUtils fileUtils, String baseNS, String libraryName,
			String version) throws IOException {
		super( fileUtils.getSubscriptionListFile( baseNS, libraryName, version ) );
		this.fileUtils = fileUtils;
		this.baseNS = baseNS;
		this.libraryName = libraryName;
		this.version = version;
		invalidateResource();
		initResource();
	}

	/**
	 * Returns the base namespace of the subscription target.
	 *
	 * @return String
	 */
	public String getBaseNS() {
		return baseNS;
	}

	/**
	 * Returns the library name of the subscription target.
	 *
	 * @return String
	 */
	public String getLibraryName() {
		return libraryName;
	}

	/**
	 * Returns the version of the subscription target.
	 *
	 * @return String
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @see org.opentravel.schemacompiler.config.FileResource#getDefaultResourceValue()
	 */
	@Override
	protected SubscriptionList getDefaultResourceValue() {
		SubscriptionList subscriptionList = null;
		
		if (baseNS != null) {
	    	SubscriptionTarget subscriptionTarget = new SubscriptionTarget();
	    	subscriptionList = new SubscriptionList();
	    	
	    	for (SubscriptionEventType eventType : SubscriptionEventType.values()) {
	    		Subscription subscription = new Subscription();
	    		
	    		subscription.setEventType( eventType );
	    		subscriptionList.getSubscription().add( subscription );
	    	}
	    	subscriptionTarget.setBaseNamespace( baseNS );
	    	subscriptionTarget.setLibraryName( libraryName );
	    	subscriptionTarget.setVersion( version );
	    	subscriptionList.setSubscriptionTarget( subscriptionTarget );
		}
    	return subscriptionList;
	}

	/**
	 * @see org.opentravel.schemacompiler.config.FileResource#loadResource(java.io.File)
	 */
	@Override
	protected SubscriptionList loadResource(File dataFile) throws IOException {
		SubscriptionList resource = null;
		
        if ((fileUtils != null) && dataFile.isFile()) {
            resource = fileUtils.loadFile( dataFile );
        }
        return resource;
	}
	
}
