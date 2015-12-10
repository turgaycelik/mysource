package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.rest.IssueFinder;
import com.atlassian.jira.issue.fields.rest.IssueLinkTypeFinder;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkBeanBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.LinkIssueRequestJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.VisibilityJsonBean;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * The Link Issue Resource provides functionality to manage issue links.
 *
 * @since v4.3
 */
@Path ("issueLink")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class LinkIssueResource
{
    private final ApplicationProperties applicationProperties;
    private final I18nHelper i18n;
    private final JiraBaseUrls jiraBaseUrls;
    private final JiraAuthenticationContext authContext;
    private final PermissionManager permissionManager;
    private final IssueLinkManager issueLinkManager;
    private final CommentService commentService;
    private final ProjectRoleManager projectRoleManager;
    private final IssueLinkTypeFinder issueLinkTypeFinder;
    private final IssueFinder issueFinder;
    private final IssueLinkService issueLinkService;
    private final IssueService issueService;
    private static final Logger log = Logger.getLogger(LinkIssueResource.class);

    public LinkIssueResource(ApplicationProperties applicationProperties, I18nHelper i18n, JiraAuthenticationContext authContext, PermissionManager permissionManager, IssueLinkManager issueLinkManager, CommentService commentService, ProjectRoleManager projectRoleManager, IssueLinkTypeFinder issueLinkTypeFinder, IssueFinder issueFinder, IssueLinkService issueLinkService, JiraBaseUrls jiraBaseUrls, IssueService issueService)
    {
        this.applicationProperties = applicationProperties;
        this.i18n = i18n;
        this.jiraBaseUrls = jiraBaseUrls;
        this.authContext = authContext;
        this.permissionManager = permissionManager;
        this.issueLinkManager = issueLinkManager;
        this.commentService = commentService;
        this.projectRoleManager = projectRoleManager;
        this.issueLinkTypeFinder = issueLinkTypeFinder;
        this.issueFinder = issueFinder;
        this.issueLinkService = issueLinkService;
        this.issueService = issueService;
    }

    /**
     * Creates an issue link between two issues.
     * The user requires the link issue permission for the issue which will be linked to another issue.
     * The specified link type in the request is used to create the link and will create a link from the first issue
     * to the second issue using the outward description. It also create a link from the second issue to the first issue using the
     * inward description of the issue link type.
     * It will add the supplied comment to the first issue. The comment can have a restriction who can view it.
     * If group is specified, only users of this group can view this comment, if roleLevel is specified only users who have the specified role can view this comment.
     * The user who creates the issue link needs to belong to the specified group or have the specified role.
     *
     * @param linkIssueRequestBean contains all information about the link relationship. Which issues to link, which issue link type to use and
     *        and an optional comment that will be added to the first issue.
     *
     * @return a response indicating if the issue link was created successfully.
     *
     *
     * @request.representation.example
     *      {@link ResourceExamples#LINK_ISSUE_REQUEST_EXAMPLE}
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      if the issue link was created successfully.
     *
     * @response.representation.400.doc
     *      if it can't create the supplied comment.
     *      The response will contain an error message indicating why it failed to create the comment.
     *      No issue link will be created if it failed to create the comment.
     *
     * @response.representation.404.doc
     *      If issue linking is disabled or
     *      it failed to find one of the issues (issue might exist, but it is not visible for this user) or
     *      it failed to find the specified issue link type.
     *
     * @response.representation.401.doc
     *      if the user does not have the link issue permission for the issue, which will be linked to another issue.
     *
     * @response.representation.500.doc
     *      if an error occurred when creating the issue link or the comment.
     */
    @POST
    public Response linkIssues(final LinkIssueRequestJsonBean linkIssueRequestBean)
    {
        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled"))));
        }

        SimpleErrorCollection errors = new SimpleErrorCollection();
        IssueLinkType linkType = issueLinkTypeFinder.findIssueLinkType(linkIssueRequestBean.getType(), errors);
        Issue inwardIssue = issueFinder.findIssue(linkIssueRequestBean.inwardIssue(), errors);
        Issue outwardIssue = issueFinder.findIssue(linkIssueRequestBean.outwardIssue(), errors);

        // if any of these was not found, return a 404
        if (linkType == null || inwardIssue == null || outwardIssue == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(errors));
        }

        if (inwardIssue.getKey().equalsIgnoreCase(outwardIssue.getKey()))
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("issuelinking.service.error.self.reference")));
        }

        if (!permissionManager.hasPermission(Permissions.LINK_ISSUE, inwardIssue, authContext.getLoggedInUser()))
        {
            throw new NotAuthorisedWebException(ErrorCollection.of(i18n.getText("rest.issue.link.error.link.no.link.permission", inwardIssue.getKey())));
        }

        // this shouldn't happen unless something was not found above, but...
        if (errors.hasAnyErrors())
        {
            throw new BadRequestWebException(ErrorCollection.of(errors));
        }

        final CommentJsonBean commentBean = linkIssueRequestBean.getComment();
        if (commentBean != null)
        {
            VisibilityJsonBean visibility = commentBean.getVisibility();
            if (visibility != null)
            {
                validateVisibilityRole(visibility);
                validateVisibilityGroup(inwardIssue, commentBean, visibility);
            }
            validateCommentBody(commentBean);
        }
        Long issueLinkId;
        try
        {
            issueLinkManager.createIssueLink(inwardIssue.getId(), outwardIssue.getId(), linkType.getId(), null, authContext.getLoggedInUser());
            issueLinkId = issueLinkManager.getIssueLink(inwardIssue.getId(), outwardIssue.getId(), linkType.getId()).getId();
        }
        catch (CreateException e)
        {
             //Exception will be logged by the ExceptionInterceptor.
             throw new RESTException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }

        if (commentBean != null && commentBean.getBody() != null)
        {
            createComment(inwardIssue, outwardIssue, commentBean);
        }
        URI issueLinkSelfURI = URI.create(jiraBaseUrls.restApi2BaseUrl() + "issueLink/" + issueLinkId.toString());
        return Response.status(Response.Status.CREATED).header("Location", issueLinkSelfURI.toString()).cacheControl(never()).build();
    }


    /**
     * Returns an issue link with the specified id.
     *
     * @param linkId the issue link id.
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.example
     *       {@link ResourceExamples#ISSUE_LINK_EXAMPLE}
     *
     * @response.representation.400.doc
     *       If the specified issue link id is invalid.
     *
     * @response.representation.404.doc
     *      If issue linking is disabled or
     *      it failed to find an issue link with the specified id. Either because none exists with this id, or the user
     *      doesn't have the permission to see one of the linked issues.
     *
     * @response.representation.401.doc
     *      if the user does not have the link issue permission for the issue, which will be linked to another issue.
     *
     * @response.representation.500.doc
     *      if an error occurred when creating the issue link or the comment.
     */
    @GET
    @Path ("{linkId}")
    public Response getIssueLink(@PathParam ("linkId") final String linkId)
    {
        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled"))));
        }

        if (StringUtils.isBlank(linkId))
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("rest.issue.link.no.id")));
        }

        try
        {
            Long.valueOf(linkId);
        }
        catch (NumberFormatException ex)
        {
            throw new BadRequestWebException(ex, ErrorCollection.of(i18n.getText("rest.issue.link.invalid.id", linkId)));
        }

        IssueLinkService.SingleIssueLinkResult issueLinkResult = issueLinkService.getIssueLink(Long.valueOf(linkId), authContext.getLoggedInUser());
        if (!issueLinkResult.isValid())
        {
           throw new NotFoundWebException(ErrorCollection.of(issueLinkResult.getErrorCollection()));
        }
        IssueLink issueLink = issueLinkResult.getIssueLink();
        IssueService.IssueResult sourceIssueResult = issueService.getIssue(authContext.getLoggedInUser(), issueLink.getSourceId());
        IssueService.IssueResult destinationIssueResult = issueService.getIssue(authContext.getLoggedInUser(), issueLink.getDestinationId());
        if (!sourceIssueResult.isValid() || !destinationIssueResult.isValid())
        {
            sourceIssueResult.getErrorCollection().getErrors().putAll(destinationIssueResult.getErrorCollection().getErrors());
            sourceIssueResult.getErrorCollection().getErrorMessages().addAll(destinationIssueResult.getErrorCollection().getErrorMessages());
            throw new NotFoundWebException(ErrorCollection.of(sourceIssueResult.getErrorCollection()));
        }
        IssueLinkBeanBuilder issueLinkBeanBuilder = new IssueLinkBeanBuilder(jiraBaseUrls);
        IssueLinkJsonBean issueLinkJsonBean = issueLinkBeanBuilder.buildIssueLinkBean(issueLink.getIssueLinkType(), issueLink.getId().toString());
        issueLinkJsonBean.outwardIssue(issueLinkBeanBuilder.createIssueRefJsonBean(destinationIssueResult.getIssue()));
        issueLinkJsonBean.inwardIssue(issueLinkBeanBuilder.createIssueRefJsonBean(sourceIssueResult.getIssue()));
        return Response.ok(issueLinkJsonBean).cacheControl(never()).build();
    }


     /**
     * Deletes an issue link with the specified id.
     * To be able to delete an issue link you must be able to view both issues and must have the link issue permission
     * for at least one of the issues.
     *
     * @param linkId the issue link id.
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.204.doc
     *       If it successfully deleted the issue link.
     *
     * @response.representation.400.doc
     *       If the specified issue link id is invalid.
     *
     * @response.representation.404.doc
     *      If issue linking is disabled or
     *      it failed to find an issue link with the specified id. Either because none exists with this id, or the user
     *      doesn't have the permission to see one of the linked issues.
     *
     * @response.representation.401.doc
     *      if the user does not have the link issue permission for the source or destination issue of the issue link.
     *
     * @response.representation.500.doc
     *      if an error occurred when deleting the issue link or the comment.
     */
    @DELETE
    @Path ("{linkId}")
    public Response deleteIssueLink(@PathParam ("linkId") final String linkId)
    {
        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled"))));
        }

        if (StringUtils.isBlank(linkId))
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n.getText("rest.issue.link.no.id")));
        }

        try
        {
            Long.valueOf(linkId);
        }
        catch (NumberFormatException ex)
        {
            throw new BadRequestWebException(ex, ErrorCollection.of(i18n.getText("rest.issue.link.invalid.id", linkId)));
        }
        log.warn("My classloader: " + this.getClass().getClassLoader());
        log.warn("IssueLinkService classloader: " + issueLinkService.getClass().getClassLoader());
        log.warn("IssueLinkService" + issueLinkService);
        IssueLinkService.SingleIssueLinkResult issueLinkResult = issueLinkService.getIssueLink(Long.valueOf(linkId), authContext.getLoggedInUser());
        if (!issueLinkResult.isValid())
        {
           throw new NotFoundWebException(ErrorCollection.of(issueLinkResult.getErrorCollection()));
        }

        IssueLink issueLink = issueLinkResult.getIssueLink();

        IssueService.IssueResult sourceIssueResult = issueService.getIssue(authContext.getLoggedInUser(), issueLink.getSourceId());
        IssueService.IssueResult destinationIssueResult = issueService.getIssue(authContext.getLoggedInUser(), issueLink.getDestinationId());
        if (!sourceIssueResult.isValid() || !destinationIssueResult.isValid())
        {
            sourceIssueResult.getErrorCollection().getErrors().putAll(destinationIssueResult.getErrorCollection().getErrors());
            sourceIssueResult.getErrorCollection().getErrorMessages().addAll(destinationIssueResult.getErrorCollection().getErrorMessages());
            throw new NotFoundWebException(ErrorCollection.of(sourceIssueResult.getErrorCollection()));
        }

        IssueLinkService.DeleteIssueLinkValidationResult deleteSourceIssueLinkValidationResult = issueLinkService.validateDelete(authContext.getLoggedInUser(), sourceIssueResult.getIssue(), issueLink);
        if (deleteSourceIssueLinkValidationResult.isValid())
        {
            issueLinkService.delete(deleteSourceIssueLinkValidationResult);
            return Response.status(Response.Status.NO_CONTENT).cacheControl(never()).build();
        }

        IssueLinkService.DeleteIssueLinkValidationResult deleteDesinationIssueLinkValidationResult = issueLinkService.validateDelete(authContext.getLoggedInUser(), destinationIssueResult.getIssue(), issueLink);
        if (deleteDesinationIssueLinkValidationResult.isValid())
        {
            issueLinkService.delete(deleteDesinationIssueLinkValidationResult);
            return Response.status(Response.Status.NO_CONTENT).cacheControl(never()).build();
        }
        deleteSourceIssueLinkValidationResult.getErrorCollection().getErrors().putAll(deleteDesinationIssueLinkValidationResult.getErrorCollection().getErrors());
        deleteSourceIssueLinkValidationResult.getErrorCollection().getErrorMessages().addAll(deleteDesinationIssueLinkValidationResult.getErrorCollection().getErrorMessages());
        throw new NotAuthorisedWebException(ErrorCollection.of(deleteSourceIssueLinkValidationResult.getErrorCollection()));
    }


    private void createComment(Issue fromIssue, Issue toIssue, CommentJsonBean commentBean)
    {
        Comment comment;
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        if (commentBean.getVisibility() != null)
        {
            final VisibilityJsonBean visibility = commentBean.getVisibility();
            Long role;
            String group;
            if (visibility.type == VisibilityJsonBean.VisibilityType.role)
            {
                role = projectRoleManager.getProjectRole(visibility.value).getId();
                group = null;
            }
            else
            {
                group = visibility.value;
                role = null;
            }
            comment = commentService.create(authContext.getLoggedInUser(), fromIssue, commentBean.getBody(), group, role, false, errorCollection);
        }
        else
        {
            comment = commentService.create(authContext.getLoggedInUser(), fromIssue, commentBean.getBody(), false, errorCollection);
        }
        if (errorCollection.hasAnyErrors() || comment == null)
        {
            errorCollection.addErrorMessage(i18n.getText("rest.issue.link.error.comment", toIssue.getKey()));
            throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(errorCollection));
        }
    }

    private void validateCommentBody(CommentJsonBean commentBean)
    {
        if (commentBean.getBody() != null)
        {
            SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            commentService.isValidCommentBody(commentBean.getBody(), errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(errorCollection));
            }
        }
    }

    private void validateVisibilityGroup(Issue fromIssue, CommentJsonBean commentBean, VisibilityJsonBean visibility)
    {
        if (visibility.type == VisibilityJsonBean.VisibilityType.group)
        {
            if (visibility.value == null)
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.visibility.no.value")));
            }

            SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            commentService.isValidAllCommentData(authContext.getLoggedInUser(), fromIssue, commentBean.getBody(), visibility.value, null /* must be null if we specify a group */, errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(errorCollection));
            }
        }
    }

    private void validateVisibilityRole(VisibilityJsonBean visibility)
    {
        if (visibility.type == VisibilityJsonBean.VisibilityType.role)
        {
            if (visibility.value == null)
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.visibility.no.value")));
            }

            final Long roleId = projectRoleManager.getProjectRole(visibility.value).getId();

            if (roleId == null)
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.issue.link.error.project.role.not.found", visibility.value)));
            }
        }
    }
}
