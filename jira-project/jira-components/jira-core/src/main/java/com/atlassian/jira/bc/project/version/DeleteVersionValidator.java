package com.atlassian.jira.bc.project.version;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Null;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

/**
 * Handles the validation for deleting versions
 *
 * @since v3.13
 */
class DeleteVersionValidator
{
    private static final Logger log = Logger.getLogger(DeleteVersionValidator.class);

    private final JiraServiceContext context;
    private final VersionManager versionManager;
    private final PermissionManager permissionManager;

    DeleteVersionValidator(final JiraServiceContext context, final VersionManager versionManager, final PermissionManager permissionManager)
    {
        Null.not("context", context);
        Null.not("versionManager", versionManager);
        Null.not("permissionManager", permissionManager);
        this.context = context;
        this.versionManager = versionManager;
        this.permissionManager = permissionManager;
    }

    VersionService.ValidationResult validate(final Long versionId, final VersionService.VersionAction affectsAction, final VersionService.VersionAction fixAction)
    {
        Null.not("affectsAction", affectsAction);
        Null.not("fixAction", fixAction);

        final Long affectsSwapVersionId = affectsAction.getSwapVersionId();
        final Long fixSwapVersionId = fixAction.getSwapVersionId();
        if (log.isDebugEnabled())
        {
            log.debug("Validating Version action - id to delete: " + versionId + ", affectsAction: " + affectsAction + ", fixAction: " + fixAction);
        }

        // ensure that the version to be deleted exists
        Version versionToDelete = validateAndGetVersion(versionId, "admin.manageversions.noversionspecified", "admin.manageversions.noversionwithid");
        if (versionToDelete == null)
        {
            log.debug("Version id: " + versionId + " was not valid");
            return new ValidationResultImpl(context.getErrorCollection(), versionToDelete, null, null, false, EnumSet.of(ValidationResultImpl.Reason.NOT_FOUND));
        }

        // check admin permissions for version before we go any further
        if (!checkProjectAdminPermission(versionToDelete.getProjectObject()))
        {
            if (log.isDebugEnabled())
            {
                log.debug("User '" + context.getLoggedInUser() + "' did not have permission to delete this version");
            }
            return new ValidationResultImpl(context.getErrorCollection(), versionToDelete, null, null, false, EnumSet.of(ValidationResultImpl.Reason.FORBIDDEN));
        }

        // if we are swapping for the Affects Version to another version, ensure that version exists and is not equal to the version being deleted
        Version affectsSwapVersion = null;
        if (affectsAction.isSwap())
        {
            affectsSwapVersion = validateAndGetVersion(affectsSwapVersionId, "admin.manageversions.noversionspecified.for.affects",
                    "admin.manageversions.noversionwithid.for.affects");
            if (affectsSwapVersion == null)
            {
                log.debug("Affects swap version id: " + affectsSwapVersionId + " was not valid");
                return new ValidationResultImpl(context.getErrorCollection(), versionToDelete, null, null, false, EnumSet.of(ValidationResultImpl.Reason.SWAP_TO_VERSION_INVALID));
            }

            if (versionId.equals(affectsSwapVersionId))
            {
                log.debug("Affects swap version id same as version to delete");
                addErrorMessage("admin.manageversions.cannot.swap.to.delete.version");
                return new ValidationResultImpl(context.getErrorCollection(), versionToDelete, affectsSwapVersion, null, false, EnumSet.of(ValidationResultImpl.Reason.SWAP_TO_VERSION_INVALID));
            }

            // need to ensure that the swap version is in the same project as the original version
            if (!versionToDelete.getProjectObject().getId().equals(affectsSwapVersion.getProjectObject().getId()))
            {
                log.debug("Affects swap version id not in same project as version to delete");
                addErrorMessage("admin.manageversions.cannot.move.to.different.project.version");
                return new ValidationResultImpl(context.getErrorCollection(), versionToDelete, affectsSwapVersion, null, false, EnumSet.of(ValidationResultImpl.Reason.SWAP_TO_VERSION_INVALID));
            }
        }

        // same for Fix Version...
        Version fixSwapVersion = null;
        if (fixAction.isSwap())
        {
            fixSwapVersion = validateAndGetVersion(fixSwapVersionId, "admin.manageversions.noversionspecified.for.fix",
                    "admin.manageversions.noversionwithid.for.fix");
            if (fixSwapVersion == null)
            {
                log.debug("Fix swap version id: " + fixSwapVersionId + " was not valid");
                return new ValidationResultImpl(context.getErrorCollection(), versionToDelete, affectsSwapVersion, null, false, EnumSet.of(ValidationResultImpl.Reason.SWAP_TO_VERSION_INVALID));
            }

            if (versionId.equals(fixSwapVersionId))
            {
                log.debug("Fix swap version id same as version to delete");
                addErrorMessage("admin.manageversions.cannot.swap.to.delete.version");
                return new ValidationResultImpl(context.getErrorCollection(), versionToDelete, affectsSwapVersion, fixSwapVersion, false, EnumSet.of(ValidationResultImpl.Reason.SWAP_TO_VERSION_INVALID));
            }

            // need to ensure that the swap version is in the same project as the original version
            if (!versionToDelete.getProjectObject().getId().equals(fixSwapVersion.getProjectObject().getId()))
            {
                log.debug("Fix swap version id not in same project as version to delete");
                addErrorMessage("admin.manageversions.cannot.move.to.different.project.version");
                return new ValidationResultImpl(context.getErrorCollection(), versionToDelete, affectsSwapVersion, fixSwapVersion, false, EnumSet.of(ValidationResultImpl.Reason.SWAP_TO_VERSION_INVALID));
            }
        }

        // everything is good!
        return new ValidationResultImpl(new SimpleErrorCollection(), versionToDelete, affectsSwapVersion, fixSwapVersion, true, Collections.<VersionService.ValidationResult.Reason>emptySet());
    }

    private Version validateAndGetVersion(final Long versionId, final String keyForNull, final String keyForBadId)
    {
        if (versionId == null)
        {
            addErrorMessage(keyForNull);
            return null;
        }

        final Version version = versionManager.getVersion(versionId);
        if (version == null)
        {
            addErrorMessage(keyForBadId, versionId.toString());
        }
        return version;
    }

    private boolean checkProjectAdminPermission(final Project project)
    {
        final ApplicationUser user = context.getLoggedInApplicationUser();
        final boolean hasProjectAdminPermission = permissionManager.hasPermission(Permissions.ADMINISTER, user) || permissionManager.hasPermission(
            Permissions.PROJECT_ADMIN, project, user);
        if (!hasProjectAdminPermission)
        {
            if (user != null)
            {
                addErrorMessage("admin.manageversions.usernopermission.withuser", user.getName());
            }
            else
            {
                addErrorMessage("admin.manageversions.usernopermission");
            }
        }

        return hasProjectAdminPermission;
    }

    private void addErrorMessage(final String key)
    {
        context.getErrorCollection().addErrorMessage(context.getI18nBean().getText(key));
    }

    private void addErrorMessage(final String key, final String value1)
    {
        context.getErrorCollection().addErrorMessage(context.getI18nBean().getText(key, value1));
    }
}
