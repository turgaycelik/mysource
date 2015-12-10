package com.atlassian.jira.rest.v2.entity.property;

import java.util.List;

import com.atlassian.fugue.Function2;
import com.atlassian.jira.issue.fields.rest.json.beans.*;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.codehaus.jackson.annotate.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since v6.2
 */
public class EntityPropertiesKeysBean
{
    @JsonProperty(value = "keys")
    private final List<EntityPropertyKeyBean> entityPropertyKeyBeans;

    EntityPropertiesKeysBean(final List<EntityPropertyKeyBean> entityPropertyKeyBeans)
    {
        this.entityPropertyKeyBeans = entityPropertyKeyBeans;
    }

    public static EntityPropertiesKeysBean build(final JiraBaseUrls urls, final Long issueId,
            final List<String> propertyKeys, final Function2<Long, String, String> selfFunction)
    {
        return new EntityPropertiesKeysBean(Lists.transform(propertyKeys, new Function<String, EntityPropertyKeyBean>()
        {
            @Override
            public EntityPropertyKeyBean apply(final String propertyKey)
            {
                return new EntityPropertyKeyBean(propertyKey, com.atlassian.jira.issue.fields.rest.json.beans.EntityPropertyBean.propertySelf(urls, issueId, propertyKey, selfFunction));
            }
        }));
    }

    public List<EntityPropertyKeyBean> getEntityPropertyKeyBeans()
    {
        return entityPropertyKeyBeans;
    }

    public static class EntityPropertyKeyBean
    {
        @JsonProperty
        private final String self;

        @JsonProperty
        private final String key;

        EntityPropertyKeyBean(final String key, final String self)
        {
            this.key = checkNotNull(key);
            this.self = checkNotNull(self);
        }

        public String getSelf()
        {
            return self;
        }

        public String getKey()
        {
            return key;
        }
    }
}
