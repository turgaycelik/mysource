package com.atlassian.jira.issue.fields.util;

import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.permission.ProjectPermissions.ADMINISTER_PROJECTS;
import static com.atlassian.jira.user.ApplicationUsers.toDirectoryUser;

public class VersionHelperBean
{
    public static final Long UNKNOWN_VERSION_ID = new Long(-1);
    public static final Long UNRELEASED_VERSION_ID = new Long(-2);
    public static final Long RELEASED_VERSION_ID = new Long(-3);

    private VersionManager versionManager;
    private final PermissionManager permissionManager;

    /**
     * @deprecated since 6.3.8. Use {@link com.atlassian.jira.issue.fields.LongIdsValueHolder#NEW_VALUE_PREFIX} instead.
     */
    @Deprecated
    public static final String NEW_VERSION_RREFIX = "nv_";

    /**
     * @deprecated since 6.3.  Use {@link #VersionHelperBean(com.atlassian.jira.project.version.VersionManager,
     * com.atlassian.jira.security.PermissionManager)} instead.
     */
    @Deprecated
    public VersionHelperBean(VersionManager versionManager)
    {
        this.versionManager = versionManager;
        this.permissionManager = ComponentAccessor.getPermissionManager();
    }

    public VersionHelperBean(VersionManager versionManager, PermissionManager permissionManager)
    {
        this.versionManager = versionManager;
        this.permissionManager = permissionManager;
    }

    public boolean validateVersionIds(Collection versionIds, ErrorCollection errorCollection, I18nHelper i18n, String fieldId)
    {
        boolean valid = true;
        if (versionIds != null)
        {
            if (versionIds.size() > 1)
            {
                if (versionIds.contains(UNKNOWN_VERSION_ID))
                {
                    errorCollection.addError(fieldId, i18n.getText("issue.field.versions.noneselectedwithother"), Reason.VALIDATION_FAILED);
                    valid = false;
                }
            }

            for (final Object o : versionIds)
            {
                final Long l = getVersionIdAsLong(o);

                // TODO: Should this check for (l < 0). See comment in validateVersionForProject().
                if (l < -1)
                {
                    errorCollection.addError(fieldId, i18n.getText("issue.field.versions.releasedunreleasedselected"), Reason.VALIDATION_FAILED);
                    valid = false;
                }
            }
        }
        return valid;
    }

    public void validateVersionForProject(final Collection versionIds, final Project project, final ErrorCollection errorCollection, final I18nHelper i18n, String fieldId)
    {
        if (versionIds != null && project != null)
        {
            final Long projectId = project.getId();
            StringBuilder sb = null;
            for (Object versionId : versionIds)
            {
                final Long id = getVersionIdAsLong(versionId);
                if (id == -1)
                {
                    // Unknown should ahve been validated earlier
                    // TODO: It looks like validateVersionIds() checks for < -1. Is this incorrect?
                    return;
                }
                final Version version = versionManager.getVersion(id);
                if (version == null)
                {
                    errorCollection.addError(fieldId, i18n.getText("issue.field.versions.invalid.version.id", id), Reason.VALIDATION_FAILED);
                    return;
                }
                final Long versionProjectId = version.getProjectObject().getId();

                // JRA-20184: Only check on the ProjectID
                if (!versionProjectId.equals(projectId))
                {
                    if (sb == null)
                    {
                        sb = new StringBuilder(version.getName()).append("(").append(version.getId()).append(")");
                    }
                    else
                    {
                        sb.append(", ").append(version.getName()).append("(").append(version.getId()).append(")");
                    }
                }
            }
            if (sb != null)
            {
                errorCollection.addError(fieldId, i18n.getText("issue.field.versions.versions.not.valid.for.project", sb.toString(), project.getName()), Reason.VALIDATION_FAILED);
            }
        }
    }

    public void validateVersionsToCreate(final ApplicationUser user, final I18nHelper i18n, final Project project, final String fieldId, final Set<String> newVersionNames, final ErrorCollection errorCollection)
    {
        if (permissionManager.hasPermission(ADMINISTER_PROJECTS, project, user))
        {
            final VersionService versionService = getVersionService();
            //looks like some yet unknown versions were entered. Need to check if the user is a project admin for this project and
            //validate that we can create this version!
            for (String version : newVersionNames)
            {
                final VersionService.VersionBuilder builder = versionService.newBuilder()
                        .projectId(project.getId())
                        .name(version);
                final VersionService.VersionBuilderValidationResult result = versionService.validateCreate(toDirectoryUser(user), builder);
                if (!result.isValid())
                {
                    final ErrorCollection errors = result.getErrorCollection();
                    for (String errorMsg : errors.getErrorMessages())
                    {
                        errorCollection.addError(fieldId, errorMsg, ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                    errorCollection.addError(fieldId, errors.getErrors().get("name"), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        else
        {
            final String bad = StringUtils.join(newVersionNames, "");
            if (!StringUtils.isEmpty(bad))
            {
                errorCollection.addError(fieldId, i18n.getText("issue.field.versions.invalid.version.id", bad), ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
    }

    /**
     * Helper to create new versions when project admins add new versions via a create or edit operation!
     */
    public List<Version> createNewVersions(final Long projectId, final Set<String> versionsToAdd)
    {
        final List<Version> newVersions = Lists.newArrayList();
        for (String version : versionsToAdd)
        {
            newVersions.add(createOrGetVersion(projectId, version));
        }
        return newVersions;
    }

    //SW-774: Last ditch effort to see if an existing version exists with this name. If it does just use it.  This can
    //        happen if a user sets both affects and fix version to the same new version in one transaction.
    private Version createOrGetVersion(final Long projectId, final String versionName)
    {
        final Version existingVersion = versionManager.getVersion(projectId, versionName);
        if (existingVersion == null)
        {
            try
            {
                return versionManager.createVersion(versionName, null, null, projectId, null);
            }
            catch (CreateException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            return existingVersion;
        }
    }

    private Long getVersionIdAsLong(Object o)
    {
        Long l;
        if (o instanceof String)
        {
            l = new Long((String) o);
        }
        else
        {
            l = (Long) o;
        }
        return l;
    }

    @VisibleForTesting
    VersionService getVersionService()
    {
        //can't be injected due to circular dependency :(.
        return ComponentAccessor.getComponentOfType(VersionService.class);
    }

}
