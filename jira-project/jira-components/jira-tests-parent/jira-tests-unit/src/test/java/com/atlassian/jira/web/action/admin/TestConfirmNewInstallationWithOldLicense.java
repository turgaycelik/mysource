/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import java.util.Collection;
import java.util.Date;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.cluster.ClusterManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraPropertiesImpl;
import com.atlassian.jira.config.properties.SystemPropertiesAccessor;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserKeyService;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventType;

import com.mockobjects.servlet.MockServletContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import webwork.action.ServletActionContext;

import static com.atlassian.jira.config.properties.APKeys.JIRA_PATCHED_VERSION;
import static com.atlassian.jira.config.properties.SystemPropertyKeys.UPGRADE_SYSTEM_PROPERTY;
import static com.atlassian.jira.license.LicenseJohnsonEventRaiser.LICENSE_TOO_OLD;
import static com.atlassian.jira.web.action.admin.ConfirmNewInstallationWithOldLicense.RADIO_OPTION_EVALUATION;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit test of {@link ConfirmNewInstallationWithOldLicense}.
 *
 * @since 6.2
 */
@SuppressWarnings("deprecation")
public class TestConfirmNewInstallationWithOldLicense
{
    private static final String PASSWORD = "testpassword";
    private static final String A_VALID_LICENSE = "a valid license";

    @Mock private ApplicationProperties mockApplicationProperties;
    @Mock private BuildUtilsInfo buildUtilsInfo;
    @Mock private CrowdService crowdService;
    @Mock private I18nHelper mockI18nHelper;
    @Mock private JiraLicenseUpdaterService jiraLicenseService;
    @Mock private JiraProperties jiraProperties;
    @Mock private JiraSystemRestarter jiraSystemRestarter;
    @Mock private PermissionManager mockPermissionManager;
    @Mock private ClusterManager clusterManager;
    private UserKeyService mockUserKeyService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        when(mockApplicationProperties.getString(JIRA_PATCHED_VERSION)).thenReturn("610");
        final JiraAuthenticationContext mockJiraAuthenticationContext = mock(JiraAuthenticationContext.class);
        when(mockJiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        mockUserKeyService = Mockito.mock(UserKeyService.class); // do this every time as there will be test-specific behaviour
        new MockComponentWorker().init()
                .addMock(ApplicationProperties.class, mockApplicationProperties)
                .addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
                .addMock(UserKeyService.class, mockUserKeyService);
        jiraProperties = new JiraPropertiesImpl(new SystemPropertiesAccessor());
    }

    @After
    public void tearDown() throws Exception
    {
        ComponentAccessor.initialiseWorker(null);
        System.clearProperty(UPGRADE_SYSTEM_PROPERTY);
    }

