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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static utility methods to handle conversions between URL's and local file handles.
 * 
 * @author S. Livezey
 */
public class URLUtils {

    private static final Pattern pathPartPattern = Pattern.compile( "(/[^/]*)/" );

    /**
     * Default constructor (private).
     */
    private URLUtils() {}

    /**
     * Converts the given file handle to a URL reference.
     * 
     * @param urlStr the URL string to convert
     * @return URL
     * @throws IllegalArgumentException thrown if the URL string cannot be converted to a well-formed URL
     */
    public static URL toURL(String urlStr) {
        try {
            return new URL( urlStr );

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException( e );
        }
    }

    /**
     * Converts the given file handle to a URL reference.
     * 
     * @param file the file handle to convert
     * @return URL
     * @throws IllegalArgumentException thrown if the file path cannot be converted to a well-formed URL
     */
    public static URL toURL(File file) {
        try {
            String filePath = file.getAbsoluteFile().getAbsolutePath();

            filePath = filePath.replace( ':', '|' ); // Handle drive-letter reference for Windows
            return normalizeUrl( new URL( "file:///" + filePath ) );

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException( e );
        }
    }

    /**
     * Returns true if the given URL is a reference to the local file system.
     * 
     * @param url the URL to analyze
     * @return boolean
     */
    public static boolean isFileURL(URL url) {
        String protocol = url.getProtocol();
        String authority = url.getAuthority();

        return (protocol != null) && protocol.equals( "file" )
            && ((authority == null) || authority.equals( "" ) || authority.equalsIgnoreCase( "localhost" ));
    }

    /**
     * Converts the given URL reference to a local file handle.
     * 
     * @param url the URL reference to convert
     * @return File
     * @throws IllegalArgumentException thrown if the URL does not reference the local file system
     */
    public static File toFile(URL url) {
        if (!isFileURL( url )) {
            throw new IllegalArgumentException( "URL is not a local file reference: " + url.toExternalForm() );
        } else {
            String filePath = url.getPath();

            // Handle drive-letter URL formatting for Windows
            if (filePath.indexOf( '|' ) >= 0) {
                if (filePath.startsWith( "/" ) && (filePath.length() > 1)) {
                    filePath = filePath.substring( 1 );
                }
                filePath = filePath.replace( '|', ':' );
            }
            return new File( filePath );
        }
    }

    /**
     * Returns a URL that is the relative parent location of the one passed to this method. If the given URL is, itself,
     * a top-level address (authority only - no path or file references), the original URL will be returned. In all
     * cases, the query string (if one exists) will be omitted from the URL that is returned.
     * 
     * @param url the URL for which to retrieve the parent address
     * @return URL
     */
    public static URL getParentURL(URL url) {
        URL parentUrl = url;
        try {
            String sourceUrl = url.toExternalForm();
            String urlPath = url.getPath();
            String baseUrl = sourceUrl.substring( 0, sourceUrl.lastIndexOf( urlPath ) );

            while (urlPath.endsWith( "/" )) {
                urlPath = urlPath.substring( 0, urlPath.length() - 1 );
            }
            int dotIdx = urlPath.lastIndexOf( '/' );

            if (dotIdx >= 0) {
                parentUrl = new URL( baseUrl + urlPath.substring( 0, dotIdx ) );
            } else {
                parentUrl = new URL( baseUrl );
            }

        } catch (MalformedURLException e) {
            // Should never happen - trap exception and return the original URL
        }
        return parentUrl;
    }

    /**
     * If the specified 'urlLocation' is a fully-qualified URL, that location is returned as a URL object. If the
     * 'urlLocation' is a relative URL, the fully-qualified URL is calculated uring the 'baseUrl' as the base
     * <i>folder</i> location from which the absolute location is to be derived.
     * 
     * <p>
     * NOTE: It is important that the 'baseUrl' passed to this method is not a file location.
     * 
     * @param urlLocation the fully-qualified or relative URL location
     * @param baseUrl the base URL from which to calculate relative locations
     * @return URL
     * @throws MalformedURLException thrown if the URL format is invalid
     */
    public static URL getResolvedURL(String urlLocation, URL baseUrl) throws MalformedURLException {
        URL resolvedUrl = null;

        if (urlLocation == null) {
            throw new NullPointerException( "URL location cannot be null." );
        } else {
            try {
                resolvedUrl = normalizeUrl( new URL( urlLocation ) );

            } catch (MalformedURLException e) {
                // No error - try to resolve as a relative URL
            }
            if (resolvedUrl == null) {
                resolvedUrl = normalizeUrl( new URL( baseUrl.toExternalForm() + "/" + urlLocation ) );
            }
        }
        return resolvedUrl;
    }

