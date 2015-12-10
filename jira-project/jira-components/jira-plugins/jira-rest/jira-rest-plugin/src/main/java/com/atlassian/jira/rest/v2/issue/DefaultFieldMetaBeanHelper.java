package com.atlassian.jira.rest.v2.issue;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.fields.AssigneeSystemField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.PrioritySystemField;
import com.atlassian.jira.issue.fields.SecurityLevelSystemField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;

/**
 * Helper for deciding default value on field
 *
 * @since v6.2
 */
public class DefaultFieldMetaBeanHelper
{
    protected Project project;
    private IssueType issueType;
    private IssueSecurityLevelManager issueSecurityLevelManager;

    public DefaultFieldMetaBeanHelper(Project project, IssueType issueType, IssueSecurityLevelManager issueSecurityLevelManager)
    {
        this.project = project;
        this.issueType = issueType;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
    }

    public boolean hasDefaultValue(OrderableField field)
    {
        final boolean hasDefaultValue;
        if (field instanceof CustomField)
        {
            final FieldConfig relevantConfig = ((CustomField) field).getRelevantConfig(new IssueContextImpl(project, issueType));
            final Object defaultValue = ((CustomField) field).getCustomFieldType().getDefaultValue(relevantConfig);
            if (defaultValue instanceof String)
            {
                return StringUtils.isNotBlank((String) defaultValue);
            }
            else if (defaultValue instanceof Collection)
            {
                return CollectionUtils.isNotEmpty((Collection<?>) defaultValue); 
            }
            else if (defaultValue instanceof Map)
            {
                return MapUtils.isNotEmpty((Map<?, ?>) defaultValue) && MapUtils.getObject((Map<?, ?>) defaultValue, null) != null;
            }
            return defaultValue != null;
        }
        else if (field instanceof PrioritySystemField)
        {
            hasDefaultValue = ((PrioritySystemField) field).getDefaultPriority() != null;
        }
        else if (field instanceof AssigneeSystemField)
        {
            hasDefaultValue = project != null && project.getAssigneeType() != null && project.getAssigneeType() != AssigneeTypes.UNASSIGNED;
        }
        else if (field instanceof SecurityLevelSystemField)
        {
            hasDefaultValue = project != null && issueSecurityLevelManager.getDefaultSecurityLevel(project) != null;
        }
        else
        {
            hasDefaultValue = false;
        }
        return hasDefaultValue;

    }

}
