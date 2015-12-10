package com.atlassian.jira.issue.worklog;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OfBizWorklogStore implements WorklogStore
{
    private final OfBizDelegator ofBizDelegator;
    private final IssueManager issueManager;
    private WorklogManager worklogManager;
    public static final String WORKLOG_ENTITY = "Worklog";

    public OfBizWorklogStore(OfBizDelegator ofBizDelegator, IssueManager issueManager)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.issueManager = issueManager;
    }

    public Worklog update(Worklog worklog)
    {
        GenericValue worklogGV = ofBizDelegator.findById(WORKLOG_ENTITY, worklog.getId());
        if (worklogGV == null)
        {
            throw new IllegalArgumentException("Could not find original worklog entity to update.");
        }

        worklogGV.setFields(createParamMap(worklog));
        try
        {
            worklogGV.store();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        return convertToWorklog(worklog.getIssue(), worklogGV);
    }

    public Worklog create(Worklog worklog)
    {       
        Map fields = createParamMap(worklog);

        GenericValue worklogGV = ofBizDelegator.createValue(WORKLOG_ENTITY, fields);

        return convertToWorklog(worklog.getIssue(), worklogGV);
    }

    public boolean delete(Long worklogId)
    {
        if(worklogId == null)
        {
            throw new IllegalArgumentException("Cannot remove a worklog with id null.");
        }
        int numRemoved = ofBizDelegator.removeByAnd(WORKLOG_ENTITY, EasyMap.build("id", worklogId));
        return numRemoved == 1;
    }

    Map<String,Object> createParamMap(Worklog worklog)
    {
        if (worklog == null)
        {
            throw new IllegalArgumentException("Cannot store a null worklog.");
        }

        if (worklog.getIssue() == null)
        {
            throw new IllegalArgumentException("Cannot store a worklog against a null issue.");
        }

        Map<String,Object> fields = new HashMap<String,Object>();
        fields.put("issue", worklog.getIssue().getId());
        fields.put("author", worklog.getAuthorKey());
        fields.put("updateauthor", worklog.getUpdateAuthorKey());
        fields.put("body", worklog.getComment());
        fields.put("grouplevel", worklog.getGroupLevel());
        fields.put("rolelevel", worklog.getRoleLevelId());
        fields.put("timeworked", worklog.getTimeSpent());
        fields.put("startdate", new Timestamp(worklog.getStartDate().getTime()));
        fields.put("created", new Timestamp(worklog.getCreated().getTime()));
        fields.put("updated", new Timestamp(worklog.getUpdated().getTime()));
        return fields;
    }

    public Worklog getById(Long id)
    {
        Worklog worklog = null;
        GenericValue worklogGV = ofBizDelegator.findById(WORKLOG_ENTITY, id);

        if (worklogGV != null)
        {
            Issue issue = getIssueForId(worklogGV.getLong("issue"));
            worklog = convertToWorklog(issue, worklogGV);
        }
        
        return worklog;
    }

    public List<Worklog> getByIssue(Issue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Cannot resolve worklogs for null issue.");
        }

        List<GenericValue> worklogGVs = ofBizDelegator.findByAnd(WORKLOG_ENTITY, EasyMap.build("issue", issue.getId()), EasyList.build("created ASC"));
        List<Worklog> worklogs = new ArrayList<Worklog>(worklogGVs.size());
        for (GenericValue worklogGV : worklogGVs)
        {
            worklogs.add(convertToWorklog(issue, worklogGV));
        }

        return worklogs;
    }

    public int swapWorklogGroupRestriction(String groupName, String swapGroup)
    {
        if (groupName == null)
        {
            throw new IllegalArgumentException("You must provide a non null group name.");
        }

        if (swapGroup == null)
        {
            throw new IllegalArgumentException("You must provide a non null swap group name.");
        }

        return ofBizDelegator.bulkUpdateByAnd(WORKLOG_ENTITY, EasyMap.build("grouplevel", swapGroup), EasyMap.build("grouplevel", groupName));
    }

    public long getCountForWorklogsRestrictedByGroup(String groupName)
    {
        if (groupName == null)
        {
            throw new IllegalArgumentException("You must provide a non null group name.");
        }

        EntityCondition condition = new EntityFieldMap(EasyMap.build("grouplevel", groupName), EntityOperator.AND);
        List worklogCount = ofBizDelegator.findByCondition("WorklogCount", condition, EasyList.build("count"), Collections.EMPTY_LIST);
        if (worklogCount != null && worklogCount.size() == 1)
        {
            GenericValue worklogCountGV = (GenericValue) worklogCount.get(0);
            return worklogCountGV.getLong("count");
        }
        else
        {
            throw new DataAccessException("Unable to access the count for the Worklog table");
        }
    }

    Worklog convertToWorklog(Issue issue, GenericValue gv)
    {
        Timestamp startDateTS = gv.getTimestamp("startdate");
        Timestamp createdTS = gv.getTimestamp("created");
        Timestamp updatedTS = gv.getTimestamp("updated");
        return new WorklogImpl(getWorklogManager(),
                issue,
                gv.getLong("id"),
                gv.getString("author"),
                gv.getString("body"),
                startDateTS == null ? null : new Date(startDateTS.getTime()),
                gv.getString("grouplevel"),
                gv.getLong("rolelevel"),
                gv.getLong("timeworked"),
                gv.getString("updateauthor"),
                createdTS == null ? null : new Date(createdTS.getTime()),
                updatedTS == null ? null : new Date(updatedTS.getTime()));
    }

    Issue getIssueForId(Long issueId)
    {
        return issueManager.getIssueObject(issueId);
    }

    private WorklogManager getWorklogManager()
    {
        if (worklogManager == null)
        {
            worklogManager = ComponentAccessor.getComponentOfType(WorklogManager.class);
        }
        return worklogManager;
    }
}
