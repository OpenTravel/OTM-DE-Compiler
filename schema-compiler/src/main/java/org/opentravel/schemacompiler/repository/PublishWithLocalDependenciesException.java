/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.repository;

import java.util.Collection;

/**
 * Thrown by the <code>ProjectManager</code> when the user attempts to publish a library that
 * has one or more dependencies on unmanaged (local) libraries.  The 'requestedPublications'
 * property provides the list of project items that were originally requested to be published.
 * The 'requiredPublications' list provies the list of items that must be included in a follow-
 * up request in order to successfully complete the publication.
 * 
 * @author S. Livezey
 */
public class PublishWithLocalDependenciesException extends Exception {
	
	private Collection<ProjectItem> requestedPublications;
	private Collection<ProjectItem> requiredPublications;
	
	/**
	 * Constructor that specifies an exception message, as well as the lists of requested and
	 * required libraries.
	 * 
	 * @param message  the detail message for the exception
	 * @param requestedPublications  the list of project items that were originally requested to be published
	 * @param requiredPublications  the list of items that must be included in a follow- up request in order to
	 *								successfully complete the publication
	 */
	public PublishWithLocalDependenciesException(String message,
			Collection<ProjectItem> requestedPublications, Collection<ProjectItem> requiredPublications) {
		super(message);
		this.requestedPublications = requestedPublications;
		this.requiredPublications = requiredPublications;
	}

	/**
	 * Returns the list of project items that were originally requested to be published.
	 *
	 * @return Collection<ProjectItem>
	 */
	public Collection<ProjectItem> getRequestedPublications() {
		return requestedPublications;
	}

	/**
	 * Returns the list of items that must be included in a follow- up request in order to
	 * successfully complete the publication.
	 *
	 * @return Collection<ProjectItem>
	 */
	public Collection<ProjectItem> getRequiredPublications() {
		return requiredPublications;
	}
	
}
