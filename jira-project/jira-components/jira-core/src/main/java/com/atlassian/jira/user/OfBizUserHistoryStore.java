package com.atlassian.jira.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.EntityListConsumer;
import com.atlassian.jira.entity.Select;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableList;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import net.jcip.annotations.ThreadSafe;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Arrays.asList;
import static org.ofbiz.core.entity.EntityOperator.AND;
import static org.ofbiz.core.entity.EntityOperator.EQUALS;
import static org.ofbiz.core.entity.EntityOperator.IN;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN_EQUAL_TO;

/**
 * OfBiz implementation of {@link com.atlassian.jira.user.UserHistoryStore}
 *
 * @since v4.0
 */
@ThreadSafe
public class OfBizUserHistoryStore implements UserHistoryStore
{
    private static final Logger LOG = Logger.getLogger(OfBizUserHistoryStore.class);

    private static final int DEFAULT_MAX_ITEMS = 50;

    // Entity name
    private static final String TABLE = "UserHistoryItem";

    // Field names
    private static final String ID = "id";
    private static final String USER = "username";   // Note: this column actually stores the user key
    private static final String TYPE = "type";
    private static final String ENTITY_ID = "entityId";
    private static final String LAST_VIEWED = "lastViewed";
    private static final String DATA = "data";

    private static final List<String> SORT_BY_LAST_VIEWED_DESC = ImmutableList.of(LAST_VIEWED + " DESC");

    private final OfBizDelegator delegator;
    private final ApplicationProperties applicationProperties;

    public OfBizUserHistoryStore(OfBizDelegator delegator, ApplicationProperties applicationProperties)
    {
        this.delegator = delegator;
        this.applicationProperties = applicationProperties;
    }

