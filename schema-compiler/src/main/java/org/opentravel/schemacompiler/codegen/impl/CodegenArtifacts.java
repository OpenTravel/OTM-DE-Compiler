/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Collection of generated artifacts that can be segregated by object type.
 * 
 * @author S. Livezey
 */
public class CodegenArtifacts {
	
	private List<Object> artifactList = new ArrayList<Object>();
	
	/**
	 * Adds an artifact to the current list.
	 * 
	 * @param artifact  the artifact to add (null is ignored without error)
	 */
	public void addArtifact(Object artifact) {
		if (artifact != null) {
			artifactList.add(artifact);
		}
	}
	
	/**
	 * Adds the contents of the given list of artifacts to this collection.
	 * 
	 * @param otherArtifacts  the other collection of artifacts to add (may be null)
	 */
	public void addAllArtifacts(CodegenArtifacts otherArtifacts) {
		if (otherArtifacts != null) {
			for (Object artifact : otherArtifacts.artifactList) {
				addArtifact(artifact);
			}
		}
	}
	
	/**
	 * Adds the contents of the given list of artifacts to this collection.
	 * 
	 * @param artifacet  the collection of artifacts to add (may be null)
	 */
	public void addAllArtifacts(Collection<?> artifacts) {
		if (artifacts != null) {
			for (Object artifact : artifacts) {
				addArtifact(artifact);
			}
		}
	}
	
	/**
	 * Returns the list of all artifacts that have been added.
	 * 
	 * @return List<Object>
	 */
	public List<Object> getAllArtifacts() {
		return Collections.unmodifiableList(artifactList);
	}
	
	/**
	 * Returns a list artifacts that match the requested type.
	 * 
	 * @param <A>  the type of artifact(s) to return
	 * @param artifactType  the type of artifact(s) to return
	 * @return List<A>
	 */
	@SuppressWarnings("unchecked")
	public <A> List<A> getArtifactsOfType(Class<A> artifactType) {
		List<A> aList = new ArrayList<A>();
		
		for (Object artifact : artifactList) {
			if ((artifact != null) && artifactType.isAssignableFrom(artifact.getClass())) {
				aList.add((A) artifact);
			}
		}
		return Collections.unmodifiableList(aList);
	}
	
}
