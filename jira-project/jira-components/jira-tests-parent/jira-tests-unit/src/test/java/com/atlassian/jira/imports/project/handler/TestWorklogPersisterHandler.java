package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.external.beans.ExternalWorklog;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.WorklogParser;
import com.atlassian.jira.imports.project.transformer.WorklogTransformer;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestWorklogPersisterHandler
{
    @Test
    public void testHandle() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalWorklog externalWorklog = new ExternalWorklog();
        externalWorklog.setIssueId("34");

        final MockControl mockWorklogParserControl = MockControl.createStrictControl(WorklogParser.class);
        final WorklogParser mockWorklogParser = (WorklogParser) mockWorklogParserControl.getMock();
        mockWorklogParser.parse(null);
        mockWorklogParserControl.setReturnValue(externalWorklog);
        mockWorklogParser.getEntityRepresentation(externalWorklog);
        mockWorklogParserControl.setReturnValue(null);
        mockWorklogParserControl.replay();

        final MockControl mockWorklogTransformerControl = MockControl.createStrictControl(WorklogTransformer.class);
        final WorklogTransformer mockWorklogTransformer = (WorklogTransformer) mockWorklogTransformerControl.getMock();
        mockWorklogTransformer.transform(projectImportMapper, externalWorklog);
        mockWorklogTransformerControl.setReturnValue(externalWorklog);
        mockWorklogTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(null);
        mockProjectImportPersisterControl.setReturnValue(new Long(12));
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);
        WorklogPersisterHandler worklogPersisterHandler = new WorklogPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, null, new ExecutorForTests())
        {
            WorklogParser getWorklogParser()
            {
                return mockWorklogParser;
            }

            WorklogTransformer getWorklogTransformer()
            {
                return mockWorklogTransformer;
            }
        };

        worklogPersisterHandler.handleEntity(WorklogParser.WORKLOG_ENTITY_NAME, null);
        worklogPersisterHandler.handleEntity("NOTWorklog", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockWorklogParserControl.verify();
        mockWorklogTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }
    
    @Test
    public void testHandleErrorAddingWorklog() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalWorklog externalWorklog = new ExternalWorklog();
        externalWorklog.setIssueId("34");
        externalWorklog.setId("12");

        final MockControl mockWorklogParserControl = MockControl.createStrictControl(WorklogParser.class);
        final WorklogParser mockWorklogParser = (WorklogParser) mockWorklogParserControl.getMock();
        mockWorklogParser.parse(null);
        mockWorklogParserControl.setReturnValue(externalWorklog);
        mockWorklogParser.getEntityRepresentation(externalWorklog);
        mockWorklogParserControl.setReturnValue(null);
        mockWorklogParserControl.replay();

        final MockControl mockWorklogTransformerControl = MockControl.createStrictControl(WorklogTransformer.class);
        final WorklogTransformer mockWorklogTransformer = (WorklogTransformer) mockWorklogTransformerControl.getMock();
        mockWorklogTransformer.transform(projectImportMapper, externalWorklog);
        mockWorklogTransformerControl.setReturnValue(externalWorklog);
        mockWorklogTransformerControl.replay();

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
        WorklogPersisterHandler worklogPersisterHandler = new WorklogPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, new ExecutorForTests())
        {
            WorklogParser getWorklogParser()
            {
                return mockWorklogParser;
            }

            WorklogTransformer getWorklogTransformer()
            {
                return mockWorklogTransformer;
            }
        };

        worklogPersisterHandler.handleEntity(WorklogParser.WORKLOG_ENTITY_NAME, null);
        worklogPersisterHandler.handleEntity("NOTWorklog", null);

        assertEquals(1, projectImportResults.getErrors().size());
        assertTrue(projectImportResults.getErrors().contains("There was a problem saving worklog with id '12' for issue 'TST-1'."));
        mockWorklogParserControl.verify();
        mockWorklogTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleErrorNullIssueId() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalWorklog externalWorklog = new ExternalWorklog();
        externalWorklog.setId("12");
        externalWorklog.setIssueId("34");

        final ExternalWorklog transformedExternalWorklog = new ExternalWorklog();
        transformedExternalWorklog.setId("12");

        final MockControl mockWorklogParserControl = MockControl.createStrictControl(WorklogParser.class);
        final WorklogParser mockWorklogParser = (WorklogParser) mockWorklogParserControl.getMock();
        mockWorklogParser.parse(null);
        mockWorklogParserControl.setReturnValue(externalWorklog);
        mockWorklogParserControl.replay();

        final MockControl mockWorklogTransformerControl = MockControl.createStrictControl(WorklogTransformer.class);
        final WorklogTransformer mockWorklogTransformer = (WorklogTransformer) mockWorklogTransformerControl.getMock();
        mockWorklogTransformer.transform(projectImportMapper, externalWorklog);
        mockWorklogTransformerControl.setReturnValue(transformedExternalWorklog);
        mockWorklogTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("34");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        WorklogPersisterHandler worklogPersisterHandler = new WorklogPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, null)
        {
            WorklogParser getWorklogParser()
            {
                return mockWorklogParser;
            }

            WorklogTransformer getWorklogTransformer()
            {
                return mockWorklogTransformer;
            }
        };

        worklogPersisterHandler.handleEntity(WorklogParser.WORKLOG_ENTITY_NAME, null);
        worklogPersisterHandler.handleEntity("NOTWorklog", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockWorklogParserControl.verify();
        mockWorklogTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }
}
