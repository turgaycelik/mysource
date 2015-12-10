package com.atlassian.jira.webtests.ztests.admin.scheme;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * 
 */
@WebTest({Category.FUNC_TEST, Category.ADMINISTRATION, Category.SCHEMES })
public class TestSchemeTools extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testComparisonToolLink()
    {
        navigation.gotoAdmin();
        tester.clickLink("scheme_tools");
        text.assertTextPresent(locator.page(), "Scheme Tools");
        text.assertTextPresent(locator.page(), "Scheme Comparison Tool");
        tester.clickLink("compare_tool");
        text.assertTextPresent(locator.page(), "Scheme Comparison: Select Schemes");
    }

    public void testGroupToRoleMappingToolLink()
    {
        navigation.gotoAdmin();
        tester.clickLink("scheme_tools");
        text.assertTextPresent(locator.page(), "Scheme Tools");
        text.assertTextPresent(locator.page(), "Group to Project Role Mapping Tool");
        tester.clickLink("mapping_tool");
        text.assertTextPresent(locator.page(), "Map Groups to Project Roles: Select Schemes");
    }

    public void testMergeToolLink()
    {
        navigation.gotoAdmin();
        tester.clickLink("scheme_tools");
        text.assertTextPresent(locator.page(), "Scheme Tools");
        text.assertTextPresent(locator.page(), "Scheme Merge Tool");
        tester.clickLink("merge_tool");
        text.assertTextPresent(locator.page(), "Merge Schemes: Select Schemes");
    }

    public void testPurgeToolLink()
    {
        navigation.gotoAdmin();
        tester.clickLink("scheme_tools");
        text.assertTextPresent(locator.page(), "Scheme Tools");
        text.assertTextPresent(locator.page(), "Bulk Delete Schemes Tool");
        tester.clickLink("delete_tool");
        text.assertTextPresent(locator.page(), "Bulk Delete Schemes: Select Schemes");
    }
}
