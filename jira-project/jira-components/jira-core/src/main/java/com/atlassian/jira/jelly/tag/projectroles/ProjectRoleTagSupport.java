package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.jelly.UserAwareDynaBeanTagSupport;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

/**
 * Copyright All Rights Reserved.
 * Created: christo 29/06/2006 16:53:17
 */
public abstract class ProjectRoleTagSupport extends UserAwareDynaBeanTagSupport
{
    public static final String KEY_PROJECTROLE_ID = "projectroleid";
    public static final String KEY_PROJECTROLE_ACTORS = "actors";
    public static final String KEY_PROJECT_ID = "projectkey";
    public static final String KEY_PROJECTROLE_ACTOR_TYPE = "actortype";
    public static final String DELIMITER = ",";
    public static final String KEY_PROJECTROLE_NAME = "name";
    public static final String KEY_PROJECTROLE_DESCRIPTION = "description";

    /**
     * Takes a ',' delimited list as a String
     *
     * @param projectRoleActors
     * @return a Collection of 0 or more <String>actors. These should be actor keys (not mutable usernames)
     */
    Collection getProjectRoleActors(String projectRoleActors)
    {
        Collection actors = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(projectRoleActors, DELIMITER);
        while (tokenizer.hasMoreElements())
        {
            String actor = ((String) tokenizer.nextElement()).trim();
            actors.add(actor);
        }
        return actors;
    }

    protected Collection getActors()
    {
        Collection actors = new ArrayList();
        String projectRoleActors = (String) getProperties().get(KEY_PROJECTROLE_ACTORS);
        if (StringUtils.isNotBlank(projectRoleActors))
        {
            actors.addAll(getProjectRoleActors(projectRoleActors));
        }
        return actors;
    }

    protected Project getProject()
    {
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        Project project = null;
        String projectKey = (String) getProperties().get(KEY_PROJECT_ID);
        if (StringUtils.isNotBlank(projectKey))
        {
            project = projectManager.getProjectObjByKey(projectKey);
        }
        return project;
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
        return projectRoleService.getProjectRole(projectRoleId, errorCollection);
    }
}
