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
package org.opentravel.schemacompiler.notification;

/**
 * Constant definitions used by the notification service.
 */
public class NotificationConstants {
	
	public static final String MSGPROP_ACTION           = "action";
	public static final String PUBLISH_ACTION_ID        = "publish";
	public static final String MODIFIED_ACTION_ID       = "modified";
	public static final String LOCKED_ACTION_ID         = "locked";
	public static final String UNLOCKED_ACTION_ID       = "unlocked";
	public static final String STATUS_CHANGED_ACTION_ID = "status-changed";
	public static final String DELETED_ACTION_ID        = "deleted";

	/**
	 * Private constructor to prevent instantiation.
	 */
	private NotificationConstants() {}
	
}
