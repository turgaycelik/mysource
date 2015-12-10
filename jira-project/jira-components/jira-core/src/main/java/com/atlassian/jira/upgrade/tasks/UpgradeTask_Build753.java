package com.atlassian.jira.upgrade.tasks;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JRA-26194: usernames in Favourite Filters should be stored lower case only. Update the storage. On the 5.0.x branch
 * this was UT_728. We introduced it on the branch and need to run it again in case people go from 5.0.2 -> 5.1
 * rather that 5.0.3+ -> 5.1.
 *
 * @since v5.1
 */
public class UpgradeTask_Build753 extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build753.class);
    private final OfBizDelegator delegator;

    private static final class Table
    {
        static final String FAVOURITE_ASSOCIATION = "FavouriteAssociations";
        static final String SEARCH_REQUEST = "SearchRequest";
        static final String PORTAL_PAGE = "PortalPage";
    }

    private static final class Column
    {
        private static final String ENTITY_ID = "entityId";
        private static final String USERNAME = "username";
        private static final String ENTITY_TYPE = "entityType";
        private static final String SEQUENCE = "sequence";
        private static final String FAV_COUNT = "favCount";
    }

    private static final class Order
    {
        private static final String ASCENDING = " ASC";
    }

    public UpgradeTask_Build753(OfBizDelegator delegator)
    {
        super(false);
        this.delegator = delegator;
    }

    @Override
    public String getBuildNumber()
    {
        return "753";
    }

    @Override
    public void doUpgrade(boolean setupMode)
    {
        final List<GenericValue> gvs = delegator.findAll(Table.FAVOURITE_ASSOCIATION);
        if (gvs == null || gvs.isEmpty())
        {
            return;
        }

        LOG.info(String.format("Analysing %d Favourite Associations...", gvs.size()));
        for (GenericValue gv : gvs)
        {
            final String username = gv.getString(Column.USERNAME);
            if (username == null)
            {
                continue;
            }
            final String lowercase_username = IdentifierUtils.toLowerCase(username);
            if (!username.equals(lowercase_username))
            {
                delegator.removeValue(gv);

                //Lets see if there is a favourite already associated with the entity.
                final Map<String, Object> lowercaseFind = new PrimitiveMap.Builder()
                        .add(Column.ENTITY_TYPE, gv.getString(Column.ENTITY_TYPE))
                        .add(Column.USERNAME, lowercase_username)
                        .add(Column.ENTITY_ID, gv.getLong(Column.ENTITY_ID)).toMap();

                final List<GenericValue> lowercaseGvs = delegator.findByAnd(Table.FAVOURITE_ASSOCIATION, lowercaseFind);
                if (lowercaseGvs == null || lowercaseGvs.size() == 0)
                {
                    // association for lower case username doesn't exist, need to create it
                    final Collection<Long> previousEntities = getFavouriteIds(lowercase_username, gv.getString(Column.ENTITY_TYPE));

                    delegator.createValue(Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder()
                            .add(Column.USERNAME, lowercase_username)
                            .add(Column.ENTITY_TYPE, gv.getString(Column.ENTITY_TYPE))
                            .add(Column.ENTITY_ID, gv.getLong(Column.ENTITY_ID))
                            .add(Column.SEQUENCE, (long)previousEntities.size()).toMap());
                }
                else
                {
                    decrementFavCount(gv);
                }
            }
        }
    }

    private void decrementFavCount(GenericValue gv)
    {
        final Long id = gv.getLong(Column.ENTITY_ID);
        if (id == null)
        {
            return;
        }

        final String type = gv.getString(Column.ENTITY_TYPE);
        if (Table.SEARCH_REQUEST.equalsIgnoreCase(type) || Table.PORTAL_PAGE.equalsIgnoreCase(type))
        {
            final GenericValue sharedEntity = delegator.findByPrimaryKey(type, id);
            if (sharedEntity != null)
            {
                Long count = sharedEntity.getLong(Column.FAV_COUNT);
                if (count == null || count > 0)
                {
                    count = count == null ? 0L : count - 1;
                    sharedEntity.set(Column.FAV_COUNT, count);
                    delegator.store(sharedEntity);

                }
            }
        }
        else
        {
            LOG.warn("Unable to update the favourite count for entity of type '" + type + "'.");
        }
    }

    private Collection<Long> getFavouriteIds(final String username, final String entityType)
    {
        final List<GenericValue> idGVs = getFavouriteGVsOfEntityType(username, entityType, Lists.<String>newArrayList(Column.ENTITY_ID));
        final List<Long> ids = new ArrayList<Long>(idGVs.size());

        for (final GenericValue genericValue : idGVs)
        {
            ids.add(genericValue.getLong(Column.ENTITY_ID));
        }

        return Collections.unmodifiableList(ids);
    }

    private List<GenericValue> getFavouriteGVsOfEntityType(final String username, final String entityType, final List<String> fieldToReturnList)
    {
        final EntityCondition userCondition = new EntityExpr(Column.USERNAME, EntityOperator.EQUALS, username);
        final EntityCondition typeCondition = new EntityExpr(Column.ENTITY_TYPE, EntityOperator.EQUALS, entityType);
        final EntityCondition joinedCondition = new EntityConditionList(
                Lists.<EntityCondition>newArrayList(userCondition, typeCondition), EntityOperator.AND);
        final List<String> orderByList = Lists.newArrayList(Column.SEQUENCE + Order.ASCENDING);
        return delegator.findByCondition(Table.FAVOURITE_ASSOCIATION, joinedCondition, fieldToReturnList, orderByList);
    }

    @Override
    public String getShortDescription()
    {
        return "Making the owner of favourites lowercase";
    }
}
