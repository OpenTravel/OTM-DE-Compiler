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

package org.opentravel.reposervice.console;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Verifies the ability of the OTA2.0 repository to authenticate against an LDAP directory in USER_SEARCH mode.
 */
public class TestLdapSearchAuthentication extends TestLdapAuthentication {

    @BeforeClass
    public static void setupTests() throws Exception {
        setupWorkInProcessArea( TestLdapSearchAuthentication.class );
        startLdapTestServer( 1489, "/ldif-snapshots/user-search.ldif" );
        startTestServer( "versions-repository", 9299, ldapSearchRepositoryConfig, true, true,
            TestLdapSearchAuthentication.class );
    }

    @AfterClass
    public static void tearDownTests() throws Exception {
        shutdownTestServer();
        stopLdapTestServer();
    }

}
