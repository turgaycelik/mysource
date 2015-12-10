package com.atlassian.jira.rest.api.gadget;

import com.atlassian.jira.rest.api.issue.FieldsSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Admin Task Lists bean.
 */
@JsonSerialize (using = FieldsSerializer.class)
public class AdminTaskLists
{
    public GettingStartedTaskList gettingStarted;
    public DoMoreTaskList doMore;

    public DoMoreTaskList getDoMore()
    {
        return doMore;
    }

    public AdminTaskLists setDoMore(DoMoreTaskList doMore)
    {
        this.doMore = doMore;
        return this;
    }

    public GettingStartedTaskList getGettingStarted()
    {
        return gettingStarted;
    }

    public AdminTaskLists setGettingStarted(GettingStartedTaskList gettingStarted)
    {
        this.gettingStarted = gettingStarted;
        return this;
    }
}
