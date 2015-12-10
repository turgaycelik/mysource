package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.SystemTenantOnly;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Verify the behaviour of custom fields in JQL queries when they have been disabled.
 * @since v4.0
 */
@SystemTenantOnly
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestCustomFieldsDisabled extends FuncTestCase
{
    @Override
    protected void tearDownTest()
    {
        administration.plugins().enablePlugin("com.atlassian.jira.plugin.system.customfieldtypes");
        super.tearDownTest();
    }

    public void testDisable() throws Exception
    {
        administration.restoreData("TestCustomFieldOperators.xml");

        final String[] customFields = new String[] {
                "CSF",
                "DP",
                "DT",
                "FTF",
                "GP",
                "II",
                "MC",
                "MGP",
                "MS",
                "MUP",
                "NF",
                "PP",
                "RB",
                "ROTF",
                "SL",
                "SVP",
                "TF",
                "UP",
                "URL",
                "VP"
        };

        for (String cf : customFields)
        {
            navigation.issueNavigator().createSearch(String.format("%s is not empty", cf));
            assertions.getIssueNavigatorAssertions().assertNoJqlErrors();
        }

        navigation.gotoAdmin();
        administration.plugins().disablePlugin("com.atlassian.jira.plugin.system.customfieldtypes");

        for (String cf : customFields)
        {
            issueTableAssertions.assertSearchWithError(String.format("%s is not empty", cf), String.format("Field '%s' does not exist or you do not have permission to view it.", cf));
        }
    }
}
