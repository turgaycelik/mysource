package com.atlassian.jira.issue;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.renderers.FieldRenderContext;

import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin;

import java.util.List;

/**
 * This is the main interface to the renderer components.
 */
@PublicApi
public interface RendererManager
{
    /**
     * Gets all the renderers in the system that have a RenderConfiguration in which the renderer is set as active.
     * @return a list containing Renderer objects whose RenderConfiguration in which the renderer is set as active.
     */
    public List<JiraRendererPlugin> getAllActiveRenderers();

    /**
     * Will return a field renderer for the given renderer type. If the renderer does not exist for the
     * type then this will return null.
     * <p>
     * Note: if there happens to be two renderers in the system with the same name (for example due to conflicting plugins)
     * then this method will return the first plugin encountered. <strong>Plugin developers beware!</strong>
     *
     * @param rendererType a string identifier that uniquely identifies a renderer in the system.
     * @return an instance of a FieldRenderer for the requested type, null if no renderer exists for the
     * type.
     */
    public JiraRendererPlugin getRendererForType(String rendererType);

    /**
     * This will returned the correct renderer for a provided field and a provided issue. This
     * method takes into account any system preferences for renderers and will return the
     * correct system renderer.
     * @param fieldConfig is the configuration object for the given field.
     * @return a field renderer that will be able to render the content of the specified field
     * in the specified issue correctly.
     */
    public JiraRendererPlugin getRendererForField(FieldLayoutItem fieldConfig);//RenderableField field, Issue issue);

    /**
     * A convienience method that is the equivilant of calling the getRendererForField method and
     * then invoking the render method on the returned renderer, using the value of the field
     * that is associated with the issue. This will make sure that the passed context is initialized.
     * @param fieldConfig identifies the configuration of the System or Custom field that is the object of the
     * renderering.
     * @param issue identifies the unique instance container of the field that will be rendered.
     * @return a string that has been processed through the correct renderer, the string will be
     * processed through the default renderer if there was an error resolving the specified renderers.
     */
    public String getRenderedContent(FieldLayoutItem fieldConfig, Issue issue);

    /**
     * A convienience method that is the equivilant of calling the getRendererForField method and
     * then invoking the render method on the returned renderer, using the value of the field
     * that is associated with the issue. This will make sure that the passed context is initialized.
     * @param rendererType is the renderer to use, if not resolved will fall back to the system default.
     * @param value is the value to render.
     * @param renderContext is the context to use for rendering.
     * @return a string that has been processed through the correct renderer, the string will be
     * unprocessed if there was an error resolving renderers.
     */
    public String getRenderedContent(String rendererType, String value, IssueRenderContext renderContext);

    public String getRenderedContent(FieldRenderContext fieldRenderContext);
}
