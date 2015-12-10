package com.atlassian.jira.favourites;

import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.user.ApplicationUsers.getKeyFor;

/**
 * FavouritesStore that uses the OfBiz to store favourites
 *
 * @since v3.13
 */
public class OfBizFavouritesStore implements FavouritesStore
{
    private final OfBizDelegator delegator;

    static final class Table
    {
        static final String FAVOURITE_ASSOCIATION = "FavouriteAssociations";
    }

    private static final class Column
    {
        private static final String ENTITY_ID = "entityId";
        private static final String USERKEY = "username";  // The mismatch is deliberate
        private static final String ENTITY_TYPE = "entityType";
        private static final String SEQUENCE = "sequence";
        private static final String ID = "id";
    }

    private static final class Order
    {
        private static final String ASCENDING = " ASC";
    }

    public OfBizFavouritesStore(final OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }

    @Override
    public boolean addFavourite(final ApplicationUser user, final SharedEntity entity)
    {
        return (getAssociation(user, entity) == null) && (createAssociation(user, entity) != null);
    }

    @Override
    public boolean removeFavourite(final ApplicationUser user, final SharedEntity entity)
    {
        final List<GenericValue> favourites = getFavouriteGVsOfEntityType(getKeyFor(user), entity.getEntityType(), Lists.newArrayList(Column.ID, Column.ENTITY_ID, Column.SEQUENCE));
        final List<GenericValue> changedValues = new ArrayList<GenericValue>(favourites.size());
        GenericValue removeGv = null;
        int expectedSequence = 0;

        //when we remove, we need to do a reorder of the sequence to make things consistent.
        for (final Object element : favourites)
        {
            final GenericValue favouriteGv = (GenericValue) element;
            final Long entityId = favouriteGv.getLong(Column.ENTITY_ID);
            if ((removeGv == null) && (entityId != null) && entityId.equals(entity.getId()))
            {
                removeGv = favouriteGv;
            }
            else
            {
                final Long storedSequence = favouriteGv.getLong(Column.SEQUENCE);
                if ((storedSequence == null) || (storedSequence != expectedSequence))
                {
                    favouriteGv.set(Column.SEQUENCE, (long) expectedSequence);
                    changedValues.add(favouriteGv);
                }
                expectedSequence++;
            }
        }

        //remove the old GV.
        if (removeGv != null)
        {
            delegator.removeValue(removeGv);
            //we only need to reorder when we remove an entity.
            if (!changedValues.isEmpty())
            {
                delegator.storeAll(changedValues);
            }
        }

        return removeGv != null;
    }

    @Override
    public boolean isFavourite(final ApplicationUser user, final SharedEntity entity)
    {
        return (getAssociation(user, entity) != null);
    }

    @Override
    public Collection<Long> getFavouriteIds(final ApplicationUser user, final SharedEntity.TypeDescriptor<?> entityType)
    {
        return getFavouriteIds(getKeyFor(user), entityType);
    }

    @Override
    public Collection<Long> getFavouriteIds(final String userKey, final SharedEntity.TypeDescriptor<?> entityType)
    {
        final List<GenericValue> idGVs = getFavouriteGVsOfEntityType(userKey, entityType, Lists.newArrayList(Column.ENTITY_ID));

        final List<Long> ids = new ArrayList<Long>(idGVs.size());

        for (final GenericValue genericValue : idGVs)
        {
            ids.add(genericValue.getLong(Column.ENTITY_ID));
        }

        return Collections.unmodifiableList(ids);
    }

    @Override
    public void removeFavouritesForUser(final ApplicationUser user, final SharedEntity.TypeDescriptor<?> entityType)
    {
        delegator.removeByAnd(Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder()
                .add(Column.USERKEY, getKeyFor(user))
                .add(Column.ENTITY_TYPE, entityType.getName()).toMap());
    }

    @Override
    public void removeFavouritesForEntity(final SharedEntity entity)
    {
        delegator.removeByAnd(Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder().add(Column.ENTITY_ID, entity.getId()).add(Column.ENTITY_TYPE,
            entity.getEntityType().getName()).toMap());
    }

    @Override
    public void updateSequence(final ApplicationUser user, final List<? extends SharedEntity> favouriteEntities)
    {
        storeGVListSequence(getFavouriteGVsOfEntityTypes(user, favouriteEntities));
    }

    private GenericValue createAssociation(final ApplicationUser user, final SharedEntity entity)
    {
        //
        // on create we want to give it a sequence equal to the number of other entities of that type
        // in other words it goes to the end of the sequence list
        //
        final Collection<Long> previousEntities = getFavouriteIds(user, entity.getEntityType());
        final Long sequence = (long) previousEntities.size();

        return delegator.createValue(Table.FAVOURITE_ASSOCIATION, new PrimitiveMap.Builder()
                .add(Column.USERKEY, getKeyFor(user))
                .add(Column.ENTITY_TYPE, entity.getEntityType().getName())
                .add(Column.ENTITY_ID, entity.getId()).add(Column.SEQUENCE, sequence).toMap());
    }

    private GenericValue getAssociation(final ApplicationUser user, final SharedEntity entity)
    {
        final Map<String, Object> map = new PrimitiveMap.Builder()
                .add(Column.USERKEY, getKeyFor(user))
                .add(Column.ENTITY_TYPE, entity.getEntityType().getName()).add(Column.ENTITY_ID, entity.getId()).toMap();
        final List<GenericValue> gvs = delegator.findByAnd(Table.FAVOURITE_ASSOCIATION, map);
        if (gvs != null)
        {
            return EntityUtil.getOnly(gvs);
        }
        ///CLOVER:OFF
        return null;
        ///CLOVER:ON
    }

    private List<GenericValue> getFavouriteGVsOfEntityType(final String userKey, final SharedEntity.TypeDescriptor<?> entityType, final List<String> fieldToReturnList)
    {
        final EntityCondition userCondition = new EntityExpr(Column.USERKEY, EntityOperator.EQUALS, userKey);
        final EntityCondition typeCondition = new EntityExpr(Column.ENTITY_TYPE, EntityOperator.EQUALS, entityType.getName());
        final EntityCondition joinedCondition = new EntityConditionList(Lists.newArrayList(userCondition, typeCondition), EntityOperator.AND);
        final List<String> orderByList = Lists.newArrayList(Column.SEQUENCE + Order.ASCENDING);
        return delegator.findByCondition(Table.FAVOURITE_ASSOCIATION, joinedCondition, fieldToReturnList, orderByList);
    }

    private List<GenericValue> getFavouriteGVsOfEntityTypes(final ApplicationUser user, final List<? extends SharedEntity> entities)
    {
        final List<GenericValue> gvList = new ArrayList<GenericValue>();
        for (final SharedEntity entity : entities)
        {
            final GenericValue associationGV = getAssociation(user, entity);
            if (associationGV != null)
            {
                gvList.add(associationGV);
            }
        }
        return gvList;
    }

    private void storeGVListSequence(final List<GenericValue> sortedGVList)
    {
        for (int i = 0; i < sortedGVList.size(); i++)
        {
            final GenericValue genericValue = sortedGVList.get(i);
            genericValue.set(Column.SEQUENCE, (long) i);
        }
        delegator.storeAll(sortedGVList);
    }
}
