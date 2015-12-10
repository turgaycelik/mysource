/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comparator.IssueLongFieldComparator;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.statistics.TimeTrackingStatisticsMapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;

public class TimeSpentSystemField extends AbstractDurationSystemField implements TimeSpentField
{
    public TimeSpentSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext)
    {
        super(IssueFieldConstants.TIME_SPENT, "common.concepts.time.spent", "common.concepts.time.spent", ORDER_DESCENDING, new IssueLongFieldComparator(IssueFieldConstants.TIME_SPENT), templatingEngine, applicationProperties, authenticationContext);
    }

    public LuceneFieldSorter getSorter()
    {
        return TimeTrackingStatisticsMapper.TIME_SPENT;
    }

    public String getHiddenFieldId()
    {
        return IssueFieldConstants.TIMETRACKING;
    }

    protected Long getDuration(Issue issue)
    {
        return issue.getTimeSpent();
    }
}
