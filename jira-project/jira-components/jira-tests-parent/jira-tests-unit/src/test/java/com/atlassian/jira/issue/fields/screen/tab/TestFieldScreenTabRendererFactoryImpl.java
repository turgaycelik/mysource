package com.atlassian.jira.issue.fields.screen.tab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTabImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.Predicate;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFieldScreenTabRendererFactoryImpl
{

    private FieldScreenTabRendererFactoryImpl fieldScreenTabRendererFactory;

    @Rule
    public InitMockitoMocks initMockitoMocks = new InitMockitoMocks(this);
    @Mock
    private FieldScreenRenderLayoutItemFilterImpl filter;
    @Mock
    private FieldManager fieldManager;
    @Mock
    private FieldLayoutManager fieldLayoutManager;
    @Mock
    private Issue issue;
    @Mock
    private IssueOperation operation;
    @Mock
    private Predicate<? super Field> condition;
    @Mock
    private FieldScreenTab fieldScreenTab;
    @Mock
    private FieldLayout fieldLayout;
    @Mock
    private Project project;
    @Mock
    private IssueType issueType;


    private int currentTabPosition = 1;
    private List<FieldScreenLayoutItem> allTabItems;

    @Before
    public void setUp()
    {
        fieldScreenTabRendererFactory = new FieldScreenTabRendererFactoryImpl(filter, fieldManager, fieldLayoutManager);
        allTabItems = Lists.newArrayList();

        when(fieldScreenTab.getFieldScreenLayoutItems())
                .thenReturn(allTabItems);

        when(fieldLayoutManager.getFieldLayout(issue))
                .thenReturn(fieldLayout);

        when(issue.getProjectObject())
                .thenReturn(project);

        when(issue.getIssueTypeObject())
                .thenReturn(issueType);
    }

    @Test
    public void shouldCreateRendererForAllNotCustomFieldItems()
    {
        //given
        final IssueTabRendererDto issueTabRenderDto = getIssueTabRendererDto();
        final FieldScreenLayoutItem item1 = mockNotCustomFieldItem();
        final FieldScreenLayoutItem item2 = mockNotCustomFieldItem();

        mockEveryItemPassingTroughFilters(issueTabRenderDto);

        //when
        final FieldScreenRenderTabImpl tabRender = fieldScreenTabRendererFactory.createTabRender(issueTabRenderDto);

        //then
        assertTabRenderContainsFieldRenders(tabRender, item1, item2);
    }

    @Test
    public void shouldIgnoreCustomFieldThatAreNotInScopeOfProject()
    {
        //given
        final IssueTabRendererDto issueTabRenderDto = getIssueTabRendererDto();
        final FieldScreenLayoutItem item1 = mockNotCustomFieldItem();
        final FieldScreenLayoutItem notInScopeItem = mockCustomFieldNotInScope();

        mockEveryItemPassingTroughFilters(issueTabRenderDto);

        //when
        final FieldScreenRenderTabImpl tabRender = fieldScreenTabRendererFactory.createTabRender(issueTabRenderDto);

        //then
        assertTabRenderContainsFieldRenders(tabRender, item1);
    }

    @Test
    public void shouldIgnoreCustomFieldThatHaveNoValueDuringViewIssue()
    {
        //given
        operation = IssueOperations.VIEW_ISSUE_OPERATION;

        final IssueTabRendererDto issueTabRenderDto = getIssueTabRendererDto();
        final FieldScreenLayoutItem item1 = mockNotCustomFieldItem();
        final FieldScreenLayoutItem itemWithoutValue = mockCustomFieldInScopeWithoutValue();

        mockEveryItemPassingTroughFilters(issueTabRenderDto);

        //when
        final FieldScreenRenderTabImpl tabRender = fieldScreenTabRendererFactory.createTabRender(issueTabRenderDto);

        //then
        assertTabRenderContainsFieldRenders(tabRender, item1);
    }

    @Test
    public void shouldRenderCustomFieldThatHaveNoValueWhenIsAnyOtherOperationThanViewIssue()
    {
        //given
        operation = IssueOperations.CREATE_ISSUE_OPERATION;

        final IssueTabRendererDto issueTabRenderDto = getIssueTabRendererDto();
        final FieldScreenLayoutItem item1 = mockNotCustomFieldItem();
        final FieldScreenLayoutItem itemWithoutValue = mockCustomFieldInScopeWithoutValue();

        mockEveryItemPassingTroughFilters(issueTabRenderDto);

        //when
        final FieldScreenRenderTabImpl tabRender = fieldScreenTabRendererFactory.createTabRender(issueTabRenderDto);

        //then
        assertTabRenderContainsFieldRenders(tabRender, item1, itemWithoutValue);
    }

    @Test
    public void shouldCreateRendersForCustomFieldWithValue()
    {
        //given
        operation = IssueOperations.VIEW_ISSUE_OPERATION;

        final IssueTabRendererDto issueTabRenderDto = getIssueTabRendererDto();
        final FieldScreenLayoutItem itemWithValue = mockCustomFieldInScope("value1");
        final FieldScreenLayoutItem itemWithValue2 = mockCustomFieldInScope("value1");

        mockEveryItemPassingTroughFilters(issueTabRenderDto);

        //when
        final FieldScreenRenderTabImpl tabRender = fieldScreenTabRendererFactory.createTabRender(issueTabRenderDto);

        //then
        assertTabRenderContainsFieldRenders(tabRender, itemWithValue, itemWithValue2);
    }

    @Test
    public void shouldIgnoreAnyFieldsThrowingExceptions()
    {
        //given
        operation = IssueOperations.VIEW_ISSUE_OPERATION;

        final IssueTabRendererDto issueTabRenderDto = getIssueTabRendererDto();
        final FieldScreenLayoutItem itemThrowingException = mockCustomFieldInScope("value1");
        final FieldScreenLayoutItem itemNotThrowingAnything = mockCustomFieldInScope("value1");

        when(itemThrowingException.getOrderableField())
                .thenThrow(new RuntimeException());

        mockEveryItemPassingTroughFilters(issueTabRenderDto);

        //when
        final FieldScreenRenderTabImpl tabRender = fieldScreenTabRendererFactory.createTabRender(issueTabRenderDto);

        //then
        assertTabRenderContainsFieldRenders(tabRender, itemNotThrowingAnything);
    }

    private FieldScreenLayoutItem mockCustomFieldInScope(final String value)
    {
        return mockItem(true, true, value);
    }

    private void mockEveryItemPassingTroughFilters(final IssueTabRendererDto issueTabRenderDto)
    {
        final Collection<FieldScreenLayoutItem> availableItems = new ArrayList<FieldScreenLayoutItem>(allTabItems);
        when(filter.filterAvailableFieldScreenLayoutItems(issueTabRenderDto.getCondition(), allTabItems))
                .thenReturn(availableItems);

        final Collection<FieldScreenLayoutItem> visibleItems = new ArrayList<FieldScreenLayoutItem>(allTabItems);
        when(filter.filterVisibleFieldScreenLayoutItems(issueTabRenderDto.getIssue(), availableItems))
                .thenReturn(visibleItems);
    }

    private void assertTabRenderContainsFieldRenders(final FieldScreenRenderTabImpl tabRender, final FieldScreenLayoutItem ... items)
    {
        final List<FieldScreenRenderLayoutItem> fieldScreenRenderLayoutItems = tabRender.getFieldScreenRenderLayoutItems();
        final Collection<FieldScreenLayoutItem> itemsToRender = Collections2.transform(fieldScreenRenderLayoutItems, new Function<FieldScreenRenderLayoutItem, FieldScreenLayoutItem>()
        {
            @Override
            public FieldScreenLayoutItem apply(@Nullable final FieldScreenRenderLayoutItem input)
            {
                return input.getFieldScreenLayoutItem();
            }
        });

        assertThat(itemsToRender, containsInAnyOrder(items));
        assertThat(itemsToRender.size(), equalTo(items.length));
    }

    private FieldScreenLayoutItem mockCustomFieldInScopeWithoutValue()
    {
        return mockItem(true, true, null);
    }

    private FieldScreenLayoutItem mockCustomFieldNotInScope()
    {
        return mockItem(true, false, "value");
    }

    private FieldScreenLayoutItem mockNotCustomFieldItem()
    {
        return mockItem(false, false, "value");
    }

    private FieldScreenLayoutItem mockItem(boolean isCustomField, boolean isInScope, String value)
    {
        FieldScreenLayoutItem item = mock(FieldScreenLayoutItem.class);

        final CustomField field = mock(CustomField.class);
        when(item.getOrderableField())
                .thenReturn(field);

        when(fieldManager.isCustomField(item.getOrderableField()))
                .thenReturn(isCustomField);

        when(field.isInScope(eq(project), any(List.class)))
                .thenReturn(isInScope);

        when(field.getValue(issue))
                .thenReturn(value);


        final CustomFieldType customFieldType = mock(CustomFieldType.class);
        when(field.getCustomFieldType())
                .thenReturn(customFieldType);

        final CustomFieldTypeModuleDescriptor descriptor = mock(CustomFieldTypeModuleDescriptor.class);
        when(customFieldType.getDescriptor())
                .thenReturn(descriptor);

        when(descriptor.isViewTemplateExists())
                .thenReturn(true);

        final FieldLayoutItem fieldLayoutItem = mock(FieldLayoutItem.class);
        when(fieldLayout.getFieldLayoutItem(field))
                .thenReturn(fieldLayoutItem);

        when(fieldLayoutItem.getOrderableField())
                .thenReturn(field);

        allTabItems.add(item);
        return item;
    }

    private IssueTabRendererDto getIssueTabRendererDto()
    {
        return new IssueTabRendererDto(issue, operation, condition, currentTabPosition, fieldScreenTab);
    }
}
