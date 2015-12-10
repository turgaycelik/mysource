package com.atlassian.jira.issue.fields;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * ConfigurableField are fields which have {@link FieldConfigItemType} that can be stored for a given
 * {@link JiraContextNode}
 */
@PublicApi
public interface ConfigurableField extends OrderableField
{
    /**
     * Returns a List of {@link FieldConfigItemType} objects. This opens up possibilties for configurable custom fields
     *
     * @return List of {@link FieldConfigItemType} @Nonnull
     */
    List<FieldConfigItemType> getConfigurationItemTypes();

    /**
     * Returns a list of projects associated with this field. Will be null if the field is global
     *
     * @return List of project generic values
     *
     * @deprecated Use {@link #getAssociatedProjectObjects()} instead. Since v5.2.
     */
    List<GenericValue> getAssociatedProjects();

    /**
     * Returns a list of projects associated with this field. Will be null if the field is global
     *
     * @return a list of projects associated with this field.
     */
    List<Project> getAssociatedProjectObjects();

    FieldConfig getRelevantConfig(IssueContext issueContext);
}
