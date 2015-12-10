package com.atlassian.jira.sharing.index;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.sharing.MockSharedEntity;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.jira.sharing.SharedEntityAccessor;
import com.atlassian.jira.sharing.UnimplementedSharedEntityAccessor;
import com.atlassian.jira.sharing.index.DefaultSharedEntityIndexer.EntityDocumentFactory;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.jira.user.MockUser;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultSharedEntityIndexer
{
    private static final String TEST_DEFAULT_SHARED_ENTITY_INDEXER = "TestDefaultSharedEntityIndexer";
    private final TypeDescriptor<?> typeDescriptor = SharedEntity.TypeDescriptor.Factory.get().create(TEST_DEFAULT_SHARED_ENTITY_INDEXER);

    @Rule
    public final RuleChain mocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private SharedEntityAccessor.Factory accessorFactory;

    private final User user = new MockUser("testClear");
    private final  ApplicationUser appUser = new DelegatingApplicationUser("key", user);

    private SharedEntity entity;


    @Before
    public void setupMocks()
    {

        entity = new MockSharedEntity(1L, typeDescriptor, user, SharePermissions.PRIVATE)
        {
            @Override
            public String getName()
            {
                return "indexMeName";
            }

            @Override
            public String getDescription()
            {
                return "indexMeDescription";
            }
        };
        when(accessorFactory.getSharedEntityAccessor(typeDescriptor)).thenReturn(new UnimplementedSharedEntityAccessor());
    }

    @Test
    public void testGetSearcher() throws Exception
    {

        final SharedEntityIndexer indexer = getIndexerInstance();
        indexer.getSearcher(typeDescriptor);
    }

    @Test
    public void testClear() throws Exception
    {
        final SharedEntityIndexer indexer = getIndexerInstance();
        indexer.clear(typeDescriptor);
    }


    @Test
    public void testOptimize() throws Exception
    {
        @SuppressWarnings("unchecked")
        final DirectoryFactory directoryFactory = mock(DirectoryFactory.class);
        when(directoryFactory.get(typeDescriptor)).thenReturn(new RAMDirectory());

        final SharedEntityIndexer indexer = getIndexerInstance();
        indexer.optimize(typeDescriptor);
    }

    @Test
    public void testShutdown() throws Exception
    {
        final SharedEntityIndexer indexer = getIndexerInstance();
        indexer.shutdown(typeDescriptor);
    }

    @Test
    public void testIndex() throws Exception
    {
        final EntityDocumentFactory documentFactory = mock(EntityDocumentFactory.class);
        when(documentFactory.get(entity)).thenReturn(new DefaultSharedEntityIndexer.EntityDocument()
        {
            public Document getDocument()
            {
                return new Document();
            }

            public Term getIdentifyingTerm()
            {
                return new Term("test", "23");
            }

            public TypeDescriptor<?> getType()
            {
                return typeDescriptor;
            }
        });

        @SuppressWarnings("unchecked")
        final DirectoryFactory directoryFactory = mock(DirectoryFactory.class);
        when(directoryFactory.get(typeDescriptor)).thenReturn(new RAMDirectory());

        final SharedEntityIndexer indexer = getIndexerInstance();
        indexer.index(entity);
    }

    @Test
    public void testDeindex() throws Exception
    {
        final EntityDocumentFactory documentFactory = mock(EntityDocumentFactory.class);
        when(documentFactory.get(entity)).thenReturn(new DefaultSharedEntityIndexer.EntityDocument()
        {
            public Document getDocument()
            {
                return new Document();
            }

            public Term getIdentifyingTerm()
            {
                return new Term("test", "23");
            }

            public TypeDescriptor<?> getType()
            {
                return typeDescriptor;
            }
        });

        @SuppressWarnings("unchecked")
        final DirectoryFactory directoryFactory = mock(DirectoryFactory.class);
        when(directoryFactory.get(typeDescriptor)).thenReturn(new RAMDirectory());

        final SharedEntityIndexer indexer = getIndexerInstance();
        indexer.deIndex(entity);
    }

    /**
     * Test DefaultEntityDocumentFactory inner class
     */
    @Test
    public void testDefaultEntityDocumentFactoryGet()
    {
        final Document expectedDocument = new Document();
        final SharedEntityDocumentFactory sharedEntityDocumentFactory = mock(SharedEntityDocumentFactory.class);
        when(sharedEntityDocumentFactory.create(entity)).thenReturn(expectedDocument);

        final ShareTypeFactory expectedShareTypeFactory = mock(ShareTypeFactory.class);

        final DefaultSharedEntityIndexer.DefaultEntityDocumentFactory documentFactoryUnderTest = new DefaultSharedEntityIndexer.DefaultEntityDocumentFactory(
            expectedShareTypeFactory)
        {
            @Override
            SharedEntityDocumentFactory createDocumentFactory(final ShareTypeFactory shareTypeFactory)
            {
                assertSame(expectedShareTypeFactory, shareTypeFactory);
                return sharedEntityDocumentFactory;
            }
        };

        final DefaultSharedEntityIndexer.EntityDocument entityDocument = documentFactoryUnderTest.get(entity);
        assertNotNull(entityDocument);
        final Term term = entityDocument.getIdentifyingTerm();
        assertNotNull(term);
        assertEquals("id:1", term.toString());

        // cant call this because deep down in the Field builders is invokes the UserManager code and up comes JIRA
        final Document document = entityDocument.getDocument();
        assertNotNull(document);
        assertSame(expectedDocument, document);
    }

    //We need to call the correct constructor. Both constructors have the same number of arguments
    private DefaultSharedEntityIndexer getIndexerInstance()
    {
        return mock(DefaultSharedEntityIndexer.class);
    }

    @Test
    public void testFoo()
    {
        getIndexerInstance();
    }

}
