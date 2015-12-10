package com.atlassian.jira.webtests.ztests.timetracking.legacy;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.parser.issue.ViewIssueDetails;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Responsible for holding tests that verify that the time tracking field can accept and display localized data.
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.I18N, Category.TIME_TRACKING })
public class TestTimeTrackingLocalization extends FuncTestCase
{
        public void testTimeTrackingI18N() throws Exception
    {
        administration.restoreData("TimeTrackingi18n.xml");

        navigation.issue().viewIssue("HSP-1");
        ViewIssueDetails issueDetails = parse.issue().parseViewIssuePage();
        assertEquals("1d 1h 1m", issueDetails.getOriginalEstimate());
        assertEquals("1d 1h 1m", issueDetails.getRemainingEstimate());

        //Fred is in French locale.
        navigation.login(FRED_USERNAME);
        navigation.issue().viewIssue("HSP-1");
        issueDetails = parse.issue().parseViewIssuePage();
        assertEquals("1j 1h 1m", issueDetails.getOriginalEstimate());
        assertEquals("1j 1h 1m", issueDetails.getRemainingEstimate());

        //Anne is in Japanese locale.
        navigation.login("anne");
        navigation.issue().viewIssue("HSP-1");
        issueDetails = parse.issue().parseViewIssuePage();
        assertEquals("1 \u65e5 1 \u6642\u9593 1 \u5206", issueDetails.getOriginalEstimate());
        assertEquals("1 \u65e5 1 \u6642\u9593 1 \u5206", issueDetails.getRemainingEstimate());
    }
}
