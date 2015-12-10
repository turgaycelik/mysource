package com.atlassian.jira.sharing.type;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.search.GroupShareTypeSearchParameter;
import com.atlassian.jira.sharing.search.ShareTypeSearchParameter;
import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.StringUtils;

/**
 * A validator for the {@link com.atlassian.jira.sharing.type.GroupShareType}.
 *
 * @since v3.13
 */
public class GroupShareTypeValidator implements ShareTypeValidator
{
    private final PermissionManager permissionManager;
    private final GroupManager groupManager;

    public GroupShareTypeValidator(final PermissionManager permissionManager, final GroupManager groupManager)
    {
        this.permissionManager = permissionManager;
        this.groupManager = groupManager;
    }

    public boolean checkSharePermission(final JiraServiceContext ctx, final SharePermission permission)
    {
        Assertions.notNull("ctx", ctx);
        Assertions.notNull("ctx.user", ctx.getLoggedInUser());
        Assertions.notNull("permission", permission);
        Assertions.equals(GroupShareType.TYPE.toString(), GroupShareType.TYPE, permission.getType());

        final boolean hasPermission = permissionManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, ctx.getLoggedInUser());
        if (!hasPermission)
        {
            ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY, ctx.getI18nBean().getText("common.sharing.exception.no.share.permission"));
        }
        else
        {
            if (StringUtils.isBlank(permission.getParam1()))
            {
                final String groupName = permission.getParam1() == null ? "" : permission.getParam1();
                ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                    ctx.getI18nBean().getText("common.sharing.exception.group.not.valid", groupName));
            }
            else
            {
                final Group group = getGroup(permission.getParam1());
                if (group == null)
                {
                    ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                            ctx.getI18nBean().getText("common.sharing.exception.group.does.not.exist", permission.getParam1()));
                }
                else if (!groupManager.isUserInGroup(ctx.getLoggedInUser(), group))
                {
                    final String userName =  ctx.getLoggedInUser() != null ? ctx.getLoggedInUser().getDisplayName() : ctx.getI18nBean().getText("common.words.Anonymous");
                    ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                        ctx.getI18nBean().getText("common.sharing.exception.not.in.group", permission.getParam1()));
                    ctx.getErrorCollection().addError(ShareTypeValidator.DELEGATED_ERROR_KEY,
                        ctx.getI18nBean().getText("common.sharing.exception.delegated.user.not.in.group", userName, permission.getParam1()));
                }
            }
        }

        return !ctx.getErrorCollection().hasAnyErrors();
    }

    public boolean checkSearchParameter(final JiraServiceContext ctx, final ShareTypeSearchParameter searchParameter)
    {
        Assertions.notNull("ctx", ctx);
        Assertions.notNull("searchParameter", searchParameter);
        Assertions.equals(GroupShareType.TYPE.toString(), GroupShareType.TYPE, searchParameter.getType());

        // the anonymous user never has permission to search by groups.
        if (ctx.getLoggedInUser() == null)
        {
            ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                ctx.getI18nBean().getText("common.sharing.searching.exception.anonymous.group.search"));
        }
        else
        {
            final GroupShareTypeSearchParameter groupShareTypeSearchParameter = (GroupShareTypeSearchParameter) searchParameter;
            final String groupName = groupShareTypeSearchParameter.getGroupName();
            if (StringUtils.isNotBlank(groupName))
            {
                final Group group = getGroup(groupName);
                if (group == null)
                {
                    ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                            ctx.getI18nBean().getText("common.sharing.exception.group.does.not.exist", groupName));
                }
                else if (!groupManager.isUserInGroup(ctx.getLoggedInUser(), group))
                {
                    ctx.getErrorCollection().addError(ShareTypeValidator.ERROR_KEY,
                        ctx.getI18nBean().getText("common.sharing.searching.exception.not.in.group", groupName));
                    ctx.getErrorCollection().addError(ShareTypeValidator.DELEGATED_ERROR_KEY,
                        ctx.getI18nBean().getText("common.sharing.exception.delegated.user.not.in.group", ctx.getLoggedInUser().getDisplayName(), groupName));
                }
            }
        }
        return !ctx.getErrorCollection().hasAnyErrors();
    }

    ///CLOVER:OFF
    Group getGroup(final String groupName)
    {
        return groupManager.getGroup(groupName);
    }
    ///CLOVER:ON
}
