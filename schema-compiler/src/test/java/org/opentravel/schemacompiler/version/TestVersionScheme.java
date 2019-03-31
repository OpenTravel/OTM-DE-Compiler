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

package org.opentravel.schemacompiler.version;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

/**
 * Verfies the operation of the <code>VersionScheme</code> components and implementation.
 * 
 * @author S. Livezey
 */
public class TestVersionScheme {

    public static final String OTA2_VERSION_SCHEME_IDENTIFIER = "OTA2";

    @Test
    public void testGetBaseNamespace_OTA2() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );
        String namespace1 = "http://www.OpenTravel.org/ns/OTA2/Common_v01_00";
        String namespace2 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v1_5";

        assertEquals( "http://www.OpenTravel.org/ns/OTA2/Common", scheme.getBaseNamespace( namespace1 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package",
            scheme.getBaseNamespace( namespace2 ) );
    }

    @Test
    public void testGetVersionIdentifier_OTA2() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );
        String namespace1 = "http://www.OpenTravel.org/ns/OTA2/Common_v01_00";
        String namespace2 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v1_5_3";
        String namespace3 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v1_5";
        String namespace4 = "http://www.OpenTravel.org/ns/OTA2/Common_vAAA_BBB_CCC";

        assertEquals( "1.0.0", scheme.getVersionIdentifier( namespace1 ) );
        assertEquals( "1.5.3", scheme.getVersionIdentifier( namespace2 ) );
        assertEquals( "1.5.0", scheme.getVersionIdentifier( namespace3 ) );
        assertEquals( "1.0.0", scheme.getVersionIdentifier( namespace4 ) );
    }

    @Test
    public void testSetVersionIdentifier_OTA2() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );
        String namespace1 = "http://www.OpenTravel.org/ns/OTA2/Common_v01_00";
        String namespace2 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v1_5";
        String namespace3 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v1_5";
        String namespace4 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-v1_5";
        String namespace5 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/";
        String namespace6 = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package";

        assertEquals( "http://www.OpenTravel.org/ns/OTA2/Common_v02_05",
            scheme.setVersionIdentifier( namespace1, "2.5" ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package_v9_3_1",
            scheme.setVersionIdentifier( namespace2, "9.3.1" ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5",
            scheme.setVersionIdentifier( namespace3, "5" ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5",
            scheme.setVersionIdentifier( namespace3, "5.0" ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5",
            scheme.setVersionIdentifier( namespace3, "5.0.0" ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package-v5_12_132",
            scheme.setVersionIdentifier( namespace4, "5.12.132" ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5_12_132",
            scheme.setVersionIdentifier( namespace5, "5.12.132" ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5_12_132",
            scheme.setVersionIdentifier( namespace6, "5.12.132" ) );
    }

    @Test
    public void testSetPrefix_OTA2() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );

        assertEquals( "abc-0205", scheme.getPrefix( "abc", "2.5" ) );
        assertEquals( "abc-0205", scheme.getPrefix( "abc-", "2.5" ) );
        assertEquals( "abc-0200", scheme.getPrefix( "abc", "2.0" ) );
        assertEquals( "abc-020501", scheme.getPrefix( "abc", "2.5.1" ) );
        assertEquals( "abc-0205", scheme.getPrefix( "abc", "2.5.0" ) );
        assertEquals( "abc-0200", scheme.getPrefix( "abc-0100", "2.0" ) );
        assertEquals( "abc-020501", scheme.getPrefix( "abc-0102", "2.5.1" ) );
        assertEquals( "abc-0205", scheme.getPrefix( "abc-010201", "2.5.0" ) );
    }

    @Test
    public void testGetPatchLevel_OTA2() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );

        assertEquals( "56", scheme.getPatchLevel( "1.234.56" ) );
        assertEquals( "0", scheme.getPatchLevel( "1.234" ) );
        assertEquals( "0", scheme.getPatchLevel( "A.B.C" ) );
    }

    @Test
    public void testIncrementMajorVersion_OTA2() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );

        assertEquals( "2.0.0", scheme.incrementMajorVersion( "1.5.3" ) );
        assertEquals( "2.0.0", scheme.incrementMajorVersion( "1.5" ) );
        assertEquals( "1.0.0", scheme.incrementMajorVersion( "A.B.C" ) );
    }

    @Test
    public void testIncrementMinorVersion_OTA2() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );

        assertEquals( "1.6.0", scheme.incrementMinorVersion( "1.5.3" ) );
        assertEquals( "1.6.0", scheme.incrementMinorVersion( "1.5" ) );
        assertEquals( "0.1.0", scheme.incrementMinorVersion( "A.B.C" ) );
    }

    @Test
    public void testIncrementPatchLevel_OTA2() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );

        assertEquals( "1.5.4", scheme.incrementPatchLevel( "1.5.3" ) );
        assertEquals( "1.5.1", scheme.incrementPatchLevel( "1.5" ) );
        assertEquals( "0.0.1", scheme.incrementPatchLevel( "A.B.C" ) );
    }

    @Test
    public void testVersionChain_MajorVersion() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );
        String ns = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5";
        List<String> versionChain = scheme.getMajorVersionChain( ns );

        assertEquals( 1, versionChain.size() );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5", versionChain.get( 0 ) );
    }

    @Test
    public void testVersionChain_MinorVersion() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );
        String ns = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5_2";
        List<String> versionChain = scheme.getMajorVersionChain( ns );

        assertEquals( 3, versionChain.size() );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5_2", versionChain.get( 0 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5_1", versionChain.get( 1 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5", versionChain.get( 2 ) );
    }

    @Test
    public void testVersionChain_PatchVersion() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );
        String ns = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5_2_3";
        List<String> versionChain = scheme.getMajorVersionChain( ns );

        assertEquals( 6, versionChain.size() );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5_2_3", versionChain.get( 0 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5_2_2", versionChain.get( 1 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5_2_1", versionChain.get( 2 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5_2", versionChain.get( 3 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5_1", versionChain.get( 4 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v5", versionChain.get( 5 ) );
    }

    @Test
    public void testVersionChain_PaddedUriVersionNumbers() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );
        String ns = "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v05_02_03";
        List<String> versionChain = scheme.getMajorVersionChain( ns );

        assertEquals( 6, versionChain.size() );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v05_02_03",
            versionChain.get( 0 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v05_02_02",
            versionChain.get( 1 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v05_02_01",
            versionChain.get( 2 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v05_02", versionChain.get( 3 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v05_01", versionChain.get( 4 ) );
        assertEquals( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v05", versionChain.get( 5 ) );
    }

    @Test
    public void testDefaultFileHint() throws Exception {
        VersionScheme scheme = VersionSchemeFactory.getInstance().getVersionScheme( OTA2_VERSION_SCHEME_IDENTIFIER );

        assertEquals( "TestLibrary_5_2_3.otm", scheme.getDefaultFileHint(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v05_02_03", "TestLibrary" ) );
        assertEquals( "TestLibrary_5_2_0.otm", scheme.getDefaultFileHint(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v05_02", "TestLibrary" ) );
        assertEquals( "TestLibrary_5_0_0.otm", scheme
            .getDefaultFileHint( "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v05", "TestLibrary" ) );
        assertEquals( "Test_Library_5_0_0.otm", scheme.getDefaultFileHint(
            "http://www.OpenTravel.org/ns/OTA2/SchemaCompiler/test-package/v05", "Test Library" ) );
    }

}
