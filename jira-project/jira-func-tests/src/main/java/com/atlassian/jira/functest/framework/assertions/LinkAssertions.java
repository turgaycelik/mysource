package com.atlassian.jira.functest.framework.assertions;

import com.meterware.httpunit.WebLink;

/**
 * Used to make assertions about links.
 *
 * @since v3.13
 */
public interface LinkAssertions
{
    /**
     * Assert that link with given ID exists and matches exactly given text
     *
     * @param linkId ID of the link
     * @param linkText the text of the link to test.
     */
    void assertLinkByIdHasExactText(String linkId, String linkText);

    /**
     * Assert that the passed link's destination ends with a the passed value.
     *
     * @param linkText the text of the link to test.
     * @param endsWith the expected end of the link's destination.
     */
    void assertLinkLocationEndsWith(String linkText, String endsWith);

    /**
     * Assert that the passed link's destination ends with the passed value.
     *
     * @param link the link to test.
     * @param endsWith the expected end of the link's destination.
     */
    void assertLinkLocationEndsWith(WebLink link, String endsWith);

    /**
     * Assert that the passed link's destination ends with a the passed value.
     *
     * @param linkId   the id to the link to check.
     * @param endsWith the expected end of the link's destination.
     */
    void assertLinkIdLocationEndsWith(String linkId, String endsWith);

    /**
     * Assert that the passed link's destination matches the passed regex.
     *
     * @param linkId   the id to the link to check.
     * @param regex the regular expression to use in the test.
     */
    void assertLinkIdLocationMatchesRegex(String linkId, String regex);

    /**
     * Assert that a link with the specified text is present within the region of the given locator.
     *
     * @param xpath the XPath query to narrow down the search. Should not contain the A element being searched for.
     * @param text the exact text within the link.
     */
    void assertLinkPresentWithExactText(String xpath, String text);

    /**
     * Assert that a link with the specified text is not present within the region of the given locator.
     *
     * @param xpath the XPath query to narrow down the search. Should not contain the A element being searched for.
     * @param text the text within the link.
     */
    void assertLinkNotPresentWithExactText(String xpath, String text);

    /**
     * Assert that a link at specified xpath is present and location ends with given url.
     *
     * Note: consider using {@link #assertLinkAtNodeContains(String, String)} instead as it is less sensitive to breakages
     * caused by the addition of parameters such as XSRF token.
     *
     * @param xpath the XPath query to specify the anchor element.  The xpath expression should end with an A element
     * @param endsWith the expected end of the link's destination.
     */
    void assertLinkAtNodeEndsWith(String xpath, String endsWith);

    /**
     * Assert that a link with the specified text is present within the region located by <tt>regionId</tt>.
     *
     * @param regionId the ID of the page section that should contain the A element being searched for.
     * @param text the exact text within the link.
     */
    void assertLinkPresentWithExactTextById(String regionId, String text);

    /**
     * Assert that a link containing the specified text is not present within the region located by <tt>regionId</tt>.
     *
     * @param regionId the ID of the page section that should not contain the A element being searched for.
     * @param text the text within the link.
     */
    void assertLinkNotPresentContainingTextById(String regionId, String text);

    /**
     * Assert that a link at specified xpath is present and location contains the given url.
     *
     * @param xpath the XPath query to specify the anchor element.  The xpath expression should end with an A element
     * @param containsUrl the expected url to be contained in the link's destination.
     */
    void assertLinkAtNodeContains(String xpath, String containsUrl);

    void assertLinkIdQueryStringContainsJqlQuery(String linkId, String expectedJqlQuery);

    void assertLinkTextQueryStringContainsJqlQuery(String xpath, String linkText, String expectedJqlQuery);

    void assertLinkQueryStringContainsJqlQuery(WebLink link, String expectedJqlQuery);

    /**
     * Assert that the page contains a link with exact text and a URL ending with the provided <tt>expecedUrlSuffix</tt>.
     *
     * @param expectedExactText expected exact text of the link
     * @param expectedUrlSuffix expected URL suffix of the link
     */
    void assertLinkWithExactTextAndUrlPresent(String expectedExactText, String expectedUrlSuffix);

}
