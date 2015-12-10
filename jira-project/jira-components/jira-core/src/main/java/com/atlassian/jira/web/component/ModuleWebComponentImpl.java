package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebLabel;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.atlassian.jira.web.component.ModuleWebComponentFields.RENDER_PARAM_CONTAINER_CLASS;
import static com.atlassian.jira.web.component.ModuleWebComponentFields.RENDER_PARAM_HEADLESS;
import static com.atlassian.jira.web.component.ModuleWebComponentFields.RENDER_PARAM_PREFIX;
import static com.atlassian.jira.web.component.ModuleWebComponentFields.SectionsAndLinks;

public class ModuleWebComponentImpl implements ModuleWebComponent
{
    private static final Logger log = LoggerFactory.getLogger(ModuleWebComponentImpl.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ModuleWebComponentFields mwcFields;

    public ModuleWebComponentImpl(JiraAuthenticationContext jiraAuthenticationContext, ModuleWebComponentFields mwcFields)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.mwcFields = mwcFields;
    }

    @Override
    public String renderModules(User user, HttpServletRequest request, List<WebPanelModuleDescriptor> webPanelModuleDescriptors, Map<String, Object> params)
    {
        final StringBuilder sb = new StringBuilder();
        for (WebPanelModuleDescriptor webPanelModuleDescriptor : webPanelModuleDescriptors)
        {
            renderModuleAndLetNoThrowablesEscape(sb, user, request, webPanelModuleDescriptor, params);
        }
        return sb.toString();
    }

    @Override
    public String renderModule(User user, HttpServletRequest request, WebPanelModuleDescriptor webPanelModuleDescriptor, Map<String, Object> params)
    {
        final StringBuilder sb = new StringBuilder();
        renderModuleAndLetNoThrowablesEscape(sb, user, request, webPanelModuleDescriptor, params);
        return sb.toString();

    }

