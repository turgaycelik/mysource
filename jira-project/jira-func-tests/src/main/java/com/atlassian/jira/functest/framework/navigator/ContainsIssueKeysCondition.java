package com.atlassian.jira.functest.framework.navigator;

import com.atlassian.jira.functest.framework.assertions.TextAssertions;
import com.atlassian.jira.functest.framework.locator.TableLocator;
import net.sourceforge.jwebunit.WebTester;

/**
 * A condition which asserts that the table containing search results specifies the issue keys in the correct order.
 *
 * Note: the current implementation is pretty loose - if issue keys appear in other parts of the table besides the
 * issue key column, this might affect the results.
 *
 * @since v4.0
 */
public class ContainsIssueKeysCondition implements SearchResultsCondition
{
    private final TextAssertions assertions;
    private final String[] issueKeys;

    /**
     * @param assertions text assertions
     * @param issueKeys the issue keys that should be present, in the specified order. You probably want to specify at
     * least one issue.
     */
    public ContainsIssueKeysCondition(final TextAssertions assertions, final String... issueKeys)
    {
        this.assertions = assertions;
        this.issueKeys = issueKeys;
    }

    public void assertCondition(final WebTester tester)
    {
        if (issueKeys != null && issueKeys.length > 0)
        {
            final TableLocator locator = new TableLocator(tester, "issuetable");
            assertions.assertTextSequence(locator, issueKeys);
        }
    }
}
