package com.atlassian.jira.issue.views;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.views.util.SearchRequestViewBodyWriterUtil;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.issue.views.util.WordViewUtils;
import com.atlassian.jira.plugin.searchrequestview.RequestHeaders;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestParams;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.web.component.IssueTableLayoutBean;
import com.atlassian.jira.web.component.IssueTableWebComponent;
import com.atlassian.jira.web.component.IssueTableWriter;
import com.atlassian.jira.web.component.TableLayoutFactory;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Map;

public abstract class AbstractSearchRequestExcelView extends AbstractSearchRequestIssueTableView
{
    protected final TableLayoutFactory tableLayoutFactory;
    private final SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil;
    protected final DateTimeFormatter dateTimeFormatter;

    protected AbstractSearchRequestExcelView(JiraAuthenticationContext authenticationContext, SearchProvider searchProvider, ApplicationProperties appProperties, TableLayoutFactory tableLayoutFactory, SearchRequestViewBodyWriterUtil searchRequestViewBodyWriterUtil, DateTimeFormatter dateTimeFormatter)
    {
        super(authenticationContext, searchProvider, appProperties, searchRequestViewBodyWriterUtil);
        this.tableLayoutFactory = tableLayoutFactory;
        this.searchRequestViewBodyWriterUtil = searchRequestViewBodyWriterUtil;
        this.dateTimeFormatter = dateTimeFormatter != null ? dateTimeFormatter.forLoggedInUser().withStyle(DateTimeStyle.COMPLETE) : null;
    }

    @Override
    public void writeSearchResults(SearchRequest searchRequest, SearchRequestParams searchRequestParams, Writer writer)
    {
        final Map<String, Object> params = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        final IssueTableLayoutBean columnLayout = getColumnLayout(searchRequest, authenticationContext.getLoggedInUser());

        I18nHelper i18nHelper = authenticationContext.getI18nHelper();
        params.put("i18n", i18nHelper);

        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(i18nHelper.getLocale());

        params.put("localeDecimalSeparator", getExcelEscapedSeparator(decimalFormatSymbols.getDecimalSeparator()));
        params.put("localeGroupingSeparator", getExcelEscapedSeparator(decimalFormatSymbols.getGroupingSeparator()));

        params.put("encoding", applicationProperties.getEncoding());
        params.put("colCount", columnLayout.getColumns().size());
        final VelocityRequestContext velocityRequestContext = (VelocityRequestContext) params.get("requestContext");
        params.put("link", SearchRequestViewUtils.getLink(searchRequest, velocityRequestContext.getBaseUrl(), authenticationContext.getLoggedInUser()));

        try
        {
            long numberOfIssues = searchProvider.searchCount(searchRequest.getQuery(), authenticationContext.getLoggedInUser());
            // the pager might not let us display all issues...so don't lie to the user. tell them how many are in the result.
            numberOfIssues = Math.min(numberOfIssues, searchRequestParams.getPagerFilter().getMax());
            if (numberOfIssues == 0)
            {
                params.put("noissues", Boolean.TRUE);
            }

            params.put("generatedInfo", SearchRequestViewUtils.getGeneratedInfo(authenticationContext.getLoggedInUser()));
            params.put("resultsDescription", getResultsDescription(numberOfIssues));

            addLayoutProperties(params);
            params.put("title", SearchRequestViewUtils.getTitle(searchRequest, applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE)));

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

    // When exporting, excel produces a "\" symbol to escape the string in the CSS if the
    // character is a dot or a comma, but not if it is a space.
    private String getExcelEscapedSeparator(char sep) {
        StringBuilder sb = new StringBuilder();
        if (sep == ',' || sep == '.' || sep == '\\')
        {
            sb.append('\\');
        }
        sb.append(sep);
        return sb.toString();
    }

    private String getResultsDescription(long searchResultsSize)
    {
        return authenticationContext.getI18nHelper().getText("navigator.excel.results.displayissues", String.valueOf(searchResultsSize), dateTimeFormatter.format(new Date()));
    }

    private void addLayoutProperties(Map<String, Object> map)
    {
        final LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(applicationProperties);
        final String topBgColour = lookAndFeelBean.getTopBackgroundColour();

        String jiraLogo = lookAndFeelBean.getLogoUrl();
        final String jiraLogoWidth = lookAndFeelBean.getLogoWidth();
        final String jiraLogoHeight = lookAndFeelBean.getLogoHeight();

        if (jiraLogo != null && !jiraLogo.startsWith("http://") && !jiraLogo.startsWith("https://"))
        {
            jiraLogo = (new DefaultVelocityRequestContextFactory(applicationProperties)).getJiraVelocityRequestContext().getBaseUrl() + jiraLogo;
        }

        map.put("topBgColour", topBgColour);
        map.put("jiraLogo", jiraLogo);
        map.put("jiraLogoWidth", jiraLogoWidth);
        map.put("jiraLogoHeight", jiraLogoHeight);
    }

    @Override
    public void writeHeaders(final SearchRequest searchRequest, final RequestHeaders requestHeaders, final SearchRequestParams searchRequestParams)
    {
        WordViewUtils.writeGenericNoCacheHeaders(requestHeaders);
        WordViewUtils.writeEncodedAttachmentFilenameHeader(
                requestHeaders,
                JiraUrlCodec.encode(SearchRequestViewUtils.getTitle(searchRequest, applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE)), true) + ".xls",
                searchRequestParams.getUserAgent(),
                applicationProperties.getEncoding());
    }

    protected abstract IssueTableLayoutBean getColumnLayout(SearchRequest searchRequest, User user);


}
