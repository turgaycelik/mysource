package com.atlassian.jira.pageobjects.pages.admin.subtask;

import com.atlassian.jira.pageobjects.components.IconPicker;

import java.util.Map;

/**
 * An interface for actions that can add subtask type dialog.
 *
 * @since v5.0.1
 */
public interface AddSubtaskType
{
    AddSubtaskType setName(String name);
    AddSubtaskType setDescription(String description);

    /**
     * @deprecated there is no such functionality in addsubtask type dialog
     */
    @Deprecated
    AddSubtaskType setIconUrl(String iconUrl);
    /**
     * @deprecated there is no such functionality in addsubtask type dialog
     */
    @Deprecated
    String getIconUrl();
    /**
     * @deprecated there is no such functionality in addsubtask type dialog
     */
    @Deprecated
    IconPicker.IconPickerPopup openIconPickerPopup();
    ManageSubtasksPage submitSuccess();
    AddSubtaskType submitFail();
    <P> P submitFail(Class<P> page, Object... args);
    <P> P submit(Class<P> klazz);
    Map<String, String> getErrors();
    ManageSubtasksPage cancel();
}
