package com.atlassian.jira.imports.project.handler;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapperImpl;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;
import com.atlassian.jira.imports.project.parser.NodeAssociationParserImpl;
import com.atlassian.jira.imports.project.parser.ProjectParser;

import com.mockobjects.dynamic.Mock;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestProjectIssueSecurityLevelMapperHandler
{
    @Test
    public void testHandleHappyPath() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final SimpleProjectImportIdMapper mapper = new SimpleProjectImportIdMapperImpl();
        mapper.flagValueAsRequired("54321");
        mapper.flagValueAsRequired("11111");

        ProjectIssueSecurityLevelMapperHandler mapperHandler = new ProjectIssueSecurityLevelMapperHandler(backupProject, mapper);

        // Pass an entity that will set the projects issue security scheme id
        mapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME,
                EasyMap.build(NodeAssociationParserImpl.SINK_NODE_ENTITY, ProjectIssueSecurityLevelMapperHandler.NODE_ASSOCIATION_ISSUE_SECURITY_SCHEME,
                NodeAssociationParserImpl.SOURCE_NODE_ENTITY, ProjectParser.PROJECT_ENTITY_NAME,
                NodeAssociationParserImpl.SOURCE_NODE_ID, "1234",
                NodeAssociationParserImpl.SINK_NODE_ID, "66666",
                NodeAssociationParserImpl.ASSOCIATION_TYPE, "SecurityLevel")
                );

        // Pass an entity that is a different projects issue security scheme id
        mapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME,
                EasyMap.build(NodeAssociationParserImpl.SINK_NODE_ENTITY, ProjectIssueSecurityLevelMapperHandler.NODE_ASSOCIATION_ISSUE_SECURITY_SCHEME,
                NodeAssociationParserImpl.SOURCE_NODE_ENTITY, ProjectParser.PROJECT_ENTITY_NAME,
                NodeAssociationParserImpl.SOURCE_NODE_ID, "9999",
                NodeAssociationParserImpl.SINK_NODE_ID, "66666",
                NodeAssociationParserImpl.ASSOCIATION_TYPE, "SecurityLevel"));

        // Pass a node association entity that is not of the right type
        mapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME,
                EasyMap.build(NodeAssociationParserImpl.SINK_NODE_ENTITY, "OtherSinkType",
                NodeAssociationParserImpl.SOURCE_NODE_ENTITY, ProjectParser.PROJECT_ENTITY_NAME,
                NodeAssociationParserImpl.SOURCE_NODE_ID, "9999",
                NodeAssociationParserImpl.SINK_NODE_ID, "66666",
                NodeAssociationParserImpl.ASSOCIATION_TYPE, "SecurityLevel"));

        // Pass a entity that has nothing to do with this parser
        mapperHandler.handleEntity("Issue", Collections.EMPTY_MAP);
        
        // Pass a security level
        mapperHandler.handleEntity(ProjectIssueSecurityLevelMapperHandler.SCHEME_ISSUE_SECURITY_LEVELS_ENTITY_NAME,
                EasyMap.build(ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_SCHEME, "66666",
                ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_ID, "54321",
                ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_NAME, "Level 1"));

        // Pass another security level
        mapperHandler.handleEntity(ProjectIssueSecurityLevelMapperHandler.SCHEME_ISSUE_SECURITY_LEVELS_ENTITY_NAME,
                EasyMap.build(ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_SCHEME, "66666",
                ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_ID, "11111",
                ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_NAME, "Level 2"));


        // Pass a security level from a different scheme to make sure it is not added
        mapperHandler.handleEntity(ProjectIssueSecurityLevelMapperHandler.SCHEME_ISSUE_SECURITY_LEVELS_ENTITY_NAME,
                EasyMap.build(ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_SCHEME, "55555",
                ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_ID, "11111",
                ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_NAME, "Level 2"));

        Collection expected = EasyList.build("54321", "11111");

        assertEquals(2, mapper.getRequiredOldIds().size());
        assertTrue(mapper.getRequiredOldIds().containsAll(expected));
    }
    
    @Test
    public void testHandleNoAssociatedScheme() throws ParseException
    {
        final ExternalProject project = new ExternalProject();
        project.setId("1234");
        BackupProject backupProject = new BackupProjectImpl(project, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        Mock mockProjectImportIdMapper = new Mock(SimpleProjectImportIdMapper.class);
        mockProjectImportIdMapper.setStrict(true);
        mockProjectImportIdMapper.expectNotCalled("registerOldValue");

        ProjectIssueSecurityLevelMapperHandler mapperHandler = new ProjectIssueSecurityLevelMapperHandler(backupProject, (SimpleProjectImportIdMapper) mockProjectImportIdMapper.proxy());

        // Pass an entity that will set the projects issue security scheme id
        mapperHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME,
                EasyMap.build(NodeAssociationParserImpl.SINK_NODE_ENTITY, ProjectIssueSecurityLevelMapperHandler.NODE_ASSOCIATION_ISSUE_SECURITY_SCHEME,
                NodeAssociationParserImpl.SOURCE_NODE_ENTITY, ProjectParser.PROJECT_ENTITY_NAME,
                NodeAssociationParserImpl.SOURCE_NODE_ID, "5678",
                NodeAssociationParserImpl.SINK_NODE_ID, "66666",
                NodeAssociationParserImpl.ASSOCIATION_TYPE, "SecurityLevel"));

        // Pass a security levels for the scheme we send in
        mapperHandler.handleEntity(ProjectIssueSecurityLevelMapperHandler.SCHEME_ISSUE_SECURITY_LEVELS_ENTITY_NAME,
                EasyMap.build(ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_SCHEME, "66666",
                ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_ID, "54321",
                ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_NAME, "Level 1"));

        // Pass another security level
        mapperHandler.handleEntity(ProjectIssueSecurityLevelMapperHandler.SCHEME_ISSUE_SECURITY_LEVELS_ENTITY_NAME,
                EasyMap.build(ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_SCHEME, "66666",
                ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_ID, "11111",
                ProjectIssueSecurityLevelMapperHandler.ISSUE_SECURITY_LEVEL_NAME, "Level 2"));

        mockProjectImportIdMapper.verify();
    }

}
