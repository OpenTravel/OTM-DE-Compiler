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

import org.opentravel.schemacompiler.repository.testutil.AbstractRepositoryTest;

import java.io.File;

/**
 * Abstract base class that defines common methods used during live repository testing.
 * 
 * @author S. Livezey
 */
public abstract class RepositoryTestBase extends AbstractRepositoryTest {

    protected static final boolean DEBUG = true;

    protected static File defaultRepositoryConfig =
        new File( System.getProperty( "user.dir" ), "/target/test-classes/ota2-repository-config.xml" );
    protected static File ldapLookupRepositoryConfig =
        new File( System.getProperty( "user.dir" ), "/target/test-classes/ota2-repository-config-ldaplookup.xml" );
    protected static File ldapSearchRepositoryConfig =
        new File( System.getProperty( "user.dir" ), "/target/test-classes/ota2-repository-config-ldapsearch.xml" );
    protected static File jmsIndexRepositoryConfig =
        new File( System.getProperty( "user.dir" ), "/target/test-classes/ota2-repository-config-jmsindex.xml" );
    protected static File svnRepositoryConfig =
        new File( System.getProperty( "user.dir" ), "/target/test-classes/ota2-repository-config-svn.xml" );

    protected synchronized static void startTestServer(String repositorySnapshotFolder, int port, Class<?> testClass)
        throws Exception {
        startTestServer( repositorySnapshotFolder, port, defaultRepositoryConfig, true, false, testClass );
    }

}
