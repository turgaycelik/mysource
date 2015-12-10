package com.atlassian.jira.web.action.func;

import org.apache.commons.lang.StringUtils;

public class ClickOnAnchor implements EventType
{
    public String getTagName()
    {
        return "A";
    }

    public String getEventType()
    {
        return "click";
    }

    public String getAlternativeHandlerMethod()
    {
        return "handleAnchor";
    }

    public String getResponseText(HtmlEvent event)
    {
        String r;
        if (StringUtils.isNotBlank(event.getElementId()))
        {
            r = ("clickLink(\"" + event.getElementId() + "\");");
        }
        else
        {
            r = ("clickLinkWithText(\"" + event.getInnerHtml() + "\");");
        }
        return r;
    }
}
