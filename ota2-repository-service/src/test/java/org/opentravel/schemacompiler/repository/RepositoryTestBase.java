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

import java.io.File;
import java.util.List;

import org.opentravel.schemacompiler.repository.Project;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;
import org.opentravel.schemacompiler.util.RepositoryTestUtils;

/**
 * Abstract base class that defines common methods used during live repository testing.
 * 
 * @author S. Livezey
 */
public abstract class RepositoryTestBase {

    protected static final boolean DEBUG = true;

    protected static ThreadLocal<RepositoryManager> repositoryManager = new ThreadLocal<RepositoryManager>();
    protected static ThreadLocal<Repository> testRepository = new ThreadLocal<Repository>();
    protected static ThreadLocal<JettyTestServer> jettyServer = new ThreadLocal<JettyTestServer>();
    protected static ThreadLocal<File> wipFolder = new ThreadLocal<File>();

    protected static void setupWorkInProcessArea(Class<?> testClass) throws Exception {
        File wipSnapshot = new File(System.getProperty("user.dir"), "/src/test/resources/test-data");
        wipFolder.set(new File(System.getProperty("user.dir"), "/target/test-workspace/"
                + testClass.getSimpleName() + "/wip"));

        if (wipFolder.get().exists()) {
            RepositoryTestUtils.deleteContents(wipFolder.get());
        }
        RepositoryTestUtils.copyContents(wipSnapshot, wipFolder.get());
    }

    protected synchronized static void startTestServer(String repositorySnapshotFolder, int port,
            Class<?> testClass) throws Exception {
        System.setProperty("ota2.repository.realTimeIndexing", "true");
        File localRepository = new File(System.getProperty("user.dir"), "/target/test-workspace/"
                + testClass.getSimpleName() + "/local-repository");
        File snapshotBase = new File(System.getProperty("user.dir"),
                "/src/test/resources/repo-snapshots");
        File repositorySnapshot = new File(snapshotBase, repositorySnapshotFolder);

        if (localRepository.exists()) {
            RepositoryTestUtils.deleteContents(localRepository);
        }
        localRepository.mkdirs();
        repositoryManager.set(new RepositoryManager(localRepository));

        jettyServer.set(new JettyTestServer(port, repositorySnapshot, testClass));
        jettyServer.get().start();

        testRepository.set(jettyServer.get().configureRepositoryManager(repositoryManager.get()));
        repositoryManager.get().setCredentials(testRepository.get(), "testuser", "password");
    }

    protected static void shutdownTestServer() throws Exception {
        jettyServer.get().stop();
    }

    protected ProjectItem findProjectItem(Project project, String filename) {
        ProjectItem result = null;

        for (ProjectItem item : project.getProjectItems()) {
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

}
