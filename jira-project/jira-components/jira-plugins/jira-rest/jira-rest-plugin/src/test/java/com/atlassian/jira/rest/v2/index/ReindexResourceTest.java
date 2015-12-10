package com.atlassian.jira.rest.v2.index;

import java.util.Collections;
import java.util.Date;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.atlassian.jira.config.BackgroundIndexTaskContext;
import com.atlassian.jira.config.ForegroundIndexTaskContext;
import com.atlassian.jira.config.IndexTaskContext;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskMatcher;
import com.atlassian.jira.task.TaskProgressEvent;
import com.atlassian.jira.task.TaskProgressIndicator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.index.IndexLifecycleManager;
import com.atlassian.jira.web.action.admin.index.ActivateAsyncIndexerCommand;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;
import com.atlassian.jira.web.action.admin.index.ReIndexBackgroundIndexerCommand;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 
 * @since v6.1.4
 */
@RunWith (MockitoJUnitRunner.class)
public class ReindexResourceTest
{
    @Mock private ReindexResource reindexResource;
    @Mock private IndexLifecycleManager mockLifeCycleManager;
    @Mock private TaskManager mockTaskManager;
    @Mock private JiraAuthenticationContext mockJiraAuthenticationContext;
    @Mock private PermissionManager mockPermissionManager;
    @Mock private JiraBaseUrls mockJiraBaseUrls;
    @Mock private ApplicationUser mockUser;
    @Mock private I18nHelper mockI18nHelper;
    @Mock private TaskDescriptor<IndexCommandResult> mockForegroundIndexTask;
    @Mock private TaskDescriptor<IndexCommandResult> mockBackgroundIndexTask;
    @Mock private TaskDescriptor<IndexCommandResult> mockActiveForegroundIndexTask;
    @Mock private TaskDescriptor<IndexCommandResult> mockActiveBackgroundIndexTask;
    @Mock private TaskDescriptor<IndexCommandResult> mockFinishedForegroundIndexTask;

    private Date submittedDate = new Date(1000000000);
    private Date starteddDate = new Date(1000001000);
    private Date finishedDate = new Date(1000005000);
    private MockI18nBean.MockI18nBeanFactory mockI18nBeanFactory;

    @Before
    public void setupMocks() throws Exception
    {
        setupLoggedInUser();
        setupCreatedTasks();
        setupAllTasks();
        when(mockJiraAuthenticationContext.getI18nHelper()).thenReturn(mockI18nHelper);
        when(mockI18nHelper.getText("admin.indexing.jira.indexing")).thenReturn("Indexing");
        when(mockI18nHelper.getText("admin.indexing.strategy.background.unsafe")).thenReturn("Background Indexing Not Available");
        when(mockI18nHelper.getText("admin.indexing.no.task.found")).thenReturn("No Indexing Task Found");
        when(mockJiraBaseUrls.restApi2BaseUrl()).thenReturn("http://localhost/rest/");
        mockI18nBeanFactory = new MockI18nBean.MockI18nBeanFactory();
        reindexResource = new ReindexResource(mockLifeCycleManager, mockTaskManager, mockJiraAuthenticationContext, mockPermissionManager, mockJiraBaseUrls, mockI18nBeanFactory);
    }


    @Test
    public void postRequestShouldStartIndexing()
    {
        final Response response = reindexResource.reindex("foreground", false, false);
        assertStatusCode(response, Response.Status.ACCEPTED);
        final MultivaluedMap<String, Object> headers = response.getMetadata();
        assertRetryAfter(headers, 10L);
        assertForegroundIndexing(response);
    }

    @Test
    public void postRequestWhileIndexingShouldReturn409()
    {
        doReturn(mockActiveForegroundIndexTask).when(mockTaskManager).getLiveTask(new IndexTaskContext());
        final Response response = reindexResource.reindex("foreground", false, false);
        assertStatusCode(response, Response.Status.CONFLICT);
    }
    
    @Test
    public void backgroundPreferredIndexShouldUseBackgroundIndexing()
    {
        when(mockLifeCycleManager.isIndexConsistent()).thenReturn(true);
        final Response response = reindexResource.reindex(null, false, false);
        assertStatusCode(response, Response.Status.ACCEPTED);
        final MultivaluedMap<String, Object> headers = response.getMetadata();
        assertRetryAfter(headers, 10L);
        assertBackgroundIndexing(response);
    }

    @Test
    public void backgroundPreferredIndexShouldBePromotedToForeground()
    {
        when(mockLifeCycleManager.isIndexConsistent()).thenReturn(false);
        final Response response = reindexResource.reindex(null, false, false);
        assertStatusCode(response, Response.Status.ACCEPTED);
        final MultivaluedMap<String, Object> headers = response.getMetadata();
        assertRetryAfter(headers, 10L);
        assertForegroundIndexing(response);
    }

    @Test
    public void backgroundIndexIsNotPromotedToForeground()
    {
        when(mockLifeCycleManager.isIndexConsistent()).thenReturn(false);
        final Response response = reindexResource.reindex("background", false, false);
        assertStatusCode(response, Response.Status.CONFLICT);
        assertEquals("Error expected in response", "Background Indexing Not Available", response.getEntity());
    }
    
    @Test
    public void getResponseWhileIndexingShouldBe303()
    {
        doReturn(Collections.singletonList(mockActiveForegroundIndexTask)).when(mockTaskManager).findTasks(isA(TaskMatcher.class));
        final Response response = reindexResource.getReindexInfo(0L);
        assertStatusCode(response, Response.Status.SEE_OTHER);
        final MultivaluedMap<String, Object> headers = response.getMetadata();
        assertRetryAfter(headers, 4L);
        assertForegroundIndexing(response);
    }
    
