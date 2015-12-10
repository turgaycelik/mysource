package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.external.beans.ExternalComment;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.CommentParser;
import com.atlassian.jira.imports.project.transformer.CommentTransformer;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestCommentPersisterHandler
{
    @Test
    public void testHandle() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalComment externalComment = new ExternalComment();
        externalComment.setIssueId("34");

        final MockControl mockCommentParserControl = MockControl.createStrictControl(CommentParser.class);
        final CommentParser mockCommentParser = (CommentParser) mockCommentParserControl.getMock();
        mockCommentParser.parse(null);
        mockCommentParserControl.setReturnValue(externalComment);
        mockCommentParser.getEntityRepresentation(externalComment);
        mockCommentParserControl.setReturnValue(null);
        mockCommentParserControl.replay();

        final MockControl mockCommentTransformerControl = MockControl.createStrictControl(CommentTransformer.class);
        final CommentTransformer mockCommentTransformer = (CommentTransformer) mockCommentTransformerControl.getMock();
        mockCommentTransformer.transform(projectImportMapper, externalComment);
        mockCommentTransformerControl.setReturnValue(externalComment);
        mockCommentTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(null);
        mockProjectImportPersisterControl.setReturnValue(new Long(12));
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);
        CommentPersisterHandler commentPersisterHandler = new CommentPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, null, new ExecutorForTests())
        {
            CommentParser getCommentParser()
            {
                return mockCommentParser;
            }

            CommentTransformer getCommentTransformer()
            {
                return mockCommentTransformer;
            }
        };

        commentPersisterHandler.handleEntity(CommentParser.COMMENT_ENTITY_NAME, null);
        commentPersisterHandler.handleEntity("NOTComment", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockCommentParserControl.verify();
        mockCommentTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleErrorAddingComment() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalComment externalComment = new ExternalComment();
        externalComment.setIssueId("34");
        externalComment.setId("12");

        final MockControl mockCommentParserControl = MockControl.createStrictControl(CommentParser.class);
        final CommentParser mockCommentParser = (CommentParser) mockCommentParserControl.getMock();
        mockCommentParser.parse(null);
        mockCommentParserControl.setReturnValue(externalComment);
        mockCommentParser.getEntityRepresentation(externalComment);
        mockCommentParserControl.setReturnValue(null);
        mockCommentParserControl.replay();

        final MockControl mockCommentTransformerControl = MockControl.createStrictControl(CommentTransformer.class);
        final CommentTransformer mockCommentTransformer = (CommentTransformer) mockCommentTransformerControl.getMock();
        mockCommentTransformer.transform(projectImportMapper, externalComment);
        mockCommentTransformerControl.setReturnValue(externalComment);
        mockCommentTransformerControl.replay();

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
        CommentPersisterHandler commentPersisterHandler = new CommentPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, new ExecutorForTests())
        {
            CommentParser getCommentParser()
            {
                return mockCommentParser;
            }

            CommentTransformer getCommentTransformer()
            {
                return mockCommentTransformer;
            }
        };

        commentPersisterHandler.handleEntity(CommentParser.COMMENT_ENTITY_NAME, null);
        commentPersisterHandler.handleEntity("NOTComment", null);

        assertEquals(1, projectImportResults.getErrors().size());
        assertTrue(projectImportResults.getErrors().contains("There was a problem saving comment with id '12' for issue 'TST-1'."));
        mockCommentParserControl.verify();
        mockCommentTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleErrorNullIssueId() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        final ExternalComment externalComment = new ExternalComment();
        externalComment.setId("12");
        externalComment.setIssueId("34");

        final ExternalComment transformedExternalComment = new ExternalComment();
        transformedExternalComment.setId("12");

        final MockControl mockCommentParserControl = MockControl.createStrictControl(CommentParser.class);
        final CommentParser mockCommentParser = (CommentParser) mockCommentParserControl.getMock();
        mockCommentParser.parse(null);
        mockCommentParserControl.setReturnValue(externalComment);
        mockCommentParserControl.replay();

        final MockControl mockCommentTransformerControl = MockControl.createStrictControl(CommentTransformer.class);
        final CommentTransformer mockCommentTransformer = (CommentTransformer) mockCommentTransformerControl.getMock();
        mockCommentTransformer.transform(projectImportMapper, externalComment);
        mockCommentTransformerControl.setReturnValue(transformedExternalComment);
        mockCommentTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("34");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        CommentPersisterHandler commentPersisterHandler = new CommentPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, mockBackupSystemInformation, null)
        {
            CommentParser getCommentParser()
            {
                return mockCommentParser;
            }

            CommentTransformer getCommentTransformer()
            {
                return mockCommentTransformer;
            }
        };

        commentPersisterHandler.handleEntity(CommentParser.COMMENT_ENTITY_NAME, null);
        commentPersisterHandler.handleEntity("NOTComment", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockCommentParserControl.verify();
        mockCommentTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }
    
    @Test
    public void testHandleNonComment() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        final MockControl mockCommentParserControl = MockControl.createStrictControl(CommentParser.class);
        final CommentParser mockCommentParser = (CommentParser) mockCommentParserControl.getMock();
        mockCommentParser.parse(null);
        mockCommentParserControl.setReturnValue(null);
        mockCommentParserControl.replay();

        final MockControl mockCommentTransformerControl = MockControl.createStrictControl(CommentTransformer.class);
        final CommentTransformer mockCommentTransformer = (CommentTransformer) mockCommentTransformerControl.getMock();
        mockCommentTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, null);
        CommentPersisterHandler commentPersisterHandler = new CommentPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, null, null)
        {
            CommentParser getCommentParser()
            {
                return mockCommentParser;
            }

            CommentTransformer getCommentTransformer()
            {
                return mockCommentTransformer;
            }
        };

        commentPersisterHandler.handleEntity(CommentParser.COMMENT_ENTITY_NAME, null);
        commentPersisterHandler.handleEntity("NOTComment", null);

        mockCommentParserControl.verify();
        mockCommentTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }

}
