package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.external.beans.ExternalProjectRoleActor;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.imports.project.core.ProjectImportOptionsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.mapper.ProjectRoleActorMapper;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetAssert;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.classextension.EasyMock;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

/**
 * @since v3.13
 */
public class TestProjectRoleActorMapperValidatorImpl
{
    @Test
    public void testValidateUnknownRoleType() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        projectImportMapper.getProjectRoleActorMapper().flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "Dog", "Rover"));

        projectImportMapper.getProjectRoleMapper().registerOldValue("12", "Dudes");

        ProjectRoleActorMapperValidatorImpl validator = new ProjectRoleActorMapperValidatorImpl(null);
        MessageSet messageSet = validator.validateProjectRoleActors(new MockI18nBean(), projectImportMapper, new ProjectImportOptionsImpl("", "", true));
        MessageSetAssert.assert1Warning(messageSet, "Project role 'Dudes' contains an actor 'Rover' of unknown role type 'Dog'. This actor will not be added to the project role.");
    }

    @Test
    public void testValidateWithDontUpdateDetailsOption() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        projectImportMapper.getProjectRoleActorMapper().flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "Dog", "Rover"));

        projectImportMapper.getProjectRoleMapper().registerOldValue("12", "Dudes");

        ProjectRoleActorMapperValidatorImpl validator = new ProjectRoleActorMapperValidatorImpl(null);
        // If the USer chooses "Don't update project details, then we don't change the role memberships,and therefore we have no validation.
        MessageSet messageSet = validator.validateProjectRoleActors(new MockI18nBean(), projectImportMapper, new ProjectImportOptionsImpl("", "", false));
        MessageSetAssert.assertNoMessages(messageSet);
    }

    @Test
    public void testValidateMissingGroup() throws Exception
    {
        final GroupManager mockGroupManager = EasyMock.createNiceMock(GroupManager.class);
        expect(mockGroupManager.groupExists("goodies")).andReturn(true);
        expect(mockGroupManager.groupExists("baddies")).andReturn(false);
        replay(mockGroupManager);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, mockGroupManager);

        final ProjectRoleActorMapper projectRoleActorMapper = projectImportMapper.getProjectRoleActorMapper();
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-group-role-actor", "goodies"));
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-group-role-actor", "baddies"));

        projectImportMapper.getProjectRoleMapper().registerOldValue("12", "Dudes");

        ProjectRoleActorMapperValidatorImpl validator = new ProjectRoleActorMapperValidatorImpl(null);
        MessageSet messageSet = validator.validateProjectRoleActors(new MockI18nBean(), projectImportMapper, new ProjectImportOptionsImpl("", "", true));
        MessageSetAssert.assert1Warning(messageSet, "Project role 'Dudes' contains a group 'baddies' that doesn't exist in the current system. This group will not be added to the project role membership.");

        verify(mockGroupManager);
    }

    @Test
    public void testValidateMissingUsersExternalUserManagement() throws Exception
    {
        final ExternalUser peter = new ExternalUser("peterKey", "peter", "Peter", "", "");

        final UserUtil mockUserUtil = EasyMock.createMock(UserUtil.class);
        expect(mockUserUtil.userExists("peter")).andReturn(true);
        expect(mockUserUtil.userExists("paul")).andReturn(false);
        replay(mockUserUtil);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(mockUserUtil, null);
        projectImportMapper.getUserMapper().registerOldValue(peter);

        final ProjectRoleActorMapper projectRoleActorMapper = projectImportMapper.getProjectRoleActorMapper();
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-user-role-actor", "peterKey"));
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-user-role-actor", "paul"));

        projectImportMapper.getProjectRoleMapper().registerOldValue("12", "Dudes");

        final MockUserManager userManager = new MockUserManager();
        ProjectRoleActorMapperValidatorImpl validator = new ProjectRoleActorMapperValidatorImpl(userManager);
        MessageSet messageSet = validator.validateProjectRoleActors(new MockI18nBean(), projectImportMapper, new ProjectImportOptionsImpl("", "", true));
        MessageSetAssert.assert1Warning(messageSet, "Project role 'Dudes' contains a user 'paul' that doesn't exist in the current system. This user will not be added to the project role membership.");

        verify(mockUserUtil);
    }

    @Test
    public void testValidateMissingUsersJiraUserManagement() throws Exception
    {
        final UserUtil mockUserUtil = EasyMock.createMock(UserUtil.class);

        expect(mockUserUtil.userExists("peter")).andReturn(true);
        expect(mockUserUtil.userExists("paul")).andReturn(false);
        expect(mockUserUtil.userExists("mary")).andReturn(false);

        replay(mockUserUtil);

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(mockUserUtil, null);

        final ProjectRoleActorMapper projectRoleActorMapper = projectImportMapper.getProjectRoleActorMapper();
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-user-role-actor", "peterKey"));
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-user-role-actor", "paulKey"));
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-user-role-actor", "mary"));

        projectImportMapper.getProjectRoleMapper().registerOldValue("12", "Dudes");

        // Make it that paul can be auto-created.
        final ExternalUser paul = new ExternalUser("paulKey", "paul", "Paul", "", "");
        final ExternalUser peter = new ExternalUser("peterKey", "peter", "Peter", "", "");
        projectImportMapper.getUserMapper().registerOldValue(paul);
        projectImportMapper.getUserMapper().registerOldValue(peter);

        ProjectRoleActorMapperValidatorImpl validator = new ProjectRoleActorMapperValidatorImpl(new MockUserManager());
        MessageSet messageSet = validator.validateProjectRoleActors(new MockI18nBean(), projectImportMapper, new ProjectImportOptionsImpl("", "", true));
        MessageSetAssert.assert1Warning(messageSet, "Project role 'Dudes' contains a user 'mary' that doesn't exist in the current system. This user will not be added to the project role membership.");

        verify(mockUserUtil);
    }
}
