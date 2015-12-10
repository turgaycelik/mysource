package com.atlassian.jira.rest.v2.issue.project;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * A bean used when updating the role actors through
 * {@link com.atlassian.jira.rest.v2.issue.project.ProjectRoleResource#setActors(String, Long, ProjectRoleActorsUpdateBean)}
 * as we may not have enough information to fully populate a ProjectRoleBean when doing an update, hence only a reduced
 * set of data consisting of {actor-type -> actor-parameter} is required for this bean.
 *
 */
@XmlRootElement(name = "actor")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectRoleActorsUpdateBean
{
    private Long id;
    private Map<String, String[]> categorisedActors;

    public ProjectRoleActorsUpdateBean()
    {
    }

    public ProjectRoleActorsUpdateBean(final Long id, final Map<String, String[]> categorisedActors)
    {
        this.id = id;
        this.categorisedActors = categorisedActors;
    }

    public Long getId()
    {
        return id;
    }

    public Map<String, String[]> getCategorisedActors()
    {
        return categorisedActors;
    }
}
