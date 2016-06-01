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

import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.util.RepositoryTestUtils;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Abstract base class for tests that verify concurrency conditions that can exist when
 * multiple users access a repository at the same time.  This class is designed such that
 * each sub-class should only implement a single test case.
 */
public class RepositoryMultiUserTestBase {
	
	protected static final boolean DEBUG = true;
	
	private static final long WAIT_TIMEOUT = 30000L;
	
    protected static JettyTestServer jettyServer;
    protected static ThreadLocal<RepositoryManager> repositoryManager = new ThreadLocal<>();
    protected static ThreadLocal<ProjectManager> projectManager = new ThreadLocal<>();
    protected static ThreadLocal<TLModel> model = new ThreadLocal<>();
    protected static ThreadLocal<Project> project = new ThreadLocal<>();
    protected static ThreadLocal<File> wipFolder = new ThreadLocal<>();
    
    private Object waitLock = new Object();
    private Object doneLock = new Object();
    private Set<RepositoryUserTasks> completedTasks = new HashSet<>();
    
    protected static void startTestServer(String repositorySnapshotFolder, int port,
            Class<?> testClass) throws Exception {
        System.setProperty("ota2.repository.realTimeIndexing", "true");
        File snapshotBase = new File(System.getProperty("user.dir"),
                "/src/test/resources/repo-snapshots");
        File repositorySnapshot = new File(snapshotBase, repositorySnapshotFolder);

        jettyServer = new JettyTestServer(port, repositorySnapshot, testClass);
        jettyServer.start();
    }
    
    @AfterClass
    public static void shutdownTestServer() throws Exception {
        jettyServer.stop();
    }
    
    public void executeUserTasks(RepositoryUserTasks user1Tasks, RepositoryUserTasks user2Tasks) throws Exception {
    	Thread user1Thread = new Thread( user1Tasks, user1Tasks.getClass().getSimpleName() );
    	Thread user2Thread = new Thread( user2Tasks, user2Tasks.getClass().getSimpleName() );
    	
    	user1Thread.start();
    	user2Thread.start();
    	
    	// Wait for whichever thread completes its work first
    	synchronized (doneLock) {
    		doneLock.wait();
    	}
    	
		// Wait for the second task to complete.  If not completed by the end of the
		// timeout period, force an interrupt.
		if (completedTasks.contains( user1Tasks )) {
			cleanupUserThread( user2Thread, user2Tasks );
			
		} else if (completedTasks.contains( user2Tasks )) {
			cleanupUserThread( user1Thread, user1Tasks );
			
		} else { // For some reason, neither thread is "done"
			cleanupUserThread( user1Thread, user1Tasks );
			cleanupUserThread( user2Thread, user2Tasks );
		}
		
		// Check the results of each task and return
		if (user1Tasks.taskException != null) {
			if (DEBUG) user1Tasks.taskException.printStackTrace( System.out );
			throw new AssertionError( "Failure in multi-user test scenario.", user1Tasks.taskException );
		}
		if (user2Tasks.taskException != null) {
			if (DEBUG) user2Tasks.taskException.printStackTrace( System.out );
			throw new AssertionError( "Failure in multi-user test scenario.", user2Tasks.taskException );
		}
    }
    
    private void setupCurrentThread(String userId, String password) throws Exception {
        File localRepository = new File(System.getProperty("user.dir"), "/target/test-workspace/"
                + this.getClass().getSimpleName() + "/" + userId + "/local-repository");
        File wipSnapshot = new File(System.getProperty("user.dir"), "/src/test/resources/test-data");
        Repository testRepository;
        
        wipFolder.set(new File(System.getProperty("user.dir"), "/target/test-workspace/"
                + this.getClass().getSimpleName() + "/" + userId + "/wip"));
        
        if (wipFolder.get().exists()) {
            RepositoryTestUtils.deleteContents(wipFolder.get());
        }
        if (localRepository.exists()) {
            RepositoryTestUtils.deleteContents(localRepository);
        }
        RepositoryTestUtils.copyContents(wipSnapshot, wipFolder.get());
        localRepository.mkdirs();
        
        model.set( new TLModel() );
        repositoryManager.set( new RepositoryManager( localRepository ) );
        testRepository = jettyServer.configureRepositoryManager( repositoryManager.get() );
        repositoryManager.get().setCredentials( testRepository, userId, password );
        projectManager.set( new ProjectManager( model.get(), false, repositoryManager.get() ) );
    }
    
