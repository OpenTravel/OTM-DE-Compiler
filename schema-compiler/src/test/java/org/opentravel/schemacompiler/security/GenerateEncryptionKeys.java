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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.opentravel.schemacompiler.security.PasswordHelper;

/**
 * Generates a new public and private key pair to be used for password encryption and decryption.
 * 
 * <p>
 * NOTE: This code has been included for completeness, but generation of new keys should not be done
 * except in extreme circumstances. The effect of replacing the existing public/private key files is
 * that current user credentials used for remote repository access will become unreadable. If this
 * occurs, users will need to re-enter their passwords for all repositories to which they have non-
 * anonymous access.
 * 
 * @author S. Livezey
 */
public class GenerateEncryptionKeys {

    private static final int ENCRYPTION_KEY_SIZE = 1024;

    /**
     * Generates the public and private key files used for encrypting and decrypting passwords.
     */
    public static void generateKeyFiles() throws Exception {
        File publicKeyFile = new File(System.getProperty("user.dir"), "/src/main/resources"
                + PasswordHelper.PUBLIC_KEYFILE);
        File privateKeyFile = new File(System.getProperty("user.dir"), "/src/main/resources"
                + PasswordHelper.PRIVATE_KEYFILE);
        KeyPairGenerator keyGenerator = KeyPairGenerator
                .getInstance(PasswordHelper.ENCRYPTION_ALGORITHM);

        keyGenerator.initialize(ENCRYPTION_KEY_SIZE);

        KeyPair keyPair = keyGenerator.generateKeyPair();
        KeyFactory fact = KeyFactory.getInstance(PasswordHelper.ENCRYPTION_ALGORITHM);
        RSAPublicKeySpec publicKeySpec = fact.getKeySpec(keyPair.getPublic(),
                RSAPublicKeySpec.class);
        RSAPrivateKeySpec privateKeySpec = fact.getKeySpec(keyPair.getPrivate(),
                RSAPrivateKeySpec.class);

        System.out.println("Public Key : " + publicKeySpec.getModulus() + " / "
                + publicKeySpec.getPublicExponent());
        System.out.println("Private Key: " + privateKeySpec.getModulus() + " / "
                + privateKeySpec.getPrivateExponent());

        writeKeyFile(publicKeyFile, publicKeySpec.getModulus(), publicKeySpec.getPublicExponent());
        writeKeyFile(privateKeyFile, privateKeySpec.getModulus(),
                privateKeySpec.getPrivateExponent());
    }

    /**
     * Saves the content of the given byte array to the indicated file location.
     * 
     * @param file
     *            the file to which the byte contents should be written
     * @param modulus
     *            the modulus of the encryption key
     * @param exponent
     *            the exponent of the encryption key
     * @throws IOException
     *             thrown if the file cannot be created
     */
    private static void writeKeyFile(File file, BigInteger modulus, BigInteger exponent)
            throws IOException {
        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream(file));
            out.println(Base64.encodeBase64String(modulus.toByteArray()));
            out.println(Base64.encodeBase64String(exponent.toByteArray()));

        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Throwable t) {
            }
        }
    }

    /**
     * Main method invoked by the Java command-line.
     * 
     * @param args
     *            the command-line parameters
     */
    public static void main(String[] args) {
        try {
            generateKeyFiles();
            System.out.println("Public/private key files generated successfully.");

        } catch (Throwable t) {
            t.printStackTrace(System.out);
        }
    }

}
