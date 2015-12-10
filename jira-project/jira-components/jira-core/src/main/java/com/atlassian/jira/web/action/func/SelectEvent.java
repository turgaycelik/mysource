package com.atlassian.jira.web.action.func;

public class SelectEvent implements EventType
{
    public String getTagName()
    {
        return "select";
    }

    public String getEventType()
    {
        return "change";
    }

    public String getAlternativeHandlerMethod()
    {
        return "handleSelect";
    }

    public String getResponseText(HtmlEvent event)
    {
        return null;
    }
}
