package com.atlassian.jira.issue.views;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.views.util.SearchRequestHeader;
import com.atlassian.jira.issue.views.util.SearchRequestPreviousView;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.IssueTableWebComponent;
import com.atlassian.jira.web.component.IssueTableWriter;
import com.atlassian.jira.web.component.TableLayoutFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class SearchRequestPrintableView extends AbstractSearchRequestIssueTableView
{
    private final SearchRequestHeader searchRequestHeader;
    private final SearchRequestPreviousView searchRequestPreviousView;
    private final TableLayoutFactory tableLayoutFactory;

    public SearchRequestPrintableView(JiraAuthenticationContext authenticationContext, SearchProvider searchProvider, ApplicationProperties appProperties, SearchRequestHeader searchRequestHeader, SearchRequestPreviousView searchRequestPreviousView, TableLayoutFactory tableLayoutFactory, SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil)
    {
        super(authenticationContext, searchProvider, appProperties, searchRequestViewBodyWriterUtil);
        this.searchRequestHeader = searchRequestHeader;
        this.searchRequestPreviousView = searchRequestPreviousView;
        this.tableLayoutFactory = tableLayoutFactory;
    }

    @Override
    public void writeSearchResults(SearchRequest searchRequest, SearchRequestParams searchRequestParams, Writer writer)
    {
        final Map<String, Object> params = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        params.put("searchRequest", searchRequest);
        params.put("i18n", authenticationContext.getI18nHelper());
        final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
        params.put("link", SearchRequestViewUtils.getLink(searchRequest, velocityRequestContext.getBaseUrl(), authenticationContext.getLoggedInUser()));
        params.put("title", SearchRequestViewUtils.getTitle(searchRequest, applicationProperties.getString(APKeys.JIRA_TITLE)));

        final IssueTableLayoutBean columnLayout = tableLayoutFactory.getPrintableLayout(searchRequest, authenticationContext.getLoggedInUser());

        params.put("colCount", columnLayout.getColumns().size());

        try
        {
            params.put("linkToPrevious", searchRequestPreviousView.getLinkToPrevious(searchRequest, descriptor));
            params.put("searchRequestHeader", searchRequestHeader.getHeader(searchRequest, searchRequestParams.getPagerFilter(), descriptor));

            writer.write(descriptor.getHtml("header", params));

            final IssueTableWriter issueTableWriter = new IssueTableWebComponent().getHtmlIssueWriter(writer, columnLayout, null, null);
            searchRequestViewBodyWriterUtil.writeTableBody(writer, issueTableWriter, searchRequest, searchRequestParams.getPagerFilter());
            writer.write(descriptor.getHtml("footer", params));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (SearchException e)
        {
            e.printStackTrace();
        }

    }


}
