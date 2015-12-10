package com.atlassian.jira.issue.fields;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.jql.context.QueryContext;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class MockFieldManager implements FieldManager
{
    private Map<String, Field> fields = new HashMap<String, Field>();
    private Set<Field> unavailable = new HashSet<Field>();

    public MockOrderableField addMockOrderableField(int id)
    {
        String stringId = String.valueOf(id);
        MockOrderableField mockOrderableField = new MockOrderableField(stringId, stringId);
        addField(mockOrderableField);
        return mockOrderableField;
    }

    public MockCustomField addMockCustomField(int id)
    {
        String stringId = String.valueOf(id);
        MockCustomField mockCustomField = new MockCustomField().setId(stringId);
        addField(mockCustomField);
        return mockCustomField;
    }

    public MockFieldManager addField(Field field)
    {
        fields.put(field.getId(), field);
        return this;
    }

    public MockFieldManager addUnavilableField(Field field)
    {
        unavailable.add(field);
        return this;
    }

    public MockFieldManager clear()
    {
        unavailable.clear();
        fields.clear();
        return this;
    }

    public Field getField(final String id)
    {
        return fields.get(id);
    }

    public boolean isCustomField(final String id)
    {
        return isCustomField(getField(id));
    }

    public boolean isCustomField(final Field field)
    {
        return field instanceof CustomField;
    }

    public CustomField getCustomField(final String id)
    {
        return getFieldType(id, CustomField.class);
    }

    public boolean isHideableField(final String id)
    {
        return isHideableField(getField(id));
    }

    public boolean isHideableField(final Field field)
    {
        return field instanceof HideableField;
    }

    public HideableField getHideableField(final String id)
    {
        return getFieldType(id, HideableField.class);
    }

    public boolean isOrderableField(final String id)
    {
        return isOrderableField(getField(id));
    }

    public boolean isOrderableField(final Field field)
    {
        return field instanceof OrderableField;
    }

    public OrderableField getOrderableField(final String id)
    {
        return getFieldType(id, OrderableField.class);
    }

    public ConfigurableField getConfigurableField(final String id)
    {
        return getFieldType(id, ConfigurableField.class);
    }

    public Set<OrderableField> getOrderableFields()
    {
        return getFieldsOfType(OrderableField.class, new HashSet<OrderableField>());
    }

    public Set<NavigableField> getNavigableFields()
    {
        return getFieldsOfType(NavigableField.class, new HashSet<NavigableField>());
    }

    public boolean isNavigableField(final String id)
    {
        return isNavigableField(getField(id));
    }

    public boolean isNavigableField(final Field field)
    {
        return field instanceof NavigableField;
    }

    public NavigableField getNavigableField(final String id)
    {
        return getFieldType(id, NavigableField.class);
    }

    public boolean isRequirableField(final String id)
    {
        return isRequirableField(getField(id));
    }

    public boolean isRequirableField(final Field field)
    {
        return field instanceof RequirableField;
    }

    public boolean isMandatoryField(final String id)
    {
        return isMandatoryField(getField(id));
    }

    public boolean isMandatoryField(final Field field)
    {
        return field instanceof MandatoryField;
    }

    public boolean isRenderableField(final String id)
    {
        return isRenderableField(getField(id));
    }

    public boolean isRenderableField(final Field field)
    {
        return field instanceof RenderableField;
    }

    public boolean isUnscreenableField(final String id)
    {
        return isUnscreenableField(getField(id));
    }

    public boolean isUnscreenableField(final Field field)
    {
        return field instanceof UnscreenableField;
    }

    public RequirableField getRequiredField(final String id)
    {
        return getFieldType(id, RequirableField.class);
    }

    public FieldLayoutManager getFieldLayoutManager()
    {
        throw new UnsupportedOperationException();
    }

    public ColumnLayoutManager getColumnLayoutManager()
    {
        throw new UnsupportedOperationException();
    }

    public void refresh()
    {
    }

    public Set<Field> getUnavailableFields()
    {
        return unavailable;
    }

    public boolean isFieldHidden(final User remoteUser, final Field field)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFieldHidden(Set<FieldLayout> fieldLayouts, Field field)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Set<FieldLayout> getVisibleFieldLayouts(User user)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isFieldHidden(final User remoteUser, final String fieldId)
    {
        throw new UnsupportedOperationException();
    }

    public Set<NavigableField> getAvailableNavigableFieldsWithScope(final User user) throws FieldException
    {
        throw new UnsupportedOperationException();
    }

    public Set<NavigableField> getAvailableNavigableFieldsWithScope(final User user, final QueryContext queryContext)
            throws FieldException
    {
        throw new UnsupportedOperationException();
    }

    public Set<CustomField> getAvailableCustomFields(final User remoteUser, final Issue issue) throws FieldException
    {
        throw new UnsupportedOperationException();
    }

    public Set<NavigableField> getAllAvailableNavigableFields() throws FieldException
    {
        throw new UnsupportedOperationException();
    }

    public Set<NavigableField> getAvailableNavigableFields(final User remoteUser) throws FieldException
    {
        throw new UnsupportedOperationException();
    }

    public Set<SearchableField> getAllSearchableFields()
    {
        throw new UnsupportedOperationException();
    }

    public Set<SearchableField> getSystemSearchableFields()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public IssueTypeField getIssueTypeField()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProjectField getProjectField()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isTimeTrackingOn()
    {
        return false;
    }

    private <T> T getFieldType(String id, Class<T> k)
    {
        final Field field = getField(id);
        if (field != null && k.isAssignableFrom(field.getClass()))
        {
            return k.cast(field);
        }
        else
        {
            return null;
        }
    }

    private <T,K extends Collection<T>> K getFieldsOfType(Class<T> klazz, K coll)
    {
        for (Field f : fields.values())
        {
            if (klazz.isAssignableFrom(f.getClass()))
            {
                coll.add(klazz.cast(f));
            }
        }
        return coll;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
