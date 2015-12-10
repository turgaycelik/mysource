package com.atlassian.jira.issue.views;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.issue.views.util.WordViewUtils;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraUrlCodec;

/**
 *
 */
public class SearchRequestWordView extends AbstractSearchRequestFullContentView
{

    public SearchRequestWordView(JiraAuthenticationContext authenticationContext, ApplicationProperties applicationProperties, SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil)
    {
        super(authenticationContext, applicationProperties,searchRequestViewBodyWriterUtil);
    }

    protected Class getIssueViewClass()
    {
        return IssueWordView.class;
    }

    protected boolean showLinkToIssueNavigator()
    {
        return false;
    }

    public void writeHeaders(SearchRequest searchRequest, RequestHeaders requestHeaders, SearchRequestParams searchRequestParams)
    {
        WordViewUtils.writeGenericNoCacheHeaders(requestHeaders);
        WordViewUtils.writeEncodedAttachmentFilenameHeader(
                requestHeaders,
                JiraUrlCodec.encode(SearchRequestViewUtils.getTitle(searchRequest, applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE))) + ".doc",
                searchRequestParams.getUserAgent(),
                applicationProperties.getEncoding());
    }
}
