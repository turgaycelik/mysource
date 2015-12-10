package com.atlassian.jira.rest.api.gadget;

import com.atlassian.jira.rest.api.issue.FieldsSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Admin Task bean.
 */
@JsonSerialize (using = FieldsSerializer.class)
public class AdminTask
{
    public boolean isCompleted;
    public boolean isEnabled;

    public boolean isCompleted()
    {
        return isCompleted;
    }

    public AdminTask setCompleted(boolean completed)
    {
        isCompleted = completed;
        return this;
    }

    public boolean isEnabled()
    {
        return isEnabled;
    }

    public AdminTask setEnabled(boolean enabled)
    {
        isEnabled = enabled;
        return this;
    }
}
