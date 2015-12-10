package com.atlassian.jira.oauth.consumer;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.consumer.ConsumerToken;
import com.atlassian.oauth.consumer.ConsumerTokenStore;
import com.opensymphony.module.propertyset.PropertySet;
import junit.framework.TestCase;
import org.ofbiz.core.entity.GenericValue;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.mockito.Mockito.mock;

/**
 * @since v4.3
 */
public class TestOfBizConsumerTokenStore extends TestCase
{
    public void testGetConsumerTokens() throws Exception
    {
        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);
        final ConsumerService consumerService = createMock(ConsumerService.class);
        final Consumer consumer = Consumer.key("iGoogle").
                signatureMethod(Consumer.SignatureMethod.RSA_SHA1).
                name("Google").
                publicKey(mock(PublicKey.class)).
                build();
        expect(consumerService.getConsumerByKey("iGoogle")).andReturn(consumer);
        expect(consumerService.getConsumerByKey("iGoogle")).andReturn(consumer);

        final GenericValue consumerGv1 = new MockGenericValue("OAuthConsumer",
                MapBuilder.newBuilder()
                        .add("tokenKey", "12343")
                        .add("tokenType", "ACCESS")
                        .add("token", "ALKLJDLKKLHKDJHLKJDHLKJFHLKDJHFJKL")
                        .add("tokenSecret", "iuywoiuhkjskljhslkfjh")
                        .add("consumerKey", "iGoogle")
                        .add("id", 789L)
                        .toMap());

        final GenericValue consumerGv2 = new com.atlassian.jira.mock.ofbiz.MockGenericValue("OAuthConsumer",
                MapBuilder.newBuilder()
                        .add("tokenKey", "456789")
                        .add("tokenType", "ACCESS")
                        .add("token", "KIOUEOUYOIEUYROIUEYROIUJKHDKJHFJKH")
                        .add("tokenSecret", "iuywoiuhkjskljhslkfjh")
                        .add("consumerKey", "iGoogle")
                        .add("id", 123L)
                        .toMap());

        final List<GenericValue> gvList = CollectionBuilder.newBuilder(consumerGv1, consumerGv2).asList();

        expect(mockDelegator.findByAnd("OAuthConsumerToken", MapBuilder.<String, Object>newBuilder()
                .add("consumerKey", "iGoogle").toMap())).andReturn(gvList);

        final JiraPropertySetFactory jiraPropertySetFactory = createMock(JiraPropertySetFactory.class);
        PropertySet propertySet = createMock(PropertySet.class);
        expect(propertySet.getKeys()).andReturn(Collections.emptyList());
        expect(propertySet.getKeys()).andReturn(Collections.emptyList());
        expect(jiraPropertySetFactory.buildCachingPropertySet("OAuthConsumerToken", 789l, true)).andReturn(propertySet);
        expect(jiraPropertySetFactory.buildCachingPropertySet("OAuthConsumerToken", 123l, true)).andReturn(propertySet);

        replay(mockDelegator, consumerService, jiraPropertySetFactory, propertySet);

        OfBizConsumerTokenStore ofBizConsumerTokenStore = new OfBizConsumerTokenStore(mockDelegator, jiraPropertySetFactory)
        {
            @Override
            protected ConsumerService getConsumerService()
            {
                return consumerService;
            }
        };
        final Map<ConsumerTokenStore.Key,ConsumerToken> tokenMap = ofBizConsumerTokenStore.getConsumerTokens("iGoogle");
        assertEquals(2, tokenMap.keySet().size());
        assertTrue(tokenMap.keySet().contains(new ConsumerTokenStore.Key("456789")));
        assertTrue(tokenMap.keySet().contains(new ConsumerTokenStore.Key("12343")));

        final ConsumerToken token2 = tokenMap.get(new ConsumerTokenStore.Key("456789"));
        assertEquals("KIOUEOUYOIEUYROIUEYROIUJKHDKJHFJKH", token2.getToken());
        assertTrue(token2.isAccessToken());
        assertEquals("iuywoiuhkjskljhslkfjh", token2.getTokenSecret());
        assertEquals("iGoogle", token2.getConsumer().getKey());

        final ConsumerToken token1 = tokenMap.get(new ConsumerTokenStore.Key("12343"));
        assertEquals("ALKLJDLKKLHKDJHLKJDHLKJFHLKDJHFJKL", token1.getToken());
        assertTrue(token1.isAccessToken());
        assertEquals("iuywoiuhkjskljhslkfjh", token1.getTokenSecret());
        assertEquals("iGoogle", token1.getConsumer().getKey());
    }
}
