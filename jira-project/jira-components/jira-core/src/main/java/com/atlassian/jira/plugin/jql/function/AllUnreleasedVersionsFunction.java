package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;

import java.util.Collection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Function that produces released versions for any specified projects (or all released versions if no project is
 * specified). The versions returned will include both archived and unarchived versions.
 * <p/>
 * Projects are resolved by project key first, then name, then id. Only Versions from Projects which the current user
 * can browse will be returned.
 *
 * @since v4.0
 */
public class AllUnreleasedVersionsFunction extends AbstractVersionsFunction
{
    public static final String FUNCTION_UNRELEASED_VERSIONS = "unreleasedVersions";
    private final VersionManager versionManager;

    public AllUnreleasedVersionsFunction(final VersionManager versionManager, final ProjectResolver projectResolver, final PermissionManager permissionManager)
    {
        super(projectResolver, permissionManager);
        this.versionManager = notNull("versionManager", versionManager);
    }

    protected Collection<Version> getAllVersions(User user)
    {
        return versionManager.getAllVersionsUnreleased(true);
    }

    protected Collection<Version> getVersionsForProject(final Long projectId)
    {
        return versionManager.getVersionsUnreleased(projectId, true);
    }
}