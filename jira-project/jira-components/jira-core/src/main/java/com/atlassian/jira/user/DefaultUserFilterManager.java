package com.atlassian.jira.user;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;

import static com.google.common.base.Predicates.notNull;
import static org.ofbiz.core.entity.EntityOperator.IN;

/**
 * @since v6.2
 */
public class DefaultUserFilterManager implements UserFilterManager
{
    static final Logger log = Logger.getLogger(DefaultUserFilterManager.class);

    static final class Entity
    {
        static final String ID = "id";
        static final String CONFIG_ID = "customfieldconfig";
        static final String CUSTOMFIELD_ID = "customfield";
        static final String ENABLED = "enabled";
    }
    static final class GroupEntity
    {
        static final String FILTER_ID = "userpickerfilter";
        static final String GROUP_NAME = "group";
    }
    static final class RoleEntity
    {
        static final String FILTER_ID = "userpickerfilter";
        static final String ROLE_ID = "projectroleid";
    }

    static final String TABLE_USERPICKER_FILTER = "UserPickerFilter";
    static final String TABLE_USERPICKER_FILTER_GROUP = "UserPickerFilterGroup";
    static final String TABLE_USERPICKER_FILTER_ROLE = "UserPickerFilterRole";

    private final OfBizDelegator delegator;

    public DefaultUserFilterManager(final OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }

    @Override
    public UserFilter getFilter(final FieldConfig fieldConfig)
    {
        if (fieldConfig == null)
        {
            return null;
        }

        final GenericValue gv = getFilterGV(fieldConfig.getId());
        if (gv == null)
        {
            // default to a disabled UserFilter
            return UserFilter.DISABLED;
        }

        return convertGVToFilter(gv);
    }

    private GenericValue getFilterGV(Long fieldConfigId)
    {
        final List<GenericValue> genericValues = delegator.findByField(TABLE_USERPICKER_FILTER, Entity.CONFIG_ID, fieldConfigId);
        if (CollectionUtils.isEmpty(genericValues))
        {
            return null;
        }
        else if (genericValues.size() > 1)
        {
            log.warn("Incorrect data in user picker filter: more than one filter exist for the same fieldconfig id " + fieldConfigId + ". Returning the first one.");
        }
        return genericValues.get(0);
    }

    private UserFilter convertGVToFilter(final GenericValue gv)
    {
        if (!isEnabled(gv))
        {
            return UserFilter.DISABLED;
        }
        return getFilterWithGroupsAndRoles(getFilterId(gv));
    }

    private boolean isEnabled(final GenericValue gv)
    {
        // null or false are considered as not enabled
        return Boolean.TRUE.equals(gv.getBoolean(Entity.ENABLED));
    }

    private UserFilter getFilterWithGroupsAndRoles(final Long filterId)
    {
        final List<GenericValue> groupFilterGVs = delegator.findByField(TABLE_USERPICKER_FILTER_GROUP, GroupEntity.FILTER_ID, filterId);
        final List<GenericValue> roleFilterGVs = delegator.findByField(TABLE_USERPICKER_FILTER_ROLE, RoleEntity.FILTER_ID, filterId);
        return new UserFilter(true, convertGVsToGroupRoleIds(roleFilterGVs), convertGVsToGroupNames(groupFilterGVs));
    }

    private Collection<Long> convertGVsToGroupRoleIds(final List<GenericValue> roleFilterGVs)
    {
        return Lists.transform(roleFilterGVs, new Function<GenericValue, Long>()
        {
            @Override
            public Long apply(final GenericValue gv)
            {
                return gv.getLong(RoleEntity.ROLE_ID);
            }
        });
    }

    private Collection<String> convertGVsToGroupNames(final List<GenericValue> groupFilterGVs)
    {
        return Lists.transform(groupFilterGVs, new Function<GenericValue, String>()
        {
            @Override
            public String apply(final GenericValue gv)
            {
                return gv.getString(GroupEntity.GROUP_NAME);
            }
        });
    }

