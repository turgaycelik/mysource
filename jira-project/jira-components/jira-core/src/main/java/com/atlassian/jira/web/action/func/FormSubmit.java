package com.atlassian.jira.web.action.func;

public class FormSubmit implements EventType
{
    public String getTagName()
    {
        return "form";
    }

    public String getEventType()
    {
        return "submit";
    }

    public String getAlternativeHandlerMethod()
    {
        return "handleFormSubmit";
    }

    public String getResponseText(HtmlEvent event)
    {
        return null;
    }
}
