package com.atlassian.jira.imports.project.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.imports.project.ProjectImportPersister;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.core.EntityRepresentationImpl;
import com.atlassian.jira.imports.project.core.ProjectImportResultsImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValueImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.parser.CustomFieldValueParser;
import com.atlassian.jira.imports.project.transformer.CustomFieldValueTransformer;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.web.bean.MockI18nBean;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v3.13
 */
public class TestCustomFieldValuePersisterHandler
{
    private final Map<String, CustomFieldValueParser> parsers = new HashMap<String, CustomFieldValueParser>();

    @Test
    public void testHandleMappedValue() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("12", "123", "1234");
        EntityRepresentation entityRepresentation = new EntityRepresentationImpl(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, Collections.EMPTY_MAP);

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(entityRepresentation);
        mockProjectImportPersisterControl.setReturnValue(new Long(12));
        mockProjectImportPersisterControl.replay();

        final MockControl mockCustomFieldManagerControl = MockControl.createStrictControl(CustomFieldManager.class);
        final CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockCustomFieldManagerControl.getMock();

        mockCustomFieldManagerControl.replay();

        final MockControl mockCustomFieldValueTransformerControl = MockControl.createStrictControl(CustomFieldValueTransformer.class);
        final CustomFieldValueTransformer mockCustomFieldValueTransformer = (CustomFieldValueTransformer) mockCustomFieldValueTransformerControl.getMock();
        mockCustomFieldValueTransformer.transform(projectImportMapper, externalCustomFieldValue, new Long(12));
        mockCustomFieldValueTransformerControl.setReturnValue(externalCustomFieldValue);
        mockCustomFieldValueTransformerControl.replay();

        final MockControl mockCustomFieldValueParserControl = MockControl.createStrictControl(CustomFieldValueParser.class);
        final CustomFieldValueParser mockCustomFieldValueParser = (CustomFieldValueParser) mockCustomFieldValueParserControl.getMock();
        mockCustomFieldValueParser.parse(Collections.EMPTY_MAP);
        mockCustomFieldValueParserControl.setReturnValue(externalCustomFieldValue);
        mockCustomFieldValueParser.getEntityRepresentation(externalCustomFieldValue);
        mockCustomFieldValueParserControl.setReturnValue(entityRepresentation);
        mockCustomFieldValueParserControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        parsers.put(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, mockCustomFieldValueParser);
        CustomFieldValuePersisterHandler customFieldValuePersisterHandler = new CustomFieldValuePersisterHandler(mockProjectImportPersister, projectImportMapper, mockCustomFieldManager, new Long(12), projectImportResults, null, new ExecutorForTests(), parsers)
        {
            CustomFieldValueTransformer getCustomFieldValueTransformer()
            {
                return mockCustomFieldValueTransformer;
            }
        };

        customFieldValuePersisterHandler.handleEntity(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, Collections.EMPTY_MAP);
        customFieldValuePersisterHandler.handleEntity("NOT_CUSTOM_FIELD_VALUE", Collections.EMPTY_MAP);

