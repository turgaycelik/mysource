package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;

import java.util.Collection;

/**
 * Jelly tag for {@link com.atlassian.jira.bc.projectroles.ProjectRoleService#removeDefaultActorsFromProjectRole(com.atlassian.crowd.embedded.api.User, java.util.Collection, com.atlassian.jira.security.roles.ProjectRole, String, com.atlassian.jira.util.ErrorCollection)}
 */
public class RemoveDefaultActorsFromProjectRole extends ProjectRoleTagSupport
{
    public void doTag(XMLOutput xmlOutput) throws MissingAttributeException, JellyTagException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);

        // get poject role
        ProjectRole projectRole = getProjectRole(errorCollection);

        // get projectrole actors
        Collection actors = getActors();

        // specify the actor type
        String actorType = (String) getProperties().get(KEY_PROJECTROLE_ACTOR_TYPE);

        projectRoleService.removeDefaultActorsFromProjectRole(getUser(), actors, projectRole, actorType, errorCollection);

        // CHECK IF ANY ERROR WERE CAUSED BY THE SERVICE - this must be checked
        if (errorCollection.hasAnyErrors())
        {
            throw new JellyTagException(errorCollection.toString());
        }

        invokeBody(xmlOutput);
    }
}
