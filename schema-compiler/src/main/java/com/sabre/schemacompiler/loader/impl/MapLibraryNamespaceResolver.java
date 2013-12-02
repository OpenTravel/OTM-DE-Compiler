/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.loader.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sabre.schemacompiler.util.URLUtils;

/**
 * <code>LibraryNamespaceResolver</code> implementation backed by a simple map.
 *
 * @author S. Livezey
 */
public class MapLibraryNamespaceResolver extends DefaultLibraryNamespaceResolver {
	
	private Map<URI,List<URL>> namespaceMappings = Collections.synchronizedMap(new HashMap<URI, List<URL>>());
	
	/**
	 * Default constructor.
	 */
	public MapLibraryNamespaceResolver() {}
	
	/**
	 * @see com.sabre.schemacompiler.loader.impl.DefaultLibraryNamespaceResolver#resovleLibraryImport(java.net.URI, java.lang.String, java.lang.String[])
	 */
	public Collection<URL> resovleLibraryImport(URI libraryNamespace, String versionScheme, String[] fileHints) {
		Collection<URL> libraryUrls;
		
		if (namespaceMappings.containsKey(libraryNamespace)) {
			libraryUrls = namespaceMappings.get(libraryNamespace);
		} else {
			libraryUrls = super.resovleLibraryImport(libraryNamespace, versionScheme, fileHints);
		}
		return libraryUrls;
	}
	
	/**
	 * Adds a library module namespace mapping to the catalog.
	 * 
	 * @param libraryNamespace  the library module namespace URI
	 * @param libraryUrl  the URL of the library module
	 */
	public void addNamespaceMapping(URI libraryNamespace, URL libraryUrl) {
		if (libraryNamespace == null) {
			throw new NullPointerException("Library namespace URI cannot be null.");
		}
		if (libraryUrl == null) {
			throw new NullPointerException("Library URL cannot be null.");
		}
		List<URL> urlList = namespaceMappings.get(libraryNamespace);
		
		if (urlList == null) {
			urlList = new ArrayList<URL>();
			namespaceMappings.put(libraryNamespace, urlList);
		}
		if (!urlList.contains(libraryUrl)) {
			urlList.add(libraryUrl);
		}
	}
	
	/**
	 * Adds a library module namespace mapping to the catalog.
	 * 
	 * @param libraryNamespace  the library module namespace URI
	 * @param libraryUrl  the URL of the library module
	 * @throws URISyntaxException  thrown if the namespace URI is badly-formed
	 * @throws MalformedURLException  thrown if the module URL is badly-formed
	 */
	public void addNamespaceMapping(String libraryNamespace, String libraryUrl) throws URISyntaxException, MalformedURLException {
		if ((libraryUrl != null) && (libraryUrl.length() > 0)) {
			if (libraryUrl.startsWith(".")) {
				addNamespaceMapping(new URI(libraryNamespace), URLUtils.getResolvedURL(libraryUrl, getRelativeUrlBase()));
				
			} else {
				addNamespaceMapping(new URI(libraryNamespace), URLUtils.normalizeUrl( new URL(libraryUrl) ));
			}
		}
	}
	
	/**
	 * Returns the base file path to use for resolving relative URL's.  The default implementation
	 * returns the URL location identified by the "user.dir" system property.  Sub-classes may override
	 * if an alternate base path is required.
	 * 
	 * @return URL
	 * @throws MalformedURLException
	 */
	protected URL getRelativeUrlBase() throws MalformedURLException {
		return URLUtils.toURL(new File(System.getProperty("user.dir")));
	}
	
}
