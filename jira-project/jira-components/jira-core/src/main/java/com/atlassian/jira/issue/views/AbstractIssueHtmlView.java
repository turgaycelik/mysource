package com.atlassian.jira.issue.views;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.LookAndFeelBean;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.fields.util.FieldPredicates;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.util.AggregateTimeTrackingBean;
import com.atlassian.jira.issue.views.util.IssueViewUtil;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.plugin.issueview.AbstractIssueView;
import com.atlassian.jira.plugin.issueview.IssueViewRequestParams;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.util.FileIconBean;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.config.properties.APKeys.JIRA_ISSUE_ACTIONS_ORDER;

public abstract class AbstractIssueHtmlView extends AbstractIssueView
{
    protected final JiraAuthenticationContext authenticationContext;
    protected final ApplicationProperties applicationProperties;
    protected final CommentManager commentManager;
    protected final FileIconBean fileIconBean;
    protected final FieldScreenRendererFactory fieldScreenRendererFactory;
    protected final IssueViewUtil issueViewUtil;
    private final FieldVisibilityManager fieldVisibilityManager;

    public AbstractIssueHtmlView(final JiraAuthenticationContext authenticationContext,
            final ApplicationProperties applicationProperties, final CommentManager commentManager,
            final FileIconBean fileIconBean, final FieldScreenRendererFactory fieldScreenRendererFactory,
            final IssueViewUtil issueViewUtil, final FieldVisibilityManager fieldVisibilityManager)
    {
        this.authenticationContext = authenticationContext;
        this.applicationProperties = applicationProperties;
        this.commentManager = commentManager;
        this.fileIconBean = fileIconBean;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.issueViewUtil = issueViewUtil;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public String getContent(Issue issue, IssueViewRequestParams issueViewRequestParams)
    {
        return getHeader(issue) + getBody(issue, issueViewRequestParams) + getFooter(issue);
    }

    public String getBody(Issue issue, IssueViewRequestParams issueViewFieldParams)
    {
        ApplicationUser user = authenticationContext.getUser();
        final boolean timeTrackingEnabled = applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        final boolean subTasksEnabled = applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS);
        Map<String, Object> bodyParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        bodyParams.put("issue", issue);
        bodyParams.put("i18n", authenticationContext.getI18nHelper());
        bodyParams.put("outlookdate", authenticationContext.getOutlookDate());
        bodyParams.put("fieldVisibility", fieldVisibilityManager);
        bodyParams.put("timeTrackingEnabled", timeTrackingEnabled);
        bodyParams.put("linkingEnabled", applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING));
        bodyParams.put("subtasksEnabled", subTasksEnabled);
        bodyParams.put("linkCollection", issueViewUtil.getLinkCollection(issue, user == null ? null : user.getDirectoryUser()));
        bodyParams.put("fieldScreenRenderer", fieldScreenRendererFactory.getFieldScreenRenderer(issue, IssueOperations.VIEW_ISSUE_OPERATION, FieldPredicates.isCustomField()));
        bodyParams.put("votingEnabled", applicationProperties.getOption(APKeys.JIRA_OPTION_VOTING));
        bodyParams.put("wordView", this);
        bodyParams.put("remoteUser", user);
        bodyParams.put("fileIconBean", fileIconBean);
        bodyParams.put("stringUtils", new StringUtils());
        bodyParams.put("encoder", new JiraUrlCodec());
        if (timeTrackingEnabled && subTasksEnabled && !issue.isSubTask())
        {
            AggregateTimeTrackingBean bean = issueViewUtil.createAggregateBean(issue);
            if (bean.getSubTaskCount() > 0)
            {
                bodyParams.put("aggregateTimeTrackingBean", issueViewUtil.createTimeTrackingBean(bean, authenticationContext.getI18nHelper()));
            }
        }

        List comments = commentManager.getCommentsForUser(issue, user);
        if (applicationProperties.getDefaultBackedString(JIRA_ISSUE_ACTIONS_ORDER).equals(ACTION_ORDER_DESC))
        {
            Collections.reverse(comments);
        }
        bodyParams.put("comments", comments);
        return descriptor.getHtml("view", bodyParams);
    }

    public String getHeader(Issue issue)
    {
        return getHeader("[#" + issue.getKey() + "] " + issue.getSummary(), getLinkToPrevious(issue));
    }

    protected abstract String getLinkToPrevious(Issue issue);

    /**
     * Get the header.
     *
     * @param title          The title of the page.  A single issue may be different to multiple issues
     * @param linkToPrevious A string containing the complete link to get back to the previous content.  If null, the previous link is not printed
     * @return The header of a single / multiple word / html view
     */
    public String getHeader(String title, String linkToPrevious)
    {
        Map<String, Object> bodyParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        bodyParams.put("title", title);
        bodyParams.put("contentType", descriptor.getContentType() + "; charset=" + applicationProperties.getEncoding());

        final LookAndFeelBean lookAndFeelBean = LookAndFeelBean.getInstance(applicationProperties);
        bodyParams.put("linkColour", lookAndFeelBean.getTextLinkColour());
        bodyParams.put("linkAColour", lookAndFeelBean.getTextActiveLinkColour());
        bodyParams.put("showCssLinks", (printCssLinks()) ? Boolean.TRUE : Boolean.FALSE);
        bodyParams.put("linkToPrevious", linkToPrevious);

        bodyParams.put("style", getStyleSheetHtml());

        return descriptor.getHtml("header", bodyParams);
    }


    /**
     * With a word view of an issue - you do not want to print the links to the CSS (which seem to hang word, as it can't
     * download remote resources)
     *
     * @return true if you want links to CSS to be shown, false otherwise
     */
    protected abstract boolean printCssLinks();


    public String getStyleSheetHtml()
    {
        return descriptor.getHtml("style", new HashMap<String, Object>());
    }

    public String getFooter(Issue issue)
    {
        Map<String, Object> footerParams = JiraVelocityUtils.getDefaultVelocityParams(authenticationContext);
        footerParams.put("generatedInfo", SearchRequestViewUtils.getGeneratedInfo(authenticationContext.getLoggedInUser()));
        return descriptor.getHtml("footer", footerParams);
    }

    public String getPrettyDuration(Long v)
    {
        return issueViewUtil.getPrettyDuration(v);
    }

    public String getRenderedContent(String fieldName, String value, Issue issue)
    {
        return issueViewUtil.getRenderedContent(fieldName, value, issue);
    }

    public String getCustomFieldHtml(FieldLayoutItem fieldLayoutItem, CustomField field, Issue issue)
    {
        // We can pass a null action because none of the searchers views use the action that is passed in. It should
        // probably be removed from the API as it does not make a ton of sense for the view html to try to attach
        // errors to the action.
        Action action = null;
        final Map<String, Object> displayParams = MapBuilder.<String, Object>newBuilder("textOnly", Boolean.TRUE).toMutableMap();

        return field.getViewHtml(fieldLayoutItem, action, issue, displayParams);
    }

}
