package com.atlassian.jira.event.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.event.AbstractEvent;

/**
 * Event that is triggered when a user changes from simple to advanced or vice versa
 */
@PublicApi
public final class SwitchIssueSearchEvent extends AbstractEvent
{
    private final String type;
    private final boolean tooComplex;

    public SwitchIssueSearchEvent(String type, boolean tooComplex)
    {
        this.type = type;
        this.tooComplex = tooComplex;
    }

    public String getType()
    {
        return type;
    }

    public boolean isTooComplex()
    {
        return tooComplex;
    }
}