    // Note: The generic UserHistoryStore interface declares the user as nullable, because anonymous users get
    // their history stored in the session, instead.  However, the user should *never* be null when it reaches us.
    public void addHistoryItem(ApplicationUser user, @Nonnull UserHistoryItem item)
    {
        notNull("user", user);
        notNull("historyItem", item);

        final int numberRemoved = delegator.removeByAnd(TABLE, FieldMap.build(
                TYPE, item.getType().getName(),
                USER, user.getKey(),
                ENTITY_ID, item.getEntityId()));
        addHistoryItemNoChecks(user, item);

        // Only keep the number of issues specified in jira-application.properties

        // Optimisation - if we removed one, the list can't be over limit
        if (numberRemoved == 0)
        {
            final String maxItemsStr = applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_HISTORY_ITEMS);
            int maxItems = DEFAULT_MAX_ITEMS;
            try
            {
                maxItems = Integer.parseInt(maxItemsStr);
            }
            catch (NumberFormatException e)
            {
                LOG.warn("Incorrect format of property 'jira.max.history.items'.  Should be a number.");
            }

            final List<GenericValue> historyItemGVs = delegator.findByAnd(TABLE, FieldMap.build(
                    TYPE, item.getType().getName(),
                    USER, user.getKey()), SORT_BY_LAST_VIEWED_DESC);

            // only keep first 50 issues.
            for (int i = maxItems; i < historyItemGVs.size(); i++)
            {
                delegator.removeByAnd(TABLE, FieldMap.build(ID, historyItemGVs.get(i).getLong(ID)));
            }
        }
    }

    boolean removeHistoryItem(@Nonnull ApplicationUser user, @Nonnull UserHistoryItem item)
    {
        final FieldMap fields = FieldMap.build(
                TYPE, item.getType().getName(),
                USER, user.getKey(),
                ENTITY_ID, item.getEntityId() );
        return delegator.removeByAnd(TABLE, fields) > 0;
    }

    /**
     * Optimised method for adding a history item.  This will throw a duplicate row exception from the db if you try and
     * insert a history item that already exists.  Should only call if we are sure it doesn't exist.
     * <p/>
     * This does not expire old items or try and update existing items.  It is dumb.
     *
     * @param user The user to insert the record for
     * @param item Teh item to insert into the db
     */
    public void addHistoryItemNoChecks(@Nonnull ApplicationUser user, @Nonnull UserHistoryItem item)
    {
        notNull("user", user);
        notNull("historyItem", item);

        delegator.createValue(TABLE, MapBuilder.<String, Object>newBuilder()
                .add(TYPE, item.getType().getName())
                .add(USER, user.getKey())
                .add(ENTITY_ID, item.getEntityId())
                .add(LAST_VIEWED, item.getLastViewed())
                .add(DATA, item.getData()).toMap());

    }

    /**
     * Optimised method for updating a record in the database.   If the record doesn't exist it will create it, otherwise just
     * update it.
     * <p/>
     * This does not expire old items or try and update existing items.  It is dumb.
     *
     * @param user The user to update the record for
     * @param item The item to update
     */
    public void updateHistoryItemNoChecks(@Nonnull ApplicationUser user, @Nonnull UserHistoryItem item)
    {
        notNull("user", user);
        notNull("historyItem", item);

        final List<GenericValue> list = delegator.findByAnd(TABLE, FieldMap.build(
                TYPE, item.getType().getName(),
                USER, user.getKey(),
                ENTITY_ID, item.getEntityId()));
        if (list.isEmpty())
        {
            createHistoryItemNoChecks(user, item);
        }
        else if (list.size() == 1)
        {
            updateHistoryItemLastViewed(list.get(0), item.getLastViewed());
        }
        else
        {
            LOG.warn("Somehow there is more than one record for the following user/type/entity - " + item.toString());
        }
    }

    private static void updateHistoryItemLastViewed(@Nonnull final GenericValue genericValue, final long lastViewed)
    {
        genericValue.set(LAST_VIEWED, lastViewed);
        try
        {
            genericValue.store();
        }
        catch (GenericEntityException e)
        {
            LOG.error("Exception thrown while updating user history item", e);
        }
    }

    private GenericValue createHistoryItemNoChecks(@Nonnull final ApplicationUser user, @Nonnull UserHistoryItem item)
    {
        return delegator.createValue(TABLE, new FieldMap()
                    .add(TYPE, item.getType().getName())
                    .add(USER, user.getKey())
                    .add(ENTITY_ID, item.getEntityId())
                    .add(LAST_VIEWED, item.getLastViewed())
                    .add(DATA, item.getData() ));
    }

    /**
     * Method for expiring old items.  You can actually delete any items but it is typically used to delete old records.
     *
     * @param user      The user to remove entries for
     * @param type      The type of record to remove
     * @param entityIds The list of entity ids to remove.
     */
    public void expireOldHistoryItems(@Nonnull ApplicationUser user, @Nonnull UserHistoryItem.Type type, Collection<String> entityIds)
    {
        final List<EntityExpr> conditions = asList(
                new EntityExpr(USER, EQUALS, user.getKey()),
                new EntityExpr(TYPE, EQUALS, type.getName()),
                new EntityExpr(ENTITY_ID, IN, entityIds) );
        delegator.removeByCondition(TABLE, new EntityConditionList(conditions, AND));
    }

    @Override
    @Nonnull
    public List<UserHistoryItem> getHistory(@Nonnull UserHistoryItem.Type type, @Nonnull String userKey)
    {
        notNull("userKey", userKey);
        notNull("type", type);
        return getSortedHistory(FieldMap.build(
                TYPE, type.getName(),
                USER, userKey));
    }

    @Nonnull
    public List<UserHistoryItem> getHistory(@Nonnull UserHistoryItem.Type type, @Nonnull ApplicationUser user)
    {
        notNull("user", user);
        return getHistory(type, user.getKey());
    }

    private List<UserHistoryItem> getSortedHistory(final FieldMap fields)
    {
        final List<GenericValue> historyItemGVs = delegator.findByAnd(TABLE, fields, SORT_BY_LAST_VIEWED_DESC);
        final List<UserHistoryItem> returnList = new ArrayList<UserHistoryItem>(historyItemGVs.size());
        for (GenericValue historyItemGV : historyItemGVs)
        {
            returnList.add(convertGV(historyItemGV));
        }
        return returnList;
    }


    public Set<UserHistoryItem.Type> removeHistoryForUser(@Nonnull ApplicationUser user)
    {
        notNull("user", user);

        final Set<UserHistoryItem.Type> types = getDistinctHistoryItemTypes(user);
        if (!types.isEmpty())
        {
            Delete.from(TABLE)
                    .whereEqual(USER, user.getKey())
                    .execute(delegator);
        }
        return types;
    }

    public void removeHistoryOlderThan(@Nonnull Long timestamp)
    {
        Long currentTimestamp = System.currentTimeMillis();
        if ((currentTimestamp - TimeUnit.DAYS.toMillis(30)) < timestamp)
        {
            throw new IllegalArgumentException("Can't delete user history that is not at least 30 days old");
        }

        delegator.removeByCondition(TABLE, new EntityExpr(LAST_VIEWED, LESS_THAN_EQUAL_TO, timestamp));
    }


    @Nonnull
    private Set<UserHistoryItem.Type> getDistinctHistoryItemTypes(@Nonnull ApplicationUser user)
    {
        return Select.distinctString(TYPE)
                .from(TABLE)
                .whereEqual(USER, user.getKey())
                .runWith(delegator)
                .consumeWith(new TypeCollector());
    }



    private static UserHistoryItem convertGV(GenericValue historyItemGV)
    {
        final UserHistoryItem.Type type = UserHistoryItem.Type.getInstance(historyItemGV.getString(TYPE));
        final String entityId = historyItemGV.getString(ENTITY_ID);
        final Long lastViewed = historyItemGV.getLong(LAST_VIEWED);
        final String data = historyItemGV.getString(DATA);
        return new UserHistoryItem(type, entityId, lastViewed, data);
    }

    static class TypeCollector implements EntityListConsumer<String,Set<UserHistoryItem.Type>>
    {
        final Set<UserHistoryItem.Type> types = new HashSet<UserHistoryItem.Type>(16);

        @Override
        public void consume(final String typeName)
        {
            types.add(UserHistoryItem.Type.getInstance(typeName));
        }

        @Override
        public Set<UserHistoryItem.Type> result()
        {
            return types;
        }
    }
}
