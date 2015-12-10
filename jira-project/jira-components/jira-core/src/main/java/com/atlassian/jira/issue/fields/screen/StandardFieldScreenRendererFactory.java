package com.atlassian.jira.issue.fields.screen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.tab.FieldScreenTabRendererFactory;
import com.atlassian.jira.issue.fields.screen.tab.IssueTabRendererDto;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.Predicates;
import com.atlassian.jira.workflow.WorkflowActionsBean;

import com.opensymphony.workflow.loader.ActionDescriptor;

import org.ofbiz.core.entity.GenericValue;

/**
 * Class that creates a {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderer} for JIRA's configured fields and screens.
 *
 * @since v4.1
 */
class StandardFieldScreenRendererFactory
{
    private final FieldManager fieldManager;
    private final FieldLayoutManager fieldLayoutManager;
    private final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private final FieldScreenManager fieldScreenManager;
    private final FieldScreenTabRendererFactory fieldScreenTabRendererFactory;

    StandardFieldScreenRendererFactory(final FieldManager fieldManager, final FieldLayoutManager fieldLayoutManager, final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager,
            final FieldScreenManager fieldScreenManager, final FieldScreenTabRendererFactory fieldScreenTabRendererFactory)
    {
        this.fieldManager = fieldManager;
        this.fieldLayoutManager = fieldLayoutManager;
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
        this.fieldScreenManager = fieldScreenManager;
        this.fieldScreenTabRendererFactory = fieldScreenTabRendererFactory;
    }

    FieldScreenRenderer createFieldScreenRenderer(Issue issue, IssueOperation issueOperation, Predicate<? super Field> predicate)
    {
        final FieldScreenScheme fieldScreenScheme = issueTypeScreenSchemeManager.getFieldScreenScheme(issue);
        return createFieldScreenRenderer(issue, fieldScreenScheme.getFieldScreen(issueOperation), issueOperation, predicate);
    }

    FieldScreenRenderer createFieldScreenRenderer(Issue issue, ActionDescriptor actionDescriptor)
    {
        return createFieldScreenRenderer(issue, getScreenFromAction(actionDescriptor), null, Predicates.truePredicate());
    }

    FieldScreen getScreenFromAction(ActionDescriptor descriptor)
    {
        final WorkflowActionsBean workflowActionsBean = new WorkflowActionsBean();
        return workflowActionsBean.getFieldScreenForView(descriptor);
    }

    FieldScreenRenderer createFieldScreenRenderer(Issue issue)
    {
        return createFieldScreenRenderer(issue, Collections.<FieldScreenTab>emptyList(), null, Predicates.truePredicate());
    }

    FieldScreenRenderer createFieldScreenRenderer(List<String> fieldIds, Issue issue, IssueOperation issueOperation)
    {
        final FieldScreenTab tab = new SubTaskFieldScreenTab(fieldIds, fieldScreenManager, fieldManager);
        return createFieldScreenRenderer(issue, Collections.singletonList(tab), issueOperation, Predicates.truePredicate());
    }

    FieldScreenRenderer createFieldScreenRenderer(Issue issue, FieldScreen fieldScreen, IssueOperation operation,
            Predicate<? super Field> condition)
    {
        final List<FieldScreenTab> fieldScreenTabs = (fieldScreen == null ? Collections.<FieldScreenTab>emptyList() : fieldScreen.getTabs());
        return createFieldScreenRenderer(issue, fieldScreenTabs, operation, condition);
    }

    FieldScreenRenderer createFieldScreenRenderer(final Issue issue, Collection<FieldScreenTab> tabs, final IssueOperation operation,
            final Predicate<? super Field> condition)
    {
        final List<FieldScreenRenderTab> fieldScreenRenderTabs = new ArrayList<FieldScreenRenderTab>();

        // Create FieldScreenRenderTabs
        // Iterate over the FieldScreen tabs and create FieldScreenRenderTab for each one
        int currentTabPosition = 0;
        for (final FieldScreenTab fieldScreenTab : tabs)
        {
            final IssueTabRendererDto issueTabRendererDto = new IssueTabRendererDto(issue, operation, condition, currentTabPosition, fieldScreenTab);
            final FieldScreenRenderTab fieldScreenRenderTab = fieldScreenTabRendererFactory.createTabRender(issueTabRendererDto);
            // Only render tabs with items on them
            if ( !fieldScreenRenderTab.getFieldScreenRenderLayoutItems().isEmpty())
            {
                fieldScreenRenderTabs.add(fieldScreenRenderTab);
                currentTabPosition++;
            }
        }

        final FieldLayout fieldLayout = getFieldLayout(issue);
        return new FieldScreenRendererImpl(Collections.unmodifiableList(fieldScreenRenderTabs), fieldLayout);
    }

