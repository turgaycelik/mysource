package com.atlassian.jira.notification.type;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.util.GroupSelectorUtils;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.transform;

/**
 * Group Custom Field notification type. Configured with a custom field, it interprets the custom field's value as a group
 * name, and then notifies members of that group.
 *
 * @since 3.6
 */
public class GroupCFValue extends AbstractNotificationType
{
    private static final Logger log = Logger.getLogger(GroupCFValue.class);

    public static final String ID = "Group_Custom_Field_Value";

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final GroupSelectorUtils groupSelectorUtils;
    private final FieldManager fieldManager;


    public GroupCFValue(JiraAuthenticationContext jiraAuthenticationContext, GroupSelectorUtils groupSelectorUtils,
            FieldManager fieldManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.groupSelectorUtils = groupSelectorUtils;
        this.fieldManager = fieldManager;
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String customFieldId)
    {
        List<ApplicationUser> users = fromEventParams(event, customFieldId);
        if (users == null)
        {
            users = fromIssue(event, customFieldId);
        }
        return transform(users, ApplicationUserToRecipient.INSTANCE);
    }

    @SuppressWarnings ( { "unchecked" })
    private List<ApplicationUser> fromEventParams(IssueEvent event, String customFieldId)
    {
        Map<String,Object> customFieldValues = (Map<String,Object>) event.getParams().get(IssueEvent.CUSTOM_FIELDS_PARAM_NAME);
        if (customFieldValues != null)
        {
            final Object rawValue = customFieldValues.get(customFieldId);
            if (rawValue != null)
            {
                return ApplicationUsers.from(groupSelectorUtils.getUsers(rawValue));
            }
        }
        return null;
    }

    @SuppressWarnings ( { "unchecked" })
    private List<ApplicationUser> fromIssue(IssueEvent event, String customFieldId)
    {
        try
        {
            return ApplicationUsers.from(groupSelectorUtils.getUsers(event.getIssue(), customFieldId));
        }
        catch (IllegalArgumentException e)
        {
            //JRA-14392: This shouldn't happen any longer, however there may still be some old invalid data that could
            //cause this.
            log.warn("Returning empty list of e-mail recipients. Please remove any invalid custom "
                    + "fields from your notification schemes.", e);
            return Collections.emptyList();
        }

    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.group.custom.field.value");
    }

    public String getType()
    {
        return "groupCF";
    }

    /**
     * Used in the UI layer.
     *
     * @return list of group-related custom fields
     */
    public List getFields()
    {
        return groupSelectorUtils.getCustomFieldsSpecifyingGroups();
    }

    public boolean doValidation(String key, Map parameters)
    {
        String value = (String) parameters.get(key);
        if (value == null || value.length() == 0)
            return false;

        try
        {
            fieldManager.getCustomField(value);
        }
        catch (IllegalArgumentException ex)
        {
            return false;
        }
        return true;
    }

    public String getArgumentDisplay(String argument)
    {
        CustomField field = fieldManager.getCustomField(argument);
        return field.getName();
    }
}
