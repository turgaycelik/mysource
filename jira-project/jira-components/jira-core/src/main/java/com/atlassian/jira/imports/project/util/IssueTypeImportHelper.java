package com.atlassian.jira.imports.project.util;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

import java.util.Collection;

/**
 * Contains shared functions around issue types required for project import.
 *
 * @since v3.13
 */
public class IssueTypeImportHelper
{
    private final ConstantsManager constantsManager;
    private final ProjectManager projectManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;

    public IssueTypeImportHelper(final ConstantsManager constantsManager, final ProjectManager projectManager, final IssueTypeSchemeManager issueTypeSchemeManager)
    {
        this.constantsManager = constantsManager;
        this.projectManager = projectManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
    }

    /**
     * Returns the IssueType, in the current system, with the given name.
     * If no Issue Type exists with this name, then returns null.
     *
     * @param name name of the IssueType.
     * @return the IssueType in the current system with the given name.
     */
    public IssueType getIssueTypeForName(final String name)
    {
        if (name == null)
        {
            return null;
        }
        // Find an Issue Type with the same name
        for (final IssueType issueType : constantsManager.getAllIssueTypeObjects())
        {
            if (name.equals(issueType.getName()))
            {
                return issueType;
            }
        }
        return null;
    }

    /**
     * Checks to see that the issue type is valid for the provided project key and that its subtask-ness matches the
     * old issue type.
     *
     * @param newIssueType          the issueType to be inspected, should not be null
     * @param projectKey            the project key to see if the issue type is relevant for that project
     * @param oldIssueTypeIsSubTask true if the old issue type was a subtask issue type, false otherwise
     * @return true if the newIssueType passes the validation, false otherwise
     */
    public boolean isMappingValid(final IssueType newIssueType, final String projectKey, final boolean oldIssueTypeIsSubTask)
    {
        return isIssueTypeValidForProject(projectKey, newIssueType.getId()) && (newIssueType.isSubTask() == oldIssueTypeIsSubTask);
    }

    /**
     * Checks the projects issue type scheme to determine if the issue type identified by newIssueTypeId is contained
     * in the projects scheme.
     *
     * @param projectKey     identifies the project
     * @param newIssueTypeId identifies the issue type to check
     * @return true if the issue type is a part of the projects issue type scheme, false otherwise.
     */
    public boolean isIssueTypeValidForProject(final String projectKey, final String newIssueTypeId)
    {

        final Project project = projectManager.getProjectObjByKey(projectKey);
        final Collection<IssueType> issueTypesForProject;
        if (project == null)
        {
            issueTypesForProject = issueTypeSchemeManager.getIssueTypesForDefaultScheme();
        }
        else
        {
            issueTypesForProject = issueTypeSchemeManager.getIssueTypesForProject(project);
        }
        final IssueType existingIssueType = getExistingIssueTypeForId(issueTypesForProject, newIssueTypeId);
        return existingIssueType != null;
    }

    private IssueType getExistingIssueTypeForId(final Collection<IssueType> issueTypesForProject, final String newId)
    {
        IssueType existingIssueType = null;
        // Run through all the issue types for this project to see if we have a matching issue type
        for (final IssueType issueType : issueTypesForProject)
        {
            if (newId.equals(issueType.getId()))
            {
                existingIssueType = issueType;
                break;
            }
        }
        return existingIssueType;
    }
}
