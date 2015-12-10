package com.atlassian.jira.event;

import com.atlassian.annotations.PublicApi;
import com.google.common.collect.Maps;

import java.util.Date;
import java.util.Map;

/**
 * An abstract implementation of a JiraEvent
 */
@PublicApi
public abstract class AbstractEvent implements JiraEvent
{
    protected Date time;
    protected Map<String,Object> params;

    public AbstractEvent()
    {
        this.time = new Date();
        this.params = Maps.newHashMap();
    }

    public AbstractEvent(Map<String,Object> params)
    {
        this.time = new Date();
        this.params = params != null ? params : Maps.<String, Object>newHashMap();
    }

    public Date getTime()
    {
        return time;
    }

    public Map<String,Object> getParams()
    {
        return params;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof AbstractEvent))
        {
            return false;
        }

        final AbstractEvent event = (AbstractEvent) o;

        if (params != null ? !params.equals(event.params) : event.params != null)
        {
            return false;
        }
        if (time != null ? !time.equals(event.time) : event.time != null)
        {
            return false;
        }
        return true;
    }

    public int hashCode()
    {
        int result;
        result = (time != null ? time.hashCode() : 0);
        result = 29 * result + (params != null ? params.hashCode() : 0);
        return result;
    }
}
