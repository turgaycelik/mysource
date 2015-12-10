package com.atlassian.jira.issue.fields;

/**
 * Factory responsible of instantiating {@link CustomFieldScope} objects.
 */
public interface CustomFieldScopeFactory
{
    /**
     * Creates a {@link CustomFieldScope} for the given {@link CustomField}.
     *
     * @param customField The custom field for which a {@link CustomFieldScope} will be built.
     * @return A {@link CustomFieldScope} for the given custom field.
     */
    CustomFieldScope createFor(final CustomField customField);
}
