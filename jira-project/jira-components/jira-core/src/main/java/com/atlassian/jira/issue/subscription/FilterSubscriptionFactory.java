package com.atlassian.jira.issue.subscription;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import com.atlassian.jira.entity.AbstractEntityFactory;
import com.atlassian.jira.ofbiz.FieldMap;

import org.ofbiz.core.entity.GenericValue;

/**
 * @since v6.2
 */
public class FilterSubscriptionFactory extends AbstractEntityFactory<FilterSubscription>
{

    public static final String ID = "id";
    public static final String FILTER_ID = "filterID";
    public static final String USER_KEY = "username";  // Deliberate mismatch -- column meaning has changed
    public static final String GROUP = "group";
    public static final String LAST_RUN_TIME = "lastRun";
    public static final String EMAIL_ON_EMPTY = "emailOnEmpty";

    @Override
    public String getEntityName()
    {
        return "FilterSubscription";
    }

    @Override
    public FilterSubscription build(GenericValue gv)
    {
        return new DefaultFilterSubscription(
                gv.getLong(ID),
                gv.getLong(FILTER_ID),
                gv.getString(USER_KEY),
                gv.getString(GROUP),
                gv.getTimestamp(LAST_RUN_TIME),
                Boolean.valueOf(gv.getString(EMAIL_ON_EMPTY)));
    }

    @Override
    public Map<String,Object> fieldMapFrom(FilterSubscription filterSubscription)
    {
        return new FieldMap()
                .add(ID, filterSubscription.getId())
                .add(FILTER_ID, filterSubscription.getFilterId())
                .add(USER_KEY, filterSubscription.getUserKey())
                .add(GROUP, filterSubscription.getGroupName())
                .add(LAST_RUN_TIME, toTimestamp(filterSubscription.getLastRunTime()))
                .add(EMAIL_ON_EMPTY, String.valueOf(filterSubscription.isEmailOnEmpty()));
    }

    private static Timestamp toTimestamp(Date date)
    {
        return (date != null) ? new Timestamp(date.getTime()) : null;
    }

}
