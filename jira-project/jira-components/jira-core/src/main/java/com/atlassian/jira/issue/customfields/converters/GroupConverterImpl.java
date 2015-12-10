package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.security.groups.GroupManager;
import org.apache.commons.lang.StringUtils;

public class GroupConverterImpl implements GroupConverter
{
    private final GroupManager groupManager;

    public GroupConverterImpl(GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }

    public String getString(Group group)
    {
        if (group == null)
        {
            return "";
        }
        return group.getName();
    }

    public Group getGroup(String stringValue) throws FieldValidationException
    {
        if (StringUtils.isBlank(stringValue))
            return null;

        Group group = groupManager.getGroup(stringValue);
        if (group == null)
        {
            throw new FieldValidationException("Group '" + stringValue + "' was not found in the system");
        }
        return group;
    }

    @Override
    public Group getGroupObject(String stringValue) throws FieldValidationException
    {
        return getGroup(stringValue);
    }
}
