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
package org.opentravel.schemacompiler.event;

/**
 * Listener that can be registered with individual <code>TLModelElement</code>
 * entities.  Listeners will only be notified of events that occur on the elements
 * with which they are registered.
 * <p>
 * NOTE: This is fundamentally different from the <code>ModelEventListener</code>
 * which is registered once with a <code>TLModel</code> and is notified of events
 * that occur anywhere within the model.
 * 
 * @author S. Livezey
 */
public interface ModelElementListener {
	
	/**
	 * Called when a child item is added to or removed from an owning entity in the
	 * model.
	 * 
	 * @param event  the ownership event that occurred
	 */
	public void processOwnershipEvent(OwnershipEvent<?,?> event);
	
	/**
	 * Called when a field value changes for the model entity.
	 * 
	 * @param event  the value change event that occurred
	 */
	public void processValueChangeEvent(ValueChangeEvent<?,?> event);
	
}
