package com.atlassian.jira.web.action.setup;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.license.LicenseJohnsonEventRaiser;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.web.HttpServletVariables;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.atlassian.johnson.config.JohnsonConfig;
import com.atlassian.plugin.event.PluginEventManager;

import com.google.common.collect.ImmutableList;
import com.mockobjects.servlet.MockServletContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;

import webwork.action.Action;
import webwork.action.ServletActionContext;

import static com.atlassian.jira.config.properties.APKeys.JIRA_OPTION_ALLOWATTACHMENTS;
import static com.atlassian.jira.config.properties.APKeys.JIRA_OPTION_ALLOWUNASSIGNED;
import static com.atlassian.jira.config.properties.APKeys.JIRA_OPTION_USER_EXTERNALMGT;
import static com.atlassian.jira.config.properties.APKeys.JIRA_OPTION_VOTING;
import static com.atlassian.jira.config.properties.APKeys.JIRA_OPTION_WATCHING;
import static com.atlassian.jira.config.properties.APKeys.JIRA_SETUP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class TestSetupComplete
{
    private SetupComplete setupCompleteAction;
    private MockHttpServletResponse mockHttpServletResponse;
    @Mock private FieldLayoutManager fieldLayoutManager;
    @Mock private JiraLicenseService jiraLicenseService;
    @Mock private LicenseJohnsonEventRaiser licenseJohnsonEventRaiser;
    @Mock private PluginEventManager pluginEventManager;
    private RedirectSanitiser mockRedirectSanitiser = new MockRedirectSanitiser();
    @Mock private SubTaskManager subTaskManager;
    @Mock private UpgradeManager mockUpgradeManager;
    @Mock private HttpServletVariables servletVariables;
    @Mock private HttpSession httpSession;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        new MockComponentWorker().init().addMock(RedirectSanitiser.class, mockRedirectSanitiser);

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);

        mockHttpServletResponse = new MockHttpServletResponse();

        ServletActionContext.setResponse(mockHttpServletResponse);

        when(servletVariables.getHttpSession()).thenReturn(httpSession);

        // Mock out the license test
        setupCompleteAction = new SetupComplete(mockUpgradeManager, licenseJohnsonEventRaiser, jiraLicenseService,
                subTaskManager, fieldLayoutManager, null, pluginEventManager, servletVariables)
        {
            @Override
            protected boolean licenseTooOld()
            {
                return false;
            }

            @Override
            void setWikiRendererOnAllRenderableFields()
            {
            }
        };
    }

    @After
    public void tearDown()
    {
        ComponentAccessor.initialiseWorker(null);
        ServletActionContext.setResponse(null);
        ServletActionContext.setServletContext(null);
    }

    @Test
    public void testDoDefaultRedirectsToTheDashboardWhenTheUserCanNotBeLoggedInAutomatically() throws Exception
    {
        // Set up
        expectNoUpgradeTaskErrors();

        // Invoke
        setupCompleteAction.doDefault();

        // Check
        assertEquals(mockHttpServletResponse.getRedirectedUrl(), "Dashboard.jspa?src=" + SetupComplete.class.getSimpleName());
    }

    private void expectNoUpgradeTaskErrors() throws Exception
    {
        final List<String> noErrors = Collections.emptyList();
        when(mockUpgradeManager.doUpgradeIfNeededAndAllowed(null, true))
                .thenReturn(new UpgradeManager.Status(false, noErrors));
    }

    @Test
    public void testExecuteWhenAlreadySetup() throws Exception
    {
        setupCompleteAction.getApplicationProperties().setString(JIRA_SETUP, "true");
        assertEquals("setupalready", setupCompleteAction.execute());
    }

    @Test
    public void testExecuteSetsApplicationPropertiesToTheirDefaultValues() throws Exception
    {
        // Set up
        setupCompleteAction.getApplicationProperties().setString(JIRA_SETUP, null);
        expectNoUpgradeTaskErrors();

        // Invoke
        setupCompleteAction.execute();

        // Check
        // set the default values for jira application properties in newly setup instances.
        assertEquals("true", setupCompleteAction.getApplicationProperties().getString(JIRA_SETUP));
        assertTrue(!setupCompleteAction.getApplicationProperties().getOption(JIRA_OPTION_ALLOWUNASSIGNED));
        assertTrue(!setupCompleteAction.getApplicationProperties().getOption(JIRA_OPTION_ALLOWATTACHMENTS));
        assertTrue(!setupCompleteAction.getApplicationProperties().getOption(JIRA_OPTION_USER_EXTERNALMGT));
        assertTrue(setupCompleteAction.getApplicationProperties().getOption(JIRA_OPTION_VOTING));
        assertTrue(setupCompleteAction.getApplicationProperties().getOption(JIRA_OPTION_WATCHING));
    }

    @Test
    public void testExecuteRunsUpgradeTasksRequiredForANewSetup() throws Exception
    {
        // Set up
        expectNoUpgradeTaskErrors();

        // Invoke
        setupCompleteAction.execute();
    }

    @Test
    public void testExecuteUpgradeManagerErrorsAreAdded() throws Exception
    {
        // Set up two errors
        when(mockUpgradeManager.doUpgradeIfNeededAndAllowed(null, true))
                .thenReturn(new UpgradeManager.Status(false, ImmutableList.of("Error1", "Error2")));

        // Invoke
        final String result = setupCompleteAction.execute();

        // Check
        assertEquals(Action.ERROR, result);
        assertEquals(2, setupCompleteAction.getErrorMessages().size());
        assertTrue(setupCompleteAction.getErrorMessages().contains("Error1"));
        assertTrue(setupCompleteAction.getErrorMessages().contains("Error2"));
    }

    @Test
    public void testAJohnsonEventIsNotRaisedIfTheLicenseIsNotTooOldGivenTheUserCanNotBeLoggedInAutomatically()
            throws Exception
    {
        // Set up
        expectNoUpgradeTaskErrors();
        final SetupComplete licenseValidSetupComplete = new SetupComplete(mockUpgradeManager, licenseJohnsonEventRaiser,
                jiraLicenseService, subTaskManager, fieldLayoutManager, null, pluginEventManager, servletVariables)
        {
            @Override
            protected boolean licenseTooOld()
            {
                //license is not too old for build
                return false;
            }

            @Override
            void setWikiRendererOnAllRenderableFields()
            {
            }
        };

        // Invoke
        licenseValidSetupComplete.execute();

        // Check
        assertEquals("Dashboard.jspa?src=" + SetupComplete.class.getSimpleName(), mockHttpServletResponse.getRedirectedUrl());
    }

    @Test
    public void testAJohnsonEventIsRaisedIfTheLicenseIsNotTooOld() throws Exception
    {
        // Set up
        final SetupComplete licenseValidSetupComplete = new SetupComplete(mockUpgradeManager, licenseJohnsonEventRaiser,
                jiraLicenseService, subTaskManager, fieldLayoutManager, null, pluginEventManager, servletVariables)
        {
            @Override
            protected boolean licenseTooOld()
            {
                //license is too old for build
                return true;
            }

            @Override
            void setWikiRendererOnAllRenderableFields()
            {
            }
        };

        // Invoke
        licenseValidSetupComplete.execute();

        // Check
        assertEquals(mockHttpServletResponse.getRedirectedUrl(), JohnsonConfig.getInstance().getErrorPath());
    }
}
