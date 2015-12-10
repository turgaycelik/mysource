package com.atlassian.jira.template.velocity;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.template.TemplateSource;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.velocity.VelocityManager;
import com.atlassian.velocity.htmlsafe.directive.DefaultDirectiveChecker;
import com.atlassian.velocity.htmlsafe.directive.DirectiveChecker;
import com.atlassian.velocity.htmlsafe.event.referenceinsertion.DisableHtmlEscapingDirectiveHandler;
import com.google.common.collect.Maps;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.event.EventCartridge;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since v5.1
 */
public class DefaultVelocityTemplatingEngine implements VelocityTemplatingEngine
{
    private final VelocityManager velocityManager;
    private final ApplicationProperties applicationProperties;
    private final DirectiveChecker directiveChecker;

    public DefaultVelocityTemplatingEngine(final VelocityManager velocityManager, final ApplicationProperties applicationProperties)
    {
        this(velocityManager, applicationProperties, null);
    }

    public DefaultVelocityTemplatingEngine(final VelocityManager velocityManager, final ApplicationProperties applicationProperties, @Nullable VelocityTemplateCache velocityTemplateCache)
    {
        this.velocityManager = velocityManager;
        this.applicationProperties = applicationProperties;
        this.directiveChecker = velocityTemplateCache == null ? new DefaultDirectiveChecker() : new CachingDirectiveChecker(velocityTemplateCache);
    }

    @Override
    public RenderRequest render(final TemplateSource source)
    {
        return new DefaultRenderRequest(source);
    }

    class DefaultRenderRequest implements RenderRequest
    {
        private final TemplateSource source;
        private VelocityContext context = createContextFrom(Collections.<String, Object>emptyMap());

        public DefaultRenderRequest(final TemplateSource source)
        {
            Assertions.notNull(source);
            this.source = source;
        }

        @Override
        public RenderRequest applying(final Map<String, Object> parameters)
        {
            this.context = createContextFrom(parameters);
            return this;
        }

        @Override
        public RenderRequest applying(final VelocityContext context)
        {
            this.context = context;
            return this;
        }

        // common code for returning String representations via writer methods
        private abstract class StringRepresentation
        {
            @Override
            public String toString()
            {
                try
                {
                    StringWriter sw = new StringWriter();
                    with(sw);
                    return sw.toString();
                }
                catch (IOException e)
                {
                    // cant happen for StringWriter
                    return "";
                }
            }

            abstract void with(final StringWriter sw) throws IOException;
        }

        @Override
        public String asPlainText()
        {
            return new StringRepresentation()
            {
                void with(final StringWriter sw) throws IOException
                {
                    asPlainText(sw);
                }
            }.toString();
        }

        @Override
        public String asHtml()
        {
            return new StringRepresentation()
            {
                void with(final StringWriter sw) throws IOException
                {
                    asHtml(sw);
                }
            }.toString();
        }

        @Override
        public void asPlainText(final Writer writer) throws IOException
        {
            toWriterImpl(writer, false);
        }

        @Override
        public void asHtml(final Writer writer) throws IOException
        {
            toWriterImpl(writer, true);
        }

        private void toWriterImpl(final Writer writer, boolean attachCartridge) throws IOException
        {
            if (source instanceof TemplateSource.File)
            {
                final TemplateSource.File template = (TemplateSource.File) source;
                if (attachCartridge)
                {
                    context.attachEventCartridge(createDefaultCartridge());
                }
                velocityManager.writeEncodedBody(writer, template.getPath(), "", applicationProperties.getEncoding(), context);
            }
            else
            {
                if (source instanceof TemplateSource.Fragment)
                {
                    final TemplateSource.Fragment fragment = (TemplateSource.Fragment) source;
                    if (attachCartridge)
                    {
                        context.attachEventCartridge(createDefaultCartridge());
                    }
                    velocityManager.writeEncodedBodyForContent(writer, fragment.getContent(), context);
                }
            }
        }

        private String getBaseUrl()
        {
            if (ExecutingHttpRequest.get() != null)
            {
                return ExecutingHttpRequest.get().getContextPath();
            }
            return applicationProperties.getString(APKeys.JIRA_BASEURL);
        }

        private VelocityContext createContextFrom(final Map<String, Object> suppliedParameters)
        {
            final Map<String, Object> contextParameters =
                    CompositeMap.of
                            (
                                    suppliedParameters,
                                    Collections.<String, Object>singletonMap("baseurl", getBaseUrl())
                            );

            return new VelocityContext(CompositeMap.of(Maps.<String, Object>newHashMap(), contextParameters));
        }

    }

    private EventCartridge createDefaultCartridge()
    {
        final EventCartridge cartridge = new EventCartridge();

        DisableHtmlEscapingDirectiveHandler handler = new DisableHtmlEscapingDirectiveHandler();
        handler.setDirectiveChecker(directiveChecker);
        cartridge.addEventHandler(handler);
        return cartridge;
    }

    private static class CachingDirectiveChecker implements DirectiveChecker
    {
        private final VelocityTemplateCache velocityTemplateCache;

        public CachingDirectiveChecker(@Nonnull VelocityTemplateCache velocityTemplateCache)
        {
            this.velocityTemplateCache = velocityTemplateCache;
        }

        @Override
        public boolean isPresent(String directiveName, Template template)
        {
            return velocityTemplateCache.isDirectivePresent(directiveName, template);
        }
    }
}
