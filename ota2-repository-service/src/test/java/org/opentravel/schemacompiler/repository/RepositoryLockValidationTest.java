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
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLSimple;

/**
 * Test that verifies repository items cannot be locked if local copies are out of
 * sync with the remote repository. 
 */
public class RepositoryLockValidationTest extends RepositoryMultiUserTestBase {
	
	private static final String TEST_DESCRIPTION1 = "Lock Validation: Description 1";
	private static final String TEST_DESCRIPTION2 = "Lock Validation: Description 2";
	
	@BeforeClass
	public static void setupRemoteRepository() throws Exception {
        startTestServer( "versions-repository", 9300, RepositoryLockValidationTest.class );
	}
	
	@Test
	public void runTest() throws Exception {
		this.executeUserTasks( new User1Tasks(), new User2Tasks() );
	}
	
	public void user1_task1_loadModel() throws Exception {
		if (DEBUG) System.out.println( "User 1: Loading model" );
		loadProject( "/projects/version_test_1.xml" );
	}
	
	public void user2_task1_loadModel() throws Exception {
		if (DEBUG) System.out.println( "User 2: Loading model" );
		loadProject( "/projects/version_test_1.xml" );
	}
	
	public void user1_task2_lockLibraryAndUpdate() throws Exception {
		if (DEBUG) System.out.println( "User 1: Updating library and committing changes" );
		
		// Obtain a lock for one of the libraries in the model
		ProjectItem item = findProjectItem( "Version_Test_1_0_0.otm" );
		TLLibrary library = (TLLibrary) item.getContent();
		
		projectManager.get().lock( item );
		
		// Update the library, save the project, and commit/unlock
		library.setComments( TEST_DESCRIPTION1 );
        projectManager.get().saveProject( project.get() );
		projectManager.get().unlock( item, true, "User 1: Updating library and committing changes" );
	}
	
	public void user2_task2_lockLibraryAndUpdate() throws Exception {
		if (DEBUG) System.out.println( "User 2: Updating library and committing changes" );
		ProjectItem item = findProjectItem( "Version_Test_1_0_0.otm" );
		
		// Attempt to lock; should fail because the local copy is out of sync with
		// the remote repository
		try {
			projectManager.get().lock( item );
			fail("Expected exception not thrown for model lock attempt.");
			
		} catch (RepositoryOutOfSyncException e) {
			// No action - this is the expected behavior
		}
		
		// Refresh the project items and re-attempt the lock
		projectManager.get().refreshManagedProjectItems();
		projectManager.get().lock( item );
		
		TLLibrary library = (TLLibrary) item.getContent();
		TLSimple simpleType = library.getSimpleType( "SimpleType_01_00" );
		
		// Update the library, save the project, and commit/unlock
		simpleType.getDocumentation().setDescription( TEST_DESCRIPTION2 );
        projectManager.get().saveProject( project.get() );
		projectManager.get().unlock( item, true, "User 2: Updating library and committing changes" );
	}
	
	public void user1_task3_refreshModel() throws Exception {
		if (DEBUG) System.out.println( "User 1: Refreshing model and verifying updates" );
		ProjectItem item = findProjectItem( "Version_Test_1_0_0.otm" );
		List<ProjectItem> refreshedItems = projectManager.get().refreshManagedProjectItems();
		TLLibrary library = (TLLibrary) item.getContent();
		TLSimple simpleType = library.getSimpleType( "SimpleType_01_00" );
		
        assertEquals( 1, refreshedItems.size() );
        assertEquals( TEST_DESCRIPTION1, library.getComments() );
        assertEquals( TEST_DESCRIPTION2, simpleType.getDocumentation().getDescription() );
	}
	
	public void user2_task3_refreshModel() throws Exception {
		if (DEBUG) System.out.println( "User 2: Refreshing model and verifying updates" );
		ProjectItem item = findProjectItem( "Version_Test_1_0_0.otm" );
		List<ProjectItem> refreshedItems = projectManager.get().refreshManagedProjectItems();
		TLLibrary library = (TLLibrary) item.getContent();
		TLSimple simpleType = library.getSimpleType( "SimpleType_01_00" );
		
        assertEquals( 0, refreshedItems.size() );
        assertEquals( TEST_DESCRIPTION1, library.getComments() );
        assertEquals( TEST_DESCRIPTION2, simpleType.getDocumentation().getDescription() );
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
					user1_task2_lockLibraryAndUpdate();
					break;
				case 2:
					user1_task3_refreshModel();
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
					user2_task1_loadModel();
					break;
				case 1:
					user2_task2_lockLibraryAndUpdate();
					break;
				case 2:
					user2_task3_refreshModel();
					break;
				default:
					isDone = true;
			}
			return isDone;
		}
		
	}
	
}
