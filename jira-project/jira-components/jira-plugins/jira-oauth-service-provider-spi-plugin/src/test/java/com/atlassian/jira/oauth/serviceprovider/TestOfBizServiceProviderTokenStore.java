package com.atlassian.jira.oauth.serviceprovider;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.MockUserKeyService;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.serviceprovider.ServiceProviderToken;
import com.atlassian.oauth.serviceprovider.StoreException;
import com.atlassian.oauth.util.RSAKeys;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opensymphony.module.propertyset.PropertySet;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericModelException;
import org.ofbiz.core.entity.GenericValue;

import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.atlassian.jira.oauth.serviceprovider.OfBizServiceProviderTokenStore.Columns;
import static com.atlassian.oauth.serviceprovider.ServiceProviderToken.Version;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestOfBizServiceProviderTokenStore
{
    @Mock OfBizDelegator mockDelegator;
    @Mock UserUtil mockUserUtil;
    @Mock ServiceProviderConsumerStore mockConsumerStore;
    @Mock JiraPropertySetFactory mockPropertySetFactory;
    @Mock EventPublisher eventPublisher;
    private MockUserKeyService userKeyService;

    private Consumer consumer;
    private User user;
    private List<GenericValue> allTokens;
    private OfBizServiceProviderTokenStore store;

    @Before
    public void setUp() throws Exception
    {
        userKeyService = new MockUserKeyService();

        store = new OfBizServiceProviderTokenStore(mockDelegator, mockUserUtil, mockConsumerStore, eventPublisher, mockPropertySetFactory, userKeyService);

        final PublicKey key = RSAKeys.generateKeyPair().getPublic();
        consumer = Consumer.key("www.google.com").name("iGoogle").publicKey(key).build();
        user = new MockUser("admin");
        when(mockUserUtil.getUserByKey("admin")).thenReturn(new MockApplicationUser("admin"));

        when(mockConsumerStore.get("GOOG")).thenReturn(consumer);

        long now = System.currentTimeMillis();
        allTokens = Lists.<GenericValue>newArrayList(
                createToken(10000L, now - 1800, 3600L),
                //expired
                createToken(10010L, now - 3800, 3600L),
                //expired
                createToken(10020L, now - 2500, 2000L),
                // has a valid session
                createTokenWithSession(10030L, (now - 1800), 3600L, "hoho_this_is_a_session", new DateTime().minusMinutes(5).toDate().getTime(), HOURS.toMillis(1)),
                // has an expired session
                createTokenWithSession(10040L, (now - 1800), 3600L, "hoho_this_is_a_session", new DateTime().minusDays(1).toDate().getTime(), HOURS.toMillis(1))
        );
    }

    @Test(expected = RuntimeException.class)
    public void testPutWithNullToken() throws Exception
    {
        store.put(null);
    }

    @Test
    public void testPut() throws NoSuchAlgorithmException
    {
        final PropertySet mockPs = mock(PropertySet.class);
        final PropertySet mockPs2 = mock(PropertySet.class);

        //first storing an ACCESS token
        when(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                add("token", "23134123412413").toMap())).
                thenReturn(Collections.<GenericValue>emptyList());
        when(mockDelegator.createValue(eq("OAuthServiceProviderToken"), eqOfBizMapArg(MapBuilder.<String, Object>newBuilder().
                add("created", null).
                add("callback", null).
                add("ttl", 157680000000L). // 5 years
                add("auth", "AUTHORIZED").
                add("token", "23134123412413").
                add("verifier", null).
                add("tokenSecret", "adfasdfasdfsdf").
                add("tokenType", "ACCESS").
                add("consumerKey", "www.google.com").
                add("username", "admin").
                add("version", null).
                toMap()))).thenReturn(new MockGenericValue("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().add("id", 10000L).toMap()));
        when(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", 10000L, true)).
                thenReturn(mockPs); // .once();

        when(mockPs.getKeys()).thenReturn(Collections.emptyList()); //.once();
        mockPs.setText("prop1", "val1");
        mockPs.setText("prop2", "val2");

        //then we store a REQUEST token
        when(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                add("token", "44444444").toMap())).
                thenReturn(Collections.<GenericValue>emptyList());
        when(mockDelegator.createValue(eq("OAuthServiceProviderToken"), eqOfBizMapArg(MapBuilder.<String, Object>newBuilder().
                add("created", null).
                add("ttl", 600000L).
                add("token", "44444444").
                add("callback", null).
                add("tokenSecret", "adfasdfasdfsdf").
                add("tokenType", "REQUEST").
                add("consumerKey", "www.google.com").
                add("verifier", "sssh...something secret").
                add("auth", "AUTHORIZED").
                add("username", "admin").
                add("version", "V_1_0_A").
                toMap()))).thenReturn(new MockGenericValue("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().add("id", 10010L).toMap()));
        when(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", 10010L, true)).
                thenReturn(mockPs2); //.once();
        when(mockPs2.getKeys()).thenReturn(Collections.emptyList()); //.once();

        final AtomicBoolean getCalled = new AtomicBoolean(false);
        final OfBizServiceProviderTokenStore store = new OfBizServiceProviderTokenStore(mockDelegator, mockUserUtil, mockConsumerStore, eventPublisher, mockPropertySetFactory, userKeyService)
        {
            @Override
            public ServiceProviderToken get(final String token) throws StoreException
            {
                //after the put we read it back from the DB  don't really care what it returns, just that it gets
                //called!
                getCalled.set(true);
                return null;
            }
        };

        final Map<String, String> props = MapBuilder.<String, String>newBuilder().add("prop1", "val1").add("prop2", "val2").toMap();
        final ServiceProviderToken token = ServiceProviderToken.newAccessToken("23134123412413").
                tokenSecret("adfasdfasdfsdf").
                consumer(consumer).
                authorizedBy(user).
                properties(props).build();

        //lets try a token without props
        final ServiceProviderToken token2 = ServiceProviderToken.newRequestToken("44444444").
                tokenSecret("adfasdfasdfsdf").
                consumer(consumer).
                verifier("sssh...something secret").
                version(Version.V_1_0_A).
                authorizedBy(user).build();

        store.put(token);
        store.put(token2);
        assertTrue(getCalled.get());
    }

    @Test
    public void testPutWithRenamedUser() throws Exception
    {
        final ApplicationUser renamedUser = new MockApplicationUser("bobkey", "bob");
        userKeyService.setMapping("bobkey", "bob");
        final PropertySet mockPs = mock(PropertySet.class);
        when(mockPs.getKeys()).thenReturn(Collections.emptyList()); //.once();
        mockPs.setText("prop1", "val1");
        mockPs.setText("prop2", "val2");
        when(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", 10000L, true)).
                thenReturn(mockPs); // .once();

        when(mockDelegator.createValue(eq("OAuthServiceProviderToken"), eqOfBizMapArg(MapBuilder.<String, Object>newBuilder().
                add("created", null).
                add("callback", null).
                add("ttl", 157680000000L). // 5 years
                add("auth", "AUTHORIZED").
                add("token", "123123123123").
                add("verifier", null).
                add("tokenSecret", "Somethingsecret").
                add("tokenType", "ACCESS").
                add("consumerKey", "www.google.com").
                add("username", "bobkey"). // The userkey is stored, instead of the username
                add("version", null).
                toMap()))).thenReturn(new MockGenericValue("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().add("id", 10000L).toMap()));

        final AtomicBoolean getCalled = new AtomicBoolean(false);
        final OfBizServiceProviderTokenStore store = new OfBizServiceProviderTokenStore(mockDelegator, mockUserUtil, mockConsumerStore, eventPublisher, mockPropertySetFactory, userKeyService)
        {
            @Override
            public ServiceProviderToken get(final String token) throws StoreException
            {
                //after the put we read it back from the DB  don't really care what it returns, just that it gets
                //called!
                getCalled.set(true);
                return null;
            }
        };

        final Map<String, String> props = MapBuilder.<String, String>newBuilder().add("prop1", "val1").add("prop2", "val2").toMap();
        final ServiceProviderToken token = ServiceProviderToken.newAccessToken("123123123123")
                .tokenSecret("Somethingsecret")
                .consumer(consumer)
                .authorizedBy(renamedUser.getDirectoryUser())
                .properties(props)
                .build();

        store.put(token);
        assertTrue(getCalled.get());
    }

    @Test(expected = RuntimeException.class)
    public void testGetWithNullToken() throws Exception
    {
        store.get(null);
    }

    @Test
    public void testGet()
    {
        final PropertySet mockPs = mock(PropertySet.class);

        when(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                add("token", "nonexistant").toMap())).
                thenReturn(Collections.<GenericValue>emptyList());

        when(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                add("token", "23134123412413").toMap())).
                thenReturn(CollectionBuilder.<GenericValue>newBuilder(
                        new MockGenericValue("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                                add("id", 10000L).
                                add("created", new Timestamp(System.currentTimeMillis())).
                                add("token", "23134123412413").
                                add("tokenSecret", "adfasdfasdfsdf").
                                add("tokenType", "ACCESS").
                                add("consumerKey", "www.google.com").
                                add("username", "admin").
                                add("ttl", 157680000000L).
                                add("version", "V_1_0_A").
                                toMap())).asList());
        when(mockConsumerStore.get("www.google.com")).thenReturn(consumer); //.once();
        when(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", 10000L, true)).
                thenReturn(mockPs); //.once();
        when(mockPs.getKeys()).thenReturn(CollectionBuilder.newBuilder("prop1", "prop2").asList()); //.once();
        when(mockPs.getText("prop1")).thenReturn("val1"); //.once();
        when(mockPs.getText("prop2")).thenReturn("val2"); //.once();

        final OfBizServiceProviderTokenStore store = new OfBizServiceProviderTokenStore(mockDelegator, mockUserUtil, mockConsumerStore, eventPublisher, mockPropertySetFactory, userKeyService)
        {
            @Override
            Principal getUser(final String username)
            {
                return user;
            }
        };

        final Map<String, String> props = MapBuilder.<String, String>newBuilder().add("prop1", "val1").add("prop2", "val2").toMap();
        final ServiceProviderToken token = ServiceProviderToken.newAccessToken("23134123412413").
                tokenSecret("adfasdfasdfsdf").
                consumer(consumer).
                authorizedBy(user).
                version(Version.V_1_0_A).
                properties(props).build();

        final ServiceProviderToken resultToken = store.get("nonexistant");
        assertNull(resultToken);

        final ServiceProviderToken accessToken = store.get("23134123412413");
        assertTokenEquals(token, accessToken);
    }

    @Test
    public void testGetAccessTokensForUserNone()
    {
        when(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder()
                .add("username", "dontexist")
                .add("tokenType", "ACCESS")
                .toMap())).thenReturn(Collections.<GenericValue>emptyList());

        Iterable<ServiceProviderToken> accessTokensForUser = store.getAccessTokensForUser("dontexist");
        assertFalse(accessTokensForUser.iterator().hasNext());
    }

    @Test
    public void testGetAccessTokensForUser()
    {
        final PropertySet mockPs = mock(PropertySet.class);

        when(mockPs.getKeys()).thenReturn(Collections.emptyList());

        when(mockUserUtil.getUserByKey("admin")).thenReturn(new MockApplicationUser("admin"));

        when(mockConsumerStore.get("www.google.com")).thenReturn(consumer);

        when(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", 10000L, true)).thenReturn(mockPs);

        when(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder()
                .add("username", "admin")
                .add("tokenType", "ACCESS")
                .toMap())).
                thenReturn(CollectionBuilder.<GenericValue>newBuilder(new MockGenericValue("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                        add("id", 10000L).
                        add("created", new Timestamp(System.currentTimeMillis())).
                        add("token", "23134123412413").
                        add("tokenSecret", "adfasdfasdfsdf").
                        add("tokenType", "ACCESS").
                        add("consumerKey", "www.google.com").
                        add("username", "admin").
                        add("ttl", 604800000L).
                        toMap())).asList());

        Iterable<ServiceProviderToken> accessTokensForUser = store.getAccessTokensForUser("admin");
        assertTrue(accessTokensForUser.iterator().hasNext());
        ServiceProviderToken token = accessTokensForUser.iterator().next();
        assertEquals("admin", token.getUser().getName());
        assertEquals("23134123412413", token.getToken());
        assertEquals("adfasdfasdfsdf", token.getTokenSecret());
        assertTrue(token.isAccessToken());
        assertEquals("www.google.com", token.getConsumer().getKey());
    }

    @Test
    public void testGetAccessTokensForRenamedUser()
    {
        final ApplicationUser renamedUser = new MockApplicationUser("bobkey", "bob");
        userKeyService.setMapping("bobkey", "bob");
        final PropertySet mockPs = mock(PropertySet.class);

        when(mockPs.getKeys()).thenReturn(Collections.emptyList());
        when(mockUserUtil.getUserByKey("bobkey")).thenReturn(renamedUser);
        when(mockConsumerStore.get("www.google.com")).thenReturn(consumer);
        when(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", 10000L, true)).thenReturn(mockPs);
        when(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder()
                .add("username", "bobkey")
                .add("tokenType", "ACCESS")
                .toMap())).
                thenReturn(CollectionBuilder.<GenericValue>newBuilder(new MockGenericValue("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                        add("id", 10000L).
                        add("created", new Timestamp(System.currentTimeMillis())).
                        add("token", "23134123412413").
                        add("tokenSecret", "adfasdfasdfsdf").
                        add("tokenType", "ACCESS").
                        add("consumerKey", "www.google.com").
                        add("username", "bobkey"). // the userkey is stored in the DB
                        add("ttl", 604800000L).
                        toMap())).asList());

        Iterable<ServiceProviderToken> accessTokensForUser = store.getAccessTokensForUser("bob");
        assertTrue(accessTokensForUser.iterator().hasNext());
        ServiceProviderToken token = accessTokensForUser.iterator().next();
        assertEquals("bob", token.getUser().getName()); // userkey was properly mapped
        assertEquals("23134123412413", token.getToken());
        assertEquals("adfasdfasdfsdf", token.getTokenSecret());
        assertTrue(token.isAccessToken());
        assertEquals("www.google.com", token.getConsumer().getKey());
    }

    @Test
    public void testRemoveExpiredTokens() throws GenericModelException
    {
        setUpEmptyPropertySets();

        final OfBizListIterator listIterator = mock(OfBizListIterator.class);
        when(listIterator.iterator()).thenReturn(allTokens.iterator());
        when(mockDelegator.findListIteratorByCondition("OAuthServiceProviderToken", null)).thenReturn(listIterator);
        when(mockDelegator.removeByOr("OAuthServiceProviderToken", "id", CollectionBuilder.newBuilder(10010L, 10020L).asList())).thenReturn(2);

        store.removeExpiredTokens();

        verify(listIterator).close();
    }

    @Test
    public void testRemoveExpiredSessionsShouldNotRemoveTokensWithNoSession() throws Exception
    {
        setUpEmptyPropertySets();

        final OfBizListIterator listIterator = mock(OfBizListIterator.class);
        when(listIterator.iterator()).thenReturn(allTokens.iterator());
        when(mockDelegator.findListIteratorByCondition("OAuthServiceProviderToken", null)).thenReturn(listIterator);
        when(mockDelegator.removeByOr("OAuthServiceProviderToken", "id", Lists.newArrayList(10040L))).thenReturn(2);

        store.removeExpiredSessions();
        verify(listIterator).close();
    }

    @Test(expected = RuntimeException.class)
    public void testRemoteWithNullToken() throws Exception
    {
        store.remove(null);
    }

    @Test
    public void testRemove()
    {
        final PropertySet mockPs = mock(PropertySet.class);

        long id = 10000L;
        String token_10000 = "token-10000";
        final MockGenericValue mockGenericValue = createToken(id, 21, 100);

        when(mockDelegator.findByAnd("OAuthServiceProviderToken", MapBuilder.<String, Object>newBuilder().
                add("token", token_10000).toMap())).
                thenReturn(CollectionBuilder.<GenericValue>newBuilder(
                        mockGenericValue).asList());
        when(mockDelegator.removeValue(mockGenericValue)).thenReturn(1);

        when(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", id, true)).thenReturn(mockPs);
        when(mockPs.getKeys()).thenReturn(CollectionBuilder.newBuilder("prop1", "prop2").asList());
        when(mockPs.getText("prop1")).thenReturn("val1");
        when(mockPs.getText("prop2")).thenReturn("val2");

        store.remove(token_10000);

        verify(mockPs).remove("prop1");
        verify(mockPs).remove("prop2");
    }

    private PropertySet setUpEmptyPropertySets()
    {
        // set up empty property sets for all tokens...
        PropertySet emptyPS = mock(PropertySet.class);
        when(emptyPS.getKeys()).thenReturn(Collections.emptyList());
        for (GenericValue allToken : allTokens)
        {
            when(mockPropertySetFactory.buildCachingPropertySet("OAuthServiceProviderToken", allToken.getLong(Columns.ID), true)).thenReturn(emptyPS);
        }

        return emptyPS;
    }

    private MockGenericValue createToken(long id, long created, long ttl)
    {
        return createTokenWithSession(id, created, ttl, null, null, null);
    }

    private MockGenericValue createTokenWithSession(long id, long created, long ttl, String sessionHandle, Long sessionCreation, Long sessionTtl)
    {
        ImmutableMap.Builder<Object, Object> values = ImmutableMap.builder()
                .put("id", id)
                .put("token", "token-" + id)
                .put("tokenType", "ACCESS")
                .put("tokenSecret", "secret-" + id)
                .put("created", new Timestamp(created))
                .put("username", "admin")
                .put("consumerKey", "GOOG")
                .put("ttl", ttl);

        if (sessionHandle != null)
        {
            values = values.put(Columns.SESSION_HANDLE, sessionHandle)
                .put(Columns.SESSION_CREATION_TIME, new Timestamp(sessionCreation))
                .put(Columns.SESSION_LAST_RENEWAL_TIME, new Timestamp(sessionCreation))
                .put(Columns.SESSION_TIME_TO_LIVE, new Timestamp(sessionTtl));
        }

        return new MockGenericValue("OAuthServiceProviderToken", values.build());
    }

    private void assertTokenEquals(ServiceProviderToken expected, ServiceProviderToken result)
    {
        assertEquals(expected.getToken(), result.getToken());
        assertEquals(expected.getTokenSecret(), result.getTokenSecret());
        assertEquals(expected.isAccessToken(), result.isAccessToken());
        assertEquals(expected.hasBeenAuthorized(), result.hasBeenAuthorized());
        assertEquals(expected.isRequestToken(), result.isRequestToken());
        assertEquals(expected.getConsumer().getKey(), result.getConsumer().getKey());
        assertEquals(expected.getUser(), result.getUser());
        assertEquals(expected.getVerifier(), result.getVerifier());
        assertEquals(expected.getTimeToLive(), result.getTimeToLive());
        assertEquals(expected.getProperties(), result.getProperties());
        assertEquals(expected.getVersion(), result.getVersion());
    }

    private static Map<String, Object> eqOfBizMapArg(Map<String, Object> in)
    {
        return argThat(new OfBizMapArgsEqual(in));
    }
}
