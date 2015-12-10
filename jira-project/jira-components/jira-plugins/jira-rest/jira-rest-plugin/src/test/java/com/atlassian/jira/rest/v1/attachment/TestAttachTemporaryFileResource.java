package com.atlassian.jira.rest.v1.attachment;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.attachment.TemporaryAttachment;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.rest.util.AttachmentHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfTokenGenerator;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.SecureUserTokenManager;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.WebAttachmentManager;

import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseBody;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertResponseCacheNever;
import static com.atlassian.jira.rest.assertions.ResponseAssertions.assertStatus;
import static javax.ws.rs.core.Response.Status;
import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.fail;

/**
 * @since v4.4
 */
public class TestAttachTemporaryFileResource
{
    private IMocksControl control;
    private MockUser user;
    private JiraAuthenticationContext authCtx;
    private WebAttachmentManager webAttachmentManager;
    private IssueService issueService;
    private ProjectService projectService;
    private HttpServletRequest request;
    private ServletInputStream stream;
    private XsrfTokenGenerator xsrfGenerator;
    private AttachmentHelper attachmentHelper;
    private SecureUserTokenManager secureUserTokenManager;

    @Before
    public void createDeps()
    {
        control = createControl();
        user = new MockUser("Brenden");
        authCtx = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
        webAttachmentManager = control.createMock(WebAttachmentManager.class);
        issueService = control.createMock(IssueService.class);
        projectService = control.createMock(ProjectService.class);
        request = control.createMock(HttpServletRequest.class);
        stream = control.createMock(ServletInputStream.class);
        xsrfGenerator = control.createMock(XsrfTokenGenerator.class);
        attachmentHelper = control.createMock(AttachmentHelper.class);
        secureUserTokenManager = control.createMock(SecureUserTokenManager.class);
    }

    @Test
    public void testBadXsrf()
    {
        AttachmentHelper.ValidationResult validationResult = control.createMock(AttachmentHelper.ValidationResult.class);

        expect(attachmentHelper.validate(request, null, null)).andReturn(validationResult);

        expect(validationResult.isValid()).andReturn(false);
        expect(validationResult.getErrorType()).andReturn(AttachmentHelper.ValidationError.XSRF_TOKEN_INVALID).anyTimes();

        String token = "Toke3423424243n";
        expect(xsrfGenerator.generateToken(request)).andReturn(token);

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfGenerator, attachmentHelper, secureUserTokenManager);

        control.replay();

        Response actualResponse = resource.addTemporaryAttachment(null, null, 178L, null, null, request);
        assertResponseCacheNever(actualResponse);

        final AttachTemporaryFileResource.BadResult expectedResponse =
                new AttachTemporaryFileResource.BadResult(NoopI18nHelper.makeTranslation("attachfile.xsrf.try.again"), token);

        assertResponseBody(expectedResponse, actualResponse);
        assertStatus(Status.INTERNAL_SERVER_ERROR, actualResponse);

