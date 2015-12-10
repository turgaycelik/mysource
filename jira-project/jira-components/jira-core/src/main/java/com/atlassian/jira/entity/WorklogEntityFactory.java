package com.atlassian.jira.entity;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 *
 * @since v6.1
 */
public class WorklogEntityFactory  extends AbstractEntityFactory<Worklog>
{
    private final Issue issue;

    public WorklogEntityFactory(final Issue issue)
    {
        this.issue = issue;
    }


    @Override
    public Map<String, Object> fieldMapFrom(@Nonnull Worklog worklog)
    {
        if (worklog.getIssue() == null)
        {
            throw new IllegalArgumentException("Cannot store a worklog against a null issue.");
        }

        Map<String,Object> fields = new HashMap<String,Object>();
        fields.put("id", worklog.getId());
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

    @Override
    public String getEntityName()
    {
        return "Worklog";
    }

    @Override
    public Worklog build(final GenericValue gv)
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

    public WorklogManager getWorklogManager()
    {
        return ComponentAccessor.getWorklogManager();
    }
}
