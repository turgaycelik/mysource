package com.atlassian.jira.issue.fields.renderer;

import com.atlassian.jira.issue.fields.OrderableField;

import java.util.Set;

import javax.annotation.Nullable;

/**
 * Shoehorn in per-field renderer types that override the pluggable renderers. This registry allows us to switch between
 * the native select list and frother control rendering for the versions and components fields.
 * <p/>
 * In future this hack should be replaced with pluggable per-field renderer types.
 *
 * @since v4.2
 */
public interface HackyFieldRendererRegistry
{
    /**
     * If this method returns true then users will be able to choose renderers in the field configuration for the field.
     * The set of renders to choose from are determined by the result of {@link #getRendererTypes(com.atlassian.jira.issue.fields.OrderableField)}
     * and will override any pluggable renderer types defined.
     * <p/>
     * Fields that don't implement the {@link com.atlassian.jira.issue.fields.renderer.RenderableField RenderableField}
     * interface (or that do but but return {@code false} for {@link RenderableField#isRenderable()}, e.g., custom
     * fields) can still return true here to signify that they should have the option to choose renderers.
     *
     * @param field the field
     * @return true if the field has custom renderer types
     */
    boolean shouldOverrideDefaultRenderers(final OrderableField field);

    /**
     * The set of renderer types for the field. Should only be used if {@link #shouldOverrideDefaultRenderers(com.atlassian.jira.issue.fields.OrderableField)}
     * returns true.
     *
     * @param field the field
     * @return the set of renderer types, or the empty set if the field doesn't have custom renderer types defined
     */
    Set<HackyRendererType> getRendererTypes(final OrderableField field);

    /**
     * Given the field, return the default renderer type that can be used to initilize the field layout item for this
     * field. The return value must be in the set returned by {@link #getRendererTypes(com.atlassian.jira.issue.fields.OrderableField)}.
     * You should only call this if {@link #shouldOverrideDefaultRenderers(com.atlassian.jira.issue.fields.OrderableField)} returns true, otherwise
     * this will return null.
     *
     * @param field the field
     * @return the default renderer type or null if the field doesn't override renderers
     */
    @Nullable
    HackyRendererType getDefaultRendererType(OrderableField field);
}
