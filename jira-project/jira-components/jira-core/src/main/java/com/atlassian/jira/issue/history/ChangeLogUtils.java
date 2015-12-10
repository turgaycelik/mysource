/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.history;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for managing ChangeGroup entities on issues, also known
 * as the Change Log or Change History.
 * <p>
 * TODO: Migrate methods to ChangeHistoryManager and deprecate
 */
public class ChangeLogUtils
{
    private static final Logger log = Logger.getLogger(ChangeLogUtils.class);

    /**
     * Deletes all the change groups and change items associated with the provided issue.
     * @param issue represents the issue that is being deleted
     *
     * @deprecated use {@link com.atlassian.jira.issue.changehistory.ChangeHistoryManager#removeAllChangeItems(com.atlassian.jira.issue.Issue)}
     * instead.
     */
    @Deprecated
    public static void deleteChangesForIssue(GenericValue issue)
    {
        OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();
        // get all changeGroups for the issue
        Map<String, ?> params = ImmutableMap.of("issue", issue.getLong("id"));
        List<GenericValue> changeGroups = delegator.findByAnd("ChangeGroup", params);
        for (GenericValue changeGroup : changeGroups)
        {
            // remove all changeItems associated with the changeGroup
            delegator.removeByAnd("ChangeItem", ImmutableMap.of("group", changeGroup.getLong("id")));
        }

        // remove all changeGroups at once
        delegator.removeByAnd("ChangeGroup", params);
    }

    /**
     * Writes the given field changes to the db and optionally a changelog.
     *
     * @param  before The issue before the update.
     * @param  after This issue after the update.
     * @param incomingChangeItems Some {@link ChangeItemBean}.
     * @param generateChangeItems if true, a changelog is actually generated.
     * @param changeAuthor the {@link ApplicationUser} making the change.
     * @return the ChangeGroup GenericValue
     */
    public static GenericValue createChangeGroup(ApplicationUser changeAuthor, GenericValue before, GenericValue after, Collection<ChangeItemBean> incomingChangeItems, boolean generateChangeItems)
    {
        OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();
        if (generateChangeItems && EntityUtils.identical(before, after) && (incomingChangeItems == null || incomingChangeItems.size() == 0))
            return null;

        GenericValue changeGroup = null;

        ArrayList<ChangeItemBean> changeItems = new ArrayList<ChangeItemBean>();

        if (generateChangeItems && !EntityUtils.identical(before, after))
            changeItems.addAll(generateChangeItems(before, after));

        if (incomingChangeItems != null)
            changeItems.addAll(incomingChangeItems);

        if (!changeItems.isEmpty())
        {
            MapBuilder<String, Object> builder = MapBuilder.<String, Object>newBuilder("issue", before.getLong("id"));
            builder.add("author", ApplicationUsers.getKeyFor(changeAuthor));
            builder.add("created", UtilDateTime.nowTimestamp());
            changeGroup = delegator.createValue("ChangeGroup", builder.toMap());

            for (ChangeItemBean cib : changeItems)
            {
                builder = MapBuilder.<String, Object>newBuilder("group", changeGroup.getLong("id"));
                builder.add("fieldtype", cib.getFieldType());
                builder.add("field", cib.getField());
                builder.add("oldvalue", cib.getFrom());
                builder.add("oldstring", cib.getFromString());
                builder.add("newvalue", cib.getTo());
                builder.add("newstring", cib.getToString());
                delegator.createValue("ChangeItem", builder.toMap());
            }
        }

        return changeGroup;
    }

    /**
     * Writes the given field changes to the db and optionally a changelog.
     *
     * @param  before The issue before the update.
     * @param  after This issue after the update.
     * @param incomingChangeItems Some {@link ChangeItemBean}.
     * @param generateChangeItems if true, a changelog is actually generated.
     * @param changeAuthor the {@link ApplicationUser} making the change.
     * @deprecated Use {@link #createChangeGroup(com.atlassian.jira.user.ApplicationUser, org.ofbiz.core.entity.GenericValue, org.ofbiz.core.entity.GenericValue, java.util.Collection, boolean)} instead. Since v6.0.
     * @return the ChangeGroup GenericValue
     */
    public static GenericValue createChangeGroup(final ApplicationUser changeAuthor, final Issue before, final Issue after, final Collection<ChangeItemBean> incomingChangeItems, final boolean generateChangeItems)
    {
        return createChangeGroup(changeAuthor, before.getGenericValue(), after.getGenericValue(), incomingChangeItems, generateChangeItems);
    }


