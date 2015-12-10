package com.atlassian.jira.web;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class HttpServletVariablesImplTest
{

    /**
     * Locks in the fact that we expect IllegalStateException if we are NOT inside a HTTP request
     */
    @Test
    public void testIllegalStateExceptions()
    {
        final HttpServletVariablesImpl httpServletVariables = new HttpServletVariablesImpl();
        assetISE(new Runnable()
        {
            @Override
            public void run()
            {
                httpServletVariables.getHttpRequest();
            }
        });
        assetISE(new Runnable()
        {
            @Override
            public void run()
            {
                httpServletVariables.getHttpSession();
            }
        });
        assetISE(new Runnable()
        {
            @Override
            public void run()
            {
                httpServletVariables.getHttpResponse();
            }
        });

    }

    private void assetISE(final Runnable runnable)
    {
        try
        {
            runnable.run();
            Assert.fail("This should have thrown an IllegalStateException");
        }
        catch (IllegalStateException ignored)
        {

        }
    }
}
