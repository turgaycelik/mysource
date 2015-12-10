package com.atlassian.jira.issue.fields.config.manager;

import com.atlassian.jira.issue.fields.config.FieldConfig;

/**
 * Responsible for cleaning up additional data when a FieldConfig is removed.
 * 
 * @since 3.13
 */
public interface FieldConfigCleanup
{
    void removeAdditionalData(FieldConfig fieldConfig);
}
