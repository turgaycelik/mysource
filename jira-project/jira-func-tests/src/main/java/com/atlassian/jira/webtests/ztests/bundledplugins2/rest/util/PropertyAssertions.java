package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.util;

import java.util.concurrent.Callable;

import javax.ws.rs.core.Response;

import com.atlassian.jira.testkit.client.restclient.EntityPropertyKeys;

import com.sun.jersey.api.client.UniformInterfaceException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @since v6.2
 */
public class PropertyAssertions
{
    public static void assertUniformInterfaceException(final Callable<Void> callable, final Response.Status expectedStatus)
    {
        try
        {
            callable.call();
            fail("Epected uniform interface exception with status " + expectedStatus.toString());
        }
        catch (UniformInterfaceException e)
        {
            assertThat(e.getResponse().getStatus(), is(expectedStatus.getStatusCode()));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Matcher<EntityPropertyKeys.EntityPropertyKey> propertyKey(final String propertyKey)
    {
        return new TypeSafeMatcher<EntityPropertyKeys.EntityPropertyKey>()
        {
            @Override
            protected boolean matchesSafely(final EntityPropertyKeys.EntityPropertyKey issuePropertyKey)
            {
                return issuePropertyKey.key.equals(propertyKey) && issuePropertyKey.self.endsWith(propertyKey);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("List does not contain property with key").appendValue(propertyKey);
            }
        };
    }
}
