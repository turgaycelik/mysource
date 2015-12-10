package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.project.version.VersionManager;

public class RaisedInVersionStatisticsMapper extends VersionStatisticsMapper implements StatisticsMapper
{
    public RaisedInVersionStatisticsMapper(VersionManager versionManager)
    {
        this(versionManager, true);
    }

    public RaisedInVersionStatisticsMapper(VersionManager versionManager, boolean includeArchived)
    {
        super(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), DocumentConstants.ISSUE_VERSION, versionManager, includeArchived);
    }
}
