package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.mapper.SimpleProjectImportIdMapper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import org.apache.log4j.Logger;

/**
 * @since v3.13
 */
public abstract class AbstractSimpleMapperValidator implements MapperValidator
{
    private static final Logger log = Logger.getLogger(AbstractSimpleMapperValidator.class);

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
                    log.warn("Project Import: The " + getEntityName() + " with ID '" + oldId + "' is orphan data and will be ignored.");
                }
                else
                {
                    // Add an error that the value is not mapped
                    messageSet.addErrorMessage(i18nHelper.getText(getEntityDoesNotExistKey(), simpleProjectImportIdMapper.getDisplayName(oldId)));
                    messageSet.addErrorMessageInEnglish("The " + getEntityName() + " '" + simpleProjectImportIdMapper.getDisplayName(oldId) + "' is required for the import but does not exist in the current JIRA instance.");
                }
            }
        }
        return messageSet;
    }

    /**
     * @return gets the i18n key that should be added to the error message set if the mapping is not valid
     */
    protected abstract String getEntityDoesNotExistKey();

    /**
     * Returns the name of the entity that this validator is for.
     * eg "Group", "Project Role", etc.
     * @return the name of the entity that this validator is for.
     */
    protected abstract String getEntityName();
}
