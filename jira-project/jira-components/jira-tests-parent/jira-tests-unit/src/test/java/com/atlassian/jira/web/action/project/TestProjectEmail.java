package com.atlassian.jira.web.action.project;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectKeys;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;

import com.mockobjects.servlet.MockHttpServletResponse;
import com.opensymphony.module.propertyset.PropertySet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class TestProjectEmail
{
    private static final long PROJECT_ID = 1;
    private static final String TEST_FROM_ADDRESS = "test1@test.com";

    private ProjectEmail projectEmail;
    @Mock private I18nHelper mockI18nHelper;
    @Mock private JiraAuthenticationContext mockJiraAuthenticationContext;
    @Mock private MailServerManager mockMailServerManager;
    @Mock private ProjectManager mockProjectManager;
    private RedirectSanitiser mockRedirectSanitiser = new MockRedirectSanitiser();
    @Mock private SMTPMailServer mockSmtpMailServer;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        when(mockJiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        when(mockMailServerManager.getDefaultSMTPMailServer()).thenReturn(mockSmtpMailServer);
        new MockComponentWorker().init()
                .addMock(I18nHelper.class, mockI18nHelper)
                .addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
                .addMock(OfBizDelegator.class, new MockOfBizDelegator())
                .addMock(RedirectSanitiser.class, mockRedirectSanitiser);
        projectEmail = new ProjectEmail(mockProjectManager, mockMailServerManager);
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testGettersSetters()
    {
        projectEmail.setProjectId(PROJECT_ID);
        assertEquals(PROJECT_ID, projectEmail.getProjectId());

        projectEmail.setFromAddress(TEST_FROM_ADDRESS);
        assertEquals(TEST_FROM_ADDRESS, projectEmail.getFromAddress());
    }

    @Test
    public void testDoDefaultWithInvalidProjectId() throws Exception
    {
        // Invalid project id
        projectEmail.setProjectId(0L);

        try
        {
            projectEmail.doDefault();
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException iae)
        {
            assertNotNull(iae.getMessage());
        }
    }

    @Test
    public void testDoDefaultWithPropertySet() throws Exception
    {
        // Set up
        final Project project = new MockProject(1, "KEY", "ProjectOne").setEmail(TEST_FROM_ADDRESS);
        when(mockProjectManager.getProjectObj(PROJECT_ID)).thenReturn(project);
        projectEmail.setProjectId(1L);

        // Invoke
        projectEmail.doDefault();

        // Check
        assertEquals(TEST_FROM_ADDRESS, projectEmail.getFromAddress());
    }

    @Test
    public void testDoDefaultWithoutPropertySet() throws Exception
    {
        // Set up
        final GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "name", "ProjectOne"));
        final Project projectObj = new MockProject(1, "FTH", "ProjectOne", project);
        when(mockProjectManager.getProjectObj(PROJECT_ID)).thenReturn(projectObj);
        final String serverFromAddress = "server@test.com";
        when(mockSmtpMailServer.getDefaultFrom()).thenReturn(serverFromAddress);
        projectEmail.setProjectId(1L);

        // Invoke
        projectEmail.doDefault();

        // Check
        assertEquals(serverFromAddress, projectEmail.getFromAddress());
    }

    @Test
    public void testDoExecute() throws Exception
    {
        // Set up
        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("/plugins/servlet/project-config/FTH");
        final GenericValue project;

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "name", "ProjectOne"));
        final MockProject projectObj = new MockProject(1, "FTH", "ProjectOne", project);

        when(mockProjectManager.getProjectObj(PROJECT_ID)).thenReturn(projectObj);

        projectEmail.setProjectId(1L);
        projectEmail.setFromAddress(TEST_FROM_ADDRESS);

        // Invoke
        assertEquals(Action.NONE, projectEmail.execute());

        // Get the property set
        final PropertySet ps = OFBizPropertyUtils.getPropertySet(project);
        assertEquals(TEST_FROM_ADDRESS, ps.getString(ProjectKeys.EMAIL_SENDER));
        response.verify();
    }

    @Test
    public void testExecuteWithInvalidEmail() throws Exception
    {
        // Set up
        projectEmail.setProjectId(1L);
        final String invalidTestFromAddress = "test.com";
        projectEmail.setFromAddress(invalidTestFromAddress);
        final String errorMessage = "Some error";
        when(mockI18nHelper.getText("admin.errors.projectemail.enter.valid.address")).thenReturn(errorMessage);

        // Invoke
        final String action = projectEmail.execute();

        // Check
        assertEquals(Action.INPUT, action);
        assertEquals(errorMessage, projectEmail.getErrors().get("fromAddress"));
    }
}
