package com.atlassian.jira.rest.api.issue;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Response from creating/updating a remote issue link.
 *
 * @since v5.0
 */
@XmlRootElement
public class RemoteIssueLinkCreateOrUpdateResponse
{
    @XmlAttribute
    private Long id;

    @XmlAttribute
    private String self;

    public Long id()
    {
        return id;
    }

    public RemoteIssueLinkCreateOrUpdateResponse id(final Long id)
    {
        this.id = id;
        return this;
    }

    public String self()
    {
        return self;
    }

    public RemoteIssueLinkCreateOrUpdateResponse self(final String self)
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
