package com.atlassian.jira.security.auth.trustedapps;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.beehive.simple.SimpleClusterLockService;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.EncryptedCertificate;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.UncheckedExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestDefaultCurrentApplicationStore
{
    public static final String PRIVATE_KEY_VALUE = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQCimjWtUnZ+ABlXM5QooZOcn/Qw4ZyZqMOeaq/GHEDcqiEThCWjEgkdoleRFDC/x+nst4weK4SBzVeYTnadRh5CbDNDTMjoutkjd39NefLH+b8vA4STU7F43C7sUPxe+proi2lYxRdsvGCE56VYfIEfYH9U0dvE1iEUi91MtMBtEBbhxKmoyG9NkFEcDBJmLYwRozYIuLS81xd1mEfVnyUNhbOfcLm+pG+vktCdlQZDcPBb7H7raFZCSjma8JvY3aby+iZu7A4Al0hlFVxLV/Qk+ac3XwQ8pZo79ua+D9K4e5ea22D4yELz9Tt2QxistjLsmaop1HEj5jqVMP0iNL/RAgMBAAECggEBAJ7z5Tkz/cWazyrhrxoxKZ3Y99u5EV9dDS4V2RLvl7CfV4o+ipRpEyQ3CSc395fjFBhmLcFohTZmyBSJz4Zbf0UrhMlrjJOF5LK5JGdWqTE4Qd7xr25eSIV/qRc27B7lGP3vGuK+ePHBDRGfaZGg1JEZwh/mrtIzCU+Okp1jrSBvJG/f/B3i3/4eotJkxJMQcfcET7kmcIuf2+kk9tSOQ4/S3z6zfcW2SX+S7wodXHYFno/s8jra2iA9oSR10zpo3z17kqkjFthy3DigC8kPUL+IFJ47eMhr77Sr4y7vSQgXhIzHTa/NDkRQv/FXFu8DuL8maNAwA4+MtCDrCDh9CckCgYEA2LQ4qxKNXcoS+Mx6Hhj+q/IMKOTJn4HEKmLsDb1oNAKlj7lv0jAq+kPVFPvH6zqqAsDK/dnIkQJKP3pWE86Sz95W7o2L6S70gIeEJjAPKOUNrvALwC2tAN+ecoL28MfYcRd1Ou4776m3t+74fDnoQwITUqagD88irjQxs/kJ82cCgYEAwBZ/NzeKWN45vSmmoAdxZQ96QQp+2Q+AUbFzE/W3wqBBeb9kdLnkuB7h9xIPUKU9SKRU053Tws7isp3i9kL4MNe2kNVz7nX2rO9xmCUiQB4nPHJyzBpEwISV7V3cBoVQVEwAMBkxrrv1fbO+15hUxF+floRkXyi6bE0uVjKBKAcCgYEAs+5wXxs4ZfezaQHfI759NyfzEMFm6BpHVypr9byfGRBZh8zhHBUCmmpYMuQcSySapU6PPdfmwQC1xZ8+eZO8MvzaEZlV4ngqa84Z29truvj1M5v9JcTHMUJ7vXkgsSw+eyiPHi9RdN3mvcKecAdk4r6GbMoNfRLwKNUNr/HIYWECgYEAgQ9cgDwH7ze4cZlHg+QcUzt397VWjv60R69DU5jX06Oa+nMWyrqPqwnM5xUyRwwOxfzrpZc4TX51oG8mrDFI0oBNALj6ALEvgPgMSZ5BifT4hcubx/iqUvOCTI6wo4z08P5zbd2vnOhaxA993RtTcPGSpy3ixnD1+IhO5sleMAcCgYADp0xdt8bS5uTtKU47h3DJx8ry1c71g2CLEevuTEyyynOLj6EZ+Hiluk97kGpHvaihZEtEPTtwbdx2fcY4MLmgOMrRzZKwZ5xJHxiSg0XeI5ZMDZaeSYMbRo+53JQ90LSGJM0o3pYLIilqiCKMxD1yFvqD5pUFUIuSDaSGRkID2A==";
    public static final String PUBLIC_KEY_VALUE = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCySptbugHAzWUJY3ALWhuSCPhVXnwbUBfsRExYQitBCVny4V1DcU2SAx22bH9dSM0X7NdMObF74r+Wd77QoPAtaySqFLqCeRCbFmhHgVSi+pGeCipTpueefSkz2AX8Aj+9x27tqjBsX1LtNWVLDsinEhBWN68R+iEOmf/6jGWObQIDAQAB";
    public static final KeyPair PAIR = getKeys(PUBLIC_KEY_VALUE, PRIVATE_KEY_VALUE);

    public static final String PRIVATE_KEY_VALUE2 = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCahHmjFeIVy8g80hODVwMeBazdzwscMQVZMQkY/nOkkR1yXr5q3ijeVc9G+fJTSW357ZlHThaajHlaCsB8mGvH3bVnmLjlEAQXPDYFJ7rAcALazPwrmOOeF8jyHhnra3E84My4lzrQKyX7tUOlrMPGuFUiqPaqwZ7R8bKCFBHzDCCjbpIkBCet9adTKb9bMTY+pX/VwEWCkDUUK6+Pilna+CfR7Eyu49ki3cvwpcJpzz2NNVUkdE+WH2jq/0DaltX4KozsxBQ5e0F4JJFow0mToEn7gVpy5jJ6lzpC8HDs7+sEvhsSg5xRrcqoXXBGnOaSFqnN0VA5l5VGFOZ82Vx3AgMBAAECggEAAiQLu2K2Oy8TmyQd+Rffhy708i3ILZ/QPygaxCniWElaaxc89CYX0tzFfmpAHT9LMsdD9GT0kzeXBtRXTmKeO6qqtMC87w+Fvvyqku+1+qySnY1xgPkhTjNuKUyZd1WJwBotO/F8cx3l7NLaGRWHsN0k8NH+eOct2CLNIlg8120yBPVD8qkBLB9aIbWFO6qKVppxmtElBcz64Cp9k0QwFeojY804Bk85PlZXtatc9rftx8rAH0mbR+HYrCjZeSCXbtXLiPpsGsb2zRX3sYfcxzTZ8ah4dYGJ4YETbTfniAc4e1nZxzDK3ZqHYfM6fwo+Nn3cjgX1m+vRpK4CxIMzAQKBgQDlzMqAVx1zfsUQbfIcYaHz/inJel+PFjnV1YfAjqLSdIruKY3zby92FQPxGMe+IzqtDxMndE+v8R1W5CXj79Bd9P1R2t+M/tyNKxoTQeBIpNDdHoHZHYzNRg4BXyqDgOC5MZg2iE5cUAkpvgB8iANiCVAxmZlG3I21ffZXv33iYQKBgQCsImbqWZoQV6QczIE9JHpk48l3+Z2oD0wIFaf0JZ+JEBcl8F+GffFpYWviu+9RgPjLNu90KXP4UTyOpE4wYJt0ynFa/itFeyoZyOCL047mIRmIWX944cFSTaQGnys2vry5jtM/mCThujOTP1YPTHz4fFlYqMvgO+5d8xJO38Nd1wKBgDRMVi29Jbu6NxkVHIuvAI+p4VK0IflLgqASt5FwJMgms9Od62tie3exMrN1A0hXdiwaiS5qfxQ4mIfE5oaodlBbyCcP296XxhPuokGNtYBy2gHoxn5mBjxVXCydvuwssXgUPGL0xHin0l4Ims28mqB+JB0VqteArFOItj6kGv8hAoGBAI47Pt6Vev/V6v0A3ikvYxLKRUalMugS+7gSbN0H2XLfoMDwhX4Tws5YeVS4BV9LOkhv9Bv/xJuCRI94pELFEK7IODH/orGeu4LiwOfIv2LYJsjLatDveVYQYWA3VPW/VSHeKh96D7+Uo9kflHS/tuN7nDfuAcgevS9UEeSqcDuxAoGALtpKwGf/9lNYpop/xDKgI19ZJi4oLtCUK1OPke6Wnv9+ZclNhsoxfxXDD/8C8q5D78oxSEEpr06A6sqsFxjzfdf0mSpo6qjmYnMITtill5TVzqkUPtOZBmxN/jj8nciho2+ghXtebNv11ERDTH9YuA5rmx1nqTse2NonoCzLoR4=";
    public static final String PUBLIC_KEY_VALUE2 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmoR5oxXiFcvIPNITg1cDHgWs3c8LHDEFWTEJGP5zpJEdcl6+at4o3lXPRvnyU0lt+e2ZR04Wmox5WgrAfJhrx921Z5i45RAEFzw2BSe6wHAC2sz8K5jjnhfI8h4Z62txPODMuJc60Csl+7VDpazDxrhVIqj2qsGe0fGyghQR8wwgo26SJAQnrfWnUym/WzE2PqV/1cBFgpA1FCuvj4pZ2vgn0exMruPZIt3L8KXCac89jTVVJHRPlh9o6v9A2pbV+CqM7MQUOXtBeCSRaMNJk6BJ+4FacuYyepc6QvBw7O/rBL4bEoOcUa3KqF1wRpzmkhapzdFQOZeVRhTmfNlcdwIDAQAB";
    public static final KeyPair PAIR2 = getKeys(PUBLIC_KEY_VALUE2, PRIVATE_KEY_VALUE2);

    public static final String TEST_UID = "jira.uid";

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreatesPropertiesIfNotThere() throws Exception
    {
        TrustedApplicationProperties properties = new TrustedApplicationProperties();

        DefaultCurrentApplicationStore factory = new DefaultCurrentApplicationStore(
                properties, stubServerId("THIS.ISNT.AREA.LSID"), new MemoryCacheManager(), new SimpleClusterLockService());

        factory.start();
        final CurrentApplication currentApplication = factory.getCurrentApplication();
        assertNotNull(currentApplication);

        KeyPair newPair = properties.getPair();
        assertNotNull(newPair.getPrivate());
        assertNotNull(newPair.getPublic());
        assertTrue(isNotBlank(properties.getApplicationId()));

        assertState(newPair, properties.getApplicationId(), factory);
    }

    private JiraLicenseService stubServerId(@Nullable final String serverId)
    {
        JiraLicenseService service = Mockito.mock(JiraLicenseService.class);
        Mockito.stub(service.getServerId()).toReturn(serverId);
        return service;
    }

    @Test
    public void testUsesExistingDodgyProperties() throws Exception
    {
        TrustedApplicationProperties applicationProperties =
                new TrustedApplicationProperties(TEST_UID, "CRAPPYNOTENCODEDKEYDATA", "CRAPPYNOTENCODEDKEYDATA");

        DefaultCurrentApplicationStore manager = new DefaultCurrentApplicationStore(
                new ReadOnlyApplicationProperties(applicationProperties), stubServerId("THIS.ISNT.AREA.LSID"), new MemoryCacheManager(), new SimpleClusterLockService());

        manager.start();
        final CurrentApplication currentApplication = manager.getCurrentApplication();
        assertNotNull(currentApplication);
        assertEquals(TEST_UID, currentApplication.getID());

        final PublicKey key = currentApplication.getPublicKey();
        assertNotNull(key);
        assertTrue(key instanceof KeyFactory.InvalidPublicKey);

        assertTrue(manager.getKeyPair().getPublic() instanceof KeyFactory.InvalidPublicKey);
        assertTrue(manager.getKeyPair().getPrivate() instanceof KeyFactory.InvalidPrivateKey);

        try
        {
            currentApplication.encode("this little sentence", null);
            fail("RuntimeException expected");
        }
        catch (UncheckedExecutionException yay)
        {
            // expected
        }
    }

    @Test
    public void testUsesExistingGoodProperties() throws Exception
    {
        TrustedApplicationProperties trustedApplicationProperties = new TrustedApplicationProperties(TEST_UID, PAIR);
        DefaultCurrentApplicationStore factory = new DefaultCurrentApplicationStore(
                new ReadOnlyApplicationProperties(trustedApplicationProperties), stubServerId("THIS.ISNT.AREA.LSID"),
                new MemoryCacheManager(), new SimpleClusterLockService());
        factory.start();
        assertState(PAIR, TEST_UID, factory);
    }

    @Test
    public void testUsesExistingPropertiesNoServerIdOrUid() throws Exception
    {
        TrustedApplicationProperties trustedApplicationProperties = new TrustedApplicationProperties(TEST_UID, PAIR);
        DefaultCurrentApplicationStore factory = new DefaultCurrentApplicationStore(
                trustedApplicationProperties, stubServerId(null), new MemoryCacheManager(), new SimpleClusterLockService());

        factory.start();
        //need to init the cache.
        factory.getCurrentApplication();

        assertTrue(isNotBlank(trustedApplicationProperties.getApplicationId()));
        assertState(PAIR, trustedApplicationProperties.getApplicationId(), factory);
    }

    @Test
    public void testSetCurrentApplication() throws Exception
    {
        String ORIG_ID = "orig";
        String NEW_ID = "new";

        TrustedApplicationProperties properties = new TrustedApplicationProperties(ORIG_ID, PAIR);
        DefaultCurrentApplicationStore store =
                new DefaultCurrentApplicationStore(properties, stubServerId("ABC"), new MemoryCacheManager(), new SimpleClusterLockService());

        store.start();
        assertState(PAIR, ORIG_ID, store);

        store.setCurrentApplication(NEW_ID, PAIR2);

        assertEquals(PAIR2.getPrivate(), properties.getPrivateKey());
        assertEquals(PAIR2.getPublic(), properties.getPublicKey());
        assertEquals(NEW_ID, properties.getApplicationId());

        assertState(PAIR2, NEW_ID, store);
    }

    @Test
    public void testSetCurrentApplicationString() throws Exception
    {
        String ORIG_ID = "orig";
        String NEW_ID = "new";

        TrustedApplicationProperties properties = new TrustedApplicationProperties(ORIG_ID, PAIR);
        DefaultCurrentApplicationStore store =
                new DefaultCurrentApplicationStore(properties, stubServerId("ABC"), new MemoryCacheManager(), new SimpleClusterLockService());

        store.start();
        assertState(PAIR, ORIG_ID, store);

        store.setCurrentApplication(NEW_ID, KeyFactory.encode(PAIR2.getPublic()), KeyFactory.encode(PAIR2.getPrivate()));

        assertEquals(PAIR2.getPrivate(), properties.getPrivateKey());
        assertEquals(PAIR2.getPublic(), properties.getPublicKey());
        assertEquals(NEW_ID, properties.getApplicationId());

        assertState(PAIR2, NEW_ID, store);
    }

    @Test
    public void testSetCurrentApplicationStringNoUid() throws Exception
    {
        String ORIG_ID = "orig";
        String NEW_ID = "new";

        TrustedApplicationProperties properties = new TrustedApplicationProperties(ORIG_ID, PAIR);
        DefaultCurrentApplicationStore store =
                new DefaultCurrentApplicationStore(properties, stubServerId("ABC"), new MemoryCacheManager(), new SimpleClusterLockService());

        store.start();
        assertState(PAIR, ORIG_ID, store);

        store.setCurrentApplication(NEW_ID, PUBLIC_KEY_VALUE2, PRIVATE_KEY_VALUE2);

        assertEquals(PAIR2.getPrivate(), properties.getPrivateKey());
        assertEquals(PAIR2.getPublic(), properties.getPublicKey());
        assertEquals(NEW_ID, properties.getApplicationId());

        assertState(PAIR2, NEW_ID, store);
    }

    @Test
    public void testSetCurrentApplicationNoUid() throws Exception
    {
        TrustedApplicationProperties properties = new TrustedApplicationProperties(TEST_UID, PAIR);
        DefaultCurrentApplicationStore store =
                new DefaultCurrentApplicationStore(properties, stubServerId("ABC"), new MemoryCacheManager(), new SimpleClusterLockService());

        store.start();
        //Need to init the cache.
        store.getCurrentApplication();
        assertState(PAIR, TEST_UID, store);

        try
        {
            store.setCurrentApplication(null, PAIR2);
            fail("Not Allowed.");
        }
        catch (IllegalArgumentException good)
        {
        }

        try
        {
            store.setCurrentApplication("", PAIR2);
            fail("Not Allowed.");
        }
        catch (IllegalArgumentException good)
        {
        }

        assertState(PAIR, TEST_UID, store);
    }

    @Test
    public void testSetCurrentApplicationNoUidString() throws Exception
    {
        TrustedApplicationProperties properties = new TrustedApplicationProperties(TEST_UID, PAIR);
        DefaultCurrentApplicationStore store =
                new DefaultCurrentApplicationStore(properties, stubServerId("ABC"), new MemoryCacheManager(), new SimpleClusterLockService());

        store.start();
        //Need to init the cache.
        store.getCurrentApplication();
        assertState(PAIR, TEST_UID, store);

        try
        {
            store.setCurrentApplication(null, PUBLIC_KEY_VALUE2, PRIVATE_KEY_VALUE2);
            fail("Not Allowed.");
        }
        catch (IllegalArgumentException good)
        {
        }

        try
        {
            store.setCurrentApplication("", PUBLIC_KEY_VALUE2, PRIVATE_KEY_VALUE2);
            fail("Not Allowed.");
        }
        catch (IllegalArgumentException good)
        {
        }

        assertState(PAIR, TEST_UID, store);
    }

    @Test
    public void testSetCurrentApplicationBadKeys() throws Exception
    {
        TrustedApplicationProperties properties = new TrustedApplicationProperties(TEST_UID, PAIR);
        DefaultCurrentApplicationStore store =
                new DefaultCurrentApplicationStore(properties, stubServerId("ABC"), new MemoryCacheManager(), new SimpleClusterLockService());

        store.start();
        //Need to init the cache.
        store.getCurrentApplication();
        assertState(PAIR, TEST_UID, store);

        try
        {
            store.setCurrentApplication("somethings", null);
            fail("Not Allowed.");
        }
        catch (IllegalArgumentException good)
        {
        }

        try
        {
            store.setCurrentApplication("somethings", new KeyPair(null, PAIR2.getPrivate()));
            fail("Not Allowed.");
        }
        catch (IllegalArgumentException good)
        {
        }

        try
        {
            store.setCurrentApplication("somethings", new KeyPair(PAIR2.getPublic(), null));
            fail("Not Allowed.");
        }
        catch (IllegalArgumentException good)
        {
        }

        assertState(PAIR, TEST_UID, store);
    }

    @Test
    public void testSetCurrentApplicationBadKeysString() throws Exception
    {
        TrustedApplicationProperties properties = new TrustedApplicationProperties(TEST_UID, PAIR);
        DefaultCurrentApplicationStore store =
                new DefaultCurrentApplicationStore(properties, stubServerId("ABC"), new MemoryCacheManager(), new SimpleClusterLockService());

        store.start();
        //Need to init the cache.
        store.getCurrentApplication();
        assertState(PAIR, TEST_UID, store);

        try
        {
            store.setCurrentApplication("somethings", null, null);
            fail("Not Allowed.");
        }
        catch (IllegalArgumentException good)
        {
        }

        try
        {
            store.setCurrentApplication("somethings", PUBLIC_KEY_VALUE2, null);
            fail("Not Allowed.");
        }
        catch (IllegalArgumentException good)
        {
        }

        try
        {
            store.setCurrentApplication("somethings", null, PRIVATE_KEY_VALUE2);
            fail("Not Allowed.");
        }
        catch (IllegalArgumentException good)
        {
        }

        try
        {
            store.setCurrentApplication("somethings", "AAAAAAA", PRIVATE_KEY_VALUE2);
            fail("Not Allowed.");
        }
        catch (IllegalArgumentException good)
        {
        }

        try
        {
            store.setCurrentApplication("somethings", PUBLIC_KEY_VALUE2, "BBBBBBBB");
            fail("Not Allowed.");
        }
        catch (IllegalArgumentException good)
        {
        }

        assertState(PAIR, TEST_UID, store);
    }

    private void assertState(KeyPair pair, String applicationId, CurrentApplicationStore applicationStore)
    {
        assertState(pair.getPublic(), pair.getPrivate(), applicationId, applicationStore);
    }

    private void assertState(final PublicKey publicKey, final PrivateKey privateKey, final String applicationId,
            final CurrentApplicationStore applicationStore)
    {
        CurrentApplication currentApplication = applicationStore.getCurrentApplication();
        assertEquals(applicationId, currentApplication.getID());
        assertEquals(publicKey, currentApplication.getPublicKey());

        KeyPair keyPair = applicationStore.getKeyPair();
        assertEquals(publicKey, keyPair.getPublic());
        assertEquals(privateKey, keyPair.getPrivate());

        // just make sure we have a key
        final EncryptedCertificate encoded = currentApplication.encode("this little sentence", null);
        assertNotNull(encoded.getCertificate());
        assertEquals(applicationId, encoded.getID());
        assertNotNull(encoded.getSecretKey());

    }

    private static KeyPair getKeys(String publicKeyString, String privateKeyString)
    {
        PrivateKey privateKey = KeyFactory.getPrivateKey(privateKeyString);
        PublicKey publicKey = KeyFactory.getPublicKey(publicKeyString);

        return new KeyPair(publicKey, privateKey);
    }

    private static class TrustedApplicationProperties implements ApplicationProperties
    {
        public static final String PROP_PUBLIC_KEY = "jira.trustedapp.key.public.data";
        public static final String PROP_UID = "jira.trustedapp.uid";
        public static final String PROP_PRIVATE_KEY = "jira.trustedapp.key.private.data";

        private String publicKey;
        private String privateKey;
        private String applicationId;

        private TrustedApplicationProperties()
        {
        }

        private TrustedApplicationProperties(String applicationId, KeyPair pairs)
        {
            this.applicationId = applicationId;
            this.privateKey = KeyFactory.encode(pairs.getPrivate());
            this.publicKey = KeyFactory.encode(pairs.getPublic());
        }

        private TrustedApplicationProperties(String applicationId, String publicKey, String privateKey)
        {
            this.applicationId = applicationId;
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        public String getText(String name)
        {
            if (name.equals(PROP_PUBLIC_KEY))
            {
                return publicKey;
            }
            else if (name.equals(PROP_PRIVATE_KEY))
            {
                return privateKey;
            }
            else
            {
                throw new IllegalArgumentException("Not a valid option: " + name);
            }
        }

        public String getDefaultBackedText(String name)
        {
            throw new RuntimeException("Not implemented.");
        }

        public void setText(String name, String value)
        {
            if (name.equals(PROP_PUBLIC_KEY))
            {
                this.publicKey = value;
            }
            else if (name.equals(PROP_PRIVATE_KEY))
            {
                this.privateKey = value;
            }
            else
            {
                throw new IllegalArgumentException("Not a valid option: " + name);
            }
        }

        public String getString(String name)
        {
            if (name.equals(PROP_UID))
            {
                return applicationId;
            }
            else
            {
                throw new IllegalArgumentException("Not a valid option: " + name);
            }

        }

        public Collection<String> getDefaultKeys()
        {
            throw new RuntimeException("Not implemented.");
        }

        public String getDefaultBackedString(String name)
        {
            throw new RuntimeException("Not implemented.");
        }

        public String getDefaultString(String name)
        {
            throw new RuntimeException("Not implemented.");
        }

        public void setString(String name, String value)
        {
            if (name.equals(PROP_UID))
            {
                this.applicationId = value;
            }
            else
            {
                throw new IllegalArgumentException("Not a valid option: " + name);
            }
        }

        public boolean getOption(String key)
        {
            throw new RuntimeException("Not implemented.");
        }

        public Collection<String> getKeys()
        {
            return asMap().keySet();
        }

        public void setOption(String key, boolean value)
        {
            throw new RuntimeException("Not implemented.");
        }

        public String getEncoding()
        {
            throw new RuntimeException("Not implemented.");
        }

        public String getMailEncoding()
        {
            throw new RuntimeException("Not implemented.");
        }

        public String getContentType()
        {
            throw new RuntimeException("Not implemented.");
        }

        public void refresh()
        {
            throw new RuntimeException("Not implemented.");
        }

        public Locale getDefaultLocale()
        {
            throw new RuntimeException("Not implemented.");
        }

        public Collection<String> getStringsWithPrefix(String prefix)
        {
            throw new RuntimeException("Not implemented.");
        }

        public Map<String, Object> asMap()
        {
            Map<String, Object> map = Maps.newHashMap();
            if (applicationId != null)
            {
                map.put(PROP_UID, applicationId);
            }
            if (privateKey != null)
            {
                map.put(PROP_PRIVATE_KEY, privateKey);
            }
            if (publicKey != null)
            {
                map.put(PROP_PUBLIC_KEY, publicKey);
            }
            return map;
        }

        public KeyPair getPair()
        {
            return new KeyPair(getPublicKey(), getPrivateKey());
        }

        public PublicKey getPublicKey()
        {
            return KeyFactory.getPublicKey(publicKey);
        }

        public PrivateKey getPrivateKey()
        {
            return KeyFactory.getPrivateKey(privateKey);
        }

        public String getApplicationId()
        {
            return applicationId;
        }
    }

    private static class ReadOnlyApplicationProperties implements ApplicationProperties
    {
        private final ApplicationProperties delegate;

        private ReadOnlyApplicationProperties(ApplicationProperties delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public String getText(String name)
        {
            return delegate.getText(name);
        }

        @Override
        public String getDefaultBackedText(String name)
        {
            return delegate.getDefaultBackedText(name);
        }

        @Override
        public void setText(String name, String value)
        {
            throw new IllegalStateException("Read-Only implementation.");
        }

        @Override
        public String getString(String name)
        {
            return delegate.getString(name);
        }

        @Override
        public Collection<String> getDefaultKeys()
        {
            return delegate.getDefaultKeys();
        }

        @Override
        public String getDefaultBackedString(String name)
        {
            return delegate.getDefaultBackedString(name);
        }

        @Override
        public String getDefaultString(String name)
        {
            return delegate.getDefaultString(name);
        }

        @Override
        public void setString(String name, String value)
        {
            throw new IllegalStateException("Read-Only implementation.");
        }

        @Override
        public boolean getOption(String key)
        {
            return delegate.getOption(key);
        }

        @Override
        public Collection<String> getKeys()
        {
            return delegate.getKeys();
        }

        @Override
        public void setOption(String key, boolean value)
        {
            throw new IllegalStateException("Read-Only implementation.");
        }

        @Override
        public String getEncoding()
        {
            return delegate.getEncoding();
        }

        @Override
        public String getMailEncoding()
        {
            return delegate.getMailEncoding();
        }

        @Override
        public String getContentType()
        {
            return delegate.getContentType();
        }

        @Override
        public void refresh()
        {
            throw new IllegalStateException("Read-Only implementation.");
        }

        @Override
        public Locale getDefaultLocale()
        {
            return delegate.getDefaultLocale();
        }

        @Override
        public Collection<String> getStringsWithPrefix(String prefix)
        {
            return delegate.getStringsWithPrefix(prefix);
        }

        @Override
        public Map<String, Object> asMap()
        {
            return delegate.asMap();
        }
    }
}