    /**
     * Normalizes the given URL path to remove relative folder path references (e.g. '/.' and '/..').
     * 
     * @param url the URL to normalize
     * @return URL
     */
    public static final URL normalizeUrl(URL url) {
        String urlPath = url.getPath();
        URL result = url;

        if ((urlPath.indexOf( "/./" ) >= 0) || (urlPath.indexOf( "/../" ) >= 0)) {
            try {
                String sourceUrl = url.toExternalForm();
                StringBuilder targetUrl =
                    new StringBuilder( sourceUrl.substring( 0, sourceUrl.lastIndexOf( urlPath ) ) );
                List<String> pathList = new ArrayList<>();

                resolveRelativePathReferences( urlPath, pathList );

                // Append the remaining path parts to the target URL
                for (String pathPart : pathList) {
                    targetUrl.append( pathPart );
                }
                if (url.getQuery() != null) {
                    targetUrl.append( url.getQuery() );
                }
                result = new URL( targetUrl.toString() );

            } catch (MalformedURLException e) {
                // ignore and return original URL
            }
        }
        return result;
    }

    /**
     * Resolves relative path references (e.g. /. and /..) in the given path.
     * 
     * @param urlPath the URL path for which relative references will be resolved
     * @param pathList the list of URL path components
     */
    private static void resolveRelativePathReferences(String urlPath, List<String> pathList) {
        boolean pathStarted = false;
        Matcher m;

        while ((m = pathPartPattern.matcher( urlPath )).find()) {
            String pathPart = m.group( 1 );

            if (pathPart.equals( "/" ) && !pathStarted) {
                pathList.add( pathPart );

            } else if (pathPart.equals( "/." ) || pathPart.equals( "/" )) {
                // no action - discard
                pathStarted = true;

            } else if (pathPart.equals( "/.." )) {
                if (!pathList.isEmpty()) {
                    pathList.remove( pathList.size() - 1 );
                }
                pathStarted = true;

            } else {
                pathList.add( pathPart );
                pathStarted = true;
            }
            urlPath = urlPath.substring( m.end( 1 ) );
        }
        if (urlPath.length() > 0) {
            pathList.add( urlPath );
        }
    }

    /**
     * Returns the relative URL string that represents the path from the specified originating location to the indicated
     * target location.
     * 
     * @param originatingLocation the starting location from which the relative URL should be calculated
     * @param targetLocation the target (i.e. destination) location of the desired reative URL
     * @param originatingLocationIsFile flag indicating whether the originating location provided represents the URL of
     *        a file or folder (for files, the last path component of the URL is disgarded before processing begins)
     * @return String
     */
    public static final String getRelativeURL(URL originatingLocation, URL targetLocation,
        boolean originatingLocationIsFile) {
        // Special Case: If the URL protocols or authorities do not match, the target location is
        // the relative URL
        if (!isMatchingProtocolAndAuthority( originatingLocation, targetLocation )) {
            return targetLocation.toExternalForm();
        }
        String basePath = originatingLocation.toExternalForm();
        String targetPath = targetLocation.toExternalForm();

        // Normalize the originating URL so that it refers to the target source folder
        if (originatingLocationIsFile) {
            int slashIdx = basePath.lastIndexOf( '/' );

            if ((slashIdx > 0) && ((slashIdx + 1) < basePath.length())) {
                basePath = basePath.substring( 0, slashIdx + 1 );
            }
        }
        if (!basePath.endsWith( "/" )) {
            basePath += "/";
        }

        // Break each of the paths into their respective components
        String[] base = basePath.split( Pattern.quote( "/" ), -1 );
        String[] target = targetPath.split( Pattern.quote( "/" ), 0 );
        StringBuilder commonPath = new StringBuilder();
        StringBuilder relativePath = new StringBuilder();
        int commonIndex = 0;

        // First get all the common elements. Store them as a string, while we count how
        // many of them there are.
        for (int i = 0; i < target.length && i < base.length; i++) {
            if (target[i].equals( base[i] )) {
                commonPath.append( target[i] + "/" );
                commonIndex++;
            } else {
                break;
            }
        }

        // No common path elements - return the target path as an absolute
        if (commonIndex == 0) {
            return targetPath;
        }

        if (base.length != commonIndex) {
            int numDirsUp = base.length - commonIndex - 1;

            // The number of directories we have to backtrack is the length of the base path MINUS
            // the number of common path elements, minus one because the last element in the path
            // is not a directory.
            for (int i = 1; i <= (numDirsUp); i++) {
                relativePath.append( "../" );
            }
        }
        relativePath.append( targetPath.substring( commonPath.length() ) );

        return relativePath.toString();
    }

