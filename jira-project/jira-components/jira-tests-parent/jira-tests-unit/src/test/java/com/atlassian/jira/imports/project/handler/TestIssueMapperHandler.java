package com.atlassian.jira.imports.project.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssue;
import com.atlassian.jira.external.beans.ExternalIssueImpl;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.IssueParser;

import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestIssueMapperHandler
{

    @Test
    public void testHandleIssueMappings() throws ParseException
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        // Setup some calls to the custom field mapper
        projectImportMapper.getCustomFieldMapper().flagValueAsRequired("55555", "12");

        final Mock mockIssueParser = new Mock(IssueParser.class);
        mockIssueParser.setStrict(true);

        final ExternalIssue externalIssue = new ExternalIssueImpl(null);
        externalIssue.setIssueType("12345");
        externalIssue.setStatus("34567");
        externalIssue.setPriority("23456");
        externalIssue.setResolution("11111");
        externalIssue.setSecurityLevel("22222");
        externalIssue.setAssignee("ted");
        externalIssue.setReporter("bill");
        externalIssue.setId("12");
        mockIssueParser.expectAndReturn("parse", P.ANY_ARGS, externalIssue);

        IssueMapperHandler issueMapperHandler = new IssueMapperHandler(backupProject, projectImportMapper)
        {
            IssueParser getIssueParser()
            {
                return (IssueParser) mockIssueParser.proxy();
            }
        };
        
        issueMapperHandler.handleEntity("Issue", Collections.EMPTY_MAP);
        projectImportMapper.getCustomFieldMapper().registerIssueTypesInUse();
        // Verify Issue Type
        assertEquals(1, projectImportMapper.getIssueTypeMapper().getRequiredOldIds().size());
        assertEquals("12345", projectImportMapper.getIssueTypeMapper().getRequiredOldIds().iterator().next());
        assertNull(projectImportMapper.getIssueTypeMapper().getMappedId("12345"));
        // Verify Status
        assertEquals("34567", projectImportMapper.getStatusMapper().getRequiredOldIds().iterator().next());
        assertEquals("12345", projectImportMapper.getStatusMapper().getIssueTypeIdsForRequiredStatus("34567").iterator().next());
        assertNull(projectImportMapper.getStatusMapper().getMappedId("34567"));
        // Verify Priority
        assertEquals(1, projectImportMapper.getPriorityMapper().getRequiredOldIds().size());
        assertEquals("23456", projectImportMapper.getPriorityMapper().getRequiredOldIds().iterator().next());
        assertNull(projectImportMapper.getPriorityMapper().getMappedId("23456"));
        // Verify Resolution
        assertEquals(1, projectImportMapper.getResolutionMapper().getRequiredOldIds().size());
        assertEquals("11111", projectImportMapper.getResolutionMapper().getRequiredOldIds().iterator().next());
        assertNull(projectImportMapper.getResolutionMapper().getMappedId("11111"));
        // Verify IssueSecurityLevel
        assertEquals(1, projectImportMapper.getIssueSecurityLevelMapper().getRequiredOldIds().size());
        assertEquals("22222", projectImportMapper.getIssueSecurityLevelMapper().getRequiredOldIds().iterator().next());
        assertNull(projectImportMapper.getIssueSecurityLevelMapper().getMappedId("22222"));
        // Verify User
        assertEquals(2, projectImportMapper.getUserMapper().getRequiredOldIds().size());
        Collection expected = EasyList.build("bill", "ted");
        assertEquals(2, projectImportMapper.getUserMapper().getRequiredOldIds().size());
        assertTrue(projectImportMapper.getUserMapper().getRequiredOldIds().containsAll(expected));
        // Verify CustomFieldMapper
        assertEquals(1, projectImportMapper.getCustomFieldMapper().getIssueTypeIdsForRequiredCustomField("55555").size());
        assertEquals("12345", projectImportMapper.getCustomFieldMapper().getIssueTypeIdsForRequiredCustomField("55555").iterator().next());

        mockIssueParser.verify();
    }

    @Test
    public void testHandleNonIssueEntity() throws ParseException
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        final Mock mockIssueParser = new Mock(IssueParser.class);
        mockIssueParser.setStrict(true);
        mockIssueParser.expectNotCalled("parse");

        IssueMapperHandler issueMapperHandler = new IssueMapperHandler(backupProject, projectImportMapper);

        issueMapperHandler.handleEntity("BSENTITY", Collections.EMPTY_MAP);

        assertEquals(0, projectImportMapper.getCustomFieldMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getIssueMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getIssueSecurityLevelMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getIssueTypeMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getPriorityMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getResolutionMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getStatusMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getUserMapper().getRequiredOldIds().size());
        mockIssueParser.verify();
    }

    @Test
    public void testIssueNotInProject() throws ParseException
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        final Mock mockIssueParser = new Mock(IssueParser.class);
        mockIssueParser.setStrict(true);

        final ExternalIssue externalIssue = new ExternalIssueImpl(null);
        externalIssue.setId("200");
        // This should not be set on any of the mappers
        externalIssue.setIssueType("1000000");
        mockIssueParser.expectAndReturn("parse", P.ANY_ARGS, externalIssue);

        IssueMapperHandler issueMapperHandler = new IssueMapperHandler(backupProject, projectImportMapper)
        {
            IssueParser getIssueParser()
            {
                return (IssueParser) mockIssueParser.proxy();
            }
        };

        issueMapperHandler.handleEntity("Issue", Collections.EMPTY_MAP);
        assertEquals(0, projectImportMapper.getCustomFieldMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getIssueMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getIssueSecurityLevelMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getIssueTypeMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getPriorityMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getResolutionMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getStatusMapper().getRequiredOldIds().size());
        assertEquals(0, projectImportMapper.getUserMapper().getRequiredOldIds().size());

        mockIssueParser.verify();
    }
}
