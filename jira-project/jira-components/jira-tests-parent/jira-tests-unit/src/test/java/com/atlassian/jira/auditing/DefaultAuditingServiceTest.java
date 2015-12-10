package com.atlassian.jira.auditing;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.PluginAccessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultAuditingServiceTest
{

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private I18nHelper.BeanFactory beanFactory;

    @Mock
    private I18nHelper i18nHelper;

    @Mock
    private AuditingManager auditingManager;

    @Mock
    private FeatureManager featureManager;

    @Mock
    private ApplicationUser user;
    
    @Mock 
    private AuditingFilter auditingFilter;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private PluginAccessor pluginAccessor;

    @Before
    public void setUp() {
        when(jiraAuthenticationContext.getUser()).thenReturn(user);
        when(beanFactory.getInstance(user)).thenReturn(i18nHelper);
    }

    @Test
    public void shouldReturnErrorForNonAdminUser() throws Exception {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);
        when(i18nHelper.getText("jira.auditing.service.no.admin.permission")).thenReturn("This is the error we expect");

        AuditingService auditingService = new DefaultAuditingService(permissionManager, beanFactory, auditingManager, featureManager, jiraAuthenticationContext, pluginAccessor);
        ServiceOutcome<Records> records = auditingService.getRecords(null, null, null);

        assertEquals(true, records.getErrorCollection().getErrors().isEmpty());
        assertEquals(1, records.getErrorCollection().getErrorMessages().size());
        assertTrue(records.getErrorCollection().getErrorMessages().contains("This is the error we expect"));
    }

    @Test
    public void shouldReturnRecordsWithoutSysadminEventsIfItsOnDemand() throws Exception {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(featureManager.isOnDemand()).thenReturn(true);

        AuditingService auditingService = new DefaultAuditingService(permissionManager, beanFactory, auditingManager, featureManager, jiraAuthenticationContext, pluginAccessor);
        auditingService.getRecords(1, 10, auditingFilter);

        verify(auditingManager).getRecordsWithoutSysAdmin(null, null, 10, 1, auditingFilter);
    }

    @Test
    public void shouldReturnAllRecordIfItsNotOnDemand() throws Exception {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(featureManager.isOnDemand()).thenReturn(false);

        AuditingService auditingService = new DefaultAuditingService(permissionManager, beanFactory, auditingManager, featureManager, jiraAuthenticationContext, pluginAccessor);
        auditingService.getRecords(1, 10, auditingFilter);

        verify(auditingManager).getRecords(null, null, 10, 1, auditingFilter);
    }

    @Test
    public void shouldNotCountSysAdminRecordsIfItsOnDemand() throws Exception {
        when(featureManager.isOnDemand()).thenReturn(true);

        AuditingService auditingService = new DefaultAuditingService(null, null, auditingManager, featureManager, jiraAuthenticationContext, pluginAccessor);
        auditingService.getTotalNumberOfRecords();

        verify(auditingManager, never()).countRecords(null, null);
        verify(auditingManager).countRecordsWithoutSysAdmin(null, null);
    }

    @Test
    public void shouldCountSysAdminRecordsIfItsNotOnDemand() throws Exception {
        when(featureManager.isOnDemand()).thenReturn(false);

        AuditingService auditingService = new DefaultAuditingService(null, null, auditingManager, featureManager, jiraAuthenticationContext, pluginAccessor);
        auditingService.getTotalNumberOfRecords();

        verify(auditingManager,never()).countRecordsWithoutSysAdmin(null, null);
        verify(auditingManager).countRecords(null, null);
    }
}
