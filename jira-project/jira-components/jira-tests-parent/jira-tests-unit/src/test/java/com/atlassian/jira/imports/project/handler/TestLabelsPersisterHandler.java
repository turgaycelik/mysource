package com.atlassian.jira.imports.project.handler;

import java.util.Map;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalLabel;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.parser.LabelParser;
import com.atlassian.jira.imports.project.transformer.LabelTransformer;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestLabelsPersisterHandler
{
    @Test
    public void testHandleNothing() throws ParseException, AbortImportException
    {
        final ProjectImportPersister mockImportPersister = createMock(ProjectImportPersister.class);

        replay(mockImportPersister);
        LabelsPersisterHandler handler = new LabelsPersisterHandler(mockImportPersister, null, null, null, null);
        handler.handleEntity("Dude", MapBuilder.newBuilder("sweet", "whatsminesay").toMap());

        verify(mockImportPersister);
    }

    @Test
    public void testHandle() throws ParseException, AbortImportException
    {
        Map badAttributes = MapBuilder.newBuilder().add("id", "10000").add("issue", "10001").add("fieldid", "12000").add("label","alabel").toMap();
        Map goodAttributes = MapBuilder.newBuilder().add("id", "10003").add("issue", "10004").add("label","alabel").toMap();
        final ProjectImportPersister mockImportPersister = createMock(ProjectImportPersister.class);
        final ProjectImportMapper mockProjectImportMapper = createMock(ProjectImportMapper.class);
        final LabelParser mockLabelParser = createMock(LabelParser.class);
        ExternalLabel badLabel = new ExternalLabel();
        badLabel.setId("10000");
        badLabel.setIssueId("10001");
        badLabel.setCustomFieldId("12000");
        badLabel.setLabel("alabel");
        ExternalLabel goodLabel = new ExternalLabel();
        goodLabel.setId("10003");
        goodLabel.setIssueId("10004");
        goodLabel.setLabel("alabel");
        expect(mockLabelParser.parse(badAttributes)).andReturn(badLabel);
        expect(mockLabelParser.parse(goodAttributes)).andReturn(goodLabel);
        final LabelTransformer mockLabelTransformer = createMock(LabelTransformer.class);
        ExternalLabel badTransformedLabel = new ExternalLabel();
        badTransformedLabel.setIssueId("10001");
        badTransformedLabel.setCustomFieldId("12000");
        badTransformedLabel.setLabel("alabel");
        ExternalLabel goodTransformedLabel = new ExternalLabel();
        goodTransformedLabel.setIssueId("10004");
        goodTransformedLabel.setLabel("alabel");
        expect(mockLabelTransformer.transform(mockProjectImportMapper, badLabel)).andReturn(badTransformedLabel);
        expect(mockLabelTransformer.transform(mockProjectImportMapper, goodLabel)).andReturn(goodTransformedLabel);
        final Map<String, String> transformedAttributes = ImmutableMap.of("issue", "10004", "label", "alabel");
        final EntityRepresentationImpl representation = new EntityRepresentationImpl("Label", transformedAttributes);
        expect(mockLabelParser.getEntityRepresentation(goodTransformedLabel)).andReturn(representation);

        final ProjectImportResults mockProjectImportResults = createMock(ProjectImportResults.class);
        expect(mockProjectImportResults.abortImport()).andReturn(false).anyTimes();
        expect(mockImportPersister.createEntity(representation)).andReturn(10023L);

        replay(mockImportPersister, mockLabelTransformer, mockLabelParser, mockProjectImportMapper, mockProjectImportResults);
        LabelsPersisterHandler handler = new LabelsPersisterHandler(mockImportPersister, mockProjectImportMapper, mockProjectImportResults, null, new ExecutorForTests())
        {
            @Override
            LabelParser getLabelParser()
            {
                return mockLabelParser;
            }

            @Override
            LabelTransformer getLabelTransformer()
            {
                return mockLabelTransformer;
            }
        };

        //shouldn't get persisted due to custom field
        handler.handleEntity("Label", badAttributes);
        handler.handleEntity("Label", goodAttributes);

        verify(mockImportPersister, mockLabelTransformer, mockLabelParser, mockProjectImportMapper, mockProjectImportResults);
    }
}
