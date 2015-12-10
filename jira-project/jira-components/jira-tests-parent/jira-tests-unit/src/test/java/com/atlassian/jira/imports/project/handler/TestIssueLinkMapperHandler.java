package com.atlassian.jira.imports.project.handler;

import java.util.Collections;
import java.util.List;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.mapper.IssueLinkTypeMapper;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.issue.MockIssue;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestIssueLinkMapperHandler
{
    @Test
    public void testHandleIssueLinkType() throws ParseException
    {
        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        IssueLinkMapperHandler issueLinkMapperHandler = new IssueLinkMapperHandler(null, null, null, issueLinkTypeMapper);
        issueLinkMapperHandler.startDocument();
        // <IssueLinkType id="10001" linkname="jira_subtask_link" inward="jira_subtask_inward" outward="jira_subtask_outward" style="jira_subtask"/>
        issueLinkMapperHandler.handleEntity("IssueLinkType", EasyMap.build("id", "12", "linkname", "Duplicate"));
        issueLinkMapperHandler.handleEntity("IssueLinkType", EasyMap.build("id", "10001", "linkname", "jira_subtask_link", "style", "jira_subtask"));
        // Include another entity
        issueLinkMapperHandler.handleEntity("NOT_IssueLinkType", EasyMap.build("id", "10001", "linkname", "jira_subtask_link", "style", "jira_subtask"));
        issueLinkMapperHandler.endDocument();

        // Check in the Mapper that we have mapped the expected values.
        assertTrue(issueLinkTypeMapper.getRegisteredOldIds().contains("12"));
        assertTrue(issueLinkTypeMapper.getRegisteredOldIds().contains("10001"));
        assertEquals(2, issueLinkTypeMapper.getRegisteredOldIds().size());
        assertEquals("Duplicate", issueLinkTypeMapper.getKey("12"));
        assertEquals("jira_subtask_link", issueLinkTypeMapper.getKey("10001"));
        assertEquals(null, issueLinkTypeMapper.getStyle("12"));
        assertEquals("jira_subtask", issueLinkTypeMapper.getStyle("10001"));
    }

    @Test
    public void testHandleIssueLink() throws ParseException
    {
        // Mock out BackupSystemInformation
        final MockControl mockBackupSystemInformationControl = MockControl.createControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("400");
        mockBackupSystemInformationControl.setReturnValue("DOG-70", 2);
        mockBackupSystemInformation.getIssueKeyForId("300");
        mockBackupSystemInformationControl.setReturnValue("DOG-69", 2);
        mockBackupSystemInformationControl.replay();

        final MockControl mockIssueManagerControl = MockControl.createControl(IssueManager.class);
        final IssueManager mockIssueManager = (IssueManager) mockIssueManagerControl.getMock();
        mockIssueManager.getIssueObject("DOG-70");
        mockIssueManagerControl.setReturnValue(null, 2);
        mockIssueManager.getIssueObject("DOG-69");
        mockIssueManagerControl.setReturnValue(new MockIssue(300), 2);
        mockIssueManagerControl.replay();

        final IssueLinkTypeMapper issueLinkTypeMapper = new IssueLinkTypeMapper();
        final List issueIds = EasyList.build(new Long(100), new Long(120));
        final BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), Collections.EMPTY_LIST, Collections.EMPTY_LIST, Collections.EMPTY_LIST, issueIds);
        IssueLinkMapperHandler issueLinkMapperHandler = new IssueLinkMapperHandler(backupProject, mockBackupSystemInformation, mockIssueManager, issueLinkTypeMapper);
        issueLinkMapperHandler.startDocument();
        //     <IssueLink id="10044" linktype="10000" source="10184" destination="10030"/>
        // Source is in our project, Destination doesn't exist in current system
        issueLinkMapperHandler.handleEntity("IssueLink", EasyMap.build("id", "12", "linktype", "3", "source", "100", "destination", "400"));
        // Destination is in our project, Source doesn't exist in current system
        issueLinkMapperHandler.handleEntity("IssueLink", EasyMap.build("id", "13", "linktype", "4", "source", "400", "destination", "120"));
        // Source is in our project, Destination exists in current system
        issueLinkMapperHandler.handleEntity("IssueLink", EasyMap.build("id", "14", "linktype", "5", "source", "100", "destination", "300"));
        // Destination is in our project, Source exists in current system
        issueLinkMapperHandler.handleEntity("IssueLink", EasyMap.build("id", "15", "linktype", "6", "source", "300", "destination", "120"));
        // Source and Destination are both in this project
        issueLinkMapperHandler.handleEntity("IssueLink", EasyMap.build("id", "16", "linktype", "7", "source", "100", "destination", "120"));
        // Link is nothing to do with us.
        issueLinkMapperHandler.handleEntity("IssueLink", EasyMap.build("id", "17", "linktype", "8", "source", "300", "destination", "400"));
        issueLinkMapperHandler.endDocument();

        // Check in the Mapper that we have set the expected required linktypes
        assertTrue(issueLinkTypeMapper.getRequiredOldIds().contains("5"));
        assertTrue(issueLinkTypeMapper.getRequiredOldIds().contains("6"));
        assertTrue(issueLinkTypeMapper.getRequiredOldIds().contains("7"));
        assertEquals(3, issueLinkTypeMapper.getRequiredOldIds().size());

        // Verify Mock objects
        mockIssueManagerControl.verify();
        mockBackupSystemInformationControl.verify();
    }
}
