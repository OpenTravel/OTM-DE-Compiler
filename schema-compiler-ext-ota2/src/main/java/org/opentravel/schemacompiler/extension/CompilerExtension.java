/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.extension;

/**
 * Defines the ID and ranking of an OTA2.0 compiler extension.
 * 
 * @author S. Livezey
 */
public final class CompilerExtension implements Comparable<CompilerExtension> {
	
	private String extensionId;
	private int rank;
	
	/**
	 * Construtor that creates a new compiler extension entry.
	 * 
	 * @param extensionId  the unique ID of the compiler extension
	 * @param rank  the rank of the extension (used for sorting and prioritization)
	 */
	public CompilerExtension(String extensionId, int rank) {
		this.extensionId = extensionId;
		this.rank = rank;
	}
	
	/**
	 * Returns the unique ID of the compiler extension.
	 *
	 * @return String
	 */
	public String getExtensionId() {
		return extensionId;
	}
	
	/**
	 * Returns the rank of the extension (used for sorting and prioritization).
	 *
	 * @return int
	 */
	public int getRank() {
		return rank;
	}
	
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CompilerExtension other) {
		int result;
		
		if (other != null) {
			if (this.rank == other.rank) {
				if (this.extensionId == null) {
					result = (other.extensionId == null) ? 0 : -1;
					
				} else {
					result = this.extensionId.compareTo( other.extensionId );
				}
			} else {
				result = (this.rank < other.rank) ? -1 : 1;
			}
		} else {
			result = 1;
		}
		return result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder( getClass().getSimpleName() ).append('[');
		
		str.append("extensionId=").append(extensionId);
		str.append(", rank=").append(rank);
		str.append(']');
		return str.toString();
	}
	
}
