/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.setup;

import javax.servlet.http.HttpServletResponse;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.dataimport.DataImportParams;
import com.atlassian.jira.bc.dataimport.DataImportService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mail.settings.MailSettings;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.RedirectSanitiser;
import com.atlassian.jira.web.util.ExternalLinkUtil;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.mockito.Mockito;

import webwork.action.Action;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestSetupImport
{
    public static final String FILENAME = "filename";
    public static final String PATH_NAME = "/etc/monkey/";

    private SetupImport setupImportAction;

    @Mock
    private ExternalLinkUtil externalLinkUtil;

    @Mock
    private BuildUtilsInfo buildUtilsInfo;

    @Mock
    private IndexPathManager indexPathManager;

    @Mock
    private DataImportService dataImportService;

    private MailSettings.DefaultMailSettings mailSettings;

    @Mock
    private FileFactory fileFactory;

    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;

    @Mock
    private JiraProperties jiraSystemProperties;

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext;

    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser = new MockRedirectSanitiser();

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);


    @Before
    public void setUp() throws Exception
    {

        //unforutnately mail settings class is an API and due to many reasons it cannot be mocked properly :(
        mailSettings = new MailSettings.DefaultMailSettings(applicationProperties, authenticationContext, jiraSystemProperties);
        setupImportAction = new SetupImport(indexPathManager, externalLinkUtil, buildUtilsInfo, fileFactory, dataImportService, null, null, mailSettings);
    }

    @Test
    public void testGetSets() throws Exception
    {
        assertNull(setupImportAction.getFilename());

        setupImportAction.setFilename(FILENAME);
        assertEquals(FILENAME, setupImportAction.getFilename());
    }

    @Test
    public void shouldReturnSetupAlreadyWhenJiraIsSetUp() throws Exception
    {
        when(applicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        assertEquals("setupalready", setupImportAction.doDefault());
    }

    @Test
    public void shouldShouldCheckOutgoingMailSettingsWhenJiraIsNotSetup() throws Exception
    {
        when(applicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn(null);
        assertEquals(Action.INPUT, setupImportAction.doDefault());
        verify(jiraSystemProperties).getBoolean(MailSettings.Send.DISABLED_SYSTEM_PROPERTY_KEY);
    }


    @Test
    public void testDoValidation() throws Exception
    {
        setupImportAction.setFilename("");
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addError(FILENAME, "Could not find file at this location.");
        when(dataImportService.validateImport(any(User.class), any(DataImportParams.class))).thenReturn(new DataImportService.ImportValidationResult(errors, null));

        assertEquals(Action.INPUT, setupImportAction.execute());
        assertEquals("Could not find file at this location.", setupImportAction.getErrors().get(FILENAME));

    }

    @Test
    public void testExecuteSetupAlready() throws Exception
    {
        when(applicationProperties.getString(APKeys.JIRA_SETUP)).thenReturn("true");
        assertEquals("setupalready", setupImportAction.execute());
    }

    @Test
    public void testExecuteWeHaveASucessfulResult() throws Exception
    {
        final DataImportService.ImportResult.Builder importResult = new DataImportService.ImportResult.Builder(null);
        ActionContext.getSession().put(SessionKeys.DATA_IMPORT_RESULT, importResult.build());
        final HttpServletResponse response = mock(HttpServletResponse.class);
        ServletActionContext.setResponse(response);
        setupImportAction.doExecute();
        verify(response).sendRedirect(Mockito.argThat(Matchers.startsWith("Dashboard.jspa")));

    }


}
