package com.atlassian.jira.util;

import com.atlassian.jira.util.velocity.MockVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class DefaultBaseUrlTest
{
    private static final String BASE_URL = "baseUrl";
    private static final String STATIC_URL = "staticUrl";

    private VelocityRequestContextFactory factory = new MockVelocityRequestContextFactory(BASE_URL, STATIC_URL);
    private DefaultBaseUrl url = new DefaultBaseUrl(factory);

    @Test
    public void testRunWithStaticBaseUrlDelegatesOffToRequest()
    {
        //Just a sanity check to make sure the baseURL is returned by default.
        assertThat(factory.getJiraVelocityRequestContext().getBaseUrl(), Matchers.equalTo(BASE_URL));

        //Within this callback, the baseURL should be == STATIC_URL.
        final String result = factory.runWithStaticBaseUrl(10, new Callback());
        assertThat(result, Matchers.equalTo(STATIC_URL + 10));

        //Just a sanity check to make sure the baseURL is reset.
        assertThat(factory.getJiraVelocityRequestContext().getBaseUrl(), Matchers.equalTo(BASE_URL));
    }

    private class Callback implements com.google.common.base.Function<Integer, String>
    {
        @Override
        public String apply(final Integer input)
        {
            return  url.getBaseUrl() + input;
        }
    }
}