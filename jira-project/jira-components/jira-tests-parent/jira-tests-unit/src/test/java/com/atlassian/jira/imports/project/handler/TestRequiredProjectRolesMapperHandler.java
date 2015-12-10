package com.atlassian.jira.imports.project.handler;

import java.util.Collections;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalWorklog;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapperImpl;
import com.atlassian.jira.imports.project.parser.CommentParser;
import com.atlassian.jira.imports.project.parser.WorklogParser;
import com.atlassian.jira.issue.worklog.OfBizWorklogStore;

import com.google.common.collect.Lists;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestRequiredProjectRolesMapperHandler
{

    @Test
    public void testCommentNoRoleLevel() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalComment externalComment = new ExternalComment();
        externalComment.setIssueId("12345");
        externalComment.setUpdateAuthor("dude");
        externalComment.setUsername("someauthor");

        final Mock mockCommentParser = new Mock(CommentParser.class);
        mockCommentParser.setStrict(true);
        mockCommentParser.expectAndReturn("parse", P.ANY_ARGS, externalComment);

        final SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        RequiredProjectRolesMapperHandler projectRoleMapperHandler = new RequiredProjectRolesMapperHandler(backupProject, mapper)
        {
            CommentParser getCommentParser()
            {
                return (CommentParser) mockCommentParser.proxy();
            }
        };

        projectRoleMapperHandler.handleEntity(CommentParser.COMMENT_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(0, mapper.getRequiredOldIds().size());
        mockCommentParser.verify();
    }

    @Test
    public void testCommentNotInProject() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, Lists.<Long>newArrayList());

        ExternalComment externalComment = new ExternalComment();
        externalComment.setIssueId("12345");
        externalComment.setUpdateAuthor("dude");
        externalComment.setUsername("someauthor");
        externalComment.setRoleLevelId(new Long(12));

        final Mock mockCommentParser = new Mock(CommentParser.class);
        mockCommentParser.setStrict(true);
        mockCommentParser.expectAndReturn("parse", P.ANY_ARGS, externalComment);

        final SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        RequiredProjectRolesMapperHandler projectRoleMapperHandler = new RequiredProjectRolesMapperHandler(backupProject, mapper)
        {
            CommentParser getCommentParser()
            {
                return (CommentParser) mockCommentParser.proxy();
            }
        };

        projectRoleMapperHandler.handleEntity(CommentParser.COMMENT_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(0, mapper.getRequiredOldIds().size());
        mockCommentParser.verify();
    }

    @Test
    public void testSimpleComment() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalComment externalComment = new ExternalComment();
        externalComment.setIssueId("12345");
        externalComment.setUpdateAuthor("dude");
        externalComment.setUsername("someauthor");
        externalComment.setRoleLevelId(new Long(12));


        final Mock mockCommentParser = new Mock(CommentParser.class);
        mockCommentParser.setStrict(true);
        mockCommentParser.expectAndReturn("parse", P.ANY_ARGS, externalComment);

        final SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        RequiredProjectRolesMapperHandler projectRoleMapperHandler = new RequiredProjectRolesMapperHandler(backupProject, mapper)
        {
            CommentParser getCommentParser()
            {
                return (CommentParser) mockCommentParser.proxy();
            }
        };

        projectRoleMapperHandler.handleEntity(CommentParser.COMMENT_ENTITY_NAME, Collections.EMPTY_MAP);

        assertEquals(1, mapper.getRequiredOldIds().size());
        assertTrue(mapper.getRequiredOldIds().contains("12"));
        mockCommentParser.verify();
    }

    @Test
    public void testWorklogNoRole() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalWorklog externalWorklog = new ExternalWorklog();
        externalWorklog.setIssueId("12345");
        externalWorklog.setUpdateAuthor("dude");

        final Mock mockWorklogParser = new Mock(WorklogParser.class);
        mockWorklogParser.setStrict(true);
        mockWorklogParser.expectAndReturn("parse", P.ANY_ARGS, externalWorklog);

        final SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        RequiredProjectRolesMapperHandler projectRoleMapperHandler = new RequiredProjectRolesMapperHandler(backupProject, mapper)
        {
            WorklogParser getWorklogParser()
            {
                return (WorklogParser) mockWorklogParser.proxy();
            }
        };

        projectRoleMapperHandler.handleEntity(OfBizWorklogStore.WORKLOG_ENTITY, Collections.EMPTY_MAP);

        assertEquals(0, mapper.getRequiredOldIds().size());
        mockWorklogParser.verify();
    }

    @Test
    public void testWorklogNotInProject() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, Lists.<Long>newArrayList());

        ExternalWorklog externalWorklog = new ExternalWorklog();
        externalWorklog.setIssueId("12345");
        externalWorklog.setUpdateAuthor("dude");
        externalWorklog.setRoleLevelId(new Long(12));

        final Mock mockWorklogParser = new Mock(WorklogParser.class);
        mockWorklogParser.setStrict(true);
        mockWorklogParser.expectAndReturn("parse", P.ANY_ARGS, externalWorklog);

        final SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        RequiredProjectRolesMapperHandler projectRoleMapperHandler = new RequiredProjectRolesMapperHandler(backupProject, mapper)
        {
            WorklogParser getWorklogParser()
            {
                return (WorklogParser) mockWorklogParser.proxy();
            }
        };

        projectRoleMapperHandler.handleEntity(OfBizWorklogStore.WORKLOG_ENTITY, Collections.EMPTY_MAP);

        assertEquals(0, mapper.getRequiredOldIds().size());
        mockWorklogParser.verify();
    }

    @Test
    public void testSimpleWorklog() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        ExternalWorklog externalWorklog = new ExternalWorklog();
        externalWorklog.setIssueId("12345");
        externalWorklog.setUpdateAuthor("dude");
        externalWorklog.setRoleLevelId(new Long(12));


        final Mock mockWorklogParser = new Mock(WorklogParser.class);
        mockWorklogParser.setStrict(true);
        mockWorklogParser.expectAndReturn("parse", P.ANY_ARGS, externalWorklog);

        final SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        RequiredProjectRolesMapperHandler projectRoleMapperHandler = new RequiredProjectRolesMapperHandler(backupProject, mapper)
        {
            WorklogParser getWorklogParser()
            {
                return (WorklogParser) mockWorklogParser.proxy();
            }
        };

        projectRoleMapperHandler.handleEntity(OfBizWorklogStore.WORKLOG_ENTITY, Collections.EMPTY_MAP);

        assertEquals(1, mapper.getRequiredOldIds().size());
        assertTrue(mapper.getRequiredOldIds().contains("12"));
        mockWorklogParser.verify();
    }

    @Test
    public void testSomeOtherEntity() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, EasyList.build(new Long(12345)));

        final SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        RequiredProjectRolesMapperHandler projectRoleMapperHandler = new RequiredProjectRolesMapperHandler(backupProject, mapper);

        projectRoleMapperHandler.handleEntity("Random", Collections.EMPTY_MAP);

        assertEquals(0, mapper.getRequiredOldIds().size());
    }
}
