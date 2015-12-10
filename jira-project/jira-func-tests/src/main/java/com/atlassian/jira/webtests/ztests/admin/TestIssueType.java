package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests for the administration of issue types.
 *
 * @since v4.0
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION })
public class TestIssueType extends FuncTestCase
{
    //JRA-18985: It is possible to make an issue type a duplicate.
    public void testSameName() throws Exception
    {
        administration.restoreBlankInstance();
        navigation.gotoAdmin();

        tester.clickLink("issue_types");
        tester.clickLink("add-issue-type");

        //Check to see that we can't add an issue type of the same name.
        addIssueType("Bug");
        assertDuplicateIssueTypeErrorAui();

        addIssueType("bUG");
        assertDuplicateIssueTypeErrorAui();

        //Check to see that we can't change an issue type name to an already existing name.
        tester.gotoPage("secure/admin/EditIssueType!default.jspa?id=4");

        //Check to see that we can't edit a issue type such that becomes a duplicate.
        tester.setFormElement("name", "Bug");
        tester.submit();
        assertDuplicateIssueTypeErrorAui();

        tester.setFormElement("name", "BUG");
        tester.submit();
        assertDuplicateIssueTypeErrorAui();
    }

    private void assertDuplicateIssueTypeError()
    {
        assertions.getJiraFormAssertions().assertFieldErrMsg("An issue type with this name already exists.");
    }

    private void assertDuplicateIssueTypeErrorAui()
    {
        assertions.getJiraFormAssertions().assertAuiFieldErrMsg("An issue type with this name already exists.");
    }

    private void addIssueType(String name)
    {
        tester.setFormElement("name", name);
        tester.submit();
    }
}