package com.atlassian.jira.rest.v2.entity;

import java.net.URI;

import com.google.common.base.Objects;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonRawValue;

/**
 * Represents a remote entity link.  More specific remote entity link types may extend
 * this implementation if there is additional metadata that they would like to include,
 * but this bean is expected to be sufficient for more remote entity link types.
 *
 * @since JIRA REST v6.5.1  (JIRA v6.1.1)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteEntityLinkJsonBean
{
    @JsonProperty
    private URI self;

    @JsonProperty
    private String name;

    @JsonProperty
    @JsonRawValue
    private String link;

    public RemoteEntityLinkJsonBean() {}

    public RemoteEntityLinkJsonBean self(URI self)
    {
        this.self = self;
        return this;
    }

    public RemoteEntityLinkJsonBean name(String name)
    {
        this.name = name;
        return this;
    }

    public RemoteEntityLinkJsonBean link(String link)
    {
        this.link = link;
        return this;
    }



    public URI getSelf()
    {
        return self;
    }

    public String getName()
    {
        return name;
    }

    public String getLink()
    {
        return link;
    }



    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final RemoteEntityLinkJsonBean that = (RemoteEntityLinkJsonBean) o;
        return Objects.equal(self, that.self) &&
               Objects.equal(name, that.name) &&
               Objects.equal(link, that.link);
    }

    @Override
    public int hashCode()
    {
        int result = self != null ? self.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 83 * result + (link != null ? link.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "RemoteEntityLinkJsonBean[self=" + self + ",name=" + name + ",link=" + link + ']';
    }
}
