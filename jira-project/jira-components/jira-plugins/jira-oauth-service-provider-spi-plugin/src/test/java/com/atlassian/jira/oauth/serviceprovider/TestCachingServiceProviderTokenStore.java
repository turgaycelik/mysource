package com.atlassian.jira.oauth.serviceprovider;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.Token;
import com.atlassian.oauth.serviceprovider.ServiceProviderToken;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.Principal;
import java.util.Map;

import static com.atlassian.oauth.serviceprovider.ServiceProviderToken.Version;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestCachingServiceProviderTokenStore
{
    private CachingServiceProviderTokenStore cachingStore;
    @Mock private UserUtil userUtil;
    @Mock private ServiceProviderTokenStore mockDelegateStore;
    @Mock private EventPublisher eventPublisher;

    @Before
    public void setUp() throws Exception
    {
        cachingStore = new CachingServiceProviderTokenStore(mockDelegateStore, eventPublisher, new MemoryCacheManager());
    }

    @Test
    public void testPutAndGet()
    {
        // Test Data
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        final ApplicationUser applicationUser = new MockApplicationUser("admin");
        Principal user = applicationUser.getDirectoryUser();
        final Map<String, String> props = MapBuilder.<String, String>newBuilder().add("prop1", "val1").add("prop2", "val2").toMap();

        ServiceProviderToken token = ServiceProviderToken.newRequestToken("mytoken")
                .tokenSecret("ssh...it's secret")
                .consumer(consumer)
                .authorizedBy(user)
                .verifier("sssshhh...itssecret")
                .properties(props)
                .version(Version.V_1_0_A)
                .build();
        //update existing token.
        ServiceProviderToken tokenUpdated = ServiceProviderToken.newAccessToken("mytoken")
                .tokenSecret("This is a new secret!!")
                .consumer(consumer)
                .authorizedBy(user)
                .build();

        // Expectations
        when(mockDelegateStore.put(token)).thenReturn(token).thenReturn(tokenUpdated);
        when(mockDelegateStore.get("mytoken")).thenReturn(token).thenReturn(tokenUpdated);
        when(mockDelegateStore.get("missingtoken")).thenReturn(null);
        when(userUtil.getUserByKey("admin")).thenReturn(applicationUser);


        // Test
        cachingStore.put(token);
        //try to get the same token a couple of times.  Should only hit the store once!
        ServiceProviderToken resultToken = cachingStore.get("mytoken");
        assertTokenEquals(token, resultToken);
        resultToken = cachingStore.get("mytoken");
        assertTokenEquals(token, resultToken);
        resultToken = cachingStore.get("mytoken");
        assertTokenEquals(token, resultToken);
        resultToken = cachingStore.get("mytoken");
        assertTokenEquals(token, resultToken);

        // Verify the store was called only once
        verify(mockDelegateStore).put(token);

        cachingStore.put(tokenUpdated);
        final ServiceProviderToken updatedResultToken = cachingStore.get("mytoken");
        assertTokenEquals(tokenUpdated, updatedResultToken);

        //now try getting a token that doesn't exist.
        final Token missingToken = cachingStore.get("missingtoken");
        assertNull(missingToken);
        // Verified the store is called once
        verify(mockDelegateStore).get("missingtoken");
    }

    @Test
    public void testRemove()
    {
        // Test Data
        Consumer consumer = Consumer.key("www.google.com").name("iGoogle").signatureMethod(Consumer.SignatureMethod.HMAC_SHA1).build();
        final ApplicationUser applicationUser = new MockApplicationUser("admin");
        Principal user = applicationUser.getDirectoryUser();

        final Map<String, String> props = MapBuilder.<String, String>newBuilder().add("prop1", "val1").add("prop2", "val2").toMap();

        ServiceProviderToken token = ServiceProviderToken.newAccessToken("mytoken")
                .tokenSecret("ssh...it's secret")
                .consumer(consumer)
                .authorizedBy(user)
                .properties(props)
                .build();

        // Expecations
        when(mockDelegateStore.put(token)).thenReturn(token);

        when(mockDelegateStore.get("mytoken")).thenReturn(token).thenReturn(null);

        when(mockDelegateStore.get("dontexist")).thenReturn(null);

        when(userUtil.getUserByKey("admin")).thenReturn(applicationUser);

        //Test
        cachingStore.put(token);
        //try to get the same token a couple of times.  Should only hit the store once!
        ServiceProviderToken resultToken = cachingStore.get("mytoken");
        assertTokenEquals(token, resultToken);
        verify(mockDelegateStore).put(token);

        //now remove the token
        cachingStore.removeAndNotify("mytoken");
        verify(mockDelegateStore).removeAndNotify("mytoken");

        //and get it again
        final Token deletedToken = cachingStore.get("mytoken");
        assertNull(deletedToken);
        verify(mockDelegateStore, times(2)).get("mytoken");

        //remove a token that doesn't exist.
        cachingStore.removeAndNotify("dontexist");
        final Token dontexistToken = cachingStore.get("dontexist");
        assertNull(dontexistToken);
        verify(mockDelegateStore).removeAndNotify("dontexist");
    }

    private void assertTokenEquals(final ServiceProviderToken expected, final ServiceProviderToken resultToken)
    {
        assertEquals(expected.getToken(), resultToken.getToken());
        assertEquals(expected.getTokenSecret(), resultToken.getTokenSecret());
        assertEquals(expected.getConsumer(), resultToken.getConsumer());
        assertEquals(expected.getProperties(), resultToken.getProperties());
        assertEquals(expected.getUser(), resultToken.getUser());
    }

}
