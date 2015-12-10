/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.option;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.List;

public class AssigneeOption implements Option, SelectChild
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private String optionName;
    private String displayName;
    private String avatarURL;
    private String emailAddress;

    /*
        All options except for option groups are enabled.
     */
    private boolean optionEnabled = true;
    private boolean selected;

    /**
     * True if the assignee User for this option represents the currently logged-in User.
     */
    private boolean isLoggedInUser;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors

    /**
     * @deprecated Use {@link #AssigneeOption(String, String, String, String)} instead. Since v5.0.
     */
    public AssigneeOption(String optionName, String displayName, boolean optionEnabled)
    {
        this.emailAddress = null;
        setOptionName(optionName);
        setDisplayName(displayName);
        avatarURL = null;
        this.optionEnabled = optionEnabled;
    }

    public AssigneeOption(String optionName, String displayName, String emailAddress, String avatarURL)
    {
        setOptionName(optionName);
        setDisplayName(displayName);
        this.emailAddress = emailAddress;
        this.avatarURL = avatarURL;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public String getId()
    {
        return null;
    }

    public String getName()
    {
        return null;
    }

    public String getDescription()
    {
        return null;
    }

    public String getEmailAddress()
    {
        return emailAddress;
    }

    public String getAvatarURL()
    {
        return avatarURL;
    }

    public String getImagePath()
    {
        return null;
    }

    public String getImagePathHtml()
    {
        return StringEscapeUtils.escapeHtml(getImagePath());
    }

    public String getCssClass()
    {
        return null;
    }

    public List getChildOptions()
    {
        return null;
    }

    public String getOptionName()
    {
        return optionName;
    }

    public void setOptionName(String optionName)
    {
        this.optionName = optionName;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public boolean isOptionEnabled()
    {
        return optionEnabled;
    }

    public void setOptionEnabled(boolean optionEnabled)
    {
        this.optionEnabled = optionEnabled;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    public boolean isLoggedInUser()
    {
        return isLoggedInUser;
    }

    public void setLoggedInUser(boolean isLoggedInUser)
    {
        this.isLoggedInUser = isLoggedInUser;
    }

    public boolean isOptionGroup()
    {
        return false;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    // -------------------------------------------------------------------------------------------------- Helper Methods


    @Override
    public String toString()
    {
        return getDisplayName() + ", " + getOptionName() + ", " + isOptionEnabled();
    }
}
