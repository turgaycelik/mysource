package com.atlassian.jira.pageobjects.pages.admin.issuetype;

import com.atlassian.jira.pageobjects.components.IconPicker;

import java.util.Map;

/**
 * Interface for a page object to add an issue type.
 *
 * @since v5.0.1
 */
public interface AddIssueType
{
    AddIssueType setName(String name);
    AddIssueType setDescription(String description);

    /**
     * @deprecated this feature no longer exists
     */
    @Deprecated
    AddIssueType setIconUrl(String iconUrl);
    AddIssueType setSubtask(boolean subtask);
    /**
     * @deprecated this functionality no longer exist
     */
    @Deprecated
    String getIconUrl();
    boolean isSubtasksEnabled();

    /**
     * @deprecated this functionality no longer exist
     */
    @Deprecated
    IconPicker.IconPickerPopup openIconPickerPopup();
    public Map<String, String> getFormErrors();
    <P> P submit(Class<P> klazz);
    <P> P cancel(Class<P> page);
    AddIssueType submitFail();
    <P> P submitFail(Class<P> page, Object... args);
}
