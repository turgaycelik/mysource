package com.atlassian.jira.rest.v2.issue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
* @since v4.2
*/
@XmlRootElement (name="link")
public class Link
{
    @XmlElement
    private String rel;

    @XmlElement
    private URI href;

    public Link() {}
    public Link(final String rel, final URI href)
    {
        this.rel = rel;
        this.href = href;
    }

    public static Link self(final URI href)
    {
        return new Link("self", href);
    }
}
