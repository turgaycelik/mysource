package com.atlassian.jira.issue.customfields.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.config.item.DefaultValueConfigItem;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.BulkEditBean;

import com.google.common.collect.Lists;

@PublicSpi
public abstract class AbstractCustomFieldType<T, S> implements CustomFieldType<T, S>
{
    private CustomFieldTypeModuleDescriptor descriptor;

    public void init(final CustomFieldTypeModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public final String getKey()
    {
        return descriptor.getCompleteKey();
    }

    public final String getName()
    {
        return descriptor.getName();
    }

    public final String getDescription()
    {
        return descriptor.getDescription();
    }

    public final CustomFieldTypeModuleDescriptor getDescriptor()
    {
        return descriptor;
    }

    @Nonnull
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        return MapBuilder.<String, Object> newBuilder().add("issueGv", issue != null ? issue.getGenericValue() : null).toMutableMap();
    }

    /**
     * Implementers should override the 3-param version of this.
     * We want to make attempts to use this version a compile error so plugin developers must add the extra params.
     *
     * @see #getVelocityParameters(Issue, CustomField, FieldLayoutItem)
     */
    public final Map getVelocityParameters(final Issue issue) throws IllegalAccessException
    {
        // TODO: This method is a warning mechanism only (since v4.0) and should be removed in a future release.
        throw new IllegalAccessException("subclasses should override and use the 3-param version");
    }

    @Nonnull
    public List<FieldConfigItemType> getConfigurationItemTypes()
    {
        return Lists.<FieldConfigItemType>newArrayList(new DefaultValueConfigItem());
    }

    public List<FieldIndexer> getRelatedIndexers(final CustomField customField)
    {
        return null;
    }

    public boolean isRenderable()
    {
        return false;
    }

    public boolean valuesEqual(final T v1, final T v2)
    {
        if (v1 == v2)
        {
            return true;
        }

        if ((v1 == null) || (v2 == null))
        {
            return false;
        }

        return v1.equals(v2);
    }

    public String getChangelogString(final CustomField field, final T value)
    {
        return null;
    }

    protected void assertObjectImplementsType(final Class<?> clazz, final Object o)
    {
        if (o == null)
        {
            return;
        }

        if (!clazz.isAssignableFrom(o.getClass()))
        {
            throw new ClassCastException(this.getClass() + " passed an invalid value of type: " + o.getClass());
        }
    }

    // Allow all custom fields to be available for bulk edit.
    // Each custom field type must override this method and perform any specific checks as required
    // By default - the field is bulkEditable
    public String availableForBulkEdit(final BulkEditBean bulkEditBean)
    {
        return null;
    }

    protected I18nHelper getI18nBean()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
    }

    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visit(this);
        }

        return null;
    }

    /**
     * Marker interface for visitors of AbstractCustomFieldType (acyclic visitor).
     */
    public interface VisitorBase<X>
    {
        // empty
    }

    /**
     * Visitor interface for AbstractCustomFieldType.
     */
    public interface Visitor<X> extends VisitorBase<X>
    {
        X visit(AbstractCustomFieldType customFieldType);
    }
}
