package com.atlassian.jira.issue.fields.screen.tab;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.util.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFieldScreenRenderLayoutItemFilterImpl
{
    private FieldScreenRenderLayoutItemFilterImpl layoutItemFilter;

    @Rule
    public InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);
    @Mock
    private FieldManager fieldManager;
    @Mock
    private FieldLayoutManager fieldLayoutManager;

    @Before
    public void setUp() throws Exception
    {
        layoutItemFilter = new FieldScreenRenderLayoutItemFilterImpl(fieldManager, fieldLayoutManager);
    }

    @Test
    public void shouldFilterOutItemsWithNullFields()
    {
        //given
        final Predicate<? super Field> condition = getAlwaysTrueCondition();
        final Set<Field> unavailableFields = Sets.newHashSet();

        final FieldScreenLayoutItem mockItemWithField = getMockItem();
        final List<FieldScreenLayoutItem> layoutItems = Lists.newArrayList(
                mockItemWithField,
                getMockItemWithNullField(),
                getMockItemWithNullField());

        when(fieldManager.getUnavailableFields())
                .thenReturn(unavailableFields);

        //when
        final Collection<FieldScreenLayoutItem> filtered = layoutItemFilter.filterAvailableFieldScreenLayoutItems(condition, layoutItems);

        //then
        assertThat(filtered, contains(mockItemWithField));
    }

    @Test
    public void shouldFilterOutUnavailableItems()
    {
        //given
        final Predicate<? super Field> condition = getAlwaysTrueCondition();

        final FieldScreenLayoutItem unavailableItem = getMockItem();
        final Set<Field> unavailableFields = Sets.newHashSet((Field)unavailableItem.getOrderableField());
        final List<FieldScreenLayoutItem> layoutItems = Lists.newArrayList(
                getMockItem(),
                unavailableItem,
                getMockItem());

        when(fieldManager.getUnavailableFields())
                .thenReturn(unavailableFields);

        //when
        final Collection<FieldScreenLayoutItem> filtered = layoutItemFilter.filterAvailableFieldScreenLayoutItems(condition, layoutItems);

        //then
        assertThat(filtered, allOf(not(hasItem(unavailableItem)), (Matcher<? super Iterable<? super FieldScreenLayoutItem>>) hasSize(2)));
    }

    @Test
    public void shouldFilterOutFieldsNotPassingCondition()
    {
        //given
        final FieldScreenLayoutItem mockPassingCondition = getMockItem();
        final Predicate<? super Field> condition = getConditionTrueForMock(mockPassingCondition);

        final Set<Field> unavailableFields = Sets.newHashSet();
        final List<FieldScreenLayoutItem> layoutItems = Lists.newArrayList(
                getMockItem(),
                mockPassingCondition,
                getMockItem());

        when(fieldManager.getUnavailableFields())
                .thenReturn(unavailableFields);

        //when
        final Collection<FieldScreenLayoutItem> filtered = layoutItemFilter.filterAvailableFieldScreenLayoutItems(condition, layoutItems);

        assertThat(filtered, contains(mockPassingCondition));
    }

    @Test
    public void shouldFilterHiddenFields()
    {
        //given
        final Issue issue = mock(Issue.class);
        FieldLayout fieldLayout = mockFieldLayout(issue);

        final FieldScreenLayoutItem mockHiddenItem = getMockHiddenItem(fieldLayout, issue);
        final FieldScreenLayoutItem mockVisibleItem = getMockVisibleItem(fieldLayout, issue);

        final Collection<FieldScreenLayoutItem> items = Lists.newArrayList(mockHiddenItem, mockVisibleItem);


        //when
        final Collection<FieldScreenLayoutItem> filtered = layoutItemFilter.filterVisibleFieldScreenLayoutItems(issue, items);

        //then
        assertThat(filtered, contains(mockVisibleItem));
    }

    @Test
    public void shouldFilterNotShownFields()
    {
        //given
        final Issue issue = mock(Issue.class);
        FieldLayout fieldLayout = mockFieldLayout(issue);

        final FieldScreenLayoutItem mockNotShownItem = getMockNotShownItem(fieldLayout, issue);
        final FieldScreenLayoutItem mockVisibleItem = getMockVisibleItem(fieldLayout, issue);

        final Collection<FieldScreenLayoutItem> items = Lists.newArrayList(mockNotShownItem, mockVisibleItem);


        //when
        final Collection<FieldScreenLayoutItem> filtered = layoutItemFilter.filterVisibleFieldScreenLayoutItems(issue, items);

        //then
        assertThat(filtered, contains(mockVisibleItem));
    }

    @Test
    public void shouldSwallowExceptionAndFilterOutFieldsThrowingSuchWhenFilteringAvailableFields()
    {
        //given
        final Predicate<? super Field> condition = getAlwaysTrueCondition();
        final Set<Field> unavailableFields = Sets.newHashSet();

        final FieldScreenLayoutItem mockItemWithField = getMockItem();
        final List<FieldScreenLayoutItem> layoutItems = Lists.newArrayList(
                mockItemWithField,
                getMockItemThrowingException());

        when(fieldManager.getUnavailableFields())
                .thenReturn(unavailableFields);

        //when
        final Collection<FieldScreenLayoutItem> filtered = layoutItemFilter.filterAvailableFieldScreenLayoutItems(condition, layoutItems);

        //then
        assertThat(filtered, contains(mockItemWithField));
    }

    @Test
    public void shouldSwallowExceptionAndFilterOutFieldsThrowingSuchWhenFilteringVisibleFields()
    {
        //given
        final Issue issue = mock(Issue.class);
        FieldLayout fieldLayout = mockFieldLayout(issue);

        final FieldScreenLayoutItem itemThrowingException = getMockItemThrowingException();
        final FieldScreenLayoutItem mockVisibleItem = getMockVisibleItem(fieldLayout, issue);

        final Collection<FieldScreenLayoutItem> items = Lists.newArrayList(itemThrowingException, mockVisibleItem);


        //when
        final Collection<FieldScreenLayoutItem> filtered = layoutItemFilter.filterVisibleFieldScreenLayoutItems(issue, items);

        //then
        assertThat(filtered, contains(mockVisibleItem));
    }

    private FieldScreenLayoutItem getMockItemThrowingException()
    {
        FieldScreenLayoutItem item = getMockItemWithNullField();
        when(item.getOrderableField())
                .thenThrow(new RuntimeException());
        return item;
    }

    private FieldScreenLayoutItem getMockNotShownItem(final FieldLayout fieldLayout, final Issue issue)
    {
        boolean hidden = false;
        boolean shown = false;
        return getMockItem(hidden, shown, fieldLayout, issue);
    }

    private FieldScreenLayoutItem getMockVisibleItem(final FieldLayout fieldLayout, final Issue issue)
    {
        boolean hidden = false;
        boolean shown = true;
        return getMockItem(hidden, shown, fieldLayout, issue);
    }

    private FieldScreenLayoutItem getMockHiddenItem(final FieldLayout fieldLayout, final Issue issue)
    {
        boolean hidden = true;
        boolean shown = true;
        return getMockItem(hidden, shown, fieldLayout, issue);
    }

    private FieldScreenLayoutItem getMockItem(boolean hidden, boolean shown, final FieldLayout fieldLayout, final Issue issue)
    {
        final FieldScreenLayoutItem item = getMockItem();

        final FieldLayoutItem layoutItem = mock(FieldLayoutItem.class);
        when(fieldLayout.getFieldLayoutItem(item.getOrderableField()))
                .thenReturn(layoutItem);

        when(layoutItem.isHidden())
                .thenReturn(hidden);

        when(item.isShown(issue))
                .thenReturn(shown);

        return item;
    }

    private FieldLayout mockFieldLayout(final Issue issue)
    {
        final FieldLayout fieldLayout = mock(FieldLayout.class);
        when(fieldLayoutManager.getFieldLayout(issue))
                .thenReturn(fieldLayout);

        return fieldLayout;
    }

    private FieldScreenLayoutItem getMockItem()
    {
        final FieldScreenLayoutItem layoutItem = mock(FieldScreenLayoutItem.class);

        when(layoutItem.getOrderableField())
                .thenReturn(mock(OrderableField.class));

        return layoutItem;
    }

    private FieldScreenLayoutItem getMockItemWithNullField()
    {
        final FieldScreenLayoutItem layoutItem = mock(FieldScreenLayoutItem.class);
        return layoutItem;
    }

    private Predicate<? super Field> getConditionTrueForMock(final FieldScreenLayoutItem mockPassingCondition)
    {
        return new Predicate<Field>()
        {
            @Override
            public boolean evaluate(final Field input)
            {
                return mockPassingCondition.getOrderableField().equals(input);
            }
        };
    }

    private Predicate<? super Field> getAlwaysTrueCondition()
    {
        return new Predicate<Field>()
        {
            @Override
            public boolean evaluate(final Field input)
            {
                return true;
            }
        };
    }
 }
