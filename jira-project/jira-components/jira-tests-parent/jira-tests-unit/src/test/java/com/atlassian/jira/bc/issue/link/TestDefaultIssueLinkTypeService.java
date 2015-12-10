package com.atlassian.jira.bc.issue.link;

import java.util.Collections;

import com.atlassian.crowd.model.user.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeDestroyer;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.matchers.IterableMatchers;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * @since v6.0
 */
public class TestDefaultIssueLinkTypeService
{
    @Rule public InitMockitoMocks initMocks = new InitMockitoMocks(this);
    @Mock private PermissionManager permissionManager;
    @Mock private IssueLinkTypeManager issueLinkTypeManager;
    @Mock private IssueLinkTypeDestroyer issueLinkTypeDestroyer;
    private MockI18nBean.MockI18nBeanFactory i18n = new MockI18nBean.MockI18nBeanFactory();
    @Mock private User user;
    @Mock IssueLinkType linkType;
    @Mock ApplicationProperties applicationProperties;

    @Before
    public void enableLinking()
    {
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING)).thenReturn(true);
    }

    @Test
    public void testOnlyAdminCreates()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        DefaultIssueLinkTypeService service = new DefaultIssueLinkTypeService(permissionManager, issueLinkTypeManager, issueLinkTypeDestroyer, i18n, applicationProperties);

        ServiceOutcome<IssueLinkType> outcome = service.createIssueLinkType(user, "Duplicate", "duplicates", "is duplicated by");
        assertThat(outcome.isValid(), is(false));
        assertThat(outcome.getErrorCollection().getReasons(), IterableMatchers.isSingleton(ErrorCollection.Reason.FORBIDDEN));
    }

    @Test
    public void testErrorWhenCreateFailsMysteriously()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(issueLinkTypeManager.getIssueLinkTypesByName("Duplicate")).thenReturn(Collections.<IssueLinkType>emptyList());

        DefaultIssueLinkTypeService service = new DefaultIssueLinkTypeService(permissionManager, issueLinkTypeManager, issueLinkTypeDestroyer, i18n, applicationProperties);
        ServiceOutcome<IssueLinkType> outcome = service.createIssueLinkType(user, "Duplicate", "duplicates", "is duplicated by");
        assertThat(outcome.isValid(), is(false));
        assertThat(outcome.getErrorCollection().getReasons(), IterableMatchers.isSingleton(ErrorCollection.Reason.SERVER_ERROR));

    }

    @Test
    public void testOnlyAdminDeletes()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        DefaultIssueLinkTypeService service = new DefaultIssueLinkTypeService(permissionManager, issueLinkTypeManager, issueLinkTypeDestroyer, i18n, applicationProperties);

        ServiceOutcome<IssueLinkType> outcome = service.deleteIssueLinkType(user, linkType);
        assertThat(outcome.isValid(), is(false));
        assertThat(outcome.getErrorCollection().getReasons(), IterableMatchers.isSingleton(ErrorCollection.Reason.FORBIDDEN));
    }

    @Test
    public void testRemoveExceptionCaught() throws Exception
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);
        when(linkType.getId()).thenReturn(100L);
        doThrow(new RemoveException("testing")).when(issueLinkTypeDestroyer).removeIssueLinkType(100L, null, user);

        DefaultIssueLinkTypeService service = new DefaultIssueLinkTypeService(permissionManager, issueLinkTypeManager, issueLinkTypeDestroyer, i18n, applicationProperties);

        ServiceOutcome<IssueLinkType> outcome = service.deleteIssueLinkType(user, linkType);
        assertThat(outcome.isValid(), is(false));
        assertThat(outcome.getErrorCollection().getReasons(), IterableMatchers.isSingleton(ErrorCollection.Reason.SERVER_ERROR));
    }

    @Test
    public void testOnlyAdminUpdates()
    {
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        DefaultIssueLinkTypeService service = new DefaultIssueLinkTypeService(permissionManager, issueLinkTypeManager, issueLinkTypeDestroyer, i18n, applicationProperties);

        ServiceOutcome<IssueLinkType> outcome = service.updateIssueLinkType(user, linkType, "Duplicate", "duplicates", "is duplicated by");
        assertThat(outcome.isValid(), is(false));
        assertThat(outcome.getErrorCollection().getReasons(), IterableMatchers.isSingleton(ErrorCollection.Reason.FORBIDDEN));
    }
}
