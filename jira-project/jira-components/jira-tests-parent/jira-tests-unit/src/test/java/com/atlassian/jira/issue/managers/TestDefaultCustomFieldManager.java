package com.atlassian.jira.issue.managers;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheLoader;
import com.atlassian.cache.CacheManager;
import com.atlassian.cache.CacheSettings;
import com.atlassian.cache.CachedReference;
import com.atlassian.cache.Supplier;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.CustomFieldFactory;
import com.atlassian.jira.issue.fields.CustomFieldImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.map.CacheObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultCustomFieldManager
{
    @Mock
    private ProjectManager projectManager;
    @Mock
    private ConstantsManager constantsManager;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private CustomFieldFactory customFieldFactory;
    @Mock
    private CachedReference<List<Long>> allCustomFieldIds;
    @Mock
    private Cache<Long, CacheObject<CustomFieldImpl>> customFieldsById;

    private DefaultCustomFieldManager manager;

    @Before
    public void setUp()
    {
        mockCacheManagerToReturn(allCustomFieldIds);
        mockCacheManagerToReturn(customFieldsById);

        manager = createCustomFieldManager(constantsManager, projectManager, cacheManager, customFieldFactory);
    }

    @Test
    public void getCustomFieldObjectsHandlesAnyProjectAndAnyIssueTypesCorrectly()
    {
        Long projectId = 1L;
        Project project = mock(Project.class);
        when(projectManager.getProjectObj(projectId)).thenReturn(project);

        List<String> issueTypeIds = Arrays.asList("bug", "task");
        when(constantsManager.expandIssueTypeIds(issueTypeIds)).thenReturn(issueTypeIds);

        Long customFieldId = 2L;
        CustomFieldImpl customField = mock(CustomFieldImpl.class);
        when(allCustomFieldIds.get()).thenReturn(Arrays.asList(customFieldId));
        when(customFieldsById.get(customFieldId)).thenReturn(CacheObject.wrap(customField));
        when(customFieldFactory.copyOf(customField)).thenReturn(customField);

        manager.getCustomFieldObjects(projectId, issueTypeIds);

        verify(customField).isInScopeForSearch(project, issueTypeIds);
    }

    @SuppressWarnings ("unchecked")
    private void mockCacheManagerToReturn(CachedReference<List<Long>> allCustomFieldIds)
    {
        when(cacheManager.getCachedReference(any(Class.class), anyString(), any(Supplier.class))).thenReturn(allCustomFieldIds);
    }

    private void mockCacheManagerToReturn(Cache<Long, CacheObject<CustomFieldImpl>> customFieldsById)
    {
        when(cacheManager.getCache(anyString(), any(CacheLoader.class), any(CacheSettings.class))).thenReturn(customFieldsById);
    }

    private DefaultCustomFieldManager createCustomFieldManager(final ConstantsManager constantsManager, final ProjectManager projectManager, final CacheManager cacheManager, final CustomFieldFactory customFieldFactory)
    {
        return new DefaultCustomFieldManager(null, null, null, constantsManager, projectManager, null, null, null, null, null, mock(EventPublisher.class), cacheManager, customFieldFactory, null, null);
    }
}
