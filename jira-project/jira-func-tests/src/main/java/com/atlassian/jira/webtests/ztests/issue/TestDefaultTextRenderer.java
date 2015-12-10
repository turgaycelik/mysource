package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v3.13.3
 */
@WebTest ({ Category.FUNC_TEST, Category.FIELDS })
public class TestDefaultTextRenderer extends FuncTestCase
{
    public void testIssueLinksWithMultipleKeysInsideUrl() throws Exception
    {
        administration.restoreBlankInstance();
        String linkKey = navigation.issue().createIssue("homosapien", "Bug", "Test issue to link to");
        final String badUrl = "http://example/" + linkKey + "/" + linkKey;

        String testKey = navigation.issue().createIssue("homosapien", "Bug", "Test issue to test");
        navigation.issue().viewIssue(testKey);
        tester.clickLink("edit-issue");
        tester.setFormElement("environment", "Some bad stuff " + badUrl + " and more");
        tester.submit("Update");
        assertions.getLinkAssertions().assertLinkLocationEndsWith(badUrl, badUrl);
    }
}
