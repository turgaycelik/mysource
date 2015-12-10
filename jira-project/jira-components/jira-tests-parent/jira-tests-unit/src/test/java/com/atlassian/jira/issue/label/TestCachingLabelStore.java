package com.atlassian.jira.issue.label;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.event.ClearCacheEvent;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.2
 */
public class TestCachingLabelStore
{
    private final static long ISSUE_ID = 10000L;
    private final Label LABEL_BAR = new Label(1L, ISSUE_ID, null, "bar");
    private final Label LABEL_FOO = new Label(2L, ISSUE_ID, null, "foo");

    private OfBizLabelStore mockStore;
    private CachingLabelStore cachingStore;

    @Before
    public void setUp() throws Exception
    {
        mockStore = createMock(OfBizLabelStore.class);
        cachingStore = new CachingLabelStore(mockStore, new MemoryCacheManager());
    }

    @Test
    public void testGetLabelsReadsCachedValue()
    {
        expect(mockStore.getLabels(ISSUE_ID, null))
                .andReturn(Collections.<Label>emptySet());
        replay(mockStore);

        assertEquals(Collections.<Label>emptySet(), cachingStore.getLabels(ISSUE_ID, null));
        // Cache hit.
        assertEquals(Collections.<Label>emptySet(), cachingStore.getLabels(ISSUE_ID, null));
        verify(mockStore);
    }

    @Test
    public void testSetLabelsModifiesStore()
    {
        final Set<Label> labels = new LinkedHashSet<Label>(Arrays.asList(LABEL_BAR, LABEL_FOO));

        expect(mockStore.getLabels(ISSUE_ID, null))
                .andReturn(Collections.<Label>emptySet());
        expect(mockStore.setLabels(ISSUE_ID, null, new HashSet<String>(Arrays.asList(LABEL_BAR.getLabel(), LABEL_FOO.getLabel()))))
                .andReturn(labels);
        expect(mockStore.getLabels(ISSUE_ID, null))
                .andReturn(labels);
        replay(mockStore);

        // Prime cache.
        assertEquals(Collections.<Label>emptySet(), cachingStore.getLabels(ISSUE_ID, null));
        assertEquals(labels, cachingStore.setLabels(ISSUE_ID, null, new TreeSet<String>(Arrays.asList(LABEL_BAR.getLabel(), LABEL_FOO.getLabel()))));
        // Read invalidated value, should load from backing store.
        assertEquals(labels, cachingStore.getLabels(ISSUE_ID, null));
        // Read cached value
        assertEquals(labels, cachingStore.getLabels(ISSUE_ID, null));
        verify(mockStore);
    }

    @Test
    public void testAddLabelModifiesStoreAndInvalidatesCache()
    {
        expect(mockStore.getLabels(ISSUE_ID, null))
                .andReturn(Collections.<Label>emptySet());
        expect(mockStore.addLabel(ISSUE_ID, null, LABEL_BAR.getLabel()))
                .andReturn(LABEL_BAR);
        expect(mockStore.getLabels(ISSUE_ID, null))
                .andReturn(Collections.singleton(LABEL_BAR));
        replay(mockStore);

        // Prime cache.
        assertEquals(Collections.<Label>emptySet(), cachingStore.getLabels(ISSUE_ID, null));
        assertEquals(LABEL_BAR, cachingStore.addLabel(ISSUE_ID, null, LABEL_BAR.getLabel()));
        // Cache miss.
        assertEquals(Collections.singleton(LABEL_BAR), cachingStore.getLabels(ISSUE_ID, null));
        verify(mockStore);
    }

    @Test
    public void testRemoveLabelModifiesStoreAndInvalidesCache()
    {
        expect(mockStore.getLabels(ISSUE_ID, null))
                .andReturn(Collections.singleton(LABEL_BAR));
        mockStore.removeLabel(LABEL_BAR.getId(), ISSUE_ID, null);
        expect(mockStore.getLabels(ISSUE_ID, null))
                .andReturn(Collections.<Label>emptySet());
        replay(mockStore);

        // Prime cache.
        assertEquals(Collections.singleton(LABEL_BAR), cachingStore.getLabels(ISSUE_ID, null));
        cachingStore.removeLabel(LABEL_BAR.getId(), ISSUE_ID, null);
        // Cache miss.
        assertEquals(Collections.<Label>emptySet(), cachingStore.getLabels(ISSUE_ID, null));
        verify(mockStore);
    }

    @Test
    public void testRespondsToClearCacheEvent()
    {
        expect(mockStore.getLabels(ISSUE_ID, null))
                .andReturn(Collections.<Label>emptySet())
                .times(2);
        replay(mockStore);

        assertEquals(Collections.<Label>emptySet(), cachingStore.getLabels(ISSUE_ID, null));
        cachingStore.onClearCache(ClearCacheEvent.INSTANCE);
        // Cache miss.
        assertEquals(Collections.<Label>emptySet(), cachingStore.getLabels(ISSUE_ID, null));
        verify(mockStore);
    }
}
