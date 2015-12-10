package com.atlassian.jira.imports.project.handler;

import java.util.Collections;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalLink;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.BackupSystemInformationImpl;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.IssueLinkParser;
import com.atlassian.jira.imports.project.transformer.IssueLinkTransformer;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.google.common.collect.ImmutableMap;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestIssueLinkPersisterHandler
{
    @Test
    public void testHandleSomeOtherEntity() throws ParseException, AbortImportException
    {
        IssueLinkPersisterHandler issueLinkPersisterHandler = new IssueLinkPersisterHandler(null, null, null, null, null, null, null);
        issueLinkPersisterHandler.startDocument();
        issueLinkPersisterHandler.handleEntity("SomeRubbish", Collections.EMPTY_MAP);
        issueLinkPersisterHandler.endDocument();

        // Basically I am asserting that we don't try to persist anything, if we did, we would get an NPE because we pass a null ProjectImportPersister
    }

    @Test
    public void testHandleIssueLinkCantCreateLink() throws ParseException, AbortImportException
    {
        // ExternalLink
        ExternalLink externalLink = new ExternalLink();
        externalLink.setLinkType("12");
        externalLink.setSourceId("101");
        externalLink.setDestinationId("102");
        externalLink.setLinkType("4");

        // Mock IssueLinkParser
        final MockControl mockIssueLinkParserControl = MockControl.createStrictControl(IssueLinkParser.class);
        final IssueLinkParser mockIssueLinkParser = (IssueLinkParser) mockIssueLinkParserControl.getMock();
        mockIssueLinkParser.parse(null);
        mockIssueLinkParserControl.setReturnValue(externalLink);
        mockIssueLinkParserControl.replay();

        // Mock IssueLinkTransformer
        final MockControl mockIssueLinkTransformerControl = MockControl.createStrictControl(IssueLinkTransformer.class);
        final IssueLinkTransformer mockIssueLinkTransformer = (IssueLinkTransformer) mockIssueLinkTransformerControl.getMock();
        mockIssueLinkTransformer.transform(null, externalLink);
        // Return a null transformed IssueLink - this means don't create.
        mockIssueLinkTransformerControl.setReturnValue(null);
        mockIssueLinkTransformerControl.replay();

        // Mock ProjectImportPersister
        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        // createEntity() should not be called - the verify below will check.
        // mockProjectImportPersister.createEntity(entityRepresentation);
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        IssueLinkPersisterHandler issueLinkPersisterHandler = new IssueLinkPersisterHandler(mockProjectImportPersister, null, null, projectImportResults, null, null, null)
        {
            IssueLinkTransformer getIssueLinkTransformer()
            {
                return mockIssueLinkTransformer;
            }

            IssueLinkParser getIssueLinkParser()
            {
                return mockIssueLinkParser;
            }
        };
        issueLinkPersisterHandler.startDocument();
        issueLinkPersisterHandler.handleEntity("IssueLink", null);
        issueLinkPersisterHandler.endDocument();
        // The point of this test is to verify that ProjectImportPersister.createEntity() is called correctly

        // Verify Mock IssueLinkParser
        mockIssueLinkParserControl.verify();
        // Verify Mock IssueLinkTransformer
        mockIssueLinkTransformerControl.verify();
        // Verify Mock ProjectImportPersister
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleIssueLinkInvalidLink() throws ParseException, AbortImportException
    {
        // ExternalLink
        ExternalLink externalLink = new ExternalLink();
        externalLink.setLinkType("12");
        externalLink.setSourceId("101");
        externalLink.setDestinationId(null);
        externalLink.setLinkType("4");

        // Mock IssueLinkParser
        final MockControl mockIssueLinkParserControl = MockControl.createStrictControl(IssueLinkParser.class);
        final IssueLinkParser mockIssueLinkParser = (IssueLinkParser) mockIssueLinkParserControl.getMock();
        mockIssueLinkParser.parse(null);
        mockIssueLinkParserControl.setReturnValue(externalLink);
        mockIssueLinkParserControl.replay();

        // Mock IssueLinkTransformer
        final MockControl mockIssueLinkTransformerControl = MockControl.createStrictControl(IssueLinkTransformer.class);
        final IssueLinkTransformer mockIssueLinkTransformer = (IssueLinkTransformer) mockIssueLinkTransformerControl.getMock();
        // transformer should nto be called.
        mockIssueLinkTransformerControl.replay();

        // Mock ProjectImportPersister
        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        // createEntity() should not be called - the verify below will check.
        // mockProjectImportPersister.createEntity(entityRepresentation);
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        IssueLinkPersisterHandler issueLinkPersisterHandler = new IssueLinkPersisterHandler(mockProjectImportPersister, null, null, projectImportResults, null, null, null)
        {
            IssueLinkTransformer getIssueLinkTransformer()
            {
                return mockIssueLinkTransformer;
            }

            IssueLinkParser getIssueLinkParser()
            {
                return mockIssueLinkParser;
            }
        };
        issueLinkPersisterHandler.startDocument();
        issueLinkPersisterHandler.handleEntity("IssueLink", null);
        issueLinkPersisterHandler.endDocument();
        // The point of this test is to verify that ProjectImportPersister.createEntity() is called correctly

        // Verify Mock IssueLinkParser
        mockIssueLinkParserControl.verify();
        // Verify Mock IssueLinkTransformer
        mockIssueLinkTransformerControl.verify();
        // Verify Mock ProjectImportPersister
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleIssueLinkHappyPathLinkNotOutsideProject() throws ParseException, AbortImportException
    {
        // ExternalLink
        ExternalLink externalLink = new ExternalLink();
        externalLink.setLinkType("12");
        externalLink.setSourceId("101");
        externalLink.setDestinationId("102");
        externalLink.setLinkType("4");

        EntityRepresentation entityRepresentation = new EntityRepresentationImpl("IssueLink", ImmutableMap.<String,String>of("id", "12", "linktype", "4", "source", "101", "destination", "102"));
        // Mock IssueLinkParser
        final MockControl mockIssueLinkParserControl = MockControl.createStrictControl(IssueLinkParser.class);
        final IssueLinkParser mockIssueLinkParser = (IssueLinkParser) mockIssueLinkParserControl.getMock();
        mockIssueLinkParser.parse(null);
        mockIssueLinkParserControl.setReturnValue(externalLink);
        mockIssueLinkParser.getEntityRepresentation(externalLink);
        mockIssueLinkParserControl.setReturnValue(entityRepresentation);
        mockIssueLinkParserControl.replay();

        // Mock IssueLinkTransformer
        final MockControl mockIssueLinkTransformerControl = MockControl.createStrictControl(IssueLinkTransformer.class);
        final IssueLinkTransformer mockIssueLinkTransformer = (IssueLinkTransformer) mockIssueLinkTransformerControl.getMock();
        mockIssueLinkTransformer.transform(null, externalLink);
        mockIssueLinkTransformerControl.setReturnValue(externalLink);
        mockIssueLinkTransformerControl.replay();

        // Mock ProjectImportPersister
        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(entityRepresentation);
        mockProjectImportPersisterControl.setReturnValue(new Long(12));
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        IssueLinkPersisterHandler issueLinkPersisterHandler = new IssueLinkPersisterHandler(mockProjectImportPersister, null, null, projectImportResults, null, new ExecutorForTests(), null)
        {
            IssueLinkTransformer getIssueLinkTransformer()
            {
                return mockIssueLinkTransformer;
            }

            IssueLinkParser getIssueLinkParser()
            {
                return mockIssueLinkParser;
            }

            boolean issueIsOutsideCurrentProject(final String issueId)
            {
                return false;
            }
        };
        issueLinkPersisterHandler.startDocument();
        issueLinkPersisterHandler.handleEntity("IssueLink", null);
        issueLinkPersisterHandler.endDocument();
        // The point of this test is to verify that ProjectImportPersister.createEntity() is called correctly

        // Verify Mock IssueLinkParser
        mockIssueLinkParserControl.verify();
        // Verify Mock IssueLinkTransformer
        mockIssueLinkTransformerControl.verify();
        // Verify Mock ProjectImportPersister
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleIssueLinkHappyPathLinkOutsideProject() throws ParseException, AbortImportException
    {
        // ExternalLink
        ExternalLink externalLink = new ExternalLink();
        externalLink.setLinkType("12");
        externalLink.setSourceId("101");
        externalLink.setDestinationId("102");
        externalLink.setLinkType("4");

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getIssueMapper().mapValue("101", "101");
        BackupSystemInformation backupSystemInformation = new BackupSystemInformationImpl("121", "prof", Collections.EMPTY_LIST, true, EasyMap.build("101", "TST-1"), 2);

        EntityRepresentation entityRepresentation = new EntityRepresentationImpl("IssueLink", ImmutableMap.<String,String>of("id", "12", "linktype", "4", "source", "101", "destination", "102"));
        // Mock IssueLinkParser
        final MockControl mockIssueLinkParserControl = MockControl.createStrictControl(IssueLinkParser.class);
        final IssueLinkParser mockIssueLinkParser = (IssueLinkParser) mockIssueLinkParserControl.getMock();
        mockIssueLinkParser.parse(null);
        mockIssueLinkParserControl.setReturnValue(externalLink);
        mockIssueLinkParser.getEntityRepresentation(externalLink);
        mockIssueLinkParserControl.setReturnValue(entityRepresentation);
        mockIssueLinkParserControl.replay();

        // Mock IssueLinkTransformer
        final MockControl mockIssueLinkTransformerControl = MockControl.createStrictControl(IssueLinkTransformer.class);
        final IssueLinkTransformer mockIssueLinkTransformer = (IssueLinkTransformer) mockIssueLinkTransformerControl.getMock();
        mockIssueLinkTransformer.transform(projectImportMapper, externalLink);
        mockIssueLinkTransformerControl.setReturnValue(externalLink);
        mockIssueLinkTransformerControl.replay();

        // Mock ProjectImportPersister
        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(entityRepresentation);
        mockProjectImportPersisterControl.setReturnValue(new Long(12));
        mockProjectImportPersister.createChangeItemForIssueLinkIfNeeded("102", "4", "TST-1", false, null);
        mockProjectImportPersisterControl.setReturnValue("5555");
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        IssueLinkPersisterHandler issueLinkPersisterHandler = new IssueLinkPersisterHandler(mockProjectImportPersister, projectImportMapper, null, projectImportResults, backupSystemInformation, new ExecutorForTests(), null)
        {
            IssueLinkTransformer getIssueLinkTransformer()
            {
                return mockIssueLinkTransformer;
            }

            IssueLinkParser getIssueLinkParser()
            {
                return mockIssueLinkParser;
            }
        };
        issueLinkPersisterHandler.startDocument();
        issueLinkPersisterHandler.handleEntity("IssueLink", null);
        issueLinkPersisterHandler.endDocument();
        // The point of this test is to verify that ProjectImportPersister.createEntity() is called correctly

        assertEquals("5555", projectImportMapper.getIssueMapper().getMappedId("5555"));
        // Verify Mock IssueLinkParser
        mockIssueLinkParserControl.verify();
        // Verify Mock IssueLinkTransformer
        mockIssueLinkTransformerControl.verify();
        // Verify Mock ProjectImportPersister
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleErrorAddingIssuelink() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalLink externalIssuelink = new ExternalLink();
        externalIssuelink.setDestinationId("34");
        externalIssuelink.setSourceId("4");
        externalIssuelink.setId("12");

        final MockControl mockIssuelinkParserControl = MockControl.createStrictControl(IssueLinkParser.class);
        final IssueLinkParser mockIssuelinkParser = (IssueLinkParser) mockIssuelinkParserControl.getMock();
        mockIssuelinkParser.parse(null);
        mockIssuelinkParserControl.setReturnValue(externalIssuelink);
        mockIssuelinkParser.getEntityRepresentation(externalIssuelink);
        mockIssuelinkParserControl.setReturnValue(null);
        mockIssuelinkParserControl.replay();

        final MockControl mockIssuelinkTransformerControl = MockControl.createStrictControl(IssueLinkTransformer.class);
        final IssueLinkTransformer mockIssuelinkTransformer = (IssueLinkTransformer) mockIssuelinkTransformerControl.getMock();
        mockIssuelinkTransformer.transform(projectImportMapper, externalIssuelink);
        mockIssuelinkTransformerControl.setReturnValue(externalIssuelink);
        mockIssuelinkTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(null);
        mockProjectImportPersisterControl.setReturnValue(null);
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("4");
        mockBackupSystemInformationControl.setReturnValue("TST-2");
        mockBackupSystemInformation.getIssueKeyForId("34");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        IssueLinkPersisterHandler issuelinkPersisterHandler = new IssueLinkPersisterHandler(mockProjectImportPersister, projectImportMapper, null, projectImportResults, mockBackupSystemInformation, new ExecutorForTests(), null)
        {
            IssueLinkParser getIssueLinkParser()
            {
                return mockIssuelinkParser;
            }

            IssueLinkTransformer getIssueLinkTransformer()
            {
                return mockIssuelinkTransformer;
            }
        };

        issuelinkPersisterHandler.handleEntity(IssueLinkParser.ISSUE_LINK_ENTITY_NAME, null);
        issuelinkPersisterHandler.handleEntity("NOTIssuelink", null);

        assertEquals(1, projectImportResults.getErrors().size());
        assertTrue(projectImportResults.getErrors().contains("There was a problem saving issue link between issue 'TST-2' and issue 'TST-1'."));
        mockIssuelinkParserControl.verify();
        mockIssuelinkTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleErrorNullIssueId() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalLink externalIssuelink = new ExternalLink();
        externalIssuelink.setId("12");
        externalIssuelink.setDestinationId("34");
        externalIssuelink.setSourceId("4");

        final ExternalLink transformedExternalIssueLink = new ExternalLink();
        transformedExternalIssueLink.setId("12");

        final MockControl mockIssueLinkParserControl = MockControl.createStrictControl(IssueLinkParser.class);
        final IssueLinkParser mockIssueLinkParser = (IssueLinkParser) mockIssueLinkParserControl.getMock();
        mockIssueLinkParser.parse(null);
        mockIssueLinkParserControl.setReturnValue(externalIssuelink);
        mockIssueLinkParserControl.replay();

        final MockControl mockIssueLinkTransformerControl = MockControl.createStrictControl(IssueLinkTransformer.class);
        final IssueLinkTransformer mockIssueLinkTransformer = (IssueLinkTransformer) mockIssueLinkTransformerControl.getMock();
        mockIssueLinkTransformer.transform(projectImportMapper, externalIssuelink);
        mockIssueLinkTransformerControl.setReturnValue(null);
        mockIssueLinkTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        IssueLinkPersisterHandler IssueLinkPersisterHandler = new IssueLinkPersisterHandler(mockProjectImportPersister, projectImportMapper, null, projectImportResults, null, null, null)
        {
            IssueLinkParser getIssueLinkParser()
            {
                return mockIssueLinkParser;
            }

            IssueLinkTransformer getIssueLinkTransformer()
            {
                return mockIssueLinkTransformer;
            }
        };

        IssueLinkPersisterHandler.handleEntity(IssueLinkParser.ISSUE_LINK_ENTITY_NAME, null);
        IssueLinkPersisterHandler.handleEntity("NOTIssueLink", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockIssueLinkParserControl.verify();
        mockIssueLinkTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }

}
