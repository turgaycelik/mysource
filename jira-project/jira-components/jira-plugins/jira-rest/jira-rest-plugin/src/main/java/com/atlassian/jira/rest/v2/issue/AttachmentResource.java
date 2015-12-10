package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.log4j.Logger;
import webwork.config.Configuration;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since v4.2
 */
@Path ("attachment")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class AttachmentResource
{
    private static final Logger log = Logger.getLogger(AttachmentResource.class);

    private PermissionManager permissionManager;
    private AttachmentManager attachmentManager;
    private AttachmentService attachmentService;
    private JiraAuthenticationContext authContext;
    private BeanBuilderFactory beanBuilderFactory;
    private I18nHelper i18n;
    private ContextUriInfo uriInfo;

    private AttachmentResource()
    {
        // this constructor used by tooling
    }

    public AttachmentResource(final AttachmentService attachmentService, final AttachmentManager attachmentManager, final PermissionManager permissionManager, final JiraAuthenticationContext authContext, BeanBuilderFactory beanBuilderFactory, I18nHelper i18n, ContextUriInfo uriInfo)
    {
        this.attachmentManager = attachmentManager;
        this.attachmentService = attachmentService;
        this.permissionManager = permissionManager;
        this.authContext = authContext;
        this.beanBuilderFactory = beanBuilderFactory;
        this.i18n = i18n;
        this.uriInfo = uriInfo;
    }

    /**
     * Returns the meta-data for an attachment, including the URI of the actual attached file.
     *
     * @param id the attachment id
     * @return a JSON representation of an attachment
     *
     * @response.representation.200.qname
     *      attachment
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.AttachmentBean#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of the attachment meta-data. The representation does not contain the
     *      attachment itself, but contains a URI that can be used to download the actual attached file.
     *
     * @response.representation.404.doc
     *      Returned if the attachment with the given id does not exist, or is not accessible by the calling user.
     */
    @GET
    @Path ("{id}")
    public Response getAttachment(@PathParam ("id") final String id)
    {
        if (!attachmentManager.attachmentsEnabled())
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(never()).build();
        }

        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(authContext.getLoggedInUser());
        try
        {
            final Attachment attachment = attachmentService.getAttachment(serviceContext, Long.parseLong(id));
            if (serviceContext.getErrorCollection().hasAnyErrors())
            {
                final ErrorCollection errors = ErrorCollection.of(serviceContext.getErrorCollection());

                return Response.status(Response.Status.NOT_FOUND).entity(errors).cacheControl(never()).build();
            }
            if (!hasPermissionToViewAttachment(authContext.getLoggedInUser(), attachment))
            {
                throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(i18n.getText("attachment.service.error.view.no.permission", id)));
            }
            final AttachmentBean bean = beanBuilderFactory.newAttachmentBeanBuilder(attachment).build();
            return Response.ok(bean).cacheControl(never()).build();
        }
        catch (AttachmentNotFoundException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.attachment.error.not.found", id)));
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.attachment.error.not.found", id)));
        }
    }

    /**
     * Remove an attachment from an issue.
     *
     * @param id the id of the attachment to ddelete.
     * @return no content
     *
     * @response.representation.204.doc
     *      Returned if successful.
     *
     * @response.representation.404.doc
     *      Returned if the attachment is not found
     *
     * @response.representation.403.doc
     *      Returned if attachments is disabled or if you don't have permission to remove attachments from this issue.
     */
    @DELETE
    @Path ("{id}")
    public Response removeAttachment(@PathParam ("id") String id)
    {
        if (!attachmentManager.attachmentsEnabled())
        {
            return Response.status(Response.Status.NOT_FOUND).cacheControl(never()).build();
        }
        JiraServiceContext serviceContext = new JiraServiceContextImpl(authContext.getLoggedInUser());
        Long attachmentId;
        try
        {
            attachmentId = Long.parseLong(id);
            attachmentService.getAttachment(serviceContext, attachmentId);
        }
        catch (AttachmentNotFoundException e)
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("rest.attachment.error.not.found", id)));
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.attachment.error.not.found", id)));
        }

        if (!attachmentService.canDeleteAttachment(serviceContext, attachmentId))
        {
            throw new RESTException(Response.Status.FORBIDDEN, ErrorCollection.of(i18n.getText("attachment.service.error.delete.no.permission", id)));
        }
        try
        {
            attachmentService.delete(serviceContext, attachmentId);
            return Response.noContent().build();
        }
        catch (Exception e)
        {
            log.error("Error deleting attachment", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    protected boolean hasPermissionToViewAttachment(User user, Attachment attachment) throws DataAccessException
    {
        Issue issue = attachment.getIssueObject();
        return permissionManager.hasPermission(Permissions.BROWSE, issue, user);
    }

    /**
     * Returns the meta informations for an attachments, specifically if they are enabled and the maximum upload size allowed.
     *
     * @return a JSON representation of the enable atachment capabilities
     *
     * @response.representation.200.qname
     *      attachmentMeta
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.AttachmentMetaBean#DOC_EXAMPLE}
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of the attachment capabilities.
     *      Consumers of this resource may also need to check if the logged in user has permission to upload or
     *      otherwise manipulate attachments using the {@link com.atlassian.jira.rest.v2.permission.PermissionsResource}.
     *
     */
    @GET
    @Path ("meta")
    public Response getAttachmentMeta()
    {
        boolean enabled = attachmentManager.attachmentsEnabled();
        long maxSize = Long.parseLong(Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE));
        AttachmentMetaBean bean = new AttachmentMetaBean(enabled, maxSize);
        return Response.ok(bean).cacheControl(never()).build();
    }
}
