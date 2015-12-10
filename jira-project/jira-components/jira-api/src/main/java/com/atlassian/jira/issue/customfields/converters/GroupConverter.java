package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;

@Internal
public interface GroupConverter
{
    public String getString(Group group);

    /**
     * Get the Group Object from the group name
     * @param stringValue
     * @return
     * @throws FieldValidationException
     */
    public Group getGroup(String stringValue) throws FieldValidationException;

    /**
     * Get the Group Object from the group name
     * @param stringValue
     * @return
     * @throws FieldValidationException
     *
     * @deprecated Use {@link #getGroup(String)} instead. Since v5.0.
     */
    public Group getGroupObject(String stringValue) throws FieldValidationException;
}
