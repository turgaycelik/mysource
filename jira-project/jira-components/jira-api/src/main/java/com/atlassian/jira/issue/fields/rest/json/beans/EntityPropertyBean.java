package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.fugue.Function2;
import com.atlassian.jira.util.JiraUrlCodec;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonRawValue;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since v6.2
 */
public class EntityPropertyBean
{
    @JsonProperty @JsonIgnore
    private String self;

    @JsonProperty
    private String key;

    @JsonProperty @JsonRawValue
    private String value;

    @SuppressWarnings("unused")
    public EntityPropertyBean()
    {
    }

    public EntityPropertyBean(final String key, final String value, final String self)
    {
        this.key = checkNotNull(key);
        this.value = checkNotNull(value);
        this.self = self;
    }

    public String getSelf()
    {
        return self;
    }

    public String getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "EntityPropertyBean{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public static Builder builder(JiraBaseUrls jiraBaseUrls, Function2<Long, String, String> entityIdToSelfFunction)
    {
        return new Builder(jiraBaseUrls, entityIdToSelfFunction);
    }

    public static class Builder
    {
        private final JiraBaseUrls urls;

        private String key;
        private String value;
        private Function2<Long, String, String> entityIdToSelfFunction;

        public Builder(final JiraBaseUrls jiraBaseUrls, final Function2<Long, String, String> entityIdToSelfFunction)
        {
            this.urls = jiraBaseUrls;
            this.entityIdToSelfFunction = entityIdToSelfFunction;
        }

        public Builder key(String key)
        {
            this.key = key;
            return this;
        }

        public Builder value(String value)
        {
            this.value = value;
            return this;
        }

        public EntityPropertyBean build(Long entityId)
        {
            return new EntityPropertyBean(checkNotNull(key), checkNotNull(value), buildSelf(entityId));
        }

        private String buildSelf(final Long entityId)
        {
           return propertySelf(urls, entityId, key, entityIdToSelfFunction);
        }
    }

    public static String propertySelf(JiraBaseUrls urls, Long entityId, String propertyKey, Function2<Long, String, String> selfFunction)
    {
        return urls.restApi2BaseUrl() + selfFunction.apply(entityId, JiraUrlCodec.encode(propertyKey));
    }

}
