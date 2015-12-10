package com.atlassian.jira.plugin.report.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.I18nHelper;

import static com.atlassian.jira.plugin.report.SubTaskInclusionOption.ALL;
import static com.atlassian.jira.plugin.report.SubTaskInclusionOption.ONLY_SELECTED_VERSION;
import static com.atlassian.jira.plugin.report.SubTaskInclusionOption.SELECTED_AND_BLANK_VERSIONS;

/**
 * Generator for sub-task inclusion select box options.
 *
 * @since v6.3
 */
public class SubTaskIncludeValuesGenerator implements ValuesGenerator <String>
{
    private final I18nHelper.BeanFactory i18nHelperFactory;

    public SubTaskIncludeValuesGenerator(final I18nHelper.BeanFactory i18nHelperFactory)
    {
        this.i18nHelperFactory = i18nHelperFactory;
    }

    /**
     * Returns a new map of applicable options. If sub-tasks are disabled returns a single option that says that this
     * select box is not relevant.
     *
     * @param userParams map of user parameters
     *
     * @return new map of applicable options
     */
    public Map<String, String> getValues(Map userParams)
    {
        User u = (User) userParams.get("User");
        I18nHelper i18nHelper = i18nHelperFactory.getInstance(u);

        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put(ONLY_SELECTED_VERSION.getKey(), ONLY_SELECTED_VERSION.getDescription(i18nHelper));
        result.put(SELECTED_AND_BLANK_VERSIONS.getKey(), SELECTED_AND_BLANK_VERSIONS.getDescription(i18nHelper));
        result.put(ALL.getKey(), ALL.getDescription(i18nHelper));
        return result;
    }
}
