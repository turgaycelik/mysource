package com.atlassian.jira.oauth.consumer;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Iterator;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.core.ConsumerServiceStore;
import com.atlassian.oauth.util.RSAKeys;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestCachingConsumerStore
{
    private static final String VALID_SERVICE_NAME = "myGoogleService";
    private static final String VALID_SERVICE_KEY = "www.google.com";
    private static final String INVALID_SERVICE_NAME = "nonexistent";
    private static final String INVALID_SERVICE_KEY = "somenonexistentkey!";

    @Mock
    private ConsumerServiceStore mockDelegate;

    @Mock
    private EventPublisher mockEventPublisher;

    private CachingConsumerStore cachingStore;

    @Before
    public void setUp()
    {
        this.cachingStore = new CachingConsumerStore(mockDelegate, mockEventPublisher, new MemoryCacheManager());
    }

    @Test(expected = RuntimeException.class)
    public void gettingWithNullKeyShouldThrowRuntimeException() throws NoSuchAlgorithmException
    {
        cachingStore.get(null);
    }

    @Test
    public void testGet() throws NoSuchAlgorithmException
    {
        // Set up
        final Consumer consumer = Consumer.key(VALID_SERVICE_KEY)
                .name("iGoogle")
                .description("Google home page")
                .callback(URI.create("http://www.google.com"))
                .signatureMethod(Consumer.SignatureMethod.HMAC_SHA1)
                .build();

        final ConsumerServiceStore.ConsumerAndSecret cas =
                new ConsumerServiceStore.ConsumerAndSecret(VALID_SERVICE_NAME, consumer, "shared secret string");

        when(mockDelegate.get(INVALID_SERVICE_NAME)).thenReturn(null, (ConsumerServiceStore.ConsumerAndSecret) null);
        when(mockDelegate.get(VALID_SERVICE_NAME)).thenReturn(cas);
        when(mockDelegate.getByKey(INVALID_SERVICE_KEY)).thenReturn(null);
        when(mockDelegate.getByKey(VALID_SERVICE_KEY)).thenReturn(cas);

        // Invoke and check
        assertNull(cachingStore.get(INVALID_SERVICE_NAME));
        assertNull(cachingStore.get(INVALID_SERVICE_NAME));
        assertCasEquals(consumer, cachingStore.get(VALID_SERVICE_NAME));
        assertCasEquals(consumer, cachingStore.get(VALID_SERVICE_NAME));
        assertCasEquals(consumer, cachingStore.getByKey(VALID_SERVICE_KEY));
        assertCasEquals(consumer, cachingStore.getByKey(VALID_SERVICE_KEY));
        assertNull(cachingStore.getByKey(INVALID_SERVICE_KEY));

        // Check
        verify(mockDelegate).get(INVALID_SERVICE_NAME);   // misses are cached
        verify(mockDelegate).getByKey(INVALID_SERVICE_KEY);   // misses are cached
        verify(mockDelegate).get(VALID_SERVICE_NAME);     // hits are of course cached
        verify(mockDelegate).getByKey(VALID_SERVICE_KEY);     // hits are of course cached
        verifyNoMoreInteractions(mockDelegate);
    }

    @Test
    public void testGetAll() throws Exception
    {
        // Set up
        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        final Consumer consumer = Consumer.key(VALID_SERVICE_KEY)
                .name("iGoogle")
                .description("Google home page")
                .callback(URI.create("http://www.google.com"))
                .signatureMethod(Consumer.SignatureMethod.RSA_SHA1)
                .publicKey(key).build();
        final Consumer consumer2 = Consumer.key("refimpl")
                .name("Refimpl")
                .signatureMethod(Consumer.SignatureMethod.RSA_SHA1)
                .publicKey(key).build();
        when(mockDelegate.getAllServiceProviders()).thenReturn(Arrays.asList(consumer, consumer2));
        final CachingConsumerStore cachingStore =
                new CachingConsumerStore(mockDelegate, mockEventPublisher, new MemoryCacheManager());

        // Invoke
        final Iterator<Consumer> providers = cachingStore.getAllServiceProviders().iterator();

        // Check
        assertConsumersEqual(consumer, providers.next());
        assertConsumersEqual(consumer2, providers.next());
        assertFalse("Expected only two entries", providers.hasNext());
    }

    private void assertConsumersEqual(final Consumer expected, final Consumer result)
    {
        assertEquals(expected.getKey(), result.getKey());
        assertEquals(expected.getDescription(), result.getDescription());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getCallback(), result.getCallback());
        assertEquals(expected.getPublicKey(), result.getPublicKey());
    }

    private void assertCasEquals(final Consumer consumer, final ConsumerServiceStore.ConsumerAndSecret returnedCas)
    {
        assertConsumersEqual(consumer, returnedCas.getConsumer());
        assertEquals("shared secret string", returnedCas.getSharedSecret());
        assertNull(returnedCas.getPrivateKey());
    }
}
