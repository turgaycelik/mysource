package com.atlassian.jira.issue.fields.screen.tab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTabImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.ozymandias.SafePluginPointAccess;

import com.google.common.base.Function;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;

/**
 * This class is factory for {@link FieldScreenRenderTabImpl}. Creating renderers for each field which should be rendered in Tab is wrapped with {@link SafePluginPointAccess#safe}
 * to provide sufficient exception handling. Any field which will throw unhandled exception while creating {@link FieldScreenRenderLayoutItem} will be filtered out - and as a result
 * not appear in rendered Tab.
 * This class was added during (JDEV-27272 - Hardening customfield plugin points) as a protection against unhandled exception from CustomFieldType plugins.
 */
public class FieldScreenTabRendererFactoryImpl implements FieldScreenTabRendererFactory
{
    private final FieldScreenRenderLayoutItemFilter fieldLayoutItemsFilter;
    private final FieldManager fieldManager;
    private final FieldLayoutManager fieldLayoutManager;

    public FieldScreenTabRendererFactoryImpl(final FieldScreenRenderLayoutItemFilter fieldLayoutItemsFilter, final FieldManager fieldManager,
            final FieldLayoutManager fieldLayoutManager) {
        this.fieldLayoutItemsFilter = fieldLayoutItemsFilter;
        this.fieldManager = fieldManager;
        this.fieldLayoutManager = fieldLayoutManager;
    }

    public FieldScreenRenderTabImpl createTabRender(final IssueTabRendererDto issueTabRendererDto)
    {
        final Issue issue = issueTabRendererDto.getIssue();
        final FieldScreenTab fieldScreenTab = issueTabRendererDto.getFieldScreenTab();
        final List<FieldScreenLayoutItem> fieldLayoutItems = fieldScreenTab.getFieldScreenLayoutItems();

        final Collection<FieldScreenLayoutItem> availableFieldLayoutItems = fieldLayoutItemsFilter.filterAvailableFieldScreenLayoutItems(issueTabRendererDto.getCondition(), fieldLayoutItems);
        final Collection<FieldScreenLayoutItem> visibleFieldScreenLayoutItems = fieldLayoutItemsFilter.filterVisibleFieldScreenLayoutItems(issue, availableFieldLayoutItems);
        final Collection<FieldScreenRenderLayoutItem> fieldScreenRenderLayoutItems = transformFieldScreenLayoutItemsToRenderers(issueTabRendererDto, visibleFieldScreenLayoutItems);

        return new FieldScreenRenderTabImpl(fieldScreenTab.getName(), issueTabRendererDto.getCurrentTabPosition(), new ArrayList(fieldScreenRenderLayoutItems));
    }

    private Collection<FieldScreenRenderLayoutItem> transformFieldScreenLayoutItemsToRenderers(final IssueTabRendererDto issueTabRendererDto, final Collection<FieldScreenLayoutItem> visibleFieldScreenLayoutItems)
    {
        final Issue issue = issueTabRendererDto.getIssue();
        final IssueOperation operation = issueTabRendererDto.getOperation();

        return filter(transform(visibleFieldScreenLayoutItems, SafePluginPointAccess.safe(new Function<FieldScreenLayoutItem, FieldScreenRenderLayoutItem>()
        {
            @Override
            public FieldScreenRenderLayoutItem apply(final FieldScreenLayoutItem fieldScreenLayoutItem)
            {
                final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
                final OrderableField orderableField = fieldScreenLayoutItem.getOrderableField();
                final FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(orderableField);

                if (fieldManager.isCustomField(orderableField))
                {
                    return getCustomFieldRenderLayoutItem(issue, operation, fieldLayoutItem, fieldScreenLayoutItem);
                }
                else
                {
                    return new FieldScreenRenderLayoutItemImpl(fieldScreenLayoutItem, fieldLayoutItem);
                }
            }
        })), com.google.common.base.Predicates.notNull());
    }

    private FieldScreenRenderLayoutItem getCustomFieldRenderLayoutItem(Issue issue, IssueOperation issueOperation, FieldLayoutItem fieldLayoutItem, FieldScreenLayoutItem fieldScreenLayoutItem)
    {
        // Check if the custom field is in scope
        CustomField customField = (CustomField) fieldLayoutItem.getOrderableField();

        if (customField.isInScope(issue.getProjectObject(), Collections.singletonList(issue.getIssueTypeObject().getId())))
        {
            if (IssueOperations.VIEW_ISSUE_OPERATION.equals(issueOperation))
            {
                // If we are viewing an issue only show fields that have a view template
                // If changing this, see if http://confluence.atlassian.com/display/JIRACOM/Displaying+Custom+Fields+with+no+value needs updating
                if (haveDataToRender(customField, issue))
                {
                    return new FieldScreenRenderLayoutItemImpl(fieldScreenLayoutItem, fieldLayoutItem);
                }
            }
            else
            {
                return new FieldScreenRenderLayoutItemImpl(fieldScreenLayoutItem, fieldLayoutItem);
            }
        }
        return null;
    }

    private boolean haveDataToRender(final CustomField customField, final Issue issue)
    {
        return customField.getCustomFieldType().getDescriptor().isViewTemplateExists() && customField.getValue(issue) != null;
    }
}
