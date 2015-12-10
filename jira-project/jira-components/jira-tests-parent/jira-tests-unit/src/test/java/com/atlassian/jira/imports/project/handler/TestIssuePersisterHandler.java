package com.atlassian.jira.imports.project.handler;

import java.util.Date;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalIssueImpl;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.IssueParser;
import com.atlassian.jira.imports.project.transformer.IssueTransformer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v3.13
 */
public class TestIssuePersisterHandler
{
    @Test
    public void testLargestIssueKeyNumber() throws ParseException, AbortImportException
    {
        final Date importDate = new Date();
        final ExternalIssueImpl extIssue1 = new ExternalIssueImpl(null);
        final ExternalIssueImpl extIssue2 = new ExternalIssueImpl(null);
        final ExternalIssueImpl extIssue3 = new ExternalIssueImpl(null);
        
        final MockIssue issue1 = new MockIssue();
        issue1.setId(new Long(12));
        issue1.setKey("TST-10");
        final MockIssue issue2 = new MockIssue();
        issue2.setId(new Long(13));
        issue2.setKey("TST-164");
        final MockIssue issue3 = new MockIssue();
        issue3.setId(new Long(14));
        issue3.setKey("TST-18");
        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createIssue(extIssue1, importDate, null);
        mockProjectImportPersisterControl.setReturnValue(issue1);
        mockProjectImportPersister.createIssue(extIssue2, importDate, null);
        mockProjectImportPersisterControl.setReturnValue(issue2);
        mockProjectImportPersister.createIssue(extIssue3, importDate, null);
        mockProjectImportPersisterControl.setReturnValue(issue3);
        mockProjectImportPersisterControl.replay();

        final MockControl mockIssueParserControl = MockControl.createStrictControl(IssueParser.class);
        final IssueParser mockIssueParser = (IssueParser) mockIssueParserControl.getMock();
        mockIssueParser.parse(null);
        mockIssueParserControl.setReturnValue(extIssue1);
        mockIssueParser.parse(null);
        mockIssueParserControl.setReturnValue(extIssue2);
        mockIssueParser.parse(null);
        mockIssueParserControl.setReturnValue(extIssue3);
        mockIssueParserControl.replay();

        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        final MockControl mockIssueTransformerControl = MockControl.createStrictControl(IssueTransformer.class);
        final IssueTransformer mockIssueTransformer = (IssueTransformer) mockIssueTransformerControl.getMock();
        mockIssueTransformer.transform(projectImportMapper, extIssue1);
        mockIssueTransformerControl.setReturnValue(extIssue1);
        mockIssueTransformer.transform(projectImportMapper, extIssue2);
        mockIssueTransformerControl.setReturnValue(extIssue2);
        mockIssueTransformer.transform(projectImportMapper, extIssue3);
        mockIssueTransformerControl.setReturnValue(extIssue3);
        mockIssueTransformerControl.replay();

        ProjectImportResults projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);

        IssuePersisterHandler issuePersisterHandler = new IssuePersisterHandler(mockProjectImportPersister, projectImportMapper, null, projectImportResults, new ExecutorForTests())
        {
            IssueParser getIssueParser()
            {
                return mockIssueParser;
            }

            IssueTransformer getIssueTransformer()
            {
                return mockIssueTransformer;
            }

            Date getImportDate()
            {
                return importDate;
            }
        };

        issuePersisterHandler.handleEntity("Issue", null);
        issuePersisterHandler.handleEntity("Issue", null);
        issuePersisterHandler.handleEntity("Issue", null);
        issuePersisterHandler.handleEntity("NotIssue", EasyMap.build("id", "16", "key", "HSP-13", "desc", "More stuff happened."));

        assertEquals(164, issuePersisterHandler.getLargestIssueKeyNumber());
        assertEquals(3, projectImportResults.getIssuesCreatedCount());

        mockProjectImportPersisterControl.verify();
        mockIssueParserControl.verify();
        mockIssueTransformerControl.verify();
    }

    @Test
    public void testNullPersistedIssue() throws ParseException, AbortImportException
    {
        final ExternalIssueImpl extIssue1 = new ExternalIssueImpl(null);
        extIssue1.setKey("TST-10");
        final Date importDate = new Date();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createIssue(extIssue1, importDate, null);
        mockProjectImportPersisterControl.setReturnValue(null);
        mockProjectImportPersisterControl.replay();

        final MockControl mockIssueParserControl = MockControl.createStrictControl(IssueParser.class);
        final IssueParser mockIssueParser = (IssueParser) mockIssueParserControl.getMock();
        mockIssueParser.parse(null);
        mockIssueParserControl.setReturnValue(extIssue1);
        mockIssueParserControl.replay();

        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        final MockControl mockIssueTransformerControl = MockControl.createStrictControl(IssueTransformer.class);
        final IssueTransformer mockIssueTransformer = (IssueTransformer) mockIssueTransformerControl.getMock();
        mockIssueTransformer.transform(projectImportMapper, extIssue1);
        mockIssueTransformerControl.setReturnValue(extIssue1);
        mockIssueTransformerControl.replay();

        ProjectImportResults projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        IssuePersisterHandler issuePersisterHandler = new IssuePersisterHandler(mockProjectImportPersister, projectImportMapper, null, projectImportResults, new ExecutorForTests())
        {
            IssueParser getIssueParser()
            {
                return mockIssueParser;
            }

            IssueTransformer getIssueTransformer()
            {
                return mockIssueTransformer;
            }

            Date getImportDate()
            {
                return importDate;
            }
        };

        issuePersisterHandler.handleEntity("Issue", null);

        assertEquals(0, issuePersisterHandler.getLargestIssueKeyNumber());
        assertEquals(0, projectImportResults.getIssuesCreatedCount());
        assertEquals(1, projectImportResults.getErrors().size());
        assertEquals("Unable to create issue with key 'TST-10'.", projectImportResults.getErrors().iterator().next());

        mockProjectImportPersisterControl.verify();
        mockIssueParserControl.verify();
        mockIssueTransformerControl.verify();
    }

}
