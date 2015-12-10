package com.atlassian.jira.rest.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import javax.ws.rs.core.Response;

/**
 * @since v6.3
 */
public class ResponseCode extends TypeSafeDiagnosingMatcher<Response>
{
    private final int code;

    public ResponseCode(Response.Status status)
    {
        this.code = status.getStatusCode();
    }

    @Override
    protected boolean matchesSafely(final Response item, final Description mismatchDescription)
    {
        if (item.getStatus() == code)
        {
            return true;
        }
        else
        {
            mismatchDescription.appendText("Status: ").appendText(getReasonPhrase(item.getStatus()));
            return false;
        }
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText("Status: ").appendText(getReasonPhrase(code));
    }

    private static String getReasonPhrase(final int status)
    {
        return Response.Status.fromStatusCode(status).getReasonPhrase();
    }
}
