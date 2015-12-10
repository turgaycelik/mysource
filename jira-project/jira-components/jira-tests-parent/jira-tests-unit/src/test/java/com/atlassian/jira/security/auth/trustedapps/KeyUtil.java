package com.atlassian.jira.security.auth.trustedapps;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;

class KeyUtil
{
    static final Map<MapKey, KeyPair> keys = new HashMap<MapKey, KeyPair>();

    /**
     * utility to generate a new keypair for testing purposes
     */
    public static void main(final String[] args) throws Exception
    {
        final KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        System.out.println(KeyFactory.encode(keyPair.getPublic()));
        System.out.println(KeyFactory.encode(keyPair.getPrivate()));
    }

    static KeyPair generateNewKeyPair(final String keyPairAlgorythm)
    {
        return getKeyPair(keyPairAlgorythm, null, -1);
    }

    /*
     * Note that this returns the same key based on the keyPairAlgorithm/securityProvider/size
     */
    static KeyPair getKeyPair(final String keyPairAlgorythm, final String securityProvider, final int keySize)
    {
        // store the keys in a map as they take forever to generate
        final MapKey mapKey = new MapKey(keyPairAlgorythm, securityProvider, keySize);

        KeyPair pair = keys.get(mapKey);
        if (pair == null)
        {
            pair = generateKeyPair(keyPairAlgorythm, securityProvider, keySize);
            keys.put(mapKey, pair);
        }
        return pair;
    }

    static KeyPair generateKeyPair(final String keyPairAlgorythm, final String securityProvider, final int keySize)
    {
        KeyPair pair;
        KeyPairGenerator gen;
        try
        {
            if (securityProvider != null)
            {
                gen = KeyPairGenerator.getInstance(keyPairAlgorythm, securityProvider);
            }
            else
            {
                gen = KeyPairGenerator.getInstance(keyPairAlgorythm);
            }
        }
        catch (final NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        catch (final NoSuchProviderException e)
        {
            throw new RuntimeException(e);
        }
        if (keySize > 0)
        {
            gen.initialize(keySize);
        }
        pair = gen.generateKeyPair();
        return pair;
    }

    private static final class MapKey
    {
        private final String keyPairAlgorythm;
        private final String securityProvider;
        private final int keySize;

        MapKey(final String keyPairAlgorythm, final String securityProvider, final int keySize)
        {
            this.keyPairAlgorythm = keyPairAlgorythm;
            this.securityProvider = securityProvider;
            this.keySize = keySize;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass()))
            {
                return false;
            }

            final MapKey mapKey = (MapKey) o;
            if (keySize != mapKey.keySize)
            {
                return false;
            }
            if (!keyPairAlgorythm.equals(mapKey.keyPairAlgorythm))
            {
                return false;
            }
            if (securityProvider != null ? !securityProvider.equals(mapKey.securityProvider) : mapKey.securityProvider != null)
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int result;
            result = keyPairAlgorythm.hashCode();
            result = 31 * result + (securityProvider != null ? securityProvider.hashCode() : 0);
            result = 31 * result + keySize;
            return result;
        }
    }
}