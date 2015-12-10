package com.atlassian.jira.plugin.contentlinks.conditions;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Checks whether the &quot;key&quot; context parameter is the project key of an existing project in JIRA.
 *
 * @since v5.2
 */
public class IsKeyDefinedInAnExistingProject implements Condition
{
    private final ProjectManager projectManager;

    public IsKeyDefinedInAnExistingProject(final ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    @Override
    public void init(Map<String, String> params) throws PluginParseException {}

    /**
     * Determines whether the &quot;key&quot; context parameter is the project key of an existing project in JIRA.
     *
     * @param context The context where the &quot;key&quot; parameter will be looked up.
     * @return {@code true}, if the &quot;key&quot; parameter is the project key of an existing project. Otherwise,
     *         {@code false}.
     */
    @Override
    public boolean shouldDisplay(final Map<String, Object> context)
    {
        final String projectKey = (String) context.get("key");
        return !isNullOrEmpty(projectKey) && projectManager.getProjectObjByKey(projectKey) != null;
    }
}