    /**
     * Returns true if the protocol and authority components of the given URL match one another. If the URL represents a
     * windows-style file URL, the drive-letter is also checked.
     * 
     * @param url1 the first URL to compare
     * @param url2 the second URL to compare
     * @return boolean
     */
    private static boolean isMatchingProtocolAndAuthority(URL url1, URL url2) {
        String protocol1 = url1.getProtocol();
        String protocol2 = url2.getProtocol();
        String authority1 = url1.getAuthority();
        String authority2 = url2.getAuthority();
        boolean protocolMatches =
            ((protocol1 == null) || protocol1.equals( "" )) ? ((protocol2 == null) || protocol2.equals( "" ))
                : protocol1.equals( protocol2 );
        boolean authorityMatches =
            ((authority1 == null) || authority1.equals( "" )) ? ((authority2 == null) || authority2.equals( "" ))
                : authority1.equals( authority2 );
        boolean isMatch = protocolMatches && authorityMatches;

        if (isMatch && (protocol1 != null) && protocol1.equals( "file" ) && (protocol2 != null)
            && protocol2.equals( "file" )) {
            String path1 = url1.getPath();
            String path2 = url2.getPath();
            int driveIdx1 = path1.indexOf( '|' );
            int driveIdx2 = path2.indexOf( '|' );
            String driveLetter1 = null;
            String driveLetter2 = null;

            if (driveIdx1 >= 0) {
                driveLetter1 = path1.substring( 0, driveIdx1 );
            }
            if (driveIdx2 >= 0) {
                driveLetter2 = path2.substring( 0, driveIdx2 );
            }
            isMatch = (driveLetter1 == null) ? (driveLetter2 == null) : driveLetter1.equals( driveLetter2 );
        }
        return isMatch;
    }

    /**
     * Returns the last path component of the URL as a string.
     * 
     * @param url the URL to process
     * @return String
     */
    public static final String getShortRepresentation(URL url) {
        String result = null;

        if (url != null) {
            String path = url.getPath();

            if (path.endsWith( "/" )) {
                path = path.substring( 0, path.length() - 1 );
            }
            int dotIdx = path.lastIndexOf( '/' );
            result = (dotIdx == 0) ? path : path.substring( dotIdx );
        }
        return result;
    }

    /**
     * Returns the filename component of the given URL without the associated path information.
     * 
     * @param url the URL for which to return the filename component
     * @return String
     */
    public static final String getUrlFilename(URL url) {
        String filepath = url.getFile();
        int lastPathBreak = filepath.lastIndexOf( '/' );
        String filename;

        if (lastPathBreak < 0) {
            filename = filepath;

        } else if (lastPathBreak < filepath.length()) {
            filename = filepath.substring( lastPathBreak + 1 );

        } else {
            filename = null; // No filename if the path ends with a '/'
        }
        return filename;
    }

    /**
     * Returns true if the given URI is valid.
     * 
     * @param namespaceUri the namespace URI to check
     * @return boolean
     */
    public static boolean isValidURI(String namespaceUri) {
        boolean result = false;
        try {
            if (namespaceUri != null) {
                new URI( namespaceUri );
                result = true;
            }
        } catch (URISyntaxException e) {
            // Ignore error and return false
        }
        return result;
    }

}
