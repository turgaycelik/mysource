package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.RedirectSanitiser;

import com.mockobjects.servlet.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDeleteScheme
{
    private static final String ERROR_MESSAGE = "Whoops!";

    private static <E> void checkSingleElementCollection(final Collection<? extends E> actual, final E expected)
    {
        assertEquals(1, actual.size());
        assertEquals(expected, actual.iterator().next());
    }

    private EditFieldLayoutScheme efls;
    @Mock private FieldLayoutManager mockFieldLayoutManager;
    @Mock private I18nHelper mockI18nHelper;
    @Mock private JiraAuthenticationContext mockJiraAuthenticationContext;
    private RedirectSanitiser mockRedirectSanitiser = new MockRedirectSanitiser();

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        when(mockJiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        efls = new EditFieldLayoutScheme(mockFieldLayoutManager);
        new MockComponentWorker().init()
                .addMock(JiraAuthenticationContext.class, mockJiraAuthenticationContext)
                .addMock(RedirectSanitiser.class, mockRedirectSanitiser);
    }

    @After
    public void tearDown()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testGetsSets()
    {
        final Long id = 1L;
        String confirm = "confirm";
        efls.setId(id);
        efls.setConfirm(confirm);
        assertEquals(id, efls.getId());
        assertEquals(confirm, efls.getConfirm());
    }

    @Test
    public void testDoDeleteNoId()
    {
        // Set up
        when(mockI18nHelper.getText("admin.errors.id.required")).thenReturn(ERROR_MESSAGE);

        // Invoke
        final String result = efls.doDeleteScheme();

        // Check
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(efls.getErrorMessages(), ERROR_MESSAGE);
    }

    @Test
    public void testDoDeleteInvalidId()
    {
        // Set up
        final Long id = 1L;
        when(mockFieldLayoutManager.getMutableFieldLayoutScheme(id)).thenReturn(null);
        efls.setId(id);
        when(mockI18nHelper.getText("admin.errors.fieldlayout.invalid.id", "'" + id + "'")).thenReturn(ERROR_MESSAGE);

        // Invoke
        final String result = efls.doDeleteScheme();

        // Check
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(efls.getErrorMessages(), ERROR_MESSAGE);
    }

    @Test
    public void testDoDeleteHasAssociatedProjects()
    {
        // Set up
        final FieldLayoutScheme fieldLayoutScheme = mock(FieldLayoutScheme.class);
        final Long id = 1L;
        when(mockFieldLayoutManager.getMutableFieldLayoutScheme(id)).thenReturn(fieldLayoutScheme);
        when(mockFieldLayoutManager.getProjects(fieldLayoutScheme))
                .thenReturn(Collections.singleton(mock(GenericValue.class)));
        efls.setId(id);
        when(mockI18nHelper.getText("admin.errors.fieldlayout.cannot.delete")).thenReturn(ERROR_MESSAGE);

        // Invoke
        final String result = efls.doDeleteScheme();

        // Check
        assertEquals(Action.ERROR, result);
        checkSingleElementCollection(efls.getErrorMessages(), ERROR_MESSAGE);
    }

    @Test
    public void testDoDeleteConfirm()
    {
        final FieldLayoutScheme fieldLayoutScheme = mock(FieldLayoutScheme.class);
        final Long id = 1L;
        when(mockFieldLayoutManager.getMutableFieldLayoutScheme(id)).thenReturn(fieldLayoutScheme);
        when(mockFieldLayoutManager.getProjects(fieldLayoutScheme)).thenReturn(Collections.<GenericValue>emptyList());
        efls.setId(id);
        assertEquals("confirm", efls.doDeleteScheme());
    }

    @Test
    public void testDoDelete() throws IOException
    {
        // Set up
        final FieldLayoutScheme fieldLayoutScheme = mock(FieldLayoutScheme.class);
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewFieldLayoutSchemes.jspa");
        final Long id = 1L;
        when(mockFieldLayoutManager.getMutableFieldLayoutScheme(id)).thenReturn(fieldLayoutScheme);
        when(mockFieldLayoutManager.getProjects(fieldLayoutScheme)).thenReturn(Collections.<GenericValue>emptyList());
        efls.setId(id);
        efls.setConfirm("true");

        // Invoke
        final String result = efls.doDeleteScheme();

        // Check
        assertEquals(Action.NONE, result);
        response.verify();
        verify(fieldLayoutScheme).remove();
    }
}
