package com.atlassian.jira.rest.assertions;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Simple assertions for the {@link Response} object.
 *
 * @since v4.4
 */
public class ResponseAssertions
{
    private static final String CACHE_CHECK = "Cache-Control";
    private static final String LOCATION = "Location";

    private ResponseAssertions()
    {
    }

    public static void assertStatus(Response.Status expected, Response actual)
    {
        assertEquals(expected.getStatusCode(), actual.getStatus());
    }

    public static void assertResponseBody(Object body, Response response)
    {
        assertEquals(format("response.body != %s.", body), body, response.getEntity());
    }

    private static Object getSingleHeader(String key, Response response)
    {
        final List<Object> headers = response.getMetadata().get(key);
        if (headers == null || headers.isEmpty())
        {
            fail("No '" + key + "' header.");
        }
        assertEquals("Wrong '" + key + "' header count", 1, headers.size());
        return headers.get(0);
    }

    public static void assertLocation(URI expectedLocation, Response response)
    {
        assertEquals("Location header is wrong", expectedLocation, getSingleHeader(LOCATION, response));
    }

    public static void assertCache(CacheControl control, Response response)
    {
        assertEquals("Cache-control header is wrong", control, getSingleHeader(CACHE_CHECK, response));
    }

    public static void assertResponseCacheNever(Response response)
    {
        assertCache(never(), response);
    }
}
