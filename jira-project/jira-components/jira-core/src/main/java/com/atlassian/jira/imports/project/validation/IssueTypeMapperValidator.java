package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.IssueTypeMapper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;

/**
 * Validates the automatic mappings that have been created to see if the mappings are relevant in the current
 * JIRA instances setup. This validator makes sure that the issue types that are required:
 * <ul>
 * <li>exists in the current instance</li>
 * <li>is valid for the current projects issue type scheme</li>
 * <li>is or is not a subtask based on how the issue type was defined in the backup system</li>
 * </ul>
 *
 * @since v3.13
 */
public interface IssueTypeMapperValidator
{

    /**
     * Makes sure that the issue types that are required:
     * <ul>
     * <li>exists in the current instance</li>
     * <li>is valid for the current projects issue type scheme</li>
     * <li>is or is not a subtask based on how the issue type was defined in the backup system</li>
     * </ul>
     *
     * @param i18nHelper helper bean that allows us to get i18n translations
     * @param backupProject is the backup project the data is mapped from
     * @param issueTypeMapper is the populated issueTypeMapper
     * @return a MessageSet that will contain any generated errors (which should stop the import) or warnings
     * (which should be displayed to the user). The error and warning collection's will be empty if all validation
     * passes.
     */
    MessageSet validateMappings(I18nHelper i18nHelper, BackupProject backupProject, IssueTypeMapper issueTypeMapper);
}
