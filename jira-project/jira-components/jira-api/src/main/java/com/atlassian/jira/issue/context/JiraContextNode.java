package com.atlassian.jira.issue.context;

import com.atlassian.annotations.PublicApi;
import com.atlassian.bandana.BandanaContext;
import com.atlassian.jira.project.ProjectCategory;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 * A constructed {@link IssueContext} with the ability to climb nodes
 */
@PublicApi
public interface JiraContextNode extends BandanaContext, IssueContext, Comparable<JiraContextNode>
{
    String FIELD_PROJECT_CATEGORY = "projectcategory";
    String FIELD_PROJECT = "project";

    /**
     * @deprecated Use {@link #getProjectCategoryObject()} instead. Since v5.2.
     *
     * @return ProjectCategory
     */
    public GenericValue getProjectCategory();

    public ProjectCategory getProjectCategoryObject();

    public boolean isInContext(IssueContext issueContext);

    /**
     * Copy the supplied parameters and add new ones.
     * @param props to copy from
     * @return the copied map
     */
    Map<String, Object> appendToParamsMap(Map<String, Object> props);

}
