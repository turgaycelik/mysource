package com.atlassian.jira.issue.fields.config.manager;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;

import org.ofbiz.core.entity.GenericValue;

/**
 * A manager to manage {@link IssueType}'s unique set of circumstances. That is, it circumvents the scheme system by
 * collpasing the scheme and config
 */
public interface IssueTypeSchemeManager
{
    FieldConfigScheme create(String schemeName, String schemeDescription, List optionIds);

    FieldConfigScheme update(FieldConfigScheme configScheme, Collection optionIds);

    /**
     * Returns the default global issue type, to which all issue types are added to
     *
     * @return FieldConfigScheme
     */
    FieldConfigScheme getDefaultIssueTypeScheme();

    boolean isDefaultIssueTypeScheme(FieldConfigScheme configScheme);

    /**
     * Adds the option with the issue type id to the default issue type scheme
     *
     * @param id issue type id
     */
    void addOptionToDefault(String id);

    /**
     * Gets all schemes that has the issue type as part of its options
     *
     * @param optionId Issue type id being queried
     * @return Collection of {@link FieldConfigScheme} objects
     */
    Collection getAllRelatedSchemes(String optionId);

    void removeOptionFromAllSchemes(String optionId);

    void deleteScheme(FieldConfigScheme configScheme);

    List<FieldConfigScheme> getAllSchemes();

    IssueType getDefaultValue(Issue issue);

    /**
     * Returns the {@link IssueType} object that is the default for thie configuration
     *
     * @param config
     * @return IssueType representing the default value of this config
     */
    IssueType getDefaultValue(FieldConfig config);

    void setDefaultValue(FieldConfig config, String optionId);

    /**
     * Returns the default {@link IssueType} object based on the passed in project.
     *
     * @param project
     * @return IssueType or null if there is no default
     *
     * @deprecated Use {@link #getDefaultIssueType(Project)} instead. Since v5.2.
     */
    IssueType getDefaultValue(GenericValue project);

    /**
     * Returns the default {@link IssueType} object based on the passed in project.
     *
     * @param project the Project
     *
     * @return IssueType or null if there is no default
     */
    IssueType getDefaultIssueType(Project project);

    /**
     * Returns a config scheme for a given project
     *
     * @param project
     * @return Relevent configscheme
     * @deprecated Use of {@link GenericValue} is discouraged. Deprecated since: v4.0. Use {@link #getConfigScheme(Project)} instead.
     */
    FieldConfigScheme getConfigScheme(GenericValue project);

    /**
     * Returns a config scheme for a given project
     *
     * @param project
     * @return Relevent configscheme
     */
    FieldConfigScheme getConfigScheme(Project project);

    /**
     * Return the collection of issue types associated with this project
     *
     * @param project project generic value
     * @return collection of issue type objects
     * @deprecated Use of {@link GenericValue} is discouraged. Deprecated since: 3.9. Use {@link #getIssueTypesForProject(Project)} instead.
     */
    @Nonnull
    @Deprecated
    Collection<IssueType> getIssueTypesForProject(GenericValue project);

    /**
     * Return the collection of issue types associated with this project
     *
     * @param project project to return the issue types of
     * @return collection of {@link IssueType} objects (possibly empty, never null).
     */
    @Nonnull
    Collection<IssueType> getIssueTypesForProject(Project project);

    /**
     * Return the collection of issue types associated with the default scheme.
     *
     * @return collection of {@link IssueType} objects (possibly empty, never null).
     */
    @Nonnull
    Collection<IssueType> getIssueTypesForDefaultScheme();

    /**
     * Return the collection of sub-task issue types associated with this project
     * Join of the result of this method and
     * {@link #getNonSubTaskIssueTypesForProject(com.atlassian.jira.project.Project)}
     * produces the same result as a call to
     * {@link #getIssueTypesForProject(com.atlassian.jira.project.Project)}.
     *
     * @param project project to return the issue types of
     * @return collection of {@link IssueType} objects (possibly empty, never null).
     */
    @Nonnull
    Collection<IssueType> getSubTaskIssueTypesForProject(@Nonnull Project project);

    /**
     * Return the collection of issue types other than sub-tasks associated with this project.
     * Join of the result of this method and
     * {@link #getSubTaskIssueTypesForProject(com.atlassian.jira.project.Project)}
     * produces the same result as a call to
     * {@link #getIssueTypesForProject(com.atlassian.jira.project.Project)}.
     *
     * @param project project to return the issue types of
     * @return collection of {@link IssueType} objects
     */
    @Nonnull
    Collection<IssueType> getNonSubTaskIssueTypesForProject(Project project);

}
