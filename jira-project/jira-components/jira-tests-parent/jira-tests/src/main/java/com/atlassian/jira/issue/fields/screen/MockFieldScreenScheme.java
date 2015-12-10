package com.atlassian.jira.issue.fields.screen;

import java.util.Collection;
import java.util.Map;

import com.atlassian.jira.issue.operation.IssueOperation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.ofbiz.core.entity.GenericValue;

/**
 * @since v4.4
 */
public class MockFieldScreenScheme implements FieldScreenScheme
{
    private Long id;
    private String name;
    private String description;
    private GenericValue genericValue;
    private Collection<FieldScreenSchemeItem> fieldScreenSchemeItems = Lists.newArrayList();
    private FieldScreen defaultScreen;
    private Map<IssueOperation, FieldScreenSchemeItem> issueOperationToFieldScreenSchemeItemMapping = Maps.newHashMap();
    private Map<IssueOperation, FieldScreen> issueOperationToFieldScreenMapping = Maps.newHashMap();

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public GenericValue getGenericValue()
    {
        return genericValue;
    }

    @Override
    public void setGenericValue(GenericValue genericValue)
    {
        this.genericValue = genericValue;
    }

    @Override
    public FieldScreenSchemeItem getFieldScreenSchemeItem(IssueOperation issueOperation)
    {
        return issueOperationToFieldScreenSchemeItemMapping.get(issueOperation);
    }

    public MockFieldScreenScheme addFieldScreenSchemeItem(IssueOperation issueOperation, FieldScreenSchemeItem fieldScreenSchemeItem)
    {
        issueOperationToFieldScreenSchemeItemMapping.put(issueOperation, fieldScreenSchemeItem);
        return this;
    }

    @Override
    public Collection<FieldScreenSchemeItem> getFieldScreenSchemeItems()
    {
        return fieldScreenSchemeItems;
    }

    @Override
    public void addFieldScreenSchemeItem(FieldScreenSchemeItem fieldScreenSchemeItem)
    {
        fieldScreenSchemeItems.add(fieldScreenSchemeItem);
    }

    @Override
    public FieldScreenSchemeItem removeFieldScreenSchemeItem(IssueOperation issueOperation)
    {
        return issueOperationToFieldScreenSchemeItemMapping.remove(issueOperation);
    }

    @Override
    public FieldScreen getFieldScreen(IssueOperation issueOperation)
    {
        final FieldScreen fieldScreen = issueOperationToFieldScreenMapping.get(issueOperation);
        if (fieldScreen == null)
        {
            return defaultScreen;
        }
        else
        {
            return fieldScreen;
        }
    }

    public MockFieldScreenScheme addFieldScreen(IssueOperation operation, FieldScreen fieldScreen)
    {
        issueOperationToFieldScreenMapping.put(operation, fieldScreen);
        return this;
    }

    public MockFieldScreen createFieldScreen(IssueOperation operation)
    {
        final MockFieldScreen fieldScreen = new MockFieldScreen(getNextItemId());
        addFieldScreen(operation, fieldScreen);
        return fieldScreen;
    }

    public MockFieldScreen createDefaultScreen()
    {
        final MockFieldScreen fieldScreen = new MockFieldScreen(getNextItemId());
        defaultScreen = fieldScreen;
        return fieldScreen;
    }

    private long getNextItemId()
    {
        long max = -1;
        for (FieldScreen item : issueOperationToFieldScreenMapping.values())
        {
            if (item.getId() != null)
            {
                max = Math.max(item.getId() & 0xFFFFFFFFL, max);
            }
        }
        return id << 32 | (max + 1);
    }

    @Override
    public void store()
    {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public MockFieldScreenScheme setDefaultScreen(final FieldScreen defaultScreen)
    {
        this.defaultScreen = defaultScreen;
        return this;
    }
}
