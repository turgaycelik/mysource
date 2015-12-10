package com.atlassian.jira.plugin.navigation;

import com.atlassian.jira.ajsmeta.HtmlMetadataManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutManager;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.plugin.webfragment.DefaultWebFragmentContext;
import com.atlassian.jira.web.filters.accesslog.AccessLogImprinter;
import com.atlassian.jira.web.pagebuilder.DecoratablePage;
import com.atlassian.jira.web.util.ProductVersionDataBeanProvider;
import com.atlassian.ozymandias.PluginPointVisitor;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.model.WebPanel;
import com.atlassian.plugin.webresource.WebResourceUrlProvider;
import com.atlassian.webresource.api.UrlMode;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.opensymphony.module.sitemesh.Page;
import com.opensymphony.util.TextUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import static com.atlassian.jira.component.ComponentAccessor.getComponent;

/**
 * This is called by the templating system to render most things header and footer.  Originally this was in the
 * header.jsp in its entirety but we moved it in here because Java code belongs in .java files.   Sure its a ghetto but
 * its a ghetto in the right place.
 *
 * @since v6.0
 */
public class HeaderFooterRendering
{
    public static final String META_X_UA_COMPATIBLE = "meta.x.ua.compatible";

    private static final Logger log = Logger.getLogger(HeaderFooterRendering.class);
    private final ApplicationProperties applicationProperties;
    private final PluginAccessor pluginAccessor;
    private final PageBuilderService pageBuilderService;
    private final WebInterfaceManager webInterfaceManager;
    private final ProductVersionDataBeanProvider productVersionDataBeanProvider;
    private final WebResourceUrlProvider webResourceUrlProvider;

    public HeaderFooterRendering(final ApplicationProperties applicationProperties, final PluginAccessor pluginAccessor,
            final PageBuilderService pageBuilderService, final WebInterfaceManager webInterfaceManager,
            final ProductVersionDataBeanProvider productVersionDataBeanProvider,
            final WebResourceUrlProvider webResourceUrlProvider)
    {
        this.applicationProperties = applicationProperties;
        this.pluginAccessor = pluginAccessor;
        this.pageBuilderService = pageBuilderService;
        this.webInterfaceManager = webInterfaceManager;
        this.productVersionDataBeanProvider = productVersionDataBeanProvider;
        this.webResourceUrlProvider = webResourceUrlProvider;
    }

    /**
     * Called to include the standard global web resources that jira used on most pages.
     * <p/>
     * This is called from head-resources.jsp
     *
     * @param out the JSP to write to
     */
    public void includeHeadResources(JspWriter out)
    {
        pageBuilderService.assembler().resources()
            .requireWebResource("jira.webresources:global-static")
            .requireWebResource("jira.webresources:key-commands")
            .requireWebResource("jira.webresources:header")
            .requireContext("atl.global")
            .requireContext("jira.global");

        pageBuilderService.assembler().assembled().drainIncludedResources().writeHtmlTags(out, UrlMode.RELATIVE);

    }

    /**
     * Called from header.jsp to render top navigation plugin point
     *
     * @param out the JSP to write to
     * @param httpServletRequest the request in play
     * @param page the SiteMesh original page
     */
    public void includeTopNavigation(final JspWriter out, final HttpServletRequest httpServletRequest, final Page page)
    {
        SafePluginPointAccess.to().runnable(new Runnable()
        {
            @Override
            public void run()
            {
                includeTopNavigation(out, httpServletRequest, page.getProperty("page.section"), getWebFragmentContext("atl.header"));
            }
        });
    }

    /**
     * Called from header-nodecorator.jsp to render top navigation plugin point
     *
     * @param out the JSP to write to
     * @param httpServletRequest the request in play
     * @param parsedBody the Parsed decorated body
     */
    public void includeTopNavigation(final JspWriter out, final HttpServletRequest httpServletRequest, final DecoratablePage.ParsedBody parsedBody)
    {
        SafePluginPointAccess.to().runnable(new Runnable()
        {
            @Override
            public void run()
            {
                includeTopNavigation(out, httpServletRequest, parsedBody.getPageProperty("page.section"), getWebFragmentContext("atl.header"));
            }
        });
    }

