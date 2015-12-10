package com.atlassian.jira.oauth.serviceprovider;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.util.RSAKeys;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericValue;

import java.net.URI;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestOfBizServiceProviderConsumerStore
{
    private OfBizServiceProviderConsumerStore store;
    @Mock private OfBizDelegator delegator;

    @Before
    public void setUp() throws Exception
    {
        store = new OfBizServiceProviderConsumerStore(delegator);
    }

    @Test(expected = RuntimeException.class)
    public void testPutWithNullKey() throws Exception
    {
        store.put(null);
    }

    @Test(expected = RuntimeException.class)
    public void testGetWithNullKey() throws Exception
    {
        store.get(null);
    }

    public void testGetNonExistingConsumer() throws Exception
    {
        when(delegator.findByAnd("OAuthServiceProviderConsumer", mapOf("consumerKey", "blargh"))).
                thenReturn(Collections.<GenericValue>emptyList());

        final Consumer nullConsumer = store.get("blargh");

        assertNull(nullConsumer);
    }

    @Test
    public void testPut() throws Exception
    {
        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        final String keyString = RSAKeys.toPemEncoding(key);

        // iGoogle consumer params
        final Map<String, Object> iGoogleParams = iGoogleParams(keyString);
        // RefImpl consumer parameters
        final Map<String, Object> refImplParams = refImplParams(keyString);
        // myGoogle params (updated)
        final Map<String, Object> myGoogleParams = myGoogleParams(keyString);
        final MockGenericValue consumerGVtoUpdate = new MockGenericValue("OAuthServiceProviderConsumer", iGoogleParams);
        consumerGVtoUpdate.expectedFields = myGoogleParams;

        when(delegator.findByAnd("OAuthServiceProviderConsumer", mapOf("consumerKey", "www.google.com"))).
                //try to get the google consumer as part of the first put().  Doesn't exist in the database yet!
                thenReturn(Collections.<GenericValue>emptyList()).
                //now we call get() the consumer exists in the data.
                thenReturn(Collections.<GenericValue>singletonList(new MockGenericValue("OAuthServiceProviderConsumer", iGoogleParams))).
                //update the google consumer.  This should call store on the GV rather than 'create'
                thenReturn(Collections.<GenericValue>singletonList(consumerGVtoUpdate)).
                //finally get the updated consumer with its updated fields.
                thenReturn(Collections.<GenericValue>singletonList(new MockGenericValue("OAuthServiceProviderConsumer", myGoogleParams)));

        //create the google consumer since it doesn't exist yet.
        when(delegator.createValue(eq("OAuthServiceProviderConsumer"), eqOfBizMapArg(iGoogleParams))).thenReturn(null);

        //try to get the refimpl consumer as part of the first put.  Doesn't exist in the database yet!
        when(delegator.findByAnd("OAuthServiceProviderConsumer", mapOf("consumerKey", "refi:1212"))).
                thenReturn(Collections.<GenericValue>emptyList()).
                //now we call get().  The refimpl exists.
                thenReturn(Collections.<GenericValue>singletonList(new MockGenericValue("OAuthServiceProviderConsumer", refImplParams)));

        //create the consumer in the database.
        when(delegator.createValue(eq("OAuthServiceProviderConsumer"), eqOfBizMapArg(refImplParams))).thenReturn(null);

        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").publicKey(key).build();
        store.put(consumer);

        final Consumer google = store.get("www.google.com");
        assertConsumersEqual(consumer, google);

        Consumer another = Consumer.key("refi:1212").name("Refimpl").publicKey(key).callback(URI.create("http://some.url.com/")).description("This is a long description!!!").build();
        store.put(another);

        final Consumer refimpl = store.get("refi:1212");
        assertConsumersEqual(refimpl, another);

        //try updating an existing conumser
        Consumer myGoogle = Consumer.key(google.getKey()).name("MyGoogle").callback(URI.create("http://my.url.com/")).publicKey(key).build();
        store.put(myGoogle);
        Consumer updatedGoogle = store.get("www.google.com");
        assertConsumersEqual(myGoogle, updatedGoogle);
        assertTrue(consumerGVtoUpdate.storeCalled.get());
    }

    @Test
    public void testAllEmpty() throws Exception
    {
        when(delegator.findAll("OAuthServiceProviderConsumer")).thenReturn(Collections.<GenericValue>emptyList());

        //test retrieving with nothing
        Iterable<Consumer> iterable = store.getAll();
        List<Consumer> list = convertIterableToList(iterable);
        assertEquals(0, list.size());
    }

    @Test
    public void testAll() throws Exception
    {
        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        final String keyString = RSAKeys.toPemEncoding(key);

        when(delegator.findAll("OAuthServiceProviderConsumer")).thenReturn(
                CollectionBuilder.<GenericValue>newBuilder(
                        new MockGenericValue("OAuthServiceProviderConsumer", MapBuilder.newBuilder().
                                add("publicKey", keyString).add("description", null).add("callback", "http://my.url.com/").
                                add("consumerKey", "www.google.com").add("name", "MyGoogle").
                                toMap()),
                        new MockGenericValue("OAuthServiceProviderConsumer", MapBuilder.newBuilder().
                                add("publicKey", keyString).add("description", "some other service").add("callback", "http://www.refimpl/").
                                add("consumerKey", "refimpl:12112").add("name", "Refimpl").
                                toMap())).asList());

        Consumer consumer = Consumer.key("www.google.com").name("MyGoogle").publicKey(key).callback(URI.create("http://my.url.com/")).build();
        Consumer consumer2 = Consumer.key("refimpl:12112").name("Refimpl").publicKey(key).callback(URI.create("http://www.refimpl/")).description("some other service").build();

        //test retrieving with nothing
        Iterable<Consumer> iterable = store.getAll();
        List<Consumer> list = convertIterableToList(iterable);
        assertEquals(2, list.size());

        final Consumer result1 = list.get(0);
        final Consumer result2 = list.get(1);
        //ordering isn't guaranteed.
        if (result1.getKey().equals("www.google.com"))
        {
            assertConsumersEqual(consumer, result1);
            assertConsumersEqual(consumer2, result2);
        }
        else
        {
            assertConsumersEqual(consumer, result2);
            assertConsumersEqual(consumer2, result1);
        }
    }

    private <T> List<T> convertIterableToList(final Iterable<T> iterable)
    {
        final List<T> ret = new ArrayList<T>();
        for (T entry : iterable)
        {
            ret.add(entry);
        }
        return ret;
    }

    private static Map<String, Object> eqOfBizMapArg(Map<String, Object> in)
    {
        return argThat(new OfBizMapArgsEqual(in));
    }

    private void assertConsumersEqual(Consumer expected, Consumer result)
    {
        assertEquals(expected.getKey(), result.getKey());
        String expectedDescrption = expected.getDescription();
        if(expected.getDescription() == null)
        {
            expectedDescrption = "";
        }
        assertEquals(expectedDescrption, result.getDescription());
        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getCallback(), result.getCallback());
        assertEquals(expected.getPublicKey(), result.getPublicKey());
    }

    private Map<String, Object> mapOf(String key, Object value)
    {
        return ImmutableMap.of(key, value);
    }

    private Map<String, Object> myGoogleParams(String keyString)
    {
        return MapBuilder.<String, Object>newBuilder().
                add("publicKey", keyString).add("description", "").
                add("consumerKey", "www.google.com").
                add("name", "MyGoogle").
                add("callback", "http://my.url.com/").
                add("twoLOAllowed", false).add("executingTwoLOUser", null).add("twoLOImpersonationAllowed", false).add("threeLOAllowed", true).
                add("created", null).toMap();
    }

    private Map<String, Object> refImplParams(String keyString)
    {
        return MapBuilder.<String, Object>newBuilder().
                add("created", null).add("publicKey", keyString).add("description", "This is a long description!!!").
                add("consumerKey", "refi:1212").add("name", "Refimpl").add("callback", "http://some.url.com/").
                add("twoLOAllowed", false).add("executingTwoLOUser", null).add("twoLOImpersonationAllowed", false).add("threeLOAllowed", true).toMap();
    }

    private Map<String, Object> iGoogleParams(String keyString)
    {
        return MapBuilder.<String, Object>newBuilder().
                add("created", null).add("publicKey", keyString).add("description", "").
                add("consumerKey", "www.google.com").add("name", "iGoogle").add("callback", null).
                add("twoLOAllowed", false).add("executingTwoLOUser", null).add("twoLOImpersonationAllowed", false).add("threeLOAllowed", true).toMap();
    }
}
