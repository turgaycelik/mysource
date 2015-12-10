package com.atlassian.jira.issue.fields.rest.json;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * @since v5.2
 */
public class DefaultCommentBeanFactory implements CommentBeanFactory
{
    private final ProjectRoleManager projectRoleManager;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final RendererManager rendererManager;
    private final JiraBaseUrls jiraBaseUrls;
    private final FieldLayoutManager fieldLayoutManager;

    public DefaultCommentBeanFactory(ProjectRoleManager projectRoleManager, DateTimeFormatterFactory dateTimeFormatterFactory, RendererManager rendererManager, JiraBaseUrls jiraBaseUrls, FieldLayoutManager fieldLayoutManager)
    {
        this.projectRoleManager = projectRoleManager;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.rendererManager = rendererManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.fieldLayoutManager = fieldLayoutManager;
    }

    @Override
    @Deprecated
    public CommentJsonBean createBean(Comment comment)
    {
        return createBean(comment, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    @Override
    public CommentJsonBean createBean(Comment comment, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        Assertions.notNull("comment", comment);
        return CommentJsonBean.shortBean(comment, jiraBaseUrls, projectRoleManager, loggedInUser, emailFormatter);
    }

    @Override
    @Deprecated
    public CommentJsonBean createRenderedBean(Comment comment)
    {
        return createRenderedBean(comment, ComponentAccessor.getComponent(JiraAuthenticationContext.class).getUser(), ComponentAccessor.getComponent(EmailFormatter.class));
    }

    @Override
    public CommentJsonBean createRenderedBean(Comment comment, final ApplicationUser loggedInUser, final EmailFormatter emailFormatter)
    {
        Assertions.notNull("comment", comment);

        final IssueRenderContext issueRenderContext = comment.getIssue().getIssueRenderContext();

        final FieldLayout layout = fieldLayoutManager.getFieldLayout(comment.getIssue());
        final FieldLayoutItem fieldLayoutItem = layout.getFieldLayoutItem(IssueFieldConstants.COMMENT);
        final String rendererType = fieldLayoutItem == null ? null : fieldLayoutItem.getRendererType();

        return CommentJsonBean.renderedShortBean(
                comment,
                jiraBaseUrls,
                projectRoleManager,
                dateTimeFormatterFactory,
                rendererManager,
                rendererType,
                issueRenderContext,
                loggedInUser,
                emailFormatter
        );
    }
}