    public void includeFooters(final JspWriter out, final HttpServletRequest httpServletRequest) {
        SafePluginPointAccess.to().runnable(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    includeFootersOnPage(out, httpServletRequest);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void includeWebPanels(final JspWriter out, final String location) {
        SafePluginPointAccess.to().runnable(new Runnable()
        {
            @Override
            public void run()
            {
                includeWebPanelsOnPage(out, location);
            }
        });
    }

    /**
     * Called to to get the title of the web page
     *
     * @param originalPage the decorated sitemesh page
     * @return the title to use
     */
    public String getPageTitle(Page originalPage)
    {
        return getPageTitle(originalPage.getTitle());
    }

    /**
     * Called to to get the title of the web page
     *
     * @param parsedHead The parsed decorated page
     * @return the title to use
     */
    public String getPageTitle(DecoratablePage.ParsedHead parsedHead)
    {
        return getPageTitle(parsedHead.getTitle());
    }

    /**
     * Called to get the ua.compatible value
     *
     * @param originalPage the decorated sitemesh page
     * @return the ua.compatible value
     */
    public String getXUACompatible(Page originalPage)
    {
        String xUaCompatible = originalPage.getProperty(META_X_UA_COMPATIBLE);
        if (xUaCompatible == null)
        {
            xUaCompatible = "IE=Edge";
        }
        return xUaCompatible;
    }

    /**
     * @return returns the relative resource prefix to use
     */
    public String getRelativeResourcePrefix()
    {
        final String result = SafePluginPointAccess.call(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return webResourceUrlProvider.getStaticResourcePrefix(com.atlassian.plugin.webresource.UrlMode.RELATIVE);
            }
        }).getOrNull();


        return (result != null) ? result : StringUtils.EMPTY;
    }

