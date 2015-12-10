package com.atlassian.jira.jelly;

import org.apache.commons.jelly.DynaBeanTagSupport;

import java.util.Map;

public abstract class JiraDynaBeanTagSupport extends DynaBeanTagSupport
{
    public JiraDynaBeanTagSupport()
    {
        super(new ActionTagSupportDynaBean(new ActionTagSupportDynaClass()));
    }

    public Map getProperties()
    {
        return ((ActionTagSupportDynaBean) getDynaBean()).getProperties();
    }
}
