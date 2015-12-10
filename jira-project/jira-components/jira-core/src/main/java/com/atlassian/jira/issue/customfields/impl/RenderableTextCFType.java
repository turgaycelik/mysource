package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.core.util.StringUtils;
import com.atlassian.jira.issue.customfields.converters.StringConverter;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;

/**
 * This custom field type returns true for its isRenderable method and is meant to
 * represent a text custom field type that is renderable.
 */
public class RenderableTextCFType extends GenericTextCFType
{
    public RenderableTextCFType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
    }

    /**
     * @deprecated Use {@link #RenderableTextCFType(com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister, com.atlassian.jira.issue.customfields.manager.GenericConfigManager)} instead. Since v5.0.
     * @param customFieldValuePersister
     * @param stringConverter
     * @param genericConfigManager
     */
    @Deprecated
    public RenderableTextCFType(CustomFieldValuePersister customFieldValuePersister, StringConverter stringConverter, GenericConfigManager genericConfigManager)
    {
        super(customFieldValuePersister, genericConfigManager);
    }

    public boolean isRenderable()
    {
        return true;
    }

    public boolean valuesEqual(String v1, String v2)
    {
        if (v1 == v2)
        {
            return true;
        }

        if (v1 == null || v2 == null)
        {
            return false;
        }

        // Compare string ignoring line terminators
        return StringUtils.equalsIgnoreLineTerminators(v1, v2);
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitRenderableText(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitRenderableText(RenderableTextCFType renderableTextCustomFieldType);
    }
}
