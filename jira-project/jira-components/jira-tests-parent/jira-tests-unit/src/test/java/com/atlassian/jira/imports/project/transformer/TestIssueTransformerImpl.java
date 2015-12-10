package com.atlassian.jira.imports.project.transformer;

import java.sql.Timestamp;

import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalIssueImpl;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserUtil;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @since v3.13
 */
public class TestIssueTransformerImpl
{

    private IssueTransformerImpl issueTransformer;

    @Before
    public void setUp() throws Exception
    {
        issueTransformer = new IssueTransformerImpl();
    }

    @Test
    public void testTransformIssue()
    {
        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        ProjectImportMapper projectImportMapper = projectImportMapper(userUtil);

        ExternalIssue externalIssue = externalIssue();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        externalIssue.setResolutionDate(now);

        final ExternalIssue newIssue = issueTransformer.transform(projectImportMapper, externalIssue);
        assertEquals("2", newIssue.getStatus());
        assertEquals("4", newIssue.getPriority());
        assertEquals("6", newIssue.getResolution());
        assertEquals("8", newIssue.getIssueType());
        assertEquals("10", newIssue.getSecurityLevel());
        assertEquals("12", newIssue.getProject());
        assertEquals("I am desc", newIssue.getDescription());
        assertEquals("TST-1", newIssue.getKey());
        assertEquals(now, newIssue.getResolutionDate());
        assertNull(newIssue.getId());
    }

    @Test
    public void testTransformIssueNoResolutionDate()
    {
        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        ProjectImportMapper projectImportMapper = projectImportMapper(userUtil);

        ExternalIssue externalIssue = externalIssue();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        externalIssue.setUpdated(now);

        final ExternalIssue newIssue = issueTransformer.transform(projectImportMapper, externalIssue);
        assertEquals("2", newIssue.getStatus());
        assertEquals("4", newIssue.getPriority());
        assertEquals("6", newIssue.getResolution());
        assertEquals("8", newIssue.getIssueType());
        assertEquals("10", newIssue.getSecurityLevel());
        assertEquals("12", newIssue.getProject());
        assertEquals("I am desc", newIssue.getDescription());
        assertEquals("TST-1", newIssue.getKey());
        assertEquals(now, newIssue.getUpdated());
        //Resolution date should have been set to the last updated
        assertEquals(now, newIssue.getResolutionDate());
        assertNull(newIssue.getId());
    }

    @Test
    public void testTransformIssueWithUserKeyMapping() throws Exception
    {
        final ExternalUser assignee = new ExternalUser("assigneeKey", "assignee", "", "", "");
        final ExternalUser reporter = new ExternalUser("reporterKey", "reporter", "", "", "");

        final UserUtil userUtil = Mockito.mock(UserUtil.class);
        Mockito.when(userUtil.getUserByName("assignee")).thenReturn(new MockApplicationUser("newAssignee", "assignee"));
        Mockito.when(userUtil.getUserByName("reporter")).thenReturn(new MockApplicationUser("newReporter", "reporter"));
        final ProjectImportMapper projectImportMapper = projectImportMapper(userUtil);
        projectImportMapper.getUserMapper().registerOldValue(assignee);
        projectImportMapper.getUserMapper().registerOldValue(reporter);

        final ExternalIssue externalIssue = externalIssue();
        externalIssue.setAssignee("assigneeKey");
        externalIssue.setReporter("reporterKey");

        final ExternalIssue newIssue = issueTransformer.transform(projectImportMapper, externalIssue);
        assertThat(newIssue.getAssignee(), is("newAssignee"));
        assertThat(newIssue.getReporter(), is("newReporter"));
    }
    
    private ExternalIssue externalIssue()
    {
        ExternalIssue externalIssue = new ExternalIssueImpl(null);
        externalIssue.setStatus("1");
        externalIssue.setPriority("3");
        externalIssue.setResolution("5");
        externalIssue.setIssueType("7");
        externalIssue.setSecurityLevel("9");
        externalIssue.setProject("11");
        externalIssue.setDescription("I am desc");
        externalIssue.setKey("TST-1");
        return externalIssue;
    }

    private ProjectImportMapper projectImportMapper(UserUtil userUtil)
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(userUtil, null);
        projectImportMapper.getStatusMapper().mapValue("1", "2");
        projectImportMapper.getPriorityMapper().mapValue("3", "4");
        projectImportMapper.getResolutionMapper().mapValue("5", "6");
        projectImportMapper.getIssueTypeMapper().mapValue("7", "8");
        projectImportMapper.getIssueSecurityLevelMapper().mapValue("9", "10");
        projectImportMapper.getProjectMapper().mapValue("11", "12");
        return projectImportMapper;
    }
}
