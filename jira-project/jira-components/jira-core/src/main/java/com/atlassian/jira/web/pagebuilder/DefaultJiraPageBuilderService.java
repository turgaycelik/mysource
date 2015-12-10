package com.atlassian.jira.web.pagebuilder;

import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.web.HttpRequestLocal;
import com.atlassian.plugin.webresource.WebResourceIntegration;
import com.atlassian.webresource.api.assembler.WebResourceAssemblerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Creates PageBuilder and PageBuilderSpi instances and caches them per-request.
 *
 * TODO: this will be merged with its superclass once the PageBuilder interface has been tested in JIRA
 *
 * @since v6.1
 */
public class DefaultJiraPageBuilderService extends com.atlassian.plugin.webresource.assembler.DefaultPageBuilderService implements JiraPageBuilderService, PageBuilderServiceSpi
{
    private final ApplicationProperties applicationProperties;
    private final FeatureManager featureManager;
    private final HttpRequestLocal<DefaultPageBuilder> pageBuilderRequestLocal = new HttpRequestLocal<DefaultPageBuilder>("page.builder");

    public DefaultJiraPageBuilderService(ApplicationProperties applicationProperties,
            WebResourceIntegration webResourceIntegration, WebResourceAssemblerFactory webResourceAssemblerFactory,
            FeatureManager featureManager)
    {
        super(webResourceIntegration, webResourceAssemblerFactory);
        this.applicationProperties = applicationProperties;
        this.featureManager = featureManager;
    }

    @Override
    public void initForRequest(final HttpServletRequest request, final HttpServletResponse response,
            final DecoratorListener decoratorListener, final ServletContext servletContext)
    {
        if (null == pageBuilderRequestLocal.get())
        {
            pageBuilderRequestLocal.set(
                    new DefaultPageBuilder(applicationProperties, request, response, decoratorListener, servletContext, featureManager));
        }
    }

    @Override
    public void clearForRequest()
    {
        pageBuilderRequestLocal.remove();
    }

    @Override
    public PageBuilder get()
    {
        PageBuilder pageBuilder = pageBuilderRequestLocal.get();
        if (null == pageBuilder)
        {
            throw new IllegalStateException("PageBuilderService.get() called before page builder has been initialised for this request");
        }
        return pageBuilder;
    }

    @Override
    public PageBuilderSpi getSpi()
    {
        PageBuilderSpi pageBuilderSpi = pageBuilderRequestLocal.get();
        if (null == pageBuilderSpi)
        {
            throw new IllegalStateException("PageBuilderServiceSpi.getSpi() called before page builder has been initialised for this request");
        }
        return pageBuilderSpi;
    }
}
