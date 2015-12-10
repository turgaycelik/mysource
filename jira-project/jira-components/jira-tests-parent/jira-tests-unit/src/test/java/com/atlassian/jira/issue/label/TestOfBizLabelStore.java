package com.atlassian.jira.issue.label;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.label.OfBizLabelStore.Columns;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;

import org.easymock.IExpectationSetters;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @since v4.2
 */
public class TestOfBizLabelStore
{
    private static final Long ISSUE_1_ID = 10000L;
    private static final Long ISSUE_2_ID = 10001L;
    private static final Long CUSTOM_FIELD_1_ID = null;
    private static final Long CUSTOM_FIELD_2_ID = 20001L;

    private static enum Data
    {
        FOO(1L, ISSUE_1_ID, CUSTOM_FIELD_1_ID, "foo"),
        BAR(2L, ISSUE_1_ID, CUSTOM_FIELD_1_ID, "bar"),
        BAR_UPPERCASE(MockOfBizDelegator.STARTING_ID, ISSUE_1_ID, CUSTOM_FIELD_1_ID, BAR.labelString.toUpperCase()),
        QUX(3L, ISSUE_2_ID, CUSTOM_FIELD_2_ID, "qux");

        long labelId;
        long issueId;
        Long customFieldId;
        String labelString;
        GenericValue genericValue;
        Label label;

        Data(long labelId, long issueId, Long customFieldId, String labelString)
        {
            this.labelId = labelId;
            this.issueId = issueId;
            this.customFieldId = customFieldId;
            this.labelString = labelString;
            this.genericValue = new MockGenericValue(OfBizLabelStore.TABLE, MapBuilder.newBuilder()
                    .add(Columns.ID, labelId)
                    .add(Columns.ISSUE_ID, issueId)
                    .add(Columns.CUSTOM_FIELD_ID, customFieldId)
                    .add(Columns.LABEL, labelString)
                    .toMap());
            this.label = new Label(labelId, issueId, customFieldId, labelString);
        }
    }

    private OfBizLabelStore store;
    private OfBizDelegator mockDelegate;

    @Before
    public void setUp() throws Exception
    {
        mockDelegate = createMock(OfBizDelegator.class);
        store = new OfBizLabelStore(mockDelegate);
    }

    @Test
    public void testSortsResults()
    {
        expectFindAllLabels(mockDelegate)
                .andReturn(Arrays.asList(Data.FOO.genericValue, Data.BAR.genericValue));
        replay(mockDelegate);

        assertEquals(
                Arrays.asList(Data.BAR.label, Data.FOO.label),
                new ArrayList<Label>(store.getLabels(ISSUE_1_ID, CUSTOM_FIELD_1_ID))
        );
        verify(mockDelegate);
    }

    @Test
    public void testAddNewLabel()
    {
        expectFindLabel(mockDelegate, Data.BAR.labelString)
                .andReturn(Collections.<GenericValue>emptyList());
        expectAddLabel(mockDelegate, Data.BAR.labelString)
                .andReturn(Data.BAR.genericValue);
        replay(mockDelegate);

        assertEquals(Data.BAR.label, store.addLabel(Data.BAR.issueId, Data.BAR.customFieldId, Data.BAR.labelString));
        verify(mockDelegate);
    }

    @Test
    public void testBlankLabelsIgnored()
    {
        mockDelegate = new MockOfBizDelegator(
                Collections.<GenericValue>emptyList(),
                Arrays.asList(Data.BAR_UPPERCASE.genericValue)
        );
        store = new OfBizLabelStore(mockDelegate);

        assertEquals(
                Collections.singleton(Data.BAR_UPPERCASE.label),
                store.setLabels(ISSUE_1_ID, CUSTOM_FIELD_1_ID, CollectionBuilder.newBuilder(Data.BAR_UPPERCASE.labelString, "", " ").asSet())
        );
        ((MockOfBizDelegator) mockDelegate).verifyAll();
    }

    @Test
    public void testAddIsCaseSensitive()
    {
        mockDelegate = new MockOfBizDelegator(
                Arrays.asList(Data.FOO.genericValue, Data.BAR.genericValue),
                Arrays.asList(Data.FOO.genericValue, Data.BAR.genericValue, Data.BAR_UPPERCASE.genericValue)
        );
        store = new OfBizLabelStore(mockDelegate);

        assertEquals(Data.BAR_UPPERCASE.labelString, store.addLabel(Data.BAR_UPPERCASE.issueId, Data.BAR_UPPERCASE.customFieldId, Data.BAR_UPPERCASE.labelString).getLabel());
        ((MockOfBizDelegator) mockDelegate).verifyAll();
    }

