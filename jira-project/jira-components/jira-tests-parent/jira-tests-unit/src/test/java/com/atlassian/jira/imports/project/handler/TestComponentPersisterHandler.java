package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.external.beans.ExternalNodeAssociation;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.NodeAssociationParser;
import com.atlassian.jira.imports.project.transformer.ComponentTransformer;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestComponentPersisterHandler
{
    @Test
    public void testHandle() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalNodeAssociation externalNodeAssociation = new ExternalNodeAssociation("", "", "", "", NodeAssociationParser.COMPONENT_TYPE);

        final MockControl mockNodeAssociationParserControl = MockControl.createStrictControl(NodeAssociationParser.class);
        final NodeAssociationParser mockNodeAssociationParser = (NodeAssociationParser) mockNodeAssociationParserControl.getMock();
        mockNodeAssociationParser.parse(null);
        mockNodeAssociationParserControl.setReturnValue(externalNodeAssociation);
        mockNodeAssociationParserControl.replay();

        final MockControl mockComponentTransformerControl = MockControl.createStrictControl(ComponentTransformer.class);
        final ComponentTransformer mockComponentTransformer = (ComponentTransformer) mockComponentTransformerControl.getMock();
        mockComponentTransformer.transform(projectImportMapper, externalNodeAssociation);
        mockComponentTransformerControl.setReturnValue(externalNodeAssociation);
        mockComponentTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createAssociation(externalNodeAssociation);
        mockProjectImportPersisterControl.setReturnValue(true);
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        ComponentPersisterHandler componentPersisterHandler = new ComponentPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, null, new ExecutorForTests())
        {
            NodeAssociationParser getNodeAssociationParser()
            {
                return mockNodeAssociationParser;
            }

            ComponentTransformer getComponentTransformer()
            {
                return mockComponentTransformer;
            }
        };

        componentPersisterHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, null);
        componentPersisterHandler.handleEntity("NOTComponent", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockComponentTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
        mockNodeAssociationParserControl.verify();
    }

    @Test
    public void testHandleNonComponent() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        final MockControl mockNodeAssociationParserControl = MockControl.createStrictControl(NodeAssociationParser.class);
        final NodeAssociationParser mockNodeAssociationParser = (NodeAssociationParser) mockNodeAssociationParserControl.getMock();
        mockNodeAssociationParser.parse(null);
        mockNodeAssociationParserControl.setReturnValue(null);
        mockNodeAssociationParserControl.replay();

        final MockControl mockComponentTransformerControl = MockControl.createStrictControl(ComponentTransformer.class);
        final ComponentTransformer mockComponentTransformer = (ComponentTransformer) mockComponentTransformerControl.getMock();
        mockComponentTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        ComponentPersisterHandler componentPersisterHandler = new ComponentPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, null, null)
        {
            NodeAssociationParser getNodeAssociationParser()
            {
                return mockNodeAssociationParser;
            }

            ComponentTransformer getComponentTransformer()
            {
                return mockComponentTransformer;
            }
        };

        componentPersisterHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, null);
        componentPersisterHandler.handleEntity("NOTComponent", null);

        mockComponentTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
        mockNodeAssociationParserControl.verify();
    }

    @Test
    public void testHandleErrorAddingFixComponent() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getComponentMapper().registerOldValue("12", "Component 1");
        final ExternalNodeAssociation externalNodeAssociation = new ExternalNodeAssociation("34", "", "12", "", NodeAssociationParser.COMPONENT_TYPE);

        final ExternalNodeAssociation transformedExternalNodeAssociation = new ExternalNodeAssociation("34", "", "12", "", NodeAssociationParser.COMPONENT_TYPE);

        final MockControl mockNodeAssociationParserControl = MockControl.createStrictControl(NodeAssociationParser.class);
        final NodeAssociationParser mockNodeAssociationParser = (NodeAssociationParser) mockNodeAssociationParserControl.getMock();
        mockNodeAssociationParser.parse(null);
        mockNodeAssociationParserControl.setReturnValue(externalNodeAssociation);
        mockNodeAssociationParserControl.replay();

        final MockControl mockComponentTransformerControl = MockControl.createStrictControl(ComponentTransformer.class);
        final ComponentTransformer mockComponentTransformer = (ComponentTransformer) mockComponentTransformerControl.getMock();
        mockComponentTransformer.transform(projectImportMapper, externalNodeAssociation);
        mockComponentTransformerControl.setReturnValue(transformedExternalNodeAssociation);
        mockComponentTransformerControl.replay();

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
        ComponentPersisterHandler componentPersisterHandler = new ComponentPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, new ExecutorForTests())
        {
            NodeAssociationParser getNodeAssociationParser()
            {
                return mockNodeAssociationParser;
            }

            ComponentTransformer getComponentTransformer()
            {
                return mockComponentTransformer;
            }
        };

        componentPersisterHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, null);
        componentPersisterHandler.handleEntity("NOTComponent", null);

        assertEquals(1, projectImportResults.getErrors().size());
        assertTrue(projectImportResults.getErrors().contains("There was a problem saving component 'Component 1' for issue 'TST-1'."));
        mockComponentTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
        mockNodeAssociationParserControl.verify();
    }

    @Test
    public void testHandleErrorNullIssueId() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getComponentMapper().registerOldValue("12", "Component 1");
        final ExternalNodeAssociation externalNodeAssociation = new ExternalNodeAssociation("34", "", "12", "", NodeAssociationParser.COMPONENT_TYPE);

        final ExternalNodeAssociation transformedExternalNodeAssociation = new ExternalNodeAssociation(null, "", "12", "", NodeAssociationParser.COMPONENT_TYPE);

        final MockControl mockNodeAssociationParserControl = MockControl.createStrictControl(NodeAssociationParser.class);
        final NodeAssociationParser mockNodeAssociationParser = (NodeAssociationParser) mockNodeAssociationParserControl.getMock();
        mockNodeAssociationParser.parse(null);
        mockNodeAssociationParserControl.setReturnValue(externalNodeAssociation);
        mockNodeAssociationParserControl.replay();

        final MockControl mockComponentTransformerControl = MockControl.createStrictControl(ComponentTransformer.class);
        final ComponentTransformer mockComponentTransformer = (ComponentTransformer) mockComponentTransformerControl.getMock();
        mockComponentTransformer.transform(projectImportMapper, externalNodeAssociation);
        mockComponentTransformerControl.setReturnValue(transformedExternalNodeAssociation);
        mockComponentTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("34");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        ComponentPersisterHandler componentPersisterHandler = new ComponentPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, null)
        {
            NodeAssociationParser getNodeAssociationParser()
            {
                return mockNodeAssociationParser;
            }

            ComponentTransformer getComponentTransformer()
            {
                return mockComponentTransformer;
            }
        };

        componentPersisterHandler.handleEntity(NodeAssociationParser.NODE_ASSOCIATION_ENTITY_NAME, null);
        componentPersisterHandler.handleEntity("NOTComponent", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockComponentTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
        mockNodeAssociationParserControl.verify();
    }

}
