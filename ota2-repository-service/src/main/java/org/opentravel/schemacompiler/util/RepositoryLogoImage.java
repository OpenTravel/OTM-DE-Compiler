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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.schemacompiler.repository.RepositoryComponentFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Returns the URL link for the repository image.
 */
public class RepositoryLogoImage {

    public static final String DEFAULT_LOGO_URL = "/images/ota_logo.png";
    public static final String CUSTOM_LOGO_URL = "/service/customLogo";

    private static final Map<String,String> CONTENT_TYPE_MAPPINGS;
    private static Log log = LogFactory.getLog( RepositoryLogoImage.class );

    private static Map<String,RepositoryLogoImage> defaultInstances = new HashMap<>();

    private File repositoryLocation;
    private String logoUrl;
    private File logoFile;
    private String contentType;
    private byte[] logoContent;

    /**
     * Constructor that assigns the root location for the repository content.
     * 
     * @param repositoryLocation the root folder location for the repository content
     */
    public RepositoryLogoImage(File repositoryLocation) {
        this.repositoryLocation = repositoryLocation;
        defaultInstances.put( repositoryLocation.getAbsolutePath(), this );
        refreshLogo();
    }

    /**
     * Returns the default instance for the current location file system of the repository.
     * 
     * @return RepositoryLogoImage
     */
    public static RepositoryLogoImage getDefault() {
        return getDefault( RepositoryComponentFactory.getDefault().getRepositoryLocation() );
    }

    /**
     * Returns the default instance for the current location file system of the repository.
     * 
     * @param repositoryLocation the root folder location for the repository content
     * @return RepositoryLogoImage
     */
    public static RepositoryLogoImage getDefault(File repositoryLocation) {
        return defaultInstances.get( repositoryLocation.getAbsolutePath() );
    }

    /**
     * Returns the URL location of the repository logo relative to the web applications's context path.
     * 
     * @return String
     */
    public String getLogoUrl() {
        if (logoUrl == null) {
            refreshLogo();
        }
        return logoUrl;
    }

    /**
     * Returns the location on the local file system where the logo image is stored.
     * 
     * @return File
     */
    public File getLogoFile() {
        if (logoUrl == null) {
            refreshLogo();
        }
        return logoFile;
    }

    /**
     * Returns the raw content of the repository logo image file.
     * 
     * @return byte[]
     */
    public byte[] getLogoContent() {
        return logoContent;
    }

    /**
     * Returns the content type of the repository logo image file.
     * 
     * @return String
     */
    public String getContentType() {
        if (logoUrl == null) {
            refreshLogo();
        }
        return contentType;
    }

    /**
     * Returns the associated content type for an image file with the specified name.
     * 
     * @param filename the name of the image file for which to return the content type
     * @return String
     */
    public static String getContentType(String filename) {
        String contentType = null;

        if (filename != null) {
            int dotIdx = filename.lastIndexOf( '.' );
            String fileExt = filename.substring( dotIdx ).toLowerCase();

            contentType = CONTENT_TYPE_MAPPINGS.get( fileExt );

            if (contentType == null) {
                contentType = "appcliation/octet-stream";
            }
        }
        return contentType;
    }

    /**
     * Invalidates the cached location of the repository logo, forcing it to be refreshed the next time it is requested.
     */
    public void invalidate() {
        logoUrl = null;
        logoFile = null;
    }

    /**
     * Refreshes the URL and file locations of the repository logo image.
     */
    private void refreshLogo() {
        File[] logoFiles = repositoryLocation.listFiles( (d, n) -> n.startsWith( "custom_logo." ) );

        if (logoFiles.length == 0) {
            logoUrl = DEFAULT_LOGO_URL;
            logoFile = null;
            contentType = null;
            logoContent = null;

        } else {
            logoUrl = CUSTOM_LOGO_URL;
            logoFile = logoFiles[0];
            contentType = getContentType( logoFile.getName() );

            try (InputStream is = new FileInputStream( logoFile )) {
                ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = is.read( buffer, 0, buffer.length )) >= 0) {
                    imageBytes.write( buffer, 0, bytesRead );
                }
                logoContent = imageBytes.toByteArray();

            } catch (IOException e) {
                log.error( "Error loading content from logo image file: " + logoFile.getName() );
                logoContent = null;
            }
        }
    }

    /**
     * Initializes the static map of image file extensions to content MIME types.
     */
    static {
        try {
            Map<String,String> contentTypeMappings = new HashMap<>();

            contentTypeMappings.put( ".jpg", "image/jpeg" );
            contentTypeMappings.put( ".jpeg", "image/jpeg" );
            contentTypeMappings.put( ".jfif", "image/pipeg" );
            contentTypeMappings.put( ".gif", "image/gif" );
            contentTypeMappings.put( ".png", "image/png" );
            contentTypeMappings.put( ".bmp", "image/bmp" );
            contentTypeMappings.put( ".ief", "image/ief" );
            contentTypeMappings.put( ".svg", "image/svg+xml" );
            contentTypeMappings.put( ".tif", "image/tiff" );
            contentTypeMappings.put( ".tiff", "image/tiff" );
            contentTypeMappings.put( ".ico", "image/x-icon" );
            CONTENT_TYPE_MAPPINGS = contentTypeMappings;

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
