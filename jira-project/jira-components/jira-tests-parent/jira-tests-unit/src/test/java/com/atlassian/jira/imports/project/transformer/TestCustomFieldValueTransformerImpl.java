package com.atlassian.jira.imports.project.transformer;

import com.atlassian.jira.action.issue.customfields.MockProjectImportableCustomFieldType;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValueImpl;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.IssueContextImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;

import org.easymock.MockControl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v3.13
 */
public class TestCustomFieldValueTransformerImpl
{

    @Test
    public void testTransformWithIgnoredCustomField() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getCustomFieldMapper().ignoreCustomField("12");

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("123", "12", "432");

        CustomFieldValueTransformerImpl customFieldValueTransformer = new CustomFieldValueTransformerImpl(null);
        assertNull(customFieldValueTransformer.transform(projectImportMapper, externalCustomFieldValue, new Long(1)));
    }

    @Test
    public void testTransformWithNotMappedIssue() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getCustomFieldMapper().mapValue("12", "14");

        final MockControl mockCustomFieldControl = MockControl.createStrictControl(CustomField.class);
        final CustomField mockCustomField = (CustomField) mockCustomFieldControl.getMock();
        mockCustomFieldControl.replay();

        final MockControl mockCustomFieldManagerControl = MockControl.createStrictControl(CustomFieldManager.class);
        final CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockCustomFieldManagerControl.getMock();
        mockCustomFieldManager.getCustomFieldObject(new Long(14));
        mockCustomFieldManagerControl.setReturnValue(mockCustomField);
        mockCustomFieldManagerControl.replay();

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("123", "12", "432");

        CustomFieldValueTransformerImpl customFieldValueTransformer = new CustomFieldValueTransformerImpl(mockCustomFieldManager);
        assertNull(customFieldValueTransformer.transform(projectImportMapper, externalCustomFieldValue, new Long(1)));
        mockCustomFieldControl.verify();
        mockCustomFieldManagerControl.verify();
    }

    @Test
    public void testTransformWithNullTransformedValue() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getCustomFieldMapper().mapValue("12", "14");
        projectImportMapper.getCustomFieldMapper().flagValueAsRequired("12", "432");
        projectImportMapper.getCustomFieldMapper().flagIssueTypeInUse("432", "1");
        projectImportMapper.getCustomFieldMapper().registerIssueTypesInUse();
        projectImportMapper.getIssueTypeMapper().mapValue("1", "2");
        projectImportMapper.getIssueMapper().mapValue("432", "765");

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("123", "12", "432");

        final MockControl mockFieldConfigControl = MockControl.createStrictControl(FieldConfig.class);
        final FieldConfig mockFieldConfig = (FieldConfig) mockFieldConfigControl.getMock();
        mockFieldConfigControl.replay();

        final MockControl mockProjectCustomFieldImporterControl = MockControl.createStrictControl(ProjectCustomFieldImporter.class);
        final ProjectCustomFieldImporter mockProjectCustomFieldImporter = (ProjectCustomFieldImporter) mockProjectCustomFieldImporterControl.getMock();
        mockProjectCustomFieldImporter.getMappedImportValue(projectImportMapper, externalCustomFieldValue, mockFieldConfig);
        mockProjectCustomFieldImporterControl.setReturnValue(new ProjectCustomFieldImporter.MappedCustomFieldValue(null));
        mockProjectCustomFieldImporterControl.replay();

        MockProjectImportableCustomFieldType mockProjectImportableCustomFieldType = new MockProjectImportableCustomFieldType(mockProjectCustomFieldImporter);

        final MockControl mockCustomFieldControl = MockControl.createStrictControl(CustomField.class);
        final CustomField mockCustomField = (CustomField) mockCustomFieldControl.getMock();
        mockCustomField.getRelevantConfig(new IssueContextImpl(new Long(1), "2"));
        mockCustomFieldControl.setReturnValue(mockFieldConfig);
        mockCustomField.getCustomFieldType();
        mockCustomFieldControl.setReturnValue(mockProjectImportableCustomFieldType);
        mockCustomFieldControl.replay();

        final MockControl mockCustomFieldManagerControl = MockControl.createStrictControl(CustomFieldManager.class);
        final CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockCustomFieldManagerControl.getMock();
        mockCustomFieldManager.getCustomFieldObject(new Long(14));
        mockCustomFieldManagerControl.setReturnValue(mockCustomField);
        mockCustomFieldManagerControl.replay();

        CustomFieldValueTransformerImpl customFieldValueTransformer = new CustomFieldValueTransformerImpl(mockCustomFieldManager);
        assertNull(customFieldValueTransformer.transform(projectImportMapper, externalCustomFieldValue, new Long(1)));
        mockCustomFieldControl.verify();
        mockCustomFieldManagerControl.verify();
        mockFieldConfigControl.verify();
        mockProjectCustomFieldImporterControl.verify();
    }

    @Test
    public void testTransformHappyPathStringValue() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getCustomFieldMapper().mapValue("12", "14");
        projectImportMapper.getCustomFieldMapper().flagValueAsRequired("12", "432");
        projectImportMapper.getCustomFieldMapper().flagIssueTypeInUse("432", "1");
        projectImportMapper.getCustomFieldMapper().registerIssueTypesInUse();
        projectImportMapper.getIssueTypeMapper().mapValue("1", "2");
        projectImportMapper.getIssueMapper().mapValue("432", "765");

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("123", "12", "432");
        externalCustomFieldValue.setStringValue("hello world");

        final MockControl mockFieldConfigControl = MockControl.createStrictControl(FieldConfig.class);
        final FieldConfig mockFieldConfig = (FieldConfig) mockFieldConfigControl.getMock();
        mockFieldConfigControl.replay();

        final MockControl mockProjectCustomFieldImporterControl = MockControl.createStrictControl(ProjectCustomFieldImporter.class);
        final ProjectCustomFieldImporter mockProjectCustomFieldImporter = (ProjectCustomFieldImporter) mockProjectCustomFieldImporterControl.getMock();
        mockProjectCustomFieldImporter.getMappedImportValue(projectImportMapper, externalCustomFieldValue, mockFieldConfig);
        mockProjectCustomFieldImporterControl.setReturnValue(new ProjectCustomFieldImporter.MappedCustomFieldValue("goodbye world"));
        mockProjectCustomFieldImporterControl.replay();

        MockProjectImportableCustomFieldType mockProjectImportableCustomFieldType = new MockProjectImportableCustomFieldType(mockProjectCustomFieldImporter);

        final MockControl mockCustomFieldControl = MockControl.createStrictControl(CustomField.class);
        final CustomField mockCustomField = (CustomField) mockCustomFieldControl.getMock();
        mockCustomField.getRelevantConfig(new IssueContextImpl(new Long(1), "2"));
        mockCustomFieldControl.setReturnValue(mockFieldConfig);
        mockCustomField.getCustomFieldType();
        mockCustomFieldControl.setReturnValue(mockProjectImportableCustomFieldType);
        mockCustomFieldControl.replay();

        final MockControl mockCustomFieldManagerControl = MockControl.createStrictControl(CustomFieldManager.class);
        final CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockCustomFieldManagerControl.getMock();
        mockCustomFieldManager.getCustomFieldObject(new Long(14));
        mockCustomFieldManagerControl.setReturnValue(mockCustomField);
        mockCustomFieldManagerControl.replay();

        CustomFieldValueTransformerImpl customFieldValueTransformer = new CustomFieldValueTransformerImpl(mockCustomFieldManager);

        final ExternalCustomFieldValue customFieldValue = customFieldValueTransformer.transform(projectImportMapper, externalCustomFieldValue, new Long(1));

        assertEquals("goodbye world", customFieldValue.getStringValue());

        mockCustomFieldControl.verify();
        mockCustomFieldManagerControl.verify();
        mockFieldConfigControl.verify();
        mockProjectCustomFieldImporterControl.verify();
    }

    @Test
    public void testTransformHappyPathTextValue() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getCustomFieldMapper().mapValue("12", "14");
        projectImportMapper.getCustomFieldMapper().flagValueAsRequired("12", "432");
        projectImportMapper.getCustomFieldMapper().flagIssueTypeInUse("432", "1");
        projectImportMapper.getCustomFieldMapper().registerIssueTypesInUse();
        projectImportMapper.getIssueTypeMapper().mapValue("1", "2");
        projectImportMapper.getIssueMapper().mapValue("432", "765");

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("123", "12", "432");
        externalCustomFieldValue.setTextValue("hello world");

        final MockControl mockFieldConfigControl = MockControl.createStrictControl(FieldConfig.class);
        final FieldConfig mockFieldConfig = (FieldConfig) mockFieldConfigControl.getMock();
        mockFieldConfigControl.replay();

        final MockControl mockProjectCustomFieldImporterControl = MockControl.createStrictControl(ProjectCustomFieldImporter.class);
        final ProjectCustomFieldImporter mockProjectCustomFieldImporter = (ProjectCustomFieldImporter) mockProjectCustomFieldImporterControl.getMock();
        mockProjectCustomFieldImporter.getMappedImportValue(projectImportMapper, externalCustomFieldValue, mockFieldConfig);
        mockProjectCustomFieldImporterControl.setReturnValue(new ProjectCustomFieldImporter.MappedCustomFieldValue("goodbye world"));
        mockProjectCustomFieldImporterControl.replay();

        MockProjectImportableCustomFieldType mockProjectImportableCustomFieldType = new MockProjectImportableCustomFieldType(mockProjectCustomFieldImporter);

        final MockControl mockCustomFieldControl = MockControl.createStrictControl(CustomField.class);
        final CustomField mockCustomField = (CustomField) mockCustomFieldControl.getMock();
        mockCustomField.getRelevantConfig(new IssueContextImpl(new Long(1), "2"));
        mockCustomFieldControl.setReturnValue(mockFieldConfig);
        mockCustomField.getCustomFieldType();
        mockCustomFieldControl.setReturnValue(mockProjectImportableCustomFieldType);
        mockCustomFieldControl.replay();

        final MockControl mockCustomFieldManagerControl = MockControl.createStrictControl(CustomFieldManager.class);
        final CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockCustomFieldManagerControl.getMock();
        mockCustomFieldManager.getCustomFieldObject(new Long(14));
        mockCustomFieldManagerControl.setReturnValue(mockCustomField);
        mockCustomFieldManagerControl.replay();

        CustomFieldValueTransformerImpl customFieldValueTransformer = new CustomFieldValueTransformerImpl(mockCustomFieldManager);

        final ExternalCustomFieldValue customFieldValue = customFieldValueTransformer.transform(projectImportMapper, externalCustomFieldValue, new Long(1));

        assertEquals("goodbye world", customFieldValue.getTextValue());

        mockCustomFieldControl.verify();
        mockCustomFieldManagerControl.verify();
        mockFieldConfigControl.verify();
        mockProjectCustomFieldImporterControl.verify();
    }

    @Test
    public void testTransformHappyPathDateValue() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getCustomFieldMapper().mapValue("12", "14");
        projectImportMapper.getCustomFieldMapper().flagValueAsRequired("12", "432");
        projectImportMapper.getCustomFieldMapper().flagIssueTypeInUse("432", "1");
        projectImportMapper.getCustomFieldMapper().registerIssueTypesInUse();
        projectImportMapper.getIssueTypeMapper().mapValue("1", "2");
        projectImportMapper.getIssueMapper().mapValue("432", "765");

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("123", "12", "432");
        externalCustomFieldValue.setDateValue("hello world");

        final MockControl mockFieldConfigControl = MockControl.createStrictControl(FieldConfig.class);
        final FieldConfig mockFieldConfig = (FieldConfig) mockFieldConfigControl.getMock();
        mockFieldConfigControl.replay();

        final MockControl mockProjectCustomFieldImporterControl = MockControl.createStrictControl(ProjectCustomFieldImporter.class);
        final ProjectCustomFieldImporter mockProjectCustomFieldImporter = (ProjectCustomFieldImporter) mockProjectCustomFieldImporterControl.getMock();
        mockProjectCustomFieldImporter.getMappedImportValue(projectImportMapper, externalCustomFieldValue, mockFieldConfig);
        mockProjectCustomFieldImporterControl.setReturnValue(new ProjectCustomFieldImporter.MappedCustomFieldValue("goodbye world"));
        mockProjectCustomFieldImporterControl.replay();

        MockProjectImportableCustomFieldType mockProjectImportableCustomFieldType = new MockProjectImportableCustomFieldType(mockProjectCustomFieldImporter);

        final MockControl mockCustomFieldControl = MockControl.createStrictControl(CustomField.class);
        final CustomField mockCustomField = (CustomField) mockCustomFieldControl.getMock();
        mockCustomField.getRelevantConfig(new IssueContextImpl(new Long(1), "2"));
        mockCustomFieldControl.setReturnValue(mockFieldConfig);
        mockCustomField.getCustomFieldType();
        mockCustomFieldControl.setReturnValue(mockProjectImportableCustomFieldType);
        mockCustomFieldControl.replay();

        final MockControl mockCustomFieldManagerControl = MockControl.createStrictControl(CustomFieldManager.class);
        final CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockCustomFieldManagerControl.getMock();
        mockCustomFieldManager.getCustomFieldObject(new Long(14));
        mockCustomFieldManagerControl.setReturnValue(mockCustomField);
        mockCustomFieldManagerControl.replay();

        CustomFieldValueTransformerImpl customFieldValueTransformer = new CustomFieldValueTransformerImpl(mockCustomFieldManager);

        final ExternalCustomFieldValue customFieldValue = customFieldValueTransformer.transform(projectImportMapper, externalCustomFieldValue, new Long(1));

        assertEquals("goodbye world", customFieldValue.getDateValue());

        mockCustomFieldControl.verify();
        mockCustomFieldManagerControl.verify();
        mockFieldConfigControl.verify();
        mockProjectCustomFieldImporterControl.verify();
    }

    @Test
    public void testTransformHappyPathNumberValue() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);
        projectImportMapper.getCustomFieldMapper().mapValue("12", "14");
        projectImportMapper.getCustomFieldMapper().flagValueAsRequired("12", "432");
        projectImportMapper.getCustomFieldMapper().flagIssueTypeInUse("432", "1");
        projectImportMapper.getCustomFieldMapper().registerIssueTypesInUse();
        projectImportMapper.getIssueTypeMapper().mapValue("1", "2");
        projectImportMapper.getIssueMapper().mapValue("432", "765");

        ExternalCustomFieldValueImpl externalCustomFieldValue = new ExternalCustomFieldValueImpl("123", "12", "432");
        externalCustomFieldValue.setNumberValue("hello world");
        externalCustomFieldValue.setParentKey("parent old value");

        final MockControl mockFieldConfigControl = MockControl.createStrictControl(FieldConfig.class);
        final FieldConfig mockFieldConfig = (FieldConfig) mockFieldConfigControl.getMock();
        mockFieldConfigControl.replay();

        final MockControl mockProjectCustomFieldImporterControl = MockControl.createStrictControl(ProjectCustomFieldImporter.class);
        final ProjectCustomFieldImporter mockProjectCustomFieldImporter = (ProjectCustomFieldImporter) mockProjectCustomFieldImporterControl.getMock();
        mockProjectCustomFieldImporter.getMappedImportValue(projectImportMapper, externalCustomFieldValue, mockFieldConfig);
        mockProjectCustomFieldImporterControl.setReturnValue(new ProjectCustomFieldImporter.MappedCustomFieldValue("goodbye world", "parent new value"));
        mockProjectCustomFieldImporterControl.replay();

        MockProjectImportableCustomFieldType mockProjectImportableCustomFieldType = new MockProjectImportableCustomFieldType(mockProjectCustomFieldImporter);

        final MockControl mockCustomFieldControl = MockControl.createStrictControl(CustomField.class);
        final CustomField mockCustomField = (CustomField) mockCustomFieldControl.getMock();
        mockCustomField.getRelevantConfig(new IssueContextImpl(new Long(1), "2"));
        mockCustomFieldControl.setReturnValue(mockFieldConfig);
        mockCustomField.getCustomFieldType();
        mockCustomFieldControl.setReturnValue(mockProjectImportableCustomFieldType);
        mockCustomFieldControl.replay();

        final MockControl mockCustomFieldManagerControl = MockControl.createStrictControl(CustomFieldManager.class);
        final CustomFieldManager mockCustomFieldManager = (CustomFieldManager) mockCustomFieldManagerControl.getMock();
        mockCustomFieldManager.getCustomFieldObject(new Long(14));
        mockCustomFieldManagerControl.setReturnValue(mockCustomField);
        mockCustomFieldManagerControl.replay();

        CustomFieldValueTransformerImpl customFieldValueTransformer = new CustomFieldValueTransformerImpl(mockCustomFieldManager);

        final ExternalCustomFieldValue customFieldValue = customFieldValueTransformer.transform(projectImportMapper, externalCustomFieldValue, new Long(1));

        assertEquals("goodbye world", customFieldValue.getNumberValue());
        assertEquals("parent new value", customFieldValue.getParentKey());

        mockCustomFieldControl.verify();
        mockCustomFieldManagerControl.verify();
        mockFieldConfigControl.verify();
        mockProjectCustomFieldImporterControl.verify();
    }

}
