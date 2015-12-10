package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class in charge of instantiating {@link CustomFieldScope} objects.
 */
public class DefaultCustomFieldScopeFactory implements CustomFieldScopeFactory
{
    private final FieldConfigSchemeManager schemeManager;

    public DefaultCustomFieldScopeFactory(final FieldConfigSchemeManager schemeManager)
    {
        this.schemeManager = schemeManager;
    }

    @Override
    public CustomFieldScope createFor(final CustomField customField)
    {
        return new CustomFieldScopeImpl(checkNotNull(customField), schemeManager);
    }
}
