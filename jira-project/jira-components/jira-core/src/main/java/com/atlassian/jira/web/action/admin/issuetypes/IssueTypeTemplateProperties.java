package com.atlassian.jira.web.action.admin.issuetypes;

import java.util.Collection;
import java.util.Map;

import com.atlassian.jira.web.action.ActionViewData;

/**
 * Created by dszuksztul on 26/03/14.
 */
public interface IssueTypeTemplateProperties
{
    public static interface IssueTypeViewData
    {
        public String getName();

        public String getDescription();

        public String getId();

        public Long getAvatarId();

        public String getIconUrlContent();
    }

    Long getAvatarId();

    @ActionViewData (key = "issueType")
    IssueTypeViewData getIssueTypeValue();

    @ActionViewData
    String getAction();

    @ActionViewData
    String getCancelAction();

    @ActionViewData
    String getActiveTab();

    @ActionViewData
    String getToken();

    @ActionViewData(key = "errors")
    Map<String, Object> getWrappedErrorsForView();

    @ActionViewData
    Collection<String> getErrorMessages();

    @ActionViewData
    String getDefaultAvatarId();

    @ActionViewData
    String getEditTitleTextId();
}
