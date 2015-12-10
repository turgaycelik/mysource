package com.atlassian.jira.imports.project.parser;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProjectRoleActor;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @since v3.13
 */
public class ProjectRoleActorParserImpl implements ProjectRoleActorParser
{
    private static final String ID = "id";
    private static final String PID = "pid";
    private static final String PROJECT_ROLE_ID = "projectroleid";
    private static final String ROLE_TYPE = "roletype";
    private static final String ROLE_TYPE_PARAMETER = "roletypeparameter";

    public ExternalProjectRoleActor parse(final Map attributes) throws ParseException
    {
        if (attributes == null)
        {
            throw new IllegalArgumentException("The 'attributes' parameter cannot be null");
        }
        // <ProjectRoleActor id="10010" pid="10001" projectroleid="10000" roletype="atlassian-group-role-actor" roletypeparameter="jira-users"/>
        final String id = (String) attributes.get(ID);
        final String projectId = (String) attributes.get(PID);
        final String projectRoleId = (String) attributes.get(PROJECT_ROLE_ID);
        final String roleType = (String) attributes.get(ROLE_TYPE);
        final String roleTypeParameter = (String) attributes.get(ROLE_TYPE_PARAMETER);

        // Validate the data
        if (StringUtils.isEmpty(id))
        {
            throw new ParseException("No 'id' field for ProjectRoleActor.");
        }
        if (StringUtils.isEmpty(projectRoleId))
        {
            throw new ParseException("No 'projectroleid' field for ProjectRoleActor " + id + ".");
        }
        if (StringUtils.isEmpty(roleTypeParameter))
        {
            throw new ParseException("No 'roletypeparameter' field for ProjectRoleActor " + id + ".");
        }
        if (StringUtils.isEmpty(roleType))
        {
            throw new ParseException("No 'roletype' field for ProjectRoleActor " + id + ".");
        }

        return new ExternalProjectRoleActor(id, projectId, projectRoleId, roleType, roleTypeParameter);
    }

    public EntityRepresentation getEntityRepresentation(final ExternalProjectRoleActor projectRoleActor)
    {
        final Map attributes = new HashMap();
        attributes.put(ID, projectRoleActor.getId());
        attributes.put(PID, projectRoleActor.getProjectId());
        attributes.put(PROJECT_ROLE_ID, projectRoleActor.getRoleId());
        attributes.put(ROLE_TYPE, projectRoleActor.getRoleType());
        attributes.put(ROLE_TYPE_PARAMETER, projectRoleActor.getRoleActor());

        return new EntityRepresentationImpl(ProjectRoleActorParser.PROJECT_ROLE_ACTOR_ENTITY_NAME, attributes);
    }
}
