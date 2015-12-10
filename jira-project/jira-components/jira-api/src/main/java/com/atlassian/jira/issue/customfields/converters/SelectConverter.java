package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;

@Internal
public interface SelectConverter
{
    public static final Long ALL_LONG = new Long(-1);
    public static final String ALL_STRING = "-1";

    public String getString(Option value);

    public Option getObject(String stringValue);

    public SearchContext getPossibleSearchContextFromValue(Option value, CustomField customField);
}