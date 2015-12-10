package com.atlassian.jira.plugin.projectoperation;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;

/**
 * A simple interface to create project operation plugins.  These will be shown at the bottom of the
 * project operation screen.
 * <p/>
 * <b>IMPORTANT NOTE:</b>  This plugin type is only available for internal use.  Please refrain from using
 * this, as the backwards compatibility of this plugin type will NOT be maintained in the future.
 *
 * @since v3.12
 */
@PublicSpi
public interface PluggableProjectOperation
{
    void init(ProjectOperationModuleDescriptor descriptor);

    /**
     * Get the HTML to present on screen
     *
     * @param project Provide the project in case we need any project specific information to render the HTML
     * @param user The currently logged in user
     * @return HTML representation for this project operation.
     */
    String getHtml(Project project, User user);

    /**
     * Whether or not to show this operation for the given project and user.
     *
     * @param project Project being viewed
     * @param user The currently logged in user
     * @return true, if the link should be shown
     */
    boolean showOperation(Project project, User user);
}
