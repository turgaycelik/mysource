package com.atlassian.jira.dev.reference.plugin.actions;

import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.ActionViewDataMappings;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.google.common.collect.Maps;
import webwork.action.Action;

import java.util.Map;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

/**
 * Demonstrates an action that is configured to use a Soy template
 *
 * @since v6.0
 */
public class ReferenceSoyAction extends JiraWebActionSupport
{
    @Override
    protected String doExecute() throws Exception
    {
        Object result = ExecutingHttpRequest.get().getParameter("result");
        if (result != null)
        {
            return result.toString();
        }
        return Action.SUCCESS;
    }

    @ActionViewData
    public String getGreeting()
    {
        return "Greetings Soyling!";
    }

    @ActionViewDataMappings ({ "soyContext", "vmContext" })
    public Map<String, Object> getDataMap()
    {
        Map<String, Object> data = Maps.newHashMap();
        data.put("train", "Cronulla");
        data.put("destination", "Kareela");
        data.put("numLines", parseInt(defaultIfEmpty(getHttpRequest().getParameter("numLines"), "1000")));
        return data;
    }
}
