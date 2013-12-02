/*
 * Copyright (c) 2013, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package com.sabre.schemacompiler.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sabre.schemacompiler.security.impl.Base64;
import com.sabre.schemacompiler.security.impl.ByteChunk;
import com.sabre.schemacompiler.security.impl.CharChunk;
import com.sabre.schemacompiler.security.impl.HexUtils;

/**
 * Handles the validation of user credentials against the encrypted password provided
 * by a remote provider.
 * 
 * @author S. Livezey
 */
public class PasswordValidator {
	
	private static Log log = LogFactory.getLog(PasswordValidator.class);
	
	private MessageDigest messageDigest = null;
	private String digestEncoding = null;
	
	/**
	 * Constructs a password validator that utilizes the default encoding for password and
	 * credential strings.
	 * 
	 * @param digestAlgorithm  the message digest algorithm to employ when encrypting passwords
	 * @throws NoSuchAlgorithmException  thrown if the message digest algorithm is invalid
	 */
	public PasswordValidator(String digestAlgorithm) throws NoSuchAlgorithmException {
		this( digestAlgorithm, null );
	}
	
	/**
	 * Constructs a password validator that utilizes the specified encoding for password and
	 * credential strings.
	 * 
	 * @param digestAlgorithm  the message digest algorithm to employ when encrypting passwords
	 * @param digestEncoding  the encoding that will be applied when encrypting passwords
	 * @throws NoSuchAlgorithmException  thrown if the message digest algorithm is invalid
	 */
	public PasswordValidator(String digestAlgorithm, String digestEncoding) throws NoSuchAlgorithmException {
		messageDigest = MessageDigest.getInstance( digestAlgorithm );
	}
	
	/**
	 * Returns true if the user's credentials should be considered valid.
	 * 
	 * @param credentials  the user-provided plain text password to be evaluated
	 * @param userPassword  the encrypted password with which to compare the user-provided credentials
	 * @return boolean
	 */
	public boolean isValidPassword(String credentials, String userPassword) {
		boolean isValid = false;
		
		if (messageDigest != null) {
			if (userPassword.startsWith("{MD5}") || userPassword.startsWith("{SHA}")) {
				synchronized (messageDigest) {
					userPassword = userPassword.substring( 5 );
					messageDigest.reset();
					messageDigest.update (credentials.getBytes() );
					isValid = userPassword.equals( Base64.encode( messageDigest.digest() ) );
				}
			} else if (userPassword.startsWith("{SSHA}")) {
				synchronized (messageDigest) {
					userPassword = userPassword.substring(6);
					
					messageDigest.reset();
					messageDigest.update(credentials.getBytes());
					
					// Decode stored password.
					ByteChunk pwbc = new ByteChunk(userPassword.length());
					try {
						pwbc.append(userPassword.getBytes(), 0, userPassword.length());
						
					} catch (IOException e) {
						// Should never happen, but just in case...
						log.error("Could not append password bytes to chunk: ", e);
					}
					
					CharChunk decoded = new CharChunk();
					Base64.decode(pwbc, decoded);
					char[] pwarray = decoded.getBuffer();
					
					// Split decoded password into hash and salt.
					final int saltpos = 20;
					byte[] hash = new byte[saltpos];
					
					for (int i = 0; i < hash.length; i++) {
						hash[i] = (byte) pwarray[i];
					}
					
					byte[] salt = new byte[pwarray.length - saltpos];
					
					for (int i = 0; i < salt.length; i++) {
						salt[i] = (byte) pwarray[i + saltpos];
					}
					messageDigest.update(salt);
					isValid = Arrays.equals(messageDigest.digest(), hash);
				}
			} else {
				isValid = digest( credentials ).equalsIgnoreCase( userPassword );
			}
		} else {
			isValid = digest( credentials ).equals( userPassword );
		}
		return isValid;
	}
	
	/**
	 * Digest the password using the specified algorithm and convert the result to a corresponding
	 * hexadecimal string. If exception, the plain credentials string is returned.
	 * 
	 * @param credentials  the password or other credentials to digest
	 * @return String
	 */
	protected String digest(String credentials) {
		// If no MessageDigest instance is specified, return unchanged
		if (messageDigest == null) {
			return credentials;
		}
		
		// Digest the user credentials and return as hexadecimal
		synchronized (messageDigest) {
			try {
				messageDigest.reset();
				
				byte[] bytes = null;
				if (digestEncoding == null) {
					bytes = credentials.getBytes();
					
				} else {
					try {
						bytes = credentials.getBytes( digestEncoding );
						
					} catch (UnsupportedEncodingException uee) {
						log.error("Illegal digestEncoding: " + digestEncoding, uee);
						throw new IllegalArgumentException(uee.getMessage());
					}
				}
				messageDigest.update(bytes);
				
				return ( HexUtils.convert(messageDigest.digest()) );
				
			} catch (Exception e) {
				log.error("Error converting login credentials to hex format.", e);
				return (credentials);
			}
		}
		
	}
	
	/**
	 * Returns the message digest algorithm to employ when encrypting passwords.
	 * 
	 * @return String
	 */
	public String getDigestAlgorithm() {
		return messageDigest.getAlgorithm();
	}
	
	/**
	 * Returns the encoding that will be applied when encrypting passwords.
	 *
	 * @return String
	 */
	public String getDigestEncoding() {
		return digestEncoding;
	}

	/**
	 * Assigns the encoding that will be applied when encrypting passwords.
	 *
	 * @param digestEncoding  the encoding value to assign
	 */
	public void setDigestEncoding(String digestEncoding) {
		this.digestEncoding = digestEncoding;
	}
	
}
