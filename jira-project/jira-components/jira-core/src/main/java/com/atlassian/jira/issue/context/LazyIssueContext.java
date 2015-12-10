package com.atlassian.jira.issue.context;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import org.ofbiz.core.entity.GenericValue;

/**
 * This class offers no real advantage over IssueContextImpl except that it does the
 * component accessor lookups exactly once regardless of how many times you call its
 * getters.  If that's worth doing, then it's worth doing in IssueContextImpl too.
 * Otherwise this class's functionality is identical except that it does not offer
 * equals and hashCode implementations.  There really isn't any point in using it.
 *
 * @deprecated Use {@link IssueContextImpl} instead. Since v6.1.
 */
@Deprecated
public class LazyIssueContext implements IssueContext
{
    private Long projectId;
    private String issueTypeId;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
    private final ProjectManager projectManager = ComponentAccessor.getProjectManager();

    // ---------------------------------------------------------------------------------------------------- Constructors
    public LazyIssueContext(Long projectId, String issueTypeId)
    {
        this.projectId = projectId;
        this.issueTypeId = issueTypeId;
    }

    public Project getProjectObject()
    {
        return projectManager.getProjectObj(projectId);
    }

    public GenericValue getProject()
    {
        return projectManager.getProject(projectId);
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public IssueType getIssueTypeObject()
    {
        return constantsManager.getIssueTypeObject(issueTypeId);
    }

    public GenericValue getIssueType()
    {
        return constantsManager.getIssueType(issueTypeId);
    }

    public String getIssueTypeId()
    {
        return issueTypeId;
    }

    @Override
    public String toString()
    {
        return "LazyIssueContext[projectId=" + projectId + ",issueTypeId=" + issueTypeId + ']';
    }
}
