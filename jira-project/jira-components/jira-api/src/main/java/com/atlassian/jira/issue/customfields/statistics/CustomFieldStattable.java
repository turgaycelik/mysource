package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.statistics.StatisticsMapper;

@PublicSpi
public interface CustomFieldStattable
{
    public StatisticsMapper getStatisticsMapper(CustomField customField);
}
