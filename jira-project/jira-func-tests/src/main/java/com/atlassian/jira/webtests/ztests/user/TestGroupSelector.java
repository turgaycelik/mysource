package com.atlassian.jira.webtests.ztests.user;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.meterware.httpunit.HttpUnitOptions;

@WebTest ({ Category.FUNC_TEST, Category.BROWSING })
public class TestGroupSelector extends FuncTestCase
{
    public static final String GROUP_PICKER_CF_NAME = "mypickerofgroups";
    public static final String INVALID_GROUP_NAME = "invalid_group_name";
    public static final String ISSUE_SUMMARY = "This is my summary";

    private String groupPickerId = null;

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        HttpUnitOptions.setScriptingEnabled(true);

        groupPickerId = administration.customFields().
                addCustomField("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker", GROUP_PICKER_CF_NAME);
    }

    public void testCreateIssueWithGroupPicker()
    {
        // Start the create issue operation
        navigation.issue().goToCreateIssueForm(null, null);
        tester.setFormElement("summary", ISSUE_SUMMARY);

        // Assert that the group picker is available
        tester.assertLinkPresent(groupPickerId + "-trigger");

        // Attempt to add invalid group name
        tester.setFormElement(groupPickerId, INVALID_GROUP_NAME);
        tester.submit("Create");
        text.assertTextPresentHtmlEncoded("Could not find group with name '" + INVALID_GROUP_NAME + "'");

        tester.setFormElement(groupPickerId, Groups.USERS);
        tester.submit("Create");

        text.assertTextPresent(locator.page(), ISSUE_SUMMARY);
        text.assertTextPresent(locator.page(), GROUP_PICKER_CF_NAME);
        tester.assertTextPresent(Groups.USERS);
    }
}
