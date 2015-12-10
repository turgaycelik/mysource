package com.atlassian.jira.dev.reference.plugin.renderer;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * An upgraded reference macro. Reverses and duplicates the body and adds 'UPGRADED' marks around it.
 *
 * @since v4.4
 */
public class ReferenceMacro extends BaseMacro
{
    public boolean isInline()
    {
        return true;
    }

    public boolean hasBody()
    {
        return true;
    }

    public RenderMode getBodyRenderMode()
    {
        return RenderMode.INLINE;
    }

    public String execute(Map map, String body, RenderContext renderContext) throws MacroException
    {
        return "[UPGRADED] ->" + StringUtils.repeat(StringUtils.reverse(body), 2);
    }
}
