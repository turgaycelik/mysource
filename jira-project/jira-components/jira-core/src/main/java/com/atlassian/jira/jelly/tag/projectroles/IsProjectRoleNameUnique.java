package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;

/**
 * Jelly tag that provides jelly access to {@link ProjectRoleService#isProjectRoleNameUnique(String, com.atlassian.jira.util.ErrorCollection)}
 */
public class IsProjectRoleNameUnique extends ProjectRoleTagSupport
{
    private static final String KEY_PROJECTROLE_NAME = "name";

    public void doTag(XMLOutput xmlOutput) throws MissingAttributeException, JellyTagException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);
        String name = (String) getProperties().get(KEY_PROJECTROLE_NAME);

        boolean isUnique = projectRoleService.isProjectRoleNameUnique(name, errorCollection);
        // CHECK IF ANY ERRORS WERE CAUSED BY THE SERVICE - this must be checked
        if (errorCollection.hasAnyErrors())
        {
            throw new JellyTagException(errorCollection.toString());
        }

        String variableName = (String) getProperties().get(KEY_VARIABLE_NAME);
        getContext().setVariable(variableName, Boolean.valueOf(isUnique));
        invokeBody(xmlOutput);
    }
}
