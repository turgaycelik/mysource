package com.atlassian.jira.rest.api.gadget;

import com.atlassian.jira.rest.api.issue.FieldsSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Admin Do More Task List bean.
 */
@JsonSerialize (using = FieldsSerializer.class)
public class DoMoreTaskList
{
    public AdminTask tryGreenHopper;
    public AdminTask tryBonfire;
    public boolean isCompleted;
    public boolean isDismissed;

    public boolean isCompleted()
    {
        return isCompleted;
    }

    public DoMoreTaskList setCompleted(boolean completed)
    {
        isCompleted = completed;
        return this;
    }

    public boolean isDismissed()
    {
        return isDismissed;
    }

    public DoMoreTaskList setDismissed(boolean dismissed)
    {
        isDismissed = dismissed;
        return this;
    }

    public AdminTask getTryBonfire()
    {
        return tryBonfire;
    }

    public DoMoreTaskList setTryBonfire(AdminTask tryBonfire)
    {
        this.tryBonfire = tryBonfire;
        return this;
    }

    public AdminTask getTryGreenHopper()
    {
        return tryGreenHopper;
    }

    public DoMoreTaskList setTryGreenHopper(AdminTask tryGreenHopper)
    {
        this.tryGreenHopper = tryGreenHopper;
        return this;
    }
}
