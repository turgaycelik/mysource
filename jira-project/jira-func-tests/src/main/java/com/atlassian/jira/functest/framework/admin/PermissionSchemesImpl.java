package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import net.sourceforge.jwebunit.WebTester;

import static com.atlassian.jira.webtests.LegacyProjectPermissionKeyMapping.getKey;

/**
 * Implements the {@link com.atlassian.jira.functest.framework.admin.PermissionSchemes} and
 * {@link com.atlassian.jira.functest.framework.admin.PermissionSchemes.PermissionScheme} interfaces.
 *
 * @since v4.0
 */
public class PermissionSchemesImpl extends AbstractFuncTestUtil implements PermissionSchemes,
        PermissionSchemes.PermissionScheme
{
    public PermissionSchemesImpl(WebTester tester, JIRAEnvironmentData environmentData)
    {
        super(tester, environmentData, 2);
    }

    public PermissionScheme defaultScheme()
    {
        getNavigation().gotoAdminSection("permission_schemes");
        tester.clickLinkWithText("Default Permission Scheme");
        return this;
    }

    public PermissionScheme scheme(String schemeName)
    {
        getNavigation().gotoAdminSection("permission_schemes");
        tester.clickLinkWithText(schemeName);
        return this;
    }

    @Override
    public void grantPermissionToGroup(int permission, String groupName)
    {
        grantPermissionToGroup(getKey(permission), groupName);
    }

    @Override
    public void grantPermissionToGroup(final String permission, final String groupName)
    {
        tester.clickLink("add_perm_" + permission);
        tester.checkCheckbox("type", "group");
        tester.setFormElement("group", groupName);
        tester.submit(" Add ");
    }

    @Override
    public void grantPermissionToReporter(int permission)
    {
        grantPermissionToReporter(getKey(permission));
    }

    @Override
    public void grantPermissionToReporter(String permission)
    {
        tester.clickLink("add_perm_" + permission);
        tester.checkCheckbox("type", "reporter");
        tester.submit(" Add ");
    }

    @Override
    public void grantPermissionToProjectLead(int permission)
    {
        grantPermissionToProjectLead(getKey(permission));
    }

    @Override
    public void grantPermissionToProjectLead(String permission)
    {
        tester.clickLink("add_perm_" + permission);
        tester.checkCheckbox("type", "lead");
        tester.submit(" Add ");
    }

    @Override
    public void grantPermissionToCurrentAssignee(int permission)
    {
        grantPermissionToCurrentAssignee(getKey(permission));
    }

    @Override
    public void grantPermissionToCurrentAssignee(String permission)
    {
        tester.clickLink("add_perm_" + permission);
        tester.checkCheckbox("type", "assignee");
        tester.submit(" Add ");
    }

    @Override
    public void grantPermissionToUserCustomFieldValue(int permission, String customFieldId)
    {
        grantPermissionToUserCustomFieldValue(getKey(permission), customFieldId);
    }

    @Override
    public void grantPermissionToUserCustomFieldValue(String permission, String customFieldId)
    {
        tester.clickLink("add_perm_" + permission);
        tester.checkCheckbox("type", "userCF");
        tester.setFormElement("userCF", customFieldId);
        tester.submit(" Add ");
    }

    @Override
    public void grantPermissionToGroupCustomFieldValue(int permission, String customFieldId)
    {
        grantPermissionToGroupCustomFieldValue(getKey(permission), customFieldId);
    }

    @Override
    public void grantPermissionToGroupCustomFieldValue(String permission, String customFieldId)
    {
        tester.clickLink("add_perm_" + permission);
        tester.checkCheckbox("type", "groupCF");
        tester.setFormElement("groupCF", customFieldId);
        tester.submit(" Add ");
    }

    @Override
    public void grantPermissionToProjectRole(int permission, String projectRoleId)
    {
        grantPermissionToProjectRole(getKey(permission), projectRoleId);
    }

    @Override
    public void grantPermissionToProjectRole(String permission, String projectRoleId)
    {
        tester.clickLink("add_perm_" + permission);
        tester.checkCheckbox("type", "projectrole");
        tester.setFormElement("projectrole", projectRoleId);
        tester.submit(" Add ");
    }

    @Override
    public void grantPermissionToSingleUser(int permission, String username)
    {
        grantPermissionToSingleUser(getKey(permission), username);
    }

    @Override
    public void grantPermissionToSingleUser(final String permission, final String username)
    {
        tester.clickLink("add_perm_" + permission);
        tester.checkCheckbox("type", "user");
        tester.setFormElement("user", username);
        tester.submit(" Add ");
    }

    @Override
    public void removePermission(int permissionType, String permissionParam)
    {
        removePermission(getKey(permissionType), permissionParam);
    }

    @Override
    public void removePermission(final String permission, final String groupName)
    {   if (tester.getDialog().isLinkPresent("del_perm_" + permission + "_" + groupName))
        {
            tester.clickLink("del_perm_" + permission + "_" + groupName);
            tester.submit("Delete");
        }
    }

    protected Navigation getNavigation()
    {
        return getFuncTestHelperFactory().getNavigation();
    }
}