        control.verify();
    }

    @Test
    public void testBadArguments()
    {
        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfGenerator, attachmentHelper, secureUserTokenManager);

        AttachmentHelper.ValidationResult validationResult = control.createMock(AttachmentHelper.ValidationResult.class);

        expect(validationResult.isValid()).andReturn(false).times(3);
        expect(validationResult.getErrorType()).andReturn(AttachmentHelper.ValidationError.FILENAME_BLANK).anyTimes();
        expect(validationResult.getErrorMessage()).andReturn(null).anyTimes();

        expect(attachmentHelper.validate(null, null, null)).andReturn(validationResult);
        expect(attachmentHelper.validate(null, "filename", null)).andReturn(validationResult);
        expect(attachmentHelper.validate(null, "    ", null)).andReturn(validationResult);

        control.replay();

        assertBadRequest(resource.addTemporaryAttachment(null, null, 178L, null, null, null));
        assertBadRequest(resource.addTemporaryAttachment("    ", null, 178L, null, null, null));
        assertBadRequest(resource.addTemporaryAttachment("filename", null, null, null, null, null));

        control.verify();
    }

    @Test
    public void testGoodCreatingIssue() throws IOException, AttachmentException
    {
        MockProject project = new MockProject(178L);
        String contentType = "application/octet-stream";
        long size = 1L;
        TemporaryAttachment ta = new TemporaryAttachment(273738L, new File("something"), "name", contentType, null);

        AttachmentHelper.ValidationResult validationResult = control.createMock(AttachmentHelper.ValidationResult.class);

        expect(validationResult.isValid()).andReturn(true);
        expect(attachmentHelper.validate(request, ta.getFilename(), 1L)).andReturn(validationResult);

        expect(projectService.getProjectById(user, project.getId()))
                .andReturn(new ProjectService.GetProjectResult(new SimpleErrorCollection(), project));

        expect(validationResult.getInputStream()).andReturn(stream).anyTimes();
        expect(validationResult.getContentType()).andReturn(contentType).anyTimes();
        expect(validationResult.getSize()).andReturn(size).anyTimes();

        stream.close();

        expect(webAttachmentManager.createTemporaryAttachment(stream, ta.getFilename(), ta.getContentType(),
                size, null, project, null))
                .andReturn(ta);

        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfGenerator, attachmentHelper, secureUserTokenManager);

        Response response = resource.addTemporaryAttachment(ta.getFilename(), project.getId(), null, size, null, request);
        assertResponseBody(new AttachTemporaryFileResource.GoodResult(ta.getId(), ta.getFilename()), response);
        assertResponseCacheNever(response);
        assertStatus(Status.CREATED, response);

        control.verify();
    }

    @Test
    public void testBadCreatingIssueNoProject() throws IOException, AttachmentException
    {
        long pid = 178L;

        String errorMsg = "badddd";

        AttachmentHelper.ValidationResult validationResult = control.createMock(AttachmentHelper.ValidationResult.class);

        expect(validationResult.isValid()).andReturn(true);
        expect(attachmentHelper.validate(request, "ignored", 1L)).andReturn(validationResult);

        expect(projectService.getProjectById(user, pid))
                .andReturn(new ProjectService.GetProjectResult(errors(errorMsg)));


        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfGenerator, attachmentHelper, secureUserTokenManager);

        try
        {
            resource.addTemporaryAttachment("ignored", pid, null, 1L, null, request);
            fail("Expected error");
        }
        catch (WebApplicationException e)
        {
            assertResponseBody(new AttachTemporaryFileResource.BadResult(errorMsg), e.getResponse());
            assertResponseCacheNever(e.getResponse());
            assertStatus(Status.NOT_FOUND, e.getResponse());
        }

        control.verify();
    }

    @Test
    public void testGoodpdatingIssueGood() throws IOException, AttachmentException
    {
        MockIssue issue = new MockIssue(178L);
        MockProject project = new MockProject(74748L);
        int contentLength = 272728;
        String contentType = "text/plain";
        TemporaryAttachment ta = new TemporaryAttachment(273738L, new File("something"), "name", contentType, null);

        expect(issueService.getIssue(user, issue.getId()))
                .andReturn(new IssueService.IssueResult(issue, ok()));


        stream.close();

        expect(webAttachmentManager.createTemporaryAttachment(stream, ta.getFilename(), ta.getContentType(),
                contentLength, issue, null, null))
                .andReturn(ta);

        AttachmentHelper.ValidationResult validationResult = control.createMock(AttachmentHelper.ValidationResult.class);

        expect(validationResult.isValid()).andReturn(true);
        expect(attachmentHelper.validate(request, ta.getFilename(), null)).andReturn(validationResult);
        expect(validationResult.getInputStream()).andReturn(stream).anyTimes();
        expect(validationResult.getSize()).andReturn(272728L).anyTimes();
        expect(validationResult.getContentType()).andReturn(contentType).anyTimes();

        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfGenerator, attachmentHelper, secureUserTokenManager);

        Response response = resource.addTemporaryAttachment(ta.getFilename(), project.getId(), issue.getId(), null, null, request);
        assertResponseBody(new AttachTemporaryFileResource.GoodResult(ta.getId(), ta.getFilename()), response);
        assertResponseCacheNever(response);
        assertStatus(Status.CREATED, response);

        control.verify();
    }

    @Test
    public void testBadUpdatingIssueNoIssue() throws IOException, AttachmentException
    {
        long pid = 178L;
        long id = 575;

        String errorMsg = "badddd";
        expect(issueService.getIssue(user, id))
                .andReturn(new IssueService.IssueResult(null, errors(errorMsg)));

        AttachmentHelper.ValidationResult validationResult = control.createMock(AttachmentHelper.ValidationResult.class);

        expect(validationResult.isValid()).andReturn(true);
        expect(attachmentHelper.validate(request, "ignored", 1L)).andReturn(validationResult);

        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfGenerator, attachmentHelper, secureUserTokenManager);

        try
        {
            resource.addTemporaryAttachment("ignored", pid, id, 1L, null, request);
            fail("Expected error");
        }
        catch (WebApplicationException e)
        {
            assertResponseBody(new AttachTemporaryFileResource.BadResult(errorMsg), e.getResponse());
            assertResponseCacheNever(e.getResponse());
            assertStatus(Status.NOT_FOUND, e.getResponse());
        }

        control.verify();
    }

    @Test
    public void testErrorWhileGettingStream() throws IOException, AttachmentException
    {
        AttachmentHelper.ValidationResult validationResult = control.createMock(AttachmentHelper.ValidationResult.class);

        MockIssue issue = new MockIssue(178L);
        MockProject project = new MockProject(74748L);
        String errorMessage = "message";
        String fileName = "name";

        expect(validationResult.isValid()).andReturn(false);
        expect(attachmentHelper.validate(request, fileName, 1L)).andReturn(validationResult);
        expect(validationResult.getErrorType()).andReturn(AttachmentHelper.ValidationError.ATTACHMENT_IO_UNKNOWN)
                .anyTimes();
        expect(validationResult.getErrorMessage()).andReturn(errorMessage);

        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfGenerator, attachmentHelper, secureUserTokenManager);

        Response response = resource.addTemporaryAttachment(fileName, project.getId(), issue.getId(), 1L, null, request);
        String expectedMessage = NoopI18nHelper.makeTranslation("attachfile.error.io.error", fileName, errorMessage);
        assertResponseBody(new AttachTemporaryFileResource.BadResult(expectedMessage), response);
        assertResponseCacheNever(response);
        assertStatus(Status.INTERNAL_SERVER_ERROR, response);

        control.verify();
    }

    @Test
    public void testAttachmentError() throws IOException, AttachmentException
    {

        MockIssue issue = new MockIssue(178L);
        MockProject project = new MockProject(74748L);
        long contentLength = 272728;
        String contentType = "text/plain";
        String message = "errorMessae";
        TemporaryAttachment ta = new TemporaryAttachment(273738L, new File("something"), "name", contentType, null);


        AttachmentHelper.ValidationResult validationResult = control.createMock(AttachmentHelper.ValidationResult.class);

        expect(attachmentHelper.validate(request, ta.getFilename(), 272728L)).andReturn(validationResult);
        expect(validationResult.isValid()).andReturn(true);

        expect(issueService.getIssue(user, issue.getId()))
                .andReturn(new IssueService.IssueResult(issue, ok()));

        expect(validationResult.getInputStream()).andReturn(stream).anyTimes();
        expect(validationResult.getContentType()).andReturn(contentType).anyTimes();
        expect(validationResult.getSize()).andReturn(272728L).anyTimes();

        stream.close();

        expect(webAttachmentManager.createTemporaryAttachment(stream, ta.getFilename(), ta.getContentType(),
                contentLength, issue, null, null))
                .andThrow(new AttachmentException(message));

        control.replay();

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfGenerator, attachmentHelper, secureUserTokenManager);

        Response response = resource.addTemporaryAttachment(ta.getFilename(), project.getId(), issue.getId(), contentLength, null, request);
        assertBadRequest(Status.INTERNAL_SERVER_ERROR, message, response);

        control.verify();
    }

    @Test
    public void testValidSecurityToken() throws IOException, AttachmentException
    {
        String secureToken = "token";
        expect(secureUserTokenManager.useToken(secureToken, SecureUserTokenManager.TokenType.SCREENSHOT)).andReturn(user);

        MockIssue issue = new MockIssue(178L);
        MockProject project = new MockProject(74748L);
        int contentLength = 272728;
        String contentType = "text/plain";
        TemporaryAttachment ta = new TemporaryAttachment(273738L, new File("something"), "name", contentType, null);

        expect(issueService.getIssue(user, issue.getId()))
                .andReturn(new IssueService.IssueResult(issue, ok()));


        stream.close();

        expect(webAttachmentManager.createTemporaryAttachment(stream, ta.getFilename(), ta.getContentType(),
                contentLength, issue, null, null))
                .andReturn(ta);

        AttachmentHelper.ValidationResult validationResult = control.createMock(AttachmentHelper.ValidationResult.class);

        expect(validationResult.isValid()).andReturn(true);
        expect(attachmentHelper.validate(request, ta.getFilename(), null)).andReturn(validationResult);
        expect(validationResult.getInputStream()).andReturn(stream).anyTimes();
        expect(validationResult.getSize()).andReturn(272728L).anyTimes();
        expect(validationResult.getContentType()).andReturn(contentType).anyTimes();

        control.replay();

        JiraAuthenticationContext authCtx = new MockSimpleAuthenticationContext(null, Locale.ENGLISH, new NoopI18nHelper());

        AttachTemporaryFileResource resource = new AttachTemporaryFileResource(authCtx, webAttachmentManager,
                issueService, projectService, xsrfGenerator, attachmentHelper, secureUserTokenManager);

        Response response = resource.addTemporaryAttachment(ta.getFilename(), project.getId(), issue.getId(), null, secureToken, null, request);
        assertResponseBody(new AttachTemporaryFileResource.GoodResult(ta.getId(), ta.getFilename()), response);
        assertResponseCacheNever(response);
        assertStatus(Status.CREATED, response);

        control.verify();
    }

    private void assertBadRequest(Status expectedStatus, String expectedError, Response response)
    {
        assertResponseCacheNever(response);
        assertResponseBody(new AttachTemporaryFileResource.BadResult(expectedError), response);
        assertStatus(expectedStatus, response);
    }

    private void assertBadRequest(Response response)
    {
        assertResponseCacheNever(response);
        assertResponseBody(null, response);
        assertStatus(Status.BAD_REQUEST, response);
    }

    public SimpleErrorCollection errors(String... errors)
    {
        SimpleErrorCollection collection = new SimpleErrorCollection();
        for (String error : errors)
        {
            collection.addErrorMessage(error);
        }
        return collection;
    }

    public SimpleErrorCollection ok()
    {
        return new SimpleErrorCollection();
    }
}
