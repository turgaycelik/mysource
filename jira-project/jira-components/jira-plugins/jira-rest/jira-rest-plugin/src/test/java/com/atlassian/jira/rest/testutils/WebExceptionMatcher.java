package com.atlassian.jira.rest.testutils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class WebExceptionMatcher extends TypeSafeMatcher<WebApplicationException>
{
    private final Response.Status status;

    public WebExceptionMatcher(final Response.Status status)
    {
        this.status = status;
    }

    @Override
    protected boolean matchesSafely(final WebApplicationException webException)
    {
        return webException.getResponse().getStatus() == status.getStatusCode();
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText("WebApplicationException(" + status + ")");
    }
}
