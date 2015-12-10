package com.atlassian.jira.issue.fields;

/**
 * Defines fields that are dependent on another field
 */
public interface DependentField extends OrderableField
{
    Field getParentField();
}
