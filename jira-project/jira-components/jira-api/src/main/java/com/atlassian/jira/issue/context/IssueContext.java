package com.atlassian.jira.issue.context;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

/**
 * A context (scope) for an issue or custom field.
 * For example, global custom fields have an IssueContext whose project and issue type are null.
 */
@PublicApi
public interface IssueContext
{
    /**
     * Global context not associated with any project or any issues types.
     */
    public static IssueContext GLOBAL = new IssueContext()
    {
        @Override
        public Project getProjectObject()
        {
            return null;
        }

        @Override
        public GenericValue getProject()
        {
            return null;
        }

        @Override
        public Long getProjectId()
        {
            return null;
        }

        @Override
        public IssueType getIssueTypeObject()
        {
            return null;
        }

        @Override
        public GenericValue getIssueType()
        {
            return null;
        }

        @Override
        public String getIssueTypeId()
        {
            return null;
        }
    };

    /**
     * Gets the Project for this IssueContext.
     * <p> A null return value is used to represent that this context applies to all Projects.
     *
     * @return The Project for this IssueContext (can be null).
     */
    Project getProjectObject();
    /**
     * Gets the Project for this IssueContext.
     * <p> A null return value is used to represent that this context applies to all Projects.
     *
     * @return The Project for this IssueContext (can be null).
     * @deprecated Please use {@link #getProjectObject()}. Deprecated since v4.0
     */
    GenericValue getProject();

    /**
     * Gets the ID of the Project for this IssueContext.
     * <p> A null return value is used to represent that this context applies to all Projects.
     *
     * @return The ID of the Project for this IssueContext (can be null).
     */
    Long getProjectId();

    /**
     * Gets the IssueType for this IssueContext.
     * <p> A null return value is used to represent that this context applies to all IssueTypes.
     *
     * @return The IssueType for this IssueContext (can be null).
     */
    IssueType getIssueTypeObject();

    /**
     * Gets the IssueType for this IssueContext.
     * <p> A null return value is used to represent that this context applies to all IssueTypes.
     *
     * @return The IssueType for this IssueContext (can be null).
     * @deprecated Please use {@link #getIssueTypeObject()}. Deprecated since v4.0
     */
    GenericValue getIssueType();

    /**
     * Gets the ID of the IssueType for this IssueContext.
     * <p> A null return value is used to represent that this context applies to all IssueTypes.
     *
     * @return The ID of the IssueType for this IssueContext (can be null).
     */
    String getIssueTypeId();
}
