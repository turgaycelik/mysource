package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalIssueImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import org.apache.log4j.Logger;

import java.sql.Timestamp;

/**
 * @since v3.13
 */
public class IssueTransformerImpl implements IssueTransformer
{
    private static final Logger log = Logger.getLogger(IssueTransformerImpl.class);

    public ExternalIssue transform(final ProjectImportMapper projectImportMapper, final ExternalIssue issue)
    {
        // Create a new issue and copy all the values that don't need to be tranformed
        String creatorKey = projectImportMapper.getUserMapper().getMappedUserKey(issue.getCreator());
        final ExternalIssue newIssue = new ExternalIssueImpl(creatorKey);
        newIssue.setKey(issue.getKey());
        newIssue.setSummary(issue.getSummary());
        newIssue.setReporter(projectImportMapper.getUserMapper().getMappedUserKey(issue.getReporter()));
        newIssue.setAssignee(projectImportMapper.getUserMapper().getMappedUserKey(issue.getAssignee()));
        newIssue.setDescription(issue.getDescription());
        newIssue.setEnvironment(issue.getEnvironment());
        newIssue.setCreated(issue.getCreated());
        newIssue.setUpdated(issue.getUpdated());
        newIssue.setDuedate(issue.getDuedate());
        newIssue.setVotes(issue.getVotes());
        newIssue.setOriginalEstimate(issue.getOriginalEstimate());
        newIssue.setTimeSpent(issue.getTimeSpent());
        newIssue.setEstimate(issue.getEstimate());

        // Transform the values that need to be
        newIssue.setProject(projectImportMapper.getProjectMapper().getMappedId(issue.getProject()));
        newIssue.setIssueType(projectImportMapper.getIssueTypeMapper().getMappedId(issue.getIssueType()));
        newIssue.setStatus(projectImportMapper.getStatusMapper().getMappedId(issue.getStatus()));
        newIssue.setPriority(projectImportMapper.getPriorityMapper().getMappedId(issue.getPriority()));
        newIssue.setResolution(projectImportMapper.getResolutionMapper().getMappedId(issue.getResolution()));
        
        //if created and updated date aren't being set form the external issue, init them to now.  An issue must have
        //a created and updated time.
        final Timestamp now = new Timestamp(System.currentTimeMillis());
        if (newIssue.getCreated() == null)
        {
            newIssue.setCreated(now);
        }
        if (newIssue.getUpdated() == null)
        {
            newIssue.setUpdated(now);
        }

        //NOTE: This HAS to come after the setResolutionId() call, otherwhise that will override this date.
        //if we have a resolution date and the resolution is set, set it on the issue
        if(issue.getResolutionDate() != null && issue.getResolution() != null)
        {
            newIssue.setResolutionDate(issue.getResolutionDate());
        }
        //otherwise, if we don't have a resolution date, however the issue is resolved, fall back to the last updated date
        //which will either be now or whatever was imported.
        else if(issue.getResolution() != null)
        {
            //For an XML project import this should really NEVER happen, but better to be safe.
            newIssue.setResolutionDate(issue.getUpdated());
        }
        newIssue.setSecurityLevel(projectImportMapper.getIssueSecurityLevelMapper().getMappedId(issue.getSecurityLevel()));
        if ((issue.getSecurityLevel() != null) && (projectImportMapper.getIssueSecurityLevelMapper().getMappedId(issue.getSecurityLevel()) == null))
        {
            // This means we had some orphan data that we are now dropping we want to log this detail
            log.warn("Dropping the issue security level with id '" + issue.getSecurityLevel() + "' for issue with key '" + issue.getKey() + "' as JIRA is unable to resolve the issue security level in the backup data.");
        }

        return newIssue;
    }
}