    @Test
    public void testAddExistingLabelNoop()
    {
        expectFindLabel(mockDelegate, Data.BAR.labelString)
                .andReturn(Arrays.asList(Data.BAR.genericValue));
        replay(mockDelegate);

        assertEquals(Data.BAR.label, store.addLabel(Data.BAR.issueId, Data.BAR.customFieldId, Data.BAR.labelString));
        verify(mockDelegate);
    }

    @Test
    public void testRemoveLabelsForAnIssue()
    {
        mockDelegate = new MockOfBizDelegator(
                Arrays.asList(Data.FOO.genericValue, Data.BAR.genericValue, Data.QUX.genericValue),
                Arrays.asList(Data.QUX.genericValue)
        );
        store = new OfBizLabelStore(mockDelegate);

        assertEquals(Collections.<Label>emptySet(), store.setLabels(ISSUE_1_ID, CUSTOM_FIELD_1_ID, Collections.<String>emptySet()));
        ((MockOfBizDelegator) mockDelegate).verifyAll();
    }

    @Test
    public void testRemoveNonexistentLabel()
    {
        mockDelegate = new MockOfBizDelegator(
                Arrays.asList(Data.FOO.genericValue, Data.BAR.genericValue),
                Arrays.asList(Data.FOO.genericValue, Data.BAR.genericValue)
        );
        store = new OfBizLabelStore(mockDelegate);

        final Long nonexistentLabelId = 3L;
        assertFalse(nonexistentLabelId.equals(Data.FOO.labelId));
        assertFalse(nonexistentLabelId.equals(Data.BAR.labelId));
        store.removeLabel(nonexistentLabelId, ISSUE_1_ID, CUSTOM_FIELD_1_ID);
        ((MockOfBizDelegator) mockDelegate).verifyAll();
    }

    @Test
    public void testRemoveOnlyUsesLabelId()
    {
        mockDelegate = new MockOfBizDelegator(
                Arrays.asList(Data.FOO.genericValue, Data.BAR.genericValue),
                Arrays.asList(Data.FOO.genericValue)
        );
        store = new OfBizLabelStore(mockDelegate);

        Long nonexistentIssueId = 3L;
        Long nonexistentCustomFieldId = 10000L;
        assertFalse(nonexistentIssueId.equals(Data.BAR.issueId));
        assertFalse(nonexistentCustomFieldId.equals(Data.BAR.customFieldId));

        store.removeLabel(Data.BAR.labelId, nonexistentIssueId, nonexistentCustomFieldId);
        ((MockOfBizDelegator) mockDelegate).verifyAll();
    }

    private static IExpectationSetters<List<GenericValue>> expectFindAllLabels(OfBizDelegator mockDelegate)
    {
        return expect(mockDelegate.findByAnd(OfBizLabelStore.TABLE, MapBuilder.<String, Object>newBuilder()
                .add(Columns.ISSUE_ID, ISSUE_1_ID)
                .add(Columns.CUSTOM_FIELD_ID, CUSTOM_FIELD_1_ID)
                .toMap()
        ));
    }

    private static IExpectationSetters<List<GenericValue>> expectFindLabel(OfBizDelegator mockDelegate, String label)
    {
        return expect(mockDelegate.findByAnd(OfBizLabelStore.TABLE, MapBuilder.<String, Object>newBuilder()
                .add(Columns.ISSUE_ID, ISSUE_1_ID)
                .add(Columns.CUSTOM_FIELD_ID, CUSTOM_FIELD_1_ID)
                .add(Columns.LABEL, label)
                .toMap()
        ));
    }

    private static IExpectationSetters<GenericValue> expectAddLabel(OfBizDelegator mockDelegate, String label)
    {
        return expect(mockDelegate.createValue(OfBizLabelStore.TABLE, MapBuilder.<String, Object>newBuilder()
                .add(Columns.ISSUE_ID, ISSUE_1_ID)
                .add(Columns.CUSTOM_FIELD_ID, CUSTOM_FIELD_1_ID)
                .add(Columns.LABEL, label)
                .toMap()
        ));
    }

}
