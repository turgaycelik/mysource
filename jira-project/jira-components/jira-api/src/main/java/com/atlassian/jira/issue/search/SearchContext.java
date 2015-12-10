package com.atlassian.jira.issue.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Represents the Project and IssueType combination which is used to determine the allowable fields and values
 * when searching for Issues. In JIRA Schemes generally define what is customised on a per "Context" basis.
 */
@PublicApi
public interface SearchContext
{

    /**
     * Returns whether the context is <em>global</em> or not. A context is global when there are no project
     * restrictions and no project category restrictions.
     * @return boolean
     */
    boolean isForAnyProjects();

    /**
     * Returns true if no specific issue types have been selected
     * @return boolean
     */
    boolean isForAnyIssueTypes();

    /**
     * Returns true if there is exactly one Project in this SearchContext.
     *
     * @return true if there is exactly one Project in this SearchContext.
     */
    boolean isSingleProjectContext();

    /**
     * Returns the single Project for this SearchContext.
     *
     * You should first call {@link #isSingleProjectContext()} to check if this is valid.
     *
     * @return the single Project for this SearchContext.
     *
     * @throws IllegalStateException if there is not exactly one Project in this SearchContext.
     *
     * @see #isSingleProjectContext()
     */
    Project getSingleProject();

    /**
     * Returns selected categories
     * @return Empty list if no categories were selected
     */
    List getProjectCategoryIds();

    /**
     * Project ids as Longs.
     * @return List of Long objects, possibly empty.
     */
    List<Long> getProjectIds();

    /**
     * Returns the single Project for this SearchContext.
     *
     * You should first call {@link #isSingleProjectContext()} to check if this is valid.
     *
     * @return the single Project for this SearchContext.
     *
     * @deprecated Use {@link #getSingleProject()} instead. Since v5.2.
     */
    GenericValue getOnlyProject();

    /**
     * Issue ids as Strings
     * @return List of issue type ids possibly empty.
     */
    List<String> getIssueTypeIds();

    /**
     * Gets the search context as a list of {@link IssueContext} objects
     * @return List of {@link IssueContext}. If no issue types or projects selected. A blank issue context is returned. Never null.
     */
    List<IssueContext> getAsIssueContexts();

    /**
     * Verifies that all issue types and projects in the context actually still exists. This might not be the case.
     * Also removes any projects or issue types from this SearchContext that do not (any longer) exist in the backing
     * store. 
     */
    void verify();

    /**
     * Returns project objects in this SearchContext
     * @return List of {@link Project}. If no projects are selected, returns an empty list.
     */
    List<Project> getProjects();

    /**
     * Returns issue types objects in this SearchContext
     * @return List of {@link IssueType}. If no issue types are selected, returns an empty list
     */
    List<IssueType> getIssueTypes();
}
