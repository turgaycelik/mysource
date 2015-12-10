package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.imports.project.mapper.UserMapper;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import org.apache.log4j.Logger;

/**
 * @since v3.13
 */
public class UserMapperValidatorImpl implements UserMapperValidator
{
    private static final Logger log = Logger.getLogger(UserMapperValidatorImpl.class);

    private final UserManager userManager;

    public UserMapperValidatorImpl(final UserManager userManager)
    {
        this.userManager = userManager;
    }

    public MessageSet validateMappings(final I18nHelper i18nHelper, final UserMapper userMapper)
    {
        final MessageSet messageSet = new MessageSetImpl();

        if (isExternalUserManagementEnabled())
        {
            final int missingMandatoryUserCount = userMapper.getUnmappedMandatoryUsers().size();
            // Unmapped Mandatory users will show an error and the import cannot continue.
            if (missingMandatoryUserCount > 0)
            {
                final MessageSet.MessageLink link = new MessageSet.MessageLink(i18nHelper.getText("common.concepts.view.details"),
                    "/secure/admin/ProjectImportMissingMandatoryUsersExtMgmt.jspa");
                messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.user.validation.missing.users.ext.mgnt.error",
                    String.valueOf(missingMandatoryUserCount)), link);
                messageSet.addErrorMessageInEnglish("There are '" + missingMandatoryUserCount + "' required user(s) that are missing from the current system. External user management is enabled so the import is unable to create the user(s). You must add the user(s) to the system before the import can proceed.");
            }
            final int missingUsersInUseCount = userMapper.getUnmappedUsersInUse().size();

            // Unmapped "optional" users will show a warning on the summary page, with a link to list of users.
            if (missingUsersInUseCount > 0)
            {
                final MessageSet.MessageLink link = new MessageSet.MessageLink(i18nHelper.getText("common.concepts.view.details"),
                    "/secure/admin/ProjectImportMissingOptionalUsersExtMgmt.jspa");
                messageSet.addWarningMessage(i18nHelper.getText("admin.errors.project.import.user.validation.missing.users.ext.mgnt.warning",
                    String.valueOf(missingUsersInUseCount)), link);
                messageSet.addWarningMessageInEnglish("There are '" + missingUsersInUseCount + "' user(s) referenced that are in use in the project and missing from the current system. External user management is enabled so the import is unable to create the user(s). You may want to add the user(s) to the system before performing the import but the import can proceed without them.");
            }
        }
        else
        {
            // External User Management is off.
            final int unmappedMandatoryUsersNoOldValue = userMapper.getUnmappedMandatoryUsersWithNoRegisteredOldValue().size();
            if (unmappedMandatoryUsersNoOldValue > 0)
            {
                // There are unmapped mandatory users that we cannot automatically create as we don't have the user details.
                final MessageSet.MessageLink link = new MessageSet.MessageLink(i18nHelper.getText("common.concepts.view.details"),
                    "/secure/admin/ProjectImportMissingMandatoryUsersCannotCreate.jspa");
                messageSet.addErrorMessage(i18nHelper.getText("admin.errors.project.import.user.validation.missing.required.users.error",
                    String.valueOf(unmappedMandatoryUsersNoOldValue)), link);
                messageSet.addErrorMessageInEnglish("There are '" + unmappedMandatoryUsersNoOldValue + "' required user(s) that JIRA can not automatically create.");
            }
            final int unmappedInUseUsersNoOldValue = userMapper.getUnmappedUsersInUseWithNoRegisteredOldValue().size();
            if (unmappedInUseUsersNoOldValue > 0)
            {
                // There are unmapped optional users that we cannot automatically create as we don't have the user details.
                final MessageSet.MessageLink link = new MessageSet.MessageLink(i18nHelper.getText("common.concepts.view.details"),
                    "/secure/admin/ProjectImportMissingOptionalUsersCannotCreate.jspa");
                messageSet.addWarningMessage(i18nHelper.getText("admin.errors.project.import.user.validation.missing.optional.users.warning",
                    String.valueOf(unmappedInUseUsersNoOldValue)), link);
                messageSet.addWarningMessageInEnglish("There are '" + unmappedInUseUsersNoOldValue + "' user(s) referenced that JIRA can not automatically create. You may want to create these users before performing the import.");
            }
            // Count the total number of users that we CAN automatically create.
            final int usersToAutoCreate = userMapper.getUsersToAutoCreate().size();
            if (usersToAutoCreate > 0)
            {
                // There are unmapped optional users that we cannot automatically create as we don't have the user details.
                final MessageSet.MessageLink link = new MessageSet.MessageLink(i18nHelper.getText("common.concepts.view.details"),
                    "/secure/admin/ProjectImportMissingUsersAutoCreate.jspa");
                messageSet.addWarningMessage(i18nHelper.getText("admin.errors.project.import.user.validation.missing.users.we.can.create",
                    String.valueOf(usersToAutoCreate)), link);
                messageSet.addWarningMessageInEnglish("There are '" + usersToAutoCreate + "' users that will be automatically created if the import continues.");
            }

        }

        return messageSet;
    }

    boolean isExternalUserManagementEnabled()
    {
        return !userManager.hasWritableDirectory();
    }

}
