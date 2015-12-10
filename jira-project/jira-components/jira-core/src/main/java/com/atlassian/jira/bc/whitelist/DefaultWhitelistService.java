package com.atlassian.jira.bc.whitelist;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.3
 */
public class DefaultWhitelistService implements WhitelistService
{
    private static final String REGEX_PREFIX = "/";

    private final PermissionManager permissionManager;
    private final WhitelistManager whitelistManager;
    private final I18nHelper.BeanFactory beanFactory;

    public DefaultWhitelistService(final PermissionManager permissionManager,
            final WhitelistManager whitelistManager, final I18nHelper.BeanFactory beanFactory)
    {
        this.permissionManager = permissionManager;
        this.whitelistManager = whitelistManager;
        this.beanFactory = beanFactory;
    }

    @Override
    public WhitelistResult getRules(final JiraServiceContext context)
    {
        notNull("context", context);

        if (checkInvalidPermissions(context))
        {
            return new WhitelistResult(context.getErrorCollection());
        }
        return new WhitelistResult(context.getErrorCollection(), whitelistManager.getRules());
    }

    @Override
    public WhitelistUpdateValidationResult validateUpdateRules(final JiraServiceContext context, final List<String> rules, final boolean disabled)
    {
        notNull("context", context);
        notNull("rules", rules);

        if (checkInvalidPermissions(context))
        {
            return new WhitelistUpdateValidationResult(context.getErrorCollection());
        }

        for (String rule: rules)
        {
            if (rule.startsWith(REGEX_PREFIX))
            {
                try
                {
                    Pattern.compile(rule.substring(REGEX_PREFIX.length()));
                }
                catch (PatternSyntaxException e)
                {
                    final I18nHelper i18n = beanFactory.getInstance(context.getLoggedInUser());
                    context.getErrorCollection().addErrorMessage(i18n.getText("whitelist.admin.errors.bad.pattern", e.getMessage()));
                }
            }
        }

        return new WhitelistUpdateValidationResult(context.getErrorCollection(), rules, disabled);
    }

    @Override
    public WhitelistResult updateRules(final WhitelistUpdateValidationResult result)
    {
        notNull("result", result);
        if(!result.isValid())
        {
            throw new IllegalStateException("Validation result has to be valid!");
        }

        return new WhitelistResult(new SimpleErrorCollection(), whitelistManager.updateRules(result.getRules(), result.getDisabled()));
    }

    @Override
    public boolean isDisabled()
    {
        return whitelistManager.isDisabled();
    }

    @Override
    public boolean isAllowed(final URI uri)
    {
        notNull("uri", uri);

        return whitelistManager.isAllowed(uri);
    }

    boolean checkInvalidPermissions(final JiraServiceContext context)
    {
        if (!permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, context.getLoggedInUser()))
        {
            final I18nHelper i18n = beanFactory.getInstance(context.getLoggedInUser());
            context.getErrorCollection().addErrorMessage(i18n.getText("whitelist.service.permission.error"));
            return true;
        }
        return false;
    }


}
