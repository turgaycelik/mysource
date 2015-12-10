package com.atlassian.jira.imports.project.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalAttachment;
import com.atlassian.jira.external.beans.ExternalChangeGroup;
import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalProjectRoleActor;
import com.atlassian.jira.external.beans.ExternalVoter;
import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.external.beans.ExternalWorklog;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.core.ProjectImportOptionsImpl;
import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.imports.project.parser.AttachmentParser;
import com.atlassian.jira.imports.project.parser.ChangeGroupParser;
import com.atlassian.jira.imports.project.parser.CommentParser;
import com.atlassian.jira.imports.project.parser.ProjectRoleActorParser;
import com.atlassian.jira.imports.project.parser.UserAssociationParser;
import com.atlassian.jira.imports.project.parser.WorklogParser;
import com.atlassian.jira.issue.attachment.AttachmentStore;
import com.atlassian.jira.issue.worklog.OfBizWorklogStore;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.easymock.MockControl;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * @since v3.13
 */
public class TestUserMapperHandler
{
    @org.mockito.Mock
    AttachmentStore attachmentStore;

    ProjectImportOptionsImpl projectImportOptions;

    UserMapperHandler mapperHandler;

    UserMapper mapper;

    BackupProject backupProject;

    ExternalProject project;

    @Before
    public void setUp()
    {
        projectImportOptions = new ProjectImportOptionsImpl("/some/path", "/Some/path");

        mapper = new UserMapper(null);


        project = new ExternalProject();
        project.setId("1234");
        project.setLead("dude");

        backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        mapperHandler = spy(new UserMapperHandler(projectImportOptions, backupProject, mapper, attachmentStore));
    }

    @Test
    public void testProjectLeadSetInEndDocument() throws Exception
    {
        projectImportOptions.setOverwriteProjectDetails(true);

        mapperHandler.endDocument();

        assertEquals(1, mapper.getRequiredOldIds().size());
        assertEquals("dude", mapper.getRequiredOldIds().iterator().next());
    }

    @Test
    public void testProjectLeadNotSetInEndDocument() throws Exception
    {
        projectImportOptions.setOverwriteProjectDetails(false);

        mapperHandler.endDocument();

        assertEquals(0, mapper.getRequiredOldIds().size());
    }

    @Test
    public void testMapperFlaggedByAttacher() throws ParseException
    {
        ExternalAttachment externalAttachment = new ExternalAttachment("1", "12345", "test.txt", new Date(), "dude");

        final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(Collections.EMPTY_MAP);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParserControl.replay();

        doReturn(mockAttachmentParser).when(mapperHandler).getAttachmentParser();

        mapperHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(1, mapper.getOptionalOldIds().size());
        assertEquals("dude", mapper.getOptionalOldIds().iterator().next());
        mockAttachmentParserControl.verify();
    }

    @Test
    public void testMapperNotFlaggedByUnhandledAttacher() throws ParseException
    {
        ExternalAttachment externalAttachment = new ExternalAttachment("1", "2", "test.txt", new Date(), "dude");

        final MockControl mockAttachmentParserControl = MockControl.createStrictControl(AttachmentParser.class);
        final AttachmentParser mockAttachmentParser = (AttachmentParser) mockAttachmentParserControl.getMock();
        mockAttachmentParser.parse(Collections.EMPTY_MAP);
        mockAttachmentParserControl.setReturnValue(externalAttachment);
        mockAttachmentParserControl.replay();

        doReturn(mockAttachmentParser).when(mapperHandler).getAttachmentParser();

        mapperHandler.handleEntity(AttachmentParser.ATTACHMENT_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(0, mapper.getRequiredOldIds().size());
        mockAttachmentParserControl.verify();
    }

    @Test
    public void testMapperFlaggedByVoter() throws ParseException
    {
        ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setIssueId("12345");
        externalVoter.setVoter("dude");

        final Mock mockUserAssociationParser = new Mock(UserAssociationParser.class);
        mockUserAssociationParser.setStrict(true);
        mockUserAssociationParser.expectAndReturn("parseVoter", P.ANY_ARGS, externalVoter);
        mockUserAssociationParser.expectAndReturn("parseWatcher", P.ANY_ARGS, null);

        final UserMapper mapper = new UserMapper(null);

        mapperHandler = spy(new UserMapperHandler(projectImportOptions, backupProject, mapper, attachmentStore));
        doReturn(mockUserAssociationParser.proxy()).when(mapperHandler).getUserAssociationParser();

        mapperHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, Collections.EMPTY_MAP);

        assertThat(mapper.getOptionalOldIds(), IsCollectionWithSize.hasSize(1));
        assertThat(mapper.getOptionalOldIds(), IsIterableContainingInOrder.contains("dude"));
        mockUserAssociationParser.verify();
    }

