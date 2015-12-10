package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.util.dbc.Null;
import com.atlassian.security.auth.trustedapps.BouncyCastleEncryptionProvider;
import com.atlassian.security.auth.trustedapps.EncryptionProvider;
import org.apache.commons.codec.binary.Base64;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Utility for encoding and decoding PublicKeys. Handles IllegalKeys by returning dummy key instances.
 *
 * @since v3.12
 */
public class KeyFactory
{
    /**
     * The EncryptionProvider used to provide public and private key creation methods.
     * Hard-coded to return BouncyCastleEncryptionProvider, as that is currently what Seraph uses by default.
     * @return an instance of BouncyCastleEncryptionProvider
     */
    public static EncryptionProvider getEncryptionProvider()
    {
        return new BouncyCastleEncryptionProvider();
    }

    /**
     * Get a PublicKey from a base64 encoded String. If there are problems creating the key,
     * a dummy key will be returned that cannot be used for decoding request data.
     *
     * @param keyStr the base64 encoded String that contains the public key
     * @return a PublicKey object, guaranteed not to be null. Will be an {@link com.atlassian.jira.security.auth.trustedapps.KeyFactory.InvalidPublicKey} if there are problems.
     */
    static PublicKey getPublicKey(String keyStr)
    {
        Null.not("keyStr", keyStr);
        final byte[] data = Base64.decodeBase64(keyStr.getBytes());
        try
        {
            return getEncryptionProvider().toPublicKey(data);
        }
        ///CLOVER:OFF
        catch (NoSuchProviderException e)
        {
            return new InvalidPublicKey(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            return new InvalidPublicKey(e);
        }
        ///CLOVER:ON
        catch (InvalidKeySpecException e)
        {
            return new InvalidPublicKey(e);
        }
    }

    /**
     * Get a PrivateKey from a base64 encoded String. If there are problems creating the key,
     * a dummy key will be returned that cannot be used for decoding request data.
     *
     * @param keyStr the base64 encoded String that contains the public key
     * @return a PublicKey object, guaranteed not to be null. Will be an {@link com.atlassian.jira.security.auth.trustedapps.KeyFactory.InvalidPublicKey} if there are problems.
     */
    static PrivateKey getPrivateKey(String keyStr)
    {
        Null.not("keyStr", keyStr);
        final byte[] data = Base64.decodeBase64(keyStr.getBytes());
        try
        {
            return getEncryptionProvider().toPrivateKey(data);
        }
        ///CLOVER:OFF
        catch (NoSuchProviderException e)
        {
            return new InvalidPrivateKey(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            return new InvalidPrivateKey(e);
        }
        ///CLOVER:ON
        catch (InvalidKeySpecException e)
        {
            return new InvalidPrivateKey(e);
        }
    }

    static String encode(Key key)
    {
        return new String(Base64.encodeBase64(key.getEncoded()));
    }

    /**
     * If there are problems creating a key, one of these will be returned instead.
     * Rather than returning the actual key, the toString() will return the causal exception.
     */
    public static class InvalidPrivateKey extends InvalidKey implements PrivateKey
    {
        public InvalidPrivateKey(Exception cause)
        {
            super(cause);
        }
    }

    /**
     * If there are problems creating a key, one of these will be returned instead.
     * Rather than returning the actual key, the toString() will return the causal exception.
     */
    public static class InvalidPublicKey extends InvalidKey implements PublicKey
    {
        public InvalidPublicKey(Exception cause)
        {
            super(cause);
        }
    }

    static class InvalidKey implements Key
    {
        private final Exception cause;

        public InvalidKey(Exception cause)
        {
            Null.not("cause", cause);
            this.cause = cause;
        }

        public String getAlgorithm()
        {
            return "";
        }

        ///CLOVER:OFF
        public String getFormat()
        {
            return "";
        }
        ///CLOVER:ON

        public byte[] getEncoded()
        {
            return new byte[0];
        }

        public String toString()
        {
            return "Invalid Key: " + cause.toString();
        }

        public Exception getCause()
        {
            return cause;
        }
    }

    ///CLOVER:OFF
    private KeyFactory()
    {
    }
    ///CLOVER:ON
}
