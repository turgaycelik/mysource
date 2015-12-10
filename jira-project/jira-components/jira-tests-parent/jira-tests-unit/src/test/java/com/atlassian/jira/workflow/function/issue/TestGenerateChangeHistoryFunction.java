/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.fugue.Pair;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.DefaultChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestGenerateChangeHistoryFunction
{

    @Mock
    @AvailableInContainer
    public FieldManager fieldManager;

    @Mock
    @AvailableInContainer
    private FieldLayoutManager fieldLayoutManager;

    @Mock
    @AvailableInContainer
    private OfBizDelegator ofBizDelegator;

    @Mock
    @AvailableInContainer
    private HistoryMetadataManager historyMetadataManager;

    @Rule
    public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    FieldLayout issueFieldLayout;

    @Mock
    MutableIssue issue;

    Map<String, Object> transientVars;
    GenerateChangeHistoryFunction function = new GenerateChangeHistoryFunction();


    @Before
    public void setUp() throws Exception
    {
        transientVars = Maps.newHashMap();
        transientVars.put("issue", issue);

        when(fieldLayoutManager.getFieldLayout(issue)).thenReturn(issueFieldLayout);
    }

    @Test
    public void shouldSetUpdatedValueAndStoreIssueOnEachCall()
    {

        function.execute(transientVars, null, null);

        verify(issue).setUpdated(any(Timestamp.class));
        verify(issue).store();
    }

    @Test
    public void shouldResetModifiedFieldsAfterSave()
    {
        when(issue.getModifiedFields()).thenReturn(ImmutableMap.of(
                "abstractField1", new ModifiedValue("aaa", "bbb")
        ));
        when(fieldManager.isOrderableField("abstractField1")).thenReturn(false);

        function.execute(transientVars, null, null);

        verify(issue).resetModifiedFields();
    }


    @Test
    public void shouldPersistUpdateForEachField()
    {

        Pair<OrderableField, FieldLayoutItem> abstractField = mockField("abstractField");
        Pair<OrderableField, FieldLayoutItem> dummyField = mockField("dummyField");

        final ModifiedValue abstractFieldVal = new ModifiedValue("aaa", "bbb");
        final ModifiedValue dummyFieldVal = new ModifiedValue(null, "bbb");
        when(issue.getModifiedFields()).thenReturn(ImmutableMap.of(
                "abstractField", abstractFieldVal,
                "dummyField", dummyFieldVal
        ));
        when(fieldManager.isOrderableField("abstractField")).thenReturn(true);
        when(fieldManager.isOrderableField("dummyField")).thenReturn(true);

        function.execute(transientVars, null, null);

        verify(abstractField.left()).updateValue(same(abstractField.right()), same(issue), same(abstractFieldVal), any(IssueChangeHolder.class));
        verify(dummyField.left()).updateValue(same(dummyField.right()), same(issue), same(dummyFieldVal), any(IssueChangeHolder.class));
    }

    @Test
    public void shouldPassChangeItemsToChangeHoldersAndExposeThemInUpdates()
    {
        Pair<OrderableField, FieldLayoutItem> abstractField = mockField("abstractField");
        final ModifiedValue abstractFieldVal = new ModifiedValue("aaa", "bbb");
        when(issue.getModifiedFields()).thenReturn(ImmutableMap.of(
                "abstractField", abstractFieldVal
        ));
        when(issue.getGenericValue()).thenReturn(new MockGenericValue("issue"));
        when(fieldManager.isOrderableField("abstractField")).thenReturn(true);

        //put changeitems into context
        List<ChangeItemBean> changeItems = ImmutableList.of(new ChangeItemBean("abc", "cde", "aaa", "bbb"));
        transientVars.put("changeItems", changeItems);

        //we have to mock a quite nice amout of world as this function uses static methods :(
        final MockGenericValue changeGroup = new MockGenericValue("ChangeGroup");
        when(ofBizDelegator.createValue(eq("ChangeGroup"), anyMap())).thenReturn(changeGroup);

        function.execute(transientVars, null, null);

        verify(abstractField.left()).updateValue(same(abstractField.right()), same(issue), same(abstractFieldVal), argThat(new ChangeItemBeanMatcher(Matchers.sameInstance(changeItems))));
        verify(ofBizDelegator).createValue(eq("ChangeItem"), anyMap());
    }

    @Test
    public void shouldPersistMetadataIfProvided() throws Exception
    {
        // having
        final HistoryMetadata metadata = HistoryMetadata.builder("bar").build();

        transientVars.put(DefaultChangeHistoryManager.HISTORY_METADATA_KEY, metadata);
        transientVars.put("changeItems", ImmutableList.of(new ChangeItemBean()));
        when(issue.getGenericValue()).thenReturn(new MockGenericValue("issue"));
        when(ofBizDelegator.createValue(eq("ChangeGroup"), anyMap())).thenReturn(new MockGenericValue("ChangeGroup"));

        // when
        function.execute(transientVars, null, null);

        // then
        verify(historyMetadataManager).saveHistoryMetadata(anyLong(), any(ApplicationUser.class), same(metadata));
    }

    private static class ChangeItemBeanMatcher extends FeatureMatcher<IssueChangeHolder, List<ChangeItemBean>>
    {

        public ChangeItemBeanMatcher(final Matcher<? super List<ChangeItemBean>> subMatcher)
        {
            super(subMatcher, "IssueChangeHolder with changeItems", "changeItems");
        }

        @Override
        protected List<ChangeItemBean> featureValueOf(final IssueChangeHolder actual)
        {
            return actual.getChangeItems();
        }
    }


    private Pair<OrderableField, FieldLayoutItem> mockField(String fieldId)
    {
        final OrderableField field = mock(OrderableField.class);
        final FieldLayoutItem layoutItem = mock(FieldLayoutItem.class);
        when(fieldManager.getOrderableField(fieldId)).thenReturn(field);
        when(issueFieldLayout.getFieldLayoutItem(field)).thenReturn(layoutItem);
        return Pair.pair(field, layoutItem);
    }
}
