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

package org.opentravel.repocommon.subscription;

import org.opentravel.ns.ota2.repositoryinfoext_v01_00.SubscriptionList;

/**
 * Specifies a callback for the <code>SubscriptionNavigator</code> to invoke each time a <code>SubscriptionList</code>
 * is discovered.
 */
public interface SubscriptionVisitor {

    /**
     * Called by the <code>SubscriptionNavigator</code> each time a new <code>SubscriptionList</code> is discovered.
     * 
     * @param subscriptionList the subscription list to process
     */
    public void visitSubscriptionList(SubscriptionList subscriptionList);

}