    private FieldLayout getFieldLayout(Issue issue)
    {
        return fieldLayoutManager.getFieldLayout(issue);
    }

    private static class SubTaskFieldScreenTab implements FieldScreenTab
    {
        private final Map<String, FieldScreenLayoutItem> fieldLayoutItems;

        public SubTaskFieldScreenTab(List<String> fieldIds, FieldScreenManager fieldScreenManager, FieldManager fieldManager)
        {
            this.fieldLayoutItems = new LinkedHashMap<String, FieldScreenLayoutItem>();
            int i = 0;
            for (String fieldId : fieldIds)
            {
                SubTaskFieldScreenlayoutItem fieldScreenLayoutItem = new SubTaskFieldScreenlayoutItem(fieldScreenManager, fieldManager);
                fieldScreenLayoutItem.setPosition(i);
                fieldScreenLayoutItem.setFieldId(fieldId);
                fieldScreenLayoutItem.setFieldScreenTab(this);
                fieldLayoutItems.put(fieldId, fieldScreenLayoutItem);
                // Increment the position for the next layout item
                i++;
            }
        }

        public int getPosition()
        {
            return 0;
        }

        public boolean isModified()
        {
            return false;
        }

        public List<FieldScreenLayoutItem> getFieldScreenLayoutItems()
        {
            return new ArrayList<FieldScreenLayoutItem>(fieldLayoutItems.values());
        }

        public FieldScreenLayoutItem getFieldScreenLayoutItem(int poistion)
        {
            return getFieldScreenLayoutItems().get(poistion);
        }

        public FieldScreenLayoutItem getFieldScreenLayoutItem(String fieldId)
        {
            return fieldLayoutItems.get(fieldId);
        }

        public boolean isContainsField(String fieldId)
        {
            return fieldLayoutItems.containsKey(fieldId);
        }

        public String getName()
        {
            return "Sub Task Quick Creation Tab";
        }


        // Not implemented methods. These should not be called when for this class ---------------------------------------
        public Long getId()
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void setName(String name)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void setPosition(int position)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void addFieldScreenLayoutItem(String fieldId)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void addFieldScreenLayoutItem(String fieldId, int position)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void moveFieldScreenLayoutItemFirst(int fieldPosition)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void moveFieldScreenLayoutItemUp(int fieldPosition)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void moveFieldScreenLayoutItemDown(int fieldPosition)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void moveFieldScreenLayoutItemLast(int fieldPosition)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public FieldScreenLayoutItem removeFieldScreenLayoutItem(int fieldPosition)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void moveFieldScreenLayoutItemToPosition(Map positionsToFields)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public GenericValue getGenericValue()
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void setGenericValue(GenericValue genericValue)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void setFieldScreen(FieldScreen fieldScreen)
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public FieldScreen getFieldScreen()
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void rename(String newName)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        public void store()
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void remove()
        {
            throw new UnsupportedOperationException("Not implemented.");
        }
    }

    private static class SubTaskFieldScreenlayoutItem extends AbstractFieldScreenLayoutItem
    {
        public SubTaskFieldScreenlayoutItem(FieldScreenManager fieldScreenManager, FieldManager fieldManager)
        {
            super(fieldScreenManager, fieldManager);
        }

        protected void init()
        {
            // Do nothing
        }

        public void setPosition(int position)
        {
            this.position = position;
        }

        public void setFieldId(String fieldId)
        {
            this.fieldId = fieldId;
        }

        public void setFieldScreenTab(FieldScreenTab fieldScreenTab)
        {
            this.fieldScreenTab = fieldScreenTab;
        }

        // Not implemented methods.
        public Long getId()
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void store()
        {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public void remove()
        {
            throw new UnsupportedOperationException("Not implemented.");
        }
    }
}
