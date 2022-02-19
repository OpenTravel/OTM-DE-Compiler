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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opentravel.repocommon.security.impl.DefaultRepositorySecurityManager;
import org.opentravel.reposervice.console.LoginController;
import org.opentravel.schemacompiler.repository.RepositorySecurityException;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Servlet filter that performs an automated sign-in for the OTM Repository web console if a BASIC authorization header
 * is present on the web request.
 */
public class BasicAuthFilter implements Filter {

    private static Logger log = LogManager.getLogger( BasicAuthFilter.class );

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // No init() action required
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // No destroy() action required
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     *      javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
        throws IOException, ServletException {

        if (req instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) req;

            if (!LoginController.isAuthenticated( request.getSession() )) {
                String authHeader = request.getHeader( "Authorization" );

                if (authHeader != null) {
                    try {
                        String[] authParts = DefaultRepositorySecurityManager.getAuthorizationCredentials( authHeader );
                        boolean authenticated =
                            LoginController.authenticateUser( authParts[0], authParts[1], request.getSession() );

                        if (!authenticated) {
                            log.warn( "Invalid authentication credentials received for user: " + authParts[0] );
                        }

                    } catch (RepositorySecurityException e) {
                        log.warn( "Invalid authorization header received." );
                    }
                }
            }
        }
        chain.doFilter( req, resp );
    }

}
