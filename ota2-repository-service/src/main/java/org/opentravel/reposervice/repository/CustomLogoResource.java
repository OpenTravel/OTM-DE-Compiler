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

package org.opentravel.reposervice.repository;

import org.opentravel.reposervice.util.RepositoryLogoImage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * Controller that streams the content for custom repository logo images.
 */
@Path("/")
public class CustomLogoResource {

    /**
     * Returns the content for the repository's custom logo (if one exists).
     * 
     * @return Response
     */
    @GET
    @Path("customLogo")
    public Response customLogo() {
        RepositoryLogoImage logoImage = RepositoryLogoImage.getDefault();
        Response response = null;

        if ((logoImage != null) && (logoImage.getLogoContent() != null)) {
            LogoStreamingOutput stream =
                new LogoStreamingOutput( new ByteArrayInputStream( logoImage.getLogoContent() ) );

            response = Response.ok( stream ).header( "Content-Type", logoImage.getContentType() ).build();
        }

        if (response == null) {
            response = Response.status( 404 ).build();
        }
        return response;
    }

    /**
     * Returns the content for the repository's temporary custom logo (if one exists). A temporary logo file is stored
     * in the system temp directory and is only displayed to an administrator during the banner image editing process.
     * 
     * @param filename the name of the temporary logo file to be displayed
     * @return Response
     */
    @GET
    @Path("tempLogo")
    public Response tempLogo(@QueryParam("file") String filename) {
        Response response = null;

        if ((filename != null) && (filename.length() > 0)) {
            File logoFile = new File( System.getProperty( "java.io.tmpdir" ), '/' + filename );
            String contentType = RepositoryLogoImage.getContentType( filename );

            if (logoFile.exists() && (contentType != null)) {
                try {
                    LogoStreamingOutput stream = new LogoStreamingOutput( new FileInputStream( logoFile ) );

                    response = Response.ok( stream ).header( "Content-Type", contentType ).build();

                } catch (FileNotFoundException e) {
                    // Ignore - will result in a 404 (not found) response
                }
            }
        }

        if (response == null) {
            response = Response.status( 404 ).build();
        }
        return response;
    }

    /**
     * Used to stream output for the repository logo from an input stream back to the caller in the HTTP response.
     */
    static class LogoStreamingOutput implements StreamingOutput {

        private InputStream stream;

        /**
         * Constructor that specifies the input stream from which image content should be obtained.
         * 
         * @param stream the input stream for local image content
         */
        public LogoStreamingOutput(InputStream stream) {
            this.stream = stream;
        }

        /**
         * @see javax.ws.rs.core.StreamingOutput#write(java.io.OutputStream)
         */
        @Override
        public void write(OutputStream os) throws IOException {
            try (InputStream is = stream) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = is.read( buffer, 0, buffer.length )) >= 0) {
                    os.write( buffer, 0, bytesRead );
                }
            }
        }

    }

}
