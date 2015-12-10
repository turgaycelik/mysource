package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestUserNameSearching extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestUserNameSearching.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testSystemFieldSearchByUserName() throws Exception
    {
        _testSearchByUserNameFits("assignee", "assignee");
        _testSearchByFullNameDoesntFit("assignee");
        _testSearchByEmailDoesntFit("assignee");
        _testSearchByEmailReturnsResultsByEmail("assignee");
    }

    public void testUserPickerSearchByUserName() throws Exception
    {
        _testSearchByUserNameFits("userpicker", "customfield_10000");
        _testSearchByFullNameDoesntFit("userpicker");
        _testSearchByEmailDoesntFit("userpicker");
        _testSearchByEmailReturnsResultsByEmail("userpicker");
    }

    /**
     * JRADEV-21991.
     * When a user gets deleted externally we want to still be able to search for that name.
     * @throws Exception
     */
    public void testSearchingUsingDeletedUser() throws Exception
    {
        assertSearchWithResults("assignee = \"deleteduser\"", "HSP-2");
    }

    public void testSearchingUsingDeletedUserDoesntFit() throws Exception
    {
        String jql = "assignee = \"deleteduser\"";
        assertTooComplex(jql);
    }


    public void testUserGroupPickerSearchByUserName() throws Exception
    {
        _testSearchByUserNameFits("usergrouppicker", "customfield_10001");
        _testSearchByFullNameDoesntFit("usergrouppicker");
        _testSearchByEmailDoesntFit("usergrouppicker");
        _testSearchByEmailReturnsResultsByEmail("usergrouppicker");
    }

    public void _testSearchByUserNameFits(String field, String formName) throws Exception
    {
        String jql = field + " = admin";
        assertFitsFilterForm(jql, createFilterFormParam(formName, ADMIN_USERNAME));
        assertSearchWithResults(jql, "HSP-1");
    }

    public void _testSearchByFullNameDoesntFit(String field) throws Exception
    {
        String jql = field + " = " + ADMIN_FULLNAME;
        assertTooComplex(jql);
        assertSearchWithResults(jql, "HSP-1");
    }

    public void _testSearchByEmailDoesntFit(String field) throws Exception
    {
        String jql = field + " = 'admin@example.com'";
        assertTooComplex(jql);
        assertSearchWithResults(jql, "HSP-1");
    }

    public void _testSearchByEmailReturnsResultsByEmail(String field) throws Exception
    {
        assertSearchWithResults(field + " = 'email@example.com'", "HSP-3");
    }
}
