package com.atlassian.jira.security.util;

import java.util.Collection;

public interface GroupMapper
{
    public Collection getMappedValues(String groupName);
}
