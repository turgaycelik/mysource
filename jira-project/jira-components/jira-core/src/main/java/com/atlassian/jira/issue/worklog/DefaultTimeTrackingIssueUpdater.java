package com.atlassian.jira.issue.worklog;

import java.util.List;
import java.util.Map;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.JiraDurationUtils;

import com.google.common.annotations.VisibleForTesting;

import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

/**
 *
 */
public class DefaultTimeTrackingIssueUpdater implements TimeTrackingIssueUpdater
{
    private final IssueManager issueManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final JiraDurationUtils jiraDurationUtils;
    private final IssueFactory issueFactory;
    private final IssueEventManager issueEventManager;
    private final IssueEventBundleFactory issueEventBundleFactory;
    private final OfBizDelegator ofBizDelegator;

    public DefaultTimeTrackingIssueUpdater(
            OfBizDelegator ofBizDelegator,
            IssueManager issueManager,
            JiraAuthenticationContext jiraAuthenticationContext,
            JiraDurationUtils jiraDurationUtils,
            IssueFactory issueFactory,
            IssueEventManager issueEventManager,
            IssueEventBundleFactory issueEventBundleFactory)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.issueManager = issueManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.jiraDurationUtils = jiraDurationUtils;
        this.issueFactory = issueFactory;
        this.issueEventManager = issueEventManager;
        this.issueEventBundleFactory = issueEventBundleFactory;
    }

    @Override
    public void updateIssueOnWorklogCreate(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent)
    {
        validateWorklogAndIssue(worklog);

        final Issue issue = worklog.getIssue();
        GenericValue issueGV = issue.getGenericValue();

        // if new estimate is null, leave it - otherwise set it now
        setNewEstimate(newEstimate, issueGV);

        // now increase the amount of total time spent on the issue
        Long timeSpent = worklog.getTimeSpent();
        Long totalTimeSpent = issue.getTimeSpent();
        if (totalTimeSpent == null)
        {
            totalTimeSpent = timeSpent;
        }
        else
        {
            totalTimeSpent = totalTimeSpent + timeSpent;
        }
        issueGV.set("timespent", totalTimeSpent);

        updateIssue(issueGV, user, null, worklog, EventType.ISSUE_WORKLOGGED_ID, constructChangeItemBeansForWorklogUpdateOrCreate(worklog), dispatchEvent);
    }

    @Override
    public void updateIssueOnWorklogUpdate(User user, Worklog originalWorklog, Worklog newWorklog, Long originalTimeSpent, Long newEstimate, boolean dispatchEvent)
    {
        validateWorklogAndIssue(newWorklog);
        // if new estimate is null, leave it - otherwise set it now
        final Issue issue = newWorklog.getIssue();
        GenericValue issueGV = issue.getGenericValue();

        setNewEstimate(newEstimate, issueGV);

        long newTotalTimeSpent;
        // Calculate new totalTimeSpent for edit
        // now increase the amount of total time spent on the issue
        Long newTimeSpent = newWorklog.getTimeSpent();
        Long totalTimeSpent = issue.getTimeSpent();
        if (totalTimeSpent == null)
        {
            newTotalTimeSpent = newTimeSpent;
        }
        else
        {
            // We calculate the total time spent by subtracting the old amount of time spent from the total and then
            // adjusting it by the newly specified time spent for this worklog.
            newTotalTimeSpent = (totalTimeSpent - originalTimeSpent) + newTimeSpent;
            if (newTotalTimeSpent < 0)
            {
                newTotalTimeSpent = 0;
            }
        }
        issueGV.set("timespent", newTotalTimeSpent);

        updateIssue(issueGV, user, originalWorklog, newWorklog, EventType.ISSUE_WORKLOG_UPDATED_ID, constructChangeItemBeansForWorklogUpdateOrCreate(newWorklog), dispatchEvent);
    }

    @Override
    public void updateIssueOnWorklogDelete(User user, Worklog worklog, Long newEstimate, boolean dispatchEvent)
    {
        validateWorklogAndIssue(worklog);

        // if new estimate is null, leave it - otherwise set it now
        final Issue issue = worklog.getIssue();
        GenericValue issueGV = issue.getGenericValue();

        setNewEstimate(newEstimate, issueGV);

        long newTotalTimeSpent;
        // Calculate new totalTimeSpent for edit
        // now increase the amount of total time spent on the issue
        Long timeSpent = worklog.getTimeSpent();
        Long totalTimeSpent = issue.getTimeSpent();
        if (totalTimeSpent == null)
        {
            newTotalTimeSpent = 0;
        }
        else
        {
            // We calculate the total time spent by subtracting the amount of time spent on the deleted worklog.
            newTotalTimeSpent = totalTimeSpent - timeSpent;
            if (newTotalTimeSpent < 0)
            {
                newTotalTimeSpent = 0;
            }
        }
        issueGV.set("timespent", newTotalTimeSpent);

        updateIssue(issueGV, user, null, worklog, EventType.ISSUE_WORKLOG_DELETED_ID, constructChangeItemBeansForWorklogDelete(worklog), dispatchEvent);
    }

    List<ChangeItemBean> constructChangeItemBeansForWorklogDelete(Worklog worklog)
    {
        // Check the level of the worklog, if the level is not null we need to override the comment
        // This is necessary as part of JRA-9394 to remove comment text from the change history for security (or lack thereof)
        String message;
        String timeSpentString = null;
        final String groupLevel = worklog.getGroupLevel();
        final String roleLevel = (worklog.getRoleLevel() == null) ? null : worklog.getRoleLevel().getName() ;
        final String actionLevel = groupLevel == null ? roleLevel : groupLevel;
        if (actionLevel != null)
        {
            message = jiraAuthenticationContext.getI18nHelper().getText("time.tracking.issue.updater.deleted.worklog.with.restricted.level", actionLevel);
        }
        else
        {
            message = getFormattedDuration(worklog.getTimeSpent());
            timeSpentString = worklog.getTimeSpent().toString();
        }

        ChangeItemBean worklogIdBean = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.WORKLOG_ID, worklog.getId().toString(), worklog.getId().toString(), null, null);
        ChangeItemBean worklogDurationBean = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.WORKLOG_TIME_SPENT, timeSpentString, message, null, null);

        return EasyList.build(worklogIdBean, worklogDurationBean);
    }

    List<ChangeItemBean> constructChangeItemBeansForWorklogUpdateOrCreate(Worklog worklog)
    {
        ChangeItemBean worklogIdBean = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.WORKLOG_ID, worklog.getId().toString(), worklog.getId().toString(), null, null);
        return EasyList.build(worklogIdBean);
    }

    String getFormattedDuration(Long duration)
    {
        return jiraDurationUtils.getFormattedDuration(duration);
    }

    void updateIssue(GenericValue issueGV, User user, Worklog originalWorklog, Worklog worklog, Long eventId, List<ChangeItemBean> changeItems, boolean dispatchEvent)
    {
        // Now update the issue with the new values
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issueGV, issueManager.getIssue(issueGV.getLong("id")), eventId, user);
        issueUpdateBean.setWorklog(worklog);
        // NOTE: The group level has been renamed in the worklog table from 'level' to 'grouplevel', we pass the
        // event a parameter called level so that the CurrentReporter notification instance can check the group level
        // (this may not be needed).
        final Map eventParams = EasyMap.build(
                "level", worklog.getGroupLevel(),
                "rolelevel", worklog.getRoleLevelId(),
                "eventsource", IssueEventSource.ACTION);
        if(originalWorklog != null)
        {
            eventParams.put(EVENT_ORIGINAL_WORKLOG_PARAMETER, originalWorklog);
        }
        issueUpdateBean.setParams(eventParams);
        issueUpdateBean.setDispatchEvent(dispatchEvent);

        // Set any passed in change items
        if(changeItems != null)
        {
            issueUpdateBean.setChangeItems(changeItems);
        }

        doUpdate(issueUpdateBean);
    }

    /**
     * Stores any changes to the issue optionally including a changelog and
     * conditionally dispatches an IssueUpdate event.
     * @param iub the description of the change.
     */
    void doUpdate(IssueUpdateBean iub)
    {
        final GenericValue changedIssue = iub.getChangedIssue();
        changedIssue.set("updated", UtilDateTime.nowTimestamp());

        ofBizDelegator.storeAll(EasyList.build(changedIssue));

        GenericValue changeGroup = ChangeLogUtils.createChangeGroup(iub.getApplicationUser(), iub.getOriginalIssue(), changedIssue, iub.getChangeItems(), true);

        //always fire events if something has changed
        if (iub.isDispatchEvent())
        {
            dispatchEvent(iub, changedIssue, changeGroup);
        }
    }

    @VisibleForTesting
    void dispatchEvent(IssueUpdateBean iub, GenericValue changedIssue, GenericValue changeGroup)
    {
        issueEventManager.dispatchRedundantEvent(
                iub.getEventTypeId(),
                issueFactory.getIssue(changedIssue),
                ApplicationUsers.toDirectoryUser(iub.getApplicationUser()),
                iub.getComment(),
                iub.getWorklog(),
                changeGroup,
                iub.getParams(),
                iub.isSendMail()
        );

        IssueEventBundle issueEventBundle = issueEventBundleFactory.createWorklogEventBundle(issueFactory.getIssue(changedIssue), changeGroup, iub, iub.getApplicationUser());
        issueEventManager.dispatchEvent(issueEventBundle);
    }

    void setNewEstimate(Long newEstimate, GenericValue issueGV)
    {
        if (newEstimate != null)
        {
            issueGV.set("timeestimate", newEstimate);
        }
    }

    void validateWorklogAndIssue(Worklog worklog)
    {
        if(worklog == null)
        {
            throw new IllegalArgumentException("Worklog must not be null.");
        }

        if(worklog.getIssue() == null)
        {
            throw new IllegalArgumentException("The worklogs issue must not be null.");
        }
    }
}
