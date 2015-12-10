package com.atlassian.jira.imports.project.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.action.issue.customfields.option.MockOption;
import com.atlassian.jira.external.beans.ExternalComponent;
import com.atlassian.jira.external.beans.ExternalCustomField;
import com.atlassian.jira.external.beans.ExternalProject;
import com.atlassian.jira.external.beans.ExternalVersion;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupProjectImpl;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldOption;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.mapper.CustomFieldOptionMapper;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static com.atlassian.jira.mock.Strict.strict;
import static com.atlassian.jira.util.MessageSetAssert.assert1ErrorNoWarnings;
import static com.atlassian.jira.util.MessageSetAssert.assertErrorMessages;
import static com.atlassian.jira.util.MessageSetAssert.assertNoMessages;
import static com.atlassian.jira.util.MessageSetAssert.assertNoWarnings;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v3.13
 */
public class TestCustomFieldOptionMapperValidatorImpl
{
    @Test
    public void testIsValidContextNoCustomFieldConfiguration()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null);

        final BackupProject backupProject = backupProject();
        final ExternalCustomFieldOption customFieldOption = new ExternalCustomFieldOption(null, null, null, null, null);

        assertFalse(customFieldOptionMapperValidator.isValidContext(customFieldOption, backupProject));
    }

    @Test
    public void testIsValidContextWrongCustomFieldConfiguration()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null);

        final ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null,
            new ExternalCustomField("1111", "", ""), "12");
        final BackupProject backupProject = backupProject(externalCustomFieldConfiguration);
        final ExternalCustomFieldOption customFieldOption = new ExternalCustomFieldOption(null, "1111", null, null, null);

        assertFalse(customFieldOptionMapperValidator.isValidContext(customFieldOption, backupProject));
    }

    @Test
    public void testIsValidContextHappyPath()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null);

        final ExternalCustomFieldConfiguration externalCustomFieldConfiguration = new ExternalCustomFieldConfiguration(null, null,
            new ExternalCustomField("1111", "", ""), "12");
        final BackupProject backupProject = backupProject(externalCustomFieldConfiguration);
        final ExternalCustomFieldOption customFieldOption = new ExternalCustomFieldOption(null, "1111", "12", null, null);

        assertTrue(customFieldOptionMapperValidator.isValidContext(customFieldOption, backupProject));
    }

    @Test
    public void testValidateOptionLevelsOldParentNewChild()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null);

        final MessageSet messageSet = new MessageSetImpl();
        final ExternalCustomFieldOption oldCustomFieldOption = new ExternalCustomFieldOption("12", null, null, null, "Bobby");
        final Option parentOption = new MockOption(null, null, null, null, null, null);
        final Option newOption = new MockOption(parentOption, null, null, "Chocolate", null, null);
        customFieldOptionMapperValidator.validateOptionLevels(new MockI18nHelper(), null, messageSet, oldCustomFieldOption, newOption);

        assert1ErrorNoWarnings(messageSet, "admin.errors.project.import.custom.field.option.old.parent.new.child [Bobby] [Chocolate]");
    }

    @Test
    public void testValidateOptionLevelsOldParentNewParent()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null);

        final MessageSet messageSet = new MessageSetImpl();
        final ExternalCustomFieldOption oldCustomFieldOption = new ExternalCustomFieldOption("12", null, null, null, "Bobby");
        final Option newOption = new MockOption(null, null, null, "Chocolate", null, null);
        customFieldOptionMapperValidator.validateOptionLevels(new MockI18nHelper(), null, messageSet, oldCustomFieldOption, newOption);
        assertFalse(messageSet.hasAnyErrors());
    }

    @Test
    public void testValidateOptionLevelsOldChildNewParent()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null);

        final MessageSet messageSet = new MessageSetImpl();
        final ExternalCustomFieldOption oldCustomFieldOption = new ExternalCustomFieldOption("12", null, null, "56", "Bobby");
        final Option newOption = new MockOption(null, null, null, "Chocolate", null, null);
        customFieldOptionMapperValidator.validateOptionLevels(new MockI18nHelper(), null, messageSet, oldCustomFieldOption, newOption);

        assert1ErrorNoWarnings(messageSet, "admin.errors.project.import.custom.field.option.old.child.new.parent [Bobby] [Chocolate]");
    }

    @Test
    public void testValidateOptionLevelsOldChildParentNotMapped()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null);

        final MessageSet messageSet = new MessageSetImpl();
        final ExternalCustomFieldOption oldCustomFieldOption = new ExternalCustomFieldOption("12", null, null, "10", "Bobby");
        final Option parentOption = new MockOption(null, null, null, null, null, null);
        final Option newOption = new MockOption(parentOption, null, null, "Chocolate", null, null);

        final CustomFieldOptionMapper customFieldOptionMapper = new CustomFieldOptionMapper();
        customFieldOptionMapper.registerOldValue(new ExternalCustomFieldOption("10", null, null, "56", "Brady"));

        customFieldOptionMapperValidator.validateOptionLevels(new MockI18nHelper(), customFieldOptionMapper, messageSet, oldCustomFieldOption,
            newOption);

        assert1ErrorNoWarnings(messageSet, "admin.errors.project.import.custom.field.option.childs.parent.not.mapped [Brady - Bobby]");
    }

    @Test
    public void testValidateOptionLevelsOldChildParentMappingInvalid()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null);

        final MessageSet messageSet = new MessageSetImpl();
        final ExternalCustomFieldOption oldCustomFieldOption = new ExternalCustomFieldOption("12", null, null, "56", "Bobby");
        final Option parentOption = new MockOption(null, null, null, null, null, 18536L);
        final Option newOption = new MockOption(parentOption, null, null, "Chocolate", null, null);

        final CustomFieldOptionMapper customFieldOptionMapper = new CustomFieldOptionMapper();
        // Set up the old parent to map to a different ID than the new parent.
        customFieldOptionMapper.mapValue("56", "42");
        customFieldOptionMapper.registerOldValue(new ExternalCustomFieldOption("56", null, null, null, "Brady"));

        customFieldOptionMapperValidator.validateOptionLevels(new MockI18nHelper(), customFieldOptionMapper, messageSet, oldCustomFieldOption,
            newOption);
        assert1ErrorNoWarnings(messageSet,
                "admin.errors.project.import.custom.field.option.old.childs.parent.mapping.invalid [Brady - Bobby] [Chocolate]");
    }

    @Test
    public void testValidateOptionLevelsOldChildHappyPath()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null);

        final MessageSet messageSet = new MessageSetImpl();
        final ExternalCustomFieldOption oldCustomFieldOption = new ExternalCustomFieldOption("12", null, null, "56", "Bobby");
        final Option parentOption = new MockOption(null, null, null, null, null, 18536L);
        final Option newOption = new MockOption(parentOption, null, null, "Chocolate", null, null);

        final CustomFieldOptionMapper customFieldOptionMapper = new CustomFieldOptionMapper();
        // Set up the old parent to map to a different ID than the new parent.
        customFieldOptionMapper.mapValue("56", "18536");

        customFieldOptionMapperValidator.validateOptionLevels(new MockI18nHelper(), customFieldOptionMapper, messageSet, oldCustomFieldOption,
            newOption);
        assertFalse(messageSet.hasAnyErrors());
    }

    @Test
    public void testValidateNewOptionIncorrectCustomField()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null);

        final MessageSet messageSet = new MessageSetImpl();
        final ExternalCustomFieldOption oldCustomFieldOption = new ExternalCustomFieldOption("12", "1", null, null, "Strawberry");
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.mapValue("1", "2");
        customFieldMapper.registerOldValue("1", "Flavour");

        final CustomField customField = mock(CustomField.class, strict());
        doReturn(156893972L).when(customField).getIdAsLong();
        final FieldConfig fieldConfig = mock(FieldConfig.class, strict());
        doReturn(customField).when(fieldConfig).getCustomField();
        final Option newOption = new MockOption(null, null, null, "Chocolate", fieldConfig, null);

        customFieldOptionMapperValidator.validateNewOption(new MockI18nHelper(), new CustomFieldOptionMapper(), customFieldMapper, messageSet,
                oldCustomFieldOption, newOption);
        assert1ErrorNoWarnings(messageSet, "admin.errors.project.import.custom.field.option.wrong.custom.field [Flavour] [Strawberry]");
    }

    @Test
    public void testValidateNewOptionHappyPath()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null);
        final MessageSet messageSet = new MessageSetImpl();

        final ExternalCustomFieldOption oldCustomFieldOption = new ExternalCustomFieldOption("12", "1", "777777", null, "Strawberry");
        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.mapValue("1", "2");
        customFieldMapper.registerOldValue("1", "Flavour");

        final CustomField customField = mock(CustomField.class, strict());
        doReturn(2L).when(customField).getIdAsLong();
        final FieldConfig fieldConfig = mock(FieldConfig.class, strict());
        doReturn(customField).when(fieldConfig).getCustomField();
        final Option newOption = new MockOption(null, null, null, "Chocolate", fieldConfig, null);

        customFieldOptionMapperValidator.validateNewOption(new MockI18nHelper(), null, customFieldMapper, messageSet, oldCustomFieldOption, newOption);
        assertNoMessages(messageSet);
    }

    @Test
    public void testValidateMappings()
    {
        final OptionsManager optionsManager = mock(OptionsManager.class);
        when(optionsManager.findByOptionId(555L)).thenReturn(new MockOption(null, null, null, null, null, null));

        final AtomicBoolean validateNewCalled = new AtomicBoolean(false);
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(optionsManager)
        {
            @Override
            boolean isValidContext(final ExternalCustomFieldOption customFieldOption, final BackupProject backupProject)
            {
                return true;
            }

            @Override
            void validateNewOption(final I18nHelper i18nHelper, final CustomFieldOptionMapper customFieldOptionMapper, final CustomFieldMapper customFieldMapper, final MessageSet messageSet, final ExternalCustomFieldOption oldCustomFieldOption, final Option newOption)
            {
                validateNewCalled.set(true);
            }
        };

        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.mapValue("1", "2");
        customFieldMapper.registerOldValue("1", "Flavour");

        final ExternalCustomFieldOption oldCustomFieldOption = new ExternalCustomFieldOption("111", "1", "777777", null, "Strawberry");
        final ExternalCustomFieldOption oldCustomFieldOption2 = new ExternalCustomFieldOption("222", "1", "777777", null, "Choc");
        final ExternalCustomFieldOption oldCustomFieldOption3 = new ExternalCustomFieldOption("444", "1", "777777", null, "Choc");
        final CustomFieldOptionMapper customFieldOptionMapper = new CustomFieldOptionMapper();
        customFieldOptionMapper.registerOldValue(oldCustomFieldOption);
        customFieldOptionMapper.flagValueAsRequired("111");
        customFieldOptionMapper.registerOldValue(oldCustomFieldOption2);
        customFieldOptionMapper.flagValueAsRequired("222");
        customFieldOptionMapper.registerOldValue(oldCustomFieldOption3);
        customFieldOptionMapper.flagValueAsRequired("444");
        customFieldOptionMapper.mapValue("111", "333");
        customFieldOptionMapper.mapValue("444", "555");
        // Don't map 222

        final Map<String,MessageSet> customFieldValueMessageSets = new HashMap<String,MessageSet>(4);
        customFieldOptionMapperValidator.validateMappings(new MockI18nHelper(), null, customFieldOptionMapper, customFieldMapper,
            customFieldValueMessageSets);

        assertThat(customFieldValueMessageSets.keySet(), hasSize(1));
        final MessageSet messageSet = customFieldValueMessageSets.get("1");
        assertNoWarnings(messageSet);
        assertErrorMessages(messageSet,
                "admin.errors.project.import.custom.field.option.does.not.exist [Flavour] [Strawberry]",
                "admin.errors.project.import.custom.field.option.does.not.exist [Flavour] [Choc]");
        assertTrue("validateNewCalled", validateNewCalled.get());
    }

    @Test
    public void testValidateMappingsOptionsWithParents()
    {
        final OptionsManager optionsManager = mock(OptionsManager.class);
        when(optionsManager.findByOptionId(555L)).thenReturn(new MockOption(null, null, null, null, null, null));

        final AtomicBoolean validateNewCalled = new AtomicBoolean(false);
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(optionsManager)
        {
            @Override
            boolean isValidContext(final ExternalCustomFieldOption customFieldOption, final BackupProject backupProject)
            {
                return true;
            }

            @Override
            void validateNewOption(final I18nHelper i18nHelper, final CustomFieldOptionMapper customFieldOptionMapper, final CustomFieldMapper customFieldMapper, final MessageSet messageSet, final ExternalCustomFieldOption oldCustomFieldOption, final Option newOption)
            {
                validateNewCalled.set(true);
            }

            @Override
            String getParentOptionValue(final ExternalCustomFieldOption oldCustomFieldOption, final CustomFieldOptionMapper customFieldOptionMapper)
            {
                return "Parent";
            }
        };

        final CustomFieldMapper customFieldMapper = new CustomFieldMapper();
        customFieldMapper.mapValue("1", "2");
        customFieldMapper.registerOldValue("1", "Flavour");

        final ExternalCustomFieldOption oldCustomFieldOption2 = new ExternalCustomFieldOption("222", "1", "777777", "Good", "Choc");
        final ExternalCustomFieldOption oldCustomFieldOption3 = new ExternalCustomFieldOption("444", "1", "777777", "Good", "Choc");
        final CustomFieldOptionMapper customFieldOptionMapper = new CustomFieldOptionMapper();
        customFieldOptionMapper.flagValueAsRequired("111");
        customFieldOptionMapper.registerOldValue(oldCustomFieldOption2);
        customFieldOptionMapper.flagValueAsRequired("222");
        customFieldOptionMapper.registerOldValue(oldCustomFieldOption3);
        customFieldOptionMapper.flagValueAsRequired("444");
        customFieldOptionMapper.mapValue("111", "333");
        customFieldOptionMapper.mapValue("444", "555");
        // Don't map 222

        final Map<String,MessageSet> customFieldValueMessageSets = new HashMap<String,MessageSet>(4);
        customFieldOptionMapperValidator.validateMappings(new MockI18nHelper(), null, customFieldOptionMapper,
                customFieldMapper, customFieldValueMessageSets);
        assertThat(customFieldValueMessageSets.keySet(), hasSize(1));
        final MessageSet messageSet = customFieldValueMessageSets.get("1");
        assert1ErrorNoWarnings(messageSet, "admin.errors.project.import.custom.field.option.child.does.not.exist [Flavour] [Parent] [Choc]");
        assertTrue("validateNewCalled", validateNewCalled.get());
    }

    @Test
    public void testValidateMappingsNotValid()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null)
        {
            boolean isValidContext(final ExternalCustomFieldOption customFieldOption, final BackupProject backupProject)
            {
                return false;
            }
        };
        final ExternalCustomFieldOption oldCustomFieldOption = new ExternalCustomFieldOption("111", "1", "777777", null, "Strawberry");
        final CustomFieldOptionMapper customFieldOptionMapper = new CustomFieldOptionMapper();
        customFieldOptionMapper.registerOldValue(oldCustomFieldOption);
        customFieldOptionMapper.flagValueAsRequired("111");

        final Map<String,MessageSet> customFieldValueMessageSets = new HashMap<String,MessageSet>(4);
        customFieldOptionMapperValidator.validateMappings(new MockI18nHelper(), null, customFieldOptionMapper, null, customFieldValueMessageSets);
        assertThat(customFieldValueMessageSets.keySet(), hasSize(0));
    }

    @Test
    public void testOrphanData()
    {
        final CustomFieldOptionMapperValidatorImpl customFieldOptionMapperValidator = new CustomFieldOptionMapperValidatorImpl(null)
        {
            boolean isValidContext(final ExternalCustomFieldOption customFieldOption, final BackupProject backupProject)
            {
                // This should not be called.
                throw new AssertionError("Don't call me - the required option has no config.");
            }
        };
        final CustomFieldOptionMapper customFieldOptionMapper = new CustomFieldOptionMapper();
        // Make the Mapper reflect some invalid data file.
        customFieldOptionMapper.flagValueAsRequired("12");

        final Map<String,MessageSet> customFieldValueMessageSets = new HashMap<String,MessageSet>(4);
        customFieldOptionMapperValidator.validateMappings(null, null, customFieldOptionMapper, null, customFieldValueMessageSets);
        assertThat(customFieldValueMessageSets.keySet(), hasSize(0));
    }

    private static BackupProject backupProject(ExternalCustomFieldConfiguration... customFieldConfigurations)
    {
        return new BackupProjectImpl(
                new ExternalProject(),
                ImmutableList.<ExternalVersion>of(),
                ImmutableList.<ExternalComponent>of(),
                ImmutableList.copyOf(customFieldConfigurations),
                ImmutableList.<Long>of());
    }
}
