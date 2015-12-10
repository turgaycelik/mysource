package com.atlassian.jira.functest.matcher;

import com.meterware.httpunit.WebResponse;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

/**
 * Matcher that checks that the response contains a specific HTTP header with the given value.
 *
 * @since v4.3
 */
public class HeaderValue extends TypeSafeMatcher<WebResponse>
{
    /**
     * Asserts that a WebResponse has a header with the given value.
     *
     * @param headerName a String containing the header's expected name
     * @param headerValue a String containing the header's expected value
     * @return a new Matcher<WebResponse>
     */
    public static Matcher<WebResponse> header(String headerName, Matcher<? super String> headerValue)
    {
        return new HeaderValue(headerName, headerValue);
    }

    /**
     * The header name.
     */
    private final String headerName;

    /**
     * The matcher for the header value.
     */
    private final Matcher<? super String> valueMatcher;

    /**
     * Creates a new HasHeaderWithValue matcher.
     *
     * @param headerName a String containing the header's  name
     * @param headerValue a Matcher<String> for the header's value
     */
    public HeaderValue(String headerName, Matcher<? super String> headerValue)
    {
        this.headerName = headerName;
        this.valueMatcher = headerValue;
    }

    @Override
    public boolean matchesSafely(WebResponse response)
    {
        String[] values = response.getHeaderFields(headerName);
        for (String value : values)
        {
            if (valueMatcher.matches(value))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("a WebResponse where the header ")
                .appendValue(headerName)
                .appendText(" is present and the value ")
                .appendDescriptionOf(valueMatcher);
    }
}
