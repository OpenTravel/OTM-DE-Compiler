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

package org.opentravel.schemacompiler.index;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionEventType;
import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionTarget;


/**
 * Search result object that encapsulates all relevant information about a user's
 * subscription in an OTM repository.
 */
public class SubscriptionSearchResult implements IndexingTerms, Comparable<SubscriptionSearchResult> {
	
	private SubscriptionTarget subscriptionTarget;
	private List<SubscriptionEventType> eventTypes = new ArrayList<>();
	private String userId;
	
	/**
	 * Constructor that initializes this search result instance using the information
	 * provided.
	 * 
	 * @param subscriptionTarget  the subscription target
	 * @param userId  the user ID to which the subscription applies
	 */
	public SubscriptionSearchResult(SubscriptionTarget subscriptionTarget, String userId) {
		this.subscriptionTarget = subscriptionTarget;
		this.userId = userId;
	}
	
	/**
	 * Returns the subscription target.
	 *
	 * @return SubscriptionTarget
	 */
	public SubscriptionTarget getSubscriptionTarget() {
		return subscriptionTarget;
	}

	/**
	 * Returns the user ID to which the subscription applies.
	 *
	 * @return String
	 */
	public String getUserId() {
		return userId;
	}
	
	/**
	 * Returns the list of events to which the user is subscribed within the
	 * scope of the subscription target.
	 *
	 * @return List<SubscriptionEventType>
	 */
	public List<SubscriptionEventType> getEventTypes() {
		return eventTypes;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (userId == null) ? 0 : userId.hashCode();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof SubscriptionSearchResult)
				&& (compareTo( (SubscriptionSearchResult) obj) == 0);
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SubscriptionSearchResult other) {
		SubscriptionTarget thisTarget = subscriptionTarget;
		SubscriptionTarget otherTarget = other.subscriptionTarget;
		int result;
		
		if (thisTarget == null) {
			result = (otherTarget == null) ? 0 : -1;
			
		} else if (otherTarget == null) {
			result = 1;
			
		} else {
			result = compareStrings( thisTarget.getBaseNamespace(), otherTarget.getBaseNamespace() );
			
			if (result == 0) {
				result = compareStrings( thisTarget.getLibraryName(), otherTarget.getLibraryName() );
				
				if (result == 0) {
					result = compareStrings( thisTarget.getVersion(), otherTarget.getVersion() );
				}
			}
		}
		return result;
	}
	
	/**
	 * Performs a comparison of the two strings, accounting for null values that
	 * might exist.
	 * 
	 * @param str1  the first string to compare
	 * @param str2  the second string to compare
	 * @return int
	 */
	private int compareStrings(String str1, String str2) {
		int result;
		
		if (str1 == null) {
			result = (str2 == null) ? 0 : -1;
			
		} else if (str2 == null) {
			result = 1;
			
		} else {
			result = str1.compareTo( str2 );
		}
		return result;
	}
	
}
