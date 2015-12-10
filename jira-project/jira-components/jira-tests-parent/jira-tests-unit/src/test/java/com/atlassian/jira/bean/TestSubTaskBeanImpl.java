package com.atlassian.jira.bean;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.web.bean.PercentageGraphModel;
import com.atlassian.jira.web.bean.PercentageGraphRow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("deprecation")
public class TestSubTaskBeanImpl
{
    private SubTaskBeanImpl bean;
    private MockGenericValue issue;
    private MockGenericValue subTaskOpen1;
    private MockGenericValue subTaskOpen2;
    private MockGenericValue subTaskClosed1;
    private MockGenericValue subTaskClosed2;
    private MockGenericValue subTaskClosed3;
    @Mock private IssueFactory mockIssueFactory;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        new MockComponentWorker().init().addMock(IssueFactory.class, mockIssueFactory);

        bean  = new SubTaskBeanImpl();

        issue = new MockGenericValue("Issue", EasyMap.build("summary", "test issue"));
        subTaskOpen1 = new MockGenericValue("Issue", EasyMap.build("summary", "test sub task issue 1"));
        subTaskOpen2 = new MockGenericValue("Issue", EasyMap.build("summary", "test sub task issue 2"));
        subTaskClosed1 = new MockGenericValue("Issue", EasyMap.build("summary", "test sub task issue 3", "resolution", "something"));
        subTaskClosed2 = new MockGenericValue("Issue", EasyMap.build("summary", "test sub task issue 3", "resolution", "something else"));
        subTaskClosed3 = new MockGenericValue("Issue", EasyMap.build("summary", "test sub task issue 3", "resolution", "something totally different"));

        long i = 0;
        bean.addSubTask(i++, subTaskOpen1, issue);
        bean.addSubTask(i++, subTaskClosed1, issue);
        bean.addSubTask(i++, subTaskOpen2, issue);
        bean.addSubTask(i++, subTaskClosed2, issue);
        bean.addSubTask(i, subTaskClosed3, issue);
    }

    @After
    public void tearDown() throws Exception
    {
        ComponentAccessor.initialiseWorker(null);
    }

    @Test
    public void testAllView()
    {
        final String view = SubTaskBean.SUB_TASK_VIEW_ALL;
        final Collection subTasks = bean.getSubTasks(view);
        assertEquals(5, subTasks.size());
        final Iterator iterator = subTasks.iterator();
        assertEquals(new SubTaskImpl(0L, subTaskOpen1, issue), iterator.next());
        assertEquals(new SubTaskImpl(1L, subTaskClosed1, issue), iterator.next());
        assertEquals(new SubTaskImpl(2L, subTaskOpen2, issue), iterator.next());
        assertEquals(new SubTaskImpl(3L, subTaskClosed2, issue), iterator.next());
        assertEquals(new SubTaskImpl(4L, subTaskClosed3, issue), iterator.next());

        assertEquals(Long.valueOf(1), bean.getNextSequence(0L, view));
        assertEquals(Long.valueOf(0), bean.getPreviousSequence(1L, view));
    }

    @Test
    public void testOpenView()
    {
        final String view = SubTaskBean.SUB_TASK_VIEW_UNRESOLVED;
        final Collection subTasks = bean.getSubTasks(view);
        assertEquals(2, subTasks.size());
        final Iterator iterator = subTasks.iterator();
        assertEquals(new SubTaskImpl(0L, subTaskOpen1, issue), iterator.next());
        assertEquals(new SubTaskImpl(2L, subTaskOpen2, issue), iterator.next());

        assertEquals(Long.valueOf(2), bean.getNextSequence(0L, view));
        assertEquals(Long.valueOf(0), bean.getPreviousSequence(2L, view));
    }

    @Test
    public void testGetSubTaskProgress()
    {
        final PercentageGraphModel subTaskProgress = bean.getSubTaskProgress();
        final List rows = subTaskProgress.getRows();
        assertEquals(2, rows.size());
        final Iterator iterator = rows.iterator();
        PercentageGraphRow percentageGraphRow = (PercentageGraphRow) iterator.next();
        assertEquals(new PercentageGraphRow("#51A825", 3L, "Resolved Sub-Tasks", null), percentageGraphRow);
        assertEquals(60, subTaskProgress.getPercentage(percentageGraphRow));
        percentageGraphRow = (PercentageGraphRow) iterator.next();
        assertEquals(new PercentageGraphRow("#cccccc", 2L, "Unresolved Sub-Tasks", null), percentageGraphRow);
        assertEquals(40, subTaskProgress.getPercentage(percentageGraphRow));
    }
}
