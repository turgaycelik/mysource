package com.atlassian.jira.webtests.ztests.project;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

@WebTest ({ Category.FUNC_TEST, Category.PROJECTS, Category.ROLES })
public class TestViewProjectRoleUsage extends FuncTestCase
{
    private static final String TEST_SCHEME_NAME_AAAAA = "AAAAA";
    private static final String TEST_SCHEME_NAME_ZZZZ = "ZZZZ";
    private static final String TEST_SCHEME_NAME_bbb = "bbb";
    private static final String TEST_SCHEME_NAME_yy = "yy";

    @Override
    public void setUpTest()
    {
        super.setUpTest();
        administration.restoreData("TestViewProjectRoleUsage.xml");
    }

    public void testSchemeUsageTablesAreOrdered() throws SAXException
    {
        navigation.gotoAdminSection("project_role_browser");
        tester.clickLink("view_Administrators");

        final WebTable notificationSchemeTable = tester.getDialog().getResponse().getTableWithID("relatednotificationschemes");
        assertSchemeUsageTableOrdered(notificationSchemeTable);

        final WebTable permissionSchemeTable = tester.getDialog().getResponse().getTableWithID("relatedpermissionschemes");
        assertSchemeUsageTableOrdered(permissionSchemeTable);

        final WebTable issueSecuritySchemeTable = tester.getDialog().getResponse().getTableWithID("issuesecurityschemes");
        assertSchemeUsageTableOrdered(issueSecuritySchemeTable);
    }

    private void assertSchemeUsageTableOrdered(WebTable table)
    {
        assertions.getTableAssertions().assertTableCellHasText(table, 1, 0, TEST_SCHEME_NAME_AAAAA);
        assertions.getTableAssertions().assertTableCellHasText(table, 2, 0, TEST_SCHEME_NAME_bbb);
        assertions.getTableAssertions().assertTableCellHasText(table, 3, 0, TEST_SCHEME_NAME_yy);
        assertions.getTableAssertions().assertTableCellHasText(table, 4, 0, TEST_SCHEME_NAME_ZZZZ);
    }
}
