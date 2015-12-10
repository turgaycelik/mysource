package com.atlassian.jira.imports.project.transformer;

import java.util.Date;

import com.atlassian.jira.external.beans.ExternalChangeGroup;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.user.util.UserUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * @since v3.13
 */
public class TestChangeGroupTransformerImpl
{
    @Test
    public void testTransformNoIssueMapping()
    {
        final UserUtil userUtil = mock(UserUtil.class);
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, null);

        ExternalChangeGroup externalChangeGroup = new ExternalChangeGroup();
        externalChangeGroup.setAuthor("Fred");
        externalChangeGroup.setCreated(new Date(0));
        externalChangeGroup.setId("12");
        externalChangeGroup.setIssueId("100");

        ChangeGroupTransformerImpl changeGroupTransformer = new ChangeGroupTransformerImpl();
        assertNull(changeGroupTransformer.transform(projectImportMapper, externalChangeGroup).getIssueId());
    }

    @Test
    public void testTransform()
    {
        final UserUtil userUtil = mock(UserUtil.class);
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, null);
        projectImportMapper.getIssueMapper().mapValue("100", "200");

        ExternalChangeGroup externalChangeGroup = new ExternalChangeGroup();
        externalChangeGroup.setAuthor("Fred");
        externalChangeGroup.setCreated(new Date(0));
        externalChangeGroup.setId("12");
        externalChangeGroup.setIssueId("100");

        ChangeGroupTransformerImpl changeGroupTransformer = new ChangeGroupTransformerImpl();
        ExternalChangeGroup transformed = changeGroupTransformer.transform(projectImportMapper, externalChangeGroup);
        assertNull(transformed.getId());
        assertEquals("200", transformed.getIssueId());
        assertEquals(new Date(0), transformed.getCreated());
        assertEquals("Fred", transformed.getAuthor());
    }
}
