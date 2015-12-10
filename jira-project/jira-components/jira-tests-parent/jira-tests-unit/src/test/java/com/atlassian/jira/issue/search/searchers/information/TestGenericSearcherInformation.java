package com.atlassian.jira.issue.search.searchers.information;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import com.atlassian.jira.issue.fields.SearchableField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.CreatedDateIndexer;
import com.atlassian.jira.issue.index.indexers.impl.DueDateIndexer;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.mock.issue.fields.MockSearchableField;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.CollectionBuilder;

import org.easymock.classextension.EasyMock;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.issue.search.searchers.information.GenericSearcherInformation}.
 *
 * @since v4.0
 */
public class TestGenericSearcherInformation
{
    @Test
    public void testConstrcutor()
    {

        String fieldId = "id";
        String fieldKey = "id.name";
        GenericSearcherInformation<SearchableField> information = new GenericSearcherInformation<SearchableField>(fieldId, fieldKey, Collections.<Class<? extends FieldIndexer>>emptyList(),
                new AtomicReference<SearchableField>(), SearcherGroupType.CUSTOM);

        assertEquals(fieldId, information.getId());
        assertEquals(fieldKey, information.getNameKey());
        assertSame(SearcherGroupType.CUSTOM, information.getSearcherGroupType());
        assertTrue(information.getRelatedIndexers().isEmpty());
    }

    @Test
    public void testConstructorBadArgs()
    {

        try
        {
            new GenericSearcherInformation<SearchableField>("id", "key",
                    Collections.<Class<? extends FieldIndexer>>emptyList(),
                    new AtomicReference<SearchableField>(), null);
            fail("Expected exception.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new GenericSearcherInformation<SearchableField>(null, "key",
                    Collections.<Class<? extends FieldIndexer>>emptyList(),
                    new AtomicReference<SearchableField>(), SearcherGroupType.DATE);
            fail("Expected exception.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new GenericSearcherInformation<SearchableField>("", "key",
                    Collections.<Class<? extends FieldIndexer>>emptyList(),
                    new AtomicReference<SearchableField>(), SearcherGroupType.DATE);
            fail("Expected exception.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new GenericSearcherInformation<SearchableField>("id", "",
                    Collections.<Class<? extends FieldIndexer>>emptyList(),
                    new AtomicReference<SearchableField>(), SearcherGroupType.DATE);
            fail("Expected exception.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new GenericSearcherInformation<SearchableField>("id", null,
                    Collections.<Class<? extends FieldIndexer>>emptyList(),
                    new AtomicReference<SearchableField>(), SearcherGroupType.DATE);
            fail("Expected exception.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new GenericSearcherInformation<SearchableField>("id", "key",
                    null,
                    new AtomicReference<SearchableField>(), SearcherGroupType.DATE);
            fail("Expected exception.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

        try
        {
            new GenericSearcherInformation<SearchableField>("id", "key",
                    Collections.<Class<? extends FieldIndexer>>emptyList(),
                    null, SearcherGroupType.DATE);
            fail("Expected exception.");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }
    }

    @Test
    public void testGetRelatedIndexers() throws Exception
    {
        List<Class<? extends FieldIndexer>> classList = CollectionBuilder.<Class<? extends FieldIndexer>>newBuilder(CreatedDateIndexer.class, DueDateIndexer.class).asList();

        GenericSearcherInformation<SearchableField> info = new InstanceSearcherInformation("id", "key",
                    classList, new MockSimpleAuthenticationContext(null, Locale.US),
                    new AtomicReference<SearchableField>(), SearcherGroupType.DATE);

        List<FieldIndexer> fieldIndexerList = info.getRelatedIndexers();
        Iterator<Class<? extends FieldIndexer>> classIterator = classList.iterator();
        assertEquals(classList.size(), fieldIndexerList.size());
        for (FieldIndexer fieldIndexer : fieldIndexerList)
        {
            assertTrue(classIterator.next().isAssignableFrom(fieldIndexer.getClass()));
        }
    }

    @Test
    public void testGetField() throws Exception
    {
        AtomicReference<SearchableField> fieldReference = new AtomicReference<SearchableField>();


        GenericSearcherInformation<SearchableField> information = new GenericSearcherInformation<SearchableField>("id", "name.key", Collections.<Class<? extends FieldIndexer>>emptyList(),
                fieldReference, SearcherGroupType.CUSTOM);

        assertNull(information.getField());
        SearchableField field1 = new MockSearchableField("id2");
        fieldReference.set(field1);
        assertSame(field1, information.getField());
        SearchableField field2 = new MockSearchableField("id3");
        fieldReference.set(field2);
        assertSame(field2, information.getField());

    }
    
    private static class InstanceSearcherInformation extends GenericSearcherInformation<SearchableField>
    {
        public InstanceSearcherInformation(String id, String nameKey, List<Class<? extends FieldIndexer>> indexers, JiraAuthenticationContext authenticationContext,
                final AtomicReference<SearchableField> fieldReference, final SearcherGroupType searcherGroupType)
        {
            super(id, nameKey, indexers, fieldReference, searcherGroupType);
        }

        @Override
        FieldIndexer loadIndexer(final Class<? extends FieldIndexer> clazz)
        {
            return EasyMock.createMock(clazz);
        }
    }

}
