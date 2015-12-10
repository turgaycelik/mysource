package com.atlassian.jira.plugin.customfield;

import com.atlassian.fugue.Option;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nonnull;

public class CustomFieldTypeModuleDescriptorsImpl implements CustomFieldTypeModuleDescriptors
{
    private static final Logger log = Logger.getLogger(CustomFieldTypeModuleDescriptorsImpl.class);

    private PluginAccessor pluginAccessor;

    public CustomFieldTypeModuleDescriptorsImpl(final PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    @Nonnull
    @Override
    public List<CustomFieldType<?, ?>> getCustomFieldTypes()
    {
        final List<CustomFieldType> types = pluginAccessor.getEnabledModulesByClass(CustomFieldType.class);
        List<CustomFieldType<?, ?>> field = Lists.newArrayListWithExpectedSize(types.size());
        for (Object type : types)
        {
            field.add((CustomFieldType<?, ?>) type);
        }
        Collections.sort(field, byName());
        return field;
    }

    private Comparator<? super CustomFieldType<?, ?>> byName()
    {
        return new Comparator<CustomFieldType<?, ?>>()
        {
            private final Ordering<String> ordering = Ordering.natural().nullsFirst();

            public int compare(CustomFieldType<?, ?> o1, CustomFieldType<?, ?> o2)
            {
                return ordering.compare(o1.getName(), o2.getName());
            }
        };

    }

    @Override
    public Option<CustomFieldType> getCustomFieldType(final String completeModuleKey)
    {
        final ModuleDescriptor<?> module = pluginAccessor.getEnabledPluginModule(completeModuleKey);

        if (module != null && module instanceof CustomFieldTypeModuleDescriptor)
        {
            return Option.some((CustomFieldType) module.getModule());
        }

        // JRA-28134: this is logged at the debug level for the following reason:
        // custom field types provided by plugins might come and go as part of normal JIRA
        // operation due to the dynamic nature of OSGi, so JIRA should really handle this scenario as something normal
        if (log.isDebugEnabled())
        {
            log.debug("Could not load custom field type plugin with key '" + completeModuleKey + "'. Is the plugin present and enabled?");
        }

        return Option.none();
    }
}
