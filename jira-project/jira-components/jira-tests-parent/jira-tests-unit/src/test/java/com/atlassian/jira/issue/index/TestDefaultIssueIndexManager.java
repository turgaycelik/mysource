/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 * WARNING this is unit like integration test.
 * This test is intended to test cross layers integration the first mocked layer is data acces layer
 */

package com.atlassian.jira.issue.index;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.instrumentation.DefaultInstrumentRegistry;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.jira.concurrent.BarrierFactory;
import com.atlassian.jira.concurrent.MockBarrierFactory;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.ReindexMessage;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.MockIndexPathManager;
import com.atlassian.jira.config.util.MockIndexingConfiguration;
import com.atlassian.jira.event.listeners.search.IssueIndexListener;
import com.atlassian.jira.index.Index;
import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.index.LuceneVersion;
import com.atlassian.jira.index.MockResult;
import com.atlassian.jira.index.MultiThreadedIndexingConfiguration;
import com.atlassian.jira.index.SearchExtractorRegistrationManager;
import com.atlassian.jira.index.ha.ReplicatedIndexManager;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.DefaultIssueFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.index.IndexDirectoryFactory.Name;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.BaseFieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DescriptionIndexer;
import com.atlassian.jira.issue.index.indexers.impl.FieldIndexerUtil;
import com.atlassian.jira.issue.index.indexers.impl.IssueIdIndexer;
import com.atlassian.jira.issue.index.indexers.impl.IssueKeyIndexer;
import com.atlassian.jira.issue.index.indexers.impl.ProjectIdIndexer;
import com.atlassian.jira.issue.index.indexers.impl.SummaryIndexer;
import com.atlassian.jira.issue.index.managers.FieldIndexerManager;
import com.atlassian.jira.issue.util.IssueObjectIssuesIterable;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.ClearStatics;
import com.atlassian.jira.junit.rules.MockitoContainer;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.mock.MockListenerManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.MockProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.task.context.Context;
import com.atlassian.jira.task.context.Contexts;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.searchers.MockSearcherFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.scheduler.core.LifecycleAwareSchedulerService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import junit.framework.Assert;

