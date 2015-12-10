package com.atlassian.jira.web.bean;

import java.util.Calendar;
import java.util.Date;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.task.MockTaskDescriptor;
import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskProgressEvent;
import com.atlassian.jira.task.TaskProgressIndicator;
import com.atlassian.jira.user.MockUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/** @since v3.13 */
@RunWith(MockitoJUnitRunner.class)
public class TestTaskDescriptorBean
{
    @Mock
    private TaskDescriptor<Long> taskDescriptor;

    @Mock
    private TaskProgressIndicator taskProgressIndicator;

    @Mock
    private DateTimeFormatter dateTimeFormatter;
    
    private I18nBean testI18nBean;
    private String testUserName;

    @Before
    public void setUp() throws Exception
    {
        testUserName = "TestTaskDescriptorBean";
        testI18nBean = new MockI18nBean();

        when(taskDescriptor.getTaskProgressIndicator()).thenReturn(taskProgressIndicator);
        when(dateTimeFormatter.withStyle(DateTimeStyle.RELATIVE)).thenReturn(dateTimeFormatter);
    }

    @After
    public void tearDown() throws Exception
    {
        taskDescriptor = null;
        testUserName = null;
        testI18nBean = null;
        dateTimeFormatter = null;
    }

    @Test
    public void testGetTaskDescriptor() throws Exception
    {
        final TaskDescriptorBean<?> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);

