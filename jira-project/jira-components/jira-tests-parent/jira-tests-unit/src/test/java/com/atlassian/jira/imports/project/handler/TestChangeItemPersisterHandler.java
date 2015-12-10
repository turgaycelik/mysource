package com.atlassian.jira.imports.project.handler;

import com.atlassian.jira.external.beans.ExternalChangeItem;
import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.ChangeItemParser;
import com.atlassian.jira.imports.project.transformer.ChangeItemTransformer;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestChangeItemPersisterHandler
{

    @Test
    public void testHandle() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalChangeItem externalChangeItem = new ExternalChangeItem("12", "15", "jira", "security", "10000", "level1", "10001", "level2");

        final MockControl mockChangeItemParserControl = MockControl.createStrictControl(ChangeItemParser.class);
        final ChangeItemParser mockChangeItemParser = (ChangeItemParser) mockChangeItemParserControl.getMock();
        mockChangeItemParser.parse(null);
        mockChangeItemParserControl.setReturnValue(externalChangeItem);
        mockChangeItemParser.getEntityRepresentation(externalChangeItem);
        mockChangeItemParserControl.setReturnValue(null);
        mockChangeItemParserControl.replay();

        final MockControl mockChangeItemTransformerControl = MockControl.createStrictControl(ChangeItemTransformer.class);
        final ChangeItemTransformer mockChangeItemTransformer = (ChangeItemTransformer) mockChangeItemTransformerControl.getMock();
        mockChangeItemTransformer.transform(projectImportMapper, externalChangeItem);
        mockChangeItemTransformerControl.setReturnValue(externalChangeItem);
        mockChangeItemTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(null);
        mockProjectImportPersisterControl.setReturnValue(new Long(123));
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        ChangeItemPersisterHandler ChangeItemPersisterHandler = new ChangeItemPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, new ExecutorForTests())
        {
            ChangeItemParser getChangeItemParser()
            {
                return mockChangeItemParser;
            }

            ChangeItemTransformer getChangeItemTransformer()
            {
                return mockChangeItemTransformer;
            }
        };

        ChangeItemPersisterHandler.handleEntity(ChangeItemParser.CHANGE_ITEM_ENTITY_NAME, null);
        ChangeItemPersisterHandler.handleEntity("NOTChangeItem", null);

        assertEquals(0, projectImportResults.getErrors().size());
        mockChangeItemParserControl.verify();
        mockChangeItemTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleNullTransformedChangeItem() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalChangeItem externalChangeItem = new ExternalChangeItem("12", "15", "jira", "security", "10000", "level1", "10001", "level2");
        ExternalChangeItem transformedExternalChangeItem = new ExternalChangeItem("12", null, "jira", "security", "10000", "level1", "10001", "level2");

        final MockControl mockChangeItemParserControl = MockControl.createStrictControl(ChangeItemParser.class);
        final ChangeItemParser mockChangeItemParser = (ChangeItemParser) mockChangeItemParserControl.getMock();
        mockChangeItemParser.parse(null);
        mockChangeItemParserControl.setReturnValue(externalChangeItem);
        mockChangeItemParserControl.replay();

        final MockControl mockChangeItemTransformerControl = MockControl.createStrictControl(ChangeItemTransformer.class);
        final ChangeItemTransformer mockChangeItemTransformer = (ChangeItemTransformer) mockChangeItemTransformerControl.getMock();
        mockChangeItemTransformer.transform(projectImportMapper, externalChangeItem);
        mockChangeItemTransformerControl.setReturnValue(transformedExternalChangeItem);
        mockChangeItemTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        ChangeItemPersisterHandler ChangeItemPersisterHandler = new ChangeItemPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, null)
        {
            ChangeItemParser getChangeItemParser()
            {
                return mockChangeItemParser;
            }

            ChangeItemTransformer getChangeItemTransformer()
            {
                return mockChangeItemTransformer;
            }
        };

        ChangeItemPersisterHandler.handleEntity(ChangeItemParser.CHANGE_ITEM_ENTITY_NAME, null);
        ChangeItemPersisterHandler.handleEntity("NOTChangeItem", null);

        mockChangeItemParserControl.verify();
        mockChangeItemTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }

    @Test
    public void testHandleErrorAddingChangeitem() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalChangeItem externalChangeItem = new ExternalChangeItem("12", "15", "jira", "security", "10000", "level1", "10001", "level2");

        final MockControl mockChangeItemParserControl = MockControl.createStrictControl(ChangeItemParser.class);
        final ChangeItemParser mockChangeItemParser = (ChangeItemParser) mockChangeItemParserControl.getMock();
        mockChangeItemParser.parse(null);
        mockChangeItemParserControl.setReturnValue(externalChangeItem);
        mockChangeItemParser.getEntityRepresentation(externalChangeItem);
        mockChangeItemParserControl.setReturnValue(null);
        mockChangeItemParserControl.replay();

        final MockControl mockChangeItemTransformerControl = MockControl.createStrictControl(ChangeItemTransformer.class);
        final ChangeItemTransformer mockChangeItemTransformer = (ChangeItemTransformer) mockChangeItemTransformerControl.getMock();
        mockChangeItemTransformer.transform(projectImportMapper, externalChangeItem);
        mockChangeItemTransformerControl.setReturnValue(externalChangeItem);
        mockChangeItemTransformerControl.replay();

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(null);
        mockProjectImportPersisterControl.setReturnValue(null);
        mockProjectImportPersisterControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        ChangeItemPersisterHandler changeitemPersisterHandler = new ChangeItemPersisterHandler(mockProjectImportPersister, projectImportMapper, projectImportResults, new ExecutorForTests())
        {
            ChangeItemParser getChangeItemParser()
            {
                return mockChangeItemParser;
            }

            ChangeItemTransformer getChangeItemTransformer()
            {
                return mockChangeItemTransformer;
            }
        };

        changeitemPersisterHandler.handleEntity(ChangeItemParser.CHANGE_ITEM_ENTITY_NAME, null);
        changeitemPersisterHandler.handleEntity("NOTChangeItem", null);

        assertEquals(1, projectImportResults.getErrors().size());
        assertTrue(projectImportResults.getErrors().contains("There was a problem saving change item with id '12' for change group with id '15'."));
        mockChangeItemParserControl.verify();
        mockChangeItemTransformerControl.verify();
        mockProjectImportPersisterControl.verify();
    }

}
