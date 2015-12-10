package com.atlassian.jira.plugin.report.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;

import static com.atlassian.jira.plugin.report.SubTaskInclusionOption.ASSIGNED_AND_UNASSIGNED;
import static com.atlassian.jira.plugin.report.SubTaskInclusionOption.ONLY_ASSIGNED;

/**
 * Generator for sub-task inclusion select box options.
 *
 * @since v6.3
 */
public class UserSubTaskIncludeValuesGenerator implements ValuesGenerator<String>
{
    private final I18nHelper.BeanFactory i18nHelperFactory;
    public UserSubTaskIncludeValuesGenerator(final JiraAuthenticationContext authContext, final I18nHelper.BeanFactory i18nHelperFactory)
    {
        this.i18nHelperFactory = i18nHelperFactory;
    }

    /**
     * Returns a new map of applicable options. If sub-tasks are disabled returns a single option that says that this
     * select box is not relevant.
     *
     * @param userParams map of user parameters
     * @return new map of applicable options
     */
    public Map<String, String> getValues(Map userParams)
    {
        User u = (User) userParams.get("User");
        I18nHelper i18nHelper = i18nHelperFactory.getInstance(u);

        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put(ONLY_ASSIGNED.getKey(), ONLY_ASSIGNED.getDescription(i18nHelper));
        result.put(ASSIGNED_AND_UNASSIGNED.getKey(), ASSIGNED_AND_UNASSIGNED.getDescription(i18nHelper));
        return result;
    }
}
