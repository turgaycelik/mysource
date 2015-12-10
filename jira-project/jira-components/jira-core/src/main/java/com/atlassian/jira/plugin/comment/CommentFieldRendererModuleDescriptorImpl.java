package com.atlassian.jira.plugin.comment;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.atlassian.fugue.Option;
import com.atlassian.jira.template.TemplateSource;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.AbstractWebFragmentModuleDescriptor;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import static com.atlassian.jira.template.TemplateSources.file;

public class CommentFieldRendererModuleDescriptorImpl extends AbstractWebFragmentModuleDescriptor<Void> implements CommentFieldRendererModuleDescriptor
{
    private static final Logger log = Logger.getLogger(CommentFieldRendererModuleDescriptorImpl.class);
    private static final Supplier<Option<String>> emptySupplier = Suppliers.memoize(new Supplier<Option<String>>()
    {
        @Override
        public Option<String> get()
        {
            return Option.none();
        }
    });

    private final VelocityTemplatingEngine velocityTemplatingEngine;
    private final Map<String, Option<ResourceDescriptor>> resources = Maps.newHashMap();

    public CommentFieldRendererModuleDescriptorImpl(ModuleFactory moduleFactory,
            WebInterfaceManager webInterfaceManager,
            VelocityTemplatingEngine velocityTemplatingEngine)
    {
        super(moduleFactory, webInterfaceManager);
        this.velocityTemplatingEngine = velocityTemplatingEngine;
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        initResources(TEMPLATE_NAME_EDIT, TEMPLATE_NAME_VIEW, TEMPLATE_NAME_ISSUE_EDIT, TEMPLATE_NAME_ISSUE_VIEW);
    }

    private void initResources(final String... resourceIds)
    {
        for (String resourceId : resourceIds)
        {
            resources.put(resourceId, Option.option(getResourceDescriptor("velocity", resourceId)));
        }
    }

    @Override
    public Option<String> getFieldEditHtml(final Map<String, Object> context)
    {
        return resources.get(TEMPLATE_NAME_EDIT).fold(emptySupplier, new RenderCommentPanelFunction(context));
    }

    @Override
    public Option<String> getFieldViewHtml(final Map<String, Object> context)
    {
        return resources.get(TEMPLATE_NAME_VIEW).fold(emptySupplier, new RenderCommentPanelFunction(context));
    }

    @Override
    public Option<String> getIssuePageEditHtml(final Map<String, Object> context)
    {
        return resources.get(TEMPLATE_NAME_ISSUE_EDIT).fold(emptySupplier, new RenderCommentPanelFunction(context));
    }

    @Override
    public Option<String> getIssuePageViewHtml(final Map<String, Object> context)
    {
        return resources.get(TEMPLATE_NAME_ISSUE_VIEW).fold(emptySupplier, new RenderCommentPanelFunction(context));
    }

    @Override
    public Void getModule()
    {
        throw new IllegalStateException("Get module on comment field renderer should never be called");
    }

    private class RenderCommentPanelFunction implements Function<ResourceDescriptor, Option<String>>
    {
        private final Map<String, Object> context;

        public RenderCommentPanelFunction(final Map<String, Object> context)
        {
            this.context = context;
        }

        @Override
        public Option<String> apply(final ResourceDescriptor resourceDescriptor)
        {
            Map<String, Object> contextMap = getContextProvider().getContextMap(context);
            return Option.some(getHtml(resourceDescriptor, contextMap));
        }
    }

    private String getHtml(final ResourceDescriptor resourceDescriptor, final Map<String, Object> context)
    {
        try
        {
            final StringWriter sw = new StringWriter();
            final TemplateSource templateSource = file(getCompleteKey() + "/" + resourceDescriptor.getLocation());

            velocityTemplatingEngine.render(templateSource).applying(context).asHtml(sw);

            return sw.toString();
        }
        catch (IOException e)
        {
            log.error("Error when rendering the comment field ", e);
            return StringUtils.EMPTY;
        }
    }
}
