package com.atlassian.jira.user;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import org.apache.commons.collections.CollectionUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.user.DefaultUserFilterManager.Entity;
import static com.atlassian.jira.user.DefaultUserFilterManager.GroupEntity;
import static com.atlassian.jira.user.DefaultUserFilterManager.RoleEntity;
import static com.atlassian.jira.user.DefaultUserFilterManager.TABLE_USERPICKER_FILTER;
import static com.atlassian.jira.user.DefaultUserFilterManager.TABLE_USERPICKER_FILTER_GROUP;
import static com.atlassian.jira.user.DefaultUserFilterManager.TABLE_USERPICKER_FILTER_ROLE;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.ofbiz.core.entity.EntityOperator.IN;

@RunWith (ListeningMockitoRunner.class)
public class TestDefaultUserFilterManager
{
    public static final Long SAMPLE_FIELDCONFIG_ID = 101L;
    public static final Long SAMPLE_FILTER_ID = 101L;
    public static final String SAMEPLE_GROUP1 = "group1";
    public static final String SAMEPLE_GROUP2 = "group2";
    private static final Long SAMEPLE_ROLEID1 = 1001L;
    private static final Long SAMEPLE_ROLEID2 = 1002L;
    private static final String SAMEPL_CUSTOMFILED_ID = "customfield_1000";
    private static final Long SAMPLE_CUSTOMFILED_ID_LONG = 1000L;
    @Mock
    private OfBizDelegator delegator;

    @Mock
    private FieldConfig fieldConfig;

    @Mock
    private GenericValue filterGV;

    @InjectMocks
    private DefaultUserFilterManager defaultUserFilterManager;

    @Before
    public void setUp() throws Exception
    {
        when(fieldConfig.getId()).thenReturn(SAMPLE_FIELDCONFIG_ID);
        CustomField customField = mock(CustomField.class);
        when(fieldConfig.getCustomField()).thenReturn(customField);
        when(customField.getId()).thenReturn(SAMEPL_CUSTOMFILED_ID);
        when(customField.getIdAsLong()).thenReturn(SAMPLE_CUSTOMFILED_ID_LONG);
    }

    @Test
    public void testGetFilterNoData() throws Exception
    {
        assertThat(defaultUserFilterManager.getFilter(fieldConfig), sameInstance(UserFilter.DISABLED));
    }

    @Test
    public void testGetFilterDisabled() throws Exception
    {
        mockFilter(false);
        assertThat(defaultUserFilterManager.getFilter(fieldConfig), sameInstance(UserFilter.DISABLED));
    }

    @Test
    public void testGetFilterNullEnabled() throws Exception
    {
        mockFilter(null);
        assertThat(defaultUserFilterManager.getFilter(fieldConfig), sameInstance(UserFilter.DISABLED));
    }

    @Test
    public void testGetFilterEnabled() throws Exception
    {
        mockFilter(true);
        final UserFilter filter = defaultUserFilterManager.getFilter(fieldConfig);
        assertThat(filter.isEnabled(), is(true));
        assertThat(filter.getGroups(), hasSize(0));
        assertThat(filter.getRoleIds(), hasSize(0));
    }

    @Test
    public void testGetFilterEnabledWithGroups() throws Exception
    {
        mockFilter(true, ImmutableSet.of(SAMEPLE_GROUP1, SAMEPLE_GROUP2), null);
        final UserFilter filter = defaultUserFilterManager.getFilter(fieldConfig);
        assertThat(filter.isEnabled(), is(true));
        assertThat(filter.getGroups(), hasSize(2));
        assertThat(filter.getGroups(), hasItems(SAMEPLE_GROUP1, SAMEPLE_GROUP2));
        assertThat(filter.getRoleIds(), hasSize(0));
    }

    @Test
    public void testGetFilterEnabledWithRoles() throws Exception
    {
        mockFilter(true, null, ImmutableSet.<Long>of(SAMEPLE_ROLEID1, SAMEPLE_ROLEID2));
        final UserFilter filter = defaultUserFilterManager.getFilter(fieldConfig);
        assertThat(filter.isEnabled(), is(true));
        assertThat(filter.getGroups(), hasSize(0));
        assertThat(filter.getRoleIds(), hasSize(2));
        assertThat(filter.getRoleIds(), hasItems(SAMEPLE_ROLEID1, SAMEPLE_ROLEID2));
    }

