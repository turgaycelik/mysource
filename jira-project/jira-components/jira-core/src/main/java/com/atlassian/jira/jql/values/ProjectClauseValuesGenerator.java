package com.atlassian.jira.jql.values;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.comparator.LocaleSensitiveProjectNameComparator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Provides the possible values for projects that the user can see.
 *
 * @since v4.0
 */
public class ProjectClauseValuesGenerator implements ClauseValuesGenerator
{
    private final PermissionManager permissionManager;

    public ProjectClauseValuesGenerator(PermissionManager permissionManager)
    {
        this.permissionManager = permissionManager;
    }

    public Results getPossibleValues(final User searcher, final String jqlClauseName, final String valuePrefix, final int maxNumResults)
    {
        final List<Project> visibleProjects = new ArrayList<Project>(permissionManager.getProjectObjects(Permissions.BROWSE, searcher));

        Collections.sort(visibleProjects, new LocaleSensitiveProjectNameComparator(getLocale(searcher)));

        final List<Result> resultVals = new ArrayList<Result>();

        for (Project visibleProject : visibleProjects)
        {
            if (resultVals.size() == maxNumResults)
            {
                break;
            }
            final String lowerCaseProjName = visibleProject.getName().toLowerCase();
            final String lowerCaseKey = visibleProject.getKey().toLowerCase();
            if (valueMatchesProject(valuePrefix, lowerCaseProjName, lowerCaseKey))
            {
                resultVals.add(new Result(visibleProject.getName(), new String[] {visibleProject.getName(), " (" + visibleProject.getKey() + ")"}));
            }
        }

        return new Results(resultVals);
    }

    private boolean valueMatchesProject(final String valuePrefix, final String lowerCaseProjName, final String lowerCaseKey)
    {
        if (StringUtils.isBlank(valuePrefix))
        {
            return true;
        }
        else if (lowerCaseProjName.startsWith(valuePrefix.toLowerCase()))
        {
            return true;
        }
        else if (lowerCaseKey.startsWith(valuePrefix.toLowerCase()))
        {
            return true;
        }
        return false;
    }

    ///CLOVER:OFF
    Locale getLocale(final User searcher)
    {
        return new I18nBean(searcher).getLocale();
    }
    ///CLOVER:ON

}
