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

package org.opentravel.schemacompiler.diff;

import java.util.Locale;

import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.springframework.context.NoSuchMessageException;

/**
 * Abstract base class for all OTM-Diff change items.
 *
 * @param <T>  the change type enumeration for this change item
 */
public abstract class ChangeItem<T extends Enum<T>> {
	
	T changeType;
	String oldValue;
	String newValue;
	
	/**
	 * Returns a user-displayable string that describes the finding for this
	 * change item.
	 * 
	 * @return String
	 */
	public String getDisplayMessage() {
		String displayMessage;
		
		if (changeType != null) {
	        String messageKey = changeType.getClass().getSimpleName() + "." + changeType.toString();

	        try {
	        	displayMessage = SchemaCompilerApplicationContext.getContext().getMessage(
	            		messageKey, new Object[] { oldValue, newValue }, Locale.getDefault());

	        } catch (NoSuchMessageException e) {
	        	displayMessage = messageKey; // No error - just use the raw message key
	        }
	        
		} else {
			displayMessage = null;
		}
		return displayMessage;
	}
	
	/**
	 * Returns the type of project change.
	 *
	 * @return T
	 */
	public T getChangeType() {
		return changeType;
	}

	/**
	 * Returns the affected value from the old version.
	 *
	 * @return String
	 */
	public String getOldValue() {
		return oldValue;
	}

	/**
	 * Returns the affected value from the new version.
	 *
	 * @return String
	 */
	public String getNewValue() {
		return newValue;
	}

}