        assertEquals(0, projectImportResults.getErrors().size());
        mockCustomFieldManagerControl.verify();
        mockProjectImportPersisterControl.verify();
        mockCustomFieldValueParserControl.verify();
        mockCustomFieldValueTransformerControl.verify();
    }

    @Test
    public void testHandleNullMappedValue() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("12", "123", "1234");

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersisterControl.replay();

        final MockControl mockCustomFieldManagerControl = MockControl.createStrictControl(CustomFieldManager.class);
        final CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockCustomFieldManagerControl.getMock();

        mockCustomFieldManagerControl.replay();

        final MockControl mockCustomFieldValueTransformerControl = MockControl.createStrictControl(CustomFieldValueTransformer.class);
        final CustomFieldValueTransformer mockCustomFieldValueTransformer = (CustomFieldValueTransformer) mockCustomFieldValueTransformerControl.getMock();
        mockCustomFieldValueTransformer.transform(projectImportMapper, externalCustomFieldValue, new Long(12));
        mockCustomFieldValueTransformerControl.setReturnValue(null);
        mockCustomFieldValueTransformerControl.replay();

        final MockControl mockCustomFieldValueParserControl = MockControl.createStrictControl(CustomFieldValueParser.class);
        final CustomFieldValueParser mockCustomFieldValueParser = (CustomFieldValueParser) mockCustomFieldValueParserControl.getMock();
        mockCustomFieldValueParser.parse(Collections.EMPTY_MAP);
        mockCustomFieldValueParserControl.setReturnValue(externalCustomFieldValue);
        mockCustomFieldValueParserControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("1234");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        parsers.put(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, mockCustomFieldValueParser);
        CustomFieldValuePersisterHandler customFieldValuePersisterHandler = new CustomFieldValuePersisterHandler(mockProjectImportPersister, projectImportMapper, mockCustomFieldManager, new Long(12), projectImportResults, mockBackupSystemInformation, null, parsers)
        {
            CustomFieldValueTransformer getCustomFieldValueTransformer()
            {
                return mockCustomFieldValueTransformer;
            }
        };

        customFieldValuePersisterHandler.handleEntity(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, Collections.EMPTY_MAP);

        mockCustomFieldManagerControl.verify();
        mockProjectImportPersisterControl.verify();
        mockCustomFieldValueParserControl.verify();
        mockCustomFieldValueTransformerControl.verify();
    }

    @Test
    public void testHandleMappedValueWithError() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("12", "123", "1234");
        EntityRepresentation entityRepresentation = new EntityRepresentationImpl(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, Collections.EMPTY_MAP);

        final MockControl mockProjectImportPersisterControl = MockControl.createStrictControl(ProjectImportPersister.class);
        final ProjectImportPersister mockProjectImportPersister = (ProjectImportPersister) mockProjectImportPersisterControl.getMock();
        mockProjectImportPersister.createEntity(entityRepresentation);
        mockProjectImportPersisterControl.setReturnValue(null);
        mockProjectImportPersisterControl.replay();

        final MockControl mockCustomFieldManagerControl = MockControl.createStrictControl(CustomFieldManager.class);
        final CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockCustomFieldManagerControl.getMock();

        mockCustomFieldManagerControl.replay();

        final MockControl mockCustomFieldValueTransformerControl = MockControl.createStrictControl(CustomFieldValueTransformer.class);
        final CustomFieldValueTransformer mockCustomFieldValueTransformer = (CustomFieldValueTransformer) mockCustomFieldValueTransformerControl.getMock();
        mockCustomFieldValueTransformer.transform(projectImportMapper, externalCustomFieldValue, new Long(12));
        mockCustomFieldValueTransformerControl.setReturnValue(externalCustomFieldValue);
        mockCustomFieldValueTransformerControl.replay();

        final MockControl mockCustomFieldValueParserControl = MockControl.createStrictControl(CustomFieldValueParser.class);
        final CustomFieldValueParser mockCustomFieldValueParser = (CustomFieldValueParser) mockCustomFieldValueParserControl.getMock();
        mockCustomFieldValueParser.parse(Collections.EMPTY_MAP);
        mockCustomFieldValueParserControl.setReturnValue(externalCustomFieldValue);
        mockCustomFieldValueParser.getEntityRepresentation(externalCustomFieldValue);
        mockCustomFieldValueParserControl.setReturnValue(entityRepresentation);
        mockCustomFieldValueParserControl.replay();

        final MockControl mockBackupSystemInformationControl = MockControl.createStrictControl(BackupSystemInformation.class);
        final BackupSystemInformation mockBackupSystemInformation = (BackupSystemInformation) mockBackupSystemInformationControl.getMock();
        mockBackupSystemInformation.getIssueKeyForId("1234");
        mockBackupSystemInformationControl.setReturnValue("TST-1");
        mockBackupSystemInformationControl.replay();

        final ProjectImportResultsImpl projectImportResults = new ProjectImportResultsImpl(0, 0, 0, 0, new MockI18nBean());
        parsers.put(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, mockCustomFieldValueParser);
        CustomFieldValuePersisterHandler customFieldValuePersisterHandler = new CustomFieldValuePersisterHandler(mockProjectImportPersister, projectImportMapper, mockCustomFieldManager, new Long(12), projectImportResults, mockBackupSystemInformation, new ExecutorForTests(), parsers)
        {
            CustomFieldValueTransformer getCustomFieldValueTransformer()
            {
                return mockCustomFieldValueTransformer;
            }
        };

        customFieldValuePersisterHandler.handleEntity(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, Collections.EMPTY_MAP);
        customFieldValuePersisterHandler.handleEntity("NOT_CUSTOM_FIELD_VALUE", Collections.EMPTY_MAP);

        assertEquals(1, projectImportResults.getErrors().size());
        assertTrue(projectImportResults.getErrors().contains("There was a problem saving custom field value with id '12' for issue 'TST-1'."));
        mockCustomFieldManagerControl.verify();
        mockProjectImportPersisterControl.verify();
        mockCustomFieldValueParserControl.verify();
        mockCustomFieldValueTransformerControl.verify();
    }

}
