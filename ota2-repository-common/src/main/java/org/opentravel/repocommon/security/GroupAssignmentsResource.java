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

package org.opentravel.repocommon.security;

import org.opentravel.ns.ota2.security_v01_00.Group;
import org.opentravel.ns.ota2.security_v01_00.GroupAssignments;
import org.opentravel.repocommon.config.FileResource;
import org.opentravel.repocommon.security.impl.SecurityFileUtils;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryFileManager;
import org.opentravel.schemacompiler.repository.RepositoryManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * File resource that returns the mappings of known user ID's to each user's group assignments.
 * 
 * @author S. Livezey
 */
public class GroupAssignmentsResource extends FileResource<Map<String,List<String>>> {

    public static final String GROUP_ASSIGNMENTS_FILE = "group-assignments.xml";

    private RepositoryFileManager fileManager;
    private SecurityFileUtils fileUtils;

    /**
     * Constructor that assigns the web service's repository location.
     * 
     * @param repositoryManager the repository manager for all file-system resources
     */
    public GroupAssignmentsResource(RepositoryManager repositoryManager) {
        super( new File( repositoryManager.getRepositoryLocation(), GROUP_ASSIGNMENTS_FILE ) );
        this.fileManager = repositoryManager.getFileManager();
        this.fileUtils = new SecurityFileUtils( repositoryManager );
        invalidateResource();
    }

    /**
     * Returns the list of assigned groups for the specified user. If no groups are assigned, this method will return an
     * empty array.
     * 
     * @param userId the ID of the user for which to return group assignments
     * @return String[]
     */
    public String[] getAssignedGroups(String userId) {
        Map<String,List<String>> groupAssignments = getResource();
        List<String> assignedGroups = new ArrayList<>();

        for (Entry<String,List<String>> entry : groupAssignments.entrySet()) {
            List<String> memberIds = new ArrayList<>( entry.getValue() );
            String groupName = entry.getKey();
            boolean memberOfGroup = false;

            for (String memberId : memberIds) {
                if (memberId.equalsIgnoreCase( userId )) {
                    memberOfGroup = true;
                    break;
                }
            }
            if (memberOfGroup) {
                assignedGroups.add( groupName );
            }
        }
        return assignedGroups.toArray( new String[assignedGroups.size()] );
    }

    /**
     * Returns the list of all group names defined in the group assignments file.
     * 
     * @return String[]
     */
    public String[] getGroupNames() {
        Map<String,List<String>> groupAssignments = getResource();
        List<String> allGroups = new ArrayList<>( groupAssignments.keySet() );

        Collections.sort( allGroups );
        return allGroups.toArray( new String[allGroups.size()] );
    }

    /**
     * Returns the list of all users assigned to the specified group.
     * 
     * @param groupName the name of the group for which to return a list of members
     * @return String[]
     */
    public String[] getAssignedUsers(String groupName) {
        Map<String,List<String>> groupAssignments = getResource();
        List<String> groupMembers = new ArrayList<>();

        for (Entry<String,List<String>> entry : groupAssignments.entrySet()) {
            String gName = entry.getKey();

            if (groupName.equals( gName )) {
                groupMembers = entry.getValue();
                break;
            }
        }
        Collections.sort( groupMembers );
        return groupMembers.toArray( new String[groupMembers.size()] );
    }

    /**
     * @see org.opentravel.repocommon.config.FileResource#getDefaultResourceValue()
     */
    @Override
    protected Map<String,List<String>> getDefaultResourceValue() {
        return new HashMap<>();
    }

    /**
     * @see org.opentravel.repocommon.config.FileResource#loadResource(java.io.File)
     */
    @Override
    protected Map<String,List<String>> loadResource(File dataFile) throws IOException {
        if (fileUtils == null) {
            return getDefaultResourceValue(); // Special case for constructor initialization
        }
        GroupAssignments groupAssignments = fileUtils.loadGroupAssignments( dataFile );
        Map<String,List<String>> assignmentMap = new HashMap<>();

        for (Group group : groupAssignments.getGroup()) {
            assignmentMap.put( group.getName(), new ArrayList<String>( group.getMember() ) );
        }
        return assignmentMap;
    }

    /**
     * Saves the list of all group assignments to the locally maintained group assignments file.
     * 
     * @param allGroups the list of groups to be saved
     * @throws RepositoryException thrown if the group assignments file cannot be saved
     */
    public synchronized void saveGroupAssignments(List<UserGroup> allGroups) throws RepositoryException {
        fileManager.startChangeSet();
        boolean success = false;
        try {
            GroupAssignments groupAssignments = new GroupAssignments();
            File dataFile = getDataFile();

            for (UserGroup group : allGroups) {
                Group jaxbGroup = new Group();

                jaxbGroup.setName( group.getGroupName() );
                jaxbGroup.getMember().addAll( group.getMemberIds() );
                groupAssignments.getGroup().add( jaxbGroup );
            }
            fileManager.addToChangeSet( dataFile );
            fileUtils.saveGroupAssignments( dataFile, groupAssignments );
            fileManager.commitChangeSet();
            invalidateResource();
            refreshResource( dataFile );
            success = true;

        } catch (IOException e) {
            throw new RepositoryException( "Error saving the group assignments file.", e );

        } finally {
            try {
                if (!success) {
                    fileManager.rollbackChangeSet();
                }
            } catch (Exception e) {
                // Ignore possible errrors and continue
            }
        }
    }

}