    @Test
    public void getResponseButNoIndexingHasTakenPlaceYetShouldIndicateWhyNot()
    {
        final Response response = reindexResource.getReindexInfo(0);
        assertStatusCode(response, Response.Status.NOT_FOUND);
        assertEquals("Error expected in response", "No Indexing Task Found", response.getEntity());
    }
    
    @Test
    public void getResponseWhenIndexHasFinishedShouldReturn200()
    {
        doReturn(Lists.newArrayList(mockFinishedForegroundIndexTask)).when(mockTaskManager).findTasks(isA(TaskMatcher.class));
        when(mockFinishedForegroundIndexTask.getTaskId()).thenReturn(1L);
        final Response response = reindexResource.getReindexInfo(0L);
        assertStatusCode(response, Response.Status.OK);
        final MultivaluedMap<String, Object> headers = response.getMetadata();
        assertTrue("Must Contain Last-Modifiedr", headers.containsKey("Last-Modified"));
        assertEquals("Last-Modified expected value", finishedDate, headers.getFirst("Last-Modified"));
    }

    @Test
    public void getResponseWithTaskIdAndIndexHasFinishedShouldReturn200()
    {
        doReturn(Lists.newArrayList(mockFinishedForegroundIndexTask)).when(mockTaskManager).findTasks(isA(TaskMatcher.class));
        when(mockFinishedForegroundIndexTask.getTaskId()).thenReturn(1L);

        when(mockTaskManager.getTask(1L)).thenReturn((TaskDescriptor) mockFinishedForegroundIndexTask);
        final Response response = reindexResource.getReindexInfo(1L);
        assertStatusCode(response, Response.Status.OK);
        final MultivaluedMap<String, Object> headers = response.getMetadata();
        assertTrue("Must Contain Last-Modifiedr", headers.containsKey("Last-Modified"));
        assertEquals("Last-Modified expected value", finishedDate, headers.getFirst("Last-Modified"));
    }

    private void setupLoggedInUser()
    {
        when(mockJiraAuthenticationContext.getUser()).thenReturn(mockUser);
        when(mockPermissionManager.hasPermission(Permissions.ADMINISTER, mockUser)).thenReturn(true);
    }

    private void setupCreatedTasks()
    {
        when(mockTaskManager.submitTask(isA(ActivateAsyncIndexerCommand.class), anyString(), isA(ForegroundIndexTaskContext.class), anyBoolean())).
                thenReturn(mockForegroundIndexTask);
        when(mockTaskManager.submitTask(isA(ReIndexBackgroundIndexerCommand.class), anyString(), isA(BackgroundIndexTaskContext.class), anyBoolean())).
                thenReturn(mockBackgroundIndexTask);
    }

    private void setupAllTasks() throws Exception
    {
        populateBeanDetails(mockForegroundIndexTask, new ForegroundIndexTaskContext(), false, false, false);
        populateBeanDetails(mockBackgroundIndexTask, new BackgroundIndexTaskContext(), false, false, false);
        populateBeanDetails(mockActiveForegroundIndexTask, new ForegroundIndexTaskContext(), true, false, false);
        populateBeanDetails(mockActiveBackgroundIndexTask, new BackgroundIndexTaskContext(), true, false, false);
        populateBeanDetails(mockFinishedForegroundIndexTask, new ForegroundIndexTaskContext(), true, true, true);
    }

    private void populateBeanDetails(final TaskDescriptor<IndexCommandResult> task, IndexTaskContext taskContext,
            boolean taskStarted, boolean taskFinished, boolean successful) throws Exception
    {
        final TaskProgressIndicator mockTaskProgressIndicator = mock(TaskProgressIndicator.class);
        final TaskProgressEvent mockTaskProgressEvent = mock(TaskProgressEvent.class);
        final IndexCommandResult mockResult = mock(IndexCommandResult.class);
        when(mockResult.isSuccessful()).thenReturn(successful);
        when(mockTaskProgressEvent.getTaskProgress()).thenReturn(50L);
        when(task.getTaskProgressIndicator()).thenReturn(mockTaskProgressIndicator);
        if (taskStarted)
        {
            when(mockTaskProgressIndicator.getLastProgressEvent()).thenReturn(mockTaskProgressEvent);
            when(task.getStartedTimestamp()).thenReturn(starteddDate);
        }
        when(task.getTaskContext()).thenReturn(taskContext);
        when(task.getSubmittedTimestamp()).thenReturn(submittedDate);
        if (taskFinished)
        {
            when(task.isFinished()).thenReturn(true);
            when(task.getFinishedTimestamp()).thenReturn(finishedDate);
            when(task.getResult()).thenReturn(mockResult);
        }
        when(task.getElapsedRunTime()).thenReturn(3000L);
    }

    private void assertStatusCode(final Response response, final Response.Status status)
    {
        assertEquals("Should return "+status.getStatusCode(), status.getStatusCode(), response.getStatus());
    }

    private void assertRetryAfter(final MultivaluedMap<String, Object> headers, final Long retryInterval)
    {
        assertTrue("Must Contain Retry-After", headers.containsKey("Retry-After"));
        assertEquals("Retry-After expected value", retryInterval, headers.getFirst("Retry-After"));
    }

    private void assertForegroundIndexing(final Response response)
    {
        final ReindexBean bean = (ReindexBean)response.getEntity();
        assertEquals("Should have been a foreground index", ReindexBean.Type.FOREGROUND, bean.getType());
    }

    private void assertBackgroundIndexing(final Response response)
    {
        final ReindexBean bean = (ReindexBean)response.getEntity();
        assertEquals("Should have been a background index", ReindexBean.Type.BACKGROUND, bean.getType());
    }
    
}
