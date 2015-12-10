package com.atlassian.jira.issue.fields.renderer;

import com.atlassian.annotations.PublicApi;
import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;

/**
 * The top-level interface that defines a renderer and its configuration within jira.
 */
@PublicSpi
@PublicApi
public interface JiraRendererPlugin
{

    /**
     * This will render the provided value within the provided context and produce a
     * rendered output.
     * @param value the raw value to render.
     * @param context the issue context for this rendering
     * @return the transformed value having passed through the rendering process.
     */
    public String render(String value, IssueRenderContext context);

    /**
     * This will render the provided value within the provided context and produce a
     * rendered output that is text readable.
     * @param value the raw value to render.
     * @param context the issue context for this rendering
     * @return the transformed value having passed through the rendering process, the value
     * must be text readable.
     */
    public String renderAsText(String value, IssueRenderContext context);

    /**
     * Returns a unique identifier for this renderer plugin.
     * @return the unique identifier for this renderer plugin implmentation.
     */
    public String getRendererType();

    /**
     * This allows the renderer to perform a transformation on the raw value before populating
     * an editable component with the value. This can be useful if the value being stored is
     * a different format (i.e. wiki markup), than the edit component expects (i.e. wysiwig
     * edit control that expects html). <b>NOTE: this method need not do anything if the
     * edit control can handle the value as stored in the system.<b>
     *
     * @param rawValue is the value stored in the system, before transform.
     * @return the value in a form that the renderers edit control expects.
     */
    public Object transformForEdit(Object rawValue);

    /**
     * This allows the renderer to perform a transformation on the submitted edit value before
     * storing the value in the system. This can be useful if the value being submitted is
     * a different format (i.e. html markup that the edit control generates), than the system
     * expects to store (i.e. wiki markup). <b>NOTE: this method need not do anything if the
     * system is happy to store the value the edit control produces.<b>
     *
     * @param editValue is the value produced by the edit control.
     * @return the value in a form that the system expects to store.
     */
    public Object transformFromEdit(Object editValue);

    /**
     * This allows a plugin to get a handle on the module descriptor that spawned the plugin.
     * @param jiraRendererModuleDescriptor is the module descriptor that spawned the plugin. If
     * the plugin uses resources, such as velocity templates, then access can be gained through
     * this descriptor.
     */
    public void init(JiraRendererModuleDescriptor jiraRendererModuleDescriptor);

    /**
     * Simple accessor method for the module descriptor.
     * @return the module descriptor that spawned this renderer.
     */
    public JiraRendererModuleDescriptor getDescriptor();

}
