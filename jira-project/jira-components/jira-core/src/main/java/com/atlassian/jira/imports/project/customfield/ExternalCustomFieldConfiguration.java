package com.atlassian.jira.imports.project.customfield;

import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.util.dbc.Null;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Holds a single configuration for a custom field and a reference to the {@link com.atlassian.jira.external.beans.ExternalCustomField}.
 *
 * @since v3.13
 */
public class ExternalCustomFieldConfiguration
{
    private final ExternalCustomField customField;
    private final String constrainedProjectId;
    private final List constrainedIssueTypes;
    private final String configurationSchemeId;

    /**
     *
     * @param constrainedIssueTypes the issue types that this custom field configuration is relevant for, a null list
     * implies relevance for all issue types.
     * @param constrainedProjectId the project that this custom field configuration is relevant for.
     * @param customField the customField this configuration describes.
     * @param configurationSchemeId is the id the represents this custom field configuration in the old system.
     */
    public ExternalCustomFieldConfiguration(final List constrainedIssueTypes, final String constrainedProjectId, final ExternalCustomField customField, final String configurationSchemeId)
    {
        Null.not("customField", customField);
        Null.not("configurationSchemeId", configurationSchemeId);
        if (customField == null)
        {
            throw new IllegalArgumentException("Can not create an ExternalCustomFieldConfiguration with a null ExternalCustomField.");
        }
        this.constrainedIssueTypes = ((constrainedIssueTypes != null) ? Collections.unmodifiableList(constrainedIssueTypes) : null);
        this.constrainedProjectId = constrainedProjectId;
        this.customField = customField;
        this.configurationSchemeId = configurationSchemeId;
    }

    public List getConstrainedIssueTypes()
    {
        return constrainedIssueTypes;
    }

    public String getConstrainedProjectId()
    {
        return constrainedProjectId;
    }

    public ExternalCustomField getCustomField()
    {
        return customField;
    }

    public String getConfigurationSchemeId()
    {
        return configurationSchemeId;
    }

    public boolean isConstrainedForIssueType(final String issueTypeId)
    {
        return (constrainedIssueTypes == null) || constrainedIssueTypes.contains(issueTypeId);
    }

    public boolean isForAllIssueTypes()
    {
        return constrainedIssueTypes == null;
    }

    public boolean isForAllProjects()
    {
        return constrainedProjectId == null;
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final ExternalCustomFieldConfiguration that = (ExternalCustomFieldConfiguration) o;

        if (constrainedIssueTypes != null ? !constrainedIssueTypes.equals(that.constrainedIssueTypes) : that.constrainedIssueTypes != null)
        {
            return false;
        }
        if (constrainedProjectId != null ? !constrainedProjectId.equals(that.constrainedProjectId) : that.constrainedProjectId != null)
        {
            return false;
        }
        if (!customField.equals(that.customField))
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = customField.hashCode();
        result = 31 * result + (constrainedProjectId != null ? constrainedProjectId.hashCode() : 0);
        result = 31 * result + (constrainedIssueTypes != null ? constrainedIssueTypes.hashCode() : 0);
        return result;
    }
}
