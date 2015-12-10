package com.atlassian.jira.rest.matchers;

import com.atlassian.jira.rest.api.util.ErrorCollection;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import javax.ws.rs.core.Response;

/**
 * @since v6.3
 */
public class ResponseMatchers
{
    private ResponseMatchers()
    {

    }

    public static Matcher<Response> noCache()
    {
        return new NoCache();
    }

    public static Matcher<Response> status(Response.Status status)
    {
        return new ResponseCode(status);
    }

    public static <T> Matcher<Response> body(Class<T> type, T match)
    {
        return body(type, Matchers.equalTo(match));
    }

    public static <T> Matcher<Response> body(Class<T> type, Matcher<? super T> object)
    {
        return new BodyMatcher<T>(type, object);
    }

    public static Matcher<Response> errorBody(String error)
    {
        return body(ErrorCollection.class, new ErrorCollectionMatcher().addErrorMessage(error));
    }
}
