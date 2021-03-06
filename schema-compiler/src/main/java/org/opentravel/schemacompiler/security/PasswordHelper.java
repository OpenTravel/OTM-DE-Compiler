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

package org.opentravel.schemacompiler.security;

import org.apache.commons.codec.binary.Base64;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

/**
 * Provides static utility methods for encrypting and testing passwords using a Unix/Linux-compatible encryption
 * algorithm.
 * 
 * @author S. Livezey
 */
public class PasswordHelper {

    protected static final String PUBLIC_KEYFILE = "/org/opentravel/schemacompiler/ota2-repository.pub";
    protected static final String PRIVATE_KEYFILE = "/org/opentravel/schemacompiler/ota2-repository.pri";
    protected static final String ENCRYPTION_ALGORITHM = "RSA";
    protected static final String CIPHER_TRANSFORMATION = ENCRYPTION_ALGORITHM + "/ECB/NoPadding";

    private static Cipher encryptionCipher;
    private static Cipher decryptionCipher;

    /**
     * Private constructor to prevent instantiation.
     */
    private PasswordHelper() {}

    /**
     * Encrypts the given plain-text password.
     * 
     * @param original the plain-text password to encrypt
     * @return String
     */
    public static final synchronized String encrypt(String original) {
        try {
            return Base64.encodeBase64String( encryptionCipher.doFinal( original.getBytes() ) );

        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException( "Unable to encrypt password.", e );
        }
    }

    /**
     * Decrypts the given encrypted password.
     * 
     * @param encryptedPassword the encrypted password to decrypt
     * @return String
     */
    public static final synchronized String decrypt(String encryptedPassword) {
        try {
            return new String( decryptionCipher.doFinal( Base64.decodeBase64( encryptedPassword ) ) ).trim();

        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException( "Unable to decrypt password.", e );
        }
    }

    /**
     * Returns true if the plain-text test password matches the encrypted one provided.
     * 
     * @param testPassword the plain-text password to test against the encrypted one
     * @param encryptedPassword the encrypted password to test
     * @return boolean
     */
    public static boolean isMatch(String testPassword, String encryptedPassword) {
        boolean result = false;

        if ((testPassword != null) && (encryptedPassword != null)) {
            result = testPassword.equals( decrypt( encryptedPassword ) );
        }
        return result;
    }

    /**
     * Returns an encryption cipher that is based on the public encryption key file located on the application's
     * classpath.
     * 
     * @return Cipher
     * @throws GeneralSecurityException thrown if encryption key is not valid
     * @throws IOException thrown if the contents of the public key file cannot be loaded
     */
    private static Cipher loadEncryptionCipher() throws GeneralSecurityException, IOException {
        BigInteger[] keyComponents = loadKeyFile( PUBLIC_KEYFILE );
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec( keyComponents[0], keyComponents[1] );
        KeyFactory factory = KeyFactory.getInstance( ENCRYPTION_ALGORITHM );
        PublicKey publicKey = factory.generatePublic( keySpec );
        Cipher cipher = Cipher.getInstance( CIPHER_TRANSFORMATION );

        cipher.init( Cipher.PUBLIC_KEY, publicKey );
        return cipher;
    }

    /**
     * Returns an decryption cipher that is based on the private encryption key file located on the application's
     * classpath.
     * 
     * @return Cipher
     * @throws GeneralSecurityException thrown if encryption key is not valid
     * @throws IOException thrown if the contents of the private key file cannot be loaded
     */
    private static Cipher loadDecryptionCipher() throws GeneralSecurityException, IOException {
        BigInteger[] keyComponents = loadKeyFile( PRIVATE_KEYFILE );
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec( keyComponents[0], keyComponents[1] );
        KeyFactory factory = KeyFactory.getInstance( ENCRYPTION_ALGORITHM );
        PrivateKey privateKey = factory.generatePrivate( keySpec );
        Cipher cipher = Cipher.getInstance( CIPHER_TRANSFORMATION );

        cipher.init( Cipher.PRIVATE_KEY, privateKey );
        return cipher;
    }

    /**
     * Loads the contents of the key file from the specified location on the classpath.
     * 
     * @param fileLocation the classpath location of the file to load
     * @return BigInteger[] (modulus, exponent)
     * @throws IOException thrown if the key file cannot be loaded
     */
    private static BigInteger[] loadKeyFile(String fileLocation) throws IOException {
        try (BufferedReader reader =
            new BufferedReader( new InputStreamReader( CompilerExtensionRegistry.loadResource( fileLocation ) ) )) {
            String modBase64 = reader.readLine();
            String expBase64 = reader.readLine();
            BigInteger modulus = new BigInteger( Base64.decodeBase64( modBase64 ) );
            BigInteger exponent = new BigInteger( Base64.decodeBase64( expBase64 ) );

            return new BigInteger[] {modulus, exponent};
        }
    }

    /**
     * Initializes the public and private keys used by the encryption algorithm.
     */
    static {
        try {
            encryptionCipher = loadEncryptionCipher();
            decryptionCipher = loadDecryptionCipher();

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
