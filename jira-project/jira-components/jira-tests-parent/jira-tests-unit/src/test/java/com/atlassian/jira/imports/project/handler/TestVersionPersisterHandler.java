package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;
import com.atlassian.jira.imports.project.transformer.VersionTransformer;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestVersionPersisterHandler
{
    @Test
    public void testHandle() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalNodeAssociation externalNodeAssociation = new ExternalNodeAssociation("", "", "", "", NodeAssociationParser.AFFECTS_VERSION_TYPE);

        final MockControl mockNodeAssociationParserControl = MockControl.createStrictControl(NodeAssociationParser.class);
        final NodeAssociationParser mockNodeAssociationParser = (NodeAssociationParser) mockNodeAssociationParserControl.getMock();
        mockNodeAssociationParser.parse(null);
        mockNodeAssociationParserControl.setReturnValue(externalNodeAssociation);
        mockNodeAssociationParserControl.replay();

        final MockControl mockVersionTransformerControl = MockControl.createStrictControl(VersionTransformer.class);
        final VersionTransformer mockVersionTransformer = (VersionTransformer) mockVersionTransformerControl.getMock();
        mockVersionTransformer.transform(projectImportMapper, externalNodeAssociation);
        mockVersionTransformerControl.setReturnValue(externalNodeAssociation);
        mockVersionTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createAssociation(externalNodeAssociation);
        mockProjectImportPersisterControl.setReturnValue(true);
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);
        VersionPersisterHandler versionPersisterHandler = new VersionPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, null, new ExecutorForTests())
        {
            NodeAssociationParser getNodeAssociationParser()
            {
                return mockNodeAssociationParser;
            }

            VersionTransformer getVersionTransformer()
            {
                return mockVersionTransformer;
            }
        };

        versionPersisterHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, null);
        versionPersisterHandler.handleEntity("NOTVersion", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockVersionTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
        mockNodeAssociationParserControl.verify();
    }

    @Test
    public void testHandleErrorAddingAffectsVersion() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getVersionMapper().registerOldValue("12", "Version 1");
        final ExternalNodeAssociation externalNodeAssociation = new ExternalNodeAssociation("34", "", "12", "", NodeAssociationParser.AFFECTS_VERSION_TYPE);

        final ExternalNodeAssociation transformedExternalNodeAssociation = new ExternalNodeAssociation("34", "", "12", "", NodeAssociationParser.AFFECTS_VERSION_TYPE);

        final MockControl mockNodeAssociationParserControl = MockControl.createStrictControl(NodeAssociationParser.class);
        final NodeAssociationParser mockNodeAssociationParser = (NodeAssociationParser) mockNodeAssociationParserControl.getMock();
        mockNodeAssociationParser.parse(null);
        mockNodeAssociationParserControl.setReturnValue(externalNodeAssociation);
        mockNodeAssociationParserControl.replay();

        final MockControl mockVersionTransformerControl = MockControl.createStrictControl(VersionTransformer.class);
        final VersionTransformer mockVersionTransformer = (VersionTransformer) mockVersionTransformerControl.getMock();
        mockVersionTransformer.transform(projectImportMapper, externalNodeAssociation);
        mockVersionTransformerControl.setReturnValue(transformedExternalNodeAssociation);
        mockVersionTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createAssociation(transformedExternalNodeAssociation);
        mockProjectImportPersisterControl.setReturnValue(false);
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("34");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        VersionPersisterHandler versionPersisterHandler = new VersionPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, new ExecutorForTests())
        {
            NodeAssociationParser getNodeAssociationParser()
            {
                return mockNodeAssociationParser;
            }

            VersionTransformer getVersionTransformer()
            {
                return mockVersionTransformer;
            }
        };

        versionPersisterHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, null);
        versionPersisterHandler.handleEntity("NOTVersion", null);

        assertEquals(1, projectImportResults.getErrors().size());
        assertTrue(projectImportResults.getErrors().contains("There was a problem saving affects version 'Version 1' for issue 'TST-1'."));
        mockVersionTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
        mockNodeAssociationParserControl.verify();
    }

    @Test
    public void testHandleErrorAddingFixVersion() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getVersionMapper().registerOldValue("12", "Version 1");
        final ExternalNodeAssociation externalNodeAssociation = new ExternalNodeAssociation("34", "", "12", "", NodeAssociationParser.FIX_VERSION_TYPE);

        final ExternalNodeAssociation transformedExternalNodeAssociation = new ExternalNodeAssociation("34", "", "12", "", NodeAssociationParser.FIX_VERSION_TYPE);

        final MockControl mockNodeAssociationParserControl = MockControl.createStrictControl(NodeAssociationParser.class);
        final NodeAssociationParser mockNodeAssociationParser = (NodeAssociationParser) mockNodeAssociationParserControl.getMock();
        mockNodeAssociationParser.parse(null);
        mockNodeAssociationParserControl.setReturnValue(externalNodeAssociation);
        mockNodeAssociationParserControl.replay();

        final MockControl mockVersionTransformerControl = MockControl.createStrictControl(VersionTransformer.class);
        final VersionTransformer mockVersionTransformer = (VersionTransformer) mockVersionTransformerControl.getMock();
        mockVersionTransformer.transform(projectImportMapper, externalNodeAssociation);
        mockVersionTransformerControl.setReturnValue(transformedExternalNodeAssociation);
        mockVersionTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createAssociation(transformedExternalNodeAssociation);
        mockProjectImportPersisterControl.setReturnValue(false);
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("34");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        VersionPersisterHandler versionPersisterHandler = new VersionPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, new ExecutorForTests())
        {
            NodeAssociationParser getNodeAssociationParser()
            {
                return mockNodeAssociationParser;
            }

            VersionTransformer getVersionTransformer()
            {
                return mockVersionTransformer;
            }
        };

        versionPersisterHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, null);
        versionPersisterHandler.handleEntity("NOTVersion", null);

        assertEquals(1, projectImportResults.getErrors().size());
        assertTrue(projectImportResults.getErrors().contains("There was a problem saving fix version 'Version 1' for issue 'TST-1'."));
        mockVersionTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
        mockNodeAssociationParserControl.verify();
    }


    @Test
    public void testHandleErrorNullIssueId() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getVersionMapper().registerOldValue("12", "Version 1");
        final ExternalNodeAssociation externalNodeAssociation = new ExternalNodeAssociation("34", "", "12", "", NodeAssociationParser.FIX_VERSION_TYPE);

        final ExternalNodeAssociation transformedExternalNodeAssociation = new ExternalNodeAssociation(null, "", "12", "", NodeAssociationParser.FIX_VERSION_TYPE);

        final MockControl mockNodeAssociationParserControl = MockControl.createStrictControl(NodeAssociationParser.class);
        final NodeAssociationParser mockNodeAssociationParser = (NodeAssociationParser) mockNodeAssociationParserControl.getMock();
        mockNodeAssociationParser.parse(null);
        mockNodeAssociationParserControl.setReturnValue(externalNodeAssociation);
        mockNodeAssociationParserControl.replay();

        final MockControl mockVersionTransformerControl = MockControl.createStrictControl(VersionTransformer.class);
        final VersionTransformer mockVersionTransformer = (VersionTransformer) mockVersionTransformerControl.getMock();
        mockVersionTransformer.transform(projectImportMapper, externalNodeAssociation);
        mockVersionTransformerControl.setReturnValue(transformedExternalNodeAssociation);
        mockVersionTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("34");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        VersionPersisterHandler versionPersisterHandler = new VersionPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, null)
        {
            NodeAssociationParser getNodeAssociationParser()
            {
                return mockNodeAssociationParser;
            }

            VersionTransformer getVersionTransformer()
            {
                return mockVersionTransformer;
            }
        };

        versionPersisterHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, null);
        versionPersisterHandler.handleEntity("NOTVersion", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockVersionTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
        mockNodeAssociationParserControl.verify();
    }

    @Test
    public void testHandleNonVersion() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        final MockControl mockNodeAssociationParserControl = MockControl.createStrictControl(NodeAssociationParser.class);
        final NodeAssociationParser mockNodeAssociationParser = (NodeAssociationParser) mockNodeAssociationParserControl.getMock();
        mockNodeAssociationParser.parse(null);
        mockNodeAssociationParserControl.setReturnValue(null);
        mockNodeAssociationParserControl.replay();

        final MockControl mockVersionTransformerControl = MockControl.createStrictControl(VersionTransformer.class);
        final VersionTransformer mockVersionTransformer = (VersionTransformer) mockVersionTransformerControl.getMock();
        mockVersionTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);
        VersionPersisterHandler versionPersisterHandler = new VersionPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, null, null)
        {
            NodeAssociationParser getNodeAssociationParser()
            {
                return mockNodeAssociationParser;
            }

            VersionTransformer getVersionTransformer()
            {
                return mockVersionTransformer;
            }
        };

        versionPersisterHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, null);
        versionPersisterHandler.handleEntity("NOTVersion", null);

        mockVersionTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
        mockNodeAssociationParserControl.verify();
    }

}
