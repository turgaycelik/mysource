package com.atlassian.jira.imports.project.handler;

import java.util.Collections;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapperImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestGroupMapperHandler
{
    BackupProject backupProject;
    SimpleProjectImportIdMapper groupMapper;
    
    @Before
    public void setUp() throws Exception
    {
        ExternalProject project = new ExternalProject();
        project.setId("1234");
        backupProject = new BackupProjectImpl(project, Collections.<ExternalVersion>emptyList(), Collections.<ExternalComponent>emptyList(),
                Collections.<ExternalCustomFieldConfiguration>emptyList(), CollectionBuilder.list(new Long(4)));

        groupMapper = new SimpleProjectImportIdMapperImpl();
    }

    @Test
    public void testHandleGroup() throws ParseException {
        GroupMapperHandler groupMapperHandler = new GroupMapperHandler(null, groupMapper);
        groupMapperHandler.startDocument();
        groupMapperHandler.handleEntity("Group", EasyMap.build("groupName", "dudes"));
        groupMapperHandler.handleEntity("Group", EasyMap.build("groupName", "dudettes"));
        groupMapperHandler.endDocument();

        assertEquals(2, groupMapper.getRegisteredOldIds().size());
        assertTrue(groupMapper.getRegisteredOldIds().contains("dudes"));
        assertTrue(groupMapper.getRegisteredOldIds().contains("dudettes"));
    }

    @Test
    public void testHandleComment() throws ParseException {
        GroupMapperHandler groupMapperHandler = new GroupMapperHandler(backupProject, groupMapper);
        groupMapperHandler.startDocument();
        groupMapperHandler.handleEntity("Action", EasyMap.build("id", "3", "issue", "4", "type", "comment", "level", "dudes"));
        groupMapperHandler.handleEntity("Action", EasyMap.build("id", "4", "issue", "4", "type", "comment", "level", "dudettes"));
        groupMapperHandler.handleEntity("Action", EasyMap.build("id", "5", "issue", "4", "type", "comment", "level", "dudes"));
        groupMapperHandler.handleEntity("Action", EasyMap.build("id", "6", "issue", "4", "type", "comment"));
        // Test empty string group name is ignored - this was discovered in JAC data.
        groupMapperHandler.handleEntity("Action", EasyMap.build("id", "7", "issue", "4", "type", "comment", "level", ""));
        // non-comment Action - should be ignored
        groupMapperHandler.handleEntity("Action", EasyMap.build("id", "8", "issue", "4", "type", "godknows", "level", "god"));
        // Comment on an issue in another project - should be ignored
        groupMapperHandler.handleEntity("Action", EasyMap.build("id", "8", "issue", "14", "type", "comment", "level", "god"));
        groupMapperHandler.endDocument();

        assertEquals(2, groupMapper.getRequiredOldIds().size());
        assertTrue(groupMapper.getRequiredOldIds().contains("dudes"));
        assertTrue(groupMapper.getRequiredOldIds().contains("dudettes"));
    }

    @Test
    public void testHandleWorklog() throws ParseException {
        GroupMapperHandler groupMapperHandler = new GroupMapperHandler(backupProject, groupMapper);
        groupMapperHandler.startDocument();
        groupMapperHandler.handleEntity("Worklog", EasyMap.build("id", "3", "issue", "4", "grouplevel", "dudes"));
        groupMapperHandler.handleEntity("Worklog", EasyMap.build("id", "4", "issue", "4", "grouplevel", "dudettes"));
        groupMapperHandler.handleEntity("Worklog", EasyMap.build("id", "5", "issue", "4", "grouplevel", "dudes"));
        groupMapperHandler.handleEntity("Worklog", EasyMap.build("id", "5", "issue", "4"));
        // Worklog on an issue in another project - should be ignored
        groupMapperHandler.handleEntity("Worklog", EasyMap.build("id", "3", "issue", "14", "grouplevel", "lepers"));
        groupMapperHandler.handleEntity("Rubbish", EasyMap.build("id", "5", "issue", "4", "grouplevel", "no-one"));
        groupMapperHandler.endDocument();

        assertEquals(2, groupMapper.getRequiredOldIds().size());
        assertTrue(groupMapper.getRequiredOldIds().contains("dudes"));
        assertTrue(groupMapper.getRequiredOldIds().contains("dudettes"));
    }

}
