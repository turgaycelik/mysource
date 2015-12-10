/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.searchers.renderer.AbstractUserSearchRenderer;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.I18nBean;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe  // It doesn't need to be
public class ParameterStore
{
    private User user;
    private List<TextOption> timePeriods;
    private List<Map<String, String>> reporterTypes;
    private List<Map<String, String>> assigneeTypes;
    private List<Map<String, String>> creatorTypes;
    private I18nHelper i18n;

    public ParameterStore(User user)
    {
        this.user = user;
        i18n = new I18nBean(user);
    }

    public ParameterStore(String userName)
    {
        this(UserUtils.getUser(userName));
    }

    /**
     * The time periods for filtering by last updated and date created.
     * All time periods are in minutes
     *
     * @return the collection of possible time options
     */
    public Collection<TextOption> getTimePeriods()
    {
        if (timePeriods == null)
        {
            setTimePeriods();
        }
        return timePeriods;
    }

    private void setTimePeriods()
    {
        // Note: TextOption is mutable
        timePeriods = Lists.newArrayList(
                new TextOption("-1h", i18n.getText("time.periods.hour")),
                new TextOption("-1d", i18n.getText("time.periods.day")),
                new TextOption("-1w", i18n.getText("time.periods.week")),
                new TextOption("-4w 2d", i18n.getText("time.periods.month")) );
    }

    public List<Map<String, String>> getReporterTypes()
    {
        if (reporterTypes == null)
        {
            setReporterTypes();
        }
        return reporterTypes;
    }

    private Map<String,String> param(String valueKey, String key, String related)
    {
        return ImmutableMap.<String,String>builder()
                .put("value", i18n.getText(valueKey))
                .put("key", key)
                .put("related", related)
                .build();
    }

    private void setReporterTypes()
    {
        reporterTypes = new ArrayList<Map<String,String>>();
        reporterTypes.add(param("reporter.types.anyuser", "", AbstractUserSearchRenderer.SELECT_LIST_NONE));
        reporterTypes.add(param("reporter.types.noreporter", DocumentConstants.ISSUE_NO_AUTHOR, AbstractUserSearchRenderer.SELECT_LIST_NONE));

        // If the current user is null (not logged in) do not include the "Current User" as one of the options
        // Fixes: JRA-3341
        if (user != null)
        {
            reporterTypes.add(param("reporter.types.currentuser", DocumentConstants.ISSUE_CURRENT_USER, AbstractUserSearchRenderer.SELECT_LIST_NONE));
        }

        reporterTypes.add(param("reporter.types.specifyuser", DocumentConstants.SPECIFIC_USER, AbstractUserSearchRenderer.SELECT_LIST_USER));
        reporterTypes.add(param("reporter.types.specifygroup", DocumentConstants.SPECIFIC_GROUP, AbstractUserSearchRenderer.SELECT_LIST_GROUP));
    }

    public List<Map<String, String>> getAssigneeTypes()
    {
        if (assigneeTypes == null)
        {
            setAssigneeTypes();
        }
        return assigneeTypes;
    }

    private void setAssigneeTypes()
    {
        assigneeTypes = new ArrayList<Map<String,String>>();
        // Note: ImmutableMap won't accept null, so this one is special
        assigneeTypes.add(MapBuilder.<String,String>newBuilder()
                .add("value", i18n.getText("assignee.types.anyuser"))
                .add("key", null)
                .add("related", AbstractUserSearchRenderer.SELECT_LIST_NONE)
                .toMap());
        assigneeTypes.add(param("assignee.types.unassigned", DocumentConstants.ISSUE_UNASSIGNED, AbstractUserSearchRenderer.SELECT_LIST_NONE));

        // If the current user is null (not logged in) do not include the "Current User" as one of the options
        // Fixes: JRA-3341
        if (user != null)
        {
            assigneeTypes.add(param("assignee.types.currentuser", DocumentConstants.ISSUE_CURRENT_USER, AbstractUserSearchRenderer.SELECT_LIST_NONE));
        }
        
        assigneeTypes.add(param("assignee.types.specifyuser", DocumentConstants.SPECIFIC_USER, AbstractUserSearchRenderer.SELECT_LIST_USER));
        assigneeTypes.add(param("assignee.types.specifygroup", DocumentConstants.SPECIFIC_GROUP, AbstractUserSearchRenderer.SELECT_LIST_GROUP));
    }

    public List<Map<String, String>> getCreatorTypes()
    {
        if (creatorTypes == null)
        {
            setCreatorTypes();
        }
        return creatorTypes;
    }

    private void setCreatorTypes()
    {
        creatorTypes = new ArrayList<Map<String,String>>();
        // Note: ImmutableMap won't accept null
        creatorTypes.add(MapBuilder.<String,String>newBuilder()
                .add("value", i18n.getText("creator.types.anyuser"))
                .add("key", null)
                .add("related", AbstractUserSearchRenderer.SELECT_LIST_NONE)
                .toMap());
        creatorTypes.add(param("creator.types.nocreator", DocumentConstants.ISSUE_ANONYMOUS_CREATOR, AbstractUserSearchRenderer.SELECT_LIST_NONE));


        if (user != null)
        {
            creatorTypes.add(param("creator.types.currentuser", DocumentConstants.ISSUE_CURRENT_USER, AbstractUserSearchRenderer.SELECT_LIST_NONE));
        }

        creatorTypes.add(param("creator.types.specifyuser", DocumentConstants.SPECIFIC_USER, AbstractUserSearchRenderer.SELECT_LIST_USER));
        creatorTypes.add(param("creator.types.specifygroup", DocumentConstants.SPECIFIC_GROUP, AbstractUserSearchRenderer.SELECT_LIST_GROUP));
    }

}
