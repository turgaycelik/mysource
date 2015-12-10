package com.atlassian.jira.workflow.migration;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * The class is used during workflow migration for a project. It holds mappings between statuses on old workflow and
 * new workflow. As workflows are configurable per issue type of Enterprise Edition of JIRA, the status mappings are
 * held on per issue type basis.
 * <p>
 * This class also holds ids of issues that start the migration on the wrong workflow. For example, if a previous
 * workflow migration failed, some issues have already been migrated. We need to keep track of these issues to ensure
 * that they get migrated again.
 * </p>
 */
public class WorkflowMigrationMapping
{
    private final Map<WorkflowMigrationMappingKey, GenericValue> mappings;
    private final Logger log = Logger.getLogger(getClass());
    private final Collection<Long> issuesIdsOnWrongWorkflow;

    public WorkflowMigrationMapping()
    {
        this.mappings = new HashMap<WorkflowMigrationMappingKey, GenericValue>();
        this.issuesIdsOnWrongWorkflow = new HashSet<Long>();
    }

    public void addMapping(GenericValue issueType, GenericValue oldStatus, GenericValue newStatus)
    {
        final WorkflowMigrationMappingKey mappingKey = new WorkflowMigrationMappingKey(issueType, oldStatus);
        log.debug("Adding (type, oldstatus) -> newstatus mapping: "+mappingKey+" -> " + newStatus.getString("name"));
        mappings.put(mappingKey, newStatus);
    }

    public void addIssueIdsOnWorongWorkflow(Collection<Long> issueIds)
    {
        this.issuesIdsOnWrongWorkflow.addAll(issueIds);
    }

    public boolean isIssueOnWrongWorkflow(Long issueId)
    {
        return issuesIdsOnWrongWorkflow.contains(issueId);
    }

    private GenericValue getTargetStatus(String issueTypeId, String oldStatusId)
    {
        final WorkflowMigrationMappingKey mappingKey = new WorkflowMigrationMappingKey(issueTypeId, oldStatusId);
        final GenericValue status = mappings.get(mappingKey);
        if (status == null) throw new RuntimeException("Encountered an issue whose status is not allowed in the existing workflow. No mapping from (type, status) = "+mappingKey+" defined.");
        return status;
    }

    public GenericValue getTargetStatus(GenericValue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null");
        }

        if (!"Issue".equals(issue.getEntityName()))
        {
            throw new IllegalArgumentException("The entity passed must be of type issue, not '" + issue.getEntityName() + "'.");
        }

        return getTargetStatus(issue.getString("type"), issue.getString("status"));
    }

    private static class WorkflowMigrationMappingKey
    {
        private final String issueTypeId;
        private final String statusId;

        public WorkflowMigrationMappingKey(GenericValue issueType, GenericValue status)
        {
            if (issueType != null)
                issueTypeId = issueType.getString("id");
            else
                issueTypeId = null;

            if (status != null)
                statusId = status.getString("id");
            else
                statusId = null;
        }

        public WorkflowMigrationMappingKey(String issueTypeId, String statusId)
        {
            this.issueTypeId = issueTypeId;
            this.statusId = statusId;
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof WorkflowMigrationMappingKey)) return false;

            final WorkflowMigrationMappingKey workflowMigrationMappingKey = (WorkflowMigrationMappingKey) o;

            if (issueTypeId != null ? !issueTypeId.equals(workflowMigrationMappingKey.issueTypeId) : workflowMigrationMappingKey.issueTypeId != null) return false;
            if (statusId != null ? !statusId.equals(workflowMigrationMappingKey.statusId) : workflowMigrationMappingKey.statusId != null) return false;

            return true;
        }

        public int hashCode()
        {
            int result;
            result = (issueTypeId != null ? issueTypeId.hashCode() : 0);
            result = 29 * result + (statusId != null ? statusId.hashCode() : 0);
            return result;
        }

        public String toString()
        {
            return "("+issueTypeId+", "+statusId+")";
        }
    }
}