package com.atlassian.jira.jql.values;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comparator.LocaleSensitiveVersionNameComparator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Gets all the possible versions.
 *
 * @since v4.0
 */
public class VersionClauseValuesGenerator implements ClauseValuesGenerator
{
    private final VersionManager versionManager;
    private final PermissionManager permissionManager;
    private final I18nHelper.BeanFactory beanFactory;

    public VersionClauseValuesGenerator(final VersionManager versionManager, final PermissionManager permissionManager, I18nHelper.BeanFactory beanFactory)
    {
        this.versionManager = versionManager;
        this.permissionManager = permissionManager;
        this.beanFactory = beanFactory;
    }

    public Results getPossibleValues(final User searcher, final String jqlClauseName, final String valuePrefix, final int maxNumResults)
    {
        final List<Version> versions = new ArrayList<Version>(versionManager.getAllVersions());

        Collections.sort(versions, new LocaleSensitiveVersionNameComparator(getLocale(searcher)));

        final Set<Result> versionValues = new LinkedHashSet<Result>();
        for (Version version : versions)
        {
            if (versionValues.size() == maxNumResults)
            {
                break;
            }
            final String lowerCaseVersionName = version.getName().toLowerCase();
            if (StringUtils.isBlank(valuePrefix) || lowerCaseVersionName.startsWith(valuePrefix.toLowerCase()))
            {
                final Project project = version.getProjectObject();
                if (project != null && permissionManager.hasPermission(Permissions.BROWSE, project, searcher))
                {
                    versionValues.add(new Result(version.getName()));
                }
            }
        }

        return new Results(new ArrayList<Result>(versionValues));
    }

    ///CLOVER:OFF
    Locale getLocale(final User searcher)
    {
        return beanFactory.getInstance(searcher).getLocale();
    }
    ///CLOVER:ON

}
