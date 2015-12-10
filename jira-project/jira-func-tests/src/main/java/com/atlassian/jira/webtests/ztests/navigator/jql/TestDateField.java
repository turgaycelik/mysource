package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebTable;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Test date fields such as Created.
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestDateField extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestDateField.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    /**
     * Confirm that Look and Feel date formats are being html encoded when rendered.
     * JRA-32516
     *
     * @throws Exception
     */
    public void testHtmlEncodedDateFormat() throws Exception
    {
        navigation.issueNavigator().createSearch("");
        WebTable issueTable = tester.getDialog().getWebTableBySummaryOrId("issuetable");
        assertions.getTableAssertions().assertTableCellHasText(issueTable, 1, 8, "10/Aug/09 <script></script>");
    }
}