    /**
     * Renders a web panel, catching and logging any Throwables that might try escape. These should not be propagated
     * because that would keep
     */
    private void renderModuleAndLetNoThrowablesEscape(final StringBuilder module, final User user, final HttpServletRequest request, final WebPanelModuleDescriptor webPanelModuleDescriptor, final Map<String, Object> params)
    {
        SafePluginPointAccess.to().runnable(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    renderModule(module, user, request, webPanelModuleDescriptor, params);
                }
                catch (Exception renderingException)
                {
                    log.error("An exception occured while rendering the web panel: " + webPanelModuleDescriptor, renderingException);
                    module.append(buildRenderFailureMessage(webPanelModuleDescriptor));
                }
            }
        });
    }

    /*
     * Puts it all together and wraps it
     */
    private void renderModule(StringBuilder module, User user, HttpServletRequest request, WebPanelModuleDescriptor webPanelModuleDescriptor, Map<String, Object> params) throws SoyException
    {
        //safe handling of exceptions thrown by getHtml is done in renderModuleAndLetNoThrowablesEscape
        final String html = webPanelModuleDescriptor.getModule().getHtml(params);
        final JiraHelper helper = (JiraHelper) params.get("helper");

        // if there is not content, don't render the module
        if (StringUtils.isBlank(html))
        {
            return;
        }
        final Map<String, String> moduleParams = webPanelModuleDescriptor.getParams();
        if ((moduleParams.containsKey(RENDER_PARAM_HEADLESS) && moduleParams.get(RENDER_PARAM_HEADLESS).equals("true")) || (params.containsKey(RENDER_PARAM_HEADLESS) && params.get(RENDER_PARAM_HEADLESS).equals(true)))
        {
            module.append(html);
            return;
        }
        final String key = webPanelModuleDescriptor.getKey();

        String additionalContainerClass = moduleParams.containsKey(RENDER_PARAM_CONTAINER_CLASS) ? " " + moduleParams.get(RENDER_PARAM_CONTAINER_CLASS) : "";
        if (StringUtils.isBlank(additionalContainerClass))
        {
            additionalContainerClass = params.containsKey(RENDER_PARAM_CONTAINER_CLASS) ? " " +  params.get(RENDER_PARAM_CONTAINER_CLASS) : "";
        }

        String prefix = moduleParams.containsKey(RENDER_PARAM_PREFIX) ? "" + moduleParams.get(RENDER_PARAM_PREFIX) : "";
        if (StringUtils.isBlank(prefix))
        {
            prefix = params.containsKey(RENDER_PARAM_PREFIX) ? "" + params.get(RENDER_PARAM_PREFIX) : "";
        }

        Map<String,Object> data = new HashMap<String,Object>(params);
        data.put("id", prefix + key);
        data.put("headerId", key + "_heading");
        data.put("headerActionsContent", renderHeaderActions(user, webPanelModuleDescriptor, helper));
        data.put("headerPanelContent", renderHeaderPanels(webPanelModuleDescriptor.getCompleteKey(), params));
        data.put("headingContent", renderModHeading(request, webPanelModuleDescriptor, params));
        data.put("content", html);
        data.put("headingLevel", 2);
        data.put("extraClasses", additionalContainerClass);

        module.append(getSoyRenderer().render("jira.webresources:soy-templates","JIRA.Templates.Modules.module", data));
    }

    private String renderHeaderActions(User user, WebPanelModuleDescriptor webPanelModuleDescriptor, JiraHelper helper)
    {
        StringBuilder sb = new StringBuilder();
        renderHeaderActions(sb, user, webPanelModuleDescriptor, helper);
        return sb.toString();
    }

    private void renderHeaderActions(StringBuilder module, User user, WebPanelModuleDescriptor webPanelModuleDescriptor, JiraHelper helper)
    {
        final List<SimpleLink> headerItems = mwcFields.getHeaderItems(webPanelModuleDescriptor.getCompleteKey(), user, helper);
        final List<SectionsAndLinks> dropDownSections = mwcFields.getDropdownSections(webPanelModuleDescriptor.getCompleteKey(), user, helper);
        if (!(headerItems.isEmpty() && dropDownSections.isEmpty())){
            final StringBuilder headerOps = new StringBuilder();

            headerOps.append("<ul class='ops'>");
            for (SimpleLink link : headerItems)
            {
                headerOps.append("<li>");
                    renderLink(headerOps, link, "");
                headerOps.append("</li>");
            }
            if (!dropDownSections.isEmpty())
            {
                headerOps.append("<li class='drop'>");
                    headerOps.append("<div class='aui-dd-parent'>");
                final String optionText = jiraAuthenticationContext.getI18nHelper().getText("admin.common.words.options");
                headerOps.append("<a href='#' class='icon drop-menu js-default-dropdown' title='").append( optionText).append("'><span>").append(optionText).append("</span></a>");
                        headerOps.append("<div class='aui-dropdown-content aui-list'>");

                for (int i = 0; i < dropDownSections.size(); i++)
                {
                    String additionalClass = "";
                    if (i == 0)
                    {
                        additionalClass = "aui-first";
                    }

                    if (i == dropDownSections.size() - 1)
                    {
                        additionalClass = additionalClass + " aui-last";
                    }
                    final SectionsAndLinks sectionAndLinks = dropDownSections.get(i);
                    renderSection(headerOps, sectionAndLinks.getSection(), additionalClass, sectionAndLinks.getLinks());
                }

                        headerOps.append("</div>");
                    headerOps.append("</div>");
                headerOps.append("</li>");

            }
            headerOps.append("</ul>");

            module.append(headerOps);
        }
    }

    private String renderHeaderPanels(String key, Map<String, Object> params)
    {
        StringBuilder sb = new StringBuilder();
        renderHeaderPanels(sb, key, params);
        return sb.toString();
    }

    private void renderHeaderPanels(StringBuilder sb, String key, Map<String, Object> params)
    {
        final List<WebPanelModuleDescriptor> panels = mwcFields.getPanels(key, params);
        if (!panels.isEmpty())
        {
            sb.append("<div class='mod-header-panels'>");
            for (WebPanelModuleDescriptor panel : panels)
            {
                sb.append("<div class='mod-header-panel'>");
                    sb.append(panel.getModule().getHtml(params));
                sb.append("</div>");
            }
            sb.append("</div>");
        }
    }

    /*
     * Renders the sections in the drop down
     */
    private String renderSection(SimpleLinkSection section, String additionalClass, List<SimpleLink> links)
    {
        StringBuilder sb = new StringBuilder();
        renderSection(sb, section, additionalClass, links);
        return sb.toString();
    }

    private void renderSection(StringBuilder sb, SimpleLinkSection section, String additionalClass, List<SimpleLink> links)
    {
        if (StringUtils.isNotBlank(section.getLabel()))
        {
            sb.append("<h5>").append(section.getLabel()).append("</h5>");
        }
        sb.append("<ul");
        if (StringUtils.isNotBlank(section.getId()))
        {
            sb.append(" id='").append(section.getId()).append("'");
        }
        sb.append(" class='aui-list-section");
        if (StringUtils.isNotBlank(additionalClass))
        {
            sb.append(" ").append(additionalClass);
        }
        if (StringUtils.isNotBlank(section.getStyleClass()))
        {
            sb.append(" ").append(section.getStyleClass());
        }
        sb.append("'>");

        for (SimpleLink link : links)
        {
            sb.append("<li class='aui-list-item'>");
                renderLink(sb, link, "aui-list-item-link");
            sb.append("</li>");
        }

        sb.append("</ul>");

    }

    /*
     * Render the individual link (used for both the header links and drop links)
     */
    private String renderLink(SimpleLink link, String additionalClass)
    {
        StringBuilder sb = new StringBuilder();
        renderLink(sb, link, additionalClass);
        return sb.toString();
    }

    private void renderLink(StringBuilder sb, SimpleLink link, String additionalClass)
    {
        sb.append("<a");

        if (StringUtils.isNotBlank(link.getId()))
        {
            sb.append(" id='").append(link.getId()).append("'");
        }

        sb.append(" href='").append(link.getUrl()).append("'");

        if (StringUtils.isNotBlank(link.getStyleClass()) || StringUtils.isNotBlank(additionalClass))
        {
            sb.append(" class='");
            if (StringUtils.isNotBlank(link.getStyleClass()))
            {
                sb.append(link.getStyleClass());
            }

            sb.append(" ").append(additionalClass);

            sb.append("'");

        }


        if (StringUtils.isNotBlank(link.getTitle()))
        {
            sb.append(" title='").append(link.getTitle()).append("'");
        }
        sb.append("><span>");

        sb.append(link.getLabel());
        sb.append("</span></a>");
    }

    @HtmlSafe
    private String renderModHeading(HttpServletRequest request, WebPanelModuleDescriptor webPanel, Map<String, Object> params)
    {
        StringBuilder sb = new StringBuilder();
        renderModHeading(sb, request, webPanel, params);
        return TextUtils.htmlEncode(sb.toString());
    }

    private void renderModHeading(StringBuilder sb, HttpServletRequest request, WebPanelModuleDescriptor webPanel, Map<String, Object> params)
    {
        try
        {
            final WebLabel webLabel = webPanel.getWebLabel();
            if (webLabel != null)
            {
                sb.append(webLabel.getDisplayableLabel(request, params));
            }
        }
        catch (Exception labelRenderException)
        {
            if (webPanel.getI18nNameKey() != null)
            {
                sb.append(jiraAuthenticationContext.getI18nHelper().getText(webPanel.getI18nNameKey()));
            }
            else
            {
                sb.append(webPanel.getKey());
            }
        }
    }

    private String buildRenderFailureMessage(WebPanelModuleDescriptor webPanelModuleDescriptor)
    {
        return jiraAuthenticationContext.getI18nHelper().getText("modulewebcomponent.exception", webPanelModuleDescriptor.getCompleteKey());
    }

    protected SoyTemplateRenderer getSoyRenderer()
    {
        return ComponentAccessor.getOSGiComponentInstanceOfType(SoyTemplateRenderer.class);
    }
}
