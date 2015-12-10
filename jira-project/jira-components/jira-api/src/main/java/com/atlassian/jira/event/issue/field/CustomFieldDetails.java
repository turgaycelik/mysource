package com.atlassian.jira.event.issue.field;

/**
 * @since v6.2
 */
public interface CustomFieldDetails
{
    String getId();

    String getFieldTypeName();

    Long getIdAsLong();

    String getUntranslatedName();

    String getUntranslatedDescription();
}
