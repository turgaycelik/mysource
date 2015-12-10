package com.atlassian.jira.web.action.admin.issuetypes;

import com.atlassian.jira.util.lang.Pair;

import java.util.List;

/**
 * Interface implemented by actions that wish to use the "addissuetype.jsp" form.
 *
 * @since v5.0.1
 */
public interface AddIssueTypeAction
{
    /**
     * Get the Icon URL for the issue type.
     *
     * @return the icon URL for the issue type.
     */
    @Deprecated
    String getIconurl();

    /**
     * Set the Icon URL for the issue type.
     *
     * @param iconUrl the icon URL for the issue type.
     */
    @Deprecated
    void setIconurl(String iconUrl);

    /**
     * Get the style (i.e. subtask) of the issue type.
     *
     * @return the style of the issue type.
     */
    String getStyle();

    /**
     * Set the style (i.e. subtask) of the issue type.
     *
     * @param style the style (i.e. subtask) of the issue type.
     */
    void setStyle(String style);

    /**
     * Return the URL used for form submission.
     *
     * @return the URL for form submission.
     */
    String getSubmitUrl();

    /**
     * The URL used for cancelling the form.
     *
     * @return the URL for cancel URL.
     */
    String getCancelUrl();

    /**
     * Get the name of the issue type.
     *
     * @return the name of the issue type.
     */
    String getName();

    /**
     * Set the name of the issue type.
     *
     * @param name the name of the issue type.
     */
    void setName(String name);

    /**
     * Get the description of the issue type.
     *
     * @return the description of the issue type.
     */
    String getDescription();

    /**
     * Set the description of the issue type.
     *
     * @param description the description of the issue type.
     */
    void setDescription(String description);
    
    /**
     * Return a list of {name, value} pairs to be added to the form as hidden fields.
     *
     * @return the list of {name, value} pairs.
     */
    List<Pair<String, Object>> getHiddenFields();

    ManageableOptionType getManageableOption();
}
