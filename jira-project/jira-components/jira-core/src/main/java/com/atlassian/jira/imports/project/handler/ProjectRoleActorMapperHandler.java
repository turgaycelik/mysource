package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProjectRoleActor;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.ProjectRoleActorMapper;
import com.atlassian.jira.imports.project.parser.ProjectRoleActorParser;
import com.atlassian.jira.imports.project.parser.ProjectRoleActorParserImpl;

import java.util.Map;

/**
 * Collects the ProjectRole actors for a provided project.
 *
 * @since v3.13
 */
public class ProjectRoleActorMapperHandler implements ImportEntityHandler
{
    private final BackupProject backupProject;
    private final ProjectRoleActorMapper projectRoleActorMapper;
    private ProjectRoleActorParser projectRoleActorParser;

    public ProjectRoleActorMapperHandler(final BackupProject backupProject, final ProjectRoleActorMapper projectRoleActorMapper)
    {
        this.backupProject = backupProject;
        this.projectRoleActorMapper = projectRoleActorMapper;
    }

    public void handleEntity(final String entityName, final Map<String, String> attributes) throws ParseException
    {
        if (ProjectRoleActorParser.PROJECT_ROLE_ACTOR_ENTITY_NAME.equals(entityName))
        {
            final ExternalProjectRoleActor externalProjectRoleActor = getProjectRoleActorParser().parse(attributes);
            // We should only store the role actors for the project we are interested in
            if (backupProject.getProject().getId().equals(externalProjectRoleActor.getProjectId()))
            {
                projectRoleActorMapper.flagValueActorAsInUse(externalProjectRoleActor);
            }
        }
    }

    ///CLOVER:OFF
    ProjectRoleActorParser getProjectRoleActorParser()
    {
        if (projectRoleActorParser == null)
        {
            projectRoleActorParser = new ProjectRoleActorParserImpl();
        }
        return projectRoleActorParser;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void startDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public void endDocument()
    {
    // No-op
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final ProjectRoleActorMapperHandler that = (ProjectRoleActorMapperHandler) o;

        if (backupProject != null ? !backupProject.equals(that.backupProject) : that.backupProject != null)
        {
            return false;
        }
        if (projectRoleActorMapper != null ? !projectRoleActorMapper.equals(that.projectRoleActorMapper) : that.projectRoleActorMapper != null)
        {
            return false;
        }
        if (projectRoleActorParser != null ? !projectRoleActorParser.equals(that.projectRoleActorParser) : that.projectRoleActorParser != null)
        {
            return false;
        }

        return true;
    }

    ///CLOVER:ON

    ///CLOVER:OFF
    public int hashCode()
    {
        int result;
        result = (backupProject != null ? backupProject.hashCode() : 0);
        result = 31 * result + (projectRoleActorMapper != null ? projectRoleActorMapper.hashCode() : 0);
        result = 31 * result + (projectRoleActorParser != null ? projectRoleActorParser.hashCode() : 0);
        return result;
    }
    ///CLOVER:ON
}
