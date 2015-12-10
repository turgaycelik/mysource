package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;

import java.util.Collection;

/**
 * Jelly Tag that will add 'actors' to a ProjectRole. actors can be defined as groups or users
 * currently.
 * Copyright All Rights Reserved.
 * Created: christo 29/06/2006 12:14:58
 */
public class AddActorsToProjectRole extends ProjectRoleTagSupport
{
    public void doTag(XMLOutput xmlOutput) throws MissingAttributeException, JellyTagException
    {

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRoleService projectRoleService = ComponentAccessor.getComponent(ProjectRoleService.class);

        // get poject role as name
        ProjectRole projectRole = getProjectRole(errorCollection);

        // get projectrole actors delimited by ,
        Collection actors = getActors();

        // get project by key
        Project project = getProject();

        // specify the actor type
        String actorType = (String) getProperties().get(KEY_PROJECTROLE_ACTOR_TYPE);

        projectRoleService.addActorsToProjectRole(actors, projectRole, project, actorType, errorCollection);

        // CHECK IF ANY ERRORS WERE CAUSED BY THE SERVICE - this must be checked
        if (errorCollection.hasAnyErrors())
        {
            throw new JellyTagException(errorCollection.toString());
        }

        invokeBody(xmlOutput);
    }

}
