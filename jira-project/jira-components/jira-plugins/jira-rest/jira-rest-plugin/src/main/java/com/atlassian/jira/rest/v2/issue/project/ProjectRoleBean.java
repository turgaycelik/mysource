package com.atlassian.jira.rest.v2.issue.project;

import com.atlassian.jira.rest.v2.issue.Examples;
import com.atlassian.jira.util.collect.CollectionBuilder;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Collection;

/**
* @since v4.4
*/
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name="projectRole")
public class ProjectRoleBean
{
    @XmlElement
    public URI self;

    @XmlElement
    public String name;

    @XmlElement
    public Long id;

    @XmlElement
    public String description;

    @XmlElement
    public Collection<RoleActorBean> actors;

    public static final ProjectRoleBean DOC_EXAMPLE;
    static
    {
        DOC_EXAMPLE = new ProjectRoleBean();
        long id = 10360;
        DOC_EXAMPLE.self = Examples.restURI("project/MKY/role/" + id);
        DOC_EXAMPLE.id = id;
        DOC_EXAMPLE.name = "Developers";
        DOC_EXAMPLE.description = "A project role that represents developers in a project";
        DOC_EXAMPLE.actors = CollectionBuilder.list(RoleActorBean.DOC_EXAMPLE);
    }
}
