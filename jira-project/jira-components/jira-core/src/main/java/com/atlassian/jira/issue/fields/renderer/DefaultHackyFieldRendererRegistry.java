package com.atlassian.jira.issue.fields.renderer;

import com.atlassian.jira.issue.fields.AffectedVersionsSystemField;
import com.atlassian.jira.issue.fields.ComponentsSystemField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FixVersionsSystemField;
import com.atlassian.jira.issue.fields.OrderableField;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import static com.atlassian.jira.issue.fields.renderer.HackyRendererType.FROTHER_CONTROL;
import static com.atlassian.jira.issue.fields.renderer.HackyRendererType.SELECT_LIST;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultHackyFieldRendererRegistry implements HackyFieldRendererRegistry
{
    private static final String CUSTOM_FIELD_KEY_PREFIX = "com.atlassian.jira.plugin.system.customfieldtypes:";

    //hardcoding the frother control renderer to system version & components fields as well as the multiversion custom field.
    private static final Set<HackyRendererType> RENDERERS = ImmutableSet.of(SELECT_LIST, FROTHER_CONTROL);

    private static final Map<Class<? extends OrderableField>, Set<HackyRendererType>> SYSTEM_FIELD_OVERRIDES =
            ImmutableMap.<Class<? extends OrderableField>, Set<HackyRendererType>>builder()
                    .put(FixVersionsSystemField.class, RENDERERS)
                    .put(AffectedVersionsSystemField.class, RENDERERS)
                    .put(ComponentsSystemField.class, RENDERERS)
                    .build();

    private static final Map<String, Set<HackyRendererType>> CUSTOM_FIELD_TYPE_OVERRIDES =
            ImmutableMap.<String, Set<HackyRendererType>>builder()
                    .put(CUSTOM_FIELD_KEY_PREFIX + "multiversion", RENDERERS)
                    .build();



    public boolean shouldOverrideDefaultRenderers(final OrderableField field)
    {
        return getRendererTypesOverride(field) != null;
    }


    public Set<HackyRendererType> getRendererTypes(final OrderableField field)
    {
        final Set<HackyRendererType> override = getRendererTypesOverride(field);
        if (override == null)
        {
            return Collections.emptySet();
        }
        return override;
    }

    @Nullable
    public HackyRendererType getDefaultRendererType(final OrderableField field)
    {
        return shouldOverrideDefaultRenderers(field) ? FROTHER_CONTROL : null;
    }



    private static Set<HackyRendererType> getRendererTypesOverride(final OrderableField field)
    {
        notNull("field", field);
        if (field instanceof CustomField)
        {
            final String type = ((CustomField)field).getCustomFieldType().getKey();
            return CUSTOM_FIELD_TYPE_OVERRIDES.get(type);
        }
        else
        {
            return SYSTEM_FIELD_OVERRIDES.get(field.getClass());
        }
    }
}
