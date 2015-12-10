package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.CustomFieldMapper;
import com.atlassian.jira.imports.project.mapper.CustomFieldOptionMapper;
import com.atlassian.jira.util.I18nHelper;

import java.util.Map;

/**
 * Validates the automatic mappings that have been created to see if the mappings are relevant in the current JIRA instances setup.
 * Basically we just check that required Custom Field Options exist in the new system.
 * However, it is possible to get invalid data in a Custom Field Value record.
 * If a "required" option is not actually valid in the correct context, then we only mark this as a warning, as this data
 * can be ignored in the import.
 *
 * @since v3.13
 */
public interface CustomFieldOptionMapperValidator
{
    /**
     * Validates the automatic mappings that have been created to see if the mappings are relevant in the current JIRA instances setup.
     * Basically we just check that required Custom Field Options exist in the new system.
     * However, it is possible to get invalid data in a Custom Field Value record.
     * If a "required" option is not actually valid in the correct context, then we only mark this as a warning, as this data
     * can be ignored in the import.
     *
     * @param i18nHelper used to i18n error messages
     * @param backupProject used to determine the project portion which helps identify which custom field configuration we are using
     * @param customFieldOptionMapper the mapper that is being validated, should have been mapped and flagged as required.
     * @param customFieldMapper a fully populated and validated custom field mapper.
     * @param customFieldValueMessageSets this is a map of that is keyed by the old custom field id who's value is a message set
     * of errors and warnings about that custom field.  This is where options related errors and warnings will be reported.
     */
    void validateMappings(final I18nHelper i18nHelper, final BackupProject backupProject, final CustomFieldOptionMapper customFieldOptionMapper, final CustomFieldMapper customFieldMapper, final Map customFieldValueMessageSets);
}
