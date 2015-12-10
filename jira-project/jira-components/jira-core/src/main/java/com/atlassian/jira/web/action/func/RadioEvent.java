package com.atlassian.jira.web.action.func;

public class RadioEvent implements EventType
{
    public String getTagName()
    {
        return "input";
    }

    public String getEventType()
    {
        return "blur";
    }

    public String getAlternativeHandlerMethod()
    {
        return "handleInput";
    }

    public String getResponseText(HtmlEvent event)
    {
        return null;
    }
}
