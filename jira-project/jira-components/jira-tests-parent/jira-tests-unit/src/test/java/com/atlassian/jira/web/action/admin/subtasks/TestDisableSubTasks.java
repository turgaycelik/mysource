package com.atlassian.jira.web.action.admin.subtasks;

import java.util.Collections;
import java.util.Set;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.web.action.MockRedirectSanitiser;
import com.atlassian.jira.web.action.RedirectSanitiser;

import com.mockobjects.servlet.MockHttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import webwork.action.Action;

import static com.atlassian.jira.JiraTestUtil.setupExpectedRedirect;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test of {@link DisableSubTasks}.
 *
 * @since 6.2
 */
public class TestDisableSubTasks
{
    private DisableSubTasks disableSubTasks;
    @Mock private SubTaskManager mockSubTaskManager;
    private RedirectSanitiser mockRedirectSanitiser = new MockRedirectSanitiser();

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        new MockComponentWorker().init().addMock(RedirectSanitiser.class, mockRedirectSanitiser);
        disableSubTasks = new DisableSubTasks(mockSubTaskManager);
    }

    @After
    public void tearDownWorker()
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testDoDefaultNoSubTasks() throws Exception
    {
        final MockHttpServletResponse expectedRedirect = setupExpectedRedirect("ManageSubTasks.jspa");
        when(mockSubTaskManager.getAllSubTaskIssueIds()).thenReturn(Collections.<Long>emptySet());
        assertEquals(Action.NONE, disableSubTasks.doDefault());
        expectedRedirect.verify();
    }

    @Test
    public void testDoDefaultSubTasksExist() throws Exception
    {
        final Set<Long> issueIds = Collections.singleton(1000L);
        when(mockSubTaskManager.getAllSubTaskIssueIds()).thenReturn(issueIds);
        assertEquals(Action.INPUT, disableSubTasks.doDefault());
        assertEquals(issueIds.size(), disableSubTasks.getSubTaskCount());
    }

    @Test
    public void testDoExecute() throws Exception
    {
        final MockHttpServletResponse expectedRedirect = setupExpectedRedirect("ManageSubTasks.jspa");
        assertEquals(Action.NONE, disableSubTasks.execute());
        expectedRedirect.verify();
    }
}
