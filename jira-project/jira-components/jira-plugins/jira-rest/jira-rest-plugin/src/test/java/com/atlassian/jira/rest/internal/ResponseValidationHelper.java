package com.atlassian.jira.rest.internal;

import junit.framework.Assert;

import javax.ws.rs.core.Response;

public class ResponseValidationHelper
{
    public void assertCreated(final Response response)
    {
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }

    public void assertUpdated(final Response response)
    {
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    public void assertNoContent(final Response response)
    {
        Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    public void assertOk(final Response response)
    {
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
}
