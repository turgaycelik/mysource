package com.atlassian.jira.sharing.type;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.search.ProjectShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Validator for {@link com.atlassian.jira.sharing.type.ProjectShareType}
 *
 * @since v3.13
 */
public class ProjectShareTypeValidator implements ShareTypeValidator
{
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final ProjectRoleManager projectRoleManager;

    public ProjectShareTypeValidator(final PermissionManager permissionManager, final ProjectManager projectManager, final ProjectRoleManager projectRoleManager)
    {
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.projectRoleManager = projectRoleManager;
    }

    /**
     * checks that the user has browse permission for the given project if no role is given, else checks that the
     * user is part of that role for the given project.
     *
     * @param ctx        The service context that contains the user trying to store permissions, the i18n bean and the error collection
     * @param permission The permission to check.  Must have project, and role is optional
     * @return true if the usr passes the above criteria, false otherwise.
     */
    public boolean checkSharePermission(final JiraServiceContext ctx, final SharePermission permission)
    {
        Assertions.notNull("ctx", ctx);
        Assertions.notNull("ctx.user", ctx.getLoggedInApplicationUser());
        Assertions.notNull("permission", permission);
        Assertions.equals(ProjectShareType.TYPE.toString(), ProjectShareType.TYPE, permission.getType());

        final boolean hasPermission = permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, ctx.getLoggedInApplicationUser());
        if (!hasPermission)
        {
            ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY, ctx.getI18nBean().getText("common.sharing.exception.no.share.permission"));
            ctx.getErrorCollection().addError(ShareTypeValidator.DELEGATED_ERROR_KEY,
                                              ctx.getI18nBean().getText("common.sharing.exception.delegated.user.no.share.permission",
                                                                              ctx.getLoggedInApplicationUser().getDisplayName()));
        }
        else
        {

            final Long projectId;
            try
            {
                projectId = new Long(permission.getParam1());
            }
            catch (final NumberFormatException e)
            {
                final String perm = permission.getParam1() == null ? "" : permission.getParam1();
                ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                    ctx.getI18nBean().getText("common.sharing.exception.project.id.not.valid", perm));
                return false;
            }

            final Project project = projectManager.getProjectObj(projectId);

            if (project == null)
            {
                ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                    ctx.getI18nBean().getText("common.sharing.exception.project.does.not.exist"));
            }
            else
            {
                if (permission.getParam2() == null)
                {
                    if (!permissionManager.hasPermission(Permissions.BROWSE, project, ctx.getLoggedInApplicationUser()))
                    {
                        ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                            ctx.getI18nBean().getText("common.sharing.exception.no.permission.project", project.getName()));
                        ctx.getErrorCollection().addError(ShareTypeValidator.DELEGATED_ERROR_KEY,
                            ctx.getI18nBean().getText("common.sharing.exception.delegated.user.no.permission.project", ctx.getLoggedInApplicationUser().getDisplayName(), project.getName()));
                    }
                }
                else
                {

                    final Long roleId;
                    try
                    {
                        roleId = new Long(permission.getParam2());
                    }
                    catch (final NumberFormatException e)
                    {
                        final String perm = permission.getParam2() == null ? "" : permission.getParam2();
                        ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                            ctx.getI18nBean().getText("common.sharing.exception.role.id.not.valid", perm));

                        return false;
                    }
                    final ProjectRole role = projectRoleManager.getProjectRole(roleId);
                    if (role == null)
                    {
                        ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                            ctx.getI18nBean().getText("common.sharing.exception.role.does.not.exist"));
                    }
                    else
                    {
                        // You can be in a role for a project but not have browse.  Doing separate check for diff error message and easier future removal
                        if (!permissionManager.hasPermission(Permissions.BROWSE, project, ctx.getLoggedInApplicationUser()))
                        {
                            ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                                ctx.getI18nBean().getText("common.sharing.exception.no.permission.project.but.in.role", project.getName()));
                              ctx.getErrorCollection().addError(ShareTypeValidator.DELEGATED_ERROR_KEY,
                              ctx.getI18nBean().getText("common.sharing.exception.delegated.user.no.permission.project.but.in.role", ctx.getLoggedInApplicationUser().getDisplayName(), project.getName()));
                        }
                        else if (!projectRoleManager.isUserInProjectRole(ctx.getLoggedInApplicationUser(), role, project))
                        {
                            ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                                ctx.getI18nBean().getText("common.sharing.exception.no.permission.role", project.getName(), role.getName()));
                            ctx.getErrorCollection().addError(ShareTypeValidator.DELEGATED_ERROR_KEY,
                                ctx.getI18nBean().getText("common.sharing.exception.delegated.user.no.permission.role", ctx.getLoggedInApplicationUser().getDisplayName(), project.getName(), role.getName()));

                        }
                    }
                }
            }
        }

        return !ctx.getErrorCollection().hasAnyErrors();
    }

    public boolean checkSearchParameter(final JiraServiceContext serviceCtx, final ShareTypeSearchParameter searchParameter)
    {
        Assertions.notNull("serviceCtx", serviceCtx);
        Assertions.notNull("searchParameter", searchParameter);
        Assertions.equals(ProjectShareType.TYPE.toString(), ProjectShareType.TYPE, searchParameter.getType());

        final ProjectShareTypeSearchParameter projectShareTypeSearchParameter = (ProjectShareTypeSearchParameter) searchParameter;

        final ErrorCollection errorCollection = serviceCtx.getErrorCollection();
        final I18nHelper i18nHelper = serviceCtx.getI18nBean();

        final Long projectId = projectShareTypeSearchParameter.getProjectId();
        final Long roleId = projectShareTypeSearchParameter.getRoleId();
        if (projectId != null)
        {
            final Project project = projectManager.getProjectObj(projectId);
            if (project == null)
            {
                errorCollection.addError(ShareTypeValidator.ERROR_KEY, i18nHelper.getText(
                    "common.sharing.searching.exception.project.does.not.exist", projectId));
            }
            else
            {
                // if they have specified a role then check that it exists and they are in it
                if (roleId != null)
                {
                    final ProjectRole projectRole = projectRoleManager.getProjectRole(roleId);
                    if (projectRole == null)
                    {
                        errorCollection.addError(ShareTypeValidator.ERROR_KEY, i18nHelper.getText(
                            "common.sharing.searching.exception.project.role.does.not.exist", roleId));
                    }
                    else if (!projectRoleManager.isUserInProjectRole(serviceCtx.getLoggedInApplicationUser(), projectRole, project))
                    {
                        errorCollection.addError(ShareTypeValidator.ERROR_KEY,
                            i18nHelper.getText("common.sharing.searching.exception.user.not.in.project.role"));
                    }
                }
                else
                {
                    // if they haven't specified a role, check that they have browse permission
                    final boolean browsePermission = permissionManager.hasPermission(Permissions.BROWSE, project, serviceCtx.getLoggedInApplicationUser());
                    if (!browsePermission)
                    {
                        errorCollection.addError(ShareTypeValidator.ERROR_KEY,
                            i18nHelper.getText("common.sharing.searching.exception.project.no.browse.permission"));
                    }

                }
            }
        }
        return !errorCollection.hasAnyErrors();
    }
}