    @Override
    public void updateFilter(final FieldConfig fieldConfig, final UserFilter filter)
    {
        GenericValue gv = getFilterGV(fieldConfig.getId());
        if (gv == null)
        {
            // only need to update if the new value is enabled
            if (filter.isEnabled())
            {
                // need to create the filter entry
                final GenericValue newGV = delegator.createValue(TABLE_USERPICKER_FILTER, ImmutableMap.<String, Object>of(
                        Entity.CONFIG_ID, fieldConfig.getId(),
                        Entity.CUSTOMFIELD_ID, fieldConfig.getCustomField().getIdAsLong(),
                        Entity.ENABLED, Boolean.TRUE));
                createGroupsAndRoles(getFilterId(newGV), filter.getGroups(), filter.getRoleIds());
            }
        }
        else
        {
            boolean oldIsEnabled = isEnabled(gv);
            // only update master filter record if isEnabled is changed.
            if (filter.isEnabled() != oldIsEnabled)
            {
                gv.set(Entity.ENABLED, filter.isEnabled());
                try
                {
                    gv.store();
                }
                catch (GenericEntityException e)
                {
                    throw new DataAccessException(e.getMessage(), e);
                }
            }
            final Long filterId = getFilterId(gv);
            if (oldIsEnabled)
            {
                removeGroupsAndRoles(filterId);
            }
            if (filter.isEnabled())
            {
                createGroupsAndRoles(filterId, filter.getGroups(), filter.getRoleIds());
            }
        }
    }

    @Override
    public void removeFilter(final Long customFieldId)
    {
        if (customFieldId == null)
        {
            ImmutableSet.of();
        }
        else
        {
            // by right, we should get all fieldconfigs first and then use the fieldconfigids to find the GVs.
            // but since we store customfield id in the table directly, we could use that to retrieve
            //  all user filters for the custom field
            final List<GenericValue> genericValues = delegator.findByField(TABLE_USERPICKER_FILTER, Entity.CUSTOMFIELD_ID, customFieldId);
            final Collection<Long> filterIds = Collections2.filter(
                    Collections2.transform(genericValues, new Function<GenericValue, Long>()
                    {
                        @Override
                        public Long apply(@Nullable final GenericValue input)
                        {
                            return input != null ? input.getLong(Entity.ID) : null;
                        }
                    }), notNull());
            if (!filterIds.isEmpty())
            {
                // NOT batching. Assume that the number of field configs per custom field is small.
                delegator.removeByCondition(TABLE_USERPICKER_FILTER_GROUP, new EntityExpr(GroupEntity.FILTER_ID, IN, filterIds));
                delegator.removeByCondition(TABLE_USERPICKER_FILTER_ROLE, new EntityExpr(RoleEntity.FILTER_ID, IN, filterIds));
                delegator.removeAll(genericValues);
            }
        }
    }

    private Long getFilterId(final GenericValue gv)
    {
        return gv.getLong(Entity.ID);
    }

    private void removeGroupsAndRoles(final Long filterId)
    {
        delegator.removeByAnd(TABLE_USERPICKER_FILTER_GROUP, ImmutableMap.<String, Object>of(GroupEntity.FILTER_ID, filterId));
        delegator.removeByAnd(TABLE_USERPICKER_FILTER_ROLE, ImmutableMap.<String, Object>of(RoleEntity.FILTER_ID, filterId));
    }

    private void createGroupsAndRoles(final Long filterId, final Set<String> groups, final Set<Long> roleIds)
    {
        if (groups != null)
        {
            for (String group : groups)
            {
                delegator.createValue(TABLE_USERPICKER_FILTER_GROUP, ImmutableMap.<String, Object>of(
                        GroupEntity.FILTER_ID, filterId,
                        GroupEntity.GROUP_NAME, group));
            }
        }
        if (roleIds != null)
        {
            for (Long roleId : roleIds)
            {
                delegator.createValue(TABLE_USERPICKER_FILTER_ROLE, ImmutableMap.<String, Object>of(
                        RoleEntity.FILTER_ID, filterId,
                        RoleEntity.ROLE_ID, roleId));
            }
        }
    }
}
