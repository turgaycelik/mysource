package com.atlassian.jira.issue.fields;

/**
 * @since v5.0
 */
public interface LabelsField extends HideableField, RequirableField, OrderableField
{
    String LABELS_NAME_KEY = "issue.field.labels";
    String SEPARATOR_CHAR = " ";
}
