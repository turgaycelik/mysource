package com.atlassian.jira.web.action.func;

public interface HtmlEvent
{
    String getElementId();
    String getEventType();
    String getTagName();
    String getInnerHtml();
}
