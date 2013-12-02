/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.version;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default version scheme implementation for OTA2.0 namespaces.
 * 
 * @author S. Livezey
 */
public class OTA2VersionScheme implements VersionScheme {
	
	private static final Pattern versionedNamespacePattern = Pattern.compile("(.*?)([/_-][vV])(\\d+)?(?:_(\\d+))?(?:_(\\d+))?");
	private static final Pattern versionPattern = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?");
	private static final Pattern prefixPattern = Pattern.compile("([A-Za-z0-9]+)-\\d*");
	
	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#getVersionIdentifier(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String getVersionIdentifier(String majorVersion, String minorVersion, String patchLevel) {
		StringBuilder versionId = new StringBuilder();
		String[] versionParts = { majorVersion, minorVersion, patchLevel };
		
		for (String versionPart : versionParts) {
			if (versionId.length() > 0) {
				versionId.append('.');
			}
			versionId.append( (versionPart == null) ? "0" : versionPart );
		}
		String versionIdentifier = versionId.toString();
		
		if (!isValidVersionIdentifier(versionIdentifier)) {
			throw new IllegalArgumentException("One or more of the version identifier's components is invalid.");
		}
		return versionIdentifier;
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#getVersionIdentifier(java.lang.String, java.lang.String)
	 */
	@Override
	public String getVersionIdentifier(String namespaceUri) {
		Matcher m = versionedNamespacePattern.matcher(namespaceUri);
		String versionIdentifier = null;
		
		if (m.matches()) {
			StringBuilder versionId = new StringBuilder();
			
			for (int i = 0; i < 3; i++) {
				String versionPart = "0";
				
				if (i < (m.groupCount() - 2)) {
					versionPart = m.group(i + 3);
				}
				if (versionId.length() > 0) {
					versionId.append('.');
				}
				if (versionPart != null) {
					versionId.append(Integer.parseInt(versionPart));
				} else {
					versionId.append('0');
				}
			}
			versionIdentifier = versionId.toString();
		} else {
			versionIdentifier = getDefaultVersionIdentifer();
		}
		return versionIdentifier;
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#setVersionIdentifier(java.lang.String, java.lang.String)
	 */
	@Override
	public String setVersionIdentifier(String namespaceUri, String versionIdentifier) {
		if (!isValidURI(namespaceUri)) {
			throw new IllegalArgumentException("Invalid namespace for version scheme: " + namespaceUri);
		}
		if (!isValidVersionIdentifier(versionIdentifier)) {
			throw new IllegalArgumentException("Invalid version identifier for version scheme: " + versionIdentifier);
		}
		Matcher m = versionedNamespacePattern.matcher(namespaceUri);
		boolean isPaddedVersionIds = m.matches() ? isPaddedIdentifier( m.group(3) ) : false;
		int[] versionParts = splitVersionIdentifier(versionIdentifier);
		String[] _versionParts = new String[versionParts.length];
		String baseNamespace = m.matches() ? m.group(1) : namespaceUri;
		String versionPrefix = m.matches() ? m.group(2) : "/v";
		StringBuilder namespace = new StringBuilder();
		
		// Add padding to the version components if it existed in the original namespace URI
		for (int i = 0; i < versionParts.length; i++) {
			_versionParts[i] =
					((isPaddedVersionIds && (versionParts[i] < 10)) ? "0" : "") + versionParts[i];
		}
		
		if (baseNamespace.endsWith("/")) {
			baseNamespace = baseNamespace.substring(0, baseNamespace.length() - 1);
		}
		namespace.append( baseNamespace ).append( versionPrefix ).append( _versionParts[0] );
		
		if ((versionParts[1] > 0) || (versionParts[2] > 0)) {
			namespace.append( '_' ).append( _versionParts[1] );
		}
		if (versionParts[2] > 0) { // only include patch in the namespace URI if greater than zero
			namespace.append( '_' ).append( _versionParts[2] );
		}
		return namespace.toString();
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#getDefaultVersionIdentifer()
	 */
	@Override
	public String getDefaultVersionIdentifer() {
		return "1.0.0";
	}

	/**
	 * Returns true if the given major version string (assumed to be obtained from a versioned namespace
	 * URI) contains a '0' character for padding in front of the actual version number.  This check was
	 * included for backwards-compatibility with some of the legacy identifiers that were in existence
	 * prior to finalizing this version scheme.
	 * 
	 * @param majorVersionStr  the major-version string that was extracted from the namespace URI
	 * @return boolean
	 */
	private boolean isPaddedIdentifier(String majorVersionStr) {
		return (majorVersionStr != null) && (majorVersionStr.length() == 2) && (majorVersionStr.charAt(0) == '0');
	}
	
	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#getPrefix(java.lang.String, java.lang.String)
	 */
	@Override
	public String getPrefix(String prefix, String versionIdentifier) {
		int[] versionParts = splitVersionIdentifier(versionIdentifier);
		String[] _versionParts = new String[versionParts.length];
		StringBuilder newPrefix = new StringBuilder();
		Matcher m;
		
		for (int i = 0; i < versionParts.length; i++) {
			_versionParts[i] = ((versionParts[i] < 10) ? "0" : "") + versionParts[i];
		}
		if ((m = prefixPattern.matcher(prefix)).matches()) {
			newPrefix.append( m.group(1) );
		} else {
			newPrefix.append( prefix );
		}
		newPrefix.append('-').append(_versionParts[0]).append(_versionParts[1]);
		
		if (versionParts[2] > 0) {
			newPrefix.append(_versionParts[2]);
		}
		return newPrefix.toString();
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#getBaseNamespace(java.lang.String)
	 */
	@Override
	public String getBaseNamespace(String namespaceUri) {
		if (!isValidNamespace(namespaceUri)) {
			throw new IllegalArgumentException("Invalid namespace URI: " + namespaceUri);
		}
		Matcher m = versionedNamespacePattern.matcher(namespaceUri);
		
		if (m.matches()) {
			return m.group(1);
		} else {
			throw new IllegalArgumentException("Version identifier not specified in namespace: " + namespaceUri);
		}
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#getMajorVersionNamespace(java.lang.String)
	 */
	@Override
	public String getMajorVersionNamespace(String namespaceUri) {
		String majorVersionNamespace = namespaceUri;
		
		if (isValidNamespace(namespaceUri)) {
			List<String> versionChain = getMajorVersionChain(namespaceUri);
			
			if (versionChain.size() > 0) {
				majorVersionNamespace = versionChain.get( versionChain.size() - 1 );
			} else {
				majorVersionNamespace = namespaceUri;
			}
		}
		return majorVersionNamespace;
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#getMajorVersion(java.lang.String)
	 */
	@Override
	public String getMajorVersion(String versionIdentifer) {
		return "" + splitVersionIdentifier(versionIdentifer)[0];
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#getMinorVersion(java.lang.String)
	 */
	@Override
	public String getMinorVersion(String versionIdentifer) {
		return "" + splitVersionIdentifier(versionIdentifer)[1];
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#getPatchLevel(java.lang.String)
	 */
	@Override
	public String getPatchLevel(String versionIdentifer) {
		return "" + splitVersionIdentifier(versionIdentifer)[2];
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#isValidNamespace(java.lang.String)
	 */
	@Override
	public boolean isValidNamespace(String namespaceUri) {
		return isValidURI(namespaceUri) && versionedNamespacePattern.matcher(namespaceUri).matches();
	}
	
	/**
	 * Returns true if the given URI is valid.
	 * 
	 * @param namespaceUri  the namespace URI to check
	 * @return boolean
	 */
	private boolean isValidURI(String namespaceUri) {
		boolean result = false;
		try {
			if (namespaceUri != null) {
				new URI(namespaceUri);
				result = true;
			}
		} catch (URISyntaxException e) {
			// Ignore error and return false
		}
		return result;
	}
	
	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#isValidVersionIdentifier(java.lang.String)
	 */
	@Override
	public boolean isValidVersionIdentifier(String versionIdentifier) {
		return (versionIdentifier != null) && versionPattern.matcher(versionIdentifier).matches();
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#isMajorVersion(java.lang.String)
	 */
	@Override
	public boolean isMajorVersion(String namespaceUri) {
		int[] versionParts = splitVersionIdentifier( getVersionIdentifier(namespaceUri) );
		return (versionParts[1] == 0) && (versionParts[2] == 0);
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#isMinorVersion(java.lang.String)
	 */
	@Override
	public boolean isMinorVersion(String namespaceUri) {
		int[] versionParts = splitVersionIdentifier( getVersionIdentifier(namespaceUri) );
		return (versionParts[1] > 0) && (versionParts[2] == 0);
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#isPatchVersion(java.lang.String)
	 */
	@Override
	public boolean isPatchVersion(String namespaceUri) {
		int[] versionParts = splitVersionIdentifier( getVersionIdentifier(namespaceUri) );
		return (versionParts[2] > 0);
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#getComparator(boolean)
	 */
	@Override
	public Comparator<Versioned> getComparator(boolean sortAscending) {
		return new OTA2VersionComparator(sortAscending);
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#incrementMajorVersion(java.lang.String)
	 */
	@Override
	public String incrementMajorVersion(String versionIdentifer) {
		int[] versionParts = splitVersionIdentifier(versionIdentifer);
		
		versionParts[0]++;
		versionParts[1] = 0;
		versionParts[2] = 0;
		return buildVersionIdentifier(versionParts);
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#decrementMajorVersion(java.lang.String)
	 */
	@Override
	public String decrementMajorVersion(String versionIdentifer) {
		int[] versionParts = splitVersionIdentifier(versionIdentifer);
		
		if (versionParts[0] > 0) {
			versionParts[0]--;
		}
		versionParts[1] = 0;
		versionParts[2] = 0;
		return buildVersionIdentifier(versionParts);
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#incrementMinorVersion(java.lang.String)
	 */
	@Override
	public String incrementMinorVersion(String versionIdentifer) {
		int[] versionParts = splitVersionIdentifier(versionIdentifer);
		
		versionParts[1]++;
		versionParts[2] = 0;
		return buildVersionIdentifier(versionParts);
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#decrementMinorVersion(java.lang.String)
	 */
	@Override
	public String decrementMinorVersion(String versionIdentifer) {
		int[] versionParts = splitVersionIdentifier(versionIdentifer);
		
		if (versionParts[1] > 0) {
			versionParts[1]--;
		}
		versionParts[2] = 0;
		return buildVersionIdentifier(versionParts);
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#incrementPatchLevel(java.lang.String)
	 */
	@Override
	public String incrementPatchLevel(String versionIdentifer) {
		int[] versionParts = splitVersionIdentifier(versionIdentifer);
		
		versionParts[2]++;
		return buildVersionIdentifier(versionParts);
	}
	
	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#decrementPatchLevel(java.lang.String)
	 */
	@Override
	public String decrementPatchLevel(String versionIdentifer) {
		int[] versionParts = splitVersionIdentifier(versionIdentifer);
		
		if (versionParts[2] > 0) {
			versionParts[2]--;
		}
		return buildVersionIdentifier(versionParts);
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#getMajorVersionChain(java.lang.String)
	 */
	@Override
	public List<String> getMajorVersionChain(String namespaceUri) {
		String versionIdentifier = getVersionIdentifier(namespaceUri);
		int[] versionParts = splitVersionIdentifier(versionIdentifier);
		List<String> versionChain = new ArrayList<String>();
		
		if (isValidNamespace(namespaceUri)) {
			do {
				String currentVersion = buildVersionIdentifier(versionParts);
				String ns = setVersionIdentifier(namespaceUri, currentVersion);
				
				versionChain.add( ns );
				
				if (versionParts[2] > 0) {
					versionParts[2]--;
				} else {
					versionParts[1]--;
				}
			} while (versionParts[1] >= 0);
			
		} else {
			versionChain.add( namespaceUri );
		}
		return versionChain;
	}

	/**
	 * @see com.sabre.schemacompiler.version.VersionScheme#getDefaultFileHint(java.lang.String, java.lang.String)
	 */
	@Override
	public String getDefaultFileHint(String namespaceUri, String libraryName) {
		return libraryName.replaceAll("\\s+", "_") + "_" + getVersionIdentifier(namespaceUri).replace('.', '_') + ".otm";
	}

	/**
	 * Splits the given version identifier according to its component separator characters.
	 * 
	 * @param versionIdentifier  the version identifer string to process
	 * @return int[]
	 */
	protected static int[] splitVersionIdentifier(String versionIdentifier) {
		Matcher m = versionPattern.matcher(versionIdentifier);
		int[] versionParts;
		
		if (m.matches()) {
			versionParts = new int[3];
			
			for (int i = 0; i < 3; i++) {
				String versionPart = m.group(i + 1);
				versionParts[i] = Integer.parseInt( (versionPart == null) ? "0" : versionPart );
			}
		} else {
			versionParts = new int[] { 0, 0, 0 };
		}
		return versionParts;
	}
	
	/**
	 * Constructs a version identifier string using the numeric values provided.
	 * 
	 * @param versionParts  the numeric identifier parts of the version
	 * @return String
	 */
	private String buildVersionIdentifier(int[] versionParts) {
		StringBuilder versionId = new StringBuilder();
		
		for (int versionPart : versionParts) {
			if (versionId.length() > 0) versionId.append('.');
			versionId.append(versionPart);
		}
		return versionId.toString();
	}
	
}
