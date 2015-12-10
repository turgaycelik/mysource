package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.mapper.IssueLinkTypeMapper;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @since v3.13
 */
public class IssueLinkTypeMapperValidatorImpl implements IssueLinkTypeMapperValidator
{
    private static final Logger log = Logger.getLogger(IssueLinkTypeMapperValidatorImpl.class);

    private final IssueLinkTypeManager issueLinkTypeManager;
    private final SubTaskManager subTaskManager;
    private final ApplicationProperties applicationProperties;

    public IssueLinkTypeMapperValidatorImpl(final IssueLinkTypeManager issueLinkTypeManager, final SubTaskManager subTaskManager, final ApplicationProperties applicationProperties)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.subTaskManager = subTaskManager;
        this.applicationProperties = applicationProperties;
    }

    public MessageSet validateMappings(final I18nHelper i18nHelper, final BackupProject backupProject, final IssueLinkTypeMapper issueLinkTypeMapper)
    {
        final MessageSet messageSet = new MessageSetImpl();

        // Check if there are Issue Links to import
        if (issueLinkTypeMapper.getRegisteredOldIds().isEmpty())
        {
            // nothing to validate
            return messageSet;
        }

        // Check if they need Issue Linking turned on.
        if (!isIssueLinkingEnabled())
        {
            messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issuelinktype.disabled"));
            messageSet.addErrorMessageInEnglish("Issue Linking must be enabled because there are issue links in the project to import.");
        }

        // loop through the required IssueLinkTypes
        for (final String oldId : issueLinkTypeMapper.getRequiredOldIds())
        {
            // Check if this has been mapped
            final String newId = issueLinkTypeMapper.getMappedId(oldId);
            if (newId == null)
            {
                // Not Mapped - lets find out why so we can add an appropriate message.
                final String issueLinkTypeName = issueLinkTypeMapper.getKey(oldId);
                final IssueLinkType issueLinkType = getIssueLinkTypeByName(issueLinkTypeName);
                if (issueLinkType == null)
                {
                    messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issuelinktype.type.does.not.exist", issueLinkTypeName));
                    messageSet.addErrorMessageInEnglish("The Issue Link Type '" + issueLinkTypeName + "' is required for the import but does not exist in the current JIRA instance.");
                }
                else
                {
                    validateStyle(oldId, issueLinkType, issueLinkTypeMapper, messageSet, i18nHelper);
                    // This SHOULD have added an error message as to WHY we aren't mapped.
                    // If there is none, then fail validation on "required link type is not mapped."
                    if (!messageSet.hasAnyErrors())
                    {
                        messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issuelinktype.no.mapping", issueLinkTypeName));
                        messageSet.addErrorMessageInEnglish("The Issue Link Type '" + issueLinkTypeName + "' is required for the import but has not been mapped.");
                    }
                }
            }
            else
            {
                // Mapped - check the mapping is valid.
                final IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(new Long(newId));
                if (issueLinkType == null)
                {
                    final String issueLinkTypeName = issueLinkTypeMapper.getKey(oldId);
                    messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issuelinktype.mapped.type.does.not.exist",
                            issueLinkTypeName, newId));
                    messageSet.addErrorMessageInEnglish("The Issue Link Type '" + issueLinkTypeName + "' was mapped to an IssuelinkType ID (" + newId + ") that does not exist.");
                }
                else
                {
                    validateStyle(oldId, issueLinkType, issueLinkTypeMapper, messageSet, i18nHelper);
                }
            }
        }
        return messageSet;
    }

    private boolean isIssueLinkingEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING);
    }

    private void validateStyle(final String oldId, final IssueLinkType issueLinkType, final IssueLinkTypeMapper issueLinkTypeMapper, final MessageSet messageSet, final I18nHelper i18nHelper)
    {
        final String oldStyle = issueLinkTypeMapper.getStyle(oldId);
        final String newStyle = issueLinkType.getStyle();
        final String issueLinkTypeName = issueLinkTypeMapper.getKey(oldId);
        if (oldStyle == null)
        {
            if (newStyle != null)
            {
                messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issuelinktype.style.in.new.type.only", issueLinkTypeName,
                    newStyle));
                messageSet.addErrorMessageInEnglish("The Issue Link Type '" + issueLinkTypeName + "' has no style value in the backup, but has style '" + newStyle + "' in the current system.");
            }
        }
        else
        {
            // The old style is non-null.
            // This is presumably because the link type is for subtasks. This is the only use of style at time of writing.
            if (oldStyle.equals(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE))
            {
                // check that subtasks are enabled.
                if (!subTaskManager.isSubTasksEnabled())
                {
                    messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issuelinktype.subtasks.disabled"));
                    messageSet.addErrorMessageInEnglish("The project to import includes subtasks, but subtasks are disabled in the current system.");
                }
            }

            if (newStyle == null)
            {
                messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issuelinktype.style.in.old.type.only", issueLinkTypeName,
                    oldStyle));
                messageSet.addErrorMessageInEnglish("The Issue Link Type '" + issueLinkTypeName + "' has style '" + oldStyle + "' in the backup, but has no style in the current system.");
            }
            else if (!oldStyle.equals(newStyle))
            {
                messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.issuelinktype.different.styles", issueLinkTypeName,
                    oldStyle, newStyle));
                messageSet.addErrorMessageInEnglish("The Issue Link Type '" + issueLinkTypeName + "' has style '" + oldStyle + "' in the backup, but has style '" + newStyle + "' in the current system.");
            }
        }
    }

    private IssueLinkType getIssueLinkTypeByName(final String linkTypeName)
    {
        final Collection linkTypes = issueLinkTypeManager.getIssueLinkTypesByName(linkTypeName);
        if (linkTypes.isEmpty())
        {
            return null;
        }
        // Return the first one - there should never be more than one.
        return (IssueLinkType) linkTypes.iterator().next();
    }
}
