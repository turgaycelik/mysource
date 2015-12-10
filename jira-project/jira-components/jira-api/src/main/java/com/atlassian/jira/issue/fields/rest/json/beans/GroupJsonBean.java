package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.util.JiraUrlCodec;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;

/**
 * @since v5.0
 */
public class GroupJsonBean
{
    @JsonProperty
    private String name;

    @JsonProperty
    private URI self;

    public GroupJsonBean()
    {
    }

    public GroupJsonBean(String name, URI self)
    {
        this.name = name;
        this.self = self;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    public URI getSelf()
    {
        return self;
    }

    @JsonIgnore
    public void setSelf(URI self)
    {
        this.self = self;
    }

    // Docummentation
    public static final GroupJsonBean DOC_EXAMPLE = BuildDocExampleUsers("jira-administrators");

    /**
     * Creates new instance of GroupJsonBean for docummentation usage.
     * @param name group name
     * @return GroupJsonBean with name and self uri.
     */
    public static GroupJsonBean BuildDocExampleUsers(String name) {
        return new GroupJsonBean(name, URI.create("http://www.example.com/jira/rest/api/2/group?groupname=" + JiraUrlCodec.encode(name, "utf8")));
    }
}

