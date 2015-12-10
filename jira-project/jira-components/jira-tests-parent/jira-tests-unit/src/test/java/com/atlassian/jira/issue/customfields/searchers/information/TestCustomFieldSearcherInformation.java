package com.atlassian.jira.issue.customfields.searchers.information;

import java.util.concurrent.atomic.AtomicReference;

import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.IssueTypeIndexer;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;

import com.google.common.collect.ImmutableList;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since v4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCustomFieldSearcherInformation
{
    private static final String SEARCHER_ID = "searcherId";
    private static final String NAME_KEY = "nameKey";

    @Mock CustomFieldType<?,?> customFieldType;
    @Mock CustomField customField;

    @After
    public void tearDown()
    {
        customFieldType = null;
        customField = null;
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalArgumentException.class)
    public void testNullIndexers() throws Exception
    {
        final AtomicReference<CustomField> fieldReference = new AtomicReference<CustomField>(null);
        new CustomFieldSearcherInformation(SEARCHER_ID, NAME_KEY, null, fieldReference);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    @Test(expected = IllegalStateException.class)
    public void testEmptyIndexers() throws Exception
    {
        final AtomicReference<CustomField> fieldReference = new AtomicReference<CustomField>(null);
        new CustomFieldSearcherInformation(SEARCHER_ID, NAME_KEY, ImmutableList.<FieldIndexer>of(), fieldReference);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetRelatedIndexersNullCustomField() throws Exception
    {
        final AtomicReference<CustomField> fieldReference = new AtomicReference<CustomField>(null);
        final CustomFieldSearcherInformation info = new CustomFieldSearcherInformation(SEARCHER_ID, NAME_KEY,
                ImmutableList.<FieldIndexer>of(new IssueTypeIndexer(null)), fieldReference);
        info.getRelatedIndexers();
    }

    @Test
    public void testGetRelatedIndexersFromCustomFieldType() throws Exception
    {
        final FieldIndexer fieldIndexer = mock(FieldIndexer.class);
        when(customField.getCustomFieldType()).thenReturn(customFieldType);
        when(customFieldType.getRelatedIndexers(customField)).thenReturn(ImmutableList.of(fieldIndexer));

        final AtomicReference<CustomField> fieldReference = new AtomicReference<CustomField>(customField);
        final CustomFieldSearcherInformation info = new CustomFieldSearcherInformation(SEARCHER_ID, NAME_KEY,
                ImmutableList.of(new IssueTypeIndexer(null)), fieldReference);
        assertThat(info.getRelatedIndexers(), contains(fieldIndexer));
    }

    @Test
    public void testGetRelatedIndexersFromProvided() throws Exception
    {
        final FieldIndexer fieldIndexer = mock(FieldIndexer.class);
        when(customField.getCustomFieldType()).thenReturn(customFieldType);
        when(customFieldType.getRelatedIndexers(customField)).thenReturn(null);

        final AtomicReference<CustomField> fieldReference = new AtomicReference<CustomField>(customField);
        final CustomFieldSearcherInformation info = new CustomFieldSearcherInformation(SEARCHER_ID, NAME_KEY,
                ImmutableList.of(fieldIndexer), fieldReference);
        assertThat(info.getRelatedIndexers(), contains(fieldIndexer));
    }

    @Test
    public void testGetSearcherGroupType() throws Exception
    {
        final AtomicReference<CustomField> fieldReference = new AtomicReference<CustomField>(null);
        final CustomFieldSearcherInformation info = new CustomFieldSearcherInformation(SEARCHER_ID, NAME_KEY,
                ImmutableList.of(new IssueTypeIndexer(null)), fieldReference);

        assertEquals(SearcherGroupType.CUSTOM, info.getSearcherGroupType());
    }
}
