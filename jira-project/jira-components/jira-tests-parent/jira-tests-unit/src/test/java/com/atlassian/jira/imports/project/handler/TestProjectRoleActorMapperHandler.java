package com.atlassian.jira.imports.project.handler;

import java.util.Collections;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalProjectRoleActor;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.mapper.ProjectRoleActorMapper;
import com.atlassian.jira.imports.project.parser.ProjectRoleActorParser;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Test;

/**
 * @since v3.13
 */
public class TestProjectRoleActorMapperHandler
{
    @Test
    public void testHandle() throws ParseException
    {
        final ExternalProjectRoleActor externalProjectRoleActor = new ExternalProjectRoleActor("123", "12", "3434", "role:type", "fred");

        final MockControl mockProjectRoleActorParserControl = MockControl.createStrictControl(ProjectRoleActorParser.class);
        final ProjectRoleActorParser mockProjectRoleActorParser = (ProjectRoleActorParser) mockProjectRoleActorParserControl.getMock();
        mockProjectRoleActorParser.parse(null);
        mockProjectRoleActorParserControl.setReturnValue(externalProjectRoleActor);
        mockProjectRoleActorParserControl.replay();

        final MockControl mockProjectRoleActorMapperControl = MockClassControl.createControl(ProjectRoleActorMapper.class);
        final ProjectRoleActorMapper mockProjectRoleActorMapper = (ProjectRoleActorMapper) mockProjectRoleActorMapperControl.getMock();
        mockProjectRoleActorMapper.flagValueActorAsInUse(externalProjectRoleActor);
        mockProjectRoleActorMapperControl.replay();

        final ExternalProject project = new ExternalProject();
        project.setId("12");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        ProjectRoleActorMapperHandler projectRoleActorMapperHandler = new ProjectRoleActorMapperHandler(backupProject, mockProjectRoleActorMapper)
        {
            ProjectRoleActorParser getProjectRoleActorParser()
            {
                return mockProjectRoleActorParser;
            }
        };

        projectRoleActorMapperHandler.handleEntity(ProjectRoleActorParser.PROJECT_ROLE_ACTOR_ENTITY_NAME, null);
        mockProjectRoleActorParserControl.verify();
        mockProjectRoleActorMapperControl.verify();
    }

    @Test
    public void testProjectRoleNullProject() throws ParseException
    {
        final ExternalProjectRoleActor externalProjectRoleActor = new ExternalProjectRoleActor("123", null, "3434", "role:type", "fred");

        final MockControl mockProjectRoleActorParserControl = MockControl.createStrictControl(ProjectRoleActorParser.class);
        final ProjectRoleActorParser mockProjectRoleActorParser = (ProjectRoleActorParser) mockProjectRoleActorParserControl.getMock();
        mockProjectRoleActorParser.parse(null);
        mockProjectRoleActorParserControl.setReturnValue(externalProjectRoleActor);
        mockProjectRoleActorParserControl.replay();

        final MockControl mockProjectRoleActorMapperControl = MockClassControl.createControl(ProjectRoleActorMapper.class);
        final ProjectRoleActorMapper mockProjectRoleActorMapper = (ProjectRoleActorMapper) mockProjectRoleActorMapperControl.getMock();
        mockProjectRoleActorMapperControl.replay();

        final ExternalProject project = new ExternalProject();
        project.setId("12");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        ProjectRoleActorMapperHandler projectRoleActorMapperHandler = new ProjectRoleActorMapperHandler(backupProject, mockProjectRoleActorMapper)
        {
            ProjectRoleActorParser getProjectRoleActorParser()
            {
                return mockProjectRoleActorParser;
            }
        };

        projectRoleActorMapperHandler.handleEntity(ProjectRoleActorParser.PROJECT_ROLE_ACTOR_ENTITY_NAME, null);
    }

    @Test
    public void testProjectRoleWrongEntity() throws ParseException
    {
        ProjectRoleActorMapperHandler projectRoleActorMapperHandler = new ProjectRoleActorMapperHandler(null, null);
        projectRoleActorMapperHandler.handleEntity("BSENTITY", null);
    }
    
}
