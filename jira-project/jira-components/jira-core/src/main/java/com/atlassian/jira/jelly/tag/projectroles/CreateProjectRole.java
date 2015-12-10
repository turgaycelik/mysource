package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;

/**
 * Creates a ProjectRole, a Jelly front end to
 * {@link ProjectRoleService#createProjectRole(com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.util.ErrorCollection)} .
 */
public class CreateProjectRole  extends ProjectRoleTagSupport
{

    public void doTag(XMLOutput xmlOutput) throws MissingAttributeException, JellyTagException
    {
        ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);

        SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        String name = (String) getProperties().get(KEY_PROJECTROLE_NAME);
        String description = (String) getProperties().get(KEY_PROJECTROLE_DESCRIPTION);

        ProjectRole projectRole = new ProjectRoleImpl(name, description);
        projectRole = projectRoleService.createProjectRole(projectRole, errorCollection);

        if (errorCollection.hasAnyErrors())
        {
            throw new JellyTagException(errorCollection.toString());
        }

        // If we are all happy set the values in the context
        getContext().setVariable("jelly.role.id", projectRole.getId());
        getContext().setVariable("jelly.role.name", projectRole.getName());
        getContext().setVariable("jelly.role.description", projectRole.getDescription());

        invokeBody(xmlOutput);
    }
}