    /**
     * Includes the version meta data for JIRA in the page
     *
     * @param out the JSP writer
     */
    public void includeVersionMetaTags(final JspWriter out)
    {
        SafePluginPointAccess.to().runnable(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    out.write(productVersionDataBeanProvider.get().getMetaTags());
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Includes the general meta data for JIRA in the page
     *
     * @param out the JSP writer
     */
    public void includeMetadata(JspWriter out) throws IOException
    {
        final HtmlMetadataManager htmlMetadataManager = getComponent(HtmlMetadataManager.class);
        // in the bootstrap mode case there is no HtmlMetadataManager
        if (htmlMetadataManager != null)
        {
            // writes the <meta> tags into the page head
            htmlMetadataManager.includeMetadata(out);
        }
    }

    /**
     * Returns the keyboard shortcut script
     *
     * @param httpServletRequest the request in play
     * @return the keyboard shortcut script
     */
    public String getKeyboardShortCutScript(final HttpServletRequest httpServletRequest)
    {
        final KeyboardShortcutManager keyboardShortcutManager = getComponent(KeyboardShortcutManager.class);
        // in the bootstrap mode case there is no HtmlMetadataManager

        if (keyboardShortcutManager != null)
        {
            final String result = SafePluginPointAccess.call(new Callable<String>()
            {
                @Override
                public String call() throws Exception
                {
                    return httpServletRequest.getContextPath() + TextUtils.htmlEncode(keyboardShortcutManager.includeShortcuts());
                }
            }).getOrNull();
            return result;
        }

        return StringUtils.EMPTY;
    }


    /**
     * Renders top navigation plugin point
     *
     * This code must be especially defensive against the plugin system, as failures here can result in blank pages being rendered.
     *
     * @param out the JSP to write to
     * @param httpServletRequest the request in play
     * @param selectedSection the selected section
     */
    protected void includeTopNavigation(final JspWriter out, final HttpServletRequest httpServletRequest, final String selectedSection, final Map<String, Object> webFragmentContext)
    {
        // Get all the top nav module descriptions and sort them by order
        final List<TopNavigationModuleDescriptor> topNavPlugins = Lists.newArrayList(pluginAccessor.getEnabledModuleDescriptorsByClass(TopNavigationModuleDescriptor.class));
        Collections.sort(topNavPlugins, ModuleDescriptorComparator.COMPARATOR);

        if (StringUtils.isNotBlank(selectedSection))
        {
            httpServletRequest.setAttribute("jira.selected.section", selectedSection);
        }

        SafePluginPointAccess.to().descriptors(topNavPlugins, new PluginPointVisitor<TopNavigationModuleDescriptor,PluggableTopNavigation>()
        {

            @Override
            public void visit(final TopNavigationModuleDescriptor topNavigation, final PluggableTopNavigation pluggableTopNavigation)
            {
                try
                {
                    if (topNavigation.getCondition() == null || !topNavigation.getCondition().shouldDisplay(webFragmentContext)) {
                        return;
                    } else {
                        out.write(pluggableTopNavigation.getHtml(httpServletRequest));
                    }
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Called from footer.jsp to render to render footer plugin point
     *
     * @param out the JSP to write to
     * @param httpServletRequest the request in play
     */
    private void includeFootersOnPage(final JspWriter out, final HttpServletRequest httpServletRequest) throws IOException
    {
        final List<FooterModuleDescriptor> footerPlugins = Lists.newArrayList(pluginAccessor.getEnabledModuleDescriptorsByClass(FooterModuleDescriptor.class));
        Collections.sort(footerPlugins, ModuleDescriptorComparator.COMPARATOR);

        SafePluginPointAccess.to().descriptors(footerPlugins, new PluginPointVisitor<FooterModuleDescriptor, PluggableFooter>()
        {
            @Override
            public void visit(final FooterModuleDescriptor footerModuleDescriptor, final PluggableFooter pluggableFooter)
            {
                try
                {
                    out.write(pluggableFooter.getFullFooterHtml(httpServletRequest));
                }
                catch (IOException e)
                {
                    log.error(String.format("Unable to include web panel in context '%s' from class '%s' because of '%s'", footerModuleDescriptor.getCompleteKey(), pluggableFooter.getClass().getCanonicalName(), e.getMessage()), e);
                }
            }
        });

        final AccessLogImprinter imprinter = new AccessLogImprinter(httpServletRequest);
        out.print(imprinter.imprintHiddenHtml());
        out.print(imprinter.imprintHTMLComment());
    }

    /**
     * Called to include web panels of a named location into the JSP page
     *
     * @param out the JSP to write to
     * @param location the named location of the web panel like alt.general
     */
    private void includeWebPanelsOnPage(final JspWriter out, final String location)
    {
        final Map<String, Object> context = getWebFragmentContext(location);
        final List<WebPanel> displayableWebPanels = webInterfaceManager.getDisplayableWebPanels(location, context);

        for (WebPanel webPanel : displayableWebPanels)
        {
            try
            {
                webPanel.writeHtml(out, context);
            }
            catch (IOException e)
            {
                log.error(String.format("Unable to include web panel in context '%s' from class '%s' because of '%s'", location, webPanel.getClass().getCanonicalName(), e.getMessage()), e);
            }
        }
    }

    /**
     * Called to to get the title of the web page
     *
     * @param pageTitle the title from the decorated page
     * @return the title to use
     */
    private String getPageTitle(final String pageTitle)
    {
        final String appTitle = TextUtils.htmlEncode(applicationProperties.getDefaultBackedString(APKeys.JIRA_TITLE));
        final String actualTitle;
        if (pageTitle == null)
        {
            actualTitle = appTitle;
        }
        else if (pageTitle.equals(appTitle))
        {
            actualTitle = pageTitle;
        }
        else
        {
            actualTitle = pageTitle + " - " + appTitle;
        }
        return actualTitle;
    }

    @VisibleForTesting
    Map<String, Object> getWebFragmentContext(final String location)
    {
        return DefaultWebFragmentContext.get(location);
    }

}
