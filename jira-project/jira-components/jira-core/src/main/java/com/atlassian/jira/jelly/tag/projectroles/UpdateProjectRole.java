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
 * Jelly tag for {@link com.atlassian.jira.bc.projectroles.ProjectRoleService#updateProjectRole(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.security.roles.ProjectRole, com.atlassian.jira.util.ErrorCollection)}
 */
public class UpdateProjectRole extends ProjectRoleTagSupport
{



    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);

        Long id = null;
        try
        {
            id = new Long(Long.parseLong((String) getProperties().get(KEY_PROJECTROLE_ID)));
        }
        catch(NumberFormatException nfe)
        {
            throw new JellyTagException(nfe.getLocalizedMessage(), nfe);             
        }

        String name = (String) getProperties().get(KEY_PROJECTROLE_NAME);
        String description = (String) getProperties().get(KEY_PROJECTROLE_DESCRIPTION);

        ProjectRole projectRole = new ProjectRoleImpl(id, name, description);
        projectRoleService.updateProjectRole(getUser(), projectRole, errorCollection);

        // CHECK IF ANY ERRORS WERE CAUSED BY THE SERVICE - this must be checked
        if (errorCollection.hasAnyErrors())
        {
            throw new JellyTagException(errorCollection.toString());
        }

        invokeBody(output);
    }
}
