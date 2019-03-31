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

package org.opentravel.schemacompiler.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLLibrary;

import java.util.List;

/**
 * Test that verifies that a refresh operation from a remote repository will properly synchronize a user's local
 * repository and workspace.
 */
public class RepositoryRefreshTest extends RepositoryMultiUserTestBase {

    private static final String TEST_COMMENTS = "Comments updated for repository refresh test.";

    @BeforeClass
    public static void setupRemoteRepository() throws Exception {
        startTestServer( "versions-repository", 9300, RepositoryRefreshTest.class );
    }

    @Test
    public void runTest() throws Exception {
        this.executeUserTasks( new User1Tasks(), new User2Tasks() );
    }

    private void user1_task1_loadModel() throws Exception {
        if (DEBUG)
            System.out.println( "User 1: Loading model" );
        loadProject( "/projects/version_test_1.xml" );
    }

    private void user2_task1_updateLibrary() throws Exception {
        if (DEBUG)
            System.out.println( "User 2: Updating model and committing changes" );
        loadProject( "/projects/version_test_2.xml" );

        // Obtain a lock for one of the libraries in the model
        ProjectItem item = findProjectItem( "Version_Test_1_0_0.otm" );
        TLLibrary library = (TLLibrary) item.getContent();

        projectManager.get().lock( item );

        // Update the library, save the project, and commit/unlock
        library.setComments( TEST_COMMENTS );
        projectManager.get().saveProject( project.get() );
        projectManager.get().unlock( item, true, "User 2: Updating model and committing changes" );
    }

    private void user1_task2_refreshModel() throws Exception {
        if (DEBUG)
            System.out.println( "User 1: Refreshing model" );
        ProjectItem item = findProjectItem( "Version_Test_1_0_0.otm" );
        TLLibrary originalLibrary = (TLLibrary) item.getContent();

        assertNotEquals( TEST_COMMENTS, originalLibrary.getComments() );

        List<ProjectItem> refreshedItems = projectManager.get().refreshManagedProjectItems();
        TLLibrary updatedLibrary = (TLLibrary) item.getContent();

        assertEquals( 1, refreshedItems.size() );
        assertEquals( item, refreshedItems.get( 0 ) );
        assertEquals( TEST_COMMENTS, updatedLibrary.getComments() );
        assertNotEquals( originalLibrary, updatedLibrary );
        assertEquals( model.get(), updatedLibrary.getOwningModel() );
        assertEquals( null, originalLibrary.getOwningModel() );
    }

    private class User1Tasks extends RepositoryUserTasks {

        public User1Tasks() {
            super( "testuser", "password", false );
        }

        @Override
        public boolean executeTask(int taskNumber) throws Exception {
            boolean isDone = false;

            switch (taskNumber) {
                case 0:
                    user1_task1_loadModel();
                    break;
                case 1:
                    user1_task2_refreshModel();
                    break;
                default:
                    isDone = true;
            }
            return isDone;
        }

    }

    private class User2Tasks extends RepositoryUserTasks {

        public User2Tasks() {
            super( "testuser2", "password", true );
        }

        @Override
        public boolean executeTask(int taskNumber) throws Exception {
            boolean isDone = false;

            switch (taskNumber) {
                case 0:
                    user2_task1_updateLibrary();
                    break;
                default:
                    isDone = true;
            }
            return isDone;
        }

    }

}
