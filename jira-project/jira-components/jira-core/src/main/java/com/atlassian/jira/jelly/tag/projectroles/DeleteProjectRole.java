package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This Jelly Tag will delete a project role from the system.
 * Copyright All Rights Reserved.
 * Created: christo 30/06/2006 08:44:54
 */
public class DeleteProjectRole extends ProjectRoleTagSupport
{
    private static final String KEY_PROJECTROLE_CONFIRM_FLAG = "confirm";

    public void doTag(XMLOutput xmlOutput) throws MissingAttributeException, JellyTagException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);

        ProjectRole projectRole = getProjectRole(errorCollection);

        boolean confirm = getConfirmFlag();

        // If they are not passing the force flag then make sure there are no associations, throw up an exception
        // if there are.
        if(!confirm)
        {
            checkIfRoleHasAssociations(projectRole, errorCollection, projectRoleService);
        }

        projectRoleService.deleteProjectRole(projectRole, errorCollection);

        // CHECK IF ANY ERROR WERE CAUSED BY THE SERVICE - this must be checked
        if (errorCollection.hasAnyErrors())
        {
            throw new JellyTagException(errorCollection.toString());
        }

        invokeBody(xmlOutput);
    }

    private boolean getConfirmFlag()
    {
        String confirmFlag = (String) getProperties().get(KEY_PROJECTROLE_CONFIRM_FLAG);
        return Boolean.valueOf(confirmFlag).booleanValue();
    }

    private void checkIfRoleHasAssociations(ProjectRole projectRole, SimpleErrorCollection errorCollection, ProjectRoleService projectRoleService)
    {
        Collection associatedSchemes = new ArrayList();
        associatedSchemes.addAll(projectRoleService.getAssociatedNotificationSchemes(getUser(), projectRole, errorCollection));
        associatedSchemes.addAll(projectRoleService.getAssociatedPermissionSchemes(getUser(), projectRole, errorCollection));

        StringBuilder message = new StringBuilder(32);

        if (!associatedSchemes.isEmpty())
        {
            message.append("Project Role: ").append(projectRole.getName());
            message.append(" is associated with the following scheme(s): ");
            for (Iterator iterator = associatedSchemes.iterator(); iterator.hasNext();)
            {
                GenericValue scheme = (GenericValue) iterator.next();
                message.append(scheme.getString("name"));
                if (iterator.hasNext())
                {
                    message.append(", ");
                }
            }
            message.append(".");
        }
        message.append("To force deletion of this role make the confirm parameter 'true'.");
        errorCollection.addErrorMessage(message.toString());

    }

}
