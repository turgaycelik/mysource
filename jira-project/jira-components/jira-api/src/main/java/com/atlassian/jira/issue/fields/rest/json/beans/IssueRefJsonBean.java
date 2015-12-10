package com.atlassian.jira.issue.fields.rest.json.beans;

import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.net.URI;

import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang.builder.HashCodeBuilder.reflectionHashCode;
import static org.apache.commons.lang.builder.ToStringBuilder.reflectionToString;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_NULL;

/**
 * A reference to an issue (either by id or by issue key).
 *
 * @since v5.0
 */
@JsonSerialize(include = NON_NULL)
public class IssueRefJsonBean
{
    @JsonProperty
    private String id;

    @JsonProperty
    private String key;

    @JsonProperty
    private URI self;

    @JsonProperty
    private Fields fields;

    public IssueRefJsonBean()
    {
    }

    public IssueRefJsonBean(String id, String key, URI self, Fields fields)
    {
        this.id = id;
        this.key = key;
        this.self = self;
        this.fields = fields;
    }

    public String id()
    {
        return this.id;
    }

    public IssueRefJsonBean id(String id)
    {
        return new IssueRefJsonBean(id, key, self, fields);
    }

    public String key()
    {
        return this.key;
    }

    public IssueRefJsonBean key(String key)
    {
        return new IssueRefJsonBean(id, key, self, fields);
    }

    public URI self()
    {
        return this.self;
    }

    public IssueRefJsonBean self(URI self)
    {
        return new IssueRefJsonBean(id, key, self, fields);
    }

    public Fields fields()
    {
        return this.fields;
    }

    public IssueRefJsonBean fields(Fields fields)
    {
        return new IssueRefJsonBean(id, key, self, fields);
    }

    @Override
    public boolean equals(Object obj) { return reflectionEquals(this, obj); }

    @Override
    public int hashCode() { return reflectionHashCode(this); }

    @Override
    public String toString() { return reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE); }

    public static class Fields
    {
        @JsonProperty
        private String summary;

        @JsonProperty
        private StatusJsonBean status;

        @JsonProperty("issuetype")
        private IssueTypeJsonBean issueType;

        @JsonProperty
        private PriorityJsonBean priority;

        public Fields()
        {
        }

        public Fields(String summary, StatusJsonBean status, IssueTypeJsonBean issueType, PriorityJsonBean priority)
        {
            this.summary = summary;
            this.status = status;
            this.issueType = issueType;
            this.priority = priority;
        }

        public String summary()
        {
            return this.summary;
        }

        public Fields summary(String summary)
        {
            return new Fields(summary, status, issueType, priority);
        }

        public StatusJsonBean status()
        {
            return this.status;
        }

        public Fields status(StatusJsonBean status)
        {
            return new Fields(summary, status, issueType, priority);
        }

        public IssueTypeJsonBean issueType()
        {
            return this.issueType;
        }

        public Fields issueType(IssueTypeJsonBean issueType)
        {
            return new Fields(summary, status, issueType, priority);
        }

        public PriorityJsonBean priority()
        {
            return this.priority;
        }

        public Fields priority(PriorityJsonBean priority)
        {
            return new Fields(summary, status, issueType, priority);
        }

        @Override
        public boolean equals(Object obj) { return reflectionEquals(this, obj); }

        @Override
        public int hashCode() { return reflectionHashCode(this); }

        @Override
        public String toString() { return reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE); }
    }

}
