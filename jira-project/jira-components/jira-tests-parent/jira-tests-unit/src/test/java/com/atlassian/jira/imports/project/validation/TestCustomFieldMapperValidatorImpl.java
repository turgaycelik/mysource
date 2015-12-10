package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.action.issue.customfields.MockCustomFieldType;
import com.atlassian.jira.action.issue.customfields.MockProjectImportableCustomFieldType;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigImpl;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.managers.MockCustomFieldManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.util.MessageSet;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static com.atlassian.jira.util.MessageSetAssert.assert1Error;
import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assert1Warning;
import static com.atlassian.jira.util.MessageSetAssert.assert1WarningNoErrors;
import static com.atlassian.jira.util.MessageSetAssert.assertErrorMessages;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static com.atlassian.jira.util.MessageSetAssert.assertNoWarnings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since v3.13
 */
@RunWith (MockitoJUnitRunner.class)
public class TestCustomFieldMapperValidatorImpl
{
    @Test
    public void testCustomFieldTypeIsImportable_TypeDoesNotExist()
    {
        final CustomFieldManager customFieldManager = mock(CustomFieldManager.class);

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(customFieldManager, null, null);
        assertThat("importable", customFieldMapperValidator.customFieldTypeIsImportable("A"), is(false));

        verify(customFieldManager).getCustomFieldType("A");
    }

    @Test
    public void testCustomFieldTypeIsImportable_TypeNotImportable()
    {
        final CustomFieldManager customFieldManager = mock(CustomFieldManager.class);
        final CustomFieldType<?, ?> customFieldType = new MockCustomFieldType();
        when(customFieldManager.getCustomFieldType("A")).thenReturn(customFieldType);

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(customFieldManager, null, null);
        assertThat("importable", customFieldMapperValidator.customFieldTypeIsImportable("A"), is(false));

        verify(customFieldManager).getCustomFieldType("A");
    }

    @Test
    public void testCustomFieldTypeIsImportable_TypeImportable()
    {
        final CustomFieldManager customFieldManager = mock(CustomFieldManager.class);
        final CustomFieldType<?, ?> customFieldType = new MockProjectImportableCustomFieldType(null);
        when(customFieldManager.getCustomFieldType("A")).thenReturn(customFieldType);

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(customFieldManager, null, null);
        assertThat("importable", customFieldMapperValidator.customFieldTypeIsImportable("A"), is(true));
    }

