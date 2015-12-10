package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;

/**
 * The renderer for the Affected Version searcher.
 *
 * @since v5.2
 */
public class FixForVersionRenderer extends AbstractVersionRenderer
{
    public FixForVersionRenderer(ProjectManager projectManager, VersionManager versionManager,
            FieldVisibilityManager fieldVisibilityManager,
            VelocityRequestContextFactory velocityRequestContextFactory, ApplicationProperties applicationProperties,
            VelocityTemplatingEngine templatingEngine, PermissionManager permissionManager, String searcherNameKey)
    {
        super(SystemSearchConstants.forFixForVersion(), searcherNameKey, projectManager, versionManager,
                velocityRequestContextFactory, applicationProperties, templatingEngine, fieldVisibilityManager, permissionManager, true);
    }
}
