package com.atlassian.jira.rest.v2.issue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.visibility.Visibilities;
import com.atlassian.jira.bc.issue.visibility.Visibility;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentsWithPaginationJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;

import org.apache.log4j.Logger;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since 4.2
 */
public class CommentResource
{
    private static final Logger log = Logger.getLogger(CommentResource.class);
    private final static String COMMENT_FIELD_KEY = "comment";
    private CommentService commentService;

    private JiraAuthenticationContext authContext;
    private I18nHelper i18n;
    private ProjectRoleManager projectRoleManager;
    private JiraBaseUrls jiraBaseUrls;
    private IssueFinder issueFinder;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;
    private final RendererManager rendererManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final EmailFormatter emailFormatter;

    @SuppressWarnings ({ "UnusedDeclaration" })
    private CommentResource(IssueFinder issueFinder, DateTimeFormatterFactory dateTimeFormatterFactory, RendererManager rendererManager,
            FieldLayoutManager fieldLayoutManager, final EmailFormatter emailFormatter)
    {
        // this constructor used by tooling
        this.issueFinder = issueFinder;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.rendererManager = rendererManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.emailFormatter = emailFormatter;
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    public CommentResource(CommentService commentService, JiraAuthenticationContext authContext, I18nHelper i18n,
            ProjectRoleManager projectRoleManager, JiraBaseUrls jiraBaseUrls, IssueFinder issueFinder,
            DateTimeFormatterFactory dateTimeFormatterFactory, RendererManager rendererManager,
            FieldLayoutManager fieldLayoutManager, final EmailFormatter emailFormatter)
    {
        this.authContext = authContext;
        this.commentService = commentService;
        this.i18n = i18n;
        this.projectRoleManager = projectRoleManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.issueFinder = issueFinder;
        this.dateTimeFormatterFactory = dateTimeFormatterFactory;
        this.rendererManager = rendererManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.emailFormatter = emailFormatter;
    }

    /**
     * Returns all comments for an issue.
     *
     * @param issueIdOrKey to get comments for
     * @param expand optional flags: renderedBody (provides body rendered in HTML), properties (provides comment
     * properties).
     * @return all comments for the issue
     */
    public Response getComments(final String issueIdOrKey, String expand)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        if (issue == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.error.issue.invalid", issueIdOrKey)));
        }

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        List<Comment> commentsForUser = commentService.getCommentsForUser(authContext.getUser(), issue);
        if (!errorCollection.hasAnyErrors())
        {
            Collection<CommentJsonBean> commentJsonBeans;
            if (expand != null)
            {
                commentJsonBeans =
                        CommentJsonBean.expandedShortBeans(commentsForUser, jiraBaseUrls, projectRoleManager,
                                dateTimeFormatterFactory, rendererManager,
                                getCommentFieldRendererType(issue), issue.getIssueRenderContext(), expand, authContext.getUser(), emailFormatter);
            }
            else
            {
                commentJsonBeans = CommentJsonBean.shortBeans(commentsForUser, jiraBaseUrls, projectRoleManager, authContext.getUser(), emailFormatter);
            }
            CommentsWithPaginationJsonBean commentsBean = new CommentsWithPaginationJsonBean();
            commentsBean.setComments(commentJsonBeans);
            commentsBean.setStartAt(0);
            commentsBean.setMaxResults(commentJsonBeans.size());
            commentsBean.setTotal(commentJsonBeans.size());

            return Response.ok(commentsBean).cacheControl(never()).build();
        }
        else
        {
            throw new NotFoundWebException(ErrorCollection.of(errorCollection));
        }
    }

