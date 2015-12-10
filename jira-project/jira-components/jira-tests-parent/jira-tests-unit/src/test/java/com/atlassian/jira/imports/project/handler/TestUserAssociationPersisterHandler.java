package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.external.beans.ExternalVoter;
import com.atlassian.jira.external.beans.ExternalWatcher;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.UserAssociationParser;
import com.atlassian.jira.imports.project.transformer.VoterTransformer;
import com.atlassian.jira.imports.project.transformer.WatcherTransformer;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestUserAssociationPersisterHandler
{
    @Test
    public void testHandleVoter() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setIssueId("12");
        externalVoter.setVoter("admin");

        final MockControl mockUserAssociationParserControl = MockControl.createStrictControl(UserAssociationParser.class);
        final UserAssociationParser mockUserAssociationParser = (UserAssociationParser) mockUserAssociationParserControl.getMock();
        mockUserAssociationParser.parseVoter(null);
        mockUserAssociationParserControl.setReturnValue(externalVoter);
        mockUserAssociationParser.parseWatcher(null);
        mockUserAssociationParserControl.setReturnValue(null);
        mockUserAssociationParserControl.replay();

        final MockControl mockVoterTransformerControl = MockControl.createStrictControl(VoterTransformer.class);
        final VoterTransformer mockVoterTransformer = (VoterTransformer) mockVoterTransformerControl.getMock();
        mockVoterTransformer.transform(projectImportMapper, externalVoter);
        mockVoterTransformerControl.setReturnValue(externalVoter);
        mockVoterTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createVoter(externalVoter);
        mockProjectImportPersisterControl.setReturnValue(true);
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);
        UserAssociationPersisterHandler userAssociationPersisterHandler = new UserAssociationPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, null, new ExecutorForTests())
        {
            UserAssociationParser getUserAssociationParser()
            {
                return mockUserAssociationParser;
            }

            VoterTransformer getVoterTransformer()
            {
                return mockVoterTransformer;
            }
        };

        userAssociationPersisterHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, null);
        userAssociationPersisterHandler.handleEntity("NOTVoter", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockProjectImportPersisterControl.verify();
        mockVoterTransformerControl.verify();
        mockUserAssociationParserControl.verify();
    }

    @Test
    public void testHandleVoterSaveError() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setIssueId("12");
        externalVoter.setVoter("admin");

        final MockControl mockUserAssociationParserControl = MockControl.createStrictControl(UserAssociationParser.class);
        final UserAssociationParser mockUserAssociationParser = (UserAssociationParser) mockUserAssociationParserControl.getMock();
        mockUserAssociationParser.parseVoter(null);
        mockUserAssociationParserControl.setReturnValue(externalVoter);
        mockUserAssociationParser.parseWatcher(null);
        mockUserAssociationParserControl.setReturnValue(null);
        mockUserAssociationParserControl.replay();

        final MockControl mockVoterTransformerControl = MockControl.createStrictControl(VoterTransformer.class);
        final VoterTransformer mockVoterTransformer = (VoterTransformer) mockVoterTransformerControl.getMock();
        mockVoterTransformer.transform(projectImportMapper, externalVoter);
        mockVoterTransformerControl.setReturnValue(externalVoter);
        mockVoterTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createVoter(externalVoter);
        mockProjectImportPersisterControl.setReturnValue(false);
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("12");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        UserAssociationPersisterHandler userAssociationPersisterHandler = new UserAssociationPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, new ExecutorForTests())
        {
            UserAssociationParser getUserAssociationParser()
            {
                return mockUserAssociationParser;
            }

            VoterTransformer getVoterTransformer()
            {
                return mockVoterTransformer;
            }
        };

        userAssociationPersisterHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, null);
        userAssociationPersisterHandler.handleEntity("NOTVoter", null);

        assertEquals(1, projectImportResults.getErrors().size());
        assertTrue(projectImportResults.getErrors().contains("There was a problem saving voter 'admin' for issue 'TST-1'."));
        mockProjectImportPersisterControl.verify();
        mockVoterTransformerControl.verify();
        mockUserAssociationParserControl.verify();
    }
    
    @Test
    public void testHandleVoterNullTransformed() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalVoter externalVoter = new ExternalVoter();
        externalVoter.setIssueId("12");
        externalVoter.setVoter("admin");

        ExternalVoter transformedExternalVoter = new ExternalVoter();
        transformedExternalVoter.setVoter("admin");

        final MockControl mockUserAssociationParserControl = MockControl.createStrictControl(UserAssociationParser.class);
        final UserAssociationParser mockUserAssociationParser = (UserAssociationParser) mockUserAssociationParserControl.getMock();
        mockUserAssociationParser.parseVoter(null);
        mockUserAssociationParserControl.setReturnValue(externalVoter);
        mockUserAssociationParser.parseWatcher(null);
        mockUserAssociationParserControl.setReturnValue(null);
        mockUserAssociationParserControl.replay();

        final MockControl mockVoterTransformerControl = MockControl.createStrictControl(VoterTransformer.class);
        final VoterTransformer mockVoterTransformer = (VoterTransformer) mockVoterTransformerControl.getMock();
        mockVoterTransformer.transform(projectImportMapper, externalVoter);
        mockVoterTransformerControl.setReturnValue(transformedExternalVoter);
        mockVoterTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("12");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);
        UserAssociationPersisterHandler userAssociationPersisterHandler = new UserAssociationPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, null)
        {
            UserAssociationParser getUserAssociationParser()
            {
                return mockUserAssociationParser;
            }

            VoterTransformer getVoterTransformer()
            {
                return mockVoterTransformer;
            }
        };

        userAssociationPersisterHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, null);
        userAssociationPersisterHandler.handleEntity("NOTVoter", null);

        mockProjectImportPersisterControl.verify();
        mockVoterTransformerControl.verify();
        mockUserAssociationParserControl.verify();
    }

    @Test
    public void testHandleWatcher() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setIssueId("12");
        externalWatcher.setWatcher("admin");

        final MockControl mockUserAssociationParserControl = MockControl.createStrictControl(UserAssociationParser.class);
        final UserAssociationParser mockUserAssociationParser = (UserAssociationParser) mockUserAssociationParserControl.getMock();
        mockUserAssociationParser.parseVoter(null);
        mockUserAssociationParserControl.setReturnValue(null);
        mockUserAssociationParser.parseWatcher(null);
        mockUserAssociationParserControl.setReturnValue(externalWatcher);
        mockUserAssociationParserControl.replay();

        final MockControl mockWatcherTransformerControl = MockControl.createStrictControl(WatcherTransformer.class);
        final WatcherTransformer mockWatcherTransformer = (WatcherTransformer) mockWatcherTransformerControl.getMock();
        mockWatcherTransformer.transform(projectImportMapper, externalWatcher);
        mockWatcherTransformerControl.setReturnValue(externalWatcher);
        mockWatcherTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createWatcher(externalWatcher);
        mockProjectImportPersisterControl.setReturnValue(true);
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);
        UserAssociationPersisterHandler userAssociationPersisterHandler = new UserAssociationPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, null, new ExecutorForTests())
        {
            UserAssociationParser getUserAssociationParser()
            {
                return mockUserAssociationParser;
            }

            WatcherTransformer getWatcherTransformer()
            {
                return mockWatcherTransformer;
            }
        };

        userAssociationPersisterHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, null);
        userAssociationPersisterHandler.handleEntity("NOTVoter", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockProjectImportPersisterControl.verify();
        mockWatcherTransformerControl.verify();
        mockUserAssociationParserControl.verify();
    }

    @Test
    public void testHandleWatcherNullTransformed() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setIssueId("12");
        externalWatcher.setWatcher("admin");

        ExternalWatcher transformedExternalWatcher = new ExternalWatcher();
        transformedExternalWatcher.setWatcher("admin");

        final MockControl mockUserAssociationParserControl = MockControl.createStrictControl(UserAssociationParser.class);
        final UserAssociationParser mockUserAssociationParser = (UserAssociationParser) mockUserAssociationParserControl.getMock();
        mockUserAssociationParser.parseVoter(null);
        mockUserAssociationParserControl.setReturnValue(null);
        mockUserAssociationParser.parseWatcher(null);
        mockUserAssociationParserControl.setReturnValue(externalWatcher);
        mockUserAssociationParserControl.replay();

        final MockControl mockWatcherTransformerControl = MockControl.createStrictControl(WatcherTransformer.class);
        final WatcherTransformer mockWatcherTransformer = (WatcherTransformer) mockWatcherTransformerControl.getMock();
        mockWatcherTransformer.transform(projectImportMapper, externalWatcher);
        mockWatcherTransformerControl.setReturnValue(transformedExternalWatcher);
        mockWatcherTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("12");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);
        UserAssociationPersisterHandler userAssociationPersisterHandler = new UserAssociationPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, null)
        {
            UserAssociationParser getUserAssociationParser()
            {
                return mockUserAssociationParser;
            }

            WatcherTransformer getWatcherTransformer()
            {
                return mockWatcherTransformer;
            }
        };

        userAssociationPersisterHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, null);
        userAssociationPersisterHandler.handleEntity("NOTVoter", null);

        mockProjectImportPersisterControl.verify();
        mockWatcherTransformerControl.verify();
        mockUserAssociationParserControl.verify();
    }

    @Test
    public void testHandleWatcherSaveError() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalWatcher externalWatcher = new ExternalWatcher();
        externalWatcher.setIssueId("12");
        externalWatcher.setWatcher("admin");

        final MockControl mockUserAssociationParserControl = MockControl.createStrictControl(UserAssociationParser.class);
        final UserAssociationParser mockUserAssociationParser = (UserAssociationParser) mockUserAssociationParserControl.getMock();
        mockUserAssociationParser.parseVoter(null);
        mockUserAssociationParserControl.setReturnValue(null);
        mockUserAssociationParser.parseWatcher(null);
        mockUserAssociationParserControl.setReturnValue(externalWatcher);
        mockUserAssociationParserControl.replay();

        final MockControl mockWatcherTransformerControl = MockControl.createStrictControl(WatcherTransformer.class);
        final WatcherTransformer mockWatcherTransformer = (WatcherTransformer) mockWatcherTransformerControl.getMock();
        mockWatcherTransformer.transform(projectImportMapper, externalWatcher);
        mockWatcherTransformerControl.setReturnValue(externalWatcher);
        mockWatcherTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createWatcher(externalWatcher);
        mockProjectImportPersisterControl.setReturnValue(false);
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("12");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        UserAssociationPersisterHandler userAssociationPersisterHandler = new UserAssociationPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, new ExecutorForTests())
        {
            UserAssociationParser getUserAssociationParser()
            {
                return mockUserAssociationParser;
            }

            WatcherTransformer getWatcherTransformer()
            {
                return mockWatcherTransformer;
            }
        };

        userAssociationPersisterHandler.handleEntity(UserAssociationParser.USER_ASSOCIATION_ENTITY_NAME, null);
        userAssociationPersisterHandler.handleEntity("NOTVoter", null);

        assertEquals(1, projectImportResults.getErrors().size());
        assertTrue(projectImportResults.getErrors().contains("There was a problem saving watcher 'admin' for issue 'TST-1'."));
        mockProjectImportPersisterControl.verify();
        mockWatcherTransformerControl.verify();
        mockUserAssociationParserControl.verify();
    }

}
