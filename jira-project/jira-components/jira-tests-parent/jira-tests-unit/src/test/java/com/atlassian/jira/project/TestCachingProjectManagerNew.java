package com.atlassian.jira.project;

import java.util.Collection;

import com.atlassian.cache.memory.MemoryCacheManager;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.junit.rules.MockComponentContainer;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.util.ProjectKeyStore;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestCachingProjectManagerNew
{
    @Rule
    public MockComponentContainer container = new MockComponentContainer(this);
    @Mock
    private NodeAssociationStore nodeAssociationStore;

    @Test
    public void testGetAllProjectKeys()
    {
        OfBizDelegator mockDelegator = mock(OfBizDelegator.class);
        container.addMock(OfBizDelegator.class, mockDelegator);

        ProjectKeyStore mockKeyStore = mock(ProjectKeyStore.class);
        when(mockKeyStore.getProjectKeys(11l)).thenReturn(
                ImmutableSet.of("ABC", "OLDKEY")
        );

        MockProjectManager mockProjectManager = new MockProjectManager();
        mockProjectManager.addProject(new MockGenericValue("Project", ImmutableMap.of("id", 11l, "name", "Abc", "key", "ABC")));
        mockProjectManager.addProject(new MockGenericValue("Project", ImmutableMap.of("id", 13l, "name", "Alphabet", "key", "ABCDEF")));

        CachingProjectManager cpm = new CachingProjectManager(mockProjectManager, null, null, null, null, mockKeyStore, new MemoryCacheManager(), nodeAssociationStore);
        Collection<String> projectKeys = cpm.getAllProjectKeys(11l);
        assertThat(projectKeys, IsCollectionWithSize.hasSize(2));
        assertThat(projectKeys, IsCollectionContaining.hasItems("ABC", "OLDKEY"));
    }
}