import static com.atlassian.jira.issue.index.DocumentConstants.COMMENT_AUTHOR;
import static com.atlassian.jira.issue.index.DocumentConstants.COMMENT_ID;
import static com.atlassian.jira.issue.index.DocumentConstants.ISSUE_ID;
import static com.atlassian.jira.issue.index.DocumentConstants.PROJECT_ID;
import static com.atlassian.jira.issue.index.indexers.FieldIndexer.NO_VALUE_INDEX_VALUE;
import static com.atlassian.scheduler.core.LifecycleAwareSchedulerService.State.SHUTDOWN;
import static com.atlassian.scheduler.core.LifecycleAwareSchedulerService.State.STARTED;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestDefaultIssueIndexManager
{
    private static final String DEF_PROJECT_KEY = "ABC";
    private static final String DEF_PROJECT_NAME = "A Project";
    private static final String ISSUE1_ID = "1";
    private static final String ISSUE2_ID = "2";
    private static final String UNINDEXED = "UNINDEXED";
    private static final Long CHANGEGROUP1_ID = 101L;

    private static final Map<String, String> ISSUE_DOCUMENT = ImmutableMap.of(
            "id", ISSUE_ID,
            "project", PROJECT_ID,
            "type", UNINDEXED);

    private static final Map<String, String> COMMENT_DOCUMENT = ImmutableMap.of(
            "id", COMMENT_ID,
            "issue", ISSUE_ID,
            "project", PROJECT_ID,
            "type", UNINDEXED,
            "author", COMMENT_AUTHOR);

    @SuppressWarnings ("UnusedDeclaration")
    @AvailableInContainer private final InstrumentRegistry instrumentRegistry = new DefaultInstrumentRegistry();

    @Rule public MockitoContainer mockitoContainer = new MockitoContainer(this);
    @Rule public TestRule clearStatic = new ClearStatics();
    @Rule public TemporaryFolder indexDirectory = new TemporaryFolder();

    @Mock @AvailableInContainer private ApplicationProperties mockApplicationProperties;
    @Mock @AvailableInContainer private LifecycleAwareSchedulerService mockSchedulerService;
    @Mock @AvailableInContainer private IssueBatcherFactory issueBatcherFactory;
    @Mock @AvailableInContainer private FieldIndexerManager fieldIndexerManager;
    @Mock @AvailableInContainer private IssueManager issueManager;
    @Mock @AvailableInContainer private FieldVisibilityManager visibilityManager;
    @Mock @AvailableInContainer private SearchExtractorRegistrationManager searchExtractorManager;

    @Mock private FeatureManager featureManager;
    @Mock private ProjectManager mockProjectManager;
    @Mock private ReplicatedIndexManager mockReplicatedIndexManager;
    @Mock private ReindexMessageManager mockReindexMessageManager;
    @Mock private EventPublisher eventPublisher;

    private GenericValue project;
    private IssueFactory issueFactory;
    private DefaultIndexManager indexManager;
    private IndexDirectoryFactory indexDirectoryFactory;
    private Directory issueDirectory;
    private Directory commentDirectory;
    private Directory changesDirectory;
    private IndexPathManager indexPath;
    private MockOfBizDelegator ofBizDelegator;

    @Before
    public void setUp() throws Exception
    {
        ofBizDelegator = Mockito.spy(new MockOfBizDelegator());
        mockitoContainer.getMockComponentContainer().addMock(OfBizDelegator.class, ofBizDelegator);

        Mockito.doReturn(
                ImmutableList.<GenericValue>of(new MockGenericValue("max", ImmutableMap.of("max", Id.TEN_THOUSAND))))
                .when(ofBizDelegator).findByCondition("IssueMaxId", null, ImmutableList.of("max"));

        project = UtilsForTests.getTestEntity("Project", ImmutableMap.of("id", Id.TEN, "name", DEF_PROJECT_NAME, "key", DEF_PROJECT_KEY));
        when(mockProjectManager.getProjectObj(Id.TEN)).thenReturn(new MockProject(Id.TEN, DEF_PROJECT_KEY, DEF_PROJECT_NAME));
        when(mockApplicationProperties.getString(APKeys.JIRA_PATH_INDEX)).thenReturn(indexDirectory.getRoot().getAbsolutePath());
        when(mockApplicationProperties.getString(APKeys.JIRA_I18N_LANGUAGE_INPUT)).thenReturn(APKeys.Languages.ENGLISH);
        when(visibilityManager.isFieldHidden(anyString(), any(Issue.class))).thenReturn(false);

        when(issueManager.getEntitiesByIssueObject(eq(IssueRelationConstants.COMMENTS), any(Issue.class))).thenAnswer(new Answer<Object>()
        {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable
            {
                final Issue issue = (Issue) invocation.getArguments()[1];
                return ofBizDelegator.findByAnd("Action", ImmutableMap.of("issue", issue.getId()));
            }
        });

        indexPath = new MockIndexPathManager();
        issueDirectory = MockSearcherFactory.getCleanRAMDirectory();
        commentDirectory = MockSearcherFactory.getCleanRAMDirectory();
        changesDirectory = MockSearcherFactory.getCleanRAMDirectory();

        indexDirectoryFactory = new MockIndexDirectoryFactory(new Function<Name, Directory>()
        {
            public Directory get(final Name input)
            {
                if (input == Name.ISSUE)
                {
                    return issueDirectory;
                }
                else if (input == Name.COMMENT)
                {
                    return commentDirectory;
                }
                else if (input == Name.CHANGE_HISTORY)
                {
                    return changesDirectory;
                }
                throw new UnsupportedOperationException("unknown indexType: " + input);
            }
        }, mockApplicationProperties);
        final BarrierFactory barrierFactory = new MockBarrierFactory();
        final IssueBatcherFactory issueBatcherFactory = new

                IssueBatcherFactory()
                {
                    @Override
                    public IssuesBatcher getBatcher()
                    {
                        return getBatcher(null, null);
                    }

                    @Override
                    public IssuesBatcher getBatcher(final IssueIdBatcher.Spy spy)
                    {
                        return getBatcher(null, spy);
                    }

                    @Override
                    public IssuesBatcher getBatcher(final EntityCondition condition)
                    {
                        return getBatcher(condition, null);
                    }

                    @Override
                    public IssuesBatcher getBatcher(final EntityCondition condition, final IssueIdBatcher.Spy spy)
                    {
                        return new IssueIdBatcher(ofBizDelegator, issueFactory, barrierFactory, 1000, null, null);
                    }

                    @Override
                    public IssuesBatcher getBatcher(final EntityCondition condition, final IssueIdBatcher.Spy spy, int batchSize)
                    {
                        return new IssueIdBatcher(ofBizDelegator, issueFactory, barrierFactory, batchSize, null, null);
                    }
                };
        mockitoContainer.getMockComponentContainer().addMock(IssueBatcherFactory.class, issueBatcherFactory);
        issueFactory = new DefaultIssueFactory(issueManager, mockProjectManager, null, null, null, null, null, null, null, null, null, null, null, null);
        mockitoContainer.getMockComponentContainer().addMock(IssueFactory.class, issueFactory);


        final IssueIndexer issueIndexer = new DefaultIssueIndexer(
                indexDirectoryFactory,
                new MemoryIssueIndexer.CommentRetrieverImpl(issueManager),
                new MemoryIssueIndexer.ChangeHistoryRetrieverImpl(issueManager),
                mockApplicationProperties,
                new DefaultIssueDocumentFactory(searchExtractorManager),
                new DefaultCommentDocumentFactory(searchExtractorManager),
                new DefaultChangeHistoryDocumentFactory(searchExtractorManager));
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), issueIndexer,
                indexPath, mockReindexMessageManager, eventPublisher, null, mockProjectManager,
                issueManager, null, ofBizDelegator, mockReplicatedIndexManager);

    }

    @Test
    public void testIndexChanges() throws Exception
    {
        prepareIndexDir();

        createIssue(ImmutableMap.of("id", Id.ONE, "key", "ABC-7348", "project", Id.TEN, "description", "This is the body",
                "summary", "An Issue"));

        final MockGenericValue changeGroup = (MockGenericValue) UtilsForTests.getTestEntity("ChangeGroup", ImmutableMap.of("id", CHANGEGROUP1_ID, "issue", Id.ONE, "author", "user", "created", new Timestamp(System.currentTimeMillis())));
        changeGroup.setRelated("ChildChangeItem",
                ImmutableList.of(
                        new MockGenericValue("ChangeItem",
                                ImmutableMap.builder().put("id", Id.TEN)
                                        .put("group", CHANGEGROUP1_ID)
                                        .put("oldstring", "Open").put("newstring", "Closed")
                                        .put("oldvalue", "1").put("newvalue", "5")
                                        .put("field", "status").build()),
                        new MockGenericValue("ChangeItem",
                                ImmutableMap.of("id", Id.HUNDRED, "group", CHANGEGROUP1_ID, "oldvalue", "Fred", "newvalue", "Barney", "field", "assignee"))));

        prepareMockIndexManager();
        when(issueManager.getEntitiesByIssueObject(eq(IssueRelationConstants.CHANGE_GROUPS), any(Issue.class))).thenReturn(ImmutableList.<GenericValue>of(changeGroup));
        indexManager.reIndexAll();

        final IndexSearcher issueSearcher = new IndexSearcher(getChangeIndexDirectory());
        try
        {
            TopDocs hits = issueSearcher.search(new TermQuery(new Term("ch_who", "ch-user")), Integer.MAX_VALUE);
            assertEquals(1, hits.totalHits);
            hits = issueSearcher.search(new TermQuery(new Term("status.ch_from", "ch-open")), Integer.MAX_VALUE);
            assertEquals(1, hits.totalHits);
            //assignee is not a suuported field
            hits = issueSearcher.search(new TermQuery(new Term("assignee.ch_to", "ch-barney")), Integer.MAX_VALUE);
            assertEquals(0, hits.totalHits);
        }

        finally
        {
            issueSearcher.close();
        }

    }

    @Test
    public void testIndexSearchIssue() throws Exception
    {
        createIssue(ImmutableMap.of("id", Id.ONE, "number", 7348L, "project", Id.TEN, "description", "This is the body",
                "summary", "An Issue"));

        prepareMockIndexManager();

        indexManager.reIndexAll();

        final IndexSearcher issueSearcher = new IndexSearcher(getIssueIndexDirectory());
        try
        {
            TopDocs hits = issueSearcher.search(new TermQuery(new Term(DocumentConstants.ISSUE_KEY, "ABC-7348")), Integer.MAX_VALUE);
            assertEquals(1, hits.totalHits);
            final QueryParser parser = new QueryParser(LuceneVersion.get(), DocumentConstants.ISSUE_DESC, DefaultIndexManager.ANALYZER_FOR_SEARCHING);
            hits = issueSearcher.search(parser.parse("body"), Integer.MAX_VALUE);
            assertEquals(1, hits.totalHits);
            hits = issueSearcher.search(parser.parse("shouldn't"), Integer.MAX_VALUE);
            assertEquals(0, hits.totalHits);
        }
        finally
        {
            issueSearcher.close();
        }
    }

    private void prepareMockIndexManager()
    {
        final java.util.Date now = new java.util.Date();
        when(mockProjectManager.getProjectObjects()).thenReturn(new MockProjectFactory().getProjects(Collections.singletonList(project)));

        when(mockReindexMessageManager.getMessageObject()).thenReturn(new ReindexMessage("bill", now, "zz"));
        mockReindexMessageManager.clearMessageForTimestamp(now);
        final FieldVisibilityManager fieldVisibilityManager = mock(FieldVisibilityManager.class);
        when(fieldIndexerManager.getAllIssueIndexers()).thenReturn(ImmutableList.<FieldIndexer>of(
                new DescriptionIndexer(fieldVisibilityManager),
                new IssueKeyIndexer(fieldVisibilityManager),
                new IssueIdIndexer(fieldVisibilityManager),
                new SummaryIndexer(fieldVisibilityManager),
                new ProjectIdIndexer(fieldVisibilityManager)
        ));
    }

    @Test(timeout=30000L) //Testing fix for reindex lockup, 30 seconds should be enough when everything's OK
    public void testIndexMultipleIssues() throws Exception
    {
        //It's a race condition we're triggering here, increasing numIssuesToCreate will help to trigger it
        //if it's not happening for you
        long numIssuesToCreate = 50;
        for (long id = 0L; id < numIssuesToCreate; id++)
        {
            createIssue(ImmutableMap.of("id", id, "number", 7347L + id, "project", Id.TEN, "description", "This is the body",
                    "summary", "An Issue"));
        }

        int numTimesToRun = 5;
        for (int iter = 0; iter < numTimesToRun; iter++)
        {
            prepareMockIndexManager();

            final Map<Name, Directory> map = new EnumMap<Name, Directory>(Name.class);
            DefaultIssueIndexer defaultIssueIndexer = new DefaultIssueIndexer(new MockIndexDirectoryFactory(
                    new Function<Name, Directory>()
                    {
                        public Directory get(final Name input)
                        {
                            Directory directory = map.get(input);
                            if (directory == null)
                            {
                                directory = MockSearcherFactory.getCleanRAMDirectory();
                                map.put(input, directory);
                            }
                            return directory;
                        }
                    }, mockApplicationProperties),
                    new MemoryIssueIndexer.CommentRetrieverImpl(issueManager),
                    new MemoryIssueIndexer.ChangeHistoryRetrieverImpl(issueManager),
                    mockApplicationProperties,
                    new DefaultIssueDocumentFactory(searchExtractorManager),
                    new DefaultCommentDocumentFactory(searchExtractorManager),
                    new DefaultChangeHistoryDocumentFactory(searchExtractorManager));
            try
            {
                indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), defaultIssueIndexer,
                        indexPath, mockReindexMessageManager, eventPublisher, null, mockProjectManager, issueManager, null,
                        ofBizDelegator, mockReplicatedIndexManager);
                indexManager.reIndexAll();

                final IndexReader reader = IndexReader.open(map.get(Name.ISSUE));
                try
                {
                    //If we get here we know the reindex task hasn't locked up, just basic validation is enough
                    assertEquals(numIssuesToCreate, reader.numDocs());
                }
                finally
                {
                    reader.close();
                }
            }
            finally
            {
                defaultIssueIndexer.shutdown();
            }
        }
    }


    @Test(timeout=30000L) //Lock-ups have happened on this test, 30 seconds should be enough for passing cases
    public void testIndexLookupIssue() throws Exception
    {
        createIssue(ImmutableMap.of("id", Id.ONE, "number", 7348L, "project", Id.TEN, "description", "This is the body",
                "summary", "An Issue"));

        prepareMockIndexManager();

        final Map<Name, Directory> map = new EnumMap<Name, Directory>(Name.class);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new DefaultIssueIndexer(new MockIndexDirectoryFactory(
                new Function<Name, Directory>()
                {
                    public Directory get(final Name input)
                    {
                        Directory directory = map.get(input);
                        if (directory == null)
                        {
                            directory = MockSearcherFactory.getCleanRAMDirectory();
                            map.put(input, directory);
                        }
                        return directory;
                    }
                }, mockApplicationProperties),
                new MemoryIssueIndexer.CommentRetrieverImpl(issueManager),
                new MemoryIssueIndexer.ChangeHistoryRetrieverImpl(issueManager),
                mockApplicationProperties,
                new DefaultIssueDocumentFactory(searchExtractorManager),
                new DefaultCommentDocumentFactory(searchExtractorManager),
                new DefaultChangeHistoryDocumentFactory(searchExtractorManager)),
                indexPath, mockReindexMessageManager, eventPublisher, null, mockProjectManager, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager);
        indexManager.reIndexAll();

        final IndexReader reader = IndexReader.open(map.get(Name.ISSUE));
        try
        {
            assertEquals(1, reader.numDocs());

            final Document doc = reader.document(0);
            assertEquals("1", doc.get(ISSUE_ID));
            assertEquals("ABC-7348", doc.get(DocumentConstants.ISSUE_KEY));
        }
        finally
        {
            reader.close();
        }

    }

    @Test
    public void testShutdown() throws Exception
    {
        final IssueIndexer mockIssueIndexer = mock(IssueIndexer.class);

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), mockIssueIndexer, indexPath, mockReindexMessageManager,
                eventPublisher, null, null, issueManager, null, ofBizDelegator, mockReplicatedIndexManager);
        // Do the right thang
        indexManager.shutdown();

    }

    @Test
    public void testDeIndex() throws Exception
    {
        prepareIndexDir();

        // Create an issue for testing
        final GenericValue issueGV = createIssue(ImmutableMap.of("id", Id.ONE, "number", 7348L, "project", project.getLong("id"),
                "description", "This is the body", "summary", "An Issue"));
        // Create some comments for the issue
        UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author",
                "somedude", "body", "Here we have a comment"));
        UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author",
                "somedude2", "body", "Here we have another comment"));

        // Create another issue
        final GenericValue issueGV2 = createIssue(ImmutableMap.of("id", new Long(ISSUE2_ID), "number", 8000L, "project",
                project.getLong("id"), "description", "This is the another body", "summary", "Another Issue"));
        // Create comments for the issue
        final Collection<GenericValue> commentGVs = new ArrayList<GenericValue>();
        commentGVs.add(UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
                "author", "somedude", "body", "Here we have stuff")));
        commentGVs.add(UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
                "author", "somedude2", "body", "Here we have another stuff")));
        commentGVs.add(UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
                "author", "somedude3", "body", "Here we have abc stuff")));
        commentGVs.add(UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
                "author", "somedude4", "body", "Here we have xyz stuff")));

        indexIssue(issueGV, 1, 2);
        indexIssue(issueGV2, 2, 6);

        // Do the right thing
        indexManager.deIndex(issueGV);

        // Assert the second issue is left in the index
        assertIndexContainsIssue(1, ISSUE2_ID);
        // Assert that second issue's comments are in the index
        assertIndexContainsComments(commentGVs, 4);


        //yay
    }

    private void prepareIndexDir() throws IOException
    {
        final java.io.File issueIndexDirectory = new java.io.File(indexDirectory.getRoot(), "issues");
        final java.io.File commentsIndexDirectory = new java.io.File(indexDirectory.getRoot(), "comments");
        final java.io.File changesIndexDirectory = new java.io.File(indexDirectory.getRoot(), "changes");
        final java.io.File entityPropertiesIndexDirectory = new java.io.File(indexDirectory.getRoot(), "entityProperties");

        //assertNotNull is just to make check style happy
        assertNotNull(issueIndexDirectory.mkdirs());
        assertNotNull(commentsIndexDirectory.mkdirs());
        assertNotNull(changesIndexDirectory.mkdirs());
        assertNotNull(entityPropertiesIndexDirectory.mkdirs());

        // Initialise the issues, comments and changes directories
        IndexWriterConfig conf = new IndexWriterConfig(LuceneVersion.get(), JiraAnalyzer.ANALYZER_FOR_INDEXING);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        new IndexWriter(getIssueIndexDirectory(), conf).close();

        conf = new IndexWriterConfig(LuceneVersion.get(), JiraAnalyzer.ANALYZER_FOR_INDEXING);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        new IndexWriter(getCommentsIndexDirectory(), conf).close();

        conf = new IndexWriterConfig(LuceneVersion.get(), JiraAnalyzer.ANALYZER_FOR_INDEXING);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        new IndexWriter(getChangeIndexDirectory(), conf).close();
    }

    @Test
    public void testDeIndexNotAnIssue() throws Exception
    {
        prepareIndexDir();

        // Create an issue for testing
        final GenericValue issueGV = createIssue(ImmutableMap.of("id", Id.ONE, "number", 7348L, "project", project.getLong("id"),
                "description", "This is the body", "summary", "An Issue"));
        // Create comments for issue
        final Collection<GenericValue> commentGVs = new ArrayList<GenericValue>();
        commentGVs.add(UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
                "author", "somedude", "body", "Here we have a comment")));
        commentGVs.add(UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
                "author", "somedude2", "body", "Here we have another comment")));

        final GenericValue projectGV = UtilsForTests.getTestEntity("Project", ImmutableMap.of("id", Id.ONE, "name", DEF_PROJECT_NAME));

        indexIssue(issueGV, 1, 2);

        // Do the right thing
        indexManager.deIndex(projectGV);

        // Assert that an issue was *not* deleted
        assertIndexContainsIssue(1, "1");
        // Assert that issue's comments were *not* deleted
        assertIndexContainsComments(commentGVs, 2);


        // true anality would test here if something got logged...
        //yay
    }

    private Documents indexIssue(final GenericValue issueGV, final int issueCount, final int commentCount)
            throws IOException, GenericEntityException
    {
        // Put an issue issueDocument in there
        final Document issueDocument = new Document();

        issueDocument.add(new Field(ISSUE_ID, issueGV.getLong("id").toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        issueDocument.add(new Field(DocumentConstants.ISSUE_KEY, issueGV.getString("key"), Field.Store.YES, Field.Index.NOT_ANALYZED));
        issueDocument.add(new Field(PROJECT_ID, issueGV.getLong("project").toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        issueDocument.add(new Field(DocumentConstants.ISSUE_DESC, issueGV.getString("description"), Field.Store.YES, Field.Index.ANALYZED));
        issueDocument.add(new Field(DocumentConstants.ISSUE_SUMMARY, issueGV.getString("summary"), Field.Store.YES, Field.Index.ANALYZED));

        IndexWriter indexWriter = null;
        try
        {
            // Make sure the index manager isn't locking the index.
            indexManager.shutdown();
            // Create a new index
            final IndexWriterConfig conf = new IndexWriterConfig(LuceneVersion.get(), JiraAnalyzer.ANALYZER_FOR_INDEXING);
            conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            indexWriter = new IndexWriter(getIssueIndexDirectory(), conf);
            indexWriter.addDocument(issueDocument);
        }
        finally
        {
            if (indexWriter != null)
            {
                indexWriter.close();
            }
        }

        final Document createdIssueDocument = assertIndexContainsIssue(issueCount, issueGV.getLong("id").toString());

        final List<GenericValue> commentGVs = ofBizDelegator.findByAnd("Action", ImmutableMap.of("issue", issueGV.get("id")));
        final List<Document> createdCommentDocs = new ArrayList<Document>(commentGVs.size());

        if (!commentGVs.isEmpty())
        {

            try
            {
                // Create a new index
                final IndexWriterConfig conf = new IndexWriterConfig(LuceneVersion.get(), JiraAnalyzer.ANALYZER_FOR_INDEXING);
                conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                indexWriter = new IndexWriter(getCommentsIndexDirectory(), conf);

                for (final Object element : commentGVs)
                {
                    final GenericValue commentGV = (GenericValue) element;
                    final Document doc = new Document();
                    final String body = commentGV.getString("body");
                    if (body != null)
                    {
                        doc.add(new Field(PROJECT_ID, String.valueOf(issueGV.getLong("project")), Field.Store.YES,
                                Field.Index.NOT_ANALYZED));
                        doc.add(new Field(ISSUE_ID, String.valueOf(issueGV.getLong("id")), Field.Store.YES,
                                Field.Index.NOT_ANALYZED));
                        doc.add(new Field(COMMENT_ID, commentGV.getString("id"), Field.Store.YES, Field.Index.NOT_ANALYZED));

                        final String author = commentGV.getString("author");
                        if (author != null) //can't add null keywords
                        {
                            doc.add(new Field(COMMENT_AUTHOR, author, Field.Store.YES, Field.Index.NOT_ANALYZED));
                        }

                        doc.add(new Field(DocumentConstants.COMMENT_BODY, body, Field.Store.YES, Field.Index.ANALYZED));
                        FieldIndexerUtil.indexKeywordWithDefault(doc, DocumentConstants.COMMENT_LEVEL, commentGV.getString("level"),
                                NO_VALUE_INDEX_VALUE);
                        FieldIndexerUtil.indexKeywordWithDefault(doc, DocumentConstants.COMMENT_LEVEL_ROLE, commentGV.getString("rolelevel"),
                                NO_VALUE_INDEX_VALUE);

                        indexWriter.addDocument(doc);
                    }
                }
            }
            finally
            {
                indexWriter.close();
            }

            // Assert comments are in the index
            createdCommentDocs.addAll(assertIndexContainsComments(commentGVs, commentCount));
        }

        return new Documents(createdIssueDocument, createdCommentDocs);
    }

    @Test
    public void testDeIndexCouldNotGetLock() throws Exception
    {
        prepareIndexDir();

        final AtomicBoolean lockCalled = new AtomicBoolean(false);
        final AtomicBoolean lockAvailable = new AtomicBoolean(true);

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new MemoryIssueIndexer(), indexPath,
                mockReindexMessageManager, eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager)
        {
            @Override
            boolean getIndexLock()
            {
                lockCalled.set(true);
                return lockAvailable.get();
            }
            @Override
            void releaseIndexLock()
            {

            }
        };

        // Create an issue for testing
        final GenericValue issueGV = createIssue(ImmutableMap.of("id", Id.ONE, "number", 7348L, "project", project.getLong("id"),
                "description", "This is the body", "summary", "An Issue"));
        final Collection<GenericValue> commentGVs = new ArrayList<GenericValue>();
        commentGVs.add(UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
                "author", "somedude", "body", "Here we have a comment")));
        commentGVs.add(UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
                "author", "somedude2", "body", "Here we have another comment")));

        indexIssue(issueGV, 1, 2);
        lockAvailable.set(false);

        // Do the right thing
        indexManager.deIndex(issueGV);

        lockAvailable.set(true);
        // Ensure that the issue was *not* deindexed
        assertIndexContainsIssue(1, "1");
        // Ensure issue's comments were *not* deleted
        assertIndexContainsComments(commentGVs, 2);

        // Ensure the getIndexLock() method was invoked
        Assert.assertTrue(lockCalled.get());
    }

    @Test
    public void testReIndexChucksNPE() throws Exception
    {
        final Context event = Contexts.nullContext();
        try
        {
            indexManager.reIndexIssues(null, event);
            Assert.fail("IllegalArg expected but not thrown.");
        }
        catch (final IllegalArgumentException ignore)
        {}
    }

    @Test
    public void testReIndexAllChucksRuntimeExceptionNotIndexExFromIndexerDeleteAndReinit() throws Exception
    {
        final UnimplementedIssueIndexer issueIndexer = new UnimplementedIssueIndexer()
        {
            @Override
            public void shutdown()
            {
                // Called from reIndexAll()
            }
        };

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), issueIndexer, indexPath, mockReindexMessageManager,
                eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager);
        final Context event = Contexts.nullContext();
        prepareMockIndexManager();
        try
        {
            indexManager.reIndexAll();
            Assert.fail("UnsupportedOpException expected.");
        }
        catch (final UnsupportedOperationException yay)
        {
            // Expected
        }

        prepareMockIndexManager();
        try
        {
            indexManager.reIndexAll(event);
            Assert.fail("UnsupportedOpException expected.");
        }
        catch (final UnsupportedOperationException yay)
        {
            // Expected
        }
    }

    @Test
    public void testReIndexAllChucksRuntimeExceptionNotIndexExFromIndexerShutdown() throws Exception
    {
        final UnimplementedIssueIndexer issueIndexer = new UnimplementedIssueIndexer();

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), issueIndexer, indexPath, mockReindexMessageManager,
                eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager);
        final Context event = Contexts.nullContext();

        prepareMockIndexManager();
        try
        {
            indexManager.reIndexAll();
            Assert.fail("UnsupportedOpException expected.");
        }
        catch (final UnsupportedOperationException yay)
        {
            // Expected
        }

        prepareMockIndexManager();
        try
        {
            indexManager.reIndexAll(event);
            Assert.fail("UnsupportedOpException expected.");
        }
        catch (final UnsupportedOperationException yay)
        {
            // Expected
        }
    }

    @Test
    public void testReIndexChucksRuntimeException() throws Exception
    {
        // If we have a Runtime Exception which is *not* a LuceneException thrown by the indexing
        // code, then it should not be wrapped into a LuceneException

        final UnimplementedIssueIndexer issueIndexer = new UnimplementedIssueIndexer()
        {
            @Override
            public Index.Result reindexIssues(final EnclosedIterable<Issue> issuesIterable, final Context event, boolean reIndexComments, boolean reIndexChangeHistory, boolean conditionalUpdate)
            {
                throw new IllegalArgumentException("ha ha");
            }

            @Override
            public void shutdown()
            {
                // Called from reIndexAll()
            }

            @Override
            public void deleteIndexes()
            {
                // Called from reIndexAll()
            }

            @Override
            public Result indexIssuesBatchMode(final EnclosedIterable<Issue> issuesIterable, final Context event)
            {
                throw new IllegalArgumentException("botched");
            }
        };

        when(ofBizDelegator.findByCondition("IssueMaxId", null, ImmutableList.of("max"))).thenReturn(Collections.<GenericValue>singletonList(new MockGenericValue("IssueMaxId", ImmutableMap.of("max", 1L))));

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), issueIndexer, indexPath, mockReindexMessageManager, eventPublisher, null, mockProjectManager, issueManager, null, ofBizDelegator, mockReplicatedIndexManager);
        final Context context = Contexts.nullContext();

        // make sure we call all the methods that delegate to reindex(IssueIterable, Context)
        try
        {
            indexManager.reIndex(new MockIssue());
            Assert.fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {
            // Expected
        }

        try
        {
            indexManager.reIndexIssueObjects(Lists.newArrayList(new MockIssue()));
            Assert.fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {
            // Expected
        }

        try
        {
            final GenericValue issueGV = new MockGenericValue("Issue", Collections.EMPTY_MAP);
            indexManager.reIndex(issueGV);
            Assert.fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {
            // Expected
        }

        try
        {
            final GenericValue issueGV = new MockGenericValue("Issue", Collections.EMPTY_MAP);
            indexManager.reIndexIssues(Lists.newArrayList(issueGV));
            Assert.fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {
            // Expected
        }

        try
        {
            indexManager.reIndexIssues(new IssueObjectIssuesIterable(Lists.newArrayList(new MockIssue())), context);
            Assert.fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {
            // Expected
        }

        try
        {
            prepareMockIndexManager();
            indexManager.reIndexAll();
            Assert.fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {
            // Expected
        }


        try
        {
            prepareMockIndexManager();
            indexManager.reIndexAll(context);
            Assert.fail("IllegalArgumentException expected.");
        }
        catch (final IllegalArgumentException yay)
        {
            // Expected
        }
    }

    @Test
    public void testReIndex() throws Exception
    {
        // Need to ensure that the project exists, as when reindexing the issue the project is pulled out from a manager
        UtilsForTests.getTestEntity("Project", ImmutableMap.of("id", 11L, "name", "Test Project", "key", "ABCD"));

        prepareIndexDir();

        // Create an issue for testing
        final Map<String, ?> issue1Vals = ImmutableMap.of("id", new Long(ISSUE1_ID), "number", 7348L, "project", project.getLong("id"),
                "description", "This is the body", "summary", "An Issue");
        final GenericValue issueGV = createIssue(issue1Vals);
        // Create some comments for the issue
        @SuppressWarnings ("unchecked")
        final Map<String, ?> comment1Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity(
                "Action",
                ImmutableMap.of("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude", "body",
                        "Here we have a comment")).getAllFields());
        @SuppressWarnings ("unchecked")
        final Map<String, ?> comment2Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity(
                "Action",
                ImmutableMap.of("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude2", "body",
                        "Here we have another comment")).getAllFields());

        final Documents issue1Documents = indexIssue(issueGV, 1, 2);
        // Assert that the right values are in the index
        assertNotNull(issue1Documents);
        assertIssueDocumentEquals(issue1Documents.issue, ImmutableMap.of("id", new Long(ISSUE1_ID), "key", "ABC-7348", "project", project.getLong("id"),
                "description", "This is the body", "summary", "An Issue"));

        // Assert issue1's comments
        assertNotNull(issue1Documents.comments);
        assertEquals(2, issue1Documents.comments.size());
        {
            final Iterator<Document> iterator = issue1Documents.comments.iterator();
            assertCommentDocumentEquals(iterator.next(), comment1Vals);
            assertCommentDocumentEquals(iterator.next(), comment2Vals);
        }
        // Create another issue
        @SuppressWarnings ("unchecked")
        Map<String, ?> issue2Vals = ImmutableMap.of("id", new Long(ISSUE2_ID), "number", 8000L, "project", project.getLong("id"),
                "description", "This is the another body", "summary", "Another Issue");
        final GenericValue issueGV2 = createIssue(issue2Vals);
        // Create comments for the issue
        @SuppressWarnings ("unchecked")
        final Map<String, ?> comment3Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity("Action",
                ImmutableMap.of("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude", "body", "Here we have stuff")).getAllFields());
        @SuppressWarnings ("unchecked")
        final Map<String, ?> comment4Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity(
                "Action",
                ImmutableMap.of("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude2", "body",
                        "Here we have another stuff")).getAllFields());
        final GenericValue commentGV5 = UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV2.getLong("id"), "type",
                ActionConstants.TYPE_COMMENT, "author", "somedude3", "body", "Here we have abc stuff"));
        @SuppressWarnings ("unchecked")
        final Map<String, ?> comment5Vals = Collections.unmodifiableMap(commentGV5.getAllFields());
        final GenericValue commentGV6 = UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV2.getLong("id"), "type",
                ActionConstants.TYPE_COMMENT, "author", "somedude4", "body", "Here we have xyz stuff"));
        @SuppressWarnings ("unchecked")
        Map<String, ?> comment6Vals = Collections.unmodifiableMap(commentGV6.getAllFields());

        final Documents issue2Documents = indexIssue(issueGV2, 2, 6);

        // Assert that the right values are in the index
        assertNotNull(issue2Documents);
        assertIssueDocumentEquals(issue2Documents.issue, ImmutableMap.of("id", new Long(ISSUE2_ID), "key", "ABC-8000", "project", project.getLong("id"),
                "description", "This is the another body", "summary", "Another Issue"));

        // Assert issue2's comments
        assertNotNull(issue2Documents.comments);
        assertEquals(4, issue2Documents.comments.size());
        {
            final Iterator<Document> iterator = issue2Documents.comments.iterator();
            assertCommentDocumentEquals(iterator.next(), comment3Vals);
            assertCommentDocumentEquals(iterator.next(), comment4Vals);
            assertCommentDocumentEquals(iterator.next(), comment5Vals);
            assertCommentDocumentEquals(iterator.next(), comment6Vals);
        }
        // assert document changed
        issueGV2.set(IssueFieldConstants.ISSUE_NUMBER, 1234l);
        issueGV2.set("project", 11L);
        issueGV2.set("description", "no longer stuffed");
        issueGV2.set("summary", "no, really!");
        issueGV2.set("key", "ABCD-1234");
        issue2Vals = ImmutableMap.copyOf(issueGV2.getAllFields());
        ofBizDelegator.store(issueGV);

        ofBizDelegator.removeValue(commentGV5);

        commentGV6.set("author", "somebabe");
        commentGV6.set("body", "Here we don't have anything much at all");
        // Need to store the comment - as the reindex of an issue pulls issue's comment from the database
        ofBizDelegator.store(commentGV6);


        comment6Vals = Collections.unmodifiableMap(commentGV6.getAllFields());

        // Do the right thing
        final MockIssue issueToReindex = new MockIssue();
        issueToReindex.setGenericValue(issueGV2);
        issueToReindex.setProjectId(11L);
        when(mockProjectManager.getProjectObj(11l)).thenReturn(new MockProject(11l, "ABCD", DEF_PROJECT_NAME));
        prepareMockIndexManager();
        final IndexSearcher searcher = indexManager.getIssueSearcher();


        indexManager.reIndex(issueToReindex);

        final IndexSearcher newSearcher = indexManager.getIssueSearcher();
        Assert.assertNotSame(searcher, newSearcher);

        final IndexSearcher indexSearcher;
        try
        {
            Document issueDocument = assertIndexContainsIssue(2, ISSUE2_ID);
            // Assert that the right values are in the index
            assertNotNull(issueDocument);
            assertIssueDocumentEquals(issueDocument, removeNum(issue2Vals));
            // Assert issue2's comments
            // Open a searcher and find all issue2's documents

            indexSearcher = indexManager.getCommentSearcher(); //new IndexSearcher(getCommentsIndexDirectory());
            TopDocs hits = indexSearcher.search(new TermQuery(new Term(ISSUE_ID, ISSUE2_ID)), Integer.MAX_VALUE);
            assertNotNull(hits);
            assertEquals(3, hits.totalHits);
            {
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[0].doc), comment3Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[1].doc), comment4Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[2].doc), comment6Vals);
            }
            issueDocument = assertIndexContainsIssue(2, ISSUE1_ID);
            // Assert that the right values are in the index
            assertNotNull(issueDocument);
            assertIssueDocumentEquals(issueDocument, removeNum(issue1Vals));

            // Assert issue1's comments
            hits = indexSearcher.search(new TermQuery(new Term(ISSUE_ID, ISSUE1_ID)), Integer.MAX_VALUE);
            assertNotNull(hits);
            assertEquals(2, hits.totalHits);
            {
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[0].doc), comment1Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[1].doc), comment2Vals);
            }
        }
        finally
        {
            // Clear the thread local so that other tests are not affected
            SearcherCache.getThreadLocalCache().closeSearchers();
        }
    }

    private GenericValue createIssue(final Map<String, ?> values)
    {
        final ImmutableMap.Builder<Object, Object> valuesBuilder = ImmutableMap.builder().putAll(values);
        if (!values.containsKey("key"))
        {
            valuesBuilder.put("key", MessageFormat.format("{0}-{1}", DEF_PROJECT_KEY, values.get("number").toString()));
        }

        return UtilsForTests.getTestEntity("Issue", valuesBuilder.build());
    }

    @Test
    public void testReindexAllCallsIssueIndexerBatchMode() throws Exception
    {
        prepareMockIndexManager();

        // set up the mock interactions
        final IssueIndexer indexer = mock(IssueIndexer.class);
        indexer.deleteIndexes();

        when(indexer.indexIssuesBatchMode(Mockito.<EnclosedIterable<Issue>>anyObject(), any(Context.class))).thenReturn(new MockResult());
        when(indexer.optimize()).thenReturn(new MockResult());
        Mockito.doReturn(
                ImmutableList.<GenericValue>of(new MockGenericValue("max", ImmutableMap.of("max", 1l))))
                .when(ofBizDelegator).findByCondition("IssueMaxId", null, ImmutableList.of("max"));

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), indexer, indexPath, mockReindexMessageManager, eventPublisher, null, mockProjectManager, issueManager, null, ofBizDelegator, mockReplicatedIndexManager);
        indexManager.reIndexAll(Contexts.nullContext());
        verify(indexer).indexIssuesBatchMode(Mockito.<EnclosedIterable<Issue>>anyObject(), any(Context.class));
    }

    @Test
    public void testReindexIssueObjects() throws Exception
    {
        // We would like to transform the Issue object to GenericValues before re-indexing to ensure that there
        // are no discrepancies between them. Once we move the entire system to Issue objects this will be unnecessary.
        // Until then, please do *not* change this behaviour.

        final MockGenericValue issueGV1 = new MockGenericValue("Issue", ImmutableMap.of("id", new Long(ISSUE1_ID), "key", "ABC-7348", "project",
                Id.TEN, "description", "This is the body", "summary", "An Issue"));
        final MockIssue issue1 = new MockIssue();
        issue1.setGenericValue(issueGV1);

        final MockGenericValue issueGV2 = new MockGenericValue("Issue", ImmutableMap.of("id", new Long(ISSUE2_ID), "key", "ABC-8000", "project",
                Id.TEN, "description", "This is the another body", "summary", "Another Issue"));
        final MockIssue issue2 = new MockIssue();
        issue2.setGenericValue(issueGV2);

        final List<? extends Issue> issueObjects = Lists.newArrayList(issue1, issue2);

        final AtomicBoolean reindexCollectionMethodCalled = new AtomicBoolean(false);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new MemoryIssueIndexer(), indexPath,
                mockReindexMessageManager, eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager)
        {
            @Override
            public long reIndexIssues(final Collection<GenericValue> issues, boolean reindexComments, boolean reindexChangeHistory)
                    throws IndexException
            {
                reindexCollectionMethodCalled.set(true);

                assertNotNull(issues);
                assertEquals(2, issues.size());

                final Iterator<GenericValue> iterator = issues.iterator();
                assertSame(issueGV1, iterator.next());
                assertSame(issueGV2, iterator.next());

                return -2;
            }
        };

        final long returned = indexManager.reIndexIssueObjects(issueObjects);
        assertEquals(-2, returned);

        // Ensure the method was called
        Assert.assertTrue(reindexCollectionMethodCalled.get());
    }

    @Test
    public void testReIndexNotAnIssue() throws Exception
    {
        prepareIndexDir();

        // Create an issue for testing
        final GenericValue issueGV = createIssue(ImmutableMap.of("id", Id.ONE, "number", 7348L, "project", project.getLong("id"),
                "description", "This is the body", "summary", "An Issue"));
        // Create comments for issue
        final Collection<GenericValue> commentGVs = new ArrayList<GenericValue>();
        commentGVs.add(UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
                "author", "somedude", "body", "Here we have a comment")));
        commentGVs.add(UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT,
                "author", "somedude2", "body", "Here we have another comment")));

        final GenericValue projectGV = UtilsForTests.getTestEntity("Project", ImmutableMap.of("id", Id.ONE, "name", DEF_PROJECT_NAME));

        indexIssue(issueGV, 1, 2);

        // Do the right thing
        indexManager.reIndex(projectGV);

        // Assert that an issue was *not* deleted
        assertIndexContainsIssue(1, "1");
        // Assert that issue's comments were *not* deleted
        assertIndexContainsComments(commentGVs, 2);

        // true anality would test here if something got logged...
        //yay
    }

    @Test
    public void testReIndexCouldNotGetLock() throws Exception
    {
        prepareIndexDir();
        // Create an issue for testing
        final Map<String, Object> issue1Vals = ImmutableMap.<String, Object>of("id", new Long(ISSUE1_ID), "number", 7348L, "project", project.getLong("id"),
                "description", "This is the body", "summary", "An Issue");
        final GenericValue issueGV = createIssue(issue1Vals);
        // Create some comments for the issue
        final Map<String, Object> comment1Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity(
                "Action",
                ImmutableMap.of("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude", "body",
                        "Here we have a comment")).getAllFields());
        final Map<String, Object> comment2Vals = Collections.unmodifiableMap(UtilsForTests.getTestEntity(
                "Action",
                ImmutableMap.of("issue", issueGV.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude2", "body",
                        "Here we have another comment")).getAllFields());

        final Documents issue1Documents = indexIssue(issueGV, 1, 2);
        // Assert that the right values are in the index
        assertNotNull(issue1Documents);
        assertIssueDocumentEquals(issue1Documents.issue, ImmutableMap.of("id", new Long(ISSUE1_ID), "key", "ABC-7348", "project", project.getLong("id"),
                "description", "This is the body", "summary", "An Issue"));

        // Assert issue1's comments
        assertNotNull(issue1Documents.comments);
        assertEquals(2, issue1Documents.comments.size());
        {
            final Iterator<Document> iterator = issue1Documents.comments.iterator();
            assertCommentDocumentEquals(iterator.next(), comment1Vals);
            assertCommentDocumentEquals(iterator.next(), comment2Vals);
        }
        // Create another issue
        final Map<String, Object> issue2Vals = ImmutableMap.<String, Object>of("id", new Long(ISSUE2_ID), "number", 8000L, "project", project.getLong("id"),
                "description", "This is the another body", "summary", "Another Issue");
        final GenericValue issueGV2 = createIssue(issue2Vals);
        // Create comments for the issue
        final Map<String, Object> comment3Vals = ImmutableMap.copyOf(UtilsForTests.getTestEntity("Action",
                ImmutableMap.of("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude", "body", "Here we have stuff")).getAllFields());
        final Map<String, Object> comment4Vals = ImmutableMap.copyOf(UtilsForTests.getTestEntity(
                "Action",
                ImmutableMap.of("issue", issueGV2.getLong("id"), "type", ActionConstants.TYPE_COMMENT, "author", "somedude2", "body",
                        "Here we have another stuff")).getAllFields());
        final GenericValue commentGV5 = UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV2.getLong("id"), "type",
                ActionConstants.TYPE_COMMENT, "author", "somedude3", "body", "Here we have abc stuff"));
        final Map<String, Object> comment5Vals = ImmutableMap.copyOf(commentGV5.getAllFields());
        final GenericValue commentGV6 = UtilsForTests.getTestEntity("Action", ImmutableMap.of("issue", issueGV2.getLong("id"), "type",
                ActionConstants.TYPE_COMMENT, "author", "somedude4", "body", "Here we have xyz stuff"));
        final Map<String, Object> comment6Vals = ImmutableMap.copyOf(commentGV6.getAllFields());

        final Documents issue2Documents = indexIssue(issueGV2, 2, 6);

        // Assert that the right values are in the index
        assertNotNull(issue2Documents);
        assertIssueDocumentEquals(issue2Documents.issue, ImmutableMap.of("id", new Long(ISSUE2_ID), "key", "ABC-8000", "project", project.getLong("id"),
                "description", "This is the another body", "summary", "Another Issue"));

        // Assert issue2's comments
        assertNotNull(issue2Documents.comments);
        assertEquals(4, issue2Documents.comments.size());
        {
            final Iterator<Document> iterator = issue2Documents.comments.iterator();
            assertCommentDocumentEquals(iterator.next(), comment3Vals);
            assertCommentDocumentEquals(iterator.next(), comment4Vals);
            assertCommentDocumentEquals(iterator.next(), comment5Vals);
            assertCommentDocumentEquals(iterator.next(), comment6Vals);
        }
        // assert document changed
        issueGV2.set("key", "ABC-1234");
        issueGV2.set("project", 11L);
        issueGV2.set("description", "no longer stuffed");
        issueGV2.set("summary", "no, really!");
        issueGV2.store();

        ofBizDelegator.removeValue(commentGV5);

        commentGV6.set("author", "somebabe");
        commentGV6.set("body", "Here we don't have anything much at all");
        // Need to store the comment - as the reindex of an issue pulls issue's comment from the database
        commentGV6.store();

        // Do the right thing
        final MockIssue issueToReindex = new MockIssue();
        issueToReindex.setGenericValue(issueGV2);

        final AtomicBoolean methodCalled = new AtomicBoolean(false);
        final AtomicBoolean lockAvailable = new AtomicBoolean(false);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new MemoryIssueIndexer(), indexPath,
                mockReindexMessageManager, eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager)
        {
            @Override
            boolean getIndexLock()
            {
                methodCalled.set(true);
                return lockAvailable.get();
            }
            @Override
            void releaseIndexLock()
            {

            }
        };

        indexManager.reIndex(issueToReindex);

        // Ensure the method was called
        Assert.assertTrue(methodCalled.get());

        lockAvailable.set(true);
        // Ensure that nothing has changed as the lock was not obtained
        IndexSearcher indexSearcher = null;
        try
        {
            Document issueDocument = assertIndexContainsIssue(2, ISSUE2_ID);
            // Assert that the right values are in the index
            assertNotNull(issueDocument);
            assertIssueDocumentEquals(issueDocument, removeNum(issue2Vals));
            // Assert issue2's comments
            // Open a seracher and find all issue2's documents
            indexSearcher = new IndexSearcher(getCommentsIndexDirectory());
            TopDocs hits = indexSearcher.search(new TermQuery(new Term(ISSUE_ID, ISSUE2_ID)), Integer.MAX_VALUE);
            assertNotNull(hits);
            assertEquals(4, hits.totalHits);
            {
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[0].doc), comment3Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[1].doc), comment4Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[2].doc), comment5Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[3].doc), comment6Vals);
            }
            issueDocument = assertIndexContainsIssue(2, ISSUE1_ID);
            // Assert that the right values are in the index
            assertNotNull(issueDocument);
            assertIssueDocumentEquals(issueDocument, removeNum(issue1Vals));

            // Assert issue1's comments
            hits = indexSearcher.search(new TermQuery(new Term(ISSUE_ID, ISSUE1_ID)), Integer.MAX_VALUE);
            assertNotNull(hits);
            assertEquals(2, hits.totalHits);
            {
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[0].doc), comment1Vals);
                assertCommentDocumentEquals(indexSearcher.doc(hits.scoreDocs[1].doc), comment2Vals);
            }
        }
        finally
        {
            if (indexSearcher != null)
            {
                indexSearcher.close();
            }
        }
    }

    private Map<String, ?> removeNum(Map<String, ?> issue2Vals)
    {
        issue2Vals = new HashMap<String, Object>(issue2Vals);
        issue2Vals.remove("number");
        return issue2Vals;
    }

    @Test
    public void testReindexThatDoesntGetLockCallsToStringOnIssuesIterable() throws Exception
    {
        final AtomicBoolean methodCalled = new AtomicBoolean(false);
        final AtomicBoolean toStringCalled = new AtomicBoolean(false);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new MemoryIssueIndexer(), indexPath,
                mockReindexMessageManager, eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager)
        {
            boolean getIndexLock()
            {
                methodCalled.set(true);
                return false;
            }
        };

        final IssuesIterable mockIssuesIterable = new IssuesIterable()
        {
            public void foreach(final Consumer<Issue> sink)
            {
                throw new UnsupportedOperationException();
            }

            public int size()
            {
                throw new UnsupportedOperationException();
            }

            public boolean isEmpty()
            {
                throw new UnsupportedOperationException();
            }

            public String toString()
            {
                toStringCalled.set(true);
                return "I'm a teapot";
            }
        };

        final long result = indexManager.reIndexIssues(mockIssuesIterable, Contexts.nullContext());

        Assert.assertTrue(methodCalled.get());
        Assert.assertTrue(toStringCalled.get());
        assertEquals(-1L, result);
    }

    @Test
    public void testDeactivate() throws Exception
    {
        // add a mock Listener to the manager
        final MockListenerManager listenerManager = new MockListenerManager();
        listenerManager.addListener("MyListener", new IssueIndexListener());

        final AtomicInteger shutDownCalled = new AtomicInteger(0);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new UnimplementedIssueIndexer()
        {
            @Override
            public void shutdown()
            {
                shutDownCalled.incrementAndGet();
            }
        }, indexPath, mockReindexMessageManager, eventPublisher, listenerManager, null,
                issueManager, null, ofBizDelegator, mockReplicatedIndexManager);

        assertEquals(0, shutDownCalled.get());

        assertEquals(1, listenerManager.getListeners().size());
        indexManager.deactivate();
        assertEquals(0, listenerManager.getListeners().size());

        assertEquals(1, shutDownCalled.get());
    }

    @Test
    public void testActivateIndexesActive() throws Exception
    {
        when(mockSchedulerService.getState()).thenReturn(SHUTDOWN);
        try
        {
            indexManager.activate(Contexts.nullContext());
            Assert.fail("IllegalStateException must have been thrown.");
        }
        catch (final IllegalStateException e)
        {
            assertEquals("Cannot enable indexing as it is already enabled.", e.getMessage());
        }
    }

    @Test
    public void testActivate() throws Exception
    {
        final AtomicReference<Context> reindexAllContext = new AtomicReference<Context>(null);
        final MockListenerManager listenerManager = new MockListenerManager();
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration()
        {
            {
                disableIndex();
            }
        }, new UnimplementedIssueIndexer()
        {
            @Override
            public void setIndexRootPath(final String path)
            {
                assertNotNull(path);
                assertEquals(indexDirectory.getRoot().getAbsolutePath(), path);
            }
        }, indexPath, mockReindexMessageManager, eventPublisher,
                listenerManager,
                null,
                issueManager, null,
                ofBizDelegator,
                mockReplicatedIndexManager
        )
        {
            @Override
            public long reIndexAll(final Context context, boolean useBackgroundIndexing)
            {
                reindexAllContext.compareAndSet(null, context);
                return 10L;
            }
        };

        when(mockSchedulerService.getState()).thenReturn(SHUTDOWN);

        assertEquals(0, listenerManager.getListeners().size());
        final Context ctx = Contexts.nullContext();
        indexManager.activate(ctx);
        assertEquals(1, listenerManager.getListeners().size());
        assertEquals(IssueIndexListener.class, listenerManager.getListeners().get(IssueIndexListener.NAME).getClass());

        assertSame(ctx, reindexAllContext.get());
    }

    @Test
    public void testActivateDontShutdownScheduler() throws Exception
    {
        prepareMockIndexManager();
        final MockListenerManager listenerManager = new MockListenerManager();

        when(mockSchedulerService.getState()).thenReturn(SHUTDOWN);

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration()
        {
            {
                disableIndex();
            }
        }, new DefaultIssueIndexer(indexDirectoryFactory,
                new MemoryIssueIndexer.CommentRetrieverImpl(issueManager),
                new MemoryIssueIndexer.ChangeHistoryRetrieverImpl(issueManager),
                mockApplicationProperties,
                new DefaultIssueDocumentFactory(searchExtractorManager),
                new DefaultCommentDocumentFactory(searchExtractorManager),
                new DefaultChangeHistoryDocumentFactory(searchExtractorManager)),
                indexPath, mockReindexMessageManager, eventPublisher, listenerManager, mockProjectManager, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager);

        assertEquals(0, listenerManager.getListeners().size());
        indexManager.activate(Contexts.nullContext());
        assertEquals(1, listenerManager.getListeners().size());
        assertEquals(IssueIndexListener.class, listenerManager.getListeners().get(IssueIndexListener.NAME).getClass());
    }

    @Test
    public void testActivateShutdownScheduler() throws Exception
    {
        prepareMockIndexManager();
        final MockListenerManager listenerManager = new MockListenerManager();

        when(mockSchedulerService.getState()).thenReturn(STARTED);

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration()
        {
            {
                disableIndex();
            }
        }, new DefaultIssueIndexer(indexDirectoryFactory,
                new MemoryIssueIndexer.CommentRetrieverImpl(issueManager),
                new MemoryIssueIndexer.ChangeHistoryRetrieverImpl(issueManager),
                mockApplicationProperties,
                new DefaultIssueDocumentFactory(searchExtractorManager),
                new DefaultCommentDocumentFactory(searchExtractorManager),
                new DefaultChangeHistoryDocumentFactory(searchExtractorManager)),
                indexPath, mockReindexMessageManager, eventPublisher, listenerManager, mockProjectManager,
                issueManager, null, ofBizDelegator, mockReplicatedIndexManager);
        assertEquals(0, listenerManager.getListeners().size());
        indexManager.activate(Contexts.nullContext());
        assertEquals(1, listenerManager.getListeners().size());
        assertEquals(IssueIndexListener.class, listenerManager.getListeners().get(IssueIndexListener.NAME).getClass());

        verify(mockSchedulerService).standby();
        verify(mockSchedulerService).start();
    }

    @Test
    public void testShutdownCallsIndexClose() throws Exception
    {

        final AtomicInteger closeCalledCount = new AtomicInteger(0);
        final AtomicInteger writerCallCount = new AtomicInteger(0);

        final IssueIndexer mockIssueIndexer = new MemoryIssueIndexer()
        {
            @Override
            public void shutdown()
            {
                closeCalledCount.incrementAndGet();
            }

            @Override
            public Result reindexIssues(@Nonnull final EnclosedIterable<Issue> issues, @Nonnull final Context event,
                    boolean reIndexComments, boolean reIndexChangeHistory, boolean conditionalUpdate)
            {
                writerCallCount.incrementAndGet();
                return new MockResult();
            }
        };

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), mockIssueIndexer, indexPath,
                mockReindexMessageManager, eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager);

        assertEquals(0, writerCallCount.get());
        indexManager.shutdown();
        assertEquals(1, closeCalledCount.get());
        assertEquals(0, writerCallCount.get());

        // This should create the LuceneConnections
        indexManager.reIndex(new MockIssue());
        assertEquals(1, writerCallCount.get());
        assertEquals(1, closeCalledCount.get());
        indexManager.shutdown();
        // The close() method should be invoked twice as in this test we return the same LuceneConnection for
        // both issue index and comment index
        assertEquals("Should have closed our connections", 2, closeCalledCount.get());
        assertEquals(1, writerCallCount.get());
    }

    @Test
    public void testOptimizeReturnsZeroIfIndexingIsDisabled() throws Exception
    {
        final IssueIndexer mockIssueIndexer = mock(IssueIndexer.class);

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration()
        {
            {
                disableIndex();
            }
        }, mockIssueIndexer, indexPath, mockReindexMessageManager, eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager);
        assertEquals(0, indexManager.optimize());

    }

    @Test
    public void testIsIndexingEnabledTrue()
    {
        final IssueIndexer mockIssueIndexer = mock(IssueIndexer.class);

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), mockIssueIndexer, indexPath,
                mockReindexMessageManager, eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager);
        Assert.assertTrue(indexManager.isIndexingEnabled());

    }

    @Test
    public void testIsIndexingEnabledFalse()
    {
        final IssueIndexer mockIssueIndexer = mock(IssueIndexer.class);

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration()
        {
            {
                disableIndex();
            }
        }, mockIssueIndexer, indexPath, mockReindexMessageManager, eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager);
        Assert.assertFalse(indexManager.isIndexingEnabled());
    }

    @Test
    public void testBackgroundReindexLock()
    {
        final AtomicBoolean lockCalled = new AtomicBoolean(false);
        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), new MemoryIssueIndexer(), indexPath,
                mockReindexMessageManager, eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager)
        {
            @Override
            boolean getIndexLock()
            {
                lockCalled.set(true);
                return false;
            }
        };
        Assert.assertEquals(-1, indexManager.reIndexAll(Contexts.nullContext(), true, false, false, false));
        Assert.assertTrue(lockCalled.get());
    }

    @Test
    public void testDeIndexChucksRuntimeExceptionNotIndexExFromIndexerDeleteAndReinit() throws Exception
    {
        final UnimplementedIssueIndexer issueIndexer = new UnimplementedIssueIndexer();

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), issueIndexer, indexPath,
                mockReindexMessageManager, eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager);

        try
        {
            indexManager.deIndex(new MockGenericValue("Issue", Collections.EMPTY_MAP));
            Assert.fail("UnsupportedOpException expected.");
        }
        catch (final UnsupportedOperationException yay)
        {
            // Expected
        }
    }

    @Test
    public void testMultiThreadedConfigurationDefaults()
    {
        final MultiThreadedIndexingConfiguration multiThreadedIndexingConfiguration = new DefaultIssueIndexer.PropertiesAdapter(new MockApplicationProperties());
        assertEquals(50, multiThreadedIndexingConfiguration.minimumBatchSize());
        assertEquals(1000, multiThreadedIndexingConfiguration.maximumQueueSize());
        assertEquals(20, multiThreadedIndexingConfiguration.noOfThreads());
    }

    @Test
    public void testMultiThreadedConfigurationCustom()
    {
        final MockApplicationProperties applicationProperties = new MockApplicationProperties();
        applicationProperties.setString(APKeys.JiraIndexConfiguration.Issue.MIN_BATCH_SIZE, "1");
        applicationProperties.setString(APKeys.JiraIndexConfiguration.Issue.MAX_QUEUE_SIZE, "2");
        applicationProperties.setString(APKeys.JiraIndexConfiguration.Issue.THREADS, "3");

        final MultiThreadedIndexingConfiguration multiThreadedIndexingConfiguration = new DefaultIssueIndexer.PropertiesAdapter(applicationProperties);
        assertEquals(1, multiThreadedIndexingConfiguration.minimumBatchSize());
        assertEquals(2, multiThreadedIndexingConfiguration.maximumQueueSize());
        assertEquals(3, multiThreadedIndexingConfiguration.noOfThreads());
    }

    @Test
    public void testDeIndexWithRuntimeExceptionClearsThreadLocalSearcher() throws Exception
    {
        _testDeIndexWithThrowableClearsThreadLocalSearcher(new RuntimeException());
    }

    @Test
    public void testDeIndexWithErrorClearsThreadLocalSearcher() throws Exception
    {
        _testDeIndexWithThrowableClearsThreadLocalSearcher(new ClassFormatError());
    }

    private void _testDeIndexWithThrowableClearsThreadLocalSearcher(final Throwable throwable) throws Exception
    {
        final RAMDirectory directory = new RAMDirectory();
        final IndexWriterConfig conf = new IndexWriterConfig(LuceneVersion.get(), null);
        conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        new IndexWriter(directory, conf).close();

        final AtomicInteger deIndexCalled = new AtomicInteger(0);
        final AtomicInteger getIssueSearcherCalled = new AtomicInteger(0);

        final IssueIndexer mockIssueIndexer = new UnimplementedIssueIndexer()
        {
            @Override
            public IndexSearcher openIssueSearcher()
            {
                getIssueSearcherCalled.incrementAndGet();
                try
                {
                    return new IndexSearcher(directory);
                }
                catch (final IOException e)
                {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Index.Result deindexIssues(final EnclosedIterable<Issue> issues, final Context event)
            {
                deIndexCalled.incrementAndGet();
                if (throwable instanceof Error)
                {
                    throw (Error) throwable;
                }
                else if (throwable instanceof RuntimeException)
                {
                    throw (RuntimeException) throwable;
                }
                else
                {
                    Assert.fail("Cannot throw a checked Exception");
                }
                return new MockResult();
            }
        };

        indexManager = new DefaultIndexManager(new MockIndexingConfiguration(), mockIssueIndexer, indexPath,
                mockReindexMessageManager, eventPublisher, null, null, issueManager, null,
                ofBizDelegator, mockReplicatedIndexManager);

        mockitoContainer.getMockComponentContainer().addMock(IssueIndexManager.class, indexManager);

        assertEquals(0, deIndexCalled.get());
        assertEquals(0, getIssueSearcherCalled.get());

        final IndexSearcher searcher = indexManager.getIssueSearcher();
        assertNotNull(searcher);
        assertEquals(1, getIssueSearcherCalled.get());

        final MockGenericValue mockIssueGV = new MockGenericValue("Issue", ImmutableMap.of("id", Id.TEN_THOUSAND));
        try
        {
            indexManager.deIndex(mockIssueGV);
        }
        catch (final Throwable yay)
        {
            assertSame(throwable, yay);
        }

        assertEquals(1, deIndexCalled.get());

        final IndexSearcher newSearcher = indexManager.getIssueSearcher();
        assertNotNull(newSearcher);
        Assert.assertNotSame(searcher, newSearcher);
        assertEquals(2, getIssueSearcherCalled.get());

    }

    private void assertIssueDocumentEquals(final Document document, final Map<String, ?> expectedFields)
    {
        assertDocumentEquals(document, expectedFields, ISSUE_DOCUMENT);
    }

    private void assertCommentDocumentEquals(final Document document, final Map<String, ?> expectedFields)
    {
        assertDocumentEquals(document, expectedFields, COMMENT_DOCUMENT);
    }

    private void assertDocumentEquals(final Document document, final Map<String, ?> expectedFields, final Map<String, String> DBToDocumentConstants)
    {
        assertNotNull(document);
        assertNotNull(expectedFields);
        for (final Map.Entry<String, ?> entry : expectedFields.entrySet())
        {
            String documentFieldKey = DBToDocumentConstants.get(entry.getKey());
            // Fall back to the actual key
            if (documentFieldKey == null)
            {
                documentFieldKey = entry.getKey();
            }
            if (!UNINDEXED.equals(documentFieldKey))
            {
                assertEquals("Did not get expected value for: " + documentFieldKey + " in doc:" + document, entry.getValue().toString(),
                        document.get(documentFieldKey));
            }
        }
    }

    private Document assertIndexContainsIssue(final int numDocs, final String issueId) throws IOException
    {
        IndexReader indexReader = null;
        try
        {
            SearcherCache.getThreadLocalCache().closeSearchers();
            indexManager.getIssueSearcher();
            indexReader = IndexReader.open(getIssueIndexDirectory());

            // Ensure the correct things are in the index before we proceed with the test
            if (indexReader.numDocs() != numDocs)
            {
                for (int i = 0; i < indexReader.numDocs(); i++)
                {
                    System.out.println(indexReader.document(i));
                }
            }
            assertEquals(numDocs, indexReader.numDocs());

            if (issueId != null)
            {
                for (int i = 0; i < indexReader.maxDoc(); ++i)
                {
                    if (!indexReader.isDeleted(i))
                    {
                        final Document doc = indexReader.document(i);
                        if (issueId.equals(doc.get(ISSUE_ID)))
                        {
                            return doc;
                        }
                    }
                }
                Assert.fail("could not find document with Issue ID: " + issueId);
            }

            throw new IllegalStateException("I so should not be here!!!! Issue id is null!");
        }
        finally
        {
            if (indexReader != null)
            {
                indexReader.close();
            }
        }
    }

    private Collection<Document> assertIndexContainsComments(final Collection<GenericValue> commentGVs, final int totalCommentCountInIndex)
            throws IOException
    {
        IndexSearcher indexSearcher = null;
        try
        {
            final List<Document> result = new ArrayList<Document>(commentGVs.size());
            indexManager.getCommentSearcher();
            indexSearcher = new IndexSearcher(getCommentsIndexDirectory());
            assertEquals(totalCommentCountInIndex, indexSearcher.getIndexReader().numDocs());

            for (final GenericValue commentGV : commentGVs)
            {
                final TopDocs hits = indexSearcher.search(new TermQuery(new Term(COMMENT_ID, commentGV.getLong("id").toString())), Integer.MAX_VALUE);
                assertNotNull(hits);
                assertEquals(1, hits.totalHits);
                result.add(indexSearcher.doc(hits.scoreDocs[0].doc));
            }

            return result;
        }
        finally
        {
            if (indexSearcher != null)
            {
                indexSearcher.close();
            }
        }
    }

    private Directory getIssueIndexDirectory() throws IOException
    {
        return issueDirectory;
    }

    private Directory getCommentsIndexDirectory() throws IOException
    {
        return commentDirectory;
    }

    private Directory getChangeIndexDirectory() throws IOException
    {
        return changesDirectory;
    }

    private static final class Id
    {
        static final Long ONE = 1L;
        static final Long TEN = 10L;
        static final Long HUNDRED = 100L;
        static final Long TEN_THOUSAND = 10000L;
    }

    static class Documents
    {
        final Document issue;
        final Collection<Document> comments;
        final Collection<Document> changes;

        Documents(final Document issue, final Collection<Document> comments)
        {
            this.issue = issue;
            this.comments = Collections.unmodifiableCollection(comments);
            this.changes = null;
        }

    }
}
