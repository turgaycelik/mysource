package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.util.url.ParsedURL;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import junit.framework.Assert;
import net.sourceforge.jwebunit.WebTester;

import java.net.URL;

/**
 * Default implementation of {@link URLAssertions}.
 *
 * @since v3.13
 */
public class URLAssertionsImpl extends AbstractFuncTestUtil implements URLAssertions
{
    public URLAssertionsImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    public void assertCurrentURLPathEndsWith(final String expectedEnd)
    {
        final String actualPath = getCurrentURL().getPath();
        Assert.assertTrue("Expected path to end with '" + expectedEnd + "' but was '" + actualPath + "'.", actualPath.endsWith(expectedEnd));

    }

    public void assertCurrentURLEndsWith(final String expectedEnd)
    {
        final String actualPath = getCurrentURL().toExternalForm();
        Assert.assertTrue("Expected url to end with '" + expectedEnd + "' but was '" + actualPath + "'.", actualPath.endsWith(expectedEnd));
    }

    public void assertCurrentURLMatchesRegex(final String regex)
    {
        final String actualPath = getCurrentURL().toExternalForm();
        Assert.assertTrue("Current URL '" + actualPath + "' did not match regular expression '" + regex + "'.", actualPath.matches(regex));
    }

    public void assertURLAreSimilair(final String msg, final String expectedURL, final String actualURL)
    {

        ParsedURL parsedURL1 = new ParsedURL(expectedURL);
        ParsedURL parsedURL2 = new ParsedURL(actualURL);

        Assert.assertEquals(msg + " - not the same path! URL1:'" + parsedURL1 + "' URL2:'" + parsedURL2 + "'",
                parsedURL1.getPath(), parsedURL2.getPath());
        Assert.assertEquals(msg + " - not the same query params! URL1:'" + parsedURL1 + "' URL2:'" + parsedURL2 + "'",
                parsedURL1.getMultiQueryParameters(), parsedURL2.getMultiQueryParameters());

    }


    private URL getCurrentURL()
    {
        return tester.getDialog().getResponse().getURL();
    }
}
