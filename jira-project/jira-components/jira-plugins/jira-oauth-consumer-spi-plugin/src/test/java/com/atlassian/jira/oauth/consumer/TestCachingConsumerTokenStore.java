package com.atlassian.jira.oauth.consumer;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.Token;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.oauth.consumer.ConsumerTokenStore;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.Principal;
import java.util.Map;

@RunWith (MockitoJUnitRunner.class)
public class TestCachingConsumerTokenStore extends TestCase
{

    @Mock
    private EventPublisher mockEventPublisher;

    @Test
    public void testGet()
    {
        final ConsumerTokenStore delegateStore = createMock(ConsumerTokenStore.class);
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        Principal user = new Principal()
        {
            public String getName()
            {
                return "admin";
            }
        };
        final Map<String, String> props = MapBuilder.<String, String>newBuilder().add("prop1", "val1").add("prop2", "val2").toMap();

        ConsumerToken requestToken = ConsumerToken.newRequestToken("mytoken")
                .tokenSecret("ssh...it's secret")
                .consumer(consumer)
                .properties(props)
                .build();

        expect(delegateStore.get(new ConsumerTokenStore.Key("nonexistentkey"))).andReturn(null).once();
        expect(delegateStore.get(new ConsumerTokenStore.Key("mytoken"))).andReturn(requestToken).once();

        replay(delegateStore);
        CachingConsumerTokenStore store = new CachingConsumerTokenStore(delegateStore, mockEventPublisher, new MemoryCacheManager());

        try
        {
            store.get(null);
            fail("Should have thrown exception!");
        }
        catch (RuntimeException e)
        {
            //yay!
        }

        final Token token = store.get(new ConsumerTokenStore.Key("nonexistentkey"));
        assertNull(token);

        Token goodToken = store.get(new ConsumerTokenStore.Key("mytoken"));
        assertTokenEquals(requestToken, goodToken);
        //request it a few more times.  Should hit the chache.  THe mock should guarantee that we only hit the
        //delegate store once!
        goodToken = store.get(new ConsumerTokenStore.Key("mytoken"));
        assertTokenEquals(requestToken, goodToken);
        goodToken = store.get(new ConsumerTokenStore.Key("mytoken"));
        assertTokenEquals(requestToken, goodToken);
        goodToken = store.get(new ConsumerTokenStore.Key("mytoken"));
        assertTokenEquals(requestToken, goodToken);

        verify(delegateStore);
    }

    @Test
    public void testPut()
    {
        final ConsumerTokenStore delegateStore = createMock(ConsumerTokenStore.class);
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        Principal user = new Principal()
        {
            public String getName()
            {
                return "admin";
            }
        };
        final Map<String, String> props = MapBuilder.<String, String>newBuilder().add("prop1", "val1").add("prop2", "val2").toMap();

        ConsumerToken requestToken = ConsumerToken.newRequestToken("mytoken")
                .tokenSecret("ssh...it's secret")
                .consumer(consumer)
                .properties(props)
                .build();
        ConsumerToken accessToken = ConsumerToken.newAccessToken("mytoken")
                .tokenSecret("ssh...it's secret")
                .consumer(consumer)
                .properties(props)
                .build();

        expect(delegateStore.put(new ConsumerTokenStore.Key("mytoken"), requestToken)).andReturn(requestToken);
        expect(delegateStore.get(new ConsumerTokenStore.Key("mytoken"))).andReturn(requestToken);
        expect(delegateStore.put(new ConsumerTokenStore.Key("mytoken"), accessToken)).andReturn(accessToken);
        expect(delegateStore.get(new ConsumerTokenStore.Key("mytoken"))).andReturn(accessToken);

        replay(delegateStore);
        CachingConsumerTokenStore store = new CachingConsumerTokenStore(delegateStore, mockEventPublisher, new MemoryCacheManager());

        try
        {
            store.put(null, null);
            fail("Should have thrown exception!");
        }
        catch (RuntimeException e)
        {
            //yay!
        }

        store.put(new ConsumerTokenStore.Key("mytoken"), requestToken);

        //now try to get the token a couple of times.  Should only hit the bcvking store once
        Token token = store.get(new ConsumerTokenStore.Key("mytoken"));
        assertTokenEquals(requestToken, token);
        token = store.get(new ConsumerTokenStore.Key("mytoken"));
        assertTokenEquals(requestToken, token);
        token = store.get(new ConsumerTokenStore.Key("mytoken"));
        assertTokenEquals(requestToken, token);

        //lets update the token.. This should clear the cached entry
        store.put(new ConsumerTokenStore.Key("mytoken"), accessToken);
        token = store.get(new ConsumerTokenStore.Key("mytoken"));
        assertTokenEquals(requestToken, token);
        token = store.get(new ConsumerTokenStore.Key("mytoken"));
        assertTokenEquals(requestToken, token);

        verify(delegateStore);
    }

    @Test
    public void testRemove()
    {
        final ConsumerTokenStore delegateStore = createMock(ConsumerTokenStore.class);
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        Principal user = new Principal()
        {
            public String getName()
            {
                return "admin";
            }
        };
        final Map<String, String> props = MapBuilder.<String, String>newBuilder().add("prop1", "val1").add("prop2", "val2").toMap();

        ConsumerToken requestToken = ConsumerToken.newRequestToken("mytoken")
                .tokenSecret("ssh...it's secret")
                .consumer(consumer)
                .properties(props)
                .build();

        delegateStore.remove(new ConsumerTokenStore.Key("mytoken"));
        expect(delegateStore.get(new ConsumerTokenStore.Key("mytoken"))).andReturn(null);

        replay(delegateStore);
        CachingConsumerTokenStore store = new CachingConsumerTokenStore(delegateStore, mockEventPublisher, new MemoryCacheManager());

        try
        {
            store.remove(null);
            fail("Should have thrown exception!");
        }
        catch (RuntimeException e)
        {
            //yay!
        }

        store.remove(new ConsumerTokenStore.Key("mytoken"));

        //now try to get the token a couple of times.  Should only hit the delegate once.
        Token token = store.get(new ConsumerTokenStore.Key("mytoken"));
        assertNull(token);
        token = store.get(new ConsumerTokenStore.Key("mytoken"));
        assertNull(token);

        verify(delegateStore);
    }

    private void assertTokenEquals(final Token expected, final Token resultToken)
    {
        assertEquals(expected.getToken(), resultToken.getToken());
        assertEquals(expected.getTokenSecret(), resultToken.getTokenSecret());
        assertEquals(expected.getConsumer(), resultToken.getConsumer());
        assertEquals(expected.getProperties(), resultToken.getProperties());
    }
}
