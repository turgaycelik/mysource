package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;

import java.util.Collection;

/**
 * Copyright All Rights Reserved.
 * Created: christo 28/06/2006 11:53:55
 */
public class GetProjectRoles extends ProjectRoleTagSupport
{

    public void doTag(XMLOutput xmlOutput) throws MissingAttributeException, JellyTagException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);
        Collection roles = projectRoleService.getProjectRoles(errorCollection);

        // CHECK IF ANY ERROR WERE CAUSED BY THE SERVICE - this must be checked
        if (errorCollection.hasAnyErrors())
        {
            throw new JellyTagException(errorCollection.toString());
        }

        String variableName = (String) getProperties().get(KEY_VARIABLE_NAME);
        getContext().setVariable(variableName, roles);
        invokeBody(xmlOutput);
    }
}
