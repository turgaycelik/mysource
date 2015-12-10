package com.atlassian.jira.imports.project;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserWithAttributes;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentImpl;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.external.ExternalException;
import com.atlassian.jira.external.ExternalUtils;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalIssueImpl;
import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.external.beans.ExternalVoter;
import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.imports.project.parser.UserAssociationParser;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressInterval;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.util.IssueObjectIssuesIterable;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectKeys;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.web.util.AttachmentException;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.memory.MemoryPropertySet;

import org.easymock.MockControl;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.MockClassControl;
import org.easymock.internal.AlwaysMatcher;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.model.ModelEntity;
import org.ofbiz.core.entity.model.ModelField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestDefaultProjectImportPersister
{
    private ApplicationProperties applicationProperties;

    @Before
    public void setUp()
    {
        new MockComponentWorker().init();
    }

    @Test
    public void testUpdateProjectDetailsProjectGoneMissing()
    {
        // ExternalProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("MNK");
        externalProject.setAssigneeType("Monkey God");
        externalProject.setDescription("Great sage, equal of heaven.");
        externalProject.setEmailSender("monkey@baboon.com");
        externalProject.setLead("Mr Bubbles");
        externalProject.setName("Monkey");
        externalProject.setUrl("http://www.monkey.com");

        // Mock ProjectManager
        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectObjByKey("MNK");
        mockProjectManagerControl.setReturnValue(null);
        mockProjectManagerControl.replay();

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null,
            mockProjectManager, null, null, null, null, null, null, null, null, applicationProperties, null);
        try
        {
            defaultProjectImportPersister.updateProjectDetails(externalProject);
            fail("Error expected.");
        }
        catch (final IllegalStateException ex)
        {
            // Yay!
        }

        mockProjectManagerControl.verify();
    }

    @Test
    public void testUpdateProjectDetails()
    {
        // ExternalProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("MKY");
        externalProject.setAssigneeType(String.valueOf(AssigneeTypes.PROJECT_LEAD));
        externalProject.setDescription("Great sage, equal of heaven.");
        externalProject.setEmailSender("monkey@baboon.com");
        externalProject.setLead("Mr Bubbles");
        externalProject.setName("Monkey");
        externalProject.setUrl("http://www.monkey.com");

        MockProjectManager mockProjectManager = new MockProjectManager();
        mockProjectManager.addProject(new MockProject(12, "MKY"));

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null,
            mockProjectManager, null, null, null, null, null, null, null, null, applicationProperties, null)
        {
            @Override
            void setEmailSenderOnProject(final Project project, final String emailSender)
            {
                assertEquals("monkey@baboon.com", emailSender);
            }
        };

        defaultProjectImportPersister.updateProjectDetails(externalProject);
        Project project = mockProjectManager.getProjectObjByKey("MKY");
        assertEquals(new Long(12), project.getId());
        assertEquals("MKY", project.getKey());
        assertEquals(AssigneeTypes.PROJECT_LEAD, project.getAssigneeType().longValue());
        assertEquals("Great sage, equal of heaven.", project.getDescription());
        assertEquals("Mr Bubbles", project.getLeadUserName());
        assertEquals("Monkey", project.getName());
        assertEquals("http://www.monkey.com", project.getUrl());
    }

    @Test
    public void testUpdateProjectDetailsNullAssigneeTypeUnassignedOff() throws Exception
    {
        _testUpdateProjectDetailsNullAssigneeType(String.valueOf(AssigneeTypes.PROJECT_LEAD), false);
    }

    @Test
    public void testUpdateProjectDetailsNullAssigneeTypeUnassignedOn() throws Exception
    {
        _testUpdateProjectDetailsNullAssigneeType(String.valueOf(AssigneeTypes.UNASSIGNED), true);
    }

    private void _testUpdateProjectDetailsNullAssigneeType(final String expectedAssigneeType, final boolean isUnassignedIssuesAllowed)
    {
        // ExternalProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setKey("MKY");
        externalProject.setAssigneeType(null);
        externalProject.setDescription("Great sage, equal of heaven.");
        externalProject.setEmailSender("monkey@baboon.com");
        externalProject.setLead("Mr Bubbles");
        externalProject.setName("Monkey");
        externalProject.setUrl("http://www.monkey.com");

        // Mock ProjectManager
        MockProjectManager mockProjectManager = new MockProjectManager();
        mockProjectManager.addProject(new MockProject(12, "MKY"));

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null,
            mockProjectManager, null, null, null, null, null, null, null, null, applicationProperties, null)
        {
            @Override
            void setEmailSenderOnProject(final Project project, final String emailSender)
            {
                assertEquals("monkey@baboon.com", emailSender);
            }

            @Override
            boolean isUnassignedIssuesAllowed()
            {
                return isUnassignedIssuesAllowed;
            }
        };

        defaultProjectImportPersister.updateProjectDetails(externalProject);
        // Assert update
        Project project = mockProjectManager.getProjectObjByKey("MKY");
        assertEquals(new Long(12), project.getId());
        assertEquals("MKY", project.getKey());
        if (isUnassignedIssuesAllowed)
        {
            assertEquals(AssigneeTypes.UNASSIGNED, project.getAssigneeType().longValue());
        }
        else
        {
            assertEquals(AssigneeTypes.PROJECT_LEAD, project.getAssigneeType().longValue());
        }
        assertEquals("Great sage, equal of heaven.", project.getDescription());
        assertEquals("Mr Bubbles", project.getLeadUserName());
        assertEquals("Monkey", project.getName());
        assertEquals("http://www.monkey.com", project.getUrl());                
    }

    @Test
    public void testCreateUser() throws Exception
    {
        final MockCrowdService crowdService = new MockCrowdService();
        ComponentAccessor.initialiseWorker(new MockComponentWorker().addMock(CrowdService.class, crowdService));

        final ExternalUser externalUser = new ExternalUser("fred", "Freddy Kruger", "fk@you", "dude");
        externalUser.setKey("fred");
        externalUser.setPasswordHash("XYZ");
        externalUser.getUserPropertyMap().put("colour", "green");

        final MockUser fredUser = new MockUser("fred");
        // Mock UserUtil
        final MockControl mockUserUtilControl = MockControl.createStrictControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.addToJiraUsePermission(fredUser);
        mockUserUtilControl.replay();

        UserPropertyManager mockUserPropertyManager = EasyMock.createMock(UserPropertyManager.class);
        MemoryPropertySet propertySet = new MemoryPropertySet();
        propertySet.init(null, null);

        final UserMapper userMapper = EasyMock.createMock(UserMapper.class);

        EasyMock.expect(mockUserPropertyManager.getPropertySet(fredUser)).andReturn(propertySet);
//        userMapper.mapUserKey("fred", "fred");
//        EasyMock.expectLastCall();

        EasyMock.replay(mockUserPropertyManager, userMapper);

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(mockUserUtil, null, null, null, null,
            null, null, null, null, null, null, null, null, null, crowdService, applicationProperties, mockUserPropertyManager);

        defaultProjectImportPersister.createUser(userMapper, externalUser);

        UserWithAttributes createdUser = crowdService.getUserWithAttributes("fred");
        assertNotNull(createdUser);
        assertEquals("Freddy Kruger", createdUser.getDisplayName());
        assertEquals("fk@you", createdUser.getEmailAddress());

        assertEquals("green", propertySet.getString("jira.meta.colour"));
        // Verify Mock UserUtil
        mockUserUtilControl.verify();
    }

    @Test
    public void testCreateUserUserAlreadyExists() throws Exception
    {
        final MockUser fredUser = new MockUser("fred");
        CrowdService crowdService = EasyMock.createStrictMock(CrowdService.class);
        EasyMock.expect(crowdService.getUser("fred")).andReturn(fredUser);
        final UserMapper userMapper = EasyMock.createMock(UserMapper.class);
        EasyMock.replay(crowdService, userMapper);

        final ExternalUser user = new ExternalUser("fred", "Freddy Kruger", "fk@you", "dude");
        user.getUserPropertyMap().put("colour", "green");

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, crowdService, applicationProperties, null);

        assertTrue(defaultProjectImportPersister.createUser(userMapper, user));

        // Verify Mocks
        EasyMock.verify(crowdService);
    }

    @Test
    public void testCreateUserUserNull() throws Exception
    {
        CrowdService crowdService = EasyMock.createStrictMock(CrowdService.class);
        EasyMock.expect(crowdService.getUser("fred")).andReturn(null);
        EasyMock.expect(crowdService.addUser(EasyMock.<com.atlassian.crowd.embedded.api.User>anyObject(), EasyMock.<String>anyObject() )).andReturn(null);
        final UserMapper userMapper = EasyMock.createMock(UserMapper.class);
        EasyMock.replay(crowdService, userMapper);

        final ExternalUser user = new ExternalUser("fred", "Freddy Kruger", "fk@you", "dude");
        user.getUserPropertyMap().put("colour", "green");

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, crowdService, applicationProperties, null);

        assertFalse(defaultProjectImportPersister.createUser(userMapper, user));

        // Verify Mocks
        EasyMock.verify();
    }

    @Test
    public void testCreateComponents() throws Exception
    {
        final ExternalComponent component1 = new ExternalComponent();
        component1.setId("1");
        component1.setName("Component1");
        component1.setDescription("Component 1 Description");
        component1.setLead("admin");
        component1.setAssigneeType("2");

        final ExternalComponent component2 = new ExternalComponent();
        component2.setId("2");
        component2.setName("Component2");
        component2.setDescription("Component 2 Description");
        component2.setLead("admin");
        component2.setAssigneeType("2");

        final ExternalComponent component3 = new ExternalComponent();
        component3.setId("3");
        component3.setName("Component3");
        component3.setDescription("Component 3 Description");
        component3.setLead("admin");
        component3.setAssigneeType("2");

        final List components = EasyList.build(component1, component2, component3);

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, components, Collections.EMPTY_LIST,
            EasyList.build(new Long(12), new Long(14)));

        final MockControl mockProjectControl = MockControl.createStrictControl(Project.class);
        final Project mockProject = (Project) mockProjectControl.getMock();
        mockProject.getId();
        mockProjectControl.setReturnValue(new Long(12));
        mockProjectControl.replay();

        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectObjByKey("TST");
        mockProjectManagerControl.setReturnValue(mockProject);
        mockProjectManagerControl.replay();

        final MockControl mockProjectComponentManagerControl = MockControl.createStrictControl(ProjectComponentManager.class);
        final ProjectComponentManager mockProjectComponentManager = (ProjectComponentManager) mockProjectComponentManagerControl.getMock();
        mockProjectComponentManager.create("Component1", "Component 1 Description", "admin", 2, new Long(12));
        mockProjectComponentManagerControl.setReturnValue(new ProjectComponentImpl("", "", "", 2));
        mockProjectComponentManager.create("Component2", "Component 2 Description", "admin", 2, new Long(12));
        mockProjectComponentManagerControl.setReturnValue(new ProjectComponentImpl("", "", "", 2));
        mockProjectComponentManager.create("Component3", "Component 3 Description", "admin", 2, new Long(12));
        mockProjectComponentManagerControl.setReturnValue(new ProjectComponentImpl("", "", "", 2));
        mockProjectComponentManagerControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null,
            mockProjectManager, null, null, null, mockProjectComponentManager, null, null, null, null, applicationProperties, null);

        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        EasyMock.expect(userUtil.getUserByName("admin")).andReturn(new MockApplicationUser("admin")).anyTimes();
        EasyMock.replay(userUtil);

        final GroupManager groupManager = EasyMock.createMock(GroupManager.class);
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, groupManager);
        final Map newComponents = projectImportPersister.createComponents(backupProject, projectImportMapper);

        assertEquals(3, newComponents.size());
        assertTrue(newComponents.keySet().contains("1"));
        assertTrue(newComponents.keySet().contains("2"));
        assertTrue(newComponents.keySet().contains("3"));

        mockProjectComponentManagerControl.verify();
        mockProjectManagerControl.verify();
        mockProjectControl.verify();
    }

    @Test
    public void testCreateComponentsNoProject() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            EasyList.build(new Long(12), new Long(14)));

        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectObjByKey("TST");
        mockProjectManagerControl.setReturnValue(null);
        mockProjectManagerControl.replay();

        final MockControl mockProjectComponentManagerControl = MockControl.createStrictControl(ProjectComponentManager.class);
        final ProjectComponentManager mockProjectComponentManager = (ProjectComponentManager) mockProjectComponentManagerControl.getMock();
        mockProjectComponentManagerControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null,
            mockProjectManager, null, null, null, mockProjectComponentManager, null, null, null, null, applicationProperties, null);

        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final GroupManager groupManager = EasyMock.createMock(GroupManager.class);
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, groupManager);
        try
        {
            projectImportPersister.createComponents(backupProject, projectImportMapper);
            fail("Should have thrown IllegalStateException");
        }
        catch (final Exception e)
        {
            // expected
        }

        mockProjectComponentManagerControl.verify();
        mockProjectManagerControl.verify();
    }

    @Test
    public void testCreateComponentsNoAssigneeType() throws Exception
    {
        final ExternalComponent component1 = new ExternalComponent();
        component1.setId("1");
        component1.setName("Component1");
        component1.setDescription("Component 1 Description");
        component1.setLead("admin");
        component1.setAssigneeType(null);

        final List components = EasyList.build(component1);

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, components, Collections.EMPTY_LIST,
            EasyList.build(new Long(12), new Long(14)));

        final MockControl mockProjectControl = MockControl.createStrictControl(Project.class);
        final Project mockProject = (Project) mockProjectControl.getMock();
        mockProject.getId();
        mockProjectControl.setReturnValue(new Long(12));
        mockProjectControl.replay();

        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectObjByKey("TST");
        mockProjectManagerControl.setReturnValue(mockProject);
        mockProjectManagerControl.replay();

        final MockControl mockProjectComponentManagerControl = MockControl.createStrictControl(ProjectComponentManager.class);
        final ProjectComponentManager mockProjectComponentManager = (ProjectComponentManager) mockProjectComponentManagerControl.getMock();
        mockProjectComponentManager.create("Component1", "Component 1 Description", "admin", AssigneeTypes.PROJECT_DEFAULT, new Long(12));
        mockProjectComponentManagerControl.setReturnValue(new ProjectComponentImpl("", "", "", 0));
        mockProjectComponentManagerControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null,
            mockProjectManager, null, null, null, mockProjectComponentManager, null, null, null, null, applicationProperties, null);

        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final GroupManager groupManager = EasyMock.createMock(GroupManager.class);
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, groupManager);

        final Map newComponents = projectImportPersister.createComponents(backupProject, projectImportMapper);

        assertEquals(1, newComponents.size());
        assertTrue(newComponents.keySet().contains("1"));
        assertEquals(AssigneeTypes.PROJECT_DEFAULT, ((ProjectComponent) newComponents.get("1")).getAssigneeType());

        mockProjectComponentManagerControl.verify();
        mockProjectManagerControl.verify();
        mockProjectControl.verify();
    }

    @Test
    public void testCreateVersions() throws Exception
    {
        final ExternalVersion version1 = new ExternalVersion();
        version1.setId("1");
        version1.setName("Version1");
        version1.setDescription("Version 1 Description");
        final Date version1Date = new Date();
        version1.setReleaseDate(version1Date);
        version1.setSequence(new Long(5));
        version1.setArchived(true);

        final ExternalVersion version2 = new ExternalVersion();
        version2.setId("2");
        version2.setName("Version2");
        version2.setDescription("Version 2 Description");
        final Date version2Date = new Date();
        version2.setReleaseDate(version2Date);
        version2.setSequence(new Long(1));
        version2.setReleased(true);

        final ExternalVersion version3 = new ExternalVersion();
        version3.setId("3");
        version3.setName("Version3");
        version3.setDescription("Version 3 Description");
        final Date version3Date = new Date();
        version3.setReleaseDate(version3Date);
        version3.setSequence(new Long(4));

        final List versions = EasyList.build(version1, version2, version3);

        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, versions, Collections.EMPTY_LIST, Collections.EMPTY_LIST, EasyList.build(
            new Long(12), new Long(14)));

        final MockControl mockProjectControl = MockControl.createStrictControl(Project.class);
        final Project mockProject = (Project) mockProjectControl.getMock();
        mockProject.getId();
        mockProjectControl.setReturnValue(new Long(12));
        mockProjectControl.replay();

        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectObjByKey("TST");
        mockProjectManagerControl.setReturnValue(mockProject);
        mockProjectManagerControl.replay();

        final MockControl mockVersionManagerControl = MockControl.createStrictControl(VersionManager.class);
        final VersionManager mockVersionManager = (VersionManager) mockVersionManagerControl.getMock();
        mockVersionManager.createVersion("Version2", version2Date, "Version 2 Description", new Long(12), null);
        mockVersionManagerControl.setReturnValue(null);
        final ArrayList releaseVersions = new ArrayList();
        releaseVersions.add(null);
        mockVersionManager.releaseVersions(releaseVersions, true);
        mockVersionManager.createVersion("Version3", version3Date, "Version 3 Description", new Long(12), null);
        mockVersionManagerControl.setReturnValue(null);
        mockVersionManager.createVersion("Version1", version1Date, "Version 1 Description", new Long(12), null);
        mockVersionManagerControl.setReturnValue(null);
        mockVersionManager.archiveVersion(null, true);
        mockVersionManagerControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null,
            mockProjectManager, mockVersionManager, null, null, null, null, null, null, null, applicationProperties, null);

        final Map newVersions = projectImportPersister.createVersions(backupProject);

        assertEquals(3, newVersions.size());
        assertTrue(newVersions.keySet().contains("1"));
        assertTrue(newVersions.keySet().contains("2"));
        assertTrue(newVersions.keySet().contains("3"));

        mockVersionManagerControl.verify();
        mockProjectManagerControl.verify();
        mockProjectControl.verify();
    }

    @Test
    public void testCreateVersionsNoProject()
    {
        final ExternalProject project = new ExternalProject();
        project.setKey("TST");
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            EasyList.build(new Long(12), new Long(14)));

        final MockControl mockProjectManagerControl = MockControl.createStrictControl(ProjectManager.class);
        final ProjectManager mockProjectManager = (ProjectManager) mockProjectManagerControl.getMock();
        mockProjectManager.getProjectObjByKey("TST");
        mockProjectManagerControl.setReturnValue(null);
        mockProjectManagerControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null,
                mockProjectManager, null, null, null, null, null, null, null, null, applicationProperties, null);

        try
        {
            projectImportPersister.createVersions(backupProject);
            fail("Should throw illegal state exception");
        }
        catch (final Exception e)
        {
            // expected
        }

        mockProjectManagerControl.verify();
    }

    @Test
    public void testCreateProject() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        project.setEmailSender("dude@test.com");
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            EasyList.build(new Long(12), new Long(14)));


        final MockControl mockProjectControl = MockControl.createStrictControl(Project.class);
        final Project mockProject = (Project) mockProjectControl.getMock();
        mockProjectControl.replay();

        final MockControl mockExternalUtilsControl = MockClassControl.createControl(ExternalUtils.class);
        final ExternalUtils mockExternalUtils = (ExternalUtils) mockExternalUtilsControl.getMock();
        mockExternalUtils.createProject(backupProject.getProject());
        mockExternalUtilsControl.setReturnValue(mockProject);
        mockExternalUtilsControl.replay();

        final MockControl mockPropertySetControl = MockControl.createStrictControl(PropertySet.class);
        final PropertySet mockPropertySet = (PropertySet) mockPropertySetControl.getMock();
        mockPropertySet.setString(ProjectKeys.EMAIL_SENDER, "dude@test.com");
        mockPropertySetControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, mockExternalUtils, null, null, null,
            null, null, null, null, null, null, null, null, null, null, applicationProperties, null)
        {
            PropertySet getPropertySet(final Project project)
            {
                return mockPropertySet;
            }
        };

        assertEquals(mockProject, projectImportPersister.createProject(backupProject.getProject()));

        mockProjectControl.verify();
        mockExternalUtilsControl.verify();
        mockPropertySetControl.verify();
    }

    @Test
    public void testCreateProjectNullEmailSender() throws Exception
    {
        final ExternalProject project = new ExternalProject();
        final BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            EasyList.build(new Long(12), new Long(14)));


        final MockControl mockProjectControl = MockControl.createStrictControl(Project.class);
        final Project mockProject = (Project) mockProjectControl.getMock();
        mockProjectControl.replay();

        final MockControl mockExternalUtilsControl = MockClassControl.createControl(ExternalUtils.class);
        final ExternalUtils mockExternalUtils = (ExternalUtils) mockExternalUtilsControl.getMock();
        mockExternalUtils.createProject(backupProject.getProject());
        mockExternalUtilsControl.setReturnValue(mockProject);
        mockExternalUtilsControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, mockExternalUtils, null, null, null,
            null, null, null, null, null, null, null, null, null, null, applicationProperties, null)
        {
            PropertySet getPropertySet(final Project project)
            {
                fail("This should not have been called");
                return null;
            }
        };

        assertEquals(mockProject, projectImportPersister.createProject(backupProject.getProject()));

        mockProjectControl.verify();
        mockExternalUtilsControl.verify();
    }

    @Test
    public void testCreateEntity() throws Exception
    {
        final Map fields = EasyMap.build("id", "123", "testfield", "something");
        final EntityRepresentation entityRepresentation = new EntityRepresentationImpl("TestEntity", fields);

        final MockControl mockModelFieldControl = MockClassControl.createControl(ModelField.class);
        final ModelField mockModelField = (ModelField) mockModelFieldControl.getMock();
        mockModelField.getName();
        mockModelFieldControl.setReturnValue("testfield");
        mockModelFieldControl.replay();

        final MockControl mockModelFieldControl2 = MockClassControl.createControl(ModelField.class);
        final ModelField mockModelField2 = (ModelField) mockModelFieldControl2.getMock();
        mockModelField2.getName();
        mockModelFieldControl2.setReturnValue("notfound");
        mockModelFieldControl2.replay();

        final MockControl mockModelEntityControl = MockClassControl.createControl(ModelEntity.class);
        final ModelEntity mockModelEntity = (ModelEntity) mockModelEntityControl.getMock();
        mockModelEntity.getFieldsIterator();
        mockModelEntityControl.setReturnValue(EasyList.build(mockModelField, mockModelField2).iterator());
        mockModelEntityControl.replay();

        final MockControl mockGenericValueControl = MockClassControl.createStrictControl(GenericValue.class);
        final GenericValue mockGenericValue = (GenericValue) mockGenericValueControl.getMock();
        mockGenericValue.getModelEntity();
        mockGenericValueControl.setReturnValue(mockModelEntity);
        mockGenericValue.setString("testfield", "something");
        mockGenericValue.getEntityName();
        mockGenericValueControl.setReturnValue("TestEntity");
        mockGenericValue.getAllFields();
        mockGenericValueControl.setReturnValue(fields);
        mockGenericValue.getLong("id");
        mockGenericValueControl.setReturnValue(new Long(123));
        mockGenericValueControl.replay();

        final MockControl mockOfBizDelegatorControl = MockControl.createStrictControl(OfBizDelegator.class);
        final OfBizDelegator mockOfBizDelegator = (OfBizDelegator) mockOfBizDelegatorControl.getMock();
        mockOfBizDelegator.makeValue(entityRepresentation.getEntityName());
        mockOfBizDelegatorControl.setReturnValue(mockGenericValue);
        mockOfBizDelegator.createValue(entityRepresentation.getEntityName(), fields);
        mockOfBizDelegatorControl.setReturnValue(mockGenericValue);
        mockOfBizDelegatorControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, mockOfBizDelegator, null,
            null, null, null, null, null, null, null, null, null, null, applicationProperties, null);

        assertEquals(new Long(123), projectImportPersister.createEntity(entityRepresentation));

        mockModelFieldControl.verify();
        mockModelFieldControl2.verify();
        mockModelEntityControl.verify();
        mockGenericValueControl.verify();
        mockOfBizDelegatorControl.verify();
    }

    @Test
    public void testCreateEntityDataAccessException() throws Exception
    {
        final Map fields = EasyMap.build("id", "123", "testfield", "something");
        final EntityRepresentation entityRepresentation = new EntityRepresentationImpl("TestEntity", fields);

        final MockControl mockModelFieldControl = MockClassControl.createControl(ModelField.class);
        final ModelField mockModelField = (ModelField) mockModelFieldControl.getMock();
        mockModelField.getName();
        mockModelFieldControl.setReturnValue("testfield");
        mockModelFieldControl.replay();

        final MockControl mockModelFieldControl2 = MockClassControl.createControl(ModelField.class);
        final ModelField mockModelField2 = (ModelField) mockModelFieldControl2.getMock();
        mockModelField2.getName();
        mockModelFieldControl2.setReturnValue("notfound");
        mockModelFieldControl2.replay();

        final MockControl mockModelEntityControl = MockClassControl.createControl(ModelEntity.class);
        final ModelEntity mockModelEntity = (ModelEntity) mockModelEntityControl.getMock();
        mockModelEntity.getFieldsIterator();
        mockModelEntityControl.setReturnValue(EasyList.build(mockModelField, mockModelField2).iterator());
        mockModelEntityControl.replay();

        final MockControl mockGenericValueControl = MockClassControl.createStrictControl(GenericValue.class);
        final GenericValue mockGenericValue = (GenericValue) mockGenericValueControl.getMock();
        mockGenericValue.getModelEntity();
        mockGenericValueControl.setReturnValue(mockModelEntity);
        mockGenericValue.setString("testfield", "something");
        mockGenericValue.getEntityName();
        mockGenericValueControl.setReturnValue("TestEntity");
        mockGenericValue.getAllFields();
        mockGenericValueControl.setReturnValue(fields);
        mockGenericValueControl.replay();

        final MockControl mockOfBizDelegatorControl = MockControl.createStrictControl(OfBizDelegator.class);
        final OfBizDelegator mockOfBizDelegator = (OfBizDelegator) mockOfBizDelegatorControl.getMock();
        mockOfBizDelegator.makeValue(entityRepresentation.getEntityName());
        mockOfBizDelegatorControl.setReturnValue(mockGenericValue);
        mockOfBizDelegator.createValue(entityRepresentation.getEntityName(), fields);
        mockOfBizDelegatorControl.setThrowable(new DataAccessException("Could not create thing."));
        mockOfBizDelegatorControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, mockOfBizDelegator, null,
            null, null, null, null, null, null, null, null, null, null, applicationProperties, null);

        assertNull(projectImportPersister.createEntity(entityRepresentation));

        mockModelFieldControl.verify();
        mockModelFieldControl2.verify();
        mockModelEntityControl.verify();
        mockGenericValueControl.verify();
        mockOfBizDelegatorControl.verify();
    }

    @Test
    public void testCreateIssueExceptionThrown() throws Exception
    {
        final Issue mockIssue = new MockIssue(new Long(12));

        final MockControl mockExternalUtilsControl = MockClassControl.createStrictControl(ExternalUtils.class);
        final ExternalUtils mockExternalUtils = (ExternalUtils) mockExternalUtilsControl.getMock();
        mockExternalUtils.createIssue(mockIssue, "1", "2");
        mockExternalUtilsControl.setThrowable(new ExternalException(new RuntimeException("blah")));
        mockExternalUtilsControl.replay();

        final MockControl mockIssueFactoryControl = MockControl.createStrictControl(IssueFactory.class);
        final IssueFactory mockIssueFactory = (IssueFactory) mockIssueFactoryControl.getMock();
        mockIssueFactoryControl.replay();

        final AtomicBoolean updateIssueKeyCalled = new AtomicBoolean(false);
        final AtomicBoolean createIssueForExternalIssueCalled = new AtomicBoolean(false);
        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, mockExternalUtils, mockIssueFactory,
            null, null, null, null, null, null, null, null, null, null, null, null, applicationProperties, null)
        {
            void updateIssueKey(final ExternalIssue externalIssue, final GenericValue issueGV)
            {
                // do nothing
                updateIssueKeyCalled.set(true);
            }

            Issue createIssueForExternalIssue(final ExternalIssue externalIssue)
            {
                createIssueForExternalIssueCalled.set(true);
                return mockIssue;
            }
        };

        final ExternalIssueImpl issue = new ExternalIssueImpl(null);
        issue.setStatus("1");
        issue.setResolution("2");

        assertNull(projectImportPersister.createIssue(issue, null, null));

        assertFalse(updateIssueKeyCalled.get());
        assertTrue(createIssueForExternalIssueCalled.get());
        mockIssueFactoryControl.verify();
        mockExternalUtilsControl.verify();
    }

    @Test
    public void testCreateIssue() throws Exception
    {
        final Date importDate = new Date();

        final MockControl mockGenericValueControl = MockClassControl.createStrictControl(GenericValue.class);
        final GenericValue mockGenericValue = (GenericValue) mockGenericValueControl.getMock();
        mockGenericValueControl.replay();

        final Issue mockIssue = new MockIssue(new Long(12));

        final MockControl mockExternalUtilsControl = MockClassControl.createStrictControl(ExternalUtils.class);
        final ExternalUtils mockExternalUtils = (ExternalUtils) mockExternalUtilsControl.getMock();
        mockExternalUtils.createIssue(mockIssue, "1", "2");
        mockExternalUtilsControl.setReturnValue(mockGenericValue);
        mockExternalUtilsControl.replay();

        final MockControl mockIssueFactoryControl = MockControl.createStrictControl(IssueFactory.class);
        final IssueFactory mockIssueFactory = (IssueFactory) mockIssueFactoryControl.getMock();
        mockIssueFactory.getIssue(mockGenericValue);
        mockIssueFactoryControl.setReturnValue(mockIssue);
        mockIssueFactoryControl.replay();

        final AtomicBoolean updateIssueKeyCalled = new AtomicBoolean(false);
        final AtomicBoolean createIssueForExternalIssueCalled = new AtomicBoolean(false);
        final AtomicBoolean createChangeItemCalled = new AtomicBoolean(false);
        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, mockExternalUtils, mockIssueFactory,
            null, null, null, null, null, null, null, null, null, null, null, null, applicationProperties, null)
        {
            void updateIssueKey(final ExternalIssue externalIssue, final GenericValue issueGV)
            {
                // do nothing
                updateIssueKeyCalled.set(true);
            }

            Issue createIssueForExternalIssue(final ExternalIssue externalIssue)
            {
                createIssueForExternalIssueCalled.set(true);
                return mockIssue;
            }

            void createChangeItem(final User author, final Issue issue, final ChangeItemBean changeItem)
            {
                assertEquals("ProjectImport", changeItem.getField());
                assertEquals(ChangeItemBean.STATIC_FIELD, changeItem.getFieldType());
                assertEquals("", changeItem.getFrom());
                assertEquals("", changeItem.getFromString());
                assertEquals(importDate.toString(), changeItem.getToString());
                assertEquals(String.valueOf(importDate.getTime()), changeItem.getTo());
                createChangeItemCalled.set(true);
            }
        };

        final ExternalIssueImpl issue = new ExternalIssueImpl(null);
        issue.setStatus("1");
        issue.setResolution("2");
        assertEquals(mockIssue, projectImportPersister.createIssue(issue, importDate, null));

        assertTrue(updateIssueKeyCalled.get());
        assertTrue(createIssueForExternalIssueCalled.get());
        assertTrue(createChangeItemCalled.get());
        mockIssueFactoryControl.verify();
        mockGenericValueControl.verify();
        mockExternalUtilsControl.verify();
    }

    @Test
    public void testUpdateIssueKeyDataAccessException() throws Exception
    {
        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, applicationProperties, null);

        final ExternalIssue externalIssue = new ExternalIssueImpl(null);
        externalIssue.setKey("TST-1");

        final MockControl mockGenericValueControl = MockClassControl.createControl(GenericValue.class);
        final GenericValue mockGenericValue = (GenericValue) mockGenericValueControl.getMock();

        mockGenericValue.setString("key", "TST-1");
        mockGenericValue.store();
        mockGenericValueControl.setThrowable(new GenericEntityException("This sucks!"));
        mockGenericValue.getLong("id");
        mockGenericValueControl.setReturnValue(new Long(12));
        mockGenericValueControl.replay();

        try
        {
            projectImportPersister.updateIssueKey(externalIssue, mockGenericValue);
            fail("Should have thrown DAE");
        }
        catch (final DataAccessException e)
        {
            // expected
            assertEquals("Unable to set the required key 'TST-1' in the Issue that we just created (id = '12').", e.getMessage());
        }

        mockGenericValueControl.verify();
    }

    @Test
    public void testUpdateIssueKey() throws Exception
    {
        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, applicationProperties, null);

        final ExternalIssue externalIssue = new ExternalIssueImpl(null);
        externalIssue.setKey("TST-1");

        final MockControl mockGenericValueControl = MockClassControl.createControl(GenericValue.class);
        final GenericValue mockGenericValue = (GenericValue) mockGenericValueControl.getMock();

        mockGenericValue.setString("key", "TST-1");
        mockGenericValue.store();
        mockGenericValueControl.replay();

        projectImportPersister.updateIssueKey(externalIssue, mockGenericValue);

        mockGenericValueControl.verify();
    }

    @Test
    public void testConvertExternalIssueToIssue() throws Exception
    {
        final ExternalIssue externalIssue = new ExternalIssueImpl("fred");
        externalIssue.setProject("11");
        externalIssue.setIssueType("7");
        externalIssue.setReporter("dylan");
        externalIssue.setAssignee("mark");
        externalIssue.setSummary("I am summary");
        externalIssue.setDescription("I am desc");
        externalIssue.setEnvironment("I am env");
        externalIssue.setPriority("3");
        externalIssue.setStatus("1");
        externalIssue.setResolution("5");

        externalIssue.setCreated(new Date());
        externalIssue.setUpdated(new Date());
        externalIssue.setDuedate(new Date());
        externalIssue.setResolutionDate(new Date());

        externalIssue.setVotes(new Long(3));
        externalIssue.setOriginalEstimate(new Long(4));
        externalIssue.setTimeSpent(new Long(2343));
        externalIssue.setEstimate(new Long(5454));
        externalIssue.setSecurityLevel("9");

        final MockControl mockMutableIssueControl = MockControl.createControl(MutableIssue.class);
        final MutableIssue mockMutableIssue = (MutableIssue) mockMutableIssueControl.getMock();
        mockMutableIssue.setProjectId(new Long(externalIssue.getProject()));

        mockMutableIssue.setIssueTypeId(externalIssue.getIssueType());

        mockMutableIssue.setReporterId(externalIssue.getReporter());
        mockMutableIssue.setAssigneeId(externalIssue.getAssignee());

        mockMutableIssue.setSummary(externalIssue.getSummary());
        mockMutableIssue.setDescription(externalIssue.getDescription());
        mockMutableIssue.setEnvironment(externalIssue.getEnvironment());
        mockMutableIssue.setPriorityId(externalIssue.getPriority());
        mockMutableIssue.setResolutionId(externalIssue.getResolution());

        mockMutableIssue.setCreated(new Timestamp(externalIssue.getCreated().getTime()));
        mockMutableIssue.setUpdated(new Timestamp((externalIssue.getUpdated().getTime())));
        mockMutableIssue.setDueDate(new Timestamp((externalIssue.getDuedate().getTime())));
        mockMutableIssue.setResolutionDate(new Timestamp((externalIssue.getResolutionDate().getTime())));

        mockMutableIssue.setVotes(externalIssue.getVotes());

        mockMutableIssue.setOriginalEstimate(externalIssue.getOriginalEstimate());
        mockMutableIssue.setTimeSpent(externalIssue.getTimeSpent());
        mockMutableIssue.setEstimate(externalIssue.getEstimate());
        mockMutableIssue.setSecurityLevelId(new Long(externalIssue.getSecurityLevel()));
        mockMutableIssueControl.replay();

        final MockControl mockIssueFactoryControl = MockControl.createStrictControl(IssueFactory.class);
        final IssueFactory mockIssueFactory = (IssueFactory) mockIssueFactoryControl.getMock();
        mockIssueFactory.getIssue();
        mockIssueFactoryControl.setReturnValue(mockMutableIssue);
        mockIssueFactoryControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, mockIssueFactory, null, null,
            null, null, null, null, null, null, null, null, null, null, applicationProperties, null);

        projectImportPersister.createIssueForExternalIssue(externalIssue);

        mockIssueFactoryControl.verify();
        mockMutableIssueControl.verify();
    }

    @Test
    public void testReIndexProject() throws Exception
    {
        final MockI18nBean i18n = new MockI18nBean();

        final MockControl mockIssueIndexManagerControl = MockControl.createStrictControl(IssueIndexManager.class);
        final IssueIndexManager mockIssueIndexManager = (IssueIndexManager) mockIssueIndexManagerControl.getMock();
        mockIssueIndexManager.reIndexIssues(null, null);
        mockIssueIndexManagerControl.setMatcher(new AlwaysMatcher());
        mockIssueIndexManagerControl.setReturnValue(2);
        mockIssueIndexManagerControl.replay();

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null,
            mockIssueIndexManager, null, null, null, null, null, null, null, null, null, null, applicationProperties, null)
        {
            IssuesIterable getIssuesIterable(final Collection newIssueIds)
            {
                assertEquals(2, newIssueIds.size());
                assertTrue(newIssueIds.contains(new Long(101)));
                assertTrue(newIssueIds.contains(new Long(102)));
                return new IssueObjectIssuesIterable(Collections.EMPTY_LIST);
            }
        };

        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("1", "101");
        projectImportMapper.getIssueMapper().mapValue("2", "102");

        defaultProjectImportPersister.reIndexProject(projectImportMapper, null, i18n);

        mockIssueIndexManagerControl.verify();
    }

    @Test
    public void testCreateAssociation() throws Exception
    {
        final ExternalNodeAssociation nodeAssociation = new ExternalNodeAssociation("1", "Issue", "2", "Version", "AssType");

        final MockControl mockNodeAssociationStoreControl = MockControl.createStrictControl(NodeAssociationStore.class);
        final NodeAssociationStore mockNodeAssociationStore = (NodeAssociationStore) mockNodeAssociationStoreControl.getMock();
        mockNodeAssociationStore.createAssociation(nodeAssociation.getSourceNodeEntity(), new Long(nodeAssociation.getSourceNodeId()),
                nodeAssociation.getSinkNodeEntity(), new Long(nodeAssociation.getSinkNodeId()), nodeAssociation.getAssociationType());
        mockNodeAssociationStoreControl.setReturnValue(null);
        mockNodeAssociationStoreControl.replay();
        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null,
            null, null, mockNodeAssociationStore, null, null, null, null, null, null, applicationProperties, null);

        defaultProjectImportPersister.createAssociation(nodeAssociation);

        mockNodeAssociationStoreControl.verify();
    }

    @Test
    public void testCreateVoter() throws Exception
    {
        final ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setIssueId("12");
        externalVoter.setVoter("admin");

        final MockControl mockUserAssociationStoreControl = MockControl.createStrictControl(UserAssociationStore.class);
        final UserAssociationStore mockUserAssociationStore = (UserAssociationStore) mockUserAssociationStoreControl.getMock();
        mockUserAssociationStore.createAssociation(UserAssociationParser.ASSOCIATION_TYPE_VOTE_ISSUE, "admin", "Issue", new Long(12));
        mockUserAssociationStoreControl.replay();
        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null,
            null, null, null, mockUserAssociationStore, null, null, null, null, null, applicationProperties, null);

        defaultProjectImportPersister.createVoter(externalVoter);

        mockUserAssociationStoreControl.verify();
    }

    @Test
    public void testCreateWatcher() throws Exception
    {
        final ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setIssueId("12");
        externalWatcher.setWatcher("admin");

        final MockControl mockUserAssociationStoreControl = MockControl.createStrictControl(UserAssociationStore.class);
        final UserAssociationStore mockUserAssociationStore = (UserAssociationStore) mockUserAssociationStoreControl.getMock();
        mockUserAssociationStore.createAssociation(UserAssociationParser.ASSOCIATION_TYPE_WATCH_ISSUE, "admin", "Issue", new Long(12));
        mockUserAssociationStoreControl.replay();
        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null,
            null, null, null, mockUserAssociationStore, null, null, null, null, null, applicationProperties, null);

        defaultProjectImportPersister.createWatcher(externalWatcher);

        mockUserAssociationStoreControl.verify();
    }

    @Test
    public void testCreateAttachment() throws Exception
    {
        final String adminKey = "admin";
        final String adminName = "adminName";
        final ExternalAttachment externalAttachment = new ExternalAttachment("12", "1212", "test.txt", new Date(), adminKey);
        externalAttachment.setAttachedFile(new File("/tmp"));

        final MockIssue mockIssue = new MockIssue(1212);

        final Attachment mockAttachment = EasyMock.createStrictMock(Attachment.class);

        final AttachmentManager mockAttachmentManager = EasyMock.createStrictMock(AttachmentManager.class);
        EasyMock.expect(mockAttachmentManager.createAttachmentCopySourceFile(externalAttachment.getAttachedFile(), externalAttachment.getFileName(),
                DefaultProjectImportPersister.GENERIC_CONTENT_TYPE, adminName, mockIssue, Collections.EMPTY_MAP,
            externalAttachment.getAttachedDate())).andReturn(mockAttachment);

        final IssueManager mockIssueManager = EasyMock.createStrictMock(IssueManager.class);
        EasyMock.expect(mockIssueManager.getIssueObject(new Long(1212))).andReturn(mockIssue);

        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        EasyMock.expect(userUtil.getUserByKey(adminKey)).andReturn(new MockApplicationUser(adminKey, adminName));
        EasyMock.replay(userUtil, mockAttachment, mockAttachmentManager, mockIssueManager);

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(userUtil, null, null, null, null,
            mockIssueManager, null, null, null, null, null, mockAttachmentManager, null, null, null, applicationProperties, null);

        final Attachment attachment = defaultProjectImportPersister.createAttachment(externalAttachment);
        assertNotNull(attachment);
        EasyMock.verify(userUtil, mockAttachment, mockAttachmentManager, mockIssueManager);
    }

    @Test
    public void testCreateAttachmentExceptionThrown() throws Exception
    {
        final String adminName = "adminName";
        final String adminKey = "admin";
        final ExternalAttachment externalAttachment = new ExternalAttachment("12", "1212", "test.txt", new Date(), adminKey);
        externalAttachment.setAttachedFile(new File("/tmp"));

        final MockIssue mockIssue = new MockIssue(1212);

        final AttachmentManager mockAttachmentManager = EasyMock.createStrictMock(AttachmentManager.class);
        EasyMock.expect(mockAttachmentManager.createAttachmentCopySourceFile(externalAttachment.getAttachedFile(), externalAttachment.getFileName(),
                DefaultProjectImportPersister.GENERIC_CONTENT_TYPE, adminName, mockIssue, Collections.EMPTY_MAP,
            externalAttachment.getAttachedDate())).andThrow(new AttachmentException("blah"));

        final IssueManager mockIssueManager = EasyMock.createStrictMock(IssueManager.class);
        EasyMock.expect(mockIssueManager.getIssueObject(new Long(1212))).andReturn(mockIssue);

        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        EasyMock.expect(userUtil.getUserByKey(adminKey)).andReturn(new MockApplicationUser(adminKey, adminName));

        EasyMock.replay(mockAttachmentManager, userUtil, mockIssueManager);

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(userUtil, null, null, null, null,
            mockIssueManager, null, null, null, null, null, mockAttachmentManager, null, null, null, applicationProperties, null);

        assertNull(defaultProjectImportPersister.createAttachment(externalAttachment));
        EasyMock.verify(mockAttachmentManager, userUtil, mockIssueManager);
    }

    @Test
    public void testCreateAttachmentNullAttachment() throws Exception
    {
        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManagerControl.replay();

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManagerControl.replay();

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null,
            mockIssueManager, null, null, null, null, null, mockAttachmentManager, null, null, null, applicationProperties, null);

        try
        {
            defaultProjectImportPersister.createAttachment(null);
            fail("Should throw IAE");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        mockAttachmentManagerControl.verify();
        mockIssueManagerControl.verify();
    }

    @Test
    public void testCreateAttachmentNullAttachmentFile() throws Exception
    {
        final ExternalAttachment externalAttachment = new ExternalAttachment("12", "1212", "test.txt", new Date(), "admin");

        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManagerControl.replay();

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManagerControl.replay();

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null,
            mockIssueManager, null, null, null, null, null, mockAttachmentManager, null, null, null, applicationProperties, null);

        try
        {
            defaultProjectImportPersister.createAttachment(externalAttachment);
            fail("Should throw IAE");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        mockAttachmentManagerControl.verify();
        mockIssueManagerControl.verify();
    }

    @Test
    public void testCreateAttachmentNullIssueId() throws Exception
    {
        final ExternalAttachment externalAttachment = new ExternalAttachment("12", null, "test.txt", new Date(), "admin");
        externalAttachment.setAttachedFile(new File("/tmp"));

        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManagerControl.replay();

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManagerControl.replay();

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null,
            mockIssueManager, null, null, null, null, null, mockAttachmentManager, null, null, null, applicationProperties, null);

        try
        {
            defaultProjectImportPersister.createAttachment(externalAttachment);
            fail("Should throw IAE");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        mockAttachmentManagerControl.verify();
        mockIssueManagerControl.verify();
    }

    @Test
    public void testCreateAttachmentNoSuchIssue() throws Exception
    {
        final ExternalAttachment externalAttachment = new ExternalAttachment("12", "1212", "test.txt", new Date(), "admin");
        externalAttachment.setAttachedFile(new File("/tmp"));

        final MockControl mockAttachmentManagerControl = MockControl.createStrictControl(AttachmentManager.class);
        final AttachmentManager mockAttachmentManager = (AttachmentManager) mockAttachmentManagerControl.getMock();
        mockAttachmentManagerControl.replay();

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject(new Long(1212));
        mockIssueManagerControl.setReturnValue(null);
        mockIssueManagerControl.replay();

        final DefaultProjectImportPersister defaultProjectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null,
            mockIssueManager, null, null, null, null, null, mockAttachmentManager, null, null, null, applicationProperties, null);

        try
        {
            defaultProjectImportPersister.createAttachment(externalAttachment);
            fail("Should throw IAE");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
        mockAttachmentManagerControl.verify();
        mockIssueManagerControl.verify();
    }

    @Test
    public void testReindexTaskProgressProcessorNullProgress() throws Exception
    {
        final DefaultProjectImportPersister.ReindexTaskProgressProcessor taskProgressProcessor = new DefaultProjectImportPersister.ReindexTaskProgressProcessor(
            null, new MockI18nBean());
        // This should not throw an exception
        taskProgressProcessor.processTaskProgress(100);
    }

    @Test
    public void testReindexTaskProgressProcessor() throws Exception
    {
        // Mock TaskProgressSink
        final MockControl mockTaskProgressSinkControl = MockControl.createStrictControl(TaskProgressSink.class);
        final TaskProgressSink mockTaskProgressSink = (TaskProgressSink) mockTaskProgressSinkControl.getMock();
        mockTaskProgressSink.makeProgress(90, "Indexing", "Re-indexing is 50% complete.");
        mockTaskProgressSinkControl.replay();

        final TaskProgressInterval progressInterval = new TaskProgressInterval(mockTaskProgressSink, 80, 100);
        final DefaultProjectImportPersister.ReindexTaskProgressProcessor taskProgressProcessor = new DefaultProjectImportPersister.ReindexTaskProgressProcessor(
            progressInterval, new MockI18nBean());
        taskProgressProcessor.processTaskProgress(50);

        // Verify Mock TaskProgressSink
        mockTaskProgressSinkControl.verify();
    }

    @Test
    public void testCreateChangeItemForIssueLinkIfNeededNullIssue() throws Exception
    {
        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject(new Long(12));
        mockIssueManagerControl.setReturnValue(null);
        mockIssueManagerControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null,
            mockIssueManager, null, null, null, null, null, null, null, null, null, applicationProperties, null);
        assertNull(projectImportPersister.createChangeItemForIssueLinkIfNeeded("12", "1234", "TST-1", true, null));

        mockIssueManagerControl.verify();
    }

    @Test
    public void testCreateChangeItemForIssueLinkIfNeededDontCreateChangeItem() throws Exception
    {
        final MockIssue issue = new MockIssue();
        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject(new Long(12));
        mockIssueManagerControl.setReturnValue(issue);
        mockIssueManagerControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null,
            mockIssueManager, null, null, null, null, null, null, null, null, null, applicationProperties, null)
        {
            boolean createIssueLinkChangeItem(final String linkedIssueKey, final Issue issue)
            {
                return false;
            }
        };

        assertNull(projectImportPersister.createChangeItemForIssueLinkIfNeeded("12", "1234", "TST-1", true, null));

        mockIssueManagerControl.verify();
    }

    @Test
    public void testCreateChangeItemForIssueLinkIfNeededHappyPath() throws Exception
    {
        final MockControl mockMutableIssueControl = MockControl.createStrictControl(MutableIssue.class);
        final MutableIssue mockMutableIssue = (MutableIssue) mockMutableIssueControl.getMock();
        mockMutableIssue.setUpdated(null);
        mockMutableIssueControl.setMatcher(new AlwaysMatcher());
        mockMutableIssue.store();
        mockMutableIssueControl.replay();

        final MockControl mockIssueManagerControl = MockControl.createStrictControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject(new Long(12));
        mockIssueManagerControl.setReturnValue(mockMutableIssue);
        mockIssueManagerControl.replay();

        final AtomicBoolean calledCreateChangeItem = new AtomicBoolean(false);
        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null,
            mockIssueManager, null, null, null, null, null, null, null, null, null, applicationProperties, null)
        {
            boolean createIssueLinkChangeItem(final String linkedIssueKey, final Issue issue)
            {
                return true;
            }

            ChangeItemBean getChangeItemBean(final String issueLinkTypeId, final String linkedIssueKey, final boolean isSource)
            {
                return null;
            }

            void createChangeItem(final User author, final Issue issue, final ChangeItemBean changeItem)
            {
                calledCreateChangeItem.set(true);
            }
        };

        assertEquals("12", projectImportPersister.createChangeItemForIssueLinkIfNeeded("12", "1234", "TST-1", true, null));

        assertTrue(calledCreateChangeItem.get());
        mockMutableIssueControl.verify();
        mockIssueManagerControl.verify();
    }

    @Test
    public void testCreateIssueLinkChangeItemLinkCreatedAndDeleted() throws Exception
    {
        final MockIssue issue = new MockIssue();

        final List changeItemsForIssue = EasyList.build(new ChangeItemBean("jira", "Link", "TST-1", null), new ChangeItemBean("jira", "Link", null,
            "TST-1"));

        final MockControl mockChangeHistoryManagerControl = MockControl.createStrictControl(ChangeHistoryManager.class);
        final ChangeHistoryManager mockChangeHistoryManager = (ChangeHistoryManager) mockChangeHistoryManagerControl.getMock();
        mockChangeHistoryManager.getChangeItemsForField(issue, "Link");
        mockChangeHistoryManagerControl.setReturnValue(changeItemsForIssue);
        mockChangeHistoryManagerControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null, null,
            null, null, null, null, null, mockChangeHistoryManager, null, null, applicationProperties, null);

        assertTrue(projectImportPersister.createIssueLinkChangeItem("TST-1", issue));
        mockChangeHistoryManagerControl.verify();
    }

    @Test
    public void testCreateIssueLinkChangeItemNoLinkChangeItems() throws Exception
    {
        final MockIssue issue = new MockIssue();

        final List changeItemsForIssue = new ArrayList();

        final MockControl mockChangeHistoryManagerControl = MockControl.createStrictControl(ChangeHistoryManager.class);
        final ChangeHistoryManager mockChangeHistoryManager = (ChangeHistoryManager) mockChangeHistoryManagerControl.getMock();
        mockChangeHistoryManager.getChangeItemsForField(issue, "Link");
        mockChangeHistoryManagerControl.setReturnValue(changeItemsForIssue);
        mockChangeHistoryManagerControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null, null,
            null, null, null, null, null, mockChangeHistoryManager, null, null, applicationProperties, null);

        assertTrue(projectImportPersister.createIssueLinkChangeItem("TST-1", issue));
        mockChangeHistoryManagerControl.verify();
    }

    @Test
    public void testCreateIssueLinkChangeItemDoesNotNeedCreating() throws Exception
    {
        final MockIssue issue = new MockIssue();

        final List changeItemsForIssue = EasyList.build(new ChangeItemBean("jira", "Link", "TST-1", null));

        final MockControl mockChangeHistoryManagerControl = MockControl.createStrictControl(ChangeHistoryManager.class);
        final ChangeHistoryManager mockChangeHistoryManager = (ChangeHistoryManager) mockChangeHistoryManagerControl.getMock();
        mockChangeHistoryManager.getChangeItemsForField(issue, "Link");
        mockChangeHistoryManagerControl.setReturnValue(changeItemsForIssue);
        mockChangeHistoryManagerControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null, null,
            null, null, null, null, null, mockChangeHistoryManager, null, null, applicationProperties, null);

        assertFalse(projectImportPersister.createIssueLinkChangeItem("TST-1", issue));
        mockChangeHistoryManagerControl.verify();
    }

    @Test
    public void testGetChangeItemBeanIsSource() throws Exception
    {
        final MockControl mockIssueLinkTypeControl = MockClassControl.createControl(IssueLinkType.class);
        final IssueLinkType mockIssueLinkType = (IssueLinkType) mockIssueLinkTypeControl.getMock();
        mockIssueLinkType.getOutward();
        mockIssueLinkTypeControl.setReturnValue("outward");
        mockIssueLinkTypeControl.replay();

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createStrictControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkType(new Long(12));
        mockIssueLinkTypeManagerControl.setReturnValue(mockIssueLinkType);
        mockIssueLinkTypeManagerControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null, null,
            null, null, null, null, null, null, mockIssueLinkTypeManager, null, applicationProperties, null);

        final ChangeItemBean bean = projectImportPersister.getChangeItemBean("12", "TST-1", true);
        assertEquals("jira", bean.getFieldType());
        assertEquals("Link", bean.getField());
        assertNull(bean.getFrom());
        assertNull(bean.getFromString());
        assertEquals("TST-1", bean.getTo());
        assertEquals("This issue outward TST-1", bean.getToString());

        mockIssueLinkTypeManagerControl.verify();
        mockIssueLinkTypeControl.verify();
    }

    @Test
    public void testGetChangeItemBeanIsDest() throws Exception
    {
        final MockControl mockIssueLinkTypeControl = MockClassControl.createControl(IssueLinkType.class);
        final IssueLinkType mockIssueLinkType = (IssueLinkType) mockIssueLinkTypeControl.getMock();
        mockIssueLinkType.getInward();
        mockIssueLinkTypeControl.setReturnValue("inward");
        mockIssueLinkTypeControl.replay();

        final MockControl mockIssueLinkTypeManagerControl = MockControl.createStrictControl(IssueLinkTypeManager.class);
        final IssueLinkTypeManager mockIssueLinkTypeManager = (IssueLinkTypeManager) mockIssueLinkTypeManagerControl.getMock();
        mockIssueLinkTypeManager.getIssueLinkType(new Long(12));
        mockIssueLinkTypeManagerControl.setReturnValue(mockIssueLinkType);
        mockIssueLinkTypeManagerControl.replay();

        final DefaultProjectImportPersister projectImportPersister = new DefaultProjectImportPersister(null, null, null, null, null, null, null,
            null, null, null, null, null, null, mockIssueLinkTypeManager, null, applicationProperties, null);

        final ChangeItemBean bean = projectImportPersister.getChangeItemBean("12", "TST-1", false);
        assertEquals("jira", bean.getFieldType());
        assertEquals("Link", bean.getField());
        assertNull(bean.getFrom());
        assertNull(bean.getFromString());
        assertEquals("TST-1", bean.getTo());
        assertEquals("This issue inward TST-1", bean.getToString());

        mockIssueLinkTypeManagerControl.verify();
        mockIssueLinkTypeControl.verify();
    }
}
