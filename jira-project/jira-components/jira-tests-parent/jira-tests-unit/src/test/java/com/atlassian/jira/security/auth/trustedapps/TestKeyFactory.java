package com.atlassian.jira.security.auth.trustedapps;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestKeyFactory
{
    @Test
    public void testEmptyPublicKey()
    {
        PublicKey key = KeyFactory.getPublicKey("");
        assertNotNull(key);
        assertTrue(key instanceof KeyFactory.InvalidPublicKey);
        assertTrue(key.toString(), key.toString().indexOf("InvalidKeySpec") > -1);
        assertNotNull(key.getAlgorithm());
        assertNotNull(key.getEncoded());
    }

    @Test
    public void testGarbledPublicKey()
    {
        // this key is one character off being good
        PublicKey key = KeyFactory.getPublicKey("MiGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCySptbugHAzWUJY3ALWhuSCPhVXnwbUBfsRExYQitBCVny4V1DcU2SAx22bH9dSM0X7NdMObF74r+Wd77QoPAtaySqFLqCeRCbFmhHgVSi+pGeCipTpueefSkz2AX8Aj+9x27tqjBsX1LtNWVLDsinEhBWN68R+iEOmf/6jGWObQIDAQAB");
        assertNotNull(key);
        assertTrue(key instanceof KeyFactory.InvalidPublicKey);
        assertTrue(key.toString(), key.toString().indexOf("InvalidKeySpec") > -1);
        assertNotNull(key.getAlgorithm());
        assertNotNull(key.getEncoded());
    }

    @Test
    public void testGoodPublicKey()
    {
        PublicKey key = KeyFactory.getPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCySptbugHAzWUJY3ALWhuSCPhVXnwbUBfsRExYQitBCVny4V1DcU2SAx22bH9dSM0X7NdMObF74r+Wd77QoPAtaySqFLqCeRCbFmhHgVSi+pGeCipTpueefSkz2AX8Aj+9x27tqjBsX1LtNWVLDsinEhBWN68R+iEOmf/6jGWObQIDAQAB");
        assertNotNull(key);
        assertFalse(key instanceof KeyFactory.InvalidPublicKey);
    }

    @Test
    public void testNullPublicKey()
    {
        try
        {
            KeyFactory.getPublicKey(null);
            fail("IAE expected");
        }
        catch (IllegalArgumentException yay)
        {
            // expected
        }
    }

    // ---------------------------------------------------------------------------------------------------- private keys

    @Test
    public void testEmptyPrivateKey()
    {
        PrivateKey key = KeyFactory.getPrivateKey("");
        assertNotNull(key);
        assertTrue(key instanceof KeyFactory.InvalidPrivateKey);
        assertTrue(key.toString(), key.toString().indexOf("InvalidKeySpec") > -1);
        assertNotNull(key.getAlgorithm());
        assertNotNull(key.getEncoded());
    }

    @Test
    public void testGarbledPrivateKey()
    {
        // this key is one character off being good
        PrivateKey key = KeyFactory.getPrivateKey("MiGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCySptbugHAzWUJY3ALWhuSCPhVXnwbUBfsRExYQitBCVny4V1DcU2SAx22bH9dSM0X7NdMObF74r+Wd77QoPAtaySqFLqCeRCbFmhHgVSi+pGeCipTpueefSkz2AX8Aj+9x27tqjBsX1LtNWVLDsinEhBWN68R+iEOmf/6jGWObQIDAQAB");
        assertNotNull(key);
        assertTrue(key instanceof KeyFactory.InvalidPrivateKey);
        assertTrue(key.toString(), key.toString().indexOf("InvalidKeySpec") > -1);
        assertNotNull(key.getAlgorithm());
        assertNotNull(key.getEncoded());
    }

    @Test
    public void testGoodPrivateKey()
    {
        PrivateKey key = KeyFactory.getPrivateKey("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALJKm1u6AcDNZQljcAtaG5II+FVefBtQF+xETFhCK0EJWfLhXUNxTZIDHbZsf11IzRfs10w5sXviv5Z3vtCg8C1rJKoUuoJ5EJsWaEeBVKL6kZ4KKlOm5559KTPYBfwCP73Hbu2qMGxfUu01ZUsOyKcSEFY3rxH6IQ6Z//qMZY5tAgMBAAECgYB4QXJAkFmWXfOEPZnZTlHCUmKN0kkLcx5vsjF8ZkUefNw6wl9Rmh6kGY30+YF+vhf3xzwAoflggjSPnP0LY0Ibf0XxMcNjR1zBsl9X7gKfXghIunS6gbcwrEwBNc5GR4zkYjYaZQ4zVvm3oMS2glV9NlXAUl41VL2XAQC/ENwbUQJBAOdoAz4hZGgke9AxoKLZh215gY+PLXqVLlWf14Ypk70Efk/bVvF10EsAOuAm9queCyr0qNf/vgHrm4HHXwJz4SsCQQDFPXir5qs+Kf2Y0KQ+WO5IRaNmrOlNvWDqJP/tDGfF/TYo6nSI0dGtWNfwZyDB47PbUq3zxCHYjExBJ9vQNZLHAkEA4JlCtHYCl1X52jug1w7c9DN/vc/Q626J909aB3ypSUdoNagFPf0EexcxDcijmDSgUEQA8Qzm5cRBPfg9Tgsc2wJBAIKbiv2hmEFowtHfTvMuJlNbMbF6zF67CaLib0oEDe+QFb4QSqyS69py20MItytM4btYy3GArbzcYl4+y5La9t8CQE2BkMV3MLcpAKjxtK5SYwCyLT591k35isGxmIlSQBQbDmGP9L5ZeXmVGVxRCGbBQjCzeoafPvUZo65kaRQHUJc=");
        assertNotNull(key);
        assertFalse(key instanceof KeyFactory.InvalidPrivateKey);
    }

    @Test
    public void testNullPrivateKey()
    {
        try
        {
            KeyFactory.getPrivateKey(null);
            fail("IAE expected");
        }
        catch (IllegalArgumentException yay)
        {
            // expected
        }
    }
}
