package com.atlassian.jira.plugin.projectoperation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;

import static java.lang.String.format;

/**
 * Class that represents a {@link PluggableProjectOperation} as something with a label and associated content.
 *
 * @since v4.4
 */
public abstract class DefaultPluggableProjectOperation extends AbstractPluggableProjectOperation
{
    private static final String TEMPLATE = "<span class=\"project-config-list-label\">%s</span><span class=\"project-config-list-value\">%s</span>";

    @Override
    final public String getHtml(Project project, User user)
    {
        return format(TEMPLATE, getLabelHtml(project, user), getContentHtml(project, user));
    }

    abstract protected String getLabelHtml(Project project, User user);
    abstract protected String getContentHtml(Project project, User user);
}
