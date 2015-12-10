package com.atlassian.jira.rest.v2.index;

import com.atlassian.jira.config.IndexTaskContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskMatcher;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**

 *
 * @since v6.1.4
 */
@RunWith(MockitoJUnitRunner.class)
public class TaskDescriptorHelperTest
{
    @Mock
    private TaskManager mockTaskManager;
    @Mock
    private TaskDescriptor<IndexCommandResult> mockLiveDescriptor;
    @Mock
    private TaskDescriptor<IndexCommandResult> mockFinishedTaskDescriptor1;
    @Mock
    private TaskDescriptor<IndexCommandResult> mockFinishedTaskDescriptor2;
    private TaskDescriptorHelper helper;


    @Before
    public void setupHelper()
    {
        helper = new TaskDescriptorHelper(mockTaskManager);
    }

    @Test
    public void testGetLiveTask() throws Exception
    {
        doReturn(mockLiveDescriptor).when(mockTaskManager).getLiveTask(new IndexTaskContext());
        assertEquals(mockLiveDescriptor, helper.getActiveIndexTask());
    }

    @Test
    public void testGetLatestTask() throws Exception
    {
        doReturn(mockLiveDescriptor).when(mockTaskManager).getLiveTask(new IndexTaskContext());
        doReturn(Lists.newArrayList(mockFinishedTaskDescriptor2, mockFinishedTaskDescriptor1)).when(mockTaskManager).findTasks(any(TaskMatcher.class));
        when(mockFinishedTaskDescriptor1.getTaskId()).thenReturn(1L);
        when(mockFinishedTaskDescriptor2.getTaskId()).thenReturn(2L);
        assertEquals(mockFinishedTaskDescriptor2, helper.getLastindexTask());
    }

}
