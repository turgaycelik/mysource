package com.atlassian.jira.admin.adminheader;

import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * TODO: Document this class / interface here
 *
 * @since v6.0
 */
public class GeneralAdminNavHeaderPanel implements WebPanel
{
    private final SoyTemplateRenderer soyTemplateRenderer;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(GeneralAdminNavHeaderPanel.class);

    public GeneralAdminNavHeaderPanel(final SoyTemplateRenderer soyTemplateRenderer)
    {
        this.soyTemplateRenderer = soyTemplateRenderer;
    }

    @Override
    public String getHtml(final Map<String, Object> context)
    {
        try
        {
            return soyTemplateRenderer.render("com.atlassian.jira.jira-admin-navigation-plugin:admin-header-new-nav-soy","JIRA.Templates.header.admin.adminnavheading", context);
        }
        catch (SoyException e)
        {
            log.warn("Could not render soy template for admin nav header");
            log.debug("Exception: ",e);
        }
        return null;
    }

    @Override
    public void writeHtml(final Writer writer, final Map<String, Object> context) throws IOException
    {
        try
        {
            soyTemplateRenderer.render(writer,"com.atlassian.jira.jira-admin-navigation-plugin:admin-header-new-nav-soy","JIRA.Templates.header.admin.adminnavheading", context);
        }
        catch (SoyException e)
        {
            log.warn("Could not render soy template for admin nav header");
            log.debug("Exception: ",e);
        }
    }
}
