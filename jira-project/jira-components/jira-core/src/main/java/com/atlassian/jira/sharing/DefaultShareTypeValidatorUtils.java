package com.atlassian.jira.sharing;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.sharing.type.ShareTypeValidator;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * Default implementation of {@link ShareTypeValidatorUtils}.
 * 
 * @since v3.13
 */
public class DefaultShareTypeValidatorUtils implements ShareTypeValidatorUtils
{
    private final ShareTypeFactory shareTypeFactory;
    private final PermissionManager permissionManager;

    public DefaultShareTypeValidatorUtils(final ShareTypeFactory shareTypeFactory, final PermissionManager permissionManager)
    {
        Assertions.notNull("shareTypeFactory", shareTypeFactory);
        Assertions.notNull("permissionManager", permissionManager);

        this.permissionManager = permissionManager;
        this.shareTypeFactory = shareTypeFactory;
    }

    public boolean isValidSharePermission(final JiraServiceContext context, final SharedEntity entity)
    {
        Assertions.notNull("entity", entity);
        final SharePermissions permissions = entity.getPermissions();
        Assertions.notNull("permissions", permissions);

        if (!permissions.isPrivate())
        {
            final boolean hasPermission = permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, context.getLoggedInUser());
            if (!hasPermission)
            {
                final String userName =  context.getLoggedInUser() != null ? context.getLoggedInUser().getDisplayName() : context.getI18nBean().getText("common.words.Anonymous");
                context.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                    context.getI18nBean().getText("common.sharing.exception.no.share.permission"));
                    context.getErrorCollection().addError(ShareTypeValidator.DELEGATED_ERROR_KEY,
                        context.getI18nBean().getText("common.sharing.exception.delegated.user.no.share.permission", userName));
                return false;
            }

            for (final SharePermission sharePermission : permissions)
            {
                final ShareType type = shareTypeFactory.getShareType(sharePermission.getType());
                if (type == null)
                {
                    context.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                        context.getI18nBean().getText("common.sharing.exception.unknown.type", sharePermission.getType()));
                }
                else
                {
                    if (type.isSingleton() && (permissions.size() > 1))
                    {
                        context.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                            context.getI18nBean().getText("common.sharing.exception.singleton", sharePermission.getType()));
                    }
                    type.getValidator().checkSharePermission(context, sharePermission);
                }
            }
        }

        return context.getErrorCollection().hasAnyErrors();
    }

    public boolean isValidSearchParameter(final JiraServiceContext context, final ShareTypeSearchParameter searchParameter)
    {
        Assertions.notNull("context", context);
        Assertions.notNull("searchParameter", searchParameter);

        final ShareType.Name shareType = searchParameter.getType();
        final ShareType type = shareTypeFactory.getShareType(shareType);
        if (type == null)
        {
            context.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                context.getI18nBean().getText("common.sharing.exception.unknown.type", shareType));
            return false;
        }
        return type.getValidator().checkSearchParameter(context, searchParameter);
    }
}
