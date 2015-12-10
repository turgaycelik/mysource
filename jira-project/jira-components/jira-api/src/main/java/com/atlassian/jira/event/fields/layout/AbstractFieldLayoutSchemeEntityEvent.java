package com.atlassian.jira.event.fields.layout;

import javax.annotation.Nonnull;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;

/**
 * @since v6.2
 */
public class AbstractFieldLayoutSchemeEntityEvent extends AbstractFieldLayoutEvent
{
    private final FieldLayoutSchemeEntityDetails entityDetails;

    public AbstractFieldLayoutSchemeEntityEvent(@Nonnull final FieldLayoutScheme scheme, final FieldLayoutSchemeEntity entity)
    {
        super(scheme);
        entityDetails = createDetails(entity);
    }

    public FieldLayoutSchemeEntityDetails getEntityDetails()
    {
        return entityDetails;
    }

    protected FieldLayoutSchemeEntityDetails createDetails(final FieldLayoutSchemeEntity entity)
    {
        return new FieldLayoutSchemeEntityDetails()
        {
            @Override
            public String getIssueTypeId()
            {
                return entity.getIssueTypeId();
            }

            @Override
            public Long getFieldLayoutId()
            {
                return entity.getFieldLayoutId();
            }
        };
    }
}
