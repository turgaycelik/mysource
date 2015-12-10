package com.atlassian.jira.plugin.issueview;

import java.io.IOException;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.plugin.PluginAccessor;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.easymock.MockControl;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class TestIssueViewURLHandler
{
    private final Integer ERROR_400 = Integer.valueOf(400);
    private final Integer ERROR_404 = Integer.valueOf(404);
    private final String ERROR_EXAMPLE = "Invalid path format. Path should be of format /si/jira.issueviews:xml/JRA-10/JRA-10.xml";
    private final String ERROR_USER = "Could not find a user with the username invalid";
    private final String ERROR_PLUGIN = "Could not find any enabled plugin with key jira.issueviews:issue-novalidformat";
    private final String ERROR_FIELD = "No valid field defined for issue custom view";
    private final String ERROR_NO_ISSUE = "Could not find issue with issue key JRA-1";


    @Test
    public void testHandleRequestMalformedURLErrorNoSlashes() throws IOException
    {
        IssueViewURLHandler issueViewURLHandler = new DefaultIssueViewURLHandler(null, null, null, null, null, null);

        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "I'm a dodgy URL (no slashes)");

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expectVoid("sendError", new Constraint[] { new IsEqual(ERROR_400), new IsEqual(ERROR_EXAMPLE) });

        issueViewURLHandler.handleRequest((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());

        mockRequest.verify();
        mockResponse.verify();
    }

    @Test
    public void testHandleRequestMalformedURLErrorOnlyOneSlash() throws IOException
    {
        IssueViewURLHandler issueViewURLHandler = new DefaultIssueViewURLHandler(null, null, null, null, null, null);

        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "/I'm a less dodgy URL (one slash)");

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expectVoid("sendError", new Constraint[] { new IsEqual(ERROR_400), new IsEqual(ERROR_EXAMPLE) });

        issueViewURLHandler.handleRequest((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());

        mockRequest.verify();
        mockResponse.verify();
    }

    @Test
    public void testHandleRequestMalformedURLErrorTwoSlashes() throws IOException
    {
        IssueViewURLHandler issueViewURLHandler = new DefaultIssueViewURLHandler(null, null, null, null, null, null);

        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "//I'm a less dodgy URL (two slash, no issue key)");

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expectVoid("sendError", new Constraint[] { new IsEqual(ERROR_400), new IsEqual(ERROR_EXAMPLE) });

        issueViewURLHandler.handleRequest((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());

        mockRequest.verify();
        mockResponse.verify();
    }

    @Test
    public void testInvalidUser() throws IOException
    {
        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "/jira.issueviews:issue-xml/HSP-1/HSP-1.xml");
        mockRequest.expectAndReturn("getRemoteUser", "invalid");

        Mock mockCrowdService = new Mock(CrowdService.class);
        mockCrowdService.expectAndReturn("getUser", "invalid", null);

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expectVoid("sendError", new Constraint[] { new IsEqual(ERROR_400), new IsEqual(ERROR_USER) });

        IssueViewURLHandler issueViewURLHandler = new DefaultIssueViewURLHandler(null, null, null, null, null, (CrowdService) mockCrowdService.proxy());
        issueViewURLHandler.handleRequest((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());

        mockRequest.verify();
        mockResponse.verify();
        mockCrowdService.verify();
    }

    @Test
    public void testNoUser() throws IOException
    {
        MockControl mockModuleDescriptorControl = MockClassControl.createControl(IssueViewModuleDescriptor.class);
        IssueViewModuleDescriptor mockModuleDescriptor = (IssueViewModuleDescriptor) mockModuleDescriptorControl.getMock();
        mockModuleDescriptor.getCompleteKey();
        mockModuleDescriptorControl.setReturnValue("jira.issueviews:issue-xml");
        mockModuleDescriptor.getFileExtension();
        mockModuleDescriptorControl.setReturnValue("xml");

        Mock mockIssueView = new Mock(IssueView.class);
        mockModuleDescriptor.getIssueView();
        mockModuleDescriptorControl.setReturnValue((IssueView) mockIssueView.proxy());

        mockModuleDescriptorControl.replay();

        Mock mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor.expectAndReturn("getEnabledPluginModule", "jira.issueviews:issue-xml", mockModuleDescriptor);

        final MockIssue mockIssue = new MockIssue();
        mockIssue.setKey("HSP-1");
        IssueManager mockIssueManager = new MockIssueManager()
        {
            public MutableIssue getIssueObject(String key) throws DataAccessException
            {
                return mockIssue;
            }
        };

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, false);

        Mock mockRequestDispatcher = new Mock(RequestDispatcher.class);
        mockRequestDispatcher.expectVoid("include", P.ANY_ARGS);

        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "jira.issueviews:issue-xml/HSP-1/HSP-1.xml");
        mockRequest.expectAndReturn("getRemoteUser", null);
        mockRequest.expectAndReturn("getParameter", "jira.issue.searchlocation", "");

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expectVoid("sendRedirect", P.ANY_ARGS);

        IssueViewURLHandler issueViewURLHandler = new DefaultIssueViewURLHandler((PluginAccessor) mockPluginAccessor.proxy(), mockIssueManager, (PermissionManager) mockPermissionManager.proxy(), null, null, null);
        issueViewURLHandler.handleRequest((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());

        mockRequest.verify();
        mockResponse.verify();
    }

    @Test
    public void testNoPlugin() throws IOException
    {
        User user = new MockUser("admin");

        Mock mockCrowdService = new Mock(CrowdService.class);
        mockCrowdService.expectAndReturn("getUser", "admin", user);

        Mock mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor.expectAndReturn("getEnabledPluginModule", "jira.issueviews:issue-novalidformat", null);

        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "jira.issueviews:issue-novalidformat/HSP-1/HSP-1.novalidformat");
        mockRequest.expectAndReturn("getRemoteUser", "admin");

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expectVoid("sendError", new Constraint[] { new IsEqual(ERROR_400), new IsEqual(ERROR_PLUGIN) });

        IssueViewURLHandler issueViewURLHandler = new DefaultIssueViewURLHandler((PluginAccessor) mockPluginAccessor.proxy(), null, null, null, null, (CrowdService) mockCrowdService.proxy());
        issueViewURLHandler.handleRequest((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());

        mockRequest.verify();
        mockResponse.verify();
        mockCrowdService.verify();
        mockPluginAccessor.verify();
    }

    @Test
    public void testNoPermissionOnIssue() throws IOException
    {
        User user = new MockUser("admin");

        Mock mockCrowdService = new Mock(CrowdService.class);
        mockCrowdService.expectAndReturn("getUser", "admin", user);

        MockControl mockModuleDescriptorControl = MockClassControl.createControl(IssueViewModuleDescriptor.class);
        IssueViewModuleDescriptor mockModuleDescriptor = (IssueViewModuleDescriptor) mockModuleDescriptorControl.getMock();
        mockModuleDescriptor.getCompleteKey();
        mockModuleDescriptorControl.setReturnValue("jira.issueviews:issue-xml");
        mockModuleDescriptor.getFileExtension();
        mockModuleDescriptorControl.setReturnValue("xml");

        Mock mockIssueView = new Mock(IssueView.class);
        mockModuleDescriptor.getIssueView();
        mockModuleDescriptorControl.setReturnValue((IssueView) mockIssueView.proxy());

        mockModuleDescriptorControl.replay();

        Mock mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor.expectAndReturn("getEnabledPluginModule", "jira.issueviews:issue-xml", mockModuleDescriptor);

        final MockIssue mockIssue = new MockIssue();
        mockIssue.setKey("HSP-1");
        IssueManager mockIssueManager = new MockIssueManager()
        {
            public MutableIssue getIssueObject(String key) throws DataAccessException
            {
                return mockIssue;
            }
        };

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, false);

        Mock mockRequestDispatcher = new Mock(RequestDispatcher.class);
        mockRequestDispatcher.expectVoid("forward", P.ANY_ARGS);

        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "jira.issueviews:issue-xml/HSP-1/HSP-1.xml");
        mockRequest.expectAndReturn("getRemoteUser", "admin");
        mockRequest.expectAndReturn("getParameter", "jira.issue.searchlocation", "");
        mockRequest.expectAndReturn("getRequestDispatcher", "/secure/views/permissionviolation.jsp", (RequestDispatcher) mockRequestDispatcher.proxy());

        Mock mockResponse = new Mock(HttpServletResponse.class);

        IssueViewURLHandler issueViewURLHandler = new DefaultIssueViewURLHandler((PluginAccessor) mockPluginAccessor.proxy(), mockIssueManager, (PermissionManager) mockPermissionManager.proxy(), null, null, (CrowdService) mockCrowdService.proxy());
        issueViewURLHandler.handleRequest((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());

        mockRequest.verify();
        mockResponse.verify();
        mockCrowdService.verify();
        mockPluginAccessor.verify();
        mockRequestDispatcher.verify();
    }

    @Test
    public void testNoPermissionOnIssueError() throws IOException
    {
        User user = new MockUser("admin");

        Mock mockCrowdService = new Mock(CrowdService.class);
        mockCrowdService.expectAndReturn("getUser", "admin", user);

        MockControl mockModuleDescriptorControl = MockClassControl.createControl(IssueViewModuleDescriptor.class);
        IssueViewModuleDescriptor mockModuleDescriptor = (IssueViewModuleDescriptor) mockModuleDescriptorControl.getMock();
        mockModuleDescriptor.getCompleteKey();
        mockModuleDescriptorControl.setReturnValue("jira.issueviews:issue-xml");
        mockModuleDescriptor.getFileExtension();
        mockModuleDescriptorControl.setReturnValue("xml");

        Mock mockIssueView = new Mock(IssueView.class);
        mockModuleDescriptor.getIssueView();
        mockModuleDescriptorControl.setReturnValue((IssueView) mockIssueView.proxy());

        mockModuleDescriptorControl.replay();

        Mock mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor.expectAndReturn("getEnabledPluginModule", "jira.issueviews:issue-xml", mockModuleDescriptor);

        final MockIssue mockIssue = new MockIssue();
        mockIssue.setKey("HSP-1");
        IssueManager mockIssueManager = new MockIssueManager()
        {
            public MutableIssue getIssueObject(String key) throws DataAccessException
            {
                return mockIssue;
            }
        };

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, false);

        Mock mockRequestDispatcher = new Mock(RequestDispatcher.class);
        mockRequestDispatcher.expectAndThrow("forward", P.ANY_ARGS, new ServletException());

        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "jira.issueviews:issue-xml/HSP-1/HSP-1.xml");
        mockRequest.expectAndReturn("getRemoteUser", "admin");
        mockRequest.expectAndReturn("getParameter", "jira.issue.searchlocation", "");
        mockRequest.expectAndReturn("getRequestDispatcher", "/secure/views/permissionviolation.jsp", mockRequestDispatcher.proxy());

        Mock mockResponse = new Mock(HttpServletResponse.class);

        IssueViewURLHandler issueViewURLHandler = new DefaultIssueViewURLHandler((PluginAccessor) mockPluginAccessor.proxy(), mockIssueManager, (PermissionManager) mockPermissionManager.proxy(), null, null, (CrowdService) mockCrowdService.proxy());
        try
        {
            issueViewURLHandler.handleRequest((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());
            fail();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getCause() instanceof ServletException);
        }

        mockRequest.verify();
        mockResponse.verify();
        mockCrowdService.verify();
        mockPluginAccessor.verify();
        mockPermissionManager.verify();
        mockRequestDispatcher.verify();
    }

    @Test
    public void testNoFieldsDefined() throws Exception
    {
        User user = new MockUser("admin");

        Mock mockCrowdService = new Mock(CrowdService.class);
        mockCrowdService.expectAndReturn("getUser", "admin", user);

        MockControl mockModuleDescriptorControl = MockClassControl.createControl(IssueViewModuleDescriptor.class);
        IssueViewModuleDescriptor mockModuleDescriptor = (IssueViewModuleDescriptor) mockModuleDescriptorControl.getMock();
        mockModuleDescriptor.getCompleteKey();
        mockModuleDescriptorControl.setReturnValue("jira.issueviews:issue-xml");
        mockModuleDescriptor.getFileExtension();
        mockModuleDescriptorControl.setReturnValue("xml");

        Mock mockIssueView = new Mock(IssueView.class);
        mockModuleDescriptor.getIssueView();
        mockModuleDescriptorControl.setReturnValue((IssueView) mockIssueView.proxy());

        mockModuleDescriptorControl.replay();

        Mock mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor.expectAndReturn("getEnabledPluginModule", "jira.issueviews:issue-xml", mockModuleDescriptor);

        final MockIssue mockIssue = new MockIssue();
        mockIssue.setKey("HSP-1");
        IssueManager mockIssueManager = new MockIssueManager()
        {
            public MutableIssue getIssueObject(String key) throws DataAccessException
            {
                return mockIssue;
            }
        };

        Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission", P.ANY_ARGS, true);

        Mock mockIssueViewRequestParamsHelper = new Mock(IssueViewRequestParamsHelper.class);
        Mock mockIssueFIeldViewParams = new Mock(IssueViewFieldParams.class);
        Map paramMap = EasyMap.build();
        mockIssueViewRequestParamsHelper.expectAndReturn("getIssueViewFieldParams", paramMap, mockIssueFIeldViewParams.proxy());

        mockIssueFIeldViewParams.expectAndReturn("isCustomViewRequested", true);
        mockIssueFIeldViewParams.expectAndReturn("isAnyFieldDefined", false);

        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "jira.issueviews:issue-xml/HSP-1/HSP-1.xml");
        mockRequest.expectAndReturn("getRemoteUser", "admin");
        mockRequest.expectAndReturn("getParameter", "jira.issue.searchlocation", "");
        mockRequest.expectAndReturn("getParameterMap", paramMap);

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expectVoid("sendError", new Constraint[] { new IsEqual(ERROR_400), new IsEqual(ERROR_FIELD) });

        IssueViewURLHandler issueViewURLHandler = new DefaultIssueViewURLHandler((PluginAccessor) mockPluginAccessor.proxy(), mockIssueManager, (PermissionManager) mockPermissionManager.proxy(), null, (IssueViewRequestParamsHelper) mockIssueViewRequestParamsHelper.proxy(), (CrowdService) mockCrowdService.proxy());
        issueViewURLHandler.handleRequest((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());

        mockRequest.verify();
        mockResponse.verify();
        mockCrowdService.verify();
        mockPluginAccessor.verify();
        mockPermissionManager.verify();
    }

    @Test
    public void testHandleRequestForMovedIssue() throws Exception
    {
        IssueManager mockIssueManager = new MockIssueManager()
        {
            public MutableIssue getIssueObject(String key) throws DataAccessException
            {
                return new MockIssue(123, "MOVED-1");
            }
        };

        MockControl mockModuleDescriptorControl = MockClassControl.createControl(IssueViewModuleDescriptor.class);
        IssueViewModuleDescriptor mockModuleDescriptor = (IssueViewModuleDescriptor) mockModuleDescriptorControl.getMock();
        mockModuleDescriptor.getCompleteKey();
        mockModuleDescriptorControl.setReturnValue("jira.issueviews:issue-xml");
        mockModuleDescriptor.getFileExtension();
        mockModuleDescriptorControl.setReturnValue("xml");
        mockModuleDescriptorControl.replay();

        User user = new MockUser("admin");

        Mock mockCrowdService = new Mock(CrowdService.class);
        mockCrowdService.expectAndReturn("getUser", "admin", user);

        Mock mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor.expectAndReturn("getEnabledPluginModule", "jira.issueviews:issue-xml", mockModuleDescriptor);

        IssueViewURLHandler issueViewURLHandler = new DefaultIssueViewURLHandler((PluginAccessor) mockPluginAccessor.proxy(), mockIssueManager, null, null, null, (CrowdService) mockCrowdService.proxy());

        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "/jira.issueviews:issue-xml/JRA-1/JRA-1.xml");
        mockRequest.expectAndReturn("getContextPath", "/someJIRAInstall");
        mockRequest.expectAndReturn("getQueryString", "key=value");
        mockRequest.expectAndReturn("getRemoteUser", "admin");

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expectVoid("sendRedirect", "/someJIRAInstall/si/jira.issueviews:issue-xml/MOVED-1/MOVED-1.xml?key=value");

        issueViewURLHandler.handleRequest((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());

        mockRequest.verify();
        mockResponse.verify();
        mockCrowdService.verify();
        mockPluginAccessor.verify();
        mockModuleDescriptorControl.verify();
    }

    @Test
    public void testNoIssue() throws IOException
    {
        IssueManager mockIssueManager = new MockIssueManager()
        {
            @Override
            public Issue findMovedIssue(final String oldIssueKey)
            {
                return null;
            }
        };

        ChangeHistoryManager mockChangeHistoryManager = EasyMock.createNiceMock(ChangeHistoryManager.class);
        EasyMock.replay(mockChangeHistoryManager);

        MockControl mockModuleDescriptorControl = MockClassControl.createControl(IssueViewModuleDescriptor.class);
        IssueViewModuleDescriptor mockModuleDescriptor = (IssueViewModuleDescriptor) mockModuleDescriptorControl.getMock();
        mockModuleDescriptor.getCompleteKey();
        mockModuleDescriptorControl.setReturnValue("jira.issueviews:issue-xml");
        mockModuleDescriptor.getFileExtension();
        mockModuleDescriptorControl.setReturnValue("xml");
        mockModuleDescriptorControl.replay();

        User user = new MockUser("admin");

        Mock mockCrowdService = new Mock(CrowdService.class);
        mockCrowdService.expectAndReturn("getUser", "admin", user);

        Mock mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor.expectAndReturn("getEnabledPluginModule", "jira.issueviews:issue-xml", mockModuleDescriptor);

        IssueViewURLHandler issueViewURLHandler = new DefaultIssueViewURLHandler((PluginAccessor) mockPluginAccessor.proxy(), mockIssueManager, null, null, null, (CrowdService) mockCrowdService.proxy());

        Mock mockRequest = new Mock(HttpServletRequest.class);
        mockRequest.expectAndReturn("getPathInfo", "/jira.issueviews:issue-xml/JRA-1/JRA-1.xml");
        mockRequest.expectAndReturn("getRemoteUser", "admin");

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expectVoid("sendError", new Constraint[] { new IsEqual(ERROR_404), new IsEqual(ERROR_NO_ISSUE) });

        issueViewURLHandler.handleRequest((HttpServletRequest) mockRequest.proxy(), (HttpServletResponse) mockResponse.proxy());

        mockRequest.verify();
        mockResponse.verify();
        mockCrowdService.verify();
        mockPluginAccessor.verify();
    }
}
