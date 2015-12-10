package com.atlassian.jira.upgrade.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.CachingTaggingAvatarStore;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UpgradeTask_Build6322Test
{
    public static final long STARTING_ID = MockOfBizDelegator.STARTING_ID;

    public static final HashSet<Long> EXPECTED_IDS = Sets.newHashSet(
            Ranges.closed(
                    STARTING_ID,
                    STARTING_ID + UpgradeTask_Build6322.ALL_ISSUE_TYPE_ICONS.size() - 1
            ).asSet(DiscreteDomains.longs())
    );
    public static final long NON_SYSTEM_ID = 30000l;
    public static final String SYSTEM_ICON_0 = UpgradeTask_Build6322.DEFAULT_AVATAR_FILENAME;

    public static final ImmutableMap<String, Object> NON_SYSTEM_ENTITY = ImmutableMap.<String, Object>builder().
            put(CachingTaggingAvatarStore.ID, NON_SYSTEM_ID).
            put(CachingTaggingAvatarStore.FILE_NAME, SYSTEM_ICON_0).
            put(CachingTaggingAvatarStore.CONTENT_TYPE, "image/png").
            put(CachingTaggingAvatarStore.AVATAR_TYPE, Avatar.Type.ISSUETYPE.getName()).
            put(CachingTaggingAvatarStore.SYSTEM_AVATAR, 0).build();

    public static final ImmutableMap<String, Object> DUPLICATED_ENTITY = ImmutableMap.<String, Object>builder().
            put(CachingTaggingAvatarStore.FILE_NAME, SYSTEM_ICON_0).
            put(CachingTaggingAvatarStore.CONTENT_TYPE, "image/png").
            put(CachingTaggingAvatarStore.AVATAR_TYPE, Avatar.Type.ISSUETYPE.getName()).
            put(CachingTaggingAvatarStore.SYSTEM_AVATAR, 1).build();

    static class HasValueFromCollection<T> extends BaseMatcher<T>
    {
        final Collection<T> usedItemsCollection;
        final Collection<T> expectedSet;

        public HasValueFromCollection(final Collection<T> expectedSet)
        {
            this.expectedSet = expectedSet;
            this.usedItemsCollection = Lists.newArrayList(expectedSet);
        }

        public Collection<T> getUnusedItems()
        {
            return usedItemsCollection;
        }

        @Override
        public boolean matches(final Object item)
        {
            if (!usedItemsCollection.remove(item))
            {
                return false;
            }

            return true;
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("has coresponding value in: " + expectedSet);
        }
    }

    static class MatchesIssueTypeSystemAvatar extends BaseMatcher<GenericValue>
    {
        MatchesIssueTypeSystemAvatar()
        {
        }

        @Override
        public boolean matches(final Object o)
        {
            if (!(o instanceof GenericValue))
            {
                return false;
            }
            final GenericValue value = (GenericValue) o;
            if (!Avatar.Type.ISSUETYPE.getName().equals(value.get(CachingTaggingAvatarStore.AVATAR_TYPE)))
            {
                return false;
            }
            if (1 != value.getInteger(CachingTaggingAvatarStore.SYSTEM_AVATAR))
            {
                return false;
            }
            if (null != value.get(CachingTaggingAvatarStore.OWNER))
            {
                return false;
            }
            if (!"image/png".equals(value.get(CachingTaggingAvatarStore.CONTENT_TYPE)))
            {
                return false;
            }
            return true;
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("conformant to system issue type avatar entity");
        }
    }

    public static final Function<GenericValue, Long> EXTRACT_ITEM_ID = new Function<GenericValue, Long>()
    {
        @Override
        public Long apply(@Nullable final GenericValue input)
        {
            return input.getLong(CachingTaggingAvatarStore.ID);
        }
    };
    public static final Function<GenericValue, String> EXTRACT_ITEM_FILENAME = new Function<GenericValue, String>()
    {
        @Override
        public String apply(@Nullable final GenericValue input)
        {
            return input.getString(CachingTaggingAvatarStore.FILE_NAME);
        }
    };

    @Test
    public void shouldAddAllIconEntriesToEmptyDatabase() throws Exception
    {
        // given
        final MockOfBizDelegator delegator = new MockOfBizDelegator(
                ImmutableList.<MockGenericValue>of(),
                ImmutableList.<MockGenericValue>of());

        ApplicationProperties applicationProperties = Mockito.mock(ApplicationProperties.class);
        final UpgradeTask_Build6322 testObj = new UpgradeTask_Build6322(delegator, applicationProperties);

        // when
        testObj.doUpgrade(false);

        // then
        final List<GenericValue> allValues = delegator.findAll(CachingTaggingAvatarStore.AVATAR_ENTITY);

        assertGivenIconsInSystemList(allValues);
    }

    @Test
    public void shouldAddOnlyNonExistingIconsToPrepopulatedDatabase() throws Exception
    {
        // given
        final MockOfBizDelegator delegator = new MockOfBizDelegator(
                ImmutableList.<GenericValue>of(),
                ImmutableList.<MockGenericValue>of());
        // i have to use this particular createValue to update nextId field
        delegator.createValue(CachingTaggingAvatarStore.AVATAR_ENTITY, DUPLICATED_ENTITY);

        ApplicationProperties applicationProperties = Mockito.mock(ApplicationProperties.class);
        final UpgradeTask_Build6322 testObj = new UpgradeTask_Build6322(delegator, applicationProperties);

        // when
        testObj.doUpgrade(false);

        // then
        final List<GenericValue> allValues = delegator.findAll(CachingTaggingAvatarStore.AVATAR_ENTITY);

        assertGivenIconsInSystemList(allValues);
    }

    @Test
    public void shouldIgnoreNonSystemIconsWithTheSameNameAsSystem() throws Exception
    {
        MockGenericValue mockEntity = new MockGenericValue(CachingTaggingAvatarStore.AVATAR_ENTITY, NON_SYSTEM_ENTITY);

        // given
        final MockOfBizDelegator delegator = new MockOfBizDelegator(
                ImmutableList.of(mockEntity),
                ImmutableList.<MockGenericValue>of());

        ApplicationProperties applicationProperties = Mockito.mock(ApplicationProperties.class);
        final UpgradeTask_Build6322 testObj = new UpgradeTask_Build6322(delegator, applicationProperties);

        // when
        testObj.doUpgrade(false);

        // then
        final List<GenericValue> allValues = delegator.findAll(CachingTaggingAvatarStore.AVATAR_ENTITY);

        final ArrayList<Long> idsWithNonSystemOne = Lists.newArrayList(EXPECTED_IDS);
        idsWithNonSystemOne.add(NON_SYSTEM_ID);
        final HasValueFromCollection<Long> hasUniqueIdInRange = new HasValueFromCollection<Long>(idsWithNonSystemOne);

        final ArrayList<String> iconsWithNonSystemOne = Lists.newArrayList(UpgradeTask_Build6322.ALL_ISSUE_TYPE_ICONS);
        iconsWithNonSystemOne.add(SYSTEM_ICON_0);
        final HasValueFromCollection<String> hasIconFromSet = new HasValueFromCollection<String>(iconsWithNonSystemOne);

        final Collection<Long> itemIds = Collections2.transform(allValues, EXTRACT_ITEM_ID);
        assertThat(itemIds, everyItem(hasUniqueIdInRange));
        assertThat(hasUniqueIdInRange.getUnusedItems(), is(Matchers.<Long>empty()));

        final Collection<String> itemIcons = Collections2.transform(allValues, EXTRACT_ITEM_FILENAME);
        assertThat(itemIcons, everyItem(hasIconFromSet));
        assertThat(hasIconFromSet.getUnusedItems(), is(Matchers.<String>empty()));
    }

    @Test
    public void shouldSetDefaultIssueTypeIconId() throws Exception
    {

        // given
        final MockOfBizDelegator delegator = new MockOfBizDelegator(
                ImmutableList.<GenericValue>of(),
                ImmutableList.<MockGenericValue>of());

        ApplicationProperties applicationProperties = Mockito.mock(ApplicationProperties.class);
        final UpgradeTask_Build6322 testObj = new UpgradeTask_Build6322(delegator, applicationProperties);

        // when
        testObj.doUpgrade(false);

        // then
        Mockito.verify(applicationProperties).setString(
                APKeys.JIRA_DEFAULT_ISSUETYPE_AVATAR_ID,
                String.valueOf(STARTING_ID));
        Mockito.verify(applicationProperties).setString(
                APKeys.JIRA_DEFAULT_ISSUETYPE_SUBTASK_AVATAR_ID,
                String.valueOf(STARTING_ID+16l));
    }

    private void assertGivenIconsInSystemList(final List<GenericValue> allValues)
    {
        final HasValueFromCollection<Long> hasUniqueIdInRange = new HasValueFromCollection<Long>(EXPECTED_IDS);
        final HasValueFromCollection<String> hasIconFromSet = new HasValueFromCollection<String>(UpgradeTask_Build6322.ALL_ISSUE_TYPE_ICONS);

        assertThat(allValues, everyItem(new MatchesIssueTypeSystemAvatar()));

        final Collection<Long> itemIds = Collections2.transform(allValues, EXTRACT_ITEM_ID);
        assertThat(itemIds, everyItem(hasUniqueIdInRange));
        assertThat(hasUniqueIdInRange.getUnusedItems(), is(Matchers.<Long>empty()));

        final Collection<String> itemIcons = Collections2.transform(allValues, EXTRACT_ITEM_FILENAME);
        assertThat(itemIcons, everyItem(hasIconFromSet));
        assertThat(hasIconFromSet.getUnusedItems(), is(Matchers.<String>empty()));
    }
}
