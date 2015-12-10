package com.atlassian.jira.event.scheme;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.scheme.SchemeEntity;

/**
 * Abstract event that captures the data relevant to scheme entity events, e.g. permission entities, notitification
 * entities etc.
 *
 * @since v5.0
 */
@Internal
public class AbstractSchemeEntityEvent
{
    private Long schemeId;
    private String type;
    private String parameter;
    private Object entityTypeId;
    private Long schemeEntityId;

    public AbstractSchemeEntityEvent(final Long schemeId, final SchemeEntity schemeEntity)
    {
        this.schemeId = schemeId;
        if (schemeEntity != null)
        {
            this.type = schemeEntity.getType();
            this.parameter = schemeEntity.getParameter();
            this.entityTypeId = schemeEntity.getEntityTypeId();
            this.schemeEntityId = schemeEntity.getId();
        }
    }

    public Long getSchemeId()
    {
        return schemeId;
    }

    public String getType()
    {
        return type;
    }

    public String getParameter()
    {
        return parameter;
    }

    public Object getEntityTypeId()
    {
        return entityTypeId;
    }

    public Long getSchemeEntityId()
    {
        return schemeEntityId;
    }
}
