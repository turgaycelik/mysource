package com.atlassian.jira.issue.customfields.statistics;

import com.atlassian.jira.issue.fields.CustomField;

public class TextStatisticsMapper extends AbstractCustomFieldStatisticsMapper
{
    public TextStatisticsMapper(CustomField customField)
    {
        super(customField);
    }

    public Object getValueFromLuceneField(String documentValue)
    {
        return documentValue;
    }

    protected String getSearchValue(Object value)
    {
        return value != null ? value.toString() : "";
    }
}

