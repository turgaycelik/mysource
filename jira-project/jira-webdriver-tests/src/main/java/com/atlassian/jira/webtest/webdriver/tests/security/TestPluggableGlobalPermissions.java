package com.atlassian.jira.webtest.webdriver.tests.security;

import java.util.List;

import com.atlassian.jira.functest.framework.backdoor.PermissionControlExt;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.pages.admin.GlobalPermissionsPage;
import com.atlassian.jira.testkit.client.util.TestKitLocalEnvironmentData;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.pageobjects.pages.admin.GlobalPermissionsPage.GlobalPermissionRow;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@WebTest (com.atlassian.jira.functest.framework.suite.Category.WEBDRIVER_TEST)
public class TestPluggableGlobalPermissions extends BaseJiraWebTest
{
    private static final String GLOBAL_PERMISSION_KEY_COMPLETE = "com.atlassian.jira.dev.func-test-plugin:func.test.global.permission";
    private static final String GLOBAL_PERMISSION_KEY = "func.test.global.permission";

    private PermissionControlExt permissionControlExt;

    @Before
    public void setup()
    {
        backdoor.restoreBlankInstance();
        backdoor.plugins().disablePluginModule(GLOBAL_PERMISSION_KEY_COMPLETE);
        backdoor.plugins().enablePluginModule(GLOBAL_PERMISSION_KEY_COMPLETE);
        this.permissionControlExt = new PermissionControlExt(new TestKitLocalEnvironmentData());
    }

    @Test
    public void pluggableGlobalPermissionDisplayed()
    {
        permissionControlExt.addGlobalPermissionByKey(GLOBAL_PERMISSION_KEY, "jira-users");
        GlobalPermissionsPage globalPermissionsPage = jira.visit(GlobalPermissionsPage.class);

        List<GlobalPermissionRow> globalPermissions = globalPermissionsPage.getGlobalPermissions();

        assertThat(globalPermissions, Matchers.<GlobalPermissionRow>hasItem(hasProperty("permissionName", is("func.test.global.permission.name"))));
        assertThat(globalPermissions, Matchers.<GlobalPermissionRow>hasItem(hasProperty("secondaryText", is("func.test.global.permission.description"))));
        assertThat(globalPermissions, Matchers.<GlobalPermissionRow>hasItem(hasProperty("groupsAndUsers", hasItem(containsString("jira-users")))));
    }

}
