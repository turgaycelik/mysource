package com.atlassian.jira.issue.search.searchers;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.elements.ResourceLocation;
import org.dom4j.Element;
import webwork.action.Action;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Simple implementation of {@link com.atlassian.jira.issue.customfields.CustomFieldSearcher}.
 *
 * @since v4.0
 */
public class MockCustomFieldSearcher extends MockIssueSearcher<CustomField> implements CustomFieldSearcher
{
    private CustomFieldSearcherModuleDescriptor descriptor;

    public MockCustomFieldSearcher(final String id)
    {
        super(id);
        this.descriptor = new Descriptor();
    }

    public void init(final CustomFieldSearcherModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public CustomFieldSearcherModuleDescriptor getDescriptor()
    {
        return descriptor;
    }

    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString()
    {
        return String.format("Mock Custom Issue Searcher[%s]", getId());
    }

    private class Descriptor implements CustomFieldSearcherModuleDescriptor
    {
        @Override
        public String getSearchHtml(final CustomField customField, final CustomFieldValueProvider provider, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action, final Map velocityParams)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getViewHtml(final CustomField customField, final CustomFieldValueProvider provider, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final Map displayParameters, final Action action, final Map velocityParams)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getViewHtml(final CustomField field, final Object value)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getStatHtml(final CustomField field, final Object value, final String urlPrefix)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Set getValidCustomFieldKeys()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public I18nHelper getI18nBean()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getHtml(final String resourceName)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getHtml(final String resourceName, final Map<String, ?> startingParams)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void writeHtml(final String resourceName, final Map<String, ?> startingParams, final Writer writer)
                throws IOException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getCompleteKey()
        {
            return MockCustomFieldSearcher.this.getId();
        }

        @Override
        public String getPluginKey()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getKey()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getName()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getDescription()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Class<CustomFieldSearcher> getModuleClass()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public CustomFieldSearcher getModule()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void init(@Nonnull final Plugin plugin, @Nonnull final Element element) throws PluginParseException
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean isEnabledByDefault()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean isSystemModule()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void destroy(final Plugin plugin)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void destroy() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Float getMinJavaVersion()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean satisfiesMinJavaVersion()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Map<String, String> getParams()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getI18nNameKey()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public String getDescriptionKey()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Plugin getPlugin()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public List<ResourceDescriptor> getResourceDescriptors()
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public List<ResourceDescriptor> getResourceDescriptors(final String type)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public ResourceDescriptor getResourceDescriptor(final String type, final String name)
        {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public ResourceLocation getResourceLocation(final String type, final String name)
        {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