    @Test
    public void testErrorIfNoUserName() throws Exception
    {
        // Set up
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("610");
        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);
        JohnsonEventContainer johnsonEventContainer = JohnsonEventContainer.get(mockServletContext);
        johnsonEventContainer.addEvent(new Event(EventType.get(LICENSE_TOO_OLD), "blah"));

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense =
                new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter,
                        crowdService, mockPermissionManager, jiraProperties, clusterManager);
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);

        // Set the confirmation so that no errors are raised for its absence (or the absence of the new license key)
        confirmNewInstallationWithOldLicense.setRadioOption(RADIO_OPTION_EVALUATION);
        
        // Invoke
        confirmNewInstallationWithOldLicense.execute();
        
        // Check
        assertEquals(1, confirmNewInstallationWithOldLicense.getErrorMessages().size());
    }

    @Test
    public void testErrorIfWrongUserName() throws Exception
    {
        // Set up
        final User user = new MockUser("testuser");
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("610");
        when(crowdService.getUser("baduser")).thenReturn(null);
        when(crowdService.getUser("testuser")).thenReturn(user);
        when(crowdService.authenticate("testuser", PASSWORD)).thenReturn(user);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);
        JohnsonEventContainer johnsonEventContainer = JohnsonEventContainer.get(mockServletContext);
        johnsonEventContainer.addEvent(new Event(EventType.get(LICENSE_TOO_OLD), "blah"));

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense =
                new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter,
                        crowdService, mockPermissionManager, jiraProperties, clusterManager);
        confirmNewInstallationWithOldLicense.setUserName("baduser");

        // Set the confirmation so that no errors are raised for its absence (or the absence of the new license key)
        confirmNewInstallationWithOldLicense.setRadioOption(RADIO_OPTION_EVALUATION);
        
        // Invoke
        confirmNewInstallationWithOldLicense.execute();
        
        // Check
        assertEquals(1, confirmNewInstallationWithOldLicense.getErrorMessages().size());
    }

    @Test
    public void testErrorIfWrongPassword() throws Exception
    {
        final User user = new MockUser("testuser");
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("610");
        when(crowdService.getUser("testuser")).thenReturn(user);
        when(crowdService.authenticate("testuser", "badpassword")).thenThrow(new FailedAuthenticationException());
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);
        JohnsonEventContainer johnsonEventContainer = JohnsonEventContainer.get(mockServletContext);
        johnsonEventContainer.addEvent(new Event(EventType.get(LICENSE_TOO_OLD), "blah"));

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense =
                new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter,
                        crowdService, mockPermissionManager, jiraProperties, clusterManager);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword("badpassword");

        // Set the confirmation so that no errors are raised for its absence (or the absence of the new license key)
        confirmNewInstallationWithOldLicense.setRadioOption(RADIO_OPTION_EVALUATION);
        final String errorMessage = "Rejected!";
        when(mockI18nHelper.getText("admin.errors.invalid.username.or.pasword")).thenReturn(errorMessage);

        // Invoke
        confirmNewInstallationWithOldLicense.execute();

        // Check
        final Collection<String> errorMessages = confirmNewInstallationWithOldLicense.getErrorMessages();
        assertEquals(1, errorMessages.size());
        assertEquals(errorMessage, errorMessages.iterator().next());
    }

    @Test
    public void testErrorIfCredentialsCorrectButNotAdmin() throws Exception
    {
        final User user = new MockUser("testuser");
        final ApplicationUser appUser = new DelegatingApplicationUser("testadminuser", user);
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("610");
        when(crowdService.getUser("testuser")).thenReturn(user);
        when(crowdService.authenticate("testuser", PASSWORD)).thenReturn(user);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, appUser)).thenReturn(false);


        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);
        JohnsonEventContainer johnsonEventContainer = JohnsonEventContainer.get(mockServletContext);
        johnsonEventContainer.addEvent(new Event(EventType.get(LICENSE_TOO_OLD), "blah"));

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense =
                new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter,
                        crowdService, mockPermissionManager, jiraProperties, clusterManager);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);

        // Set the confirmation so that no errors are raised for its absence (or the absence of the new license key)
        confirmNewInstallationWithOldLicense.setRadioOption(RADIO_OPTION_EVALUATION);
        final String errorMessage = "Whoopsie!";
        when(mockI18nHelper.getText("admin.errors.no.admin.permission")).thenReturn(errorMessage);

        // Invoke
        confirmNewInstallationWithOldLicense.execute();

        // Check
        assertEquals(1, confirmNewInstallationWithOldLicense.getErrorMessages().size());
        assertEquals(errorMessage, confirmNewInstallationWithOldLicense.getErrorMessages().iterator().next());
    }

    @Test
    public void testNoErrorIfCorrectAdminCredentialsProvided() throws Exception
    {
        final User user = new MockUser("testadminuser");
        final ApplicationUser appUser = new DelegatingApplicationUser("testadminuser", user);
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("610");
        when(crowdService.getUser("testadminuser")).thenReturn(user);
        when(crowdService.authenticate("testadminuser", PASSWORD)).thenReturn(user);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, appUser)).thenReturn(true);


        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);
        JohnsonEventContainer johnsonEventContainer = JohnsonEventContainer.get(mockServletContext);
        johnsonEventContainer.addEvent(new Event(EventType.get(LICENSE_TOO_OLD), "blah"));

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense =
                new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter,
                        crowdService, mockPermissionManager, jiraProperties, clusterManager);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);
        confirmNewInstallationWithOldLicense.setRadioOption(RADIO_OPTION_EVALUATION);

        // Invoke
        confirmNewInstallationWithOldLicense.execute();

        // Check
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrorMessages().size());
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrors().size());
        verify(mockPermissionManager).hasPermission(Permissions.ADMINISTER, appUser);
        verifyNoMoreInteractions(mockPermissionManager);
    }

    @Test
    public void testNoErrorIfNotAdminWithSystemProperty() throws Exception
    {
        final User user = new MockUser("testadminuser");
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("610");
        when(crowdService.getUser("testadminuser")).thenReturn(user);
        when(crowdService.authenticate("testadminuser", PASSWORD)).thenReturn(user);
        System.setProperty(UPGRADE_SYSTEM_PROPERTY, "true");

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);
        JohnsonEventContainer johnsonEventContainer = JohnsonEventContainer.get(mockServletContext);
        johnsonEventContainer.addEvent(new Event(EventType.get(LICENSE_TOO_OLD), "blah"));

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense =
                new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter,
                        crowdService, mockPermissionManager, jiraProperties, clusterManager);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);
        confirmNewInstallationWithOldLicense.setRadioOption(RADIO_OPTION_EVALUATION);

        // Invoke
        confirmNewInstallationWithOldLicense.execute();

        // Check
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrorMessages().size());
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrors().size());
        verifyZeroInteractions(mockPermissionManager);
    }

    @Test
    public void testErrorIfNoLicenseKeyAndNoConfirmation() throws Exception
    {
        // Set up
        final User user = new MockUser("testadminuser");
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("610");
        when(crowdService.getUser("testadminuser")).thenReturn(user);
        when(crowdService.authenticate("testadminuser", "testadminpassword")).thenReturn(user);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);
        JohnsonEventContainer johnsonEventContainer = JohnsonEventContainer.get(mockServletContext);
        johnsonEventContainer.addEvent(new Event(EventType.get(LICENSE_TOO_OLD), "blah"));

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense =
                new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter,
                        crowdService, mockPermissionManager, jiraProperties, clusterManager);
        confirmNewInstallationWithOldLicense.setUserName("testadminuser");
        confirmNewInstallationWithOldLicense.setPassword("testadminpassword");

        // Invoke
        confirmNewInstallationWithOldLicense.execute();

        // Check
        assertEquals(1, confirmNewInstallationWithOldLicense.getErrorMessages().size());
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrors().size());
    }


    @Test
    public void testNoErrorIfLicenseKeySupplied() throws Exception
    {
        // Set up
        final User user = new MockUser("testadminuser");
        final ApplicationUser appUser = new DelegatingApplicationUser("testadminuser", user);
        when(crowdService.getUser("testadminuser")).thenReturn(user);
        when(crowdService.authenticate("testadminuser", PASSWORD)).thenReturn(user);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, appUser)).thenReturn(true);


        final JiraLicenseService.ValidationResult validationResult = mock(JiraLicenseService.ValidationResult.class);
        final LicenseDetails licenseDetails = mock(LicenseDetails.class);
        final Date buildDate = new Date();
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        when(jiraLicenseService.validate((I18nHelper) any(), eq(A_VALID_LICENSE))).thenReturn(validationResult);
        when(validationResult.getErrorCollection()).thenReturn(errorCollection);
        when(jiraLicenseService.setLicense(validationResult)).thenReturn(licenseDetails);
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("610");
        when(buildUtilsInfo.getCurrentBuildDate()).thenReturn(buildDate);
        when(licenseDetails.isMaintenanceValidForBuildDate(buildDate)).thenReturn(true);


        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);
        JohnsonEventContainer johnsonEventContainer = JohnsonEventContainer.get(mockServletContext);
        johnsonEventContainer.addEvent(new Event(EventType.get(LICENSE_TOO_OLD), "blah"));


        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense =
                new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter,
                        crowdService, mockPermissionManager, jiraProperties, clusterManager);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);
        confirmNewInstallationWithOldLicense.setLicense(A_VALID_LICENSE);
        confirmNewInstallationWithOldLicense.setRadioOption(ConfirmNewInstallationWithOldLicense.RADIO_OPTION_LICENSE);

        // Invoke
        confirmNewInstallationWithOldLicense.execute();

        // Check
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrorMessages().size());
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrors().size());
    }

    @Test
    public void testErrorIfOutdatedLicenseKey() throws Exception
    {
        // Set up
        final User user = new MockUser("testadminuser");
        final ApplicationUser appUser = new DelegatingApplicationUser("testadminuser", user);
        when(crowdService.getUser("testadminuser")).thenReturn(user);
        when(crowdService.authenticate("testadminuser", PASSWORD)).thenReturn(user);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, appUser)).thenReturn(true);

        final JiraLicenseService.ValidationResult validationResult = mock(JiraLicenseService.ValidationResult.class);
        final LicenseDetails licenseDetails = mock(LicenseDetails.class);
        final Date buildDate = new Date();
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        when(jiraLicenseService.validate((I18nHelper) any(), eq(A_VALID_LICENSE))).thenReturn(validationResult);
        when(validationResult.getErrorCollection()).thenReturn(errorCollection);

        when(jiraLicenseService.setLicense(validationResult)).thenReturn(licenseDetails);
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("610");
        when(buildUtilsInfo.getCurrentBuildDate()).thenReturn(buildDate);
        when(licenseDetails.isMaintenanceValidForBuildDate(buildDate)).thenReturn(false);

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);
        JohnsonEventContainer johnsonEventContainer = JohnsonEventContainer.get(mockServletContext);
        johnsonEventContainer.addEvent(new Event(EventType.get(LICENSE_TOO_OLD), "blah"));

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense =
                new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter,
                        crowdService, mockPermissionManager, jiraProperties, clusterManager);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);
        confirmNewInstallationWithOldLicense.setLicense(A_VALID_LICENSE);
        confirmNewInstallationWithOldLicense.setRadioOption(ConfirmNewInstallationWithOldLicense.RADIO_OPTION_LICENSE);

        // Invoke
        confirmNewInstallationWithOldLicense.execute();

        // Check
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrorMessages().size());
        assertEquals(1, confirmNewInstallationWithOldLicense.getErrors().size());
    }

    @Test
    public void testErrorIfOutdatedLicenseKeyAndThereIsNoApplicationUserKeyForThisUser() throws Exception
    {
        final User user = new MockUser("testadminuser");
        final ApplicationUser appUser = new DelegatingApplicationUser("testadminuser", user);
        when(buildUtilsInfo.getCurrentBuildNumber()).thenReturn("610");
        when(crowdService.getUser("testadminuser")).thenReturn(user);
        when(crowdService.authenticate("testadminuser", PASSWORD)).thenReturn(user);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, appUser)).thenReturn(false);

        when(mockUserKeyService.getKeyForUsername("testadminuser")).thenReturn(null); // testing specific code path

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);
        JohnsonEventContainer johnsonEventContainer = JohnsonEventContainer.get(mockServletContext);
        johnsonEventContainer.addEvent(new Event(EventType.get(LICENSE_TOO_OLD), "blah"));

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense =
                new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter,
                        crowdService, mockPermissionManager, jiraProperties, clusterManager);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);
        confirmNewInstallationWithOldLicense.setRadioOption(RADIO_OPTION_EVALUATION);

        // Invoke
        confirmNewInstallationWithOldLicense.execute();

        // Check
        assertEquals(1, confirmNewInstallationWithOldLicense.getErrorMessages().size());
        verify(mockPermissionManager).hasPermission(Permissions.ADMINISTER, appUser);
    }

}
