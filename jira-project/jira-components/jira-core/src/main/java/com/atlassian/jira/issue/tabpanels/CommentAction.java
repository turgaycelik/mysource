package com.atlassian.jira.issue.tabpanels;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.atlassian.fugue.Option;
import com.atlassian.fugue.Options;
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.comment.CommentFieldRenderer;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.webfragment.model.CommentHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraVelocityHelper;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opensymphony.util.TextUtils;

import org.apache.log4j.Logger;

import static com.atlassian.jira.datetime.DateTimeStyle.COMPLETE;
import static com.google.common.collect.Iterables.transform;
import static com.opensymphony.util.TextUtils.htmlEncode;

/**
 * This class is the wrapper around the comment object and is used when displaying comments in the View Issue page,
 * on the 'Comment' issue tab panel.
 */
@SuppressWarnings ( { "UnusedDeclaration" })
public class CommentAction extends AbstractIssueAction
{
    private static final Logger log = Logger.getLogger(CommentAction.class);

    private final Comment comment;
    private final Issue issue;
    private final boolean canEditComment;
    private final boolean canDeleteComment;
    private final boolean isCollapsed;
    private final RendererManager rendererManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final DateTimeFormatter dateTimeFormatter;
    private final CommentFieldRenderer commentFieldRenderer;
    private final CommentPropertyService commentPropertyService;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public CommentAction(IssueTabPanelModuleDescriptor descriptor,
            Comment comment,
            boolean canEditComment,
            boolean canDeleteComment,
            boolean isCollapsed,
            RendererManager rendererManager,
            FieldLayoutManager fieldLayoutManager,
            DateTimeFormatter dateTimeFormatter,
            CommentFieldRenderer commentFieldRenderer,
            CommentPropertyService commentPropertyService,
            JiraAuthenticationContext jiraAuthenticationContext)
    {
        super(descriptor);
        this.comment = comment;
        this.dateTimeFormatter = dateTimeFormatter;
        this.commentFieldRenderer = commentFieldRenderer;
        this.commentPropertyService = commentPropertyService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.issue = comment.getIssue();
        this.canEditComment = canEditComment;
        this.canDeleteComment = canDeleteComment;
        this.isCollapsed = isCollapsed;
        this.rendererManager = rendererManager;
        this.fieldLayoutManager = fieldLayoutManager;
    }

    /**
     * Returns the comment created date
     * @return the comment created date
     */
    public Date getTimePerformed()
    {
        return comment.getCreated();
    }

    /**
     * This will populate the passed in map with this object referenced as "action" and the rendered comment body as
     * "renderedContent".
     * @param params map of params to populate
     */
    protected void populateVelocityParams(Map params)
    {
        params.put("action", this);
        params.put("velocityhelper", new JiraVelocityHelper(ComponentAccessor.getFieldManager()));
        params.put("requestContext", new DefaultVelocityRequestContextFactory(ComponentAccessor.getApplicationProperties()).getJiraVelocityRequestContext());
        params.put("userformat", ComponentAccessor.getComponent(UserFormatManager.class));
        params.put("textutils", new TextUtils());

        try
        {
            FieldLayoutItem fieldLayoutItem = fieldLayoutManager.getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(IssueFieldConstants.COMMENT);
            if (fieldLayoutItem != null)
            {
                params.put("renderedContent", rendererManager.getRenderedContent(fieldLayoutItem.getRendererType(), comment.getBody(), issue.getIssueRenderContext()));
            }
        }
        catch (DataAccessException e)
        {
            log.error(e);
        }
    }

    @Override
    public String getHtml()
    {
        final Map<String, Object> params = Maps.newHashMap();

        populateVelocityParams(params);

        return commentFieldRenderer.getIssuePageViewHtml(params, CommentHelper.builder().issue(issue).comment(comment).build());
    }

    //-------------------------------------------------------------------------------- Methods used by velocity template

    /**
     * Returns the comment
     * @return the comment
     */
    public Comment getComment()
    {
        return comment;
    }

    public List<EntityProperty> getCommentProperties()
    {
        // This is calculated in the getter for the reason. There is not a lot usages of comment properties, so it is better
        // to calculate this once this is needed.
        final ApplicationUser applicationUser = jiraAuthenticationContext.getUser();

        Iterable<Option<EntityProperty>> propOptions = transform(commentPropertyService.getPropertiesKeys(applicationUser, comment.getId()).getKeys(), new Function<String, Option<EntityProperty>>()
        {
            @Override
            public Option<EntityProperty> apply(final String propertyKey)
            {
                return commentPropertyService.getProperty(applicationUser, comment.getId(), propertyKey).getEntityProperty();
            }
        });

        return Lists.newArrayList(Options.flatten(propOptions));
    }

    /**
     * Returns issue related to this comment
     * @return issue related to this comment
     */
    public Issue getIssue()
    {
        return issue;
    }

    /**
     * Returns true is comment is editable, false otherwise
     * @return true is comment is editable, false otherwise
     */
    public boolean isCanEditComment()
    {
        return canEditComment;
    }

    /**
     * Returns true is comment can be deleted, false otherwise
     * @return true is comment can be deleted, false otherwise
     */
    public boolean isCanDeleteComment()
    {
        return canDeleteComment;
    }

    /**
     * @return true if comment should be rendered already collapsed
     */
    public boolean isCollapsed()
    {
        return isCollapsed;
    }

    public String formatDisplayHtml(Date date)
    {
        if (date == null)
        {
            return null;
        }

        DateTimeFormatter completeFormatter = dateTimeFormatter().withStyle(COMPLETE);
        return htmlEncode(completeFormatter.format(date));
    }

    @SuppressWarnings ( { "UnusedDeclaration" })
    public String formatIso8601Html(Date date)
    {
        if (date == null)
        {
            return null;
        }

        DateTimeFormatter iso8601Formatter = dateTimeFormatter().withStyle(DateTimeStyle.ISO_8601_DATE_TIME);
        return htmlEncode(iso8601Formatter.format(date));
    }

    /**
     * Returns a DateTimeFormatter for the logged in user.
     *
     * @return a DateTimeFormatter
     */
    protected DateTimeFormatter dateTimeFormatter()
    {
        return dateTimeFormatter.forLoggedInUser();
    }
}
