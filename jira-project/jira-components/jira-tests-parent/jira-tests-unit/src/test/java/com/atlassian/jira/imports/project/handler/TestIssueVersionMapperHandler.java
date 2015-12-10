package com.atlassian.jira.imports.project.handler;

import java.util.ArrayList;
import java.util.Map;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapperImpl;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestIssueVersionMapperHandler
{
    @Test
    public void testNodeAssociationButNotForVersions() throws Exception
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final SimpleProjectImportIdMapperImpl versionMapper = new SimpleProjectImportIdMapperImpl();
        IssueVersionMapperHandler issueVersionMapperHandler = new IssueVersionMapperHandler(backupProject, versionMapper);
        final Map attributes = EasyMap.build("sourceNodeId", "12", "sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Version", "associationType", "NotAVersion");

        issueVersionMapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, attributes);
        assertTrue(versionMapper.getRequiredOldIds().isEmpty());
    }

    @Test
    public void testHandleVersionButNotIssueInProject() throws Exception
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final SimpleProjectImportIdMapperImpl versionMapper = new SimpleProjectImportIdMapperImpl();
        IssueVersionMapperHandler issueVersionMapperHandler = new IssueVersionMapperHandler(backupProject, versionMapper);
        final Map attributes = EasyMap.build("sourceNodeId", "10", "sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Version", "associationType", NodeAssociationParser.AFFECTS_VERSION_TYPE);

        issueVersionMapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, attributes);
        assertTrue(versionMapper.getRequiredOldIds().isEmpty());
    }
    @Test
    public void testHandleNotNodeAssocication() throws Exception
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final SimpleProjectImportIdMapperImpl versionMapper = new SimpleProjectImportIdMapperImpl();
        IssueVersionMapperHandler issueVersionMapperHandler = new IssueVersionMapperHandler(backupProject, versionMapper);
        final Map attributes = EasyMap.build("sourceNodeId", "10", "sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Version", "associationType", NodeAssociationParser.AFFECTS_VERSION_TYPE);

        issueVersionMapperHandler.handleEntity("NOT_NODE_ASSOCIATION", attributes);
        assertTrue(versionMapper.getRequiredOldIds().isEmpty());
    }

    @Test
    public void testHandleFixVersion() throws Exception
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final SimpleProjectImportIdMapperImpl versionMapper = new SimpleProjectImportIdMapperImpl();
        IssueVersionMapperHandler issueVersionMapperHandler = new IssueVersionMapperHandler(backupProject, versionMapper);
        final Map attributes = EasyMap.build("sourceNodeId", "12", "sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Version", "associationType", NodeAssociationParser.FIX_VERSION_TYPE);

        issueVersionMapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, attributes);
        assertEquals(1, versionMapper.getRequiredOldIds().size());
        assertEquals("20001", versionMapper.getRequiredOldIds().iterator().next());
    }

    @Test
    public void testHandleAffectsVersion() throws Exception
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final SimpleProjectImportIdMapperImpl versionMapper = new SimpleProjectImportIdMapperImpl();
        IssueVersionMapperHandler issueVersionMapperHandler = new IssueVersionMapperHandler(backupProject, versionMapper);
        final Map attributes = EasyMap.build("sourceNodeId", "12", "sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Version", "associationType", NodeAssociationParser.AFFECTS_VERSION_TYPE);

        issueVersionMapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, attributes);
        assertEquals(1, versionMapper.getRequiredOldIds().size());
        assertEquals("20001", versionMapper.getRequiredOldIds().iterator().next());
    }

    @Test
    public void testParseException()
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final SimpleProjectImportIdMapperImpl versionMapper = new SimpleProjectImportIdMapperImpl();
        IssueVersionMapperHandler issueVersionMapperHandler = new IssueVersionMapperHandler(backupProject, versionMapper);
        final Map attributes = EasyMap.build("sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Version", "associationType", NodeAssociationParser.FIX_VERSION_TYPE);

        try
        {
            issueVersionMapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, attributes);
            fail("Should have thrown a parse exception");
        }
        catch (ParseException e)
        {
            // expected
        }
        assertTrue(versionMapper.getRequiredOldIds().isEmpty());
    }
}
