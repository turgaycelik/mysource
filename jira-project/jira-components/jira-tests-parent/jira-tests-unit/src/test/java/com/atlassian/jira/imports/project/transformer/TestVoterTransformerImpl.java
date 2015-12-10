package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.external.beans.ExternalVoter;
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
public class TestVoterTransformerImpl
{
    @Test
    public void testTransform() throws Exception
    {
        final UserUtil userUtil = mock(UserUtil.class);
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, null);
        projectImportMapper.getIssueMapper().mapValue("12", "13");

        ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setVoter("admin");
        externalVoter.setIssueId("12");

        VoterTransformerImpl voterTransformer = new VoterTransformerImpl();
        final ExternalVoter transformedVoter = voterTransformer.transform(projectImportMapper, externalVoter);
        assertEquals("13", transformedVoter.getIssueId());
        assertEquals("admin", externalVoter.getVoter());
    }

    @Test
    public void testTransformNoMappedIssueId() throws Exception
    {
        final UserUtil userUtil = mock(UserUtil.class);
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, null);

        ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setVoter("admin");
        externalVoter.setIssueId("12");

        VoterTransformerImpl voterTransformer = new VoterTransformerImpl();
        assertNull(voterTransformer.transform(projectImportMapper, externalVoter).getIssueId());
    }
    
}
