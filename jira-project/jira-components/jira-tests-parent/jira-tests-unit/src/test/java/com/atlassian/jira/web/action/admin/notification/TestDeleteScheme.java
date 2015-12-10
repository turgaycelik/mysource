package com.atlassian.jira.web.action.admin.notification;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.RedirectSanitiser;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockitoAnnotations.Mock;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDeleteScheme
{
    private final static Long SCHEME_ID = 1l;

    @Rule
    public MockitoContainer mockitoContainer = new MockitoContainer(this);

    @Mock
    @AvailableInContainer
    NotificationSchemeManager notificationSchemeManager;
    @Mock
    @AvailableInContainer
    private RedirectSanitiser redirectSanitiser;
    @Mock
    @AvailableInContainer
    JiraAuthenticationContext jiraAuthenticationContext;

    private I18nHelper i18nHelper = new MockI18nHelper();

    private Scheme mockScheme;

    @Before
    public void setUp()
    {
        when(jiraAuthenticationContext.getI18nHelper()).thenReturn(i18nHelper);

        mockScheme = mock(Scheme.class);
        when(mockScheme.getId()).thenReturn(SCHEME_ID);

        when(notificationSchemeManager.getSchemeObject(SCHEME_ID)).thenReturn(mockScheme);
    }

    @Test
    public void testDoExecuteValidationFailed() throws Exception
    {
        List<Project> projects = ImmutableList.of(mock(Project.class), mock(Project.class));

        when(notificationSchemeManager.getProjects(mockScheme)).thenReturn(projects);

        executeDeleteScheme(SCHEME_ID, true);

        for (Project project : projects)
        {
            verify(notificationSchemeManager, never()).removeSchemesFromProject(project);
            verify(notificationSchemeManager, never()).addDefaultSchemeToProject(project);
        }

        verify(notificationSchemeManager, never()).deleteScheme(SCHEME_ID);
    }

    @Test
    public void testDoExecuteConcurrently() throws Exception
    {
        List<Project> projects = ImmutableList.of(mock(Project.class), mock(Project.class));

        // return empty list first and then return project list assuming that we will be inside doExecute function
        when(notificationSchemeManager.getProjects(mockScheme)).thenReturn(ImmutableList.<Project>of()).thenReturn(projects);

        executeDeleteScheme(SCHEME_ID, true);

        for (Project project : projects)
        {
            verify(notificationSchemeManager).removeSchemesFromProject(project);
            verify(notificationSchemeManager).addDefaultSchemeToProject(project);
        }

        verify(notificationSchemeManager).deleteScheme(SCHEME_ID);
    }

    @Test
    public void testDoValidateCheckForNullSchemeId() throws Exception
    {
        assertThat(validateDeleteSchemeForErrors(null, false).values(), hasItem("admin.errors.deletescheme.no.scheme.specified"));
        assertThat(validateDeleteSchemeForErrors(SCHEME_ID, false).values(), not(hasItem("admin.errors.deletescheme.no.scheme.specified")));
    }

    @Test
    public void testDoValidateCheckForNullDefaultSchemeObjectAndIdEquality() throws Exception
    {
        assertThat(validateDeleteScheme(SCHEME_ID, true), not(hasItem("admin.errors.deletescheme.cannot.delete.default")));
        when(notificationSchemeManager.getDefaultSchemeObject()).thenReturn(mockScheme);
        assertThat(validateDeleteScheme(SCHEME_ID, true), hasItem("admin.errors.deletescheme.cannot.delete.default"));
    }

    @Test
    public void testDoValidateCheckForConfirmed() throws Exception
    {
        assertThat(validateDeleteScheme(SCHEME_ID, true), not(hasItem("admin.errors.deletescheme.confirmation")));
        assertThat(validateDeleteScheme(SCHEME_ID, false), hasItem("admin.errors.deletescheme.confirmation"));
    }

    @Test
    public void testDoValidateCheckForActiveProjects() throws Exception
    {
        assertThat(validateDeleteScheme(SCHEME_ID, false), not(hasItem("admin.errors.deletescheme.cannot.delete.active [null]")));
        when(notificationSchemeManager.getProjects(mockScheme)).thenReturn(ImmutableList.<Project>of(mock(Project.class)));
        assertThat(validateDeleteScheme(SCHEME_ID, false), hasItem("admin.errors.deletescheme.cannot.delete.active [null]"));
    }

    private Collection<String> validateDeleteScheme(Long schemeId, boolean confirmed) throws Exception
    {
        DeleteScheme deleteScheme = executeDeleteScheme(schemeId, confirmed);
        return deleteScheme.getErrorMessages();
    }

    private Map<String, String> validateDeleteSchemeForErrors(Long schemeId, boolean confirmed) throws Exception
    {
        DeleteScheme deleteScheme = executeDeleteScheme(schemeId, confirmed);
        return deleteScheme.getErrors();
    }

    private DeleteScheme executeDeleteScheme(Long schemeId, boolean confirmed) throws Exception
    {
        DeleteScheme deleteScheme = new DeleteScheme(notificationSchemeManager);
        deleteScheme.setSchemeId(schemeId);
        deleteScheme.setConfirmed(confirmed);
        deleteScheme.execute();

        return deleteScheme;
    }
}
