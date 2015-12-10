package com.atlassian.jira.issue.worklog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class TestDefaultTimeTrackingIssueUpdater
{
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Before
    public void setUp() throws Exception
    {
        if (jiraAuthenticationContext == null)
        {
            jiraAuthenticationContext = new MockSimpleAuthenticationContext(null);
        }
    }

    @Test
    public void testConstructChangeItemBeanForWorklogDeleteNoSecurity()
    {
        // Create our mock worklog that will specify the issue we want to update and the time spent
        final Mock mockWorklog = new Mock(Worklog.class);
        final Long worklogId = new Long(12345);
        mockWorklog.expectAndReturn("getId", worklogId);
        final Long timeSpent = new Long(10);
        mockWorklog.expectAndReturn("getTimeSpent", timeSpent);
        final String groupLevel = null;
        mockWorklog.expectAndReturn("getGroupLevel", groupLevel);
        mockWorklog.expectAndReturn("getRoleLevel", null);
        final String formattedDuration = "formatted string";

        final DefaultTimeTrackingIssueUpdater timeTrackingIssueUpdater = new DefaultTimeTrackingIssueUpdater(null, null, jiraAuthenticationContext,
            null, null, null, null)
        {
            @Override
            String getFormattedDuration(final Long duration)
            {
                return formattedDuration;
            }
        };

        final List changeItemBeans = timeTrackingIssueUpdater.constructChangeItemBeansForWorklogDelete((Worklog) mockWorklog.proxy());
        assertEquals(2, changeItemBeans.size());
        final ChangeItemBean idChangeItemBean = (ChangeItemBean) changeItemBeans.get(0);
        assertEquals(worklogId.toString(), idChangeItemBean.getFrom());
        assertEquals(worklogId.toString(), idChangeItemBean.getFromString());
        assertEquals(IssueFieldConstants.WORKLOG_ID, idChangeItemBean.getField());
        assertEquals(ChangeItemBean.STATIC_FIELD, idChangeItemBean.getFieldType());
        final ChangeItemBean prettyDurationChangeItemBean = (ChangeItemBean) changeItemBeans.get(1);
        assertEquals(timeSpent.toString(), prettyDurationChangeItemBean.getFrom());
        assertEquals(formattedDuration, prettyDurationChangeItemBean.getFromString());
        assertEquals(IssueFieldConstants.WORKLOG_TIME_SPENT, prettyDurationChangeItemBean.getField());
        assertEquals(ChangeItemBean.STATIC_FIELD, prettyDurationChangeItemBean.getFieldType());

        mockWorklog.verify();
    }

    @Test
    public void testConstructChangeItemBeanForWorklogDeleteGroupSecurityLevel()
    {
        // Create our mock worklog that will specify the issue we want to update and the time spent
        final Mock mockWorklog = new Mock(Worklog.class);
        final Long worklogId = new Long(12345);
        mockWorklog.expectAndReturn("getId", worklogId);
        final String groupLevel = "MAXIMUM SECURITY";
        mockWorklog.expectAndReturn("getGroupLevel", groupLevel);
        mockWorklog.expectAndReturn("getRoleLevel", null);

        final AtomicBoolean getFormattedDurationCalled = new AtomicBoolean(false);
        final DefaultTimeTrackingIssueUpdater timeTrackingIssueUpdater = new DefaultTimeTrackingIssueUpdater(null, null, jiraAuthenticationContext,
            null, null, null, null)
        {
            @Override
            String getFormattedDuration(final Long duration)
            {
                getFormattedDurationCalled.set(true);
                return "";
            }
        };

        final List changeItemBeans = timeTrackingIssueUpdater.constructChangeItemBeansForWorklogDelete((Worklog) mockWorklog.proxy());

        assertFalse(getFormattedDurationCalled.get());

        assertEquals(2, changeItemBeans.size());
        final ChangeItemBean idChangeItemBean = (ChangeItemBean) changeItemBeans.get(0);
        assertEquals(worklogId.toString(), idChangeItemBean.getFrom());
        assertEquals(worklogId.toString(), idChangeItemBean.getFromString());
        assertEquals(IssueFieldConstants.WORKLOG_ID, idChangeItemBean.getField());
        assertEquals(ChangeItemBean.STATIC_FIELD, idChangeItemBean.getFieldType());

        final ChangeItemBean prettyDurationChangeItemBean = (ChangeItemBean) changeItemBeans.get(1);
        assertEquals("A worklog with security level 'MAXIMUM SECURITY' was removed.", prettyDurationChangeItemBean.getFromString());
        assertNull(prettyDurationChangeItemBean.getFrom());
        assertEquals(IssueFieldConstants.WORKLOG_TIME_SPENT, prettyDurationChangeItemBean.getField());
        assertEquals(ChangeItemBean.STATIC_FIELD, prettyDurationChangeItemBean.getFieldType());

        mockWorklog.verify();
    }

    @Test
    public void testUpdateIssueOnWorklogDeleteNoSecurityLevelSet()
    {
        final Long issueId = new Long(1);
        final MockGenericValue mockGenericValue = new MockGenericValue("Issue", EasyMap.build("id", new Long(1)));

        // Create an IssueManager that will return our mock GV given our mock's issue id
        final Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.expectAndReturn("getIssue", P.args(new IsEqual(issueId)), mockGenericValue);

        // Create a mock issue that will be returned by the worklog and will give our mock GV
        final Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getGenericValue", mockGenericValue);
        final Long totalTimeSpent = new Long(1000);
        mockIssue.expectAndReturn("getTimeSpent", totalTimeSpent);

        // Create our mock worklog that will specify the issue we want to update and the time spent
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.setStrict(true);
        final Long worklogId = new Long(12345);
        mockWorklog.expectAndReturn("getId", worklogId);
        mockWorklog.expectAndReturn("getIssue", mockIssue.proxy());
        final Long timeSpent = new Long(10);
        mockWorklog.expectAndReturn("getTimeSpent", timeSpent);
        final String groupLevel = null;
        mockWorklog.expectAndReturn("getGroupLevel", groupLevel);
        mockWorklog.expectAndReturn("getRoleLevel", null);
        mockWorklog.expectAndReturn("getRoleLevelId", null);
        final String FORMATTED_DURATION = "a nicely formatted duration";

        final boolean dispatchEvent = true;
        final DefaultTimeTrackingIssueUpdater timeTrackingIssueUpdater = new DefaultTimeTrackingIssueUpdater(null,
            (IssueManager) mockIssueManager.proxy(), jiraAuthenticationContext, null, null, null, null)
        {

            @Override
            void doUpdate(final IssueUpdateBean iub)
            {
                // NOTE: The group level has been renamed in the worklog table from 'level' to 'grouplevel', we pass the
                // event a parameter called level so that the CurrentReporter notification instance can check the group level
                // (this may not be needed).
                assertEquals(groupLevel, iub.getParams().get("level"));
                assertEquals(null, iub.getParams().get("rolelevel"));
                assertEquals(IssueEventSource.ACTION, iub.getParams().get("eventsource"));
                assertEquals(dispatchEvent, iub.isDispatchEvent());
                assertEquals(EventType.ISSUE_WORKLOG_DELETED_ID, iub.getEventTypeId());
                assertEquals(2, iub.getChangeItems().size());
                final Iterator beanIt = iub.getChangeItems().iterator();
                final ChangeItemBean idBean = (ChangeItemBean) beanIt.next();
                assertEquals("12345", idBean.getFrom());
                assertEquals("12345", idBean.getFromString());
                final ChangeItemBean prettyDurationBean = (ChangeItemBean) beanIt.next();
                assertEquals(FORMATTED_DURATION, prettyDurationBean.getFromString());
                assertEquals(timeSpent.toString(), prettyDurationBean.getFrom());
            }

            @Override
            String getFormattedDuration(final Long duration)
            {
                return FORMATTED_DURATION;
            }
        };

        final Long newEstimate = new Long(12345);
        timeTrackingIssueUpdater.updateIssueOnWorklogDelete(null, (Worklog) mockWorklog.proxy(), newEstimate, dispatchEvent);

        // Assert that the "fake" issueGV has had the correct values set on it for update
        assertEquals(new Long(990), mockGenericValue.getLong("timespent"));
        assertEquals(newEstimate, mockGenericValue.getLong("timeestimate"));
        mockIssueManager.verify();
        mockWorklog.verify();
        mockIssue.verify();
    }

    @Test
    public void testUpdateIssueOnWorklogDeleteSecurityLevelSet()
    {
        final Long issueId = new Long(1);
        final MockGenericValue mockGenericValue = new MockGenericValue("Issue", EasyMap.build("id", new Long(1)));

        // Create an IssueManager that will return our mock GV given our mock's issue id
        final Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.expectAndReturn("getIssue", P.args(new IsEqual(issueId)), mockGenericValue);

        // Create a mock issue that will be returned by the worklog and will give our mock GV
        final Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getGenericValue", mockGenericValue);
        final Long totalTimeSpent = new Long(1000);
        mockIssue.expectAndReturn("getTimeSpent", totalTimeSpent);

        // Create our mock worklog that will specify the issue we want to update and the time spent
        final Mock mockWorklog = new Mock(Worklog.class);
        final Long worklogId = new Long(12345);
        mockWorklog.expectAndReturn("getId", worklogId);
        mockWorklog.expectAndReturn("getIssue", mockIssue.proxy());
        final Long timeSpent = new Long(10);
        mockWorklog.expectAndReturn("getTimeSpent", timeSpent);
        final String groupLevel = "test group level";
        mockWorklog.expectAndReturn("getGroupLevel", groupLevel);
        final Long roleLevelId = new Long(2112);
        mockWorklog.expectAndReturn("getRoleLevelId", roleLevelId);

        final boolean dispatchEvent = true;
        final DefaultTimeTrackingIssueUpdater timeTrackingIssueUpdater = new DefaultTimeTrackingIssueUpdater(null,
            (IssueManager) mockIssueManager.proxy(), jiraAuthenticationContext, null, null, null, null)
        {

            @Override
            void doUpdate(final IssueUpdateBean iub)
            {
                // NOTE: The group level has been renamed in the worklog table from 'level' to 'grouplevel', we pass the
                // event a parameter called level so that the CurrentReporter notification instance can check the group level
                // (this may not be needed).
                assertEquals(groupLevel, iub.getParams().get("level"));
                assertEquals(roleLevelId, iub.getParams().get("rolelevel"));
                assertEquals(IssueEventSource.ACTION, iub.getParams().get("eventsource"));
                assertEquals(dispatchEvent, iub.isDispatchEvent());
                assertEquals(EventType.ISSUE_WORKLOG_DELETED_ID, iub.getEventTypeId());
                assertEquals(2, iub.getChangeItems().size());
                final Iterator beanIt = iub.getChangeItems().iterator();
                final ChangeItemBean idBean = (ChangeItemBean) beanIt.next();
                assertEquals("12345", idBean.getFrom());
                assertEquals("12345", idBean.getFromString());
                final ChangeItemBean prettyDurationBean = (ChangeItemBean) beanIt.next();
                assertEquals("A worklog with security level 'test group level' was removed.", prettyDurationBean.getFromString());
                assertNull(prettyDurationBean.getFrom());
            }
        };

        final Long newEstimate = new Long(12345);
        timeTrackingIssueUpdater.updateIssueOnWorklogDelete(null, (Worklog) mockWorklog.proxy(), newEstimate, dispatchEvent);

        // Assert that the "fake" issueGV has had the correct values set on it for update
        assertEquals(new Long(990), mockGenericValue.getLong("timespent"));
        assertEquals(newEstimate, mockGenericValue.getLong("timeestimate"));
        mockIssueManager.verify();
        mockWorklog.verify();
        mockIssue.verify();
    }

    @Test
    public void testUpdateIssueOnWorklogUpdate()
    {
        final Long issueId = new Long(1);
        final MockGenericValue mockGenericValue = new MockGenericValue("Issue", EasyMap.build("id", new Long(1)));

        // Create an IssueManager that will return our mock GV given our mock's issue id
        final Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.expectAndReturn("getIssue", P.args(new IsEqual(issueId)), mockGenericValue);

        // Create a mock issue that will be returned by the worklog and will give our mock GV
        final Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getGenericValue", mockGenericValue);
        final Long totalTimeSpent = new Long(1000);
        mockIssue.expectAndReturn("getTimeSpent", totalTimeSpent);

        // Create our mock worklog that will specify the issue we want to update and the time spent
        final Mock mockNewWorklog = new Mock(Worklog.class);
        mockNewWorklog.expectAndReturn("getIssue", mockIssue.proxy());
        mockNewWorklog.expectAndReturn("getId", 123L);
        
        final Long timeSpent = new Long(10);
        mockNewWorklog.expectAndReturn("getTimeSpent", timeSpent);
        final String groupLevel = "test group level";
        mockNewWorklog.expectAndReturn("getGroupLevel", groupLevel);
        final Long roleLevelId = new Long(2112);
        mockNewWorklog.expectAndReturn("getRoleLevelId", roleLevelId);

        final Mock mockOriginalWorklog = new Mock(Worklog.class);

        final boolean dispatchEvent = true;
        final DefaultTimeTrackingIssueUpdater timeTrackingIssueUpdater = new DefaultTimeTrackingIssueUpdater(null,
            (IssueManager) mockIssueManager.proxy(), jiraAuthenticationContext, null, null, null, null)
        {
            @Override
            void doUpdate(final IssueUpdateBean iub)
            {
                // NOTE: The group level has been renamed in the worklog table from 'level' to 'grouplevel', we pass the
                // event a parameter called level so that the CurrentReporter notification instance can check the group level
                // (this may not be needed).
                assertEquals(groupLevel, iub.getParams().get("level"));
                assertEquals(roleLevelId, iub.getParams().get("rolelevel"));
                assertEquals(IssueEventSource.ACTION, iub.getParams().get("eventsource"));
                assertEquals(mockOriginalWorklog.proxy(), iub.getParams().get(TimeTrackingIssueUpdater.EVENT_ORIGINAL_WORKLOG_PARAMETER));
                assertEquals(dispatchEvent, iub.isDispatchEvent());
                assertNotNull(iub.getChangeItems());
                assertEquals(EventType.ISSUE_WORKLOG_UPDATED_ID, iub.getEventTypeId());
            }
        };

        final Long newEstimate = new Long(12345);
        timeTrackingIssueUpdater.updateIssueOnWorklogUpdate(null, (Worklog) mockOriginalWorklog.proxy(), (Worklog) mockNewWorklog.proxy(), new Long(
            100), newEstimate, dispatchEvent);

        // Assert that the "fake" issueGV has had the correct values set on it for update
        assertEquals(new Long(910), mockGenericValue.getLong("timespent"));
        assertEquals(newEstimate, mockGenericValue.getLong("timeestimate"));
        mockIssueManager.verify();
        mockNewWorklog.verify();
        mockIssue.verify();
    }

    @Test
    public void testUpdateIssueOnWorklogCreate()
    {
        final Long issueId = new Long(1);
        final MockGenericValue mockGenericValue = new MockGenericValue("Issue", EasyMap.build("id", new Long(1)));

        // Create an IssueManager that will return our mock GV given our mock's issue id
        final Mock mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.expectAndReturn("getIssue", P.args(new IsEqual(issueId)), mockGenericValue);

        // Create a mock issue that will be returned by the worklog and will give our mock GV
        final Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getGenericValue", mockGenericValue);
        final Long totalTimeSpent = new Long(1000);
        mockIssue.expectAndReturn("getTimeSpent", totalTimeSpent);

        // Create our mock worklog that will specify the issue we want to update and the time spent
        final Mock mockWorklog = new Mock(Worklog.class);
        mockWorklog.expectAndReturn("getIssue", mockIssue.proxy());
        mockWorklog.expectAndReturn("getId", 123L);

        final Long timeSpent = new Long(10);
        mockWorklog.expectAndReturn("getTimeSpent", timeSpent);
        final String groupLevel = "test group level";
        mockWorklog.expectAndReturn("getGroupLevel", groupLevel);
        final Long roleLevelId = new Long(2112);
        mockWorklog.expectAndReturn("getRoleLevelId", roleLevelId);

        final boolean dispatchEvent = true;

        final DefaultTimeTrackingIssueUpdater timeTrackingIssueUpdater = new DefaultTimeTrackingIssueUpdater(null,
            (IssueManager) mockIssueManager.proxy(), jiraAuthenticationContext, null, null, null, null)
        {
            // Test that the state of the IssueUpdateBean has been set correctly
            @Override
            public void doUpdate(final IssueUpdateBean iub)
            {
                // NOTE: The group level has been renamed in the worklog table from 'level' to 'grouplevel', we pass the
                // event a parameter called level so that the CurrentReporter notification instance can check the group level
                // (this may not be needed).
                assertEquals(groupLevel, iub.getParams().get("level"));
                assertEquals(roleLevelId, iub.getParams().get("rolelevel"));
                assertEquals(IssueEventSource.ACTION, iub.getParams().get("eventsource"));
                assertEquals(dispatchEvent, iub.isDispatchEvent());
                assertNotNull(iub.getChangeItems());
                assertEquals(EventType.ISSUE_WORKLOGGED_ID, iub.getEventTypeId());
            }

        };

        final Long newEstimate = new Long(12345);
        timeTrackingIssueUpdater.updateIssueOnWorklogCreate(null, (Worklog) mockWorklog.proxy(), newEstimate, dispatchEvent);

        // Assert that the "fake" issueGV has had the correct values set on it for update
        assertEquals(new Long(1010), mockGenericValue.getLong("timespent"));
        assertEquals(newEstimate, mockGenericValue.getLong("timeestimate"));
        mockIssueManager.verify();
        mockWorklog.verify();
        mockIssue.verify();
    }
    
    @Test
    public void dispatchEvent()
    {
        ApplicationUser user = mock(ApplicationUser.class);
        Worklog worklog = mock(Worklog.class);
        Map<String, Object> params = new HashMap<String, Object>();
        Comment comment = mock(Comment.class);
        IssueUpdateBean issueUpdateBean = createIssueUpdateBean(user, true, comment, worklog, params);
        GenericValue changeGroup = new MockGenericValue("ChangeGroup");

        IssueFactory issueFactory = mock(IssueFactory.class);
        MutableIssue issue = mock(MutableIssue.class);
        GenericValue changedIssue = new MockGenericValue("Issue");
        when(issueFactory.getIssue(changedIssue)).thenReturn(issue);

        IssueEventBundleFactory issueEventBundleFactory = mock(IssueEventBundleFactory.class);
        IssueEventBundle issueEventBundle = mock(IssueEventBundle.class);
        when(issueEventBundleFactory.createWorklogEventBundle(issue, changeGroup, issueUpdateBean, user)).thenReturn(issueEventBundle);

        IssueEventManager issueEventManager = mock(IssueEventManager.class);
        DefaultTimeTrackingIssueUpdater updater = createTimeTracker(issueFactory, issueEventManager, issueEventBundleFactory);

        updater.dispatchEvent(issueUpdateBean, changedIssue, changeGroup);

        verify(issueEventManager).dispatchRedundantEvent(issueUpdateBean.getEventTypeId(), issue, ApplicationUsers.toDirectoryUser(user), comment, worklog, changeGroup, params, true);
        verify(issueEventManager).dispatchEvent(issueEventBundle);
    }
    
    private DefaultTimeTrackingIssueUpdater createTimeTracker(IssueFactory issueFactory, IssueEventManager issueEventManager, IssueEventBundleFactory issueEventBundleFactory)
    {
        return new DefaultTimeTrackingIssueUpdater(null, null, null, null, issueFactory, issueEventManager, issueEventBundleFactory);
    }

    private IssueUpdateBean createIssueUpdateBean(ApplicationUser user, boolean sendMail, Comment comment, Worklog worklog, Map<String, Object> params)
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean((GenericValue) null, null, EventType.ISSUE_WORKLOGGED_ID, user, sendMail, false);
        issueUpdateBean.setComment(comment);
        issueUpdateBean.setWorklog(worklog);
        issueUpdateBean.setParams(params);
        return issueUpdateBean;
    }
}