    // The voter is not relevant because the issue is not handled by the project
    @Test
    public void testMapperFlaggedNotFlaggedByUnhandledVoter() throws ParseException
    {
        mapper = Mockito.mock(UserMapper.class);
        ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setIssueId("66666");
        externalVoter.setVoter("dude");

        final Mock mockUserAssociationParser = new Mock(UserAssociationParser.class);
        mockUserAssociationParser.setStrict(true);
        mockUserAssociationParser.expectAndReturn("parseWatcher", P.ANY_ARGS, null);
        mockUserAssociationParser.expectAndReturn("parseVoter", P.ANY_ARGS, externalVoter);

        doReturn(mockUserAssociationParser.proxy()).when(mapperHandler).getUserAssociationParser();

        mapperHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, Collections.EMPTY_MAP);
        verifyZeroInteractions(mapper);
    }

    @Test
    public void testMapperFlaggedByProjectRole() throws ParseException
    {
        ExternalProjectRoleActor externalProjectRoleActor = new ExternalProjectRoleActor("12", "1234", "4321", UserRoleActorFactory.TYPE, "dude");

        final MockControl mockProjectRoleActorParserControl = MockControl.createStrictControl(ProjectRoleActorParser.class);
        final ProjectRoleActorParser mockProjectRoleActorParser = (ProjectRoleActorParser) mockProjectRoleActorParserControl.getMock();
        mockProjectRoleActorParser.parse(null);
        mockProjectRoleActorParserControl.setReturnValue(externalProjectRoleActor);
        mockProjectRoleActorParserControl.replay();

        doReturn(mockProjectRoleActorParser).when(mapperHandler).getProjectRoleActorParser();

        mapperHandler.handleEntity(ProjectRoleActorParser.PROJECT_ROLE_ACTOR_ENTITY_NAME, null);

        assertEquals(1, mapper.getOptionalOldIds().size());
        assertEquals("dude", mapper.getOptionalOldIds().iterator().next());
        mockProjectRoleActorParserControl.verify();
    }

    @Test
    public void testMapperNotFlaggedByUnhandledProjectRoleNotRightProject() throws ParseException
    {
        ExternalProjectRoleActor externalProjectRoleActor = new ExternalProjectRoleActor("12", null, "4321", UserRoleActorFactory.TYPE, "dude");

        final MockControl mockProjectRoleActorParserControl = MockControl.createStrictControl(ProjectRoleActorParser.class);
        final ProjectRoleActorParser mockProjectRoleActorParser = (ProjectRoleActorParser) mockProjectRoleActorParserControl.getMock();
        mockProjectRoleActorParser.parse(null);
        mockProjectRoleActorParserControl.setReturnValue(externalProjectRoleActor);
        mockProjectRoleActorParserControl.replay();

        doReturn(mockProjectRoleActorParser).when(mapperHandler).getProjectRoleActorParser();

        mapperHandler.handleEntity(ProjectRoleActorParser.PROJECT_ROLE_ACTOR_ENTITY_NAME, null);

        assertEquals(0, mapper.getOptionalOldIds().size());
        mockProjectRoleActorParserControl.verify();
    }

    @Test
    public void testMapperNotFlaggedByUnhandledProjectRoleNotRightRoleType() throws ParseException
    {
        ExternalProjectRoleActor externalProjectRoleActor = new ExternalProjectRoleActor("12", "5555", "4321", GroupRoleActorFactory.TYPE, "dude");

        final MockControl mockProjectRoleActorParserControl = MockControl.createStrictControl(ProjectRoleActorParser.class);
        final ProjectRoleActorParser mockProjectRoleActorParser = (ProjectRoleActorParser) mockProjectRoleActorParserControl.getMock();
        mockProjectRoleActorParser.parse(null);
        mockProjectRoleActorParserControl.setReturnValue(externalProjectRoleActor);
        mockProjectRoleActorParserControl.replay();

        doReturn(mockProjectRoleActorParser).when(mapperHandler).getProjectRoleActorParser();

        mapperHandler.handleEntity(ProjectRoleActorParser.PROJECT_ROLE_ACTOR_ENTITY_NAME, null);

        assertEquals(0, mapper.getOptionalOldIds().size());
        mockProjectRoleActorParserControl.verify();
    }

    @Test
    public void testMapperFlaggedByWatcher() throws ParseException
    {
        ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setIssueId("12345");
        externalWatcher.setWatcher("dude");

        final Mock mockUserAssociationParser = new Mock(UserAssociationParser.class);
        mockUserAssociationParser.setStrict(true);
        mockUserAssociationParser.expectAndReturn("parseVoter", P.ANY_ARGS, null);
        mockUserAssociationParser.expectAndReturn("parseWatcher", P.ANY_ARGS, externalWatcher);

        doReturn(mockUserAssociationParser.proxy()).when(mapperHandler).getUserAssociationParser();

        mapperHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(1, mapper.getOptionalOldIds().size());
        assertEquals("dude", mapper.getOptionalOldIds().iterator().next());
        mockUserAssociationParser.verify();
    }

    // The watcher is not relevant because the issue is not handled by the project
    @Test
    public void testMapperFlaggedNotFlaggedByUnhandledWatcher() throws ParseException
    {
        mapper = Mockito.mock(UserMapper.class);

        ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setIssueId("66666");
        externalWatcher.setWatcher("dude");

        final Mock mockUserAssociationParser = new Mock(UserAssociationParser.class);
        mockUserAssociationParser.setStrict(true);
        mockUserAssociationParser.expectAndReturn("parseVoter", P.ANY_ARGS, null);
        mockUserAssociationParser.expectAndReturn("parseWatcher", P.ANY_ARGS, externalWatcher);

        doReturn(mockUserAssociationParser.proxy()).when(mapperHandler).getUserAssociationParser();

        mapperHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, Collections.EMPTY_MAP);

        verifyZeroInteractions(mapper);
    }


    @Test
    public void testMapperFlaggedByComment() throws ParseException
    {
        ExternalComment externalComment = new ExternalComment();
        externalComment.setIssueId("12345");
        externalComment.setUpdateAuthor("dude");
        externalComment.setUsername("someauthor");

        final Mock mockCommentParser = new Mock(CommentParser.class);
        mockCommentParser.setStrict(true);
        mockCommentParser.expectAndReturn("parse", P.ANY_ARGS, externalComment);

        doReturn(mockCommentParser.proxy()).when(mapperHandler).getCommentParser();

        mapperHandler.handleEntity(CommentParser.COMMENT_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(2, mapper.getOptionalOldIds().size());
        Collection expected = EasyList.build("dude", "someauthor");
        assertTrue(mapper.getOptionalOldIds().containsAll(expected));
        mockCommentParser.verify();
    }

    // The comment is not relevant because the issue is not handled by the project
    @Test
    public void testMapperFlaggedNotFlaggedByUnhandledComment() throws ParseException
    {
        mapper = Mockito.mock(UserMapper.class);

        ExternalComment externalComment = new ExternalComment();
        externalComment.setIssueId("66666");
        externalComment.setUpdateAuthor("dude");
        externalComment.setUsername("someauthor");

        final Mock mockCommentParser = new Mock(CommentParser.class);
        mockCommentParser.setStrict(true);
        mockCommentParser.expectAndReturn("parse", P.ANY_ARGS, externalComment);

        doReturn(mockCommentParser.proxy()).when(mapperHandler).getCommentParser();

        mapperHandler.handleEntity(CommentParser.COMMENT_ENTITY_NAME, Collections.EMPTY_MAP);
        verifyZeroInteractions(mapper);
    }

    @Test
    public void testMapperFlaggedByWorklog() throws ParseException
    {
        ExternalWorklog externalWorklog = new ExternalWorklog();
        externalWorklog.setIssueId("12345");
        externalWorklog.setUpdateAuthor("dude");
        externalWorklog.setAuthor("someauthor");

        final Mock mockWorklogParser = new Mock(WorklogParser.class);
        mockWorklogParser.setStrict(true);
        mockWorklogParser.expectAndReturn("parse", P.ANY_ARGS, externalWorklog);

        doReturn(mockWorklogParser.proxy()).when(mapperHandler).getWorklogParser();

        mapperHandler.handleEntity(OfBizWorklogStore.WORKLOG_ENTITY, Collections.EMPTY_MAP);

        assertEquals(2, mapper.getOptionalOldIds().size());
        assertTrue(mapper.getOptionalOldIds().containsAll(EasyList.build("dude", "someauthor")));
        mockWorklogParser.verify();
    }

    // The worklog is not relevant because the issue is not handled by the project
    @Test
    public void testMapperFlaggedNotFlaggedByUnhandledWorklog() throws ParseException
    {
        mapper = Mockito.mock(UserMapper.class);

        ExternalWorklog externalWorklog = new ExternalWorklog();
        externalWorklog.setIssueId("66666");
        externalWorklog.setUpdateAuthor("dude");
        externalWorklog.setAuthor("someauthor");

        final Mock mockWorklogParser = new Mock(WorklogParser.class);
        mockWorklogParser.setStrict(true);
        mockWorklogParser.expectAndReturn("parse", P.ANY_ARGS, externalWorklog);

        doReturn(mockWorklogParser.proxy()).when(mapperHandler).getWorklogParser();

        mapperHandler.handleEntity(OfBizWorklogStore.WORKLOG_ENTITY, Collections.EMPTY_MAP);
        verifyZeroInteractions(mapper);
    }

    @Test
    public void testMapperFlaggedByChangeGroup() throws ParseException
    {
        ExternalChangeGroup externalChangeGroup = new ExternalChangeGroup();
        externalChangeGroup.setIssueId("12345");
        externalChangeGroup.setAuthor("dude");

        final Mock mockChangeGroupParser = new Mock(ChangeGroupParser.class);
        mockChangeGroupParser.setStrict(true);
        mockChangeGroupParser.expectAndReturn("parse", P.ANY_ARGS, externalChangeGroup);

        doReturn(mockChangeGroupParser.proxy()).when(mapperHandler).getChangeGroupParser();

        mapperHandler.handleEntity(ChangeGroupParser.CHANGE_GROUP_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(1, mapper.getOptionalOldIds().size());
        assertEquals("dude", mapper.getOptionalOldIds().iterator().next());
        mockChangeGroupParser.verify();

    }

    // The changeGroup is not relevant because the issue is not handled by the project
    @Test
    public void testMapperFlaggedNotFlaggedByUnhandledChangeGroup() throws ParseException
    {
        mapper = Mockito.mock(UserMapper.class);

        ExternalChangeGroup externalChangegroup = new ExternalChangeGroup();
        externalChangegroup.setIssueId("66666");
        externalChangegroup.setAuthor("dude");

        final Mock mockChangeGroupParser = new Mock(ChangeGroupParser.class);
        mockChangeGroupParser.setStrict(true);
        mockChangeGroupParser.expectAndReturn("parse", P.ANY_ARGS, externalChangegroup);

        doReturn(mockChangeGroupParser.proxy()).when(mapperHandler).getChangeGroupParser();

        mapperHandler.handleEntity(ChangeGroupParser.CHANGE_GROUP_ENTITY_NAME, Collections.EMPTY_MAP);
        verifyZeroInteractions(mapper);
    }

    @Test
    public void testMapperFlaggedNotFlaggedByUnhandledEntity() throws ParseException
    {
        mapper = Mockito.mock(UserMapper.class);

        mapperHandler.handleEntity("SomeEntity", Collections.EMPTY_MAP);

        verifyZeroInteractions(mapper);
    }

}
