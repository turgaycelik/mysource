package com.atlassian.jira.jelly.tag.admin;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jelly.UserAwareDynaBeanTagSupport;
import com.atlassian.jira.jelly.tag.projectroles.ProjectRoleTagSupport;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Copyright All Rights Reserved.
 * Created: christo 3/07/2006 12:18:39
 */
public class GetAssociatedSchemes extends UserAwareDynaBeanTagSupport
{
    public static final String KEY_SCHEME_TYPE = "schemetype";

    public static final String SCHEME_TYPE_PERMISSION = "permission";

    public static final String SCHEME_TYPE_NOTIFICATION = "notification";


    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException
    {
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);

        ProjectRole projectRole = getProjectRole(errorCollection);

        // get the scheme type
        String schemeType = (String) getProperties().get(KEY_SCHEME_TYPE);

        Collection schemes = new ArrayList();

        if (SCHEME_TYPE_NOTIFICATION.equals(schemeType))
        {
            schemes.addAll(projectRoleService.getAssociatedNotificationSchemes(getUser(), projectRole, errorCollection));
        }
        else if(SCHEME_TYPE_PERMISSION.equals(schemeType))
        {
            schemes.addAll(projectRoleService.getAssociatedPermissionSchemes(getUser(), projectRole, errorCollection));
        }

        // CHECK IF ANY ERROR WERE CAUSED BY THE SERVICE - this must be checked
        if (errorCollection.hasAnyErrors())
        {
            throw new JellyTagException(errorCollection.toString());
        }

        String variableName = (String) getProperties().get(KEY_VARIABLE_NAME);
        getContext().setVariable(variableName, schemes);
        invokeBody(output);
    }

    protected ProjectRole getProjectRole(SimpleErrorCollection errorCollection)
    {
        ProjectRoleService projectRoleService = ComponentAccessor.getComponentOfType(ProjectRoleService.class);
        String projectRoleIdAsString = (String) getProperties().get(ProjectRoleTagSupport.KEY_PROJECTROLE_ID);
        Long projectRoleId = null;
        if (StringUtils.isNotBlank(projectRoleIdAsString))
        {
            projectRoleId = new Long(projectRoleIdAsString);
        }
        return projectRoleService.getProjectRole(getUser(), projectRoleId, errorCollection);
    }

}