    /**
     * Writes the given field changes to the db and optionally a changelog.
     *
     * @param  before The issue before the update.
     * @param  after This issue after the update.
     * @param incomingChangeItems Some {@link ChangeItemBean}.
     * @param generateChangeItems if true, a changelog is actually generated.
     * @param changeAuthor the User making the change.
     * @return the ChangeGroup GenericValue
     * @deprecated Use {@link #createChangeGroup(com.atlassian.jira.user.ApplicationUser, org.ofbiz.core.entity.GenericValue, org.ofbiz.core.entity.GenericValue, java.util.Collection, boolean)} instead. Since v6.0.
     */
    public static GenericValue createChangeGroup(User changeAuthor, GenericValue before, GenericValue after, Collection<ChangeItemBean> incomingChangeItems, boolean generateChangeItems)
    {
        return createChangeGroup(ApplicationUsers.from(changeAuthor), before, after, incomingChangeItems, generateChangeItems);
    }

    /**
     *
     * @param before The issue before the update.
     * @param after This issue after the update.
     * @param incomingChangeItems Some {@link ChangeItemBean}.
     * @param generateChangeItems if true, a changelog is actually generated.
     * @param changeAuthor the User making the change.
     * @return the ChangeGroup GenericValue
     * @deprecated Use {@link #createChangeGroup(com.atlassian.jira.user.ApplicationUser, org.ofbiz.core.entity.GenericValue, org.ofbiz.core.entity.GenericValue, java.util.Collection, boolean)} instead. Since v6.0.
     */
    public static GenericValue createChangeGroup(User changeAuthor, Issue before, Issue after, Collection<ChangeItemBean> incomingChangeItems, boolean generateChangeItems)
    {
        return createChangeGroup(changeAuthor, before.getGenericValue(), after.getGenericValue(), incomingChangeItems, generateChangeItems);
    }

    /**
     * Returns a List of ChangeItemBean objects for each of the relevant fields
     * that differ between the two issues.
     *
     * @param before A GenericValue for the issue before the change.
     * @param after A GenericValue for the issue after the change.
     * @return the list of ChangeItemBeans.
     */
    public static List<ChangeItemBean> generateChangeItems(GenericValue before, GenericValue after)
    {
        List<ChangeItemBean> changeItems = new ArrayList<ChangeItemBean>();

        for (String fieldname : before.getModelEntity().getAllFieldNames())
        {
            if (fieldname.equals("id") || fieldname.equals("created") || fieldname.equals("updated") || fieldname.equals("workflowId") || fieldname.equals("key") || fieldname.equals("project") || fieldname.equals("fixfor") || fieldname.equals("component") || fieldname.equals("votes"))
            { continue; }

            ChangeItemBean changeItem = generateChangeItem(before, after, fieldname);

            if (changeItem != null)
            {
                changeItems.add(changeItem);
            }
        }

        return changeItems;
    }

