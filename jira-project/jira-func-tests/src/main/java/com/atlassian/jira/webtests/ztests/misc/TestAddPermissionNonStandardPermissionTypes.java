package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import org.junit.Ignore;

/**
 * <p>
 * Tests the AddPermission action.
 *
 * <p>
 * Note that in order for this test to run, you must add the non-standard permission types "assigneeassignable"
 * and "reportercreate". Currently you edit permission-types.xml and run the test manually.
 *
 * @since v3.12
 */
@Ignore ("Can only be run manually because it requires a non-standard permission-types.xml config")
@WebTest ({ Category.FUNC_TEST, Category.PERMISSIONS })
public class TestAddPermissionNonStandardPermissionTypes extends JIRAWebTest
{

    public TestAddPermissionNonStandardPermissionTypes(String name)
    {
        super(name);
    }


    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    public void testAssignableUser()
    {
        gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLinkWithText("Grant permission");
        checkCheckbox("type", "assignee");
        selectOption("permissions", "Assignable User");
        submit(" Add ");
        assertTextPresent("Errors");
        assertTextPresent("Selected Permission 'Assignable User' is not valid for 'Assignee (show only projects with assignable permission)'.");
    }

    public void testReporterCreate_Create()
    {
        gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLinkWithText("Grant permission");
        checkCheckbox("type", "reporter");
        selectOption("permissions", "Create Issues");
        submit(" Add ");
        assertTextPresent("Errors");
        assertTextPresent("Selected Permission 'Create Issues' is not valid for 'Reporter (show only projects with create permission)'.");
    }

    public void testGroupCreate()
    {
        gotoAdmin();
        clickLink("permission_schemes");
        clickLink("0_edit");
        clickLinkWithText("Grant permission");
        checkCheckbox("type", "group");
        selectOption("permissions", "Create Issues");
        submit(" Add ");
        assertTextPresent("Edit Permissions &mdash; Default Permission Scheme");
        assertTextNotPresent("Errors");
    }

}