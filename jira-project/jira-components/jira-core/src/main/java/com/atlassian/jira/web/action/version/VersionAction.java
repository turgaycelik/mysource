package com.atlassian.jira.web.action.version;

import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.action.ActionViewData;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.Map;

/**
 * Provides simple JIRA version information and also demonstrates how use soy templates from a JIRA core action
 *
 * @since v6.0
 */
public class VersionAction extends JiraWebActionSupport
{
    private final BuildUtilsInfo buildUtilsInfo;

    public VersionAction(BuildUtilsInfo buildUtilsInfo)
    {
        this.buildUtilsInfo = buildUtilsInfo;
    }

    @Override
    protected String doExecute() throws Exception
    {
        return SUCCESS;
    }

    @ActionViewData("success")
    public Map<String, Object> getData()
    {
        return MapBuilder.<String, Object>newBuilder()
                .add("version", buildUtilsInfo.getVersion())
                .add("buildNumber", buildUtilsInfo.getApplicationBuildNumber())
                .add("commitId", buildUtilsInfo.getCommitId())
                .add("buildDate", getDateTimeFormatter().format(buildUtilsInfo.getCurrentBuildDate()))
                .toMap();

    }
}