    public static ChangeItemBean generateChangeItem(GenericValue before, GenericValue after, String fieldname)
    {
        if (before.get(fieldname) == null && after.get(fieldname) == null)
            return null;

        if (before.get(fieldname) != null && after.get(fieldname) != null && before.get(fieldname).equals(after.get(fieldname)))
            return null;

        String from = null;
        String to = null;
        String fromString = null;
        String toString = null;

        if (fieldname.equals("assignee") || fieldname.equals("reporter"))
        {
            from = before.getString(fieldname);
            to = after.getString(fieldname);

            if (from != null)
            {
                ApplicationUser fromUser = getUserManager().getUserByKey(from);
                if (fromUser != null)
                {
                    fromString = fromUser.getDisplayName();
                }
                else
                {
                    log.warn("User: " + from + " not found - change item will be missing full name.");
                }
            }

            if (to != null)
            {
                ApplicationUser toUser = getUserManager().getUserByKey(to);
                if (toUser != null)
                {
                    toString = toUser.getDisplayName();
                }
                else
                {
                    log.warn("User: " + to + " not found - change item will be missing full name.");
                }
            }
        }
        else if (fieldname.equals("type"))
        {
            from = before.getString(fieldname);
            to = after.getString(fieldname);

            if (from != null)
                fromString = getConstantsManager().getIssueType(from).getString("name");

            if (to != null)
                toString = getConstantsManager().getIssueType(to).getString("name");
        }
        else if (fieldname.equals("resolution"))
        {
            from = before.getString(fieldname);
            to = after.getString(fieldname);

            if (from != null)
                fromString = getConstantsManager().getResolution(from).getString("name");

            if (to != null)
                toString = getConstantsManager().getResolution(to).getString("name");
        }
        else if (fieldname.equals("priority"))
        {
            from = before.getString(fieldname);
            to = after.getString(fieldname);

            if (from != null)
                fromString = getConstantsManager().getPriorityName(from);

            if (to != null)
                toString = getConstantsManager().getPriorityName(to);
        }
        else if (fieldname.equals("timeestimate") || fieldname.equals("timespent"))
        {
            Long fromValue = before.getLong(fieldname);
            Long toValue = after.getLong(fieldname);

            if (fromValue != null)
            {
                from = fromValue.toString();
                // DO NOT store formatted strings in the database, as they cannot be i18n'ed when they are retrieved
                // store the raw value and i18n it when displaying
                fromString = from = fromValue.toString();
            }

            if (toValue != null)
            {
                to = toValue.toString();
                // DO NOT store formatted strings in the database, as they cannot be i18n'ed when they are retrieved
                // store the raw value and i18n it when displaying
                toString = toValue.toString();
            }
        }
        else if (fieldname.equals("status"))
        {
            from = before.getString(fieldname);
            to = after.getString(fieldname);

            if (from != null)
            {
                GenericValue fromStatus = getConstantsManager().getStatus(from);

                if (fromStatus != null)
                    fromString = fromStatus.getString("name");
            }

            if (to != null)
            {
                GenericValue toStatus = getConstantsManager().getStatus(to);

                if (toStatus != null)
                    toString = toStatus.getString("name");
            }
        }
        else if (fieldname.equals("security"))
        {
            from = before.getString(fieldname);
            to = after.getString(fieldname);

            return generateSecurityChangeItem(fieldname, from, to);
        }
        else
        {
            fromString = before.getString(fieldname);
            toString = after.getString(fieldname);
        }

        return new ChangeItemBean(ChangeItemBean.STATIC_FIELD, fieldname, from, fromString, to, toString);
    }

    private static ConstantsManager getConstantsManager()
    {
        return ComponentAccessor.getConstantsManager();
    }

    public static ChangeItemBean generateSecurityChangeItem(String fieldname, String from, String to)
    {
        String fromString = null;
        String toString = null;
        if (from != null)
        {
            try
            {
                GenericValue fromLevel = ComponentAccessor.getIssueSecurityLevelManager().getIssueSecurityLevel(new Long(from));

                if (fromLevel != null)
                    fromString = fromLevel.getString("name");
            }
            catch (GenericEntityException e)
            {
                log.error(e, e);
            }
            catch (NumberFormatException e)
            {
                log.error(e, e);
            }
        }

        if (to != null)
        {
            try
            {
                GenericValue toLevel = ComponentAccessor.getIssueSecurityLevelManager().getIssueSecurityLevel(new Long(to));

                if (toLevel != null)
                    toString = toLevel.getString("name");
            }
            catch (GenericEntityException e)
            {
                log.error(e, e);
            }
            catch (NumberFormatException e)
            {
                log.error(e, e);
            }
        }
        return new ChangeItemBean(ChangeItemBean.STATIC_FIELD, fieldname, from, fromString, to, toString);
    }

    private static String getUserkey(User user)
    {
        if (user == null)
        {
            return null;
        }
        ApplicationUser applicationUser = getUserManager().getUserByName(user.getName());
        return applicationUser.getKey();
    }

    private static UserManager getUserManager()
    {
        return ComponentAccessor.getUserManager();
    }
}