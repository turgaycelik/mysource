package com.atlassian.jira.plugin.headernav.customcontentlinks.admin;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.projectconfig.util.ServletRequestProjectConfigRequestCache;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the admin UI for Custom Content Linbks in JIRA
 */
public class AdminServlet extends HttpServlet
{
    private static final Logger log = LoggerFactory.getLogger(AdminServlet.class);

    private final WebResourceManager webResourceManager;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final UserManager userManager;
    private final I18nResolver i18nResolver;
    private final ContentLinkAdminDescriptionProvider contentLinkAdminDescriptionProvider;
    private ProjectAdminPermissionChecker projectAdminPermissionChecker;
    private final ProjectManager projectManager;

    public AdminServlet(
            WebResourceManager webResourceManager,
            SoyTemplateRenderer soyTemplateRenderer,
            UserManager userManager,
            I18nResolver i18nResolver,
            ContentLinkAdminDescriptionProvider contentLinkAdminDescriptionProvider,
            ProjectAdminPermissionChecker projectAdminPermissionChecker,
            ProjectManager projectManager)
    {
        this.webResourceManager = webResourceManager;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.userManager = userManager;
        this.i18nResolver = i18nResolver;
        this.contentLinkAdminDescriptionProvider = contentLinkAdminDescriptionProvider;
        this.projectAdminPermissionChecker = projectAdminPermissionChecker;
        this.projectManager = projectManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {

        String entityKey = req.getParameter("entityKey");
        if (entityKey == null)
        {
            resp.sendError(400, "No 'entityKey' parameter provided.");
        }
        else
        {
            if (!projectAdminPermissionChecker.canAdminister(entityKey, userManager.getRemoteUsername(req)))
            {
                resp.sendError(401);
            }
            else
            {
                webResourceManager.requireResource("com.atlassian.jira.jira-header-plugin:custom-content-links-admin-ui-resources");
                resp.setContentType("text/html");

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("title", i18nResolver.getText("custom-content-links.page.title"));
                params.put("description", contentLinkAdminDescriptionProvider.getDescription());
                params.put("createInstructions", i18nResolver.getText("custom-content-links.page.description.createInstructions"));
                params.put("entityKey", entityKey);
                try
                {
                    resp.getWriter().print(
                            soyTemplateRenderer.render(
                                    "com.atlassian.jira.jira-header-plugin:custom-content-links-admin-page-template",
                                    "com.atlassian.jira.plugin.headernav.customcontentlinks.admin.customContentLinksAdminPage",
                                    params
                            )
                    );
                }
                catch (SoyException e)
                {
                    log.error("Error rendering template", e);
                    resp.sendError(500, e.getMessage());
                }
            }
        }
    }
}

