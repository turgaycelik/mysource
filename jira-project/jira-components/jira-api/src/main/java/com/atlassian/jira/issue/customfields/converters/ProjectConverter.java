package com.atlassian.jira.issue.customfields.converters;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

@Internal
public interface ProjectConverter
{
    /**
     * Get the String value that represents the given Project.
     *
     * @param project Project to convert
     * @return the String value that represents the given Project.
     */
    public String getString(Project project);

    /**
     * Get the String value that represents the given Project.
     *
     * @param project Project to convert
     * @return the String value that represents the given Project.
     * @deprecated - Use {@link #getString(com.atlassian.jira.project.Project)}. Deprecated since v4.0
     */
    @Deprecated
    public String getString(GenericValue project);

    /**
     * Get the Project that this String value represents.
     *
     * @param stringValue the String representation.
     * @return the Project that this String value represents.
     * @throws com.atlassian.jira.issue.customfields.impl.FieldValidationException if we are unable to convert the String representation.
     */
    public Project getProjectObject(String stringValue) throws FieldValidationException;

    /**
     * Get the Project for the given ID.
     * If a null projectId is passed in, then a null Project is returned.
     *
     * @param projectId the Project ID.
     * @return the Project for the given ID.
     * @throws com.atlassian.jira.issue.customfields.impl.FieldValidationException if the Project ID is invalid.
     */
    public Project getProjectObject(Long projectId) throws FieldValidationException;

    /**
     * Get the Project that this String value represents.
     *
     * @param stringValue the String representation.
     * @return the Project that this String value represents.
     * @throws com.atlassian.jira.issue.customfields.impl.FieldValidationException if we are unable to convert the String representation.
     *
     * @deprecated - Use {@link #getProjectObject(String)}. Deprecated since v4.0
     */
    @Deprecated
    public GenericValue getProject(String stringValue) throws FieldValidationException;

    /**
     * Get the Project for the given ID.
     *
     * @param projectId the Project ID.
     * @return the Project for the given ID.
     * @throws com.atlassian.jira.issue.customfields.impl.FieldValidationException if the Project ID is invalid.
     *
     * @deprecated - Use {@link #getProjectObject(String)}. Deprecated since v4.0
     */
    @Deprecated
    public GenericValue getProject(Long projectId) throws FieldValidationException;
}
