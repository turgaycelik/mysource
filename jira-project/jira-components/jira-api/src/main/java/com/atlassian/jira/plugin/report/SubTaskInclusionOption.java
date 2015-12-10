package com.atlassian.jira.plugin.report;

import javax.annotation.Nonnull;

import com.atlassian.jira.util.I18nHelper;

/**
 * The available options for subtask inclusion when configuring a JIRA project.
 *
 * @since 6.3
 */
public enum SubTaskInclusionOption
{
    ONLY_ASSIGNED("onlyAssigned", "report.subtasks.user.include.selected.only"),
    ASSIGNED_AND_UNASSIGNED("assignedAndUnassigned", "report.subtasks.user.include.selected.and.unassigned"),
    ONLY_SELECTED_VERSION("onlySelected", "report.subtasks.include.selected.only"),
    SELECTED_AND_BLANK_VERSIONS("selectedAndBlank", "report.subtasks.include.selected.none"),
    ALL("all", "report.subtasks.include.all");

    private final String key;
    private final String descriptionI18nKey;

    private SubTaskInclusionOption(String key, String descriptionI18nKey)
    {
        this.key = key;
        this.descriptionI18nKey = descriptionI18nKey;
    }

    public String getKey()
    {
        return key;
    }

    public String getDescription(I18nHelper i18nHelper)
    {
        return i18nHelper.getText(descriptionI18nKey);
    }

    public static SubTaskInclusionOption fromKey(@Nonnull String key)
    {
        for (SubTaskInclusionOption options : SubTaskInclusionOption.values())
        {
            if (options.getKey().equals(key))
            {
                return options;
            }
        }
        return null;
    }

    public static boolean isValidKey (String key) {
        if (key != null) {
            for (SubTaskInclusionOption option : SubTaskInclusionOption.values())
            {
                if (key.equals(option.getKey()))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
