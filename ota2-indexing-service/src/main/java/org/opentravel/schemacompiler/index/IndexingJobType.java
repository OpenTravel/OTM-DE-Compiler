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

import org.opentravel.repocommon.index.IndexingConstants;

import java.io.File;

/**
 * Specifies the type of an indexing batch job and assists with the creation and decoding of job filenames.
 */
public enum IndexingJobType {

    CREATE(IndexingConstants.JOB_TYPE_CREATE_INDEX, "-create.xml"),
    DELETE(IndexingConstants.JOB_TYPE_DELETE_INDEX, "-delete.xml"),
    DELETE_ALL(IndexingConstants.JOB_TYPE_DELETE_ALL, "-delete-all.txt"),
    SUBSCRIPTION(IndexingConstants.JOB_TYPE_SUBSCRIPTION, "-subscr.xml");

    private String messageType;
    private String fileSuffix;

    /**
     * Constructor that specifies the JMS message type for the indexing job type and the file suffix that will be used
     * for batch job files of that type.
     * 
     * @param messageType the JMS message type for the indexing job
     * @param fileSuffix the file suffix that will be used for jobs of this type
     */
    private IndexingJobType(String messageType, String fileSuffix) {
        this.messageType = messageType;
        this.fileSuffix = fileSuffix;
    }

    /**
     * Returns the <code>IndexingJobType</code> value for the given message type or null if the message type string is
     * not valid.
     * 
     * @param messageType the message type for which to return the indexing job type
     * @return IndexingJobType
     */
    public static IndexingJobType fromMessageType(String messageType) {
        IndexingJobType jobType = null;

        if (messageType != null) {
            for (IndexingJobType jt : values()) {
                if (jt.messageType.equals( messageType )) {
                    jobType = jt;
                    break;
                }
            }
        }
        return jobType;
    }

    /**
     * Returns the <code>IndexingJobType</code> value associated with the given batch job file.
     * 
     * @param jobFile the batch job file for which to return the indexing job type
     * @return IndexingJobType
     */
    public static IndexingJobType fromJobFile(File jobFile) {
        IndexingJobType jobType = null;

        if (jobFile != null) {
            String filename = jobFile.getName().toLowerCase();

            for (IndexingJobType jt : values()) {
                if (filename.endsWith( jt.fileSuffix )) {
                    jobType = jt;
                    break;
                }
            }
        }
        return jobType;
    }

    /**
     * Creates a batch job file using the information provided. The job file itself is not created by this method, only
     * the file handle.
     * 
     * @param baseFilename the base filename of the batch job file
     * @param jobFolder the folder location of the batch job file
     * @param messageType the JMS message type for all items contained within the batch job file
     * @return File
     */
    public static File toJobFile(String baseFilename, File jobFolder, String messageType) {
        IndexingJobType jobType = fromMessageType( messageType );

        if (jobType == null) {
            throw new IllegalArgumentException( "Invalid indexing message type: " + messageType );
        }
        return new File( jobFolder, File.separatorChar + "job" + baseFilename + jobType.fileSuffix );
    }

}
