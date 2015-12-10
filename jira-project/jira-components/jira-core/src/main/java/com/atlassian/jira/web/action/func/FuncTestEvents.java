package com.atlassian.jira.web.action.func;

import org.apache.commons.collections.map.ListOrderedMap;
import webwork.action.ActionContext;

public class FuncTestEvents extends ListOrderedMap
{
    public static FuncTestEvents getInstance()
    {
        FuncTestEvents funcTestEvents = (FuncTestEvents) ActionContext.getSession().get(FuncTestEvents.class.getName());
        if (funcTestEvents == null)
        {
            funcTestEvents = new FuncTestEvents();
            ActionContext.getSession().put(FuncTestEvents.class.getName(), funcTestEvents);
        }

        return funcTestEvents;
    }
}
