package com.atlassian.jira.sharing.index;

import java.util.Collection;
import java.util.Collections;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.MockResult;
import com.atlassian.jira.index.MultiThreadedIndexingConfiguration;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.sharing.IndexableSharedEntity;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.util.collect.MockCloseableIterable;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createNiceMock;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * A test case for DefaultSharedEntityIndexManager
 *
 * @since v3.13
 */
public class TestDefaultSharedEntityIndexManager extends MockControllerTestCase
{
    PortalPage portalPage1;
    SharedEntity indexableSearchRequest1;

    @Before
    public void setUp() throws Exception
    {
        ApplicationUser owner = new MockApplicationUser("ownername");
        portalPage1 = PortalPage.name("name").description("desc").owner(owner).build();
        indexableSearchRequest1 = new IndexableSharedEntity<SearchRequest>(1L, "name", "desc", SearchRequest.ENTITY_TYPE, owner, 0L);
    }

    private void recordMocksForReIndexAll()
    {
        final Index.Result mockResult = new MockResult();

        final ApplicationProperties applicationProperties = mockController.getNiceMock(ApplicationProperties.class);
        expect(applicationProperties.getDefaultBackedString(APKeys.JiraIndexConfiguration.SharedEntity.MIN_BATCH_SIZE)).andReturn("50");

        final PortalPageManager portalPageManager = getMock(PortalPageManager.class);
        expect(portalPageManager.getAllIndexableSharedEntities()).andReturn(
            new MockCloseableIterable<SharedEntity>(Collections.<SharedEntity> singletonList(portalPage1)));

        final SearchRequestManager searchRequestManager = getMock(SearchRequestManager.class);
        expect(searchRequestManager.getAllIndexableSharedEntities()).andReturn(
            new MockCloseableIterable<SharedEntity>(Collections.singletonList(indexableSearchRequest1)));

        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        indexer.recreate(SearchRequest.ENTITY_TYPE);
        expect(indexer.index(indexableSearchRequest1, false)).andReturn(mockResult);

        indexer.recreate(PortalPage.ENTITY_TYPE);
        expect(indexer.index(portalPage1, false)).andReturn(mockResult);
    }

    @Test
    public void testReIndexAll() throws IndexException
    {
        recordMocksForReIndexAll();

        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        final long time = sharedEntityIndexManager.reIndexAll(Contexts.nullContext());
        assertThat(time, greaterThanOrEqualTo(0L));
    }

    @Test
    public void testReIndexAllThrowsIllegalArgForNullEvent() throws IndexException
    {
        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        try
        {
            sharedEntityIndexManager.reIndexAll(null);
            fail("IllegalArg expected");
        }
        catch (final IllegalArgumentException ignore)
        {}
    }

    @Test
    public void testActivate() throws Exception
    {
        recordMocksForReIndexAll();

        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        sharedEntityIndexManager.activate(Contexts.nullContext());
    }

    @Test
    public void testActivateThrowsIllegalArgForNullEvent() throws Exception
    {
        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        try
        {
            sharedEntityIndexManager.activate(null);
            fail("IllegalArg expected");
        }
        catch (final IllegalArgumentException ignore)
        {}
    }

    @Test
    public void testShutdown()
    {
        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        indexer.shutdown(SearchRequest.ENTITY_TYPE);
        indexer.shutdown(PortalPage.ENTITY_TYPE);

        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        sharedEntityIndexManager.shutdown();
    }

    @Test
    public void testDeactivate() throws Exception
    {
        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        expect(indexer.clear(SearchRequest.ENTITY_TYPE)).andReturn("testing");

        expect(indexer.clear(PortalPage.ENTITY_TYPE)).andReturn("testing");

        addObjectInstance(createNiceMock(FileFactory.class));
        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        sharedEntityIndexManager.deactivate();
    }

    @Test
    public void testOptimize() throws Exception
    {
        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        expect(indexer.optimize(SearchRequest.ENTITY_TYPE)).andReturn(5L);
        expect(indexer.optimize(PortalPage.ENTITY_TYPE)).andReturn(2L);

        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        final long actualTime = sharedEntityIndexManager.optimize();
        assertThat(actualTime, is(7L));
    }

    @Test
    public void testIsIndexingEnabled()
    {
        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        assertThat(sharedEntityIndexManager.isIndexingEnabled(), is(true));
    }

    @Test
    public void testMultiThreadedConfigurationDefaults()
    {
        final MultiThreadedIndexingConfiguration multiThreadedIndexingConfiguration = new DefaultSharedEntityIndexManager.PropertiesAdapter(new MockApplicationProperties());
        assertThat(multiThreadedIndexingConfiguration.minimumBatchSize(), is(50));
        assertThat(multiThreadedIndexingConfiguration.maximumQueueSize(), is(1000));
        assertThat(multiThreadedIndexingConfiguration.noOfThreads(), is(10));
    }

    @Test
    public void testMultiThreadedConfigurationCustom()
    {
        final MockApplicationProperties applicationProperties = new MockApplicationProperties();
        applicationProperties.setString(APKeys.JiraIndexConfiguration.SharedEntity.MIN_BATCH_SIZE, "1");
        applicationProperties.setString(APKeys.JiraIndexConfiguration.SharedEntity.MAX_QUEUE_SIZE, "2");
        applicationProperties.setString(APKeys.JiraIndexConfiguration.SharedEntity.THREADS, "3");

        final MultiThreadedIndexingConfiguration multiThreadedIndexingConfiguration = new DefaultSharedEntityIndexManager.PropertiesAdapter(applicationProperties);
        assertThat(multiThreadedIndexingConfiguration.minimumBatchSize(), is(1));
        assertThat(multiThreadedIndexingConfiguration.maximumQueueSize(), is(2));
        assertThat(multiThreadedIndexingConfiguration.noOfThreads(), is(3));
    }

    @Test
    public void testGetAllIndexPaths()
    {
        final Collection<String> expectedList = Lists.newArrayList("This should be returned");
        final SharedEntityIndexer indexer = getMock(SharedEntityIndexer.class);
        expect(indexer.getAllIndexPaths()).andReturn(expectedList);

        final DefaultSharedEntityIndexManager sharedEntityIndexManager = instantiate(DefaultSharedEntityIndexManager.class);
        final Collection<String> actualList = sharedEntityIndexManager.getAllIndexPaths();
        assertThat(actualList, sameInstance(expectedList));
    }
}
