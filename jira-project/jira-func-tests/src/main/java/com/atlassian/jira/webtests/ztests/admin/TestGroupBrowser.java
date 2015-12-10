package com.atlassian.jira.webtests.ztests.admin;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.locator.XPathLocator;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Test related to the admin Group Browser page
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.USERS_AND_GROUPS })
public class TestGroupBrowser extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
    }

    @Override
    protected void setUpHttpUnitOptions()
    {
        super.setUpHttpUnitOptions();
    }

    public void testGroupPagingWorks()
    {
        addSomeGroups();

        assertGroupNamesIsPresent(0, 19);
        assertNextOnlyIsPresent();

        clickNext();
        assertGroupNamesIsPresent(20, 39);
        assertNextAndPrevOnlyIsPresent();

        clickNext();
        assertGroupNamesIsPresent(40, 49);
        assertPrevOnlyIsPresent();

        clickPrevious();
        assertGroupNamesIsPresent(20, 39);
        assertNextAndPrevOnlyIsPresent();

        clickPrevious();
        assertGroupNamesIsPresent(0, 19);
        assertNextOnlyIsPresent();
    }

    public void testGroupFilteringPagingWorks()
    {
        addSomeGroups();

        assertGroupNamesIsPresent(0, 19);
        assertNextOnlyIsPresent();

        setFilter("03");

        assertGroupNamesIsPresent(3, 3);
        assertGroupNamesIsPresent(30, 39);
        assertNextAndPrevNotPresent();


        // now set a filter that pages
        setFilter("group");

        assertGroupNamesIsPresent(0, 19);
        assertNextOnlyIsPresent();

        assertGroupNamesIsPresent(0, 19);
        assertNextOnlyIsPresent();

        clickNext();
        assertGroupNamesIsPresent(20, 39);
        assertNextAndPrevOnlyIsPresent();

        clickNext();
        assertGroupNamesIsPresent(40, 49);
        assertPrevOnlyIsPresent();

        clickPrevious();
        assertGroupNamesIsPresent(20, 39);
        assertNextAndPrevOnlyIsPresent();

        clickPrevious();
        assertGroupNamesIsPresent(0, 19);
        assertNextOnlyIsPresent();
    }

    private void setFilter(final String filterStr)
    {
        tester.setFormElement("nameFilter", filterStr);
        tester.submit("filter");
    }

    private void assertPrevOnlyIsPresent()
    {
        tester.assertTextNotPresent("Next");
        tester.assertTextPresent("Previous");
    }

    private void assertNextAndPrevOnlyIsPresent()
    {
        tester.assertTextPresent("Next");
        tester.assertTextPresent("Previous");
    }

    private void assertNextAndPrevNotPresent()
    {
        tester.assertTextNotPresent("Next");
        tester.assertTextNotPresent("Previous");
    }

    private void assertNextOnlyIsPresent()
    {
        tester.assertTextPresent("Next");
        tester.assertTextNotPresent("Previous");
    }


    private void clickNext()
    {
        tester.clickLinkWithText("Next >>");
    }

    private void clickPrevious()
    {
        tester.clickLinkWithText("Previous");
    }

    private void assertGroupNamesIsPresent(final int start, final int end)
    {
        List<String> listOfGroupNames = new ArrayList<String>();
        for (int i = start; i <= end; i++)
        {
            listOfGroupNames.add(makeGroupName("group", i));
        }
        final String[] groupNames = listOfGroupNames.toArray(new String[listOfGroupNames.size()]);
        text.assertTextSequence(new XPathLocator(tester, "//table[@id='group_browser_table']"), groupNames);
    }

    private void addSomeGroups()
    {
        for (int i = 0; i < 50; i++)
        {
            addGroup("group", i);
        }
    }

    private void addGroup(String userName, final int i)
    {
        navigation.gotoAdmin();
        tester.clickLink("group_browser");
        tester.setFormElement("addName", makeGroupName(userName, i));
        tester.submit("add_group");
    }

    private String makeGroupName(final String userName, final int i)
    {
        return userName + new DecimalFormat("000").format(i);
    }

    public void testAddBlankGroup()
    {
        navigation.gotoAdmin();
        tester.clickLink("group_browser");
        tester.setFormElement("addName", "");
        tester.submit("add_group");

        tester.assertTextPresent("You must specify valid group name.");
    }

}
