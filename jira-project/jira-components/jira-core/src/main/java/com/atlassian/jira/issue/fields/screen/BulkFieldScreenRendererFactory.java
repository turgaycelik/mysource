package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.HackyFieldRendererRegistry;
import com.atlassian.jira.workflow.WorkflowActionsBean;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Class Used to create {@link com.atlassian.jira.issue.fields.screen.BulkFieldScreenRendererImpl} items for bulk move
 * operations.
 *
 * @since v4.1
 */
class BulkFieldScreenRendererFactory
{
    private static final Logger log = Logger.getLogger(BulkFieldScreenRendererFactory.class);

    private final FieldManager fieldManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final HackyFieldRendererRegistry hackyFieldRendererRegistry;

    BulkFieldScreenRendererFactory(FieldManager fieldManager, FieldLayoutManager fieldLayoutManager, HackyFieldRendererRegistry hackyFieldRendererRegistry)
    {
        this.hackyFieldRendererRegistry = hackyFieldRendererRegistry;
        this.fieldManager = notNull("fieldManager", fieldManager);
        this.fieldLayoutManager = notNull("fieldLayoutManager", fieldLayoutManager);
    }

    BulkFieldScreenRendererImpl createRenderer(Collection<Issue> issues, ActionDescriptor actionDescriptor)
    {
        final Collection<FieldLayout> fieldLayouts = getFieldLayouts(issues);
        final Collection<Field> unavailableFields = fieldManager.getUnavailableFields();

        // Create FieldScreenRenderTabs
        // Iterate over the FieldScreen tabs and create FieldScreenRenderTab for each one
        final List<FieldScreenTab> fieldScreenTabs = getScreenTabs(actionDescriptor);
        final List<FieldScreenRenderTab> fieldScreenRenderTabs = new ArrayList<FieldScreenRenderTab>();

        int i = 0;
        for (final FieldScreenTab fieldScreenTab : fieldScreenTabs)
        {
            final List<FieldScreenRenderLayoutItem> fieldScreenRenderLayoutItems = new ArrayList<FieldScreenRenderLayoutItem>();
            for (FieldScreenLayoutItem fieldScreenLayoutItem : fieldScreenTab.getFieldScreenLayoutItems())
            {
                // If the field is null (e.g. a disabled custom field plugin) do not show it and if the field is unavailable do not include it
                final OrderableField field = fieldScreenLayoutItem.getOrderableField();
                if (field != null && !unavailableFields.contains(field))
                {
                    final Collection<FieldLayoutItem> fieldLayoutItems = new ArrayList<FieldLayoutItem>();

                    for (FieldLayout fieldLayout : fieldLayouts)
                    {
                        fieldLayoutItems.add(fieldLayout.getFieldLayoutItem(field));
                    }

                    if (fieldManager.isCustomField(field))
                    {
                        CustomField customField = (CustomField) field;
                        // If we are viewing an issue only show fields that have a view template
                        // We are not doing a customField.isInScope check here since we may have issues across multiple projects
                        // and we simply want to show a message saying that you cannot edit the given custom field
                        // not remove the field from the view!
                        if (customField.getCustomFieldType().getDescriptor().isViewTemplateExists())
                        {
                            BulkFieldScreenRenderLayoutItemImpl fieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(fieldManager, hackyFieldRendererRegistry, fieldScreenLayoutItem, fieldLayoutItems);
                            fieldScreenRenderLayoutItems.add(fieldScreenRenderLayoutItem);
                        }
                    }
                    else
                    {
                        BulkFieldScreenRenderLayoutItemImpl fieldScreenRenderLayoutItem = new BulkFieldScreenRenderLayoutItemImpl(fieldManager, hackyFieldRendererRegistry, fieldScreenLayoutItem, fieldLayoutItems);
                        fieldScreenRenderLayoutItems.add(fieldScreenRenderLayoutItem);
                    }
                }
            }

            // Only render tabs with items on them
            if (!fieldScreenRenderLayoutItems.isEmpty())
            {
                fieldScreenRenderTabs.add(new FieldScreenRenderTabImpl(fieldScreenTab.getName(), i++, fieldScreenRenderLayoutItems));
            }
        }

        return new BulkFieldScreenRendererImpl(fieldScreenRenderTabs);
    }

    private List<FieldScreenTab> getScreenTabs(final ActionDescriptor actionDescriptor)
    {
        final FieldScreen fieldScreen = getScreen(actionDescriptor);
        return (fieldScreen == null ? Collections.<FieldScreenTab>emptyList() : fieldScreen.getTabs());
    }

    //Package protected for tests.
    FieldScreen getScreen(final ActionDescriptor actionDescriptor)
    {
        final WorkflowActionsBean workflowActionsBean = new WorkflowActionsBean();
        return workflowActionsBean.getFieldScreenForView(actionDescriptor);
    }

    private Collection<FieldLayout> getFieldLayouts(Collection<Issue> issues)
    {
        Collection<FieldLayout> fieldLayouts = new HashSet<FieldLayout>();

        for (Issue issue : issues)
        {
            try
            {
                fieldLayouts.add(fieldLayoutManager.getFieldLayout(issue));
            }
            catch (DataAccessException e)
            {
                log.error("Unable to retrieve the fieldlayout associated with the issue: " + issue.getKey() + ".", e);
            }
        }
        return fieldLayouts;
    }
}
