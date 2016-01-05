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
package org.opentravel.schemacompiler.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.opentravel.schemacompiler.util.URLUtils;

/**
 * Unit tests for the static methods of the <code>URLUtils</code> class.
 * 
 * @author S. Livezey
 */
public class TestURLUtils {

    private static final boolean isWindowsOS = System.getProperty("os.name").startsWith("Windows");

    @Test
    public void testIsFileUrl() throws Exception {
        URL fileUrl = new URL("file:////my/folder/location/myfile.txt");
        URL webUrl = new URL("http://localhost:8080/my/location/myfile.html");

        assertTrue(URLUtils.isFileURL(fileUrl));
        assertFalse(URLUtils.isFileURL(webUrl));
    }

    @Test
    public void testToURL() throws Exception {
        if (isWindowsOS) {
            File file = new File("c:/my/folder/location/myfile.txt");
            URL fileUrl = URLUtils.toURL(file);

            assertEquals("file:/c|/my/folder/location/myfile.txt", fileUrl.toExternalForm());
        } else {
            File file = new File("/my/folder/location/myfile.txt");
            URL fileUrl = URLUtils.toURL(file);

            assertEquals("file:////my/folder/location/myfile.txt", fileUrl.toExternalForm());
        }
    }

    @Test
    public void testToFile() throws Exception {
        if (isWindowsOS) {
            URL fileUrl = new URL("file:/c|/my/folder/location/myfile.txt");
            File file = URLUtils.toFile(fileUrl);

            assertEquals("c:\\my\\folder\\location\\myfile.txt", file.getAbsolutePath());
        } else {
            URL fileUrl = new URL("file:////my/folder/location/myfile.txt");
            File file = URLUtils.toFile(fileUrl);

            assertEquals("/my/folder/location/myfile.txt", file.getAbsolutePath());
        }

        try {
            URL webUrl = new URL("http://localhost:8080/my/location/myfile.html");

            URLUtils.toFile(webUrl);
            fail("Expected IllegalArgumentException - Not Thrown");

        } catch (IllegalArgumentException e) {
            // expected exception - test passed
        } catch (Throwable t) {
            fail("Expected IllegalArgumentException - Caught: " + t.getClass().getSimpleName());
        }
    }

    @Test
    public void testGetResolvedUrl() throws Exception {
        URL baseUrl = new URL("file:////my/folder/location");

        assertEquals("file:////my/folder/location/test.xml",
                URLUtils.getResolvedURL("test.xml", baseUrl).toExternalForm());
        assertEquals("file:////my/folder/location/test.xml",
                URLUtils.getResolvedURL("./test.xml", baseUrl).toExternalForm());
        assertEquals("file:////my/folder/test.xml", URLUtils.getResolvedURL("../test.xml", baseUrl)
                .toExternalForm());
        assertEquals("file:////my/testfolder/test.xml",
                URLUtils.getResolvedURL("../../testfolder/test.xml", baseUrl).toExternalForm());
    }

    @Test
    public void testGetParentUrl() throws Exception {
        URL fileUrl = new URL("file:////my/folder/location/myfile.txt");
        URL baseUrl = new URL("http://www.sabre-holdings.com");
        URL baseUrlWithSlash = new URL("http://www.sabre-holdings.com/");
        URL baseUrlWithSingleFolder = new URL("http://www.sabre-holdings.com/test");

        assertEquals("file:////my/folder/location", URLUtils.getParentURL(fileUrl).toExternalForm());
        assertEquals("http://www.sabre-holdings.com", URLUtils.getParentURL(baseUrl)
                .toExternalForm());
        assertEquals("http://www.sabre-holdings.com", URLUtils.getParentURL(baseUrlWithSlash)
                .toExternalForm());
        assertEquals("http://www.sabre-holdings.com", URLUtils
                .getParentURL(baseUrlWithSingleFolder).toExternalForm());
    }

    @Test
    public void testNormalizeUrl_noChange() throws Exception {
        URL origUrl = new URL("file:////my/folder/location/myfile.txt");
        String normalizedUrl = URLUtils.normalizeUrl(origUrl).toExternalForm();

        assertEquals("file:////my/folder/location/myfile.txt", normalizedUrl);
    }

