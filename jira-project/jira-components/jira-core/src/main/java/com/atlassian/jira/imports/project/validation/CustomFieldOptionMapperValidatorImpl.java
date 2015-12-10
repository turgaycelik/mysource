package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldConfiguration;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldOption;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.mapper.CustomFieldOptionMapper;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @since v3.13
 */
public class CustomFieldOptionMapperValidatorImpl implements CustomFieldOptionMapperValidator
{
    private static final Logger log = Logger.getLogger(CustomFieldOptionMapperValidatorImpl.class);

    private final OptionsManager optionsManager;

    public CustomFieldOptionMapperValidatorImpl(final OptionsManager optionsManager)
    {
        this.optionsManager = optionsManager;
    }

    public void validateMappings(final I18nHelper i18nHelper, final BackupProject backupProject, final CustomFieldOptionMapper customFieldOptionMapper, final CustomFieldMapper customFieldMapper, final Map customFieldValueMessageSets)
    {
        // Loop through the "required" values
        for (final String oldId : customFieldOptionMapper.getRequiredOldIds())
        {
            final ExternalCustomFieldOption oldCustomFieldOption = customFieldOptionMapper.getCustomFieldOption(oldId);
            if (oldCustomFieldOption == null)
            {
                log.warn("Cannot map required Custom Field Option '" + oldId + "' because there was no configuration for it in the import file. This is orphan data that is no longer valid.");
            }
            else
            {
                // Check if this is from a valid context -
                if (isValidContext(oldCustomFieldOption, backupProject))
                {
                    // Check that we have a mapping
                    final String newId = customFieldOptionMapper.getMappedId(oldId);
                    if (newId == null)
                    {
                        // Required but not mapped.
                        final String nameFor = getDisplayNameFor(oldCustomFieldOption, customFieldOptionMapper);

                        if (oldCustomFieldOption.getParentId() == null)
                        {
                            final MessageSet messageSet = getMessageSetForFieldId(oldCustomFieldOption.getCustomFieldId(),
                                    customFieldValueMessageSets);
                            messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.option.does.not.exist",
                                    customFieldMapper.getDisplayName(oldCustomFieldOption.getCustomFieldId()), nameFor));
                            messageSet.addErrorMessageInEnglish("The custom field '" + customFieldMapper.getDisplayName(oldCustomFieldOption.getCustomFieldId()) + "' requires option '" + nameFor + "' for the import but it does not exist in the current JIRA instance.");
                        }
                        else
                        {
                            final String parentName = getParentOptionValue(oldCustomFieldOption, customFieldOptionMapper);
                            final String childOptionValue = oldCustomFieldOption.getValue();
                            final MessageSet messageSet = getMessageSetForFieldId(oldCustomFieldOption.getCustomFieldId(),
                                    customFieldValueMessageSets);
                            messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.option.child.does.not.exist",
                                    customFieldMapper.getDisplayName(oldCustomFieldOption.getCustomFieldId()), parentName, childOptionValue));
                            messageSet.addErrorMessageInEnglish("The custom field '" + customFieldMapper.getDisplayName(oldCustomFieldOption.getCustomFieldId()) + "' requires option with parent '" + parentName + "' and value '" + childOptionValue + "' for the import but it does not exist in the current JIRA instance.");
                        }
                    }
                    else
                    {
                        // Please note that the following checks are redundant. The auto-mapper should never put the mappings in this state.

                        // Get the option in the current system.
                        final Option newOption = optionsManager.findByOptionId(new Long(newId));
                        if (newOption == null)
                        {
                            final String displayNameFor = getDisplayNameFor(oldCustomFieldOption, customFieldOptionMapper);
                            final MessageSet messageSet = getMessageSetForFieldId(oldCustomFieldOption.getCustomFieldId(),
                                    customFieldValueMessageSets);
                            messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.option.does.not.exist",
                                    customFieldMapper.getDisplayName(oldCustomFieldOption.getCustomFieldId()), displayNameFor));
                            // Add a message for the log.
                            messageSet.addErrorMessageInEnglish("The custom field '" + customFieldMapper.getDisplayName(oldCustomFieldOption.getCustomFieldId()) + "' requires option '" + displayNameFor + "' for the import but it does not exist in the current JIRA instance.");
                        }
                        else
                        {
                            final MessageSet messageSet = getMessageSetForFieldId(oldCustomFieldOption.getCustomFieldId(),
                                    customFieldValueMessageSets);
                            validateNewOption(i18nHelper, customFieldOptionMapper, customFieldMapper, messageSet, oldCustomFieldOption, newOption);
                        }
                    }
                }
                else
                {
                    log.debug("Custom Field Option '" + oldCustomFieldOption.getValue() + " (" + oldCustomFieldOption.getId() + ") from the backup file is being safely ignored because it is invalid.");
                }
            }
        }
    }

    /**
     * Returns a full display name for the given custom field option.
     * If the option has a parent, then it returns a "fully-qualified" path-like name like it is displayed in the View Screen.
     *
     * @param oldCustomFieldOption The option to get a name for.
     * @param customFieldOptionMapper CustomFieldOptionMapper
     * @return a full display name for the given custom field option.
     */
    private String getDisplayNameFor(final ExternalCustomFieldOption oldCustomFieldOption, final CustomFieldOptionMapper customFieldOptionMapper)
    {
        if (oldCustomFieldOption.getParentId() == null)
        {
            return oldCustomFieldOption.getValue();
        }
        final String parentName = getParentOptionValue(oldCustomFieldOption, customFieldOptionMapper);

        return parentName + " - " + oldCustomFieldOption.getValue();
    }

    String getParentOptionValue(final ExternalCustomFieldOption oldCustomFieldOption, final CustomFieldOptionMapper customFieldOptionMapper)
    {
        // Get the parent option as well
        final ExternalCustomFieldOption parentCustomFieldOption = customFieldOptionMapper.getCustomFieldOption(oldCustomFieldOption.getParentId());
        String parentName;
        if (parentCustomFieldOption == null)
        {
            parentName = "???";
        }
        else
        {
            parentName = parentCustomFieldOption.getValue();
        }
        return parentName;
    }

    MessageSet getMessageSetForFieldId(final String oldCustomFieldId, final Map customFieldValueMessageSets)
    {
        MessageSet customFieldMessageSet = (MessageSet) customFieldValueMessageSets.get(oldCustomFieldId);
        if (customFieldMessageSet == null)
        {
            customFieldMessageSet = new MessageSetImpl();
            customFieldValueMessageSets.put(oldCustomFieldId, customFieldMessageSet);
        }
        return customFieldMessageSet;
    }

    /**
     * Validates the new option that we are mapped to.
     *
     * @param i18nHelper I18nHelper
     * @param customFieldOptionMapper CustomFieldOptionMapper
     * @param customFieldMapper CustomFieldMapper
     * @param messageSet MessageSet
     * @param oldCustomFieldOption ExternalCustomFieldOption
     * @param newOption Option
     */
    void validateNewOption(final I18nHelper i18nHelper, final CustomFieldOptionMapper customFieldOptionMapper, final CustomFieldMapper customFieldMapper, final MessageSet messageSet, final ExternalCustomFieldOption oldCustomFieldOption, final Option newOption)
    {
        // Check that it is for the current Custom Field.
        final String mappedCustomFieldId = customFieldMapper.getMappedId(oldCustomFieldOption.getCustomFieldId());
        final FieldConfig relatedCustomField = newOption.getRelatedCustomField();
        final CustomField newCustomField = relatedCustomField.getCustomField();
        final Long newCustomFieldId = newCustomField.getIdAsLong();
        if (!newCustomFieldId.toString().equals(mappedCustomFieldId))
        {
            final String nameFor = getDisplayNameFor(oldCustomFieldOption, customFieldOptionMapper);
            messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.option.wrong.custom.field",
                customFieldMapper.getDisplayName(oldCustomFieldOption.getCustomFieldId()), nameFor));
            messageSet.addErrorMessageInEnglish("The custom field '" + customFieldMapper.getDisplayName(oldCustomFieldOption.getCustomFieldId()) + "' does not contain option '" + nameFor + "', the import can not continue.");
        }
        else
        {
            // Check that the mapped option agrees with the level
            validateOptionLevels(i18nHelper, customFieldOptionMapper, messageSet, oldCustomFieldOption, newOption);
        }
    }

    /**
     * This method validates first of all that the old an new option are either both parents, or both children.
     * Also, if they are children, it makes sure that the parents are mapped to each other.
     *
     * @param i18nHelper I18nHelper
     * @param customFieldOptionMapper CustomFieldOptionMapper
     * @param messageSet MessageSet
     * @param oldCustomFieldOption ExternalCustomFieldOption
     * @param newOption Option
     */
    void validateOptionLevels(final I18nHelper i18nHelper, final CustomFieldOptionMapper customFieldOptionMapper, final MessageSet messageSet, final ExternalCustomFieldOption oldCustomFieldOption, final Option newOption)
    {
        // Now check that it is at the correct level
        if (oldCustomFieldOption.getParentId() == null)
        {
            // Old Option is a parent - make sure the new one is a parent too.
            if (newOption.getParentOption() != null)
            {
                messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.option.old.parent.new.child",
                    oldCustomFieldOption.getValue(), newOption.getValue()));
                messageSet.addErrorMessageInEnglish("The parent custom field option '" + oldCustomFieldOption.getValue() + "' from the import file was mapped to a child option ('" + newOption.getValue() + "') in the current JIRA instance.");
            }
        }
        else
        {
            // Old Option is a child - make sure the new one is a child too.
            if (newOption.getParentOption() == null)
            {
                messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.option.old.child.new.parent",
                    oldCustomFieldOption.getValue(), newOption.getValue()));
                messageSet.addErrorMessageInEnglish("The child custom field option '" + oldCustomFieldOption.getValue() + "' from the import file was mapped to a parent option ('" + newOption.getValue() + "') in the current JIRA instance.");
            }
            else
            {
                // Both are children - check the old and new parents are valid.
                final String mappedParentId = customFieldOptionMapper.getMappedId(oldCustomFieldOption.getParentId());
                if (mappedParentId == null)
                {
                    final String displayNameFor = getDisplayNameFor(oldCustomFieldOption, customFieldOptionMapper);
                    messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.custom.field.option.childs.parent.not.mapped",
                        displayNameFor));
                    messageSet.addErrorMessageInEnglish("The child custom field option '" + displayNameFor + "' from the import file has a parent option that is not mapped.");
                }
                else
                {
                    // Check that the option's parent is mapped
                    if (!mappedParentId.equals(newOption.getParentOption().getOptionId().toString()))
                    {
                        final String nameFor = getDisplayNameFor(oldCustomFieldOption, customFieldOptionMapper);
                        messageSet.addErrorMessage(i18nHelper.getText(
                            "admin.errors.project.import.custom.field.option.old.childs.parent.mapping.invalid", nameFor, newOption.getValue()));
                        messageSet.addErrorMessageInEnglish("The child custom field option '" + nameFor + "' from the import file is mapped to option ('" + newOption.getValue() + "') in the current JIRA instance, but their parents aren''t mapped to each other.");
                    }
                }
            }
        }
    }

    boolean isValidContext(final ExternalCustomFieldOption customFieldOption, final BackupProject backupProject)
    {
        // Find the relevant config for this custom field in the context of our import project
        final ExternalCustomFieldConfiguration customFieldConfiguration = backupProject.getCustomFieldConfiguration(customFieldOption.getCustomFieldId());
        if (customFieldConfiguration == null)
        {
            // This Custom Field is not valid at all for this project.
            return false;
        }
        // If the config scheme id for the given option is the same, then we are good.
        return customFieldConfiguration.getConfigurationSchemeId().equals(customFieldOption.getFieldConfigId());
    }
}
