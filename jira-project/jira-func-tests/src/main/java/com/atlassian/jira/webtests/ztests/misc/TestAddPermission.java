package com.atlassian.jira.webtests.ztests.misc;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests the AddPermission action.
 */
@WebTest ({ Category.FUNC_TEST, Category.PERMISSIONS })
public class TestAddPermission extends FuncTestCase
{
    private static final String PROJECT_PERMISSION_KEY = "func.test.project.permission";
    private static final String PROJECT_PERMISSION_KEY_COMPLETE = "com.atlassian.jira.dev.func-test-plugin:func.test.project.permission";

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
    }

    public void testValidationOnPermission()
    {
        administration.permissionSchemes().defaultScheme();
        
        tester.clickLinkWithText("Grant permission");
        tester.checkCheckbox("type", "group");

        // if we don't choose any permissions, we should get a validation error
        tester.submit(" Add ");
        text.assertTextPresent("Errors");
        text.assertTextPresent("You must select a permission to add.");
    }

    public void testCannotAddEntryToDisabledPermission()
    {
        backdoor.plugins().enablePluginModule(PROJECT_PERMISSION_KEY_COMPLETE);

        administration.permissionSchemes().defaultScheme();
        tester.clickLink("add_perm_" + PROJECT_PERMISSION_KEY);

        tester.checkCheckbox("type", "group");

        backdoor.plugins().disablePluginModule(PROJECT_PERMISSION_KEY_COMPLETE);

        tester.submit(" Add ");
        text.assertTextPresent("Errors");
        text.assertTextPresent("Permission with key &#39;" + PROJECT_PERMISSION_KEY + "&#39; doesn&#39;t exist.");
    }
}
