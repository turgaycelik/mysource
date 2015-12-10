package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.VersionIndexInfoResolver;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;

import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A clause validator that can be used for version clause types and considers permissions
 *
 */
class VersionValuesExistValidator extends ValuesExistValidator
{
    private final VersionIndexInfoResolver versionIndexInfoResolver;
    private final PermissionManager permissionManager;
    private final VersionManager versionManager;

    VersionValuesExistValidator(final JqlOperandResolver operandResolver, VersionIndexInfoResolver versionIndexInfoResolver, PermissionManager permissionManager, VersionManager versionManager, I18nHelper.BeanFactory beanFactory)
    {
        super(operandResolver, beanFactory);
        this.versionIndexInfoResolver = notNull("versionIndexInfoResolver",versionIndexInfoResolver);
        this.permissionManager = notNull("permissionManager", permissionManager);
        this.versionManager = notNull("versionManager", versionManager);
    }

    boolean stringValueExists(final User searcher, final String value)
    {
        final List<String> ids = versionIndexInfoResolver.getIndexedValues(value);
        return versionExists(searcher, ids);
    }

    boolean longValueExist(final User searcher, final Long value)
    {
        final List<String> ids = versionIndexInfoResolver.getIndexedValues(value);
        return versionExists(searcher, ids);
    }

    boolean versionExists(final User searcher, final List<String> ids)
    {
        for (String sid : ids)
        {
            Long id = convertToLong(sid);
            if (id != null)
            {
                final Version version = versionManager.getVersion(id);
                if (version != null && permissionManager.hasPermission(Permissions.BROWSE, version.getProjectObject(), searcher))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private Long convertToLong(String str)
    {
        try
        {
            return Long.parseLong(str);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
