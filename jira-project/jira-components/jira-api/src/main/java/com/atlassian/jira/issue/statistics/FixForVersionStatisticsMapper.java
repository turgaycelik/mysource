package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.project.version.VersionManager;

public class FixForVersionStatisticsMapper extends VersionStatisticsMapper implements StatisticsMapper
{
    public FixForVersionStatisticsMapper(VersionManager versionManager, boolean includeArchived)
    {
        super(SystemSearchConstants.forFixForVersion().getJqlClauseNames().getPrimaryName(), SystemSearchConstants.forFixForVersion().getIndexField(), versionManager, includeArchived);
    }

    public FixForVersionStatisticsMapper(VersionManager versionManager)
    {
        this(versionManager, true);
    }
}
