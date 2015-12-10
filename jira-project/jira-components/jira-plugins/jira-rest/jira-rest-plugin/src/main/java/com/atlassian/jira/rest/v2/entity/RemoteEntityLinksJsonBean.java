package com.atlassian.jira.rest.v2.entity;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents a collection of remote entity links
 *
 * @since JIRA REST v6.5.1  (JIRA v6.1.1)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteEntityLinksJsonBean
{
    @JsonProperty
    private List<RemoteEntityLinkJsonBean> links;

    public RemoteEntityLinksJsonBean() {}

    public RemoteEntityLinksJsonBean links(List<RemoteEntityLinkJsonBean> links)
    {
        this.links = links;
        return this;
    }

    public List<RemoteEntityLinkJsonBean> getLinks()
    {
        return links;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final RemoteEntityLinksJsonBean that = (RemoteEntityLinksJsonBean) o;
        return (links != null) ? links.equals(that.links) : (that.links == null);
    }

    @Override
    public int hashCode()
    {
        return links != null ? links.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return "RemoteEntityLinksJsonBean[links=" + links + ']';
    }
}
