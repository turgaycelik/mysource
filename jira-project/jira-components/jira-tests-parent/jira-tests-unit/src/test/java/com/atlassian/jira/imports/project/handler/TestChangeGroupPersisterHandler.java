package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.external.beans.ExternalChangeGroup;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.ChangeGroupParser;
import com.atlassian.jira.imports.project.transformer.ChangeGroupTransformer;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestChangeGroupPersisterHandler
{

    @Test
    public void testHandle() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalChangeGroup externalChangeGroup = new ExternalChangeGroup();
        externalChangeGroup.setId("111");
        externalChangeGroup.setIssueId("34");

        final MockControl mockChangeGroupParserControl = MockControl.createStrictControl(ChangeGroupParser.class);
        final ChangeGroupParser mockChangeGroupParser = (ChangeGroupParser) mockChangeGroupParserControl.getMock();
        mockChangeGroupParser.parse(null);
        mockChangeGroupParserControl.setReturnValue(externalChangeGroup);
        mockChangeGroupParser.getEntityRepresentation(externalChangeGroup);
        mockChangeGroupParserControl.setReturnValue(null);
        mockChangeGroupParserControl.replay();

        final MockControl mockChangeGroupTransformerControl = MockControl.createStrictControl(ChangeGroupTransformer.class);
        final ChangeGroupTransformer mockChangeGroupTransformer = (ChangeGroupTransformer) mockChangeGroupTransformerControl.getMock();
        mockChangeGroupTransformer.transform(projectImportMapper, externalChangeGroup);
        mockChangeGroupTransformerControl.setReturnValue(externalChangeGroup);
        mockChangeGroupTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(null);
        mockProjectImportPersisterControl.setReturnValue(new Long(123));
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        ChangeGroupPersisterHandler ChangeGroupPersisterHandler = new ChangeGroupPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, null, new ExecutorForTests())
        {
            ChangeGroupParser getChangeGroupParser()
            {
                return mockChangeGroupParser;
            }

            ChangeGroupTransformer getChangeGroupTransformer()
            {
                return mockChangeGroupTransformer;
            }
        };

        ChangeGroupPersisterHandler.handleEntity(ChangeGroupParser.CHANGE_GROUP_ENTITY_NAME, null);
        ChangeGroupPersisterHandler.handleEntity("NOTChangeGroup", null);

        assertEquals("123", projectImportMapper.getChangeGroupMapper().getMappedId("111"));
        assertEquals(0, projectImportResults.getErrors().size());
        mockChangeGroupParserControl.verify();
        mockChangeGroupTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleNullTransformedChangeGroup() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalChangeGroup externalChangeGroup = new ExternalChangeGroup();
        externalChangeGroup.setIssueId("34");

        ExternalChangeGroup transformedExternalChangeGroup = new ExternalChangeGroup();

        final MockControl mockChangeGroupParserControl = MockControl.createStrictControl(ChangeGroupParser.class);
        final ChangeGroupParser mockChangeGroupParser = (ChangeGroupParser) mockChangeGroupParserControl.getMock();
        mockChangeGroupParser.parse(null);
        mockChangeGroupParserControl.setReturnValue(externalChangeGroup);
        mockChangeGroupParserControl.replay();

        final MockControl mockChangeGroupTransformerControl = MockControl.createStrictControl(ChangeGroupTransformer.class);
        final ChangeGroupTransformer mockChangeGroupTransformer = (ChangeGroupTransformer) mockChangeGroupTransformerControl.getMock();
        mockChangeGroupTransformer.transform(projectImportMapper, externalChangeGroup);
        mockChangeGroupTransformerControl.setReturnValue(transformedExternalChangeGroup);
        mockChangeGroupTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("34");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        ChangeGroupPersisterHandler ChangeGroupPersisterHandler = new ChangeGroupPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, null)
        {
            ChangeGroupParser getChangeGroupParser()
            {
                return mockChangeGroupParser;
            }

            ChangeGroupTransformer getChangeGroupTransformer()
            {
                return mockChangeGroupTransformer;
            }
        };

        ChangeGroupPersisterHandler.handleEntity(ChangeGroupParser.CHANGE_GROUP_ENTITY_NAME, null);
        ChangeGroupPersisterHandler.handleEntity("NOTChangeGroup", null);

        mockChangeGroupParserControl.verify();
        mockChangeGroupTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }


    @Test
    public void testHandleErrorAddingChangegroup() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalChangeGroup externalChangeGroup = new ExternalChangeGroup();
        externalChangeGroup.setIssueId("34");
        externalChangeGroup.setId("12");

        final MockControl mockChangeGroupParserControl = MockControl.createStrictControl(ChangeGroupParser.class);
        final ChangeGroupParser mockChangeGroupParser = (ChangeGroupParser) mockChangeGroupParserControl.getMock();
        mockChangeGroupParser.parse(null);
        mockChangeGroupParserControl.setReturnValue(externalChangeGroup);
        mockChangeGroupParser.getEntityRepresentation(externalChangeGroup);
        mockChangeGroupParserControl.setReturnValue(null);
        mockChangeGroupParserControl.replay();

        final MockControl mockChangeGroupTransformerControl = MockControl.createStrictControl(ChangeGroupTransformer.class);
        final ChangeGroupTransformer mockChangeGroupTransformer = (ChangeGroupTransformer) mockChangeGroupTransformerControl.getMock();
        mockChangeGroupTransformer.transform(projectImportMapper, externalChangeGroup);
        mockChangeGroupTransformerControl.setReturnValue(externalChangeGroup);
        mockChangeGroupTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(null);
        mockProjectImportPersisterControl.setReturnValue(null);
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("34");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        ChangeGroupPersisterHandler changegroupPersisterHandler = new ChangeGroupPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, new ExecutorForTests())
        {
            ChangeGroupParser getChangeGroupParser()
            {
                return mockChangeGroupParser;
            }

            ChangeGroupTransformer getChangeGroupTransformer()
            {
                return mockChangeGroupTransformer;
            }
        };

        changegroupPersisterHandler.handleEntity(ChangeGroupParser.CHANGE_GROUP_ENTITY_NAME, null);
        changegroupPersisterHandler.handleEntity("NOTChangeGroup", null);

        assertEquals(1, projectImportResults.getErrors().size());
        assertTrue(projectImportResults.getErrors().contains("There was a problem saving change group with id '12' for issue 'TST-1'."));
        mockChangeGroupParserControl.verify();
        mockChangeGroupTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }
}
