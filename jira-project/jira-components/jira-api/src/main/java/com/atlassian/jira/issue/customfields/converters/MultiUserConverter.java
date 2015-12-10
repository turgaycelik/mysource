package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.annotations.Internal;

import java.util.Collection;

@Internal
public interface MultiUserConverter extends UserConverter
{
    public Collection<String> extractUserStringsFromString(String value);
}
