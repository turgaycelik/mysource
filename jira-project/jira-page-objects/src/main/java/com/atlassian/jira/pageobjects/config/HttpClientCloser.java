package com.atlassian.jira.pageobjects.config;

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Those freakin HTTP responses have to be closed!
 *
 * @since v4.4
 */
final class HttpClientCloser
{
    private static final Logger logger = LoggerFactory.getLogger(RestConfigProvider.class);

    private HttpClientCloser()
    {
        throw new AssertionError("Don't instantiate me");
    }

    static void closeQuietly(HttpResponse response)
    {
        if (response != null)
        {
            try
            {
                response.getEntity().consumeContent();
            }
            catch (IOException e)
            {
                logger.warn("Exception while closing connection", e);
            }
        }
    }
}
