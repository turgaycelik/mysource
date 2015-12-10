package com.atlassian.jira.functest.framework.assertions;

/**
 * Make some assertions about a URL.
 *
 * @since v3.13
 */
public interface URLAssertions
{
    /**
     * Make sure the current URL path ends with the specified value.
     *
     * @param expectedEnd the expected current path the check.
     */
    void assertCurrentURLPathEndsWith(String expectedEnd);

    /**
     * Make sure the current URL ends with the specified value.
     *
     * @param expectedEnd the expected end of the current url.
     */
    void assertCurrentURLEndsWith(String expectedEnd);

    /**
     * Make sure current URL match the passed regular expression.
     *
     * @param regex the regular to use to match.
     */
    void assertCurrentURLMatchesRegex(String regex);

    /**
     * Makes sure that the 2 URLs are similair enough to be compared
     *
     * @param msg the assertyion message
     * @param expectedURL the expected URL
     * @param actualURL the actual URL
     */
    void assertURLAreSimilair(final String msg, final String expectedURL, final String actualURL);

}
