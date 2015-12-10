package com.atlassian.jira.imports.project.transformer;

import java.util.Date;

import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.user.util.UserUtil;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v3.13
 */
public class TestCommentTransformerImpl
{

    private CommentTransformerImpl commentTransformer;

    @Before
    public void setUp() throws Exception
    {
        commentTransformer = new CommentTransformerImpl();
    }

    @Test
    public void testTransform()
    {
        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        ProjectImportMapper projectImportMapper = projectImportMapper(userUtil);

        ExternalComment externalComment = new ExternalComment();
        externalComment.setBody("I comment on stuff.");
        externalComment.setGroupLevel("dudes");
        externalComment.setId("1");
        externalComment.setIssueId("2");
        externalComment.setRoleLevelId(3L);
        final Date timePerformed = new Date();
        externalComment.setTimePerformed(timePerformed);
        externalComment.setUpdateAuthor("someone");
        externalComment.setUpdated(new Date(3));
        externalComment.setUsername("fred");

        final ExternalComment newComment = commentTransformer.transform(projectImportMapper, externalComment);
        assertEquals("I comment on stuff.", newComment.getBody());
        assertEquals("dudes", newComment.getGroupLevel());
        assertEquals("102", newComment.getIssueId());
        assertEquals(new Long(103), newComment.getRoleLevelId());
        assertEquals(timePerformed, newComment.getTimePerformed());
        assertEquals("someone", newComment.getUpdateAuthor());
        assertEquals(new Date(3), newComment.getUpdated());
        assertEquals("fred", newComment.getUsername());
        assertNull(newComment.getId());
    }

    @Test
    public void testTransformWithNulls()
    {
        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        ProjectImportMapper projectImportMapper = projectImportMapper(userUtil);

        ExternalComment externalComment = new ExternalComment();
        externalComment.setBody("I comment on stuff.");
        externalComment.setGroupLevel(null);
        externalComment.setId("1");
        externalComment.setIssueId("2");
        externalComment.setRoleLevelId(null);
        externalComment.setTimePerformed(null);
        externalComment.setUpdateAuthor(null);
        externalComment.setUpdated(null);
        externalComment.setUsername("fred");

        final ExternalComment newComment = commentTransformer.transform(projectImportMapper, externalComment);
        assertEquals("I comment on stuff.", newComment.getBody());
        assertNull(newComment.getGroupLevel());
        assertEquals("102", newComment.getIssueId());
        assertNull(newComment.getRoleLevelId());
        assertNull(newComment.getTimePerformed());
        assertNull(newComment.getUpdateAuthor());
        assertNull(newComment.getUpdated());
        assertEquals("fred", newComment.getUsername());
        assertNull(newComment.getId());
    }

    @Test
    public void testTransformWithUserKeyMapping()
    {
        final ExternalUser someone = new ExternalUser("someoneKey", "someone", "SomeOne", "", "");
        final ExternalUser fred = new ExternalUser("fredKey", "fred", "Fred", "", "");

        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        ProjectImportMapper projectImportMapper = projectImportMapper(userUtil);
        projectImportMapper.getUserMapper().registerOldValue(someone);
        projectImportMapper.getUserMapper().registerOldValue(fred);

        ExternalComment externalComment = new ExternalComment();
        externalComment.setBody("I comment on stuff.");
        externalComment.setGroupLevel("dudes");
        externalComment.setId("1");
        externalComment.setIssueId("2");
        externalComment.setRoleLevelId(3L);
        final Date timePerformed = new Date();
        externalComment.setTimePerformed(timePerformed);
        externalComment.setUpdateAuthor("someoneKey");
        externalComment.setUpdated(new Date(3));
        externalComment.setUsername("fredKey");

        final ExternalComment newComment = commentTransformer.transform(projectImportMapper, externalComment);
        assertEquals("I comment on stuff.", newComment.getBody());
        assertEquals("dudes", newComment.getGroupLevel());
        assertEquals("102", newComment.getIssueId());
        assertEquals(new Long(103), newComment.getRoleLevelId());
        assertEquals(timePerformed, newComment.getTimePerformed());
        assertEquals("someoneKey", newComment.getUpdateAuthor());
        assertEquals(new Date(3), newComment.getUpdated());
        assertEquals("fredKey", newComment.getUsername());
        assertNull(newComment.getId());
    }

    private ProjectImportMapper projectImportMapper(UserUtil userUtil)
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, null);
        projectImportMapper.getIssueMapper().mapValue("2", "102");
        projectImportMapper.getProjectRoleMapper().mapValue("3", "103");
        return projectImportMapper;
    }

}
