package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.annotations.Internal;

import java.util.Collection;

@Internal
public interface MultiGroupConverter extends GroupConverter
{
    public Collection<String> extractGroupStringsFromString(String value);
}
