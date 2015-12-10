package com.atlassian.jira.auditing.handlers;

import java.util.Locale;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.permission.PermissionAddedEvent;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/*
 * @since v6.3
 */
@RunWith (ListeningMockitoRunner.class)
public class PermissionChangeHandlerImplTest
{

    @Mock
    private PermissionSchemeManager permissionSchemeManager;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private PermissionTypeManager permissionTypeManager;

    @Mock
    private I18nHelper.BeanFactory beanFactory;

    @Mock
    private SchemeEntity schemeEntity;

    @Mock
    private com.atlassian.jira.util.I18nHelper i18Helper;

    @Mock
    private com.atlassian.jira.security.type.SecurityType securityType;

    @Before
    public void setUp() throws Exception
    {
        ComponentAccessor.initialiseWorker(new MockComponentWorker()
                        .addMock(I18nHelper.BeanFactory.class, beanFactory)
        );

        when(beanFactory.getInstance(Locale.ENGLISH)).thenReturn(i18Helper);
        when(i18Helper.getText(anyString())).thenReturn("some text value");
    }


    @Test
    public void schemeEntityTypeIdCanBeInteger() throws Exception
    {
        when(permissionTypeManager.getSchemeType("scheme type")).thenReturn(securityType);
        when(securityType.getDisplayName()).thenReturn("some name");

        final PermissionChangeHandlerImpl permissionChangeHandler =
                new PermissionChangeHandlerImpl(permissionSchemeManager, permissionManager, permissionTypeManager, beanFactory)
                {
                    protected String getPermissionName(Long permissionId)
                    {
                        return "some permission key";
                    }
                };

        final PermissionAddedEvent permissionAddedEvent = new PermissionAddedEvent(1L, new SchemeEntity("scheme type", 1));
        permissionChangeHandler.computeChangedValues(permissionAddedEvent);
    }

    @Test
    public void schemeEntityTypeIdCanBeLong() throws Exception
    {
        when(permissionTypeManager.getSchemeType("scheme type")).thenReturn(securityType);
        when(securityType.getDisplayName()).thenReturn("some name");

        final PermissionChangeHandlerImpl permissionChangeHandler =
                new PermissionChangeHandlerImpl(permissionSchemeManager, permissionManager, permissionTypeManager, beanFactory)
                {
                    protected String getPermissionName(Long permissionId)
                    {
                        return "some permission key";
                    }
                };

        final PermissionAddedEvent permissionAddedEvent = new PermissionAddedEvent(1L, new SchemeEntity("scheme type", 1L));
        permissionChangeHandler.computeChangedValues(permissionAddedEvent);
    }
}
