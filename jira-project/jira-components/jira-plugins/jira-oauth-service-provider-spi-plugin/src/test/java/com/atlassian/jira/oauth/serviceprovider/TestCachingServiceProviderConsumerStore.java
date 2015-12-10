package com.atlassian.jira.oauth.serviceprovider;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.util.RSAKeys;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestCachingServiceProviderConsumerStore
{
    private CachingServiceProviderConsumerStore store;
    @Mock private ServiceProviderConsumerStore mockConsumerStore;
    @Mock private EventPublisher mockEventPublisher;

    @Before
    public void setUp() throws Exception
    {
        store = new CachingServiceProviderConsumerStore(mockConsumerStore, mockEventPublisher, new MemoryCacheManager());
    }

    @Test
    public void testPut() throws NoSuchAlgorithmException
    {
        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").publicKey(key).build();
        Consumer consumer2 = Consumer.key("refimpl").name("iGoogle").publicKey(key).build();

        //now only 1 call for get should go through to the delegate store.  All other calls should hit the cache!
        when(mockConsumerStore.get("www.google.com")).thenReturn(consumer);
        when(mockConsumerStore.get("refimpl")).thenReturn(consumer2);
        when(mockConsumerStore.get("refimpl")).thenReturn(consumer2);

        store.put(consumer);
        store.get("www.google.com");
        store.get("www.google.com");
        store.get("www.google.com");
        store.get("www.google.com");

        //now check put invalidates cache
        store.put(consumer2);
        store.get("refimpl");
        store.get("refimpl");
        store.put(consumer2);
        store.get("refimpl");
        store.get("refimpl");

        verify(mockConsumerStore).get("www.google.com");
        verify(mockConsumerStore, times(2)).get("refimpl");
        verify(mockConsumerStore).put(consumer);
        verify(mockConsumerStore, times(2)).put(consumer2);
    }

    @Test
    public void testGetAllNotCached() throws NoSuchAlgorithmException
    {
        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").publicKey(key).build();

        List<Consumer> list = new ArrayList<Consumer>();
        list.add(consumer);
        Iterable<Consumer> result = new ArrayList<Consumer>(list);


        //now only 1 call for get should go through to the delegate store.  All other calls should hit the cache!
        when(mockConsumerStore.getAll()).thenReturn(result);

        store.getAll();
        store.getAll();
        store.getAll();

        verify(mockConsumerStore, times(3)).getAll();
    }

    @Test
    public void testRemoveNotImplemented() throws NoSuchAlgorithmException
    {
        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").publicKey(key).build();

        mockConsumerStore.put(consumer);
        //currently the call for get will result in another call to the delegate store to get the consumer.
        //what should really happen is that a call to remove is made to the delegate store first!
        when(mockConsumerStore.get("www.google.com"))
                .thenReturn(consumer)
                .thenReturn(null);

        store.put(consumer);
        Consumer updatedConsumer = store.get("www.google.com");
        assertNotNull(updatedConsumer);
        updatedConsumer = store.get("www.google.com");
        assertNotNull(updatedConsumer);
        updatedConsumer = store.get("www.google.com");
        assertNotNull(updatedConsumer);
        updatedConsumer = store.get("www.google.com");
        assertNotNull(updatedConsumer);

        store.remove("www.google.com");
        Consumer removedConsumer = store.get("www.google.com");
        assertNull(removedConsumer);

        verify(mockConsumerStore).remove("www.google.com");
        verify(mockConsumerStore, times(2)).get("www.google.com");
    }
}
