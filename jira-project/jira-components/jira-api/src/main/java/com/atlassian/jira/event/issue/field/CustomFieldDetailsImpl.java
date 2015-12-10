package com.atlassian.jira.event.issue.field;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.fields.CustomField;

import javax.annotation.Nonnull;

/**
 * @since v6.2
 */
@Internal
public class CustomFieldDetailsImpl implements CustomFieldDetails
{
    private final String id;
    private final String fieldTypeName;
    private final Long idAsLong;
    private final String untranslatedName;
    private final String untranslatedDescription;

    public CustomFieldDetailsImpl(@Nonnull CustomField customField)
    {
        id = customField.getId();
        fieldTypeName = customField.getCustomFieldType() != null ? customField.getCustomFieldType().getName() : null;
        idAsLong = customField.getIdAsLong();
        untranslatedName = customField.getUntranslatedName();
        untranslatedDescription = customField.getUntranslatedDescription();
    }

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public String getFieldTypeName()
    {
        return fieldTypeName;
    }

    @Override
    public Long getIdAsLong()
    {
        return idAsLong;
    }

    @Override
    public String getUntranslatedName()
    {
        return untranslatedName;
    }

    @Override
    public String getUntranslatedDescription()
    {
        return untranslatedDescription;
    }
}
