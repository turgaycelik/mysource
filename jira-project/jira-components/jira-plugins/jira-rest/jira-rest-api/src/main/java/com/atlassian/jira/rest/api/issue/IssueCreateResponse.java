package com.atlassian.jira.rest.api.issue;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean for issue create response.
 */
@XmlRootElement
public class IssueCreateResponse
{
    public String id;
    public String key;
    public String self;

    public String key()
    {
        return key;
    }

    public IssueCreateResponse key(String key)
    {
        this.key = key;
        return this;
    }

    public String id()
    {
        return this.id;
    }

    public IssueCreateResponse id(String id)
    {
        this.id = id;
        return this;
    }

    public String self()
    {
        return self;
    }

    public IssueCreateResponse self(String self)
    {
        this.self = self;
        return this;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
