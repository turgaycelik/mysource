package com.atlassian.jira.functest.framework.assertions;

import com.atlassian.jira.testkit.client.log.FuncTestLogger;
import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.util.text.TextKit;
import com.opensymphony.util.TextUtils;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import net.sourceforge.jwebunit.WebTester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of the {@link TextAssertions}
 *
 * @since v3.13
 */
public class TextAssertionsImpl extends AbstractFuncTestUtil implements TextAssertions, FuncTestLogger
{
    private final WebTester tester;

    public TextAssertionsImpl()
    {
        super(null, null, 2);
        this.tester = null;
    }

    public TextAssertionsImpl(final WebTester tester)
    {
        super(tester, null, 2);
        this.tester = tester;
    }

    public void assertTextPresent(final String expectedText)
    {
        if (!getFullResponseText().contains(expectedText))
        {
            Assert.fail("Expected text '" + expectedText + "' not found in the current page.");
        }
    }

    public void assertTextPresentHtmlEncoded(final String expectedText)
    {
        assertTextPresent(TextUtils.htmlEncode(expectedText, false));
    }

    public void assertTextNotPresent(final String expectedText)
    {
        if (getFullResponseText().contains(expectedText))
        {
            Assert.fail("Text '" + expectedText + "' was found in the current page.");
        }
    }

    public void assertTextPresent(Locator locator, String expectedText)
    {
        assertTextPresentImpl(locator, locator.getText(), expectedText);
    }

    public void assertTextPresent(String srcText, String expectedText)
    {
        assertTextPresentImpl(null, srcText, expectedText);
    }

    public void assertTextPresentNumOccurences(final String expectedText, final int numOccurences)
    {
        assertTextPresentNumOccurences(getFullResponseText(), expectedText, numOccurences);
    }

    public void assertTextPresentNumOccurences(final String srcText, final String subString, final int expectedCount)
    {
        int count = TextKit.getNumOccurences(srcText, subString);
        if (count != expectedCount)
        {
            String occurrences = expectedCount == 1 ? " occurrence" : " occurrences";
            Assert.fail("Expected to find exactly " + expectedCount + occurrences + " of '" + subString + "' but " + count + " were found.");
        }
    }

    public void assertTextPresentNumOccurences(final Locator locator, final String expectedText, final int numOccurences)
    {
        assertTextPresentNumOccurences(locator.getText(), expectedText, numOccurences);
    }

    private void assertTextPresentImpl(Locator locator, String srcText, String expectedText)
    {
        if (srcText == null || srcText.indexOf(expectedText) == -1)
        {
            Assert.fail("The text '" + expectedText + "' could not be found" + (locator == null ? "" : " via locator " + locator));
        }
    }

    public void assertTextNotPresent(String srcText, String expectedText)
    {
        assertTextNotPresentImpl(null, srcText, expectedText);
    }

    public void assertTextNotPresent(Locator locator, String expectedText)
    {
        assertTextNotPresentImpl(locator, locator.getText(), expectedText);
    }

    private void assertTextNotPresentImpl(Locator locator, String srcText, String expectedText)
    {
        if (srcText != null && srcText.indexOf(expectedText) != -1)
        {
            Assert.fail("The text '" + expectedText + "' unexpectantly found" + (locator == null ? "" : " via locator " + locator));
        }
    }

    public void assertTextSequence(Locator locator, String expectedTextSequence[])
    {
        assertTextSequence(locator.getText(), expectedTextSequence);
    }

    public void assertTextSequence(final Locator locator, final String expected1, final String... expected2)
    {
        List<String> expected = new ArrayList<String>(Arrays.asList(expected2));
        expected.add(0, expected1);
        assertTextSequence(locator.getText(), expected.toArray(new String[expected.size()]));
    }

    public void assertAtLeastOneTextPresent(final Locator locator, final String option1, final String... moreOptions)
    {
        final List<String> options = new ArrayList<String>(Arrays.asList(moreOptions));
        options.add(0, option1);
        for (final String option : options)
        {
            try
            {
                assertTextPresent(locator, option);
                return;
            }
            catch (AssertionFailedError ignored)
            {
            }
        }

        Assert.fail("The none of the text strings in " + options.toString() + "' could not be found via locator " + locator);
    }

    public void assertTextSequence(final String srcText, final String expected1, final String... expected2)
    {
        List<String> expected = new ArrayList<String>(Arrays.asList(expected2));
        expected.add(0, expected1);
        assertTextSequence(srcText, expected.toArray(new String[expected.size()]));
    }

    public void assertTextSequence(String srcText, String expectedTextSequence[])
    {
        TextKit.assertContainsTextSequence(srcText, expectedTextSequence);
    }

    private void assertRegexImpl(Locator locator, String srcText, String regexPattern, boolean postiveMatchBehaviour)
    {
        Pattern pattern = Pattern.compile(regexPattern, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(srcText);
        boolean matches = matcher.find();
        if (postiveMatchBehaviour)
        {
            if (!matches)
            {
                Assert.fail("The regex '" + regexPattern + "' did not have any matches" + (locator == null ? "" : " via locator " + locator));
            }
        }
        else
        {
            if (matches)
            {
                Assert.fail("The regex '" + regexPattern + "' unexpectantly has some matches" + (locator == null ? "" : " via locator " + locator));
            }
        }
    }


    public void assertRegexMatch(String srcText, String regexPattern)
    {
        assertRegexImpl(null, srcText, regexPattern, true);
    }

    public void assertRegexNoMatch(String srcText, String regexPattern)
    {
        assertRegexImpl(null, srcText, regexPattern, false);
    }

    public void assertRegexMatch(Locator locator, String regexPattern)
    {
        assertRegexImpl(locator, locator.getText(), regexPattern, true);
    }

    public void assertRegexNoMatch(Locator locator, String regexPattern)
    {
        assertRegexImpl(locator, locator.getText(), regexPattern, false);
    }

    private String getFullResponseText()
    {
        return tester.getDialog().getResponseText();
    }
}