    /**
     * Returns a single issue comment.
     *
     * @param commentId the ID of the comment to request
     * @param issueIdOrKey of the issue the comment belongs to
     * @param expand optional flags: renderedBody (provides body rendered in HTML), properties (provides comment
     * properties).
     * @return a Response containing a CommentJsonBean
     */
    public Response getComment(final String issueIdOrKey, final String commentId, String expand)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        if (issue == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.error.issue.invalid", issueIdOrKey)));
        }

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        try
        {
            final Comment comment = commentService.getCommentById(authContext.getUser(), Long.parseLong(commentId), errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                return Response.status(Response.Status.NOT_FOUND).entity(ErrorCollection.of(errorCollection)).cacheControl(never()).build();
            }

            if (!issue.equals(comment.getIssue()))
            {
                return Response.status(Response.Status.NOT_FOUND).entity(ErrorCollection.of(i18n.getText("rest.comment.error.invalidIssueForComment", issue.getKey()))).cacheControl(never()).build();
            }

            if (expand != null)
            {
                return Response.ok(CommentJsonBean.expandedShortBean(comment, jiraBaseUrls, projectRoleManager, dateTimeFormatterFactory, rendererManager, getCommentFieldRendererType(issue),
                        issue.getIssueRenderContext(), expand, authContext.getUser(), emailFormatter)).cacheControl(never()).build();
            }
            else
            {
                return Response.ok(CommentJsonBean.shortBean(comment, jiraBaseUrls, projectRoleManager, authContext.getUser(), emailFormatter)).cacheControl(never()).build();
            }
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("comment.service.error.no.comment.for.id", commentId)));
        }
    }

    /**
     * Updates an existing comment using its JSON representation.
     *
     * @param issueIdOrKey a string containing the issue id or key the comment belongs to
     * @param commentId id of the comment to be updated
     * @param expand optional flags: renderedBody (provides body rendered in HTML), properties (provides comment
     * properties).
     * @param request json body of request converted to a {@link com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean}
     * @return updated Comment
     */
    public Response updateComment(final String issueIdOrKey, final String commentId, final String expand, final CommentJsonBean request)
    {

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        if (request.getId() != null && !request.getId().equals(commentId))
        {
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(i18n.getText("rest.comment.error.id.mismatch"))).cacheControl(never()).build();
        }

        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        if (issue == null)
        {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorCollection.of(i18n.getText("rest.comment.error.issue.invalid", issueIdOrKey))).cacheControl(never()).build();
        }

        try
        {
            final Comment comment = commentService.getCommentById(authContext.getUser(), Long.parseLong(commentId), errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                return Response.status(Response.Status.NOT_FOUND).entity(ErrorCollection.of(errorCollection)).cacheControl(never()).build();
            }

            if (comment == null)
            {
                return Response.status(Response.Status.NOT_FOUND).entity(ErrorCollection.of(i18n.getText("comment.service.error.no.comment.for.id", commentId))).cacheControl(never()).build();
            }

            if (!issue.equals(comment.getIssue()))
            {
                return Response.status(Response.Status.NOT_FOUND).entity(ErrorCollection.of(i18n.getText("rest.comment.error.invalidIssueForComment", issue.getKey()))).cacheControl(never()).build();
            }

            Visibility visibility;
            if (request.isVisibilitySet())
            {
                visibility = Visibilities.fromVisibilityBean(request.getVisibility(), projectRoleManager);
            }
            else
            {
                visibility = Visibilities.fromGroupAndRoleId(comment.getGroupLevel(), comment.getRoleLevelId());
            }

            final ApplicationUser user = authContext.getUser();

            final CommentService.CommentParameters commentParameters = CommentService.CommentParameters.builder()
                    .author(user)
                    .body(request.getBody())
                    .commentProperties(getCommentProperties(request.getProperties()))
                    .visibility(visibility)
                    .issue(issue)
                    .build();

            final CommentService.CommentUpdateValidationResult commentValidationResult = commentService.validateCommentUpdate(user, comment.getId(), commentParameters);

            return commentValidationResult.getCommentProperties().fold(new Supplier<Response>()
            {
                @Override
                public Response get()
                {
                    return Response.status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(commentValidationResult.getErrorCollection())).cacheControl(never()).build();
                }
            }, new Function<Map<String, JSONObject>, Response>()
            {
                @Override
                public Response apply(final Map<String, JSONObject> properties)
                {
                    Comment updatedComment = commentValidationResult.getComment().get();
                    commentService.update(user, commentValidationResult, true);
                    CommentJsonBean bean;
                    if (expand != null)
                    {
                        bean = CommentJsonBean.expandedShortBean(updatedComment, jiraBaseUrls, projectRoleManager, dateTimeFormatterFactory,
                                rendererManager, getCommentFieldRendererType(issue),
                                issue.getIssueRenderContext(), expand, authContext.getUser(), emailFormatter);
                    }
                    else
                    {
                        bean = CommentJsonBean.shortBean(updatedComment, jiraBaseUrls, projectRoleManager, authContext.getUser(), emailFormatter);
                    }
                    return Response.ok(bean).location(getUri(bean)).cacheControl(never()).build();
                }
            });
        }
        catch (NumberFormatException e)
        {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorCollection.of(i18n.getText("comment.service.error.no.comment.for.id", commentId))).cacheControl(never()).build();
        }
    }

    /**
     * Deletes an existing comment .
     *
     * @param issueIdOrKey a string containing the issue id or key the comment belongs to
     * @param commentId id of the comment to be deleted
     * @return No Content Response
     */
    public Response deleteComment(final String issueIdOrKey, final String commentId)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        if (issue == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.error.issue.invalid", issueIdOrKey)));
        }

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        try
        {
            final Comment comment = commentService.getCommentById(authContext.getUser(), Long.parseLong(commentId), errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                throw new NotFoundWebException(ErrorCollection.of(errorCollection));
            }

            if (!issue.equals(comment.getIssue()))
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.error.invalidIssueForComment", issue.getKey())));
            }

            final JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(authContext.getUser(), errorCollection);
            commentService.delete(jiraServiceContext, comment, true);
            if (errorCollection.hasAnyErrors())
            {
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(errorCollection)).cacheControl(never()).build();
            }

            return Response.noContent().cacheControl(never()).build();
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("comment.service.error.no.comment.for.id", commentId)));
        }
    }

    /**
     * Adds a new comment to an issue.
     *
     * @param issueIdOrKey a string containing the issue id or key the comment will be added to
     * @param expand optional flags: renderedBody (provides body rendered in HTML), properties (provides comment
     * properties).
     * @param request json body of request converted to a {@link com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean}
     * @return the added comment
     */
    public Response addComment(final String issueIdOrKey, final String expand, final CommentJsonBean request)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);

        if (issue == null)
        {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorCollection.of(i18n.getText("rest.comment.error.issue.invalid", issueIdOrKey))).cacheControl(never()).build();
        }

        final ApplicationUser user = authContext.getUser();

        Visibility commentVisibility = Visibilities.fromVisibilityBean(request.getVisibility(), projectRoleManager);

        final CommentService.CommentParameters commentParameters = CommentService.CommentParameters.builder()
                .author(user)
                .body(request.getBody())
                .commentProperties(getCommentProperties(request.getProperties()))
                .visibility(commentVisibility)
                .issue(issue)
                .build();

        final CommentService.CommentCreateValidationResult commentValidationResult = commentService.validateCommentCreate(user, commentParameters);

        return commentValidationResult.getCommentInputParameters().fold(new Supplier<Response>()
        {
            @Override
            public Response get()
            {
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(commentValidationResult.getErrorCollection())).cacheControl(never()).build();
            }
        }, new Function<CommentService.CommentParameters, Response>()
        {
            @Override
            public Response apply(final CommentService.CommentParameters commentParameters)
            {
                Comment comment = commentService.create(user, commentValidationResult, true);
                CommentJsonBean entity;
                if (expand != null)
                {
                    entity = CommentJsonBean.expandedShortBean(comment, jiraBaseUrls, projectRoleManager, dateTimeFormatterFactory,
                            rendererManager, getCommentFieldRendererType(issue), issue.getIssueRenderContext(), expand, authContext.getUser(), emailFormatter);
                }
                else
                {
                    entity = CommentJsonBean.shortBean(comment, jiraBaseUrls, projectRoleManager, authContext.getUser(), emailFormatter);
                }
                return Response.status(Response.Status.CREATED).location(getUri(entity)).entity(entity).cacheControl(never()).build();
            }
        });
    }

    private URI getUri(CommentJsonBean comment)
    {
        try
        {
            return new URI(comment.getSelf());
        }
        catch (URISyntaxException e)
        {
            return null;
        }
    }

    private String getCommentFieldRendererType(Issue issue)
    {
        final FieldLayout layout = fieldLayoutManager.getFieldLayout(issue);

        final List<FieldLayoutItem> fieldLayoutItems = layout.getVisibleLayoutItems(issue.getProjectObject(), CollectionBuilder.list(issue.getIssueTypeObject().getId()));
        for (FieldLayoutItem item : fieldLayoutItems)
        {
            if (COMMENT_FIELD_KEY.equals(item.getOrderableField().getId()))
            {
                return item.getRendererType();
            }
        }
        return null;
    }

    private Map<String, JSONObject> getCommentProperties(final List<EntityPropertyBean> properties)
    {
        final Map<String, JSONObject> mapOfProperties = Maps.newHashMap();
        if (properties != null)
        {
            for (EntityPropertyBean propertyBean : properties)
            {
                try
                {
                    mapOfProperties.put(propertyBean.getKey(), new JSONObject(propertyBean.getValue()));
                }
                catch (JSONException e)
                {
                    log.error("Error when building comment properties", e);
                }
            }
        }
        return mapOfProperties;
    }
}
