package com.atlassian.jira.util.velocity;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultVelocityRequestContextFactoryTest
{
    private static final String STATIC_BASE_URL = "http://base.url.com/jira/";
    private static final String DYNAMIC_BASE_URL = "/jira/";

    public ApplicationProperties properties = new MockApplicationProperties();
    public DefaultVelocityRequestContextFactoryForTest factory =
            new DefaultVelocityRequestContextFactoryForTest(properties);

    @After
    public void after()
    {
        factory.clearVelocityRequestContext();
    }

    @Before
    public void before()
    {
        properties.setString(APKeys.JIRA_BASEURL, STATIC_BASE_URL);
        factory.setVelocityRequestContext(new SimpleVelocityRequestContext(DYNAMIC_BASE_URL));
    }

    @Test
    public void testRunWithStaticBaseUrlUsesStaticUrlWhenDynamicOneProvided()
    {
        //The DYNAMIC URL should be returned before the call.
        assertThat(factory.getJiraVelocityRequestContext().getBaseUrl(), Matchers.equalTo(DYNAMIC_BASE_URL));

        //The STATIC URL should be returned within the callback.
        assertThat(factory.runWithStaticBaseUrl(10, new Callback()), Matchers.equalTo(STATIC_BASE_URL + 10));

        //The DYNAMIC URL should be restored after the callback is finished.
        assertThat(factory.getJiraVelocityRequestContext().getBaseUrl(), Matchers.equalTo(DYNAMIC_BASE_URL));
    }

    @Test
    public void testRunWithStaticBaseUrlUsesStaticUrlWhenNoOtherUrlProvided()
    {
        factory.clearVelocityRequestContext();

        //Nothing should exist before the call.
        assertThat(factory.getJiraVelocityRequestContext().getBaseUrl(), Matchers.equalTo(STATIC_BASE_URL));

        //The STATIC URL should be returned within the callback.
        assertThat(factory.runWithStaticBaseUrl(10, new Callback()), Matchers.equalTo(STATIC_BASE_URL + 10));

        //Nothing should exist after the call.
        assertThat(factory.getJiraVelocityRequestContext().getBaseUrl(), Matchers.equalTo(STATIC_BASE_URL));

        //The static URL should not be added to the cache.
        assertThat(factory.requestCache.isEmpty(), Matchers.equalTo(true));
    }

    private static class DefaultVelocityRequestContextFactoryForTest extends DefaultVelocityRequestContextFactory
    {
        private Map<String, Object> requestCache = Maps.newHashMap();

        public DefaultVelocityRequestContextFactoryForTest(final ApplicationProperties applicationProperties)
        {
            super(applicationProperties);
        }

        @Override
        Map<String, Object> getRequestCache()
        {
            return requestCache;
        }
    }

    private class Callback implements Function<Integer, String>
    {
        @Override
        public String apply(final Integer integer)
        {
            return factory.getJiraVelocityRequestContext().getBaseUrl() + integer;
        }
    }
}