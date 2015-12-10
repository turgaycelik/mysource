package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Null;

import java.util.Collections;
import java.util.Set;

/**
 * The default implementation of TrustedApplicationService.
 *
 * @since v3.12
 */
public class DefaultTrustedApplicationService implements TrustedApplicationService
{
    private static final class Keys
    {
        static final String NO_PERMISSION = "admin.errors.trustedapps.no.permission";
    }

    private final TrustedApplicationManager manager;
    private final PermissionCheck permissionCheck;
    private final TrustedApplicationValidator validator;

    ///CLOVER:OFF
    public DefaultTrustedApplicationService(final TrustedApplicationManager manager, final GlobalPermissionManager permissionManager, final TrustedApplicationValidator validator)
    {
        this(manager, new PermissionCheck()
        {
            public boolean check(final User user)
            {
                return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
            }
        }, validator);
    }

    ///CLOVER:ON

    DefaultTrustedApplicationService(final TrustedApplicationManager manager, final PermissionCheck permissionCheck, final TrustedApplicationValidator validator)
    {
        Null.not("manager", manager);
        Null.not("permissionCheck", permissionCheck);
        Null.not("validator", validator);

        this.manager = manager;
        this.permissionCheck = permissionCheck;
        this.validator = validator;
    }

    public Set<TrustedApplicationInfo> getAll(final JiraServiceContext context)
    {
        if (notAllowed(context, Keys.NO_PERMISSION))
        {
            return Collections.emptySet();
        }
        return manager.getAll();
    }

    public TrustedApplicationInfo get(final JiraServiceContext context, final String applicationId)
    {
        if (notAllowed(context, Keys.NO_PERMISSION))
        {
            return null;
        }
        return manager.get(applicationId);
    }

    public TrustedApplicationInfo get(final JiraServiceContext context, final long id)
    {
        if (notAllowed(context, Keys.NO_PERMISSION))
        {
            return null;
        }
        return manager.get(id);
    }

    public boolean delete(final JiraServiceContext context, final long id)
    {
        return !notAllowed(context, Keys.NO_PERMISSION) && manager.delete(context.getLoggedInUser(), id);
    }

    public TrustedApplicationInfo store(final JiraServiceContext context, final TrustedApplicationInfo info)
    {
        if (notAllowed(context, Keys.NO_PERMISSION))
        {
            return null;
        }
        final SimpleTrustedApplication application = new TrustedApplicationBuilder().set(info).toSimple();
        if (!validate(context, application))
        {
            throw new IllegalArgumentException("Invalid TrustedApplication: " + context.getErrorCollection().getErrorMessages());
        }
        return manager.store(context.getLoggedInUser(), info);
    }

    public boolean validate(final JiraServiceContext jiraServiceContext, final SimpleTrustedApplication builder)
    {
        if (notAllowed(jiraServiceContext, Keys.NO_PERMISSION))
        {
            return false;
        }
        final I18nHelper helper = jiraServiceContext.getI18nBean();
        return validator.validate(jiraServiceContext, helper, builder);
    }

    /**
     * Convenience to check the context for whether the user is not allowed to do something here.
     * If not allowed, the I18n text for the specified message key will be added to the error
     * collection.
     *
     * @param context         containing the user and error collection
     * @param errorMessageKey error message key for i18n error message
     * @return true if NOT ALLOWED
     */
    private boolean notAllowed(final JiraServiceContext context, final String errorMessageKey)
    {
        final boolean notAllowed = !permissionCheck.check(context.getLoggedInUser());
        if (notAllowed)
        {
            context.getErrorCollection().addErrorMessage((context.getI18nBean()).getText(errorMessageKey));
        }
        return notAllowed;
    }

    static interface PermissionCheck
    {
        boolean check(User user);
    }
}
