package com.atlassian.jira.rest.v1.attachment;

import com.atlassian.core.util.FileSize;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.util.AttachmentHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.user.SecureUserTokenManager;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.WebAttachmentManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.CorsAllowed;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.util.Collection;

import webwork.config.Configuration;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

@Path ("AttachTemporaryFile")
@Produces (MediaType.APPLICATION_JSON)
@AnonymousAllowed
@CorsAllowed
public class AttachTemporaryFileResource
{
    private final JiraAuthenticationContext authContext;
    private final WebAttachmentManager webAttachmentManager;
    private final IssueService issueService;
    private final ProjectService projectService;
    private final XsrfTokenGenerator xsrfGenerator;
    private final AttachmentHelper attachmentHelper;
    private final SecureUserTokenManager secureUserTokenManager;

    public AttachTemporaryFileResource(JiraAuthenticationContext authContext,
            WebAttachmentManager webAttachmentManager, IssueService issueService, ProjectService projectService,
            XsrfTokenGenerator xsrfGenerator, AttachmentHelper attachmentHelper, final SecureUserTokenManager secureUserTokenManager)
    {
        this.authContext = authContext;
        this.webAttachmentManager = webAttachmentManager;
        this.issueService = issueService;
        this.projectService = projectService;
        this.xsrfGenerator = xsrfGenerator;
        this.attachmentHelper = attachmentHelper;
        this.secureUserTokenManager = secureUserTokenManager;
    }

    @POST
    @Consumes (MediaType.WILDCARD)
    @Path ("/secure")
    public Response addTemporaryAttachment(@QueryParam ("filename") String filename,
            @QueryParam ("projectId") Long projectId, @QueryParam ("issueId") Long issueId,
            @QueryParam ("size") Long size,  @QueryParam ("secureToken") String secureToken,
            @QueryParam ("formToken") String formToken, @Context HttpServletRequest request)
    {
        if (secureToken == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(never()).build();
        }

        User secureUser = secureUserTokenManager.useToken(secureToken, SecureUserTokenManager.TokenType.SCREENSHOT);

        if (secureUser == null)
        {
            return Response.status(Response.Status.UNAUTHORIZED).cacheControl(never()).build();
        }

        authContext.setLoggedInUser(secureUser);

        return addTemporaryAttachment(filename, projectId, issueId, size, formToken, request);
    }

    @POST
    @Consumes (MediaType.WILDCARD)
    public Response addTemporaryAttachment(@QueryParam ("filename") String filename,
            @QueryParam ("projectId") Long projectId, @QueryParam ("issueId") Long issueId,
            @QueryParam ("size") Long size, @QueryParam ("formToken") String formToken, @Context HttpServletRequest request)
    {
        final AttachmentHelper.ValidationResult validationResult = attachmentHelper.validate(request, filename, size);

        if (!validationResult.isValid())
        {
            switch (validationResult.getErrorType())
            {
                case ATTACHMENT_TO_LARGE:
                {
                    final String message = authContext.getI18nHelper().getText("upload.too.big", filename,
                            FileSize.format(size),
                            FileSize.format(new Long(Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE))));
                    return createError(Response.Status.BAD_REQUEST, message);
                }
                case ATTACHMENT_IO_SIZE:
                {
                    final String message = authContext.getI18nHelper().getText("attachfile.error.io.size", filename);
                    return createError(Response.Status.BAD_REQUEST, message);
                }
                case ATTACHMENT_IO_UNKNOWN:
                {
                    final String message = authContext.getI18nHelper().getText("attachfile.error.io.error", filename, validationResult.getErrorMessage());
                    return createError(Response.Status.INTERNAL_SERVER_ERROR, message);
                }
                case FILENAME_BLANK:
                    return Response.status(Response.Status.BAD_REQUEST).cacheControl(never()).build();
                case XSRF_TOKEN_INVALID:
                    return createTokenError(xsrfGenerator.generateToken(request));
            }
        }

        if (issueId == null && projectId == null)
        {
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(never()).build();
        }

        Project project = null;
        Issue issue = null;

        final User user = authContext.getLoggedInUser();
        if (issueId != null)
        {
            issue = getIssue(user, issueId);
        }
        else
        {
            project = getProject(user, projectId);
        }

        InputStream inputStream = validationResult.getInputStream();

        try
        {
            final TemporaryAttachment attach = webAttachmentManager.createTemporaryAttachment(validationResult.getInputStream(), filename,
                    validationResult.getContentType(), validationResult.getSize(), issue, project, formToken);
            return Response.status(Response.Status.CREATED)
                    .entity(new GoodResult(attach.getId(), filename)).cacheControl(never()).build();
        }
        catch (AttachmentException e)
        {
            return createError(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private Issue getIssue(User user, Long id)
    {
        IssueService.IssueResult result = issueService.getIssue(user, id);
        if (result.isValid())
        {
            return result.getIssue();
        }
        else
        {
            return throwFourOhFour(result.getErrorCollection());
        }
    }

    private Project getProject(User user, Long id)
    {
        ProjectService.GetProjectResult projectResult = projectService.getProjectById(user, id);
        if (projectResult.isValid())
        {
            return projectResult.getProject();
        }
        else
        {
            return throwFourOhFour(projectResult.getErrorCollection());
        }
    }

    private static Response createError(Response.Status status, com.atlassian.jira.util.ErrorCollection collection)
    {
        String message = getFirstElement(collection.getErrorMessages());
        if (message == null)
        {
            message = getFirstElement(collection.getErrors().values());
        }
        return createError(status, message);
    }

    private static Response createError(Response.Status status, String message)
    {
        return Response.status(status).cacheControl(never()).entity(new BadResult(message)).build();
    }

    private Response createTokenError(String newToken)
    {
        String message = authContext.getI18nHelper().getText("attachfile.xsrf.try.again");
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .cacheControl(never()).entity(new BadResult(message, newToken)).build();
    }

    private <T> T throwFourOhFour(com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        throw new WebApplicationException(createError(Response.Status.NOT_FOUND, errorCollection));
    }

    private static <T> T getFirstElement(Collection<? extends T> values)
    {
        if (!values.isEmpty())
        {
            return values.iterator().next();
        }
        else
        {
            return null;
        }
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @XmlRootElement
    public static class GoodResult
    {
        @XmlElement
        private String name;

        @XmlElement
        private String id;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private GoodResult() {}

        GoodResult(long id, String name)
        {
            this.id = String.valueOf(id);
            this.name = name;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            GoodResult that = (GoodResult) o;

            if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
            if (name != null ? !name.equals(that.name) : that.name != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @XmlRootElement
    public static class BadResult
    {
        @XmlElement
        private String errorMessage;

        @XmlElement
        private String token;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private BadResult() {}

        BadResult(String msg)
        {
            this(msg, null);
        }

        BadResult(String msg, String token)
        {
            this.errorMessage = msg;
            this.token = token;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            BadResult badResult = (BadResult) o;

            if (errorMessage != null ? !errorMessage.equals(badResult.errorMessage) : badResult.errorMessage != null)
            { return false; }
            if (token != null ? !token.equals(badResult.token) : badResult.token != null) { return false; }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = errorMessage != null ? errorMessage.hashCode() : 0;
            result = 31 * result + (token != null ? token.hashCode() : 0);
            return result;
        }
    }
}
