package com.atlassian.jira.sharing.type;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * A GlobalShareTypeValidator representing a globally shared {@link com.atlassian.jira.sharing.SharedEntity}.
 *
 * @since v3.13
 */
public class GlobalShareTypeValidator implements ShareTypeValidator
{
    private final PermissionManager permissionManager;

    public GlobalShareTypeValidator(final PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    /**
     * The user must have the global permission to share filters.
     *
     * @param ctx Service context containing user, i18n bean and error collection.
     * @param permission Permission must have type of {@link GlobalShareType#TYPE}
     * @return true if user has Share Filter Global Permission and permission is of right type, else false.
     */
    public boolean checkSharePermission(final JiraServiceContext ctx, final SharePermission permission)
    {
        Assertions.notNull("ctx", ctx);
        Assertions.notNull("permission", permission);
        Assertions.equals(GlobalShareType.TYPE.toString(), GlobalShareType.TYPE, permission.getType());

        final boolean hasPermission = permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, ctx.getLoggedInUser());
        if (!hasPermission)
        {
            ctx.getErrorCollection().addErrorMessage(ctx.getI18nBean().getText("common.sharing.exception.no.share.permission"));
        }
        return hasPermission;
    }

    public boolean checkSearchParameter(final JiraServiceContext ctx, final ShareTypeSearchParameter searchParameter)
    {
        Assertions.notNull("ctx", ctx);
        Assertions.notNull("searchParameter", searchParameter);
        Assertions.equals(GlobalShareType.TYPE.toString(), GlobalShareType.TYPE, searchParameter.getType());
        return true;
    }
}
