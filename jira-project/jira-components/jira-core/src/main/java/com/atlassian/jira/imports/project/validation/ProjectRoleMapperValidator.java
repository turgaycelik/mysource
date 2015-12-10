package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;

/**
 * Validates that a ProjectRoleMapper has all required project roles mapped.
 *
 * @since v3.13
 */
public class ProjectRoleMapperValidator
{
    public MessageSet validateMappings(final I18nHelper i18nHelper, final SimpleProjectImportIdMapper simpleProjectImportIdMapper)
    {
        final MessageSet messageSet = new MessageSetImpl();
        for (final String oldId : simpleProjectImportIdMapper.getRequiredOldIds())
        {
            // Get the mapped id
            final String newId = simpleProjectImportIdMapper.getMappedId(oldId);
            if (newId == null)
            {
                // Check for orphan data
                if (simpleProjectImportIdMapper.getKey(oldId) == null)
                {
                    messageSet.addWarningMessage(i18nHelper.getText("admin.errors.project.import.project.role.validation.orphan", oldId));
                    messageSet.addWarningMessageInEnglish("The project role with id '" + oldId + "' can not be resolved into an actual project role in the backup file. Any comments or worklogs that were protected by this project role will no longer have a visibility restriction.");
                }
                else
                {
                    // Add an error that the value is not mapped
                    messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.project.role.validation.does.not.exist",
                            simpleProjectImportIdMapper.getDisplayName(oldId)));
                    messageSet.addErrorMessageInEnglish("The Project Role '" + simpleProjectImportIdMapper.getDisplayName(oldId) + "' is required for the import but does not exist in the current JIRA instance.");
                }
            }
        }
        return messageSet;
    }

}
