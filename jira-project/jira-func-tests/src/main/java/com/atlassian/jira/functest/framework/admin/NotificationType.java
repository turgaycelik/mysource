package com.atlassian.jira.functest.framework.admin;

/**
 * Represents notification type in the 'Edit notifications' admin section.
 *
 * @since 4.4
 */
public enum NotificationType
{
    CURRENT_ASSIGNEE("Current_Assignee"),
    REPORTER("Current_Reporter"),
    CURRENT_USER("Remote_User"),
    PROJECT_LEAD("Project_Lead"),
    COMPONENT_LEAD("Component_Lead"),
    SINGLE_USER("Single_User"),
    GROUP("Group_Dropdown"),
    PROJECT_ROLE("Project_Role"),
    SINGLE_EMAIL_ADDRESS("Single_Email_Address"),
    ALL_WATCHERS("All_Watchers"),
    USER_CUSTOM_FIELD_VALUE("User_Custom_Field_Value"),
    GROUP_CUSTOM_FIELD_VALUE("Group_Custom_Field_Value");


    private final String uiCode;

    private NotificationType(String uiCode)
    {
        this.uiCode = uiCode;
    }

    public String uiCode()
    {
        return uiCode;
    }
}
