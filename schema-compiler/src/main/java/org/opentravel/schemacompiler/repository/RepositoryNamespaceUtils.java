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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for manipulating namespace URI strings in conjunction with the repository API's.
 * 
 * @author S. Livezey
 */
public class RepositoryNamespaceUtils {

    private static final Pattern pathPartPattern = Pattern.compile("(/[^/]*)/");

    /**
     * Normalizes the given URI path to remove relative folder path references (e.g. '/.', '/..',
     * and '//'). The URI that is returned will never end with a '/' path separator.
     * 
     * @param url
     *            the namespace URI to normalize
     * @return URL
     */
    public static final String normalizeUri(String namespace) {
        int protocolSeparatorIdx = namespace.indexOf("://");
        String uriProtocol = (protocolSeparatorIdx < 0) ? null : namespace.substring(0,
                protocolSeparatorIdx);
        String urlPath = (uriProtocol == null) ? namespace : namespace
                .substring(protocolSeparatorIdx + 2);
        String result = namespace;

        if ((urlPath.indexOf("/./") >= 0) || (urlPath.indexOf("/../") >= 0)
                || (urlPath.indexOf("//") >= 0)) {
            StringBuilder targetUrl = new StringBuilder();
            List<String> pathList = new ArrayList<String>();

            if (uriProtocol != null) {
                targetUrl.append(uriProtocol).append(":/");
            }

            // Use the stack to resolve relative path references
            boolean pathStarted = false;
            Matcher m;

            while ((m = pathPartPattern.matcher(urlPath)).find()) {
                String pathPart = m.group(1);

                if (pathPart.equals("/") && !pathStarted) {
                    pathList.add(pathPart);

                } else if (pathPart.equals("/.") || pathPart.equals("/")) {
                    // no action - discard
                    pathStarted = true;

                } else if (pathPart.equals("/..")) {
                    if (!pathList.isEmpty()) {
                        pathList.remove(pathList.size() - 1);
                    }
                    pathStarted = true;

                } else {
                    pathList.add(pathPart);
                    pathStarted = true;
                }
                urlPath = urlPath.substring(m.end(1));
            }
            if (urlPath.length() > 0) {
                pathList.add(urlPath);
            }

            // Append the remaining path parts to the target URL
            for (String pathPart : pathList) {
                targetUrl.append(pathPart);
            }
            result = targetUrl.toString();

        }

        // Remove the trailing '/' if one exists
        if (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * Appends the given child path to the base namespace URI.
     * 
     * @param baseNS
     *            the base namespace to which the child path should be appended
     * @param childPath
     *            the child namespace path to append
     * @return String
     */
    public static String appendChildPath(String baseNS, String childPath) {
        StringBuilder ns = new StringBuilder(baseNS);

        if ((childPath != null) && (childPath.length() > 0)) {
            if (!baseNS.endsWith("/")) {
                ns.append('/');
            }
            if (childPath.startsWith("/")) {
                if (childPath.length() > 1) {
                    childPath = childPath.substring(1);

                } else {
                    childPath = "";
                }
            }
            ns.append(childPath);
        }
        return ns.toString();
    }

    /**
     * Returns the parent URI of the given namespace. If the namespace provided is a base namespace
     * of the associated repository, this method will return null. If the namespace is not part of
     * one of the repository's base namespace, an <code>IllegalArgumentException</code> will be
     * thrown.
     * 
     * @param ns
     *            the namespace for which to return the parent URI path
     * @param repository
     *            the repository with which the namespace is associated associated
     * @return String
     * @throws RepositoryException
     *             thrown if the root namespaces of the associated repository cannot be accessed
     * @throws IllegalArgumentException
     *             thrown if the given namespace URI is not part of the base namespace hierarchy
     *             from the associated repository
     */
    public static String getParentNamespace(String ns, Repository repository)
            throws RepositoryException {
        boolean isValidNS = false, isRootNS = false;
        String parentNS;

        if (!ns.endsWith("/"))
            ns += "/";

        for (String rootNS : repository.listRootNamespaces()) {
            if (!rootNS.endsWith("/"))
                rootNS += "/";

            if (rootNS.equals(ns)) {
                isValidNS = isRootNS = true;
                break;

            } else if (ns.startsWith(rootNS)) {
                isValidNS = true;
                break;
            }
        }

        if (isRootNS) {
            parentNS = null;

        } else if (isValidNS) {
            parentNS = ns.substring(0, ns.length() - 1); // strip the trailing '/' character
            parentNS = parentNS.substring(0, parentNS.lastIndexOf('/')); // not a root NS, so always
                                                                         // guranteed to have a
                                                                         // seconds path separator

        } else {
            throw new IllegalArgumentException(
                    "The namespace is not part of the repository's root namespace hierarchy: " + ns);
        }
        return parentNS;
    }

    /**
     * Returns the root namespace from the given repository that contains the given namespace URI.
     * 
     * @param ns
     *            the namespace URI for which to return the root
     * @param repository
     *            the repository with which the namespace is associated associated
     * @return String
     * @throws RepositoryException
     *             thrown if the root namespaces of the associated repository cannot be accessed
     * @throws IllegalArgumentException
     *             thrown if the given namespace URI is not part of the base namespace hierarchy
     *             from the associated repository
     */
    public static String getRootNamespace(String ns, Repository repository)
            throws RepositoryException {
        String parentNS = (ns == null) ? null : getParentNamespace(ns, repository);
        String currentNS = ns;

        while (parentNS != null) {
            currentNS = parentNS;
            parentNS = getParentNamespace(currentNS, repository);
        }
        return currentNS;
    }

}
