package com.atlassian.jira.issue.fields.rest.json.beans;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_NULL;

/**
 * This bean holds the information that is reported for each issue link.
 *
 * @since v4.2
 */
@JsonSerialize(include = NON_NULL)
@XmlRootElement (name = "issueLinks")
public class IssueLinkJsonBean
{
    @JsonProperty
    private String id;

    @XmlElement
    private URI self;

    @XmlElement (name = "type")
    private IssueLinkTypeJsonBean type;

    @JsonProperty
    private IssueRefJsonBean inwardIssue;

    @JsonProperty
    private IssueRefJsonBean outwardIssue;

    public IssueLinkJsonBean()
    {
    }

    public IssueLinkJsonBean(IssueLinkTypeJsonBean type, IssueRefJsonBean inwardIssue, IssueRefJsonBean outwardIssue, String id)
    {
        this.type = type;
        this.inwardIssue = inwardIssue;
        this.outwardIssue = outwardIssue;
        this.id = id;
    }

    public IssueLinkTypeJsonBean type()
    {
        return this.type;
    }

    public String getId()
    {
        return id;
    }

    public IssueLinkJsonBean id(String id)
    {
        this.id = id;
        return this;
    }

    public IssueLinkJsonBean self(URI selfUri)
    {
        this.self = selfUri;
        return this;
    }

    public IssueLinkJsonBean type(IssueLinkTypeJsonBean type)
    {
        this.type = type;
        return this;
    }

    public IssueRefJsonBean inwardIssue()
    {
        return this.inwardIssue;
    }

    public IssueLinkJsonBean inwardIssue(IssueRefJsonBean inwardIssue)
    {
        this.inwardIssue = inwardIssue;
        return this;
    }

    public IssueRefJsonBean outwardIssue()
    {
        return this.outwardIssue;
    }

    public IssueLinkJsonBean outwardIssue(IssueRefJsonBean outwardIssue)
    {
        this.outwardIssue = outwardIssue;
        return this;
    }
}
