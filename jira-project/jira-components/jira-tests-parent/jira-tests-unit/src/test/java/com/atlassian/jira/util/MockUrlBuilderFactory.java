package com.atlassian.jira.util;

/**
 * Use this factory to create {@link UrlBuilder}s for your classes under test, as you need to specify an encoding,
 * otherwise the test will access the {@link com.atlassian.jira.ComponentManager}
 *
 * @since v4.0
 */
public class MockUrlBuilderFactory
{
    public static UrlBuilder createUrlBuilder(String baseUrl, boolean snippet)
    {
        return new UrlBuilder(baseUrl, "UTF-8", snippet);
    }

    public static UrlBuilder createUrlBuilder(boolean snippet)
    {
        return new UrlBuilder("", "UTF-8", snippet);
    }
}