    @Test
    public void testGetIssueTypeDisplayNames()
    {
        final ConstantsManager constantsManager = mockConstantsManager(
                new MockIssueType("2", "Bug"),
                new MockIssueType("4", "Improvement"));

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(null, constantsManager, null);

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");
        issueTypeMapper.mapValue("3", "4");

        ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("", "", ""), "12");
        assertEquals("Bug, Improvement", customFieldMapperValidator.getIssueTypeDisplayNames(externalCustomFieldConfiguration, ImmutableList.of("1", "3"), issueTypeMapper, new MockI18nHelper()));
    }

    @Test
    public void testGetIssueTypeDisplayNamesNullList()
    {
        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");
        issueTypeMapper.mapValue("3", "4");

        final ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("", "", ""), "12");
        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(null, null, null);

        assertEquals("common.words.none", customFieldMapperValidator.getIssueTypeDisplayNames(externalCustomFieldConfiguration, null, issueTypeMapper, new MockI18nHelper()));
    }

    @Test
    public void testCustomFieldIsValidForRequiredContexts_CustomFieldNotUsed()
    {
        final MockProjectManager projectManager = new MockProjectManager();
        projectManager.addProject(new MockGenericValue("Project", FieldMap.build("key", "PIG")));

        final ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("", "", ""), "12");
        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(null, null, projectManager);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();

        assertEquals(true, customFieldMapperValidator.customFieldIsValidForRequiredContexts(externalCustomFieldConfiguration, null, "10", customFieldMapper, null, "PIG"));
    }

    @Test
    public void testCustomFieldIsValidForRequiredContexts_InvalidContext()
    {
        final MockProjectManager projectManager = new MockProjectManager();
        projectManager.addProject(new MockProject(500, "PIG"));

        final ConstantsManager constantsManager = mockConstantsManager(
                new MockIssueType("11", "Bug"),
                new MockIssueType("12", "Improvement"));

        final ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("", "", ""), "12");
        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(null, constantsManager, projectManager);
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();

        // Flag that old CustomField 10 is used by Issue Type 1
        customFieldMapper.flagValueAsRequired("10", "1200");
        customFieldMapper.flagIssueTypeInUse("1200", "1");
        // Flag that old CustomField 10 is used by Issue Type 2
        customFieldMapper.flagValueAsRequired("10", "1201");
        customFieldMapper.flagIssueTypeInUse("1201", "2");
        customFieldMapper.registerIssueTypesInUse();

        // Map the old Issue Type ID's to new Issue Type IDs
        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "11");
        issueTypeMapper.mapValue("2", "12");

        // Create a custom field.
        final CustomField newCustomField = mock(CustomField.class);
        when(newCustomField.getRelevantConfig(any(IssueContext.class))).thenReturn(null);

        assertEquals(false, customFieldMapperValidator.customFieldIsValidForRequiredContexts(externalCustomFieldConfiguration, newCustomField, "10", customFieldMapper, issueTypeMapper, "PIG"));
    }

    @Test
    public void testCustomFieldIsValidForRequiredContexts_HappyPath()
    {
        final MockProjectManager projectManager = new MockProjectManager();
        projectManager.addProject(new MockProject(500, "PIG"));

        final ConstantsManager constantsManager = mockConstantsManager(
                new MockIssueType("11", "Bug"),
                new MockIssueType("12", "Improvement"));

        ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("", "", ""), "12");

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(null, constantsManager, projectManager);

        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        // Flag that old CustomField 10 is used by Issue Type 1
        customFieldMapper.flagValueAsRequired("10", "1200");
        customFieldMapper.flagIssueTypeInUse("1200", "1");
        // Flag that old CustomField 10 is used by Issue Type 2
        customFieldMapper.flagValueAsRequired("10", "1201");
        customFieldMapper.flagIssueTypeInUse("1201", "2");

        // Map the old Issue Type ID's to new Issue Type IDs
        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "11");
        issueTypeMapper.mapValue("2", "12");

        // Create a custom field.
        final CustomField newCustomField = mock(CustomField.class);
        when(newCustomField.getRelevantConfig(any(IssueContext.class))).thenReturn(anyFieldConfig());

        assertEquals(true, customFieldMapperValidator.customFieldIsValidForRequiredContexts(externalCustomFieldConfiguration, newCustomField, "10", customFieldMapper, issueTypeMapper, "PIG"));
    }

    @Test
    public void testValidateMappings_CustomFieldDoesNotExist()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomFieldType("com.atlassian.cf:Flavour", new MockProjectImportableCustomFieldType("com.atlassian.cf:Flavour", "Flavour"));
        mockCustomFieldManager.addCustomFieldType("com.atlassian.cf:Spin", new MockProjectImportableCustomFieldType("com.atlassian.cf:Spin", "Spin"));

        final ExternalCustomField customFieldMyStuff = new ExternalCustomField("12", "My Stuff", "com.atlassian.cf:Flavour");
        final ExternalCustomFieldConfiguration customFieldMyStuffConfig = new ExternalCustomFieldConfiguration(null, null, customFieldMyStuff, "34");
        final ExternalCustomField customFieldMadStuff = new ExternalCustomField("14", "Mad Stuff", "com.atlassian.cf:Spin");
        final ExternalCustomFieldConfiguration customFieldMadStuffConfig = new ExternalCustomFieldConfiguration(null, null, customFieldMadStuff, "34");
        final BackupProject backupProject = backupProject(customFieldMyStuffConfig, customFieldMadStuffConfig);

        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.registerOldValue("14", "Mad Stuff");

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, null, null);

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nHelper(), backupProject, null, customFieldMapper);
        assertNoWarnings(messageSet);
        assertErrorMessages(messageSet,
                "admin.errors.project.import.custom.field.does.not.exist [Mad Stuff] [Spin]",
                "admin.errors.project.import.custom.field.does.not.exist [My Stuff] [Flavour]");
    }

    @Test
    public void testValidateMappings_CustomFieldNotMappedBecauseWrongType()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(22, "My Stuff", "Text"));
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "Text"));
        mockCustomFieldManager.addCustomFieldType("com.atlassian.cf:Date", new MockProjectImportableCustomFieldType("com.atlassian.cf:Date", "Date"));
        mockCustomFieldManager.addCustomFieldType("com.atlassian.cf:Numeric", new MockProjectImportableCustomFieldType("com.atlassian.cf:Numeric", "Numeric"));

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, null, null);
        final BackupProject backupProject = backupProject(
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "com.atlassian.cf:Date"), "321"),
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("14", "My Stuff", "com.atlassian.cf:Numeric"), "321"));
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.registerOldValue("14", "Mad Stuff");

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nHelper(), backupProject, null, customFieldMapper);
        assertNoWarnings(messageSet);
        assertErrorMessages(messageSet,
                "admin.errors.project.import.custom.field.wrong.type [Mad Stuff] [Numeric]",
                "admin.errors.project.import.custom.field.wrong.type [My Stuff] [Date]");
    }

    @Test
    public void testValidateMappings_CustomFieldMappedButWrongType()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(22, "My Stuff", "Text"));
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "Text"));

        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, null, null)
        {
            public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
            {
                return true;
            }
        };
        final BackupProject backupProject = backupProject(
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "Date"), "321"),
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("14", "My Stuff", "Numeric"), "321"));
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.registerOldValue("14", "Mad Stuff");
        customFieldMapper.mapValue("12", "22");
        customFieldMapper.mapValue("14", "24");

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nHelper(), backupProject, null, customFieldMapper);
        assertNoWarnings(messageSet);
        assertErrorMessages(messageSet,
                "admin.errors.project.import.custom.field.wrong.type [Mad Stuff] [Numeric]",
                "admin.errors.project.import.custom.field.wrong.type [My Stuff] [Date]");
    }

    @Test
    public void testValidateMappings_CustomFieldNotMappedBecauseNotValidForSomeIssueTypes()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(22, "My Stuff", "Date"));
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "com.atlassian.cf:Numeric"));
        mockCustomFieldManager.addCustomFieldType("com.atlassian.cf:Numeric", new MockProjectImportableCustomFieldType("com.atlassian.cf:Numeric", "Numeric"));

        final BackupProject backupProject = backupProject(
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "Date"), "321"),
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("14", "My Stuff", "com.atlassian.cf:Numeric"), "321"));
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.flagIssueTypeInUse("1000", "1");
        customFieldMapper.registerOldValue("14", "Mad Stuff");
        customFieldMapper.mapValue("12", "22");
        customFieldMapper.registerIssueTypesInUse();

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");

        final ConstantsManager constantsManager = mockConstantsManager(new MockIssueType("2", "FUBAR"));
        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, constantsManager, null)
        {
            // This method is tested separately, so we can mock it out here.
            public boolean customFieldIsValidForRequiredContexts(final ExternalCustomFieldConfiguration externalCustomFieldConfiguration, final CustomField newCustomField, final String oldCustomFieldId, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final String projectKey)
            {
                // Valid for 12, but not 14
                return oldCustomFieldId.equals("12");
            }

            public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
            {
                return true;
            }
        };

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nHelper(), backupProject, issueTypeMapper, customFieldMapper);
        assert1ErrorNoWarnings(messageSet, "admin.errors.project.import.custom.field.wrong.context [Mad Stuff] [FUBAR]");
    }

    @Test
    public void testValidateMappings_CustomFieldNotMappedBecauseNotProjectImportable()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(22, "My Stuff", "Date"));
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "Numeric"));
        mockCustomFieldManager.addCustomFieldType("Numeric", new MockCustomFieldType("Numeric", "Numeric"));

        final BackupProject backupProject = backupProject(
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "Date"), "321"),
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("14", "Mad Stuff", "Numeric"), "321"));
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.flagIssueTypeInUse("1000", "1");
        customFieldMapper.registerOldValue("14", "Mad Stuff");
        customFieldMapper.mapValue("12", "22");

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");

        final ConstantsManager constantsManager = mockConstantsManager(new MockIssueType("2", "FUBAR"));
        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, constantsManager, null)
        {
            // This method is tested separately, so we can mock it out here.
            public boolean customFieldIsValidForRequiredContexts(final ExternalCustomFieldConfiguration externalCustomFieldConfiguration, final CustomField newCustomField, final String oldCustomFieldId, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final String projectKey)
            {
                return true;
            }

            public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
            {
                return false;
            }
        };

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nHelper(), backupProject, issueTypeMapper, customFieldMapper);
        assert1Warning(messageSet, "admin.errors.project.import.custom.field.not.importable [Mad Stuff]");
        assert1Error(messageSet, "admin.errors.project.import.custom.field.not.importable [My Stuff]");
    }

    @Test
    public void testValidateMappings_CustomFieldMappedButNotValidForSomeIssueTypes()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(22, "My Stuff", "Date"));
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "Numeric"));

        final BackupProject backupProject = backupProject(
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "Date"), "321"),
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("14", "My Stuff", "Numeric"), "321"));
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.flagIssueTypeInUse("1000", "1");
        customFieldMapper.registerOldValue("14", "Mad Stuff");
        customFieldMapper.mapValue("12", "22");
        customFieldMapper.mapValue("14", "24");
        customFieldMapper.registerIssueTypesInUse();

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");

        final ConstantsManager constantsManager = mockConstantsManager(new MockIssueType("2", "FUBAR"));
        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, constantsManager, null)
        {
            // This method is tested separately, so we can mock it out here.
            public boolean customFieldIsValidForRequiredContexts(final ExternalCustomFieldConfiguration externalCustomFieldConfiguration, final CustomField newCustomField, final String oldCustomFieldId, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final String projectKey)
            {
                // Valid for 12, but not 14
                return oldCustomFieldId.equals("12");
            }

            public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
            {
                return true;
            }
        };

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nHelper(), backupProject, issueTypeMapper, customFieldMapper);
        assert1ErrorNoWarnings(messageSet, "admin.errors.project.import.custom.field.wrong.context [Mad Stuff] [FUBAR]");
    }

    @Test
    public void testValidateMappings_HappyPath()
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(22, "My Stuff", "Date"));
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "Numeric"));

        final BackupProject backupProject = backupProject(
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "Date"), "321"),
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("14", "My Stuff", "Numeric"), "321"));
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        customFieldMapper.flagValueAsRequired("14", "1000");
        customFieldMapper.flagIssueTypeInUse("1000", "1");
        customFieldMapper.registerOldValue("14", "Mad Stuff");
        customFieldMapper.mapValue("12", "22");
        customFieldMapper.mapValue("14", "24");

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");

        final ConstantsManager constantsManager = mockConstantsManager(new MockIssueType("2", "FUBAR"));
        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, constantsManager, null)
        {
            // This method is tested separately, so we can mock it out here.
            public boolean customFieldIsValidForRequiredContexts(final ExternalCustomFieldConfiguration externalCustomFieldConfiguration, final CustomField newCustomField, final String oldCustomFieldId, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final String projectKey)
            {
                // Valid for all
                return true;
            }

            public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
            {
                return true;
            }
        };

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nHelper(), backupProject, issueTypeMapper, customFieldMapper);
        assertNoMessages(messageSet);
    }

    @Test
    public void testCustomFieldTypePluginMissing() throws Exception
    {
        final MockCustomFieldManager mockCustomFieldManager = new MockCustomFieldManager();
        mockCustomFieldManager.addCustomField(createCustomField(24, "Mad Stuff", "Numeric"));

        final BackupProject backupProject = backupProject(
                new ExternalCustomFieldConfiguration(null, null, new ExternalCustomField("12", "My Stuff", "Date"), "321"));

        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.flagValueAsRequired("12", "1000");
        customFieldMapper.registerOldValue("12", "My Stuff");
        // Throw in some orphan data for coverage.
        customFieldMapper.flagValueAsRequired("14", "1000");

        final IssueTypeMapper issueTypeMapper = new IssueTypeMapper();
        issueTypeMapper.mapValue("1", "2");

        final ConstantsManager constantsManager = mockConstantsManager(new MockIssueType("2", "FUBAR"));
        final CustomFieldMapperValidatorImpl customFieldMapperValidator = new CustomFieldMapperValidatorImpl(mockCustomFieldManager, constantsManager, null)
        {
            // This method is tested separately, so we can mock it out here.
            public boolean customFieldIsValidForRequiredContexts(final ExternalCustomFieldConfiguration externalCustomFieldConfiguration, final CustomField newCustomField, final String oldCustomFieldId, final CustomFieldMapper customFieldMapper, final IssueTypeMapper issueTypeMapper, final String projectKey)
            {
                // Valid for all
                return true;
            }

            public boolean customFieldTypeIsImportable(final String customFieldTypeKey)
            {
                return true;
            }
        };

        final MessageSet messageSet = customFieldMapperValidator.validateMappings(new MockI18nHelper(), backupProject, issueTypeMapper, customFieldMapper);
        assert1WarningNoErrors(messageSet, "admin.errors.project.import.custom.field.plugin.missing [My Stuff] [Date]");
    }

    private CustomField createCustomField(final long id, final String name, final String typeKey)
    {
        CustomField customField = mock(CustomField.class);
        when(customField.getId()).thenReturn(String.valueOf(id));
        when(customField.getName()).thenReturn(name);

        CustomFieldType customFieldType = mockCustomFieldType(typeKey);
        when(customField.getCustomFieldType()).thenReturn(customFieldType);

        return customField;
    }

    private CustomFieldType mockCustomFieldType(String typeKey)
    {
        CustomFieldType customFieldType = mock(CustomFieldType.class);
        when(customFieldType.getKey()).thenReturn(typeKey).getMock();
        return customFieldType;
    }

    private FieldConfig anyFieldConfig()
    {
        return new FieldConfigImpl(null, null, null, Collections.<FieldConfigItemType>emptyList(), null);
    }

    private static ConstantsManager mockConstantsManager(IssueType... issueTypes)
    {
        final ConstantsManager constantsManager = mock(ConstantsManager.class);
        for (IssueType issueType : issueTypes)
        {
            when(constantsManager.getIssueTypeObject(issueType.getId())).thenReturn(issueType);
        }
        return constantsManager;
    }

    private static BackupProject backupProject(final ExternalCustomFieldConfiguration... customFieldConfigurations)
    {
        return new BackupProjectImpl(
                new ExternalProject(),
                ImmutableList.<ExternalVersion>of(),
                ImmutableList.<ExternalComponent>of(),
                ImmutableList.copyOf(customFieldConfigurations),
                ImmutableList.<Long>of());
    }
}
