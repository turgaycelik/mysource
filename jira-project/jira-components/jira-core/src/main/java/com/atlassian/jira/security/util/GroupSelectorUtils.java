package com.atlassian.jira.security.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.GroupSelectorField;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.util.profiling.UtilTimerStack;

import com.google.common.collect.ImmutableSet;

import org.apache.log4j.Logger;

/**
 * Utility class for mapping group custom field values to the corresponding groups and/or users.
 */
public class GroupSelectorUtils
{
    private static final Logger log = Logger.getLogger(GroupSelectorUtils.class);

    private final FieldManager fieldManager;
    private final UserUtil userUtil;
    private final GroupManager groupManager;

    public GroupSelectorUtils(final FieldManager fieldManager, final UserUtil userUtil, GroupManager groupManager)
    {
        this.fieldManager = fieldManager;
        this.userUtil = userUtil;
        this.groupManager = groupManager;
    }

    /**
     * Get users from a group chosen by a Group Selector custom field, in a certain issue.
     *
     * @param issue The current issue
     * @param customFieldId Id of {@link GroupSelectorField}.
     * @return Set of {@link User}s.
     */
    public Set<User> getUsers(Issue issue, String customFieldId)
    {
        UtilTimerStack.push("GroupCF.getUsers");
        if (issue == null)
        {
            return ImmutableSet.of();
        }
        final CustomField field = fieldManager.getCustomField(customFieldId);
        if (field == null)
        {
            throw new IllegalArgumentException("Group Selector permission configured with custom field " + customFieldId + ", but this field does not exist");
        }
        if (!(field.getCustomFieldType() instanceof GroupSelectorField))
        {
            throw new IllegalArgumentException("Group Selector permission configured with field " + customFieldId + ", but this is not a type that can select groups");
        }
        try
        {
            return getUsers(issue, field);
        }
        finally
        {
            UtilTimerStack.pop("GroupCF.getUsers");
        }
    }

    private Set<User> getUsers(final Issue issue, final CustomField field)
    {
        final Object groupCFValue = field.getValue(issue);
        final Set<Group> groups = getGroups(groupCFValue);
        if (log.isDebugEnabled())
        {
            if (groupCFValue == null)
            {
                log.debug("Issue " + issue + " does not have a value for field " + field);
            }
            else if (groups.isEmpty())
            {
                log.debug("No groups found for group selector value '" + groupCFValue + "' on issue " + issue + ". Perhaps that group no longer exists?");
            }
            else
            {
                log.debug("GroupCF returned users from groups " + Arrays.toString(groups.toArray()));
            }
        }
        return userUtil.getAllUsersInGroups(groups);
    }

    public Set<User> getUsers(Object groupCustomFieldRawValue)
    {
        if (groupCustomFieldRawValue == null)
        {
            return ImmutableSet.of();
        }
        return userUtil.getAllUsersInGroups(getGroups(groupCustomFieldRawValue));
    }

    /**
     * Get all custom fields that could possibly be identifying a group. For example, select-lists, text fields.
     *
     * @return list of Field objects, never null
     */
    public List<Field> getCustomFieldsSpecifyingGroups()
    {
        Set<NavigableField> fieldSet;
        try
        {
            fieldSet = fieldManager.getAllAvailableNavigableFields();
        }
        catch (FieldException e)
        {
            return Collections.emptyList();
        }

        List<Field> fields = new ArrayList<Field>(fieldSet.size() / 4);
        for (Field field : fieldSet)
        {
            if (fieldManager.isCustomField(field))
            {
                final CustomField customField = (CustomField)field;
                // Exclude field types that obviously don't specify a group
                if (customField.getCustomFieldType() instanceof GroupSelectorField)
                {
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    /**
     * Determines if a user is a member of a group specified by a custom field value.
     *
     * @param issue The current issue
     * @param field The custom field specifying the group(s). Eg. a select-list.
     * @param user  The user we wish to check for
     * @return If user is in one of the groups specified by the custom field.
     */
    public boolean isUserInCustomFieldGroup(Issue issue, CustomField field, User user)
    {
        Object cfValue = issue.getCustomFieldValue(field);
        Collection<Group> groups = getGroups(cfValue);
        for (Group group: groups)
        {
            if (groupManager.isUserInGroup(user, group))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Given an object (usually a custom field value) find the associated group.
     *
     * @param cfValue A {@link String} (eg. "JIRA Developers" or "jira-developers") {@link Group} or {@link Collection} of {@link String}s or {@link Group}s.
     * @return A Set of {@link Group}s.
     */
    private Set<Group> getGroups(Object cfValue)
    {
        if (cfValue == null)
        {
            return ImmutableSet.of();
        }
        if (cfValue instanceof Group)
        {
            return ImmutableSet.of((Group)cfValue);
        }
        if (cfValue instanceof String)
        {
            return getGroups((String)cfValue);
        }
        if (cfValue instanceof Option)
        {
            return getGroups(((Option)cfValue).getValue());
        }
        if (cfValue instanceof Collection<?>)
        {
            final Collection<?> groupList = (Collection<?>)cfValue;
            final Set<Group> groups = new HashSet<Group>(groupList.size());
            for (Object groupValue : groupList)
            {
                groups.addAll(getGroups(groupValue));
            }
            return groups;
        }
        log.error("Object '" + cfValue + "' is of type " + cfValue.getClass().getName() + " which cannot be converted to a Group. Needs to be a Group object or a String representing group name.");
        return ImmutableSet.of();
    }

    /**
     * Given a string representing a group, return the Group.
     *
     * @param groupStr eg. "JIRA Developers" or "jira-developers".
     * @return A Set of {@link Group}s.
     */
    private Set<Group> getGroups(String groupStr)
    {
        final Group group = groupManager.getGroup(groupStr);
        if (group != null)
        {
            return ImmutableSet.of(group);
        }
        return ImmutableSet.of();
    }
}
