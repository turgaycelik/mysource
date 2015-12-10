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
public class TestIssueComponentMapperHandler
{
    @Test
    public void testNodeAssociationButNotForComponents() throws Exception
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final SimpleProjectImportIdMapperImpl componentMapper = new SimpleProjectImportIdMapperImpl();
        IssueComponentMapperHandler issueComponentMapperHandler = new IssueComponentMapperHandler(backupProject, componentMapper);
        final Map attributes = EasyMap.build("sourceNodeId", "12", "sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Component", "associationType", "NotAComponent");

        issueComponentMapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, attributes);
        assertTrue(componentMapper.getRequiredOldIds().isEmpty());
    }

    @Test
    public void testHandleComponentButNotIssueInProject() throws Exception
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final SimpleProjectImportIdMapperImpl componentMapper = new SimpleProjectImportIdMapperImpl();
        IssueComponentMapperHandler issueComponentMapperHandler = new IssueComponentMapperHandler(backupProject, componentMapper);
        final Map attributes = EasyMap.build("sourceNodeId", "10", "sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Component", "associationType", NodeAssociationParser.COMPONENT_TYPE);

        issueComponentMapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, attributes);
        assertTrue(componentMapper.getRequiredOldIds().isEmpty());
    }
    @Test
    public void testHandleNotNodeAssocication() throws Exception
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final SimpleProjectImportIdMapperImpl componentMapper = new SimpleProjectImportIdMapperImpl();
        IssueComponentMapperHandler issueComponentMapperHandler = new IssueComponentMapperHandler(backupProject, componentMapper);
        final Map attributes = EasyMap.build("sourceNodeId", "10", "sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Component", "associationType", NodeAssociationParser.COMPONENT_TYPE);

        issueComponentMapperHandler.handleEntity("NOT_NODE_ASSOCIATION", attributes);
        assertTrue(componentMapper.getRequiredOldIds().isEmpty());
    }

    @Test
    public void testHandleComponent() throws Exception
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final SimpleProjectImportIdMapperImpl componentMapper = new SimpleProjectImportIdMapperImpl();
        IssueComponentMapperHandler issueComponentMapperHandler = new IssueComponentMapperHandler(backupProject, componentMapper);
        final Map attributes = EasyMap.build("sourceNodeId", "12", "sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Component", "associationType", NodeAssociationParser.COMPONENT_TYPE);

        issueComponentMapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, attributes);
        assertEquals(1, componentMapper.getRequiredOldIds().size());
        assertEquals("20001", componentMapper.getRequiredOldIds().iterator().next());
    }

    @Test
    public void testParseException()
    {
        BackupProject backupProject = new BackupProjectImpl(new ExternalProject(), new ArrayList(), new ArrayList(),
                new ArrayList(), EasyList.build(new Long(12), new Long(14)));
        final SimpleProjectImportIdMapperImpl componentMapper = new SimpleProjectImportIdMapperImpl();
        IssueComponentMapperHandler issueComponentMapperHandler = new IssueComponentMapperHandler(backupProject, componentMapper);
        final Map attributes = EasyMap.build("sourceNodeEntity", "Issue", "sinkNodeId", "20001", "sinkNodeEntity", "Component", "associationType", NodeAssociationParser.COMPONENT_TYPE);

        try
        {
            issueComponentMapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, attributes);
            fail("Should have thrown a parse exception");
        }
        catch (ParseException e)
        {
            // expected
        }
        assertTrue(componentMapper.getRequiredOldIds().isEmpty());
    }
    
}
