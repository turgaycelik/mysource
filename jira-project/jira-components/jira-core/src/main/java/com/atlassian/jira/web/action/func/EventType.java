package com.atlassian.jira.web.action.func;

public interface EventType
{
    String getTagName();
    String getEventType();

    String getAlternativeHandlerMethod();
    String getResponseText(HtmlEvent event);
}
