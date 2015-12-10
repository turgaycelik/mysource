package com.atlassian.jira.webtests.ztests.issue;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.rules.RestRule;
import com.atlassian.jira.webtests.util.issue.IssueInlineEdit;

/**
 * Test inline editing of fields from the view issue page.
 * <p/>
 * Note: this test should ideally be moved to the issue-nav plugin. The problem is that the build plan for the issue-nav
 * plugin master doesn't compile against jira master.
 *
 * @since v5.1
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestInlineEditIssueFields extends FuncTestCase
{

    private RestRule restRule;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        restRule = new RestRule(this);
        restRule.before();
        administration.restoreData("TestEditIssueVersion.xml");
        navigation.login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    @Override
    public void tearDownTest()
    {
        restRule.after();
    }


    public void testInlineEditIssueType() throws Exception
    {
        testInlineEditField("issuetype", "type-val", "Bug", "4", "Improvement");
    }

    public void testInlineEditPriority() throws Exception
    {
        testInlineEditField("priority", "priority-val", "Major", "4", "Minor");
    }

    public void testInlineEditDescription() throws Exception
    {
        testInlineEditField("description", "description-val", "oneoneoneoneoneoneoneoneone", "blablabla", "blablabla");
    }

    private void testInlineEditField(String fieldName, String fieldId, String oldValue, String newFormValue, String newTextValue)
            throws Exception
    {
        navigation.issue().gotoIssue("MK-1");
        assertions.assertNodeByIdHasText(fieldId, oldValue);

        IssueInlineEdit inlineEdit = new IssueInlineEdit(locator, tester, new RestRule(this));
        inlineEdit.inlineEditField("10020", fieldName, newFormValue);

        // Refresh the page
        navigation.issue().gotoIssue("MK-1");
        // Make sure the field has the new value
        assertions.assertNodeByIdDoesNotHaveText(fieldId, oldValue);
        assertions.assertNodeByIdHasText(fieldId, newTextValue);
    }
}
