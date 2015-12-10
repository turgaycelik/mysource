package com.atlassian.jira.web.action.util;

import java.util.Map;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.web.action.ActionViewDataMappings;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.MetalResourcesManager;

import com.google.common.collect.ImmutableMap;

/**
 * Needed to cleanup the webwork ActionContext to avoid it from throwing thread corrupted errors.
 *
 * @since v4.4
 */
public class ImportResult extends JiraWebActionSupport
{
    @ActionViewDataMappings ({ "success", "input", "error" })
    public Map<String, Object> getContextParams()
    {
        return ImmutableMap.<String, Object>builder()
                .put("jiraTitle", getApplicationProperties().getDefaultBackedString(APKeys.JIRA_TITLE))
                .put("jiraLogoUrl", getApplicationProperties().getDefaultBackedString(APKeys.JIRA_LF_LOGO_URL))
                .put("resourcesContent", MetalResourcesManager.getMetalResources(getHttpRequest().getContextPath()))
                .build();
    }
}
