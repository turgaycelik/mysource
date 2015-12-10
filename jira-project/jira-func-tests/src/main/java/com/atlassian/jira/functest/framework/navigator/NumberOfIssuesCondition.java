package com.atlassian.jira.functest.framework.navigator;

import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.locator.CssLocator;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.framework.locator.Locator;
import com.atlassian.jira.functest.framework.locator.WebPageLocator;
import net.sourceforge.jwebunit.WebTester;

/**
 * A condition which asserts that the number of results returned by a search matches an exact number. This is done by
 * matching the text above the issue table which states "Displaying a to b of x issues"; we utilise the particular DOM
 * structure of the DIV containing the text.
 *
 * @since v4.0
 */
public class NumberOfIssuesCondition implements SearchResultsCondition
{
    private final TextAssertions assertions;
    private final Integer numberOfIssues;

    /**
     * @param assertions text assertions
     * @param numberOfIssues the exact number of results expected. Should be >= 0.
     */
    public NumberOfIssuesCondition(final TextAssertions assertions, final Integer numberOfIssues)
    {
        this.assertions = assertions;
        this.numberOfIssues = numberOfIssues;
    }

    public void assertCondition(final WebTester tester)
    {
        if (numberOfIssues <= 0)
        {
//            final WebPageLocator locator = new WebPageLocator(tester);
//            assertions.assertTextPresent(locator, "No matching issues found.");
        }
        else
        {
            final Locator locator = new CssLocator(tester, ".results-count-total");
            assertions.assertTextPresent(locator, numberOfIssues.toString());
        }
    }
}