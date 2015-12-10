package com.atlassian.jira.issue.fields.screen.tab;

import java.util.Collection;
import java.util.List;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.util.Predicate;
import com.atlassian.ozymandias.SafePluginPointAccess;

import static com.google.common.collect.Collections2.filter;

/**
 * This class is providing filtering functionality for collections of {@link FieldScreenLayoutItem}. Each filtering logic is wrapped with {@link SafePluginPointAccess#safe(com.google.common.base.Predicate)}
 * to provide sufficient exception handling. Every {@link FieldScreenLayoutItem} throwing unhandled exceptions while evaluating Predicates will be filtered out from result collection.
 * This class was added during (JDEV-27272 - Hardening customfield plugin points) as a protection against unhandled exception from CustomFieldType plugins.
 */
public class FieldScreenRenderLayoutItemFilterImpl implements FieldScreenRenderLayoutItemFilter
{
    private final FieldManager fieldManager;
    private final FieldLayoutManager fieldLayoutManager;

    public FieldScreenRenderLayoutItemFilterImpl(final FieldManager fieldManager, final FieldLayoutManager fieldLayoutManager)
    {
        this.fieldManager = fieldManager;
        this.fieldLayoutManager = fieldLayoutManager;
    }

    @Override
    public Collection<FieldScreenLayoutItem> filterAvailableFieldScreenLayoutItems(final Predicate<? super Field> condition, final List<FieldScreenLayoutItem> fieldLayoutItems)
    {
        final Collection<Field> unavailableFields = fieldManager.getUnavailableFields();
        final com.google.common.base.Predicate<FieldScreenLayoutItem> availableItemsPredicate = SafePluginPointAccess.safe(new com.google.common.base.Predicate<FieldScreenLayoutItem>()
        {
            @Override
            public boolean apply(final FieldScreenLayoutItem fieldScreenLayoutItem)
            {
                // If the field is null (e.g. a disabled custom field plugin) do not show it and if the field is unavailable do not include it
                final OrderableField orderableField = fieldScreenLayoutItem.getOrderableField();
                if (orderableField == null || !condition.evaluate(orderableField) || unavailableFields.contains(orderableField))
                {
                    return false;
                }
                return true;
            }
        });
        return filter(fieldLayoutItems, availableItemsPredicate);
    }

    @Override
    public Collection<FieldScreenLayoutItem> filterVisibleFieldScreenLayoutItems(final Issue issue, final Collection<FieldScreenLayoutItem> fieldLayoutItems)
    {
        return filter(fieldLayoutItems, SafePluginPointAccess.safe(new com.google.common.base.Predicate<FieldScreenLayoutItem>()
        {
            @Override
            public boolean apply(final FieldScreenLayoutItem fieldScreenLayoutItem)
            {
                final FieldLayout fieldLayout = fieldLayoutManager.getFieldLayout(issue);
                final OrderableField orderableField = fieldScreenLayoutItem.getOrderableField();
                final FieldLayoutItem fieldLayoutItem = fieldLayout.getFieldLayoutItem(orderableField);
                // Only add fields that can be seen by the user.
                if (!fieldLayoutItem.isHidden() && fieldScreenLayoutItem.isShown(issue))
                {
                    return true;
                }
                return false;
            }
        }));
    }
}
