package com.atlassian.jira.notification.type;

import com.atlassian.annotations.PublicApi;

/**
 * A simple enumeration of the different JIRA notification types
 *
 * @since v6.0
 */
@PublicApi
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


    private final String dbCode;

    private NotificationType(String dbCode)
    {
        this.dbCode = dbCode;
    }

    public String dbCode()
    {
        return dbCode;
    }

    /**
     * Returns a NotificationType from the database code that JIRA uses internally.
     *
     * @param dbCode a magic string stored in the database tables
     * @return a notification type
     * @throws IllegalArgumentException if you give an invalid string
     */
    public static NotificationType from(String dbCode)
    {
        for (NotificationType notificationType : NotificationType.values())
        {
            if (notificationType.dbCode().equals(dbCode))
            {
                return notificationType;
            }
        }
        throw new IllegalArgumentException("You have to provide a valid mapped string");
    }
}