        assertSame(taskDescriptor, bean.getTaskDescriptor());
    }

    public void getFormattedElapsedRunTime() throws Exception
    {
        when(taskDescriptor.getElapsedRunTime()).thenReturn(0L).thenReturn(50000L);

        final TaskDescriptorBean<?> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals("0 seconds", bean.getFormattedElapsedRunTime());
        assertEquals("50 seconds", bean.getFormattedElapsedRunTime());
    }

    @Test
    public void testGetResult() throws Exception
    {
        when(taskDescriptor.getResult()).thenReturn(Long.MAX_VALUE);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(Long.MAX_VALUE, bean.getResult().longValue());
    }

    @Test
    public void testIsStarted() throws Exception
    {
        when(taskDescriptor.isStarted()).thenReturn(true).thenReturn(false);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(true, bean.isStarted());
        assertEquals(false, bean.isStarted());
    }

    @Test
    public void testIsFinished() throws Exception
    {
        when(taskDescriptor.isFinished()).thenReturn(true).thenReturn(false);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(true, bean.isFinished());
        assertEquals(false, bean.isFinished());
    }

    @Test
    public void testGetTaskId() throws Exception
    {
        when(taskDescriptor.getTaskId()).thenReturn(Long.MAX_VALUE).thenReturn(Long.MIN_VALUE);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(new Long(Long.MAX_VALUE), bean.getTaskId());
        assertEquals(new Long(Long.MIN_VALUE), bean.getTaskId());
    }

    @Test
    public void testGetStartedTimestamp() throws Exception
    {
        final Calendar cal = Calendar.getInstance();
        cal.set(2006, Calendar.DECEMBER, 11, 11, 12, 12);

        final Date date1 = cal.getTime();

        cal.set(2005, Calendar.NOVEMBER, 10, 10, 11, 11);

        final Date date2 = cal.getTime();

        when(taskDescriptor.getStartedTimestamp()).thenReturn(date1).thenReturn(date2);

        final TaskDescriptorBean<?> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(date1, bean.getStartedTimestamp());
        assertEquals(date2, bean.getStartedTimestamp());
    }

    @Test
    public void testGetStartedFinishedTimestamp() throws Exception
    {
        final Date date1 = new Date();
        final String date1String = "date1";

        when(taskDescriptor.getStartedTimestamp()).thenReturn(date1).thenReturn(null);
        when(dateTimeFormatter.format(date1)).thenReturn(date1String);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);

        assertEquals(date1String, bean.getFormattedStartedTimestamp());
        assertEquals("", bean.getFormattedStartedTimestamp());
    }

    @Test
    public void testGetFinishedTimestamp() throws Exception
    {
        final Calendar cal = Calendar.getInstance();
        cal.set(2006, Calendar.DECEMBER, 11, 11, 12, 12);

        final Date date1 = cal.getTime();

        cal.set(2005, Calendar.NOVEMBER, 10, 10, 11, 11);

        final Date date2 = cal.getTime();

        when(taskDescriptor.getFinishedTimestamp()).thenReturn(date1).thenReturn(date2);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(date1, bean.getFinishedTimestamp());
        assertEquals(date2, bean.getFinishedTimestamp());
    }

    @Test
    public void testGetFormattedFinishedTimestamp() throws Exception
    {
        final Date date1 = new Date();
        final String date1String = "date1String";

        when(taskDescriptor.getFinishedTimestamp()).thenReturn(date1).thenReturn(null);
        when(dateTimeFormatter.format(date1)).thenReturn(date1String);

        final TaskDescriptorBean bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);

        assertEquals(date1String, bean.getFormattedFinishedTimestamp());
        assertEquals("", bean.getFormattedFinishedTimestamp());
    }

    @Test
    public void testGetSubmittedTimestamp() throws Exception
    {
        final Calendar cal = Calendar.getInstance();
        cal.set(2006, Calendar.DECEMBER, 11, 11, 12, 12);

        final Date date1 = cal.getTime();

        cal.set(2004, Calendar.NOVEMBER, 10, 10, 11, 11);

        final Date date2 = cal.getTime();

        when(taskDescriptor.getSubmittedTimestamp()).thenReturn(date1).thenReturn(date2);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(date1, bean.getSubmittedTimestamp());
        assertEquals(date2, bean.getSubmittedTimestamp());
    }

    @Test
    public void testGetFormattedSubmittedTimestamp() throws Exception
    {
        final Date date1 = new Date();
        final String date1String = "date1String";

        when(taskDescriptor.getSubmittedTimestamp()).thenReturn(date1).thenReturn(null);
        when(dateTimeFormatter.format(date1)).thenReturn(date1String);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);

        assertEquals(date1String, bean.getFormattedSubmittedTimestamp());
    }

    @Test
    public void testGetElapsedRunTime() throws Exception
    {
        when(taskDescriptor.getElapsedRunTime()).thenReturn(0L).thenReturn(Long.MAX_VALUE);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(0L, bean.getElapsedRunTime());
        assertEquals(Long.MAX_VALUE, bean.getElapsedRunTime());
    }

    @Test
    public void testGetUser() throws Exception
    {
        when(taskDescriptor.getUserName()).thenReturn(testUserName);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(testUserName, bean.getUserName());
    }

    @Test
    public void testGetDescription() throws Exception
    {
        final String description1 = "Description1";
        final String description2 = "Description2";

        when(taskDescriptor.getDescription()).thenReturn(description1).thenReturn(description2);

        final TaskDescriptorBean bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(description1, bean.getDescription());
        assertEquals(description2, bean.getDescription());
    }

    @Test
    public void testGetContext() throws Exception
    {
        final TestContext ctx1 = new TestContext();
        final TestContext ctx2 = new TestContext();

        when(taskDescriptor.getTaskContext()).thenReturn(ctx1).thenReturn(ctx2);

        final TaskDescriptorBean bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(ctx1, bean.getTaskContext());
        assertEquals(ctx2, bean.getTaskContext());
    }

    @Test
    public void testGetTaskProgressIndicator() throws Exception
    {
        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertNull(bean.getTaskProgressIndicator());
        assertNull(bean.getTaskProgressIndicator());
        assertNull(bean.getTaskProgressIndicator());
        assertNull(bean.getTaskProgressIndicator());
    }

    @Test
    public void testGetFormattedProgress() throws Exception
    {
        final Date submittedDate = new Date();

        final MockTaskDescriptor<Long> descriptor = new MockTaskDescriptor<Long>();
        descriptor.setSubmittedTime(submittedDate);
        TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(descriptor, testI18nBean, dateTimeFormatter, testUserName);

        //it should not have started.
        String expectedText = testI18nBean.getText("common.tasks.info.starting", bean.getFormattedSubmittedTimestamp());
        assertEquals(expectedText, bean.getFormattedProgress());

        //it should now be started.
        final Date startedDate = new Date();
        descriptor.setStartedTime(startedDate);
        expectedText = testI18nBean.getText("common.tasks.info.progress.unknown", bean.getFormattedElapsedRunTime());
        assertEquals(expectedText, bean.getFormattedProgress());

        //should finish without error.
        final Date finishedDate = new Date();
        descriptor.setFinishedTime(finishedDate);
        descriptor.setElapsedRunTime(1000);
        expectedText = testI18nBean.getText("common.tasks.info.completed", bean.getFormattedElapsedRunTime());

        assertEquals(expectedText, bean.getFormattedProgress());

        //should finish with error.
        bean.setExceptionCause(new Exception());
        expectedText = testI18nBean.getText("common.tasks.info.completed.with.error", bean.getFormattedElapsedRunTime());

        assertEquals(expectedText, bean.getFormattedProgress());

        final TaskProgressEvent lastEvent = new TaskProgressEvent(7L, Long.MAX_VALUE, 43, "SubTask", "ThisIsAMessage");

        when(taskProgressIndicator.getLastProgressEvent()).thenReturn(lastEvent);

        //lets check that progress is seen.
        descriptor.setTaskProgressIndicator(taskProgressIndicator);
        descriptor.setFinishedTime(null);

        bean = new TaskDescriptorBean<Long>(descriptor, testI18nBean, dateTimeFormatter, testUserName);

        expectedText = testI18nBean.getText("common.tasks.info.progressing", String.valueOf(43), bean.getFormattedElapsedRunTime());
        assertEquals(expectedText, bean.getFormattedProgress());
    }

    @Test
    public void testGetProgressWithBadProgress() throws Exception
    {
        final TaskProgressEvent lastEvent = new TaskProgressEvent(7L, Long.MAX_VALUE, Long.MAX_VALUE, "SubTask", "ThisIsAMessage");
        when(taskProgressIndicator.getLastProgressEvent()).thenReturn(lastEvent);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);

        assertSame(lastEvent, bean.getLastProgressEvent());
        assertEquals(100, bean.getProgressNumber());
        assertEquals(0, bean.getInverseProgressNumber());
    }

    @Test
    public void testGetProgressWithGoodProgress() throws Exception
    {
        final TaskProgressEvent lastEvent = new TaskProgressEvent(7l, Long.MAX_VALUE, 56, "SubTask", "ThisIsAMessage");
        when(taskProgressIndicator.getLastProgressEvent()).thenReturn(lastEvent);

        when(taskDescriptor.isFinished()).thenReturn(false);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);

        assertSame(lastEvent, bean.getLastProgressEvent());
        assertEquals(56, bean.getProgressNumber());
        assertEquals(44, bean.getInverseProgressNumber());
    }

    @Test
    public void testGetProgressWhenNoIndicator() throws Exception
    {
        when(taskDescriptor.getTaskProgressIndicator()).thenReturn(null);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(100, bean.getProgressNumber());
        assertEquals(0, bean.getInverseProgressNumber());
    }

    @Test
    public void testGetProgressWhenFinished() throws Exception
    {
        when(taskDescriptor.isFinished()).thenReturn(true);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertEquals(100, bean.getProgressNumber());
        assertEquals(0, bean.getInverseProgressNumber());
    }

    @Test
    public void testException() throws Exception
    {
        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        final Exception exception1 = new Exception("Exception1");
        final Exception exception2 = new Exception("Exception2");

        bean.setExceptionCause(exception1);
        assertSame(exception1, bean.getExceptionCause());
        assertNotNull(bean.getFormattedExceptionCause());

        bean.setExceptionCause(exception2);
        assertSame(exception2, bean.getExceptionCause());
        assertNotNull(bean.getFormattedExceptionCause());
    }

    @Test
    public void testIsUserWhoStartedTask() throws Exception
    {
        final User otherUser = TestTaskDescriptorBean.createUser("OtherUser");

        when(taskDescriptor.getUserName()).thenReturn(testUserName).thenReturn(otherUser.getName());
        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);

        assertTrue(bean.isUserWhoStartedTask());
        assertFalse(bean.isUserWhoStartedTask());
    }

    @Test
    public void testGetProgressUrl() throws Exception
    {
        final String url1 = "/root/dir/progress.action";
        final String url2 = "/IndexAdmin.action";

        when(taskDescriptor.getProgressURL()).thenReturn(url1).thenReturn(url2);

        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        String progressUrl = bean.getProgressURL();
        assertNotNull(progressUrl);
        assertTrue(progressUrl.contains(url1));

        progressUrl = bean.getProgressURL();
        assertNotNull(progressUrl);
        assertTrue(progressUrl.contains(url2));
    }

    @Test
    public void testGetUserUrl() throws Exception
    {
        when(taskDescriptor.getUserName()).thenReturn(testUserName);

        final TaskDescriptorBean bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        String usrUrl = bean.getUserURL();
        assertNotNull(usrUrl);
        assertTrue("URL '" + usrUrl + "' does not start with a /", usrUrl.startsWith("/"));

        usrUrl = bean.getUserURL();
        assertNotNull(usrUrl);
        assertTrue("URL '" + usrUrl + "' does not start with a /", usrUrl.startsWith("/"));
    }

    @Test
    public void testGetLastProgressEvent() throws Exception
    {
        final TaskProgressEvent lastEvent = new TaskProgressEvent(7L, Long.MAX_VALUE, 56, "SubTask", "ThisIsAMessage");
        when(taskProgressIndicator.getLastProgressEvent()).thenReturn(lastEvent);
        final TaskDescriptorBean<Long> bean = new TaskDescriptorBean<Long>(taskDescriptor, testI18nBean, dateTimeFormatter, testUserName);
        assertSame(lastEvent, bean.getLastProgressEvent());
    }

    private static User createUser(final String name)
    {
        return new MockUser(name);
    }

    private static class TestContext implements TaskContext
    {
        public String buildProgressURL(final Long taskId)
        {
            return null;
        }
    }
}
