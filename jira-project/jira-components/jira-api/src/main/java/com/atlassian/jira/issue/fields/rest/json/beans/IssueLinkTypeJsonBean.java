package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.issue.link.IssueLinkType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.net.URI;

import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_NULL;

/**
 *
 * @since v4.3
 */
@JsonSerialize(include = NON_NULL)
public class IssueLinkTypeJsonBean
{
    public static IssueLinkTypeJsonBean create(IssueLinkType issueLinkType, URI self)
    {
        return new IssueLinkTypeJsonBean(issueLinkType.getId(), issueLinkType.getName(), issueLinkType.getInward(), issueLinkType.getOutward(), self);
    }

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String inward;

    @JsonProperty
    private String outward;

    @JsonProperty
    private URI self;

    public IssueLinkTypeJsonBean(){}

    public IssueLinkTypeJsonBean(Long id, String name, String inward, String outward, URI self)
    {
        this(id != null ? String.valueOf(id) : null, name, inward, outward, self);
    }

    public IssueLinkTypeJsonBean(String id, String name, String inward, String outward, URI self)
    {
        this.id = id;
        this.name = name;
        this.inward = inward;
        this.outward = outward;
        this.self = self;
    }

    public String id()
    {
        return this.id;
    }

    public IssueLinkTypeJsonBean id(String id)
    {
        return new IssueLinkTypeJsonBean(id, name, inward, outward, self);
    }

    public String name()
    {
        return this.name;
    }

    public IssueLinkTypeJsonBean name(String name)
    {
        return new IssueLinkTypeJsonBean(id, name, inward, outward, self);
    }

    public String inward()
    {
        return this.inward;
    }

    public IssueLinkTypeJsonBean inward(String inward)
    {
        return new IssueLinkTypeJsonBean(id, name, inward, outward, self);
    }

    public String outward()
    {
        return this.outward;
    }

    public IssueLinkTypeJsonBean outward(String outward)
    {
        return new IssueLinkTypeJsonBean(id, name, inward, outward, self);
    }

    public URI self()
    {
        return this.self;
    }

    public IssueLinkTypeJsonBean self(URI self)
    {
        return new IssueLinkTypeJsonBean(id, name, inward, outward, self);
    }
}
