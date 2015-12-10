package com.atlassian.jira.webtests.ztests.security;

import java.util.List;
import java.util.concurrent.Callable;

import javax.ws.rs.core.Response;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.backdoor.PermissionControlExt;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

import org.hamcrest.Matcher;

import static com.atlassian.jira.webtests.ztests.bundledplugins2.rest.util.PropertyAssertions.assertUniformInterfaceException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;

@WebTest ({ Category.FUNC_TEST, Category.SECURITY })
public class TestGlobalPermissions extends FuncTestCase
{
    public static final String GLOBAL_PERMISSION_COMPLETE = "com.atlassian.jira.dev.func-test-plugin:func.test.global.permission";
    public static final String GLOBAL_PERMISSION_ANNON_ALLOWED_COMPLETE = "com.atlassian.jira.dev.func-test-plugin:func.global.permission.anon.allowed";
    public static final String GLOBAL_PERMISSION = "func.test.global.permission";
    public static final String GLOBAL_PERMISSION_ANNON_ALLOWED = "func.global.permission.anon.allowed";
    private PermissionControlExt permissionsControl;

    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
        permissionsControl = new PermissionControlExt(environmentData);
        restartPluginModule(GLOBAL_PERMISSION_COMPLETE);
        restartPluginModule(GLOBAL_PERMISSION_ANNON_ALLOWED_COMPLETE);
    }

    public void testAddingGlobalPermissionToGroup()
    {
        permissionsControl.addGlobalPermissionByKey(GLOBAL_PERMISSION, "jira-users");
        assertThat(permissionsControl.getGlobalPermissionGroupsByKey(GLOBAL_PERMISSION), hasItem("jira-users"));

        // adding anonymous user not allowed
        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                permissionsControl.addAnyoneGlobalPermissionByKey(GLOBAL_PERMISSION);
                return null;
            }
        }, Response.Status.INTERNAL_SERVER_ERROR);

        permissionsControl.addAnyoneGlobalPermissionByKey(GLOBAL_PERMISSION_ANNON_ALLOWED);
    }

    public void testAddingPluggableGlobalPermissionWhenModuleDown()
    {
        backdoor.plugins().disablePluginModule(GLOBAL_PERMISSION_COMPLETE);
        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                permissionsControl.addGlobalPermissionByKey(GLOBAL_PERMISSION, "jira-users");
                return null;
            }
        }, Response.Status.INTERNAL_SERVER_ERROR);
    }

    public void testRemovingPluggableGlobalPermissionWhenModuleDown()
    {
        permissionsControl.addGlobalPermissionByKey(GLOBAL_PERMISSION, "jira-users");
        backdoor.plugins().disablePluginModule(GLOBAL_PERMISSION_COMPLETE);

        assertUniformInterfaceException(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                permissionsControl.removeGlobalPermissionByKey(GLOBAL_PERMISSION, "jira-users");
                return null;
            }
        }, Response.Status.INTERNAL_SERVER_ERROR);
    }

    public void testGroupDoesntHavePluggableGlobalPermissionAfterModulRestart()
    {
        permissionsControl.addGlobalPermissionByKey(GLOBAL_PERMISSION, "jira-users");
        assertThat(permissionsControl.getGlobalPermissionGroupsByKey(GLOBAL_PERMISSION), hasItem("jira-users"));

        backdoor.plugins().disablePluginModule(GLOBAL_PERMISSION_COMPLETE);
        assertTrue(permissionsControl.getGlobalPermissionGroupsByKey(GLOBAL_PERMISSION).isEmpty());

        backdoor.plugins().enablePluginModule(GLOBAL_PERMISSION_COMPLETE);
        assertThat(permissionsControl.getGlobalPermissionGroupsByKey(GLOBAL_PERMISSION), hasItem("jira-users"));
    }

    private void restartPluginModule(final String globalPermissionModuleCompleteKey)
    {
        backdoor.plugins().disablePluginModule(globalPermissionModuleCompleteKey);
        backdoor.plugins().enablePluginModule(globalPermissionModuleCompleteKey);
    }
}
