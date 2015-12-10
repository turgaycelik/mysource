package com.atlassian.jira.imports.project.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.action.issue.customfields.MockProjectImportableCustomFieldType;
import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValueImpl;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapperImpl;
import com.atlassian.jira.imports.project.parser.CustomFieldValueParser;
import com.atlassian.jira.imports.project.parser.CustomFieldValueParserImpl;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;

import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @since v3.13
 */
public class TestCustomFieldValueValidatorHandler
{
    private final Map<String, CustomFieldValueParser> parsers = new HashMap<String, CustomFieldValueParser>();

    @Before
    public void setUp() throws Exception
    {
        parsers.put(CustomFieldValueParser.CUSTOM_FIELD_VALUE_ENTITY_NAME, new CustomFieldValueParserImpl());
    }

    @Test
    public void testHandleEntityRubbishEntityName() throws Exception
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        // set up a PProjectImportMapper
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getProjectMapper().mapValue("10", "20");
        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            projectImportMapper, null, parsers);
        customFieldValueValidatorHandler.handleEntity("Basuro", null);
        assertTrue(customFieldValueValidatorHandler.getValidationResults().isEmpty());
    }

    @Test
    public void testHandleEntityCustomFieldIsIgnored() throws Exception
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        // set up a PProjectImportMapper
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getProjectMapper().mapValue("10", "20");
        projectImportMapper.getCustomFieldMapper().ignoreCustomField("12");

        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            projectImportMapper, null, parsers);
        customFieldValueValidatorHandler.handleEntity("CustomFieldValue", EasyMap.build("id", "123", "issue", "63", "customfield", "12"));
        assertTrue(customFieldValueValidatorHandler.getValidationResults().isEmpty());
    }

    @Test
    public void testHandleEntityCustomFieldNullCustomFieldFound() throws Exception
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        // set up a PProjectImportMapper
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getProjectMapper().mapValue("10", "20");
        projectImportMapper.getCustomFieldMapper().mapValue("12", "14");

        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            projectImportMapper, null, parsers)
        {
            @Override
            CustomField getCustomField(final ExternalCustomFieldValue externalCustomFieldValue) throws ParseException
            {
                return null;
            }
        };
        customFieldValueValidatorHandler.handleEntity("CustomFieldValue", EasyMap.build("id", "123", "issue", "63", "customfield", "12"));
        assertTrue(customFieldValueValidatorHandler.getValidationResults().isEmpty());
    }

    @Test
    public void testHandleEntityCustomFieldNotImportableCustomFieldFound() throws Exception
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        // set up a PProjectImportMapper
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getProjectMapper().mapValue("10", "20");
        projectImportMapper.getCustomFieldMapper().mapValue("12", "14");

        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            projectImportMapper, null, parsers)
        {
            @Override
            CustomField getCustomField(final ExternalCustomFieldValue externalCustomFieldValue) throws ParseException
            {
                final Mock mockCustomField = new Mock(CustomField.class);
                mockCustomField.setStrict(true);
                final Mock mockCustomFieldType = new Mock(CustomFieldType.class);
                mockCustomFieldType.setStrict(true);
                mockCustomField.expectAndReturn("getCustomFieldType", mockCustomFieldType.proxy());
                return (CustomField) mockCustomField.proxy();
            }
        };
        customFieldValueValidatorHandler.handleEntity("CustomFieldValue", EasyMap.build("id", "123", "issue", "63", "customfield", "12"));
        assertTrue(customFieldValueValidatorHandler.getValidationResults().isEmpty());
    }

    @Test
    public void testHandleEntityCustomFieldHappyPath() throws Exception
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        // set up a PProjectImportMapper
        final ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getProjectMapper().mapValue("10", "20");
        projectImportMapper.getCustomFieldMapper().mapValue("12", "14");

        final AtomicBoolean validateCalled = new AtomicBoolean(false);
        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            projectImportMapper, null, parsers)
        {
            @Override
            CustomField getCustomField(final ExternalCustomFieldValue externalCustomFieldValue) throws ParseException
            {
                final Mock mockCustomField = new Mock(CustomField.class);
                mockCustomField.setStrict(true);
                mockCustomField.expectAndReturn("getCustomFieldType", new MockProjectImportableCustomFieldType(null));
                return (CustomField) mockCustomField.proxy();
            }

            @Override
            void validateCustomFieldValueWithField(final CustomField customField, final ProjectCustomFieldImporter projectCustomFieldImporter, final ExternalCustomFieldValue customFieldValue)
            {
                validateCalled.set(true);
            }
        };
        customFieldValueValidatorHandler.handleEntity("CustomFieldValue", EasyMap.build("id", "123", "issue", "63", "customfield", "12"));
        assertTrue(customFieldValueValidatorHandler.getValidationResults().isEmpty());
        assertTrue(validateCalled.get());
    }

    @Test
    public void testValidateCustomFieldValueWithFieldNoMappedIssueType()
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockProjectImportMapperControl = MockControl.createStrictControl(ProjectImportMapper.class);
        final ProjectImportMapper mockProjectImportMapper = (ProjectImportMapper) mockProjectImportMapperControl.getMock();
        mockProjectImportMapper.getProjectMapper();
        mockProjectImportMapperControl.setReturnValue(new SimpleProjectImportIdMapperImpl());
        final MockControl mockCustomFieldMapperControl = MockClassControl.createControl(CustomFieldMapper.class);
        final CustomFieldMapper mockCustomFieldMapper = (CustomFieldMapper) mockCustomFieldMapperControl.getMock();
        mockCustomFieldMapper.getIssueTypeForIssue("1111");
        mockCustomFieldMapperControl.setReturnValue("2222");
        mockCustomFieldMapperControl.replay();
        mockProjectImportMapper.getCustomFieldMapper();
        mockProjectImportMapperControl.setReturnValue(mockCustomFieldMapper);
        final MockControl mockIssueTypeMapperControl = MockClassControl.createControl(IssueTypeMapper.class);
        final IssueTypeMapper mockIssueTypeMapper = (IssueTypeMapper) mockIssueTypeMapperControl.getMock();
        mockIssueTypeMapper.getMappedId("2222");
        mockIssueTypeMapperControl.setReturnValue(null);
        mockIssueTypeMapperControl.replay();
        mockProjectImportMapper.getIssueTypeMapper();
        mockProjectImportMapperControl.setReturnValue(mockIssueTypeMapper);
        mockProjectImportMapperControl.replay();

        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            mockProjectImportMapper, null, parsers);

        final MockControl mockExternalCustomFieldValueControl = MockClassControl.createControl(ExternalCustomFieldValueImpl.class);
        final ExternalCustomFieldValueImpl mockExternalCustomFieldValue = (ExternalCustomFieldValueImpl) mockExternalCustomFieldValueControl.getMock();
        mockExternalCustomFieldValue.getIssueId();
        mockExternalCustomFieldValueControl.setReturnValue("1111");
        mockExternalCustomFieldValueControl.replay();
        customFieldValueValidatorHandler.validateCustomFieldValueWithField(null, null, mockExternalCustomFieldValue);

        assertTrue(customFieldValueValidatorHandler.getValidationResults().isEmpty());
        mockExternalCustomFieldValueControl.verify();
        mockProjectImportMapperControl.verify();
        mockIssueTypeMapperControl.verify();
        mockCustomFieldMapperControl.verify();
    }

    @Test
    public void testValidateCustomFieldValueWithField()
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        final MockI18nBean i18nBean = new MockI18nBean();

        final MockControl mockProjectImportMapperControl = MockControl.createStrictControl(ProjectImportMapper.class);
        final ProjectImportMapper mockProjectImportMapper = (ProjectImportMapper) mockProjectImportMapperControl.getMock();
        mockProjectImportMapper.getProjectMapper();
        mockProjectImportMapperControl.setReturnValue(new SimpleProjectImportIdMapperImpl());
        final MockControl mockCustomFieldMapperControl = MockClassControl.createControl(CustomFieldMapper.class);
        final CustomFieldMapper mockCustomFieldMapper = (CustomFieldMapper) mockCustomFieldMapperControl.getMock();
        mockCustomFieldMapper.getIssueTypeForIssue("1111");
        mockCustomFieldMapperControl.setReturnValue("2222");
        mockCustomFieldMapperControl.replay();
        mockProjectImportMapper.getCustomFieldMapper();
        mockProjectImportMapperControl.setReturnValue(mockCustomFieldMapper);
        final MockControl mockIssueTypeMapperControl = MockClassControl.createControl(IssueTypeMapper.class);
        final IssueTypeMapper mockIssueTypeMapper = (IssueTypeMapper) mockIssueTypeMapperControl.getMock();
        mockIssueTypeMapper.getMappedId("2222");
        mockIssueTypeMapperControl.setReturnValue("3333");
        mockIssueTypeMapperControl.replay();
        mockProjectImportMapper.getIssueTypeMapper();
        mockProjectImportMapperControl.setReturnValue(mockIssueTypeMapper);
        mockProjectImportMapperControl.replay();

        final Mock mockFieldConfig = new Mock(FieldConfig.class);
        mockFieldConfig.setStrict(true);
        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            mockProjectImportMapper, null, parsers)
        {
            @Override
            FieldConfig getCustomFieldConfig(final CustomField customField, final String newIssueTypeId)
            {
                return (FieldConfig) mockFieldConfig.proxy();
            }

            @Override
            I18nBean getI18nFromCustomField(final CustomField customField)
            {
                return i18nBean;
            }
        };

        final MockControl mockExternalCustomFieldValueControl = MockClassControl.createControl(ExternalCustomFieldValueImpl.class);
        final ExternalCustomFieldValueImpl mockExternalCustomFieldValue = (ExternalCustomFieldValueImpl) mockExternalCustomFieldValueControl.getMock();
        mockExternalCustomFieldValue.getIssueId();
        mockExternalCustomFieldValueControl.setReturnValue("1111");
        mockExternalCustomFieldValue.getCustomFieldId();
        mockExternalCustomFieldValueControl.setReturnValue("5555");
        mockExternalCustomFieldValueControl.replay();

        final Mock mockProjectCustomFieldImporter = new Mock(ProjectCustomFieldImporter.class);
        mockProjectCustomFieldImporter.setStrict(true);
        final MessageSet messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("I am an error");
        final Constraint[] constraints = new Constraint[] { P.eq(mockProjectImportMapper), P.eq(mockExternalCustomFieldValue), P.eq(mockFieldConfig), P.eq(i18nBean) };
        mockProjectCustomFieldImporter.expectAndReturn("canMapImportValue", constraints, messageSet);

        customFieldValueValidatorHandler.validateCustomFieldValueWithField(null, (ProjectCustomFieldImporter) mockProjectCustomFieldImporter.proxy(),
            mockExternalCustomFieldValue);

        assertEquals(1, customFieldValueValidatorHandler.getValidationResults().size());
        assertTrue(((MessageSet) customFieldValueValidatorHandler.getValidationResults().get("5555")).hasAnyErrors());
        assertEquals("I am an error",
            ((MessageSet) customFieldValueValidatorHandler.getValidationResults().get("5555")).getErrorMessages().iterator().next());
        mockExternalCustomFieldValueControl.verify();
        mockProjectImportMapperControl.verify();
        mockIssueTypeMapperControl.verify();
        mockCustomFieldMapperControl.verify();
        mockFieldConfig.verify();
        mockProjectCustomFieldImporter.verify();
    }

    @Test
    public void testValidateCustomFieldValueWithFieldExitingMessageSet()
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        final MockI18nBean i18nBean = new MockI18nBean();

        final MockControl mockProjectImportMapperControl = MockControl.createStrictControl(ProjectImportMapper.class);
        final ProjectImportMapper mockProjectImportMapper = (ProjectImportMapper) mockProjectImportMapperControl.getMock();
        mockProjectImportMapper.getProjectMapper();
        mockProjectImportMapperControl.setReturnValue(new SimpleProjectImportIdMapperImpl());
        final MockControl mockCustomFieldMapperControl = MockClassControl.createControl(CustomFieldMapper.class);
        final CustomFieldMapper mockCustomFieldMapper = (CustomFieldMapper) mockCustomFieldMapperControl.getMock();
        mockCustomFieldMapper.getIssueTypeForIssue("1111");
        mockCustomFieldMapperControl.setReturnValue("2222");
        mockCustomFieldMapperControl.replay();
        mockProjectImportMapper.getCustomFieldMapper();
        mockProjectImportMapperControl.setReturnValue(mockCustomFieldMapper);
        final MockControl mockIssueTypeMapperControl = MockClassControl.createControl(IssueTypeMapper.class);
        final IssueTypeMapper mockIssueTypeMapper = (IssueTypeMapper) mockIssueTypeMapperControl.getMock();
        mockIssueTypeMapper.getMappedId("2222");
        mockIssueTypeMapperControl.setReturnValue("3333");
        mockIssueTypeMapperControl.replay();
        mockProjectImportMapper.getIssueTypeMapper();
        mockProjectImportMapperControl.setReturnValue(mockIssueTypeMapper);
        mockProjectImportMapperControl.replay();

        final Mock mockFieldConfig = new Mock(FieldConfig.class);
        mockFieldConfig.setStrict(true);
        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            mockProjectImportMapper, null, parsers)
        {
            @Override
            FieldConfig getCustomFieldConfig(final CustomField customField, final String newIssueTypeId)
            {
                return (FieldConfig) mockFieldConfig.proxy();
            }

            @Override
            I18nBean getI18nFromCustomField(final CustomField customField)
            {
                return i18nBean;
            }
        };
        customFieldValueValidatorHandler.fieldMessages.put("5555", new MessageSetImpl());

        final MockControl mockExternalCustomFieldValueControl = MockClassControl.createControl(ExternalCustomFieldValueImpl.class);
        final ExternalCustomFieldValueImpl mockExternalCustomFieldValue = (ExternalCustomFieldValueImpl) mockExternalCustomFieldValueControl.getMock();
        mockExternalCustomFieldValue.getIssueId();
        mockExternalCustomFieldValueControl.setReturnValue("1111");
        mockExternalCustomFieldValue.getCustomFieldId();
        mockExternalCustomFieldValueControl.setReturnValue("5555");
        mockExternalCustomFieldValueControl.replay();

        final Mock mockProjectCustomFieldImporter = new Mock(ProjectCustomFieldImporter.class);
        mockProjectCustomFieldImporter.setStrict(true);
        final MessageSet messageSet = new MessageSetImpl();
        messageSet.addErrorMessage("I am an error");
        final Constraint[] constraints = new Constraint[] { P.eq(mockProjectImportMapper), P.eq(mockExternalCustomFieldValue), P.eq(mockFieldConfig), P.eq(i18nBean) };
        mockProjectCustomFieldImporter.expectAndReturn("canMapImportValue", constraints, messageSet);

        customFieldValueValidatorHandler.validateCustomFieldValueWithField(null, (ProjectCustomFieldImporter) mockProjectCustomFieldImporter.proxy(),
            mockExternalCustomFieldValue);

        assertEquals(1, customFieldValueValidatorHandler.getValidationResults().size());
        assertTrue(((MessageSet) customFieldValueValidatorHandler.getValidationResults().get("5555")).hasAnyErrors());
        assertEquals("I am an error",
            ((MessageSet) customFieldValueValidatorHandler.getValidationResults().get("5555")).getErrorMessages().iterator().next());
        mockExternalCustomFieldValueControl.verify();
        mockProjectImportMapperControl.verify();
        mockIssueTypeMapperControl.verify();
        mockCustomFieldMapperControl.verify();
        mockFieldConfig.verify();
        mockProjectCustomFieldImporter.verify();
    }

    @Test
    public void testGetCustomFieldNoMappedId()
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockProjectImportMapperControl = MockControl.createStrictControl(ProjectImportMapper.class);
        final ProjectImportMapper mockProjectImportMapper = (ProjectImportMapper) mockProjectImportMapperControl.getMock();
        mockProjectImportMapper.getProjectMapper();
        mockProjectImportMapperControl.setReturnValue(new SimpleProjectImportIdMapperImpl());
        final MockControl mockCustomFieldMapperControl = MockClassControl.createControl(CustomFieldMapper.class);
        final CustomFieldMapper mockCustomFieldMapper = (CustomFieldMapper) mockCustomFieldMapperControl.getMock();
        mockCustomFieldMapper.getMappedId("5555");
        mockCustomFieldMapperControl.setReturnValue(null);
        mockCustomFieldMapperControl.replay();
        mockProjectImportMapper.getCustomFieldMapper();
        mockProjectImportMapperControl.setReturnValue(mockCustomFieldMapper);
        mockProjectImportMapperControl.replay();

        final MockControl mockExternalCustomFieldValueControl = MockClassControl.createControl(ExternalCustomFieldValueImpl.class);
        final ExternalCustomFieldValueImpl mockExternalCustomFieldValue = (ExternalCustomFieldValueImpl) mockExternalCustomFieldValueControl.getMock();
        mockExternalCustomFieldValue.getCustomFieldId();
        mockExternalCustomFieldValueControl.setReturnValue("5555");
        mockExternalCustomFieldValueControl.replay();

        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            mockProjectImportMapper, null, parsers);
        try
        {
            customFieldValueValidatorHandler.getCustomField(mockExternalCustomFieldValue);
            fail("Should have been a parse exception.");
        }
        catch (final ParseException e)
        {
            // expected
            assertEquals(
                "During custom field value validation we have encountered a custom field with id '5555' which the mapper does not know about.",
                e.getMessage());
        }

        mockProjectImportMapperControl.verify();
        mockCustomFieldMapperControl.verify();
        mockExternalCustomFieldValueControl.verify();
    }

    @Test
    public void testGetCustomFieldBadMappedId()
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockProjectImportMapperControl = MockControl.createStrictControl(ProjectImportMapper.class);
        final ProjectImportMapper mockProjectImportMapper = (ProjectImportMapper) mockProjectImportMapperControl.getMock();
        mockProjectImportMapper.getProjectMapper();
        mockProjectImportMapperControl.setReturnValue(new SimpleProjectImportIdMapperImpl());
        final MockControl mockCustomFieldMapperControl = MockClassControl.createControl(CustomFieldMapper.class);
        final CustomFieldMapper mockCustomFieldMapper = (CustomFieldMapper) mockCustomFieldMapperControl.getMock();
        mockCustomFieldMapper.getMappedId("5555");
        mockCustomFieldMapperControl.setReturnValue("badnumber");
        mockCustomFieldMapperControl.replay();
        mockProjectImportMapper.getCustomFieldMapper();
        mockProjectImportMapperControl.setReturnValue(mockCustomFieldMapper);
        mockProjectImportMapperControl.replay();

        final MockControl mockExternalCustomFieldValueControl = MockClassControl.createControl(ExternalCustomFieldValueImpl.class);
        final ExternalCustomFieldValueImpl mockExternalCustomFieldValue = (ExternalCustomFieldValueImpl) mockExternalCustomFieldValueControl.getMock();
        mockExternalCustomFieldValue.getCustomFieldId();
        mockExternalCustomFieldValueControl.setReturnValue("5555");
        mockExternalCustomFieldValueControl.replay();

        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            mockProjectImportMapper, null, parsers);
        try
        {
            customFieldValueValidatorHandler.getCustomField(mockExternalCustomFieldValue);
            fail("Should have been a parse exception.");
        }
        catch (final ParseException e)
        {
            // expected
            assertEquals("Encountered a custom field value with a custom field id 'badnumber' which is not a valid number.", e.getMessage());
        }

        mockProjectImportMapperControl.verify();
        mockCustomFieldMapperControl.verify();
        mockExternalCustomFieldValueControl.verify();
    }

    @Test
    public void testGetCustomField() throws ParseException
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockProjectImportMapperControl = MockControl.createStrictControl(ProjectImportMapper.class);
        final ProjectImportMapper mockProjectImportMapper = (ProjectImportMapper) mockProjectImportMapperControl.getMock();
        mockProjectImportMapper.getProjectMapper();
        mockProjectImportMapperControl.setReturnValue(new SimpleProjectImportIdMapperImpl());
        final MockControl mockCustomFieldMapperControl = MockClassControl.createControl(CustomFieldMapper.class);
        final CustomFieldMapper mockCustomFieldMapper = (CustomFieldMapper) mockCustomFieldMapperControl.getMock();
        mockCustomFieldMapper.getMappedId("5555");
        mockCustomFieldMapperControl.setReturnValue("1111");
        mockCustomFieldMapperControl.replay();
        mockProjectImportMapper.getCustomFieldMapper();
        mockProjectImportMapperControl.setReturnValue(mockCustomFieldMapper);
        mockProjectImportMapperControl.replay();

        final Mock mockCustomFieldManager = new Mock(CustomFieldManager.class);
        mockCustomFieldManager.setStrict(true);
        mockCustomFieldManager.expectAndReturn("getCustomFieldObject", P.args(P.eq(new Long(1111))), null);

        final MockControl mockExternalCustomFieldValueControl = MockClassControl.createControl(ExternalCustomFieldValueImpl.class);
        final ExternalCustomFieldValueImpl mockExternalCustomFieldValue = (ExternalCustomFieldValueImpl) mockExternalCustomFieldValueControl.getMock();
        mockExternalCustomFieldValue.getCustomFieldId();
        mockExternalCustomFieldValueControl.setReturnValue("5555");
        mockExternalCustomFieldValueControl.replay();

        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            mockProjectImportMapper, (CustomFieldManager) mockCustomFieldManager.proxy(), parsers);
        customFieldValueValidatorHandler.getCustomField(mockExternalCustomFieldValue);

        mockProjectImportMapperControl.verify();
        mockCustomFieldMapperControl.verify();
        mockCustomFieldManager.verify();
        mockExternalCustomFieldValueControl.verify();
    }

    @Test
    public void testGetCustomFieldConfig()
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockProjectImportMapperControl = MockControl.createStrictControl(ProjectImportMapper.class);
        final ProjectImportMapper mockProjectImportMapper = (ProjectImportMapper) mockProjectImportMapperControl.getMock();
        mockProjectImportMapper.getProjectMapper();
        mockProjectImportMapperControl.setReturnValue(new SimpleProjectImportIdMapperImpl());
        mockProjectImportMapperControl.replay();

        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            mockProjectImportMapper, null, parsers);

        final Mock mockFieldConfig = new Mock(FieldConfig.class);
        mockFieldConfig.setStrict(true);

        final Mock mockCustomField = new Mock(CustomField.class);
        mockCustomField.setStrict(true);
        mockCustomField.expectAndReturn("getId", "5555");
        mockCustomField.expectAndReturn("getRelevantConfig", P.args(P.eq(new IssueContextImpl(null, "2"))), mockFieldConfig.proxy());
        assertEquals(mockFieldConfig.proxy(), customFieldValueValidatorHandler.getCustomFieldConfig((CustomField) mockCustomField.proxy(), "2"));

        mockProjectImportMapperControl.verify();
        mockFieldConfig.verify();
        mockCustomField.verify();
    }

    @Test
    public void testGetCustomFieldConfigUsesCachedValue()
    {
        // Set up the BackupProject
        final ExternalProject externalProject = new ExternalProject();
        externalProject.setId("10");
        final BackupProject backupProject = new BackupProjectImpl(externalProject, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
            Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        final MockControl mockProjectImportMapperControl = MockControl.createStrictControl(ProjectImportMapper.class);
        final ProjectImportMapper mockProjectImportMapper = (ProjectImportMapper) mockProjectImportMapperControl.getMock();
        mockProjectImportMapper.getProjectMapper();
        mockProjectImportMapperControl.setReturnValue(new SimpleProjectImportIdMapperImpl());
        mockProjectImportMapperControl.replay();

        final Mock mockFieldConfig = new Mock(FieldConfig.class);
        mockFieldConfig.setStrict(true);

        final CustomFieldValueValidatorHandler customFieldValueValidatorHandler = new CustomFieldValueValidatorHandler(backupProject,
            mockProjectImportMapper, null, parsers);
        customFieldValueValidatorHandler.customFieldConfigMap.put(new CustomFieldValueValidatorHandler.CustomFieldConfigMapKey("5555", "2"),
            mockFieldConfig.proxy());

        final Mock mockCustomField = new Mock(CustomField.class);
        mockCustomField.setStrict(true);
        mockCustomField.expectAndReturn("getId", "5555");
        mockCustomField.expectNotCalled("getRelevantConfig");
        assertEquals(mockFieldConfig.proxy(), customFieldValueValidatorHandler.getCustomFieldConfig((CustomField) mockCustomField.proxy(), "2"));

        mockProjectImportMapperControl.verify();
        mockFieldConfig.verify();
        mockCustomField.verify();
    }

}
