package com.atlassian.jira.web.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.attachment.AttachmentConstants;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.atlassian.jira.security.Permissions.BROWSE;
import static com.atlassian.jira.util.BrowserUtils.USER_AGENT_HEADER;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the ViewAttachmentServlet class.
 */
public class TestViewAttachmentServlet
{
    @Mock private AttachmentManager mockAttachmentManager;
    @Mock private HttpServletRequest mockRequest;
    @Mock private HttpServletResponse mockResponse;
    @Mock private IssueManager mockIssueManager;
    @Mock private JiraAuthenticationContext mockAuthenticationContext;
    @Mock private MimeSniffingKit mockMimeSniffingKit;
    @Mock private PermissionManager mockPermissionManager;
    @Mock private RequestDispatcher mockRequestDispatcher;
    @Mock private UserManager mockUserManager;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        new MockComponentWorker().init()
                .addMock(JiraAuthenticationContext.class, mockAuthenticationContext)
                .addMock(MimeSniffingKit.class, mockMimeSniffingKit)
                .addMock(OfBizDelegator.class, new MockOfBizDelegator())
                .addMock(PermissionManager.class, mockPermissionManager)
                .addMock(UserManager.class, mockUserManager);
    }

    @After
    public void tearDown()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testHasPermissions() throws Exception
    {
        // Set up
        final Long issueId = 1L;

        //the issue that will be returned from the attachment
        final MutableIssue issue = new MockIssue();
        issue.setIssueTypeId(issueId.toString());

        //our trusty test user edwin
        final String username = "edwin";
        final ApplicationUser edwin = new MockApplicationUser(username);
        when(mockUserManager.getUserByName(username)).thenReturn(edwin);
        when(mockPermissionManager.hasPermission(BROWSE, issue, edwin)).thenReturn(true);
        when(mockIssueManager.getIssueObject(issueId)).thenReturn(issue);

        final Attachment attachment = new Attachment(mockIssueManager, UtilsForTests.getTestEntity(AttachmentConstants.ATTACHMENT_ENTITY_NAME,
                EasyMap.build("issue", issueId)), null);
        final PublicViewAttachmentServlet viewAttachmentServlet = new PublicViewAttachmentServlet(attachment);

        // Invoke and check
        assertTrue(viewAttachmentServlet.hasPermissionToViewAttachment(edwin.getName(), attachment));
    }

    @Test
    public void testRedirectIfUserHasNoPermissions() throws Exception
    {
        //set it up so the user has no permissions
        final ViewAttachmentServlet viewAttachmentServlet = new PublicViewAttachmentServlet(null)
        {
            @Override
            protected boolean hasPermissionToViewAttachment(final String userString, final Attachment attachment)
            {
                return false;
            }
        };

        when(mockRequest.getContextPath()).thenReturn("someContext");
        when(mockRequest.getMethod()).thenReturn("GET");
        when(mockRequest.getPathInfo()).thenReturn("/attachments/something");
        when(mockRequest.getRemoteUser()).thenReturn("");
        when(mockRequest.getRequestDispatcher("/secure/views/securitybreach.jsp")).thenReturn(mockRequestDispatcher);

        when(mockAuthenticationContext.getUser()).thenReturn(new MockApplicationUser("edwin"));

        // Invoke
        viewAttachmentServlet.service(mockRequest, mockResponse);

        // Check
        verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
    }

    @Test
    public void testResponseHeaders() throws Exception
    {
        // Set up
        final Attachment attachment = new Attachment(null, UtilsForTests.getTestEntity(AttachmentConstants.ATTACHMENT_ENTITY_NAME,
                EasyMap.build("mimetype", "sampleMimeType", "filesize", 11L, "filename", "sampleFilename")), null);
        final PublicViewAttachmentServlet viewAttachmentServlet = new PublicViewAttachmentServlet(attachment);
        final String userAgent = "secretAgent";
        when(mockRequest.getHeader(USER_AGENT_HEADER)).thenReturn(userAgent);
        when(mockRequest.getPathInfo()).thenReturn("/attachments/something");
        when(mockRequest.getRemoteUser()).thenReturn("");

        // Invoke
        viewAttachmentServlet.setResponseHeaders(mockRequest, mockResponse);

        // Check
        verify(mockMimeSniffingKit).setAttachmentResponseHeaders(attachment, userAgent, mockResponse);
        verify(mockResponse).setContentType("sampleMimeType");
        verify(mockResponse).setContentLength(11);
        verify(mockResponse).setDateHeader("Expires", -1);
    }

    @Test
    public void testCacheControlHeadersShouldEncourageLongTermCaching() throws Exception
    {
        // Set up
        final Attachment attachment = new Attachment(null, UtilsForTests.getTestEntity(AttachmentConstants.ATTACHMENT_ENTITY_NAME,
                EasyMap.build("mimetype", "sampleMimeType", "filesize", 11L, "filename", "sampleFilename")), null);
        final PublicViewAttachmentServlet viewAttachmentServlet = new PublicViewAttachmentServlet(attachment);
        when(mockRequest.getPathInfo()).thenReturn("/attachments/something");
        when(mockRequest.getRemoteUser()).thenReturn("");

        // Invoke
        viewAttachmentServlet.setResponseHeaders(mockRequest, mockResponse);

        // Check
        verify(mockResponse).setHeader("Cache-control", "private, max-age=31536000");
        verify(mockResponse).setDateHeader("Expires", -1L); // forbid HTTP1.0 shared proxies from caching attachments
    }

    @Test(expected = InvalidAttachmentPathException.class)
    public void testInvalidAttachmentPath() throws Exception
    {
        // Set up
        final PublicViewAttachmentServlet viewAttachmentServlet = new PublicViewAttachmentServlet(null);
        when(mockRequest.getPathInfo()).thenReturn("abc");
        when(mockRequest.getRemoteUser()).thenReturn("");

        // Invoke and expect exception
        viewAttachmentServlet.setResponseHeaders(mockRequest, mockResponse);
    }

    @Test
    public void testAttachmentNotFoundWithStringId() throws Exception
    {
        ViewAttachmentServlet viewAttachmentServlet = new ViewAttachmentServlet();
        try
        {
            viewAttachmentServlet.getAttachment("/abc/");
            fail("AttachmentNotFoundException should have been thrown.");
        }
        catch (AttachmentNotFoundException e)
        {
            assertTrue(e.getMessage().contains("abc"));
        }
    }

    private static class PublicViewAttachmentServlet extends ViewAttachmentServlet
    {
        private final Attachment attachment;

        private PublicViewAttachmentServlet(final Attachment attachment)
        {
            this.attachment = attachment;
        }

        @Override
        public Attachment getAttachment(final String query)
        {
            return attachment;
        }
    }
}
