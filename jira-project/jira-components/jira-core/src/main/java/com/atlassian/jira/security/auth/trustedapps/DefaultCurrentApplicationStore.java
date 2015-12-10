package com.atlassian.jira.security.auth.trustedapps;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.security.auth.trustedapps.CurrentApplication;
import com.atlassian.security.auth.trustedapps.DefaultCurrentApplication;
import com.atlassian.security.random.DefaultSecureRandomService;
import com.atlassian.security.random.SecureRandomService;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * @since v3.12
 */
@EventComponent
public class DefaultCurrentApplicationStore implements CurrentApplicationStore, Startable
{

    public static final String ACCESS_LOCK = DefaultCurrentApplicationStore.class.getName() + ".accessLock";

    private static final class Keys
    {

        private static final String PRIVATE_KEY_DATA = "jira.trustedapp.key.private.data";
        private static final String PUBLIC_KEY_DATA = "jira.trustedapp.key.public.data";
        private static final String UID = "jira.trustedapp.uid";
    }
    private final ApplicationProperties applicationProperties;

    private final CachedReference<Pair<KeyPair, CurrentApplication>> cache;
    private final JiraLicenseService licenseService;
    private final ClusterLockService clusterLockService;
    private ClusterLock accessLock;

    public DefaultCurrentApplicationStore(final ApplicationProperties applicationProperties,
            final JiraLicenseService jiraLicenseService, final CacheManager cacheManager, final ClusterLockService clusterLockService)
    {
        this.clusterLockService = clusterLockService;
        this.applicationProperties = notNull("applicationProperties", applicationProperties);
        this.licenseService = notNull("jiraLicenseService", jiraLicenseService);
        this.cache = cacheManager.getCachedReference(getClass(), "cache",
                new Supplier<Pair<KeyPair, CurrentApplication>>()
                {
                    @Override
                    public Pair<KeyPair, CurrentApplication> get()
                    {
                        return getOrCreateCurrentApplication();
                    }
                });
    }

    @Override
    public void start() throws Exception
    {
        accessLock = clusterLockService.getLockForName(ACCESS_LOCK);
    }

    @EventListener
    public void onClearCache(@SuppressWarnings("unused") final ClearCacheEvent event)
    {
        cache.reset();
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="UG_SYNC_SET_UNSYNC_GET", justification="This is a valid unsynchronized getter")
    public CurrentApplication getCurrentApplication()
    {
        return cache.get().second();
    }

    @Override
    public KeyPair getKeyPair()
    {
        return cache.get().first();
    }

    @Override
    public void setCurrentApplication(String applicationId, KeyPair pair)
    {
        notBlank("applicationId cannot be blank.", applicationId);
        Assertions.notNull("pair cannot be null.", pair);
        Assertions.notNull("pair.private cannot be null.", pair.getPrivate());
        Assertions.notNull("pair.public cannot be null.", pair.getPublic());

        accessLock.lock();
        try
        {
            applicationProperties.setText(Keys.PRIVATE_KEY_DATA, KeyFactory.encode(pair.getPrivate()));
            applicationProperties.setText(Keys.PUBLIC_KEY_DATA, KeyFactory.encode(pair.getPublic()));
            applicationProperties.setString(Keys.UID, applicationId);
            cache.reset();
        }
        finally
        {
            accessLock.unlock();
        }
    }

    @Override
    public void setCurrentApplication(String applicationId, String publicKey, String privateKey)
    {
        Assertions.notBlank("applicationId cannot be blank.", applicationId);
        Assertions.notNull("publicKey cannot be null.", publicKey);
        Assertions.notNull("privateKey cannot be null.", privateKey);

        PublicKey publicKeyObj = KeyFactory.getPublicKey(publicKey);
        if (publicKeyObj instanceof KeyFactory.InvalidPublicKey)
        {
            throw new IllegalArgumentException("publicKey is not a valid public key.", ((KeyFactory.InvalidKey) publicKeyObj).getCause());
        }

        PrivateKey privateKeyObj = KeyFactory.getPrivateKey(privateKey);
        if (privateKeyObj instanceof KeyFactory.InvalidPrivateKey)
        {
            throw new IllegalArgumentException("privateKey is not a valid private key.", ((KeyFactory.InvalidKey) privateKeyObj).getCause());
        }

        setCurrentApplication(applicationId, new KeyPair(publicKeyObj, privateKeyObj));
    }

    private Pair<KeyPair, CurrentApplication> getOrCreateCurrentApplication()
    {
        accessLock.lock();
        final KeyPair keyPair;
        String uid;
        try
        {
            final String privateKeyData = applicationProperties.getText(Keys.PRIVATE_KEY_DATA);
            final String publicKeyData = applicationProperties.getText(Keys.PUBLIC_KEY_DATA);

            if (isBlank(privateKeyData))
            {
                keyPair = generateNewKeyPair();
                applicationProperties.setText(Keys.PRIVATE_KEY_DATA, KeyFactory.encode(keyPair.getPrivate()));
                applicationProperties.setText(Keys.PUBLIC_KEY_DATA, KeyFactory.encode(keyPair.getPublic()));
            }
            else
            {
                PrivateKey privateKey = KeyFactory.getPrivateKey(privateKeyData);
                PublicKey publicKey = KeyFactory.getPublicKey(publicKeyData);

                keyPair = new KeyPair(publicKey, privateKey);
            }

            uid = applicationProperties.getString(Keys.UID);
            if (isBlank(uid))
            {
                uid = new UIDGenerator().generateUID(licenseService);
                applicationProperties.setString(Keys.UID, uid);
            }
        }
        finally
        {
            accessLock.unlock();
        }
        CurrentApplication application = new DefaultCurrentApplication(keyPair.getPublic(), keyPair.getPrivate(), uid);
        return Pair.of(keyPair,  application);
    }

    private static KeyPair generateNewKeyPair()
    {
        try
        {
            return KeyFactory.getEncryptionProvider().generateNewKeyPair();
        }
        ///CLOVER:OFF
        catch (final NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        catch (final NoSuchProviderException e)
        {
            throw new RuntimeException(e);
        }
        ///CLOVER:ON
    }

    private static class UIDGenerator
    {
        SecureRandomService secureRandom = DefaultSecureRandomService.getInstance();

        /**
         * Generate the UID. Note that this is dependent on there being a Server ID in the properties. If there isn't,
         * it'll use a random number. So the presence of the Server ID makes the return value stable and idempotent.
         * Without it it will always return something different each time.
         *
         * @param jiraLicenseService the JIRA license service
         * @return a uid.
         */
        String generateUID(final JiraLicenseService jiraLicenseService)
        {
            String serverId = jiraLicenseService.getServerId();
            // don't pass null into the md5 method
            serverId = (serverId != null) ? serverId : String.valueOf(secureRandom.nextLong());
            // Grab the first 4 bytes of the hashed SID and convert it to an integer
            final byte[] idHash = ArrayUtils.subarray(DigestUtils.md5(serverId), 0, 3);
            return "jira:" + new BigInteger(1, idHash).intValue();
        }
    }
}