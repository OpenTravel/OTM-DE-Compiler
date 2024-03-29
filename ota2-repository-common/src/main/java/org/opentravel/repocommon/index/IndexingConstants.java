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

package org.opentravel.repocommon.index;

/**
 * Constant definitions used for various aspects of the indexing service.
 */
public class IndexingConstants {

    public static final String MSGPROP_JOB_TYPE = "jobType";
    public static final String JOB_TYPE_CREATE_INDEX = "create-index";
    public static final String JOB_TYPE_DELETE_INDEX = "delete-index";
    public static final String JOB_TYPE_SUBSCRIPTION = "subscription-index";
    public static final String JOB_TYPE_DELETE_ALL = "delete-all";

    public static final String MSGPROP_RESPONSE_TYPE = "responseType";
    public static final String RESPONSE_TYPE_COMMIT = "index-commit";

    /**
     * Private constructor to prevent instantiation.
     */
    private IndexingConstants() {}

}
