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

package org.opentravel.schemacompiler.repository.impl;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.impl.client.SystemDefaultCredentialsProvider;

/**
 * Extended {@link SystemDefaultCredentialsProvider} with support for NTLM proxy authentication
 * 
 * @author Pawel Jedruch
 */
public class NTLMSystemCredentialsProvider extends SystemDefaultCredentialsProvider {

    @Override
    public Credentials getCredentials(final AuthScope authscope) {
        Credentials credentials = super.getCredentials( authscope );

        if (AuthSchemes.NTLM.equalsIgnoreCase( authscope.getScheme() )) {
            credentials = super.getCredentials( authscope );
            return traslateToNTLMCredentials( credentials );
        }
        return credentials;
    }

    private NTCredentials traslateToNTLMCredentials(Credentials credentials) {
        String fullUserName = credentials.getUserPrincipal().getName();
        String[] tokens = fullUserName.split( "\\\\", 2 );
        String userName = "";
        String domain = null;
        if (tokens.length == 2) {
            if (tokens[0] != null && !tokens[0].isEmpty()) {
                domain = tokens[0];
            }
            userName = tokens[1];
        } else {
            userName = tokens[0];
        }
        String workstation = null; // how to support workstation ???
        return new NTCredentials( userName, credentials.getPassword(), workstation, domain );
    }
}