    @Test
    public void testGetFilterEnabledWithGroupsAndRoles() throws Exception
    {
        mockFilter(true, ImmutableSet.of(SAMEPLE_GROUP1, SAMEPLE_GROUP2), ImmutableSet.<Long>of(SAMEPLE_ROLEID1, SAMEPLE_ROLEID2));
        final UserFilter filter = defaultUserFilterManager.getFilter(fieldConfig);
        assertThat(filter.isEnabled(), is(true));
        assertThat(filter.getGroups(), hasSize(2));
        assertThat(filter.getGroups(), hasItems(SAMEPLE_GROUP1, SAMEPLE_GROUP2));
        assertThat(filter.getRoleIds(), hasSize(2));
        assertThat(filter.getRoleIds(), hasItems(SAMEPLE_ROLEID1, SAMEPLE_ROLEID2));
    }

    @Test
    public void testUpdateFilterNoExistingDisabled() throws Exception
    {
        defaultUserFilterManager.updateFilter(fieldConfig, UserFilter.DISABLED);
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CONFIG_ID, SAMPLE_FIELDCONFIG_ID);
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void testUpdateFilterNoExistingEnabled() throws Exception
    {
        when(delegator.createValue(eq(TABLE_USERPICKER_FILTER), argThat(new CreateValueMapMatcher()))).thenReturn(mock(GenericValue.class));
        defaultUserFilterManager.updateFilter(fieldConfig, UserFilter.ENABLED_NO_USERS);
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CONFIG_ID, SAMPLE_FIELDCONFIG_ID);
        verify(delegator).createValue(eq(TABLE_USERPICKER_FILTER), argThat(new CreateValueMapMatcher()));
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void testUpdateFilterExistingDisabledNewDisabled() throws Exception
    {
        mockUpdateFilter(false);
        defaultUserFilterManager.updateFilter(fieldConfig, UserFilter.DISABLED);
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CONFIG_ID, SAMPLE_FIELDCONFIG_ID);
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void testUpdateFilterExistingDisabledNewEnabled() throws Exception
    {
        mockUpdateFilter(false);
        defaultUserFilterManager.updateFilter(fieldConfig, UserFilter.ENABLED_NO_USERS);
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CONFIG_ID, SAMPLE_FIELDCONFIG_ID);
        verifyNoMoreInteractions(delegator);
        verify(filterGV).getLong(Entity.ID);
        verify(filterGV).getBoolean(Entity.ENABLED);
        verify(filterGV).set(Entity.ENABLED, true);
        verify(filterGV).store();
        verifyNoMoreInteractions(filterGV);
    }

    @Test
    public void testUpdateFilterExistingEnabledNewEnabled() throws Exception
    {
        mockUpdateFilter(true);
        defaultUserFilterManager.updateFilter(fieldConfig, UserFilter.ENABLED_NO_USERS);
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CONFIG_ID, SAMPLE_FIELDCONFIG_ID);
        verify(delegator).removeByAnd(TABLE_USERPICKER_FILTER_GROUP, ImmutableMap.<String, Object>of(GroupEntity.FILTER_ID, SAMPLE_FILTER_ID));
        verify(delegator).removeByAnd(TABLE_USERPICKER_FILTER_ROLE, ImmutableMap.<String, Object>of(RoleEntity.FILTER_ID, SAMPLE_FILTER_ID));
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void testUpdateFilterExistingEnabledNewEnabledGroups() throws Exception
    {
        mockUpdateFilter(true);
        defaultUserFilterManager.updateFilter(fieldConfig, new UserFilter(true, null, ImmutableSet.<String>of(SAMEPLE_GROUP1, SAMEPLE_GROUP2)));
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CONFIG_ID, SAMPLE_FIELDCONFIG_ID);
        verify(delegator).removeByAnd(TABLE_USERPICKER_FILTER_GROUP, ImmutableMap.<String, Object>of(GroupEntity.FILTER_ID, SAMPLE_FILTER_ID));
        verify(delegator).removeByAnd(TABLE_USERPICKER_FILTER_ROLE, ImmutableMap.<String, Object>of(RoleEntity.FILTER_ID, SAMPLE_FILTER_ID));
        verify(delegator).createValue(TABLE_USERPICKER_FILTER_GROUP, ImmutableMap.<String, Object>of(
                GroupEntity.FILTER_ID, SAMPLE_FILTER_ID,
                GroupEntity.GROUP_NAME, SAMEPLE_GROUP1));
        verify(delegator).createValue(TABLE_USERPICKER_FILTER_GROUP, ImmutableMap.<String, Object>of(
                GroupEntity.FILTER_ID, SAMPLE_FILTER_ID,
                GroupEntity.GROUP_NAME, SAMEPLE_GROUP2));
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void testUpdateFilterExistingEnabledNewEnabledRoles() throws Exception
    {
        mockUpdateFilter(true);
        defaultUserFilterManager.updateFilter(fieldConfig, new UserFilter(true, ImmutableSet.<Long>of(SAMEPLE_ROLEID1, SAMEPLE_ROLEID2), null));
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CONFIG_ID, SAMPLE_FIELDCONFIG_ID);
        verify(delegator).removeByAnd(TABLE_USERPICKER_FILTER_GROUP, ImmutableMap.<String, Object>of(GroupEntity.FILTER_ID, SAMPLE_FILTER_ID));
        verify(delegator).removeByAnd(TABLE_USERPICKER_FILTER_ROLE, ImmutableMap.<String, Object>of(RoleEntity.FILTER_ID, SAMPLE_FILTER_ID));
        verify(delegator).createValue(TABLE_USERPICKER_FILTER_ROLE, ImmutableMap.<String, Object>of(
                RoleEntity.FILTER_ID, SAMPLE_FILTER_ID,
                RoleEntity.ROLE_ID, SAMEPLE_ROLEID1));
        verify(delegator).createValue(TABLE_USERPICKER_FILTER_ROLE, ImmutableMap.<String, Object>of(
                RoleEntity.FILTER_ID, SAMPLE_FILTER_ID,
                RoleEntity.ROLE_ID, SAMEPLE_ROLEID2));
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void testUpdateFilterExistingEnabledNewEnabledGroupsAndRoles() throws Exception
    {
        mockUpdateFilter(true);
        defaultUserFilterManager.updateFilter(fieldConfig,
                new UserFilter(true, ImmutableSet.<Long>of(SAMEPLE_ROLEID1, SAMEPLE_ROLEID2),
                                     ImmutableSet.<String>of(SAMEPLE_GROUP1, SAMEPLE_GROUP2)));
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CONFIG_ID, SAMPLE_FIELDCONFIG_ID);
        verify(delegator).removeByAnd(TABLE_USERPICKER_FILTER_GROUP, ImmutableMap.<String, Object>of(GroupEntity.FILTER_ID, SAMPLE_FILTER_ID));
        verify(delegator).removeByAnd(TABLE_USERPICKER_FILTER_ROLE, ImmutableMap.<String, Object>of(RoleEntity.FILTER_ID, SAMPLE_FILTER_ID));
        verify(delegator).createValue(TABLE_USERPICKER_FILTER_GROUP, ImmutableMap.<String, Object>of(
                GroupEntity.FILTER_ID, SAMPLE_FILTER_ID,
                GroupEntity.GROUP_NAME, SAMEPLE_GROUP1));
        verify(delegator).createValue(TABLE_USERPICKER_FILTER_GROUP, ImmutableMap.<String, Object>of(
                GroupEntity.FILTER_ID, SAMPLE_FILTER_ID,
                GroupEntity.GROUP_NAME, SAMEPLE_GROUP2));
        verify(delegator).createValue(TABLE_USERPICKER_FILTER_ROLE, ImmutableMap.<String, Object>of(
                RoleEntity.FILTER_ID, SAMPLE_FILTER_ID,
                RoleEntity.ROLE_ID, SAMEPLE_ROLEID1));
        verify(delegator).createValue(TABLE_USERPICKER_FILTER_ROLE, ImmutableMap.<String, Object>of(
                RoleEntity.FILTER_ID, SAMPLE_FILTER_ID,
                RoleEntity.ROLE_ID, SAMEPLE_ROLEID2));
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void testUpdateFilterExistingEnabledNewDisabledGroupsAndRoles() throws Exception
    {
        mockUpdateFilter(false);
        defaultUserFilterManager.updateFilter(fieldConfig,
                new UserFilter(true, ImmutableSet.<Long>of(SAMEPLE_ROLEID1, SAMEPLE_ROLEID2),
                        ImmutableSet.<String>of(SAMEPLE_GROUP1, SAMEPLE_GROUP2)));
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CONFIG_ID, SAMPLE_FIELDCONFIG_ID);
        verify(delegator).createValue(TABLE_USERPICKER_FILTER_GROUP, ImmutableMap.<String, Object>of(
                GroupEntity.FILTER_ID, SAMPLE_FILTER_ID,
                GroupEntity.GROUP_NAME, SAMEPLE_GROUP1));
        verify(delegator).createValue(TABLE_USERPICKER_FILTER_GROUP, ImmutableMap.<String, Object>of(
                GroupEntity.FILTER_ID, SAMPLE_FILTER_ID,
                GroupEntity.GROUP_NAME, SAMEPLE_GROUP2));
        verify(delegator).createValue(TABLE_USERPICKER_FILTER_ROLE, ImmutableMap.<String, Object>of(
                RoleEntity.FILTER_ID, SAMPLE_FILTER_ID,
                RoleEntity.ROLE_ID, SAMEPLE_ROLEID1));
        verify(delegator).createValue(TABLE_USERPICKER_FILTER_ROLE, ImmutableMap.<String, Object>of(
                RoleEntity.FILTER_ID, SAMPLE_FILTER_ID,
                RoleEntity.ROLE_ID, SAMEPLE_ROLEID2));
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void testRemoveFilterNullInput() throws Exception
    {
        defaultUserFilterManager.removeFilter(null);
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void testRemoveFilterNoData() throws Exception
    {
        when(delegator.findByField(TABLE_USERPICKER_FILTER, Entity.CUSTOMFIELD_ID, SAMPLE_CUSTOMFILED_ID_LONG))
                .thenReturn(ImmutableList.<GenericValue>of());
        defaultUserFilterManager.removeFilter(SAMPLE_CUSTOMFILED_ID_LONG);
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CUSTOMFIELD_ID, SAMPLE_CUSTOMFILED_ID_LONG);
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void testRemoveFilterSingleFilterButNullId() throws Exception
    {
        reset(filterGV);
        when(filterGV.getLong(Entity.ID)).thenReturn(null);
        when(delegator.findByField(TABLE_USERPICKER_FILTER, Entity.CUSTOMFIELD_ID, SAMPLE_CUSTOMFILED_ID_LONG))
                .thenReturn(ImmutableList.<GenericValue>of(filterGV));
        defaultUserFilterManager.removeFilter(SAMPLE_CUSTOMFILED_ID_LONG);
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CUSTOMFIELD_ID, SAMPLE_CUSTOMFILED_ID_LONG);
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void testRemoveFilterSingleFilter() throws Exception
    {
        reset(filterGV);
        when(filterGV.getLong(Entity.ID)).thenReturn(SAMPLE_FILTER_ID);
        when(delegator.findByField(TABLE_USERPICKER_FILTER, Entity.CUSTOMFIELD_ID, SAMPLE_CUSTOMFILED_ID_LONG))
                .thenReturn(ImmutableList.<GenericValue>of(filterGV));
        defaultUserFilterManager.removeFilter(SAMPLE_CUSTOMFILED_ID_LONG);
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CUSTOMFIELD_ID, SAMPLE_CUSTOMFILED_ID_LONG);
        verify(delegator).removeByCondition(eq(TABLE_USERPICKER_FILTER_GROUP),
                eqEntityExpr(new EntityExpr(GroupEntity.FILTER_ID, IN, ImmutableList.of(SAMPLE_FILTER_ID))));
        verify(delegator).removeByCondition(eq(TABLE_USERPICKER_FILTER_ROLE),
                eqEntityExpr(new EntityExpr(RoleEntity.FILTER_ID, IN, ImmutableList.of(SAMPLE_FILTER_ID))));
        verify(delegator).removeAll(ImmutableList.of(filterGV));
        verifyNoMoreInteractions(delegator);
    }

    @Test
    public void testRemoveFilterMultipleFilter() throws Exception
    {
        reset(filterGV);
        when(filterGV.getLong(Entity.ID)).thenReturn(SAMPLE_FILTER_ID);
        GenericValue gv1 = mock(GenericValue.class);
        GenericValue gv2 = mock(GenericValue.class);
        when(gv1.getLong(Entity.ID)).thenReturn(SAMPLE_FILTER_ID + 1);
        when(gv2.getLong(Entity.ID)).thenReturn(SAMPLE_FILTER_ID + 2);
        when(delegator.findByField(TABLE_USERPICKER_FILTER, Entity.CUSTOMFIELD_ID, SAMPLE_CUSTOMFILED_ID_LONG))
                .thenReturn(ImmutableList.<GenericValue>of(filterGV, gv1, gv2));
        defaultUserFilterManager.removeFilter(SAMPLE_CUSTOMFILED_ID_LONG);
        verify(delegator).findByField(TABLE_USERPICKER_FILTER, Entity.CUSTOMFIELD_ID, SAMPLE_CUSTOMFILED_ID_LONG);
        verify(delegator).removeByCondition(eq(TABLE_USERPICKER_FILTER_GROUP),
                eqEntityExpr(new EntityExpr(GroupEntity.FILTER_ID, IN,
                        ImmutableList.of(SAMPLE_FILTER_ID, SAMPLE_FILTER_ID + 1, SAMPLE_FILTER_ID + 2))));
        verify(delegator).removeByCondition(eq(TABLE_USERPICKER_FILTER_ROLE),
                eqEntityExpr(new EntityExpr(RoleEntity.FILTER_ID, IN,
                        ImmutableList.of(SAMPLE_FILTER_ID, SAMPLE_FILTER_ID + 1, SAMPLE_FILTER_ID + 2))));
        verify(delegator).removeAll(ImmutableList.of(filterGV, gv1, gv2));
        verifyNoMoreInteractions(delegator);
    }

    private void mockFilter(final Boolean enabled)
    {
        mockFilter(enabled, null, null);
    }

    private void mockFilter(final Boolean enabled, final Set<String> groups, final Set<Long> roleIds)
    {
        when(filterGV.getBoolean(Entity.ENABLED)).thenReturn(enabled);
        when(filterGV.getLong(Entity.ID)).thenReturn(SAMPLE_FILTER_ID);
        when(delegator.findByField(TABLE_USERPICKER_FILTER, Entity.CONFIG_ID, SAMPLE_FIELDCONFIG_ID))
                .thenReturn(ImmutableList.of(filterGV));
        if (groups != null)
        {
            final ImmutableList<GenericValue> gvList = ImmutableList.copyOf(Iterables.transform(groups, new Function<String, GenericValue>()
            {
                @Override
                public GenericValue apply(@Nullable final String group)
                {
                    return mockGroupGV(group);
                }
            }));
            when(delegator.findByField(TABLE_USERPICKER_FILTER_GROUP, GroupEntity.FILTER_ID, SAMPLE_FILTER_ID))
                    .thenReturn(gvList);
        }
        if (roleIds != null)
        {
            final ImmutableList<GenericValue> gvList = ImmutableList.copyOf(Iterables.transform(roleIds, new Function<Long, GenericValue>()
            {
                @Override
                public GenericValue apply(@Nullable final Long roleId)
                {
                    return mockRoleGV(roleId);
                }
            }));
            when(delegator.findByField(TABLE_USERPICKER_FILTER_ROLE, RoleEntity.FILTER_ID, SAMPLE_FILTER_ID))
                    .thenReturn(gvList);
        }
    }
    private GenericValue mockRoleGV(final Long roleId)
    {
        GenericValue gv = mock(GenericValue.class);
        when(gv.getLong(RoleEntity.ROLE_ID)).thenReturn(roleId);

        return gv;
    }

    private GenericValue mockGroupGV(final String group)
    {
        GenericValue gv = mock(GenericValue.class);
        when(gv.getString(GroupEntity.GROUP_NAME)).thenReturn(group);

        return gv;
    }

    private void mockUpdateFilter(final Boolean enabled)
    {
        when(filterGV.getBoolean(Entity.ENABLED)).thenReturn(enabled);
        when(filterGV.getLong(Entity.ID)).thenReturn(SAMPLE_FILTER_ID);
        when(delegator.findByField(TABLE_USERPICKER_FILTER, Entity.CONFIG_ID, SAMPLE_FIELDCONFIG_ID))
                .thenReturn(ImmutableList.of(filterGV));
    }

    class CreateValueMapMatcher extends TypeSafeMatcher<Map<String, Object>>
    {
        @Override
        protected boolean matchesSafely(final Map<String, Object> params)
        {
            return SAMPLE_FIELDCONFIG_ID.equals(params.get(Entity.CONFIG_ID))
                    && SAMPLE_CUSTOMFILED_ID_LONG.equals(params.get(Entity.CUSTOMFIELD_ID))
                    && Boolean.TRUE.equals(params.get(Entity.ENABLED));
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("matching: " + "fieldConfigId=" + SAMPLE_FIELDCONFIG_ID +
                    ", customFieldId=" + SAMEPL_CUSTOMFILED_ID +
                    ", enabled=" + Boolean.TRUE);
        }
    }

    private EntityExpr eqEntityExpr(final EntityExpr expected)
    {
        return argThat(new TypeSafeMatcher<EntityExpr>()
        {

            @Override
            protected boolean matchesSafely(final EntityExpr actual)
            {
                return (actual == expected) || (actual != null && expected != null &&
                                                actual.getLhs().equals(expected.getLhs()) &&
                                                actual.getOperator().equals(expected.getOperator()) &&
                        CollectionUtils.isEqualCollection((Collection)actual.getRhs(), (Collection)expected.getRhs()));
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendValue(expected);
            }
        });
    }
}
