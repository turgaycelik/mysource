package com.atlassian.jira.web.component;

/**
 * Represents an instruction that the container uses to decide whether to render the associated
 * web component or not during any given rendering cycle. It also instructs the container to either
 * send or not send the output to the requesting client.
 *
 * The decision is made based on content ID values which are calculated based on the associated
 * web component content. Depending on the instruction type these ID values can be calculated from the
 * rendered content or custom custom values can be used to avoid unnecessary rendering.
 *
 * Instances of this class are returned by {@link ContentRenderingInstructionsProvider} implementations.
 */
public class ContentRenderingInstruction
{
    private static final ContentRenderingInstruction DONT_RENDER = new ContentRenderingInstruction();
    private static final ContentRenderingInstruction RENDER_HTML = new ContentRenderingInstruction();

    private String contentId;

    private ContentRenderingInstruction()
    {
    }

    private ContentRenderingInstruction(String contentId)
    {
        this.contentId = contentId;
    }

    /**
     * The associated component will not be rendered and output will not be sent to the client.
     * It indicates that the current content ID must continue to be used.
     *
     * @return instruction to not render the associated component and use the current content ID.
     */
    public static ContentRenderingInstruction dontRender()
    {
        return DONT_RENDER;
    }

    /**
     * The associated component will be rendered.
     * The new content ID will be calculated from the rendered output.
     * The output and the new content ID will be sent to the client if the new content ID doesn't
     * match the current content ID.
     *
     * @return instruction to render the associated component, calculate the new content ID from the output
     * and send output to client if needed.
     */
    public static ContentRenderingInstruction renderHtml()
    {
        return RENDER_HTML;
    }

    /**
     * Instruction to use the passed content ID value to make a desicion.
     * The component will be rendered if the new content ID doesn't match the current content ID.
     * In this case the rendered output and the new content ID will also be sent to the client.
     *
     * @return instruction to use the passed content ID in order to make a decision about
     * whether to render the component and send output to client.
     */
    public static ContentRenderingInstruction customContentId(String contentId)
    {
        return new ContentRenderingInstruction(contentId);
    }

    public String getContentId()
    {
        return contentId;
    }

    public boolean isDontRender()
    {
        return DONT_RENDER.equals(this);
    }

    public boolean isRenderHtml()
    {
        return RENDER_HTML.equals(this);
    }
}