    private void cleanupUserThread(Thread userThread, RepositoryUserTasks userTasks) {
    	if (userTasks.waiting) {
    		if (userThread.isAlive()) {
    			userTasks.forceKill = true;
    			userThread.interrupt();
    		}
    		
    	} else {
    		try {
    			userThread.join( WAIT_TIMEOUT );
    			
    		} catch (InterruptedException e) {}
    		
    		if (userThread.isAlive()) {
    			userTasks.forceKill = true;
    			userThread.interrupt();
    		}
    	}
    }
    
    protected void loadProject(String projectFilePath) throws Exception {
        File projectFile = new File(wipFolder.get(), projectFilePath);
        ValidationFindings findings = new ValidationFindings();

        if (!projectFile.exists()) {
            throw new FileNotFoundException("Test File Not Found: " + projectFile.getAbsolutePath());
        }

        project.set( projectManager.get().loadProject(projectFile, findings) );

        // Verify that the project loaded correctly
        if (findings.hasFinding(FindingType.ERROR)) {
            RepositoryTestUtils.printFindings(findings);
        }
        assertFalse(findings.hasFinding(FindingType.ERROR));
    }
    
    protected ProjectItem findProjectItem(String filename) {
        ProjectItem result = null;

        for (ProjectItem item : projectManager.get().getAllProjectItems()) {
            if (item.getFilename().equals(filename)) {
                result = item;
                break;
            }
        }
        return result;
    }

    protected RepositoryItem findRepositoryItem(List<RepositoryItem> itemList, String filename) {
        RepositoryItem result = null;

        for (RepositoryItem item : itemList) {
            if (item.getFilename().equals(filename)) {
                result = item;
                break;
            }
        }
        return result;
    }

    protected abstract class RepositoryUserTasks implements Runnable {
    	
    	private String userId;
    	private String password;
    	private boolean waitToExecute;
    	
    	public boolean waiting = false;
    	public boolean forceKill = false;
    	public Throwable taskException;
    	
    	public RepositoryUserTasks(String userId, String password, boolean waitToExecute) {
    		this.userId = userId;
    		this.password = password;
    		this.waitToExecute = waitToExecute;
    	}
    	
    	public void run() {
    		try {
    			int taskNumber = 0;
    			boolean done = false;
    			
    			setupCurrentThread( userId, password );
    			
    			while (!done && !forceKill) {
    				// If required, wait for the other user to finish their first task
    				if (waitToExecute) {
						synchronized (waitLock) {
							waiting = true;
    						waitLock.wait();
    						waiting = false;
						}
						waitToExecute = false;
    				}
    				
    				// Execute the next tasks
    				done = executeTask( taskNumber );
    				taskNumber++;
    				
    				// Notify the other user to execute their next task and wait for them to finish
					synchronized (waitLock) {
						waitLock.notify();
					}
					
    				if (!done && !forceKill) {
    					synchronized (waitLock) {
    						waiting = true;
    						waitLock.wait();
    						waiting = false;
    					}
    				}
    			}
    			
    		} catch (Throwable t) {
    			taskException = t;
    			
    		} finally {
    			synchronized (doneLock) {
        			completedTasks.add( this );
        			doneLock.notify();
    			}
    		}
    	}
    	
    	/**
    	 * Executes the current task and returns true if all tasks are complete.
    	 * 
    	 * @param taskNumber  the task number to be executed
    	 * @return boolean
    	 * @throws Exception  thrown if an error occurs during task execution
    	 */
    	public abstract boolean executeTask(int taskNumber) throws Exception;
    	
    }
    
}