    @Test
    public void testNormalizeUrl_localFile() throws Exception {
        URL origUrl = new URL("file:////my/folder/../location/myfile.txt");
        String normalizedUrl = URLUtils.normalizeUrl(origUrl).toExternalForm();

        assertEquals("file:////my/location/myfile.txt", normalizedUrl);
    }

    @Test
    public void testNormalizeUrl_remoteFile() throws Exception {
        URL origUrl = new URL("http://localhost:8080/my/folder/../location/.//myfile.html");
        String normalizedUrl = URLUtils.normalizeUrl(origUrl).toExternalForm();

        assertEquals("http://localhost:8080/my/location/myfile.html", normalizedUrl);
    }

    @Test
    public void testNormalizeUrl_win32File() throws Exception {
        URL origUrl = new URL("file:///c|/my/folder/../location/.//myfile.html");
        String normalizedUrl = URLUtils.normalizeUrl(origUrl).toExternalForm();

        assertEquals("file:/c|/my/location/myfile.html", normalizedUrl);
    }

    @Test
    public void testNormalizeUrl_win32FileWithHost() throws Exception {
        URL origUrl = new URL("file://localhost/c|/my/folder/../location/.//myfile.html");
        String normalizedUrl = URLUtils.normalizeUrl(origUrl).toExternalForm();

        assertEquals("file://localhost/c|/my/location/myfile.html", normalizedUrl);
    }

    @Test
    public void testRelativeUrl() throws Exception {

        // Linux-style file URL's
        assertEquals("myFolder/myFile.txt", URLUtils.getRelativeURL(new URL(
                "file:////usr/local/someFolder/someFile.txt"), new URL(
                "file:////usr/local/someFolder/myFolder/myFile.txt"), true));

        assertEquals("../../home/slivezey/myFolder/myFile.txt", URLUtils.getRelativeURL(new URL(
                "file:////usr/local/someFolder/someFile.txt"), new URL(
                "file:////usr/home/slivezey/myFolder/myFile.txt"), true));

        assertEquals("myFile.txt", URLUtils.getRelativeURL(new URL(
                "file:////usr/local/someFolder/someFile.txt"), new URL(
                "file:////usr/local/someFolder/myFile.txt"), true));

        assertEquals("subFolder/myFile.txt", URLUtils.getRelativeURL(new URL(
                "file:////usr/local/someFolder/someFile.txt"), new URL(
                "file:////usr/local/someFolder/subFolder/myFile.txt"), true));

        // Windows-style file URL's
        assertEquals("../../home/slivezey/myFolder/myFile.txt", URLUtils.getRelativeURL(new URL(
                "file:/c|/usr/local/someFolder/someFile.txt"), new URL(
                "file:/c|/usr/home/slivezey/myFolder/myFile.txt"), true));

        assertEquals("subFolder/myFile.txt", URLUtils.getRelativeURL(new URL(
                "file:/c|/usr/local/someFolder/someFile.txt"), new URL(
                "file:/c|/usr/local/someFolder/subFolder/myFile.txt"), true));

        // Internet-style URL's
        assertEquals("../my-folder/myFile.txt", URLUtils.getRelativeURL(new URL(
                "http://www.sabre.com/some-folder"), new URL(
                "http://www.sabre.com/my-folder/myFile.txt"), false));

        // Special Case: Mis-matches URL authority
        assertEquals("http://www.sabre.com/my-folder/myFile.txt", URLUtils.getRelativeURL(new URL(
                "http://www.sabre-holdings.com/some-folder"), new URL(
                "http://www.sabre.com/my-folder/myFile.txt"), false));

        // Special Case: Mis-matches URL protocol
        assertEquals("http://www.sabre.com/my-folder/myFile.txt", URLUtils.getRelativeURL(new URL(
                "file:////some-folder/someFile.txt"), new URL(
                "http://www.sabre.com/my-folder/myFile.txt"), true));

        // Special Case: Different drive letter for windows-style file URL's
        assertEquals("file:/d|/usr/local/someFolder/subFolder/myFile.txt", URLUtils.getRelativeURL(
                new URL("file:/c|/usr/local/someFolder/someFile.txt"), new URL(
                        "file:/d|/usr/local/someFolder/subFolder/myFile.txt"), true));
    }

}
