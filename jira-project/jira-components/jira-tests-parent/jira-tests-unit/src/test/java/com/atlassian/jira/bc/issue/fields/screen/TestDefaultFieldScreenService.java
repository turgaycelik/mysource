package com.atlassian.jira.bc.issue.fields.screen;

import java.util.Collections;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.MockFieldScreen;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultFieldScreenService
{
    @Mock
    private I18nHelper.BeanFactory i18nFactory;

    @Mock
    private FieldScreenManager fieldScreenManager;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private I18nHelper i18n;

    @Mock
    private ApplicationUser user;

    private DefaultFieldScreenService fieldScreenService;

    @Before
    public void setUp()
    {
        when(i18nFactory.getInstance(user)).thenReturn(i18n);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(true);

        fieldScreenService = new DefaultFieldScreenService(i18nFactory, fieldScreenManager, permissionManager);
    }

    @Test
    public void testCopyWithInvalidName()
    {
        FieldScreen screen = createScreenForCopy();

        when(i18n.getText("admin.common.errors.validname")).thenReturn("Invalid name");

        ServiceOutcome<FieldScreen> result = fieldScreenService.copy(screen, null, "", user);
        assertEquals("Invalid name", result.getErrorCollection().getErrorMessages().iterator().next());

        result = fieldScreenService.copy(screen, "", "", user);
        assertEquals("Invalid name", result.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testNoPermissionToCopy()
    {
        FieldScreen screen = createScreenForCopy();

        when(i18n.getText("admin.errors.screens.no.permission")).thenReturn("No permission");
        when(permissionManager.hasPermission(Permissions.ADMINISTER, user)).thenReturn(false);

        ServiceOutcome<FieldScreen> result = fieldScreenService.copy(screen, "Copy name", "", user);
        assertEquals("No permission", result.getErrorCollection().getErrorMessages().iterator().next());
    }

    @Test
    public void testCopyWithExistingName()
    {
        FieldScreen screen = createScreenForCopy();

        when(i18n.getText("admin.errors.screens.duplicate.screen.name")).thenReturn("Duplicate name");

        FieldScreen existing = new MockFieldScreen();
        existing.setName("Copy name");
        when(fieldScreenManager.getFieldScreens()).thenReturn(asList(existing));

        ServiceOutcome<FieldScreen> result = fieldScreenService.copy(screen, "Copy name", "", user);
        assertEquals("Duplicate name", result.getErrorCollection().getErrorMessages().iterator().next());

        when(fieldScreenManager.getFieldScreens()).thenReturn(Collections.<FieldScreen>emptyList());
    }

    @Test
    public void testCopiesAllAttributes()
    {
        FieldScreen screen = createScreenForCopy();

        FieldScreenLayoutItem itemOne = Mockito.mock(FieldScreenLayoutItem.class);
        FieldScreenLayoutItem itemTwo = Mockito.mock(FieldScreenLayoutItem.class);

        when(fieldScreenManager.buildNewFieldScreenLayoutItem("field 1")).thenReturn(itemOne);
        when(fieldScreenManager.buildNewFieldScreenLayoutItem("field 2")).thenReturn(itemTwo);

        ServiceOutcome<FieldScreen> result = fieldScreenService.copy(screen, "Copy name", "", user);

        FieldScreen copy = result.getReturnedValue();

        assertNotNull(copy);
        assertEquals("Copy name", copy.getName());
        assertEquals("", copy.getDescription());

        assertEquals("Tab 1", copy.getTab(0).getName());
        assertEquals("Tab 2", copy.getTab(1).getName());

        assertEquals(asList(itemOne), copy.getTab(0).getFieldScreenLayoutItems());
        assertEquals(asList(itemTwo), copy.getTab(1).getFieldScreenLayoutItems());
    }

    private FieldScreen createScreenForCopy()
    {
        FieldScreen screen = new MockFieldScreen();
        screen.setName("Name");
        screen.setDescription("Description");

        FieldScreenTab tabOne = screen.addTab("Tab 1");
        FieldScreenTab tabTwo = screen.addTab("Tab 2");

        tabOne.addFieldScreenLayoutItem("field 1");
        tabTwo.addFieldScreenLayoutItem("field 2");

        return screen;
    }
}
