package com.atlassian.jira.issue.fields.screen;

import java.util.Collection;
import java.util.List;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.event.issue.field.screen.FieldScreenLayoutItemCreatedEvent;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.util.collect.CollectionBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(ListeningMockitoRunner.class)
public class TestCachingFieldScreenStore
{
    private static final FieldScreen ANT = newFieldScreen(1, "Ant");
    private static final FieldScreen BEE = newFieldScreen(2, "Bee");
    private static final FieldScreen CATERPILLAR = newFieldScreen(3, "Caterpillar");
    private static final FieldScreen MAGGOT = newFieldScreen(4, "Maggot");

    @Mock
    FieldScreenManager fieldScreenManager;
    private CacheManager cacheManager;

    @Before
    public void setup()
    {
        cacheManager = new MemoryCacheManager();
    }

    @Test
    public void testGetFieldScreens() throws Exception
    {
        final MockFieldScreenStore delegate = new MockFieldScreenStore();
        delegate.createFieldScreen(ANT);
        delegate.createFieldScreen(MAGGOT);
        delegate.createFieldScreen(CATERPILLAR);

        CachingFieldScreenStore cachingFieldScreenStore = new CachingFieldScreenStore(delegate, cacheManager);
        // Call refresh to init the cache.
        cachingFieldScreenStore.refresh();
        // FieldScreens should be ordered alphabetically.
        List<FieldScreen> expected = CollectionBuilder.list(ANT, CATERPILLAR, MAGGOT);
        assertEquals(expected, cachingFieldScreenStore.getFieldScreens());
        // Now add a new Screen
        cachingFieldScreenStore.createFieldScreen(BEE);
        // this should slot into the alphabetical order
        expected = CollectionBuilder.list(ANT, BEE, CATERPILLAR, MAGGOT);
        assertEquals(ImmutableSet.copyOf(expected), ImmutableSet.copyOf(cachingFieldScreenStore.getFieldScreens()));
    }

    @Test
    public void testGetFieldScreensImmutable()
    {
        final MockFieldScreenStore delegate = new MockFieldScreenStore();
        delegate.createFieldScreen(ANT);
        delegate.createFieldScreen(MAGGOT);

        CachingFieldScreenStore cachingFieldScreenStore = new CachingFieldScreenStore(delegate, cacheManager);
        // Call refresh to init the cache.
        cachingFieldScreenStore.refresh();

        // change the passed-in field screens and make sure the cached stuff is not affected
        delegate.setFieldScreens(singletonList(MAGGOT));
        assertThat("Cache must make a defensive copy of the passed-in list", cachingFieldScreenStore.getFieldScreens().size(), equalTo(2));

        // change the values obtained from the cache and make sure the cache is not affected
        List<FieldScreen> screenList = cachingFieldScreenStore.getFieldScreens();
        try
        {
            screenList.add(BEE);
        }
        catch (UnsupportedOperationException e)
        {
            // It should do this
        }
        assertThat("Cache must make a defensive copy of the returned list", cachingFieldScreenStore.getFieldScreens().size(), equalTo(2));
    }

    @Test
    public void testGetFieldScreensConcurrentModification()
    {
        final MockFieldScreenStore delegate = new MockFieldScreenStore();
        delegate.createFieldScreen(ANT);
        delegate.createFieldScreen(MAGGOT);

        CachingFieldScreenStore cachingFieldScreenStore = new CachingFieldScreenStore(delegate, cacheManager);
        // Call refresh to init the cache.
        cachingFieldScreenStore.refresh();
        // Get the List of screens.
        Collection<FieldScreen> screenList = cachingFieldScreenStore.getFieldScreens();
        // Now update the store
        cachingFieldScreenStore.createFieldScreen(BEE);
        // Try to iterate over the list - just want to make sure it doesn't throw a Concurrent Modification Exception
        for (FieldScreen fieldScreen : screenList)
        {
            fieldScreen.getId();
        }
    }

    @Test
    public void testRemove() throws Exception
    {
        final MockFieldScreenStore delegate = new MockFieldScreenStore();
        delegate.createFieldScreen(ANT);
        delegate.createFieldScreen(MAGGOT);

        CachingFieldScreenStore cachingFieldScreenStore = new CachingFieldScreenStore(delegate, cacheManager);
        // Call refresh to init the cache.
        cachingFieldScreenStore.refresh();
        List<FieldScreen> expected;
        expected = CollectionBuilder.list(ANT, MAGGOT);
        assertEquals(expected, cachingFieldScreenStore.getFieldScreens());
        // Remove ANT
        cachingFieldScreenStore.removeFieldScreen(ANT.getId());
        expected = CollectionBuilder.list(MAGGOT);
        assertEquals(expected, cachingFieldScreenStore.getFieldScreens());
    }

    @Test
    public void testUpdate() throws Exception
    {
        final MockFieldScreenStore delegate = new MockFieldScreenStore();
        delegate.createFieldScreen(ANT);
        delegate.createFieldScreen(MAGGOT);

        CachingFieldScreenStore cachingFieldScreenStore = new CachingFieldScreenStore(delegate, cacheManager);
        // Call refresh to init the cache.
        cachingFieldScreenStore.refresh();
        List<FieldScreen> expected;
        expected = CollectionBuilder.list(ANT, MAGGOT);
        assertEquals(expected, cachingFieldScreenStore.getFieldScreens());
        // Update the Maggot Screen
        FieldScreen maggie = newFieldScreen(4, "Maggie");

        cachingFieldScreenStore.updateFieldScreen(maggie);
        expected = CollectionBuilder.list(ANT, maggie);
        assertEquals(expected, cachingFieldScreenStore.getFieldScreens());
    }

    @Test
    public void testCacheShouldUpdateItselfBasedOnEvents() throws Exception
    {
        final FieldScreen antUpdatedScreen = newFieldScreen(ANT.getId(), "Ant updated");

        FieldScreenStore delegate = Mockito.mock(FieldScreenStore.class);
        when(delegate.getFieldScreens()).thenReturn(ImmutableList.of(ANT, MAGGOT));
        when(delegate.getFieldScreen(ANT.getId())).thenReturn(antUpdatedScreen);

        // the screen/tab that will need to be reloaded from DB
        MockFieldScreenTab fieldScreenTab = new MockFieldScreenTab();
        fieldScreenTab.setFieldScreen(ANT);

        MockFieldScreenLayoutItem createdFieldScreenLayoutItem = new MockFieldScreenLayoutItem();
        createdFieldScreenLayoutItem.setFieldScreenTab(fieldScreenTab);

        // dispatch the event and then make sure the screen was updated
        CachingFieldScreenStore cachingFieldScreenStore = new CachingFieldScreenStore(delegate, cacheManager);
        cachingFieldScreenStore.onFieldScreenLayoutChange(new FieldScreenLayoutItemCreatedEvent(createdFieldScreenLayoutItem));
        Assert.assertThat(cachingFieldScreenStore.getFieldScreen(ANT.getId()), equalTo(antUpdatedScreen));
    }

    private static FieldScreen newFieldScreen(long id, final String name)
    {
        final GenericValue gvFieldScreen = new MockGenericValue("FieldScreen");
        gvFieldScreen.set("id", id);
        gvFieldScreen.setString("name", name);
        gvFieldScreen.setString("description", name + " description");
        return new FieldScreenImpl(null, gvFieldScreen);
    }
}
