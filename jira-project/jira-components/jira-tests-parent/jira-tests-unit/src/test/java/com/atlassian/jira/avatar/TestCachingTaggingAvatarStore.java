package com.atlassian.jira.avatar;

import java.io.IOException;
import java.util.Map;

import com.atlassian.cache.CacheManager;
import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.avatar.CachingTaggingAvatarStore.AVATAR_TYPE;
import static com.atlassian.jira.avatar.CachingTaggingAvatarStore.CONTENT_TYPE;
import static com.atlassian.jira.avatar.CachingTaggingAvatarStore.FILE_NAME;
import static com.atlassian.jira.avatar.CachingTaggingAvatarStore.ID;
import static com.atlassian.jira.avatar.CachingTaggingAvatarStore.OWNER;
import static com.atlassian.jira.avatar.CachingTaggingAvatarStore.SYSTEM_AVATAR;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestCachingTaggingAvatarStore
{
    @Mock
    private OfBizDelegator mockOfBizDelegator;
    @Mock
    private AvatarTagger mockAvatarTagger;

    private CacheManager cacheManager = new MemoryCacheManager();

    @Before
    public void setUp()
    {
        initMocks(this);
    }

    @Test
    public void testTaggedAvatarIsNotRetaggedOnRetrieval() throws IOException
    {
        GenericValue taggedAvatarGv = taggedAvatarGv();
        long id = taggedAvatarGv.getLong(ID);
        when(mockOfBizDelegator.findById("Avatar", id)).thenReturn(taggedAvatarGv);
        final String message = "Tagged avatars should not be re-tagged!";
        when(mockAvatarTagger.tagAvatar(anyLong(), anyString())).thenThrow(new RuntimeException(message));
        Avatar retrievedAvatar =
                new CachingTaggingAvatarStore(mockOfBizDelegator, mockAvatarTagger, cacheManager).getByIdTagged(id);
        assertEquals(id, (long) retrievedAvatar.getId());
        verifyZeroInteractions(mockAvatarTagger);
    }

    @Test
    public void testProjectAvatarIsNotTaggedOnRetrieval() throws IOException
    {
        ImmutableMap<String, Object> gvFields =
                ImmutableMap.<String, Object>builder().put(ID, 1030L)
                .put(OWNER, "bbenassi")
                .put(FILE_NAME, "1030_project.png")
                .put(CONTENT_TYPE, "img/png")
                .put(AVATAR_TYPE, "project")
                .put(SYSTEM_AVATAR, 0).build();

        GenericValue projectGv = new MockGenericValue("Avatar", gvFields);
        when(mockOfBizDelegator.findById("Avatar", 1030L)).thenReturn(projectGv);
        final String message = "Project Avatars should not be tagged!";
        when(mockAvatarTagger.tagAvatar(anyLong(), anyString())).thenThrow(new RuntimeException(message));
        Avatar retrievedAvatar =
                new CachingTaggingAvatarStore(mockOfBizDelegator, mockAvatarTagger, cacheManager).getByIdTagged(1030L);
        assertEquals(1030L, (long) retrievedAvatar.getId());
        verifyZeroInteractions(mockAvatarTagger);
    }

    @Test
    public void testSystemAvatarIsNotTaggedOnRetrieval() throws IOException
    {
        Map<String, Object> gvFields = ImmutableMap.<String, Object>builder()
                .put(ID, 1030L)
                .put(FILE_NAME, "1030_system.png")
                .put(CONTENT_TYPE, "img/png")
                .put(AVATAR_TYPE, "user")
                .put(SYSTEM_AVATAR, 1).build();

        GenericValue systemAvatarGv = new MockGenericValue("Avatar", gvFields);
        when(mockOfBizDelegator.findById("Avatar", 1030L)).thenReturn(systemAvatarGv);
        final String message = "System Avatars should not be tagged!";
        when(mockAvatarTagger.tagAvatar(anyLong(), anyString())).thenThrow(new RuntimeException(message));
        Avatar retrievedAvatar =
                new CachingTaggingAvatarStore(mockOfBizDelegator, mockAvatarTagger, cacheManager).getById(1030L);
        assertEquals(1030L, (long) retrievedAvatar.getId());
        verifyZeroInteractions(mockAvatarTagger);
    }

    @Test
    public void testAvatarIsTaggedWhenTaggingRequested() throws IOException
    {
        GenericValue untaggedAvatarGv = untaggedAvatarGv();
        final Long id = untaggedAvatarGv.getLong(ID);

        when(mockOfBizDelegator.findById("Avatar", id)).thenReturn(untaggedAvatarGv);
        when(mockAvatarTagger.tagAvatar(anyLong(), anyString())).thenReturn("1010_notTaggedjrvtg.png");
        final CachingTaggingAvatarStore cachingTaggingAvatarStore = new CachingTaggingAvatarStore(mockOfBizDelegator, mockAvatarTagger, cacheManager);

        Avatar retrievedAvatar = cachingTaggingAvatarStore.getById(1010L);
        assertEquals(id, retrievedAvatar.getId());
        verifyZeroInteractions(mockAvatarTagger);

        retrievedAvatar = cachingTaggingAvatarStore.getByIdTagged(1010L);
        assertEquals(id, retrievedAvatar.getId());
        verify(mockAvatarTagger, times(1)).tagAvatar(id, "1010_notTagged.png");
    }

    @Test
    public void testDelete() throws IOException
    {
        GenericValue untaggedAvatarGv = untaggedAvatarGv();
        final Long id = untaggedAvatarGv.getLong(ID);

        when(mockOfBizDelegator.findById("Avatar", id)).thenReturn(untaggedAvatarGv);
        when(mockAvatarTagger.tagAvatar(anyLong(), anyString())).thenReturn("1010_notTaggedjrvtg.png");
        when(mockOfBizDelegator.removeByAnd(anyString(), anyMapOf(String.class, Object.class))).thenReturn(1);
        CachingTaggingAvatarStore store = new CachingTaggingAvatarStore(mockOfBizDelegator, mockAvatarTagger, cacheManager);

        // Warm up the cache
        Avatar retrievedAvatar = store.getById(id);
        assertEquals(id, retrievedAvatar.getId());

        // Delete
        assertTrue(store.delete(id));

        // Finding by ID again should prompt the store to check the delegator but find nothing there
        when(mockOfBizDelegator.findById("Avatar", id)).thenReturn(null);
        assertNull(store.getById(id));
        verify(mockOfBizDelegator, times(2)).findById("Avatar", id);
    }

    @Test
    public void testUpdate() throws IOException
    {
        GenericValue untaggedAvatarGv = untaggedAvatarGv();
        final Long id = untaggedAvatarGv.getLong(ID);

        when(mockOfBizDelegator.findById("Avatar", id)).thenReturn(untaggedAvatarGv);
        when(mockAvatarTagger.tagAvatar(anyLong(), anyString())).thenReturn("1010_notTaggedjrvtg.png");

        CachingTaggingAvatarStore store = new CachingTaggingAvatarStore(mockOfBizDelegator, mockAvatarTagger, cacheManager);

        // Warm up the cache
        store.getById(id);

        final GenericValue taggedAvatarGv = taggedAvatarGv();
        final AvatarImpl avatar = avatarFromGv(taggedAvatarGv);

        store.update(avatar);
        when(mockOfBizDelegator.findById("Avatar", id)).thenReturn(taggedAvatarGv);

        // We should get a matching avatar and a cleared cache
        assertEquals(avatar, store.getById(id));

        // Delegator should be used once when we warm up the cache, once on update,
        // and then a third time, after we've cleared the cache due to an update
        verify(mockOfBizDelegator, times(3)).findById("Avatar", id);
    }

    private static AvatarImpl avatarFromGv(GenericValue taggedAvatarGv)
    {
        return new AvatarImpl(taggedAvatarGv.getLong(ID),
                    taggedAvatarGv.getString(FILE_NAME),
                    taggedAvatarGv.getString(CONTENT_TYPE),
                    Avatar.Type.getByName(taggedAvatarGv.getString(AVATAR_TYPE)),
                    taggedAvatarGv.getString(OWNER),
                    taggedAvatarGv.getInteger(SYSTEM_AVATAR) == 1);
    }

    @Test
    public void testCreate()
    {
        final GenericValue taggedAvatarGv = taggedAvatarGv();
        long id = taggedAvatarGv.getLong(ID);
        CachingTaggingAvatarStore store = new CachingTaggingAvatarStore(mockOfBizDelegator, mockAvatarTagger, cacheManager);

        when(mockOfBizDelegator.findById("Avatar", id)).thenReturn(null);
        when(mockOfBizDelegator.createValue(eq("Avatar"), anyMapOf(String.class, Object.class))).thenReturn(taggedAvatarGv);

        //cache miss
        assertNull(store.getById(id));

        //create
        taggedAvatarGv.set(ID, null);
        Avatar newAvatar = avatarFromGv(taggedAvatarGv);
        taggedAvatarGv.set(ID, id);
        Avatar createdAvatar = store.create(newAvatar);

        assertEquals(avatarFromGv(taggedAvatarGv), createdAvatar);
    }

    private static GenericValue taggedAvatarGv()
    {
        Map<String, Object> gvFields = ImmutableMap.<String, Object>builder()
                .put(ID, 1010L)
                .put(OWNER, "bbenassi")
                .put(FILE_NAME, "1010_taggedjrvtg.png")
                .put(CONTENT_TYPE, "img/png")
                .put(AVATAR_TYPE, "user")
                .put(SYSTEM_AVATAR, 0).build();

        return new MockGenericValue("Avatar", gvFields);
    }

    private static GenericValue untaggedAvatarGv()
    {
        Map<String, Object> gvFields = ImmutableMap.<String, Object>builder()
                .put(ID, 1010L)
                .put(OWNER, "bbenassi")
                .put(FILE_NAME, "1010_notTagged.png")
                .put(CONTENT_TYPE, "img/png")
                .put(AVATAR_TYPE, "user")
                .put(SYSTEM_AVATAR, 0).build();

        return new MockGenericValue("Avatar", gvFields);
    }
}
